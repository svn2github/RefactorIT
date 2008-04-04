/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import java.util.HashMap;


public interface Scope {
  void initScope(HashMap variableMap, HashMap typeMap);

//  ScopeRules getScopeRules();
//
//  class ScopeRules {
//
//    public ScopeRules(Scope aScope, HashMap variableMap, HashMap typeMap) {
//      // these were not used but wasted a lot of memory for no reason
//      Collection variables;
//      Collection types;
//      Scope myScope;
//
//      myScope = aScope;
//      variables = variableMap.values();
//      types = typeMap.values();
//
//      for (Iterator i = variables.iterator(); i.hasNext(); ) {
//        BinLocalVariable var = (BinLocalVariable) i.next();
//        var.setScope(myScope);
//      }
//      for (Iterator i = types.iterator(); i.hasNext(); ) {
//        BinTypeRef typeRef = (BinTypeRef) i.next();
//        typeRef.setScope(myScope);
//      }
//    }
//  }

  boolean contains(Scope other);
}
