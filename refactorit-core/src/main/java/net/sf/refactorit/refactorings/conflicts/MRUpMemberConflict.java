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
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.edit.Editor;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author vadim
 */
public abstract class MRUpMemberConflict extends UpMemberConflict implements
    MultipleResolveConflict {

  public List resolutions = new ArrayList();

  public MRUpMemberConflict(BinMember upMember) {
    super(upMember);
  }

  public int getSeverity() {
    return RefactoringStatus.QUESTION;
  }

  public String getDescription() {
    return "";
  }

  public boolean isResolvable() {
    return true;
  }

  public void addPossibleResolution(ConflictResolution resolution) {
    resolutions.add(resolution);
  }

  public List getPossibleResolutions() {
    return resolutions;
  }

  public abstract Editor[] getEditors();

  public abstract ConflictType getType();

  public abstract void resolve();

}
