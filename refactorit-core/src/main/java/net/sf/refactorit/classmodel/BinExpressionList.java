/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.format.BinExpressionListFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;


/**
 * Contains coma separated list of expressions : parameters list, for ( ; ; < statement_list )
 */
public class BinExpressionList extends BinSourceConstruct {

  public static final BinExpressionList NO_EXPRESSIONLIST
      = new BinExpressionList(new BinExpression[0]);

  /**
   * pseudo constructor because we are using BinMethodInvocation as where used target
   */
  public BinExpressionList(BinParameter[] parameters) {
    super(null);
    BinExpression[] expressions = new BinExpression[parameters.length];

    for (int i = 0, max = parameters.length; i < max; i++) {
      expressions[i] = new BinLiteralExpression(parameters[i].getName(),
          parameters[i].getTypeRef(), null);
    }

    init(expressions);
  }

  /**
   * This constructor should be used when constructing from source
   */
  public BinExpressionList(BinExpression[] expressions, ASTImpl rootAst) {
    super(rootAst);
    init(expressions);
  }

  /**
   * This constructor should be used when constructing 'synthetic' expression lists
   * maybe set a property isSynthetic?
   */
  public BinExpressionList(BinExpression[] expressions) {
    this(expressions, null);
  }

  private void init(BinExpression[] expressions) {
    this.expressions = expressions;

    /*for (int i = 0 ; i < expressions.length ; i++) {
     this.expressions[i].setOwnerExpressionList(this);
       }*/
  }

  public BinExpression[] getExpressions() {
    return this.expressions;
  }

  public BinTypeRef[] getExpressionTypes() {
    BinTypeRef result[] = new BinTypeRef[expressions.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = expressions[i].getReturnType();
    }

    return result;
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    for (int i = 0; i < expressions.length; ++i) {
      expressions[i].accept(visitor);
    }
  }

  public String toString() {
    StringBuffer result = new StringBuffer(256);
    result.append(super.toString());

    if (this.expressions.length > 0) {
      result.append(", params: ");
    }
    for (int i = 0; i < this.expressions.length; i++) {
      result.append(i > 0 ? ", " : "");
      result.append(this.expressions[i].getReturnType() != null
          ? this.expressions[i].getReturnType().getName()
          : "null");
    }

    return result.toString();
  }

  public void clean() {
    if (expressions != null) {
      for (int i = 0; i < expressions.length; i++) {
        expressions[i].clean();
      }
    }
    expressions = null;
    super.clean();
  }

  public boolean isSame(BinItem other) {
    if (!(other instanceof BinExpressionList)) {
      return false;
    }
    final BinExpressionList x = (BinExpressionList) other;
    if (this.expressions.length != x.expressions.length) {
      return false;
    }
    for (int i = 0; i < this.expressions.length; i++) {
      if (!this.expressions[i].isSame(x.expressions[i])) {
        return false;
      }
    }

    return true;
  }

  private BinExpression[] expressions;

  public int getExpressionIndex(BinExpression expr) {
    for (int i = 0; i < expressions.length; i++) {
      if (expressions[i] == expr) {
        return i;
      }
    }
    return -1;
  }

  public BinItemFormatter getFormatter() {
    return new BinExpressionListFormatter(this);
  }
}
