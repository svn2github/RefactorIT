/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinParameterReference;
import net.sf.refactorit.common.util.Assert;



/**
 * Method parameter.
 */
public class BinParameter extends BinLocalVariable {

  public static final BinParameter[] NO_PARAMS = new BinParameter[0];

  public BinParameter(String name, BinTypeRef typeRef, int modifiers) {
    super(name, typeRef, modifiers);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /** Overrides protected method */
  public final void setName(final String name) {
    super.setName(name);
  }

  public String getMemberType() {
    return memberType;
  }

  public BinMethod getMethod() {
    return (BinMethod) getParent();
  }

  public static final BinTypeRef[] parameterTypes(final BinParameter[] params) {
    final BinTypeRef[] types = new BinTypeRef[params.length];
    for (int i = 0; i < params.length; i++) {
      types[i] = params[i].getTypeRef();
    }

    return types;
  }

  private static final String memberType = "parameter";

  public final int getIndex() {
    if (!(getParentMember() instanceof BinMethod)) {
      if (Assert.enabled) {
        Assert.must(false, "Called on strange param: " + this);
      }
      return 0; // could be catch, but shouldn't be called on
    }

    final BinParameter[] params
        = ((BinMethod)this.getParentMember()).getParameters();
    for (int i = 0; i < params.length; i++) {
      if (params[i] == this) {
        return i;
      }
    }

    if (Assert.enabled) {
      Assert.must(false, "Param doesn't belong to its own parent: " + this
          +", parent: " + this.getParentMember());
    }
    return 0;
  }

  public boolean isVariableArity() {
    return false;
  }
  
  public BinItemReference createReference() {
    return new BinParameterReference(this);
  }
}
