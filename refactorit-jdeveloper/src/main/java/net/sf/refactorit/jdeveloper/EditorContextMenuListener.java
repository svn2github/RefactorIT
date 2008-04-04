/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.commonIDE.FastItemFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.utils.LinePositionUtil;

import oracle.ide.addin.Context;
import oracle.ide.addin.Controller;
import oracle.ide.model.Document;
import oracle.jdeveloper.ceditor.CodeEditor;
import oracle.jdeveloper.model.JavaSourceNode;
import oracle.jdeveloper.model.JspSourceNode;


/**
 *
 * @author  Tanel
 */
public class EditorContextMenuListener extends RefactorItContextMenuListener {
  /**
   * Default constructor. A singleton instance is created when the addin
   * is loaded at startup.
   */
  public EditorContextMenuListener(Controller controller) {
    super();
  }

  public RunContext extractRunContext(Context context) {
    CodeEditor editor = (CodeEditor) context.getView();
    String text = editor.getSelectedText();
    Class[] binClasses = null;

    int code = getContextType(context);

    if (text == null || text.length() == 0) {
//      Element[] elements = context.getSelection();
//
//      // paranoid
////      System.err.println("IsJsp="+isJsp);
////      System.err.println("noProcessable"+notProcessable);
//
//      // Not working
//      if (elements.length > 0) {
//        if (elements[0] instanceof JspSourceNode) {
//          isJsp = true;
//        } else if (!(elements[0] instanceof JavaSourceNode)) {
//          notProcessable = true;
//        }
//        System.err.println("NotProcessable="+notProcessable+", isJsp="+isJsp);
//      } else {
//        System.err.println("Selection =null");
//        System.err.println("Document class"+context.getDocument().getClass().getName());
//      }
      try {
        if (code == RunContext.JSP_CONTEXT) {
          // FIXME: getCurrentBinClass doesn't work for JSP yet
          binClasses = new Class[] {
              BinLocalVariable.class};
        } else if (code == RunContext.JAVA_CONTEXT) {
          // Should be Java file, process it
//          System.err.println("@@document URL:" + context.getDocument().getURL()); ;
          String content = editor.getText(0,
              editor.getFocusedEditorPane().
              getDocument().
              getLength());
          LinePositionUtil.setTabSize(1);
          SourceCoordinate sc = LinePositionUtil.convert(editor.
              getCaretPosition(), content);
          binClasses = FastItemFinder.getCurrentBinClass("", content,
              sc.getLine(), sc.getColumn());
        }
      } catch (Exception e) {
        // silent
      }
    } else {
      binClasses = new Class[] {BinSelection.class};
    }

    RunContext result = new RunContext(code, binClasses, false);

//    if (code) {
//      result = new UnsupportedRunContext();
//    } else if (isJsp) {
//      result = new JspRunContext(binClasses);
//    } else {
//      result = new RunContext(binClasses);
//    }

    return result;
  }

//  protected void populateRefactorITMenu(JMenu menu, Context context) {
//        menu.add(separator);
//        CodeEditor editor = (CodeEditor)context.getView();
//        String text = editor.getSelectedText();
//        if ( text == null || text.length() == 0 ) {
//      Class[] binClasses = null;
//      Element []elements=context.getSelection();
//      boolean isJsp=false;
//      boolean notProcessable=false;
//      Document document=context.getDocument();
//      if( document instanceof JspSourceNode ) {
//        isJsp=true;
//      } else if ( !( document instanceof JavaSourceNode )) {
//        notProcessable=true;
//      }
//      try {
//        if( isJsp ) {
//          // FIXME: getCurrentBinClass doesn't work for JSP yet
//          binClasses=new Class[] {BinLocalVariable.class};
//        } else if(!notProcessable) {
//          String content = editor.getText(0,
//                                          editor.getFocusedEditorPane().getDocument().
//                                          getLength());
//          LinePositionUtil.setTabSize(1);
//          SourceCoordinate sc = LinePositionUtil.convert(editor.
//              getCaretPosition(), content);
//          binClasses = SourceUtil.getCurrentBinClass("", content.getBytes(),
//              sc.getLine(), sc.getColumn());
//        }
//      } catch (Exception e) {
//        // silent
//      }
//      if (binClasses != null) {
//        SwingUtil.addIntoMenu(menu,
//            OldMenuBuilder.getInstance().
//            getMenuItemsForBinClasses(binClasses, isJsp, false));
//      } else {
//        if (shortcutMenuItems == null) {
//          shortcutMenuItems = OldMenuBuilder.getInstance().getShortcutMenuItems();
//          moreMenu = OldMenuBuilder.getInstance().getMoreMenu();
//        }
//        SwingUtil.addIntoMenu(menu, shortcutMenuItems);
//        menu.add(moreMenu);
//      }
//    } else {
//      if (selectionMenuItems == null) {
//        selectionMenuItems = OldMenuBuilder.getInstance().getMenuItemsForBinClass(
//            BinSelection.class);
//      }
//
//      SwingUtil.addIntoMenu(menu, selectionMenuItems);
//    }
//  }

  /**
   * @param context
   * @return
   */
  private int getContextType(final Context context) {
    Document document = context.getDocument();

    if (document instanceof JspSourceNode) {
      return RunContext.JSP_CONTEXT;
    }

    if (document instanceof JavaSourceNode) {
      return RunContext.JAVA_CONTEXT;
    }

    return RunContext.UNSUPPORTED_CONTEXT;
  }
}
