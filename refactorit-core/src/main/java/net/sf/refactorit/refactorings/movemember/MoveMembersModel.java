/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Table of members to be moved.
 *
 * @author Anton Safonov, Vadim Hahhulin
 */
public class MoveMembersModel extends AbstractTableModel {
  private static final String[] columns = {" ", "Member"};

  private List rows = new ArrayList();

  private Map memberToNode = new HashMap();
  private ConflictResolver resolver;

  public MoveMembersModel(ConflictResolver resolver) {
    this.resolver = resolver;

    init();
  }

  private void init() {
    List selectedMembers = resolver.getBinMembersToMove();

    BinField[] fields = resolver.getNativeType().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (!(fields[i] instanceof BinEnumConstant)) {
	      final MoveMemberNode child = new MoveMemberNode(fields[i]);
	      addRow(child);
	      child.setSelected(selectedMembers.contains(fields[i]));
	      memberToNode.put(fields[i], child);
      }
    }

    BinMethod[] methods = resolver.getNativeType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].isToString()) {
        continue; // we can't move toString() by now - too complex
      }

      final MoveMemberNode child = new MoveMemberNode(methods[i]);

      addRow(child);
      child.setSelected(selectedMembers.contains(methods[i]));
      memberToNode.put(methods[i], child);

      if (child.isSelected()) {
        if (isReallyToMoveMethodMain(methods[i]) == JOptionPane.NO_OPTION) {
          child.setSelected(false);
          resolver.getConflictData(methods[i]).setIsSelectedToMove(false);
        }
      }
    }
    BinTypeRef[] innerClasses = resolver.getNativeType().getDeclaredTypes();
    for (int i = 0; i < innerClasses.length; i++) {
    	BinCIType innerClass = innerClasses[i].getBinCIType();
      final MoveMemberNode child = new MoveMemberNode(innerClass);
      addRow(child);
      child.setSelected(selectedMembers.contains(innerClass));
      memberToNode.put(innerClass, child);
    }    
    sortRows();
  }

  public void runResolver(MoveMemberNode node) {
    resolver.runConflictsResolver((BinMember) node.getBin(), node.isSelected());
    update();
  }

  public void update() {
    List selectedMembers = resolver.getBinMembersToMove();

    // unselect all
    for (int i = 0; i < getRowCount(); i++) {
      getRow(i).setSelected(false);
    }

    for (int i = 0, max = selectedMembers.size(); i < max; i++) {
      BinMember member = (BinMember) selectedMembers.get(i);
      MoveMemberNode memberNode = getNodeFor(member);

      if (!memberNode.isSelected()) {
        memberNode.setSelected(true);
      }
    }

    fireTableChanged(new TableModelEvent(this, 0, getRowCount(), 0));
    fireTableChanged(new TableModelEvent(this, 0, getRowCount(), 1));
  }

  public MoveMemberNode getNodeFor(BinMember member) {
    return (MoveMemberNode) memberToNode.get(member);
  }

  public String getColumnName(int column) {
    return columns[column];
  }

  public Class getColumnClass(int columnIndex) {
    final Object value = getValueAt(0, columnIndex);
    if (value != null) {
      return value.getClass();
    }
    return null;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return true;
    }

    final MoveMemberNode node = getRow(rowIndex);

    if (!node.isSelected()) {
      return false;
    }

    return columnIndex != 1;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    final MoveMemberNode node = getRow(rowIndex);
    if (node == null) {
      return;
    }

    switch (columnIndex) {
      case 0:
        node.setSelected(((Boolean) aValue).booleanValue());

        if (node.isSelected()) {
          if (isReallyToMoveMethodMain(node.getBin()) == JOptionPane.NO_OPTION) {
            node.setSelected(false);
            break;
          }
        }

        runResolver(node);
        break;

      case 1:

        // no action
        break;
    }
  }

  public void addRow(MoveMemberNode node) {
    this.rows.add(node);
  }

  public int getRowCount() {
    return rows.size();
  }

  public MoveMemberNode getRow(int rowIndex) {
    return (MoveMemberNode) rows.get(rowIndex);
  }

  public int getColumnCount() {
    return columns.length;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= getRowCount()) {
      return null;
    }

    final MoveMemberNode node = getRow(rowIndex);
    if (node == null) {
      return null;
    }

    switch (columnIndex) {
      case 0:
        return node.isSelected() ? Boolean.TRUE : Boolean.FALSE;

      case 1:
        return node;
    }
    return null;
  }

  public void sortRows() {
    Collections.sort(this.rows, new BinTreeTableNode.NodeComparator());
  }

  private int isReallyToMoveMethodMain(Object o) {
    if ((o instanceof BinMethod) && ((BinMethod) o).isMain()) {
//      Window oldParent = DialogManager.getDialogParent();
//      DialogManager.setDialogParent(parentWindow);

      int result = DialogManager.getInstance().getResultFromQuestionDialog(
          "Move method main?", "Do you really want to move method main?");

//      DialogManager.setDialogParent(oldParent);
      return result;
    }

    return -1;
  }
}
