/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import java.io.File;


public class ProfileUtil {
  public static Profile createProfile(String profileFilename) throws
      IllegalArgumentException {
    try {
      return new Profile(new File(profileFilename));
    } catch (Exception e) {
      throw new IllegalArgumentException("Corrupted profile: "
          + profileFilename);
    }
  }

  public static boolean isValidProfile(File file) {
    try {
      new Profile(file);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
