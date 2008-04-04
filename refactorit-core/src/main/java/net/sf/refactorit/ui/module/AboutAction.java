/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.ui.license.AboutDialog;


/**
 * @author Tonis Vaga
 */
public class AboutAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.AboutAction";

  public String getName() {
    return "About RefactorIT";
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(IdeWindowContext context){
    AboutDialog dialog = new AboutDialog(context);
    dialog.show();
    return false;
  }

  public char getMnemonic() {
    return 'A';
  }
}
