/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import java.awt.Component;

// FIXME: Is there a way to get rid of this interface? Should we?
public interface IProfilePanel {
  Profile getProfile();

  void addOptionsPanel(OptionsPanel p, String key);
  
  Component[] getOptionsPanelComponents();
}
