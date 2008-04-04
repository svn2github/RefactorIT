/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.classmodelvisitor;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;



public class AstVisitorAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.AstVisitorAction";
  public static final String NAME = "ASTVisitor";

  public String getName() {
    return NAME;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    return true;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean isAvailableForType(Class type) {
    if (BinItem.class.isAssignableFrom(type)) {
      return true;
    }

    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    {
      Assert.must(context != null,
          "Attempt to pass NULL context into ClassmodelVisitorAction.run()");
      Assert.must(object != null,
          "Attempt to pass NULL object into ClassmodelVisitorAction.run()");
    }

    Project project = context.getProject();
    AstVisitorModel model = new AstVisitorModel(project, object);

    BinTreeTable table = ClassmodelVisitorAction.createTable(model, context);

    ResultArea results = ResultArea.create(table, context, this);
    results.setTargetBinObject(object);
    BinPanel panel = BinPanel.getPanel(context, getName(), results);
    table.smartExpand();

    // Register default help for panel's current toolbar
    panel.setDefaultHelp("refact");

    // we never change anything
    return false;
  }
}
