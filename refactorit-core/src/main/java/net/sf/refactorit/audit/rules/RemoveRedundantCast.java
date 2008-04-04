/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;


import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Set;


public class RemoveRedundantCast extends MultiTargetCorrectiveAction {
  static final RemoveRedundantCast instance = new RemoveRedundantCast();
  public static final String KEY = "refactorit.audit.action.cast.remove";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Remove redundant cast";
  }

  public String getMultiTargetName() {
    return "Remove redundant casts";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof RedundantCast)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    BinCastExpression castExpression = ((RedundantCast) violation)
        .getCastExpression();
    CompilationUnit compilationUnit = violation.getCompilationUnit();
    
    manager.add(new StringEraser(compilationUnit, castExpression
        .getCompoundAst(), false));
    manager.add(new StringInserter(compilationUnit,
        castExpression.getStartLine(), castExpression.getStartColumn(), 
        castExpression.getExpression().getText()));

    return Collections.singleton(compilationUnit);
  }
}
