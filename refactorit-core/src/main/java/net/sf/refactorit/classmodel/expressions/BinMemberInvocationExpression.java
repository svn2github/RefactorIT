/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinConvertorTypeRef;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.FakeBinMemberInvocationReference;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.format.BinFormatter;



public abstract class BinMemberInvocationExpression extends BinExpression {

  public BinMemberInvocationExpression(BinMember member,
      BinExpression expression,
      BinTypeRef invokedOn,
      ASTImpl rootAst) {
    super(rootAst);
    if (Assert.enabled && member == null) {
      Assert.must(false, "Illegal use of member invocation expression");
    }

    this.member = member;
    this.expression = expression;
    this.invokedOn = invokedOn;
  }

  public final BinMember getMember() {
    return this.member;
  }

  public abstract BinTypeRef getReturnType();

  public final BinExpression getExpression() {
    return this.expression;
  }

  public final BinTypeRef getInvokedOn() {
    return this.invokedOn;
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    // expression before DOT
    if (this.expression != null) {
      this.expression.accept(visitor);
    }
  }

  public final String getDetails() {
    return BinFormatter.formatNotQualified(this.invokedOn) + " - "
        + BinFormatter.format(this.member);
  }

  public final String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": \"" + this.member
        + "\" in " + getCompilationUnit() + " "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + " "
        + Integer.toHexString(hashCode());
  }

  /**
   * @return <code>false</code> when member called either on <code>super</code>
   * or <code>this</code> (including implicit <code>this</code> calls)
   */
  public final boolean isOutsideMemberInvocation() {

    //FIXME: following check seems to be correct for the first glance,
    // but breaks WhereUsed test 258
    /*    if (getMember().isStatic()) {
          return true;
        }*/

    return!(invokedViaThisReference() || invokedViaSuperReference());
  }

  public final boolean invokedViaSuperReference() {
    return getExpression() instanceof BinLiteralExpression
        && ((BinLiteralExpression) getExpression()).isSuper();
  }

  /** Includes implicit and explicit usages both
   */
  public final boolean invokedViaThisReference() {
  	//FIXME: do static imports break this logic?
    return getExpression() == null
        || (getExpression() instanceof BinLiteralExpression
        && ((BinLiteralExpression) getExpression()).isThis());
  }

  /**
   * Determines if member is used via single or on-demand static import.
   *
   * @return
   */
  public final boolean invokedViaStaticImport() {
  	return (getMember().isStatic()
				&& (getExpression() == null)
  			&& (!getInvokedOn().getBinCIType().getTypeRef().isDerivedFrom(getMember().getOwner())));
  }

  public static final BinTypeRef convertTypeParameter(
      final BinTypeRef superReturn, BinTypeRef returnType) {
    if (returnType == null || !returnType.isReferenceType()) {
      return returnType;
    }

//System.err.println("returnType: " + returnType + " - " + returnType.getBinType().isTypeParameter() + " -- " + superReturn);
    if (returnType.getBinType().isTypeParameter()
        || (returnType.isArray()
        && returnType.getNonArrayType().getBinType().isTypeParameter())) {
      if (superReturn != null && superReturn.isSpecific()) {
        // JAVA5: this array handling looks like a hack...
        int dim = -1;
        BinTypeRef typeToSearch = returnType;
        if (typeToSearch.isArray()) {
          dim = ((BinArrayType) typeToSearch.getBinCIType()).getDimensions();
          typeToSearch = typeToSearch.getNonArrayType();
        }

        BinTypeRef realType = ((BinSpecificTypeRef) superReturn)
            .findTypeArgumentByParameter(typeToSearch);
//        Project project = Project.getProjectFor(superReturn);
        if (realType != null
            // small hack - better not convert to unbound wildcard,
            // type parameter have more info and will resolve better
            && !realType.equals(realType.getProject().getObjectRef())) {
          if (dim < 0) {
            returnType = realType;
          } else {
            returnType = realType.getProject().createArrayTypeForType(realType, dim);
          }

          if (returnType.hasUnresolvedTypeParameters()
              && !returnType.getBinType().isTypeParameter()) {
            BinConvertorTypeRef conversionAwareRef
                = new BinConvertorTypeRef(returnType, superReturn);
            returnType = conversionAwareRef;
          }
        }
      }
    } else if (returnType.hasUnresolvedTypeParameters()) {
      BinConvertorTypeRef conversionAwareRef
          = new BinConvertorTypeRef(returnType, superReturn);
      returnType = conversionAwareRef;
    }
    //        System.err.println("super: " + superReturn);

    return returnType;
  }


  public ASTImpl getNameAst() {
    if (this.nameAst == -1) {
      this.nameAst = ASTUtil.indexFor(this.getRootAst());
    }
    return getCompilationUnit().getSource().getASTByIndex(this.nameAst);
  }

  public void setNameAst(final ASTImpl nameAst) {
    this.nameAst = ASTUtil.indexFor(nameAst);
  }

  public final ASTImpl getClickableNode() {
    return getNameAst();
  }

  public void clean() {
    nameAst = -1;
    member = null;
    if (expression != null) {
      expression.clean();
      expression = null;
    }
    invokedOn = null;
    super.clean();
  }

  public BinItemReference createReference() {
    if (getRootAst() == null){
      return new FakeBinMemberInvocationReference(this);
    } else {
      return super.createReference();
    }
  }

  private BinMember member;
  private BinExpression expression;
  private BinTypeRef invokedOn;

  private int nameAst = -1;
}
