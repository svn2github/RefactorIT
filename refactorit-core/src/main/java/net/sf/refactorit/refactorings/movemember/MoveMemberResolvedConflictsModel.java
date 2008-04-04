/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.movemember;


import net.sf.refactorit.refactorings.ConflictsTreeNode;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.ConflictData;
import net.sf.refactorit.refactorings.conflicts.ConflictModel;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;

import java.util.List;


/**
 *
 * @author vadim
 */
public class MoveMemberResolvedConflictsModel extends MoveMemberConflictsModel {
  private ConflictModel conflictModel;
  private ConflictResolver resolver;

  public MoveMemberResolvedConflictsModel(Object root,
      ConflictResolver resolver) {
    super(root);

    this.resolver = resolver;
    this.conflictModel = new ConflictModel(resolver);
  }

  public void update() {
    ConflictsTreeNode root = (ConflictsTreeNode) getRoot();
    root.removeAllChildren();

    List binMembersToMove = resolver.getBinMembersToMove();

    for (int i = 0, max = binMembersToMove.size(); i < max; i++) {
      ConflictData data = resolver.getConflictData(binMembersToMove.get(i));
      if (data.resolvedConflictsExist()) {
        RefactoringStatus status = conflictModel.getResolvedConflictsStatus(
            data);

        if (status == null) {
          continue;
        }

        List entries = status.getEntries();
        addConflictNode((RefactoringStatus.Entry) entries.get(0), root);
      }
    }

    root.sortAllChildren(ConflictsTreeNode.conflictsComparator);
    fireSomethingChanged();
    expandPath();
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Resolved conflicts";
    }

    return null;
  }
}
