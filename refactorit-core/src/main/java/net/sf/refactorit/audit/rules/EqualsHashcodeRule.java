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
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class EqualsHashcodeRule extends AuditRule {
  public static final String NAME = "equals_hashcode";

  /* Cache */
  private BinTypeRef[] eqParams;
  private BinTypeRef[] hcParams;

  public void visit(BinCIType type) {
    BinMethod equals = type.getDeclaredMethod("equals", getEqualsParams());
    BinMethod hashCode = type.getDeclaredMethod("hashCode", getHashcodeParams());

    // Catch unpaired #equals
    if (equals != null && hashCode == null) {
      addViolation(new HashcodeNotDeclared(equals));
    }

    // Catch unpaired #hashCode
    if (equals == null && hashCode != null) {
      addViolation(new EqualsNotDeclared(hashCode));
    }

    // Dispatch to super
    super.visit(type);
  }

  private BinTypeRef[] getEqualsParams() {
    if (this.eqParams == null) {
      this.eqParams = new BinTypeRef[] {getProject().objectRef};
    }

    return this.eqParams;
  }

  private BinTypeRef[] getHashcodeParams() {
    if (this.hcParams == null) {
      this.hcParams = BinTypeRef.NO_TYPEREFS;
    }

    return this.hcParams;
  }
}


class HashcodeNotDeclared extends AwkwardMember {
  HashcodeNotDeclared(BinMethod method) {
    super(method, "#equals(Object) is overriden but #hashCode isn't", "refact.audit.equals_hashcode");
  }
}


class EqualsNotDeclared extends AwkwardMember {
  EqualsNotDeclared(BinMethod method) {
    super(method, "#hashCode is overriden but #equals(Object) isn't", "refact.audit.equals_hashcode");
  }
}
