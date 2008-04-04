/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.refactorings.RefactoringStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author vadim
 */
public class ConflictData {
  private BinMember member;
  private List conflicts = new ArrayList();
  private List usesList = new ArrayList();
  private List usedByList = new ArrayList();

  private int conflictSeverity = RefactoringStatus.UNDEFINED;
  private boolean isSelectedToMove;
  private boolean isDuringChecking;
  private boolean isSelectedByUser;

  public ConflictData(BinMember member) {
    this.member = member;
  }

  public ConflictData(BinMember member, boolean isSelectedToMove) {
    this.member = member;
    this.isSelectedToMove = isSelectedToMove;
  }

  public void addConflict(Conflict conflict) {
    if ((conflict != null) && !conflicts.contains(conflict)) {
      conflicts.add(conflict);
      setConflictSeverity(conflict.getSeverity());
    }
  }

  public void addConflicts(Collection conflicts) {
    if (conflicts == null) {
      return;
    }
    for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
      addConflict((Conflict) i.next());
    }
  }

  public BinMember getMember() {
    return member;
  }

  public void clearConflicts() {
    conflicts.clear();
  }

  public int getConflictSeverity() {
    return conflictSeverity;
  }

  public void setConflictSeverity(int newConflictSeverity) {
    conflictSeverity = Math.max(conflictSeverity, newConflictSeverity);
  }

  public List getConflicts() {
    return Collections.unmodifiableList(conflicts);
  }

  public void addUses(Object o) {
    if (!usesList.contains(o)) {
      usesList.add(o);
    }
  }

  public void addUsedBy(Object o) {
    if (!usedByList.contains(o)) {
      usedByList.add(o);
    }
  }

  public List getUsesList() {
    return Collections.unmodifiableList(usesList);
  }

  public List getUsedByList() {
    return Collections.unmodifiableList(usedByList);
  }

  public List getDependencies() {
    List dependencies = new ArrayList();
    dependencies.addAll(getUsesList());
    dependencies.addAll(getUsedByList());

    return dependencies;
  }

  public boolean isNoConflicts() {
    return (conflicts.size() == 0);
  }

  public boolean resolvedConflictsExist() {
    for (int i = 0, max = conflicts.size(); i < max; i++) {
      if (((Conflict) conflicts.get(i)).isResolved()) {
        return true;
      }
    }

    return false;
  }

  public boolean unresolvedConflictsExist() {
    for (int i = 0, max = conflicts.size(); i < max; i++) {
      if (!((Conflict) conflicts.get(i)).isResolved()) {
        return true;
      }
    }

    return false;
  }

  public boolean unresolvableConflictsExist() {
    for (int i = 0, max = conflicts.size(); i < max; i++) {
      if (!((Conflict) conflicts.get(i)).isResolvable()) {
        return true;
      }
    }

    return false;
  }

  public boolean isValidToMove() {
    return (!unresolvedConflictsExist() && isSelectedToMove);
  }

  public void setIsSelectedToMove(boolean value) {
    this.isSelectedToMove = value;

    if (!value) {
      setIsSelectedByUser(false);
    }
  }

  public boolean isSelectedToMove() {
    return isSelectedToMove;
  }

  public void setIsDuringChecking(boolean value) {
    this.isDuringChecking = value;
  }

  public boolean isDuringChecking() {
    return isDuringChecking;
  }

  public void setIsSelectedByUser(boolean value) {
    this.isSelectedByUser = value;
  }

  public boolean isSelectedByUser() {
    return isSelectedByUser;
  }
}
