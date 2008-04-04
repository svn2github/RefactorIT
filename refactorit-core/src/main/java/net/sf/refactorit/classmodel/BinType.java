/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinAnonymousTypeReference;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinLocalTypeReference;
import net.sf.refactorit.classmodel.references.BinTypeParameterReference;
import net.sf.refactorit.classmodel.references.BinTypeReference;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.BinItemVisitor;



/**
 * Defines bass class for type
 */
public abstract class BinType extends BinMember {
  private BinTypeRef typeRef;

  public BinType(String name, int modifiers, BinTypeRef owner) {
    super(name, modifiers, owner);
  }

  public final boolean isInnerType() {
    return getOwner() != null;
  }

  public boolean isArray() {
    return false;
  }

  /**
   * Checks whether this type is a class.
   *
   * @return <code>true</code> if and only if this type is a class;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isClass();

  /**
   * Checks whether this type is an interface.
   *
   * @return <code>true</code> if and only if this type is an interface;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isInterface();

  /**
   * Checks whether this type is an enum.
   *
   * @return <code>true</code> if and only if this type is an enum;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isEnum();

  /**
   * Checks whether this type is an annotation.
   *
   * @return <code>true</code> if and only if this type is an annotation;
   *         <code>false</code> otherwise.
   */
  public abstract boolean isAnnotation();

  public abstract boolean isPrimitiveType();

  public abstract boolean isAnonymous();

  public abstract boolean isLocal();

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void setTypeRef(BinTypeRef typeRef) {
    if (Assert.enabled && (typeRef == null || typeRef.isSpecific())) {
      Assert.must(false, "Setting specific type ref: " + typeRef);
    }

    this.typeRef = typeRef;
  }

  public final BinTypeRef getTypeRef() {
    return this.typeRef;
  }

  public abstract String getDefaultValue();

  public boolean isTypeParameter() {
    return false;
  }

  public boolean isWildcard() {
    return false;
  }

  public boolean isFromCompilationUnit() {
    return false;
  }

  public BinItemReference createReference(){
    if (getOwner() == null){
      return new BinTypeReference(this);
    } else if (isAnonymous() || getOwner().getBinCIType().isEnum()){
      // HACK: getOwner().getBinCIType().isEnum() -- why doesn`t anonymous class
      // initializes enum-constant field just honestly anwser isAnonymous()
      // == true? Look for similar hacks here:
      //    - BinAnonymousTypeReference,
      //    - SerialVersionUIDRule
      return new BinAnonymousTypeReference(this);
    } else if (this instanceof BinClass && ((BinClass) this).isLocal()){
      return new BinLocalTypeReference(this);
    } else if (isTypeParameter()) {
      return new BinTypeParameterReference(this);
    } else {
      return new BinTypeReference(this);
    }
  }
  
  public String getNameWithLocals(boolean isQualified) {
    if (isLocal() && !isPrimitiveType()) {
      return getOwner().getBinType().getNameWithLocals(isQualified) + '$'
          + ((BinCIType)this).getLocalPrefix() + getName();
    } else {
      return (isQualified)? getQualifiedName(): getName();
    }
  }
}
