/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;

import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePathFilter;


// java classes

/**
 *  FIXME:
 *  Hack to get rid of static functions in NBSourcePath.
 *  maybe later need to think some other way how todo file filtering.
 *  assume that in the future there can be multiple project open in same time
 *  and valid extensions can be project specific.
 *
 */
public class NBFileUtil {
  private static SourcePathFilter filter = new SourcePathFilter();
  private static String[] extensions = new String[] {".jsp"};

  private NBFileUtil() {}

  public static void setValidExtensions(String[] ext) {
    extensions = ext;
  }

  public static String[] getValidExtensions() {
    return extensions;
  }

  private static boolean fileAcceptedByName(Source file) {
    if (file == null) {
      return false;
    }
    return isValidSource(file.getName());
  }

  private static boolean directoryAcceptedByName(Source folder) {
    return filter.acceptDirectoryByName(folder.getName().toLowerCase());
  }

  public static boolean sourceAcceptedIfNotIgnored(Source source) {
    if (source.isFile()) {
      return fileAcceptedByName(source);
    } else {
      return directoryAcceptedByName(source);
    }
  }

  public static boolean isValidSource(String sourceName) {
    String name = sourceName.toLowerCase();
    // accept .java files by default
    // as there is many java sources, it give quick return.
    if (name.endsWith(".java")) {
      return true;
    }
    String[] extensions = getValidExtensions();
    // check additional extensions
    if (extensions != null) {
      for (int i = 0; i < extensions.length; i++) {
        if (name.endsWith(extensions[i])) {
          return true;
        }
      }
    }
    return false;
  }
}
