/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.ui.module.AboutAction;
import net.sf.refactorit.ui.module.CleanAction;
import net.sf.refactorit.ui.module.CrossHtmlAction;
import net.sf.refactorit.ui.module.HelpAction;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.module.ProductivityGuideAction;
import net.sf.refactorit.ui.module.ProjectOptionsAction;
import net.sf.refactorit.ui.module.RebuildAction;
import net.sf.refactorit.ui.module.StandaloneBrowserAction;
import net.sf.refactorit.ui.module.audit.AuditAction;
import net.sf.refactorit.ui.module.calltree.CallTreeAction;
import net.sf.refactorit.ui.module.classmodelvisitor.AstVisitorAction;
import net.sf.refactorit.ui.module.classmodelvisitor.ClassmodelVisitorAction;
import net.sf.refactorit.ui.module.dependencies.DrawDependenciesAction;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.module.shell.ShellAction;
import net.sf.refactorit.ui.module.type.JavadocAction;
import net.sf.refactorit.ui.module.type.TypeAction;
import net.sf.refactorit.ui.module.where.WhereAction;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;


/**
 *
 *
 * @author Tonis Vaga
 */
public interface MenuMetadata {
  String[] GANG_FOUR_KEYS = new String[] {
      WhereAction.KEY, RenameAction.KEY, TypeAction.KEY, GotoAction.KEY
  };

  String[] MOST_USED_ACTIONS = new String[] {
      AstVisitorAction.KEY,
      AuditAction.KEY,
      CallTreeAction.KEY,
      DrawDependenciesAction.KEY,
      GotoAction.KEY,
      JavadocAction.KEY,
//      NotUsedAction.KEY,
      MetricsAction.KEY,
      ShellAction.KEY,
      TypeAction.KEY,
      ClassmodelVisitorAction.KEY,
      WhereAction.KEY
  };

  String[] GO_TO_ACTIONS = new String[] {GotoAction.KEY};

  String COMMON_IDE_ACTION_KEYS[][] = new String[][] {
      {AboutAction.KEY, HelpAction.KEY, ProductivityGuideAction.KEY},
      {CleanAction.KEY, RebuildAction.KEY, ProjectOptionsAction.KEY, OptionsAction.KEY},
      {CrossHtmlAction.KEY, StandaloneBrowserAction.KEY}
  };
}
