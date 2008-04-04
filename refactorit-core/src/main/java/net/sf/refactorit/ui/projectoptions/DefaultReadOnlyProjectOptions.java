/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;

import java.util.HashMap;
import java.util.Map;


public class DefaultReadOnlyProjectOptions extends ProjectOptions {
  public static final ProjectOptions instance
      = new DefaultReadOnlyProjectOptions();

  private Map map = new HashMap();

  private  DefaultReadOnlyProjectOptions() {
  }

  /** null if not found  */
  public String get(String propertyName) {
    return (String) map.get(propertyName);
  }

  public void set(String propertyName, String value) {
    map.put(propertyName, value);
  }
}
