/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.modifiers;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTreeTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.BinWildcardTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.EjbUtil;
import net.sf.refactorit.utils.SerializationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemberUsageCollector extends BinItemVisitor {
  private static final String SERIAL_VERSION_UID = "serialVersionUID";

  private static final int NO_VISIBILITY = 0;
  private static final int SAME_INHERITAGE_VISIBILITY = 1;
  private static final int INNER_VISIBILITY = 2;

  private static final Integer PRIVATE_ACCESS = new Integer(BinModifier.PRIVATE);

  private Map memberAccessModifiers;
  private Set hierarchyTopMethods;

  private BinTypeRef currentType;
  private BinPackage currentPackage;

  ProgressListener listener;

  private int compilationUnits, cntr = 0;

  private BinTypeRef objTypeRef;
  private BinTypeRef classTypeRef;

  private boolean reflectionUsed = false;

  private HashSet visited; //used in checkGenerics()

  public MemberUsageCollector(Map memberAccess, Set hierarchyTopMethods,
      int compilationUnits) {
    this.memberAccessModifiers = memberAccess;
    this.hierarchyTopMethods = hierarchyTopMethods;
    this.compilationUnits = compilationUnits;
    listener = (ProgressListener) CFlowContext.get(ProgressListener.class
        .getName());
  }

  public void visit(CompilationUnit cu) {
    cntr++;
    if (listener != null) {
      listener.progressHappened(ProgressMonitor.Progress.FULL.getPercentage(
          cntr, compilationUnits));
    }
    if (objTypeRef == null) {
      objTypeRef = cu.getProject().getTypeRefForName("java.lang.Object");
    }
    if (classTypeRef == null) {
      classTypeRef = cu.getProject().getTypeRefForName("java.lang.Class");
    }

    // handle import usages
    handleImports(cu);

    super.visit(cu);
  }

  private void handleImports(final CompilationUnit cu) {
    List importUsages = cu.getImportedTypeNames();
    if(importUsages != null) {
      Project p = cu.getProject();
      for(int i = 0, max = importUsages.size(); i < max; i++) {
        String name = (String)importUsages.get(i);
        BinTypeRef ref = p.getTypeRefForSourceName(name);
        if(ref != null && ref.getCompilationUnit() != null) {
          handleType(ref);
        }
      }
    }
  }

  public void visit(BinCIType type) {
    currentType = type.getTypeRef();
    currentPackage = type.getPackage();
    if (type.isInnerType()) {
      markDeclaration(type);
    }
    checkGenerics(currentType, true);

    super.visit(type);
  }

  public void visit(BinFieldInvocationExpression expr) {
    handleField(expr.getField(), handleInvokedOn(expr.getExpression()));
    super.visit(expr);
  }

  public void visit(BinMethodInvocationExpression expr) {
    BinMethod meth = expr.getMethod();
    handleMethod(meth, handleInvokedOn(expr.getExpression()));

    super.visit(expr);
  }

  public void visit(BinConstructorInvocationExpression expr) {
    handleMethod(expr.getMethod(), false);
    super.visit(expr);
  }

  public void visit(BinNewExpression expr) {
    handleType(expr.getTypeRef());
    if (expr.getConstructor() != null) {
      handleMethod(expr.getConstructor(), true);
    }
    super.visit(expr);
  }

  public void visit(BinLocalVariableDeclaration decl) {
    BinVariable vars[] = decl.getVariables();
    for (int i = 0; i < vars.length; i++) {
      handleType(vars[i].getTypeRef());
    }
    super.visit(decl);
  }

  public void visit(BinFieldDeclaration decl) {
    boolean isSerializable =
      SerializationUtils.isSerializable(decl.getParentType());

    BinVariable vars[] = decl.getVariables();

    for (int i = 0; i < vars.length; i++) {
      BinTypeRef varTypeRef = vars[i].getTypeRef();
      handleType(varTypeRef);
      if(isSerializable && SERIAL_VERSION_UID.equals(vars[i].getName()) &&
          varTypeRef.equals(BinPrimitiveType.LONG.getTypeRef())) {
        memberAccessModifiers
          .put(vars[i], new Integer(vars[i].getAccessModifier()));
        return;
      } else {
        markDeclaration(vars[i]);
      }
    }
    super.visit(decl);
  }

  public void visit(BinConstructor meth) {
    visitMethod(meth);
    handleSuperConstructor(meth);
    super.visit(meth);
  }

  public void visit(BinCastExpression expr) {
    handleType(expr.getReturnType());
    super.visit(expr);
  }

  public void visit(BinMethod method) {
    visitMethod(method);
    handleType(method.getReturnType());
    super.visit(method);
  }

  /**
   * Handles method parameter types and return type when visiting method
   * declaration
   * @param method - BinMethod to handle
   */
  private void visitMethod(BinMethod method) {
    BinParameter[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      handleType(params[i].getTypeRef());
    }

    if (method.isMain() || EjbUtil.isEjbMethod(method)) {
      memberAccessModifiers
          .put(method, new Integer(method.getAccessModifier()));
    } else {
      if(method.isAbstract()) {
        setAccess(method, new Integer(BinModifier.PACKAGE_PRIVATE));
      } else {
        markDeclaration(method);
      }
      if (method.getCompilationUnit() != null) {
        BinMethod superTop = method.getTopSuperclassMethod();
        if (!(superTop instanceof BinConstructor) && !superTop.equals(method)) {
          hierarchyTopMethods.add(superTop);
        }

        List overrides = method.findOverrides();
        for (int i = 0; i < overrides.size(); i++) {
          BinMethod meth = (BinMethod) overrides.get(i);
          handleMemberAccess(meth, false);
        }
      }
    }
  }

  /**
   * set members minimal access to minimial and extend if usages appear
   */
  private void markDeclaration(BinMember member) {
    if (!memberAccessModifiers.keySet().contains(member)) {
      setAccess(member, PRIVATE_ACCESS);
    }
  }

  private void setAccess(BinMember member, Integer value) {
    memberAccessModifiers.put(member, value);
  }

  private boolean isInvokedFromInner(BinTypeRef upper, BinTypeRef current) {
    if (upper == null || current == null) {
      return false;
    }
    BinCIType ciType = current.getBinCIType();
    if (!(ciType.isLocal() || ciType.isAnonymous() || ciType.isInnerType())) {
      return false;
    }
    return current.getBinType().contains(upper.getBinCIType());
  }

  private boolean samePackage(BinPackage p1, BinPackage p2) {
    return (p1 == null && p2 == null) || p1.equals(p2);
  }

  private int getMinimalAccess(BinMember member) {
    BinTypeRef typeRef = member.getOwner();
    if (currentType != null && typeRef != null && !typeRef.isPrimitiveType()
        && typeRef.getCompilationUnit() != null) {

      boolean fromOnePackage = samePackage(currentPackage, typeRef.getPackage());
      boolean oneInheritage = isFromOneInheritage(currentType, typeRef);
      boolean invokedFromInner = isInvokedFromInner(typeRef, currentType);

      if (!currentType.equals(typeRef)
          && !fromOnePackage
          && !invokedFromInner
          && (oneInheritage || (getVisibility(currentType, typeRef) & SAME_INHERITAGE_VISIBILITY) > 0)) {
        return BinModifier.PROTECTED;
      } else if (fromOnePackage) {
        if (currentType.equals(typeRef) || invokedFromInner
            || (getVisibility(currentType, typeRef) & INNER_VISIBILITY) > 0) {
          return BinModifier.PRIVATE;
        } else {
          return BinModifier.PACKAGE_PRIVATE;
        }
      } else {
        return BinModifier.PUBLIC;
      }
    }
    return member.getAccessModifier();
  }

  private void handleType(BinTypeRef typeRef) {
    BinCIType ciType = typeRef.getBinCIType();

    if (ciType != null && !ciType.isPrimitiveType()) {
      checkGenerics(typeRef, true);
    }

    if (typeRef.getCompilationUnit() == null || !ciType.isInnerType()
        || ciType.isAnonymous() || ciType.isInterface()
        || typeRef.getTypeParameters().length > 0) {
      return;
    }

    handleMemberAccess(ciType, false);
  }

  private void handleField(BinField field, boolean invokedOnInstance) {
    handleMemberAccess(field, invokedOnInstance);
  }

  private void handleMethod(BinMethod method, boolean invokedOnInstance) {
    if (method.isMain() || EjbUtil.isEjbMethod(method)) {
      memberAccessModifiers
          .put(method, new Integer(method.getAccessModifier()));
      return;
    }

    handleMemberAccess(method, invokedOnInstance);
  }

  private void handleMemberAccess(BinMember member, boolean invokedOnInstance) {
    Integer currentAccess = (Integer) memberAccessModifiers.get(member);
    int newAccess = getMinimalAccess(member);

    // to avoid wrong handling of references from hierarchy
    if (invokedOnInstance && newAccess == BinModifier.PROTECTED) {
      newAccess = BinModifier.PUBLIC;
    } else if (newAccess == BinModifier.PACKAGE_PRIVATE
        && !isPackagePrivateAllowed(member)) {
      newAccess = BinModifier.PROTECTED;
    }
    if (currentAccess == null
        || BinModifier.compareAccesses(newAccess, currentAccess.intValue()) > 0) {
      currentAccess = new Integer(newAccess);
      memberAccessModifiers.put(member, currentAccess);
    }
  }

  public void postProcess() {
    for (Iterator it = hierarchyTopMethods.iterator(); it.hasNext();) {
      BinMethod method = (BinMethod) it.next();
      boolean hierarchical = traverseHierarchy(method);
      if (!hierarchical) {
        it.remove();
      }
    }
    //hierarchyTopMethods.clear();
  }

  private boolean traverseHierarchy(BinMethod upper) {
    int access = getMemberAccess(upper);
    boolean result = false;
    Set subclasses = upper.getOwner().getDirectSubclasses();
    for (Iterator it = subclasses.iterator(); it.hasNext();) {
      BinTypeRef typeRef = (BinTypeRef) it.next();
      List subs = upper.findOverrideInType(typeRef);
      for (int i = 0; i < subs.size(); i++) {
        BinMethod sub = (BinMethod) subs.get(i);
        int subAccess = getMemberAccess(sub);
        if (BinModifier.compareAccesses(subAccess, access) < 0) {
          memberAccessModifiers.put(sub, new Integer(access));
          result = true;
        }
        result |= traverseHierarchy(sub);
      }
    }
    return result;
  }

  private int getMemberAccess(BinMember member) {
    Integer foundAccess = (Integer) memberAccessModifiers.get(member);
    return (foundAccess == null) ? member.getAccessModifier() : foundAccess
        .intValue();
  }

  private boolean handleInvokedOn(BinExpression expr) {
    boolean result;
    if (expr != null) {
      BinTypeRef typeRef = expr.getReturnType();
      handleType(typeRef);
      reflectionUsed |= (typeRef != null && typeRef.equals(classTypeRef));
      result = !typeRef.equals(currentType);
    } else {
      result = false;
    }
    return result;
  }

  private void handleSuperConstructor(BinConstructor meth) {
    BinTypeRef upper = meth.getOwner().getSuperclass();
    if (upper != null && upper.getCompilationUnit() != null) {
      BinCIType ciType = upper.getBinCIType();
      if (ciType.isClass()) {
        BinConstructor defaultConstr = ((BinClass) ciType)
            .getDefaultConstructor();
        if (defaultConstr != null) {
          handleMemberAccess(defaultConstr, false);
        }
      }
    }
  }

  private void checkGenerics(BinTypeRef typeRef, boolean skipFirst) {
    if (typeRef == null || typeRef.equals(objTypeRef)) {
      return;
    }

    if (skipFirst) {
      visited = new HashSet();
    }

    if (typeRef instanceof BinCITypeRef && !skipFirst) {
      BinCIType ciType = typeRef.getBinCIType();
      if (typeRef.getCompilationUnit() != null && ciType.isInnerType()
          && !ciType.isTypeParameter()) {
        handleMemberAccess(ciType, false);
      }
    }

    if (typeRef instanceof BinSpecificTypeRef
        && typeRef.getTypeRef() instanceof BinWildcardTypeRef
        || typeRef instanceof BinTreeTypeRef) {
      if (!visited.contains(typeRef.getTypeRef())) {
        checkGenerics(typeRef.getTypeRef(), false);
      }
    }

    visited.add(typeRef);

    List types = new ArrayList();
    if (typeRef instanceof BinCITypeRef) {
      BinTypeRef typeRefs[] = typeRef.getSupertypes();
      if (typeRefs != null && typeRefs.length > 0) {
        types.addAll(Arrays.asList(typeRefs));
        types.remove(objTypeRef);
      }
    }

    BinTypeRef typeRefs[] = typeRef.getTypeArguments();
    if (typeRefs != null && typeRefs.length > 0) {
      types.addAll(Arrays.asList(typeRefs));
    }

    for (int i = 0; i < types.size(); i++) {
      BinTypeRef type = (BinTypeRef) types.get(i);
      checkGenerics(type, false);
    }
    types.clear();
  }

  private boolean isGeneric(BinTypeRef typeRef) {
    return typeRef.getTypeArguments() != null
        && typeRef.getTypeArguments().length > 0;
  }

  private boolean isFromOneInheritage(BinTypeRef current, BinTypeRef typeRef) {
    if (typeRef.isDerivedFrom(current) || current.isDerivedFrom(typeRef)) {
      return true;
    }
    return false;
  }

  private int getVisibility(BinTypeRef current, BinTypeRef typeRef) {
    BinCIType ciType = current.getBinCIType();
    if (!(ciType.isLocal() || ciType.isAnonymous() || ciType.isInnerType())
        || ciType.isTypeParameter()) {
      return NO_VISIBILITY;
    }

    while (ciType != null) {
      BinTypeRef owner = ciType.getOwner();
      ciType = (owner == null) ? null : owner.getBinCIType();
      if (owner != null)
        if (ciType.equals(typeRef.getBinCIType())) {
          return INNER_VISIBILITY;
        } else if (owner.isDerivedFrom(typeRef)) {
          return SAME_INHERITAGE_VISIBILITY;
        }
    }
    return NO_VISIBILITY;
  }

  private BinTypeRef unwrapInnerCall(BinTypeRef current, BinTypeRef typeRef) {
    BinCIType ciType = current.getBinCIType();
    while (ciType != null) {
      BinTypeRef owner = ciType.getOwner();
      ciType = (owner == null) ? null : owner.getBinCIType();
      if (owner != null
          && (owner.isDerivedFrom(typeRef) || ciType.equals(typeRef))) {
        return owner;
      }
    }
    return current;
  }

  private boolean isPackagePrivateAllowed(BinMember member) {
    // if a hierarchy passes through other packages
    BinTypeRef typeRef = currentType;
    BinTypeRef stopOwner = member.getOwner();
    if(stopOwner == null) {
      // this cannot happen! But REF-1470 has own's will ;( 
      // need to log some more information, if this happens:
      String message = "Unexcepted error occured! The [" + member.toString() 
      + "]'s owner was null. The current type is [" + currentType.getBinType().toString() + "]";
      throw new RuntimeException(message);
    }
    while (typeRef != null && !stopOwner.equals(typeRef)) {
      if (!samePackage(currentPackage, typeRef.getPackage())) {
        return false;
      }
      if (typeRef.isDerivedFrom(stopOwner)) {
        typeRef = typeRef.getSuperclass();
      } else {
        typeRef = typeRef.getBinCIType().getOwner();
      }
    }
    return true;
  }

  public boolean isReflectionUsed() {
    return reflectionUsed;
  }
}
