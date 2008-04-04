/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli.actions;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;


public interface ModelBuilder {
  public BinTreeTableModel populateModel(Project p);

  public boolean supportsProfiles();
}
