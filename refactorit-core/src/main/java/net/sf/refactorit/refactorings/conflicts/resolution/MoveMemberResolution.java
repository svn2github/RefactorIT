/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts.resolution;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.conflicts.ConflictData;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.DialogManager;

import java.util.List;


/**
 *
 * @author vadim
 */
public class MoveMemberResolution extends ConflictResolution {
  private List downMembers;
  private BinMember upMember;
  private boolean isMoveField;

//  private MoveMemberResolution(BinMember upMember, BinMember downMember) {
//    this(upMember, CollectionUtil.singletonArrayList(downMember));
//  }

  public MoveMemberResolution(BinMember upMember, List downMembers) {
    this.upMember = upMember;
    this.downMembers = downMembers;
  }

  public void runResolution(ConflictResolver resolver) {
    boolean isMoveAll = true;
    BinField field = null;

    if (!isMoveField) {
      field = isDownMembersContainNonStaticField();
      if (field != null) {

        String msg = "Do you really want to move field " +
            BinFormatter.format(field) + "?\n" +
            "It can change the functionality of your program!\n" +
            "If you wonder how click Help button.";

        isMoveAll = askIfAcceptChangedFunctionality(msg);
        isMoveField = isMoveAll;
      }
    }

    if (isMoveAll) {
      ConflictData upMemberData = resolver.getConflictData(upMember);
      boolean isDownMembersHaveStatic = false;
      boolean isUpMemberStatic = (upMember.isStatic() ||
          resolver.willBeStatic(resolver.
          getConflictData(upMember)));
      boolean isUpMemberHasInstanceOfTargetType =
          resolver.isMethodHasInstanceOfType(upMember, resolver.getTargetType());

      for (int i = 0, max = downMembers.size(); i < max; i++) {
        BinMember downMember = (BinMember) downMembers.get(i);
        ConflictData downMemberData = resolver.getConflictData(downMember);
        downMemberData.setIsSelectedToMove(true);

        // static uses non-static and static has no target instance -> make non-static as static
        if (isUpMemberStatic) {
          if (!downMember.isStatic()) {
            if (upMemberData.getUsesList().contains(downMember)) {
              if (!isUpMemberHasInstanceOfTargetType) {
                resolver.makeStaticSinceUsedByStatic(downMemberData);
              }
            }
          }
        }
        // non-static is used by static and static has no target instance -> make non-static as static
        else {
          if (!isDownMembersHaveStatic) {
            if (downMember.isStatic()) {
              if (upMemberData.getUsedByList().contains(downMember)) {
                if (!resolver.isMethodHasInstanceOfType(downMember,
                    resolver.getTargetType())) {
                  isDownMembersHaveStatic = true;
                }
              }
            }
          }
        }
      }

      if (isDownMembersHaveStatic) {
        resolver.makeStaticSinceUsedByStatic(resolver.getConflictData(upMember));
      }

      setIsResolved(true);
    } else {
      if (resolver.getConflictData(field).isSelectedToMove()) {
        resolver.runConflictsResolver(field, false);
      }
    }
  }

  private BinField isDownMembersContainNonStaticField() {
    for (int i = 0, max = downMembers.size(); i < max; i++) {
      Object o = downMembers.get(i);
      if ((o instanceof BinField) && !((BinField) o).isStatic()) {
        return (BinField) o;
      }
    }

    return null;
  }

  protected boolean askIfAcceptChangedFunctionality(String msg) {
    int res = DialogManager.getInstance().showYesNoHelpQuestion(
        IDEController.getInstance().createProjectContext(),
        "move.member.field.not_safe",
        msg, "refact.movemember.functionality");

    return (res == DialogManager.YES_BUTTON);
  }

  public String getDescription() {
    return "Move the following members";
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    return new Editor[0];
  }

  public String toString() {
    return "MoveMemberResolution";
  }

  public boolean isMoveField() {
    return isMoveField;
  }

  public List getDownMembers() {
    return downMembers;
  }
}
