/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Wildcard type may have only one upper("extends") or lower("super") bounds,
 * but inherits also all the supertypes of the corresponding type parameter
 * (according to Eclipse).
 *
 * @author Arseni Grigorjev
 * @author Anton Safonov
 */
public final class BinWildcardTypeRef extends BinCITypeRef {

  private BinTypeRef[] upperBounds;
  private BinTypeRef lowerBound;

  private BinTypeParameterManager typeParameterResolver;
  private byte typeParameterPosition = -1;

  private boolean supersFixed = false;

  public BinWildcardTypeRef(BinPackage apackage, BinTypeRef owner, Project project) {
    super(new BinClass(
        apackage,
        "?", BinMethod.NO_METHODS, BinField.NO_FIELDS,
        BinFieldDeclaration.NO_FIELDDECLARATIONS,
        BinConstructor.NO_CONSTRUCTORS, BinInitializer.NO_INITIALIZERS,
        BinTypeRef.NO_TYPEREFS, owner, BinModifier.PUBLIC,
        project) {
      public final String getMemberType() {
        return "wildcard type";
      }

      public final boolean isTypeParameter() {
        return false;
      }

      public final boolean isWildcard() {
        return true;
      }
    });
  }

  public BinTypeRef getLowerBound(){
    return this.lowerBound;
  }

  public void setLowerBound(BinTypeRef lowerBound){
    this.lowerBound = lowerBound;
  }

  public BinTypeRef getUpperBound(){
    if (this.upperBounds != null) {
      return this.upperBounds[0];
    }
    return null;
  }

  public BinTypeRef[] getAllUpperBounds(){
    return this.upperBounds;
  }

  public void setUpperBound(BinTypeRef upperBound){
    this.upperBounds = new BinTypeRef[] {upperBound};
  }

  public void setTypeParameterResolver(
      BinTypeParameterManager typeParameterResolver, int position) {
    this.typeParameterResolver = typeParameterResolver;
    this.typeParameterPosition = (byte) position;
  }

  protected void resolve() {
    super.resolve();

    if (this.supersFixed) {
      return;
    }
    this.supersFixed = true;

    // FIXME: refactor to create supertypes array right away

    if (this.typeParameterResolver != null && this.typeParameterPosition >= 0) {
      BinTypeRef[] upperBounds = this.typeParameterResolver
          .getTypeParameters()[this.typeParameterPosition].getSupertypes();
      List uppers;
      if (this.upperBounds == null) {
        uppers = Arrays.asList(upperBounds);
      } else {
        uppers = new ArrayList(2);
        uppers.addAll(Arrays.asList(this.upperBounds));
        CollectionUtil.addAllNew(uppers, upperBounds);
      }
      this.upperBounds = (BinTypeRef[]) uppers.toArray(
          new BinTypeRef[uppers.size()]);
    }

    if (this.lowerBound != null) {
      if (this.lowerBound.getBinType().isClass()) {
        setSuperclass(this.lowerBound);
      } else {
        setInterfaces(new BinTypeRef[] {this.lowerBound});
      }
    }
    if (this.upperBounds != null) {
      List interfaces = null;
      if (this.lowerBound != null && this.lowerBound.getBinType().isInterface()) {
        interfaces = new ArrayList(2);
        interfaces.add(this.lowerBound);
      }
      for (int i = 0, max = this.upperBounds.length; i < max; i++) {
        if (this.upperBounds[i].getBinType().isClass()) {
          if (getSuperclass() == null) {
            setSuperclass(this.upperBounds[i]);
          }
        } else {
          if (interfaces == null) {
            interfaces = new ArrayList(2);
          }
          CollectionUtil.addNew(interfaces, this.upperBounds[i]);
        }
      }
      if (interfaces != null) {
        setInterfaces((BinTypeRef[]) interfaces.toArray(new BinTypeRef[interfaces.size()]));
      }
    }

    // hmm, when?
    if (getSuperclass() == null && (getInterfaces() == null || getInterfaces().length == 0)) {
System.err.println("aaaaaaaaa");
      setSuperclass(getProject().getObjectRef());
    }
  }

  public boolean isWildCard() {
    return true;
  }

  public void traverse(final BinTypeRefVisitor visitor) {
    super.traverse(visitor);

    BinTypeRef[] supers = getSupertypes();
    for (int i = 0, max = supers.length; i < max; i++) {
      supers[i].accept(visitor);
    }
  }

  public final void accept(BinTypeRefVisitor visitor) {
    visitor.visit(this);
  }
}
