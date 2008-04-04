/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.module.RefactorItContext;


public class FindRequest {
  public static final int FIELDSEARCH = 1;
  public static final int PARAMSEARCH = 2;
  public static final int RETURNSEARCH = 3;
  public static final int TYPECASTSEARCH = 4;
  public static final int INSTANCEOFSEARCH = 5;
  public static final int COMPARISONSEARCH = 6;

  public int searchType;
  public BinTypeRef searchableType;
  public boolean includeSubtypes = false;
  
  /**
   * Factory method for Search
   */
  public AbstractSearch createSearch(final RefactorItContext context, 
      Object target){
    switch (searchType) {
	    case FIELDSEARCH:
	      return new FieldSearch(context, target, this);
	    case PARAMSEARCH:
	      return new ParamSearch(context, target, this);
	    case RETURNSEARCH:
	      return new MethodsReturnSearch(context, target, this);
	    case TYPECASTSEARCH:
	      return new TypeCastSearch(context, target, this);
	    case INSTANCEOFSEARCH:
	    	return new InstanceofSearch(context, target, this);
	    case COMPARISONSEARCH:
	    	return new ComparisonEqSearch(context, target, this);
	    default:
	      return null;
	  }
  }

  public String toString() {
    return "[Search type: " + searchType + " target:'" +
        ((searchableType != null) ? searchableType.getQualifiedName()
        : "NULL!?!?")
        + "' includeSubtypes : " + includeSubtypes + "]";
  }
}
