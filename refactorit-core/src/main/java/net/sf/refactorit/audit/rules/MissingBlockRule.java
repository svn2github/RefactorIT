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
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinStatementListFormatter;
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
public class MissingBlockRule extends AuditRule {
  public static final String NAME = "missing_block";

  public void visit(BinForStatement statement) {
    if (!hasBlock(statement.getStatementList())) {
      addViolation(new BlocklessForStatement(statement));
    }

    super.visit(statement);
  }

  public void visit(BinIfThenElseStatement statement) {
    if (!hasBlock(statement.getTrueList())) {
      addViolation(new BlocklessIfStatement(statement));
    }

    if (!hasBlock(statement.getFalseList(), true)) {
      addViolation(new BlocklessElseStatement(statement));
    }

    super.visit(statement);
  }

//  public void visit(BinLabeledStatement statement) {
//    if (!hasBlock(statement.getLabelStatementList())) {
//      addViolation(new BlocklessLabeledStatement(statement));
//    }
//
//    super.visit(statement);
//  }

  public void visit(BinWhileStatement statement) {
    if (!hasBlock(statement.getStatementList())) {
      if (statement.isDoWhile()) {
        addViolation(new BlocklessDoStatement(statement));
      } else {
        addViolation(new BlocklessWhileStatement(statement));
      }
    }

    super.visit(statement);
  }

  private static boolean hasBlock(BinStatementList list) {
    return hasBlock(list, false);
  }

  private static boolean hasBlock(BinStatementList list, boolean isRightAfterElse) {
    if (list != null) {
      int type = list.getRootAst().getType();

      // Nested if-then-else construct
      if (type == JavaTokenTypes.LITERAL_if) {
        return isRightAfterElse;
      }

      return (type == JavaTokenTypes.SLIST);
    }

    return true;
  }
}


class MissingBlock extends AwkwardStatement {
  private BinStatementList statementList;

  MissingBlock(BinStatement statement, BinStatementList list, String message) {
    super(statement, message, "refact.audit.missing_block");
    
    statementList = list;
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  BinStatementList getStatementList() {
    return statementList;
  }

  public List getCorrectiveActions() {
    if (statementList != null) {
      return Collections.singletonList(AddMissingBlock.instance);
    }
    return Collections.EMPTY_LIST;
  }
}


class BlocklessForStatement extends MissingBlock {
  BlocklessForStatement(BinForStatement statement) {
    super(statement, statement.getStatementList(), "For statement has no block");
  }
}


class BlocklessIfStatement extends MissingBlock {
  BlocklessIfStatement(BinIfThenElseStatement statement) {
    super(statement, statement.getTrueList(), "If statement has no block");
  }
}


class BlocklessElseStatement extends MissingBlock {
  BlocklessElseStatement(BinIfThenElseStatement statement) {
    super(statement, statement.getFalseList(), "Else statement has no block");
  }
}


// TODO do we really need this???
//class BlocklessLabeledStatement extends MissingBlock {
//  BlocklessLabeledStatement(BinLabeledStatement statement) {
//    super(statement, statement.getLabelStatementList(),
//        Priority.NORMAL, "Labeled statement has no block");
//  }
//}

class BlocklessDoStatement extends MissingBlock {
  BlocklessDoStatement(BinWhileStatement statement) {
    super(statement, statement.getStatementList(), "Do statement has no block");
  }
}


class BlocklessWhileStatement extends MissingBlock {
  BlocklessWhileStatement(BinWhileStatement statement) {
    super(statement, statement.getStatementList(), "While statement has no block");
  }
}


class AddMissingBlock extends MultiTargetCorrectiveAction {
  static final AddMissingBlock instance = new AddMissingBlock();

  public String getKey() {
    return "refactorit.audit.action.block.add";
  }

  public String getName() {
    return "Surround with brackets";
  }

  public String getMultiTargetName() {
    return "Add missing brackets to control statements";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof MissingBlock)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinStatementList list = ((MissingBlock) violation).getStatementList();
    BinStatementListFormatter formatter = new BinStatementListFormatter(list);

    ASTImpl opening = formatter.getOpeningBrace();
    ASTImpl closing = formatter.getClosingBrace();

    manager.add(new StringInserter(compilationUnit,
        closing.getStartLine(), closing.getStartColumn() - 1, closing.getText()));

    manager.add(new StringInserter(compilationUnit,
        opening.getStartLine(), opening.getStartColumn() - 1, opening.getText()));

    return Collections.singleton(compilationUnit);
  }
}
