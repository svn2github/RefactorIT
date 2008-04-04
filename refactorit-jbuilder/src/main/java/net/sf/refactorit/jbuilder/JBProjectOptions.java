/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.primetime.properties.GlobalProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JBProjectOptions extends net.sf.refactorit.ui.projectoptions.
    ProjectOptions {
  private static final JBProjectOptions instance = new JBProjectOptions();

  /** This class must be a singleton because there should only be one copy of each GlobalPropery */
  public static JBProjectOptions getInstance() {
    return instance;
  }

  private JBProjectOptions() {}

  public void set(String name, String value) {
    RefactorItPropGroup.setProjectProperty(name, value);
  }

  public String get(String name) {
    String projectSpecificValue = RefactorItPropGroup.getProjectProperty(name, null);

    if (projectSpecificValue != null) {
      return projectSpecificValue;
    }

    // Use old, imported settings
    return getOldFormatPropertyForName(name).getValue();
  }

  // ------- The old way of storing options; replaced because it was global; used for importing old settings ---------------------

  private final List globalProperties = new ArrayList();

  /** Never returns null (if a property is not found then creates a new property). */
  public GlobalProperty getOldFormatPropertyForName(String name) {
    for (Iterator i = globalProperties.iterator(); i.hasNext(); ) {
      GlobalProperty property = (GlobalProperty) i.next();
      if (property.getPropertyName().equals(name)) {
        return property;
      }
    }

    GlobalProperty property = new GlobalProperty(RefactorItPropGroup.CATEGORY,
        name);
    globalProperties.add(property);
    return property;
  }
}
