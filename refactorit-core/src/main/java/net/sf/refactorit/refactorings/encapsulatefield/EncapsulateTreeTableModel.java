/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.encapsulatefield;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.usage.EncapsulationInvocationData;
import net.sf.refactorit.query.usage.InvocationTreeTableNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * TreeTableModel Encapsulate Field usage confirmation dialog model.
 *
 * @author Tanel Alumae
 */
public class EncapsulateTreeTableModel extends BinTreeTableModel {
  private BinTreeTableNode declaringBranch;
  private BinTreeTableNode subclassesBranch;
  private BinTreeTableNode otherBranch;
  private BinTreeTableNode notPossibleBranch;

  private BinField field;

  private BinCIType hostingClass;
  private List innerClasses;

  private List[] nodesWithInvocationData;

  /**
   * Constructor for EncapsulateTreeTableModel.
   * @param fieldA filed being encapsulated
   * @param usagesA usages of that field, list of InvocationData objects
   */
  public EncapsulateTreeTableModel(List fieldA, List usagesA) {
    super(new BinTreeTableNode("", false));

    BinField field;

    List usages;
    int size = fieldA.size();
    BinTreeTableNode node;

    this.nodesWithInvocationData = new List[size];

    for (int x = 0; x < size; x++) {
      if((BinField)fieldA.get(x)!=null) {
        this.nodesWithInvocationData[x] = new LinkedList();

        field = (BinField) fieldA.get(x);
        usages = (List) usagesA.get(x);

        this.field = field;

        hostingClass = field.getOwner().getBinCIType();
        innerClasses = new ArrayList();
        innerClasses.addAll(Arrays.asList(hostingClass.getDeclaredTypes()));

        node = new BinTreeTableNode(field, false);

        initChildren(usages, node, x);

        if(node.getChildCount() > 0) {
          ( (BinTreeTableNode) getRoot()).addChild(node);
        }

        if (notPossibleBranch != null) {
          notPossibleBranch.setShowCheckBox(false);
          notPossibleBranch.setSelected(false);
        }

        declaringBranch = null;
        subclassesBranch = null;
        otherBranch = null;
        notPossibleBranch = null;
        hostingClass = null;
        innerClasses = null;

      }

    }

    ( (BinTreeTableNode) getRoot()).reflectLeafNumberToParentName();
    ( (BinTreeTableNode) getRoot()).sortAllChildren();

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
    if (node instanceof BinTreeTableNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          String lineNumber = ((BinTreeTableNode) node).getLineNumber();
          return lineNumber + NUMBER_PADDING;
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

  private void initChildren(List usages, BinTreeTableNode chnode, int nr) {
    BinTreeTableNode root;//(BinTreeTableNode) getRoot();
    root = chnode;//(BinTreeTableNode) getRoot();
    // Append nodes
    for (int i = 0, max = usages.size(); i < max; i++) {
      final EncapsulationInvocationData data =
          (EncapsulationInvocationData) usages.get(i);

      boolean possible = data.isEncapsulationPossible();

      BinTreeTableNode node = null;
      Object item = data.getWhere();
      if (item instanceof BinCIType) {
        // FIXME: should somehow use getPackage()?
        node = getSuitableNode(((BinType) item).getTypeRef(), possible, chnode);
      } else if (item instanceof BinMember) {
        if (Assert.enabled) {
          Assert.must(((BinMember) item).getOwner() != null,
              "Owner of " + ((BinMember) item).getName() + " is null, class: " +
              item.getClass().getName());
        }

        BinTypeRef owner = ((BinMember) item).getOwner();
        node = getSuitableNode(owner, possible, chnode);
      } else if (item instanceof BinTypeRef) {
        node = getSuitableNode((BinTypeRef) item, possible, chnode);
      } else if (item instanceof CompilationUnit) {
        BinPackage pack = ((CompilationUnit) item).getPackage();
        if (pack != null) {
          node = (BinTreeTableNode) getOtherBranch(chnode).findParent(pack, false);
        } else {
          node = root;
        }
      } else {
        Assert.must(false,
            "Unsupported Location in EncapsulateTreeTableModel: "
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

      if (source == null) {
        Assert.must(false, "Unsupported item in EncapsulateTreeTableModel: "
            + item.getClass().getName());
      }

      InvocationTreeTableNode bn = (InvocationTreeTableNode)
          node.findChildByType(item, data.getLineNumber());
      if (bn != null) {
        if (((ASTImpl) bn.getAsts().get(0)) == data.getWhereAst()) {
          continue;
        }
      }

      bn = new InvocationTreeTableNode(item);
      bn.addAst(data.getWhereAst());
      bn.setSourceHolder(source);

      // put InvocationData to the leaf node
      bn.setInvocationData(data);
      nodesWithInvocationData[nr].add(bn);

      node.addChild(bn);

      if (item instanceof LocationAware &&
          hostingClass.contains((LocationAware) item)) {
        bn.setSelected(false);
      }
    }
  }

  /**
   * Determines the most suitable node for usage in the given type.
   */
  private BinTreeTableNode getSuitableNode(BinTypeRef type,
      boolean changePossible, BinTreeTableNode chnode) {
    if (changePossible) {
      if (type.equals(hostingClass.getTypeRef()) || innerClasses.contains(type)) {
        return (BinTreeTableNode) getDeclaringBranch(chnode).findParent(type, false);
      } else if (Arrays.asList(type.getSupertypes()).contains(hostingClass.getTypeRef())) {
        return (BinTreeTableNode) getSubclassesBranch(chnode).findParent(type, false);
      } else {
        return (BinTreeTableNode) getOtherBranch(chnode).findParent(type, false);
      }
    } else {
      BinTreeTableNode node = (BinTreeTableNode) getNotPossibleBranch(chnode).
          findParent(type, false);
      return node;
    }
  }

  private BinTreeTableNode getDeclaringBranch(BinTreeTableNode node) {
    if (this.declaringBranch == null) {
      this.declaringBranch = new BinTreeTableNode("Usages in declaring class");
      //((BinTreeTableNode) getRoot()).addChild(declaringBranch);
      node.addChild(declaringBranch);
    }
    return this.declaringBranch;
  }

  private BinTreeTableNode getSubclassesBranch(BinTreeTableNode node) {
    if (this.subclassesBranch == null) {
      this.subclassesBranch = new BinTreeTableNode("Usages in subclasses");
      //((BinTreeTableNode) getRoot()).addChild(subclassesBranch);
      node.addChild(subclassesBranch);
    }
    return this.subclassesBranch;
  }

  private BinTreeTableNode getOtherBranch(BinTreeTableNode node) {
    if (this.otherBranch == null) {
      this.otherBranch = new BinTreeTableNode("Other usages");
      //((BinTreeTableNode) getRoot()).addChild(otherBranch);
      node.addChild(otherBranch);
    }

    return this.otherBranch;
  }

  private BinTreeTableNode getNotPossibleBranch(BinTreeTableNode node) {
    if (this.notPossibleBranch == null) {
      this.notPossibleBranch = new BinTreeTableNode(
          "Usages that cannot be modified");
      //((BinTreeTableNode) getRoot()).addChild(notPossibleBranch);
      node.addChild(notPossibleBranch);
    }

    return this.notPossibleBranch;
  }

  public List getUsages(int nr) {
    List usages = new LinkedList();

    Iterator iter = nodesWithInvocationData[nr].iterator();
    while (iter.hasNext()) {
      InvocationTreeTableNode n = (InvocationTreeTableNode) iter.next();

      if (n.isCheckBoxNeeded() && n.isSelected()) {
        usages.add(n.getInvocationData());
      }
    }

    return usages;
  }
}
