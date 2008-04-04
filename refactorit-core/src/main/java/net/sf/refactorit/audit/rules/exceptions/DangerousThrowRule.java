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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;


/**
 * @author Igor Malinin
 */
public class DangerousThrowRule extends AuditRule {
  public static final String NAME = "dangerous_throw";

  /* Cache */
  private BinTypeRef exceptionRef;
  private BinTypeRef errorRef;
  private BinTypeRef throwableRef;

  public void visit(BinThrowStatement ts) {
    BinTypeRef paramType = ts.getExpression().getReturnType();

    // Subclasses of java.lang.Exception is the right way, take no-op
    if (!paramType.isDerivedFrom(getExceptionRef())) {
      if (paramType.isDerivedFrom(getErrorRef())) {
        // Subclasses of java.lang.Error
        addViolation(new DangerousThrowError(ts, paramType));
      } else if (paramType.isDerivedFrom(getThrowableRef())) {
        // Subclasses of java.lang.Throwable
        addViolation(new DangerousThrowThrowable(ts, paramType));
      }
    }

    super.visit(ts);
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


class DangerousThrowThrowable extends AwkwardStatement {
  DangerousThrowThrowable(BinThrowStatement ts, BinTypeRef typeRef) {
    super(ts, "Avoid throwing subclasses of java.lang.Throwable - "
        + typeRef.getQualifiedName(), "refact.audit.dangerous_throw");
  }

  public BinMember getSpecificOwnerMember() {
    return ((BinStatement) getSourceConstruct()).getParentMember();
  }
}

class DangerousThrowError extends AwkwardStatement {
  DangerousThrowError(BinThrowStatement ts, BinTypeRef typeRef) {
    super(ts, "Avoid throwing subclasses of java.lang.Error - "
        + typeRef.getQualifiedName(), "refact.audit.dangerous_throw");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getParentMember();
  }
}
