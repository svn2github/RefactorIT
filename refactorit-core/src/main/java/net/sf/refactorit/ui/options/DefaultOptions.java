/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 */
public class DefaultOptions implements Options {
  private ArrayList tabs = new ArrayList();

  public DefaultOptions(OptionsTab[] tabs) {
    this.tabs.addAll(Arrays.asList(tabs));
  }

  public OptionsTab getTab(int index) {
    return (OptionsTab) tabs.get(index);
  }

  public int getTabCount() {
    return tabs.size();
  }

  public void addTab(CustomOptionsTab tab) {
    tabs.add(tab);
  }
}
