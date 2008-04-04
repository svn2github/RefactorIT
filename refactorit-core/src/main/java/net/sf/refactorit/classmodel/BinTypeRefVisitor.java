/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


/**
 * Visitor of BinTypeRef items.
 * Possible to specify different options:
 * : visiting specific ref only (currently - disabled)
 * : visiting self types
 * : visiting types in new expressions  
 * 
 * @author Anton Safonov
 */
public class BinTypeRefVisitor {
  /**
   * Specifies, whether visitor shall visit only BinSpecificTypeRef-s or all BinTypeRef-s.
   * Currently does not work. This is for the future optimization.
   */
  private final boolean visitSpecificRefsOnly;
  
  /**
   * Enables visiting of self type declaration. 
   */
  private boolean checkTypeSelfDeclaration = false;
  
  /**
   * Enables visiting of types in BinNewExpression-s.
   */
  private boolean includeNewExpressions = false;

  public BinTypeRefVisitor() {
    this(true);
  }

  public BinTypeRefVisitor(boolean visitSpecificRefsOnly) {
    this.visitSpecificRefsOnly = visitSpecificRefsOnly;
  }


  public void visit(final BinTypeRef typeRef) {
    typeRef.traverse(this);
  }


  public final boolean isVisitSpecificRefsOnly() {
    return this.visitSpecificRefsOnly;
  }

  public final boolean isCheckTypeSelfDeclaration() {
    return this.checkTypeSelfDeclaration;
  }

  public final void setCheckTypeSelfDeclaration(final boolean checkTypeSelfDeclaration) {
    this.checkTypeSelfDeclaration = checkTypeSelfDeclaration;
  }

  public final boolean isIncludeNewExpressions() {
    return this.includeNewExpressions;
  }

  public final void setIncludeNewExpressions(final boolean includeNewExpressions) {
    this.includeNewExpressions = includeNewExpressions;
  }
}
