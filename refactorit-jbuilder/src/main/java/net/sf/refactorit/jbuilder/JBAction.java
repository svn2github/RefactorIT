/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import java.awt.event.ActionEvent;

import net.sf.refactorit.ui.module.ActionProxy;


/**
 * @author tonis
 */
public interface JBAction {
  /*
     public void actionPerformed(Browser browser) {
    EditorPane pane = EditorAction.getFocusedEditor();
   if (pane.hasFocus()) performAction(pane, key);
     }
   */

  public void setAction(ActionProxy action);

  public ActionProxy getAction();

  public Object clone();

  public String getName();

  public void actionPerformed(ActionEvent e);
}
