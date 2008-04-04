/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.utils.RefactorItInterruptedException;


/**
 * <p>Title: </p>
 * <p>Description: Thrown when user interrupts processing.
 *  For example in <code>JProgressDialog.run()</code> </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: AQRIS Software AS</p>
 * @author Tõnis Vaga
 * @version 1.0
 */

public class SearchingInterruptedException extends
    RefactorItInterruptedException { //InterruptedException {

  public SearchingInterruptedException() {
  }

  public SearchingInterruptedException(String p0) {
    super(p0);
  }
}
