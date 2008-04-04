/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.minaccess;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.query.usage.filters.SimpleFilter;
import net.sf.refactorit.refactorings.EjbUtil;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author vadim
 */
public class MinimizeAccessTableModel extends AbstractTableModel {
  private BinCIType type;
  private BinMember member;
  private List nodes = new ArrayList();
  private List fieldNodes = new ArrayList();
  private List methodNodes = new ArrayList();
  private List constructorNodes = new ArrayList();
  private List innerNodes = new ArrayList();

  public MinimizeAccessTableModel(BinMember member,
      boolean minimizeConstructors) {
    if (member instanceof BinConstructor || member instanceof BinMethod ||
        member instanceof BinField) {
      this.type = member.getOwner().getBinCIType();

    } else {
      this.type = (BinCIType) member;
    }
    this.member = member;

    collectDataForModel(minimizeConstructors);

    if (fieldNodes.size() > 0) {
      nodes.addAll(fieldNodes);
    }

    if (methodNodes.size() > 0) {
      nodes.addAll(methodNodes);
    }

    if (innerNodes.size() > 0) {
      nodes.addAll(innerNodes);
    }

    if (constructorNodes.size() > 0) {
      nodes.addAll(constructorNodes);
    }
    setNodesSelectedState();
  }

  private void collectDataForModel(boolean minimizeConstructors) {
    createMethodNodes(type.getDeclaredMethods());
    if (minimizeConstructors) {
      createMethodNodes(((BinClass) type).getDeclaredConstructors());
    }

    createFieldNodes();
    createInnerClassNodes();
  }

  private void createFieldNodes() {
    BinField[] fields = type.getDeclaredFields();
    Object last = null;
    HashMap lastNodeForCoords = new HashMap(fields.length);

    for (int i = 0; i < fields.length; i++) {
      BinField field = fields[i];

      Integer key = new Integer(field.getStartLine() +
        (field.getStartColumn() * 10000));
      
      last = lastNodeForCoords.get(key); 
      
      List usages = getInvocations(field);
      int[] stricterAccess = MinimizeAccessUtil.getStricterAccessRights(
          type.getTypeRef(), field, usages);
      if (stricterAccess.length > 0) {
        MinimizeAccessNode node = new MinimizeAccessNode(field, stricterAccess, this);

        if (last == null) {
          lastNodeForCoords.put(key, node); 
          fieldNodes.add(node);
        } else {
          if (last instanceof MinimizeAccessNode) {
            MinimizeAccessNode lastNode = (MinimizeAccessNode) last;
            lastNode.setNextNode(node);
            node.setPreviousNode(lastNode);

            lastNodeForCoords.put(key, node); 
            last = node;
            fieldNodes.add(node);
          }
        }
      } else {
        if (last instanceof MinimizeAccessNode) {
          MinimizeAccessNode node = (MinimizeAccessNode) last;
          do {
            fieldNodes.remove(node);
            node = node.getPreviousNode();
          } while (node != null);
        }

        lastNodeForCoords.put(key, "MAX ACCESS"); 
      }
    }
  }

  private void createMethodNodes(BinMethod[] methods) {
    List usages;
    int[] stricterAccess;

    for (int i = 0; i < methods.length; i++) {
      if (methods[i].isMain() || EjbUtil.isEjbMethod(methods[i])) {
        continue;
      }

      usages = getInvocations(methods[i]);
      stricterAccess = MinimizeAccessUtil.getStricterAccessRights(type.
          getTypeRef(),
          methods[i], usages);
      if (stricterAccess.length > 0) {
        methodNodes.add(new MinimizeAccessNode(methods[i], stricterAccess, this));
      }
    }
  }

  private void createInnerClassNodes() {
    List usages;
    int[] stricterAccess;

    BinTypeRef[] innersRef = type.getDeclaredTypes();
    for (int i = 0; i < innersRef.length; i++) {
      BinCIType inner = innersRef[i].getBinCIType();
      usages = Finder.getInvocations(inner);
      stricterAccess = MinimizeAccessUtil.getStricterAccessRights(
          type.getTypeRef(),
          inner, usages);

      if (stricterAccess.length > 0) {
        innerNodes.add(new MinimizeAccessNode(inner, stricterAccess, this));
      }
    }
  }

//  /**
//   * @deprecated
//   */
//	private List getInvocationsForField(BinField field) {
//		ManagingIndexer supervisor = new ManagingIndexer();
//
//		new FieldIndexer(supervisor, field, true, type.getTypeRef(), false);
//		supervisor.visit(type.getProject());
//
//		return supervisor.getInvocations();
//    //return getInvocations(field);
//	}

//	private List getInvocationsForMethod(BinMethod method) {
//		ManagingIndexer supervisor = new ManagingIndexer();
//
//
//
//    BinMethodSearchFilter filter = new BinMethodSearchFilter(true, true, false, true,
//											false, false, true);
//		new MethodIndexer(supervisor, method,
//											filter);
//
//		supervisor.visit(type.getProject());
//		return supervisor.getInvocations();
//	}

  private List getInvocations(BinMember member) {
    SearchFilter filter = null;
    if (member instanceof BinConstructor) {
      filter = new SimpleFilter(false, false, false);
    } else if (member instanceof BinMethod) {
      filter = new BinMethodSearchFilter(true, true, false, true, false, false,
          false, false, false);
    } else if (member instanceof BinField) {
      filter = new SimpleFilter(false, false, false);
    } else {
      Assert.must(false, "wrong method parameter");
    }

    return Finder.getInvocations(type.getProject(), member, filter);
  }

  public MinimizeAccessNode getNodeForRow(int row) {
    return (MinimizeAccessNode) nodes.get(row);
  }

  public int getColumnCount() {
    return 5;
  }

  public int getRowCount() {
    return nodes.size();
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Members";

      case 1:
        return "Current";

      case 2:
        return "Minimal";

      case 3:
        return "Change to";

      case 4:
        return "Select";

      default:
        return "";
    }
  }

  public void setValueAt(Object value, int row, int column) {
    MinimizeAccessNode node = getNodeForRow(row);

    switch (column) {
      case 3:
        node.setSelectedAccess(value);
        break;

      case 4:
        node.setSelected(((Boolean) value).booleanValue());
        break;
    }
  }

  public Class getColumnClass(int column) {
    if (column == 3) {
      return String.class;
    }

    final Object value = getValueAt(0, column);

    return (value != null) ? value.getClass() : null;
  }

  public boolean isCellEditable(int row, int column) {
    if (column == 3) {
      return getNodeForRow(row).isSelected();
    }

    if (column == 4) {
      return true;
    }

    return false;
  }

  public Object getValueAt(int row, int column) {
    MinimizeAccessNode node = getNodeForRow(row);

    switch (column) {
      case 0:
        return node;
      case 1:
        return node.getCurrentAccessName();
      case 2:
        return node.getMinimalAccessName();
      case 3:
        return node;
      case 4:
        return node.isSelected() ? Boolean.TRUE : Boolean.FALSE;
    }

    return null;
  }

  public List getNodes() {
    return nodes;
  }

  public BinCIType getType() {
    return type;
  }

  public void selectAllCheckBoxes() {
    for (int i = 0, max = nodes.size(); i < max; i++) {
      ((MinimizeAccessNode) nodes.get(i)).setSelected(true);
    }

    fireTableStructureChanged();
  }

  public boolean isAnythingSelected() {
    for (int i = 0, max = nodes.size(); i < max; i++) {
      if (((MinimizeAccessNode) nodes.get(i)).isSelected()) {
        return true;
      }
    }

    return false;
  }

  private void setNodesSelectedState() {
    List nodes = getNodes();
    MinimizeAccessNode node;
    if (member instanceof BinCIType) {
      selectAllCheckBoxes();
      return;
    }

    for (int i = 0; i < nodes.size(); i++) {
      node = (MinimizeAccessNode) nodes.get(i);
      if (node.getMember().equals(member)) {
        node.setSelected(true);
        break;
      }
    }
  }
}
