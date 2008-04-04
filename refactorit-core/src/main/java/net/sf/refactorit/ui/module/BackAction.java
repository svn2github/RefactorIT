/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.ShortcutKeyStrokes;

import javax.swing.KeyStroke;

import java.util.LinkedList;


//FIXME: implementation not used

/**
 * @author Tonis Vaga
 */
public class BackAction extends AbstractIdeAction implements ShortcutAction {
  public static final String KEY = "refactorit.action.BackAction";

  public KeyStroke getKeyStroke() {
    return ShortcutKeyStrokes.getByKey(BackAction.KEY);
  }

  public String getName() {
    return "Back Action";
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return 'B';
  }

  public static class BackInfo {
    public CompilationUnit compilationUnit;
    public int line;

    /**
     * @param line
     * @param compilationUnit
     */
    public BackInfo(CompilationUnit compilationUnit, int line) {
      this.line = line;
      this.compilationUnit = compilationUnit;
    }

    public String toString() {
      return compilationUnit.toString() + " line:" + line;
    }
  }


  private static LinkedList list = new LinkedList();

//  public BackAction(IdeAction action) {
//    super(action,ResourceUtil.getIcon(Main.class, "back_action.gif"));
//		super( "Back Action", 'B', "Returns cursor position to the previous GoTo Action result" );
//
//		putValue("ActionGroup", "RefactorIT");
//		putValue( BrowserAction.SMALL_ICON, ResourceUtil.getIcon(Main.class, "back_action.gif") );
//  }

  public static void addRecord(CompilationUnit n, int l) {
    if (n == null) {
      AppRegistry.getLogger(BackAction.class).debug("BackInfo.addRecord called with sourcefile==null");
      return;
    }

    BackInfo info = new BackInfo(n, l);

    list.addLast(info);
  }

  /**
   * Override this
   * @param parent
   */
  public boolean run(IdeWindowContext context) {
    return backCursor(context);
  }

  public static boolean backCursor(IdeWindowContext context) {
    if (list.isEmpty()) {
      return false;
    }

    BackInfo info = (BackInfo) list.removeLast();

    try {
      context.show(info.compilationUnit, info.line, false);
    } catch (Exception ignore) {}

    return false;
  }
}
