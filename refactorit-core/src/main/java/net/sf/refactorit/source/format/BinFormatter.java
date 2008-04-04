/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.parser.FastJavaLexer;


/**
 * Helper methods to format BinXXX nodes to strings.
 *
 * @author Igor Malinin
 * @author Anton Safonov
 */
public class BinFormatter {
  public static String formatLocationAware(LocationAware lw) {
    return lw.toString() + " [" + lw.getStartLine() + "," + lw.getStartColumn()
        + "]-[" + lw.getEndLine() + "," + lw.getEndColumn() + "]";
  }

  public static String formatLocationAwareStartLine(LocationAware lw) {
    return lw.getCompilationUnit().getDisplayPath() + ":" + lw.getStartLine();
  }

  public static String format(Object bin) {
    if (bin instanceof BinType) {
      return format(((BinType) bin).getTypeRef());
    } else if (bin instanceof BinTypeRef) {
      return format((BinTypeRef) bin);
    } else if (bin instanceof BinVariable) {
      return format((BinVariable) bin);
    } else if (bin instanceof BinMethod) {
      return format((BinMethod) bin);
    } else if (bin instanceof BinPackage) {
      return ((BinPackage) bin).getQualifiedDisplayName();
    } else if (bin instanceof String) {
      return (String) bin;
    }
    return "<unknown binary>";
  }

  public static String formatQualified(Object bin) {
    if (bin instanceof BinType) {
      return formatQualified(((BinType) bin).getTypeRef());
    } else if (bin instanceof BinTypeRef) {
      return formatQualified((BinTypeRef) bin);
    } else if (bin instanceof BinVariable) {
      return formatQualified((BinVariable) bin);
    } else if (bin instanceof BinMethod) {
      return formatQualified((BinMethod) bin);
    } else if (bin instanceof BinPackage) {
      // NOTE DisplayName here will break DependenciesModel for dependencies on default package
      return ((BinPackage) bin).getQualifiedName();
    } else if (bin instanceof String) {
      return (String) bin;
    }
    return "<unknown binary>";
  }

  public static String format(BinTypeRef bin) {
    return formatNotQualified(new StringBuffer(64), bin, false).toString();
  }

  public static String formatWithAllOwners(BinTypeRef bin) {
    return formatNotQualified(new StringBuffer(64), bin, true).toString();
  }

  public static String formatNotQualified(BinTypeRef bin) {
    return formatNotQualified(new StringBuffer(64), bin, false).toString();
  }

  public static String formatQualified(BinTypeRef bin) {
    return formatQualified(new StringBuffer(64), bin).toString();
  }

  public static String formatQualified(BinVariable bin) {
    return formatQualified(new StringBuffer(64),
        bin.getOwner()).toString() + '.' + format(bin);
  }

  public static String formatQualified(BinMethod bin) {
    return formatQualified(new StringBuffer(64),
        bin.getOwner()).toString() + '.' + format(bin);
  }

  public static String format(BinVariable bin) {
    return formatWithoutType(new StringBuffer(64), bin).toString();
  }

  public static String formatWithType(BinVariable bin) {
    return formatWithType(new StringBuffer(64), bin).toString();
  }

  public static String formatWithoutType(BinVariable bin) {
    return formatWithoutType(new StringBuffer(64), bin).toString();
  }

  public static String format(BinMethod bin) {
    return formatWithoutReturnType(new StringBuffer(64), bin).toString();
  }

  public static String formatWithoutReturn(BinMethod bin) {
    return formatWithoutReturnType(new StringBuffer(64), bin).toString();
  }

  public static String formatWithReturn(BinMethod bin) {
    return formatWithReturnType(new StringBuffer(64), bin).toString();
  }

//
// Implementation
//

  private static StringBuffer formatNotQualified(
      StringBuffer buf, final BinTypeRef ref, boolean nameAllOwners) {
    if (ref.isArray()) {
      BinArrayType type = (BinArrayType) ref.getBinType();
      buf.append(
          nameAllOwners ? type.getArrayType().getBinType().getNameWithAllOwners()
          : type.getArrayType().getName());
      if(ref.isArity()) {
        buf.append(type.getVariableArityString());
      } else {
        buf.append(type.getDimensionString());
      }
    } else {
      buf.append(
          nameAllOwners ? ref.getBinType().getNameWithAllOwners()
          : ref.getName());
    }

    formatTypeArguments(buf, ref);

    return buf;
  }

  private static StringBuffer formatQualified(StringBuffer buf, final BinTypeRef ref) {
    if (ref == null) {
      buf.append("null");
      return buf;
    }
    if (ref.isArray()) {
      BinArrayType type = (BinArrayType) ref.getBinType();
      buf.append(type.getArrayType().getQualifiedName());
      if(ref.isArity()) {
        buf.append(type.getVariableArityString());
      } else {
        buf.append(type.getDimensionString());
      }
    } else {
      buf.append(ref.getQualifiedName());
    }

    formatTypeArguments(buf, ref);

    return buf;
  }

  public static void formatTypeArguments(final StringBuffer buf, BinTypeRef parameterized) {
    formatTypeArguments(buf, parameterized, parameterized.getTypeArguments(), false);
  }

  public static void formatTypeArguments(final StringBuffer buf,
      final BinTypeRef[] args, final boolean printTypeParams) {
    formatTypeArguments(buf, null, args, printTypeParams);
  }

  public static void formatTypeArguments(final StringBuffer buf,
      final BinTypeRef parameterized,
      final BinTypeRef[] args, final boolean printTypeParams) {
    if (FastJavaLexer.getActualJvmMode() != FastJavaLexer.JVM_50) {
      return;
    }

    if (args == null || args.length == 0) {
      return;
    }

    // JAVA5: must be improved, there are cases when type parameters are used as arguments for other type
    if (!printTypeParams) {
      for (int i = 0, max = args.length; i < max; i++) {
        BinTypeRef arg = args[i];
        if ("?".equals(arg.getName())) {
          return; // can't print wildcard
        }

        if (arg.getBinType().isTypeParameter()) {
          // try to resolve in the given context
          if (parameterized != null && parameterized.isSpecific()) {
            arg = ((BinSpecificTypeRef) parameterized).findTypeArgumentByParameter(arg);
//System.err.println("new arg: " + arg + " --- " + parameterized);
          }
          // FIXME: it might resolve type param into another type param, must be recursive

          if (arg == null || arg.getBinType().isTypeParameter()) {
            return; // can't print anything
          } else {
            args[i] = arg;
          }
        }
      }
    }

    boolean opened = false;
    for (int i = 0, max = args.length; i < max; i++) {
      BinTypeRef arg = args[i];

      if (!opened) {
        buf.append("<");
        opened = true;
      } else {
        buf.append(", ");
      }
      // FIXME: this will definitely produce errors
      formatNotQualified(buf, arg, false);
    }
    if (opened) {
      buf.append(">");
    }
  }	

  private static StringBuffer formatWithType(StringBuffer buf, BinVariable bin) {
    return formatNotQualified(buf, bin.getTypeRef(), true)
        .append(' ').append(bin.getName());
  }

  private static StringBuffer formatWithoutType(
      StringBuffer buf, BinVariable bin) {
    return buf.append(bin.getName());
  }

  private static StringBuffer formatWithReturnType(
      StringBuffer buf, BinMethod bin) {
    if (!(bin instanceof BinConstructor)) {
      formatNotQualified(buf, bin.getReturnType(), false).append(' ');
    }

    return formatWithoutReturnType(buf, bin);
  }

  private static StringBuffer formatWithoutReturnType(
      StringBuffer buf, BinMethod bin) {
    buf.append(bin.getName()).append('(');

    BinParameter[] parametrs = bin.getParameters();
    if (parametrs.length > 0) {
      for (int i = 0; ; buf.append(", ")) {
        formatNotQualified(buf, parametrs[i].getTypeRef(), false);
        if (++i >= parametrs.length) {
          break;
        }
      }
    }

    buf.append(')');

    return buf;
  }

  public static String formatMethodParameters(BinMethod method) {
  	return formatMethodParameters(method, null);
  }

  public static String formatMethodParameters(BinMethod method, boolean [] useFqn) {
    StringBuffer buffer = new StringBuffer();

    boolean isFirst = true;

    BinParameter[] pars = method.getParameters();
    for (int i = 0; i < pars.length; i++) {

      if (!isFirst) {
        buffer.append(", ");
      } else {
        isFirst = false;
      }
      BinModifierFormatter modifierFormatter = new BinModifierFormatter(pars[i]
          .getModifiers());
      modifierFormatter.needsPostfix(true);
      buffer.append(modifierFormatter.print());
      if ((useFqn != null) && (useFqn[i])) {
      	buffer.append(BinFormatter.formatQualified(pars[i].getTypeRef()));
      } else {
      	buffer.append(BinFormatter.formatNotQualified(pars[i].getTypeRef()));
      }

      buffer.append(" " + pars[i].getName());

    }
    return buffer.toString();
  }

	private static StringBuffer formatWithTypeNestedGenerics(StringBuffer buf, BinVariable bin) {
	  return formatNotQualified(buf, bin.getTypeRef(), true)
	      .append(' ').append(bin.getName());
	}
	
	public static String formatWithTypeNestedGenerics(BinVariable bin) {
	  return formatWithTypeNestedGenerics(new StringBuffer(64), bin).toString();
	}
	
	public static StringBuffer formatNotQualifiedForTypeArgumentsWithAllOwners(
      StringBuffer buf, final BinTypeRef ref, boolean nameAllOwners) {
    if (ref.isArray()) {
      BinArrayType type = (BinArrayType) ref.getBinType();
      buf.append(
          nameAllOwners ? type.getArrayType().getBinType().getNameWithAllOwners()
          : type.getArrayType().getName());
      if(ref.isArity()) {
        buf.append(type.getVariableArityString());
      } else {
        buf.append(type.getDimensionString());
      }
    } else {
      buf.append(
          nameAllOwners ? ref.getBinType().getNameWithAllOwners()
          : ref.getName());
    }

		formatTypeArgumentsWithAllOwners(buf, ref, ref.getTypeArguments(), false);

    return buf;
	}
	
	public static StringBuffer formatQualifiedForTypeArgumentsWithAllOwners(StringBuffer buf, final BinTypeRef ref) {
    if (ref == null) {
      buf.append("null");
      return buf;
    }
    if (ref.isArray()) {
      BinArrayType type = (BinArrayType) ref.getBinType();
      buf.append(type.getArrayType().getQualifiedName());
      if(ref.isArity()) {
        buf.append(type.getVariableArityString());
      } else {
        buf.append(type.getDimensionString());
      }
    } else {
      buf.append(ref.getQualifiedName());
    }

		formatTypeArgumentsWithAllOwners(buf, ref, ref.getTypeArguments(), false);

    return buf;
  }
  
	public static void formatTypeArgumentsWithAllOwners(final StringBuffer buf,
      final BinTypeRef parameterized,
      final BinTypeRef[] args, final boolean printTypeParams) {
    if (FastJavaLexer.getActualJvmMode() != FastJavaLexer.JVM_50) {
      return;
    }

    if (args == null || args.length == 0) {
      return;
    }

    // JAVA5: must be improved, there are cases when type parameters are used as arguments for other type
    if (!printTypeParams) {
      for (int i = 0, max = args.length; i < max; i++) {
        BinTypeRef arg = args[i];
        if ("?".equals(arg.getName())) {
          return; // can't print wildcard
        }

        if (arg.getBinType().isTypeParameter()) {
          // try to resolve in the given context
          if (parameterized != null && parameterized.isSpecific()) {
            arg = ((BinSpecificTypeRef) parameterized).findTypeArgumentByParameter(arg);
//System.err.println("new arg: " + arg + " --- " + parameterized);
          }
          // FIXME: it might resolve type param into another type param, must be recursive

          if (arg == null || arg.getBinType().isTypeParameter()) {
            return; // can't print anything
          } else {
            args[i] = arg;
          }
        }
      }
    }

    boolean opened = false;
    for (int i = 0, max = args.length; i < max; i++) {
      BinTypeRef arg = args[i];

      if (!opened) {
        buf.append("<");
        opened = true;
      } else {
        buf.append(", ");
      }
      // FIXME: this will definitely produce errors
      //Dmitri: was false, changed to true for inner class names.
      formatNotQualified(buf, arg, true);
    }
    if (opened) {
      buf.append(">");
    }
	}
	
	public static String formatNotQualifiedForTypeArgumentsWithAllOwners(BinTypeRef typeRef){
		return formatNotQualifiedForTypeArgumentsWithAllOwners(new StringBuffer(64), typeRef, false).toString();
	}
	
	public static String formatQualifiedForTypeArgumentsWithAllOwners(BinTypeRef typeRef){
		return formatQualifiedForTypeArgumentsWithAllOwners(new StringBuffer(64), typeRef).toString();
	}
}
