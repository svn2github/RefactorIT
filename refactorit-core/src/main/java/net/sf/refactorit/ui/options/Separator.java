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
 * Serves as Separator.class for the recognizing of class type in Options model.
 *
 * @author Vlad
 */
public class Separator {
  private String value;
  /**
   * Constructor for Separator.
   */
  public Separator(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
