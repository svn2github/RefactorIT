/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.classmodelvisitor;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.HashSet;
import java.util.Set;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */

public class AstVisitorModel extends BinTreeTableModel {
  private SourceHolder source;
  private BinTreeTableNode targetNode = null;

  AstVisitorModel(Project project, Object target) {
    super(new BinTreeTableNode("root", false));
System.err.println("ASTVisitor target = " + target.getClass());

    this.targetNode = new BinTreeTableNode(target);
    if (target instanceof BinMember) {
//      if (target instanceof BinCIType
//          && ((BinCIType) target).getCompilationUnit().getMainType().equals(
//          ((BinCIType) target).getTypeRef())) {
//        this.targetNode.addAst(((BinCIType) target).getCompilationUnit().getSource().getFirstNode());
//      } else {
        this.targetNode.addAst(((BinMember) target).getOffsetNode());
//      }
    } else if (target instanceof SourceConstruct) {
      this.targetNode.addAst(((SourceConstruct) target).getRootAst());
    }

    ((BinTreeTableNode) getRoot()).addChild(targetNode);

    ASTImpl rootAst = null;

    if (targetNode.getAsts().size() > 0) {
      if (rootAst == null) {
        rootAst = (ASTImpl) targetNode.getAsts().get(0);
      }
      source = targetNode.getSource();

      buildTree(targetNode, rootAst);
    }
  }

  private Set loopBreaker = new HashSet();
  private void buildTree(BinTreeTableNode parent, ASTImpl astRoot) {
    if (this.loopBreaker.contains(astRoot)) {
      parent.addChild(new BinTreeTableNode("loop: " + astRoot));
      return;
    }

    this.loopBreaker.add(astRoot);

    for (ASTImpl cur = astRoot; cur != null; cur = (ASTImpl) cur.getNextSibling()) {
      BinTreeTableNode addNode = new BinTreeTableNode(cur, false);
      addNode.addAst(cur);
      addNode.setSourceHolder(source);
      parent.addChild(addNode);
      buildTree(addNode, (ASTImpl) cur.getFirstChild());
      if (parent == this.targetNode) {
        break;
      }
    }

    this.loopBreaker.remove(astRoot);
  }
}
