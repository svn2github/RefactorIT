/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


/**
 * ExceptionLogger - interface for handling exceptions
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.6 $ $Date: 2004/12/29 12:44:24 $
 */
public interface ExceptionLogger {
  /**
   * For handling exceptions with error level
   * @param error
   * @param source class where error was generated, either java.lang.Object or java.lang.Class
   */
  void error(Throwable error, Object source);
  
  void error(Throwable error,String message, Object source);
  
  /**
   * For logging exceptions with debug level
   * @param error
   * @param source
   */
  public void debug(Throwable error, Object source);
  
  public void debug(Throwable error,String message, Object source);
  /**
   * For handling exceptions with warning level
   * @param error
   * @param source
   */
  void warning(Throwable error, Object source);

  /**
   * @param e
   * @param source
   */
  void warn(Throwable e,Object source);
  
  public void fatal(Throwable e,Object source);


}
