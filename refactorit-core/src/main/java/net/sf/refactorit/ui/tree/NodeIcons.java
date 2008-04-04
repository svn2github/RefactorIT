/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.ResourceUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * Factory of tree icons.
 *
 * @author Igor Malinin
 */
public abstract class NodeIcons {
  public static final int ACCESS = 0x03;

  public static final int DEFAULT = 0x00;
  public static final int PUBLIC = 0x01;
  public static final int PROTECTED = 0x02;
  public static final int PRIVATE = 0x03;

  public static final int ABSTRACT = 0x04;
  public static final int INNER = 0x08;
  public static final int STATIC = 0x10;

  static final class Default extends NodeIcons {
    static final Default instance = new Default();

    private static final ImageIcon iconSource =
        ResourceUtil.getIcon(NodeIcons.class, "Source.gif");

    private static final ImageIcon iconPackage =
        ResourceUtil.getIcon(NodeIcons.class, "Package.gif");

    private static final ImageIcon iconClassAbs =
        ResourceUtil.getIcon(NodeIcons.class, "ClassAbs.gif");
    private static final ImageIcon iconClassAbsI =
        ResourceUtil.getIcon(NodeIcons.class, "ClassAbsI.gif");
    private static final ImageIcon iconClassPub =
        ResourceUtil.getIcon(NodeIcons.class, "ClassPub.gif");
    private static final ImageIcon iconClassPubI =
        ResourceUtil.getIcon(NodeIcons.class, "ClassPubI.gif");
    private static final ImageIcon iconClassProI =
        ResourceUtil.getIcon(NodeIcons.class, "ClassProI.gif");
    private static final ImageIcon iconClassPkg =
        ResourceUtil.getIcon(NodeIcons.class, "ClassPkg.gif");
    private static final ImageIcon iconClassPkgI =
        ResourceUtil.getIcon(NodeIcons.class, "ClassPkgI.gif");
    private static final ImageIcon iconClassPriI =
        ResourceUtil.getIcon(NodeIcons.class, "ClassPriI.gif");

    private static final ImageIcon iconInterfacePub =
        ResourceUtil.getIcon(NodeIcons.class, "InterfacePub.gif");
    private static final ImageIcon iconInterfacePubI =
        ResourceUtil.getIcon(NodeIcons.class, "InterfacePubI.gif");
    private static final ImageIcon iconInterfaceProI =
        ResourceUtil.getIcon(NodeIcons.class, "InterfaceProI.gif");
    private static final ImageIcon iconInterfacePkg =
        ResourceUtil.getIcon(NodeIcons.class, "InterfacePkg.gif");
    private static final ImageIcon iconInterfacePkgI =
        ResourceUtil.getIcon(NodeIcons.class, "InterfacePkgI.gif");
    private static final ImageIcon iconInterfacePriI =
        ResourceUtil.getIcon(NodeIcons.class, "InterfacePriI.gif");

    private static final ImageIcon iconConstructorPub =
        ResourceUtil.getIcon(NodeIcons.class, "ConstructorPub.gif");
    private static final ImageIcon iconConstructorPro =
        ResourceUtil.getIcon(NodeIcons.class, "ConstructorPro.gif");
    private static final ImageIcon iconConstructorPkg =
        ResourceUtil.getIcon(NodeIcons.class, "ConstructorPkg.gif");
    private static final ImageIcon iconConstructorPri =
        ResourceUtil.getIcon(NodeIcons.class, "ConstructorPri.gif");

    private static final ImageIcon iconMethodPub =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPub.gif");
    private static final ImageIcon iconMethodPubA =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPubA.gif");
    private static final ImageIcon iconMethodPubS =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPubS.gif");
    private static final ImageIcon iconMethodPro =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPro.gif");
    private static final ImageIcon iconMethodProA =
        ResourceUtil.getIcon(NodeIcons.class, "MethodProA.gif");
    private static final ImageIcon iconMethodProS =
        ResourceUtil.getIcon(NodeIcons.class, "MethodProS.gif");
    private static final ImageIcon iconMethodPkg =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPkg.gif");
    private static final ImageIcon iconMethodPkgA =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPkgA.gif");
    private static final ImageIcon iconMethodPkgS =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPkgS.gif");
    private static final ImageIcon iconMethodPri =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPri.gif");
    private static final ImageIcon iconMethodPriS =
        ResourceUtil.getIcon(NodeIcons.class, "MethodPriS.gif");

    private static final ImageIcon iconFieldPub =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPub.gif");
    private static final ImageIcon iconFieldPubS =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPubS.gif");
    private static final ImageIcon iconFieldPro =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPro.gif");
    private static final ImageIcon iconFieldProS =
        ResourceUtil.getIcon(NodeIcons.class, "FieldProS.gif");
    private static final ImageIcon iconFieldPkg =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPkg.gif");
    private static final ImageIcon iconFieldPkgS =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPkgS.gif");
    private static final ImageIcon iconFieldPri =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPri.gif");
    private static final ImageIcon iconFieldPriS =
        ResourceUtil.getIcon(NodeIcons.class, "FieldPriS.gif");
    private static final ImageIcon iconNonJava =
        ResourceUtil.getIcon(NodeIcons.class, "NonJava.gif");

    private Default() {}

    public final Icon getSourceIcon() {
      return iconSource;
    }

    public final Icon getPackageIcon(boolean open) {
      return iconPackage;
    }

    public final Icon getClassIcon(int modifiers) {
      if ((modifiers & ABSTRACT) != 0) {
        return ((modifiers & INNER) != 0) ? iconClassAbsI : iconClassAbs;
      }

      switch (modifiers & ACCESS) {
        case PUBLIC:
          return ((modifiers & INNER) != 0) ? iconClassPubI : iconClassPub;
        case PROTECTED:
          return iconClassProI;
        case PRIVATE:
          return iconClassPriI;

        default:
          return ((modifiers & INNER) != 0) ? iconClassPkgI : iconClassPkg;
      }
    }

    public final Icon getInterfaceIcon(int modifiers) {
      switch (modifiers & ACCESS) {
        case PUBLIC:
          return ((modifiers & INNER) != 0)
              ? iconInterfacePubI
              : iconInterfacePub;
        case PROTECTED:
          return iconInterfaceProI;
        case PRIVATE:
          return iconInterfacePriI;

        default:
          return ((modifiers & INNER) != 0)
              ? iconInterfacePkgI
              : iconInterfacePkg;
      }
    }

    public final Icon getFieldIcon(int modifiers) {
      switch (modifiers & ACCESS) {
        case PUBLIC:
          return ((modifiers & STATIC) != 0) ? iconFieldPubS : iconFieldPub;
        case PROTECTED:
          return ((modifiers & STATIC) != 0) ? iconFieldProS : iconFieldPro;
        case PRIVATE:
          return ((modifiers & STATIC) != 0) ? iconFieldPriS : iconFieldPri;

        default:
          return ((modifiers & STATIC) != 0) ? iconFieldPkgS : iconFieldPkg;
      }
    }

    public final Icon getMethodIcon(int modifiers) {
      switch (modifiers & ACCESS) {
        case PUBLIC:
          return ((modifiers & STATIC) != 0)
              ? iconMethodPubS
              : ((modifiers & ABSTRACT) != 0)
              ? iconMethodPubA
              : iconMethodPub;
        case PROTECTED:
          return ((modifiers & STATIC) != 0)
              ? iconMethodProS
              : ((modifiers & ABSTRACT) != 0)
              ? iconMethodProA
              : iconMethodPro;
        case PRIVATE:
          return ((modifiers & STATIC) != 0) ? iconMethodPriS : iconMethodPri;

        default:
          return ((modifiers & STATIC) != 0)
              ? iconMethodPkgS
              : ((modifiers & ABSTRACT) != 0)
              ? iconMethodPkgA
              : iconMethodPkg;
      }
    }

    public final Icon getConstructorIcon(int modifiers) {
      switch (modifiers & ACCESS) {
        case PUBLIC:
          return iconConstructorPub;
        case PROTECTED:
          return iconConstructorPro;
        case PRIVATE:
          return iconConstructorPri;

        default:
          return iconConstructorPkg;
      }
    }

    public final Icon getNonJavaIcon() {
      return iconNonJava;
    }
  }


  private static NodeIcons icons;

  public static int getModifiers(BinType bin) {
    int modifiers;

    if (bin.isPublic()) {
      modifiers = PUBLIC;
    } else if (bin.isProtected()) {
      modifiers = PROTECTED;
    } else if (bin.isPrivate()) {
      modifiers = PRIVATE;
    } else {
      modifiers = DEFAULT;
    }

    if (bin.isAbstract()) {
      modifiers |= ABSTRACT;
    }

    if (bin.isInnerType()) {
      modifiers |= INNER;
    }

    return modifiers;
  }

  public static int getModifiers(BinMember bin) {
    int modifiers;

    if (bin.isPublic()) {
      modifiers = PUBLIC;
    } else if (bin.isProtected()) {
      modifiers = PROTECTED;
    } else if (bin.isPrivate()) {
      modifiers = PRIVATE;
    } else {
      modifiers = DEFAULT;
    }

    if (bin.isAbstract()) {
      modifiers |= ABSTRACT;
    }

    if (bin.isStatic()) {
      modifiers |= STATIC;
    }

    return modifiers;
  }

  /** Creates new NodeImage */
  protected NodeIcons() {}

  public abstract Icon getSourceIcon();

  public abstract Icon getPackageIcon(boolean expanded);

  public abstract Icon getClassIcon(int modifiers);

  public abstract Icon getInterfaceIcon(int modifiers);

  public abstract Icon getFieldIcon(int modifiers);

  public abstract Icon getMethodIcon(int modifiers);

  public abstract Icon getConstructorIcon(int modifiers);

  public static NodeIcons getNodeIcons() {
    return (icons != null) ? icons : Default.instance;
  }

  public static void setNodeIcons(NodeIcons factory) {
    icons = factory;
  }

  /**
   * Convenience method to get icon based on node type and node object.
   */
  public static Icon getBinIcon(int type, Object bin, boolean expanded) {
    final NodeIcons icons = NodeIcons.getNodeIcons();

    switch (type) {
      case UITreeNode.NODE_PACKAGE:
        return icons.getPackageIcon(expanded);

      case UITreeNode.NODE_ENUM: // JAVA5: todo
        if (bin instanceof BinType) {
          return icons.getClassIcon(NodeIcons.getModifiers((BinType) bin));
        }
        return icons.getClassIcon(NodeIcons.DEFAULT);

      case UITreeNode.NODE_CLASS:
        if (bin instanceof BinType) {
          return icons.getClassIcon(NodeIcons.getModifiers((BinType) bin));
        }
        return icons.getClassIcon(NodeIcons.DEFAULT);

      case UITreeNode.NODE_UNRESOLVED_TYPE:
        return icons.getClassIcon(NodeIcons.PUBLIC);

      case UITreeNode.NODE_SOURCE:
        return icons.getSourceIcon();

      case UITreeNode.NODE_NON_JAVA:
        return icons.getNonJavaIcon();

      case UITreeNode.NODE_INTERFACE:
        if (bin instanceof BinType) {
          return icons.getInterfaceIcon(NodeIcons.getModifiers((BinType) bin));
        }
        return icons.getInterfaceIcon(NodeIcons.DEFAULT);

      case UITreeNode.NODE_TYPE_FIELD:
        if (bin instanceof BinFieldInvocationExpression) {
          return icons.getFieldIcon(NodeIcons
              .getModifiers(((BinFieldInvocationExpression) bin).getField()));
        }
        if (bin instanceof BinMember) {
          return icons.getFieldIcon(
              NodeIcons.getModifiers((BinMember) bin));
        }
        return icons.getFieldIcon(NodeIcons.DEFAULT);

      case UITreeNode.NODE_TYPE_CNSTOR:
        if (bin instanceof BinMethodInvocationExpression) {
          return icons.getConstructorIcon(NodeIcons
              .getModifiers(((BinMethodInvocationExpression) bin).getMethod()));
        }
        if (bin instanceof BinMember) {
          return icons.getConstructorIcon(
              NodeIcons.getModifiers((BinMember) bin));
        }
        return icons.getConstructorIcon(NodeIcons.DEFAULT);

      case UITreeNode.NODE_TYPE_METHOD:
        if (bin instanceof BinMethodInvocationExpression) {
          return icons.getMethodIcon(NodeIcons
              .getModifiers(((BinMethodInvocationExpression) bin).getMethod()));
        }
        if (bin instanceof BinMember) {
          return icons.getMethodIcon(NodeIcons.getModifiers((BinMember) bin));
        }
        return icons.getMethodIcon(NodeIcons.DEFAULT);

      default:
        return null;
    }
  }

  public abstract Icon getNonJavaIcon();
}
