/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
 package net.sf.refactorit.refactorings.usesupertype;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.common.util.BidirectionalMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Tonis Vaga
 */
public class SuperClassInfo {
  BinTypeRef ref;

  //private Map virtualMethodSignatures = new HashMap();
  private BidirectionalMap virtualMethodSignatures = new BidirectionalMap();

  private List fields;
//  private BinMethod methods[];

  private List failuresList;

  public SuperClassInfo(BinTypeRef ref) {
    this.ref = ref;
    fields = ref.getBinCIType().getAccessibleFields(ref.getBinCIType());
//    methods = type.getAccessibleMethods(type);
  }

  public BinTypeRef getSupertypeRef() {
    return ref;
  }

  /**
   * Checks if member is accessible from context when type
   * or invokedOn == this.ref.getBinCIType()
   * @param member
   * @param context
   */
  public BinMember hasAccessibleMemberWithSignature(BinMember member,
      BinCIType context) {
    if (member instanceof BinField) {
      return hasFieldWithSignature(member);
    }

    BinMember result = null;

    if (member instanceof BinMethod) {
      BinMethod method = (BinMethod) member;

      BinTypeRef[] parameterTypes
          = BinParameter.parameterTypes(method.getParameters());

      if (method instanceof BinConstructor) {
        result = hasAccessibleConstructorWithSignature(context, parameterTypes);
      } else {
        result = hasAccessibleMethodWithSignature(
            this.ref.getBinCIType(), context, method.getName(), parameterTypes);
      }

    }

    return result;
  }

  /**
   * @param member
   */
  public BinMember hasFieldWithSignature(final BinMember member) {
    if (fields.contains(member)) {
      return member;
    } else {
      return null;
    }
  }

  private BinMethod checkObjectMethods(final String methodName,
      final BinCIType context,
      BinTypeRef parameterTypes[]) {
    BinTypeRef objectType = ref.getProject().getObjectRef();

    return MethodInvocationRules.getMethodDeclaration(context, objectType,
        methodName, parameterTypes);

  }

  public BinMethod hasAccessibleMethodWithSignature(final BinCIType owner, final BinCIType context,
      final String methodName, final BinTypeRef[] parameterTypes) {

    MethodSignature methSignature = new MethodSignature(owner, methodName, parameterTypes);
    BinMethod result = (BinMethod)virtualMethodSignatures.getKeyByValue(methSignature);

    if (result != null) {
      return result;
    }

    result = MethodInvocationRules.getMethodDeclaration(context,
        owner.getTypeRef(), methodName, parameterTypes);

    if ((result != null) && virtualMethodSignatures.getKeySet().contains(result)) {
      // method is already altered
      result = null;
    }

    //BinCIType type = ref.getBinCIType();

    if (result == null && owner.isInterface()) {
      result = checkObjectMethods(methodName, owner,  parameterTypes);
    }

    return result;
  }

  public BinMethod hasAccessibleConstructorWithSignature(final BinCIType
      type, final BinTypeRef[] parameterTypes) {
    if (!(type instanceof BinClass)) {
      return null;
    }

    //BinClass type = (BinClass) ref.getBinCIType();

    return ((BinClass)type).getAccessibleConstructor(type, parameterTypes);
  }

  /**
   * @return true if
   */
  public boolean isReportFailures() {
    return failuresList == null;
  }
  public void setReportFailures(boolean bVal) {
    if ( bVal ) {
      failuresList=new ArrayList();
    } else {
      failuresList=null;
    }
  }
  public void addFailure(Object obj) {
    if ( failuresList != null ) {
      failuresList.add(obj);
    }
  }

  /**
   * @param ownerType
   * @param name
   * @param refs
   * @param parameterTypes
   */
  public void changeVirtualMethod(BinMethod method, BinTypeRef[] newParameterTypes) {
    // since all hierarchy signatures are changed, create virtual signatures for
    // all the methods of the hierarchy
	List hierarchy = method.findAllOverridesOverriddenInHierarchy();
    hierarchy.add(method);
    for(int j = 0; j < hierarchy.size(); j++) {
      BinMethod tmpMeth = (BinMethod) hierarchy.get(j);
	  virtualMethodSignatures.put(tmpMeth,
        new MethodSignature(tmpMeth.getOwner().getBinCIType(),
            tmpMeth.getName(),
            newParameterTypes));
    }
  }

  static class MethodSignature {

    BinCIType ownerType;
    String name;
    BinTypeRef[] parameterTypes;

    /**
     * @param owner
     * @param name
     * @param parameterTypes
     */
    public MethodSignature(BinCIType ownerType, String name,
        BinTypeRef[] parameterTypes) {
      this.ownerType = ownerType;
      this.name = name;
      this.parameterTypes = parameterTypes;
    }

    public boolean equals(Object o) {
      MethodSignature s2 = (MethodSignature) o;
      return (s2.ownerType.equals(ownerType)
          && s2.name.equals(name)
          && Arrays.equals(s2.parameterTypes, parameterTypes));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
      int paramaterHash = 0;
      for (int i = 0; i< parameterTypes.length; i++) {
        paramaterHash += parameterTypes[i].hashCode();
      }
      return ownerType.hashCode() + name.hashCode() + paramaterHash;
    }
  }

  public boolean hasChangedMethodSignature(BinMethod m){
    return virtualMethodSignatures.getKeySet().contains(m);
  }

  public BinTypeRef[] getVirtualMethodParameters(BinMethod m){
    MethodSignature methSignature = (MethodSignature) virtualMethodSignatures
        .getValueByKey(m);
    if (methSignature != null) {
      return (BinTypeRef[]) methSignature.parameterTypes.clone();
    }
    return null;
  }
}
