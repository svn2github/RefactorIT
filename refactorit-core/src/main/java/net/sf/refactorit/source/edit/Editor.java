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

import java.io.IOException;



// FIXME these lines counting from 1 is ugly :(
// FIXME or better fix column to count also from 1 as in AST ?
/** Lines start from 1 and columns start from 0 */
public interface Editor {

  /**
   * Performs modification of files (actually modifies buffers of SourceManager).
   *
   * @return list of logic errors occured, e.g. when didn't find correct text on rename
   * @throws IOException when failed to read the file
   */
  //List apply(SourceManager manager) throws IOException;

  /**
   * Performs modification of files (actually modifies buffers of SourceManager).
   *
   * @return RefactoringStatus with logic errors occured, e.g. when didn't find correct text on rename
   * @throws IOException when failed to read the file
   */
  RefactoringStatus apply(LineManager manager);

}
