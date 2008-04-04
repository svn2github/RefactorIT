/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import javax.swing.filechooser.FileFilter;

import java.io.File;


public class FileExtensionFilter extends FileFilter {
  private final String description;
  private final String[] suffixes;

  public static final String SUFFIX_ACCEPT_ALL_FILES = "";

  /** @param  suffix  for example, ".exe" for executable files, SUFFIX_ACCEPT_ALL_FILES to accept all files. */
  public FileExtensionFilter(String suffix, String description) {
    this.description = description;
    this.suffixes = new String[1];
    this.suffixes[0] = suffix;
  }

  /** @param  suffixes  for example, FileExtensionFilter(new String[] {".tif",".gif",".jpg"} , "Pictures(.tif, .gif, .jpg)"); */
  public FileExtensionFilter(String[] suffixes, String description) {
    this.description = description;
    this.suffixes = suffixes;
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true; // show always if derectory
    } else if (suffixes == null) {
      return false; // no extensions no files
    } else if (SUFFIX_ACCEPT_ALL_FILES.equals(suffixes[0])) {
      return true; // show all files if SUFFIX_ACCEPT_ALL_FILES is given
    }
    for (int i = 0; i < suffixes.length; i++) {
      if (f.getName().endsWith(suffixes[i])) {
        return true;
      }
    }
    return false;
  }

  public final String getDescription() {
    return this.description;
  }
}
