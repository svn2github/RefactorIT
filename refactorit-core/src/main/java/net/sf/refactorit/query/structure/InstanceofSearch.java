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
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.List;



/**
*    Logic of searching operator instanceof
*    @author Oleg Tsernetsov
*/

public class InstanceofSearch extends AbstractSearch{
	
	public InstanceofSearch(RefactorItContext context, Object object, FindRequest findRequest){
		super(context, object, findRequest);
	}
	
	public SearchVisitor createVisitor(){
		return new InstanceofSearchVisitor();
	}
	
	String getSearchable(){
		return "instanceof operator with right parameter type";
	}

	protected void addResultsToTable(List results, BinTreeTableNode rootNode){
		for (int i = 0; i < results.size(); ++i) {
			BinInstanceofExpression foundExpr = (BinInstanceofExpression) results.get(i);
			BinMember parentMember = foundExpr.getParentMember();
			
			BinTreeTableNode found = new BinTreeTableNode(parentMember);
			found.addAst(foundExpr.getRootAst());
			
			rootNode.findParent(parentMember.getOwner(), true).addChild(found);
		}
	}
	
	private class InstanceofSearchVisitor extends SearchVisitor{
		public void visit(BinInstanceofExpression expr){
			BinTypeRef testable = expr.getRightExpression().getReturnType();
			if(isMatching(testable)){
				results.add(expr);
				super.visit(expr);
			}
		}
	}
}
