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
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Villu Ruusmann
 * @author Anton Safonov
 */
public class StringToStringRule extends AuditRule {
  public static final String NAME = "to_string";

  public void visit(BinMethodInvocationExpression expression) {
    BinMethod method = expression.getMethod();

    if (method.isToString()) {
      BinExpression left = expression.getExpression();

      if (left != null && left.getReturnType().isString()) {
        addViolation(new StringToString(expression));
      }
    }

    super.visit(expression);
  }
}


class StringToString extends AwkwardExpression {
  StringToString(BinExpression expression) {
    super(expression, "Avoid String.toString()", "refact.audit.to_string");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveStringToString.instance);
  }
}


class RemoveStringToString extends MultiTargetCorrectiveAction {
  static final RemoveStringToString instance = new RemoveStringToString();

  public String getKey() {
    return "refactorit.audit.action.toString.remove";
  }

  public String getName() {
    return "Remove redundant toString() call";
  }

  public String getMultiTargetName() {
    return "Remove redundant toString() calls";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof StringToString)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    ASTImpl start;
    ASTImpl end;
    // FIXME refactor, looks bad
    if (((AwkwardSourceConstruct) violation).getSourceConstruct().getParent()
        instanceof BinExpressionStatement
        && !((BinMethodInvocationExpression)
        ((AwkwardSourceConstruct) violation).getSourceConstruct()).
        getExpression()
        .isChangingAnything()) {
      // can't leave orphan variable invocations
      start = end = violation.getAst().getParent();
    } else {
      start = (ASTImpl) violation.getAst().getFirstChild();
      end = (ASTImpl) start.getNextSibling();
    }
    final StringEraser stringEraser = new StringEraser(compilationUnit,
        start.getStartLine(), start.getStartColumn() - 1,
        end.getEndLine(), end.getEndColumn() - 1);
    stringEraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(stringEraser);

    return Collections.singleton(compilationUnit);
  }
}
