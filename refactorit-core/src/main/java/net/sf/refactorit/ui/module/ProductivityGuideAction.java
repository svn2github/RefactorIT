/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.ui.ProductivityGuideDialog;

public class ProductivityGuideAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.productivityguide";

  public boolean run(IdeWindowContext context){
    (new ProductivityGuideDialog(context)).show();
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Productivity Guide";
  }

  public char getMnemonic(){
    return 'G';
  }

}
