/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

/**
 * <p>Title: </p>
 * <p>Description: Superclass for RefactorIT exceptions thrown when RefactorIT gets interrupted. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tõnis Vaga
 * @version 1.0
 */
//FIXME: RuntimeException should changed to java.lang.Exception.
public class RefactorItInterruptedException extends Exception {
  public RefactorItInterruptedException() {
  }

  public RefactorItInterruptedException(String msg) {
    super(msg);
  }

}
