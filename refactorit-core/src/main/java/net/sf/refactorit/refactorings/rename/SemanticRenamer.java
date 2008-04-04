/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;


public class SemanticRenamer {

  public String oldWord;
  public String newWord;

  public SemanticRenamer(String oldWord, String newWord) {
    this.oldWord = oldWord;
    this.newWord = newWord;
  }

  public TransformationList rename(RefactorItContext context, BinMember[] memberList,
      boolean renameInNonJavaFiles, boolean renameInJavadocs) {
    TransformationList transList = new TransformationList();
    for (int i = 0; i < memberList.length; i++) {
      String memberName = memberList[i].getName();
      String newMemberName = StringUtil.smartPhraseReplace(memberName,
          oldWord, newWord);

      RenameRefactoring refactoring = RenameRefactoring.getRefactoring(context, memberList[i]);

      // hack..
      if(refactoring instanceof RenameField) {
        ((RenameField)refactoring).setRenameGettersAndSetters(false);
      } else if(refactoring instanceof RenameType) {
//        ((RenameType)refactoring).setSemanticRename(true);
        ((RenameType)refactoring).setRenameInNonJavaFiles(renameInNonJavaFiles);
      } // end of hack

      refactoring.setRenameInJavadocs(renameInJavadocs);

      RefactoringStatus status = refactoring.checkPreconditions();
      transList.merge(status);

      if(transList.getStatus().isErrorOrFatal()) {
        return transList;
      }

      refactoring.setNewName(newMemberName);
      status = refactoring.checkUserInput();
      transList.merge(status);

      if(transList.getStatus().isErrorOrFatal()) {
        return transList;
      }

      TransformationList list = refactoring.performChange();
      transList.merge(list);

    }
    return transList;
  }

}
