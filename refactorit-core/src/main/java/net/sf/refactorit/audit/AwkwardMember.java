/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;

/**
 *
 * @author Arseni Grigorjev
 */
public class AwkwardMember extends SimpleViolation {
  
  public AwkwardMember(BinMember member, String message, String helpId) {
    super(getBinTypeRef(member), member.getNameAstOrNull(), message, helpId);
    setTargetItem(member);
  }
  
  private static BinTypeRef getBinTypeRef(final BinMember member) {
    if (member instanceof BinCIType) {
      return ((BinCIType) member).getTypeRef();
    }

    return member.getOwner();
  }
  
  public BinMember getSpecificOwnerMember() {
    return (BinMember) getTargetItem();
  }
}
