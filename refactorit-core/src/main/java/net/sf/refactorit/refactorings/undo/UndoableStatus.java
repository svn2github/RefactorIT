/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import java.util.Collection;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
class UndoableStatus {
  static final int ERROR = 0;
  static final int WARNING = 1;
  static final int OK = 2;

  static final UndoableStatus OK_STATUS = new UndoableStatus(OK);

  int status = OK;

  String errorMsg;

  /**
   * @param status
   */
  public UndoableStatus(int status) {
    this.status = status;
  }

//  public boolean isSignificantChanges() {
//    return getStatus() == SIGNIFICANT;
//  }

  public int getStatus() {
    return this.status;
  }

  public void setStatus(final int status) {
    this.status = status;
  }

  public boolean isOk() {
    return (status == OK);
  }

  public void resolve() {
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(final String errorMsg) {
    this.errorMsg = errorMsg;
  }
}


class CannotCreateStatus extends ErrorStatus {
  Collection headers = null;

  /**
   * @param collection of can't create headers
   */
  public CannotCreateStatus(List headers) {
    super("");

    String msg = "RefactorIT cannot create ";
    if (headers.size() == 1) {
      msg += "file " + headers.get(0);
    } else {
      msg += " files";
    }
    setErrorMsg(msg);
    this.headers = headers;
  }
}


class ErrorStatus extends UndoableStatus {
//  Exception exception;

  /**
   * @param msg
   */
  public ErrorStatus(String msg) {
    super(ERROR);
    setErrorMsg(msg);
  }

//  public Exception getException() {
//    return this.exception;
//  }
//
//  public void setException(final Exception exception) {
//    this.exception = exception;
//  }
}
