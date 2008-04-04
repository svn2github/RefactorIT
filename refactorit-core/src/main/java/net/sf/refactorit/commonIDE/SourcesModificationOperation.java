/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;


/**
 * SourcesModificationOperation -- batched atomic sources modification operation
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.2 $ $Date: 2004/11/16 13:04:27 $
 */
public abstract class SourcesModificationOperation {
  private Exception exception;

  /**
   * Runs operation, if runImpl throws RuntimeException exception will be
   * thrown out.
   * Use getException to find out if exception was thrown during call.
   */
  public final void run() {
    try {
      runImpl();
    } catch (Exception e) {
      this.exception = e;
      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      }
    }
  }

  /**
   * override this, but you shouldn't call directly it
   */
  protected abstract void runImpl() throws Exception;
  
  /**
   * @return exception what was thrown in {@link #runImpl()}
   *         or null if no exception
   */
  public Exception getException() {
    return this.exception;
  }
}
