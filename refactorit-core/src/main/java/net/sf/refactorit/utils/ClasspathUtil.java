/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;



import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.test.Utils;

import java.io.File;


/**
 * Classpath utilities.
 */
public final class ClasspathUtil {
  /**
   * May contain multiple paths & must contain rt.jar.
   * Don't cache the value over long periods of time because the value might change when
   * someone drops a jar file into JVM's lib/ext folder.
   */
  public static String getDefaultClasspath() {
    String result = "";

    // Selected J2SE 5.0 classes to run appropriate tests
    if (IDEController.runningTest()) {
      result = addPaths(result, Utils.getTestFileDirectory().getAbsolutePath()
          + File.separatorChar + "misc" + File.separatorChar + "jdk15"
          + File.separatorChar + "classes" + File.separatorChar);
    }

    result = addPaths(result, getAllJarFilesInRtJarFolder());
    result = addPaths(result, getPathForAllJarFilesInLibExt());

    return result;
  }

  private static String addPaths(String path1, String path2) {
    if (!("".equals(path1)) && !("".equals(path2))) {
      return path1 + File.pathSeparatorChar + path2;
    } else {
      return path1 + path2;
    }
  }

  private static File getRtJarFile() {
    return FileCopier.getFileFromJarUrl(
        ClasspathUtil.class.getClassLoader().getResource(
        "java/lang/Object.class")
        );
  }

//  private static File getChildFile( File folder, String name ) {
//    File[] files = folder.listFiles();
//    if(files == null) return null; // Was an open bug with NPE
//    for( int i = 0; i < files.length; i++ )
//      if( files[ i ].getName().equals( name ) )
//        return files[ i ];
//
//    return null;
//  }

  private static File getRtJarFolder() {
    return getRtJarFile().getParentFile();
  }

  private static String getAllJarFilesInRtJarFolder() {
    return getPathForAllJarFiles(getRtJarFolder());
  }

  private static String getPathForAllJarFilesInLibExt() {
    File libExtFolder = new File(System.getProperty("java.ext.dirs"));

    if (libExtFolder.exists()) {
      return getPathForAllJarFiles(libExtFolder);
    } else {
      return "";
    }
  }

  /**
   * The algorithm here might need some fine-tuning
   */
  public static boolean isJarFile(File file) {
    return file.getPath().toLowerCase().endsWith(".jar");
  }

  private static String getPathForAllJarFiles(File folder) {
    String result = "";

    File[] files = folder.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (isJarFile(files[i])) {
        result = addPaths(result, files[i].getPath());

      }
    }
    return result;
  }
}
