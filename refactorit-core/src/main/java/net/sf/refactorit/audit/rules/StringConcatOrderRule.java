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
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Tsernetsov
 */

public class StringConcatOrderRule extends AuditRule {
  public static final String NAME = "string_concat_order";

  public void visit(BinStringConcatenationExpression expr) {
    BinExpression leftExpr = expr.getLeftExpression();

    if (leftExpr instanceof BinArithmeticalExpression) {
      ASTImpl block = leftExpr.getRootAst().getParent();
      if ((block != null) && (block.getType() != JavaTokenTypes.LPAREN)) {
        addViolation(new StringConcatViolation(leftExpr));
      }
    }
    super.visit(expr);
  }
}

class StringConcatViolation extends AwkwardSourceConstruct {

  public StringConcatViolation(BinSourceConstruct construct) {
    super(construct, "Arithmetical expression`s concatenation with string",
        "refact.audit.string_concat_order");
  }

  public List getCorrectiveActions() {
    List actionList = new ArrayList(2);
    actionList.add(ConcatenateEmptyStringAction.INSTANCE);
    actionList.add(EmbraceArithmExpressionAction.INSTANCE);

    return actionList;
  }

  public BinMember getSpecificOwnerMember() {
    return (getSourceConstruct()).getParentMember();
  }
}

// Add an empty string before arithmetical statement corrective action

class ConcatenateEmptyStringAction extends MultiTargetCorrectiveAction {
  static final ConcatenateEmptyStringAction INSTANCE = new ConcatenateEmptyStringAction();

  public String getKey() {
    return "refactorit.audit.action.string_concat_order.addemptystring";
  }

  public String getName() {
    return "Add an empty string before arithmetical expression";
  }

  public String getMultiTargetName() {
    return "Add empty strings before arithmetical expressions";
  }

  protected Set process(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof StringConcatViolation)) {
      return Collections.EMPTY_SET;
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinArithmeticalExpression expr = (BinArithmeticalExpression) ((AwkwardSourceConstruct) violation)
        .getSourceConstruct();

    StringInserter inserter = new StringInserter(compilationUnit, expr
        .getStartLine(), expr.getStartColumn() - 1, "\"\" + ");
    manager.add(inserter);

    return Collections.singleton(compilationUnit);
  }
}

// Embrace arithmetical statement corrective action

class EmbraceArithmExpressionAction extends MultiTargetCorrectiveAction {
  static final EmbraceArithmExpressionAction INSTANCE = new EmbraceArithmExpressionAction();

  public String getKey() {
    return "refactorit.audit.action.string_concat_order.embracearithm";
  }

  public String getName() {
    return "Embrace arithmetical expression";
  }

  public String getMultiTargetName() {
    return "Embrace arithmetical expressions";
  }

  protected Set process(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof StringConcatViolation)) {
      return Collections.EMPTY_SET;
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinArithmeticalExpression expr = (BinArithmeticalExpression) ((AwkwardSourceConstruct) violation)
        .getSourceConstruct();

    StringInserter inserter = new StringInserter(compilationUnit, expr
        .getStartLine(), expr.getStartColumn() - 1, "(");

    manager.add(inserter);

    inserter = new StringInserter(compilationUnit, expr.getEndLine(), expr
        .getEndColumn() - 1, ")");
    manager.add(inserter);
    return Collections.singleton(compilationUnit);
  }

}
