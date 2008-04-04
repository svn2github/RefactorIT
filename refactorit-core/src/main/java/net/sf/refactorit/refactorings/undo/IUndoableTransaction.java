/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.vfs.Source;

import java.util.List;


public interface IUndoableTransaction {

  boolean addEdit(IUndoableEdit edit);

  IUndoableEdit createCreateFileUndo(SourceInfo info);

  IUndoableEdit createDeleteFileUndo(Source source);

  IUndoableEdit createModifiedSourcesUndo(List sources);

  IUndoableEdit createRenameFileUndo(Source source, Source destDir,
      String newName);

  String getPresentationName();

}
