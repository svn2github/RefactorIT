/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractsuper;


import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Table of members to be extracted.
 *
 * @author Anton Safonov
 */
public class ExtractableMembersModel extends AbstractTableModel {
  private static final String[] columns = {" ", "Member", "Abstract"};

  private List rows = new ArrayList();

  private Set hiddenRows = new HashSet();

  private Map memberToNode = new HashMap();

  private ExtractSuper extractor;

  private ExtractSuperDialog dialog;

  public ExtractableMembersModel(ExtractSuper extractor) {
    this.extractor = extractor;
    init();
  }

  public ExtractSuper getExtractor() {
    return this.extractor;
  }

  private void init() {
    BinField[] fields = extractor.getTypeRef().getBinCIType()
        .getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (!(fields[i] instanceof BinEnumConstant)) {
	      final ExtractableMemberNode child
	          = new ExtractableMemberNode(this, fields[i]);
	      addRow(child);
	      child.setSelected(false);
	      memberToNode.put(fields[i], child);
      }
    }

    BinMethod[] methods = extractor.getTypeRef().getBinCIType()
        .getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      final ExtractableMemberNode child =
          new ExtractableMemberNode(this, methods[i]);

      addRow(child);

      child.setSelectedSilently(false);

      memberToNode.put(methods[i], child);
    }

    sortRows();

    update();

    List selected = extractor.getMembersToExtract();
    for (int i = 0, max = getAllRowCount(); i < max; i++) {
      final ExtractableMemberNode node = getRowFromAll(i);
      if (node.isHidden()) {
        continue;
      }

      if (!node.isSelected()) {
        node.setSelectedSilently(selected.contains(node.getBin()));
      }
    }
  }

  public boolean update() {
    boolean didChange = false;

    for (int i = 0, max = getAllRowCount(); i < max; i++) {
      final ExtractableMemberNode node = getRowFromAll(i);

      node.setConvertPrivates(extractor.isConvertPrivate());

      boolean hide = false;

      if (extractor.isExtractInterface()) {
        if (!((BinMember) node.getBin()).isPublic()) {
          hide = true;
        }
        if (node.getBin() instanceof BinField
            && (!((BinField) node.getBin()).isPublic()
            || !((BinField) node.getBin()).isFinal()
            || !((BinField) node.getBin()).isStatic())) {
          hide = true;
        } else if (node.getBin() instanceof BinMethod) {
          if (((BinMethod) node.getBin()).isStatic()) {
            hide = true;
          }
        }

      } else if (extractor.isForceExtractMethodsAbstract()) {
        if (((BinMember) node.getBin()).isPrivate()
            || (node.getBin() instanceof BinMethod
            && ((BinMember) node.getBin()).isStatic())) {
          hide = true;
        }
      }

      if (node.isHidden() != hide) {
        setHidden(node, hide);
        didChange = true;
      }
    }

    if (didChange) {
      fireTableStructureChanged();
    }

    return didChange;
  }

  void updateSelection(final BinMember member, boolean selected) {
    if (!extractor.isExtractClass()) {
      return; // no deps for interfaces
    }

    final ExtractableMemberNode node = getNodeFor(member);
    if (node == null || node.isVisiting()) {
      return;
    }
    node.setVisiting(true);

    if (node.setSelectedSilently(selected).booleanValue()
        && !node.isAbstract()) {
      List members = this.extractor.getDependants(member, selected);

      for (int i = 0, max = members.size(); i < max; i++) {
        final BinMember dependant = (BinMember) members.get(i);
        updateSelection(dependant, selected);
      }
    }

    node.setVisiting(false);
  }

  public void setDialog(ExtractSuperDialog dialog) {
    this.dialog = dialog;
  }

  void updatePreview() {
    if (dialog != null) {
      dialog.updatePreview();
    }
  }

  public ExtractableMemberNode getNodeFor(BinMember member) {
    return (ExtractableMemberNode) memberToNode.get(member);
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
    if (columnIndex == 0
        || columnIndex == 1 // this one for usability
        ) {
      return true;
    }

    final ExtractableMemberNode node = findNodeForRow(rowIndex);

//    if (!node.isSelected()) {
//      return false;
//    }

    if (columnIndex == 2 && node.isForcedAbstract()) {
      return false;
    }

    return (node.isConvertPrivates() || !((BinMember) node.getBin()).isPrivate())
        && !(node.getBin() instanceof BinField)
        && !((BinMember) node.getBin()).isStatic();
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    final ExtractableMemberNode node = findNodeForRow(rowIndex);
    if (node == null) {
      return;
    }

    switch (columnIndex) {
      case 0:
        node.setSelected(((Boolean) aValue).booleanValue());

      case 1:

        // no action
        break;

      case 2:
        node.setAbstract(((Boolean) aValue).booleanValue());
    }
  }

  void setSelectionForAll(boolean select) {
    int size = getRowCount();

    for (int i = 0; i < size; i++) {
      ExtractableMemberNode node = findNodeForRow(i);

      if (node == null) {
        continue;
      }

      node.setSelected(select);
    }

    updatePreview();
  }

  public void addRow(ExtractableMemberNode node) {
    this.rows.add(node);
  }

  public int getRowCount() {
    return rows.size() - hiddenRows.size();
  }

  public int getAllRowCount() {
    return rows.size();
  }

  public ExtractableMemberNode getRowFromAll(int rowIndex) {
    return (ExtractableMemberNode) rows.get(rowIndex);
  }

  public void setHidden(ExtractableMemberNode node, boolean willBeHidden) {
    if (willBeHidden) {
      this.hiddenRows.add(node);
    } else {
      this.hiddenRows.remove(node);
    }

    node.setHidden(willBeHidden);
    node.setDisplayName(null); // to get rid of grey color and HTML
  }

  public int getColumnCount() {
    return columns.length;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= getRowCount()) {
      return null;
    }

    final ExtractableMemberNode node = findNodeForRow(rowIndex);
    if (node == null) {
      return null;
    }

    switch (columnIndex) {
      case 0:
        return node.isSelected() ? Boolean.TRUE : Boolean.FALSE;
      case 1:
        return node;
      case 2:
        return node.isAbstract() ? Boolean.TRUE : Boolean.FALSE;
    }

    return null;
  }

  private ExtractableMemberNode findNodeForRow(int rowIndex) {
    int current = 0;

    for (int i = 0, max = rows.size(); i < max; i++) {
      final ExtractableMemberNode node = (ExtractableMemberNode) rows.get(i);
      if (node.isHidden()) {
        continue;
      }

      if (current == rowIndex) {
        return node;
      }

      ++current;
    }

    return null;
  }

  public void sortRows() {
    Collections.sort(this.rows, new BinTreeTableNode.NodeComparator());
  }
}
