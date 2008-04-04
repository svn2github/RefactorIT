/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.changesignature.analyzer.RecursiveParameterAddingAnalyzer;
import net.sf.refactorit.refactorings.common.Permutation;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessUtil;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;
import net.sf.refactorit.utils.CommentAllocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Tonis Vaga
 */
public final class MethodSignatureChange {
  List deletedParameters = new ArrayList();

  ArrayList addedParameters = new ArrayList();

  List parameterInfos;

  private BinMethod method;
  private ChangeMethodSignatureRefactoring ref;
  private TransformationList additionalTransformations =
      new TransformationList();
  private List overridesOverridenHierarchy;
  private boolean testRun = false;

  // method original parameters array in original order
  //  private ExistingParameterInfo existingParameters[];

  private String methName;

  private int visibility;

//  private List anonymousConstructors;

  public boolean isFinal = false;
  public boolean isStatic = false;
  public boolean delChecked = false;
  public boolean isReordered = false;

  private Map invocations = new HashMap();
  private BinTypeRef returnType;

  MethodSignatureChange(final BinMethod method, final List overridesOverridden,
      ChangeMethodSignatureRefactoring ref) {
    this.method = method;
    this.ref = ref;
    this.visibility = method.getAccessModifier();

    this.returnType = method.getReturnType();

    this.overridesOverridenHierarchy = overridesOverridden;
    this.methName = method.getName();

    final BinParameter[] pars = method.getParameters();

    // existingParameters = new ExistingParameterInfo[pars.length];
    parameterInfos = new ArrayList(pars.length);

    MultiValueMap map = CommentAllocator.allocateComments(method);

    for (int i = 0; i < pars.length; i++) {
//      existingParameters[i] = new ExistingParameterInfo(pars[i]);
      List li = map.get(pars[i]);
      parameterInfos.add(new ExistingParameterInfo(pars[i], li));
    }

//    if (method instanceof BinConstructor) {
//      anonymousConstructors = findAnonymousConstructors((BinConstructor) method);
//    }

    isFinal = method.isFinal();
    isStatic = method.isStatic();
  }

  public void setTestRun(final boolean testRun) {
    this.testRun = testRun;
  }

  public void reorderParameters(Permutation perm) {
    List result = new ArrayList(parameterInfos.size());

    if (Assert.enabled) {
      Assert.must(perm.size() == parameterInfos.size(),
          "permutaton size was " + perm.size() +
          ", expected " + parameterInfos.size());
    }

    perm = perm.reverse();

    for (int i = 0; i < perm.size(); i++) {
      result.add(parameterInfos.get(perm.getIndex(i)));
    }

    Assert.must(result.containsAll(parameterInfos));

    parameterInfos = result;
  }

  public BinMethod getMethod() {
    return method;
  }

  public void addParameter(final NewParameterInfo parInfo, final int parIndex) {
    parameterInfos.add(parIndex, parInfo);
    addedParameters.add(parInfo);
  }

  /**
   * Delete parameter with index i.
   */
  public void deleteParameter(int parIndex) {
    final Object removed = parameterInfos.remove(parIndex);

    Assert.must(removed != null);

    if (removed instanceof NewParameterInfo) {
      return;
    }

    deletedParameters.add(removed);
  }
BinMethod m;
  public RefactoringStatus checkCanChange() {
    final RefactoringStatus status = new RefactoringStatus();

    BinMethodProcessor deletedProcessor = new BinMethodProcessor() {
      public boolean process(BinMethod method) {
        for (int index = 0; index < deletedParameters.size(); ++index) {
          final int parIndex = ((ExistingParameterInfo)
              deletedParameters.get(index)).getIndex();

          BinParameter par = method.getParameters()[parIndex];

          List invocations = getInvocations(par);

          if (invocations.size() > 0 && !delChecked) {
            status.addEntry("Parameter " + par.getName() + " is used",
                RefactoringStatus.ERROR);
            return false; // only one message per parameter
          }
        }

        return true;
      }
    };

    processAllMethods(deletedProcessor);

    if (!checkStatus(status)) {
      return status;
    }

    for (int index = 0; index < parameterInfos.size(); ++index) {
      ParameterInfo item = (ParameterInfo) parameterInfos.get(index);

      BinTypeRef type = item.getType();
      if (type == null || !type.isResolved()) {
        status.addEntry("Wrong type for parameter " + item.getName(),
            RefactoringStatus.ERROR);
        break;
      }

      if (item.getName() == null || item.getName().length() == 0) {
        status.addEntry("Empty parameter name", RefactoringStatus.ERROR);
      }

      if (item instanceof NewParameterInfo) {
        if (!((NewParameterInfo) item).checkDefaultValue()) {
          status.addEntry("Wrong default value for " + item.getName(),
              RefactoringStatus.ERROR);
        }
      }
    }

    if (!checkStatus(status)) {
      return status;
    }

    // this shall go after checking param correctness
    status.merge(checkParamsForCastability(parameterInfos));

    // TODO rename existing parameters
    BinMethodProcessor addProcessor = new BinMethodProcessor() {
      CanAddParameterChecker checker = new CanAddParameterChecker();

      public boolean process(BinMethod method) {
        for (int j = 0; j < parameterInfos.size(); ++j) {
          if (!(parameterInfos.get(j) instanceof NewParameterInfo)) {
            continue;
          }

          NewParameterInfo info = (NewParameterInfo) parameterInfos.get(j);

          List conflicts = checker.checkNewParameterConflicts(method, info);

          for (int i = 0; i < conflicts.size(); i++) {
            BinVariable conflictWith = (BinVariable) conflicts.get(i);
            status.addEntry("Parameter " + info.getName() +
                " conflicts with variable " + conflictWith.getQualifiedName(),
                RefactoringStatus.ERROR);
          }
        }

        return true;
      }
    };

    if (!checkStatus(status)) {
      return status;
    }

    processAllMethods(addProcessor);

    if (isCloneForHierarchy()) {

      if (testRun) {
        runTestRename(status);
      } else {
        runRename(status);
      }
    }

    return status;
  }

  private void runRename(final RefactoringStatus result) {
    RitDialog.showMessageDialog(ref.getContext(),
        " You should rename current method to\n" +
        "avoid appearance of duplicate methods");

    RefactorItAction rename = ModuleManager.getAction(method.getClass(),
        RenameAction.KEY);

    RefactorItContext new_context = ref.getContext().copy();
    new_context.setState(methName);
    boolean dialogResult = true;

    do {
      additionalTransformations.clear();
      // if testrun - generate sequential names for equally named methods
      dialogResult = ((RenameAction) rename).run(new_context, method,
          additionalTransformations);
      if ((dialogResult)
          && (isCloneForHierarchy(((RenameAction) rename).getNewName()))) {
        RitDialog.showMessageDialog(ref.getContext(),
            "Sorry, Object with such a name already exists. Try once again");
      } else {
        break;
      }
    } while (true);

    if (!dialogResult && !testRun) {
      result.addEntry("Method " + method.getName() +
          " signature change will cause appearance of" +
          " duplicate methods",
          RefactoringStatus.ERROR);

    } else {
      methName = ((RenameAction) rename).getNewName();
    }
  }

  private void runTestRename(final RefactoringStatus result) {
    int testRenameDefiner = 1;
    do {
      additionalTransformations.clear();

      RenameMethod renRef = new RenameMethod(ref.getContext(), method);
      String newMethName = method.getName() + testRenameDefiner;

      renRef.setNewName(newMethName);
      renRef.setRenameInJavadocs(true);

      additionalTransformations.merge(renRef.performChange());
      if (renRef.getStatus().isOk() && !isCloneForHierarchy(newMethName)) {
        methName = newMethName;
        break;
      } else {
        testRenameDefiner++;
      }
    } while (true);
  }

  /**
   * Checks, wheather new parameters types are castable from the old ones
   * @param paramInfos
   */
  private RefactoringStatus checkParamsForCastability(List paramInfos) {
    RefactoringStatus status = new RefactoringStatus();
    StringBuffer errorString = new StringBuffer();
    for (int index = 0; index < paramInfos.size(); ++index) {
      ParameterInfo item = (ParameterInfo) paramInfos.get(index);

      if (item instanceof ExistingParameterInfo) {
        ExistingParameterInfo existingItem = (ExistingParameterInfo) item;
        BinTypeRef newType = existingItem.getType();
        BinTypeRef oldType = existingItem.getOriginalParameter().getTypeRef();

        boolean allowed = TypeConversionRules.isMethodInvocationConversion(
            oldType, newType);
        if (!allowed) {
          errorString.append("\"");
          errorString.append(existingItem.getName());
          errorString.append("\"");
          errorString.append(" parameter type: ");
          errorString.append(oldType.getQualifiedName());
          errorString.append(" -> ");
          errorString.append(newType.getQualifiedName());
          errorString.append("\n");
        }
      }
    }
    if (errorString.length() > 0) {
      String error = "Following types will not cast implicitly: \n\n"
          + errorString.toString()
          +
          "\nThis may cause a not compilable code.\n Do you want to continue?";
      status.addEntry(error, RefactoringStatus.QUESTION);
    }

    return status;
  }

  /**
   * @param status
   */
  private boolean checkStatus(final RefactoringStatus status) {
    return!(status.isCancel() || status.isErrorOrFatal());
  }

  class CanAddParameterChecker {
    private final List varNameHasConflicts(
        final BinMethod where, final String newName) {

      LocalVariableDuplicatesFinder duplicateFinder =
          new LocalVariableDuplicatesFinder(null, newName, where);
      where.accept(duplicateFinder);

      return duplicateFinder.getDuplicates();
    }

    List checkNewParameterConflicts(BinMethod method,
        NewParameterInfo newParameter) {
      String name = newParameter.getName();

      List result = new ArrayList(1);

      BinParameter[] pars = method.getParameters();
      for (int i = 0; i < pars.length; i++) {
        if (!deletedParameters.contains(new ExistingParameterInfo(pars[i]))) {
          if (pars[i].getName().equals(name)) {
            result.add(pars[i]);
            return result;
          }
        }
      }

      result = varNameHasConflicts(method, name);

      for (Iterator iter = result.iterator(); iter.hasNext(); ) {
        BinVariable var = (BinVariable) iter.next();

        if (var instanceof BinParameter) {
          if (deletedParameters.contains(new ExistingParameterInfo((
              BinParameter) var))) {
            iter.remove();
          }
        }
      }

      return result;
    }
  }


  public boolean isCloneForHierarchy() {
    return isCloneForHierarchy(methName);
  }

  public boolean isCloneForHierarchy(String name) {
    List hierarchy = method.findAllOverridesOverriddenInHierarchy();

    BinTypeRef[] params = new BinTypeRef[parameterInfos.size()];
    BinMethod tmpMeth;

    for (int i = 0; i < params.length; i++) {
      params[i] = ((ParameterInfo) parameterInfos.get(i)).getType();
    }

    hierarchy.add(method);

    for (int i = 0; i < hierarchy.size(); i++) {
      tmpMeth = ((BinMethod) hierarchy.get(i)).getOwner().getBinCIType().
          getDeclaredMethod(name, params);
      if ((tmpMeth != null) && (!hierarchy.contains(tmpMeth))) {
        return true;
      }
    }
    return false;
  }

  private void processAllMethods(BinMethodProcessor processor) {
    if (!processor.process(method)) {
      return;
    }

    for (int index = 0; index < overridesOverridenHierarchy.size(); ++index) {
      final BinMethod binMethod = (BinMethod) overridesOverridenHierarchy.get(
          index);

      if (!processor.process(binMethod)) {
        break;
      }
    }

//    if (method instanceof BinConstructor && anonymousConstructors != null) {
//      for (int index = 0; index < anonymousConstructors.size(); ++index) {
//        BinConstructor item = (BinConstructor) anonymousConstructors.get(index);
//      }
//    }
  }

  public TransformationList edit() {
    final TransformationList transList = new TransformationList();
    final ImportManager importManager = new ImportManager();

    // it is important to call processRecurseAddingParamImplementation(); first
    // when is used recuresive parameter adding
    transList.merge(processRecurseAddingParamImplementation());

    BinMethodProcessor usagesEditor = new BinMethodProcessor() {
      public boolean process(BinMethod method) {

        // must be sure we edit all invocations only once!
        List invocations = Finder.getInvocations(method.getProject(), method,
            new BinMethodSearchFilter(true, false,
            false, false, true, false, false, true, false));

        for (int i = 0; i < invocations.size(); i++) {
          if (ChangeMethodSignatureRefactoring.debug) {
            System.out.println("invocation == " + invocations.get(i));
          }

          Object o = ((InvocationData) invocations.get(i)).getInConstruct();
          if (o == null) {
            if (ChangeMethodSignatureRefactoring.debug) {
              System.out.println("skipping invocation data: "
                  + invocations.get(i));
            }
            continue;
          }

          if (!(o instanceof MethodOrConstructorInvocationExpression)) {
            Assert.must(false,
                "construct not method invocation " + o.getClass() + " :" + o);
          }

          MethodInvocationEditor invEditor =
              new MethodInvocationEditor(MethodSignatureChange.this,
              (MethodOrConstructorInvocationExpression) o);
          invEditor.doEdit(transList);
        }

        return true;
      }
    };

    processAllMethods(usagesEditor);

    BinMethodProcessor declEditor = new BinMethodProcessor() {
      public boolean process(BinMethod method) {
        new MethodDeclarationEditor(method, MethodSignatureChange.this)
            .doEdit(transList, importManager);
        return true;
      }
    };

    processAllMethods(declEditor);
    importManager.createEditors(transList);

    if (additionalTransformations.getTransformationsCount() > 0) {
      transList.merge(additionalTransformations);
    }

    return transList;
  }

//  public NewParameterInfo getNewParameter(int i) {
//    return (NewParameterInfo) newParameters.get(i);
//  }

  /**
   * Parameters sequence according to this change.
   *
   * @return iterator where elements are either BinParameters
   *     or ParameterInfo's
   */
  public List getParametersList(BinMethod targetMethod) {
    Assert.must(method == targetMethod ||
        overridesOverridenHierarchy.contains(targetMethod));

    List result = new ArrayList();

    for (int index = 0; index < parameterInfos.size(); ++index) {
      ParameterInfo item = (ParameterInfo) parameterInfos.get(index);

      if (item instanceof ExistingParameterInfo) {
        item = cloneForMethod((ExistingParameterInfo) item, targetMethod);
      }

      result.add(item);
    }

    return result;
  }

  /**
   * Clone ParmeterInfo for method in hierarchy (assuring valid names)
   *
   * @param parameterInfo
   * @param method
   * precond: method is in same hierarchy
   */
  private static ExistingParameterInfo cloneForMethod(
      ExistingParameterInfo parameterInfo, BinMethod method
      ) {
//    if (parameterInfo.getOriginalParameter().getMethod() == method) {
//      return parameterInfo;
//    }
//
//    BinParameter par = parameterInfo.getOriginalParameter();
//    par = method.getParameters()[par.getIndex()];
//    ExistingParameterInfo result = new ExistingParameterInfo(parameterInfo, method);

    return parameterInfo.cloneFor(method);
  }

  public void deleteParameters(int[] deleted) {
    Arrays.sort(deleted);

    for (int i = 0; i < deleted.length; i++) {
      deleteParameter(deleted[i] - i); // index is decreasing
    }
  }

  public List getParametersList() {
    return getParametersList(method);
  }

  public boolean isHierarchyMethod(BinMethod member) {
    return member == this.method || overridesOverridenHierarchy.contains(member);
  }

  public boolean isOverridesOverriden() {
//System.err.println("member: " + member);
//System.err.println("overridesOverridenHierarchy: " + overridesOverridenHierarchy);
    return overridesOverridenHierarchy.size() > 0;
  }

  /** @return false when failed */
  public boolean setAccessModifier(int newVisibility) {
    this.visibility = newVisibility;

    return true;
  }

  public BinTypeRef getReturnType() {
    return this.returnType;
  }

  public void setReturnType(BinTypeRef returnType) {
    this.returnType = returnType;
  }

  /**
   * @return composition of possible modifiers
   */
  public int[] getPossibleModifiers() {
    int[] accessRights = MinimizeAccessUtil.findAccessRights(
        this.method.getOwner().getBinCIType(),
        Finder.getInvocations(this.method,
        new BinMethodSearchFilter(true, true, true, true, true, false, false, false, false)));

    return accessRights;
  }

  public int getAccessModifier() {
    return this.visibility;
  }

//  private static List findAnonymousConstructors(BinConstructor method) {
//    List result = new ArrayList();
//
//    method.getProject().discoverAllUsedTypes();
//    List subclasses = method.getOwner().getAllSubclasses();
//    System.out.println("found sublcasses" + subclasses);
//
//    for (Iterator iter = subclasses.iterator(); iter.hasNext(); ) {
//      BinTypeRef item = (BinTypeRef)iter.next();
//
//      if (item.getBinCIType().isAnonymous()) {
//        final BinConstructor[] ctrs = ((BinClass) item.getBinCIType()).getConstructors();
//        System.out.println("addding constructors");
//
//        Assert.must(ctrs.length == 1);
//        CollectionUtil.addAll(result, ctrs);
//      }
//    }
//    System.out.println("found anonymousConstructors " + result);
//    return result;
//  }

  public boolean isChangingToWeakerAccess() {
    return BinModifier.compareAccesses(
        this.visibility, method.getAccessModifier()) == -1;
  }

  public void renamePrameter(int parIndex, String str) {
    getParameterInfo(parIndex).changeName(str);
  }

  public ParameterInfo getParameterInfo(final int parIndex) {
    return (ParameterInfo) parameterInfos.get(parIndex);
  }

  public void changeParameterType(int index, BinTypeRef newType) {
    getParameterInfo(index).changeType(newType);
  }

  public List getInvocations(BinParameter par) {
    List result = (List) invocations.get(par);

    if (result == null) {
      result = Collections.unmodifiableList(Finder.getInvocations(par));
      invocations.put(par, result);
    }

    return result;
  }

  public int getParametersCount() {
    return parameterInfos.size();
  }

  public String canBeFinal() {

    if (this.method.getOwner().getBinCIType().isInterface()) {
      return "Can not change to final, because it is an interface`s method";
    }

    int overridesInSubClasses
        = method.findAllOverridesOverriddenInHierarchy().size()
        - method.findAllOverrides().size();

    if (overridesInSubClasses > 0) {
      return "Can not change to final, because the method has overrides";
    }
    return null;
  }

  public String canBeStatic() {

    if (method.findAllOverrides().size() > 0) {
      return "Can`t change to static because the"
          + " method overrides a non-static method";
    }

    if (method.findAllOverridesOverriddenInHierarchy().size() > 0) {
      return "Can`t change to static because the method has overrides";
    }

    BinMemberInvocationVisitor visitor = new BinMemberInvocationVisitor();
    method.accept(visitor);

    for (int x = 0; x < visitor.getBinFieldList().size(); x++) {
      BinField temp;
      temp = (BinField) visitor.getBinFieldList().get(x);
      if (!temp.isStatic()) {
        if (temp.getOwner().equals(method.getOwner())) {
          return
              "Can`t change to static because the method uses non-static fields";
        }
      }
    }

    for (int x = 0; x < visitor.getBinMethodList().size(); x++) {
      BinMethod temp;
      temp = (BinMethod) visitor.getBinMethodList().get(x);
      if (!temp.isStatic()) {
        if (temp.getOwner().equals(method.getOwner())) {
          return "Can`t change to static "
              + "because the method calls non-static methods";
        }
      }
    }

    return null;
  }

  class BinMemberInvocationVisitor extends BinItemVisitor {
    private List binMethodList = new ArrayList();
    private List binFieldList = new ArrayList();

    public void visit(BinMethodInvocationExpression x) {
      binMethodList.add(x.getMethod());
      super.visit(x);
    }

    public void visit(BinFieldInvocationExpression x) {
      binFieldList.add(x.getField());
      super.visit(x);
    }

    public List getBinFieldList() {
      return this.binFieldList;
    }

    public List getBinMethodList() {
      return this.binMethodList;
    }
  }


  public List getOverridesOverridenHierarchy() {
    return this.overridesOverridenHierarchy;
  }

  private TransformationList processRecurseAddingParamImplementation() {
    RecursiveParameterAddingAnalyzer analyzer = new
        RecursiveParameterAddingAnalyzer(this);
    return analyzer.getTransList();
  }

  public List getDeletedParameters() {
    return this.deletedParameters;
  }

  public ArrayList getAddedParameters() {
    return this.addedParameters;
  }
}
