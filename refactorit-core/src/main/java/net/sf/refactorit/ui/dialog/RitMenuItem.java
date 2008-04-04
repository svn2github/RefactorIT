/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.dialog;

public class RitMenuItem {

  private String text;
  private Runnable action;

  public RitMenuItem(String text, Runnable action) {
    this.text = text;
    this.action = action;
  }

  public String getText() {
    return text;
  }

  public void runAction() {
    action.run();
  }

}
