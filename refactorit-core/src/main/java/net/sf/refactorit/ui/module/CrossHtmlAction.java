/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.JHTMLDialog;


/**
 * @author Tonis Vaga
 */
public class CrossHtmlAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.CrossHtmlAction";

  public String getName() {
    return "Generate Cross-Referenced HTML";
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return 'G';
  }

  public boolean run(IdeWindowContext context) {
    IDEController instance = IDEController.getInstance();
    instance.ensureProject();

    new JHTMLDialog(context, instance.getActiveProject()).display();

    return false;
  }
}
