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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.statements.BinStatementList;

import java.util.Collections;
import java.util.List;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class NestedBlockRule extends AuditRule {
  public static final String NAME = "nested_block";

  public void visit(BinStatementList block) {
    // The algorithm is rather simple
    if (block.getParent() instanceof BinStatementList) {
      addViolation(new NestedBlock(block));
    }

    super.visit(block);
  }
}

class NestedBlock extends AwkwardStatement {
  NestedBlock(BinStatementList block) {
    super(block, "Avoid loose blocks", "refact.audit.nested_block");
  }
  
  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getParentMember();
  }
  
  public List getCorrectiveActions() {
    if (((BinStatementList) getSourceConstruct()).getStatements().length == 0){
      return Collections.singletonList(RemoveBracketsAction.INSTANCE);
    } else {
      return Collections.EMPTY_LIST;
    }
  }
}
