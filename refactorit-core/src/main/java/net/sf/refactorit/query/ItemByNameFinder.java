/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper methods to find BinXXX nodes in class tree by names.
 *
 * @author Igor Malinin
 * @author Anton Safonov
 * @author Sander Magi
 */
public final class ItemByNameFinder {
  public static BinPackage findBinPackage(Project project, String name) {
    if (project == null || name == null) {
      return null;
    }

    BinPackage result = project.getPackageForName(name);
    // if no such package - i.e was asked for package 'net' that does not exist
    // (but package net.sf exists) will create a fake one
    if (result == null) {
      result = project.createPackageForName(name);
    }

    return result;
  }

  public static BinCIType findBinCIType(Project project, String name) {
    if (name == null) {
      return null;
    }

    BinTypeRef ref = project.findTypeRefForName(name);

    if (ref == null) {
      return null;
    }
    if (ref.isReferenceType()) {
      return ref.getBinCIType();
    }

    return null;
  }

  public static BinField findBinField(BinCIType type, String name) {
    BinField[] fields = type.getDeclaredFields();
    for (int i = 0, len = fields.length; i < len; i++) {
      BinField bin = fields[i];
      if (bin.getName().equals(name)) {
        return bin;
      }
    }

    return null;
  }

  /**
   * @param type BinCIType
   * @param name String
   * @param args String[] parameter type names, seems both short and qualified
   * @return BinMethod
   */
  public static BinMethod findBinMethod(BinCIType type, String name,
      String[] args) {
    BinMethod bin = findBinMethod(type, name, args, true);
    if (bin != null) {
      return bin;
    }

    // revert to non-qualified names
    return findBinMethod(type, name, args, false);
  }

  private static BinMethod findBinMethod(BinCIType type, String name,
      String[] args, boolean qualified) {
    BinMethod[] methods = type.getDeclaredMethods();
    for (int i = 0, len = methods.length; i < len; i++) {
      BinMethod bin = methods[i];
      if (bin.isSynthetic()) {
        continue;
      }

      if (!bin.getName().equals(name)) {
        continue;
      }

      if (args == null || equals(bin.getParameters(), args, qualified)) {
        return bin;
      }
    }

    return null;
  }

  public static BinConstructor findBinConstructor(BinClass type, String[] args) {
    BinConstructor bin = findBinConstructor(type, args, true);
    if (bin != null) {
      return bin;
    }

    // revert to non-qualified names
    return findBinConstructor(type, args, false);
  }

  public static BinLocalVariable findLocalVariable(BinItemVisitable v,
      final String name) {
    final BinLocalVariable result[] = new BinLocalVariable[] {null};
    v.accept(new BinItemVisitor(true) {
      public void visit(BinLocalVariable x) {
        if (name.equals(x.getName())) {
          result[0] = x;
        } else {
          super.visit(x);
        }
      }
    });
    return result[0];
  }

  private static BinConstructor findBinConstructor(BinClass type, String[] args,
      boolean qualified) {
    BinConstructor[] constructors = type.getDeclaredConstructors();
    for (int i = 0, len = constructors.length; i < len; i++) {
      BinConstructor bin = constructors[i];

      if (equals(bin.getParameters(), args, qualified)) {
        return bin;
      }
    }

    return null;
  }

  private static boolean equals(BinParameter[] params, String[] args,
      boolean qualified) {
    int len = params.length;

    if (args.length != len) {
      return false;
    }

    for (int i = 0; i < len; i++) {
      final BinTypeRef ref = params[i].getTypeRef();

      String arg;
      if (ref.isArray()) {
        arg = ((BinArrayType) ref.getBinType()).getArrayType().getQualifiedName()
            + ((BinArrayType) ref.getBinType()).getDimensionString();
      } else {
        arg = ref.getQualifiedName();
      }

      if (qualified) {
        if (!arg.equals(args[i])) {
          return false;
        }
      } else {
        if (!arg.endsWith(args[i])) {
          return false;
        }

        int pos = arg.length() - args[i].length();
        if (pos > 0) {
          if (arg.charAt(pos - 1) != '.' && arg.charAt(pos - 1) != '$') {
            return false;
          }
        }
      }
    }

    return true;
  }

  public static BinVariable findVariable(BinItemVisitable visitable,
      String name) {
    List vars = findVariables(visitable, name);
    if (vars.size() == 0) {
      return null;
    } else {
      return (BinVariable) vars.get(0);
    }
  }

  public static List findVariables(BinItemVisitable visitable,
      final String name) {
    final ArrayList result = new ArrayList(2);

    visitable.accept(new BinItemVisitor() {
      public void visit(BinField x) {
        check(x);

        super.visit(x);
      }

      public void visit(BinLocalVariable x) {
        check(x);

        super.visit(x);
      }

      private void check(BinVariable x) {
        if (x.getName().equals(name)) {
          result.add(x);
        }
      }
    });

    return result;
  }

}
