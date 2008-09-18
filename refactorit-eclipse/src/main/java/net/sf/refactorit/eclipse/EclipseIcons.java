/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.tree.NodeIcons;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import java.net.URL;


/**
 * @author Igor Malinin
 */
public class EclipseIcons extends NodeIcons {
  private static final Logger log = AppRegistry.getLogger(EclipseIcons.class);

  public static final EclipseIcons instance = new EclipseIcons();

  private static final ImageIcon iconNonJava = loadJavaIcon("file_obj.gif");

  private static final ImageIcon iconSource = loadJavaIcon("jcu_obj.gif");
  private static final ImageIcon iconPackage = loadJavaIcon("package_obj.gif");

  private static final ImageIcon iconClass = loadJavaIcon("class_obj.gif");
  private static final ImageIcon iconClassDefault =
      loadJavaIcon("class_default_obj.gif");
  private static final ImageIcon iconClassInner =
      loadJavaIcon("innerclass_public_obj.gif");
  private static final ImageIcon iconClassInnerProtected =
      loadJavaIcon("innerclass_protected_obj.gif");
  private static final ImageIcon iconClassInnerDefault =
      loadJavaIcon("innerclass_default_obj.gif");
  private static final ImageIcon iconClassInnerPrivate =
      loadJavaIcon("innerclass_private_obj.gif");

  private static final ImageIcon iconInterface = loadJavaIcon("int_obj.gif");
  private static final ImageIcon iconInterfaceDefault =
      loadJavaIcon("int_default_obj.gif");
  private static final ImageIcon iconInterfaceInner =
      loadJavaIcon("innerinterface_public_obj.gif");
  private static final ImageIcon iconInterfaceInnerProtected =
      loadJavaIcon("innerinterface_protected_obj.gif");
  private static final ImageIcon iconInterfaceInnerDefault =
      loadJavaIcon("innerinterface_default_obj.gif");
  private static final ImageIcon iconInterfaceInnerPrivate =
      loadJavaIcon("innerinterface_private_obj.gif");

  // TODO: Vverlay Method with Constructur sign
//  private static final ImageIcon iconConstructorPub =
//      loadJavaIcon("constructorPublic.gif");
//  private static final ImageIcon iconConstructorPro =
//      loadJavaIcon("constructorProtected.gif");
//  private static final ImageIcon iconConstructorPkg =
//      loadJavaIcon("constructorPackage.gif");
//  private static final ImageIcon iconConstructorPri =
//      loadJavaIcon("constructorPrivate.gif");

  // TODO: Overlay Method/Field with Abstract/Static sign;
  // RIT missing Final/Native/Synchronized/Volatile
  // and Implement/Overide/MainClass support.

  private static final ImageIcon iconMethodPub =
      loadJavaIcon("methpub_obj.gif");
//  private static final ImageIcon iconMethodPubS =
//      loadJavaIcon("methodStPublic.gif");
  private static final ImageIcon iconMethodPro =
      loadJavaIcon("methpro_obj.gif");
//  private static final ImageIcon iconMethodProS =
//      loadJavaIcon("methodStProtected.gif");
  private static final ImageIcon iconMethodPkg =
      loadJavaIcon("methdef_obj.gif");
//  private static final ImageIcon iconMethodPkgS =
//      loadJavaIcon("methodStPackage.gif");
  private static final ImageIcon iconMethodPri =
      loadJavaIcon("methpri_obj.gif");
//  private static final ImageIcon iconMethodPriS =
//      loadJavaIcon("methodStPrivate.gif");

  private static final ImageIcon iconFieldPub =
      loadJavaIcon("field_public_obj.gif");
//  private static final ImageIcon iconFieldPubS =
//      loadJavaIcon("variableStPublic.gif");
  private static final ImageIcon iconFieldPro =
      loadJavaIcon("field_protected_obj.gif");
//  private static final ImageIcon iconFieldProS =
//      loadJavaIcon("variableStProtected.gif");
  private static final ImageIcon iconFieldPkg =
      loadJavaIcon("field_default_obj.gif");
//  private static final ImageIcon iconFieldPkgS =
//      loadJavaIcon("variableStPackage.gif");
  private static final ImageIcon iconFieldPri =
      loadJavaIcon("field_private_obj.gif");
//  private static final ImageIcon iconFieldPriS =
//      loadJavaIcon("variableStPrivate.gif");

  private static ImageIcon loadJavaIcon(String name) {
    Bundle bundle = Platform.getBundle("org.eclipse.jdt.ui");
    URL url = bundle.getEntry("/icons/full/obj16/" + name);
    if (url != null) {
      log.debug("Found icon: " + url);
      try {
        return new ImageIcon(url);
      } catch (Exception e) {}
    }

    log.warn("Error loading icon: " + name);
    return null;
  }

  private EclipseIcons() {}

  public Icon getSourceIcon() {
    return iconSource;
  }

  public Icon getPackageIcon(boolean expanded) {
    return iconPackage;
  }

  public Icon getClassIcon(int modifiers) {
    if ((modifiers & INNER) == 0) {
      if ((modifiers & ACCESS) == DEFAULT) {
        return iconClassDefault;
      }
      return iconClass;
    }
    
    // inner classes
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return iconClassInner;
      case PROTECTED:
        return iconClassInnerProtected;
      case PRIVATE:
        return iconClassInnerPrivate;
      default:
        return iconClassInnerDefault;
    }
  }

  public Icon getInterfaceIcon(int modifiers) {
    if ((modifiers & INNER) == 0) {
      if ((modifiers & ACCESS) == DEFAULT) {
        return iconInterfaceDefault;
      }
      return iconInterface;
    }
    
    // inner classes
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return iconInterfaceInner;
      case PROTECTED:
        return iconInterfaceInnerProtected;
      case PRIVATE:
        return iconInterfaceInnerPrivate;
      default:
        return iconInterfaceInnerDefault;
    }
  }

  public Icon getFieldIcon(int modifiers) {
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return iconFieldPub;
      case PROTECTED:
        return iconFieldPro;
      case PRIVATE:
        return iconFieldPri;
      default:
        return iconFieldPkg;
    }
  }

  public Icon getMethodIcon(int modifiers) {
    switch (modifiers & ACCESS) {
      case PUBLIC:
        return iconMethodPub;
      case PROTECTED:
        return iconMethodPro;
      case PRIVATE:
        return iconMethodPri;
      default:
        return iconMethodPkg;
    }
  }

  public Icon getConstructorIcon(int modifiers) {
    return getMethodIcon(modifiers);
  }

  public Icon getNonJavaIcon() {
    return iconNonJava;
  }
}
