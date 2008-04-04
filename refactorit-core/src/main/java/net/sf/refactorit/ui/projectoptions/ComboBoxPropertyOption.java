/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;

/**
 * 'Name' is displayed to user, 'value' is used internally for storage.
 */
public class ComboBoxPropertyOption {
  private String name;
  private String value;

  public ComboBoxPropertyOption(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public ComboBoxPropertyOption(String name, int value) {
    this(name, "" + value);
  }

  public ComboBoxPropertyOption(String name, boolean value) {
    this(name, value ? "true" : "false");
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }
}
