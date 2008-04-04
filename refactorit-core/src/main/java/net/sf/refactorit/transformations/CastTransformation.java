/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;

/**
 * Inserts cast for given expression`s return type to given type
 *
 * @author  Arseni Grigorjev
 */
public class CastTransformation extends AbstractTransformation {
  
  private final BinTypeRef castTo;
  private final BinExpression targetExpression;
  
  public CastTransformation(BinExpression targetExpression, BinTypeRef castTo) {
    super(targetExpression.getCompilationUnit());
    this.castTo = castTo;
    this.targetExpression = targetExpression;
  }
  
  public RefactoringStatus apply(EditorManager manager) {
    RefactoringStatus status = new RefactoringStatus();
    
    boolean needBraces = castNeedsBraces();
       
    String castString = "(" + BinFormatter.format(castTo) + ") ";
    if (needBraces){
      castString = "(" + castString;
    }
    
    manager.addEditor(new StringInserter(targetExpression.getCompilationUnit(),
        targetExpression.getStartLine(), targetExpression.getStartColumn()-1,
        castString));
    
    if (needBraces){
      manager.addEditor(new StringInserter(
          targetExpression.getCompilationUnit(), targetExpression.getEndLine(),
          targetExpression.getEndColumn()-1, ")")
          );
    }
    return status;
  }

  /**
   * @return true, if expression needs to be surrounded by braces in case of
   *    cast expression
   */
  private boolean castNeedsBraces() {
    BinItemVisitable parentItem = targetExpression.getParent();
    if (parentItem instanceof BinMemberInvocationExpression
        || parentItem instanceof BinArithmeticalExpression){
      return true;
    }
    return false;
  }
  
}
