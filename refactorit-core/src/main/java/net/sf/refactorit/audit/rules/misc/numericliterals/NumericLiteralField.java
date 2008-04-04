/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;

import org.w3c.dom.Element;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteralField extends NumericLiteral {
  public NumericLiteralField(BinLiteralExpression expression, Element config){
    super("Use of a \"magic number\" (numeric literal) - 'static final' " 
        + "constant should be used.", expression, config);
  }
   
  public final BinField getField(){
    return (BinField) ((BinLiteralExpression) getSourceConstruct()).getParent();
  }
}
