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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * 
 * 
 * @author Igor Malinin
 */
public interface RitDialogFactory {
  RitDialog createDialog(IdeWindowContext context);
  RitDialog createDialog(IdeWindowContext context, JOptionPane pane);

  int showFileDialog(IdeWindowContext context, JFileChooser chooser);

//  void showMessageDialog(RefactorItContext context,
//          String message, String title, int messageType, String helpId);

  Object showInputDialog(
      IdeWindowContext context,
      Object message, String title, int messageType,
      Object[] selectionValues, Object initialSelectionValue
  );

  int showOptionDialog(
      IdeWindowContext context,
      Object message, String title,
      int optionType, int messageType,
      Object[] options, Object initialValue
  );
  
  
  void showPopupMenu(IdeWindowContext context, RitMenuItem[] items, boolean showSingleItemMenu);
}
