/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ChildrenResultsUpForwardingVisitor;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.List;

/**
 *
 * @author Arseni Grigorjev
 */
public class SelectFieldFromProjectVisitor
    extends ChildrenResultsUpForwardingVisitor {
  public static final ProgressMonitor.Progress PROJECT_PARSE_PROGRESS =
      new ProgressMonitor.Progress(0, 100);

  private BinTreeTableModel model;
  private BinTypeRef accessedFrom;
  private BinTypeRef returnType;

  public SelectFieldFromProjectVisitor(BinTreeTableModel model,
      BinTypeRef accessedFrom, BinTypeRef returnType){
    super();

    this.model = model;
    this.accessedFrom = accessedFrom;
    this.returnType = returnType;
  }

  public void visit(Project proj){
    super.visit(proj);

    List results = getCurrentResults();
    for (int i = 0; i < results.size(); i++){
      insertIntoPackage((BinTreeTableNode) model.getRoot(),
          (BinTreeTableNode) results.get(i));
    }

    ((BinTreeTableNode) model.getRoot()).sortAllChildren(
        ProjectTreeNodesComparator.instance);
  }

  public void visit(CompilationUnit unit){
    super.visit(unit);
    getTopResults().addAll(getCurrentResults());
  }

  public void visit(BinCIType type){
    super.visit(type);

    BinTreeTableNode node = new BinTreeTableNode(type);

    List results = getCurrentResults();
    if (results.size() > 0){
      for (int i = 0; i < results.size(); i++){
        node.addChild(results.get(i));
      }
    }

    BinField[] fields = type.getDeclaredFields();
    for (int i = 0; i < fields.length; i++){
      if (fields[i].isStatic() && fields[i].isFinal()
          && fields[i].getTypeRef().equals(this.returnType)
          && fields[i].isAccessible(fields[i].getOwner().getBinCIType(),
              accessedFrom.getBinCIType())){
        node.addChild(new BinTreeTableNode(fields[i]));
      }
    }

    if (node.getChildCount() > 0){
      getTopResults().add(node);
    }
  }

  public static void insertIntoPackage(BinTreeTableNode rootNode,
      BinTreeTableNode node){
    BinPackage pack = ((BinType) node.getBin()).getPackage();
    List children = rootNode.getChildren();
    boolean found = false;
    for (int i = 0; i < children.size(); i++){
      BinTreeTableNode curNode = (BinTreeTableNode) children.get(i);
      if (curNode.getBin() == pack){
        curNode.addChild(node);
        found = true;
        break;
      }
    }

    if (!found){
      BinTreeTableNode newNode = new BinTreeTableNode(pack);
      rootNode.sortAllChildren(ProjectTreeNodesComparator.instance);
      rootNode.addChild(newNode);
      newNode.addChild(node);
    }
  }
}
