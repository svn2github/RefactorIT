/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.notused;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.source.UserFriendlyError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * @author Anton Safonov
 * @author Sander M\u00E4gi
 * @author Alexander Klyubin
 * @author Tanel Alum\u00E4e
 * @author Risto Alas
 * @author Vadim Hahhulin
 */
public final class NotUsedIndexer extends AbstractIndexer {
  private static final ProgressMonitor.Progress FIND_EXISTING =
      new ProgressMonitor.Progress(0, 25);
  private static final ProgressMonitor.Progress REMOVE_USED =
      new ProgressMonitor.Progress(25, 100);

  private HashSet notUsedTypes;
  private HashSet notUsedFields;
  private HashSet notUsedMethods;
  private MultiValueMap fieldAssignments = new MultiValueMap();

  private final ExcludeFilterRule[] filterRules;

//  /** time measurement */
//  private long lastTime;

  private final TypeRefVisitor typeRefVisitor = new TypeRefVisitor();

  private class TypeRefVisitor extends BinTypeRefVisitor {
    private BinTypeRef curType;

    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(false);
      setIncludeNewExpressions(true);
    }

    public void setCurType(BinTypeRef typeRef) {
      this.curType = typeRef;
    }

    public void visit(final BinTypeRef typeRef) {
      final BinTypeRef type = typeRef.getTypeRef();

      if (type == null) { // strange imports occur sometimes
        super.visit(typeRef);
        return;
      }
      if (type.isPrimitiveType()) {
        super.visit(typeRef);
        return;
      }

      BinTypeRef currentType = this.curType;
      if (currentType == null) {
        currentType = getCurrentType();
      }

      removeType(type, currentType);

      super.visit(typeRef);
    }
  }


  public NotUsedIndexer() {
    this(ExcludeFilterRule.getDefaultRules());
  }

  public NotUsedIndexer(ExcludeFilterRule[] filterRules) {
    this.filterRules = filterRules;
  }

//  private final void printTimeElapsed(final String message) {
//    final long curTime = System.currentTimeMillis();
//    if (lastTime > 0) {
//      System.err.println(message + ": " + (curTime - lastTime) + "ms");
//    }
//    lastTime = curTime;
//  }

  public void visit(final Project project) {

//    printTimeElapsed(null); // init
//    long startTime = lastTime;

    findExistingItems(project);

//    printTimeElapsed("  Collected existing items");

    // remove "used" entities
    AbstractIndexer.runWithProgress(REMOVE_USED, new Runnable() {
      public void run() {
        NotUsedIndexer.super.visit(project);
      }
    });

//    printTimeElapsed("  Visited project");

    removeRuleBasedMembers();
    removeMembersOfNotUsedTypes();

//    printTimeElapsed("  Removed marked and hidded");
//    lastTime = startTime; // to check the whole thing
//    printTimeElapsed("Completed NotUsed in");
  }

  private void removeRuleBasedMembers() {
    final Iterator types = this.notUsedTypes.iterator();
    while (types.hasNext()) {
      BinTypeRef type = (BinTypeRef) types.next();
      if (isToBeExcludedByFilter(type.getBinType())) {
        types.remove();
        removeAllOwners(type.getBinType());
      }
    }

    final Iterator fields = this.notUsedFields.iterator();
    while (fields.hasNext()) {
      BinField field = (BinField) fields.next();
      if (isToBeExcludedByFilter(field)) {
        fields.remove();
        removeAllOwners(field);
      }
    }

    final Iterator methods = this.notUsedMethods.iterator();
    while (methods.hasNext()) {
      BinMethod method = (BinMethod) methods.next();
      if (isToBeExcludedByFilter(method)) {
        methods.remove();
        removeAllOwners(method);
      }
    }

  }

  private void removeAllOwners(BinMember member) {
    BinTypeRef owner = member.getOwner();
    while (owner != null) {
      notUsedTypes.remove(owner);
      owner = owner.getBinCIType().getOwner();
    }
  }

  private void removeMembersOfNotUsedTypes() {
    final Iterator types = this.notUsedTypes.iterator();
    while (types.hasNext()) {
      final BinTypeRef typeRef = (BinTypeRef) types.next();
      removeMembers(typeRef);
    }
  }

  public void clear() {
    notUsedMethods.clear();
    notUsedMethods = null;
    notUsedFields.clear();
    notUsedFields = null;
    notUsedTypes.clear();
    notUsedTypes = null;
  }

  private void findExistingItems(Project project) {
    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

// seems it doesn't help much to discover more not used, but slows down a lot
//    project.discoverAllUsedTypes();

    final List allTypes = project.getDefinedTypes();

    int typesNum = allTypes.size();
    notUsedTypes = new HashSet(typesNum);
    notUsedFields = new HashSet(typesNum * 3);
    notUsedMethods = new HashSet(typesNum * 3);

    for (int iType = 0; iType < typesNum; iType++) {
      final BinTypeRef type = (BinTypeRef) allTypes.get(iType);
      notUsedTypes.add(type);

      final BinField[] fields = type.getBinCIType().getDeclaredFields();
      for (int iField = 0; iField < fields.length; iField++) {
        final BinField field = fields[iField];
        notUsedFields.add(field);
      }

      BinMethod[] methods = type.getBinCIType().getDeclaredMethods();
      for (int iMethod = 0; iMethod < methods.length; iMethod++) {
        final BinMethod method = methods[iMethod];
        if (!method.isMain() && !method.isSynthetic()) {
          notUsedMethods.add(method);
        } else if(method.isMain()) {
          notUsedTypes.remove(type);
        }
      }

      if (type.getBinCIType().isClass()
          || type.getBinCIType().isEnum()) {
        BinConstructor[] cnstrs = ((BinClass) type.getBinCIType()).getConstructors();
        for (int i = 0, max = cnstrs.length; i < max; i++) {
          final BinConstructor constructor = cnstrs[i];
          if (!constructor.isSynthetic()) {
            notUsedMethods.add(constructor);
          }
        }
      }

      if (listener != null) {
        listener.progressHappened(
            FIND_EXISTING.getPercentage(iType, typesNum));
      }

    }
  }

  private boolean isToBeExcludedByFilter(BinMember member) {
    if (filterRules != null) {
      for (int i = 0; i < filterRules.length; i++) {
        if (filterRules[i].isToBeExcluded(member)) {
          return true;
        }
      }
    }
    return false;
  }

  public void visit(CompilationUnit x) {
    x.accept(typeRefVisitor); // imports

    super.visit(x);
  }

  public void visit(BinCIType x) {
    typeRefVisitor.setCurType(x.getTypeRef());
    x.accept(typeRefVisitor);
    typeRefVisitor.setCurType(null);

    super.visit(x);
  }

  public void visit(BinConstructor x) {
    // check for implicit call of default super constructor
    if (!x.getProject().getObjectRef().equals(x.getOwner())
        && !x.hasExplicitConstructorInvocation()) {
      BinClass superClass
          = (BinClass) x.getOwner().getSuperclass().getBinCIType();
      BinConstructor superCnstr = superClass.getDefaultConstructor();
      if (superCnstr == null) {
        if (Assert.enabled) {
          (x.getProject().getProjectLoader().getErrorCollector()).addNonCriticalUserFriendlyError(new UserFriendlyError(
                    "No 0-argument constructor in supertype for: " + x,
                    x.getCompilationUnit(), x.getNameAstOrNull()));
        }
      } else {
        removeMethod(superCnstr);
      }
    }

    super.visit(x);
  }

  public void visit(BinField x) {
    x.accept(typeRefVisitor);
    super.visit(x);
  }

  public void visit(BinFieldInvocationExpression x) {
    BinField f = x.getField();
    // RIM-786 Improvement
    boolean remove = true;
    if(x.getParent() instanceof BinAssignmentExpression) {
      BinAssignmentExpression expr = (BinAssignmentExpression)x.getParent();
      BinExpression leftExpr = expr.getLeftExpression();
      BinExpression rightExpr = expr.getRightExpression();

      if(leftExpr.equals(x) && !isUsed(expr)) {
        if(!fieldAssignments.contains(f, expr)) {
          fieldAssignments.put(f, expr);
        }
        remove = false;
      } else {
        fieldAssignments.clearKey(f);
      }
    }
    if(remove) {
      notUsedFields.remove(f);
    }
    super.visit(x);
  }

  public void visit(BinMethod x) {
    x.accept(typeRefVisitor);
    super.visit(x);
  }

  public void visit(BinConstructorInvocationExpression x) {
    removeMethod(x.getConstructor());
    super.visit(x);
  }

  public void visit(BinMethodInvocationExpression x) {
    final BinMethod method = x.getMethod();

    if (x.isOutsideMemberInvocation()) {
      // removing outside call
      removeType(x.getInvokedOn());
      removeMethodAndDeps(method, x.getInvokedOn());
    } else {
      if (x.invokedViaSuperReference()) {
        // removing "super" call
        removeMethod(method);
      } else {
        // "removing "this" call
        removeMethodAndDeps(method, getCurrentType());
      }
    }

    super.visit(x);
  }

//  private List getDirectOverrides(final BinMethod method) {
//// doesn't give anything, but wastes RAM significantly
////    List overs = (List) overrides.get(method);
////    if (overs == null) {
////      overs = method.overridesList();
////      overrides.put(method, overs);
////    }
////
////    return overs;
//
//    return method.overridesList();
//  }

  private void removeMethodAndDeps(BinMethod method, BinTypeRef startFrom) {
    removeMethod(method);

    // remove sub methods
    final List submethods = startFrom.getBinCIType().getSubMethods(method);
    for (int i = 0, max = submethods.size(); i < max; i++) {
      removeMethod((BinMethod) submethods.get(i));
    }

    List dependantsFromFilters = getDependantsFromFilters(method);
    if (dependantsFromFilters.size() > 0) {
      Iterator iter = dependantsFromFilters.iterator();
      while (iter.hasNext()) {
        BinMethod depMethod = (BinMethod) iter.next();
        removeMethod(depMethod);
      }
    }

    /*
    for (Iterator it = method.findAllOverrides()
        .iterator(); it.hasNext();) {
      BinMethod meth = (BinMethod) it.next();
      removeMethod(meth);
    }*/

    // remove super methods
    /*
         if (filter.isNotUsedAsInterface()) {
      removeOverrides(method);
         }
    */
  }

  private List getDependantsFromFilters(BinMethod method) {
    List result = new ArrayList();
    if (filterRules != null) {
      for (int i = 0; i < filterRules.length; i++) {
        result.addAll(filterRules[i].getCustomDependants(method));
      }
    }
    return result;

  }

  public void visit(BinLocalVariable x) {
    x.accept(typeRefVisitor);

    super.visit(x);
  }

  public void visit(BinNewExpression x) {
    x.accept(typeRefVisitor);
    BinConstructor constructor = x.getConstructor();
    if (constructor != null) {
      removeMethod(constructor);
    }

    super.visit(x);
  }

  public void visit(BinCastExpression x) {
    x.accept(typeRefVisitor);

    super.visit(x);
  }

  private void removeType(final BinTypeRef type) {
    removeType(type, getCurrentType());
  }

  private void removeType(final BinTypeRef type, final BinTypeRef curType) {
    if (!invokesSelf(type, curType)) {
      notUsedTypes.remove(type);
    } else {
      // TODO: here is an exclusion for main method since it is usually called
      // from outside of app and always potentially used
      // should be done in a generic way to exclude e.g. unit tests also etc.
      final BinItem location = getCurrentLocation();
      if (location instanceof BinMethod
          && ((BinMethod) location).isMain()) {
        notUsedTypes.remove(type);
      }
    }
  }

  private void removeMethod(final BinMethod method) {
    if (!invokesSelf(method)) {
      if (Assert.enabled) {
        Assert.must(method.getOwner() != null,
            "null owner for method: " + method + " invoked at: "
            + getCurrentType() + ", " + getCurrentLocation());
      }
      removeType(method.getOwner());
      //List overrides = method.findAllOverrides();
      notUsedMethods.remove(method);
      //notUsedMethods.removeAll(overrides);
    }
  }

  private boolean invokesSelf(BinMethod method) {
    BinItem location = getCurrentLocation();
    if (Assert.enabled) {
      Assert.must(location instanceof LocationAware, "Location is not LA: "
          + location.getClass() + " - " + location);
    }

    if (method instanceof BinConstructor && method == location) {
      // almost never happens that constructor calls itself explicitly,
      // but field initialization occurs in constructor actually, so:
      // private Test instance = new Test();
      // looks like constructor calls itself, which is wrong
      return false;
    }

    if (method == location) {
      return true; // shortcut
    }

    return method.contains((LocationAware) location);
  }

  private boolean invokesSelf(final BinTypeRef type,
      final BinTypeRef curType) {

    if (curType != null) {
      if (curType.equals(type)) {
        return true; // shortcut
      }

      if (Assert.enabled) {
        Assert.must(type != null, "type given is null");
        Assert.must(type.getBinCIType() != null,
            "type.getBinCIType() is null for: " + type);
      }
      return type.getBinCIType().contains((LocationAware) curType.getBinCIType());
    } else {
//      if (Assert.enabled) {
//        new Exception("curType was null :( for " + type).printStackTrace();
//      }
      return false;
    }
  }

  public void visit(BinCITypeExpression x) {
    x.accept(typeRefVisitor);

    super.visit(x);
  }

  public final Set getNotUsedTypes() {
    return notUsedTypes;
  }

  public final List getNotUsedSingleMembers() {
    final List members = new ArrayList(notUsedFields.size()
        + notUsedMethods.size());
    members.addAll(getNotUsedFields());
    members.addAll(getNotUsedMethods());

    return members;
  }

  /** For tests only! */
  public final Set getNotUsedFields() {
    return notUsedFields;
  }

  /** For tests only! */
  public final Set getNotUsedMethods() {
    return notUsedMethods;
  }

  private void removeMembers(BinTypeRef typeRef) {
    final BinField[] fields = typeRef.getBinCIType().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      this.notUsedFields.remove(fields[i]);
    }

    final BinMethod[] methods = typeRef.getBinCIType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      this.notUsedMethods.remove(methods[i]);
    }

    if (typeRef.getBinCIType().isClass()
        || typeRef.getBinCIType().isEnum()) {
      final BinMethod[] cnstrs
          = ((BinClass) typeRef.getBinCIType()).getDeclaredConstructors();
      for (int i = 0; i < cnstrs.length; i++) {
        this.notUsedMethods.remove(cnstrs[i]);
      }
    }

//    BinTypeRef[] inners = typeRef.getBinCIType().getDeclaredTypes();
//    for (int i = 0; i < inners.length; i++) {
//      removeMembers(inners[i]);
//    }
  }

  public MultiValueMap getFieldAssignments() {
    return fieldAssignments;
  }

  public boolean isUsed(BinAssignmentExpression expr) {
    BinItemVisitable parent = expr.getParent();
    while(!(parent instanceof BinCIType)) {
      if(parent instanceof BinField ||
          parent instanceof BinAssignmentExpression) {
        return true;
      }
      parent = parent.getParent();
    }

    final class ChangesAnalyzer extends AbstractIndexer {
      public boolean isChanging = false;

      public final void visit(BinMethodInvocationExpression x) {
        this.isChanging = true;
      }

      public final void visit(BinIncDecExpression x) {
        this.isChanging = true;
      }
    };
    ChangesAnalyzer visitor = new ChangesAnalyzer();
    expr.getRightExpression().accept(visitor);

    return visitor.isChanging;
  }
}
