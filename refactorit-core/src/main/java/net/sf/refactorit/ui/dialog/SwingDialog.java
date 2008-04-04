/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.dialog;


import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class SwingDialog extends RitDialog {
  public SwingDialog(IdeWindowContext owner) {
    super(owner);
  }

  public SwingDialog(IdeWindowContext owner, JOptionPane pane) {
    super(owner, pane);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#createDialogInstance()
   */
  protected JDialog createDialogInstance() {
    return createDialogInstance(getOwnerWindow());
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#createDialogInstance(javax.swing.JOptionPane)
   */
  protected JDialog createDialogInstance(JOptionPane pane) {
    return createDialogInstance(getOwnerWindow(), pane);
  }

  protected final Window getOwnerWindow() {
    if (owner == null) {
      return null;
    }

    Window window = ((AWTContext) owner).getWindow();
    while (window != null) {
      if (window instanceof Dialog || window instanceof Frame) {
        return window;
      }
      window = window.getOwner();
    }

    return null;
  }
}
