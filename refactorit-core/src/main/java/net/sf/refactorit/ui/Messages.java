/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Optional class for obtaining of all localized strings.
 *
 * @author Vladislav Vislogubov
 */
public class Messages {
  public static final ResourceBundle bundle = ResourceBundle.getBundle(
      "net.sf.refactorit.ui.resources.LocalizedStrings"); //$NON-NLS-1$

  private Messages() {}

  public static String getString(String key) {
    try {
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
