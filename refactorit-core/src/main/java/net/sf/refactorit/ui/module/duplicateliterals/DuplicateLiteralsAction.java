/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.duplicateliterals;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.refactorings.InternationalUtils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;


public class DuplicateLiteralsAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.DuplicateLiteralsAction";
  public static final String NAME = "Find Duplicate Strings";

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean run(final RefactorItContext context, final Object inObject) {
    Object target = RefactorItActionUtils.unwrapTarget(inObject);
    if (target instanceof BinMember && !(target instanceof BinCIType)) {
      target = ((BinMember) target).getTopLevelEnclosingType();
    }

    if (target instanceof BinCIType && !((BinCIType) target).isFromCompilationUnit()) {
      DialogManager.getInstance().showNonSourcePathItemInfo(
          context, getName(), target);
      return false;
    }

    final Object finalTarget = target;
    try {
      final ArrayList results = new ArrayList();

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          CompilationUnit visitables[] = ModuleManager.getCompilationUnits(finalTarget,
              context);
          LiteralSearchVisitor visitor = new LiteralSearchVisitor(results);
          for (int i = 0; i < visitables.length; ++i) {
            visitor.visit(visitables[i]);
          }
        }
      }, true);

      BinTreeTableNode rootNode = new BinTreeTableNode(
          "Strings having duplicates");

      InternationalUtils.duplicateLiteralsReport(results, rootNode);
      rootNode.reflectLeafNumberToParentName();

      BinTreeTableModel model = new BinTreeTableModel(rootNode);
      BinTreeTable table = new BinTreeTable(model, context);
      ResultArea binComp = ResultArea.create(table, context, this);

      binComp.setTargetBinObject(finalTarget);
      BinPanel panel = BinPanel.getPanel(context, getName(), binComp);

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.dupll");
    } catch (SearchingInterruptedException ex) {
    }

    return false;
  }
}


class LiteralSearchVisitor extends AbstractIndexer {
  ArrayList results;

  LiteralSearchVisitor(ArrayList results) {
    this.results = results;
  }

  public void visit(BinLiteralExpression literal) {
    if (literal.getLiteral().startsWith("\"")) {
      results.add(literal);
    }
    super.visit(literal);
  }
}
