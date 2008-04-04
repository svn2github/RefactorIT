/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.AwkwardStatement;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 *
 * @author  Arseni Grigorjev
 */

public class EmptyBlocksAndBodiesRule extends AuditRule {
  public static final String NAME = "empty_blocks";
  
  private boolean skipWithComments = true;
  
  public void init(){
    super.init();
    skipWithComments = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "with_comments", skipWithComments);
  }
  
  /*
   * find empty blocks -> empty statement lists. 
   * for current classmodel case ' if (...) ; ' is rather an empty statement,
   * than an empty block, so they will be ignored by this audit
   */
  public void visit(BinStatementList list) {
    
    BinStatement[] statements = list.getStatements();
    if (statements.length == 0){
      if (!skipWithComments || !containsComments(list)){
        addViolation(new EmptyBlocksAndBodies(list));
      }
    }
    super.visit(list);
  }
  
  private static boolean containsComments(BinStatementList statementList){
    final CompilationUnit source = statementList.getCompilationUnit();
    List comments = source.getSimpleComments();
    for (Iterator it = comments.iterator(); it.hasNext(); ){
      Comment comment = (Comment) it.next();
      if (statementList.contains(comment)){
        return true;
      }
    }

    comments = source.getJavadocComments();
    for (Iterator it = comments.iterator(); it.hasNext(); ){
      Comment comment = (Comment) it.next();
      if (statementList.contains(comment)){
        return true;
      }
    }

    return false;
  }
}

class EmptyBlocksAndBodies extends AwkwardStatement {
  
  public EmptyBlocksAndBodies(BinStatementList stlist) {
    super(stlist, "Empty block or body", "refact.audit.empty_blocks");
  }
    
  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getParentMember();
  }
    
  public List getCorrectiveActions() {
    List actions = new ArrayList(2);
    if (getSourceConstruct().getParent() instanceof BinStatementList){
      actions.add(RemoveBracketsAction.INSTANCE);
    }
    actions.add(InsertEmptyBlockCommentAction.INSTANCE);
    return actions;
  }
}

/*
 * this corrective action will mark empty blocks with CAUTION-comment
 */
class InsertEmptyBlockCommentAction extends MultiTargetCorrectiveAction {
  static final InsertEmptyBlockCommentAction INSTANCE 
      = new InsertEmptyBlockCommentAction();

  public String getKey() {
    return "refactorit.audit.action.empty_blocks.mark";
  }

  public String getName() {
    return "Mark empty block with 'CAUTION' comment";
  }

  public String getMultiTargetName() {
    return "Mark empty blocks with 'CAUTION' comments";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof EmptyBlocksAndBodies)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();
    
    BinSourceConstruct srcConstr = 
        ((AwkwardSourceConstruct) violation).getSourceConstruct();
    
    BinStatementList list = (BinStatementList) ((AwkwardSourceConstruct)
        violation).getSourceConstruct();
    
    StringInserter inserter = new StringInserter(
        compilationUnit, 
        srcConstr.getStartLine(),
        srcConstr.getStartColumn(),
        InsertLineAction.getTextWithIndents(list, "/* CAUTION: empty block! */"));
    manager.add(inserter);

    return Collections.singleton(compilationUnit);
  }
}

