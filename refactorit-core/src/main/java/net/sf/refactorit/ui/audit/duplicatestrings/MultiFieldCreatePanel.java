/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.duplicatestrings;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.table.BinTable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Oleg Tsernetsov
 * @author Aleksei Sosnovski
 */
public class MultiFieldCreatePanel extends JPanel {

  private String defaultName = "str";

  private List  expressions;

  private List classes;

  private List fieldNewNames = new ArrayList();

  private boolean isNamesOk = false;

  private RefactoringStatus stat;

  final class NamesTableModel extends AbstractTableModel {
    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0:
        return "Expression";
      case 1:
        return "Name of new field";
      }
      throw new IndexOutOfBoundsException("column: " + column);
    }

    public int getRowCount() {
      if ( expressions == null) {
        return 0; // debug mode
      }

      return  expressions.size();
    }

    public Object getValueAt(int row, int column) {

      switch (column) {
      case 0:
        return ((BinLiteralExpression) expressions.get(row)).getLiteral();

      case 1:
        return (String) fieldNewNames.get(row);
      }
      throw new IndexOutOfBoundsException("column: " + column);
    }

    public void setValueAt(Object value, int row, int column) {

      switch (column) {
      case 0:
        break;

      case 1:
        fieldNewNames.set(row, value.toString());
        break;
      }

      updateStatus();
    }

    public boolean isCellEditable(int row, int column) {
      switch (column) {
      case 1:
        return true;
      }

      return false;
    }

  }

  final NamesTableModel parametersTableModel = new NamesTableModel();

  final BinTable parametersTable = new BinTable(parametersTableModel);
  {
    parametersTable
        .setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
  }

  private final JTextArea editor = new JTextArea();
  {
    editor.setEditable(false);
  }

  public MultiFieldCreatePanel(List  expressions, List classes) {
    super(new BorderLayout());

    this. expressions =  expressions;

    this. classes =  classes;

    int cnt = 0;

    for (Iterator it =  expressions.iterator(); it.hasNext(); ) {
      it.next();
      cnt++;
        fieldNewNames.add(defaultName + cnt);
    }

    Box box = new Box(BoxLayout.Y_AXIS);

    box.add(createNamesPanel());

    add(box, BorderLayout.NORTH);
    add(createStatusPanel());
    updateStatus();
  }

  public void stopEditing() {
    this.parametersTable.stopEditing();
  }

  private JComponent createNamesPanel() {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel
        .setBorder(BorderFactory
            .createTitledBorder("Parameters " +
            "(double-click on \"Name of new field\" column to edit)"));

    JScrollPane sp = new JScrollPane(parametersTable);
    sp.setPreferredSize(new Dimension(10, 180));
    panel.add(sp);

    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(6, 1));

    panel.add(buttons, BorderLayout.EAST);

    return panel;
  }

  private JComponent createStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Status"));

    panel.add(new JScrollPane(editor));

    return panel;
  }

  public void setStatusText(String text) {
    editor.setText(text);
  }

  public void appendStatusText(String text) {
    editor.append(text);
  }

  public boolean isNewNamesOk() {
    return isNamesOk;
  }

  void updateStatus() {
    isNamesOk = true;
    editor.setText("");
    for (int k = 0; k < fieldNewNames.size(); k++) {
      String kName = (String) fieldNewNames.get(k);

      BinCIType cls = (BinCIType) classes.get(k);

      if (!isNameOk(kName, cls)) {
        editor.append(stat.getFirstMessage() + "\n" +
            "Please specify new names for variables");
        isNamesOk = false;
      }

      if (kName.trim().equals("")) {
        editor.append("Error: Names for all fields must be cpecified\n");
        isNamesOk = false;
      }

      for (int j = k + 1; j < fieldNewNames.size(); j++) {
        if (k != j) {
          String jName = (String) fieldNewNames.get(j);
          BinLiteralExpression jExpr =
              (BinLiteralExpression) expressions.get(j);
          BinLiteralExpression kExpr =
              (BinLiteralExpression) expressions.get(k);

          if (jName.equals(kName)//) {
              && getLastOwner(kExpr) == getLastOwner(jExpr)) {
            editor.append("Error: New names for "
                + jExpr.getLiteral() + " and "
                + kExpr.getLiteral() + " are equal\n");
            isNamesOk = false;
          }
        }
      }
    }
  }

  public List getExpressions() {
    return expressions;
  }

  public List getFieldNames() {
    return fieldNewNames;
  }

  public static BinCIType getLastOwner(BinExpression expr) {
    BinCIType owner = null;
    BinTypeRef type = expr.getOwner();

    while (type != null) {
      owner = type.getBinCIType();
      type = owner.getOwner();
    }

    return owner;
  } // end of getLastOwner method


  private boolean isNameOk (String name, BinCIType cls) {
    stat = new RefactoringStatus();

    MyVisitor visitor = new MyVisitor();
    cls.accept(visitor);
    ArrayList vars = visitor.getVarList();

    for (int i = 0; i < vars.size(); i++) {
      BinVariable var = (BinVariable) vars.get(i);

      if (name.equals(var.getName())) {
        stat.addEntry
            ("Existing field / local variable in target or inner class" +
            " with name " + name, RefactoringStatus.WARNING);
        return false;
      }
    }

    stat.merge(nameCheck.checkUserInput(name, cls));
    if (nameCheck.checkUserInput(name, cls).getEntries().size() != 0) {
      return false;
    }

    return true;
  }
}


class MyVisitor extends BinItemVisitor {
  ArrayList varList = new ArrayList();

  public void visit (BinLocalVariableDeclaration dec) {
    BinVariable[] vars = dec.getVariables();
    for (int i = 0; i < vars.length; i++) {
      varList.add(vars[i]);
    }
  }

  public void visit (BinFieldDeclaration dec) {
    BinVariable[] vars = dec.getVariables();
    for (int i = 0; i < vars.length; i++) {
      varList.add(vars[i]);
    }
  }

  public ArrayList getVarList() {
    return varList;
  }
}


class nameCheck {
  public static RefactoringStatus checkUserInput
      (String name, BinCIType target) {
    RefactoringStatus status = new RefactoringStatus();

    if (!NameUtil.isValidIdentifier(name)) {
      status.merge(
          new RefactoringStatus("Not a valid Java 2 field identifier",
          RefactoringStatus.ERROR));
    }

    List conflicts
        = net.sf.refactorit.refactorings.rename.RenameField.
        findConflictsInSupertypes(
        target.getTypeRef().getSupertypes(), name);
    if (conflicts.size() > 0) {
      status.addEntry("Existing field in supertypes of the owner",
          conflicts, RefactoringStatus.WARNING);
    }

    conflicts = net.sf.refactorit.refactorings.rename.RenameField.
        findConflictsInSubtypes(
        target.getTypeRef().getDirectSubclasses(), name);
    if (conflicts.size() > 0) {
      status.addEntry("Existing field) in subtypes of the owner",
          conflicts, RefactoringStatus.WARNING);
    }

    conflicts = net.sf.refactorit.refactorings.rename.RenameField.
        findConflictsWithOuterTypes(target, name);
    if (conflicts.size() > 0) {
      status.addEntry("Existing field in outer types",
          conflicts, RefactoringStatus.WARNING);
    }

    return status;
  }
}
