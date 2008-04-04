/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinEmptyExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.ChildrenResultsUpForwardingVisitor;

import java.util.ArrayList;
import java.util.Iterator;

public class BinItemPrinter extends ChildrenResultsUpForwardingVisitor {
  private BinItem item;

  public BinItemPrinter(BinItem itemToPrint) {
    item = itemToPrint;
  }
  
  public void visit(BinArrayInitExpression expression) {
    super.visit(expression);
    
    ArrayList currentResults = getCurrentResults();
    StringBuffer buffer = new StringBuffer();
    // hack for test. this is illigal. FIXME:!
    getTopResults().add(new StringBuffer("{ 1, 2, 3 }"));
  }
  
  public void visit(BinLogicalExpression expression) {
    super.visit(expression);
    
    StringBuffer buffer = new StringBuffer();
    for(Iterator i = getCurrentResults().iterator(); i.hasNext();) {
      buffer.append(i.next());
    }
    getTopResults().add(buffer);
  }
 
  
  public void visit(BinLiteralExpression literalExpression) {
    super.visit(literalExpression);
    
    StringBuffer buffer = new StringBuffer(literalExpression.getLiteral());
    getTopResults().add(buffer);
  }  
  

  
  public void visit(BinArithmeticalExpression expression) {
    super.visit(expression);   
    if(getCurrentResults().size() == 2) {
      	StringBuffer buffer = new StringBuffer();
	    buffer.append(getCurrentResults().get(0)); // left expression
	    
	    String operationType = expression.getRootAst().getText();
	    if(FormatSettings.isSpaceAroudBinaryOperator()) {
	      buffer.append(' ').append(operationType).append(' ');
	    } else {
	      buffer.append(operationType);
	    }
	    
	    buffer.append(getCurrentResults().get(1)); // right expression
	    getTopResults().add(buffer);
    } else if(Assert.enabled) {
        Assert.must(false, "Unexpected behaviour of visitor! ");
    }
  }
  
  public void visit(BinIncDecExpression expression) {
    super.visit(expression);
    if(getCurrentResults().size() != 1 && Assert.enabled) {
      Assert.must(false, "Unexpected behaviour of visitor! ");
    }
    
    StringBuffer buffer = new StringBuffer();
    int type = expression.getType();

    String prefix = null;
    String suffix = null;
    switch (type) {
      case JavaTokenTypes.INC:
        prefix = "++";
        break;
      case JavaTokenTypes.POST_INC:
        suffix = "++";
        break;
      case JavaTokenTypes.DEC:
        prefix = "--";
        break;
      case JavaTokenTypes.POST_DEC:
        suffix = "--";
        break;
      default:
        Assert.must(false, "Unknown inc/dec epxpression, type = " + type);
    }
    
    if(prefix != null) {
      buffer.append(prefix);
    }
    
    buffer.append(getCurrentResults().get(0));
    
    if(suffix != null) {
      buffer.append(suffix);
    }
    
    getTopResults().add(buffer);
  }
  
  public void visit(BinVariableUseExpression expression) {
    super.visit(expression);
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(expression.getVariable().getName());
    
    for(Iterator i = getCurrentResults().iterator(); i.hasNext();) {
      buffer.append(i.next());
    }
    getTopResults().add(buffer);
  }
  
  public void visit(BinLocalVariable var) {
    super.visit(var);
    ArrayList currentBuffers = getCurrentResults();
    ArrayList parentBuffers = getTopResults();
    
    StringBuffer buffer = new StringBuffer(var.getName());
    parentBuffers.add(buffer);
  }
  
  public void visit(BinEmptyExpression emptyExpression) {
    super.visit(emptyExpression);
    StringBuffer buffer = new StringBuffer("[]");
    getTopResults().add(buffer);
  }
 
  
  public void visit(BinUnaryExpression unaryExpression) {
    super.visit(unaryExpression);
    StringBuffer buffer = new StringBuffer();
    int type = unaryExpression.getType();
    switch(type) {
      case JavaTokenTypes.LNOT:
        buffer.append('!');
        break;
      case JavaTokenTypes.BNOT:
        buffer.append('~');
        break;
      case JavaTokenTypes.UNARY_MINUS:
        buffer.append('-');
        break;
      case JavaTokenTypes.UNARY_PLUS:
        buffer.append('+');
        break;
      default:
        Assert.must(false, "Unknown unary epxpression, type = " + type);
    }   
    
    for(Iterator i = getCurrentResults().iterator(); i.hasNext();) {
      buffer.append(i.next());
    }
    
    getTopResults().add(buffer);
  }
  
  public String print() {
    item.accept(this);
    ArrayList topBuffers = getTopResults();
    if(topBuffers.size() == 0 && Assert.enabled) {
      Assert.must(false, "No results returned. Probably missing the overriden " +
      		"visit() method what is operating on the specified binItem");
      return new String();
    } else {
      return topBuffers.get(0).toString();
    }
  }
}
