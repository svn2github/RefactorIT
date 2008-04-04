/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.move;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.ConflictsTreeModel;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.tree.NodeIcons;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Shows conflicts during MoveType action. User can resolve them with help
 * of navigate buttons.
 *
 * @author Vladislav Vislogubov
 */
public class MoveTypeConflictViewer {
  private static final Logger log = Logger.getLogger(MoveTypeConflictViewer.class);

  final RitDialog dialog;

  boolean okPressed;

  private JButton buttonOk = new JButton("Ok");

  JButton resolveConflictButton = new JButton("Resolve Conflict",
      ResourceUtil.getIcon(UIResources.class, "arrow_up.gif"));
  JButton removeTypeButton = new JButton("Remove Type",
      ResourceUtil.getIcon(UIResources.class, "Remove.gif"));

  private ActionListener cancelActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      okPressed = false;
      dialog.dispose();
    }
  };

  class TypesListModel extends DefaultListModel {
    MoveType mover;

    public TypesListModel(MoveType mover) {
      super();
      this.mover = mover;
    }

    public void update() {
      clear();

      List types = this.mover.getTypes();
      Collections.sort(types, new BinCIType.QualifiedNameSorter());
      for (int i = 0; i < types.size(); i++) {
        addElement(types.get(i));
      }
    }

    public List getTypes() {
      List types = new ArrayList();
      for (int i = 0; i < super.getSize(); i++) {
        CollectionUtil.addNew(types, super.getElementAt(i));
      }

      return types;
    }
  }

  // TODO HtmlTableCellRenderer?
  private class TypesListRenderer extends JLabel implements ListCellRenderer {
    public TypesListRenderer() {
      setOpaque(true);
    }

    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      final BinCIType type = (BinCIType) value;
      setText(type.getQualifiedName());

      this.setFont(list.getFont());

      if (isSelected) {
        this.setBackground(list.getSelectionBackground());
        this.setForeground(list.getSelectionForeground());
      } else {
        this.setBackground(list.getBackground());
        this.setForeground(list.getForeground());
      }

      int nodeType;
      if (type.isClass()) {
        nodeType = UITreeNode.NODE_CLASS;
      } else if (type.isEnum()) {
        nodeType = UITreeNode.NODE_ENUM;
      } else {
        nodeType = UITreeNode.NODE_INTERFACE;
      }
      setIcon(NodeIcons.getBinIcon(nodeType, type, false));

      return this;
    }
  }

  TypesListModel listModel;
  JList list;
  MoveType mover;
  BinTreeTable tree;

  private ConflictsTreeModel treeModel;

  private RefactorItContext context;

  public MoveTypeConflictViewer(RefactorItContext context, MoveType mover) {
    this.context = context;
    this.mover = mover;

    dialog = RitDialog.create(context);
    dialog.setTitle("Conflict viewer");

    init();
  }

  public void cleanup() {
    listModel.mover = null;
    this.mover = null;
    listModel = null;
    dialog.dispose();
  }

  RefactoringStatus status;

  public boolean display() {
    listModel.update();

    status = null;

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          status = checkUserInput(mover.resolveConflicts(), mover);
        }
      }, true);
    } catch (SearchingInterruptedException ex) {
      status.clear();
      status.addEntry("", RefactoringStatus.CANCEL);
    }

    if (status == null || status.isCancel()) {
      okPressed = false;
      dialog.dispose();
      return true;
    }

    treeModel.update(status);
    if (!status.isOk()) {
      show();
    } else {
      okPressed = true;
    }

    return true;
  }

  public void show() {
    okPressed = false;
    dialog.show();
  }

  public boolean isOkPressed() {
    return this.okPressed;
  }

  private void init() {

    //DialogManager.optimizeDialogSize(this);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    resolveConflictButton.setEnabled(false);
    removeTypeButton.setEnabled(false);

    resolveConflictButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resolveConflictPressed();
      }
    });

    removeTypeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] selected = list.getSelectedIndices();
        if (selected.length == 0) {
          return;
        }

        for (int i = 0; i < selected.length; i++) {
          listModel.removeElementAt(selected[i]);
        }

        removeTypeButton.setEnabled(false);

        removeTypePressed();
      }
    });

    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        int[] selected = list.getSelectedIndices();
        if (list.getModel().getSize() > 1 && selected.length > 0) {
          removeTypeButton.setEnabled(true);
        } else {
          removeTypeButton.setEnabled(false);
        }
      }
    });

    /*    list.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            int[] selected = list.getSelectedIndices();
            if (selected.length > 0) {
              removeTypeButton.setEnabled(true);
            } else {
              removeTypeButton.setEnabled(false);
            }
          }

          public void focusLost(FocusEvent e) {
            removeTypeButton.setEnabled(false);
          }
        });*/

    if (tree != null) {
      tree.getSelectionModel().addListSelectionListener(
          new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) {
            return;
          }

          int[] selected = tree.getSelectedRows();
          if (tree.getBinTreeTableModel().getChildCount(
              tree.getBinTreeTableModel().getRoot()) > 0 &&
              selected.length > 0) {
            resolveConflictButton.setEnabled(true);
          } else {
            resolveConflictButton.setEnabled(false);
          }
        }
      });

    }
  }

  private JComponent createMainPanel() {
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
    center.add(createMessagePanel(), constraints);

    constraints.insets = new Insets(0, 5, 0, 5);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    center.add(getListComponent(), constraints);

    constraints.insets = new Insets(5, 5, 2, 5);
    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    center.add(getNavigateButtonsComponent(), constraints);

    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 2.0;
    center.add(getTableComponent(), constraints);

    return center;
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "There was found a number of conflicts arising when moving given type(s). "
        +
        "You can try to resolve them or continue and fix them later manually."
        );
  }

  private JComponent getListComponent() {
    listModel = new TypesListModel(this.mover);
    list = new JList(listModel);
    list.setCellRenderer(new TypesListRenderer());
    list.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
    list.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
    list.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
    list.setSelectionBackground(
        Color.decode(GlobalOptions.getOption("tree.selection.background")));
    list.setSelectionForeground(
        Color.decode(GlobalOptions.getOption("tree.selection.foreground")));

    JScrollPane pane = new JScrollPane(list);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    TitledBorder tb = BorderFactory.createTitledBorder("Type(s) to move");
    tb.setTitleColor(Color.black);
    pane.setBorder(tb);

    return pane;
  }

  private JComponent getTableComponent() {
    treeModel = new ConflictsTreeModel();
    tree = new BinTreeTable(treeModel, this.context);
    treeModel.setTree(tree);
    tree.expandAll();
    tree.getTree().setRootVisible(false);
    tree.getTree().setShowsRootHandles(true);
    tree.setTableHeader(null);
    tree.setListenForEnterKey(false);

    JScrollPane pane = new JScrollPane(tree);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    TitledBorder tb = BorderFactory.createTitledBorder("Conflicts and warnings");
    tb.setTitleColor(Color.black);
    pane.setBorder(tb);

    return pane;
  }

  private JComponent getNavigateButtonsComponent() {
    JPanel center = new JPanel(new GridLayout(1, 2, 2, 2));
    //center.setBorder( BorderFactory.createTitledBorder( "Factory Method Entry") );
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    //center.setBorder(BorderFactory.createEtchedBorder());

    center.add(resolveConflictButton);
    center.add(removeTypeButton);

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    p.add(center, constraints);

    return p;
  }

  private JComponent createButtonsPanel() {
    JButton buttonCancel = new JButton("Cancel");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;
        // TODO run resolve on all top nodes, if user forgot to do it himself?
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel.addActionListener(cancelActionListener);
    buttonPanel.add(buttonCancel);

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.movetype");
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

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp,
        cancelActionListener);

    return downPanel;
  }

  RefactoringStatus userInputCheckStatus;

  void resolveConflictPressed() {
    TreePath path = tree.getTree().getSelectionPath();
    if (path == null) {
      return;
    }
    BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();

    List types = listModel.getTypes();
    int size_before = types.size();

    resolveNode(types, node);

    if (types.size() == size_before) {
      return;
    }

    this.mover.setTypes(types);
    listModel.update();

    userInputCheckStatus = null;

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          userInputCheckStatus = mover.checkUserInput();
        }
      }, true);
    } catch (SearchingInterruptedException ex) {
      userInputCheckStatus = null;
    }

    if (userInputCheckStatus == null || userInputCheckStatus.isCancel()) {
      okPressed = false;
      dialog.dispose();
    }
    treeModel.update(userInputCheckStatus);
  }

  private void resolveNode(final List types, final BinTreeTableNode node) {
    if (node.getBin() instanceof BinMember) {
      resolveMember(types, (BinMember) node.getBin());
    } else if (node.getBin() instanceof CompilationUnit) {
      resolveSource((CompilationUnit) node.getBin());
    } else if (node.getBin() instanceof RefactoringStatus.Entry) {
      resolveStatusEntry(types, (RefactoringStatus.Entry) node.getBin());

      final List children = node.getAllChildren();
      for (int i = 0; i < children.size(); ++i) {
        resolveNode(types, (BinTreeTableNode) children.get(i));
      }
    }
  }

  private boolean resolveStatusEntry(final List types,
      final RefactoringStatus.Entry entry) {
    if (entry.getBin() instanceof BinMember) {
      resolveMember(types, (BinMember) entry.getBin());
      return true;
    } else if (entry.getBin() instanceof CompilationUnit) {
      resolveSource((CompilationUnit) entry.getBin());
      return true;
    } else {
      return false;
    }
  }

  private void resolveMember(List types, BinMember resolve) {
    do {
      if (resolve instanceof BinCIType
          && !((BinCIType) resolve).isAnonymous()) {
        CollectionUtil.addNew(types, resolve);
        break;
      } else {
        BinTypeRef owner = resolve.getOwner();
        if (owner != null) {
          resolve = owner.getBinCIType();
        } else {
          break;
        }
      }
    } while (true);
  }

  private void resolveSource(CompilationUnit resolve) {
    if (!resolve.getSource().canWrite()) {
      resolve.getSource().startEdit();
    }
  }

  void removeTypePressed() {
    this.mover.setTypes(listModel.getTypes());
    final RefactoringStatus status = mover.checkUserInput();
    if (status.isCancel()) {
      okPressed = false;
      dialog.dispose();
    }
    treeModel.update(status);
  }

  public static void main(String[] args) {
    final NullContext context = new NullContext(null);
    MoveTypeConflictViewer d = new MoveTypeConflictViewer(
        context, new MoveType(context, null));
    d.show();

    System.exit(0);
  }

  public static RefactoringStatus checkUserInput(List typesToAddToMove,
      MoveType mover) {
    if (typesToAddToMove.isEmpty()) {
      return mover.checkUserInput();
    }

    StringBuffer message = new StringBuffer("Will also move class ");
    for (int i = 0; i < typesToAddToMove.size(); i++) {
      BinType type = (BinType) typesToAddToMove.get(i);
      if (i != 0) {
        if (i == typesToAddToMove.size() - 1) {
          message.append(" and ");
        } else {
          message.append(", ");
        }
      }
      message.append("'");
      message.append(type.getName());
      message.append("'");
    }
    message.append(". Continue?");

    int choice = DialogManager.getInstance().showYesNoQuestion(
        IDEController.getInstance().createProjectContext(),
        "add.more.types.to.moveclass.operation",
        message.toString(), DialogManager.YES_BUTTON);
    if (choice == DialogManager.YES_BUTTON) {
      return mover.checkUserInput();
    }
    return new RefactoringStatus("", RefactoringStatus.CANCEL);
  }
}
