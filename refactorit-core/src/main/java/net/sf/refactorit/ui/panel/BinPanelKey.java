/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;

import net.sf.refactorit.ui.module.IdeWindowContext;


/**
 * Used to identify the BinPanels in the IDE context.
 * @author  Jaanek Oja
 */
public class BinPanelKey {

  // it identifies a single panel inside of group.
  private static int panelId = 0;

  // the key identifier.
  private String key = null;

  /** Creates a new instance of BinPanelKey */
  private BinPanelKey(String key) {
    this.key = key;
  }

  /**
   * Creates a new BinPanelKey for specified moduleId.
   *
   * @param moduleId as specified by module to identify itself.
   * @return BinPanelKey instance for BinPanel.
   */
  public static BinPanelKey create(IdeWindowContext context, String moduleId) {
    String groupKey = createGroupKey(context, moduleId);
    return new BinPanelKey(groupKey + "#" + (panelId++));
  }

  /**
   * Creates a group key for specified module id.
   *
   * @param moduleId for what the group id is needed.
   */
  public static String createGroupKey(IdeWindowContext context, String moduleId) {
    String groupKey = moduleId;
    String wid = context.getWindowId();
    if (wid != null) {
      groupKey += wid;
    }
    return groupKey;
  }

  /**
   * Returns a key that identifies a group this BinPanel belongs to.
   *
   * i.e. for examle all "Where Used" BinPanels belong to one group
   * and all "Not Used" belong to another group.
   *
   * @return String that identifies a group for this BinPanelKey.
   */
  public String getGroupKey() {
    int separatorIndex = this.key.indexOf('#');
    return this.key.substring(0, separatorIndex);
  }

  /**
   * Generates a String representation for this instance of BinPanelKey
   *
   * @return String, representing a key for this instance of BinPanelKey
   */
  public String toString() {
    return this.key;
  }

  /**
   * Overrides the equals method to compare the equality of another
   * BinPanel object against this one.
   *
   * @param another object against to compare the equality.
   */
  public boolean equals(Object another) {
    if (another instanceof BinPanelKey) {
      BinPanelKey other = (BinPanelKey) another;
      //System.out.println("another key: "+ other.key);
      //System.out.println("this key: "+ this.key);
      return other.key.equals(this.key);
    }
    return false;
  }

  /**
   * Return the hashcode for this instance of BinPanelKey.
   *
   * @return int, as a hashcode for this instance of BinPanelKey
   */
  public int hashCode() {
    return this.key.hashCode();
  }
}
