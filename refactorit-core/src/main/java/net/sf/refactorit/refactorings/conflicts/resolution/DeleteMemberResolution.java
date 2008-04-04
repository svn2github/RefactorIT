/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts.resolution;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.MemberEraser;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.List;


/**
 * @author tanel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeleteMemberResolution extends ConflictResolution {

  private BinMember member;
  private BinMember downMember;
  
  /**
   * 
   */
  public DeleteMemberResolution(BinMember member, BinMember downMember) {
    this.member = member;
    this.downMember = downMember;
  }


  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#getDescription()
   */
  public String getDescription() {
    return "Delete declaration of " + BinFormatter.format(downMember);
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#getEditors(net.sf.refactorit.refactorings.conflicts.ConflictResolver)
   */
  public Editor[] getEditors(ConflictResolver resolver) {
    Editor[] editors = new Editor[1]; 
    editors[0] = new MemberEraser(downMember);
    return editors;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#runResolution(net.sf.refactorit.refactorings.conflicts.ConflictResolver)
   */
  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution#getDownMembers()
   */
  public List getDownMembers() {
    return CollectionUtil.singletonArrayList(downMember);
  }

}
