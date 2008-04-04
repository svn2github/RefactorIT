/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public interface BinTypeRef extends Scope, Referable, BinTypeParameterManager {

  BinTypeRef[] NO_TYPEREFS = new BinTypeRef[0];

  boolean equals(Object other);

  boolean isBuilt();

  boolean isResolved();

  boolean isPrimitiveType();

  boolean isReferenceType();

  boolean isSpecific();

  boolean isWildCard();

  boolean isArray();

  boolean isString();

  boolean isArity();


  void cleanUp();


  BinType getBinType();

  BinCIType getBinCIType();

  void setBinType(BinType aType);

  BinTypeRef getTypeRef();

  BinTypeRef getTypeRefAsIs();


  BinPackage getPackage();

  String getName();

  String getQualifiedName();


  ASTImpl getNode();
  int getNodeIndex();

  CompilationUnit getCompilationUnit();

  boolean hasCoordinates();


  /**
   * Loops through array type if needed and returns BinCIType for
   * which always isArrayType == false
   *
   * @return array type of this if it's array or this if it isn't.
   */
  BinTypeRef getNonArrayType();

  Project getProject();

  /**
   *
   * @param superType
   * @return true if is derived or equals supertype
   */
  boolean isDerivedFrom(BinTypeRef superType);

  boolean isDerivedFrom(String superTypeQualifiedName);


  /**
   * @return list of BinTypeRef of direct supertypes (classes and interfaces)
   */
  BinTypeRef[] getSupertypes();

  /**
   * @return list of BinTypeRef of indirect supertypes of this type
   */
  Set getAllSupertypes();

  /**
   * An <code>interface</code> can have <code>null</code> superclass.
   * <p><code>Class</code> <em>always</em> has a superclass.
   *
   * @return a reference to direct superclass
   */
  BinTypeRef getSuperclass();

  void setSuperclass(BinTypeRef superclass);

  String getSuperclassQualifiedName();

  /**
   * Returns the working copy of interfaces array - caller should take care of cloning
   * if needed
   */
  BinTypeRef[] getInterfaces();

  void setInterfaces(BinTypeRef[] interfaces);

  String[] getInterfaceQualifiedNames();

  /**
   * @return list of BinTypeRef of direct subclasses
   */
  Set getDirectSubclasses();

  /**
   * @return list of BinTypeRef of all subclasses of this type
   *
   * NB! If interface is in hierarchy it can return duplicate elements!
   */
  List getAllSubclasses();

  /**
   * It still has to be called inside because some types come to view only
   * in methodbodyloading
   */
  void addDirectSubclass(BinTypeRef subclass);

  void removeDirectSublasses(Collection subclasses);

  /**
   * Used in rebuild
   */
  void clearSubclasses();


  BinTypeRef[] getTypeArguments();

  void setTypeArguments(BinTypeRef[] typeParameters);

  BinTypeRef getUpperBound();

  void setUpperBound(BinTypeRef typeRef);

  BinTypeRef getLowerBound();

  void setLowerBound(BinTypeRef typeRef);

  boolean hasUnresolvedTypeParameters();

  Resolver getResolver();

  void setResolver(Resolver resolver);


  void accept(BinTypeRefVisitor visitor);

  void traverse(BinTypeRefVisitor visitor);


  final class QualifiedNameSorter implements Comparator {
    private static final QualifiedNameSorter instance = new QualifiedNameSorter();

    private QualifiedNameSorter() {
    }

    public static final QualifiedNameSorter getInstance() {
      return instance;
    }

    public final int compare(final Object o1, final Object o2) {
      BinTypeRef typeRef1 = (BinTypeRef) o1;
      BinTypeRef typeRef2 = (BinTypeRef) o2;
      return BinFormatter.formatQualified(typeRef1)
          .compareTo(BinFormatter.formatQualified(typeRef2));
    }
  }

  final class NameSorter implements Comparator {
    private static final NameSorter instance = new NameSorter();

    private NameSorter() {
    }

    public static final NameSorter getInstance() {
      return instance;
    }

    public final int compare(final Object o1, final Object o2) {
      return ((BinTypeRef) o1).getName()
          .compareTo(((BinTypeRef) o2).getName());
    }
  }

}
