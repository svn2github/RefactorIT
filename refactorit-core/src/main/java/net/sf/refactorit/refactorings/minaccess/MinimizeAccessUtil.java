/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.minaccess;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.query.usage.InvocationData;

import java.util.ArrayList;
import java.util.List;


/**
 * @author vadim
 * @author tonis
 */
public class MinimizeAccessUtil {
  private static final int[] PUB_PROT_PP_PRIV = {
      BinModifier.PUBLIC, BinModifier.PROTECTED,
      BinModifier.PACKAGE_PRIVATE, BinModifier.PRIVATE
  };

  private static final int[] PUB_PROT_PP = {
      BinModifier.PUBLIC, BinModifier.PROTECTED, BinModifier.PACKAGE_PRIVATE
  };

  private static final int[] PUB_PROT = {
      BinModifier.PUBLIC, BinModifier.PROTECTED
  };

  private static final int[] PUB = {BinModifier.PUBLIC};

  private static final int[] NULL_ACCESS_ARRAY = {};

  private MinimizeAccessUtil() {}

  static int[] getStricterAccessRights(
      BinTypeRef owner, BinMember member, List usages
      ) {
    if (member.isPrivate()) {
      return NULL_ACCESS_ARRAY;
    }

    int[] access = findAccessRights(owner.getBinCIType(), usages);
    int stricterAccessStarts = checkIfHasStricterAccess(member, access);

    if (stricterAccessStarts < access.length) {
      int[] stricter = new int[access.length - stricterAccessStarts];

      for (int i = stricterAccessStarts, j = 0; i < access.length; i++, j++) {
        stricter[j] = access[i];
      }

      if (member instanceof BinMethod) {
        stricter = extractMethodAccessRights((BinMethod) member, stricter);
      }

      return copyOfArray(stricter);
    }

    return NULL_ACCESS_ARRAY;
  }

  public static int[] findMethodAccessRights(final BinMethod method,
      List usages) {
    int[] access = findAccessRights(method.getOwner().getBinCIType(), usages);
    return copyOfArray(extractMethodAccessRights(method, access));
  }

  private static int[] extractMethodAccessRights(final BinMethod method,
      int[] access) {
    access = defineAccessIfOverrides(method.findOverrides(), access);
    if (method.isAbstract()) {
      access = removePrivateAccessIfExists(access);
    }
    return access;
  }

  private static int[] removePrivateAccessIfExists(int[] stricterOld) {
    if (stricterOld.length == 0 ||
        ((stricterOld.length == 1) && (stricterOld[0] == BinModifier.PRIVATE))) {
      return NULL_ACCESS_ARRAY;
    }

    List list = new ArrayList();

    for (int i = 0; i < stricterOld.length; i++) {
      if (stricterOld[i] == BinModifier.PRIVATE) {
        continue;
      }
      list.add(new Integer(stricterOld[i]));
    }

    if (list.size() == stricterOld.length) {
      return stricterOld;
    }

    Integer[] intArr = (Integer[]) list.toArray(new Integer[0]);

    int[] stricterNew = new int[intArr.length];
    for (int i = 0; i < intArr.length; i++) {
      stricterNew[i] = intArr[i].intValue();
    }

    return stricterNew;
  }

  private static int checkIfHasStricterAccess(BinMember member, int[] access) {
    int memberModif = member.getModifiers();

    for (int i = 0; i < access.length; i++) {
      if ((access[i] == BinModifier.PACKAGE_PRIVATE && member.isPackagePrivate())
          || ((memberModif & access[i]) != 0)) {
        return (i + 1);
      }
    }

    return access.length;
  }

  public static int getNewAccessForMember(BinMember member, BinCIType owner,
      List invocationData) {
    if ((invocationData == null) || (invocationData.size() == 0)) {
      return BinModifier.PRIVATE;
    }

    int[] accessRights = MinimizeAccessUtil.findAccessRights(
        owner, invocationData);

    int newAccess = accessRights[accessRights.length - 1];
    int currentAccess = member.getAccessModifier();

    return (BinModifier.compareAccesses(currentAccess, newAccess) == 1)
        ? currentAccess
        : newAccess;
  }

  public static final int[] findAccessRights(final BinCIType owner,
      final List invocationData) {
    final List invokedInTypes = new ArrayList();

    boolean invokedThroughNonSubtypeReference = false;

    for (int i = 0, max = invocationData.size(); i < max; i++) {
      InvocationData data = (InvocationData) invocationData.get(i);
      BinTypeRef locationType = data.getWhereType();

      if (locationType == null) {
        // bug 2050
        // InnerClass cases, now don't optimize anyway classes visibility, but should check

        continue;

// FIXME: remove this
//        System.err.println("[tonisdebug]: location ==null, sourceconstruct=" +
//                           data.getSourceConstruct() + ", itemCalled= " +
//                           data.getItemCalled()+ ",line:" + data.getLineNumber() +
//                           ", sourcefile " + data.getCompilationUnit());

      }

      // self or overriden is an interface method which can be PUBLIC only
      if (data.getWhat() instanceof BinMethod
          && ((BinMethod) data.getWhat()).getOwner().getBinCIType().isInterface()) {
        return new int[] {BinModifier.PUBLIC};
      }

      BinCIType invokedInType = locationType.getBinCIType();

      invokedThroughNonSubtypeReference = invokedThroughNonSubtypeReference
          || isInvokedThroughNonSubTypeReference(data, owner);

      //updateInvokedThroughRefMap(data, invokedInType, invokedThroughRefs);

      CollectionUtil.addNew(invokedInTypes, invokedInType);
    }

// FIXME: remove this
//    System.err.println("[tonisdebug]:" + "type =" + owner.getName() +
//                       "invokedThroughNonSubtypeReference=" +
//                       invokedThroughNonSubtypeReference);

    return findAccessRights(owner, invokedInTypes,
        invokedThroughNonSubtypeReference);
  }

  /**
   * returns true if ownerType has subTypes and and its member is invoked through non subtype reference
   * @param invocationData invocation data
   * @param ownerType owner type
   * @return true if invoked through non subtype reference.
   */
  private static final boolean isInvokedThroughNonSubTypeReference(
      final InvocationData invocationData,
      BinCIType ownerType) {

    BinTypeRef invokedOnRef = null;

    if(ownerType == null || ownerType.getTypeRef() == null) {
      return false;
    }

    List ownerSubtypes = ownerType.getTypeRef().getAllSubclasses();
    if (ownerSubtypes == null || ownerSubtypes.isEmpty()) {
      return false;
    }

    SourceConstruct sourceConstr = invocationData.getInConstruct();

    if (sourceConstr instanceof BinMemberInvocationExpression) {
      BinMemberInvocationExpression invocationExpression
          = (BinMemberInvocationExpression) sourceConstr;
      if (invocationExpression.isOutsideMemberInvocation()) {
        invokedOnRef = invocationExpression.getInvokedOn();
        BinTypeRef superOfInvokedOnRef = invokedOnRef.getSuperclass();
        if (superOfInvokedOnRef != null &&
            superOfInvokedOnRef.getBinCIType().equals(ownerType)) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

//  private final static void updateInvokedThroughRefMap(
//      final InvocationData data,
//      final BinCIType invokedInType,
//      final Map invokedThroughRefs) {
//    Boolean invokedThroughRef = (Boolean) invokedThroughRefs.get(invokedInType);
//
//    BinCIType invokedOnRef=null;
//
//    if (invokedThroughRef == null) {
//      invokedThroughRef = Boolean.FALSE;
//    }
//
//    if (!invokedThroughRef.booleanValue()) {
//      BinSourceConstruct sourceConstr = data.getSourceConstruct();
//      if (sourceConstr instanceof BinMemberInvocationExpression) {
//        BinMemberInvocationExpression invocationExpression=((BinMemberInvocationExpression)sourceConstr);
//        if (invocationExpression.isOutsideMemberInvocation()) {
//          invokedThroughRef = Boolean.TRUE;
//          invokedOnRef= invocationExpression.getInvokedOn().getBinCIType();
//        }
//      }
//      //invokedThroughRefs.put(invokedInType, invokedThroughRef);
//      invokedThroughRefs.put(invokedInType, invokedOnRef);
//
//    }
//  }

  /**
   * finds out what possible access rights can have a member declared in owner
   * if this member is invoked in invokedInTypes
   * @param owner a class which declares a member
   * @param invokedInTypes a set of classes which invoke a member declared in owner
   * @param invokedThroughNotSubtypeReference a flag
   * @return possible access rights
   */
  private static int[] findAccessRights(
      final BinCIType owner,
      final List invokedInTypes,
      boolean invokedThroughNotSubtypeReference) {

    boolean sameType = true;
    boolean samePackage = true;
    // for situation: p1.Super1 <- p2.Super2 <- p1.Invoker
    boolean isAllSubsBetweenInSamePackage = true;
    boolean subType = false;
    // for situation: member is invoked in subtype through reference of super class

    InheritanceInfo oldInfo = null;

    for (int i = 0, max = invokedInTypes.size(); i < max; i++) {
      BinCIType invokedInType = (BinCIType) invokedInTypes.get(i);

      while (invokedInType.isAnonymous() || invokedInType.isLocal() || invokedInType.isInnerType()) {
        if (invokedInType.getTypeRef().isDerivedFrom(owner.getTypeRef())) {
          break;
        }

        invokedInType = invokedInType.getOwner().getBinCIType();
      }
      // if none of the owners satisfied our special needs, let's start from original type
      if (invokedInType == null || invokedInType.getTypeRef() == null
          || !invokedInType.getTypeRef().isDerivedFrom(owner.getTypeRef())) {
        invokedInType = (BinCIType) invokedInTypes.get(i);
      }

      if (!owner.getPackage().isIdentical(invokedInType.getPackage())) { // different packages
        samePackage = false;
        sameType = false;
        isAllSubsBetweenInSamePackage = false;

        final InheritanceInfo info = checkForInheritance(owner, invokedInType, false);
        if (oldInfo == null || oldInfo.subType) {
          subType = info.subType;
        }
        oldInfo = info;
      }

      if (samePackage && (owner != invokedInType)) { // same package
        sameType = false;

        final InheritanceInfo info = checkForInheritance(owner, invokedInType, true);
        if (oldInfo == null || oldInfo.subType) {
          subType = info.subType;
        }
        oldInfo = info;

        if (isAllSubsBetweenInSamePackage) {
          isAllSubsBetweenInSamePackage = info.isAllSubsBetweenInSamePackage;
        }
      }

    }

//		printConditions(sameType, subType, isAllSubsBetweenInSamePackage,
//                    samePackage, isInvokedThroughRef); //innnnn
    return getPossibleAccess(sameType, subType, isAllSubsBetweenInSamePackage,
        samePackage, invokedThroughNotSubtypeReference);
  }

  private static int[] getPossibleAccess(boolean sameType, boolean subType,
      boolean allSubsInSamePackage,
      boolean samePackage,
      boolean invokedThroughNotSubtypeReference) {
    if (sameType) {
      return PUB_PROT_PP_PRIV;
    }

    if (samePackage) {
      if (subType) {
//        if (isInvokedThroughRef) {
//  				return PUB;
//        }else
        if (allSubsInSamePackage) {
          return PUB_PROT_PP;
        } else {
          // case when supertype is accessed through inheritance from different package
          return PUB_PROT;
        }

      } else {
        return PUB_PROT_PP;
      }
    } else {
      if (subType) {
        if (invokedThroughNotSubtypeReference) {
          return PUB;
        } else {
          return PUB_PROT;
        }
      } else {
        return PUB;
      }
    }
  }

  private static InheritanceInfo checkForInheritance(BinCIType type,
      BinCIType newType,
      boolean isAllSubsBetweenInSamePackage) {
    BinTypeRef superTypeRef = newType.getTypeRef().getSuperclass();
    BinCIType superType = superTypeRef != null ? superTypeRef.getBinCIType() : null;

    if ((superType == null) || !superType.isFromCompilationUnit()) {
      return new InheritanceInfo(false, false);
    }

    if (superType == type) {
      return new InheritanceInfo(true, isAllSubsBetweenInSamePackage);
    } else {
      isAllSubsBetweenInSamePackage = isAllSubsBetweenInSamePackage
          ? areInSamePackage(type, superType)
          : false;

      return checkForInheritance(type, superType, isAllSubsBetweenInSamePackage);
    }
  }

  private static boolean areInSamePackage(BinCIType type, BinCIType newType) {
    return (type.getPackage() == newType.getPackage());
  }

  private static class InheritanceInfo {
    boolean subType;
    boolean isAllSubsBetweenInSamePackage;

    InheritanceInfo(boolean subType, boolean isAllSubsBetweenInSamePackage) {
      this.subType = subType;
      this.isAllSubsBetweenInSamePackage = isAllSubsBetweenInSamePackage;
    }
  }


  private static int[] defineAccessIfOverrides(List overrides,
      int[] stricterOld) {
    if (overrides == null || overrides.size() == 0) {
      return stricterOld;
    }

    int result = (PUB_PROT_PP_PRIV.length - 1);
    for (int i = 0, max = overrides.size(); i < max; i++) {
      int index = indexSearch(PUB_PROT_PP_PRIV,
          ((BinMethod) overrides.get(i)).getAccessModifier());

      if (index > -1 && index < result) {
        result = index;
      }
    }

    int[] certainModifiers = new int[result + 1];
    for (int i = 0; i <= result; i++) {
      certainModifiers[i] = PUB_PROT_PP_PRIV[i];
    }

    return selectCertainModifiers(stricterOld, certainModifiers);
  }

  private static int indexSearch(int[] array, int key) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == key) {
        return i;
      }
    }

    return -1;
  }

  private static int[] selectCertainModifiers(int[] stricterOld,
      int[] whichToSelect) {
    List places = findPlaces(stricterOld, whichToSelect);

    if (places.size() == 0) {
      return NULL_ACCESS_ARRAY;
    }

    Integer[] intArr = (Integer[]) places.toArray(new Integer[0]);
    int[] stricterNew = new int[intArr.length];

    for (int i = 0; i < intArr.length; i++) {
      int place = intArr[i].intValue();
      stricterNew[i] = stricterOld[place];
    }

    return stricterNew;
  }

  private static List findPlaces(int[] stricterOld, int[] whichToSelect) {
    List places = new ArrayList();

    for (int i = 0; i < stricterOld.length; i++) {
      for (int j = 0; j < whichToSelect.length; j++) {
        if (stricterOld[i] == whichToSelect[j]) {
          places.add(new Integer(i));
        }
      }
    }

    return places;
  }

//	private static void printConditions(boolean sameType, boolean subType,
//																			boolean isAllSubsBetweenInSamePackage,
//																			boolean samePackage,
//                                      boolean isInvokedThroughSuperClass) {
//		System.err.println("sameType:" + sameType);
//		System.err.println("subType:" + subType);
//		System.err.println("isAllSubsBetweenInSamePackage:" + isAllSubsBetweenInSamePackage);
//		System.err.println("samePackage:" + samePackage);
//		System.err.println("isInvokedThroughSuperClass:" + isInvokedThroughSuperClass + "\n");
//	}

  private static int[] copyOfArray(int[] a) {
    int[] result = new int[a.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = a[i];
    }
    return result;
  }
}
