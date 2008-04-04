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
import net.sf.refactorit.audit.AwkwardStatement;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinEmptyStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class EmptyStatementRule extends AuditRule {
  public static final String NAME = "empty_statement";

  private int nonSlistDepth = 0;

  public void visit(BinEmptyStatement statement) {
    if (nonSlistDepth == 0) {
      addViolation(new EmptyStatement(statement));
    }
    super.visit(statement);
  }

  public void visit(BinStatementList list) {
    if(list.getRootAst().getType() != JavaTokenTypes.SLIST) {
      nonSlistDepth++;
    }
    super.visit(list);
  }
  
  public void leave(BinStatementList list) {
    if(list.getRootAst().getType() != JavaTokenTypes.SLIST) {
      nonSlistDepth--;
    }
  }
}


class EmptyStatement extends AwkwardStatement {
  EmptyStatement(BinEmptyStatement statement) {
    super(statement, "Redundant semicolon", "refact.audit.empty_statement");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveEmptyStatement.instance);
  }
    
  public BinMember getSpecificOwnerMember() {
    return ( getSourceConstruct()).getParentMember();
  }  
}


class RemoveEmptyStatement extends MultiTargetCorrectiveAction {
  static final RemoveEmptyStatement instance = new RemoveEmptyStatement();

  public String getKey() {
    return "refactorit.audit.action.semicolon.remove";
  }

  public String getName() {
    return "Remove redundant semicolon";
  }

  public String getMultiTargetName() {
    return "Remove redundant semicolons";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof EmptyStatement)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    ASTImpl ast = violation.getAst();
    StringEraser eraser = new StringEraser(compilationUnit, ast, true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    return Collections.singleton(compilationUnit);
  }
}
