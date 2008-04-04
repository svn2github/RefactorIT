/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

/**
 * Describes category of options.
 * Can obtain and preserve option value by it key
 *
 * @author Vladislav Vislogubov
 */
public interface CustomOptionsTab extends OptionsTab {
  Object getValue(String key);

  void setValue(String key, Object value);

  /**
   * Checks wherther we can set this value for this key
   */
  boolean isValid(String key, Object value);
}
