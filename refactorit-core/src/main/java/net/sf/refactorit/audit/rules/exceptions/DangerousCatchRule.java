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
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.statements.BinTryStatement;


/**
 * @author Villu Ruusmann
 */
public class DangerousCatchRule extends AuditRule {
  public static final String NAME = "dangerous_catch";

  /* Cache */
  private BinTypeRef exceptionRef;
  private BinTypeRef errorRef;
  private BinTypeRef throwableRef;

  public void visit(BinTryStatement.CatchClause catchClause) {
    BinParameter parameter = catchClause.getParameter();

    BinTypeRef paramType = parameter.getTypeRef();

    // Subclasses of java.lang.Exception is the right way, take no-op
    if (!paramType.isDerivedFrom(getExceptionRef())) {
      if (paramType.isDerivedFrom(getErrorRef())) {
        // Subclasses of java.lang.Error
        addViolation(new DangerousCatchError(catchClause, paramType));
      } else if (paramType.isDerivedFrom(getThrowableRef())) {
        // Subclasses of java.lang.Throwable
        addViolation(new DangerousCatchThrowable(catchClause, paramType));
      }
    }

    super.visit(catchClause);
  }

  private BinTypeRef getExceptionRef() {
    if (this.exceptionRef == null) {
      this.exceptionRef = getBinTypeRef("java.lang.Exception");
    }

    return this.exceptionRef;
  }

  private BinTypeRef getErrorRef() {
    if (this.errorRef == null) {
      this.errorRef = getBinTypeRef("java.lang.Error");
    }

    return this.errorRef;
  }

  private BinTypeRef getThrowableRef() {
    if (this.throwableRef == null) {
      this.throwableRef = getBinTypeRef("java.lang.Throwable");
    }

    return this.throwableRef;
  }
}


class DangerousCatchThrowable extends AwkwardSourceConstruct {
  private BinMember ownerMember;

  DangerousCatchThrowable(BinTryStatement.CatchClause clause,
      BinTypeRef typeRef) {
    super(clause, "Avoid catching subclasses of java.lang.Throwable - "
        + typeRef.getQualifiedName(), "refact.audit.dangerous_catch");

    this.ownerMember = clause.getStatementList().getParentMember();
  }

  public BinMember getSpecificOwnerMember() {
    return this.ownerMember;
  }
}


class DangerousCatchError extends AwkwardSourceConstruct {
  DangerousCatchError(BinTryStatement.CatchClause clause, BinTypeRef typeRef) {
    super(clause, "Avoid catching subclasses of java.lang.Error - "
        + typeRef.getQualifiedName(), null);
  }
}
