/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.loader.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Resolver responsibilities can be divided to following tasks:
 * * resolving name as long qualified name ( sample: net.sf.Owner.Inner)
 * * resolving short name as superclass name (sample: extends Owner.Inner)
 *		short name is looked up from:
 *   - source file imports and own package
 *		- owner inners
 *		- owner super inners
 *	* resolving short name as implemented interface name (sample: implements Owner.InnerInterface)
 *		short name is looked up from:
 *   - source file imports and own package
 *		- owner inners
 *		- owner super inners
 *		- super inners ( this is different from resolving superclass name)
 *	* resolving short name type name used in class,
 *     that could be any of following: field type name, method return value type name,
 *     method parameter type name or local variable type name
 *			(sample: Owner.Inner myField)
 *			short name is looked up from:
 *   - source file imports and own package
 *		- owner inners
 *		- owner super inners
 *		- super inners
 *		- own inners ( this is different from resolving interface type name, look above)
 * * resolving inner type name.
 *			This is called only from other resolver's to get inner type for this type.This is part of resolver,
 *			because some parts are not yet resolved
 *			( for an instance current superclasses are not resolved from name to type yet)
 *			and could possibly throw SourceParsingException.
 *			short name is looked up from:
 *			- super inner
 *			- own inners
 */
public abstract class Resolver {
  protected Resolver(BinTypeRef aType) {
    this.type = aType;
  }

  public abstract BinTypeRef resolve(String name) throws SourceParsingException,
      LocationlessSourceParsingException;

  private void throwFriendlySourceParsingException(String description) throws
      SourceParsingException {

    final String locationInfo = getLocationInfo();

    SourceParsingException.throwWithUserFriendlyError(
        (locationInfo == null) ? (description)
        : (description + " at location: " + locationInfo),
        type.getBinCIType().getCompilationUnit(),
        type.getBinCIType().getOffsetNode()
        );
  }

  private UserFriendlyError createUserFriendlyError(String description) {
    final String locationInfo = getLocationInfo();

    UserFriendlyError friendlyError = new UserFriendlyError(
        (locationInfo == null) ? (description)
        : (description + ", " + locationInfo),
        type.getBinCIType().getCompilationUnit(),
        type.getBinCIType().getOffsetNode()
        );

    return friendlyError;
  }

  /**
   * @return map what contains all visible inners for given context
   * (owner inner(with supers), owner owner... inners(with supers),
   * own inners and super inners)
   * (currently unused)
   *
   * @see #getAccessibleInnersMapForSupers
   */
  private Map getAllAccessibleInnersMap() throws SourceParsingException {

    if (accessibleInners == null) {
      accessibleInners = getAccessibleInnersMapForSupers();

      BinTypeRef owner = type.getBinCIType().getOwner();
      if (owner != null) {
        for (final Iterator it =
            owner.getResolver().getAllAccessibleInnersMap().values().iterator();
            it.hasNext(); ) {

          BinTypeRef currentInner = (BinTypeRef) it.next();

          if (!accessibleInners.containsKey(currentInner.getName())) {
            accessibleInners.put(currentInner.getName(), currentInner);
          }
        }
      }
    }

    return accessibleInners;
  }

  private boolean checkingInnersInSupers = false;

  /**
   * Returs inners map for given context(own inners and super inners).
   *
   * @see #getSuperTypes
   */
  private Map getAccessibleInnersMapForSupers() throws SourceParsingException {

    if (checkingInnersInSupers) {
      getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(createUserFriendlyError("Cyclic inheritance for: "
            + type.getQualifiedName()));
      return new HashMap();
    }

    checkingInnersInSupers = true;

    Map anAccessibleInners = new HashMap();

    // add my own inners
    BinTypeRef[] myInners = type.getBinCIType().getDeclaredTypes();
    for (int i = 0; i < myInners.length; i++) {
      BinTypeRef currentInner = myInners[i];

      if (Assert.enabled && anAccessibleInners.containsKey(currentInner.getName())) {
        Assert.must(false,
            "Already defined Type !? " + currentInner.getName()
            + ", in " + type.getQualifiedName());
      }
      anAccessibleInners.put(currentInner.getName(), currentInner);
    }

    // add inners for all supertypes
    final BinTypeRef[] superTypes = getSuperTypes();
    for (int superI = 0; superI < superTypes.length; superI++) {
      final BinTypeRef currentSuper = superTypes[superI];

      Resolver superResolver = null;
      Map superInners = null;
      Iterator superInnersIterator = null;
      try {
        superResolver = currentSuper.getResolver();
        if (!Assert.enabled && superResolver == null) {
          UserFriendlyError e = createUserFriendlyError("Failed to resolve super: "
                    + (currentSuper == null ? "null" : currentSuper.getName())
                    + " in type: " + (type == null ? "null" : type.getName()));
          getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(e);
          continue;
        }
        superInners = superResolver.getAccessibleInnersMapForSupers();
        superInnersIterator = superInners.values().iterator();
      } catch (Exception e) {
        System.err.println("Registered exception:");
        e.printStackTrace();
        UserFriendlyError e1 = createUserFriendlyError("Failed to resolve super inner, type: "
                + (type == null ? "null" : type.getName())
                + ", super: "
                + (currentSuper == null ? "null" : currentSuper.getName())
                + ", resolver: " + (superResolver == null ? "no" : "yes")
                + ", inners: " + (superInners == null ? "no" : "yes"));
        getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(e1);
        continue;
      }

      while (superInnersIterator.hasNext()) {
        final BinTypeRef currentInner =
            (BinTypeRef) superInnersIterator.next();
        if (!currentInner.getBinCIType().isAccessible(type.getBinCIType())) {
          continue; // This inner is not accessible from this type
        }
        if (!anAccessibleInners.containsKey(currentInner.getName())) {
          anAccessibleInners.put(currentInner.getName(), currentInner);
        }
      }

    }

    checkingInnersInSupers = false;

    return anAccessibleInners;
  }

  /**
   * Searches type for inner within own & super inners.
   *
   * @see #getAccessibleInnersMapForSupers
   */
  final BinTypeRef getInnerTypeRefForName(String name) throws
      SourceParsingException {

    int pos = name.indexOf('.');
    String tmpName;
    if (pos == -1) {
      tmpName = name;
    } else {
      tmpName = name.substring(0, pos);
    }
    BinTypeRef currentInner =
        (BinTypeRef) getAccessibleInnersMapForSupers().get(tmpName);

    if (currentInner == null) {
      // inner not found
      return null;
    }

    if (pos == -1) {
      return currentInner; // inner found
    }
    if (Assert.enabled && !currentInner.isResolved()) {
      Assert.must(false, "type not resolved !? " + currentInner.getName());
    }
    String remainingName = name.substring(pos + 1); // +1 for dot
    return currentInner.getResolver().getInnerTypeRefForName(remainingName);
  }

  // FIXME: really slow one
  protected final BinTypeRef findTypeParameter(final String name) {
    BinTypeRef typeParameter = null;
    BinTypeRef curType = type;
    while (curType != null) {
      BinCIType curCIType = curType.getBinCIType();
      typeParameter = curCIType.getTypeParameter(name);
      if (typeParameter != null) {
        typeParameter = typeParameter.getTypeRefAsIs();
        break;
      }
      curType = curCIType.getOwner();
    }

    return typeParameter;
  }

  /**
   * Searches type for inner within all owners inners & owners super inners.
   *
   * @see #getAccessibleInnersMapForSupers
   */
  final BinTypeRef getAllInnerForName(String name) throws SourceParsingException {

    int pos = name.indexOf('.');
    String tmpName;
    if (pos == -1) {
      tmpName = name;
    } else {
      tmpName = name.substring(0, pos);
    }
    BinTypeRef currentInner =
        (BinTypeRef) getAllAccessibleInnersMap().get(tmpName);

    if (currentInner == null) {
      // JAVA5: this might be a wrong place and a wrong way to resolve type parameters
      currentInner = findTypeParameter(name);
    }

    if (currentInner == null) {
      // inner not found
      return null;
    }

    if (pos == -1) {
      return currentInner; // inner found
    }

    if (Assert.enabled && !currentInner.isResolved()) {
      Assert.must(false, "type not resolved !? " + currentInner.getName());
    }
    String remainingName = name.substring(pos + 1); // +1 for dot
    BinTypeRef innerTypeRefForName
        = currentInner.getResolver().getInnerTypeRefForName(remainingName);
    return innerTypeRefForName;
  }

  // ==============================================================================================

  /**
   * Returns array what contains all super types for given context
   * used for constructing own & super inners map for given context.
   *
   * @see #getAccessibleInnersMapForSupers
   */
  private BinTypeRef[] getSuperTypes() throws SourceParsingException {
    List superTypes = new ArrayList(2);

    resolveSuperTypes();

    // FIXME: can't just getSupertypes() be used? taking care of the nulls of course
    BinTypeRef currentSuper = type.getSuperclass();
    if (currentSuper != null) {
      superTypes.add(currentSuper);
    }
    BinTypeRef[] currentInterfaces = type.getInterfaces();
    if (currentInterfaces != null) {
      for (int i = 0; i < currentInterfaces.length; i++) {
        if (currentInterfaces[i] != null) {
          superTypes.add(currentInterfaces[i]);
        } else {
          // silently ignore, since the error is already reported
        }
      }
    }

    BinTypeRef[] retVal = new BinTypeRef[superTypes.size()];
    superTypes.toArray(retVal);
    return retVal;
  }

  final Project getProject() {
    return type.getProject();
  }

  final BinTypeRef getOwnerInnerForName(String name) throws SourceParsingException {

    BinTypeRef owner = type.getBinCIType().getOwner();
    if (owner == null) {
      return null;
    }
    return owner.getResolver().getAllInnerForName(name);
  }

  /**
   * Resolves interface for given context.
   *
   * @see #getOwnerInnerForName
   */
  private BinTypeRef resolveInterface(String name) throws
      SourceParsingException {

    BinTypeRef retVal = null;
    BinTypeRef superclass = type.getSuperclass();
    if (superclass != null) {
      Resolver resolver = superclass.getResolver();
      retVal = resolver.getInnerTypeRefForName(name);
    }

    if (retVal == null) {
      try {
        retVal = resolveAsSuperclass(name);
      } catch (LocationlessSourceParsingException e) {
        SourceParsingException.rethrowWithUserFriendlyError(
            e,
            type.getBinCIType().getOffsetNode());
      }
    }

    return retVal;
  }

  /**
   * Resolves superclass for given context.
   *
   * @see #getOwnerInnerForName
   */
  BinTypeRef resolveAsSuperclass(String name) throws SourceParsingException,
      LocationlessSourceParsingException {

    return null;
  }

  /**
   * Gets extra location information. Used to provide more detailed error
   * messages.
   *
   * @return extra location information or <code>null</code> no information
   * available.
   */
  String getLocationInfo() {
    return null;
  }

  private boolean inResolveSuperTypes = false;

  private boolean superTypesResolved = false;

  /**
   * This function try to resolve all supertypes.
   *
   * @see #getSuperTypes
   * @see #resolveAsSuperclass
   * @see #resolve
   */
  public void resolveSuperTypes() throws SourceParsingException {
    if (superTypesResolved) {
      return;
    }
//      final Stack typesInProgress = getTypesInProgress();
//System.err.println("--> resolveSuperTypes (" + resolveSuperTypesCalls
//    + "): " + type.getQualifiedName()
//  /*  + ", Stack: " + typesInProgress*/);
//    final String qName = type.getQualifiedName();

//    if (typesInProgress.search(qName) != -1) {
    if (inResolveSuperTypes) {
      getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(createUserFriendlyError("Cyclic inheritance for: "
            + type.getQualifiedName()));
      return;
    }

    inResolveSuperTypes = true;
//System.err.println("Pushing: " + qName);
//    typesInProgress.push(qName);

    try {
      if (type.getSuperclass() == null
          && type.getSuperclassQualifiedName() != null) {
        // Supertype exists, but needs to be resolved first

        BinTypeRef superclass = null;
        try {
          superclass = resolveAsSuperclass(type.getSuperclassQualifiedName());
        } catch (LocationlessSourceParsingException e) {
          SourceParsingException.rethrowWithUserFriendlyError(
              e,
              type.getBinCIType().getOffsetNode());
        }

        if (superclass == null) {
          throwFriendlySourceParsingException(
              "Could not resolve superclass " + type.getSuperclassQualifiedName());
        }

        if (!superclass.getBinCIType().isClass()
            && !superclass.getBinCIType().isEnum()
            && !superclass.getBinCIType().isAnnotation()) {
          final String description = "Superclass is not class !? " +
              type.getSuperclassQualifiedName();

          final String fullDescription =
              (getLocationInfo() == null) ? (description)
              : (description + ". " + getLocationInfo());
          (type.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(new UserFriendlyError(fullDescription,
                    type.getBinCIType().getCompilationUnit(),
                    type.getBinCIType().getOffsetNode()));

//          throwFriendlySourceParsingException("Superclass is not class !? "
//            + type.getSuperclassName());
        } else {
          if (type == superclass || type.equals(superclass)) { // TODO: resolving ways must be refactored, too complex and duplicated
            getProject().getProjectLoader().getErrorCollector().addNonCriticalUserFriendlyError(createUserFriendlyError("Cyclic inheritance for: "
                        + type.getQualifiedName()));
            superclass = getProject().getObjectRef();
          }

          type.setSuperclass(superclass);

          if (Settings.debugLevel > 50) {
            System.out.println("Superclass for " + type.getQualifiedName()
                + " is " + superclass.getQualifiedName());
          }
        }
      }

      String[] interfaceNames;

      // Resolve all implements/extended interfaces if any
      BinTypeRef[] interfaces = type.getInterfaces();
      if ((interfaces == null || interfaces.length == 0)
          && (interfaceNames = type.getInterfaceQualifiedNames()) != null) {
        final BinTypeRef[] resolvedInterfaces =
            new BinTypeRef[interfaceNames.length];

        for (int j = 0; j < interfaceNames.length; j++) {
          String interfaceName = interfaceNames[j];
          final BinTypeRef interfaceRef = resolveInterface(interfaceName);
          if (interfaceRef == null) {
            throwFriendlySourceParsingException(
                "Could not resolve interface " + interfaceName);
          }

          if (!interfaceRef.getBinCIType().isInterface()
              && !interfaceRef.getBinCIType().isAnnotation()) {
            throwFriendlySourceParsingException("Interface is not declared as"
                + " Interface !? "
                + interfaceRef.getBinCIType().getClass().getName() + ", "
                + interfaceName);
          }

          resolvedInterfaces[j] = interfaceRef;
        }
        type.setInterfaces(resolvedInterfaces);
      }
    } catch (Exception e) {
      //e.printStackTrace(System.err);
    } finally {
//      String tmpName = (String) typesInProgress.pop();
      /*System.err.println("Popping: " + tmpName);
            if (Assert.enabled) {
              Assert.must(qName.equals(tmpName),
       "Stack error:" + typesInProgress + "\n in type: " + qName);
            }*/
      inResolveSuperTypes = false;
      superTypesResolved = true;
    }
//System.err.println("<-- resolveSuperTypes: " + type.getQualifiedName());
  }

  /**
   * FIXME: If there is Inner defined in Owner itself,
   * then it will on same level of visibility as Inners in superclass
   */
  public static Resolver getForSourceType(BinTypeRef aType,
      CompilationUnit compilationUnit) {
    return new SourceTypeResolver(aType, compilationUnit);
  }

  public static Resolver getForClassFile(BinTypeRef aType) {
    return new ClassTypeResolver(aType);
  }

  public static Resolver getForLocalType(BinTypeRef aType,
      BinTypeRef globalOwnerType,
      BinTypeRef[] localDefinedTypes) {

    return new LocalTypeResolver(aType, globalOwnerType, localDefinedTypes);
  }

  public final String toString() {
    return ClassUtil.getShortClassName(this) + ": " + type;
  }

  private Map accessibleInners;
  final BinTypeRef type;
}
