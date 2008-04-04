/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.List;


/**
*    Logic of searching comparison operator 
*    @author Oleg Tsernetsov
*/

public class ComparisonEqSearch extends AbstractSearch{
	
	public ComparisonEqSearch(RefactorItContext context, Object object, FindRequest findRequest){
		super(context, object, findRequest);
	}
	
	public SearchVisitor createVisitor(){
		return new ComparisonSearchVisitor();
	}
	
	String getSearchable(){
		return "comparison operator`s any argument of type";
	}

	protected void addResultsToTable(List results, BinTreeTableNode rootNode){
		for (int i = 0; i < results.size(); ++i) {
			
			BinExpression expr = (BinExpression) results.get(i);
			if(expr.getParent() instanceof BinLogicalExpression){
				BinLogicalExpression foundExpr =  (BinLogicalExpression) expr.getParent() ;
				BinMember parentMember = foundExpr.getParentMember();
				
				BinTreeTableNode found = new BinTreeTableNode(parentMember);
				found.addAst(foundExpr.getRootAst());
				
				String secondaryText="";
				if(foundExpr.getLeftExpression() == expr) secondaryText=" Left argument";
				else secondaryText=" Right argument";
				
				if(foundExpr.getAssigmentType() == JavaTokenTypes.EQUAL)
					secondaryText = secondaryText + " { == } ";
				else secondaryText = secondaryText + " { != } ";
				
				if(expr instanceof BinLiteralExpression) secondaryText = secondaryText+" (Literal expression)";
				found.setSecondaryText(secondaryText);
				rootNode.findParent(parentMember.getOwner(), true).addChild(found);
			}
		}
	}
	
	private class ComparisonSearchVisitor extends SearchVisitor{
		public void visit(BinLogicalExpression expr){
			if((expr.getAssigmentType() == JavaTokenTypes.EQUAL) || 
			   (expr.getAssigmentType() == JavaTokenTypes.NOT_EQUAL)){
				
				BinTypeRef testable = expr.getLeftExpression().getReturnType();
				
				if((testable != null) && (isMatching(testable))){
					results.add(expr.getLeftExpression());
					super.visit(expr);
				}
			
				testable = expr.getRightExpression().getReturnType();
				if((testable != null) && (isMatching(testable))){
					results.add(expr.getRightExpression());
					super.visit(expr);
				}	
			}
		}
	}
}
