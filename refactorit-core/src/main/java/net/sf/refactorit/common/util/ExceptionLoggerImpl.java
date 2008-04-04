/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import org.apache.log4j.Logger;

/**
 * ExceptionLoggerImpl
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.8 $ $Date: 2004/12/29 12:44:24 $
 */
class ExceptionLoggerImpl implements ExceptionLogger {

  /**
   * @see net.sf.refactorit.common.util.ExceptionLogger#error(java.lang.Throwable, Object)
   */
  public void error(Throwable error, Object source) {
    getLogger(source).error(error.getMessage() ,error);

  }
  

  /**
   * @param source
   * @returns
   */
  private Logger getLogger(Object source) {
    Class sourceClass=(Class) (source instanceof Class?source:source.getClass());
    return Logger.getLogger(sourceClass);
  }
  
  public void debug(Throwable error, Object source) {
    getLogger(source).debug(error.getMessage() ,error);
  }


  /**
   * @see net.sf.refactorit.common.util.ExceptionLogger#warning(java.lang.Throwable, java.lang.Object)
   */
  public void warning(Throwable error, Object source) {
    getLogger(source).warn(error.getMessage() ,error);
  }


  /**
   * @see net.sf.refactorit.common.util.ExceptionLogger#error(java.lang.Throwable, java.lang.String, java.lang.Object)
   */
  public void error(Throwable error, String message, Object source) {
    getLogger(source).error(message,error);
  }


  /**
   * @see net.sf.refactorit.common.util.ExceptionLogger#debug(java.lang.Throwable, java.lang.String, java.lang.Object)
   */
  public void debug(Throwable error, String message, Object source) {
    getLogger(source).debug(message,error);
  }


  public void warn(Throwable e,Object source) {
    getLogger(source).warn(e.getMessage(),e);
  }
  
  public void fatal(Throwable e,Object source) {
    getLogger(source).fatal(e.getMessage(),e);
  }

}
