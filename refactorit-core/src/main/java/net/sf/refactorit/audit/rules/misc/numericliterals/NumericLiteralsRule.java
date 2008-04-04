/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.NumericLiteralsUtils;

import org.w3c.dom.Element;

import java.util.Arrays;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteralsRule extends AuditRule {
  public static final String NAME = "numeric_literals";
  
  private String[] skipedLiterals = null;
  private boolean skipCollections = true;
  private BinTypeRef[] collectionsRefs = null;
  
  private String literalToFind = null;
  
  private boolean traverseInnerClasses = true;
  private boolean aClassAlreadyVisited = false;
  
  public void init(){
    super.init();
    
    final Element configuration = getConfiguration();
    skipedLiterals = AuditProfileUtils.getStringOptionsList(configuration,
        "options", "accepted");
    skipCollections = AuditProfileUtils.getBooleanOption(configuration,
        "options", "skip_collections", skipCollections);
  }
  
  public void visit(BinLiteralExpression expr){
    if (NumericLiteralsUtils.isNumericLiteral(expr)
        && !skipedLiteral(expr.getLiteral())){
      
      BinItemVisitable parentItem = expr.getParent();
      
      if (skipCollections){
        if (parentItem instanceof BinExpressionList 
            && parentItem.getParent() instanceof BinNewExpression){
          BinNewExpression newExpression = (BinNewExpression) parentItem.getParent();
          if (newExpression.getArrayInitExpression() == null
              && isCollectionType(newExpression.getTypeRef())){
            return;
          }
        }
      }
      
      if(parentItem instanceof BinUnaryExpression) {
        parentItem = parentItem.getParent();
      }
      
      if (!(parentItem instanceof BinField)){
        addViolation(new NumericLiteral(expr, getConfiguration()));
      } else if (parentItem instanceof BinField
          && (!((BinField) parentItem).isFinal()
          || !((BinField) parentItem).isStatic())){
        addViolation(new NumericLiteralField(expr, getConfiguration()));
      }
    }
  }
  
  public void visit(BinCIType type){
    if (traverseInnerClasses || !aClassAlreadyVisited){
      this.aClassAlreadyVisited = true;
      super.visit(type);
    }
  }
        
  private boolean skipedLiteral(String literal){
    if (literalToFind != null){
      if (literal.equals(literalToFind)){
        return false;
      }
      return true;
    }
    
    if (Arrays.binarySearch(skipedLiterals, literal) < 0){
      return false;
    }
    return true;
  }
  
  public void setLiteralToFind(final String literalToFind){
    this.literalToFind = literalToFind;
  }

  public void setTraverseInnerClasses(final boolean traverseInnerClasses) {
    this.traverseInnerClasses = traverseInnerClasses;
  }

  private boolean isCollectionType(final BinTypeRef type) {
    // init here due to usages in corrective action 
    if (skipCollections && collectionsRefs == null){
      Project project = type.getProject();
      collectionsRefs = new BinTypeRef[] {
        project.getTypeRefForName("java.util.Collection"),
        project.getTypeRefForName("java.util.Map")};
    }
    
    for (int i = 0; i < collectionsRefs.length; i++){
      if (type.isDerivedFrom(collectionsRefs[i])){
        return true;
      }
    }
    return false;
  }
}
