/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movetype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.DialogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Anton Safonov
 * @author Risto
 */
public class MoveTypeAnalyzer {
  /** types to be moved, contains {@link BinCIType}s */
  private List types;
  private BinPackage targetPackage;

  private TypeDependencies usages = new TypeDependencies();

  private boolean changeMemberAccess;

  private List membersToTurnToPublic = new ArrayList();
  private List membersToTurnToPackagePrivate = new ArrayList();
  private List membersToTurnToProtected = new ArrayList();


  private HashSet confirmedSources = new HashSet(1);

  public MoveTypeAnalyzer(boolean changeMemberAccess) {
    this.changeMemberAccess = changeMemberAccess;
  }

  public List getTypes() {
    return this.types;
  }

  public void setTypes(final List types) {
    this.types = types;
  }

  public void setChangeMemberAccess(boolean b) {
    this.changeMemberAccess = b;
  }

  public List getMembersToTurnToPublic() {
    return this.membersToTurnToPublic;
  }

  public List getMembersToTurnToPackagePrivate() {
    return this.membersToTurnToPackagePrivate;
  }

  public List getMembersToTurnToProtected() {
    return this.membersToTurnToProtected;
  }

  public boolean isMoving(final BinMember member) {
    BinMember search = member;
    do {
      if (types.contains(search)) {
        return true;
      }
      BinTypeRef owner = search.getOwner();
      if (owner == null) {
        search = null;
      } else {
        search = owner.getBinCIType();
      }
    } while (search != null);

    return false;
  }

  /*public class TimeDiffPrinter {
    public long lastTime;

    public void start() {
      lastTime = System.currentTimeMillis();
    }

    public void checkpoint(String s) {
      long checkpointTime = System.currentTimeMillis();
      System.err.println( "Time diff: " + (checkpointTime - lastTime) + "    -- " + s );
      lastTime = checkpointTime;
    }
     }*/

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();

    status.merge(checkDuplicateNamesInMovedTypes());
    status.merge(checkedMovedTypesAreFromSourcePath());
    status.merge(checkForLocalTypes());

    return status;
  }

  public RefactoringStatus checkForLocalTypes() {
    RefactoringStatus result = new RefactoringStatus();

    for (int i = 0; i < types.size(); i++) {
      BinCIType type = (BinCIType) types.get(i);

      if (type.isLocal()) {
        result.addEntry("Moving of local types not supported: " + type.getName(),
            RefactoringStatus.ERROR);
      }
    }

    return result;
  }

  public RefactoringStatus calculateConflicts(BinPackage targetPackage) {
    this.targetPackage = targetPackage;

    RefactoringStatus status = new RefactoringStatus();

    usages.findUsages(types, MoveType.ANALYZER_FINDS_USAGES);

    status.merge(checkDuplicateNamesInMovedTypes());
    status.merge(checkTargetPackage(types, targetPackage));

    status.merge(isAllWritable(usages));
    if (status.isErrorOrFatal()) {
      return status;
    }

    ProgressMonitor.Progress progress = MoveType.CONFLICT_CALCULATION;

    for (int i = 0; i < types.size(); i++) {
      final BinCIType type = (BinCIType) types.get(i);

      if (isInGuarded(type, usages)) {
        status.merge(new RefactoringStatus("", RefactoringStatus.CANCEL));
      }

      status.merge(importConflictsInThisSource(type, usages));
      status.merge(importConflictsInOtherSources(type, progress.subdivision(i,
          types.size())));

      if (!type.getPackage().isIdentical(targetPackage)) {
        status.merge(packageOrProtectedAccessMembersOfTypeUsed(getMemberUsages(
            type)));
        status.merge(usesOtherPackageOrProtectedAccessMembers(usages));
        //status.merge(superMembersBecomeInaccessible(type));
        //status.merge(thisPackageOrProtectedAccessMembersMayBecomeInaccesible(type, types));
      }

      if (type.isInnerType()) {
        status.merge(privateMembersInToplevelEnclosingClassUsedByMovedType(type,
            usages));
        status.merge(privateMembersOfMovedTypeUsedInToplevelEnclosingClass(type,
            usages));
      }
    }

    usages.clear();

    return status;
  }

  public static RefactoringStatus checkTargetPackage(List types,
      BinPackage targetPackage) {
    RefactoringStatus status = new RefactoringStatus();

    if (targetPackage == null) {
      status.addEntry("", RefactoringStatus.CANCEL);
      return status;
    }

    for (int i = 0; i < types.size(); i++) {
      final BinCIType type = (BinCIType) types.get(i);
      status.merge(isTargetPackageContainsTypeAlready(type, targetPackage));
    }

    return status;
  }

  /** Only deals with package access -- only visits types in type's own package */
  private List getMemberUsages(final BinCIType type) {
    ManagingIndexer supervisor = new ManagingIndexer(ProgressMonitor.Progress.
        DONT_SHOW);

    BinClassSearchFilter filter = new BinClassSearchFilter(false, true);
    filter.setIncludeNewExpressions(true);
    new TypeIndexer(supervisor, type, filter);

    type.getPackage().accept(supervisor);
    return supervisor.getInvocations();
  }

  RefactoringStatus checkDuplicateNamesInMovedTypes() {
    RefactoringStatus result = new RefactoringStatus();

    List typeNames = new ArrayList();

    for (int i = 0; i < types.size(); i++) {
      BinType type = (BinType) types.get(i);

      if (typeNames.contains(type.getName())) {
        result.addEntry("Duplicate type name among selected types: "
            + type.getName() + " (" + type.getQualifiedName() + ")",
            RefactoringStatus.ERROR);
      } else {
        typeNames.add(type.getName());
      }
    }

    return result;
  }

  RefactoringStatus checkedMovedTypesAreFromSourcePath() {
    RefactoringStatus result = new RefactoringStatus();

    for (int i = 0; i < types.size(); i++) {
      BinCIType type = (BinCIType) types.get(i);

      if (!type.isFromCompilationUnit()) {
        result.addEntry("Type is not from the source path: "
            + type.getQualifiedName(), RefactoringStatus.ERROR);
      }
    }

    return result;
  }

  private RefactoringStatus
      privateMembersInToplevelEnclosingClassUsedByMovedType(BinCIType movedType,
      TypeDependencies usages) {
    RefactoringStatus result = new RefactoringStatus();

    List u = getUsagesFromType(movedType, usages.usesList);

    for (int i = 0; i < u.size(); i++) {
      InvocationData data = (InvocationData) u.get(i);

      if (data.getWhat() instanceof BinMember) {

        BinMember member = (BinMember) data.getWhat();
        BinCIType called = MoveTypeUtil.getType(member);

        if (member.isPrivate() && called != movedType &&
            member.getTopLevelEnclosingType()
            == movedType.getTopLevelEnclosingType()) {
          if (changeMemberAccess) {
            if (targetPackage.isIdentical(movedType.getPackage())) {
              addAccessChange(membersToTurnToPackagePrivate, member);
            } else {
              if (!(member instanceof BinCIType)
                  || canBePublicOrProtected((BinCIType) member)) {
                if (movedType.getTypeRef().isDerivedFrom(called.getTypeRef())) {
                  addAccessChange(membersToTurnToProtected, member);
                } else {
                  addAccessChange(membersToTurnToPublic, member);
                }
              } else {
                result.addEntry(member.getQualifiedName()
                    + " will become inaccessible",
                    RefactoringStatus.ERROR);
              }
            }
          } else {
            result.addEntry(member.getQualifiedName()
                + " will become inaccessible",
                RefactoringStatus.ERROR);
          }
        }
      }
    }

    return result;
  }

  private RefactoringStatus
      privateMembersOfMovedTypeUsedInToplevelEnclosingClass(BinCIType movedType,
      TypeDependencies usages) {
    RefactoringStatus result = new RefactoringStatus();

    List u = getUsagesFromSource(movedType.getCompilationUnit(),
        usages.usedList);

    for (int i = 0; i < u.size(); i++) {
      InvocationData data = (InvocationData) u.get(i);

      if (data.getWhat() instanceof BinMember) {
        BinMember memberCalled = (BinMember) data.getWhat();

        // If a private member is used from outside its own type then it is always used from a toplevel enclosing class.
        if (usedFromSameFileButNotInsideMovedType(data,
            movedType) && memberCalled.isPrivate()) {
          if (changeMemberAccess) {
            if (movedType.getPackage().isIdentical(targetPackage)) {
              addAccessChange(membersToTurnToPackagePrivate, memberCalled);
            } else {
              if (!(memberCalled instanceof BinCIType)
                  || canBePublicOrProtected((BinCIType) memberCalled)) {
                addAccessChange(membersToTurnToPublic, memberCalled);
              } else {
                result.addEntry(memberCalled.getQualifiedName()
                    + " will become inaccessible",
                    RefactoringStatus.ERROR);
              }
            }
          } else {
            result.addEntry(memberCalled.getQualifiedName()
                + " will become inaccessible",
                RefactoringStatus.ERROR);
          }
        }
      }
    }

    return result;
  }

  private static boolean usedFromSameFileButNotInsideMovedType(InvocationData
      data, BinCIType movedType) {
    if (data.getWhere() instanceof BinTypeRef
        && ((BinTypeRef) data.getWhere()).isReferenceType()) {
      return MoveType.typeIsInSameFileButNotInsideMovedType(
          ((BinTypeRef) data.getWhere()).getBinCIType(), movedType);
    } else if (data.getWhere() instanceof CompilationUnit) {
      return ((CompilationUnit) data.getWhere()).getSource().getAbsolutePath().
          equals(
          movedType.getCompilationUnit().getSource().getAbsolutePath());
    } else if (data.getWhere() instanceof BinCIType) {
      return MoveType.typeIsInSameFileButNotInsideMovedType(
          (BinCIType) data.getWhere(), movedType);
    } else {
      return MoveType.typeIsInSameFileButNotInsideMovedType(
          MoveTypeUtil.getType((BinMember) data.getWhere()), movedType);
    }
  }

  private List getUsagesFromType(BinCIType type, List allUsages) {
    List result = new ArrayList();

    for (int i = 0; i < allUsages.size(); i++) {
      InvocationData usage = (InvocationData) allUsages.get(i);

      if (usage.getWhereType().getBinCIType().getQualifiedName().equals(type.
          getQualifiedName())) {
        result.add(usage);
      }
    }

    return result;
  }

  private List getUsagesFromSource(CompilationUnit compilationUnit,
      List allUsages) {
    List result = new ArrayList();

    for (int i = 0; i < allUsages.size(); i++) {
      InvocationData usage = (InvocationData) allUsages.get(i);

      if (usage.getWhereType() != null) {
        CompilationUnit usageCompilationUnit = usage.getWhereType().
            getBinCIType().
            getCompilationUnit();
        if (usageCompilationUnit != null
            && usageCompilationUnit.getSource().getAbsolutePath().equals(
            compilationUnit.getSource().getAbsolutePath())) {
          result.add(usage);
        }
      }
    }

    return result;
  }

  public RefactoringStatus importConflictsInThisSource(BinCIType movedType,
      TypeDependencies usages) {
    RefactoringStatus result = new RefactoringStatus();

    for (Iterator i = getTypesCalledWithoutFqn(movedType, usages.usesList);
        i.hasNext(); ) {
      BinCIType calledWithoutFqn = (BinCIType) i.next();

      if (containsAnotherTypeWithSameName(calledWithoutFqn) &&
          movedType.getCompilationUnit().importsOnlyOnDemand(calledWithoutFqn)) {
        result.addEntry(calledWithoutFqn.getQualifiedName()
            + ": name also present in target package",
            RefactoringStatus.ERROR);
      }
    }

    return result;
  }

  private boolean containsAnotherTypeWithSameName(BinCIType type) {
    return
        !type.getPackage().isIdentical(targetPackage) &&
        targetPackage.findTypeForShortName(type.getNameWithAllOwners()) != null;
  }

  /**
   * Excludes implicit java.lang.Object references (these come when the caller extends "nothing").
   */
  private Iterator getTypesCalledWithoutFqn(BinCIType caller,
      List invocationDatas) {
    Set result = new HashSet();

    for (int i = 0; i < invocationDatas.size(); i++) {
      InvocationData data = (InvocationData) invocationDatas.get(i);

      if ((!ImportUtils.isFqnUsage(caller.getCompilationUnit(),
          data.getWhereAst())) &&
          data.getWhat() instanceof BinMember) {
        BinCIType typeCalled = MoveTypeUtil.getType((BinMember) data.getWhat());
        BinCIType callerInUsage = data.getWhereType().getBinCIType();

        if (callerInUsage == caller &&
            (!implicitJavaLangObjectInvocation(data, caller, typeCalled))) {
          result.add(typeCalled);
        }
      }
    }

    return result.iterator();
  }

  private static boolean implicitJavaLangObjectInvocation(InvocationData data,
      BinCIType caller, BinCIType calledInIvocation) {
    boolean invokedByExtendingIt = data.getWhere() instanceof BinTypeRef &&
        ((BinTypeRef) data.getWhere()).getBinType() == caller;

    boolean objectExplicitlyWritten
        = data.getWhereAst().getText().indexOf("Object") >= 0;

    return calledInIvocation.getQualifiedName().equals("java.lang.Object") &&
        invokedByExtendingIt && (!objectExplicitlyWritten);
  }

  public RefactoringStatus importConflictsInOtherSources(BinCIType type,
      ProgressMonitor.Progress progress) {
    RefactoringStatus result = new RefactoringStatus();

    List whereTypesWithSameNameAreUsed
        = getWhereTypesWithSameNameAreUsed(type, progress);

    for (int i = 0; i < whereTypesWithSameNameAreUsed.size(); i++) {
      InvocationData data = (InvocationData) whereTypesWithSameNameAreUsed.get(
          i);

      if (!ImportUtils.isFqnUsage(data.getCompilationUnit(), data.getWhereAst())
          && data.getWhat() instanceof BinMember) {
        BinCIType calledType = MoveTypeUtil.getType((BinMember) data.getWhat());
        BinCIType caller = data.getWhereType().getBinCIType();

        result.merge(checkSameNameTypeUsageConflict(
            caller, calledType, data.getWhereAst().getLine()));
      }
    }

    return result;
  }

  private RefactoringStatus checkSameNameTypeUsageConflict(BinCIType caller,
      BinCIType calledType, int usageLineNumber) {

    if (caller.getCompilationUnit().importsOnlyOnDemand(calledType)
        && caller.hasAccessToAllPublicClassesIn(targetPackage)) {
      return new RefactoringStatus(
          "Conflicting reference of " + calledType.getQualifiedName() + " in " +
          caller.getQualifiedName() + " (line " + usageLineNumber + ")",
          RefactoringStatus.ERROR);
    } else {
      return null;
    }
  }

  private List getWhereTypesWithSameNameAreUsed(BinCIType type,
      ProgressMonitor.Progress progress) {
    return TypeDependencies.getTypesUsedByName(getOtherTypesWithSameName(type),
        progress);
  }

  private List getOtherTypesWithSameName(BinCIType type) {
    String typeName = MoveType.isExtract(types, type)
        ? type.getName()
        : type.getNameWithAllOwners();

    List result = new ArrayList();

    BinPackage[] packages = type.getProject().getAllPackages();

    for (int i = 0; i < packages.length; i++) {
      if (!packages[i].isIdentical(type.getPackage())) {
        BinTypeRef typeRef = packages[i].findTypeForShortName(typeName);
        if (typeRef != null) {
          result.add(typeRef.getBinCIType());
        }
      }
    }

    return result;
  }

  public RefactoringStatus isAllWritable(final TypeDependencies usages) {
//    RefactoringStatus status = new RefactoringStatus();

    final List sources = new ArrayList(usages.used.keySet().size() + 1);
    sources.addAll(usages.used.keySet());
    for (int i = 0; i < types.size(); i++) {
      BinType type = (BinType) types.get(i);
      sources.add(type.getCompilationUnit());
    }

    final List notModifiable = new ArrayList();

    Iterator it = sources.iterator();
    while (it.hasNext()) {
      final CompilationUnit source = (CompilationUnit) it.next();
      if (!source.getSource().canWrite()) {
        CollectionUtil.addNew(notModifiable, source);
      }
    }

    if (notModifiable.size() > 0) {
      return new RefactoringStatus(
          "Source files are write protected",
          notModifiable, RefactoringStatus.ERROR);
    }

    return null;
  }

  private boolean isInGuarded(final BinCIType type,
      final TypeDependencies usages) {
    List guarded = new ArrayList();
    String guardedNames = "";

    for (int i = 0, max = usages.usedList.size(); i < max; i++) {
      final InvocationData data
          = (InvocationData) usages.usedList.get(i);
      final CompilationUnit source = data.getCompilationUnit();

      if (guarded.contains(source)) {
        continue;
      }

      // HACK: to avoid "asking dialog" many times

      if (!confirmedSources.contains(source) && source.isWithinGuardedBlocks(
          data.getWhereAst().getLine(), data.getWhereAst().getColumn())) {
        guarded.add(source);
        confirmedSources.add(source);
        if (guardedNames.length() > 0) {
          guardedNames += "\n";
        }
        guardedNames += "  " + source.getDisplayPath();

      }
    }

    if (guardedNames.length() > 0) {
      String message;
      if (types.size() <= 1) {
        message =
            "Given " + type.getMemberType() + " " + type.getQualifiedName()
            + "\nis used in guarded sections of the following files:"
            + "\n" + guardedNames
            + "\n\n" + "Moving it to another package may lead to errors unless"
            + "\nthose sections are edited manually."
            + "\nContinue with move?";
      } else {
        message =
            "Given types are used in guarded sections of the following files:"
            + "\n" + guardedNames
            + "\n\n"
            + "Moving them to another package may lead to errors unless"
            + "\nthose sections are edited manually."
            + "\nContinue with move?";
      }
      int res = DialogManager.getInstance().showYesNoQuestion(
          IDEController.getInstance().createProjectContext(),
          "warning.movetype.guarded",
          message, DialogManager.NO_BUTTON);

      return res != DialogManager.YES_BUTTON;
    }

    return false;
  }

  public static RefactoringStatus isTargetPackageContainsTypeAlready(final
      BinCIType type, BinPackage targetPackage) {
    if (type.getPackage() != targetPackage
        || type.isInnerType()) {
      if (targetPackage.findTypeForShortName(type.getName()) != null) {
        return new RefactoringStatus(
            "Target package already contains one of the types with such name: "
            + type.getName() + "!",
            RefactoringStatus.ERROR);
      }
    }

    return null;
  }

  private RefactoringStatus packageOrProtectedAccessMembersOfTypeUsed(
      final List usedList) {
    List items = new ArrayList();
    List inaccessible = new ArrayList();

    for (int i = 0, max = usedList.size(); i < max; i++) {
      final InvocationData data = (InvocationData) usedList.get(i);
      if (data.getWhat() != null && calledItemWillBeInaccessible(data)) {
        Object loc = data.getWhere();
        if (loc instanceof BinTypeRef
            && ((BinTypeRef) loc).isReferenceType()) {
          loc = ((BinTypeRef) loc).getBinCIType();
        }

        if (loc instanceof BinMember && !isMoving((BinMember) loc)) {
          if (changeMemberAccess) {
            if (!addMemberToPublicOrProtectedCandidateList(data)) {
              CollectionUtil.addNew(inaccessible, loc);
            }
          } else {
            CollectionUtil.addNew(items, loc);
          }
        }
      }
    }

    RefactoringStatus status = new RefactoringStatus();
    if (inaccessible.size() > 0) {
      status.addEntry(
          "Types to move will become inaccessible for",
          inaccessible,
          RefactoringStatus.ERROR);
    }
    if (items.size() > 0) {
      status.addEntry(
          "Members of types to move will become inaccessible for",
          items,
          RefactoringStatus.ERROR);
    }

    return status;
  }

  /** Only deals with "package private" and "protected" access; undertands synthetic constructors. */
  private boolean calledItemWillBeInaccessible(InvocationData data) {
    BinMember itemCalled = (BinMember) data.getWhat();

    if (willBeTurnedToPublicOnMove(itemCalled) && isMoving(itemCalled)) {
      return false;
    }

    if (itemCalled.isPackagePrivate()) {
      return true;
    }

    if (itemCalled.isProtected() && !invokedFromSubclass(data)) {
      return true;
    }

    return false;
  }

  private boolean willBeTurnedToPublicOnMove(BinMember member) {
    if (member instanceof BinConstructor
        && ((BinConstructor) member).isSynthetic()) {
      return willBeTurnedToPublicOnMove(member.getOwner().getBinCIType());
    } else if (!(member instanceof BinCIType)) {
      return false;
    }

    BinCIType type = (BinCIType) member;

    if (!type.isFromCompilationUnit()) {
      return false;
    }

    return
        MoveType.isExtract(types, type) &&
        MoveType.willBeTurnedToPublicOnExtract(type, targetPackage, types);
  }

  private boolean invokedFromSubclass(InvocationData data) {
    BinTypeRef invokersType = data.getWhereType();
    BinTypeRef invokedType = MoveTypeUtil.getType((BinMember) data.getWhat()).
        getTypeRef();

    return invokersType.isDerivedFrom(invokedType);
  }

  private RefactoringStatus usesOtherPackageOrProtectedAccessMembers(
      final TypeDependencies usages) {
    List items = new ArrayList();
    List inaccessible = new ArrayList();

    for (int i = 0, max = usages.usesList.size(); i < max; i++) {
      final InvocationData data = (InvocationData) usages.usesList.get(i);

      if (data.getWhat() != null
          && !isMoving((BinMember) data.getWhat())) {
        BinMember member = (BinMember) data.getWhat();

        while (member != null) {
          if (calledItemWillBeInaccessible(data)) {
            if (changeMemberAccess) {
              if (!addMemberToPublicOrProtectedCandidateList(data)) {
                CollectionUtil.addNew(inaccessible, member);
              }
            } else {
              CollectionUtil.addNew(items, member);
            }
          }

          BinTypeRef owner = member.getOwner();
          if (owner != null) {
            member = owner.getBinCIType();
          } else {
            member = null;
          }
        }
      }
    }

    RefactoringStatus status = new RefactoringStatus();
    if (inaccessible.size() > 0) {
      status.addEntry(
          "Following classes will become inaccessible",
          inaccessible,
          RefactoringStatus.ERROR);
    }
    if (items.size() > 0) {
      status.addEntry(
          "Types to move are using other members with package private or protected access",
          items,
          RefactoringStatus.WARNING);
    }

    return status;
  }

  private boolean addMemberToPublicOrProtectedCandidateList(
      final InvocationData data) {
    BinMember memberCalled = (BinMember) data.getWhat();
    boolean isInterfaceOrAbstract = false;

    if (memberCalled instanceof BinCIType) {
      isInterfaceOrAbstract = ((BinCIType) memberCalled).isInterface()
          || ((BinCIType) memberCalled).isAbstract();
    }

    if (memberCalled instanceof BinCIType
        && !canBePublicOrProtected((BinCIType) memberCalled)) {
      return false;
    }

    Object loc = data.getWhere();
    if (loc instanceof BinTypeRef
        && ((BinTypeRef) loc).isReferenceType()) {
      loc = ((BinTypeRef) loc).getBinCIType();
    }

    BinCIType called = MoveTypeUtil.getType(memberCalled);
    BinCIType caller = MoveTypeUtil.getType((BinMember) loc);

    boolean protectedIsEnough
        = caller.getTypeRef().isDerivedFrom(called.getTypeRef());

    //interface can not be protected
    //abstract can not be protected
    if (protectedIsEnough && !isInterfaceOrAbstract) {
      if ((memberCalled).isPackagePrivate()) {
        addAccessChange(membersToTurnToProtected, memberCalled);
      }
    } else {
      addAccessChange(membersToTurnToPublic, memberCalled);
    }

    return true;
  }

  /*private RefactoringStatus superMembersBecomeInaccessible(final BinCIType type) {
    List inaccessibleMembers = new ArrayList();

    BinMethod[] methods = type.getAccessibleMethods(type);
    for (int i = 0; i < methods.length; i++) {
      final BinMethod method = methods[i];

      if (method.isPackagePrivate() && !isMoving(method)) {
        CollectionUtil.addNew(inaccessibleMembers, method);
      }
    }

    List fields = type.getAccessibleFields(type);
    for (int i = 0; i < fields.size(); i++) {
      final BinField field = (BinField) fields.get(i);

      if (field.isPackagePrivate() && !isMoving(field)) {
        CollectionUtil.addNew(inaccessibleMembers, field);
      }
    }

    List supers = type.getTypeRef().getSupertypes();
    for (int i = 0; i < supers.size(); i++) {
      final BinTypeRef superType = (BinTypeRef) supers.get(i);

      List inners = superType.getBinCIType().getAccessibleInners(type);
      for (int k = 0; k < inners.size(); k++) {
        final BinCIType inner = (BinCIType) inners.get(k);

        if (inner.isPackagePrivate() && !isMoving(inner)) {
          CollectionUtil.addNew(inaccessibleMembers, inner);
        }
      }
    }

    if (inaccessibleMembers.size() > 0) {
      return new RefactoringStatus(
   "Current code will work, but these inherited members won't be visible:",
          inaccessibleMembers,
          RefactoringStatus.INFO);
    }

    return null;
     }*/

  /*private RefactoringStatus thisPackageOrProtectedAccessMembersMayBecomeInaccesible(
      final BinCIType type,
      final List types) {
    if (type.getTypeRef().getAllSubclasses().size() <= 0) {
      return null;
    }

    List packagePrivateMembers = new ArrayList();

    BinField[] fields = type.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i].isPackagePrivate() || fields[i].isProtected()) {
        CollectionUtil.addNew(packagePrivateMembers, fields[i]);
      }
    }

    BinMethod[] methods = type.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].isPackagePrivate() || methods[i].isProtected()) {
        CollectionUtil.addNew(packagePrivateMembers, methods[i]);
      }
    }

    BinTypeRef[] inners = type.getDeclaredTypes();
    for (int i = 0; i < inners.length; i++) {
      if (types.contains(inners[i].getBinCIType())) {
        continue; // the user has already read the warning before got inner to the types list
      }
      if (inners[i].getBinCIType().isPackagePrivate() || inners[i].getBinCIType().isProtected()) {
        CollectionUtil.addNew(packagePrivateMembers, inners[i].getBinCIType());
      }
    }

    if (type.isClass()) {
      final BinMethod[] cnstrs
          = ((BinClass) type).getDeclaredConstructors();
      for (int i = 0; i < cnstrs.length; i++) {
        if (cnstrs[i].isPackagePrivate() || cnstrs[i].isProtected()) {
          CollectionUtil.addNew(packagePrivateMembers, cnstrs[i]);
        }
      }
    }

    if (packagePrivateMembers.size() > 0) {
      return new RefactoringStatus(
   "Current code will work, but these will be inaccessible in source package:",
          packagePrivateMembers,
          RefactoringStatus.INFO);
    }

    return null;
     }*/

  private void addAccessChange(List list, Object member) {
    if (member instanceof BinConstructor
        && ((BinConstructor) member).isSynthetic()) {
      member = ((BinConstructor) member).getOwner().getBinCIType();
    }
    
    // hack ;( 
    // you cannot change a binary file!
    // (actually, an error message would be better, that this return statement
    if(member instanceof BinMember) {
      BinType type = null;
      
      if(member instanceof BinType) {
        type = (BinType)member;
      } else {
        type = ((BinMember)member).getParentType();
      }
      
      if(!type.isFromCompilationUnit()) {
        return;
      }
    }
    CollectionUtil.addNew(list, member);
  }

  private boolean canBePublicOrProtected(BinCIType type) {
    if (!type.isNameMatchesSourceName() && !isMoving(type)) {
      return false;
    }

    return true;
  }
}
