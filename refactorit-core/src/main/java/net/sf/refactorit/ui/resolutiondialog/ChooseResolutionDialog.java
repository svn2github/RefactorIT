/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.resolutiondialog;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


/**
 *
 * @author vadim
 */
public class ChooseResolutionDialog {
  RitDialog dialog;

  ConflictResolution chosenResolution;

  private BinTreeTable resolutionTreeTable;
  
  private JButton buttonOk;
  private JButton buttonCancel;
  
  private final ActionListener closeActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      chosenResolution = null;
      dialog.dispose();
    }
  };

  public ChooseResolutionDialog(ConflictResolution resolution) {
    this(CollectionUtil.singletonArrayList(resolution));
  }

  public ChooseResolutionDialog(List resolutions) {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createResolutionsPanel(resolutions), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    dialog = RitDialog.create(
        IDEController.getInstance().createProjectContext());
    dialog.setTitle("Possible resolutions for conflict");
    dialog.setContentPane(contentPanel);
    dialog.setSize(600, 300);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, 
        closeActionListener);
  }

  public void show() {
    dialog.show();
  }

  private JPanel createResolutionsPanel(List resolutions) {
    JPanel conflictsPanel = new JPanel(new BorderLayout());
    ResolutionModel model = new ResolutionModel("Possible resolutions",
        resolutions);
    resolutionTreeTable = new BinTreeTable(model, null);
    resolutionTreeTable.setTableHeader(null);

    resolutionTreeTable.getTree().setRootVisible(false);
    resolutionTreeTable.getTree().setShowsRootHandles(true);

//    resolutionTreeTable.getColumnModel().getColumn(1).setMinWidth(5);
//    resolutionTreeTable.getColumnModel().getColumn(1).setPreferredWidth(50);
//    resolutionTreeTable.getColumnModel().getColumn(1).setMaxWidth(100);

    resolutionTreeTable.getTree().getSelectionModel()
        .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    resolutionTreeTable.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);

    resolutionTreeTable.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        setChosenResolution();
      }
    }
    );

    model.expandPath(resolutionTreeTable.getTree());

    conflictsPanel.add(new JScrollPane(resolutionTreeTable),
        BorderLayout.CENTER);
    return conflictsPanel;
  }

  void setChosenResolution() {
    TreePath path = resolutionTreeTable.getTree().getSelectionPath();
    if (path == null) {
      return;
    }

    ResolutionNode firstChildNode = (ResolutionNode) path.getPath()[1];
    chosenResolution = firstChildNode.getResolution();
    buttonOk.setEnabled(true);
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 4, 0));

    buttonOk = new JButton("OK");
    buttonOk.setEnabled(false);
    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel = new JButton("Cancel");
    buttonCancel.setSelected(true);
    buttonCancel.addActionListener(closeActionListener);
    buttonPanel.add(buttonCancel);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);

    return downPanel;
  }

  public ConflictResolution getChosenResolution() {
    return chosenResolution;
  }
}
