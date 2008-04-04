/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import java.awt.Color;


/**
 * Defines a component for which font and colors should be set and updated when
 * needed.
 *
 * @author Anton Safonov
 */
public interface TunableComponent {

  /**
   * Get new options from the {@link net.sf.refactorit.ui.UIResources}.
   */
  void optionsChanged();

  Color getSelectedBackground();

  Color getSelectedForeground();

}
