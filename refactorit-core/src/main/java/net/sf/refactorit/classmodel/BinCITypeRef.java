/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.loader.LoadingSourceBinCIType;
import net.sf.refactorit.loader.RebuildLogic;
import net.sf.refactorit.loader.Settings;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.utils.TypeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Contains or create new references only to class and interface types
 */
public class BinCITypeRef extends BinPrimitiveTypeRef
    implements DependencyParticipant {

  /** Interface change dependencies
   * N.B! currently not added/removed dependencies
   */
  private final HashSet dependables = new HashSet(16);

  // note - this myScope is a declaring scope of this Types instance
//  private Scope myScope;

//  private ScopeRules myScopeRules;

  private BinTypeRef[] supertypes = NO_TYPEREFS;
  private boolean clazz = false;

  private boolean expectInterface = false;

  private HashSet subclasses;

  private String qualifiedName;

  private ClassFilesLoader loader;

  private Resolver resolver;


  /** for classes being loaded
   * @param qualifiedName fully qualified name
   */
  public BinCITypeRef(final String qualifiedName, final ClassFilesLoader loader) {
    if (Settings.debugLevel > 50) {
      System.out.println("DD BinCITypeRef-String with: " + qualifiedName);
    }

    this.qualifiedName = qualifiedName;
    this.loader = loader;

//    String packageName = ClassData.extractPackageName(qualifiedName);
//    BinPackage aPackage = loader.getProject().createPackageForName(packageName);
//    aPackage.addType(this);

    if (RebuildLogic.debug) {
      System.err.println("new BinCITypeRef1: " + this);
    }
  }

  /**
   * Creates a BinCIType reference for known type in resolved form.
   */
  public BinCITypeRef(final BinCIType binType) {
    super(binType);

    if (RebuildLogic.debug) {
      //if (aType.getQualifiedName().endsWith("Entry")) {
      System.err.println("new BinCITypeRef2: " + this);
    }
  }

  public final String toString() {
    final BinType type = getBinTypeWithoutResolve();
    if (type == null) {
      return ClassUtil.getShortClassName(this) + " unresolved: \""
          + getQualifiedName() + "\""
          + ", " + getHashString();
    } else {
      return ClassUtil.getShortClassName(this) + ": \""
          + getQualifiedName() + "\""
          + ", "
          + type.getCompilationUnit() + ", "
          + type.getStartLine() + ":" + type.getStartColumn() + " - "
          + type.getEndLine() + ":" + type.getEndColumn()
          + ", " + getHashString();
    }
  }

  private final BinCIType createFakeType() {
    BinCIType result;
    final Project project = getProject();

    if (/*!clazz*/ expectInterface) {
      result = MissingBinInterface.createMissingBinInterface(qualifiedName,
          project);
    } else {
      result = MissingBinClass.createMissingBinClass(qualifiedName, project);
      setSuperclass(project.getObjectRef());
    }

    result.setTypeRef(this);
    setBinType(result);

    if (getInterfaces() == null) {
      setInterfaces(BinTypeRef.NO_TYPEREFS);
    }

    project.getProjectLoader().getErrorCollector().addUserFriendlyInfo(
        new UserFriendlyError("Missing class : " + qualifiedName, null));
    //new RuntimeException("INFO:trace for encountering missing class").printStackTrace();

    return result;
  }

  protected void resolve() {
    if (isResolved()) {
      return;
    }

    BinCIType aType = null;

    // NOTE: ExtractSuper creates fake ref with both name and loader == null
    //if(Assert.enabled) Assert.must(loader != null, "Type " + qualifiedName + " has aType = null && loader=null");
    if (loader != null) {
      final Project project = loader.getProject();
      try {
        project.classLoaderEntries++;
        aType = (BinCIType)this.loader.findTypeForQualifiedName(
            qualifiedName);
        project.classLoaderEntries--;
      } catch (RuntimeException e) {
        if (Assert.enabled) {
          e.printStackTrace();
        }
        throw e;
      } finally {
      }

      if (aType != null) {
        aType.setTypeRef(this);
        setBinType(aType);
    //          qualifiedName = null;
      } else {
        // Commented out -- caused CLI tests to fail when Assert.enabled.
        /*
        if (qualifiedName == null
            || (qualifiedName.indexOf("borland.") == -1
            && qualifiedName.indexOf("oracle.") == -1
            && qualifiedName.indexOf("ice.") == -1
            && qualifiedName.indexOf("java.util.regex.Pattern") == -1)) {
          String msg =
              "PLEASE REPORT TO support@refactorit.com TOGETHER WITH .refactorit/usage.log"
              + " - Couldn't resolve binary type: " + qualifiedName;
    //            new Exception(
    //                msg)
    //                .printStackTrace();
          // FIXME
          if (Assert.enabled) {
            System.err.println(msg);
          }
        }*/

        aType = createFakeType();
      }
    } else if (qualifiedName != null && getProject() != null) {
      AppRegistry.getLogger(getClass()).warn(
          "PLEASE REPORT TO support@refactorit.com",
          new Exception("Couldn't resolve source type: " + qualifiedName));
      aType = createFakeType();
    } else {
      AppRegistry.getLogger(getClass()).warn(
          "PLEASE REPORT TO support@refactorit.com",
          new Exception("Couldn't resolve source type: " + qualifiedName));
    }

    aType.setTypeRef(this);
    setBinType(aType);
  }

//  public void setBinType(final BinType aType) {
//    super.setBinType(aType);
//  }

  private final String getHashString() {
    final BinType binType = getBinTypeWithoutResolve();
    return Integer.toHexString(hashCode()) + " ("
        + (binType == null ? "???" : Integer.toHexString(binType.hashCode()))
        + ")";
  }

  public final void cleanUp() {
    final BinType binType = getBinTypeWithoutResolve();

    if (RebuildLogic.debug) {
      String name = this.getClass().getName();
      name = name.substring(name.lastIndexOf('.') + 1);
//    if (getQualifiedName().indexOf("classmodel") != - 1) {
      System.err.println(name + ", cleanUp: " + this.getQualifiedName()
          + " " + getHashString());
//    }
    }

    if (binType != null) {
      getQualifiedName(); // cache name if it is not yet cached
      binType.cleanForPrototype();
    }

    this.resolver = null;
//    this.myScope = null;
//    this.myScopeRules = null;
    this.supertypes = NO_TYPEREFS;
    this.clazz = false;
    clearSubclasses();

    // dependables cause severe memory leak
    this.dependables.clear();

    super.cleanUp();
  }

  public final BinCIType getBinCIType() {
    BinType binType = super.getBinType();

    if (binType != null && !binType.isPrimitiveType()) {
      return (BinCIType) binType;
    }

//    if (Assert.enabled) {
//      String name = this.getClass().getName();
//      name = name.substring(name.lastIndexOf('.') + 1);
//      Assert.must(aType != null, "Type is null for "
//          + name + ": " + qualifiedName + ", loader: " + loader);
//    }

    return null; // hmm
  }

  public final boolean isPrimitiveType() {
    return false;
//    // shortcut - primitive types are always resolved
//    if (!isResolved()) {
//      return false;
//    }
//
//    BinType type = getBinType();
//    boolean primitive = type == null || type.isPrimitiveType();
////    if (Assert.enabled) {
////      Assert.must(!(primitive ^ this.getClass().equals(BinPrimitiveTypeRef.class)),
////          "PrimitiveRef (" + this + ") holding complex type: " + type);
////    }
//    return primitive;
  }

  public final boolean isArray() {
    if (!isResolved()) {
      return getQualifiedName().charAt(0) == '[';
    }

    return getBinType().isArray();
  }

  public final void setResolver(final Resolver resolver) {
    if (Assert.enabled && resolver == null) {
      Assert.must(false, "Resolver is null !? for type: " + this);
    }
    this.resolver = resolver;
//if (super.getBinType() != null
//    && ((BinCIType) super.getBinType()).isFromCompilationUnit()
//        /*&& super.getBinType().getQualifiedName().endsWith("Entry")*/) {
//System.err.println("setResolver for: " + getQualifiedName()
//    + "@" + Integer.toHexString(hashCode()) + " - " + this.resolver);
//}
  }

  public final Resolver getResolver() {
    if (this.resolver == null && loader != null) {
      resolve();
    }

    if (Assert.enabled && this.resolver == null) {
      System.err.println("couldn't get resolver for type: "
          + getQualifiedName() + "@" + Integer.toHexString(hashCode()));
    }

    return this.resolver;
  }

  public final String getQualifiedName() {
    // TODO: if this.qualifiedName != null, return it right away...
    // must check that it always matches name returned by binType

    if (isResolved()) {
      String qName = getBinType().getQualifiedName();
      if (this.qualifiedName == null) {
        this.qualifiedName = qName;
      }
    } else {
      if (this.qualifiedName == null) {
        AppRegistry.getLogger(this.getClass()).debug(
            "No name for " + ClassUtil.getShortClassName(this));
        //new Exception("unresolved typeref without name").printStackTrace();
        this.qualifiedName = "_unknown_";
      }
    }

    return this.qualifiedName;
  }

  public final String getName() {
    if (isResolved()) {
      return getBinType().getName();
    } else {
      if (Assert.enabled && qualifiedName == null) {
        //new Exception("unresolved typeref without name").printStackTrace();
        return "<unknown>";
      }

      final int lastInnerI = qualifiedName.lastIndexOf('$');

      final String retVal;
      if (lastInnerI != -1) {
        retVal = qualifiedName.substring(lastInnerI + 1);
      } else {
        retVal = TypeUtil.extractShortname(qualifiedName);
      }

      return retVal;
    }
  }

  public final CompilationUnit getCompilationUnit() {
    BinType type = getBinTypeWithoutResolve();
    if (type != null) {
      return type.getCompilationUnit();
    } else {
      return null;
    }
  }

  public final Project getProject() {
    Project project = null;
    if (getBinTypeWithoutResolve() != null) {
      project = getBinTypeWithoutResolve().getProject();
    }
    if (project == null && this.loader != null) {
      project = this.loader.getProject();
    }
    if (project == null) {
      if (this.supertypes != null) {
        for (int i = 0; i < this.supertypes.length; i++) {
          project = this.supertypes[i].getProject();
          if (project != null) {
            break;
          }
        }
      }
    }
    if (project == null) {
      try {
        project = IDEController.getInstance().getActiveProject();
      } catch (Exception e) {
        // wasn't lucky
      }
    }

    return project;
  }

  /**
   * @param superclass a reference to super class of this class
   */
  public final void setSuperclass(final BinTypeRef superclass) {
    if (superclass == null) {
      if (clazz) {
        if (this.supertypes != null && this.supertypes.length > 1) {
          BinTypeRef[] supers = new BinTypeRef[this.supertypes.length - 1];
          System.arraycopy(this.supertypes, 1, supers, 0,
              this.supertypes.length - 1);
          this.supertypes = supers;
        } else {
          this.supertypes = NO_TYPEREFS;
        }
        this.clazz = false;
      }
      return;
    }

    if (this.supertypes == null || this.supertypes.length == 0) {
      this.supertypes = new BinTypeRef[1];
    } else {
      if (!clazz) {
        BinTypeRef[] supers = new BinTypeRef[this.supertypes.length + 1];
        System.arraycopy(this.supertypes, 0, supers, 1, this.supertypes.length);
        this.supertypes = supers;
      }
    }
    this.supertypes[0] = superclass;
    this.clazz = true;

    superclass.addDirectSubclass(this);
  }

  /**
   * An <code>interface</code> can have <code>null</code> superclass.
   * <p><code>Class</code> <em>always</em> has a superclass.
   *
   * @return a reference to direct superclass
   */
  public BinTypeRef getSuperclass() {
    resolve();
    if (clazz) {
      return this.supertypes[0];
    } else {
      return null;
    }
  }

  public final String getSuperclassQualifiedName() {
    String superName = null;

    if (isBuilt()) {
      BinTypeRef superr = getSuperclass(); // ensure wildcard fixes it's supertypes
      if (superr != null) {
        return superr.getQualifiedName();
      }
    } else {
      superName = ((LoadingSourceBinCIType) getBinCIType()).getSuperclassName();
    }

    return superName;
  }

  /**
   * @return list of BinTypeRef of direct supertypes (classes and interfaces)
   */
  public BinTypeRef[] getSupertypes() {
    resolve();
    return this.supertypes;
  }

  /**
   * @return list of BinTypeRef of indirect supertypes of this type
   */
  public final Set getAllSupertypes() {
    final BinTypeRef[] supertypes = getSupertypes();

    final HashSet result = new HashSet(10);
    for (int i = 0, max = supertypes.length; i < max; i++) {
      final BinTypeRef superType = supertypes[i];
      result.add(superType);
      result.addAll(superType.getAllSupertypes());
//      CollectionUtil.addNew(result, superType);
//      CollectionUtil.addAllNew(result, superType.getAllSupertypes());
    }
    return result;
  }

  /**
   * @return list of BinTypeRef of direct subclasses
   */
  public final Set getDirectSubclasses() {
    if (this.subclasses == null) {
      return CollectionUtil.EMPTY_SET;
    }
    // NOTE: it adds e.g. some array ref as subclasses to Object during
    // analysis of the methods, which causes ConcurrentModificationException
    // Don't know exactly, may be this cloning causes errors on searching for
    // usages of some methods, e.g. "clone()" of array type
    return (HashSet) this.subclasses.clone();
  }

  public final void removeDirectSublasses(Collection subclasses) {
    if (this.subclasses != null) {
      this.subclasses.removeAll(subclasses);
    }
  }

  /**
   * @return list of BinTypeRef of all subclasses of this type
   */
  public final List getAllSubclasses() {
    final List result = new ArrayList();

    final Iterator subclasses = getDirectSubclasses().iterator();
    while (subclasses.hasNext()) {
      final BinTypeRef subclass = (BinTypeRef) subclasses.next();
      result.add(subclass);
      result.addAll(subclass.getAllSubclasses());
    }

    return result;
  }

  /**
   * Used in rebuild
   */
  public final void clearSubclasses() {
    if (this.subclasses != null) {
      this.subclasses.clear();
    }
  }

  /**
   * It still has to be called inside because some types come to view only
   * in methodbodyloading
   */
  public final void addDirectSubclass(final BinTypeRef subclass) {
    if (this.subclasses == null) {
      int capacity;
      if ("Object".equals(getName())) {
        capacity = 1024;
      } else {
        capacity = 10;
      }
      this.subclasses = new HashSet(capacity);
      this.subclasses.add(subclass);
    } else if (!this.subclasses.contains(subclass)) {
      this.subclasses.add(subclass);
    }
  }

  public final String[] getInterfaceQualifiedNames() {
    String[] retVal;
    if (isBuilt()) {
      BinTypeRef[] inters = getInterfaces(); // ensure wildcard fixes it's supertypes
      if (Assert.enabled) {
        Assert.must(inters != null, "No interfaces in built type: " + this);
      }

      retVal = new String[inters.length];
      for (int i = 0; i < inters.length; i++) {
        retVal[i] = inters[i].getQualifiedName();
      }
    } else {
      retVal = ((LoadingSourceBinCIType) getBinCIType()).getInterfaceNames();
    }

    return retVal;
  }

  public final void setInterfaces(final BinTypeRef[] interfaces) {
    if (interfaces == null) {
      if (clazz && this.supertypes != null && this.supertypes.length > 0) {
        BinTypeRef superr = this.supertypes[0];
        this.supertypes = new BinTypeRef[] {superr};
      } else {
        this.supertypes = NO_TYPEREFS;
        this.clazz = false;
      }
      return;
    }

    if (Assert.enabled && interfaces == null) {
      Assert.must(false,
          "Setting interfaces to null in type: " + getQualifiedName());
    }

    if (this.supertypes == null || this.supertypes.length == 0
        || getBinCIType().isInterface()) {
      this.supertypes = interfaces;
      this.clazz = false; // ensure
    } else {
      BinTypeRef superr = this.supertypes[0];
      this.supertypes = new BinTypeRef[interfaces.length + 1];
      this.supertypes[0] = superr;
      System.arraycopy(interfaces, 0, this.supertypes, 1, interfaces.length);
    }

    for (int i = 0; i < interfaces.length; i++) {
      if (interfaces[i] != null) {
        // consider classes where implemented interfaces as sub classes to
        // interface class

        interfaces[i].addDirectSubclass(this);
      }
    }
  }

  /**
   * Returns the working copy of interfaces array - caller should take care of cloning
   * if needed
   */
  public BinTypeRef[] getInterfaces() {
    resolve();
    if (this.supertypes == null) {
      return NO_TYPEREFS;
    }
    if (!clazz) {
      return this.supertypes;
    }
    if (this.supertypes.length <= 1) {
      return NO_TYPEREFS;
    }
    BinTypeRef[] interfaces = new BinTypeRef[this.supertypes.length - 1];
    System.arraycopy(this.supertypes, 1, interfaces, 0, this.supertypes.length - 1);
    return interfaces;
  }

  private final boolean isDerivedFromClass(final BinTypeRef superType) {
    for (BinTypeRef testableSuperClass = getSuperclass();
        testableSuperClass != null;
        testableSuperClass = testableSuperClass.getSuperclass()) {
      if (testableSuperClass == superType
          || testableSuperClass.equals(superType)) {
        return true;
      }
    }

    return false;
  }

  private final boolean isDerivedFromInterface(final BinTypeRef superType) {
    if (this == superType || this.equals(superType)) {
      return true;
    }

    boolean retVal = false;
    final BinTypeRef[] superTypes = getSupertypes();
    for (int i = 0, max = superTypes.length; i < max; i++) {
      BinCITypeRef curSuperType = (BinCITypeRef) superTypes[i].getTypeRefAsIs();

      if (curSuperType.isDerivedFromInterface(superType)) {
        retVal = true;
        break;
      }
    }

    return retVal;
  }

  public final boolean isDerivedFrom(final BinTypeRef superType) {
    if (this == superType || this.equals(superType)) {
      return true;
    }

    if (superType == null || superType.isPrimitiveType()) {
      return false; // can't inherit from primitive anyway
    }

    if (superType == null) {
      new Exception("super type is null when checking for inheritance in: "
          + this.getQualifiedName()).printStackTrace();
    }

    if (Assert.enabled && superType.getBinType() == null) {
      new Exception("no BinType for ref: " + superType.getQualifiedName())
          .printStackTrace();
    }

    if (superType.getBinType().isInterface()) {
      if (isDerivedFromInterface(superType)) {
        return true;
      }
    } else {
      if (isDerivedFromClass(superType)) {
        return true;
      }
    }

//    if (isArray() && superType.isArray()) {
//      int thisDim =  ((BinArrayType) getBinType()).getDimensions();
//      int superDim = ((BinArrayType) superType.getBinType()).getDimensions();
//
//      BinTypeRef superNonArray = superType.getNonArrayType();
//
//      if (thisDim == superDim) {
//        return getNonArrayType().isDerivedFrom(superNonArray);
//      } else if (thisDim > superDim
//          && "java.lang.Object".equals(superNonArray.getQualifiedName())) {
//        return true;
//      }
//    }

    return false;
  }

  public final void initScope(final HashMap variableMap, final HashMap typeMap) {
    // CAUTION: due to design this is sometimes called many times - only first one should count
    // currently does not work anyway for types
//    if( this.myScopeRules != null) {
//      this.myScopeRules = new ScopeRules(this, variableMap, typeMap);
//    }
  }

//  public final ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

//  public final void setScope(final Scope aScope) {
//    myScope = aScope;
//  }
//
//  public final Scope getScope() {
//    return myScope;
//  }

  public final boolean contains(Scope other) {
    return false;
  }

  public final void cleanForReuse() {
    if (RebuildLogic.debug) {
      String name = this.getClass().getName();
      name = name.substring(name.lastIndexOf('.') + 1);
//    if (getQualifiedName().indexOf("classmodel") != - 1) {
      System.err.println(name + ", clean for reuse: " + this.getQualifiedName()
          + " " + Integer.toHexString(hashCode()));
//    }
    }

//    myScope = null;
//    myScopeRules = null;
    this.supertypes = NO_TYPEREFS;
    this.clazz = false;
    clearSubclasses();
    this.resolver = null;

    final BinType binType = getBinTypeWithoutResolve();
    if (binType != null) {
      if (this.qualifiedName == null) {
        try {
          this.qualifiedName = binType.getQualifiedName();
        } catch (Exception e) {
          System.err.println("Ignored exception:");
          e.printStackTrace();
        }
      }
      ((BinCIType) binType).invalidateCache();
    }

    // dependables could generate severe memory leak during full rebuild,
    // but here it seems that we must leave it to not to loose important links
    // dependables.clear();

    super.cleanUp();
  }

  public final void addDependableWithoutCheck(final BinTypeRef dependable) {
    if (dependable == null || dependable.equals(this)) {
      return;
    }

    dependables.add(dependable);
  }

  public final void addDependable(BinTypeRef dependable) {
    if (dependable == null || dependable.equals(this)) {
      return;
    }

    // FIXME: is it a bug??? it should cause all classes to resolve immediately
    while (dependable.getBinType().isLocal()) {
      dependable = dependable.getBinType().getOwner();
    }

    // FIXME: doesn't seem to be real - real is that customers have changing binary types :(
    if (!getBinCIType().isFromCompilationUnit()) {
      return;
    }

    dependables.add(dependable);
  }

  public final void removeDependables(Collection dependables) {
    this.dependables.removeAll(dependables);
  }

  /**
   * FIXME: currently when a dependable has changed so it no longer depends on this class
   * it won't be removed from this list
   */
  public final Set getDependables() {
    return dependables;
  }

  public final Set getAllBinaryDependables() {
    return getAllBinaryDependables(new HashSet(32));
  }

  private final Set getAllBinaryDependables(final HashSet visited) {
    final HashSet result = new HashSet(32);

    visited.add(this);

    Iterator deps = dependables.iterator();
    while (deps.hasNext()) {
      BinCITypeRef cur = (BinCITypeRef) ((BinTypeRef) deps.next()).getTypeRefAsIs();
      if (!visited.contains(cur) && cur.isResolved()
          && !cur.getBinCIType().isFromCompilationUnit()) {
        result.add(cur);
        result.addAll(cur.getAllBinaryDependables(visited));
      }
    }

    deps = getDirectSubclasses().iterator();
    while (deps.hasNext()) {
      BinCITypeRef cur = (BinCITypeRef) ((BinTypeRef) deps.next()).getTypeRefAsIs();
      if (!visited.contains(cur) && cur.isResolved()
          && !cur.getBinCIType().isFromCompilationUnit()) {
        result.add(cur);
        result.addAll(cur.getAllBinaryDependables(visited));
      }
    }

    return result;
  }

  public final void setClazz(final boolean clazz) {
//    this.clazz = clazz;
      expectInterface = !clazz;
  }
  
  public BinTypeRef[] getTypeParameters() {
    BinCIType type = getBinCIType();
    if(type.isArray()) {
      return ((BinArrayType)type).getArrayType().getTypeParameters();
    } else {
      return type.getTypeParameters();
    }
  }

  public BinTypeRef getTypeParameter(String name) {
    BinCIType type = getBinCIType();
    if(type.isArray()) {
      return ((BinArrayType)type).getArrayType().getTypeParameter(name);
    } else {
      return type.getTypeParameter(name);
    }
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
