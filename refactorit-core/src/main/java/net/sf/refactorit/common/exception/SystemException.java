/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.common.exception;



/**
 *
 * General unchecked exception with errorcodes
 *
 * Developer should throw it whenever unexpected error occurs.
 *  Should handle and log it at least in toplevel.
 *
 *  Error codes are intended to end users, not for developers! Usually they are keys
 *    to i18n messages.
 *
 * For more info about exception handling read Rod Johnson book
 *  TODO create isLogged property to avoid logging same exception multiple time
 *  @author tonis
 */
public class SystemException extends RuntimeException {
  /**
   * @see java.lang.Throwable#getMessage()
   */
  public String getMessage() {
    return super.getMessage()+", error code="+getErrorCode();
  }
  private String errorCode;


  /**
   * Constructs a <code>SystemException</code> with no detail message.
   */
  public SystemException(String errorCode) {
    super();
    setErrorCode(errorCode);
  }


  /**
   * Constructs a <code>SystemException</code> with the specified
   * detail message.
   *
   * @param message the detail message meant for developers.
   * @param errorCode error code for end users
   */
  public SystemException(String errorCode,String message) {
    super(message);
    setErrorCode(errorCode);
  }

  /**
   * @param errorCode
   */
  private void setErrorCode(String errorCode) {
    this.errorCode=errorCode;
  }


  public SystemException(String errorCode,Throwable e) {
    super(e);
    setErrorCode(errorCode);
  }

  /**
   * @param errorCode
   * @param msg
   * @param e
   */
  public SystemException(String errorCode, String msg, Exception e) {
    super(msg,e);
    setErrorCode(errorCode);
  }

  /**
   * @see ErrorCoded#getErrorCode()
   */
  public String getErrorCode() {
   return errorCode;
  }
}
