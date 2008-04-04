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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 *
 * @author Igor Malinin
 */
public class SwingDialogFactory implements RitDialogFactory {
  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext)
   */
  public RitDialog createDialog(IdeWindowContext context) {
    return new SwingDialog(context);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext, javax.swing.JOptionPane)
   */
  public RitDialog createDialog(IdeWindowContext context, JOptionPane pane) {
    return new SwingDialog(context, pane);
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

  public void showPopupMenu(IdeWindowContext context, RitMenuItem[] items,
      boolean showSingleItemMenu) {
    if (!showSingleItemMenu && items.length == 1) {
      items[0].runAction();
      return;
    }

    Point point = context.getPoint();

    JPopupMenu menu = new JPopupMenu();
    for (int i = 0; i < items.length; i++) {
      final RitMenuItem ritItem = items[i];

      JMenuItem item = new JMenuItem(ritItem.getText());
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ritItem.runAction();
        }
      });
      menu.add(item);
    }

    // in StandAlone when user choose Project->GoTo class, there is no any point
    // and no need to show extended 'goto' dialog
    if (point != null) { // ~:)
      menu.show(((AWTContext) context).getWindow(), (int) point.getX(),
          (int) point.getY());
    }  else {
      items[0].runAction();
    }


  }
}
