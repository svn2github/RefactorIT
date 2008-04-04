/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.ejb.RitEjbModule;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class MethodIndexer extends TargetIndexer {
  private final boolean toString;

  private final Set allowedMethods = new HashSet();
  private final Set allowedTypes = new HashSet();

  public MethodIndexer(final ManagingIndexer supervisor,
      final BinMethod target,
      final BinMethodSearchFilter filter) {
    this(supervisor, target, target.getOwner().getBinCIType(), filter);
  }

  public MethodIndexer(final ManagingIndexer supervisor,
      final BinMethod target,
      final BinCIType invokedOn,
      final BinMethodSearchFilter filter) {
    super(supervisor, target, invokedOn, filter);

    this.toString = target.isToString();

    fillUpAllowed();
  }

  private void fillUpAllowed() {
    final BinMethod targetMethod = (BinMethod) getTarget();

    final BinTypeRef pointType;
    if (((BinMethodSearchFilter) getFilter()).isInterfaceSearch()
        || targetMethod.isStatic()) {
      pointType = targetMethod.getOwner();
    } else {
      pointType = getTypeRef();
    }

    // minimal list contains method clicked on
    allowedMethods.add(targetMethod);
    allowedTypes.add(pointType);

    if (isIncludeSubtypes()) {
      final List subs = pointType.getBinCIType().getSubMethods(targetMethod);
      if (((BinMethodSearchFilter) getFilter()).isInterfaceSearch()) {
        allowedMethods.addAll(subs);
        allowedTypes.addAll(pointType.getAllSubclasses());
        if(pointType.getBinType().isInterface()) {
          allowedTypes.addAll(RitEjbModule.getRelatedEJBImplTypeRefs(pointType));
        }
      } else {
        final List subTypes = pointType.getAllSubclasses();
        BinTypeRef lowLimit = null;
        if (subs.size() > 0) {
          lowLimit = ((BinMethod) subs.get(0)).getOwner();
        }
        for (int i = 0; i < subTypes.size(); i++) {
          final BinTypeRef subType = (BinTypeRef) subTypes.get(i);
          if (subType == lowLimit || subType.equals(lowLimit)) {
            break; // after that it starts using sub method and target can't be called
          }
          this.allowedTypes.add(subType);
        }
      }
    }

    if (isIncludeSupertypes()) {
      if (isAllBranchesScan()) {
        List allBranchesMethods=targetMethod.findAllOverridesOverriddenInHierarchy();
        allowedMethods.addAll(allBranchesMethods);
        for (Iterator i = allBranchesMethods.iterator(); i.hasNext();) {
          BinMethod branchMethod = (BinMethod) i.next();
          BinTypeRef branchMethodOwner = branchMethod.getOwner();
          allowedTypes.add(branchMethodOwner);
          allowedTypes.addAll(branchMethodOwner.getAllSubclasses());
        }
      } else {
        allowedMethods.addAll(targetMethod.findAllOverrides());
        final List topMethods = targetMethod.getTopMethods();
        for (int i = 0, max = topMethods.size(); i < max; i++) {
          final BinMethod topMethod = (BinMethod) topMethods.get(i);
          final BinTypeRef topMethodOwner = topMethod.getOwner();
          allowedTypes.add(topMethodOwner);
          final List subClasses = topMethodOwner.getAllSubclasses();
          if (isAllBranchesScan()) {
            allowedTypes.addAll(subClasses);
            allowedMethods.addAll(topMethodOwner.getBinCIType().getSubMethods(
                topMethod));
          } else {
            for (int k = 0, maxK = subClasses.size(); k < maxK; k++) {
              final BinTypeRef sub = (BinTypeRef) subClasses.get(k);
              if (!pointType.isDerivedFrom(sub)) {
                continue;
              }
              if (sub == pointType || sub.equals(pointType)) {
                break;
              }
              allowedTypes.add(sub);
            }
          }
        }
      }
    }
//System.err.println("allowed methods: " + allowedMethods);
//System.err.println("allowed types: " + allowedTypes);
  }

  protected final boolean isAllBranchesScan() {
    return isIncludeSupertypes() && isIncludeSubtypes()
        && ((BinMethodSearchFilter) getFilter()).isInterfaceSearch();
  }

  public final void visit(final BinMethodInvocationExpression expression) {
    if (!((BinMethodSearchFilter) getFilter()).isUsages()) {
      return;
    }

    final BinMethod method = expression.getMethod();

    if (this.allowedMethods.contains(method)) {
      final BinTypeRef invokedOn;
      BinTypeRef owner = method.getOwner();
      if (method.isStatic()) {
        invokedOn = owner;
      } else {
        invokedOn = expression.getInvokedOn();
      }

      final boolean viaSuper = expression.invokedViaSuperReference();
      if (viaSuper && method == getTarget()) {
        addInvocation(expression);
      } else
      if (viaSuper
          && ((BinMethodSearchFilter) getFilter()).isImplementationSearch()) {
        BinTypeRef typeRef = getTypeRef();
        if ((invokedOn == typeRef || invokedOn.equals(typeRef))
            || (owner == typeRef || owner.equals(typeRef))) {
          addInvocation(expression);
        }
      } else
      if (this.allowedTypes.contains(invokedOn)) {
        addInvocation(expression);
      }
    }
  }

  /** finds methods which override our target */
  public void visit(final BinMethod method) {
    if (!((BinMethodSearchFilter) getFilter()).isOverrides()) {
      return;
    }

    if (method == getTarget() || method.isPrivate() || method.isStatic()) {
      return; // these are not overridable anyway; self is also not interesting
    }

    if (isAllBranchesScan()) {
      if (this.allowedMethods.contains(method)
          && this.allowedTypes.contains(method.getOwner())) {
        addInvocation(method);
      }
    } else {
      // FIXME: redesign these checks!
      if (!((BinMethod) getTarget()).getName().equals(method.getName())) {
        return;
      }

      // FIXME: that checkMethodsDown is really strange, suspect it's useless...
      if (isIncludeSubtypes() && getSubtypes().contains(method.getOwner())) {
        if (checkMethodsDown(method, getTypeRef()) != null) {
          addInvocation(method);
        }
      } else if (isIncludeSupertypes()
          && getSupertypes().contains(method.getOwner())) {
        if (checkMethodsDown(method, method.getOwner()) != null) {
          addInvocation(method);
        }
      }
    }
  }

  protected final void addInvocation(final BinMethod method) {
    // HACK HERE: nameAST is null for methods, that are not defined abstract in
    // abstract classes, but inherit from interfaces
    // bug # 1374
    if (method.getNameAstOrNull() != null) {
      getSupervisor().addInvocation(
          getTarget(),
          method,
          method.getNameAstOrNull());
    }
  }

  protected final void addInvocation(final BinMethodInvocationExpression x) {
    getSupervisor().addInvocation(
        x.getMember(),
        getSupervisor().getCurrentLocation(),
        x.getNameAst(), x);
  }

  public void visit(final BinStringConcatenationExpression expression) {
    if (!((BinMethodSearchFilter) getFilter()).isUsages() || !this.toString) {
      return;
    }

    final BinExpression leftExpression = expression.getLeftExpression();

    if (this.allowedTypes.contains(leftExpression.getReturnType())) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          leftExpression.getClickableNode(),
          null); // NOTE if we put here left expression, it causes errors in InstanceFinder
    }

    final BinExpression rightExpression = expression.getRightExpression();

    if (this.allowedTypes.contains(rightExpression.getReturnType())) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          rightExpression.getClickableNode(),
          null); // NOTE if we put here right expression, it causes errors in InstanceFinder
    }
  }

  /**
   * Checks if any of methods overriding the given method in subtypes is our
   * target.
   *
   * @param method which is invoked
   * @param compileType is the type on which invocation occured
   *
   * @return true if our target method is invoked as given method
   */
  private BinMethod checkMethodsDown(final BinMethod method,
      final BinTypeRef compileType) {

    // shortcut - private methods are not overridable and doesn't define an interface
    if (((BinMethod) getTarget()).isPrivate() && method != getTarget()) {
      return null;
    }

//    FIXME: seems to be no good test? or it's really that simple???
//    if (/*((BinMethod)getTarget()).getOwner().isDerivedFrom(compileType)
//        &&*/ ((BinMethod)getTarget()).sameSignature(method)
//        && !method.isPrivate()
//        && !((BinMethod) getTarget()).isPrivate()) {
//      List allOverrides = ((BinMethod)getTarget()).allOverridesList();
//      if (allOverrides.contains(method)) {
//        return (BinMethod) getTarget();
//      }
//      List allMethodOverrides = method.allOverridesList();
//      if (allMethodOverrides.retainAll(allOverrides)
//          && allMethodOverrides.size() > 0) {
//        return (BinMethod) getTarget();
//      }

//      return (BinMethod) getTarget();
//    }

    final List methods = compileType.getBinCIType().getSubMethods(method);

    for (int i = 0, max = methods.size(); i < max; i++) {
      final BinMethod checkable = (BinMethod) methods.get(i);
      // FIXME jama? just finds first similar method which doesn't prove anything!
      if (((BinMethod) getTarget()).sameSignature(checkable)) {
        return checkable;
      }
    }

    return null;
  }

  public final void visit(BinCITypesDefStatement x) {
    if (x.getTypeRef().isDerivedFrom(getTypeRef())) {
      fillUpAllowed(); // update our hashes
    }
  }
}
