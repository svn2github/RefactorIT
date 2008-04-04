/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteral;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Arseni Grigorjev
 */
public class UseExistingFieldDialog extends AbstractNumLitDialog {
  private static final String SELECT_A_FIELD
      = "You must select a field that You wish to use.";

  private TreeRefactorItContext context;
  private BinTypeRef wantToAccessFrom;
  private BinTypeRef returnType;
  private Map usedConstants;

  private SelectFieldFromProjectModel projectModel;
  private BinTreeTable tableRecent;
  private BinTreeTable tableProject;
  private JScrollPane scrollPaneRecent;
  private JScrollPane scrollPaneProject;
  private JTabbedPane tabbedPane;

  // RETURN DATA
  private BinField retUseField = null;

  public UseExistingFieldDialog(TreeRefactorItContext context,
      List violations, Map usedConstants, BinTypeRef lastAccessContext,
      BinTreeTableModel lastModel, boolean showConstantalizeOption){
    super(context, "Select existing field ...", showConstantalizeOption);

    NumericLiteral violation = (NumericLiteral) violations.get(0);

    this.usedConstants = usedConstants;
    this.context = context;
    this.wantToAccessFrom = ((BinType) violation.getOwnerMember()).getTypeRef();
    if (this.wantToAccessFrom.equals(lastAccessContext)){
      this.projectModel = (SelectFieldFromProjectModel) lastModel;
    } else {
      this.projectModel = null;
    }

    this.returnType = ((BinExpression) violation.getSourceConstruct())
        .getReturnType();

    dialog.setSize(400,450);

    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
    dialog.getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);

    dialog.getContentPane().repaint();
  }

  private static JPanel createHelpPanel() {
    return DialogManager.getHelpPanel("'Use existing field' dialog allows You"
        + " to select an existing accessable constant field from Your project,"
        + " that You wish to use instead of numeric literal value.");
  }

  private JPanel createCenterPanel(){
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(createHelpPanel(), BorderLayout.NORTH);

    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
    );
    panel.add(getTab(), BorderLayout.CENTER);
    if (super.showConstantalizeOption()){
      panel.add(createConstantalizeOption(), BorderLayout.SOUTH);
    }

    return panel;
  }


  private JTabbedPane getTab(){
    this.tabbedPane = new JTabbedPane();


    this.tabbedPane.addTab("Browse project", selectFromProjectPanel());
    this.tabbedPane.addTab("Recently used/created", selectRecentPanel());

    if (!((SelectRecentModel) this.tableRecent.getBinTreeTableModel())
        .isEmpty()){
      tabbedPane.setSelectedIndex(1);
    } else {
      tabbedPane.setSelectedIndex(0);
    }
    performTabsSwitched();

    this.tabbedPane.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        performTabsSwitched();
        dialog.getContentPane().repaint();
      }
    });

    return this.tabbedPane;
  }

  private JPanel selectRecentPanel(){
    final SelectRecentModel model
        = new SelectRecentModel(usedConstants);
    this.tableRecent = new BinTreeTable(model, BinTreeTable.NONE_STYLE,
        context);
    this.tableRecent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.tableRecent.expandAll();

    addEnterAndEscapeKeysListener(this.tableRecent);
    setValueColumnWidth(this.tableRecent);

    this.scrollPaneRecent = new JScrollPane(this.tableRecent);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(this.scrollPaneRecent, BorderLayout.CENTER);
    return panel;
  }

  private static final void setValueColumnWidth(BinTreeTable table) {
    TableColumnModel tcm = table.getColumnModel();
    tcm.getColumn(1).setMaxWidth(150);
    tcm.getColumn(1).setPreferredWidth(75);
    tcm.getColumn(1).setMinWidth(20);
    tcm.getColumn(1).setResizable(false);
  }

  private JPanel selectFromProjectPanel() {
    if (this.projectModel == null){
      this.projectModel = new SelectFieldFromProjectModel(
          this.wantToAccessFrom, this.returnType);
    }

    this.tableProject = new BinTreeTable(this.projectModel,
        BinTreeTable.NONE_STYLE,
        context);
    this.tableProject.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    addEnterAndEscapeKeysListener(this.tableProject);
    setValueColumnWidth(this.tableProject);

    this.scrollPaneProject = new JScrollPane(this.tableProject);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(this.scrollPaneProject, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createButtonsPanel(){
    final JPanel panel = new JPanel();
    final JButton buttonOk = new JButton("OK");
    final JButton buttonCancel = new JButton("Cancel");

    panel.add(buttonOk);
    panel.add(buttonCancel);

    buttonOk.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        okActionPerformed();
      }
    });

    buttonCancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        cancelActionPerformed();
      }
    });
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, 
        escapeActionListener);

    return panel;
  }

  public void okActionPerformed() {
    BinTreeTable currentTable = getCurrentTable();
    if (currentTable.getSelectedNodes().size() == 0){
      showErrorMessage(SELECT_A_FIELD, JOptionPane.ERROR_MESSAGE);
    } else {
      BinTreeTableNode node
          = (BinTreeTableNode) currentTable.getSelectedNodes().get(0);
      if (!(node.getBin() instanceof BinField)){
        showErrorMessage(SELECT_A_FIELD, JOptionPane.ERROR_MESSAGE);
      } else {
        // user selected a field from project or from recently used fields

        // create return information
        this.retUseField = (BinField) node.getBin();

        super.setButtonOKPressed(true);
        dialog.dispose();
      }
    }
  }

  private BinTreeTable getCurrentTable(){
    if (this.tabbedPane.getSelectedIndex() == 0){
      return this.tableProject;
    } else {
      return this.tableRecent;
    }
  }

  void performTabsSwitched(){
    if (tabbedPane.getSelectedIndex() == 0
        && ((BinTreeTableNode) projectModel.getRoot()).getChildCount() == 0){
      projectModel.buildProjectTree(dialog.getContext());
      tableProject.getTree().expandPath(
          new TreePath(((ParentTreeTableNode) projectModel.getRoot())
          .getPath()));
    }

    if (getCurrentTable() == this.tableProject){
      super.setScrollPane(this.scrollPaneProject);
    } else {
      super.setScrollPane(this.scrollPaneRecent);
    }
  }

  public void cancelActionPerformed(){
    dialog.dispose();
  }

  public BinTreeTableModel getProjectModel(){
    return (this.projectModel.isBuildCorrectly() ? this.projectModel : null);
  }

  public final BinTypeRef getWantToAccessFrom(){
    return wantToAccessFrom;
  }

  public void show() {
    dialog.show();
  }

  public BinField getRetUseField() {
    return this.retUseField;
  }
}
