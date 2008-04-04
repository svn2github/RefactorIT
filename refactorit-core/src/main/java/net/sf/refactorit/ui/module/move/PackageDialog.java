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
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.PackageModel;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.Source;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Dialog for selecting/creating packages.
 *
 * @author Anton Safonov
 * @author Vladislav Vislogubov (redesigned all UI here)
 *                               (created source path choosing logic)
 */
public class PackageDialog {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(PackageDialog.class);

  private static final String CHANGE_MEMBER_ACCESS_OPTION =
      "movetype.change_member_access";
  private static final String CHANGE_IN_NON_JAVA_FILES =
      "movetype.change_in_nonjava_files";

  final RitDialog dialog;

  PackageModel model;

  private BinTreeTableNode selectedNode;

  private RefactorItContext context;
  private Object target;

  private Source[] sources;
  private JComboBox pathes;

  private BinTreeTable table;
  private JButton addButton;
  private JButton removeButton;
  private JButton okButton;

  private JCheckBox changeMemberAccess = new JCheckBox(
      "Widen access modifiers if neccessary",
      GlobalOptions.getOptionAsBoolean(CHANGE_MEMBER_ACCESS_OPTION, true));

  private JCheckBox changeInNonJavaFiles = new JCheckBox(
      "Change in non-java files",
      GlobalOptions.getOptionAsBoolean(CHANGE_IN_NON_JAVA_FILES, true));

  public PackageDialog(final RefactorItContext context, final Object target) {
    this.context = context;
    this.target = target;

    dialog = RitDialog.create(context);

    JPanel sourcePath = createSourcePathPanel(target);

    dialog.setSize(600, 450);

    dialog.setTitle("Select destination for " + MoveTypeAction.getName(target));

    final List openPackages = new ArrayList();
    if (target instanceof Object[]) {
      for (int i = 0; i < ((Object[]) target).length; i++) {
        CollectionUtil.addNew(openPackages,
            ((BinCIType) ((Object[]) target)[i]).getPackage());
      }
    } else if (target instanceof BinCIType) {
      CollectionUtil.addNew(openPackages, ((BinCIType) target).getPackage());
    }

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          AbstractIndexer.runWithProgress(ProgressMonitor.Progress.FULL,
              new Runnable() {
            public void run() {
              model = new PackageModel(context.getProject(),
                  getTargetSource(), openPackages);
            }
          });
        }
      }, true);
    } catch (SearchingInterruptedException ex) {}

    table = new BinTreeTable(this.model, context);
    table.setTableHeader(null);
    table.setListenForEnterKey(false);
    table.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);

    table.getTree().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        BinTreeTableNode node = null;

        if (path != null) {
          node = (BinTreeTableNode) path.getLastPathComponent();
        }

        if (PackageDialog.this.getSelectedNode() != node) {
          PackageDialog.this.setSelectedNode(node);
        }
      }
    });

    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(table.getBackground());
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    addButton = new JButton(resLocalizedStrings.getString("button.add"));
    addButton.setMnemonic(KeyEvent.VK_A);
    addButton.setDefaultCapable(false);
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PackageDialog.this.addPackageNode();
      }
    });

    removeButton = new JButton(resLocalizedStrings.getString("button.remove"));
    removeButton.setMnemonic(KeyEvent.VK_R);
    removeButton.setDefaultCapable(false);
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PackageDialog.this.removePackageNode();
      }
    });

    okButton = new JButton(resLocalizedStrings.getString("button.ok"));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GlobalOptions.setOption(CHANGE_MEMBER_ACCESS_OPTION, "" + isChangeMemberAccess());
        GlobalOptions.setOption(CHANGE_IN_NON_JAVA_FILES, "" + isChangeInNonJavaFiles());

        dispose();
      }
    });

    JButton cancelButton = new JButton(
        resLocalizedStrings.getString("button.cancel"));
    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PackageDialog.this.setSelectedNode(null);
        dispose();
      }
    };
    cancelButton.addActionListener(cancelActionListener);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        PackageDialog.this.setSelectedNode(null);
        dispose();
      }
    });

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.movetype");

    JPanel actionsPanel = new JPanel();
    actionsPanel.setLayout(new GridLayout(1, 2, 4, 0));
    actionsPanel.add(addButton);
    actionsPanel.add(removeButton);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(buttonHelp);

    JPanel south = new JPanel();
    south.setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.insets = new Insets(4, 10, 4, 10);
    south.add(actionsPanel, constraints);
    south.add(buttonPanel, constraints);

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder()));

    main.add(DialogManager.getHelpPanel(
        "Choose physical location and" +
        " desired destination package in it." +
        "\n Use Add/Remove to create/delete package."),
        BorderLayout.NORTH);

    JPanel central = new JPanel(new BorderLayout());
    central.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    /*
         central.setBorder( BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder( 5, 5, 5, 5 ),
        BorderFactory.createEtchedBorder() )
         );
     */
    central.add(sourcePath, BorderLayout.NORTH);
    central.add(scroll, BorderLayout.CENTER);

    main.add(central, BorderLayout.CENTER);

    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(main, BorderLayout.CENTER);
    dialog.getContentPane().add(south, BorderLayout.SOUTH);

    table.selectNode(this.model.getOpenNode());
    setSelectedNode(this.model.getOpenNode());

    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, buttonHelp,
        cancelActionListener);
  }

  public boolean isChangeMemberAccess() {
    return changeMemberAccess.isSelected();
  }

  public boolean isChangeInNonJavaFiles() {
    return changeInNonJavaFiles.isSelected();
  }

  private JPanel createSourcePathPanel(Object target) {
    JPanel panel = new JPanel(new GridBagLayout());
    //panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

    sources = context.getProject().getPaths().getSourcePath().getRootSources();
    if (sources == null || sources.length == 0) {
      return panel;
    }
    if (target instanceof Object[]) {
      target = ((Object[]) target)[0];
    }
    String targetPath = ((BinMember) target).getCompilationUnit()
        .getSource().getAbsolutePath() + File.separator;

    Object[] names = new Object[sources.length];
    int selected = -1;
    for (int i = 0; i < sources.length; i++) {
      names[i] = sources[i].getDisplayPath();
      if (selected < 0 && targetPath.startsWith(
          sources[i].getAbsolutePath() + File.separator)) {
        selected = i;
      }
    }
    if (selected < 0) {
      selected = 0;
    }

    pathes = new JComboBox(names);
    pathes.setSelectedIndex(selected);

    pathes.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        PackageDialog.this.model.reload(getTargetSource());
      }
    });

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 0, 5, 5);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 1;
    panel.add(new Label("Package physical location"), constraints);

    constraints.insets = new Insets(0, 0, 5, 0);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 2.0;
    constraints.weighty = 1.0;
    constraints.gridx = 2;
    constraints.gridy = 1;
    panel.add(pathes, constraints);

    constraints.insets = new Insets(0, 0, 5, 0);
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 2;
    panel.add(changeMemberAccess, constraints);

    constraints.insets = new Insets(0, 0, 5, 0);
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridx = 1;
    constraints.gridy = 3;
    panel.add(changeInNonJavaFiles, constraints);

    return panel;
  }

  public Source getTargetSource() {
    if (pathes == null || sources == null) {
      return null;
    }

    return sources[pathes.getSelectedIndex()];
  }

  BinTreeTableNode getSelectedNode() {
    return this.selectedNode;
  }

  void setSelectedNode(BinTreeTableNode selectedNode) {
    this.selectedNode = selectedNode;

    this.addButton.setEnabled(this.selectedNode != null);

    this.removeButton.setEnabled(isRemoveAllowed(this.selectedNode));

    this.okButton.setEnabled(isSelectAllowed(this.selectedNode));
  }

  private boolean isSelectAllowed(final BinTreeTableNode node) {
    if (node == null) {
      return false;
    }

    if (BinTreeTableNode.class.equals(node.getClass())) {
      return false;
    }

    // we can not move to the same package, only extract
    if (node == model.getOpenNode()) { // TODO support multiply types, now checks for first only
      if (target instanceof Object[]) {
        List types = Arrays.asList((Object[]) target);

        for (int i = 0; i < ((Object[]) target).length; i++) {
          if (!MoveType.isExtract(
              types, (BinCIType) ((Object[]) target)[i])) {
            return false;
          }
        }
      } else if (target instanceof BinCIType) {
        List types = new ArrayList(1);
        types.add(target);

        if (!MoveType.isExtract(types, (BinCIType) target)) {
          return false;
        }
      }
    }

    String packName = node.getFullName();

    if (packName != null) {
      Source src = getTargetDir(packName);

      if (src.isIgnored(context.getProject())) {
        RitDialog.showMessageDialog(
            IDEController.getInstance().createProjectContext(),
            "You are trying to move type into ignored source. " +
            "Are you sure you want to do it?",
            "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }

    return true;
  }

  private Source getTargetDir(String relativeDirName) {
    Source targetSource = this.getTargetSource();

    String[] tokens = relativeDirName.split("\\.");

    boolean found = true;

    for (int n = 0; n < tokens.length; n++) {
      String targetName =
        BinPackage.convertPathToPackageName(targetSource.getAbsolutePath());

      Source[] children = targetSource.getChildren();

      for (int i = 0; i < children.length; i++) {
        String childName =
            BinPackage.convertPathToPackageName(children[i].getAbsolutePath());

        if (childName.equals(targetName + "." + tokens[n])) {
          targetSource = children[i];
          found = true;
          break;
        }
      }

      if (!found) {
        //should never get here
        System.err.println("PackageDialog.getTargetDir: cannot find" +
            "source for name " + tokens[n - 1] + " in path "
            + targetSource.getAbsolutePath());
        return targetSource;
      } else {
        found = false;
      }
    }

    return targetSource;
  }

  private boolean isRemoveAllowed(final BinTreeTableNode node) {
    if (node == null || isRootNode(node)) {
      return false;
    }

    if (node.getChildCount() > 0) { // remove children first
      return false;
    }

    if (node.getBin() instanceof BinPackage) {
      final BinPackage pack = (BinPackage) node.getBin();

      if (pack.getAllTypes().hasNext()) { // has sources within
        return false;
      }
    }

    return true;
  }

  private boolean isRootNode(BinTreeTableNode node) {
    return BinTreeTableNode.class.equals(node.getClass());
  }

  void addPackageNode() {
    if ("".equals(getSelectedNode().getDisplayName())) {
      RitDialog.showMessageDialog(context,
          "Default (nameless) packages cannot have subpackages",
          "Cannot add", JOptionPane.ERROR_MESSAGE);
      return;
    }

    String newPackageName = RitDialog.showInputDialog(context,
        "Enter non-qualified name of new package:",
        "New package", JOptionPane.QUESTION_MESSAGE);

    if (newPackageName == null) {
      return;
    }

    if ("".equals(newPackageName) && (!isRootNode(getSelectedNode()))) {
      RitDialog.showMessageDialog(context,
          "Default (nameless) packages can only be created under the top node (the \"Packages\" node).",
          "Default package", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (!NameUtil.isValidIdentifier(newPackageName)) {
      RitDialog.showMessageDialog(context,
          "Invalid package name",
          "Invalid name", JOptionPane.ERROR_MESSAGE);
      return;
    }

    newPackageName = newPackageName.trim();

    if (containsChildWithName(getSelectedNode(), newPackageName)) {
      RitDialog.showMessageDialog(context,
          "Package already exists: \"" + newPackageName + "\"",
          "Already exists", JOptionPane.ERROR_MESSAGE);
      return;
    }

    PackageModel.PackageNode node
        = new PackageModel.PackageNode(newPackageName);
    getSelectedNode().addChild(node);
    this.model.updateNode(node);

    setSelectedNode(node);
    this.table.selectNode(node); // also asks for focus
  }

  private boolean containsChildWithName(BinTreeTableNode node, String nodeName) {
    for (Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
      if (nodeName.equals(i.next().toString())) {
        return true;
      }
    }

    return false;
  }

  void removePackageNode() {
    BinTreeTableNode removedNode = getSelectedNode();

//    int row = this.table.getTree().getRowForPath(new TreePath(removedNode.getPath()));

    BinTreeTableNode parent = (BinTreeTableNode) removedNode.getParent();
//    int index = parent.getIndex(removedNode);
    parent.removeChild(removedNode);
//    this.model.updateNode(removedNode);
    // Strange enough, but usual parameters doesn't work - swing falls with NPE
    // this way it collapses a bit higher, but works.
    this.model.packageTreeStructureChanged(this.model, parent.getParent().getPath(),
        null, new Object[]{removedNode});

    setSelectedNode(parent);
    this.table.selectNode(parent); // also asks for focus
  }

  public BinPackage getPackage() {
    final BinTreeTableNode node = getSelectedNode();
    if (node == null) {
      return null;
    }

    BinPackage selected = null;
    if (node.getBin() instanceof BinPackage) {
      selected = (BinPackage) node.getBin();
    } else if (node.getBin() instanceof String) {
      String fullName = ((PackageModel.PackageNode) node).getFullName();
      selected = this.context.getProject().getPackageForName(fullName);
      if (selected == null) {
        selected = this.context.getProject()
            .createPackageForName(fullName, true);
      }
    }
    return selected;
  }

  public void dispose() {
    BinPackage selected = getPackage();

    if (selected != null) {
      updateFilesystem();
    }

    dialog.dispose();
  }

  private boolean updateFilesystem() {
    return true;
  }

  public void show() {
    dialog.show();
  }
}
