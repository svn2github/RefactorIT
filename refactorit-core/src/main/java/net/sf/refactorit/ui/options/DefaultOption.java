/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

public class DefaultOption implements Option, Comparable {
  private String key;
  private String value;
  private Class type;

  private boolean visible = true;

  public DefaultOption(String key, Class type) {
    this.key = key;
    this.type = type;
  }

  public DefaultOption(String key, String value, Class type) {
    this.key = key;
    this.value = value;
    this.type = type;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public Class getType() {
    return type;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean b) {
    visible = b;
  }

  public int compareTo(Object o) {
    try {
      return getValue().compareTo(((Option) o).getValue());
    } catch (Exception e) {
      return 1;
    }
  }
}
