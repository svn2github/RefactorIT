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
 * <p>Description: Exception thrown from ParsingMessageDialog.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author T�nis Vaga
 * @version 1.0
 */

public final class ParsingInterruptedException extends RefactorItInterruptedException {

  public ParsingInterruptedException() {
  }

  public ParsingInterruptedException(String msg) {
    super(msg);
  }
}
