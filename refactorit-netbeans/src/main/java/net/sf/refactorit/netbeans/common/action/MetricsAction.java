/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;

public class MetricsAction extends RITAction {
  public String getActionKey() {
    return net.sf.refactorit.metrics.MetricsAction.KEY;
  }

  public String getName() {
    return net.sf.refactorit.metrics.MetricsAction.NAME;
  }
}
