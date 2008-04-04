/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.exceptions;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardStatement;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;


/**
 * TODO: Check for BinBreakStatement, BinContinueStatement
 *
 * @author Villu Ruusmann
 */
public class AbortedFinallyRule extends AuditRule {
  public static final String NAME = "aborted_finally";

  public void visit(BinReturnStatement statement) {
    if (hasEnclosingFinally(statement)) {
      addViolation(new ReturnInsideFinallyBlock(statement));
    }

    super.visit(statement);
  }

  public void visit(BinThrowStatement statement) {
    if (hasEnclosingFinally(statement)) {
      addViolation(new ThrowInsideFinallyBlock(statement));
    }

    super.visit(statement);
  }

  private static boolean hasEnclosingFinally(BinStatement statement) {
    BinItemVisitable temp = statement;

    // Ascend in classmodel hierarchy
    while (temp instanceof BinSourceConstruct) {
      if (temp instanceof BinTryStatement.Finally) {
        return true;
      }

      temp = temp.getParent();
    }

    return false;
  }
}


class ReturnInsideFinallyBlock extends AwkwardStatement {
  ReturnInsideFinallyBlock(BinReturnStatement statement) {
    super(statement, "Avoid return statements inside finally blocks", "refact.audit.aborted_finally");
  }
  
  public BinMember getSpecificOwnerMember() {
    return ((BinStatement) getSourceConstruct()).getParentMember();
  }
}


class ThrowInsideFinallyBlock extends AwkwardStatement {
  ThrowInsideFinallyBlock(BinThrowStatement statement) {
    super(statement, "Avoid throw statements inside finally block", "refact.audit.aborted_finally");
  }
  
  public BinMember getSpecificOwnerMember() {
    return ((BinStatement) getSourceConstruct()).getParentMember();
  }
}
