/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinParentFinder;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.DependencyParticipant;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.loader.CancelSupport;
import net.sf.refactorit.loader.MethodBodyLoader;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;

import org.apache.log4j.Logger;

import java.util.ArrayList;


public class BinAnnotationExpression extends BinExpression implements BinTypeRefManager {

  private BinTypeRef typeRef;
  private BinExpressionList expressions = BinExpressionList.NO_EXPRESSIONLIST;
  private boolean expressionEnsured = false;

  public BinAnnotationExpression(BinTypeRef ref, ASTImpl rootAst) {
    super(rootAst);
    typeRef = ref;
  }

  public BinTypeRef getReturnType() {
    return typeRef;
  }

  public final boolean isExpressionEnsured() {
    return this.expressionEnsured;
  }

  public final void setExpressionEnsured(final boolean expressionEnsured) {
    this.expressionEnsured = expressionEnsured;
  }

  public void accept(BinTypeRefVisitor visitor) {
    this.typeRef.accept(visitor);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }
//
//  public CompilationUnit getCompilationUnit() {
//    BinCIType parent = (BinCIType) getParent();
//    return parent.getCompilationUnit();
//  }

  public final BinExpressionList getExpressions() {
    ensureExpression();
    return expressions;
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    BinExpressionList exprs = getExpressions();
    exprs.accept(visitor);
  }

  // FIXME: it is almost copy-paste from BinField.ensureExpression()
  private void ensureExpression() {
    Object ob = getOwner();

    if (!getOwner().getBinCIType().isLocal()) {
      CancelSupport.checkThreadInterrupted();
    }

    if (this.isExpressionEnsured()) {
      return;
    }

    try {
      BodyContext context = new BodyContext(
          getOwner().getBinCIType().getCompilationUnit());

      (getOwner().getProject().getProjectLoader().getErrorCollector()).startRecoverableUserErrorSection();

      context.startType(getOwner());
      BinTypeRef annotationTypeRef = ensureReturnType(context);
      context.endType();

      context.startType(annotationTypeRef);

      try {
        callBodyLoader(context);
      } finally {
        context.endType();
        (getOwner().getProject().getProjectLoader().getErrorCollector()).endRecoverableUserErrorSection();
      }
    } catch (SourceParsingException e) {
      Logger log = AppRegistry.getLogger(this.getClass());
      log.error("",e);

      //e.printStackTrace();
      if (!e.isUserFriendlyErrorReported()) {
        (getOwner().getProject().getProjectLoader().getErrorCollector()).addUserFriendlyError(new UserFriendlyError(e.getMessage(),
                this.getCompilationUnit(), this.getStartLine(), this.getStartColumn()));
      }
    } finally {
      this.setExpressionEnsured(true);
      BinParentFinder.findParentsFor(this);
    }
  }

  /**
   * @param context
   */
  private BinTypeRef ensureReturnType(BodyContext context) {
    BinExpressionList savedExpressionList = context.getExpressionList();
    context.setExpressionList(BinExpressionList.NO_EXPRESSIONLIST);
    BinTypeRef annotationTypeRef = null;
    ASTImpl annotationNode = getRootAst();
    BinTypeRef superTypeRef = getReturnType();
    try {
      annotationTypeRef = context.getTypeRef().getProject().getProjectLoader()
          .getMethodBodyLoader().buildAnonymousType(annotationNode, superTypeRef,
              null, context);
    } catch (SourceParsingException e) {
      e.printStackTrace();
    }

    // add dependency for constructor type / annotation constant types owner
    ((DependencyParticipant) context.getTypeRef()).addDependable(context
        .getTypeRef());

    this.typeRef = BinSpecificTypeRef.create(context.getCompilationUnit(),
        (ASTImpl) annotationNode.getFirstChild(), superTypeRef, true);
    context.setExpressionList(savedExpressionList);

    return annotationTypeRef;
  }

  private void callBodyLoader(BodyContext context)
      throws SourceParsingException {
    ASTImpl identNode = (ASTImpl) getRootAst().getFirstChild();
    ASTImpl nextNode = (ASTImpl) identNode.getNextSibling();
    MethodBodyLoader loader = context.getProject().getProjectLoader()
        .getMethodBodyLoader();
    ArrayList exprs = new ArrayList();
    while (nextNode != null) {
      BinExpression expression = loader.buildAnnotationFieldExpression(
          nextNode, context);
      if (expression != null) {
        exprs.add(expression);
      }
      nextNode = (ASTImpl) nextNode.getNextSibling();
    }

    final BinExpressionList expressionList;
    if (exprs.size() > 0) {
      expressionList = new BinExpressionList((BinExpression[]) exprs
          .toArray(new BinExpression[exprs.size()]), getRootAst());
    } else {
      expressionList = BinExpressionList.NO_EXPRESSIONLIST;
    }

    setExpressions(expressionList);

  }

  public final void setExpressions(BinExpressionList expressionList) {
    this.expressions = expressionList;
  }


}
