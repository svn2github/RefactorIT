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
 * Describes options.
 *
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 */
public interface Options {
  OptionsTab getTab(int index);

  int getTabCount();

  void addTab(CustomOptionsTab tab);
}
