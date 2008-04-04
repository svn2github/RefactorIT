/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class RemoveBracketsAction extends MultiTargetCorrectiveAction {
  public static final RemoveBracketsAction INSTANCE 
      = new RemoveBracketsAction();

  public final String getKey() {
    return "refactorit.audit.action.remove_brackets";
  }

  public final String getName() {
    return "Remove surrounding brackets";
  }
  
  protected final Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    
    BinStatementList sourceConstruct = (BinStatementList) 
        ((AwkwardSourceConstruct) violation).getSourceConstruct();
    
    // remove brackets only if it is:
    // 1) EmptyBlocksAndBodies or NestedBlock violation
    // 2) If the statement list is loose and empty.
    if (!(violation instanceof EmptyBlocksAndBodies 
        || violation instanceof NestedBlock) 
        || !((sourceConstruct.getParent() instanceof BinStatementList) 
        && sourceConstruct.getStatements().length == 0)){
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    
    BinStatementList list = sourceConstruct;
    CompilationUnit cUnit = list.getCompilationUnit();
    
    int rem_start, rem_end, opening_brace_pos, closing_brace_pos, 
        line_start_pos, line_end_pos;
    
    opening_brace_pos = cUnit.getSource().getLineIndexer().lineColToPos(
        list.getStartLine(), list.getStartColumn());
    closing_brace_pos = cUnit.getSource().getLineIndexer().lineColToPos(
        list.getEndLine(), list.getEndColumn()-1);
    line_start_pos = cUnit.getSource().getLineIndexer().lineColToPos(
        list.getStartLine(), 0);
    line_end_pos = cUnit.getSource().getLineIndexer().lineColToPos(
        list.getStartLine() + 1, 0);
    
    if (list.getStartLine() == list.getEndLine()){
      // if code is like '*{*}*'
      if (lineWillBeEmptyAfterAction(cUnit.getSource().getText(
          line_start_pos, line_end_pos))){
        // can delete all line
        rem_start = line_start_pos;
        rem_end = line_end_pos;
        manager.add(new StringEraser(cUnit, rem_start, rem_end));
      } else {
        // must delete only brackets
        rem_start = opening_brace_pos;
        rem_end = rem_start + 1;
        manager.add(new StringEraser(cUnit, rem_start, rem_end));
        rem_start = closing_brace_pos;
        rem_end = rem_start + 1;
        manager.add(new StringEraser(cUnit, rem_start, rem_end));
      }
    } else {
      if (cUnit.getSource().getText(line_start_pos, line_end_pos).trim()
          .equals("{")){
        // can delete all line
        rem_start = line_start_pos;
        rem_end = line_end_pos;
      } else {
        // can delete only brackets
        rem_start = opening_brace_pos;
        rem_end = rem_start + 1;
      }
      
      // remove opening brace
      manager.add(new StringEraser(cUnit, rem_start, rem_end));
      
      line_start_pos = cUnit.getSource().getLineIndexer().lineColToPos(
          list.getEndLine(), 0);
      line_end_pos = cUnit.getSource().getLineIndexer().lineColToPos(
          list.getEndLine() + 1, 0);
      
      if (cUnit.getSource().getText(line_start_pos, line_end_pos).trim()
          .equals("}")){
        // can delte all line
        rem_start = line_start_pos;
        rem_end = line_end_pos;
      } else {
        // can delete only brackets
        rem_start = closing_brace_pos;
        rem_end = rem_start + 1;
      }
      
      // remove closing brace
      manager.add(new StringEraser(cUnit, rem_start, rem_end));
    }
    
    return Collections.singleton(cUnit);
  }
  
  /**
   * @return true, if line is like '[spaces]{[spaces]}[spaces]'
   */
  private static final boolean lineWillBeEmptyAfterAction(String text){
    String buf = text.trim();
    if (buf.charAt(0) != '{' || buf.charAt(buf.length()-1) != '}'){
      return false;
    }
    
    if (buf.length() == 2 
        || buf.substring(1, buf.length()-2).trim().length() == 0){
      return true;
    } else {
      return false;
    }
  }
}
