/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;

/**
 * Creating/erasing of files/directories.
 * @author Anton Safonov
 */
public interface FilesystemEditor {

  RefactoringStatus changeInFilesystem(LineManager manager);

  void disable();

  boolean isEnabled();

  SourceHolder getTarget();
}
