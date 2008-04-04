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
 * Describes category of options.
 *
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 */
public interface OptionsTab {
  String getName();

  Option getVisibleOption(int index);

  int getVisibleOptionsCount();

  /**
   * is called when user selects Reset button in JOptionDialog
   */
  void setDefault();

  /**
   * is called  when user selects Ok button in JOptionDialog
   */
  void save();

  /**
   * is called  when user press cancel button or close JOptionDialog
   */
  void cancel();
}
