/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.parser.JavaTokenTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Tsernetsov
 */

public class DangerousIteratorUsageRule extends AuditRule {
  public static final String NAME = "dangerous_iterator";

  private static BinTypeRef iterTypeRef;

  private static BinMethod nextMethod;

  private static BinMethod hasNextMethod;

  /** Key - BinVariable, Value - BinAssignmentExpression */
  private MultiValueMap assignments = new MultiValueMap();

  /** Key - BinVariable, Value - BinMethodInvocationExpression */
  private MultiValueMap hasNextInvocations = new MultiValueMap();

  /** Key - BinVariable, Value - BinMethodInvocationExpression */
  private MultiValueMap nextInvocations = new MultiValueMap();

  public void visit(CompilationUnit cu) {
    if (iterTypeRef == null) {
      iterTypeRef = cu.getProject().getTypeRefForName("java.util.Iterator");
      if(iterTypeRef!=null && iterTypeRef.getBinCIType()!=null) {
        nextMethod = iterTypeRef.getBinCIType().getDeclaredMethod("next",
            new BinTypeRef[] {});
        hasNextMethod = iterTypeRef.getBinCIType().getDeclaredMethod("hasNext",
            new BinTypeRef[] {});
      } else {
        iterTypeRef = null;
      }
    }
  }

  public void visit(BinMethodInvocationExpression expr) {
    if (iterTypeRef!=null && 
        expr.getExpression() instanceof BinVariableUseExpression) {
      BinVariable var = ((BinVariableUseExpression) expr.getExpression())
          .getVariable();
      if (iterTypeRef.equals(var.getTypeRef())) {
        BinMethod method = expr.getMethod();
        if (hasNextMethod.equals(method)) {
          hasNextInvocations.put(var, expr);
        } else if (nextMethod.equals(method)) {
          nextInvocations.put(var, expr);
        }
      }
    }
    super.visit(expr);
  }

  public void visit(BinLocalVariableDeclaration expr) {
    BinVariable[] vars = expr.getVariables();
    for (int i = 0; i < vars.length; i++) {
      if (iterTypeRef!=null && iterTypeRef.equals(vars[i].getTypeRef())
          && vars[i].getExpression() != null) {
        assignments.put(vars[i], expr);
      }
    }
    super.visit(expr);
  }

  public void visit(BinAssignmentExpression expr) {
    BinExpression leftExpr = expr.getLeftExpression();
    if (leftExpr instanceof BinVariableUseExpression 
        && iterTypeRef!=null 
        && (iterTypeRef.equals(((BinVariableUseExpression) leftExpr)
            .getReturnType()))) {
      assignments
          .put(((BinVariableUseExpression) leftExpr).getVariable(), expr);
    }
    super.visit(expr);
  }

  public void postProcess() {
    if(iterTypeRef!=null) {
      for (Iterator it = nextInvocations.keySet().iterator(); it.hasNext();) {
        BinVariable var = (BinVariable) it.next();
  
        // prepare list of Iterator.hasNext() responsibility blocks
        BidirectionalMap hasNextBlocks = findHasNextBlocks(hasNextInvocations
            .get(var));
        if (hasNextBlocks == null) {
          continue;
        }
  
        // for each next() invocation block find suitable hasNext()
        MultiValueMap corresponds = findOwnerBlocks(var,
            nextInvocations.get(var), assignments.get(var), hasNextBlocks);
  
        // resolve duplicate correspondings to responsibility blocks
        for (Iterator iter = corresponds.keySet().iterator(); iter.hasNext();) {
          BinStatementList block = (BinStatementList) iter.next();
          List expressions = corresponds.get(block);
  
          if (expressions.size() > 1) {
            List results = new ArrayList();
            BinMethodInvocationExpression first = 
              (BinMethodInvocationExpression) expressions.get(0);
  
            // get main owner conditional statement
            BinStatement owner = getWrappingStatement(
                (BinMethodInvocationExpression) expressions.get(0), block);
  
            if (owner != null) {
              defineNonViolative(expressions, owner, results);
            } else {
              results.add(first);
            }
            // remove all approved results - violations will remain
            expressions.removeAll(results);
  
            for (int i = 0; i < expressions.size(); i++) {
              BinMethodInvocationExpression tmpNextInv 
              = (BinMethodInvocationExpression) expressions.get(i);
              addViolation(new DangerousIteratorUsageViolation(tmpNextInv));
            }
          }
        }
  
        corresponds.clear();
        hasNextBlocks.clear();
      }
    }
    nextInvocations.clear();
    hasNextInvocations.clear();
    assignments.clear();
  }

  private List defineNonViolative(List expressions, BinStatement owner,
      List results) {

    MultiValueMap blockExpressions = new MultiValueMap();
    MultiValueMap expressionBlocks = new MultiValueMap();

    // fill potential results
    for (int k = 0; k < expressions.size(); k++) {
      LocationAware inv = (LocationAware) expressions.get(k);
      if (owner.contains(inv)) {
        results.add(inv);
      }
    }

    if (expressions != null) {
      int depth = fillMaps(blockExpressions, expressionBlocks, results);
      for (int k = depth; k >= 0; k--) { // bypass the block wrapping hierarchy
        MultiValueMap mergedIfelseExprs = new MultiValueMap();

        // define expressions, that can be merged into ifelse structure
        /*
         * ArrayList sorted = new ArrayList(expressionBlocks.keySet());
         * Collections.sort(sorted, BlockComparator.instance);
         */

        for (Iterator it = expressionBlocks.keySet().iterator(); it.hasNext();) {
          Object expr = it.next();
          List blockList = expressionBlocks.get(expr);
          if (blockList.size() == k && k != 0) {
            BinStatementList block = (BinStatementList) blockList.get(0);

            List exprList = blockExpressions.get(block);
            if (exprList != null) {
              BinItemVisitable key = block.getParent();
              if (isFromTryStatement(key)) {
                key = key.getParent();
              }
              mergedIfelseExprs.putAll(key, blockExpressions.get(block));
            }
            blockExpressions.clearKey(block);
          }
        }

        /*
         * sorted.clear(); sorted.addAll(mergedIfelseExprs.keySet());
         * Collections.sort(sorted, BlockComparator.instance);
         */
        // merge expressions to lower level wrapper block expressions
        for (Iterator it = mergedIfelseExprs.keySet().iterator(); it.hasNext();) {
          BinStatement condWrapper = (BinStatement) it.next();
          List exprs = mergedIfelseExprs.get(condWrapper);
          if (exprs.size() > 0) {
            List blockList = expressionBlocks.get(exprs.get(0));
            if (blockList != null && blockList.size() > 0) {
              blockList.remove(0);
              if (blockList.size() > 0) {
                BinStatementList block = (BinStatementList) blockList.get(0);
                if (blockExpressions.containsKey(block)) {
                  results.removeAll(exprs);
                } else {
                  blockExpressions.putAll(block, exprs);
                }
              }
            }
          }
        }
      }
    }

    return results;
  }

  private boolean isFromTryStatement(BinItemVisitable item) {
    return (item instanceof BinTryStatement.TryBlock
        || item instanceof BinTryStatement.CatchClause 
        || item instanceof BinTryStatement.Finally);
  }

  /**
   * Fills appropriate data structures for following algorithm processing
   * 
   * @param blockExpressions -
   *          method invocation expressions corresponding to statement list
   *          block
   * @param expressionBlocks -
   *          block wrapping hierarchy of candidate expressions
   * @param expressions -
   *          source list of next() invocation expressions
   * @return - maximum depth of block wrapping hierarchy
   */
  private int fillMaps(MultiValueMap blockExpressions,
      MultiValueMap expressionBlocks, List expressions) {
    int maxDepth = 0;
    List removed = new ArrayList();
    for (int i = 0; i < expressions.size(); i++) {
      BinMethodInvocationExpression expr = 
        (BinMethodInvocationExpression) expressions.get(i);
      BinItemVisitable parent = expr.getParent();
      int depth = 0;
      boolean firstBlockPassed = false;

      while (parent != null
          && !(parent instanceof BinMethod) && !(parent instanceof BinClass)) {
        BinItemVisitable ownerItem = parent.getParent();
        if (isFromTryStatement(ownerItem)) {
          ownerItem = ownerItem.getParent();
        }

        if (parent instanceof BinStatementList
            && ownerItem != null
            && (ownerItem instanceof BinIfThenElseStatement
                || ownerItem instanceof BinSwitchStatement 
                || ownerItem instanceof BinTryStatement)) {

          if (!firstBlockPassed) {
            if (blockExpressions.containsKey(parent)) {
              removed.add(expr);
              break;
            } else {
              blockExpressions.put(parent, expr);
              firstBlockPassed = true;
            }
          }
          expressionBlocks.put(expr, parent);
          if (++depth > maxDepth) {
            maxDepth = depth;
          }
        }
        parent = ownerItem;
      }
    }
    expressions.removeAll(removed);
    expressionBlocks.removeKeys(removed);
    removed.clear();
    return maxDepth;
  }

  private BinStatement getWrappingStatement(BinItemVisitable expr,
      BinStatementList block) {
    BinItemVisitable parent = expr.getParent();
    BinStatement result = null;
    while (parent != null 
        && block.contains((LocationAware) parent)
        && !(parent instanceof BinMethod) 
        && !(parent instanceof BinClass)) {
      BinItemVisitable ownerItem = parent.getParent();
      if (isFromTryStatement(ownerItem)) {
        ownerItem = ownerItem.getParent();
      }
      if (parent instanceof BinStatementList
          && ownerItem != null
          && (ownerItem instanceof BinIfThenElseStatement
              || ownerItem instanceof BinSwitchStatement 
              || ownerItem instanceof BinTryStatement)) {
        result = (BinStatement) parent.getParent();
      }
      parent = ownerItem;
    }

    return result;
  }

  /**
   * @param var
   *          BinVariable - variable
   * @param nextInvocations -
   *          List of next() invocations for all variables
   *          {BinMethodInvocationExpression}
   * @param assignments -
   *          MultiValueMap of assignments invocations for all variables
   *          {BinLocalVariableDeclaration, BinAssignmentExpression}
   * @return multivalue map: key block {BinStatementList}, value - method next()
   *         invocations {BinMethodInvocationExpression}
   */
  private MultiValueMap findOwnerBlocks(BinVariable var, List nextInv,
      List assignments, BidirectionalMap hasNextBlocks) {
    if (nextInv == null) {
      return null;
    }

    MultiValueMap corresponds = new MultiValueMap();
    for (int i = 0; i < nextInv.size(); i++) {
      BinMethodInvocationExpression expr = (BinMethodInvocationExpression) nextInv
          .get(i);
      BinStatementList ownerBlock = getSuitableOwnerBlock(hasNextBlocks
          .getKeySet(), expr);

      if (ownerBlock == null) {
        // no appropriate hasNext block found
        addViolation(new DangerousIteratorUsageViolation(expr));
      } else {

        BinMethodInvocationExpression hasNextCall = 
          (BinMethodInvocationExpression) hasNextBlocks.getValueByKey(ownerBlock);

        if (hasNextCall != null && goesAfter(hasNextCall, expr)) {
          // if hasNext() check goes after next() call
          addViolation(new DangerousIteratorUsageViolation(expr));
        } else {
          if (assignments != null
              && isAssignmentBetween(ownerBlock, expr, assignments)) {
            // next() is preceded by violative assignment
            addViolation(new DangerousIteratorUsageViolation(expr));
          } else {
            corresponds.put(ownerBlock, expr);
          }
        }
      }
    }
    return corresponds;
  }

  private BidirectionalMap findHasNextBlocks(List hasNextInv) {
    if (hasNextInv == null) {
      return null;
    }

    BidirectionalMap result = new BidirectionalMap();
    for (int i = 0; i < hasNextInv.size(); i++) {
      BinMethodInvocationExpression expr = 
        (BinMethodInvocationExpression) hasNextInv.get(i);
      BinStatementList block = getConditionBlock(expr);
      if (block != null) {
        result.put(block, expr);
      }
    }
    return result;
  }

  private boolean goesAfter(BinSourceConstruct hasNextInv,
      BinSourceConstruct nextInv) {
    return (hasNextInv.getStartLine() > nextInv.getStartLine() || (hasNextInv
        .getStartLine() == nextInv.getStartLine() && hasNextInv
        .getStartColumn() > nextInv.getEndColumn()));
  }

  private boolean isAssignmentBetween(BinStatementList list,
      BinMethodInvocationExpression expr, List assignments) {
    int startLine;
    int startColumn;
    for (int i = 0; i < assignments.size(); i++) {
      BinSourceConstruct assignment = (BinSourceConstruct) assignments.get(i);
      if (list.contains(assignment)) {
        startLine = assignment.getStartLine();
        startColumn = assignment.getStartColumn();
        if (startLine < expr.getStartLine()
            || (startLine == expr.getStartLine() && startColumn < expr
                .getStartColumn())) {
          return true;
        }
      }
    }
    return false;
  }

  private BinStatementList getSuitableOwnerBlock(Set blocks,
      BinMethodInvocationExpression expr) {
    BinStatementList block = null;
    BinItemVisitable parent = expr.getParent();

    while (parent != null) {
      if (parent instanceof BinStatementList && blocks.contains(parent)) {
        block = (BinStatementList) parent;
        break;
      } else if (parent instanceof BinMethod || parent instanceof BinClass) {
        break;
      }
      parent = parent.getParent();
    }

    return block;
  }

  private BinStatementList getConditionBlock(BinMethodInvocationExpression expr) {
    BinStatementList block = null;
    BinItemVisitable parent = expr.getParent();

    boolean notExprUsed = false;

    while (!(parent instanceof BinStatementList)) {
      if (parent instanceof BinWhileStatement) {
        block = ((BinWhileStatement) parent).getStatementList();
        break;
      } else if (parent instanceof BinForStatement) {
        block = ((BinForStatement) parent).getStatementList();
        break;
      } else if (parent instanceof BinIfThenElseStatement) {
        if (!notExprUsed) {
          block = ((BinIfThenElseStatement) parent).getTrueList();
        } else {
          block = ((BinIfThenElseStatement) parent).getFalseList();
        }
        break;
      } else if (parent instanceof BinUnaryExpression) {
        if (((BinUnaryExpression) parent).getRootAst().getType() 
            == JavaTokenTypes.LNOT) {
          notExprUsed = !notExprUsed;
        }
      }
      parent = parent.getParent();
    }
    return block;
  }

  /*
   * static class BlockComparator implements Comparator { public static final
   * BlockComparator instance = new BlockComparator(); public int compare(Object
   * o1, Object o2) { if(o1 instanceof LocationAware && o2 instanceof
   * LocationAware) { return ((LocationAware)o1).getStartPosition() -
   * ((LocationAware)o2).getStartPosition(); } else return Integer.MIN_VALUE; }
   * 
   * public boolean equals(Object obj) { return (obj instanceof
   * BlockComparator); } }
   */
}

class DangerousIteratorUsageViolation extends AwkwardExpression {
  public DangerousIteratorUsageViolation(BinExpression expr) {
    super(expr, "Dangerous iterator usage", "");
  }
}
