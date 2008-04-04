/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.source.format.BinArithmeticalExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BinArithmeticalExpression extends BinExpression {
  public BinArithmeticalExpression(BinExpression leftExpression,
      BinExpression rightExpression, ASTImpl rootAst) {
    super(rootAst);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  // TODO: optimize
  /**
   * Based on JLS3 (§5.6.2)
   * When an operator applies binary numeric promotion to a pair of operands, 
   * each of which must denote a value that is convertible to a numeric type, 
   * the following rules apply, in order, using widening conversion (§5.1.2) 
   * to convert operands as necessary:
   * 1) If any of the operands is of a reference type, unboxing conversion (§5.1.8)
   *  is performed. Then:
   * 2) If either operand is of type double, the other is converted to double.
   * 3) Otherwise, if either operand is of type float, the other is converted to float.
   * 4) Otherwise, if either operand is of type long, the other is converted to long.
   * 5) Otherwise, both operands are converted to type int.
   */
  public final BinTypeRef getReturnType() {
    BinTypeRef leftReturnType = leftExpression.getReturnType();
    BinTypeRef rightReturnType = rightExpression.getReturnType();

    if(leftReturnType.isReferenceType()) {
      Object candidate = getUnboxedReferenceType(leftReturnType);
      if(candidate == null) {
        return leftReturnType;
      }
      leftReturnType = (BinTypeRef)candidate;
    }
    
    if(rightReturnType.isReferenceType()) {
      Object candidate = getUnboxedReferenceType(rightReturnType);
      if(candidate == null) {
        return rightReturnType;
      }
      rightReturnType = (BinTypeRef)candidate;
    }
    
    
    // JAVA5: generics; and this not correct place for this logic
    if(leftReturnType.equals(BinPrimitiveType.BOOLEAN_REF)
      || rightReturnType.equals(BinPrimitiveType.BOOLEAN_REF)) {
        return BinPrimitiveType.BOOLEAN_REF;
      }
      
    
    int leftPriority = ((BinPrimitiveType)leftReturnType.getBinType())
    	.getPriority();
    
    int rightPriority = ((BinPrimitiveType) rightReturnType.getBinType())
    	.getPriority();

    int returnType;
    if (rightPriority != leftPriority) {
        if (Assert.enabled) {
          Assert.must((rightPriority != -1
            && leftPriority != -1), "Expression have different types");
        }
        returnType = Math.max(leftPriority, rightPriority);
      } else {
        returnType = leftPriority;
      }
    
    switch (returnType) {
    case 3:
      return BinPrimitiveType.DOUBLE_REF;
    case 2:
      return BinPrimitiveType.FLOAT_REF;
    case 1:
      return BinPrimitiveType.LONG_REF;
    default:
      return BinPrimitiveType.INT_REF;
    }
  }

  public final BinExpression getLeftExpression() {
    return this.leftExpression;
  }

  public final BinExpression getRightExpression() {
    return this.rightExpression;
  }

  public final int getType() {
    return this.getRootAst().getType();
  }

  public final int getLevel() {
    return this.getRootAst().getLevel();
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    leftExpression.accept(visitor);
    rightExpression.accept(visitor);
  }

  public final void clean() {
    leftExpression.clean();
    leftExpression = null;
    rightExpression.clean();
    rightExpression = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinArithmeticalExpression)) {
      return false;
    }

    final List thisDecomposed = this.decompose();
    final List otherDecomposed = ((BinArithmeticalExpression) other).decompose();
    return isSame(thisDecomposed, otherDecomposed);
  }

  public static boolean isSame(
      final List firstDecomposed, final List secondDecomposed) {
    if (firstDecomposed.size() != secondDecomposed.size()) {
      return false;
    }

    for (int i = 0, max = firstDecomposed.size(); i < max; i++) {
      if ((i + 2) % 2 == 0 && max > 1) {
        if (!((BinItem) firstDecomposed.get(i)).isSame(
            (BinItem) secondDecomposed.get(i))) {
          return false;
        }
      } else {
        if (((BinArithmeticalExpression) firstDecomposed.get(i)).getType()
            != ((BinArithmeticalExpression) secondDecomposed.get(i)).getType()) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * @param start list of inclusive interval start indexes
   * @param end list of exclusive interval end indexes
   */
  public final void group(final Integer[] start, final Integer[] end) {
//System.err.println("group: " + this +" - " + Arrays.asList(start) + " - " + Arrays.asList(end));
    final List items = decompose();

    if (items.size() == 0) {
      if (Assert.enabled) {
        Assert.must(false,
            "Grouping called for non-decomposable expression: " + this);
      }
      return;
    }

    ASTImpl parent = this.getRootAst().getParent();
    List children = ASTUtil.getChildren(parent);

    final List result = new ArrayList(items.size());

    boolean grouped = false;
    for (int i = 0; i < start.length; i++) {
      if (i == 0 && start[i].intValue() != 0) {
        result.addAll(items.subList(0, start[i].intValue()));
      }

      if (end[i].intValue() - start[i].intValue() >= 3) { // composable ?
        final BinArithmeticalExpression groupedExpr
            = compose(items.subList(start[i].intValue(), end[i].intValue()));
        result.add(groupedExpr);
        grouped = true;
      } else {
        result.addAll(items.subList(start[i].intValue(), end[i].intValue()));
      }

      if (i == end.length - 1 && end[i].intValue() < items.size()) {
        result.addAll(items.subList(end[i].intValue(), items.size()));
      }
    }

    if (items.size() <= 1 || !grouped) {
      if (Assert.enabled) {
        Assert.must(!grouped, "Called grouping for single expression: "
            + items.get(0));
      }
      return;
    }

    final BinArithmeticalExpression newExpr = compose(result);

    // replace existing expression content
    this.leftExpression = newExpr.leftExpression;
    this.leftExpression.setParent(this);
    this.rightExpression = newExpr.rightExpression;
    this.rightExpression.setParent(this);
    final ASTImpl newRoot = newExpr.getRootAst();
    if (this.getRootAst() != newRoot) {
      for (int i = 0, max = children.size(); i < max; i++) {
        ASTImpl child = (ASTImpl) children.get(i);
        if (child == this.getRootAst()) {
          if (i == 0) {
            parent.setFirstChild(newRoot);
          } else {
            ((ASTImpl) children.get(i - 1)).setNextSibling(newRoot);
          }
          if (i + 1 < max) {
            newRoot.setNextSibling((ASTImpl) children.get(i + 1));
          }
          newRoot.setParent(parent);
          break;
        }
      }
      this.setRootAst(newRoot);
    }

    this.decomposed = null;
  }

  /**
   * @param items decomposed and may be regrouped tree in a sequential form
   * @return new tree
   */
  private final BinArithmeticalExpression compose(final List items) {
    if (items.size() == 1) {
      if (items.get(0) instanceof BinArithmeticalExpression) {
        return (BinArithmeticalExpression) items.get(0);
      } else {
        if (Assert.enabled) {
          Assert.must(false, "Single expression and not arithmetical: "
              + items.get(0));
        }
      }
    }

//System.err.println("compose: " + items);
    BinExpression left;
    if (items.size() <= 3) {
      left = (BinExpression) items.get(0);
    } else {
      left = compose(items.subList(0, items.size() - 2));
    }
    final BinArithmeticalExpression middle
        = (BinArithmeticalExpression) items.get(items.size() - 2);
    final BinExpression right
        = (BinExpression) items.get(items.size() - 1);
//System.err.println("middle: " + middle);
//System.err.println("left: " + left);
//System.err.println("right: " + right);

    if (middle.getLeftExpression() == left
        && middle.getRightExpression() == right) {
      middle.setRecomposed(false); // even if it was recomposed earlier, now we didn't do that
      return middle; // already existing exactly what we need
    }

    // restructure asts also to resemble new expression structure
    final ASTImpl newRoot = middle.getRootAst();
    final ASTImpl leftAST = getRootAstOrParenthesis(left);
    newRoot.setFirstChild(leftAST);
    leftAST.setParent(newRoot);
    final ASTImpl rightAST = getRootAstOrParenthesis(right);
    leftAST.setNextSibling(rightAST);
    rightAST.setParent(newRoot);
    rightAST.setNextSibling(null);

    newRoot.setNextSibling(null);

    final BinArithmeticalExpression newExpr
        = new BinArithmeticalExpression(left, right, newRoot);
    newExpr.setRecomposed(true);
    newExpr.setParent(this); // to have ASTs to be able to resolve, will be replaced right upon exit
    left.setParent(newExpr);
    right.setParent(newExpr);

    return newExpr;
  }

  private ASTImpl getRootAstOrParenthesis(final BinExpression expr) {
    ASTImpl ast = expr.getRootAst();
    if (!(expr instanceof BinArithmeticalExpression)
        || !((BinArithmeticalExpression) expr).isRecomposed()) {
      while (ast.getParent().getType() == JavaTokenTypes.LPAREN) {
        ast = ast.getParent();
      }
    }

    return ast;
  }

  /**
   * Presents as list that part of the tree starting with this node that has the
   * same level and no brackets.
   * @return decomposed tree
   */
  public final List decompose() {
    if (this.decomposed != null) {
      return this.decomposed;
    }

    final int thisLevel = getLevel();
    final ArrayList items = new ArrayList();
    final class Splitter extends SinglePointVisitor {
      /** Overrides */
      public final void onEnter(Object o) {}

      /** Overrides */
      public final void onLeave(Object o) {
        if (!shouldVisitContentsOf((BinItem) o)) { // leaf
          items.add(o);
        } else {
          items.add(items.size() - 1, o);
        }
      }

      /** Overrides */
      public final boolean shouldVisitContentsOf(BinItem x) {
        return x == BinArithmeticalExpression.this
            || (x instanceof BinArithmeticalExpression
            && ((BinArithmeticalExpression) x).getLevel() == thisLevel
            && ((BinArithmeticalExpression) x).getRootAst().getParent().getType()
            != JavaTokenTypes.LPAREN);
      }
    }


    Splitter splitter = new Splitter();
    this.accept(splitter);

//    Iterator it = items.iterator();
//    while (it.hasNext()) {
//      Object obj = it.next();
//      System.err.println("item: " + obj);
//      if (obj instanceof BinSourceConstruct) {
//        System.err.println("ast: " + ((BinSourceConstruct) obj).getRootAst());
//      }
//    }

    // fighting with array being stored in local class and causing memory leak
    this.decomposed = new ArrayList(items);
    items.clear();

    return this.decomposed;
  }

  private BinExpression leftExpression;
  private BinExpression rightExpression;

  private List decomposed = null; // tree in a row form
  private boolean recomposed = false;

  public final boolean isRecomposed() {
    return this.recomposed;
  }

  public final void setRecomposed(final boolean recomposed) {
    this.recomposed = recomposed;
  }

  public final BinItemFormatter getFormatter() {
    return new BinArithmeticalExpressionFormatter(this);
  }

  public static BinArithmeticalExpression createSynthetic(
      BinExpression leftExpression,
      BinExpression rightExpression, ASTImpl ast) {
    return new BinArithmeticalExpression(leftExpression, rightExpression, ast) {

      private ASTImpl rootAst;

      protected void setRootAst(final ASTImpl rootAst) {
        this.rootAst = rootAst;
      }

      public ASTImpl getRootAst() {
        return rootAst;
      }
    };
  }
  
  private static HashMap referenceTypeUnboxing = null; 
  
  
  /**
   * returns an unboxed (not casted) BinTypeRef, or null otherwise
   */
  private static Object getUnboxedReferenceType(BinTypeRef type) {
    if(referenceTypeUnboxing == null) {
      referenceTypeUnboxing = new HashMap();
      referenceTypeUnboxing.put("java.lang.Boolean", BinPrimitiveType.BOOLEAN_REF);
      referenceTypeUnboxing.put("java.lang.Byte", BinPrimitiveType.BYTE_REF);
      referenceTypeUnboxing.put("java.lang.Character", BinPrimitiveType.CHAR_REF);
      referenceTypeUnboxing.put("java.lang.Short", BinPrimitiveType.SHORT_REF);
      referenceTypeUnboxing.put("java.lang.Integer", BinPrimitiveType.INT_REF);
      referenceTypeUnboxing.put("java.lang.Long", BinPrimitiveType.LONG_REF);
      referenceTypeUnboxing.put("java.lang.Float", BinPrimitiveType.FLOAT_REF);
      referenceTypeUnboxing.put("java.lang.Double", BinPrimitiveType.DOUBLE_REF);
    }
    return referenceTypeUnboxing.get(type.getQualifiedName());
  }
}
