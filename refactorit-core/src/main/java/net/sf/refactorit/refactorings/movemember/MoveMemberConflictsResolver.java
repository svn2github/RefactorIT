/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.refactorings.conflicts.ConflictData;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.refactorings.conflicts.MultipleResolveConflict;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.MakeStaticResolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Vadim Hahhulin, Anton Safonov
 */
public class MoveMemberConflictsResolver extends ConflictResolver {
  private ReferenceUpdater referenceUpdater;

  public MoveMemberConflictsResolver(List selectedMembers,
      BinCIType nativeType,
      BinCIType targetType,
      ReferenceUpdater referenceUpdater) {
    super(selectedMembers, targetType, nativeType);

    this.referenceUpdater = referenceUpdater;

    for (int i = 0, max = selectedMembers.size(); i < max; i++) {
      runConflictsResolver((BinMember) selectedMembers.get(i), true);
    }
  }

  protected void resolveConflicts(List conflictData) {
    this.importManager.clear();

    if (getTargetType() != null) {
      this.referenceUpdater.analyze(getBinMembersToMove(true),
          getMembersToBeStatic(),
          getTargetType());
      this.importManager.setParamsToSkip(this.referenceUpdater.getParamsToSkip());
    }

    super.resolveConflicts(conflictData);
  }

  private List getMembersToBeStatic() {
    List result = new ArrayList();
    List binMembersToMove = getBinMembersToMove();

    for (int i = 0, max = binMembersToMove.size(); i < max; i++) {
      Object memberToMove = binMembersToMove.get(i);

      if (((BinMember) memberToMove).isStatic()) {
        result.add(memberToMove);
      } else {
        List conflicts = getConflictData(memberToMove).getConflicts();
        for (int j = 0, maxJ = conflicts.size(); j < maxJ; j++) {
          if (((Conflict) conflicts.get(j)).getResolution() instanceof
              MakeStaticResolution) {
            result.add(memberToMove);

//System.err.println("added1: " + memberToMove);
            // XXX added by Maddy, absolutely not sure if it is the right thing to do!!!
//            Iterator it = getUses((BinMember) memberToMove);
//            while (it.hasNext()) {
//              Object uses = it.next();
//              result.add(uses);
//System.err.println("added2: " + uses);
//            }
            // XXX
          }
        }
      }
    }

    return result;
  }

  public void runConflictsResolver(BinMember selectedMember, boolean isSelected) {
    if (selectedMember == null || !isValidToMove(selectedMember)) {
      return;
    }

    if (isSelected) {
      ConflictData data = getConflictData(selectedMember);
      if (data == null) {
        data = new MemberToMoveData(selectedMember);
        addConflictData(selectedMember, data);
      }

      data.setIsSelectedToMove(true);
      data.setIsSelectedByUser(true);
      resolveDependencies(selectedMember, new HashSet());
    } else {
      removeMemberAndItsDeps(selectedMember);
    }

    resolveConflicts();
  }

  private void resolveDependencies(BinMember memberToCheck, HashSet visited) {
    if (visited.contains(memberToCheck)) {
      return;
    }

    List dependencies = new ArrayList();

//    ProgressMonitor.Progress progressArea
//        = new ProgressMonitor.Progress(0, 100);
//    ProgressListener listener = (ProgressListener)
//        CFlowContext.get(ProgressListener.class.getName());

    traverseIterator(getUses(memberToCheck), dependencies, memberToCheck, true);

    traverseIterator(getUsedBy(memberToCheck), dependencies, memberToCheck, false);

    visited.add(memberToCheck);

    for (int i = 0, max = dependencies.size(); i < max; i++) {
      BinMember dependant = (BinMember) dependencies.get(i);
      if (!dependant.getOwner().equals(getNativeType().getTypeRef())) {
        continue;
      }
      resolveDependencies(dependant, visited);
//      if (listener != null) {
//        listener.progressHappened(progressArea.getPercentage(i, max));
//      }
    }
  }

  private void traverseIterator(Iterator iterator, List dependencies,
      BinMember member, boolean isUses) {
    while (iterator.hasNext()) {
      BinMember dependency = (BinMember) iterator.next();
      if (member == dependency) {
        continue;
      }

      if (getConflictData(dependency) == null) {
        MemberToMoveData data = new MemberToMoveData(dependency, false);
        CollectionUtil.addNew(dependencies, dependency);
        addConflictData(dependency, data);
      }

      if (isUses) {
        getConflictData(member).addUses(dependency);
      } else {
        getConflictData(member).addUsedBy(dependency);
      }
    }
  }

  private void removeMemberAndItsDeps(BinMember member) {
    List membersToRemove = new ArrayList();
    membersToRemove.add(member);

    ConflictData conflictData = getConflictData(member);
    if (conflictData == null) {
      return;
    }

    collectDependencies(conflictData.getDependencies(), membersToRemove);

    for (int i = 0, max = membersToRemove.size(); i < max; i++) {
      BinMember memberToRemove = (BinMember) membersToRemove.get(i);
      ConflictData data = getConflictData(memberToRemove);

      if ((memberToRemove != member) && data.isSelectedByUser()) {
        continue;
      }

      data.setIsSelectedToMove(false);
    }
  }

  private void collectDependencies(List dependencies, List membersToRemove) {
    for (int i = 0, max = dependencies.size(); i < max; i++) {
      Object o = dependencies.get(i);

      if (membersToRemove.contains(o) ||
          !getConflictData(o).isSelectedToMove() ||
          getConflictData(o).isSelectedByUser()) {
        continue;
      }

      membersToRemove.add(o);
      collectDependencies(getConflictData(o).getDependencies(),
          membersToRemove);
    }

//    leaveDependencyIfUsedByRetainedMember(membersToRemove);
  }

//  private void leaveDependencyIfUsedByRetainedMember(List membersToRemove) {
//    List membersToMoveData = getAllConflictData();
//
//    for (int i = 0, max = membersToMoveData.size(); i < max; i++) {
//      MemberToMoveData memberToMoveData = (MemberToMoveData)membersToMoveData.get(i);
//
//      if (membersToRemove.contains(memberToMoveData.getMember())) {
//        continue;
//      }
//
//      List deps = memberToMoveData.getDependencies();
//      for (int j = 0, maxJ = deps.size(); j < maxJ; j++) {
//        if (membersToRemove.contains(deps.get(j))) {
//          membersToRemove.remove(deps.get(j));
//        }
//      }
//    }
//  }

  public void setTargetType(BinCIType targetType) {
    super.setTargetType(targetType);
    resolveConflicts();

//    printTree(); //innnnn
  }

  public void printTree() {
    List conflictData = getAllConflictData();
    for (int i = 0, max = conflictData.size(); i < max; i++) {
      ConflictData data = (ConflictData) conflictData.get(i);
      System.out.println("binMember:" + data.getMember() +
          "; isSelectedToMove:" + data.isSelectedToMove());

      System.out.println("  Uses:");
      List usesList = data.getUsesList();
      for (int j = 0, maxJ = usesList.size(); j < maxJ; j++) {
        System.out.println("    " + usesList.get(j));
      }

      System.out.println("  UsedBy:");
      List usedByList = data.getUsedByList();
      for (int j = 0, maxJ = usedByList.size(); j < maxJ; j++) {
        System.out.println("    " + usedByList.get(j));
      }

      List conflicts = data.getConflicts();
      System.out.println("  Conflicts:");
      for (int k = 0, maxK = conflicts.size(); k < maxK; k++) {
        Conflict conflict = (Conflict) conflicts.get(k);
        System.out.println("    " + conflict.toString());

        System.out.println("    Resolutions:");
        if (conflict instanceof MultipleResolveConflict) {
          List resolutions = ((MultipleResolveConflict) conflict).
              getPossibleResolutions();
          for (int m = 0, maxM = resolutions.size(); m < maxM; m++) {
            System.out.println("      " + resolutions.get(m).toString()); //innnnn
          }
        } else {
          ConflictResolution resolution = conflict.getResolution();
          if (resolution != null) {
            System.out.println("      " + conflict.getResolution().toString());
          }
        }
      }

      System.out.println(); //innnnn
    }
  }
}
