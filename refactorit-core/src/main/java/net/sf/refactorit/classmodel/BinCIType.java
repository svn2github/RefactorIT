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
import net.sf.refactorit.common.util.AdaptiveMultiValueMap;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.ejb.RitEjbModule;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.loader.Settings;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.PositionsForNewItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Base class for classes and interfaces.
 * N.B! Scope is defined for BinCITypeRef
 */
public abstract class BinCIType extends BinType
    implements BinTypeRefManager, Scope, BinTypeParameterManager {

  // LoadingSourceBinCIType needs this constructor
  public BinCIType(BinPackage aPackage,
      String name,
      BinTypeRef b_owner,
      int b_modifiers,
      Project project) {
    super(name, b_modifiers, b_owner);
//    String className = this.getClass().getName();
//System.err.println("creating " + className.substring(className.lastIndexOf('.') + 1)
//        + ": \"" + name + "\"");
    this.project = project;
    this.aPackage = aPackage;
    setParent(this.aPackage);

    this.declaredMethods = BinMethod.NO_METHODS;
    this.declaredFields = BinField.NO_FIELDS;
    this.fieldDeclarations = BinFieldDeclaration.NO_FIELDDECLARATIONS;
    this.inners = BinTypeRef.NO_TYPEREFS;

    if (ProjectLoader.checkIntegrityAfterLoad) {
      ProjectLoader.registerCreatedItem(this);
    }

//    if (RebuildLogic.debug) {
//      System.err.println("new " + ClassUtil.getShortClassName(this)
//          + ": " + name + ", " + Integer.toHexString(this.hashCode()));
//    }
  }

  public BinCIType(BinPackage aPackage,
      String name,
      BinMethod[] declaredMethods,
      BinField[] declaredFields,
      BinFieldDeclaration[] fieldDeclarations,
      BinTypeRef[] inners,
      BinTypeRef declaringType,
      int modifiers,
      Project project) {
    super(name, modifiers, declaringType);

    if (Assert.enabled && aPackage == null) {
      Assert.must(false,
          "Package == null for type with name " + name);
    }
//System.err.println("Creating: " + name + " in " + declaringType);

    this.aPackage = aPackage;
    setParent(this.aPackage);

    /*    if (Assert.enabled && name != null) {
          Assert.must(name.lastIndexOf('.') < 0 && name.lastIndexOf('$') < 0,
              "Not a simple name: " + name);
        }*/

    this.declaredMethods = declaredMethods /*.clone()*/;
    if (this.declaredMethods == null) {
      this.declaredMethods = BinMethod.NO_METHODS;
    }
    this.declaredFields = declaredFields /*.clone()*/;
    if (this.declaredFields == null) {
      this.declaredFields = BinField.NO_FIELDS;
    }
    this.fieldDeclarations = fieldDeclarations;
    if (this.fieldDeclarations == null) {
      this.fieldDeclarations = BinFieldDeclaration.NO_FIELDDECLARATIONS;
    }
    this.inners = inners /*.clone()*/;
    if (this.inners == null) {
      this.inners = BinTypeRef.NO_TYPEREFS;
    }
    this.project = project;

    if (ProjectLoader.checkIntegrityAfterLoad) {
      ProjectLoader.registerCreatedItem(this);
    }

//    if (RebuildLogic.debug) {
//      System.err.println("new " + ClassUtil.getShortClassName(this)
//          + ": " + name + ", " + Integer.toHexString(this.hashCode()));
//    }
  }

  public void setOwners(BinTypeRef myRef) {
    setTypeRef(myRef);
    myRef.setBinType(this);

    if (getOwner() != null) {
      this.setParent(getOwner().getBinCIType());
    }

    for (int i = 0; i < this.declaredMethods.length; ++i) {
      this.declaredMethods[i].setOwner(myRef);
      this.declaredMethods[i].setParent(this);
    }

    if (this.fieldDeclarations != null && this.fieldDeclarations.length > 0) {
      for (int i = 0; i < this.fieldDeclarations.length; ++i) {
        this.fieldDeclarations[i].setParent(this);
      }

      for (int i = 0; i < this.declaredFields.length; ++i) {
        this.declaredFields[i].setOwner(myRef);
      }
    } else {
      // this one is for binary
      for (int i = 0; i < this.declaredFields.length; ++i) {
        this.declaredFields[i].setOwner(myRef);
        this.declaredFields[i].setParent(this);
      }
    }
  }

  /**
   * @return An id to name an anonymous inner within this type.
   */
  public final int getNextAnonymousNumber() {
    return++this.anonymousInnersCount;
  }

  public String getQualifiedName() {
    if (this.cachedQualifiedName == null) {
      this.cachedQualifiedName = makeQualifiedName();
    }

    return this.cachedQualifiedName;
  }

  private String makeQualifiedName() {
    String fqn;

    BinTypeRef ownerType = getOwner();
    if (ownerType != null) {
      String ownName = getName();
      fqn = ownerType.getQualifiedName() + '$' + ownName;
    } else {
      BinPackage pack = getPackage();
      if (pack != null) {
        fqn = pack.getQualifiedForShortname(getName());
      } else {
        fqn = getName();
        AppRegistry.getLogger(this.getClass()).warn(
            "No owner and package for type: " + fqn);
      }
    }

    return fqn;
  }

  /**
   * @return array of inners
   */
  public BinTypeRef[] getDeclaredTypes() {
    return inners;
  }

  /**
   * Could be slow!
   * @return all inners which can be accessed from the given <code>context</code>
   */
  public final List getAccessibleInners(BinCIType context) {
    Map innersMap = getAccessibleInnersMap();
    List innersList = new ArrayList(innersMap.values().size());

    Iterator it = innersMap.values().iterator();
    while (it.hasNext()) {
      final BinCIType inner = ((BinTypeRef) it.next()).getBinCIType();
      if (!inner.isLocal() && inner.isAccessible(context)) {
        CollectionUtil.addNew(innersList, inner);

        // This "if" prevents never-ending recursive calls that cause stack overflow
        if (!innersList.contains(inner)) {
          CollectionUtil.addAllNew(innersList, inner.getAccessibleInners(context));
        }
      }
    }

    return innersList;
  }

  /**
   * Fetches declared type for name allowing fetching also inner inners
   * @param name inner class name in format inner.innerInner
   * @return reference to inner type with such name
   */
  public final BinTypeRef getDeclaredType(String name) {
    if (name == null) {
      return null;
    }

    Map myInnerMap = getAccessibleInnersMap();
    BinTypeRef retVal = (BinTypeRef) myInnerMap.get(name);
    if (retVal != null) {
      return retVal;
    }

    final int pos = name.indexOf('.');
    if (pos == -1) {
      return null;
    }

    String innerName = name.substring(0, pos);
    BinTypeRef inner = (BinTypeRef) myInnerMap.get(innerName);
    String remainingName = name.substring(pos + 1);
    return inner.getBinCIType().getDeclaredType(remainingName);
  }

  /**
   * Follows the contract of {@link java.lang.Class#getDeclaredFields()}:
   * Returns an array of <code>BinField</code> objects reflecting all the
   * fields declared by the class or interface represented by this
   * <code>BinCIType</code> object. This includes public, protected, default
   * (package) access, and private fields, but excludes inherited fields.
   * The elements in the array returned are not sorted and are not in any
   * particular order.  This method returns an array of length 0 if the class
   * or interface declares no fields, or if this <code>BinCIType</code> object
   * represents an array class, or void.
   *
   * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
   *
   * @return    the array of <code>BinField</code> objects representing all
   * the declared fields of this class
   */
  public final BinField[] getDeclaredFields() {
    if (Assert.enabled) {
      return (BinField[]) declaredFields.clone();
    } else {
      return declaredFields;
    }
  }

  /**
   * Searches this type and it's supertypes for the fields which
   * can be accessed in type <code>context</code>.
   *
   * @param context type in which we want to use desired field.
   *
   * @return list of <code>BinField</code>s.
   */
  public final List getAccessibleFields(BinCIType context) {
    return getAccessibleFields(this, context);
  }

  private List getAccessibleFields(BinCIType invokedOn, BinCIType context) {
    List result = new ArrayList(this.declaredFields.length);

    for (int i = 0, max = this.declaredFields.length; i < max; i++) {
      if (this.declaredFields[i].isAccessible(invokedOn, context)) {
        result.add(this.declaredFields[i]);
      }
    }

    BinTypeRef[] supertypes = getTypeRef().getSupertypes();
    for (int i = 0, max = supertypes.length; i < max; i++) {
      result.addAll(supertypes[i].getBinCIType()
          .getAccessibleFields(invokedOn, context));
    }

    return result;
  }

  // FIXME: it's possible that this function is broken, need to check later.
  // One possible bug: getName() should be replaced with getNameWithOwners() ?
  // Any more bugs?
  private Map getAccessibleInnersMap() {
    HashMap accessibleInners = new HashMap(3);

    // add my own inners
    BinTypeRef[] myInners = getDeclaredTypes();
    for (int i = 0; i < myInners.length; i++) {
      BinTypeRef currentInner = myInners[i];

      if (Assert.enabled && accessibleInners.containsKey(currentInner.getName())) {
        Assert.must(false,
            "Already defined Type !? " + currentInner.getName() + ", in "
            + getQualifiedName());
      }
      accessibleInners.put(currentInner.getName(), currentInner);
    }

    // add inners for all supertypes

    BinTypeRef[] superTypes = getTypeRef().getSupertypes(); // get superclass & interfaces list
    for (int i = 0, max = superTypes.length; i < max; i++) {
      final BinTypeRef currentSuper = superTypes[i];

      final BinCIType currentSuperType = currentSuper.getBinCIType();
      ArrayList accessibleSuperInners =
          new ArrayList(currentSuperType.getAccessibleInnersMap().values());

      for (int innersCount = 0, innersMax = accessibleSuperInners.size();
          innersCount < innersMax; innersCount++) {

        BinTypeRef currentInner =
            (BinTypeRef) accessibleSuperInners.get(innersCount);
        if (!accessibleInners.containsKey(currentInner.getName())) {
          accessibleInners.put(currentInner.getName(), currentInner);
        }
      }
    }

    return accessibleInners;
  }

  /**
   * Gets all methods declared in this type. Same contract as
   * {@link java.lang.Class#getDeclaredMethods()}.
   * <p>
   * The rules can be summarized as follows:
   * <ul>
   *  <li>For concrete class or interface returns all methods declared in the
   *      source file</li>
   *  <li>For abstract class returns all methods declared in the source file and
   *      implemented interfaces and their superinterfaces excluding methods
   *      declared in superclass.</li>
   * </ul>
   * </p>
   *
   * @return methods. Never returns <code>null</code>.
   * @see java.lang.Class#getDeclaredMethods()
   */
  public BinMethod[] getDeclaredMethods() {
    if (Assert.enabled) {
      return (BinMethod[]) declaredMethods.clone();
    } else {
      return declaredMethods;
    }
  }

  public final void cleanMethodCaches() {
    accessibleMethodsMap = null;
  }

  /**
   * Methods accessible in <code>this</code> context.
   */
  protected final AdaptiveMultiValueMap getAccessibleInThisMethods() {
    if (accessibleMethodsMap == null) {
      // fill it right away to avoid recursive loops (e.g. on static imports check)
      accessibleMethodsMap = AdaptiveMultiValueMap.EMPTY_MAP;

      // my methods, also interface methods!!
      BinMethod[] myMethods = getDeclaredMethods();

      int potentialMethods = myMethods.length;

      List methodArrays = new ArrayList(3);

      BinTypeRef[] superTypes = getTypeRef().getSupertypes();
      if (superTypes != null) {
        for (int i = 0, max = superTypes.length; i < max; i++) {
          BinCIType curInterface = superTypes[i].getBinCIType();
          // there are also superclass'es interface methods!!
          AdaptiveMultiValueMap superMethods
              = curInterface.getAccessibleInThisMethods();
          int num = superMethods.getValuesAdded();
          if (num > 0) {
            methodArrays.add(superMethods.valuesIterator());
            potentialMethods += num;
          }
        }
      }

      // collect all static import methods
      List staticImportMethods = null;
      if (getCompilationUnit() != null) {
        staticImportMethods = getCompilationUnit().getStaticImportMethods(this);
        if (staticImportMethods != null) {
          potentialMethods += staticImportMethods.size();
        }
      }

      accessibleMethodsMap
          = new AdaptiveMultiValueMap((int) (potentialMethods / 2.5f));

      for (int i = 0; i < myMethods.length; i++) {
        accessibleMethodsMap.put(myMethods[i].getName(), myMethods[i]);
      }

      boolean hadAlready = false;
      for (int i = 0, max = methodArrays.size(); i < max; i++) {
        Iterator superMethods = (Iterator) methodArrays.get(i);
        while (superMethods.hasNext()) {
          BinMethod superMethod = (BinMethod) superMethods.next();

          if (!superMethod.isAccessible(this, this)) {
            continue;
          }

          hadAlready = false;
          String superMethodName = superMethod.getName();
          Iterator existings = accessibleMethodsMap.findIteratorFor(superMethodName);
          if (existings != null) {
            while (existings.hasNext()) {
              BinMethod existing = (BinMethod) existings.next();
              if (superMethod.sameSignature(existing)) {
                hadAlready = true;
                break;
              }
            }
          }

          if (!hadAlready) {
            accessibleMethodsMap.put(superMethodName, superMethod);
          }
        }
      }

    	if (staticImportMethods != null) {
    		for (int i = 0, max = staticImportMethods.size(); i < max; i++) {
    			BinMethod m = (BinMethod) staticImportMethods.get(i);
    			accessibleMethodsMap.put(m.getName(), m);
      	}
      }

      // Let's compact the memory usage
      accessibleMethodsMap.compact();
    }

    return accessibleMethodsMap;
  }

  /**
   * Methods from this type and it's super classes and interfaces which are
   * accessible from the given context.
   *
   * @return the array of <code>BinMethod</code> objects representing the
   * public methods of this class
   */
  public final BinMethod[] getAccessibleMethods(final BinCIType context) {
    AdaptiveMultiValueMap accessibleInThisMethods = getAccessibleInThisMethods();
    Iterator methodsIterator = accessibleInThisMethods.valuesIterator();
    final ArrayList accessibleMethodsList
        = new ArrayList((int) (accessibleInThisMethods.getValuesAdded() / 1.75f));

    while (methodsIterator.hasNext()) {
      final BinMethod curMethod = (BinMethod) methodsIterator.next();
      if (curMethod.isAccessible(this, context)) {
        accessibleMethodsList.add(curMethod);
      }
    }

    return (BinMethod[]) accessibleMethodsList.toArray(
        new BinMethod[accessibleMethodsList.size()]);
  }

  /**
   * Gets all methods with the specified name accessible in this class from the
   * given context.
   *
   * @param name name.
   *
   * @return methods. Never returns <code>null</code>.
   */
  public final BinMethod[] getAccessibleMethods(String name, BinCIType context) {
    final Iterator methodsIterator
        = getAccessibleInThisMethods().findIteratorFor(name);

    if (methodsIterator != null) {
      List accessibleMethodsList = new ArrayList(3);

      while (methodsIterator.hasNext()) {
        final BinMethod curMethod = (BinMethod) methodsIterator.next();

        if (curMethod.isAccessible(this, context)) {
          accessibleMethodsList.add(curMethod);
        }
      }

      return (BinMethod[]) accessibleMethodsList.toArray(
          new BinMethod[accessibleMethodsList.size()]);
    } else {
      return BinMethod.NO_METHODS;
    }
  }

  /**
   * Finds a method declared within this type with exactly the same parameters
   * as given parameters, thus no method invocation type conversion rules
   * applied.
   *
   * @param name method name
   * @param parameters method's parameters
   *
   * @return method in this type,
   * <code>null</code> when no appropriate method found
   */
  public final BinMethod getDeclaredMethod(String name, BinParameter[] parameters) {
    return getDeclaredMethod(getDeclaredMethods(), name, parameters);
  }

  public final BinMethod getDeclaredMethod(String name, BinTypeRef[] paramTypes) {
    return getDeclaredMethod(getDeclaredMethods(), name, paramTypes);
  }

  protected final BinMethod getDeclaredMethod(BinMethod[] methods,
      String name,
      BinParameter[] parameters) {
    return getDeclaredMethod(methods, name,
        BinParameter.parameterTypes(parameters));
  }

  protected final BinMethod getDeclaredMethod(final BinMethod[] methods,
      final String name,
      final BinTypeRef[] paramTypes) {

    for (int i = 0, max = methods.length; i < max; i++) {
      final BinParameter[] curParams = methods[i].getParameters();
      if (paramTypes.length == curParams.length
          && name.equals(methods[i].getName())) {

        int k = 0;
        for (; k < curParams.length; k++) {
          if (!(paramTypes[k] == curParams[k].getTypeRef()
              || paramTypes[k].equals(curParams[k].getTypeRef()))
              && paramTypes[k].getBinType()
              != curParams[k].getTypeRef().getBinType()) {
            break;
          }
        }

        if (k >= curParams.length) {
          return methods[i];
        }
      }
    }

    return null;
  }

  /**
   * Searches for overriding methods in subtypes.
   *
   * @param method which defines the signature we search for.
   *
   * @return methods. Never returns <code>null</code>.
   */
  public final List getSubMethods(BinMethod method) {
    final List results = new ArrayList(3);

    // static methods never have sub methods
    if (method.isStatic()) {
      return results;
    }

    final Iterator subtypes = getTypeRef().getDirectSubclasses().iterator();

    getSubMethodsFromSubtypes(method, results, subtypes);

    if(isInterface()) {
      final Iterator ejbRelatedTypes = RitEjbModule.getRelatedEJBImplTypeRefs(
          getTypeRef()).iterator();
      getSubMethodsFromSubtypes(method,results,ejbRelatedTypes);
    }

    return results;
  }

  private void getSubMethodsFromSubtypes(BinMethod method, final List results, final Iterator subtypes) {
    while (subtypes.hasNext()) {
      final BinCIType type = ((BinTypeRef) subtypes.next()).getBinCIType();

      final BinMethod[] methods = type.getDeclaredMethods();
      for (int k = 0, maxK = methods.length; k < maxK; k++) {
        if (method.sameSignature(methods[k]) && !methods[k].isPrivate()
            && method.isAccessible(method.getOwner().getBinCIType(), type)) {
          results.add(methods[k]);
        }
      }

      results.addAll(type.getSubMethods(method));
    }
  }

  public final boolean isPrimitiveType() {
    return false;
  }

  public final boolean isLocal() {
    return this.local;
  }

  public final void setLocal(boolean isLocal) {
    this.local = isLocal;
  }

  public final boolean isAnonymous() {
    return anonymous;
  }

  public final void setAnonymous(boolean isAnonymous) {
    this.anonymous = isAnonymous;
  }

  /**
   * Searches <code>this</code> type for a field with a given <code>name</code>.
   * First checks own declared fields, then fields of super types.
   *
   * @param name the field name
   * @param context type within which we want to get access
   *
   * @return the <code>BinField</code> object of this class specified by
   * <code>name</code>
   */
  public final BinField getAccessibleField(String name, BinCIType context) {
    // self fields have highest priority.
    BinField field = getDeclaredField(name);

    if (field != null && !field.isAccessible(context, context)) {
      field = null;
    }

    if (field == null) {
      field = getAccessibleFieldInSupers(name, context);
    }

    if (field == null && (context == this)) {
    	field = getStaticImportField(name, context);
    }

    return field;
  }

  public final BinField getDeclaredField(String name) {
    if (Settings.debugLevel > 50) {
      System.out.println("searching field " + name + " in "
          + getQualifiedName());
    }

    if (declaredFields == null) {
      return null;
    }

    if (declaredFieldsMap == null) {
      declaredFieldsMap = new HashMap(declaredFields.length);
      for (int i = 0; i < declaredFields.length; i++) {
        declaredFieldsMap.put(declaredFields[i].getName(), declaredFields[i]);
      }
    }
    // do i have this field...
    return (BinField) declaredFieldsMap.get(name);
  }

  private BinField getAccessibleFieldInSupers(String name, BinCIType context) {
    BinField field = null;

    final BinTypeRef[] supertypes = getTypeRef().getSupertypes();
    for (int i = 0, max = supertypes.length; i < max; i++) {
      field = supertypes[i].getBinCIType().getAccessibleField(name, context);

      if (field != null) {
        break;
      }
    }

    return field;
  }

  /**
   * Tries to fetch a reference to the statically imported field in the
   * compilation unit of the current type.
   */
  private BinField getStaticImportField(String name, BinCIType context) {
  	return context.getCompilationUnit().getStaticImportField(name, context);
  }


  //
  // Create/Remove members
  //

  /**
   * Does not work for source files because it does not set field declarations.
   */
  public final void setDeclaredFields(BinField[] fields) {
    if (isFromCompilationUnit()) {
      throw new UnsupportedOperationException(
          "Does not work for source files because it does not set field declarations.");
    }

    if (fields == null) {
      this.declaredFields = null;
    } else {
      this.declaredFields = (BinField[]) fields.clone();
    }

    this.declaredFieldsMap = null;
  }

  public final BinFieldDeclaration[] getFieldDeclarations() {
    return this.fieldDeclarations;
  }

  /**
   * Adds method to this type.
   * Does nothing if this method is already in the list.
   *
   * @param method method.
   * @return <code>true</code> when added method successfully, otherwise
   *         <code>false</code>
   */
  public final boolean addDeclaredMethod(BinMethod method) {
    List methodsList = CollectionUtil.toMutableList(this.declaredMethods);

    // FIXME: Checks for equality of methods are done via
    //        method.equals() which is not properly overriden in BinMethod.
    int index = methodsList.indexOf(method);

    if (index < 0) {
      methodsList.add(method);
      method.setOwner(getTypeRef());
      method.setParent(this);
      BinParentFinder.findParentsFor(method);
    } else {

      // Already exists
      return false;
    }

    this.declaredMethods
        = (BinMethod[]) methodsList.toArray(new BinMethod[methodsList.size()]);
    return true;
  }

  /**
   * Removes method from this type. Does nothing if method doesn't exist in this
   * type.
   *
   * @param method method.
   * @return <code>true</code> when removed method successfully, otherwise
   *         <code>false</code>
   */
  public final boolean removeDeclaredMethod(BinMethod method) {
    List methodsList = CollectionUtil.toMutableList(this.declaredMethods);

    // FIXME: Checks for equality of methods are done via
    //        method.equals() which is not properly overriden in BinMethod.
    int index = methodsList.indexOf(method);

    if (index >= 0) {
      methodsList.remove(index);
    } else {
      // Not found
      return false;
    }

    this.declaredMethods = (BinMethod[]) methodsList.toArray(BinMethod.NO_METHODS);
    return true;
  }

  public final BinPackage getPackage() {
    return aPackage;
  }

  public void defaultTraverse(BinItemVisitor visitor) {
    if (hasBuildErrors()) {
      return; // Silent ignore
    }

    BinExpressionList annotations = getAnnotations();
    if (annotations != null) {
      annotations.accept(visitor);
    }
    // NPE quickfix. See bug #1596
    // Should be fixed now! Not sure. It was traversing LoadingSourrceBinCIType [Anton]
    if (declaredFields == null ||
        declaredMethods == null ||
        inners == null
        ) {
      hasBuildErrors = true;
      System.err.println("Error in BinCIType.defaultTraverse(): "
          + getQualifiedName()
          + ", " + "Please send this message to support@refactorit.com");
      return; // Silent ignore
    }

    BinTypeRef[] typeParams = getTypeParameters();
    if (typeParams != null) {
      for (int i = 0; i < typeParams.length; i++) {
        BinCIType typeParam = typeParams[i].getBinCIType();
        if (typeParam != null) {
          typeParam.accept(visitor);
        }
      }
    }

    acceptForFields(visitor);

    for (int i = 0; i < declaredMethods.length; ++i) {
      declaredMethods[i].accept(visitor);
    }

    for (int i = 0; i < inners.length; ++i) {
      BinCIType innerType = inners[i].getBinCIType();
      if (innerType != null) {
        innerType.accept(visitor);
      }
    }
  }

  public BinExpressionList getAnnotations() {
    return this.annotationsList;
  }

  private void acceptForFields(BinItemVisitor visitor) {
    if (fieldDeclarations != null && fieldDeclarations.length > 0) {
      for (int i = 0; i < fieldDeclarations.length; i++) {
        fieldDeclarations[i].accept(visitor);
      }
    } else {
      for (int i = 0; i < declaredFields.length; i++) {
        declaredFields[i].accept(visitor);
      }
    }
  }

  public void cleanForPrototype() {
    //System.err.println("BINCITYPE - clean for prototype: " + this.getQualifiedName());

    super.cleanForPrototype();
    BinField[] fields = this.declaredFields; // getDeclaredFields();
    if (fields != null) {
      for (int i = 0; i < fields.length; ++i) {
        fields[i].cleanForPrototype();
      }
    }

    BinMethod[] methods = this.declaredMethods; //getDeclaredMethods();
    if (methods != null) {
      for (int i = 0; i < methods.length; ++i) {
        methods[i].cleanForPrototype();
      }
    }

    invalidateCache();
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final void setCompilationUnit(final CompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;
  }

  public final Project getProject() {
    return this.project;
  }

  public final void setProject(Project project) {
    this.project = project;
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void invalidateCache() {
    this.cachedQualifiedName = null;
    if (this.declaredFieldsMap != null) {
      this.declaredFieldsMap.clear();
      this.declaredFieldsMap = null;
    }
    if (this.accessibleMethodsMap != null) {
      this.accessibleMethodsMap.clear();
      this.accessibleMethodsMap = null;
    }
  }

  public final boolean isFromCompilationUnit() {
    return getCompilationUnit() != null;
  }

  public final void setSelfUsageInfo(BinTypeRef selfUsageInfo) {
//    if (Assert.enabled && selfUsageInfo.getTypeRefAsIs() != super.getTypeRef()) {
//      new Exception("Error - setting: " + selfUsageInfo + " OVER " + super.getTypeRef()).printStackTrace();
//    }
//    setTypeRef(selfUsageInfo);
      this.selfUsageInfo = selfUsageInfo;
  }

  public final BinTypeRef getSelfUsageInfo() {
    return this.selfUsageInfo;
  }

  public final List getSpecificSuperTypeRefs() {
    BinTypeRef[] supers = getTypeRef().getSupertypes();
    ArrayList result = new ArrayList(supers.length);
    for (int i = 0, max = supers.length; i < max; i++) {
      BinTypeRef superType = supers[i];
      if (superType.isSpecific()) {
        result.add(superType);
      }
    }
    return result;
  }

  public final void accept(BinTypeRefVisitor visitor) {
    BinTypeRef[] supers = getTypeRef().getSupertypes();
    for (int i = 0, max = supers.length; i < max; i++) {
      supers[i].accept(visitor);
    }

    if (visitor.isCheckTypeSelfDeclaration() && this.selfUsageInfo != null) {
      this.selfUsageInfo.accept(visitor);
    }
  }

  public final BinTypeRef[] getTypeParameters() {
    if (this.typeParameters == null) {
      this.typeParameters = BinTypeRef.NO_TYPEREFS;
    }
    return this.typeParameters;
  }

  public final BinTypeRef getTypeParameter(final String name) {
    // JAVA5: optimize -- looking through array for every name is too slow
    for (int i = 0,
        max = this.typeParameters == null ? 0 : this.typeParameters.length;
        i < max; i++) {
      if (name.equals(this.typeParameters[i].getName())) {
        return this.typeParameters[i];
      }
    }

    return null;
  }

  public final void setTypeParameters(final BinTypeRef[] typeParameters) {
    this.typeParameters = typeParameters;
    for (int i = 0,
        max = this.typeParameters == null ? 0 : this.typeParameters.length;
        i < max; i++) {
      this.typeParameters[i].getBinType().setParent(this);
    }
  }

  /**
   * FIXME: is this correct?
   * @return AST node declaring type name
   */
  public final ASTImpl getNameAstOrNull() {
    // NOTE: it is possible to get it from getOffsetNode, but there is
    // difference between normal and anonymous types

    if (isClass() && isAnonymous()) {
      return getOffsetNode(); // anonymous types doesn't have normal name
    }

    try {
      return this.selfUsageInfo.getNode();
    } catch (NullPointerException e) {
      return null; // type without actual node in source
    }
  }

  /**
   * Gets top-level type (JLS 7.6) in which this type is enclosed.
   * Returns self type for top-level type.
   *
   * @return top-level enclosing type. Never returns <code>null</code> as types
   *         are either top-level themselves or enclosed into a top-level type.
   */
  public final BinCIType getTopLevelEnclosingType() {
    if (isInnerType()) {
      return super.getTopLevelEnclosingType();
    } else {
      return this;
    }
  }

  /**
   * Gets AST node corresponding to the body of this type.
   * Body is part of declaration enclosed in <code>{</code> and
   * <code>}</code> brackets containing declaration of this type's members.
   *
   * @return AST node or <code>null</code> if node is not known.
   */
  public final ASTImpl getBodyAST() {
    ASTImpl node = (ASTImpl) getOffsetNode().getFirstChild();
    while ((node != null) && (node.getType() != JavaTokenTypes.OBJBLOCK)) {
      node = (ASTImpl) node.getNextSibling();
    }

    return node;
  }

  /**
   * Checks whether type is accessible (JLS 6.6) from within the other specified
   * type.
   *
   * @param context type from which accessed is checked.
   *
   * @return <code>true</code> if and only if the type is accessible from within
   *         the specified type, <code>false</code> otherwise.
   */
  public final boolean isAccessible(BinCIType context) {

    // JLS 6.6.1:
    // If a class or interface type is declared public, then it may be accessed
    // by any code, provided that the compilation unit (§7.3) in which it is
    // declared is observable. If a top level class or interface type is not
    // declared public, then it may be accessed only from within the package in
    // which it is declared.
    // Speed optimization: type can always access itself.
    if ((this == context)
        /*|| (this.getQualifiedName().equals(context.getQualifiedName()))*/) {

      return true;
    }

    // local type cannot be accessed from anything except itself and other
    // local types of the same scope, which are declared lower
    // (anonymous class is not local)
    if (this.isLocal() && !this.isAnonymous()) {
      BinTypeRef owner = context.getOwner();
      // this - local class, context - class declared in local class
      while (owner != null) {
        if (owner == this.getTypeRef() || owner.equals(this.getTypeRef())) {
          return true;
        }
        owner = owner.getBinCIType().getOwner();
      }

      // this - local class, context - local class in the same scope
      if (this.getOwner() == context.getOwner()
          || this.getOwner().equals(context.getOwner())) {
        return true;
      }

      return false;
    }

    // If a top level class or interface type is not declared public, then it
    // may be accessed only from within the package in which it is declared.
    if (this.isInnerType()) {
      // Not a top level type.
      // Is accessible only if owner is accessible.
      if (!this.getOwner().getBinCIType().isAccessible(context)) {
        return false; // Owner not accessible
      }

      // Is accessible if it is accessible as member of owner type.
      if (!this.isAccessible(this.getOwner().getBinCIType(),
          context)) {
        return false; // Inner class not accessible
      }

      return true;

    } else if (this.isAnonymous()) {
      throw new RuntimeException("isAccessible not yet implemented for"
          + " anonymous classes");
    }

    // If a class or interface type is declared public, then it may be accessed
    // by any code, provided that the compilation unit (§7.3) in which it is
    // declared is observable.
    if (this.isPublic()) {
      return true;
    }

    // Type is not public and it is top, so it can be only package private.

    // Top level type with accessible owner
    if (this.getPackage().isIdentical(context.getPackage())) {
      return true;
    }

    return false; // Not accessible
  }

  public final boolean hasAccessToAllPublicClassesIn(BinPackage aPackage) {
    return getPackage().isIdentical(aPackage)
        || getCompilationUnit().importsPackage(aPackage);
  }

  /**
   * @return true if type and it's container source file are named the same
   */
  public final boolean isNameMatchesSourceName() {
    String sourceName = getCompilationUnit().getName();
    int ind = sourceName.indexOf('.');
    if (ind > 0) {
      sourceName = sourceName.substring(0, ind);
    }
    return getName().equals(sourceName);
  }

  public final boolean isOwnedBy(BinCIType potentialOwner) {
    if (potentialOwner.getTypeRef().equals(getOwner())) {
      return true;
    } else if (getOwner() == null) {
      return false;
    } else {
      // FIXME: hmm, who owns who? here it reverses the order
      return getOwner().getBinCIType().isOwnedBy(potentialOwner);
    }
  }

  public final boolean hasBuildErrors() {
    return this.hasBuildErrors;
  }

  public final void setHasBuildErrors(boolean b) {
    this.hasBuildErrors = b;
  }

  /**
   * NB! Doesn't include cloned members from inherited interfaces.
   */

  public final BinMember hasMemberWithSignature(BinMember memberToCheck) {
    if (memberToCheck instanceof BinMethod) {
      BinMethod[] methods = getDeclaredMethods();
      for (int i = 0; i < methods.length; i++) {
        if (((BinMethod) memberToCheck).sameSignature(methods[i])
            && !methods[i].isSynthetic()) {
          return methods[i];
        }
      }
    } else if (memberToCheck instanceof BinField) {
      BinField[] fields = getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        if (((BinField) memberToCheck).sameSignature(fields[i])) {
          return fields[i];
        }
      }
    } else if (memberToCheck instanceof BinCIType) {
      BinTypeRef[] types = getDeclaredTypes();
      for (int i = 0; i < types.length; i++) {
        if (memberToCheck.getName().equals(types[i].getName())) {
          return types[i].getBinCIType();
        }
      }
    } else {
      throw new RuntimeException(
          "memberToCheck is neither BinMethod nor BinField nor BinCIType:" +
          memberToCheck.getClass().getName());
    }

    return null;
  }

  public final SourceCoordinate findNewFieldPosition() {
    return PositionsForNewItems.findNewFieldPosition(this);
  }

  public final SourceCoordinate findNewMethodPosition() {
    return PositionsForNewItems.findNewMethodPosition(this);
  }

  public final SourceCoordinate findNewConstructorPosition() {
    return PositionsForNewItems.findNewConstructorPosition(this);
  }

  public final void setDefinitelyInWrongFolder(boolean b) {
    definitelyInWrongFolder = b;
  }

  // FIXME extract into some name conflict analyzer (should handle also methods
  // and locals, see LocalDuplicatesFinder)
  public final boolean canCreateField(final String name) {
    if (hasField(name)) {
      return false;
    }

    Iterator supertypes = getTypeRef().getAllSupertypes().iterator();
    while (supertypes.hasNext()) {
      BinCIType supertype = ((BinTypeRef) supertypes.next()).getBinCIType();
      if (supertype.hasField(name) &&
          supertype.getDeclaredField(name).isInvokedVia(getTypeRef())) {
        return false;
      }
    }

    if (invokesVariableOfOuterType(this, name)) {
      return false;
    }

    List subclasses = getTypeRef().getAllSubclasses();
    for (int i = 0; i < subclasses.size(); i++) {
      BinCIType sub = ((BinTypeRef) subclasses.get(i)).getBinCIType();
      if (invokesVariableOfOuterType(sub, name)) {
        return false;
      }
    }

    return true;
  }

  private boolean invokesVariableOfOuterType(BinCIType type,
      String requiredVariableName) {
    boolean found = false;
    List vars = BinItemVisitableUtil.getVariablesInvokedIn(type);
    for (int i = 0; i < vars.size(); i++) {
      BinVariable var = (BinVariable) vars.get(i);
      if (requiredVariableName.equals(var.getName())
          && var.isOwnedByOuterTypeOf(type)) {
        found = true;
      }
    }

    return found;
  }

  public final boolean hasField(final String name) {
    return getDeclaredField(name) != null;
  }

  /** If false, the type may or may not be in the wrong folder. */
  public final boolean isDefinitelyInWrongFolder() {
    return definitelyInWrongFolder;
  }

  public final BinItemFormatter getFormatter() {
    return new BinTypeFormatter(this);
  }

  /**
   * @return	true if this member is deprecated
   */
  public final boolean isDeprecated() {
    return deprecated;
  }

  public final void setDeprecated(final boolean setTo) {
    deprecated = setTo;
  }

//  public void markRebuilding() {
//    super.markRebuilding();
//    getTypeRef().cleanSubClassMethodCaches();
//    cleanMethodCaches();
//  }

  public final String getDefaultValue() {
    return "null";
  }

  public final void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public final boolean contains(Scope other) {
    if (getCompilationUnit() != null && other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else if (other instanceof BinMember) { // for binary types having no coordinates
      BinMember member = (BinMember) other;
      while (member != null) {
        if (member == this) {
          return true;
        }

        if (member.getOwner() == null) {
          return false;
        }

        member = member.getOwner().getBinCIType();
      }
    }

    return false;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": \""
        + getNameWithAllOwners() + "\", "
        + getCompilationUnit() + ", "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn()
        /* + ", " + Integer.toHexString(hashCode())*/
        // hashCode() makes tests fail differently each time (MinimizeAccessRightsTest, for example)
        ;
  }

  public void setAnnotations(BinExpressionList list) {
    this.annotationsList = list;
  }

  public Set getLocalTypeNames() {
    if (this.localTypeNames == null) {
      this.localTypeNames = new HashSet(3);
    }

    return this.localTypeNames;
  }

  public void setLocalPrefix(byte localPrefix) {
    this.localPrefix = localPrefix;
  }

  public byte getLocalPrefix() {
    return this.localPrefix;
  }

  private boolean hasBuildErrors = false;
  private boolean definitelyInWrongFolder = false;

  private Project project;
  private final BinPackage aPackage;

  private BinField[] declaredFields;
  protected BinMethod[] declaredMethods;
  private BinTypeRef[] inners;
  private Map declaredFieldsMap; // caching
  private AdaptiveMultiValueMap accessibleMethodsMap; // caching

  private BinFieldDeclaration[] fieldDeclarations;
  private BinExpressionList annotationsList;

  private String cachedQualifiedName;

  private boolean local;
  private boolean anonymous;

  // to generate unique names for anonymous types
  private int anonymousInnersCount;

  private BinTypeRef selfUsageInfo;
  private BinTypeRef[] typeParameters;

  private CompilationUnit compilationUnit;

  private boolean deprecated;

  private HashSet localTypeNames;

  private byte localPrefix;
}
