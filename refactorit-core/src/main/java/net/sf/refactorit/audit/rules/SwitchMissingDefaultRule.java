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
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.parser.JavaTokenTypes;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class SwitchMissingDefaultRule extends AuditRule {
  public static final String NAME = "switch_default";

  public void visit(BinSwitchStatement statement) {
    BinSwitchStatement.CaseGroup[] groups = statement.getCaseGroupList();

    boolean defaultSeen = false;

    for (int i = 0; i < groups.length; i++) {
      BinSwitchStatement.Case[] cases = groups[i].getCaseList();

      for (int j = 0; j < cases.length; j++) {
        if (cases[j].getRootAst().getType() == JavaTokenTypes.LITERAL_default) {
          defaultSeen = true;
        }
      }
    }

    if (!defaultSeen) {
      addViolation(new MissingSwitchDefault(statement));
    }

    super.visit(statement);
  }
}


class MissingSwitchDefault extends AwkwardStatement {
  MissingSwitchDefault(BinSwitchStatement statement) {
    super(statement, "Switch statement does not specify the default case", "refact.audit.switch_default");
  }
  
  public BinMember getSpecificOwnerMember() {
    
    BinMember member = getSourceConstruct().getParentMember();
    while (member instanceof BinCIType && ((BinCIType) member).isAnonymous()){
      member = member.getParentMember();
    }
    return member;
  }
}
