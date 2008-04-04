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
import net.sf.refactorit.classmodel.references.BinTypeRefReference;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.LoadingSourceBinCIType;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.Resolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * A reference to {@link BinPrimitiveType}
 */
public class BinPrimitiveTypeRef implements BinTypeRef {
  private BinType binType;

  protected BinPrimitiveTypeRef() {
  }

  /**
   * Creates a BinType reference for known binType in resolved form.
   * This constructor is used to create references for built in
   * types like int and boolean
   */
  public BinPrimitiveTypeRef(final BinType binType) {
//    if (Assert.enabled && binType == null) {
//      Assert.must(false, "Creating typeRef without type");
//    }
    this.binType = binType;
    if (this.binType != null) {
      this.binType.setTypeRef(this);
    }
  }

  public void cleanUp() {
    if (Assert.enabled && this.getClass().equals(BinPrimitiveTypeRef.class)) {
      new Exception("TRYING TO CLEAN PRIMITIVE: " + this).printStackTrace();
    }
    this.binType = null;
  }

  /**
   * Creates a BinType reference in unresolved form from fully qualified name.
   * When the reference is resolved resolver should call setType.
   */
  public final BinType getBinType() {
    resolve();

    return this.binType;
  }

  protected final BinType getBinTypeWithoutResolve() {
    return this.binType;
  }

  public BinCIType getBinCIType() {
    return null;
  }

  public final BinTypeRef getTypeRef() {
    return this;
  }

  public final BinTypeRef getTypeRefAsIs() {
    return this;
  }

  public final boolean hasCoordinates() {
    return getCompilationUnit() != null && getNode() != null;
  }

  public final ASTImpl getNode() {
    return null;
  }

  public final int getNodeIndex() {
    return -1;
  }

  public CompilationUnit getCompilationUnit() {
    return null;
  }


  public final BinPackage getPackage() {
    if (isPrimitiveType()) {
      return getProject().getPackageForName(""); // FIXME: is there a faster way?
    }

    return getBinCIType().getPackage();
  }

  public final boolean isResolved() {
    return this.binType != null;
  }

  public final boolean isBuilt() {
    resolve();
    return isResolved() && !(this.binType instanceof LoadingSourceBinCIType);
  }


  /**
   * Sets the built BinType for this binType reference.
   * The reference is marked resolved
   */
  public void setBinType(final BinType binType) {
    if (Assert.enabled && binType == null) {
      Assert.must(false, "Called with null argument");
    }

    this.binType = binType;
  }

  /*
   * convenience method that forwards it to the binType referenced
   */
  public String getQualifiedName() {
    return getBinType().getQualifiedName();
  }

  /*
   * convinience method that forwards it to the binType referenced
   */
  public String getName() {
    try {
      return getBinType().getName();
    } catch (NullPointerException e) {
      // debug
      return "<unknown>";
    }
  }

//===========================================

  public boolean isPrimitiveType() {
    return true;
  }

  public final boolean isReferenceType() {
    return !isPrimitiveType();
  }

  public final boolean isSpecific() {
    return false;
  }

  public boolean isWildCard() {
    return false;
  }

  public boolean isArray() {
    return false;
  }

  public final boolean isArity() {
    return false;
  }

  public final boolean isString() {
    return "java.lang.String".equals(getQualifiedName());
  }

  public final boolean equals(final Object other) {
    BinTypeRef thisRef = this;
    if (thisRef == other) { // optimization
      return true;
    }

    if (other == null) {
      return false;
    }

    try {
      BinTypeRef otherRef = ((BinTypeRef) other).getTypeRefAsIs();
      final boolean equalz = thisRef == otherRef; // they are unique
//System.err.println("equals1: " + this + " == " + otherRef + " -- " + equalz);
      return equalz;
    } catch (ClassCastException e) { // optimization, it shouldn't happen often
      // given something strange
      return false;
    }
  }

  public final int hashCode() {
    return super.hashCode(); // default
  }

//===========================================

  /**
   * Gets string representation of this reference.
   *
   * @return string representation.
   */
  public String toString() {
    return getName();
  }

  /**
   * Loops through array binType if needed and returns BinCIType for
   * which always isArrayType == false
   *
   * @return array binType of this if it's array or this if it isn't.
   */
  public final BinTypeRef getNonArrayType() {
    BinTypeRef result = this;
    try {
      while (result.isArray()) {
        result = ((BinArrayType) result.getBinType()).getArrayType();
      }
    } catch (NullPointerException e) {
      // can't resolve BinType anymore, probably cleaned
      result = null;
    }

    return result;
  }

  public boolean isDerivedFrom(final BinTypeRef superType) {
    return this == superType || this.equals(superType);
  }

  public final boolean isDerivedFrom(final String superTypeQualifiedName) {
    try {
      return isDerivedFrom(getProject().getTypeRefForName(superTypeQualifiedName));
    } catch (NullPointerException e) {
      return false;
    }
  }

  public Project getProject() {
    Project project = null;
    if (binType != null) {
      project = binType.getProject();
    }

    return project;
  }

  /** override */
  protected void resolve() {
    // primitive not intended to be resolved
  }


  /**
   * @return list of BinTypeRef of direct supertypes (classes and interfaces)
   */
  public BinTypeRef[] getSupertypes() {
    return NO_TYPEREFS;
  }

  /**
   * @return list of BinTypeRef of indirect supertypes of this binType
   */
  public Set getAllSupertypes() {
    return CollectionUtil.EMPTY_SET;
  }

  /**
   * An <code>interface</code> can have <code>null</code> superclass.
   * <p><code>Class</code> <em>always</em> has a superclass.
   *
   * @return a reference to direct superclass
   */
  public BinTypeRef getSuperclass() {
    return null;
  }

  public void setSuperclass(final BinTypeRef superclass) {
  }

  public String getSuperclassQualifiedName() {
    return null;
  }

  /**
   * Returns the working copy of interfaces array - caller should take care of cloning
   * if needed
   */
  public BinTypeRef[] getInterfaces() {
    return BinTypeRef.NO_TYPEREFS;
  }

  public void setInterfaces(final BinTypeRef[] interfaces) {
  }

  public String[] getInterfaceQualifiedNames() {
    return StringUtil.NO_STRINGS;
  }

  /**
   * @return list of BinTypeRef of direct subclasses
   */
  public Set getDirectSubclasses() {
    return CollectionUtil.EMPTY_SET;
  }

  /**
   * @return list of BinTypeRef of all subclasses of this binType
   */
  public List getAllSubclasses() {
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  /**
   * It still has to be called inside because some types come to view only
   * in methodbodyloading
   */
  public void addDirectSubclass(final BinTypeRef subclass) {
  }

  public void removeDirectSublasses(Collection subclasses) {
  }

  public void clearSubclasses() {
  }


  public void initScope(final HashMap variableMap, final HashMap typeMap) {
  }

  public boolean contains(Scope other) {
    return false;
  }

  public Resolver getResolver() {
    return null;
  }

  public void setResolver(Resolver resolver) {
  }

  public final BinTypeRef[] getTypeArguments() {
    return NO_TYPEREFS;
  }

  public void setTypeArguments(BinTypeRef[] typeParameters) {
  }

  public BinTypeRef getUpperBound(){
    return null;
//    return getSuperclass();
  }

  public BinTypeRef getLowerBound(){
    return null;
  }

  public void setUpperBound(final BinTypeRef typeRef){
//    setSuperclass(typeRef);
  }

  public void setLowerBound(final BinTypeRef typeRef){
  }

  public final boolean hasUnresolvedTypeParameters() {
    return false;
  }

  public void accept(BinTypeRefVisitor visitor) {
//    if (!visitor.isVisitSpecificRefsOnly()) {
//      visitor.visit(this);
//    }
  }

  public void traverse(BinTypeRefVisitor visitor) {
  }

  public BinItemReference createReference(){
    return new BinTypeRefReference(this);
  }

  public BinTypeRef[] getTypeParameters() {
    // the primitives cannot have type parameters!
    // return getBinCIType().getTypeParameters();
    return BinTypeRef.NO_TYPEREFS;
  }

  public BinTypeRef getTypeParameter(String name) {
    // the primitives cannot have type parameters!
    // return getBinCIType().getTypeParameter(name);
    return null; 
  }

  public void setTypeParameters(BinTypeRef[] typeParameters) {
    // return getBinCIType().setTypeParameters(typeParameters);
    return;
  }

}
