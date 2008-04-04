/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

public class MetricsUserObject implements UserObject {
  private String name;
  private String description;
  private String key;

  public MetricsUserObject(String name, String description, String key) {
    this.name = name;
    this.key = key;
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String toString() {
    return getName();
  }
}
