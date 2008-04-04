/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinInitializerReference;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.loader.CancelSupport;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.PositionsForNewItems;

import java.util.HashMap;


public final class BinInitializer extends BinMember implements Scope {

  public static final BinInitializer[] NO_INITIALIZERS = new BinInitializer[0];

  public BinInitializer(String name, int modifiers) {
    super(name, modifiers, null); // owner will be set with setOwner later
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    BinStatementList statements = getStatementList();
    if (statements != null) { // in synthetic it does
      statements.accept(visitor);
    }
  }

  public void setStatementList(BinStatementList statementList) {
    this.statementList = statementList;
    this.statementListEnsured = true;
  }

  /**
   * Gets AST node corresponding to the body of this method.
   * Body is part of declaration enclosed in <code>{</code> and
   * <code>}</code> brackets containing statements implementing this method.
   *
   * @return AST node or <code>null</code> if node is not known
   *         (e.g. if this method is abstract).
   */
  public ASTImpl getBodyAST() {
    ASTImpl node = (ASTImpl) getOffsetNode().getFirstChild();
    while ((node != null) && (node.getType() != JavaTokenTypes.SLIST)) {
      node = (ASTImpl) node.getNextSibling();
    }

    return node;
  }

  public ASTImpl getNameAstOrNull() {
    return getOffsetNode();
  }

  public SourceCoordinate findNewStatementPositionAtEnd() {
    return PositionsForNewItems.findNewStatementPositionAtEnd(this);
  }

  private void ensureStatementList() {
    if (!getOwner().getBinCIType().isLocal()) {
      CancelSupport.checkThreadInterrupted();
    }

    if (this.statementListEnsured) {
      return;
    }

    try {
      BodyContext context = new BodyContext(
          getOwner().getBinCIType().getCompilationUnit()/*, this*/);

      getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();
      context.startType(getOwner());
      try {
        getProject().getProjectLoader().getMethodBodyLoader()
            .buildInitializerBody(this, context);
      } finally {
        context.endType();
        getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();
      }
    } catch (NullPointerException e) {
      // JAVA5: falls here, when switch to 1.3 and encounter enum
      e.printStackTrace();
      getProject().getProjectLoader().getErrorCollector().addUserFriendlyError(new UserFriendlyError(
            "Possible enum construct - switch Java Version Support in Project Options",
            this.getCompilationUnit(), this.getStartLine(), this.getStartColumn()));
      this.statementList = new BinStatementList(BinStatement.NO_STATEMENTS, null);
    } catch (SourceParsingException e) {
      e.printStackTrace();
      getProject().getProjectLoader().getErrorCollector().addUserFriendlyError(new UserFriendlyError(e.getMessage(),
            this.getCompilationUnit(), this.getStartLine(), this.getStartColumn()));
    } finally {
      this.statementListEnsured = true;
    }
  }

  public boolean hasStatementList() {
    return statementList != null;
  }

  public BinStatementList getStatementList() {
    ensureStatementList();
    return this.statementList;
  }

  public void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public boolean contains(Scope other) {
    if (other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else {
      return false;
    }
  }

  public String getMemberType() {
    return memberType;
  }

  /**
   * @return	true if this member is deprecated
   */
  public final boolean isDeprecated() {
    return deprecated;
  }

  public final void setDeprecated(final boolean setTo) {
    deprecated = setTo;
  }

  public void clean() {
    if (statementList != null) {
//      statementList.clean(); // for debug
      statementList = null;
    }
//    myScopeRules = null;
  }

  public BinItemReference createReference() {
    return new BinInitializerReference(this);
  }

  private static final String memberType = "initializer";

//  private ScopeRules myScopeRules;

  private BinStatementList statementList;
  private boolean statementListEnsured = false;

  private boolean deprecated;
}
