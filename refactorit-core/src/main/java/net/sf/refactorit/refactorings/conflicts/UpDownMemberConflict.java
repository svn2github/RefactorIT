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
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.source.edit.Editor;

import java.util.Collections;
import java.util.List;


/**
 *
 * @author vadim
 */
public abstract class UpDownMemberConflict extends UpMemberConflict {
  private List downMembers;

  public UpDownMemberConflict(BinMember upMember, List downMembers) {
    super(upMember);
    this.downMembers = downMembers;
  }

  public void addDownMember(BinMember downMember) {
    CollectionUtil.addNew(downMembers, downMember);
  }

  public abstract void resolve();

  public abstract Editor[] getEditors();

  public abstract ConflictType getType();

  public abstract boolean isResolvable();

  public abstract String getDescription();

  public abstract int getSeverity();

  public List getDownMembers() {
    return Collections.unmodifiableList(downMembers);
  }

  public void removeDownMember(BinMember downMember) {
    downMembers.remove(downMember);
  }

  public void updateDownMembers(List newDownMembers) {
    if (newDownMembers.size() < downMembers.size()) {
      downMembers.clear();
      downMembers.addAll(newDownMembers);
    }
  }
}
