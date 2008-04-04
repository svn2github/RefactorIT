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
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.List;



/**
 * Encapsulates logic of search for class cast
 * 
 * @author Sergey Fedulov
 */
public class TypeCastSearch extends AbstractSearch {

  TypeCastSearch(RefactorItContext context, Object object, FindRequest findRequest){
    super(context, object, findRequest);
  }
  
  public SearchVisitor createVisitor() {
    return new TypeCastSearchVisitor();
  }

  String getSearchable() {
    return "type casts into type";
  }
  
  /**
   * Overrides addResultsToTable from class AbstractSearch
   */
  protected void addResultsToTable(List results, BinTreeTableNode rootNode){
	  for (int i = 0; i < results.size(); ++i) {
	    BinCastExpression foundCast = (BinCastExpression) results.get(i);
	    BinMember parentMember = foundCast.getParentMember();
	
	    BinTreeTableNode found = new BinTreeTableNode(parentMember);
	    found.addAst(foundCast.getRootAst());
	
	    rootNode.findParent(parentMember.getOwner(), true).addChild(found);
	  }
  }
  

  private class TypeCastSearchVisitor extends AbstractSearch.SearchVisitor {
    public void visit(BinCastExpression cast) {
      BinTypeRef castType = cast.getReturnType();
      
      if (isMatching(castType)) {
        results.add(cast);
      }
      
      super.visit(cast);
    }
  }
}
