/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;


/**
 * @see LocationlessSourceParsingException
 */
public final class SourceParsingException extends Exception {
  private final UserFriendlyError userFriendlyError;
  private boolean justInformsThatUserFriendlyErrorsExist = false;

  /** Without UserFriendlyError */
//  public SourceParsingException(String message) {
//    this(message, null);
//  }

  private SourceParsingException(String message,
      UserFriendlyError userFriendlyError) {
    super(message);
    if (message == null) {
      new Exception("Creating SourceParsingException with null message!")
          .printStackTrace(System.err);
    }
    this.userFriendlyError = userFriendlyError;
  }

  public boolean isUserFriendlyErrorReported() {
    return this.userFriendlyError != null;
  }

  public UserFriendlyError getUserFriendlyError() {
    return this.userFriendlyError;
  }

  public boolean justInformsThatUserFriendlyErrorsExist() {
    return this.justInformsThatUserFriendlyErrorsExist;
  }

  public void setJustInformsThatUserFriendlyErrorsExist(boolean b) {
    this.justInformsThatUserFriendlyErrorsExist = b;
  }

  public static void throwWithUserFriendlyError(String description,
      UserFriendlyError friendlyError) throws SourceParsingException {
//new Exception("thrown at 3").printStackTrace();
    // FIXME: very experimental and ugly
    (friendlyError.getCompilationUnit().getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(friendlyError);
    throw new SourceParsingException(friendlyError.toString(), friendlyError);
  }

  public static void throwWithUserFriendlyError(String description,
      CompilationUnit compilationUnit) throws SourceParsingException {
//new Exception("thrown at 2").printStackTrace();
    UserFriendlyError friendlyError
        = new UserFriendlyError(description, compilationUnit);

    (compilationUnit.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(friendlyError);
    throw new SourceParsingException(friendlyError.toString(), friendlyError);
  }

  public static void throwWithUserFriendlyError(String description,
      CompilationUnit compilationUnit,
      ASTImpl ast) throws SourceParsingException {
//new Exception("thrown at 1").printStackTrace();
    UserFriendlyError friendlyError =
        new UserFriendlyError(description, compilationUnit, ast);

    (compilationUnit.getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(friendlyError);
    throw new SourceParsingException(friendlyError.toString(), friendlyError);
  }

  public static void rethrowWithUserFriendlyError(
      LocationlessSourceParsingException e,
      ASTImpl node) throws SourceParsingException {
    throwWithUserFriendlyError(
        e.toString(),
        e.getCompilationUnit(),
        node
        );
  }
}
