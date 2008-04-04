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
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;


/**
 * @author Oleg Tsernetsov
 */

public class LoopConditionRule extends AuditRule{
	
	
public static final String NAME = "loop_condition";
	
	public void visit(BinAssignmentExpression expression){
		if((expression.getParent() instanceof BinWhileStatement)||
                (expression.getParent() instanceof BinForStatement)){
			addViolation(new DemoBoolViolation(expression));
		}
		super.visit(expression);
	}
}


class DemoBoolViolation extends AwkwardSourceConstruct {
	public DemoBoolViolation(BinSourceConstruct construct){
		super(construct, "Boolean assignment in loop condition", "refact.audit.loop_condition");
	}
	
	public List getCorrectiveActions(){
		return Collections.singletonList(ReplaceBoolEquationAssignment.INSTANCE);
	}
	
	public BinMember getSpecificOwnerMember(){
		return (getSourceConstruct()).getParentMember();
	}
}

class ReplaceBoolEquationAssignment extends MultiTargetCorrectiveAction{
	static final ReplaceBoolEquationAssignment INSTANCE = 
		new ReplaceBoolEquationAssignment();
	
	public String getKey(){
		return "refactorit.audit.action.loop_condition.replace";
	}
	
	public String getName(){
		return "Replace assignment with comparison operator";
	}
	
	public String getMultiTargetName(){
		return "Replace assignments with comparison operators";
	}
	
	protected java.util.Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation){
		if(!(violation instanceof DemoBoolViolation)){
			return Collections.EMPTY_SET; // Foreign violations, do nothing
		}
		
		CompilationUnit compilationUnit = violation.getCompilationUnit();
	    
	    BinAssignmentExpression expr = (BinAssignmentExpression) ((AwkwardSourceConstruct)
	        violation).getSourceConstruct();
	    BinExpression leftExpr = expr.getLeftExpression();
	    BinExpression rightExpr = expr.getRightExpression();
	    
	    String newLine = leftExpr.getText()+" == "+rightExpr.getText();

	    StringEraser eraser = new StringEraser(
	    		compilationUnit,
	    		expr.getStartLine(),
	    		expr.getStartColumn()-1,
	    		expr.getEndLine(),
	    		expr.getEndColumn()-1,false,false);
	    manager.add(eraser);
	    
	    StringInserter inserter = new StringInserter(
	        compilationUnit, 
	        expr.getStartLine(),
	        expr.getStartColumn(),
	        newLine);
	    manager.add(inserter);
	    
		return Collections.singleton(compilationUnit);
	}
	
}
