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
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 *
 *
 * @author Igor Malinin
 */
public class SWTDialog extends RitDialog {
  static final int SHADOW_STYLE = SWT.NO_BACKGROUND | SWT.EMBEDDED;

  Shell shadowShell;
  Frame shadowFrame;


  // ===== RitDialog Construction

  SWTDialog(IdeWindowContext owner) {
    super(owner);
  }

  SWTDialog(IdeWindowContext owner, JOptionPane pane) {
    super(owner, pane);
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#createDialogInstance()
   */
  protected JDialog createDialogInstance() {
    createShadow();
    JDialog dialog = createDialogInstance(shadowFrame);
    attachShadow(dialog);
    return dialog;
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#createDialogInstance(javax.swing.JOptionPane)
   */
  protected JDialog createDialogInstance(JOptionPane pane) {
    createShadow();
    JDialog dialog = createDialogInstance(shadowFrame, pane);
    attachShadow(dialog);
    return dialog;
  }

  private void createShadow() {
    getDisplay().syncExec(new Runnable() {
      public void run() {
        shadowShell = new Shell(getOwnerShell(), SWT.APPLICATION_MODAL);
        shadowShell.setSize(1, 1); // almost invisible

        Composite composite = new Composite(shadowShell, SHADOW_STYLE);
        try {
          shadowFrame = SWT_AWT.new_Frame(composite);
        } catch (SWTError e) {
          AppRegistry.getExceptionLogger().error(e, this);
          RitPlugin.showSwtAwtErrorMessage();
          shadowShell.close();
          shadowShell.dispose();
        } catch(Exception e) {
          AppRegistry.getExceptionLogger().error(e, this);
          RitPlugin.showSwtAwtErrorMessage();
          shadowShell.close();
          shadowShell.dispose();
        }
      }
    });
  }

  private void attachShadow(JDialog dialog) {
    dialog.addWindowListener(new WindowAdapter() {
      /*
       * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
       */
      public void windowClosed(WindowEvent e) {
        disposed();
      }
    });

    dialog.addComponentListener(new ComponentAdapter() {
      /*
       * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
       */
      public void componentShown(ComponentEvent e) {
        componentMoved(e);
        componentResized(e);
      }

      /*
       * @see java.awt.event.ComponentAdapter#componentMoved(java.awt.event.ComponentEvent)
       */
      public void componentMoved(ComponentEvent e) {
        Component c = e.getComponent();
        moved(c.getX(), c.getY());
      }

      /*
       * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
       */
      public void componentResized(ComponentEvent e) {
        Component c = e.getComponent();
        setSize(c.getWidth(), c.getHeight());
      }
    });
  }


  // ===== Public RitDialog interface

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#show()
   */
  public void show() {
    if (shadowShell == null) {
      throw new IllegalStateException("The dialog is disposed already!");
    }

    Display display = getDisplay();

    // MUST be invoked from SWT event queue

    display.syncExec(new Runnable() {
      public void run() {
        shadowShell.setVisible(true);
      }
    });

    // MUST NOT be invoked from SWT event queue

    if (display.getThread() != Thread.currentThread()) {
      showDialog();
      return;
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        showDialog();
      }
    });

    // modal wait
    while (shadowShell != null) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  void showDialog() {
    super.show();
    dialog.dispose();
  }

  /*
   * @see net.sf.refactorit.ui.dialog.RitDialog#dispose()
   */
  public void dispose() {
    if (shadowShell == null) {
      IllegalStateException exception = new IllegalStateException();
      Logger log = AppRegistry.getLogger(SWTDialog.class);
      log.error("The dialog was disposed already!", exception);
      return;
    }

    dialog.dispose();
  }


  // ===== Shadow synchronization events

  void disposed() {
    if (shadowFrame == null) {
      return; // fix bug with double windowClosed() event
    }

    shadowFrame = null;

    getDisplay().syncExec(new Runnable() {
      public void run() {
        shadowShell.dispose();
        shadowShell = null;
      }
    });
  }

  void moved(final int x, final int y) {
    getDisplay().syncExec(new Runnable() {
      public void run() {
        shadowShell.setLocation(x, y);
      }
    });
  }


  // ===== Access to SWT context internals

  Shell getOwnerShell() {
    return ((SWTContext) owner).getShell();
  }

  private Display getDisplay() {
    return getOwnerShell().getDisplay();
  }
}
