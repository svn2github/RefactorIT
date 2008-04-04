/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.ui.options.Options;

import java.util.Properties;
import java.util.ResourceBundle;


/**
 * It is an interface for all refactoring modules. All modules which are
 * responsible for some refactoring procedure must implement this interface.
 *
 * All modules implementing this interface must register itself to ModuleManager
 * in module class definition. For example.
 * <pre>
 *   // Register with ModuleManager
 *   static {
 *     ModuleManager.registerModule(new GoToModule());
 *   }
 * <pre>
 *
 * @author Igor Malinin
 */
public interface RefactorItModule {

  /**
   * Returns all RefactorItActions supported by this module.
   */
  RefactorItAction[] getActions();

  /**
   * Returns the name of this module.
   *
   * Creation date: (5/25/2001 3:13:14 AM)
   * @return java.lang.String
   */
  String getName();

  /**
   * FIXME: write javadoc for this function.
   *
   * Creation date: (5/25/2001 12:08:57 AM)
   * @return net.sf.refactorit.ui.options.Options
   */
  Options getOptions();

  /**
   * FIXME: write javadoc for this function.
   *
   * Creation date: (5/25/2001 12:07:37 AM)
   * @return java.util.Properties
   */
  Properties getProperties();

  /**
   * FIXME: write javadoc for this function.
   *
   * Creation date: (5/25/2001 12:07:37 AM)
   * @return java.util.Properties
   */
  ResourceBundle getPropertyNames();

  /**
   * FIXME: write javadoc for this function.
   *
   * Creation date: (5/25/2001 12:08:04 AM)
   * @param props java.util.Properties
   */
  void setProperties(Properties props);
}
