/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.refactorings.RefactoringStatus;

/**
 * @author tanel
 *
 * Editor that deletes a member, together with its javadoc.
 */
public class MemberEraser extends DefaultEditor {

  private BinMember member;
  
  public MemberEraser(BinMember member) {
    super(member.getCompilationUnit());
    this.member = member;
  }
  
  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();
    StringEraser eraser = new StringEraser(member);
    eraser.setRemoveLinesContainingOnlyComments(true);
    status.merge(eraser.apply(manager));
    Comment comment = Comment.findFor(member);
    if (comment != null) {
	    StringEraser commentEraser = new StringEraser(comment);
	    commentEraser.setRemoveLinesContainingOnlyComments(true);
	    status.merge(commentEraser.apply(manager));
    }    
    return status;
  }

}
