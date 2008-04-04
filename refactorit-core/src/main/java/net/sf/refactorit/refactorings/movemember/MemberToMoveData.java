/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.refactorings.conflicts.ConflictData;


/**
 *
 * @author Anton Safonov, Vadim Hahhulin
 */
public class MemberToMoveData extends ConflictData {
//  private RefactoringStatus status = new RefactoringStatus();

  public MemberToMoveData(BinMember member) {
    super(member);
  }

  public MemberToMoveData(BinMember member, boolean isSelectedToMove) {
    super(member, isSelectedToMove);
  }

  public boolean equals(Object that) {
    return (this.getMember() == ((MemberToMoveData) that).getMember());
  }
}
