/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.AdaptiveMultiValueMap;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.conflicts.resolution.AddImplementationResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.ChangeAccessResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.CreateAbstractDeclarationResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.CreateDeclarationResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.CreateDefinitionResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MakeStaticResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MoveMemberResolution;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessUtil;
import net.sf.refactorit.refactorings.movemember.InstanceFinder;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 *
 * @author vadim
 */
public abstract class ConflictResolver {
  private List selectedMembers;
  BinCIType targetType;
  private BinCIType nativeType;
  private HashMap conflictDataMap = new HashMap();

  protected ImportManager importManager = new ImportManager();

  private AdaptiveMultiValueMap usesMap = new AdaptiveMultiValueMap();
  private AdaptiveMultiValueMap usedByMap = new AdaptiveMultiValueMap();
  private AdaptiveMultiValueMap usesThroughReference = new
      AdaptiveMultiValueMap();
  private AdaptiveMultiValueMap usedByThroughReference = new
      AdaptiveMultiValueMap();
  private AdaptiveMultiValueMap usedByThroughComplexExpression = new
      AdaptiveMultiValueMap();

  private ProgressMonitor.Progress progressArea;

  ConflictRepository rep = new ConflictRepository();

  public void clearConflictRepository() {
    rep.clear();
  }

//  private ConflictResolver() {
//    this(null, null, null);
//  }
//
//  private ConflictResolver(List selectedMembers) {
//    this(selectedMembers, null, null);
//  }

  protected ConflictResolver(List selectedMembers, BinCIType targetType,
      BinCIType nativeType) {
    this.nativeType = nativeType;
    this.selectedMembers = selectedMembers;
//    ConflictFactory.clear();

    setTargetType(targetType);
  }

  public abstract void runConflictsResolver(BinMember selectedMember,
      boolean isSelected);

  public void resolveConflicts() {
    int oldSize;

//    System.out.println("[tonisdebug]: conflicts before resolving :" +
//                       ConflictFactory.getConflicts());
//    System.out.println("[tonisdebug]: conflictData " + getAllConflictData());

    do {
      oldSize = getBinMembersToMove().size();
      resolveConflicts(getAllConflictData());
    } while (oldSize != getBinMembersToMove().size());
//    System.out.println("[tonisdebug]: conflicts after resolving :" +
//                       ConflictFactory.getConflicts());
//    System.out.println("[tonisdebug]: conflictData "+getAllConflictData());

  }

  protected void resolveConflicts(List conflictData) {
    for (int i = 0, max = conflictData.size(); i < max; i++) {
      ConflictData data = (ConflictData) conflictData.get(i);

      if (data.isSelectedToMove() || isMovedWithOwner(data.getMember())) {
        findConflicts(data);
      }
    }
  }

  private void findConflicts(ConflictData data) {
    data.clearConflicts();

    if (targetType == nativeType) {
      return;
    }

    if (data.isSelectedByUser()) {
      checkIfFieldIsMoved(data);
    }

    checkIfMethodOverridesOrOverriden(data);

    //<hack>
    if (targetType != null && targetType.getCompilationUnit() == null) {
      BinMember member = data.getMember();
      Conflict conflict = new UnresolvableConflict(this,
          ConflictType.METHD_TO_FOREIGN_TARGET,
          member, new ArrayList());
      rep.addConflict(conflict, member, targetType);
      data.addConflict(conflict);
    } else
    //</hack>

    if (targetType != null) {
      checkIfMemberWithSameSignatureAlreadyExists(data);
      checkIfMainMethodMovesIntoRightClass(data);

//      if (nativeType.isInterface()) {
//        checkMovingFromInterface(data);
//      }
//
//
//      if (targetType.isInterface()) {
//        checkIfPossibleToMoveMemberIntoInterface(data);
//      } else if (targetType.isAbstract()) {
//        checkIfDeclarationOrDefinitionIsNecessary(data);
//      }
      checkIfDeclarationOrDefinitionIsNecessary(data);
      checkIfImplementationIsNecessaryForPullDown(data);

      if (isDefinitionOfMemberMoves(data)) {
        if (nativeType.isLocal()) {
          checkIfUsesLocalVariablesOfOwner(data);
        }

        checkIfMustBeStatic(data);
        checkIfDRConflictIsNecessaryForUsedBy(data);
        checkIfDRConflictIsNecessaryForUses(data);

        checkImports(data);
        if (nativeType.getTypeRef().isDerivedFrom(nativeType.getTypeRef())) {
          checkOtherSubclasses(data);
        }
      }
    }
//    data.clearConflicts();
//    data.addConflicts(rep.getConflicts(data.getMember()));
  }

  /**
   * @param data
   */
  private void checkOtherSubclasses(ConflictData data) {
    BinMember member = data.getMember();
    Conflict conflict = rep.getConflict(member,
        ConflictType.DELETE_IMPLEMENTATIONS_IN_SUBCLASSES);

    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    List otherImplementers = new ArrayList();
    for (Iterator i = targetType.getTypeRef().getAllSubclasses().iterator(); i
        .hasNext();) {
      BinCITypeRef subType = (BinCITypeRef) i.next();
      if (subType != nativeType.getTypeRef()) {
        BinMember parallelMember = subType.getBinCIType()
            .hasMemberWithSignature(member);
        if (parallelMember != null) {
          // other subclass of target superclass has also such member
          CollectionUtil.addNew(otherImplementers, parallelMember);
        }
      }
    }
    if (otherImplementers.size() > 0) {
	    conflict = new DeleteOtherImplementersConflict(this, member,
	        otherImplementers);

	    data.addConflict(conflict);
	    rep.addConflict(conflict, member);
    }

  }

  private void showProgress(int i, int size) {
    if (progressArea == null) {
      progressArea = new ProgressMonitor.Progress(0, 100);
    }

    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    if (listener != null) {
      listener.progressHappened(progressArea.getPercentage(i, size));
    }
  }


  private void clearAllConflicts() {
    List list = getAllConflictData();

    for (int i = 0, max = list.size(); i < max; i++) {
      ConflictData data = (ConflictData) list.get(i);
      showProgress(i, 100);
      if (data.getConflicts().size() == 0) {
        continue;
      }

      data.clearConflicts();

      List usages = new ArrayList();
      usages.addAll(data.getUsesList());
      usages.addAll(data.getUsedByList());
      for (int j = 0, maxJ = usages.size(); j < maxJ; j++) {
        ConflictData usageData = getConflictData(usages.get(j));
        if (usageData != null) {
          usageData.clearConflicts();
        }
      }
    }
  }

  private void checkIfUsesLocalVariablesOfOwner(ConflictData data) {
    BinMember member = data.getMember();

    class Visitor extends AbstractIndexer {
      public List localVarsOfOwner = new ArrayList();
      public void visit(BinVariableUseExpression x) {
        if (!(x.getVariable() instanceof BinParameter)) {
          localVarsOfOwner.add(x.getVariable());
        }
      }
    }


    Visitor visitor = new Visitor();
    member.accept(visitor);
    if ((visitor.localVarsOfOwner.size() > 0) &&
        !targetType.getTypeRef().isDerivedFrom(nativeType.getTypeRef())) {

      Conflict conflict = new UnresolvableConflict(this,
          ConflictType.USES_FOREIGN_LOCAL_VARIABLES,
          member, visitor.localVarsOfOwner);

      data.addConflict(conflict);
      rep.addConflict(conflict, member, targetType);
    }
  }

  private void checkIfMustBeStatic(ConflictData data) {
    BinMember member = data.getMember();
//    Conflict conflict = ConflictFactory.getConflict(member,
//        ConflictType.MAKE_STATIC);
    Conflict conflict = rep.getConflict(member, ConflictType.MAKE_STATIC);

    if (conflict != null) {
      data.addConflict(conflict);
    }
  }

  public void makeStaticSinceUsedByStatic(ConflictData data) {
    BinMember member = data.getMember();
//    Conflict conflict = ConflictFactory.getConflict(member,
//        ConflictType.MAKE_STATIC);
    Conflict conflict = rep.getConflict(member, ConflictType.MAKE_STATIC);

    if (conflict == null) {
      conflict = new MakeStaticConflict(this, member, data.getUsedByList());

//      ConflictFactory.addConflict(member, conflict.getType(), conflict);
      rep.addConflict(conflict, member);

    }

    if (conflict != null) {
      data.addConflict(conflict);
    }
  }

  private void checkIfFieldIsMoved(ConflictData data) {
    BinMember member = data.getMember();
    if (!(member instanceof BinField) || member.isStatic()) {
      return;
    }

//    Conflict conflict = ConflictFactory.getConflict(member,
//        ConflictType.CHANGED_FUNCTIONALITY);
    Conflict conflict = rep.getConflict(member,
        ConflictType.CHANGED_FUNCTIONALITY);

    if (conflict != null) {
      if (((ChangedFunctionalityConflict) conflict).isMoveField()) {
        data.addConflict(conflict);
        return;
      } else {
//        ConflictFactory.removeConflict(member,
//                                       ConflictType.CHANGED_FUNCTIONALITY);
        data.addConflict(conflict);
        return;
      }
    }

    BinField field = (BinField) member;

    if (isMovingOfFieldCanChangeFunctionality(field)) {
      conflict = new ChangedFunctionalityConflict(this, field,
          new MoveMemberResolution(field,
          CollectionUtil.singletonArrayList(field)));

//      ConflictFactory.addConflict(member, conflict.getType(),
//                                  conflict);
      rep.addConflict(conflict, member);
      data.addConflict(conflict);
    }
  }

  private boolean isMovingOfFieldCanChangeFunctionality(BinField field) {
    BinExpression expression = field.getExpression();

    return
        /*((expression instanceof BinNewExpression)
                 || (expression instanceof BinMethodInvocationExpression))*/
        (expression != null && expression.isChangingAnything())
        || getUsedBy(field).hasNext();
  }

  private void checkIfMainMethodMovesIntoRightClass(ConflictData data) {
    BinMember member = data.getMember();

    Conflict conflict = rep.getConflict(member,
        ConflictType.MAIN_INTO_WRONG_CLASS,
        targetType);

    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    if ((member instanceof BinMethod) && ((BinMethod) member).isMain()) {
      if (!(targetType.getName() + ".java").
          equals(targetType.getCompilationUnit().getName())) {
        conflict = new UnresolvableConflict(this,
            ConflictType.MAIN_INTO_WRONG_CLASS,
            member, new ArrayList());
        rep.addConflict(conflict, member, targetType);
        data.addConflict(conflict);
      }
    }
  }

//  private void checkIfFieldInitializedByComplexExpression(ConflictData data) {
//    BinMember member = data.getMember();
//
//    if (! (member instanceof BinField)) {
//      return;
//    }
//
////    Conflict conflict = ConflictFactory.getConflict(member,
////        ConflictType.FIELD_INIT_IS_COMPLEX_EXPR);
//    Conflict conflict=rep.getConflict(member,ConflictType.FIELD_INIT_IS_COMPLEX_EXPR);
//
//    if (conflict != null) {
//      data.addConflict(conflict);
//      return;
//    }
//
//    BinExpression expression = ( (BinField) member).getExpression();
//
//    // FIXME what about "int field = new A().a;"  ?
//    // should check if this part of tree contains these types of expressions!
//    if ( (expression instanceof BinNewExpression) ||
//        (expression instanceof BinMethodInvocationExpression)) {
//      conflict = new UnresolvableConflict(this,
//                                          ConflictType.FIELD_INIT_IS_COMPLEX_EXPR,
//                                          member, new ArrayList());
////      ConflictFactory.addConflict(member,conflict.getType(),
////                                  conflict);
//      rep.addConflict(conflict,member);
//
//      data.addConflict(conflict);
//    }
//  }

  private boolean isDefinitionOfMemberMoves(ConflictData data) {
    List conflicts = data.getConflicts();

    if (data.getMember() instanceof BinField) {
      return true;
    }

    for (int i = 0, max = conflicts.size(); i < max; i++) {
      Conflict conflict = (Conflict) conflicts.get(i);

      if ((conflict instanceof DeclarationOrDefinitionConflict) &&
          !(conflict.getResolution() instanceof CreateDefinitionResolution) ||
          (conflict instanceof CreateOnlyDeclarationConflict)) {
        return false;
      }
    }

    return true;
  }

  private void checkIfImplementationIsNecessaryForPullDown(ConflictData data) {
    BinMember member = data.getMember();
    Conflict conflict = rep.getConflict(member, data.getUsedByList(), targetType);
    if ((conflict != null) && conflict.isResolved()) {
      if (conflict.getResolution() instanceof CreateAbstractDeclarationResolution) {
        int accessForAbstract = member.isPrivate() ? BinModifier.PROTECTED : member.getAccessModifier();
        checkIfImplementationIsNecessaryForOthers(data, accessForAbstract, member.getOwner().getBinCIType(),
            Collections.singletonList(targetType.getTypeRef()));

        checkImports(data, targetType, true);
      }
    }
  }

  private void checkIfDeclarationOrDefinitionIsNecessary(ConflictData data) {
    BinMember member = data.getMember();

    // FIXME: remove duplication for handling abstract class and interface case

    if (nativeType.isInterface()) {
      checkMovingFromInterface(data);
    }

    if (targetType.isInterface()) {
      checkIfPossibleToMoveMemberIntoInterface(data);
      return;
    }

    if (!(member instanceof BinMethod) || member.isStatic() /*||
         targetType.getTypeRef().isDerivedFrom(nativeType.getTypeRef()) ||
         !nativeType.getTypeRef().isDerivedFrom(targetType.getTypeRef())*/
        ||
        usesMovingMember(data)) {
      return;
    }

    Conflict conflict = null;
    // invariant : member instanceof BinMethod

    if (targetType.isAbstract() && !member.isAbstract()) {

      // invariant: not interface case

      // check if member already exists in target type
      if (rep.getConflict(member, ConflictType.ALREADY_DEFINED,  targetType) != null) {
        return;
      }

      conflict = rep.getConflict(member, ConflictType.DECLARATION_OR_DEFINITION,
          targetType);

      if (conflict != null) {
        if (conflict.getResolution() instanceof CreateDeclarationResolution) {
          int accessForAbstract = findAccessForAbstract(member);
          checkIfImplementationIsNecessaryForOthers(data, accessForAbstract, targetType, null);
          checkIfAccessOfAbstractImplementationIsEnough(data,
              accessForAbstract);

          checkImports(data, targetType, true);
          data.addConflict(conflict);
        }
      } else {
        conflict = new DeclarationOrDefinitionConflict(this, member);
        MultipleResolveConflict mrConflict = (MultipleResolveConflict)
            conflict;
        mrConflict.addPossibleResolution(
            new CreateDeclarationResolution(member,
            findAccessForAbstract(member)));

        mrConflict.addPossibleResolution(new CreateDefinitionResolution(
            member));
        data.addConflict(conflict);

        rep.addConflict(conflict, member,
            targetType);
      }
    } else if (member.isAbstract()) {
      if (targetType.isAbstract()) {
        conflict = rep.getConflict(member,
            ConflictType.
            CREATE_ONLY_DECLARATION,
            targetType);

        if (conflict != null) {
          data.addConflict(conflict);
          checkImports(data, targetType, true);
        } else {

          conflict = new CreateOnlyDeclarationConflict(this, member);
//            conflict.setResolution(new CreateDeclarationResolution(member));
          checkImports(data, targetType, true);

          rep.addConflict(conflict, member, targetType);
          data.addConflict(conflict);
        }

      } else {
//      conflict = ConflictFactory.getConflict(member,
//                                             ConflictType.IMPLEMENTATION_NEEDED,
//                                             targetType);
//
//      conflict = ConflictFactory.getConflict(member,
//                                             ConflictType.IMPLEMENTATION_NEEDED,
//                                             targetType);
//
//      if (conflict != null) {
//        data.addConflict(conflict);
//      } else {
//        List targetList = Collections.singletonList(
//            targetType);
//        conflict = new OtherImplementersExistConflict(this,
//            member,
//            targetList);
//        conflict.setResolution(new AddImplementationResolution(this, member,
//            targetList, member.getAccessModifier()));
//
//        ConflictFactory.addConflict(member, ConflictType.IMPLEMENTATION_NEEDED,
//                                    targetType, conflict);
//
//        data.addConflict(conflict);
//      }
//      checkImports(data, targetType, true);

        // FIXME: create implementation here
//        conflict = ConflictFactory.getConflict(member,
//                                               ConflictType.ABSTRACT_METHOD_TO_CLASS);
        conflict = rep.getConflict(member,
            ConflictType.ABSTRACT_METHOD_TO_CLASS);
        if (conflict == null) {
          conflict = new UnresolvableConflict(this,
              ConflictType.
              ABSTRACT_METHOD_TO_CLASS,
              member,
              Collections.singletonList(
              targetType));
//          ConflictFactory.addConflict(member,
//                                      conflict.getType(),
//                                      conflict);
          rep.addConflict(conflict, member);

        }
        data.addConflict(conflict);

      }
    }
  }

  private boolean usesMovingMember(ConflictData data) {
    List usesList = data.getUsesList();

    for (int i = 0, max = usesList.size(); i < max; i++) {
      ConflictData usesData = getConflictData(usesList.get(i));
      if ((usesData != null) && usesData.isSelectedToMove()) {
        return true;
      }
    }

    return false;
  }

  private void checkIfAccessOfAbstractImplementationIsEnough(ConflictData data,
      int accessForAbstract) {
    BinMember member = data.getMember();

    if (!targetType.getTypeRef().getDirectSubclasses().contains(nativeType.
        getTypeRef())) {
      return;
    }

    if (BinModifier.compareAccesses(member.getAccessModifier(),
        accessForAbstract) == -1) {
      Conflict conflict = rep.getConflict(member,
          ConflictType.WEAK_ACCESS_FOR_ABSTRACT,
          targetType);
      if (conflict != null) {
        data.addConflict(conflict);
        return;
      }

      conflict = new WeakAccessConflict(this,
          ConflictType.WEAK_ACCESS_FOR_ABSTRACT,
          member, new ArrayList(), new ChangeAccessResolution(member,
          accessForAbstract));
//      conflict.setResolution(new ChangeAccessResolution(member,
//          accessForAbstract));
//      ConflictFactory.addConflict(member, conflict.getType(),
//                                  targetType, conflict);
      rep.addConflict(conflict, member, targetType);

      data.addConflict(conflict);
    }
  }

  private int findAccessForAbstract(BinMember member) {
    BinPackage targetPackage = targetType.getPackage();
    Iterator subs = targetType.getTypeRef().getDirectSubclasses().iterator();

    boolean isDifferentPackages = false;
    while (subs.hasNext()) {
      if (!((BinTypeRef) subs.next()).getPackage()
          .isIdentical(targetPackage)) {
        isDifferentPackages = true;
        break;
      }
    }

    int access = isDifferentPackages ? BinModifier.PROTECTED :
        BinModifier.PACKAGE_PRIVATE;
    return access;
  }

  private void checkIfPossibleToMoveMemberIntoInterface(ConflictData data) {
    BinMember member = data.getMember();

    if (member instanceof BinField) {
      checkIfFieldForInterfaceIsStatic(data);
      checkIfFieldForInterfaceIsAssignedWhenUsed(data);
    } else if (member instanceof BinMethod) {
      checkIfMethodForInterfaceIsNotStatic(data);
      checkIfPublicForInterface(data);
      checkIfImplementationIsNecessaryForOthers(data, BinModifier.PUBLIC, targetType, null);
    } else if (member instanceof BinCIType) {
      // no checks needed
    } else {
      throw new RuntimeException("member is neither BinField nor BinMethod nor BinCIType");
    }

    if (!data.unresolvableConflictsExist()) {
      Conflict conflict = rep.getConflict(member,
          ConflictType.CREATE_ONLY_DECLARATION,
          targetType);
      if (conflict != null) {
        data.addConflict(conflict);
        checkImports(data, targetType, true);
        return;
      }

      conflict = new CreateOnlyDeclarationConflict(this, member);
//      conflict.setResolution(new CreateDeclarationResolution(member));
      checkImports(data, targetType, true);

//      ConflictFactory.addConflict(member,conflict.getType(),
//                                  targetType, conflict);
      rep.addConflict(conflict, member, targetType);

      data.addConflict(conflict);
    }
  }

  private void checkIfImplementationIsNecessaryForOthers(ConflictData data,
      int implementAccess, BinCIType parentType, List excludeTypes) {
    BinMember member = data.getMember();

    Conflict conflict = rep.getConflict(member,
        ConflictType.IMPLEMENTATION_NEEDED,
        parentType);
    if (conflict != null) {
      data.addConflict(conflict);
      List needImplementation =
          ((AddImplementationResolution) conflict.getResolution()).
          getNeedImplementation();
      for (int i = 0, max = needImplementation.size(); i < max; i++) {
        checkImports(data, (BinCIType) needImplementation.get(i), true);
      }
      return;
    }

    Iterator subs = parentType.getTypeRef().getDirectSubclasses().iterator();
    List needImplementation = new ArrayList();

    sub:while (subs.hasNext()) {
      BinTypeRef sub = (BinTypeRef) subs.next();
      if ((excludeTypes != null) && (excludeTypes.contains(sub))) {
        continue;
      }
      BinMethod[] methods = sub.getBinCIType().getDeclaredMethods();
      for (int j = 0; j < methods.length; j++) {
        if (methods[j].sameSignature((BinMethod) member)) {
          continue sub;
        }
      }

      needImplementation.add(sub.getBinCIType());
      checkImports(data, sub.getBinCIType(), true);
    }

    if (needImplementation.size() > 0) {
      conflict = new AddImplementationConflict(this, member,
          needImplementation, implementAccess);
//      conflict.setResolution(new AddImplementationResolution(this, member,
//          needImplementation,
//          implementAccess));
      rep.addConflict(conflict, member, parentType);

//      ConflictFactory.addConflict(member, conflict.getType(),
//                                  targetType, conflict);
      data.addConflict(conflict);
    }
  }

  private void checkIfPublicForInterface(ConflictData data) {
    BinMember member = data.getMember();

    if (!member.isPublic()) {
//      Conflict conflict = ConflictFactory.getConflict(member,
//          ConflictType.NOT_PUBLIC_FOR_INTERFACE);
      Conflict conflict = rep.getConflict(member,
          ConflictType.NOT_PUBLIC_FOR_INTERFACE);

      if (conflict != null) {
        data.addConflict(conflict);
        return;
      }

      conflict = new WeakAccessConflict(this,
          ConflictType.NOT_PUBLIC_FOR_INTERFACE,
          member, new ArrayList(), new ChangeAccessResolution(member,
          BinModifier.PUBLIC));
//      conflict.setResolution(new ChangeAccessResolution(member,
//          BinModifier.PUBLIC));
//      ConflictFactory.addConflict(member, conflict.getType(),
//                                  conflict);
      rep.addConflict(conflict, member);

      data.addConflict(conflict);
    }
  }

  private void checkIfFieldForInterfaceIsStatic(ConflictData data) {
    BinMember member = data.getMember();

    Conflict conflict = rep.getConflict(member,
        ConflictType.NOT_STATIC_FIELD_INTO_INTERFACE);
    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    if ((member instanceof BinField) && !member.isStatic()) {
      conflict = new UnresolvableConflict(this,
          ConflictType.NOT_STATIC_FIELD_INTO_INTERFACE,
          member, new ArrayList());

//      ConflictFactory.addConflict(member,conflict.getType(),
//                                  conflict);
      rep.addConflict(conflict, member);

      data.addConflict(conflict);
    }
  }

  private void checkIfMethodForInterfaceIsNotStatic(ConflictData data) {
    BinMember member = data.getMember();

    Conflict conflict = rep.getConflict(member,
        ConflictType.STATIC_METHOD_INTO_INTERFACE);
    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    if ((member instanceof BinMethod) && member.isStatic()) {
      conflict = new UnresolvableConflict(this,
          ConflictType.STATIC_METHOD_INTO_INTERFACE,
          member, new ArrayList());
//      ConflictFactory.addConflict(member,conflict.getType(),
//                                  conflict);
      rep.addConflict(conflict, member);

      data.addConflict(conflict);
    }
  }

  private void checkIfFieldForInterfaceIsAssignedWhenUsed(ConflictData data) {
    BinMember member = data.getMember();
    Conflict conflict = rep.getConflict(member,
        ConflictType.ASSIGNMENT_FOR_FINAL);
    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    if (member instanceof BinField) {
      List withAssignment = new ArrayList();
      List invocations = Finder.getInvocations(member);
      for (int i = 0, max = invocations.size(); i < max; i++) {
        InvocationData invocData = (InvocationData) invocations.get(i);
        BinSourceConstruct sourceConstruct = (BinSourceConstruct) invocData.
            getInConstruct();

        if (sourceConstruct.getParent() instanceof BinAssignmentExpression) {
          CollectionUtil.addNew(withAssignment,
              invocData.getWhereType().getBinCIType());
        }
      }

      if (withAssignment.size() > 0) {
        conflict = new UnresolvableConflict(this,
            ConflictType.ASSIGNMENT_FOR_FINAL,
            member, withAssignment);
//        ConflictFactory.addConflict(member, conflict.getType(),
//                                    conflict);
        rep.addConflict(conflict, member);

        data.addConflict(conflict);
      }
    }
  }

//  private void checkIfIsUsedInstanceField(ConflictData data) {
//    BinMember member = data.getMember();
//    if (member.isStatic() || ! (member instanceof BinField)) {
//      return;
//    }
//
//    List usages = new ArrayList();
//    Iterator where = getUsedBy(member);
//    while (where.hasNext()) {
//      CollectionUtil.addNew(usages, where.next());
//    }
//
//    if (usages.size() > 0) {
//      Conflict conflict = new UnresolvableConflict(this,
//          ConflictType.USED_INSTANCE_FIELD,
//          member, usages);
//      data.addConflict(conflict);
//    }
//  }

  private void checkImports(ConflictData data) {
      checkImports(data, targetType, false);
  }

  private void checkImports(ConflictData data, BinCIType target,
      final boolean isCheckOnlySignature) {
    data.addConflicts(importManager.addImportsForMember(data.getMember(),
        target.getTypeRef(), isCheckOnlySignature));
  }

  private void checkIfMemberWithSameSignatureAlreadyExists(ConflictData data) {
    BinMember member = data.getMember();
    Conflict conflict = null;

    if (!(member instanceof BinMethod) && !(member instanceof BinField) && !(member instanceof BinCIType)) {
      return;
    }

    conflict = rep.getConflict(member, ConflictType.ALREADY_DEFINED,
        targetType);
    if (conflict != null) {
      data.addConflict(conflict);
      return;
    }

    BinMember bin = targetType.hasMemberWithSignature(member);
    if (bin != null) {
      if (targetType.isAbstract() && bin.isAbstract() && (member instanceof BinMethod)
          && ((BinMethod)member).findAllOverrides().contains(bin)) {
        conflict = new SubstituteAbstractMethodConflict(this, member,
            CollectionUtil.singletonArrayList(bin));
      } else {

        conflict = new UnresolvableConflict(this, ConflictType.ALREADY_DEFINED,
            member,
            CollectionUtil.singletonArrayList(bin));
        //      ConflictFactory.addConflict(member, conflict.getType(),
        //                                  targetType, conflict);
      }
      rep.addConflict(conflict, member, targetType);
      data.addConflict(conflict);

    }
  }

  private void checkIfMethodOverridesOrOverriden(ConflictData data) {
    if (!(data.getMember() instanceof BinMethod)) {
      return;
    }

    Conflict conflict;
    BinMethod method = (BinMethod) data.getMember();

    conflict = rep.getConflict(method, ConflictType.OVERRIDES);

    if (conflict != null) {
      if (conflict instanceof OverridesMethodConflict) {
        if (conflict.isResolved()) {
//          rep.removeConflict(method, ConflictType.OVERRIDES);
        } else {
          data.addConflict(conflict);
        }
      }

    } else {

      List overrides = method.findOverrides();

      if (overrides.size() > 0) {

        boolean implementsAbstract = true;

        for (int i = 0; i < overrides.size(); i++) {
          BinMethod overriddenMethod = (BinMethod) overrides.get(i);

          if (!overriddenMethod.isAbstract()) {
            implementsAbstract = false;
            break;
          }
        }
        if (implementsAbstract) {
          conflict = new OverridesMethodConflict(this, method, true);
//          conflict.setResolution(new OverridesAbstractMethodResolution(method));
        } else {
          conflict = new OverridesMethodConflict(this, method, false);
//          conflict.setResolution(new OverridesMethodResolution(method));

        }

//        ConflictFactory.addConflict(method, conflict.getType(),
//                                    conflict);
        rep.addConflict(conflict, method);

        data.addConflict(conflict);

      }
    }

    conflict = rep.getConflict(method, ConflictType.OVERRIDEN);
    if (conflict != null) {
      if (conflict instanceof OverriddenMethodConflict) {
        if (conflict.isResolved()) {
//          rep.removeConflict(method, ConflictType.OVERRIDEN);
        } else {
          data.addConflict(conflict);
        }
      } else {
        data.addConflict(conflict);
      }
    } else {
      List subMethods = method.getOwner().getBinCIType().getSubMethods(method);
      if (subMethods.size() > 0) {
        conflict = new OverriddenMethodConflict(this,
            method
            /*, subMethods*/);
//        conflict.setResolution(new MethodInheritanceResolution(method));
//        ConflictFactory.addConflict(method, conflict.getType(), conflict);

        rep.addConflict(conflict, method);

        data.addConflict(conflict);

//        conflict = new UnresolvableConflict(this, ConflictType.OVERRIDEN,
//                                            method, subMethods);
//        ConflictFactory.addConflict(method, ConflictType.OVERRIDEN, conflict);
//        data.addConflict(conflict);
      }
    }
  }

  public List addConflictEditors(final TransformationList transList) {
    List binMembersToMove = getBinMembersToMove();
    List definitionsToMove = new ArrayList();
    //<FIX> Aleksei Sosnovski 08.2005
    importManager.createEditors(transList, binMembersToMove);
    //old code
    //importManager.createEditors(transList);
    //</FIX>

    for (int i = 0, max = binMembersToMove.size(); i < max; i++) {
      BinMember member = (BinMember) binMembersToMove.get(i);
      ConflictData data = getConflictData(member);
      List conflicts = data.getConflicts();

      for (int j = 0, maxJ = conflicts.size(); j < maxJ; j++) {
        Conflict conflict = (Conflict) conflicts.get(j);

        if (conflict.isResolved()) {
          Editor[] editors = conflict.getEditors();

          for (int k = 0; k < editors.length; k++) {
            transList.add(editors[k]);
          }
        }
      }

      if (isDefinitionOfMemberMoves(data)) {
        definitionsToMove.add(member);
      }
    }

    return definitionsToMove;
  }

  private void checkIfDRConflictIsNecessaryForUses(ConflictData data) {
    BinMember member = data.getMember();

    List allUses = data.getUsesList();
    List usesThroughRef = createListOfUsagesThroughRef(member, true);
    List usesThroughRefCheck = new ArrayList();
    List usesThroughThisCheck = new ArrayList();
    List usesThroughNewCheck = new ArrayList();

    boolean isTargetSub = targetType.getTypeRef().isDerivedFrom(member.getOwner());

    for (int i = 0, max = allUses.size(); i < max; i++) {
      Object o = allUses.get(i);
      ConflictData usesData = getConflictData(o);

      if (usesData.isSelectedToMove() ||
          usesInFutureTarget((BinMember) o) || targetBelongsToNative()) {
        continue;
      }

      if (usesThroughRef.contains(o)) {
        usesThroughRefCheck.add(o);
      } else if (o instanceof BinConstructor) {
        usesThroughNewCheck.add(o);
      } else {
        usesThroughThisCheck.add(o);
      }
    }

    HashMap dataForFutureConflicts = new HashMap();
    boolean isCreateNewConflicts = checkIfUsesLetMemberMove(data,
        usesThroughThisCheck,
        usesThroughRefCheck,
        usesThroughNewCheck,
        dataForFutureConflicts);

    if (isCreateNewConflicts) {
      createConflictsFromData(dataForFutureConflicts, data);
    }
  }

  private boolean checkIfUsesLetMemberMove(ConflictData data,
      List usesThroughThisList,
      List usesThroughRefList,
      List usesThroughNewList,
      HashMap dataForConflicts) {
    BinMember member = data.getMember();
    List usesList = new ArrayList();
    usesList.addAll(usesThroughThisList);
    usesList.addAll(usesThroughRefList);
    usesList.addAll(usesThroughNewList);
    boolean isTargetSub = targetType.getTypeRef().isDerivedFrom(member.getOwner());

    for (int i = 0, max = usesList.size(); i < max; i++) {
      BinMember usesMember = (BinMember) usesList.get(i);

      if (isConflictForDownMemberExists(member, usesMember, dataForConflicts)) {
        continue;
      }

      if (isMemberNative(usesMember)) {
        if (isTargetSub) {
          if (isVisible(usesMember, false)) {
            continue;
          }
          ConflictType conflictType = getConflictTypeForUses(member, usesMember);
          addDataForFutureConflict(dataForConflicts, conflictType, member,
              usesMember, null);
        } else { // target is not sub
          Conflict importConflict = null;
          if (!usesThroughNewList.contains(usesMember)) {
            importConflict = importManager.addExtraImport(member,
                nativeType.getTypeRef(), targetType.getTypeRef());
          }

          if ((importConflict == null)
              && (getUsedByThroughComplexExpression(member).size() == 0)
              && !isMethodWithFutureParamExistsInTarget(member, nativeType)
							&& !(member instanceof BinCIType)) {
            if (isVisible(usesMember, false)) {
              continue;
            }

            HashMap extraImports =
                collectExtraImports(CollectionUtil.singletonArrayList(targetType));
            ConflictType conflictType = getConflictTypeForUses(member,
                usesMember);
            addDataForFutureConflict(dataForConflicts, conflictType, member,
                usesMember, extraImports);
          } else { // not possible to import native or member is not called on simple expressions everywhere
            if (isPossibleToMove(getConflictData(usesMember), data)) {
              addDataForFutureConflict(dataForConflicts,
                  ConflictType.MOVE_USE_ALSO,
                  member, usesMember, null);
            } else { // not possible to move native
              //TODO: add explaining why member cannot be moved
              UnresolvableConflict conflict2 = new UnresolvableConflict(this,
                  ConflictType.MOVE_NOT_POSSIBLE,
                  member, new ArrayList());
              data.addConflict(conflict2);
              Assert.must(data.getMember() == member);

              rep.addConflict(conflict2, data.getMember());

              return false;
            }
          }
        }
      } else { // not native
        if (isTargetSub) {
          if (isVisible(usesMember, false)) {
            continue;
          }

          HashMap extraImports =
              collectExtraImports(CollectionUtil.singletonArrayList(targetType));
          addDataForFutureConflict(dataForConflicts,
              ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
              member, usesMember, extraImports);
        } else { // target is not sub
          Conflict importConflict = null;
          if (usesThroughThisList.contains(usesMember)) {
            importConflict = importManager.addExtraImport(member,
                nativeType.getTypeRef(), targetType.getTypeRef());
          }

          if ((importConflict == null)
              && getUsedByThroughComplexExpression(member).size() == 0
              && !isMethodWithFutureParamExistsInTarget(member, nativeType)) {
            if (isVisible(usesMember, false)) {
              continue;
            }

            HashMap extraImports =
                collectExtraImports(CollectionUtil.singletonArrayList(targetType));
            addDataForFutureConflict(dataForConflicts,
                ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
                member, usesMember, extraImports);
          } else { // not possible to import native or member is not called on simple expressions everywhere
            //TODO: add explaining why member cannot be moved
            UnresolvableConflict conflict = new UnresolvableConflict(this,
                ConflictType.MOVE_NOT_POSSIBLE,
                member, new ArrayList());
            data.addConflict(conflict);
            Assert.must(data.getMember() == member);
            rep.addConflict(conflict, data.getMember());
            return false;
          }
        }
      }
    }

    return true;
  }

  private ConflictType getConflictTypeForUses(BinMember member,
      BinMember downMember) {
    return isPossibleToMove(getConflictData(downMember), getConflictData(member)) ?
        ConflictType.USES : ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED;
  }

  private boolean isConflictForDownMemberExists(BinMember member,
      BinMember downMember,
      HashMap dataForConflicts) {
    Conflict conflict = rep.getConflict2(member, downMember,
        targetType);

    if (conflict == null) {
      return false;
    }

    addDataForFutureConflict(dataForConflicts, conflict.getType(),
        member, downMember, null);

    return true;
  }

  private void addDataForFutureConflict(HashMap dataForConflicts,
      ConflictType conflictType,
      BinMember member, BinMember downMember,
      HashMap extraImports) {
    DataForFutureConflict dataForConflict =
        (DataForFutureConflict) dataForConflicts.get(conflictType);
    if (dataForConflict == null) {
      dataForConflict = new DataForFutureConflict(downMember, extraImports);
      dataForConflicts.put(conflictType, dataForConflict);
    } else {
      dataForConflict.addDownMember(downMember);
      dataForConflict.addExtraImports(extraImports);
    }
  }

  private void createConflictsFromData(HashMap dataForFutureConflicts,
      ConflictData data) {
    BinMember member = data.getMember();
    Set keySet = dataForFutureConflicts.keySet();
    Iterator iter = keySet.iterator();
    Conflict conflict = null;

    while (iter.hasNext()) {
      ConflictType conflictType = (ConflictType) iter.next();
      DataForFutureConflict dataForFutureConflict =
          (DataForFutureConflict) dataForFutureConflicts.get(conflictType);
      List downMembers = dataForFutureConflict.getDownMembers();
      HashMap extraImports = dataForFutureConflict.getExtraImports();

      conflict = rep.getConflict(member, downMembers, targetType);
      if ((conflict != null) &&
          (conflict.getResolution() instanceof MoveMemberResolution)) {
//        ConflictFactory.removeConflict(member, downMembers, targetType);
        rep.removeConflict(member, downMembers, targetType);
        conflict = null;
      }

      if (conflict != null) {
        addConflictFromFactory(conflict, data, downMembers);
        // should be already in ConflictFactory
//        for (int i = 0, max = downMembers.size(); i < max; i++) {
//          ConflictFactory.addConflict(member, downMembers.get(i), targetType,
//                                      conflict);
//        }
      } else {
        conflict = createConflictOfType(conflictType, member, downMembers,
            extraImports);

        if (conflict != null) {
          rep.addConflict(conflict, member, downMembers, targetType);
          for (int i = 0, max = downMembers.size(); i < max; i++) {
            rep.addConflict2(conflict, member, (BinMember) downMembers.get(i),
                targetType);
          }
          addConflictForData(data, downMembers, conflict);
        }
      }
    }
  }

  private Conflict createConflictOfType(ConflictType conflictType,
      BinMember upMember,
      List downMembers, HashMap extraImports) {
    Conflict conflict = null;
    if (conflictType.equals(ConflictType.USES)) {
      conflict = new MRUsesConflict(this, upMember, downMembers);
      MultipleResolveConflict mrConflict = (MultipleResolveConflict) conflict;
      mrConflict.addPossibleResolution(
          new ChangeAccessResolution(downMembers, extraImports));
      mrConflict.addPossibleResolution(new MoveMemberResolution(upMember,
          downMembers));
    } else if (conflictType.equals(ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED)) {
      conflict = new WeakAccessConflict(this,
          ConflictType.UNMOVABLE_CANNOT_BE_ACCESSED,
          upMember, downMembers, new ChangeAccessResolution(downMembers,
          extraImports));
//      conflict.setResolution(new ChangeAccessResolution(downMembers,
//          extraImports));
    } else if (conflictType.equals(ConflictType.MOVE_USE_ALSO)) {
      conflict = new MoveDependentConflict(this, ConflictType.MOVE_USE_ALSO,
          upMember, downMembers);
//      conflict.setResolution(new MoveMemberResolution(upMember, downMembers));
    } else {
      conflict = null;
    }

    return conflict;
  }

  private class DataForFutureConflict {
    private List downMembers = new ArrayList();
    private HashMap extraImports = new HashMap();

    public DataForFutureConflict() {}

    public DataForFutureConflict(BinMember downMember) {
      this(downMember, null);
    }

    public DataForFutureConflict(BinMember downMember, HashMap extraImports) {
      addDownMember(downMember);
      addExtraImports(extraImports);
    }

    public void addDownMember(BinMember downMember) {
      downMembers.add(downMember);
    }

    public void addExtraImports(HashMap newExtraImports) {
      if (newExtraImports == null) {
        return;
      }

      Set keySet = newExtraImports.keySet();
      Iterator iter = keySet.iterator();
      while (iter.hasNext()) {
        Object key = iter.next();
        List list = (List) extraImports.get(key);
        if (list == null) {
          extraImports.put(key, newExtraImports.get(key));
        } else {
          CollectionUtil.addAllNew(list, (List) newExtraImports.get(key));
        }
      }
    }

    public List getDownMembers() {
      return downMembers;
    }

    public HashMap getExtraImports() {
      return extraImports;
    }
  }


  /**
   * if method uses native members and they stay in native class but method moves
   * it must retain access to them. to make that possible parameter of native type
   * is added for method. but before it must be checked that method with such
   * signature don't exists in target already
   * @param member member
   * @param futureParam future param
   * @return true if method exists
   */
  private boolean isMethodWithFutureParamExistsInTarget(BinMember member,
      BinCIType futureParam) {
    if (!(member instanceof BinMethod)) {
      return false;
    }

    BinMethod method = (BinMethod) member;
    BinMethod[] methods = targetType.getAccessibleMethods(targetType);
    for (int i = 0; i < methods.length; i++) {
      if (method.getName().equals(methods[i].getName()) &&
          ((method.getParameters().length + 1) ==
          methods[i].getParameters().length) &&
          (methods[i].getParameters()[0].getTypeRef().getBinType() ==
          futureParam)) {
        return true;
      }
    }

    return false;
  }

  private void checkIfDRConflictIsNecessaryForUsedBy(ConflictData data) {
    List usedByList = data.getUsedByList();
    List usedByRequireDR = new ArrayList();
    List onComplexList = getUsedByThroughComplexExpression(data.getMember());
    List throughRefList = CollectionUtil.toList(getUsedByThroughReference(data.getMember()));

    for (int i = 0, max = usedByList.size(); i < max; i++) {
      Object o = usedByList.get(i);
      ConflictData usedByData = getConflictData(o);


      if (isUsedByInFutureTarget((BinMember) o)) {
        continue;
      }


      if (usedByData.isSelectedToMove()) {
        if ((!throughRefList.contains(o) && (!onComplexList.contains(o)))
            || (!getConflictData(data.getMember()).isSelectedToMove())) {
          continue;
        }
      }

      if (isMovedWithOwner((BinMember) o)) {
      	continue;
      }

      usedByRequireDR.add(o);
    }

    checkIfUsedByLetMemberMove(data, usedByRequireDR);
    checkIfTargetAccessible(data, usedByRequireDR);
  }

  /**
   * @param member
   * @return
   */
  private boolean isMovedWithOwner(BinMember member) {
  	BinTypeRef owner = member.getOwner();
  	do {
  		ConflictData ownerData = getConflictData(owner.getBinCIType());
  		if ((ownerData != null) &&( ownerData.isSelectedToMove())) {
  			return true;
  		}
  		owner = owner.getBinCIType().getOwner();
  	} while (owner != null);
  	return false;
  }

  private void checkIfUsedByLetMemberMove(ConflictData data,
      List usedByRequireDR) {
    BinMember member = data.getMember();

    if ((usedByRequireDR.size() == 0) || willBeStatic(data)) {
      return;
    }

    Conflict conflict = rep.getConflict(member, usedByRequireDR,
        targetType);

    if (conflict != null) {
      if (conflict.getResolution() instanceof MoveMemberResolution) {
//        ConflictFactory.removeConflict(member, usedByRequireDR, targetType);
        rep.removeConflict(member, usedByRequireDR, targetType);
        conflict = null;
      }
//       // Refactor conflict resolver design!!!
      else {
        addConflictFromFactory(conflict, data, usedByRequireDR);
        return;
      }
    }
    conflict = new UsedByChecker(this, data, usedByRequireDR).doCheck();

//    System.err.println("conflict:" + conflict); //innnnn
//    System.err.println(""); //innnnn

    if (conflict != null) {
      rep.addConflict(conflict, member, usedByRequireDR, targetType);

      addConflictForData(data, usedByRequireDR, conflict);
    }
  }

  private void addConflictForData(ConflictData data, List usages,
      Conflict conflict) {
    for (int i = 0, max = usages.size(); i < max; i++) {
      getConflictData(usages.get(i)).addConflict(conflict);
    }

    data.addConflict(conflict);
  }

  private void addConflictFromFactory(Conflict conflict, ConflictData data,
      List downMembers) {
    if (conflict == null) {
      return;
    }

    addConflictForData(data, downMembers, conflict);
    if (conflict.getResolution() != null) {
      addExtraImports(conflict.getResolution().getImports());
    }
  }

  HashMap collectExtraImports(List members) {
    HashMap result = new HashMap();
    for (int i = 0, max = members.size(); i < max; i++) {
      BinCIType type;
      BinMember member = (BinMember) members.get(i);
//      if (getConflictData(member).isSelectedToMove()) {
//        continue;
//      }
      if (member instanceof BinCIType) {
        type = (BinCIType) member;
      } else {
        type = member.getOwner().getBinCIType();
      }

      List extraImports = importManager.getExtraImportsFor(type.getTypeRef());
      if (extraImports != null) {
        result.put(type, extraImports);
      }
    }

    return result;
  }

  private void addExtraImports(HashMap imports) {
    if (imports != null) {
      Set keys = imports.keySet();
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {
        BinCIType type = (BinCIType) iter.next();
        try {
	        importManager.addExtraImports((List) imports.get(type),
	            type.getTypeRef());
        } catch (AmbiguousImportImportException e) {
        	// FIXME: do smth here
        	e.printStackTrace();
        }
      }
    }
  }

  void removeExtraImport(List members, BinCIType typeToRemove) {
    for (int i = 0, max = members.size(); i < max; i++) {
      BinTypeRef type = ((BinMember) members.get(i)).getOwner();
      importManager.removeExtraImport(type, typeToRemove.getTypeRef());
    }
  }

  private boolean usesInFutureTarget(BinMember usesMember) {
    return usesMember.getOwner().equals(targetType.getTypeRef());
  }

  private boolean isUsedByInFutureTarget(BinMember usedByMember) {
    return usedByMember.getOwner().equals(targetType.getTypeRef());
  }

  boolean isHasTargetInstance(BinMember member, List withInstance) {
    return (withInstance.contains(member) ||
        withInstance.contains(member.getOwner().getBinCIType()));
  }

  boolean isPossibleToMove(ConflictData dataCheck,
      ConflictData dataCause) {
    if (dataCheck.isDuringChecking()) {
      //dataCheck.setIsDuringChecking(false);
      return true;
    }

    dataCause.setIsDuringChecking(true);

    if ((dataCheck.getMember() instanceof BinField)) {
      if (willBeStatic(dataCause)) {
        return false;
      }
    }

    if (isValidToMove(dataCheck.getMember())) {
      findConflicts(dataCheck);

      return!dataCheck.unresolvableConflictsExist();
    }

    dataCheck.setIsDuringChecking(false);

    return false;
  }

  boolean isVisible(BinMember member, boolean isMemberMoves) {
    if (targetBelongsToNative() ||
        getAccessChangedByResolution(member) != member.getAccessModifier()) {
      return true;
    }

    int newAccess;
    if (isMemberMoves) {
      newAccess = MinimizeAccessUtil.getNewAccessForMember(member, targetType,
          Finder.getInvocations(member));
    } else {
      // FIXME: hack[tonis]
      // bug : 13.11.03
      // did pull up for private VariableUsage.getParameterUsage->TypeUsage(
      // function without usages
      // and got exception because this!

      if (member.getOffsetNode() == null) {
        return true;
      }
      List invocationData = CollectionUtil.singletonArrayList(new InvocationData(
          member,
          targetType.getTypeRef(), member.getOffsetNode()));
      newAccess = MinimizeAccessUtil.getNewAccessForMember(member,
          member.getOwner().getBinCIType(),
          invocationData);
    }

    return (member.getAccessModifier() == newAccess);
  }

  public boolean willBeStatic(ConflictData data) {
    List conflicts = data.getConflicts();
    for (int i = 0, max = conflicts.size(); i < max; i++) {
      if (conflicts.get(i) instanceof MakeStaticConflict) {
        return true;
      }
    }

    return false;
  }

  private int getAccessChangedByResolution(BinMember member) {
    List conflicts = getConflictData(member).getConflicts();

    for (int i = 0, max = conflicts.size(); i < max; i++) {
      Conflict conflict = (Conflict) conflicts.get(i);
      ConflictResolution resolution = conflict.getResolution();
      if (resolution instanceof ChangeAccessResolution) {
        return ((ChangeAccessResolution) resolution)
            .getNewAccessModifier(member);
      }
    }

    return member.getAccessModifier();
  }

  public boolean isMethodHasInstanceOfType(BinMember member,
      BinCIType instanceOfType) {
    ConflictData data = getConflictData(member);
    if ((data == null) || (data.getUsesList().size() == 0)) {
      return false;
    }

    List uses = data.getUsesList();
    for (int i = 0, max = uses.size(); i < max; i++) {
      Object o = uses.get(i);
      if (o instanceof BinConstructor) {
        if (((BinMember) o).getOwner().getBinCIType() == instanceOfType) {
          return true;
        }
      }
    }

    return false;
//    return findInvokersOfMemberWithInstance((BinMember)data.getUsesList().get(0),
//                                            instanceOfType)[0].contains(member);
  }

  /**
   * finds invokers of given member, then checks if this invoker has instance
   * of given type
   * @param memberToCheck find invokers of this member
   * @param instanceOfType check presence of instance of this type in invoker
   * @return invokers struct
   */
  List[] findInvokersOfMemberWithInstance(final BinMember memberToCheck,
      final BinCIType
      instanceOfType) {
    final List invocations = Finder.getInvocations(memberToCheck);
    List membersWithInstances = new ArrayList();
    List invocationsWithoutInstance = new ArrayList();

    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData data = (InvocationData) invocations.get(i);

      SourceConstruct inConstruct = data.getInConstruct();
      BinMember instanceToCallOn = null;
      if (inConstruct instanceof BinMemberInvocationExpression) {
      	BinMemberInvocationExpression expression
	          = (BinMemberInvocationExpression) inConstruct;
	      if (expression == null) {
	        continue; // can get here only for toString(), i.e. never in our case
	      }

	      instanceToCallOn = new InstanceFinder().findInstance(
	          data.getWhereMember(), expression, instanceOfType);
	    }
      if (instanceToCallOn == null) {
        invocationsWithoutInstance.add(inConstruct);
      } else {
        membersWithInstances.add(data.getWhere());
      }
    }

    return new List[] {
        membersWithInstances, invocationsWithoutInstance};
  }

  boolean isMemberNative(BinMember member) {
    return nativeType.contains(member);
  }

  private List createListOfUsagesThroughRef(BinMember member, boolean isUses) {
    List result = new ArrayList();
    Iterator iter;

    if (isUses) {
      iter = getUsesThroughReference(member);
    } else {
      iter = getUsedByThroughReference(member);
    }

    while (iter.hasNext()) {
      result.add(iter.next());
    }

    return result;
  }

  protected boolean isValidToMove(Object o) {
    if (o instanceof BinField) {
      return true;
    }

    if (o instanceof BinCIType) {
    	return true;
    }

    return ((o instanceof BinMethod) && !(o instanceof BinConstructor) &&
        !typeBelongsToNative(((BinMethod) o).getOwner().getBinCIType()));
  }

  public List getBinMembersToMove() {
    return getBinMembersToMove(false);
  }

  protected List getBinMembersToMove(boolean isSkipIfDeclaration) {
    List binMembersToMove = new ArrayList();
    List allBinMembers = getAllBinMembers();

    for (int i = 0, max = allBinMembers.size(); i < max; i++) {
      Object member = allBinMembers.get(i);

      if (isSkipIfDeclaration &&
          !isDefinitionOfMemberMoves(getConflictData(member))) {
        continue;
      }

      if (getConflictData(member).isSelectedToMove()) {
        binMembersToMove.add(member);
      }
    }

    return binMembersToMove;
  }

//  private List getConflictDataToMove() {
//    List conflictDataToMove = new ArrayList();
//    List allBinMembers = getAllBinMembers();
//
//    for (int i = 0, max = allBinMembers.size(); i < max; i++) {
//      Object member = allBinMembers.get(i);
//      ConflictData data = getConflictData(member);
//
//      if (data.isSelectedToMove()) {
//        conflictDataToMove.add(data);
//      }
//    }
//
//    return conflictDataToMove;
//  }

  public ConflictData getConflictData(Object key) {
    return (ConflictData) conflictDataMap.get(key);
  }

  /**
   * @param key BinMember
   * @param value ConflictData
   */
  protected void addConflictData(BinMember key, ConflictData value) {
    conflictDataMap.put(key, value);
  }

//  private void removeConflictData(BinMember key) {
//    conflictDataMap.remove(key);
//  }

  public List getAllConflictData() {
    return new ArrayList(conflictDataMap.values());
  }

  private List getAllBinMembers() {
    return new ArrayList(conflictDataMap.keySet());
  }

  protected Iterator getUses(BinMember whoUses) {
    if (!whoUses.getOwner().equals(this.nativeType.getTypeRef())) {
      return Collections.EMPTY_LIST.iterator();
    }

    Iterator result = this.usesMap.findIteratorFor(whoUses);
    if (result == null) {
      ManagingIndexer supervisor = new ManagingIndexer(ProgressMonitor.Progress.
          DONT_SHOW);
      new DependenciesIndexer(supervisor, whoUses);
      whoUses.accept(supervisor);

      List invocations = supervisor.getInvocations();
      for (int j = 0, maxJ = invocations.size(); j < maxJ; j++) {
        InvocationData data = (InvocationData) invocations.get(j);

        BinItem itemCalled = data.getWhat();

        if ((itemCalled instanceof BinMethod) &&
            !(itemCalled instanceof BinConstructor) &&
            (data.getInConstruct() == null)) {
          continue; // had to be toString()
        }

        if ((itemCalled instanceof BinField) ||
            (itemCalled instanceof BinMethod) ||
            ((itemCalled instanceof BinClass) &&
            ((BinClass) itemCalled).isInnerType() &&
            !((BinCIType) itemCalled).isLocal())) {
          BinCIType owner = (itemCalled instanceof BinClass)
              ? (BinCIType) itemCalled
              : ((BinMember) itemCalled).getOwner().getBinCIType();

          if (owner.isFromCompilationUnit()) {
            this.usesMap.putNew(whoUses, itemCalled);

            if (!(itemCalled instanceof BinClass) &&
                !(itemCalled instanceof BinConstructor)) {
              if (isUsedThroughRef((BinMember) itemCalled,
                  ((BinMemberInvocationExpression) data.
                  getInConstruct())
                  .getExpression())) {
                this.usesThroughReference.putNew(whoUses, itemCalled);
              }
            }
          }
        }
      }
      result = this.usesMap.findIteratorFor(whoUses);
      if (result == null) {
        this.usesMap.put(whoUses, new ArrayList(0));
        result = this.usesMap.findIteratorFor(whoUses);
      }
    }

    return result;
  }

  protected Iterator getUsedBy(BinMember whoUsed) {
    if (whoUsed instanceof BinInitializer
        || whoUsed instanceof BinConstructor // FIXME very strange feature, explore later!!!!!
        || !whoUsed.getOwner().equals(this.nativeType.getTypeRef())) {
      return Collections.EMPTY_LIST.iterator();
    }

    Iterator result = this.usedByMap.findIteratorFor(whoUsed);

    if (result == null) {
//      ProgressListener listener = (ProgressListener)
//          CFlowContext.get(ProgressListener.class.getName());
//      if (listener != null && listener instanceof JProgressDialog) {
//        ( (JProgressDialog) listener).setDetails(
//            whoUsed.getNameWithAllOwners());
//      }

      List invocations = Finder.getInvocations(whoUsed);

      for (int j = 0, maxJ = invocations.size(); j < maxJ; j++) {
        InvocationData data = (InvocationData) invocations.get(j);

        if (data.getInConstruct() == null
            || data.getInConstruct() instanceof BinVariable // HACK after TypeIndexer added BinVariable to construct
            ) {
          continue; // had to be toString()
        }

        BinMember location = data.getWhereMember();

        if (Assert.enabled) {
          Assert.must(!(location instanceof BinCIType),
              "usedBy location: " + data.getWhere().getClass() +
              " - "
              + data);
        }
        this.usedByMap.putNew(whoUsed, location);

        if (!(whoUsed instanceof BinClass)) {
          BinExpression expression =
              ((BinMemberInvocationExpression) data.getInConstruct())
              .getExpression();

          if (isUsedThroughRef(whoUsed, expression)) {
            if (whoUsed.isStatic()
                || !expression.isChangingAnything()) {
              this.usedByThroughReference.putNew(whoUsed, location);
            } else {
              this.usedByThroughComplexExpression.putNew(whoUsed, location);
            }
          }
        }
      }

      result = this.usedByMap.findIteratorFor(whoUsed);
      if (result == null) {
        this.usedByMap.put(whoUsed, new ArrayList(0));
        result = this.usedByMap.findIteratorFor(whoUsed);
      }
    }

    return result;
  }

  private Iterator getUsesThroughReference(BinMember whoUses) {
    if (!this.usesMap.contains(whoUses)) {
      getUses(whoUses); // as a side effect it fills up the needed map
    }

    return this.usesThroughReference.iteratorFor(whoUses);
  }

  private Iterator getUsedByThroughReference(BinMember whoUsed) {
    if (!this.usedByMap.contains(whoUsed)) {
      getUsedBy(whoUsed); // as a side effect it fills up the needed map
    }

    return this.usedByThroughReference.iteratorFor(whoUsed);
  }

  List getUsedByThroughComplexExpression(BinMember whoUsed) {
    if (!this.usedByMap.contains(whoUsed)) {
      getUsedBy(whoUsed); // as a side effect it fills up the needed map
    }

    return CollectionUtil.toList(
        this.usedByThroughComplexExpression.iteratorFor(whoUsed));
  }

  public List getProbableTargetClasses() {
    ArrayList result = new ArrayList(5);
    List members = getBinMembersToMove();

    for (int i = 0, max = members.size(); i < max; i++) {
      if (members.get(i) instanceof BinMethod) {
        BinParameter[] params = ((BinMethod) members.get(i)).getParameters();

        for (int j = 0; j < params.length; j++) {
          BinType type = params[j].getTypeRef().getBinType();
          if (isProbableTarget(type)) {
            result.add(type);
          }
        }

        BinType returnType
            = ((BinMethod) members.get(i)).getReturnType().getBinType();
        if (isProbableTarget(returnType)) {
          result.add(returnType);
        }
      }
    }

    return result;
  }

  private final boolean isProbableTarget(BinType type) {
    return !type.isPrimitiveType() && type != nativeType
        && ((BinCIType) type).isFromCompilationUnit()
        /*&& !((BinCIType) type).isInterface()*/;
  }

  private boolean isUsedThroughRef(final BinMember member,
      final BinExpression invokedOnExpression) {
    if (member.isStatic() // static can't be called with 'this' or 'super'
        || (invokedOnExpression != null
        // FIXME what about ((ThisClass) super).method() ?
        // here it will be counted as through ref, but actually it is not!
        && !(invokedOnExpression instanceof BinLiteralExpression
        && (((BinLiteralExpression) invokedOnExpression).isThis()
        || ((BinLiteralExpression) invokedOnExpression).isSuper())))) {
      return true;
    } else {
      return false;
    }
  }

  private boolean typeBelongsToNative(BinCIType type) {
    return (type.isInnerType() &&
        (nativeType.getDeclaredType(type.getName()) != null));
  }

  public boolean targetBelongsToNative() {
    return typeBelongsToNative(targetType);
  }

  boolean isStaticResolutionAllowed(BinMember member) {
    return (!(member instanceof BinField) &&
        !(member instanceof BinConstructor) &&
        (!targetBelongsToNative() ||
        (targetBelongsToNative() && targetType.isStatic())));
  }

  public void setTargetType(BinCIType targetType) {
    this.targetType = targetType;
    clearAllConflicts();
  }

  public BinCIType getTargetType() {
    return targetType;
  }

  public BinCIType getNativeType() {
    return nativeType;
  }

//  public ConflictStatus getConflictStatus(BinMember member) {
//    return new ConflictStatus(member,new HashSet(rep.getConflicts(member)));
//  }

  protected void printTree() {
  }

  public boolean isTargetSuperclass(final BinMethod method) {
    boolean result = (targetType != null && !targetType.isInterface() &&
        method.getOwner().
        getAllSupertypes().contains(targetType.getTypeRef()));
    return result;
  }

  private void checkMovingFromInterface(ConflictData data) {
    BinMember member = data.getMember();
    if (!(member instanceof BinField || member instanceof BinMethod)) {
      return;
    }

    if (targetType == null) {
      return;
    }

    Conflict conflict = null;
    if (targetType.isAbstract() || member instanceof BinField) {
      conflict = rep.getConflict(member,
          ConflictType.CREATE_ONLY_DECLARATION, targetType);

      if (conflict != null) {
        data.addConflict(conflict);
        checkImports(data, targetType, true);
      } else {

        conflict = new CreateOnlyDeclarationConflict(this, member);
        checkImports(data, targetType, true);

        rep.addConflict(conflict, member, targetType);

        data.addConflict(conflict);
      }
    } else {
      conflict = new UnresolvableConflict(this,
          ConflictType.ABSTRACT_METHOD_TO_CLASS,
          member,
          Collections.singletonList(targetType));
//      ConflictFactory.addConflict(member,
//                                conflict.getType(),
//                                conflict);
      rep.addConflict(conflict, member);
      data.addConflict(conflict);

      // FIXME:  should create impl here
//      conflict = ConflictFactory.getConflict(member,
//                                             ConflictType.IMPLEMENTATION_NEEDED,
//                                             targetType);
//
//      if (conflict != null) {
//        data.addConflict(conflict);
//      } else {
//        List targetList = Collections.singletonList(
//            targetType);
//        conflict = new OtherImplementersExistConflict(this,
//            member,
//            targetList);
//        conflict.setResolution(new AddImplementationResolution(this, member,
//            targetList, member.getAccessModifier()));
//
//        ConflictFactory.addConflict(member, ConflictType.IMPLEMENTATION_NEEDED,
//                                    targetType, conflict);
//
//        data.addConflict(conflict);
//      }
//      checkImports(data, targetType, true);
    }
  }

  class UsedByChecker {
    private ConflictData data;
    private List usedByRequireDR;
    private ConflictResolver resolver;
    private List importConflictsForStatic = new ArrayList();
    private List importConflictsForNonStatic = new ArrayList();
    private List unmovableMembers = new ArrayList();

    UsedByChecker(ConflictResolver resolver,
        ConflictData data, List usedByRequireDR) {
      this.data = data;
      this.resolver = resolver;
      this.usedByRequireDR = usedByRequireDR;
    }

    Conflict doCheck() {
      BinMember member = data.getMember();
      Conflict conflict = null;
      BinCIType targetType = resolver.getTargetType();

      boolean isTargetSuper =
          member.getOwner().getAllSupertypes().contains(targetType.getTypeRef());
      boolean hasTargetInstance = true;
      boolean isVisible = resolver.isVisible(member, true);

      List[] invokers = resolver.findInvokersOfMemberWithInstance(member,
          targetType);
      List membersWithTI = invokers[0];

      hasTargetInstance = processUsedByRequireDR(isTargetSuper,
          hasTargetInstance,
          membersWithTI);

      HashMap extraImports = resolver.collectExtraImports(usedByRequireDR);
      boolean isMoveOfUsedByPossible = (unmovableMembers.size() == 0);
      boolean isUnmovableCanAccess =
          member.isStatic() ? (importConflictsForStatic.size() == 0)
          : (hasTargetInstance ||
          (importConflictsForNonStatic.size() == 0));

//
//    conflict = ConflictFactory.getConflict(member, usedByRequireDR,
//    targetType);
//
//    // FIXME: hack to use UNMOVABLE_CANNOT_ACCESS
//
//    if ( conflict != null ) {
//      addConflictFromFactory(conflict, data, usedByRequireDR);
//      return;
//    }

//    System.err.println("member:" + member); //innnnn
//    System.err.println("isTargetSuper:" + isTargetSuper); //innnnn
//    System.err.println("isMoveOfUsedByPossible:" + isMoveOfUsedByPossible); //innnnn
//    System.err.println("isHasTargetInstance:" + isHasTargetInstance); //innnnn
//    System.err.println("importConflictsForNonStatic.size:" + importConflictsForNonStatic.size()); //innnnn
//    System.err.println("isUnmovableCanAccess:" + isUnmovableCanAccess); //innnnn

      if (isTargetSuper) {
        if (isVisible) {
          return null;
        }

        if (isMoveOfUsedByPossible) {
          conflict = new MRUsedByConflict(this.resolver, member,
              usedByRequireDR);
          MultipleResolveConflict mrConflict = (MultipleResolveConflict)
              conflict;
          mrConflict.addPossibleResolution(new ChangeAccessResolution(member, true));
          mrConflict.addPossibleResolution(new MoveMemberResolution(member,
              usedByRequireDR));
        } else {
          conflict = new WeakAccessConflict(this.resolver,
              ConflictType.
              UNMOVABLE_CANNOT_ACCESS,
              member, unmovableMembers,
              new ChangeAccessResolution(member, true));
//        conflict.setResolution(new ChangeAccessResolution(member, true));
        }
      } else { // target type is not super
        if (isUnmovableCanAccess && isMoveOfUsedByPossible) {
          conflict = processCanAccessMoveOfUsedByPossible(
              hasTargetInstance, isVisible, extraImports);
        } else if (!isUnmovableCanAccess && isMoveOfUsedByPossible) {
          conflict = new MoveDependentConflict(this.resolver,
              ConflictType.MOVE_USEDBY_ALSO,
              member, usedByRequireDR);
//        conflict.setResolution(new MoveMemberResolution(member, usedByRequireDR));
        } else if (isUnmovableCanAccess && !isMoveOfUsedByPossible) {
          if (member.isStatic()) {
            if (isVisible) {
              return null;
            }
            conflict = new WeakAccessConflict(this.resolver,
                ConflictType.
                UNMOVABLE_CANNOT_ACCESS,
                member, unmovableMembers,
                new ChangeAccessResolution(member, true,
                extraImports));
//          conflict.setResolution(new ChangeAccessResolution(member, true,
//              extraImports));
          } else { // member is not static
            conflict = processMemberIsNotStatic(hasTargetInstance, isVisible,
                extraImports);
          }
        } else {
          if (member.isStatic()) {
            //TODO: add explaining why member cannot be moved
            UnresolvableConflict conflict2 = new UnresolvableConflict(this.
                resolver,
                ConflictType.MOVE_NOT_POSSIBLE,
                member, new ArrayList());
            data.addConflict(conflict2);

            resolver.rep.addConflict(conflict2, data.getMember());

          } else {
            //TODO: add explaining why member cannot be moved
            Conflict conflict2 = new UnresolvableConflict(this.resolver,
                ConflictType.MOVE_NOT_POSSIBLE,
                member, new ArrayList());

            data.addConflict(conflict2);

            rep.addConflict(conflict2, data.getMember());

          }
          return null;
        }
      }
      return conflict;
    }

    private Conflict processMemberIsNotStatic(final boolean hasTargetInstance,
        final boolean isVisible,
        final HashMap extraImports) {
      BinMember member = data.getMember();
      Conflict conflict = null;
      List onComplex = resolver.getUsedByThroughComplexExpression(member);
      onComplex.retainAll(usedByRequireDR);

      if (onComplex.size() > 0) {
        data.addConflict(new UnresolvableConflict(this.resolver,
            ConflictType.USED_ON_COMPLEX,
            member, onComplex));
      } else {
        if (hasTargetInstance) {
          if (isVisible) {
            resolver.removeExtraImport(usedByRequireDR,
                resolver.getTargetType());
            return null;
          }

          if (resolver.isStaticResolutionAllowed(member) &&
              importConflictsForNonStatic.size() == 0) {
            conflict = new MRUsedByConflict(this.resolver, member,
                usedByRequireDR);
            MultipleResolveConflict mrConflict =
                (MultipleResolveConflict) conflict;
            mrConflict.addPossibleResolution(new ChangeAccessResolution(
                member,
                true));
            mrConflict.addPossibleResolution(new MakeStaticResolution(
                member,
                usedByRequireDR, extraImports));
          } else {
            conflict = new WeakAccessConflict(this.resolver,
                ConflictType.
                UNMOVABLE_CANNOT_ACCESS,
                member, unmovableMembers,
                new ChangeAccessResolution(member, true));
            //                conflict.setResolution(new ChangeAccessResolution(member, true));
          }
        } else { // if isHasTargetInstance is false then member can be made static since isUnmovableCanAccess is true
          if (resolver.isStaticResolutionAllowed(member)) {
            conflict = new MakeStaticConflict(this.resolver, member,
                usedByRequireDR, extraImports);
            //                conflict.setResolution(new MakeStaticResolution(member,
            //                    usedByRequireDR, extraImports));
          } else {
            data.addConflict(new InstanceNotAccessibleConflict(this.
                resolver,
                member));
          }
        }
      }

      return conflict;
    }

    private Conflict processCanAccessMoveOfUsedByPossible(final boolean
        hasTargetInstance,
        final boolean isVisible, final HashMap extraImports) {

      Conflict conflict = null;
      BinMember member = data.getMember();

      if (member.isStatic()) {
        if (isVisible) {
          return null;
        }

        conflict = new MRUsedByConflict(this.resolver, member, usedByRequireDR);
        MultipleResolveConflict mrConflict = (MultipleResolveConflict)
            conflict;
        mrConflict.addPossibleResolution(new MoveMemberResolution(member,
            usedByRequireDR));
        mrConflict.addPossibleResolution(new ChangeAccessResolution(member, true,
            extraImports));
      } else { // not static
        if (hasTargetInstance) {
          if (isVisible) {
            this.resolver.removeExtraImport(usedByRequireDR,
                this.resolver.targetType);
            return null;
          }

          conflict = new MRUsedByConflict(this.resolver, member,
              usedByRequireDR);
          MultipleResolveConflict mrConflict = (MultipleResolveConflict)
              conflict;
          mrConflict.addPossibleResolution(new ChangeAccessResolution(member, true));
          mrConflict.addPossibleResolution(new MoveMemberResolution(member,
              usedByRequireDR));

          if (this.resolver.isStaticResolutionAllowed(member) &&
              (importConflictsForNonStatic.size() == 0)) {
            mrConflict.addPossibleResolution(
                new MakeStaticResolution(member, usedByRequireDR,
                extraImports));
          }
        } else {
          // if isHasTargetInstance is false then member can be made static
          // since isUnmovableCanAccess is true
          // usual type or static native's inner
          if (this.resolver.isStaticResolutionAllowed(member)) {
            conflict = new MRUsedByConflict(this.resolver, member,
                usedByRequireDR);
            MultipleResolveConflict mrConflict =
                (MultipleResolveConflict) conflict;
            mrConflict.addPossibleResolution(new MoveMemberResolution(member,
                usedByRequireDR));
            mrConflict.addPossibleResolution(new MakeStaticResolution(member,
                usedByRequireDR, extraImports));
            if ((data.getMember() instanceof BinMethod) && isPullDownFromAbstract()) {
              mrConflict.addPossibleResolution(new CreateAbstractDeclarationResolution((BinMethod)data.getMember()));
            }

          }
          // member is field (don't offer to make field as static) or
          // target is nonstatic native's inner
          else {
            List moveAlso = new ArrayList();
            for (Iterator iter = usedByRequireDR.iterator(); iter.hasNext();) {
              BinMember usedBy = (BinMember) iter.next();
              ConflictData usedByData = this.resolver.getConflictData(usedBy);
              if (!usedByData.isSelectedToMove()) {
                moveAlso.add(usedBy);
              }
            }
            if (moveAlso.size() > 0) {
	            conflict = new MoveDependentConflict(this.resolver,
	                ConflictType.MOVE_USEDBY_ALSO,
	                member, moveAlso);
            }
            //              conflict.setResolution(new MoveMemberResolution(member,
            //                  usedByRequireDR));
          }
        }
      }

      return conflict;
    }

    private boolean isPullDownFromAbstract() {
      return (data.getMember().getOwner().getBinCIType().isAbstract() &&
      resolver.getTargetType().getTypeRef().isDerivedFrom(data.getMember().getOwner()));
    }

    private boolean processUsedByRequireDR(final boolean isTargetSuper,
        boolean hasTargetInstance,
        final List membersWithTI) {
      BinMember member = data.getMember();

      for (int i = 0, max = usedByRequireDR.size(); i < max; i++) {
        BinMember usedBy = (BinMember) usedByRequireDR.get(i);
        ConflictData usedByData = this.resolver.getConflictData(usedBy);

        if (this.resolver.isMemberNative(usedBy)) {
          if (isTargetSuper) {

            if (this.resolver.isPossibleToMove(usedByData, data)) {
              continue;
            }

            unmovableMembers.add(usedBy);
          } else { // target is not super
          	Conflict importConflict = null;
          	if (!usedByData.isSelectedToMove()) {
          		importConflict = this.resolver.importManager.
                addExtraImport(member,
                this.resolver.targetType.getTypeRef(), usedBy.getOwner());
          	}
            if (member.isStatic()) {
              if (importConflict != null) {
                importConflictsForStatic.add(importConflict);
              }
            } else { // member is not static
              if (hasTargetInstance) {
                hasTargetInstance = this.resolver.isHasTargetInstance(usedBy,
                    membersWithTI);
              }

              if (importConflict != null) {
                importConflictsForNonStatic.add(importConflict);
              }
            }

            if (usedBy.isStatic()) {
              if (importConflict == null) {
                unmovableMembers.add(usedBy);
                continue;
              }
            }

            if (this.resolver.isPossibleToMove(usedByData, data)) {
              continue;
            }

            unmovableMembers.add(usedBy);
          }
        } else { // not native
          unmovableMembers.add(usedBy);

          if (isTargetSuper) {
            continue;
          } else { // target is not super
            Conflict importConflict = this.resolver.importManager.
                addExtraImport(member,
                this.resolver.targetType.getTypeRef(), usedBy.getOwner());
            if (member.isStatic()) {
              if (importConflict != null) {
                importConflictsForStatic.add(importConflict);
              }
            } else {
              if (hasTargetInstance) {
                hasTargetInstance =
                    (usedBy.getOwner().isDerivedFrom(this.resolver.targetType.
                    getTypeRef()) ||
                    this.resolver.isHasTargetInstance(usedBy, membersWithTI));
              }

              if (importConflict != null) {
                importConflictsForNonStatic.add(importConflict);
              }
            }
          }
        }
      }

      return hasTargetInstance;
    }
  }


  void checkIfTargetAccessible(ConflictData data, List usedByRequireDR) {
    BinMember member = data.getMember();
    BinCIType target = getTargetType();

    // usages changes, so don't use repository

//    Conflict conflict = rep.getConflict(member, ConflictType.
//                                        UNMOVABLE_CANNOT_ACCES_TARGET, target);
//    if ( conflict != null ) {
//      data.addConflict(conflict);
//      return;
//    }
    Conflict conflict = null;

    boolean targetNotAccessible = false;
    if (BinModifier.hasFlag(target.getAccessModifier(), BinModifier.PUBLIC)) {
      return;
    }

    for (int i = 0, max = usedByRequireDR.size(); i < max; i++) {
      BinMember usedBy = (BinMember) usedByRequireDR.get(i);
      if (getConflictData(usedBy).isSelectedToMove()) {
        continue;
      }
//      ConflictData usedByData = this.getConflictData(usedBy);
      if (!target.isAccessible(usedBy.getOwner().getBinCIType())) {
        targetNotAccessible = true;
        break;
      }
    }

    if (targetNotAccessible) {
      conflict = new UnresolvableConflict(this,
          ConflictType.UNMOVABLE_CANNOT_ACCES_TARGET, member,
          Collections.singletonList(getTargetType()));

//      rep.addConflict(conflict, member, getTargetType());
      data.addConflict(conflict);
    }
  }
}
