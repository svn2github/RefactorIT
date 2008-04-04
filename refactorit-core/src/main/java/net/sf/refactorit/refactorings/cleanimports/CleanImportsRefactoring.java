/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.cleanimports;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


public class CleanImportsRefactoring extends AbstractRefactoring {
  public static String key = "refactoring.cleanimports";

  private Object target;
  private List usages;

  public CleanImportsRefactoring(RefactorItContext context, Object target) {
    super("Clean Imports", context);
    this.target = target;
  }

  public List findUnusedUsages() {
    final CompilationUnit visitables[] =
        ModuleManager.getCompilationUnits(target, getContext());

    final ASTImpl[][] unusedImports = new ASTImpl[visitables.length][];
    // initialize
    for(int i = 0; i < unusedImports.length; i++) {
      unusedImports[i] = new ASTImpl[0];
    }

    try {
      JProgressDialog.run(getContext(), new Runnable() {
        public void run() {
          ProgressListener listener = (ProgressListener)
              CFlowContext.get(ProgressListener.class.getName());

          ProgressMonitor.Progress progress = ProgressMonitor.Progress.FULL;

          for (int i = 0; i < visitables.length; ++i) {
            unusedImports[i] = ImportUtils.listUnusedImports(visitables[i]);

            listener.progressHappened(
                progress.getPercentage(i, visitables.length));
          }
        }
      }


      , true);
    } catch (SearchingInterruptedException e) {
      return null;
    }

    List usagesForConfirmation = new ArrayList();

    for (int i = 0; i < visitables.length; ++i) {
      for (int u = 0; u < unusedImports[i].length; ++u) {
        usagesForConfirmation.add(
            new InvocationData(null, visitables[i], unusedImports[i][u]));
      }
    }

    return usagesForConfirmation;
  }

  public RefactoringStatus checkUserInput() {
    return new RefactoringStatus();
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    for (int i = 0; i < usages.size(); i++) {
      InvocationData id = (InvocationData) usages.get(i);
      CompoundASTImpl node = new CompoundASTImpl(id.getWhereAst().getParent());
      transList.add(new StringEraser(id.getCompilationUnit(), node, true));
    }

    return transList;
  }

  public RefactoringStatus checkPreconditions() {
    return new RefactoringStatus();
  }

  public String getDescription() {

    // @todo: override this
    return super.getDescription();
  }

  public String getKey() {
    return this.key;
  }

  public void setImportsToRemove(final List usages) {
    this.usages = usages;
  }
}
