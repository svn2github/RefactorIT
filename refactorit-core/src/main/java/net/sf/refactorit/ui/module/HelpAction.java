/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.ui.help.HelpViewer;


/**
 * @author Tonis Vaga
 */
public class HelpAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.HelpAction";

  public String getName() {
    return "Help";
  }

  public char getMnemonic() {
    return 'H';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(IdeWindowContext context) {
    HelpViewer.displayDefaultTopic();
    return false;
  }
}
