/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.utils.FileUtil;
import oracle.ide.Ide;

import java.io.File;
import java.net.URL;


/**
 * Provides the properites specific for IDE but needed by RefactorIT
 * module. Such as RefactorIT addin installation directory, RefactorIT modules
 * installation directory and so on.
 *
 * @author  jaanek
 */
public class PropertyManager {
  /**
   * Creates new PropertyManager. We do not allow to create objects
   * of this type. This is supposed to behave as singleton.
   */
  private PropertyManager() {
  }

  /**
   * Returns the RefactorIT modules absolute path. I.e. the path where the RefactorIT
   * modules are installed by installer. For example under Linux for
   * JDeveloper it returns for example: /opt/jdeveloper/lib/ext/refactory/modules
   */
  public static String getRefactorITModulesInstallDirectory() {
    final URL moduleUrl =
        RefactorItAddin.class.getClassLoader().getResource(
        RefactorItAddin.class.getName().replace('.', '/')
        + ClassFilesLoader.CLASS_FILE_EXT);
    final File module = FileCopier.getFileFromJarUrl(moduleUrl);

    if (!module.getName().equals("refactoryJDev.jar")) {
      throw new RuntimeException(
          "Cannot locate refactoryJDev.jar!");
    }

    String modulesPath = module.getParentFile().getAbsolutePath()
        + File.separator + "refactory";

    return modulesPath;
  }

  /**
   * Returns the directory path where the Updater installs the packages for
   * temporary.
   * FIXME! I guessed it from reading the JBuilder plugin sources.
   */
  public static String getUpdatesTempDirectory() {

    String updatesTempDir = getRefactorITModulesInstallDirectory() +
        File.separator +
        "update";
    return updatesTempDir;
  }

  /**
   * Returns the IDE (JDeveloper) installation directory. The root directory
   * where the JDeveloper was installed
   */
  public static String getIdeInstallDirectory() {

    return Ide.getHomeDirectory();
  }

  /**
   * Returns the Ide (JDeveloper) start program path. (For example:
   * C:\Oracle\JDeveloper\bin\jdeveloper\jdev.exe, or /opt/jdeveloper/bin/jdev)
   */
  public static String getIdeStartProgramPath() {

    // Determine the program extension to be used
    String programExtension = "";
    String os = System.getProperty("os.name");

    if ((os != null) && os.toLowerCase().startsWith("windows")) {
      programExtension = ".exe";
    }
    /*
           else {
      programExtension = ".sh";
           }
     */

    // set the executable path and return it
    String binPath = Ide.getBinDirectory() + "jdev" + programExtension;

    return binPath;
  }
}
