/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;


import net.sf.refactorit.audit.rules.misc.FieldWriteAccessFinder;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFix;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixConstantalize;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixCreateField;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumLitFixUseField;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteral;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteralField;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.JTreeTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitDialog extends AbstractNumLitDialog {
  private static final String SELECT_SOMETHING
      = "Please select some violations to fix.";
  private static final String CONSTANTALIZE_AVAILABLE
      = "You have selected some violations, that can be fixed"
      + "\n using \"make field 'static' and 'final'\" action."
      + "\n\nDo You wish to apply it?";

  final TreeRefactorItContext context;
  List violations;
  String literal;

  BinTreeTableModel selectFieldModel = null;
  BinTreeTableModel selectClassModel = null;
  BinTypeRef accessedFromArgField = null;
  BinTypeRef accessedFromArgClass = null;

  // works for current functionality (manage literals inside a single class)
  public static Set importedTypes = new HashSet();

  Map usedConstants = new HashMap();

  BinTreeTable table;

  public NumLitDialog(TreeRefactorItContext context, List violations) {
    super(context, "Manage Numeric literals", false);
    super.setScrollPanePadding(150, 0);

    this.context = context;
    this.violations = violations;

    markConstantalizableViolations(this.violations);

    dialog.setSize(600,250);

    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
    dialog.getContentPane().add(createMainPanel(), BorderLayout.CENTER);
    dialog.getContentPane().repaint();
  }

  private static JPanel createHelpPanel() {
    return DialogManager.getHelpPanel(
        "Please select violations that You want to fix and select a fix method "
       + "using buttons on the right. Fixed violations are marked with green.");
  }

  private JPanel createMainPanel(){
    final JPanel center = new JPanel(new BorderLayout());

    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
    );

    center.add(createHelpPanel(), BorderLayout.NORTH);
    center.add(createRefactoringPanel(), BorderLayout.CENTER);

    return center;
  }

  private JPanel createRefactoringPanel(){
    final JPanel panel = new JPanel(new BorderLayout());

    NumLitTreeTableModel model = new NumLitTreeTableModel();
    BinTreeTableNode rootNode = (BinTreeTableNode) model.getRoot();
    for (Iterator it = violations.iterator(); it.hasNext(); ){
      rootNode.addChild(new NumLitTreeTableNode((NumericLiteral) it.next()));
    }

    this.table = new BinTreeTable(model, JTreeTable.NONE_STYLE, context);
    super.addEnterAndEscapeKeysListener(table);
    setTableProperties(table);

    super.setScrollPane(new JScrollPane(this.table));
    panel.add(super.getScrollPane(), BorderLayout.CENTER);

    JButton buttonAdd = new JButton("Create new field");
    JButton buttonUse = new JButton("Use existing field");

    JButton buttonClear = new JButton("Clear");
    JPanel controlsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constr = new GridBagConstraints();
    constr.fill = GridBagConstraints.HORIZONTAL;
    constr.anchor = GridBagConstraints.NORTH;
    constr.gridx = 0;
    constr.gridy = 0;
    controlsPanel.add(buttonAdd, constr);
    constr.gridy = 1;
    controlsPanel.add(buttonUse, constr);
    constr.gridy = 2;
    controlsPanel.add(buttonClear, constr);

    JPanel tempPanel = new JPanel(new BorderLayout());
    tempPanel.add(controlsPanel, BorderLayout.NORTH);
    panel.add(tempPanel, BorderLayout.EAST);

    buttonAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        List selected = getSelectedViolations();
        if (selected.size() == 0){
          showErrorMessage(SELECT_SOMETHING, JOptionPane.ERROR_MESSAGE);
          return;
        }

        List constantalizable = getConstantalizableViolations(selected);
        int result = JOptionPane.NO_OPTION;
        if (constantalizable.size() > 0){
          result = showQuestionMessage(CONSTANTALIZE_AVAILABLE);
        }

        if (result != JOptionPane.CANCEL_OPTION){
          if (result == JOptionPane.YES_OPTION){
            clearSelectedViolationsFixes(constantalizable);
            applyConstantalizeAction(selected, constantalizable);
          }

          if (selected.size() > 0){
            clearSelectedViolationsFixes(selected);

            CreateNewFieldDialog subdialog = new CreateNewFieldDialog(
                context, selected, literal,
                accessedFromArgClass, selectClassModel,
                (result == JOptionPane.NO_OPTION ? true : false));

            selectClassModel = subdialog.getProjectModel();
            accessedFromArgClass = subdialog.getWantToAccessFrom();
            subdialog.show();
            if (subdialog.isButtonOKPressed()){

              // assign fixes to selected violations
              BinField fakeField = NumLitFixCreateField.createFakeField(
                  subdialog.getRetName(),
                  subdialog.getRetWhere(),
                  subdialog.getRetAccess(), literal);
              usedConstants.put(fakeField, new NumLitFixCreateField(fakeField));

              for (int i = 0; i < selected.size(); i++){
                NumericLiteral violation = (NumericLiteral) selected.get(i);
                violation.setFix( new NumLitFixUseField(
                    violation, fakeField, usedConstants, subdialog.getRetWhere()
                    == accessedFromArgClass.getBinCIType(),
                    subdialog.definesConstantalizeFix()));
              }

              clearSelection();
              dialog.getRootPane().repaint();
            }
          } else {
            clearSelection();
            dialog.getRootPane().repaint();
          }
        }
      }
    });

    buttonUse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        List selected = getSelectedViolations();
        if (selected.size() == 0){
          showErrorMessage(SELECT_SOMETHING, JOptionPane.ERROR_MESSAGE);
          return;
        }

        List constantalizable = getConstantalizableViolations(selected);
        int result = JOptionPane.NO_OPTION;
        if (constantalizable.size() > 0){
          result = showQuestionMessage(CONSTANTALIZE_AVAILABLE);
        }

        if (result != JOptionPane.CANCEL_OPTION){
          if (result == JOptionPane.YES_OPTION){
            clearSelectedViolationsFixes(constantalizable);
            applyConstantalizeAction(selected, constantalizable);
          }

          if (selected.size() > 0){
            clearSelectedViolationsFixes(selected);

            UseExistingFieldDialog subdialog
                = new UseExistingFieldDialog(context, selected,
                usedConstants, accessedFromArgField, selectFieldModel,
                (result == JOptionPane.NO_OPTION ? true : false));

            selectFieldModel = subdialog.getProjectModel();
            accessedFromArgField = subdialog.getWantToAccessFrom();
            subdialog.show();
            if (subdialog.isButtonOKPressed()){

              // assign fixes to selected violations
              if (!usedConstants.containsKey(subdialog.getRetUseField())){
                usedConstants.put(subdialog.getRetUseField(), null);
              }

              for (int i = 0; i < selected.size(); i++){
                NumericLiteral violation = (NumericLiteral) selected.get(i);
                violation.setFix(new NumLitFixUseField(
                    violation, subdialog.getRetUseField(), usedConstants,
                    subdialog.getRetUseField().getOwner().equals(accessedFromArgField),
                    subdialog.definesConstantalizeFix()));
              }

              clearSelection();
              dialog.getRootPane().repaint();
            }
          } else {
            clearSelection();
            dialog.getRootPane().repaint();
          }
        }
      }
    });

    buttonClear.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        List selected = getSelectedViolations();
        clearSelectedViolationsFixes(selected);
        dialog.getRootPane().repaint();
      }
    });

    return panel;
  }

  void clearSelectedViolationsFixes(List selected) {
    for (int i = 0; i < selected.size(); i++){
      NumericLiteral violation = (NumericLiteral) selected.get(i);
      if (violation.hasFix()){
        NumLitFix fix = violation.getFix();
        if (fix instanceof NumLitFixConstantalize){
          usedConstants.remove(fix.getField());
          clearFixesFor(fix.getField(), violations);
        }
      }
      violation.clearFix();
    }
    return;
  }

  private void clearFixesFor(BinField field, List violations){
    for (int i = 0; i < violations.size(); i++){
      NumericLiteral violation = (NumericLiteral) violations.get(i);
      if (violation.hasFix()){
        NumLitFix fix = violation.getFix();
        if (fix.getField() == field){
          violation.clearFix();
        }
      }
    }
  }

  void applyConstantalizeAction(final List selected,
     final List constantalizable) {
    for (Iterator it = constantalizable.iterator(); it.hasNext(); ){
      NumericLiteralField numericViolation
          = (NumericLiteralField) it.next();
      NumLitFixConstantalize fix = new NumLitFixConstantalize(
          numericViolation, numericViolation.getField());
      usedConstants.put(numericViolation.getField(), fix);
      numericViolation.setFix(fix);
    }

    selected.removeAll(constantalizable);
  }

  static final List getConstantalizableViolations(List exviolations){
    final List result = new ArrayList();
    for (int i = 0; i < exviolations.size(); i++){
      NumericLiteral violation = (NumericLiteral) exviolations.get(i);
      if (violation.isConstantalizable()){
        result.add(violation);
      }
    }
    return result;
  }

  List getSelectedViolations() {
    List selected = table.getSelectedNodes();
    for (int i = 0; i < selected.size(); i++){
      selected.set(i, ((NumLitTreeTableNode) selected.get(i))
          .getNumericViolation());
    }

    return selected;
  }

  private static void setTableProperties(final BinTreeTable table) {
    TableColumnModel tcm = table.getColumnModel();
    table.getTableHeader().setReorderingAllowed(false);
    tcm.getColumn(0).setMaxWidth(50);
    tcm.getColumn(0).setResizable(false);
    tcm.getColumn(2).setMaxWidth(55);
    tcm.getColumn(2).setPreferredWidth(40);
    tcm.getColumn(0).setResizable(true);
    table.setShowGrid(true);
    table.getTree().setRootVisible(false);

    DefaultTableCellRenderer colorManager = new DefaultTableCellRenderer() {

      Color fixedColor = new Color( 215, 255, 215);
      Color fixedAndSelectedColor = new Color( 255, 255, 190);

      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
          ) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
            row, column);

        NumLitTreeTableNode node
            = ((NumLitTreeTableModel) ((BinTreeTable) table)
            .getBinTreeTableModel()).getNodeForRow(row);

        if (node.getNumericViolation().hasFix()){
          if (isSelected){
            this.setBackground(fixedAndSelectedColor);
          } else {
            this.setBackground(fixedColor);
          }
        } else {
          if (isSelected){
            this.setBackground(table.getSelectionBackground());
          } else {
            this.setBackground(table.getBackground());
          }
        }

        if (column == 0){
          this.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
          this.setHorizontalAlignment(SwingConstants.LEFT);
        }
        return this;
      }
    };

    table.setDefaultRenderer(String.class, colorManager);
    table.setDefaultRenderer(Integer.class, colorManager);
  }

  void clearSelection(){
    this.table.getSelectionModel().clearSelection();
  }

  private JPanel createButtonsPanel(){
    final JPanel panel = new JPanel();
    final JButton buttonOk = new JButton("OK");
    final JButton buttonCancel = new JButton("Cancel");

    panel.add(buttonOk);
    buttonOk.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        okActionPerformed();
      }
    });

    panel.add(buttonCancel);
    buttonCancel.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        cancelActionPerformed();
      }
    });

    SwingUtil.initCommonDialogKeystrokes(dialog,
        buttonOk, buttonCancel, escapeActionListener);

    return panel;
  }

  public void okActionPerformed(){
    super.setButtonOKPressed(true);
    dialog.dispose();
  }

  public void cancelActionPerformed() {
    dialog.dispose();
  }

  public void setLiteral(final String literal){
    this.literal = literal;
  }

  public void markConstantalizableViolations(List exviolations){
    FieldWriteAccessFinder finder = new FieldWriteAccessFinder();
    for (Iterator it = exviolations.iterator(); it.hasNext(); ){
      NumericLiteral violation = (NumericLiteral) it.next();
      if (!violation.isConstantalizeChecked()){
        if (violation instanceof NumericLiteralField){
          BinField field = ((NumericLiteralField) violation).getField();
          if (!finder.checkWriteAccess(field)){
            violation.setConstantalizable(true);
            NumLitFixConstantalize fix = new NumLitFixConstantalize(
                violation, field);
            usedConstants.put(field, fix);
            violation.setFix(fix);
          }
          violation.setConstantalizeChecked(true);
        }
      }
    }
  }

  public void finalize() throws Throwable {
    importedTypes.clear();
    super.finalize();
  }

  public void show() {
    dialog.show();
  }
}
