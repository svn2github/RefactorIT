/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;



import net.sf.refactorit.refactorings.ConflictsTreeModel;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Shows conflicts during {@link net.sf.refactorit.refactorings.Refactoring}
 * validity checks.
 *
 * @author Anton Safonov
 */
public class RefactoringStatusViewer {
  final RitDialog dialog;

  boolean okPressed = false;

  BinTreeTable tree;

  private JButton buttonOk = new JButton("Ok");

  private ConflictsTreeModel treeModel;

  public RefactoringStatusViewer(
      RefactorItContext context, String helpText, String helpTopic
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Conflict viewer");

    init(context, helpText, helpTopic);
  }

  public void display(RefactoringStatus status) {
    if (status.isCancel()) {
      okPressed = false;
      dialog.dispose();
    }
    treeModel.update(status);
    if (!status.isOk()) {
      okPressed = false;
      dialog.show();
    } else {
      okPressed = true;
    }
  }

  /**
   * @deprecated use {@link #display}
   */
  public void show() {
    okPressed = false;
    dialog.show();
  }

  public boolean isOkPressed() {
    return this.okPressed;
  }

  private void init(RefactorItContext context, String helpText, String helpTopic) {
    dialog.setSize(600, 300);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(context, helpText), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(helpTopic), BorderLayout.SOUTH);

    if (tree != null) {
      tree.getSelectionModel().addListSelectionListener(
          new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) {
            return;
          }

          int[] selected = tree.getSelectedRows();
          if (tree.getBinTreeTableModel().getChildCount(
              tree.getBinTreeTableModel().getRoot()) > 0
              && selected.length > 0) {

          } else {

          }
        }
      });
    }

    SwingUtil.invokeLater(new Runnable() {
      public void run() {
        buttonOk.requestFocus();
      }
    } );
  }

  private JComponent createMainPanel(RefactorItContext context, String helpText) {
    JPanel center = new JPanel(new GridBagLayout());
    //center.setBorder( BorderFactory.createTitledBorder( "Factory Method Entry") );
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    center.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    center.add(createMessagePanel(helpText), constraints);

    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 2.0;
    center.add(getTableComponent(context), constraints);

    return center;
  }

  private JPanel createMessagePanel(String text) {
    return DialogManager.getHelpPanel(text);
  }

  private JComponent getTableComponent(RefactorItContext context) {
    treeModel = new ConflictsTreeModel();
    tree = new BinTreeTable(treeModel, context);
    treeModel.setTree(tree);
    tree.setListenForEnterKey(false);
    tree.expandAll();
    tree.getTree().setRootVisible(false);
    tree.getTree().setShowsRootHandles(true);
    tree.setTableHeader(null);

    JScrollPane pane = new JScrollPane(tree);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

//    TitledBorder tb = BorderFactory.createTitledBorder("Conflict(s)");
//    tb.setTitleColor(Color.black);
//    pane.setBorder(tb);

    return pane;
  }

  private JComponent createButtonsPanel(String helpTopic) {
    JButton buttonCancel = new JButton("Cancel");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    // NOTE: we don't need a Cancel button in the plain viewer, right?
    buttonCancel.setVisible(false);
//    buttonCancel.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        okPressed = false;
//        dispose();
//      }
//    });
//    buttonPanel.add(buttonCancel);

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, helpTopic);
    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel,
        buttonHelp, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = false;
        dialog.dispose();
      } } );

    return downPanel;
  }

  public static void main(String[] args) {
    final NullContext context = new NullContext(null);

    RefactoringStatusViewer d = new RefactoringStatusViewer(
        context, "Test", "refact");
    d.show();

    System.exit(0);
  }
}
