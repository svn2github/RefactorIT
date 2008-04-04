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
 * AppRegistry - main singleton class for library.
 *   From Rod Johnson book "1-to-1 J2EE development".
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.7 $ $Date: 2004/12/10 14:05:30 $
 */
public final class AppRegistry {
  static {
    instance = new AppRegistry();
  }
  private static AppRegistry instance;

  public AppRegistry getInstance() {
    return instance;
  }

  private AppRegistry() { }

  private static ExceptionLogger errorHandler;

  /**
   * @return exception logger
   */
  public static ExceptionLogger getExceptionLogger() {
    if ( errorHandler == null ) {
      errorHandler=new ExceptionLoggerImpl();
    }
    return errorHandler;
  }

  /**
   * @param class1
   * @return logger for class
   */
  public static Logger getLogger(Class class1) {
    return Logger.getLogger(class1);
  }
}


