/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

public interface ActionProxy {
  /**
   * Returns unique key for this action.
   *
   * @return  key
   */
  String getKey();

  /**
   * Name of action (shown in menus).
   *
   * @return  name
   */
  String getName();

  char getMnemonic();
}
