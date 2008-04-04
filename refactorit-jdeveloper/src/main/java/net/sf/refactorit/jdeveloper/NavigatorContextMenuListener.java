/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.module.RunContext;


import oracle.ide.addin.Context;
import oracle.ide.addin.Controller;
import oracle.ide.model.Element;
import oracle.ide.model.PackageFolder;
import oracle.jdeveloper.model.JProject;
import oracle.jdeveloper.model.JavaSourceNode;


/**
 *
 *
 * @author  Tanel
 */
public class NavigatorContextMenuListener extends RefactorItContextMenuListener {
//  /** the JSeparator object. */
//  private JSeparator separator = null;

  /**
   * Default constructor. A singleton instance is created when the addin
   * is loaded at startup.
   */
  public NavigatorContextMenuListener(Controller controller) {
    super();
  }

  public RunContext extractRunContext(Context context) {
    Element[] selectedElements = context.getSelection();
    // add the menu items for selected element.

    Class[] binClasses = null;

    int typeCode = RunContext.JAVA_CONTEXT;

    if (selectedElements == null || selectedElements.length == 0) {
      binClasses = null;
    } else {
      int size = selectedElements.length;
      binClasses = new Class[size];

      for (int i = 0; i < size; i++) {
        if ((JavaSourceNode.class).isAssignableFrom(selectedElements[i].
            getClass())) {
          binClasses[i] = BinClass.class;
        } else if ((JProject.class).isAssignableFrom(selectedElements[i].
            getClass())) {
          binClasses[i] = Project.class;
        } else if ((PackageFolder.class).isAssignableFrom(selectedElements[i].
            getClass())) {
          binClasses[i] = BinPackage.class;
        } else if (selectedElements[i].getClass().getName().indexOf(
            "JspSourceNode") >= 0) {
          typeCode = RunContext.JSP_CONTEXT;
        } else {
          AppRegistry.getLogger(this.getClass()).debug("unknown node " + selectedElements[i].getClass());
          typeCode = RunContext.UNSUPPORTED_CONTEXT;
          binClasses = null;
          break;
        }
      }
    }

    return new RunContext(typeCode, binClasses, true);
  }

//  protected void populateRefactorITMenu(JMenu menu, Context context) {
//    Element[] selectedElements = context.getSelection();
//    // add the menu items for selected element.
//    if ( selectedElements == null || selectedElements.length == 0 )
//      return;
//
//    int size = selectedElements.length;
//    Class[] binClasses = new Class[size];
//
//    for( int i = 0; i < size; i++ ) {
//      if ((JavaSourceNode.class).isAssignableFrom(selectedElements[i].getClass())) {
//        binClasses[i] = BinClass.class;
//      } else if ((JProject.class).isAssignableFrom(selectedElements[i].getClass())) {
//        binClasses[i] = Project.class;
//      } else if ((PackageFolder.class).isAssignableFrom(selectedElements[i].getClass())) {
//        binClasses[i] = BinPackage.class;
//      } else {
//        // unknown node
//        return;
//      }
//    }
//
//    menu.add(getSeparator());
//    if ( size == 1 ) {
//      SwingUtil.addIntoMenu(menu,
//          OldMenuBuilder.getInstance().getMenuItemsForBinClass( binClasses[0] ) );
//    } else {
//      SwingUtil.addIntoMenu(menu,
//          OldMenuBuilder.getInstance().getMenuItemsForBinClasses( binClasses, true ) );
//    }
//  }
//
//  /**
//   * Return the JSeparator object.
//   */
//  private JSeparator getSeparator() {
//    if (this.separator == null)
//      this.separator = new JSeparator();
//    return this.separator;
//  }
}
