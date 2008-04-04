/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;


import net.sf.refactorit.ui.RuntimePlatform;

import org.apache.log4j.RollingFileAppender;

import java.io.File;


/**
 * RefactorItLogAppender -- appends log to refactorit.home/refactorit.log
 * Configuration in log4j.xml
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.1 $ $Date: 2005/12/09 12:03:19 $
 */
public class RefactorItLogAppender extends RollingFileAppender {

  public RefactorItLogAppender() {
    setFile(getLogFileLocation());
  }

  public static String getLogFileLocation() {
    return RuntimePlatform.getConfigDir()+File.separator+"refactorit.log";
  }
}
