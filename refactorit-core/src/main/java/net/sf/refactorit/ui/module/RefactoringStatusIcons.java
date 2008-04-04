/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.UIResources;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * To represent {@link net.sf.refactorit.refactorings.RefactoringStatus status} in the report tree.
 *
 * @author Vlad Vislogubov
 */
public class RefactoringStatusIcons {

  private static final ImageIcon okIcon
      = ResourceUtil.getIcon(UIResources.class, "ok_status.gif");
  private static final ImageIcon infoIcon
      = ResourceUtil.getIcon(UIResources.class, "info_status.gif");
  private static final ImageIcon warningIcon
      = ResourceUtil.getIcon(UIResources.class, "warning_status.gif");
  private static final ImageIcon questionIcon
      = ResourceUtil.getIcon(UIResources.class, "question_status.gif");
  private static final ImageIcon errorIcon
      = ResourceUtil.getIcon(UIResources.class, "error_status.gif");
  private static final ImageIcon fatalIcon
      = ResourceUtil.getIcon(UIResources.class, "fatal_status.gif");

  public static Icon getSeverityIcon(int severity) {
    Assert.must(severity >= RefactoringStatus.UNDEFINED
        && severity <= RefactoringStatus.CANCEL,
        "Wrong severity: " + severity);

    switch (severity) {
      case RefactoringStatus.UNDEFINED:
        return null; // no icon
      case RefactoringStatus.OK:
        return okIcon;
      case RefactoringStatus.INFO:
        return infoIcon;
      case RefactoringStatus.WARNING:
        return warningIcon;
      case RefactoringStatus.QUESTION:
        return questionIcon;
      case RefactoringStatus.ERROR:
        return errorIcon;
      case RefactoringStatus.FATAL:
        return fatalIcon;
      case RefactoringStatus.CANCEL:
        return fatalIcon; // actually, shouldn't be shown anywhere!!!
    }

    return null;
  }
}
