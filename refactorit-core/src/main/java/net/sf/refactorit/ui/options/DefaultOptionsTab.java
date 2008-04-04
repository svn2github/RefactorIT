/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.source.format.FormatSettings;

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of OptionsTab interface.
 *
 * @author Igor Malinin
 */
public class DefaultOptionsTab implements OptionsTab {
  private String name;
  private Option[] options;

  public DefaultOptionsTab(String name, Option[] options) {
    this.name = name;
    this.options = options;
  }

  /**
   * getName method comment.
   */
  public String getName() {
    return name;
  }

  public Option getVisibleOption(int index) {
    return (Option) getVisibleOptions(options).get(index);
  }

  public int getVisibleOptionsCount() {
    return getVisibleOptions(options).size();
  }

  /** called when user selects Reset button in JOptionDialog */
  public void setDefault() {
  }

  /** called  when user selects Ok button in JOptionDialog */
  public void save() {
    FormatSettings.clearCache();
  }

  /** called  when user press cancel button or close JOptionDialog */
  public void cancel() {
  }

  private List getVisibleOptions(Option[] options) {
    List result = new ArrayList();
    for (int i = 0; i < options.length; i++) {
      if (options[i].isVisible()) {
        result.add(options[i]);
      }
    }
    return result;
  }
}
