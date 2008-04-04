/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit;

public class AuditRootNode {
  public String getDisplayName() {
    return
        "<html>" +
        "Audit results <b>(right-click on item for corrective actions)" +
        "</b></html>";
  }

  public String toString() {
    return "Audit results";
  }
}
