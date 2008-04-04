/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.sf.refactorit.ui.dialog.SwingDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.awt.Window;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class NBDialog extends SwingDialog {
  protected NBDialog(IdeWindowContext context) {
    super(context);
  }

  protected NBDialog(IdeWindowContext context, JOptionPane pane) {
    super(context, pane);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#createDialogInstance(java.awt.Window)
   */
  protected JDialog createDialogInstance(Window owner) {
    return super.createDialogInstance(owner);
  }
}
