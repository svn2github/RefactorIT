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
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.conflicts.resolution.DeleteMemberResolution;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author tanel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubstituteAbstractMethodConflict extends UpDownMemberConflict {
  private ConflictResolver resolver;
  private boolean isResolved;
  
  /**
   * @param upMember
   * @param downMembers
   */
  public SubstituteAbstractMethodConflict(ConflictResolver resolver, 
      BinMember upMember, List downMembers) {
    super(upMember, downMembers);
    this.resolver = resolver;
    Assert.must(downMembers.size() == 1, "One downMember expected");
    setResolution(new DeleteMemberResolution(upMember, (BinMember)downMembers.get(0)));
  }


  public void resolve() {
    getResolution().runResolution(resolver);
  }

  public Editor[] getEditors() {
    List editors = new ArrayList(2);
    BinMember upMember = getUpMember();
    if (upMember.isFinal()) {
      editors.add(new ModifierEditor(upMember, BinModifier.clearFlags(upMember.getModifiers(), BinModifier.FINAL)));
    }
    editors.addAll(Arrays.asList(getResolution().getEditors(resolver)));
    return  (Editor[]) editors.toArray(new Editor[0]);
  }

  public boolean isResolvable() {
    return true;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.Conflict#getType()
   */
  public ConflictType getType() {
    return ConflictType.ALREADY_DEFINED;
  }


  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.Conflict#getDescription()
   */
  public String getDescription() {
    return "Substitute " + BinFormatter.format(getDownMembers().get(0)) + " in target class?";
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.refactorings.conflicts.Conflict#getSeverity()
   */
  public int getSeverity() {
    return RefactoringStatus.QUESTION;
  }

}
