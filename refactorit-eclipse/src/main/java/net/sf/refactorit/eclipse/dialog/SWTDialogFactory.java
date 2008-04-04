/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse.dialog;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.eclipse.RitPlugin;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.RitDialogFactory;
import net.sf.refactorit.ui.dialog.RitMenuItem;
import net.sf.refactorit.ui.dialog.SwingDialog;
import net.sf.refactorit.ui.dialog.SwingDialogFactory;
import net.sf.refactorit.ui.module.IdeWindowContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/**
 * @author Igor Malinin
 */
public class SWTDialogFactory implements RitDialogFactory {
  private static final class Shadow {
    Shell shell;
    Frame frame;
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext)
   */
  public RitDialog createDialog(IdeWindowContext context) {
    if (context instanceof SWTContext) {
      return new SWTDialog(context);
    }

    return new SwingDialog(context);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#createDialog(net.sf.refactorit.ui.module.RefactorItContext, javax.swing.JOptionPane)
   */
  public RitDialog createDialog(IdeWindowContext context, JOptionPane pane) {
    if (context instanceof SWTContext) {
      return new SWTDialog(context, pane);
    }

    return new SwingDialog(context, pane);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showFileDialog(net.sf.refactorit.ui.module.RefactorItContext, javax.swing.JFileChooser)
   */
  public int showFileDialog(IdeWindowContext context, JFileChooser chooser) {
    if (context instanceof SWTContext) {
      return showFileDialog((SWTContext) context, chooser);
    }

    return chooser.showDialog(((AWTContext) context).getWindow(), null);
  }

  private int showFileDialog(SWTContext context, final JFileChooser chooser) {
    final Shell shell = context.getShell();

    Display display = shell.getDisplay();

    final Shadow shadow = new Shadow();

    display.syncExec(new Runnable() {
      public void run() {
        shadow.shell = new Shell(shell, SWT.APPLICATION_MODAL);
        shadow.shell.setSize(1, 1); // almost invisible

        Composite composite = new Composite(
            shadow.shell, SWTDialog.SHADOW_STYLE);

        try {
          shadow.frame = SWT_AWT.new_Frame(composite);
        } catch (Exception e) {
          AppRegistry.getExceptionLogger().error(e, this);
          RitPlugin.showSwtAwtErrorMessage();
          return;
        }
        shadow.shell.setVisible(true);
      }
    });

    try {
      if (display.getThread() != Thread.currentThread()) {
        return showFileDialog(shadow.frame, chooser);
      }

      final int[] result = {JFileChooser.ERROR_OPTION};

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          result[0] = showFileDialog(shadow.frame, chooser);
        }
      });

      // modal wait
      while (shadow.shell != null) {
        if (!display.readAndDispatch()) {
          display.sleep();
        }
      }

      return result[0];
    } finally {
      display.syncExec(new Runnable() {
        public void run() {
          shadow.shell.dispose();
        }
      });
    }
  }

  int showFileDialog(Frame frame, JFileChooser chooser) {
    // HACK: enable dialog centering
    Rectangle r = frame.getGraphicsConfiguration().getBounds();
    frame.setBounds(r.x, r.y, r.width, r.height);

    return chooser.showDialog(frame, null);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showInputDialog(net.sf.refactorit.ui.module.RefactorItContext, java.lang.Object, java.lang.String, int, java.lang.Object[], java.lang.Object)
   */
  public Object showInputDialog(
      IdeWindowContext context, Object message, String title,
      int messageType, Object[] selectionValues, Object initialSelectionValue
  ) {
    JOptionPane pane = new JOptionPane(
        message, messageType, JOptionPane.OK_CANCEL_OPTION, null, null, null);

    pane.setWantsInput(true);
    pane.setSelectionValues(selectionValues);
    pane.setInitialSelectionValue(initialSelectionValue);

// REVISIT
//    pane.setComponentOrientation(
//        ((parentComponent == null) ? getRootFrame()
//            : parentComponent).getComponentOrientation());

    RitDialog dialog = RitDialog.create(context, pane);
    dialog.setTitle(title);

    pane.selectInitialValue();

    dialog.show();
//    dialog.dispose();

    Object value = pane.getInputValue();

    if (value == JOptionPane.UNINITIALIZED_VALUE) {
        return null;
    }

    return value;
  }


  /*
   * @see net.sf.refactorit.ui.dialog.RitDialogFactory#showOptionDialog(net.sf.refactorit.ui.module.RefactorItContext, java.lang.Object, java.lang.String, int, int, javax.swing.Icon, java.lang.Object[], java.lang.Object)
   */
  public int showOptionDialog(
      IdeWindowContext context, Object message, String title,
      int optionType, int messageType, Object[] options, Object initialValue
  ) throws HeadlessException {
      JOptionPane pane = new JOptionPane(
          message, messageType, optionType, null, options, initialValue);

      pane.setInitialValue(initialValue);

// REVISIT
//      pane.setComponentOrientation(
//          ((parentComponent == null) ? getRootFrame() : parentComponent)
//              .getComponentOrientation());

      RitDialog dialog = RitDialog.create(context, pane);

      pane.selectInitialValue();

      dialog.show();
//      dialog.dispose();

      Object selectedValue = pane.getValue();

      if (selectedValue == null) {
          return JOptionPane.CLOSED_OPTION;
      }

      if (options == null) {
          if (selectedValue instanceof Integer) {
              return ((Integer)selectedValue).intValue();
          }

          return JOptionPane.CLOSED_OPTION;
      }

      for (int counter = 0; counter < options.length; counter++) {
          if (options[counter].equals(selectedValue)) {
              return counter;
          }
      }

      return JOptionPane.CLOSED_OPTION;
  }


  public void showPopupMenu(IdeWindowContext context, final RitMenuItem[] items, boolean showSingleItemMenu) {
    if(!showSingleItemMenu && items.length == 1) {
      items[0].runAction();
      return;
    }
    
    final Point point = context.getPoint();
    final Shell shell = ((SWTContext) context).getShell();
    final Display display = shell.getDisplay();
    
    display.syncExec(new Runnable() {
    
      public void run() {
        Menu menu = new Menu (shell, SWT.POP_UP);
        for(int i = 0; i < items.length; i++) {
          final RitMenuItem ritItem = items[i];
          MenuItem item = new MenuItem (menu, SWT.PUSH);
          item.setText (ritItem.getText());
          item.addListener (SWT.Selection, new Listener () {
            public void handleEvent (Event e) {
              ritItem.runAction();
            }
          });
        }
        menu.setLocation (point.x, point.y);
        menu.setVisible (true);
        while (!menu.isDisposed () && menu.isVisible ()) {
          if (!display.readAndDispatch ()) display.sleep ();
        }
        menu.dispose ();
      }
    });
  }
}
