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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.ui.module.RunContext;

import oracle.ide.addin.Context;
import oracle.ide.addin.Controller;
import oracle.ide.explorer.TNode;


/**
 *
 * @author  Tanel
 */
public class ExplorerContextMenuListener extends RefactorItContextMenuListener {
//  // the JSeparator object.
//  private JSeparator separator;

  /**
   * Default constructor. A singleton instance is created when the addin
   * is loaded at startup.
   */
  public ExplorerContextMenuListener(Controller controller) {
    super();
  }

  public RunContext extractRunContext(Context context) {

    ExplorerWrapper wrapper = new ExplorerWrapper(context);
    TNode[] nodes = wrapper.getCurrentNodes();

    int code = RunContext.JAVA_CONTEXT;
    Class[] binClasses = null;

    if (nodes == null || nodes.length == 0) {
      code = RunContext.UNSUPPORTED_CONTEXT;
    } else {
      binClasses = new Class[nodes.length];

      for (int i = 0; i < (nodes.length); i++) {
        if (ExplorerWrapper.isPackageNode(nodes[i])) {
          binClasses[i] = BinPackage.class;
        } else if (ExplorerWrapper.isClassNode(nodes[i])) {
          binClasses[i] = BinClass.class;
        } else if (wrapper.isFieldNode(nodes[i])) {
          binClasses[i] = BinField.class;
        } else if (wrapper.isMethodNode(nodes[i])) {
          binClasses[i] = BinMethod.class;
        } else {
          System.err.println("unknown node: " + nodes[i] + " - " +
              nodes[i].getClass()
              + " - " + nodes[i].getData().getClass());
          code = RunContext.UNSUPPORTED_CONTEXT;
          binClasses = null;
          break;
        }
      }
    }

    return new RunContext(code, binClasses, true);
  }

//  protected void populateRefactorITMenu(JMenu menu, Context context) {
//    ExplorerWrapper wrapper = new ExplorerWrapper(context);
//    TNode[] nodes = wrapper.getCurrentNodes();
//    if ( nodes == null || nodes.length == 0 ) {
//      return;
//    }
//
//    Class[] binClasses = new Class[nodes.length];
//
//    for( int i = 0; i < (nodes.length); i++ ) {
//      if (wrapper.isPackageNode( nodes[i] )) {
//        binClasses[i] = BinPackage.class;
//      } else if (wrapper.isClassNode( nodes[i] )) {
//        binClasses[i] = BinClass.class;
//      } else if (wrapper.isFieldNode( nodes[i] )) {
//        binClasses[i] = BinField.class;
//      } else if (wrapper.isMethodNode( nodes[i] )) {
//        binClasses[i] = BinMethod.class;
//      } else {
//        System.err.println("unknown node: " + nodes[i] + " - " + nodes[i].getClass()
//            + " - " + nodes[i].getData().getClass());
//      }
//    }
//
//    menu.add(getSeparator());
//    if ( (nodes.length) == 1 ) {
//      SwingUtil.addIntoMenu(menu, OldMenuBuilder.getInstance().getMenuItemsForBinClass( binClasses[0] ) );
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
