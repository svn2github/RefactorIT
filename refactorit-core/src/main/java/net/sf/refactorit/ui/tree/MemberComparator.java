/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.Comparator;


public final class MemberComparator implements Comparator {
  public static final TypeComparator TYPE = new TypeComparator();
  public static final FieldComparator FIELD = new FieldComparator();
  public static final MethodComparator METHOD = new MethodComparator();

  public final int compare(Object o1, Object o2) {
    if (o1 instanceof String && o2 instanceof String) {
      return ((String) o1).compareTo((String) o2);
    }

    if (o1 instanceof BinPackage && o2 instanceof BinPackage) {
      return ((BinPackage) o1).getQualifiedName()
          .compareTo(((BinPackage) o2).getQualifiedName());
    }

    int res = getWeight(o1) - getWeight(o2); // first by type and modifiers

    if (res == 0) {
      if (o1 instanceof BinMember && ((BinMember) o1).getName() == null) {
        return -1;
      } else if (o2 instanceof BinMember && ((BinMember) o1).getName() == null) {
        return +1;
      }

      if (o1 instanceof BinType && o2 instanceof BinType) {
        res = ((BinType) o1).getName().compareTo(((BinType) o2).getName());
      } else if (o1 instanceof BinTypeRef && o2 instanceof BinTypeRef) {
        res = TYPE.compare(o1, o2);
      } else if (o1 instanceof BinMethod && o2 instanceof BinMethod) {
        res = METHOD.compare(o1, o2);
      } else if (o1 instanceof BinField && o2 instanceof BinField) {
        res = FIELD.compare(o1, o2);
      }
    }

    return res;
  }

  private static int getWeight(Object o) {
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
      weight += ((BinMember) o).getModifiers();
    }

    return weight;
  }

  private static final class TypeComparator implements Comparator {
    /**
     * @see Comparator#compare(Object, Object)
     */
    public final int compare(Object o1, Object o2) {
      BinTypeRef r1 = (BinTypeRef) o1;
      BinTypeRef r2 = (BinTypeRef) o2;

      return r1.getName().compareTo(r2.getName());
    }
  }


  private static final class FieldComparator implements Comparator {
    /**
     * @see Comparator#compare(Object, Object)
     */
    public final int compare(Object o1, Object o2) {
      BinField f1 = (BinField) o1;
      BinField f2 = (BinField) o2;

      return f1.getName().compareTo(f2.getName());
    }
  }


  private static final class MethodComparator implements Comparator {
    /**
     * @see Comparator#compare(Object, Object)
     */
    public final int compare(Object o1, Object o2) {
      int cmp = ((BinMember) o1).getName().compareTo(
          ((BinMember) o2).getName());
      if (cmp != 0) {
        return cmp;
      }

      if (!(o1 instanceof BinMethod) || !(o2 instanceof BinMethod)) {
        return cmp;
      }

      BinMethod m1 = (BinMethod) o1;
      BinMethod m2 = (BinMethod) o2;

      BinParameter[] p1 = m1.getParameters();
      BinParameter[] p2 = m2.getParameters();
      int len = Math.min(p1.length, p2.length);
      for (int i = 0; i < len; i++) {
        int d1 = 0, d2 = 0;

        BinTypeRef r1 = p1[i].getTypeRef();
        if (r1.isArray()) {
          BinArrayType at = (BinArrayType) r1.getBinType();
          d1 = at.getDimensions();
          r1 = at.getArrayType();
        }

        BinTypeRef r2 = p2[i].getTypeRef();
        if (r2.isArray()) {
          BinArrayType at = (BinArrayType) r2.getBinType();
          d2 = at.getDimensions();
          r2 = at.getArrayType();
        }

        cmp = r1.getName().compareTo(r2.getName());
        if (cmp != 0) {
          return cmp;
        }

        if (d1 < d2) {
          return -1;
        }
        if (d1 > d2) {
          return 1;
        }
      }

      if (p1.length < p2.length) {
        return -1;
      }
      if (p1.length > p2.length) {
        return 1;
      }

      return 0;
    }
  }
}
