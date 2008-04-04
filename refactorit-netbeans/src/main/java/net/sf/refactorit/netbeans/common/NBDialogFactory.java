/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.SwingDialogFactory;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.awt.HeadlessException;


/**
 * Adaptor of NetBeans API for dialog factoring.
 * 
 * @author Igor Malinin
 */
public class NBDialogFactory extends SwingDialogFactory {
  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext)
   */
  public RitDialog createDialog(IdeWindowContext context) {
    return new NBDialog(context);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext)
   */
  public RitDialog createDialog(IdeWindowContext context, JOptionPane pane) {
    return new NBDialog(context, pane);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showFileDialog(net.sf.refactorit.ui.module.RefactorItContext, javax.swing.JFileChooser)
   */
  public int showFileDialog(IdeWindowContext context, JFileChooser chooser) {
    return chooser.showDialog(((AWTContext) context).getWindow(), null);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showInputDialog(net.sf.refactorit.ui.module.RefactorItContext, java.lang.Object, java.lang.String, int, java.lang.Object[], java.lang.Object)
   */
  public Object showInputDialog(
      IdeWindowContext context, Object message, String title,
      int messageType, Object[] selectionValues, Object initialSelectionValue
  ) {
    return JOptionPane.showInputDialog(
        ((AWTContext) context).getWindow(),
        message, title, messageType,
        null, selectionValues, initialSelectionValue);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showOptionDialog(net.sf.refactorit.ui.module.RefactorItContext, java.lang.Object, java.lang.String, int, int, javax.swing.Icon, java.lang.Object[], java.lang.Object)
   */
  public int showOptionDialog(
      IdeWindowContext context, Object message, String title,
      int optionType, int messageType, Object[] options, Object initialValue
  ) throws HeadlessException {
      return JOptionPane.showOptionDialog(
          ((AWTContext) context).getWindow(),
          message, title, optionType, messageType,
          null, options, initialValue);
  }
}
