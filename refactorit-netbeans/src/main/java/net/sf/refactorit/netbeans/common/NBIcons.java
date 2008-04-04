/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.tree.NodeIcons;

import org.openide.util.Utilities;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import java.awt.Image;


/**
 * @author  Igor Malinin
 */
public class NBIcons extends NodeIcons {
  public static final NBIcons instance = new NBIcons();

  // Icons bundled with RefactorIT
  private static final ImageIcon iconSource = loadLocalIcon("class_source.gif");
  private static final ImageIcon iconPackage = loadLocalIcon("Package.gif");
  private static final ImageIcon iconPackageX = loadLocalIcon("PackageX.gif");

  // Icons bundled with NB
  private static final ImageIcon iconClass = loadOpenideIcon("class.gif");
  private static final ImageIcon iconInterface = loadOpenideIcon(
      "interface.gif");

  private static final ImageIcon iconConstructorPub = loadOpenideIcon(
      "constructorPublic.gif");
  private static final ImageIcon iconConstructorPro = loadOpenideIcon(
      "constructorProtected.gif");
  private static final ImageIcon iconConstructorPkg = loadOpenideIcon(
      "constructorPackage.gif");
  private static final ImageIcon iconConstructorPri = loadOpenideIcon(
      "constructorPrivate.gif");

  private static final ImageIcon iconMethodPub = loadOpenideIcon(
      "methodPublic.gif");
  private static final ImageIcon iconMethodPubS = loadOpenideIcon(
      "methodStPublic.gif");
  private static final ImageIcon iconMethodPro = loadOpenideIcon(
      "methodProtected.gif");
  private static final ImageIcon iconMethodProS = loadOpenideIcon(
      "methodStProtected.gif");
  private static final ImageIcon iconMethodPkg = loadOpenideIcon(
      "methodPackage.gif");
  private static final ImageIcon iconMethodPkgS = loadOpenideIcon(
      "methodStPackage.gif");
  private static final ImageIcon iconMethodPri = loadOpenideIcon(
      "methodPrivate.gif");
  private static final ImageIcon iconMethodPriS = loadOpenideIcon(
      "methodStPrivate.gif");

  private static final ImageIcon iconFieldPub = loadOpenideIcon(
      "variablePublic.gif");
  private static final ImageIcon iconFieldPubS = loadOpenideIcon(
      "variableStPublic.gif");
  private static final ImageIcon iconFieldPro = loadOpenideIcon(
      "variableProtected.gif");
  private static final ImageIcon iconFieldProS = loadOpenideIcon(
      "variableStProtected.gif");
  private static final ImageIcon iconFieldPkg = loadOpenideIcon(
      "variablePackage.gif");
  private static final ImageIcon iconFieldPkgS = loadOpenideIcon(
      "variableStPackage.gif");
  private static final ImageIcon iconFieldPri = loadOpenideIcon(
      "variablePrivate.gif");
  private static final ImageIcon iconFieldPriS = loadOpenideIcon(
      "variableStPrivate.gif");

  /**
   * Note: from NB 2002-Oct-17 version or so NB did not have these icons bundled anymore,
   * that's why we load local icons when openide icon loading fails.
   */
  private static ImageIcon loadOpenideIcon(String name) {
    if (getOpenideImage(name) != null) {
      return new ImageIcon(getOpenideImage(name));
    } else {
      return loadLocalIcon(name);
    }
  }

  private static ImageIcon loadLocalIcon(String name) {
    // HACK: because CVS could not add "class.gif" I added the file as "classNormal.gif"
    if (name.equals("class.gif")) {
      name = "classNormal.gif";
    }

    return ResourceUtil.getIcon(NBIcons.class, name);
  }

  private static Image getOpenideImage(final String name) {
    return getOpenideImage("org/openide/resources/src", name);
  }

  private static Image getOpenideImage(final String directory,
      final String name) {
    // First tries to load without the initial slash; this prevents the NB warnings
    Image withoutSlash = Utilities.loadImage(directory + '/' + name);
    if (withoutSlash != null) {
      return withoutSlash;
    } else {
      return Utilities.loadImage('/' + directory + '/' + name);
    }
  }

  private NBIcons() {}

  public Icon getSourceIcon() {
    return iconSource;
  }

  public Icon getPackageIcon(boolean expanded) {
    return expanded ? iconPackageX : iconPackage;
  }

  public Icon getClassIcon(int modifiers) {
    return iconClass;
  }

  public Icon getInterfaceIcon(int modifiers) {
    return iconInterface;
  }

  public Icon getFieldIcon(int modifiers) {
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return ((modifiers & STATIC) != 0)
            ? iconFieldPubS
            : iconFieldPub;
      case PROTECTED:
        return ((modifiers & STATIC) != 0)
            ? iconFieldProS
            : iconFieldPro;
      case PRIVATE:
        return ((modifiers & STATIC) != 0)
            ? iconFieldPriS
            : iconFieldPri;

      default:
        return ((modifiers & STATIC) != 0)
            ? iconFieldPkgS
            : iconFieldPkg;
    }
  }

  public Icon getMethodIcon(int modifiers) {
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return ((modifiers & STATIC) != 0)
            ? iconMethodPubS
            : iconMethodPub;
      case PROTECTED:
        return ((modifiers & STATIC) != 0)
            ? iconMethodProS
            : iconMethodPro;
      case PRIVATE:
        return ((modifiers & STATIC) != 0)
            ? iconMethodPriS
            : iconMethodPri;

      default:
        return ((modifiers & STATIC) != 0)
            ? iconMethodPkgS
            : iconMethodPkg;
    }
  }

  public Icon getConstructorIcon(int modifiers) {
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

  public Icon getNonJavaIcon() {
    Image icon = getOpenideImage("org/openide/resources", "defaultNode.gif");
    if (icon != null) {
      return new ImageIcon(icon);
    } else {
      return loadLocalIcon("defaultNode.gif");
    }
  }
}
