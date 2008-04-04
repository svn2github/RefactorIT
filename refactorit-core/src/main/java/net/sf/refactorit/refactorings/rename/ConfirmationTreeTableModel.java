/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.InvocationTreeTableNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * TreeTableModel for JTreeTable component
 * Also constructs childs for the root node accoding to the InvocationData.
 *
 * This is a generic class for confirmation models - if anything special is
 * needed please subclass this
 *
 * @author Vladislav Vislogubov
 */
public class ConfirmationTreeTableModel extends BinTreeTableModel {
  private final List nodesWithInvocationData = new ArrayList();

  /** No mandatory usages (see the other constructor for these) */
  public ConfirmationTreeTableModel(Object member, List usages) {
    this(member, usages, Collections.EMPTY_LIST);
  }

  /**
   * @param nonCheckedUsages contains these items not needed to be selected
   */
  public ConfirmationTreeTableModel(Object member, List usages,
      List nonCheckedUsages) {
    super(new BinTreeTableNode(member, false));
    ((BinTreeTableNode) getRoot()).setShowCheckBox(false);

    initChildren(usages, nonCheckedUsages);

    ((BinTreeTableNode) getRoot()).reflectLeafNumberToParentName();
    ((BinTreeTableNode) getRoot()).sortAllChildren();
  }

  public int getColumnCount() {
    return 3;
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    // FIXME: i18n
    switch (column) {
      case 0:
        return "Location";
      case 1:
        return "Line";
      case 2:
        return "Source";
      default:
    }
    return null;
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {
    if (node instanceof ParentTreeTableNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          String lineNumber = ((ParentTreeTableNode) node).getLineNumber();
          return lineNumber + NUMBER_PADDING;
        case 2:
          return ((ParentTreeTableNode) node).getLineSource();
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

  private void initChildren(List usages, List nonCheckedUsages) {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();
    // Append nodes
    for (int i = 0, max = usages.size(); i < max; i++) {
      final InvocationData data = (InvocationData) usages.get(i);

      BinTreeTableNode node = null;
      Object item = data.getWhere();

      //HACK: RIM-825
      if(!(item instanceof BinArrayType)){
        if (item instanceof BinCIType) {
          node = (BinTreeTableNode) root.findParent(
              ((BinCIType) item).getPackage(), false);
        } else if (item instanceof BinMember) {
          if (Assert.enabled) {
            Assert.must(((BinMember) item).getOwner() != null,
                "Owner of " + ((BinMember) item).getName() + " is null, class: " +
                item.getClass().getName());
          }
          node = (BinTreeTableNode) root.findParent(((BinMember) item).getOwner(), false);
        } else if (item instanceof BinTypeRef) {
          node = (BinTreeTableNode) root.findParent((BinTypeRef) item, false);
          item = ((BinTypeRef) item).getBinCIType();
        } else if (item instanceof CompilationUnit) {
          final BinPackage pack = ((CompilationUnit) item).getPackage();
          final List independentTypes
              = ((CompilationUnit) item).getIndependentDefinedTypes();
          if (independentTypes.size() == 1) {
            node = (BinTreeTableNode) root.findParent(
                (BinTypeRef) independentTypes.get(0), false);
          } else if (pack != null) {
            node = (BinTreeTableNode) root.findParent(pack, false);
          } else {
            node = root;
          }
        } else {
          Assert.must(false, "Unsupported Location in RenameModel: "
              + item.getClass().getName());
        }
  
        CompilationUnit source = null;
  
        if (item instanceof BinCIType) {
          source = ((BinCIType) item).getCompilationUnit();
        } else if (item instanceof BinMember) {
          source = ((BinMember) item).getOwner().getBinCIType().getCompilationUnit();
        } else if (item instanceof CompilationUnit) {
          source = (CompilationUnit) item;
        }
  //      else if(item instanceof AbstractLocationAware){
  //        source = ((AbstractLocationAware) item).getCompilationUnit();
  //      }
        
        //source = data.getCompilationUnit();
  
        if (source == null) {
          Assert.must(false, "Unsupported item in RenameModel: "
              + item.getClass().getName());
        }
  
        InvocationTreeTableNode bn
            = (InvocationTreeTableNode) node
            .findChildByType(item, data.getLineNumber());
        if (bn != null) {
          if (((ASTImpl) bn.getAsts().get(0)) == data.getWhereAst()) {
            continue;
          }
        }
  
        bn = new InvocationTreeTableNode(item);
        bn.addAst(data.getWhereAst());
        bn.setSourceHolder(source);
        if (nonCheckedUsages.contains(data)) {
          bn.setSelected(false);
        }
        
        // put InvocationData to the leaf node
        bn.setInvocationData(data);
        nodesWithInvocationData.add(bn);
  
        node.addChild(bn);
      }
    }
  }

  public List getCheckedUsages() {
    List usages = new ArrayList();

    Iterator iter = nodesWithInvocationData.iterator();
    while (iter.hasNext()) {
      InvocationTreeTableNode n = (InvocationTreeTableNode) iter.next();
      if (n.isCheckBoxNeeded() && n.isSelected()) {
        usages.add(n.getInvocationData());
      }
    }

    return usages;
  }
}
