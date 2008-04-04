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
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.query.notused.NotUsedTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;


public class NotUsedModelBuilder implements ModelBuilder {
  public BinTreeTableModel populateModel(final Project p) {
    BinTreeTableModel result = new NotUsedTreeTableModel(p, p,
        ExcludeFilterRule.getDefaultRules());
    return result;
  }

  public boolean supportsProfiles() {
    return false;
  }
}
