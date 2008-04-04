/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.service;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.parser.JavaTokenTypes;


/**
 *
 *
 * @author Arseni Grigorjev
 */
public class ServiceForinUsagesRule extends AuditRule {
  public static final String NAME = "service_forin";
  
  public void visit(BinForStatement stmt){
    if (stmt.getInitSourceConstruct() != null
        && stmt.getInitSourceConstruct().getRootAst().getParent().getType()
        == JavaTokenTypes.FOR_EACH_CLAUSE){
      addViolation(new ForinUsage(stmt));
    }
  }
}

class ForinUsage extends AwkwardSourceConstruct {
  ForinUsage(BinSourceConstruct construct) {
    super(construct, "[service] Forin usage", null);
  }
}
