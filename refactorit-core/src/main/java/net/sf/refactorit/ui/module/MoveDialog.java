/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.refactorings.movemember.MoveMemberNode;
import net.sf.refactorit.refactorings.movemember.MoveMemberResolvedConflictsModel;
import net.sf.refactorit.refactorings.movemember.MoveMemberUnresolvedConflictsModel;
import net.sf.refactorit.refactorings.movemember.MoveMembersModel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.ui.tree.NodeIcons;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 *
 * @author Anton Safonov
 * @author Vadim Hahhulin
 */
public abstract class MoveDialog {
  boolean isOkPressed;

  protected final RitDialog dialog;

  MoveMemberUnresolvedConflictsModel unresolvedConflictsModel;
  MoveMemberResolvedConflictsModel resolvedConflictsModel;
  MoveMembersModel membersModel;
  BinTable membersTable;

  public JButton buttonOk = new JButton("OK");
  protected JButton buttonCancel = new JButton("Cancel");
  protected JButton buttonHelp = new JButton("Help");

  public ConflictResolver resolver;
  protected RefactorItContext context;

  private JButton resolveConflictButton = new JButton(
      "Resolve Conflict", ResourceUtil.getIcon(UIResources.class, "arrow_up.gif"));

  private BinTreeTable unresolvedConflictsTT;

  public MoveDialog(
      ConflictResolver resolver, RefactorItContext context,
      String title, final String key, String helpKey
  ) {
    this.resolver = resolver;
    this.context = context;

    JPanel contentPanel = new JPanel();
    contentPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(helpKey), BorderLayout.SOUTH);
    //contentPanel.setPreferredSize(DialogManager.getOptimalDialogSize());
    contentPanel.setPreferredSize(new Dimension(640, 480));

    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setContentPane(contentPanel);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, helpKey);
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel,
        buttonHelp);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            membersTable.requestFocus();
            if (membersTable.getRowCount() > 0) {
              membersTable.setRowSelectionInterval(0, 0);
            }
          }
        });
      }

      public void windowClosing(WindowEvent windowEvent) {
        dialog.removeWindowListener(this);
      }
    });

    buttonHelp.setNextFocusableComponent(membersTable);
  }

  protected abstract JComponent createHierarchyPanel();

  private JComponent createButtonsPanel(String helpKey) {
    JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 4, 0));

    buttonOk.setEnabled(false);
    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        membersTable.stopEditing(); // just in case
        isOkPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel.setSelected(true); // ???
    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonCancel);

    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;

    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 20, 3, 0);
    downPanel.add(resolveConflictButton, constraints);
    resolveConflictButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resolveConflict();
        unresolvedConflictsModel.update();
        resolvedConflictsModel.update();
        membersModel.update();
      }
    });

    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonHelp);

    return downPanel;
  }

  private JComponent createMainPanel() {
    JPanel center = new JPanel(new BorderLayout(5, 5));
    center.setBorder(BorderFactory.createEtchedBorder());

    center.add(createMessagePanel(), BorderLayout.NORTH);

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));
    p.add(createCenterPanel(), BorderLayout.CENTER);
    center.add(p, BorderLayout.CENTER);

    return center;
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "Select target class and specify methods and fields to move into it.\n"
        + "Selection list tracks inter-member dependencies automatically."
        );
  }

  private Container createCenterPanel() {
    JComponent classHierarchyPanel = createHierarchyPanel();
    JComponent membersPanel = createMembersPanel();
    JComponent unresolvedConflictsPanel = createUnresolvedConflictsPanel();
    JComponent resolvedConflictsPanel = createResolvedConflictsPanel();

    membersModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        unresolvedConflictsModel.update();
        resolvedConflictsModel.update();
        updateResolveConflictButton();
        updateOkButton();
      }
    });

    JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    topSplit.setDividerSize(3);
    topSplit.setLeftComponent(membersPanel);
    topSplit.setRightComponent(classHierarchyPanel);

    JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    bottomSplit.setDividerSize(3);
    bottomSplit.setLeftComponent(unresolvedConflictsPanel);
    bottomSplit.setRightComponent(resolvedConflictsPanel);

    JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    verticalSplit.setDividerSize(3);
    verticalSplit.setTopComponent(topSplit);
    verticalSplit.setBottomComponent(bottomSplit);

    verticalSplit.setBorder(BorderFactory.createEmptyBorder());
    topSplit.setBorder(BorderFactory.createEmptyBorder());
    bottomSplit.setBorder(BorderFactory.createEmptyBorder());

    verticalSplit.setResizeWeight(0.62);
    verticalSplit.setDividerSize(3);

    topSplit.setResizeWeight(0.5);
    topSplit.setDividerSize(3);
    bottomSplit.setResizeWeight(0.5);
    bottomSplit.setDividerSize(3);

    JPanel vertical = new JPanel(new BorderLayout(0, 2));
    JPanel horizontal = new JPanel(new BorderLayout(7, 0));
    horizontal.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    horizontal.add(new JLabel("Initial class name:"), BorderLayout.WEST);
    JTextField initialTypeName = new JTextField(
        resolver.getNativeType().getQualifiedName());
    initialTypeName.setEditable(false);
    initialTypeName.setEnabled(false);
    initialTypeName.setDisabledTextColor(Color.black);
    horizontal.add(initialTypeName, BorderLayout.CENTER);
    vertical.add(horizontal, BorderLayout.NORTH);
    vertical.add(verticalSplit, BorderLayout.CENTER);

    return vertical;
  }

  private JPanel createMembersPanel() {
    JPanel membersPanel = new JPanel(new BorderLayout());
    membersPanel.setBorder(BorderFactory.createTitledBorder("Members to move"));

    membersModel = new MoveMembersModel(resolver);
    membersTable = new BinTable(membersModel);

    membersTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent ke) {
        int kc = ke.getKeyCode();
        if (kc == KeyEvent.VK_SPACE) {
          if (membersTable.getSelectedColumn() == 0) {
            return;
          }

          int[] rows = membersTable.getSelectedRows();
          if (rows == null || rows.length != 1) {
            return;
          }
          int row = rows[0];

          MoveMemberNode node = membersModel.getRow(row);
          node.toggle();
          membersTable.tableChanged(new TableModelEvent(membersModel, row, row));

          ke.consume();
        }
      }
    });

    membersTable.setDefaultRenderer(
        Boolean.class, new MoveMemberTableRenderer());
    membersTable.setDefaultRenderer(
        MoveMemberNode.class, new MoveMemberTableRenderer());
    membersTable.getTableHeader().setReorderingAllowed(false);
    membersTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    fixColumnSizes();
    final JScrollPane membersScroll = new JScrollPane(membersTable);
    membersScroll.getViewport().setBackground(membersTable.getBackground());

    membersPanel.add(membersScroll, BorderLayout.CENTER);

    return membersPanel;
  }

  private JPanel createResolvedConflictsPanel() {
    BinTreeTable resolvedConflictsTT;
    JPanel conflictsPanel = new JPanel(new BorderLayout());
    conflictsPanel.setBorder(BorderFactory.createTitledBorder("Future changes"));
    resolvedConflictsModel = new MoveMemberResolvedConflictsModel(
        new RefactoringStatus().addEntry(""), resolver);
    resolvedConflictsTT = new BinTreeTable(resolvedConflictsModel, context);
    resolvedConflictsTT.getTree().setRootVisible(false);
    resolvedConflictsTT.getTree().setShowsRootHandles(true);
    resolvedConflictsModel.setTreeTable(resolvedConflictsTT);

    resolvedConflictsTT.setTableHeader(null);
    resolvedConflictsTT.getTree().getSelectionModel()
        .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    resolvedConflictsTT.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);
    conflictsPanel.add(new JScrollPane(resolvedConflictsTT),
        BorderLayout.CENTER);

    resolvedConflictsModel.update();

    return conflictsPanel;
  }

  private JPanel createUnresolvedConflictsPanel() {
    JPanel conflictsPanel = new JPanel(new BorderLayout());
    conflictsPanel.setBorder(BorderFactory.createTitledBorder(
        "Unresolved conflicts"));

    unresolvedConflictsModel = new MoveMemberUnresolvedConflictsModel(
        new RefactoringStatus().addEntry(""), resolver);
    unresolvedConflictsTT = new BinTreeTable(unresolvedConflictsModel, context);
    unresolvedConflictsTT.getTree().setRootVisible(false);
    unresolvedConflictsTT.getTree().setShowsRootHandles(true);
    unresolvedConflictsModel.setTreeTable(unresolvedConflictsTT);

    unresolvedConflictsTT.setTableHeader(null);
    unresolvedConflictsTT.getTree().getSelectionModel()
        .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    unresolvedConflictsTT.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);
    conflictsPanel.add(new JScrollPane(unresolvedConflictsTT),
        BorderLayout.CENTER);

    unresolvedConflictsTT.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          updateResolveConflictButton();
        }
      }
    }
    );

    unresolvedConflictsModel.update();
    membersModel.update();
    updateResolveConflictButton();

    return conflictsPanel;
  }

  void updateResolveConflictButton() {
    TreePath path = unresolvedConflictsTT.getTree().getSelectionPath();
    if (path == null || path.getPath().length <= 1) {
      resolveConflictButton.setEnabled(false);
      return;
    }

    BinTreeTableNode firstChildNode = (BinTreeTableNode) path.getPath()[1];
    Object entryBin = ((RefactoringStatus.Entry) firstChildNode.getBin()).
        getBin();
    if (resolver.getConflictData(entryBin).unresolvableConflictsExist()) {
      resolveConflictButton.setEnabled(false);
    } else {
      resolveConflictButton.setEnabled(true);
    }
  }

  public void show() {
    dialog.show();
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  private void fixColumnSizes() {
    int width = 20;
    membersTable.getColumn(membersTable.getColumnName(0)).setMinWidth(2);
    membersTable.getColumn(membersTable.getColumnName(0)).setPreferredWidth(
        width);
    membersTable.getColumn(membersTable.getColumnName(0)).setWidth(width);
    membersTable.getColumn(membersTable.getColumnName(0)).setMaxWidth(width);
    membersTable.getColumn(membersTable.getColumnName(0)).setResizable(false);
  }

  public void setSelectedClass(BinCIType type) {
    ResolveRunner runner = new ResolveRunner(resolver, type);

    try {
      JProgressDialog.run(context, runner
          , false);
    } catch (SearchingInterruptedException e) {
    }
//    resolver.setTargetType(type); // take much time

    unresolvedConflictsModel.update();
    resolvedConflictsModel.update();
    membersModel.update();

    updateResolveConflictButton();
    updateOkButton();
  }

  void updateOkButton() {
    // FIXME somewhere is a bug that doesn't allow to move into inner?
    boolean result = ((resolver.getTargetType() != null) &&
        (resolver.getAllConflictData().size() > 0) &&
        !(unresolvedConflictsModel.isUnresolvedConflictsExist()) &&
        resolver.getTargetType() != resolver.getNativeType());
    buttonOk.setEnabled(result);
  }

  void resolveConflict() {
    TreePath path = unresolvedConflictsTT.getTree().getSelectionPath();
    if (path == null) {
      return;
    }
    BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();
    RefactoringStatus.Entry entry = (RefactoringStatus.Entry) node.getBin();

    while (entry.getConflict() == null) {
      node = (BinTreeTableNode) node.getParent();
      entry = (RefactoringStatus.Entry) node.getBin();
    }

    Conflict conflict = entry.getConflict();
//    Window oldParent = DialogManager.getDialogParent();

    conflict.resolve();
//    DialogManager.setDialogParent(oldParent);

    if (conflict.isResolved()) {
      resolver.resolveConflicts();
    }
  }

  // TODO HtmlTableCellRenderer?
  public static class MoveMemberTableRenderer implements TableCellRenderer {
    private static final JLabel label = new DefaultTableCellRenderer();
    private static final JCheckBox checkBox = new JCheckBox();

    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      Component component;

      if (value instanceof MoveMemberNode) {
        final MoveMemberNode node = (MoveMemberNode) value;
        label.setIcon(NodeIcons.getBinIcon(node.getType(), node.getBin(), true));
        label.setText(node.getDisplayName());

        component = label;
      } else {
        component = checkBox;
        checkBox.setSelected(((Boolean) value).booleanValue());
        checkBox.setHorizontalAlignment(JLabel.CENTER);
      }

      component.setEnabled(true);
      component.setBackground(table.getBackground());

      if (column != 0) {
        final MoveMemberNode node
            = ((MoveMemberNode) table.getModel().getValueAt(row, 1));
        if (!node.isSelected()) {
          component.setEnabled(false);
          component.setBackground(new Color(240, 240, 240));
        }
      }

      return component;
    }
  }
}


class ResolveRunner implements Runnable {
  private ConflictResolver resolver;
  private BinCIType type;
  public ResolveRunner(ConflictResolver r, BinCIType t) {
    resolver = r;
    type = t;
  }

  public void run() {
    resolver.setTargetType(type);
  }
}
