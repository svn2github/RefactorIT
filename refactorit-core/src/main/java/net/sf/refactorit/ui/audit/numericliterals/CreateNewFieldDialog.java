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
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


/**
 * @author Arseni Grigorjev
 */
public class CreateNewFieldDialog extends AbstractNumLitDialog {
  private static final String SELECT_A_CLASS_ERROR
      = "First You must select a class where You wish to create "
      + "\nYour new constant field.";
  private static final String INVALID_NAME
      = "The entered field name is not a valid java 2 identifier.";
  private static final String NAME_CONFLICTS_THIS
      = "Such field already exists in this type.";
  private static final String NAME_CONFLICTS_SUPER
      = "Existing fields in supertypes of the owner.";
  private static final String NAME_CONFLICTS_SUB
      = "Existing fields in subtypes of the owner.";

  private static final String[] PUBLIC = { "public" };
  private static final String[] PUBLIC_PROTECTED = { "public", "protected" };
  private static final String[] PUBLIC_PROTECTED_PRIVATE = { "public",
      "protected", "private" };
  private static final String[] NOT_CLASS = { "select a class" };

  private TreeRefactorItContext context;
  private BinTypeRef wantToAccessFrom;
  private String literal = null;

  private SelectClassFromProjectModel projectModel;
  private BinTreeTable projectTable;
  private JComboBox accessInput;
  private JTextField nameInput;
  private String[] curAccess;

  // RETURN INFORMATION
  private String retName = null;
  private BinCIType retWhere = null;
  private String retAccess = null;

  public CreateNewFieldDialog(
      TreeRefactorItContext context, List violations,
      String literal, BinTypeRef lastAccessContext, 
      BinTreeTableModel lastModel, boolean showConstantalizeOption
  ) {
    super(context, "Create new constant field", showConstantalizeOption);
    super.setScrollPanePadding(5, 5);

    NumericLiteral violation = (NumericLiteral) violations.get(0);

    this.context = context;
    this.literal = literal;
    this.wantToAccessFrom = ((BinCIType) violation.getOwnerMember())
        .getTypeRef();
    
    if (this.wantToAccessFrom.equals(lastAccessContext)){
      this.projectModel = (SelectClassFromProjectModel) lastModel;
    } else {
      this.projectModel = null;
    }

    dialog.setSize(400,450);

    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    dialog.getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
    dialog.getContentPane().repaint();
  }

  private static JPanel createHelpPanel() {
    return DialogManager.getHelpPanel("'Create new field' dialog allows You to "
        + "create a new constant field in any accessable class of Your project."
        + " Please select a class where You want to create the field and enter "
        + "new field`s name.");
  }

  private JPanel createCenterPanel(){
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
    );

    panel.add(createHelpPanel(), BorderLayout.NORTH);
    panel.add(createSelectFromProjectPanel(), BorderLayout.CENTER);
    panel.add(createFieldDefPanel(), BorderLayout.SOUTH);
    setAvailableAccessModifiers();
    
    return panel;
  }

  private JPanel createSelectFromProjectPanel(){
    if (this.projectModel == null){
      this.projectModel = new SelectClassFromProjectModel(
        this.wantToAccessFrom, dialog.getContext());
    }

    this.projectTable = new BinTreeTable(this.projectModel,
        BinTreeTable.NONE_STYLE,
        context);
    this.projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    
    BinTreeTableNode nodeToSelect = ((BinTreeTableNode) projectModel.getRoot()).
    			findChildByType(wantToAccessFrom.getBinType());
    
    if(nodeToSelect != null){
		TreePath path = new TreePath(nodeToSelect.getPath());
		this.projectTable.getTree().expandPath(path.getParentPath());
		this.projectTable.getTree().setSelectionPath(path);
    }
    
    super.addEnterAndEscapeKeysListener(this.projectTable);
    this.projectTable.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e) {
        setAvailableAccessModifiers();
      }

      public void mousePressed(final MouseEvent e) {}

      public void mouseReleased(final MouseEvent e) {}
    });

    super.setScrollPane(new JScrollPane(this.projectTable));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(super.getScrollPane(), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createFieldDefPanel(){
    final JPanel fieldDescriptionPanel = new JPanel(new GridBagLayout());
    this.nameInput = new JTextField(15);
    this.accessInput = new JComboBox(NOT_CLASS);
    accessInput.setEnabled(false);

    GridBagConstraints constr = new GridBagConstraints();
    constr.fill = GridBagConstraints.NONE;
    constr.insets = new Insets(4,4,4,4);
    constr.anchor = GridBagConstraints.CENTER;
    constr.gridx = 0; constr.gridy = 0;
    constr.gridwidth = 2;
    fieldDescriptionPanel.add(new JLabel("NEW FIELD`S DETAILS"), constr);

    constr.anchor = GridBagConstraints.EAST;
    constr.gridy++;
    constr.gridwidth = 1;
    fieldDescriptionPanel.add(new JLabel("Access modifier: "), constr);

    constr.anchor = GridBagConstraints.WEST;
    constr.gridx++;
    fieldDescriptionPanel.add(this.accessInput, constr);

    constr.anchor = GridBagConstraints.EAST;
    constr.gridx = 0;
    constr.gridy++;
    fieldDescriptionPanel.add(new JLabel("Name: "), constr);

    constr.anchor = GridBagConstraints.WEST;
    constr.gridx++;
    fieldDescriptionPanel.add(this.nameInput, constr);

    if (showConstantalizeOption()){
      JPanel parentPanel = new JPanel(new BorderLayout());
      parentPanel.add(fieldDescriptionPanel, BorderLayout.CENTER);
      parentPanel.add(createConstantalizeOption(), BorderLayout.SOUTH);
      return parentPanel;
    }

    return fieldDescriptionPanel;
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
    
    SwingUtil.initCommonDialogKeystrokes(dialog,
        buttonOk, buttonCancel, escapeActionListener);

    return panel;
  }

  public void okActionPerformed() {
    RefactoringStatus status = new RefactoringStatus();
    boolean takeCIType = true;

    if (this.projectTable.getSelectedNodes().size() == 0){
      status.addEntry(SELECT_A_CLASS_ERROR, RefactoringStatus.ERROR);
      takeCIType = false;
    } else if (this.curAccess == NOT_CLASS){
      status.addEntry(SELECT_A_CLASS_ERROR, RefactoringStatus.ERROR);
      takeCIType = false;
    }

    BinCIType where = null;
    if (takeCIType){
      where = (BinCIType) ((BinTreeTableNode) this.projectTable
          .getSelectedNodes().get(0)).getBin();
    }

    checkNameInput(this.nameInput.getText(), where, status);

    if (status.getSeverity() >= RefactoringStatus.WARNING){
      showErrorMessage(status.getAllMessages(),
          status.getJOptionMessageType());
    }

    if (!(status.getSeverity() >= RefactoringStatus.ERROR)){
      super.setButtonOKPressed(true);
//      final String access = (String) this.accessInput.getSelectedItem();

      this.retName = this.nameInput.getText().trim();
      this.retWhere = where;
      this.retAccess = (String) accessInput.getSelectedItem();

      dialog.dispose();
    }
  }

  public void cancelActionPerformed() {
    dialog.dispose();
  }

  public void setAvailableAccessModifiers(){
    BinTreeTableNode node
        = (BinTreeTableNode) projectTable.getSelectedNodes().get(0);
    if (node.getBin() instanceof BinCIType){
      BinCIType selectedClass = (BinCIType) node.getBin();
      String[] newAccess;
      if (selectedClass == wantToAccessFrom.getBinCIType()){
        newAccess = PUBLIC_PROTECTED_PRIVATE;
      } else if (wantToAccessFrom.getBinCIType() == selectedClass
          || wantToAccessFrom.isDerivedFrom(selectedClass.getTypeRef())){
        newAccess = PUBLIC_PROTECTED;
      } else {
        newAccess = PUBLIC;
      }

      if (newAccess != this.curAccess){
        if (!this.accessInput.isEnabled()){
          this.accessInput.setEnabled(true);
        }

        this.curAccess = newAccess;
        this.accessInput.removeAllItems();
        for (int i = 0; i < this.curAccess.length; i++){
          accessInput.addItem(curAccess[i]);
        }
      }
    } else {
      this.accessInput.setEnabled(false);
      this.curAccess = NOT_CLASS;
      this.accessInput.removeAllItems();
      for (int i = 0; i < this.curAccess.length; i++){
        accessInput.addItem(curAccess[i]);
      }
    }
  }

  private static void checkNameInput(final String name,
      BinCIType where, RefactoringStatus status){

    if (!NameUtil.isValidIdentifier(name)){
      status.addEntry(INVALID_NAME, RefactoringStatus.ERROR);
    }

    if (where != null && where.getDeclaredField(name) != null){
      status.addEntry(NAME_CONFLICTS_THIS, RefactoringStatus.ERROR);
    }

    if (where != null){
      List conflicts
          = RenameField.findConflictsInSupertypes(
          where.getTypeRef().getSupertypes(), name);
      if (conflicts.size() > 0) {
        status.addEntry(NAME_CONFLICTS_SUPER, RefactoringStatus.WARNING);
      }

      conflicts = RenameField.findConflictsInSubtypes(
          where.getTypeRef().getDirectSubclasses(), name);
      if (conflicts.size() > 0) {
        status.addEntry(NAME_CONFLICTS_SUB, RefactoringStatus.WARNING);
      }
    }
  }

  public final BinTreeTableModel getProjectModel(){
    return (this.projectModel.isBuildCorrectly() ? this.projectModel : null);
  }

  public final BinTypeRef getWantToAccessFrom(){
    return wantToAccessFrom;
  }

  public void show() {
    dialog.show();
  }

  public String getRetName() {
    return this.retName;
  }

  public BinCIType getRetWhere() {
    return this.retWhere;
  }

  public String getRetAccess() {
    return this.retAccess;
  }
}
