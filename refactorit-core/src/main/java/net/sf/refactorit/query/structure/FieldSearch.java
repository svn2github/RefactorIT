/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.module.RefactorItContext;

import org.apache.log4j.Logger;



/**
 * Contains logic of structure search for fields
 * 
 * @author Sergey Fedulov
 */
class FieldSearch extends AbstractSearch {
  private static final Logger log = AppRegistry.getLogger(FieldSearch.class);

  FieldSearch(RefactorItContext context, Object object, FindRequest findRequest){
    super(context, object, findRequest);
  }
  
  public SearchVisitor createVisitor() {
    return new FieldSearchVisitor();
  }


  String getSearchable() {
    return "fields of type";
  }
  
  private class FieldSearchVisitor extends SearchVisitor {
    public void visit(BinField field) {
      BinTypeRef testable = field.getTypeRef();
      
      if (isMatching(testable)) {
        results.add(field);
      }
      
      // we still keep visiting to get local types also;
      super.visit(field);
    }
  }
}
