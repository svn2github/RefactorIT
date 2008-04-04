/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.source.MethodNotFoundError;


/**
 * FIXME: this class was introduced because of CreateMissingMethod, should improve design later
 *
 * @author tonis
 */
public final class MissingBinMember extends AbstractLocationAware {

  /* (non-Javadoc)
   * @see net.sf.refactorit.classmodel.LocationAware#getCompilationUnit()
   */
  public final MethodNotFoundError error;
  
  public MissingBinMember(MethodNotFoundError object) {
    error = object;
  }

  public final CompilationUnit getCompilationUnit() {
    return error.getCompilationUnit();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.classmodel.LocationAware#getStartLine()
   */
  public final int getStartLine() {
    return error.getLine();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.classmodel.LocationAware#getEndLine()
   */
  public final int getEndLine() {
    return error.getLine();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.classmodel.LocationAware#getStartColumn()
   */
  public final int getStartColumn() {
    return error.getColumn();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.classmodel.LocationAware#getEndColumn()
   */
  public final int getEndColumn() {
    return getStartColumn();
  }

}
