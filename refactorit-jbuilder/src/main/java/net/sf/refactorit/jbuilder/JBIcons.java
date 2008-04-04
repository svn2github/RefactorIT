/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import java.lang.reflect.Method;

import javax.swing.Icon;

import net.sf.refactorit.ui.tree.NodeIcons;

import com.borland.jbuilder.node.JavaFileNode;
import com.borland.jbuilder.node.java.JavaStructureIcons;
import com.borland.primetime.ide.BrowserIcons;


/**
 * Implementation of tree icon factory for JBuilder.
 *
 * @author  Igor Malinin
 */
public class JBIcons extends NodeIcons {
  public static final JBIcons instance = new JBIcons();

  private JBIcons() {}

  private int toJbModifiers(final int modifiers) {
    int s = ((modifiers & STATIC) != 0) ? JavaStructureIcons.STATIC : 0;

    switch (modifiers & ACCESS) {
      case PUBLIC:
        return s | JavaStructureIcons.PUBLIC;
      case PROTECTED:
        return s | JavaStructureIcons.PROTECTED;
      case PRIVATE:
        return s | JavaStructureIcons.PRIVATE;
      default:
        return s | JavaStructureIcons.PACKAGE;
    }
  }

  /** For JBuilder 11 */
  private Icon getNewIcon(final int modifiers, final String methodName) {
    try {
      Method method = JavaStructureIcons.class
          .getMethod(methodName, new Class[] {Integer.TYPE});
      return (Icon) method.invoke(JavaStructureIcons.class,
          new Integer[] {new Integer(modifiers)
      });
    } catch (Throwable ex) {
      return null;
    }
  }

  /** For JBuilder 10 and earlier */
  private Icon getOldIcon(Class clazz, String name) {
    try {
      return (javax.swing.Icon)clazz.getField(name).get(clazz);
    } catch (Throwable e) {
      return null;
    }
  }

  private Icon[] getOldIcons(Class clazz, String name) {
    try {
      return (javax.swing.Icon[])clazz.getField(name).get(clazz);
    } catch (Throwable e) {
      return null;
    }
  }
  
  
  public Icon getSourceIcon() {
    try {
      return JavaFileNode.ICON;
    } catch (Throwable e) {
      try {
        return getOldIcon(BrowserIcons.class, "ICON_FILEJAVA");
      } catch (Throwable ex) {
        try {
          return getOldIcons(JavaStructureIcons.class, "ICONS_CLASS")[JavaStructureIcons.PUBLIC];
        } catch (Throwable exx) {
          return null;
        }
      }
    }
  }

  public Icon getPackageIcon(boolean expanded) {
    return JavaStructureIcons.ICON_PACKAGE;
  }

  public Icon getClassIcon(int modifiers) {
    try {
      return getOldIcons(JavaStructureIcons.class, "ICONS_CLASS")[toJbModifiers(modifiers)];
    } catch (Throwable e) {
      return getNewIcon(toJbModifiers(modifiers), "getClassIcon");
    }
  }

  public Icon getInterfaceIcon(int modifiers) {
    try {
      return getOldIcons(JavaStructureIcons.class, "ICONS_INTERFACE")[toJbModifiers(modifiers)];
    } catch (Throwable e) {
      return getNewIcon(toJbModifiers(modifiers), "getInterfaceIcon");
    }
  }

  public Icon getFieldIcon(int modifiers) {
    try {
      return getOldIcons(JavaStructureIcons.class, "ICONS_FIELD")[toJbModifiers(modifiers)];
    } catch (Throwable e) {
      return getNewIcon(toJbModifiers(modifiers), "getFieldIcon");
    }
  }

  public Icon getMethodIcon(int modifiers) {
    try {
      return getOldIcons(JavaStructureIcons.class, "ICONS_METHOD")[toJbModifiers(modifiers)];
    } catch (Throwable e) {
      return getNewIcon(toJbModifiers(modifiers), "getMethodIcon");
    }
  }

  public Icon getConstructorIcon(int modifiers) {
    try {
      return getOldIcons(JavaStructureIcons.class, "ICONS_CONSTRUCTOR")[toJbModifiers(modifiers)];
    } catch (Throwable e) {
      return getNewIcon(toJbModifiers(modifiers), "getConstructorIcon");
    }
  }

  public Icon getNonJavaIcon() {
    return com.borland.primetime.node.TextFileNode.ICON;
  }
}
