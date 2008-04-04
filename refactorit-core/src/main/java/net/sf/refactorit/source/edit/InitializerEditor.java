/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.refactorings.RefactoringStatus;

import java.io.IOException;


/**
 *
 * @author vadim
 */
public class InitializerEditor extends DefaultEditor {
  private BinField field;

  public InitializerEditor(BinField field) {
    super(field.getCompilationUnit());

    this.field = field;
  }

  /** Performs modification of files (actually modifies buffers of SourceManager).
   *
   * @return list of logic errors occured, e.g. when didn't find correct text on rename
   * @throws IOException when failed to read the file
   *
   */
  //public List apply(SourceManager manager) throws IOException {
  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    try {
      int endColumn = field.getEndColumn();

      Line line = manager.getLine(getTarget(), field.getEndLine());
      line.replace(endColumn - 2, endColumn - 1, getInitStringForField());
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }

    return status;
  }

  private String getInitStringForField() {
    if (field.getTypeRef().isPrimitiveType()) {
      if ("boolean".equals(field.getTypeRef().getName())) {
        return " = false;";
      }

      return " = 0;";
    }

    return " = null;";
  }
}
