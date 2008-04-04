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

import javax.swing.AbstractAction;

import net.sf.refactorit.ui.module.ActionProxy;

import java.awt.event.ActionEvent;


/**
 * @author tonis
 */
public class JBEmptyAction extends AbstractAction implements JBAction {
  String name;

  public JBEmptyAction(String name) {
    super(name);

    this.name = name;

    putValue(EditorAction.SHORT_DESCRIPTION, name);
    putValue(EditorAction.LONG_DESCRIPTION, name);
    putValue(EditorAction.NAME, name);
  }

  /*
   * @see net.sf.refactorit.jbuilder.JBAction#setAction(net.sf.refactorit.ui.module.ActionProxy)
   */
  public void setAction(ActionProxy action) {
  }

  /*
   * @see net.sf.refactorit.jbuilder.JBAction#getAction()
   */
  public ActionProxy getAction() {
    return null;
  }

  /*
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * @see net.sf.refactorit.jbuilder.JBAction#getName()
   */
  public String getName() {
    return NAME;
  }

  /*
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
  }
}
