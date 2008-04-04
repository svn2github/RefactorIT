/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.hierarchy;

import org.openide.windows.TopComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public abstract class ActiveNodesChangedListener implements
    PropertyChangeListener {
  public void propertyChange(PropertyChangeEvent e) {
    if (TopComponent
        .Registry
        .PROP_ACTIVATED_NODES
        .equals(e.getPropertyName())) {

      activeNodesChanged();
    }
  }

  protected abstract void activeNodesChanged();
}
