/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;

import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;



/**
 * Base class for rename dialogs.
 * 
 * NB! Dialog must be disposed explicitly!
 * 
 * @author Igor Malinin
 */
abstract class AbstractRenameDialog {
  final static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(JRenameMemberDialog.class);

  private final IdeWindowContext context;
  private final String title;

  private final String helpContextId;

  final Container content;

  final JTextField renameTo = new JTextField();

  final JButton rename = new JButton(
      resLocalizedStrings.getString("button.rename"));

  final JButton cancel = new JButton(
      resLocalizedStrings.getString("button.cancel"));

  final JButton help = new JButton("Help");

  RitDialog dialog;
  boolean okPressed;
  
  AbstractRenameDialog(
      IdeWindowContext context, String title, String helpContextId
  ) {
    this.context = context;
    this.title = title;
    this.helpContextId = helpContextId;

    content = createContentPane();
  }

  protected abstract Container createContentPane();

  protected final JPanel createButtonPanel() {
    JPanel south = new JPanel(new GridBagLayout());

    JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 3, 3));

    rename.setMnemonic(KeyEvent.VK_R);
    cancel.setMnemonic(KeyEvent.VK_C);
    help.setMnemonic(KeyEvent.VK_H);

    // Attach components
    buttonPanel.add(rename);
    buttonPanel.add(cancel);
    buttonPanel.add(help);

    cancel.setDefaultCapable(false);

    GridBagConstraints con = new GridBagConstraints();
    con.anchor = GridBagConstraints.EAST;
    con.weightx = 1.0;
    con.insets = new Insets(3, 10, 3, 20);
    south.add(buttonPanel, con);

    // Add action listeners
    rename.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        okPressed = true;
        dialog.dispose();
      }
    });

    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        okPressed = false;
        dialog.dispose();
      }
    });

    return south;
  }

  /**
   * NB! Dialog must be disposed explicitly!
   */
  public void show() {
    okPressed = false;

    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setContentPane(content);

    HelpViewer.attachHelpToDialog(dialog, help, this.helpContextId);

    JRootPane root = dialog.getRootPane();
    root.setDefaultButton(rename);

    final String key = "closeActionOfRename";
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    final Action act = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };

    root.getInputMap().put(stroke, key);
    root.getActionMap().put(key, act);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            renameTo.requestFocus();
            renameTo.selectAll();

            renameTo.getInputMap().put(stroke, key);
            renameTo.getActionMap().put(key, act);
          }
        });
      }

      public void windowClosing(WindowEvent windowEvent) {
        dialog.removeWindowListener(this);
        dialog.dispose();
      }
    });

    dialog.show();
  }

  public boolean isOkPressed() {
    return this.okPressed;
  }

  public String getNewName() {
    return renameTo.getText().trim();
  }
  
  public void setNewName(String name) {
    this.renameTo.setText(name);
  }
}
