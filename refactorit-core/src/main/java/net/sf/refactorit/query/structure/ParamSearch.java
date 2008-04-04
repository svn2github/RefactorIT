/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;

import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.module.RefactorItContext;

import org.apache.log4j.Logger;


/**
 * Contains logic of structure search for method parameters
 * 
 * @author Sergey Fedulov
 */
public class ParamSearch extends AbstractSearch {
  private static final Logger log = AppRegistry.getLogger(ParamSearch.class);

  ParamSearch(RefactorItContext context, Object object, FindRequest findRequest){
    super(context, object, findRequest);
  }

  public SearchVisitor createVisitor() {
    return new ParamSearchVisitor();
  }

  String getSearchable() {
    return "parameters of type";
  }

  
  private class ParamSearchVisitor extends SearchVisitor {
    public void visit(BinLocalVariable var) {
      if (!(var instanceof BinParameter) || var instanceof BinCatchParameter) {
        return;
      }

      BinTypeRef testable = var.getTypeRef();

      if (isMatching(testable)) {
        results.add(var.getParentMember());
      }

      super.visit(var);
    }
  }
}
