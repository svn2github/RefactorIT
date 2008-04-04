/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.editor.EditorAction;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.Node;

import java.util.LinkedList;

import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.UIResources;


/**
 * Implements store for the last GoToAction lines.
 * Opens this place if action is performed.
 *
 * @author Vladislav Vislogubov
 */
public class BackAction extends JBIdeAction {
  private static class BackInfo {
    public Node node;
    public int line;
  }

  private static LinkedList list = new LinkedList();

  public BackAction(IdeAction action) {
    super(action, ResourceUtil.getIcon(UIResources.class, "back_action.gif"));
//    super( "Back Action", 'B', "Returns cursor position to the previous GoTo Action result" );

//    putValue("ActionGroup", "RefactorIT");
//    putValue( BrowserAction.SMALL_ICON, ResourceUtil.getIcon(Main.class, "back_action.gif") );
  }

  /**
   * @see com.borland.primetime.ide.BrowserAction#actionPerformed(Browser)
   */
  public void actionPerformed(Browser browser) {
    if (list.isEmpty()) {
      return;
    }

    BackInfo info = (BackInfo) list.removeLast();

    try {
      browser.setActiveNode(info.node, true);
      EditorAction.getFocusedEditor().gotoLine(info.line, false);
    } catch (Exception ignore) {}
  }

  public static void addRecord(Node n, int l) {
    if (n == null) {
      return;
    }

    BackInfo info = new BackInfo();
    info.node = n;
    info.line = l;

    list.addLast(info);
  }
}
