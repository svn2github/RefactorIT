/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.classmodelvisitor;


import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.CallbackModel;
import net.sf.refactorit.query.CallbackVisitor;
import net.sf.refactorit.refactorings.SelectionAnalyzer;
import net.sf.refactorit.refactorings.extract.FlowAnalyzer;
import net.sf.refactorit.refactorings.extract.VariableUseAnalyzer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.ResourceBundle;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class ClassmodelVisitorModel extends BinTreeTableModel implements
    CallbackModel {
  private FastStack nodes = new FastStack();
  private BinTreeTableNode currentNode;
  private BinTreeTableNode lastNode;

  private CallbackVisitor dumper;

  public static final boolean FLOWTEST = false;
  public static final boolean VARANALYZERTEST = false;

  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(ClassmodelVisitorModel.class);

  public ClassmodelVisitorModel(RefactorItContext context, Object target) {
    super(new ClassmodelVisitorNode(resLocalizedStrings.getString("action.name")));
System.err.println("ClassmodelVisitorModel target = " + target.getClass());

    dumper = new CallbackVisitor(this);

    currentNode = (BinTreeTableNode) getRoot();

    if (target == null) {
      dumper.visit(context.getProject());
    } else {
      if (FLOWTEST) {
// FIXME: outdated
//        FlowAnalyzer visitor = new FlowAnalyzer() {
//          private FastStack nodeStack = new FastStack();
//          {
//            // initializer
//            nodeStack.push(currentNode);
//          }
//
//          public void onEnter(Object o) {
//            BinTreeTableNode currentNode = (BinTreeTableNode)nodeStack.peek();
//            BinTreeTableNode newNode = new BinTreeTableNode(o.getClass().getName() + " - " + getCurrentFlow());
//            if( o instanceof BinSourceConstruct) {
//              newNode.addAst( ((BinSourceConstruct)o).getRootAst() );
//              newNode.setCompilationUnit( ((BinSourceConstruct)o).getCompilationUnit() );
//            }
//            currentNode.addChild(newNode);
//            nodeStack.push(newNode);
//          }
//
//          public void onLeave(Object o) {
//            nodeStack.pop();
//          }
//
//          protected boolean isInside() { return false; }
//
//          protected boolean isAfter() { return false; }
//        };
//        //visitor.startFlowBlock();
//        ((BinItem)target).accept(visitor);
//        //visitor.endFlowBlock();
      } else if (VARANALYZERTEST) {
//        if (target instanceof BinMemberInvocationExpression) {
//          target = ((BinMemberInvocationExpression) target).getMember();
//        }

        VariableUseAnalyzer analyzer = new VariableUseAnalyzer(
            context, (BinMember) target, new ArrayList());
        buildTreeRecursively((BinTreeTableNode) getRoot(),
            analyzer.getTopBlock());

      } else if (target instanceof BinSelection) {
        List items = new SelectionAnalyzer((BinSelection) target).getSelectedItems();
        for (int i = 0; i < items.size(); i++) {
          Object o = items.get(i);
          if (o instanceof BinItemVisitable) {
            ((BinItemVisitable) o).accept(dumper);
          }
        }
      } else {
        if (target instanceof BinExpression) {
          target = ((BinExpression) target).getEnclosingStatement();
        }

        if (target instanceof BinItemVisitable) {
          ((BinItemVisitable) target).accept(dumper);
        }
      }
    }
  }

  private void buildTreeRecursively(BinTreeTableNode parent,
      FlowAnalyzer.Flow curFlow) {
    BinTreeTableNode child
        = new BinTreeTableNode(curFlow.toString(), false);
    parent.addChild(child);

    if (curFlow.children != null) {
      for (int i = 0, max = curFlow.children.size(); i < max; i++) {
        final FlowAnalyzer.Flow childFlow
            = (FlowAnalyzer.Flow) curFlow.children.get(i);
        buildTreeRecursively(child, childFlow);
      }
    }
  }

  /**
   * @used
   */
  private ClassmodelVisitorModel(Object root) {
    super(root);
  }

  public void goUp() {
    try {
      currentNode = (ClassmodelVisitorNode) nodes.pop();
      lastNode = currentNode;
    } catch (EmptyStackException e) {
      currentNode.addChild(new ClassmodelVisitorNode("empty stack"));
    }
  }

  public void goDown() {
    nodes.push(currentNode);
    currentNode = lastNode;
  }

  public void callback(Object item) {
    lastNode = new ClassmodelVisitorNode(item);
    currentNode.addChild(lastNode);

    /*
        // test
        if(item instanceof LocationAware) {
          LocationAware lw = (LocationAware)item;
          try {
            lw.getCompilationUnit();
          } catch(NullPointerException ex) {
            //ex.printStackTrace();
            System.err.println("NPE happened");
          }
          System.err.print("DEBUG:" + lw.getClass().getName() + " ");
          System.err.println(lw.getCompilationUnit().getRelativePath()+" - " +
            "["+lw.getStartLine()+","+lw.getStartColumn() + "] - [" +
            lw.getEndLine()+","+lw.getEndColumn()+"]"
          );
        }
     */
  }
}
