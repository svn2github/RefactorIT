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
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.MemberEraser;

import java.util.List;


public class DeleteOtherImplementersResolution extends ConflictResolution {

  private List members;
  
  public DeleteOtherImplementersResolution(List members) {
    this.members = members;
  }
  
  public String getDescription() {
    return "Delete implementations in other subclasses";
    
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    Editor[] editors = new Editor[members.size()];
    
    for (int i = 0; i < members.size(); i++) {
      BinMember member = (BinMember) members.get(i);
      editors[i] = new MemberEraser(member);
    }
    return editors;
  }

  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
    
  }

  public List getDownMembers() {
    return members;
  }

}
