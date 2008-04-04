/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.notused;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class NotUsedTreeTableModel extends BinTreeTableModel {
  private BinTreeTableNode typesBranch;
  private BinTreeTableNode membersBranch;
  private BinPackage targPackage;
  private BinType targBinType;
  private ExcludeFilterRule[] filterRules;

  private List typeNodes = new ArrayList();

  private static final String[] columnNames =
      new String[] {"Not Used", "Line", "Source"};
  public static final String WHOLE_TYPES = "Whole Types";
  public static final String SINGLE_MEMBERS = "Single Members";

  public NotUsedTreeTableModel(
      Project project, Object target, ExcludeFilterRule[] filterRules
      ) {
    super(new BinTreeTableNode(target, false));

    this.filterRules = filterRules;
    
    String name = "Overall Not Used";
    if (target instanceof BinPackage) {
      name = MessageFormat.format("Not used in {0}",
          new Object[] {((BinPackage) target).getQualifiedName()});
    } else if (target instanceof BinMember) {
      name = MessageFormat.format("Not used in {0}",
          new Object[] {((BinMember) target).getQualifiedName()});
    }

    ((BinTreeTableNode) getRoot()).setDisplayName(name);

    if (target instanceof BinPackage) {
      targPackage = (BinPackage) target;
    } else if (target instanceof BinType) {
      targBinType = (BinType) target;
    }

    populateTree(project, filterRules);

    ((BinTreeTableNode) getRoot()).reflectLeafNumberToParentName();

    // small hack here - we add children later to skip them on types counting above
    for (int i = 0, max = typeNodes.size(); i < max; i++) {
      addMembersOfType((BinTreeTableNode) typeNodes.get(i));
    }

    if (this.typesBranch != null) {
      this.typesBranch.sortAllChildren();
    }
  }

  private void populateTree(Project project, ExcludeFilterRule[] filterRules) {
    NotUsedIndexer nui = new NotUsedIndexer(filterRules);
    nui.visit(project);

    addTypes(nui);
    addMembers(nui);

    // let's help garbage collector
    nui.clear();
    nui = null;
  }

  private void addTypes(NotUsedIndexer nui) {
    for (Iterator iter = nui.getNotUsedTypes().iterator(); iter.hasNext(); ) {
      BinTypeRef x = (BinTypeRef) iter.next();
      //System.err.println("Not used type: "+x.getQualifiedName() );

      if (targPackage != null
          && ((targPackage.getQualifiedName().length() == 0
          && x.getQualifiedName().length()
          != x.getBinCIType().getNameWithAllOwners().length())
          || !x.getPackage().getQualifiedName()
          .startsWith(targPackage.getQualifiedName()))) {
        continue;
      }
      if (targBinType != null && targBinType != x.getBinType()) {
        continue;
      }

      BinTreeTableNode parent = (BinTreeTableNode)
          getTypesBranch().findParent(x.getPackage(), true);

      BinTreeTableNode newNode = new BinTreeTableNode(x.getBinCIType());
      newNode.setDisplayName(x.getBinCIType().getNameWithAllOwners());
      parent.addChild(newNode);

      typeNodes.add(newNode);
    }
  }

  private void addMembersOfType(BinTreeTableNode typeNode) {
    BinCIType type = (BinCIType) typeNode.getBin();

    final BinField[] aFields = type.getDeclaredFields();
    for (int iField = 0; iField < aFields.length; iField++) {
      final BinField field = aFields[iField];
      typeNode.addChild(new BinTreeTableNode(field));
    }

    BinMethod[] aMethods = type.getDeclaredMethods();
    for (int iMethod = 0; iMethod < aMethods.length; iMethod++) {
      final BinMethod method = aMethods[iMethod];
      if (method.isSynthetic()) {
        continue;
      }
      typeNode.addChild(new BinTreeTableNode(method));
    }

    if (type.isClass() || type.isEnum()) {
      final BinConstructor[] cnstrs = ((BinClass) type).getConstructors();
      for (int i = 0, max = cnstrs.length; i < max; i++) {
        final BinConstructor constructor = cnstrs[i];
        if (constructor.isSynthetic()) {
          continue;
        }
        typeNode.addChild(new BinTreeTableNode(constructor));
      }
    }

//    final BinTypeRef[] inners = type.getDeclaredTypes();
//    for (int i = 0; i < inners.length; i++) {
//      BinTreeTableNode newNode = new BinTreeTableNode(inners[i].getBinCIType());
//      typeNode.addChild(newNode);
//      addMembersOfType(newNode);
//    }
  }

  private void addMembers(NotUsedIndexer nui) {
    List notUsedMembers = nui.getNotUsedSingleMembers();
    for (int i = 0, max = notUsedMembers.size(); i < max; i++) {
      BinMember x = (BinMember) notUsedMembers.get(i);
      //System.err.println("Not used member: "+x.getQualifiedName());

      if (targPackage != null
          && ((targPackage.getQualifiedName().length() == 0
          && x.getOwner().getQualifiedName().length()
          != x.getOwner().getBinCIType().getNameWithAllOwners().length())
          || !x.getOwner().getPackage().getQualifiedName()
          .startsWith(targPackage.getQualifiedName()))) {
        continue;
      }
      if (targBinType != null && targBinType != x.getOwner().getBinType()) {
        continue;
      }

      BinTreeTableNode newNode = new BinTreeTableNode(x);
      getMembersBranch().findParent(x.getOwner(), true).addChild(newNode);
    }

    if (this.membersBranch != null) {
      this.membersBranch.sortAllChildren();
    }
  }

  private BinTreeTableNode getTypesBranch() {
    if (this.typesBranch == null) {
      this.typesBranch = new BinTreeTableNode(WHOLE_TYPES);
      ((BinTreeTableNode) getRoot()).addChild(typesBranch);
    }

    return this.typesBranch;
  }

  private BinTreeTableNode getMembersBranch() {
    if (this.membersBranch == null) {
      this.membersBranch = new BinTreeTableNode(SINGLE_MEMBERS);
      ((BinTreeTableNode) getRoot()).addChild(membersBranch);
    }

    return this.membersBranch;
  }

  /**
   * Returns the number of available column.
   */
  public int getColumnCount() {
    return columnNames.length;
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    return columnNames[column];
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {
    if (node instanceof BinTreeTableNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          return ((BinTreeTableNode) node).getLineNumber() + NUMBER_PADDING;
        case 2:
          return ((BinTreeTableNode) node).getLineSource();
      }
    }

    return null;
  }

  public Class getColumnClass(int col) {
    switch (col) {
      case 1:
        return Integer.class;
      case 2:
        return String.class;
      default:
        return super.getColumnClass(col);
    }
  }

  public ExcludeFilterRule[] getFilterRules() {
    return this.filterRules;
  }
}
