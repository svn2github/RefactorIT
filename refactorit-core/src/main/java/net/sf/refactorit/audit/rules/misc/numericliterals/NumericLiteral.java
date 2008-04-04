/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;


import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;

import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteral extends AwkwardExpression {
  private String literal;
  private Element configuration;
  
  private NumLitFix fix = null;
  private boolean constantalizable = false;
  private boolean constantalizeChecked = false;
  
  public NumericLiteral(String message, BinLiteralExpression expression, 
      Element config){
    super(expression, message, "refact.audit.numeric_literals");
    
    this.literal = expression.getLiteral();
    this.configuration = config;
  }
  
  public NumericLiteral(BinLiteralExpression expression, Element config){
    this("Use of a \"magic number\" (numeric literal) - 'static final' " 
        + "constant should be used.", expression, config);
  }
  
  public Element getConfiguration(){
    return this.configuration;
  }
  
  public List getCorrectiveActions() {
    return Collections.singletonList(ManageNumericLiteralsAction.instance);
  }
    
  public String getLiteral(){
    return this.literal;
  }
  
  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentType();
  }
      
  public final NumLitFix getFix(){
    return this.fix;
  }
  
  public void clearFix(){
    this.fix = null;
  }
  
  public final void setFix(NumLitFix fix){
    this.fix = fix;
  }
  
  public final boolean hasFix(){
    return this.fix != null;
  }

  public final boolean isConstantalizable(){
    return this.constantalizable;
  }
  
  public final void setConstantalizable(final boolean constantalizable){
    this.constantalizable = constantalizable;
  } 
  
  /**
   * The check that says, if the field can be made static final or not is 
   * rather heavy (can visit() all project in one pass!). So constantalizable
   * attribute sheuld be counted only once.
   */
  public final boolean isConstantalizeChecked(){
    return this.constantalizeChecked;
  }
  
  public final void setConstantalizeChecked(
      final boolean constantalizeChecked){
    this.constantalizeChecked = constantalizeChecked;
  }
}
