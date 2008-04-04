/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import oracle.ide.model.Displayable;

import javax.swing.Icon;


/**
 * @author Anton Safonov
 */
public class DisplayableInfo implements Displayable {

  public DisplayableInfo() {
  }

  public String getShortLabel() {
    return "RefactorIT";
  }

  public String getLongLabel() {
    return "RefactorIT";
  }

  public Icon getIcon() {
    return net.sf.refactorit.common.util.ResourceUtil.getIcon(
        net.sf.refactorit.ui.UIResources.class, "RefactorIt.gif");
  }

  public String getToolTipText() {
    return null;
  }

  public String toString() {
    return getShortLabel();
  }
}
