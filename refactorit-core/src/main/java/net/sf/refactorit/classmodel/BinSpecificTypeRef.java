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
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.loader.LoadingASTUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.utils.RefactorItConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public class BinSpecificTypeRef
    implements BinTypeRef, DependencyParticipant, JavaTokenTypes {
  private BinTypeRef typeRef;

  /** e.g. List<String> - List is parameterResolver for type argument String
   * A<B>.C<D> - A is parameterResolver for B and C, but the purposes are different
   * (looks like an optimization (saving 4 bytes on a field) though */
  private BinTypeParameterManager parameterResolver;
  private byte parameterPosition;

  HashMap cachedTypeArguments = new HashMap();

  protected BinSpecificTypeRef(final BinTypeRef typeRef) {
    setTypeRef(typeRef);
  }

  public final boolean hasCoordinates() {
    return getCompilationUnit() != null && getNode() != null;
  }

  public ASTImpl getNode() {
    return null;
  }

  public int getNodeIndex() {
    return -1;
  }

  public final boolean isArity() {
    // FIXME: make some other way
    try {
      return getNode().getParent().getParent().getType() == VARIABLE_PARAMETER_DEF;
    } catch (NullPointerException e) { // called on typeRef which can't be analyzed
      return false;
    }
  }

  public BinTypeRef getCorrespondingTypeParameter() {
    if (this.parameterResolver != null) { // so, this one is a type argument
      BinTypeRef[] params = this.parameterResolver.getTypeParameters();
      if (this.parameterPosition < params.length) {
        return params[this.parameterPosition];
      }
//      BinTypeRef[] args = this.parameterResolver.getTypeArguments();
//      if (args != null) {
//        for (int i = 0, max = args.length; i < max; i++) {
//          if (args[i] == this) { // NOTE: we search an exact instance, so == is ok
//            return params[i];
//          }
//        }
//      }
    }

    return null;
  }

  public BinTypeRef findTypeArgumentByParameter(BinTypeRef typeParameter) {
    // JAVA5: make it breadth-first, not depth-first

    /**
     * FIXME Needed to avoid endless recursion, but there may be possibility
     * that after partial rebuilding of classmodel errors will appear
     */
    if (cachedTypeArguments.containsKey(typeParameter)) {
      return (BinTypeRef) cachedTypeArguments.get(typeParameter);
    } else {
      cachedTypeArguments.put(typeParameter, null);
    }

    BinTypeRef[] args = getTypeArguments();
    if (args != null) {
      for (int i = 0, max = args.length; i < max; i++) {
        BinTypeRef arg = args[i];
        if (arg.isSpecific()) {
          BinTypeRef curTypeParam
              = ((BinSpecificTypeRef) arg).getCorrespondingTypeParameter();
          if (typeParameter.equals(curTypeParam)) {
//            System.out.println("PARAM: " + typeParameter.getQualifiedName()
//                    + " ARG: " + arg.getQualifiedName());
//            System.out.println("    " + this.getQualifiedName());
            cachedTypeArguments.put(typeParameter, arg);
            return arg;
          }

          //It was a hack:)) Without it All RB Tests fell
          //But now it is no longer needed
          /*
          BinTypeRef superArg
              = findTypeArgumentInSuperTypes(getSupertypes(), typeParameter);
          if (superArg != null) {
            return superArg;
          }
          */

          BinTypeRef childArg = ((BinSpecificTypeRef) arg)
              .findTypeArgumentByParameter(typeParameter);
          if (childArg != null) {
//            System.out.println("PARAM: " + typeParameter.getQualifiedName() + " CHILDARG: "
//                    + childArg.getQualifiedName());
//            System.out.println("    " + this.getQualifiedName());
            cachedTypeArguments.put(typeParameter, childArg);
            return childArg;
          }
        }
      }
    }

    BinTypeRef superArg
    = findTypeArgumentInSuperTypes(getSupertypes(), typeParameter);

    if (superArg != null) {
//      System.out.println("PARAM: " + typeParameter.getQualifiedName() + " SUPERARG: "
//              + superArg.getQualifiedName());
//      System.out.println("    " + this.getQualifiedName());
      cachedTypeArguments.put(typeParameter, superArg);
      return superArg;
    }

//    System.out.println("PARAM: " + typeParameter.getQualifiedName() + " ARG: " + "null");
//    System.out.println("    " + this.getQualifiedName());
    return null;
  }

  private BinTypeRef findTypeArgumentInSuperTypes(BinTypeRef[] supers, BinTypeRef neededTypeParameter) {
    for (int i = 0, max = supers.length; i < max; i++) {
      BinTypeRef superType = supers[i];
      if (!superType.isSpecific()) {
        continue;
      }
      BinTypeRef arg = ((BinSpecificTypeRef) superType).findTypeArgumentByParameter(neededTypeParameter);
      if (arg != null) {
        return arg;
      }

//      BinTypeRef[] superArgs = superType.getTypeArguments();
//      if (superArgs == null) {
//        continue;
//      }
//      for (int k = 0, maxK = superArgs.length; k < maxK; k++) {
//        if (superArgs[k].equals(curTypeParameter)) {
//          BinTypeRef superTypeParam
//              = ((BinSpecificTypeRef) superArgs[k]).getCorrespondingTypeParameter();
//          if (findParamInSuperTypes(superType.getSupertypes(),
//              neededTypeParameter, superTypeParam)) {
//            return true;
//          }
//        }
//      }
    }
    return null;
  }

  public void setNode(ASTImpl node) {
  }

  public CompilationUnit getCompilationUnit() {
    return null;
  }

  /**
   * @return never returns array type reference
   */
  public final BinTypeRef getTypeRef() {
    if (this.typeRef.isReferenceType() && this.typeRef.getBinCIType().isAnonymous()) {
      return (BinTypeRef) this.typeRef.getSupertypes()[0];
    }

    return this.typeRef.getNonArrayType();
  }

  public final void setTypeRef(BinTypeRef typeRef) {
//    if (Assert.enabled && (typeRef == null || typeRef.isSpecific())) {
//      Assert.must(false, "Setting specific type ref with wrong base ref: " + typeRef);
//    }

    this.typeRef = typeRef.getTypeRefAsIs(); // unwrap specific ref - sometimes they still get through
  }

  /**
   * @return may return array type reference
   */
  public final BinTypeRef getTypeRefAsIs() {
    return this.typeRef;
  }

  public final boolean hasChildren() {
    return getChild() != null;
  }

  public final boolean isSpecific() {
    return true;
  }

  public boolean isWildCard() {
    return false;
  }

  public BinTypeRef addChild(final BinSpecificTypeRef child) {
    throw new UnsupportedOperationException(
        "addChild called on BinSpecificTypeRef with: " + child);
  }

  public BinSpecificTypeRef getChild() {
    return null;
  }

  public final String toString() {
    String name = ClassUtil.getShortClassName(this) + "(";

    if (getTypeRefAsIs() != null) {
      if (hasCoordinates()) {
        if (!getNode().getText().equals(getTypeRefAsIs().getName())) {
          name += getTypeRefAsIs().getName() + ", ";
        }
        name += getNode(); // + ", " + getCompilationUnit();
      } else {
        name += getTypeRefAsIs().getName();
      }
    } else {
      name += "null";
    }

    if (getChild() != null) {
      name += ", " + getChild();
    }

    BinTypeRef[] arguments = getTypeArguments();
    if (arguments != null) {
      for (int i = 0, max = arguments.length; i < max; i++) {
        name += ", " + arguments[i];
      }
    }

//    name += ")";
    name += ", " + Integer.toHexString(System.identityHashCode(this)) + ")";

    return name;
  }

  public final int hashCode() {
    return this.typeRef.hashCode();
  }

  // JAVA5: take into account type arguments also
  public final boolean equals(final Object other) {
    final BinTypeRef thisRef = this.typeRef; //this.getTypeRefAsIs();
    if (other == null) {
      return thisRef == null; // both nulls are equal
    }

    try {
      final BinTypeRef otherRef = ((BinTypeRef) other).getTypeRefAsIs();
      boolean equalz = thisRef == otherRef;
      if (!equalz && otherRef != other) {
        equalz = thisRef.equals(otherRef);
      }
//System.err.println("equals2: " + thisRef + " == " + otherRef + " -- " + equalz);
      return equalz;
    } catch (ClassCastException e) { // optimization, it shouldn't happen often
      return false; // given something strange
    }
  }

  public final void accept(BinTypeRefVisitor visitor) {
    if (hasCoordinates()
        || (this.typeRef != null && this.typeRef.isWildCard())) {
      visitor.visit(this);
    }
  }

  public void traverse(final BinTypeRefVisitor visitor) {
    if (this.typeRef != null && this.typeRef.isWildCard()) {
      this.typeRef.accept(visitor);
    }
  }

  public static BinTypeRef create(final BinTypeRef typeRef) {
    return new BinSpecificTypeRef(typeRef);
  }

  /**
   * @return never returns null!
   */
  public static BinTypeRef create(
      final CompilationUnit compilationUnit,
      final ASTImpl node,
      final BinTypeRef typeRef,
      final boolean analyze) {
    if (analyze) {
      return create(compilationUnit, node, typeRef);
    } else {
      if (typeRef.isPrimitiveType()) {
//        if (compilationUnit != null) {
//          return new BinSourceTypeRef(compilationUnit, node, typeRef);
//        } else {
          return typeRef;
//        }
      } else {
        return new BinSourceTypeRef(compilationUnit, node, typeRef);
      }
    }
  }

  /**
   * @return never returns null!
   */
  private static BinTypeRef create(
      final CompilationUnit compilationUnit,
      final ASTImpl node,
      final BinTypeRef typeRef) {
    BinTypeRef ciType = typeRef.getNonArrayType();

    if (ciType.isPrimitiveType()) {
//      if (compilationUnit != null) {
//        return new BinSourceTypeRef(compilationUnit, node, typeRef);
//      } else {
        return typeRef;
//      }
    }
//new rantlr.debug.misc.ASTFrame(typeRef.toString(), node).setVisible(true);

    ASTImpl curNode = node;
    while (curNode.getType() == ARRAY_DECLARATOR) {
      curNode = (ASTImpl) curNode.getFirstChild();
    }

    // JAVA5: this is a fast hack for "new <String>X()" expression
    // 1) TYPE_ARGS - TYPE_ARG - TYPE - String
    // 2) IDENT - TYPE_ARGS - TYPE_ARG - TYPE - String
    if (curNode.getType() == TYPE_ARGUMENTS) {
      curNode = (ASTImpl) curNode.getFirstChild();
      while (curNode != null && curNode.getType() == TYPE_ARGUMENT) {
        curNode = (ASTImpl) curNode.getNextSibling();
      }
      if (curNode == null) {
        new Exception("curNode got null for: " + node + " - " + typeRef)
            .printStackTrace();
        return typeRef;
      }
    }

    final int nodeType = curNode.getType();
    if (Assert.enabled
        && nodeType != IDENT
        && nodeType != DOT
        && nodeType != WILDCARD_TYPE
        ) {
      Assert.must(false,
          "addEntry called with non IDENT, non DOT node for "
          + typeRef.getQualifiedName(), curNode);
    }

    // if it's not a dot node there can not be a package
    if (nodeType == DOT) {
      final BinPackage binPackage = ciType.getPackage();
      final ASTImpl packageNode = extractPackageNode(binPackage, curNode);

      if (packageNode != null) {
        compilationUnit.addPackageUsageInfo(
            new PackageUsageInfo(packageNode, binPackage, true, compilationUnit));
      }
    }

    BinTypeRef rootUsage = null;
    BinSpecificTypeRef leastChildUsage = null;
    BinTypeRef realType = typeRef;
    final ASTImpl[] nodes = LoadingASTUtil.extractIdentNodesFromDot(curNode);
    for (int k = nodes.length; --k >= 0 && ciType != null; ) {
      ASTImpl typeNode = nodes[k];
      String nodeText = typeNode.getText();

      if (ciType.getName().equals(nodeText)) {
        // FIXME it crashes here on broken types
        try {
          ciType = ciType.getBinCIType().getOwner();
        } catch (NullPointerException e) {
          ciType = null;
        }
        leastChildUsage = createComplexTypeUsage(
            compilationUnit, typeNode, realType, leastChildUsage, ciType != null);
        if (rootUsage == null) {
          rootUsage = leastChildUsage;
        }
      } else {
        final List subs = ciType.getAllSubclasses();
        ciType = null;
        for (int i = 0, max = subs.size(); i < max; i++) {
          final BinTypeRef sub = (BinTypeRef) subs.get(i);
          if (sub.getName().equals(nodeText)) {
            // FIXME it may crash here on broken types
            try {
              ciType = sub.getBinCIType().getOwner();
            } catch (NullPointerException e) {
              ciType = null;
            }
            leastChildUsage = createComplexTypeUsage(
                compilationUnit, typeNode, sub, leastChildUsage, ciType != null);
            if (rootUsage == null) {
              rootUsage = leastChildUsage;
            }
            break;
          }
        }
      }
      realType = ciType;
    }

    if (rootUsage == null) { // this could happen on that old strange code "Owner$Inner"
      if (RefactorItConstants.debugInfo) {
        System.err.println("strange node: " + node + " - " + typeRef);
      }
      rootUsage
          = createComplexTypeUsage(compilationUnit, node, typeRef, null, false);
    }

    return rootUsage;
  }

  /**
   * node is the exact node of a type name.
   * For example if you have a line
   * com.package.Outer.Inner = null;
   *
   * Then you need to call this twice with both Outer and Inner and you also
   * need to call addPackageNameEntry with node denoting to the first .
   *
   * N.B! just calling create with analysis will autodetect these things
   */
  private static final BinSpecificTypeRef createComplexTypeUsage(
      final CompilationUnit compilationUnit, final ASTImpl node,
      final BinTypeRef type, final BinSpecificTypeRef parentUsage,
      final boolean mayAddMore) {
    /*    if(Assert.enabled) {
          Assert.must(node.getType() == IDENT,
     "addTypeNameEntry called with non IDENT for " + type.getQualifiedName(), node );
          Assert.must( !type.isArray(), "addTypeNameEntry should not be called with array type", node);
          Assert.must( !type.isPrimitiveType(), "addTypeNameEntry should not be called for primitive types", node);
        }*/

    BinSpecificTypeRef typeUsage;
    if (mayAddMore) {
      typeUsage = new BinTreeTypeRef(compilationUnit, node, type);
    } else {
      typeUsage = new BinSourceTypeRef(compilationUnit, node, type);
    }
    if (parentUsage != null) {
      parentUsage.addChild(typeUsage);
    }

    return typeUsage;
  }

  /**
   * returns null if no such package node here
   */
  private static ASTImpl extractPackageNode(final BinPackage searchable,
      ASTImpl node) {

    final String nameString = searchable.getQualifiedName();
    // default package is not renamable
    if (nameString == null || nameString.length() == 0) {
      return null;
    }

    String extractedName = LoadingASTUtil.extractPackageStringFromExpression(node);

    if (extractedName != null && extractedName.equals(nameString)) {
      return (ASTImpl) node.getFirstChild();
    } else {
      while (extractedName != null && extractedName.startsWith(nameString)) {
        node = (ASTImpl) node.getFirstChild();
        if (node == null) {
          break;
        }
        extractedName = LoadingASTUtil.extractPackageStringFromExpression(node);

        if (nameString.equals(extractedName)) {
          return (ASTImpl) node.getFirstChild();
        }
      }

    }

    return null;
  }

  public final boolean hasUnresolvedTypeParameters() {
    BinTypeRef typeRef = getNonArrayType();
    if (typeRef.getBinType().isTypeParameter()) { // JAVA5: what about wildcards?
      return true;
    }

    BinTypeRef[] args = getTypeArguments();
    if (args != null) {
      for (int i = 0, max = args.length; i < max; i++) {
        if (args[i].hasUnresolvedTypeParameters()) {
          return true;
        }
      }
    }

    return false;
  }

  public BinTypeRef[] getTypeArguments() {
    return NO_TYPEREFS;
  }

  public void setTypeArguments(final BinTypeRef[] typeArguments) {
  }

  public BinTypeRef getUpperBound(){
    return getSuperclass();
  }

  public BinTypeRef getLowerBound(){
    return null;
  }

  public final void setUpperBound(final BinTypeRef typeRef){
    setSuperclass(typeRef);
  }

  public void setLowerBound(final BinTypeRef typeRef){
  }

  /********************** DELEGATES **********************/

  public final void cleanUp() {
    this.typeRef.cleanUp();
  }

  public final List getAllSubclasses() {

    return typeRef.getAllSubclasses();
  }

  public final BinTypeRef getSuperclass() {

    return typeRef.getSuperclass();
  }

  public final boolean isString() {

    return typeRef.isString();
  }

  public final Set getDirectSubclasses() {

    return typeRef.getDirectSubclasses();
  }

  public final BinPackage getPackage() {

    return typeRef.getPackage();
  }

  public final BinTypeRef getNonArrayType() {
    try {
      return typeRef.getNonArrayType();
    } catch (NullPointerException e) {
      return null;
    }
  }

  public final boolean isResolved() {

    return typeRef.isResolved();
  }

  public BinTypeRef[] getSupertypes() {
    return typeRef.getSupertypes();
  }

  public final Project getProject() {

    return typeRef.getProject();
  }

  public final boolean isReferenceType() {

    return typeRef.isReferenceType();
  }

  public final BinCIType getBinCIType() {

    return typeRef.getBinCIType();
  }

  public final boolean isArray() {

    return typeRef.isArray();
  }

  public final boolean isPrimitiveType() {

    return typeRef.isPrimitiveType();
  }

  public final BinTypeRef[] getInterfaces() {

    return typeRef.getInterfaces();
  }

  public final void addDirectSubclass(final BinTypeRef subclass) {

    typeRef.addDirectSubclass(subclass);
  }

  public final boolean contains(Scope other) {

    return typeRef.contains(other);
  }

  public final Resolver getResolver() {
    return typeRef.getResolver();
  }

  public final void setSuperclass(final BinTypeRef superclass) {
    typeRef.setSuperclass(superclass);
  }

  public final void setBinType(final BinType aType) {
    typeRef.setBinType(aType);
  }

  public final void removeDirectSublasses(Collection subclasses) {
    typeRef.removeDirectSublasses(subclasses);
  }

  public final String getQualifiedName() {
    return typeRef.getQualifiedName();
  }

  public final String getSuperclassQualifiedName() {
    return typeRef.getSuperclassQualifiedName();
  }

  public final boolean isBuilt() {
    return typeRef.isBuilt();
  }

  public final void setInterfaces(final BinTypeRef[] interfaces) {
    typeRef.setInterfaces(interfaces);
  }

  public final void clearSubclasses() {
    typeRef.clearSubclasses();
  }

  public final String getName() {
    return typeRef.getName();
  }

  public final void initScope(HashMap variableMap, HashMap typeMap) {
    typeRef.initScope(variableMap, typeMap);
  }

  public final BinType getBinType() {
    return typeRef.getBinType();
  }

  public final Set getAllSupertypes() {
    return typeRef.getAllSupertypes();
  }

  public final boolean isDerivedFrom(final BinTypeRef superType) {
    return typeRef.isDerivedFrom(superType);
  }

  public final boolean isDerivedFrom(final String superTypeQualifiedName) {
    return typeRef.isDerivedFrom(superTypeQualifiedName);
  }

  public final String[] getInterfaceQualifiedNames() {

    return typeRef.getInterfaceQualifiedNames();
  }

  public final void setResolver(Resolver resolver) {

    typeRef.setResolver(resolver);
  }

  /******************** DependencyParticipant *********************/

  public final void cleanForReuse() {
    try {
      ((DependencyParticipant) this.typeRef).cleanForReuse();
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
    }
  }

  public final void addDependableWithoutCheck(BinTypeRef dependable) {
    try {
      ((DependencyParticipant) this.typeRef).addDependableWithoutCheck(
          dependable.getTypeRefAsIs());
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
    }
  }

  public final void addDependable(BinTypeRef dependable) {
    try {
      ((DependencyParticipant) this.typeRef).addDependable(
          dependable.getTypeRefAsIs());
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
    }
  }

  public final void removeDependables(Collection dependables) {
    try {
      // FIXME: shouldn't unwrap type usages here?
      ((DependencyParticipant) this.typeRef).removeDependables(dependables);
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
    }
  }

  public final Set getDependables() {
    try {
      return ((DependencyParticipant) this.typeRef).getDependables();
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
      return CollectionUtil.EMPTY_SET;
    }
  }

  public final Set getAllBinaryDependables() {
    try {
      return ((DependencyParticipant) this.typeRef).getAllBinaryDependables();
    } catch (ClassCastException e) {
      // ignore - primitive typeRef
      return CollectionUtil.EMPTY_SET;
    }
  }

  public BinItemReference createReference(){
    return new BinTypeRefReference(this);
  }

  public void setTypeParameterResolver(
      final BinTypeParameterManager parameterResolver,
      final int parameterPosition) {
    this.parameterResolver = parameterResolver;
    this.parameterPosition = (byte) parameterPosition;

    if (this.typeRef instanceof BinWildcardTypeRef) {
      ((BinWildcardTypeRef) this.typeRef)
          .setTypeParameterResolver(parameterResolver, parameterPosition);
    }
  }

  public BinTypeParameterManager getTypeParameterResolver() {
    return this.parameterResolver;
  }

  public BinTypeRef[] getTypeParameters() {
    BinCIType type = getBinCIType();
    if(type.isArray()) {
      return ((BinArrayType)type).getArrayType().getTypeParameters();
    } else {
      return type.getTypeParameters();
    }
//    return getBinCIType().getTypeParameters();
  }

  public BinTypeRef getTypeParameter(String name) {
    BinCIType type = getBinCIType();
    if(type.isArray()) {
      return ((BinArrayType)type).getArrayType().getTypeParameter(name);
    } else {
      return type.getTypeParameter(name);
    }
//    return getBinCIType().getTypeParameter(name);
  }

  public void setTypeParameters(BinTypeRef[] typeParameters) {
    BinCIType type = getBinCIType();
    if(type.isArray()) {
      ((BinArrayType)type).getArrayType().setTypeParameters(typeParameters);
    } else {
      type.setTypeParameters(typeParameters);
    }
  }
}

