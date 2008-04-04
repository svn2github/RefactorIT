/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.CompilationUnit;

import java.util.Comparator;


/**
 * Compares correctly BinXXX types, usable in sorted trees
 *
 * @author Anton Safonov
 * @author Risto
 */
public class BinComparator implements Comparator {
  private static final Comparator instance = new BinComparator();

  public static Comparator getInstance() {
    return instance;
  }

  protected BinComparator() {}

  public int compare(Object o1, Object o2) {
    if (o1 instanceof BinTreeTableNode && o2 instanceof BinTreeTableNode) {
      o1 = ((BinTreeTableNode) o1).getBin();
      o2 = ((BinTreeTableNode) o2).getBin();
    }

    //System.err.println("Compare: " + o1 + " - " + o2);
    int res = 0;
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return +1;
    }

    if (o1 instanceof String && o2 instanceof String) {
      res = ((String) o1).compareTo((String) o2);
      return res;
    }

    if (o1 instanceof CompilationUnit && o2 instanceof CompilationUnit) {
      res = ((CompilationUnit) o1).getName().compareTo(((CompilationUnit) o2).getName());
      return res;
    }

    if (o1 instanceof BinCIType && o2 instanceof CompilationUnit) {
      String sourceName = ((CompilationUnit) o2).getName();
      if (sourceName.endsWith(".java")) {
        sourceName = sourceName.substring(0,
            sourceName.length() - ".java".length());
      }
      res = ((BinCIType) o1).getNameWithAllOwners().compareTo(sourceName);
      //System.err.println("SourceName1: " + sourceName + " - " + o1 + " = " + res);
      /*if (res == 0) {
        res = +2;
             }*/
      return res;
    }

    if (o1 instanceof CompilationUnit && o2 instanceof BinCIType) {
      String sourceName = ((CompilationUnit) o1).getSource().getName();
      if (sourceName.endsWith(".java")) {
        sourceName = sourceName.substring(0,
            sourceName.length() - ".java".length());
      }
      res = sourceName.compareTo(((BinCIType) o2).getName());
      //System.err.println("SourceName2: " + sourceName);
      /*if (res == 0) {
        res = -2;
             }*/
      return res;
    }

    if (o1 instanceof BinPackage && !(o2 instanceof BinPackage)) {
      return -1;
    }

    if (o2 instanceof BinPackage && !(o1 instanceof BinPackage)) {
      return +1;
    }

    // FIXME: the following comparisons are not proved for bugs
    if (o1 instanceof BinPackage && o2 instanceof BinPackage) {
      res = ((BinPackage) o1).getQualifiedName().compareTo(((BinPackage) o2).
          getQualifiedName());
      return res;
    }

    res = getWeight(o1) - getWeight(o2); // first by type and modifiers

    if (res == 0) {
      // equal in general, so by name then
      // FIXME: BinCIType inherits BinMember
      if (o1 instanceof BinCIType && o2 instanceof BinCIType) {
        // NOTE: they are already grouped by package so simple name matters
        res = ((BinCIType) o1).getName().compareTo(
            ((BinCIType) o2).getName());
      } else if (o1 instanceof BinMember && o2 instanceof BinMember) {
        final BinMember member1 = (BinMember) o1;
        final BinMember member2 = (BinMember) o2;

        // If the members are from a different package, let's sort them according to their package first
        // (For example, this is needed for module 'CallStack')
        final BinPackage package1 = member1.getOwner().getBinCIType().
            getPackage();
        final BinPackage package2 = member2.getOwner().getBinCIType().
            getPackage();

        if (!package1.isIdentical(package2)) {
          return package1.getQualifiedName().compareTo(package2.
              getQualifiedName());
        }

        // Then let's compare according to types.
        final String typeName1 = member1.getOwner().getQualifiedName();
        final String typeName2 = member2.getOwner().getQualifiedName();

        if (!typeName1.equals(typeName2)) {
          return typeName1.compareTo(typeName2);
        }

        // we are not interested in qualified name if members are in one type
        if (member1.getName() == null) {
          res = -1;
        } else if (member2.getName() == null) {
          res = +1;
        } else {
          res = member1.getName().compareTo(member2.getName());
        }
        if (res == 0 && o1 instanceof BinMethod && o2 instanceof BinMethod) {
          // then by return type
          res = ((BinMethod) o1).getReturnType().getQualifiedName().compareTo(
              ((BinMethod) o2).getReturnType().getQualifiedName());
          if (res == 0) {
            // and finally by parameter types
            res = toString(((BinMethod) o1).getParameters()).compareTo(
                toString(((BinMethod) o2).getParameters()));
          }
        }
      }
    }

    //System.err.println("Result: " + res);
    return res;
  }

  public static final int getWeight(final Object o) {
    int weight = 0x1000;
    if (o instanceof BinMember) {
      weight += 0x1000;
      if (o instanceof BinField) {
        weight += 0x1000;
      } else if (o instanceof BinInitializer) {
        weight += 0x2000;
      } else if (o instanceof BinConstructor) {
        weight += 0x3000;
      } else if (o instanceof BinMethod) {
        weight += 0x4000;
      }

      // NOTE: intentionally removed comparing by modifiers
      // weight += ((BinMember)o).getModifiers();
    }

    return weight;
  }

  public static final String toString(final BinParameter[] parameters) {
    final StringBuffer result = new StringBuffer(256);
    final int max = parameters.length;
    if (max > 0) {
      result.append(parameters[0].getTypeRef().getQualifiedName());
    }
    for (int pos = 1; pos < max; pos++) {
      result.append(',');
      result.append(' ');
      result.append(parameters[pos].getTypeRef().getQualifiedName());
    }

    return result.toString();
  }
}
