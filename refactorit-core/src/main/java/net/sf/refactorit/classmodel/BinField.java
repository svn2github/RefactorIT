/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.references.BinFieldReference;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.loader.CancelSupport;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinFieldFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;

import java.util.HashMap;
import java.util.List;


/**
 * Defines class member variable.
 */
// FIXME: not really a scope but needed to have something for BinMethodBodyContext - refactoring needed?
public class BinField extends BinVariable implements Scope {

  public static final BinField[] NO_FIELDS = new BinField[0];

  public BinField(String name, BinTypeRef typeRef, int modifiers,
      boolean isWithoutExpression) {
    super(name, typeRef, modifiers);
    if (isWithoutExpression) {
      setExpression(null);
    }

    if (ProjectLoader.checkIntegrityAfterLoad) {
      ProjectLoader.registerCreatedItem(this);
    }
  }

  public static BinField createByPrototype(String name, BinTypeRef typeRef,
      int modifiers, boolean isWithoutExpression, BinTypeRef forRef) {
    BinField field = forRef.getProject().getProjectLoader()
        .getPrototypeManager().findField(forRef, name);
    if (field == null) {
      field = new BinField(name, typeRef, modifiers, isWithoutExpression);
      field.setOwner(forRef);
    } else {
      //System.err.println("Found prototype for : " + name);
      field.reInit(typeRef, modifiers, isWithoutExpression, forRef);
    }

    return field;
  }

  public final boolean isLocalVariable() {
    return false;
  }

  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Gets string represenation of this field. Follows the contract of
   * {@link java.lang.reflect.Field#toString}.
   *
   * @return string represenation.
   */
  public final String toString() {
    BinModifierFormatter modifierFormatter = new BinModifierFormatter(getModifiers());
    modifierFormatter.needsPostfix(true);
    String str = modifierFormatter.print();
    final StringBuffer result =
        new StringBuffer(str);

    result.append(super.toString());

    //result.append(" " + Integer.toHexString(hashCode())); // hashCode() makes tests fail differently each time

    return result.toString();
  }

  public final Project getProject() {
    return getOwner().getProject();
  }

  protected void ensureExpression() {
    if (!getOwner().getBinCIType().isLocal()) {
      CancelSupport.checkThreadInterrupted();
    }

    if (this.isExpressionEnsured()) {
      return;
    }

    try {
      BodyContext context = new BodyContext(
          getOwner().getBinCIType().getCompilationUnit()/*, this*/);

      getProject().getProjectLoader().getErrorCollector().startRecoverableUserErrorSection();
      context.startType(getOwner());
      try {
        callBodyLoader(context);
      } finally {
        context.endType();
        getProject().getProjectLoader().getErrorCollector().endRecoverableUserErrorSection();
      }
    } catch (SourceParsingException e) {
//      AppRegistry.getExceptionLogger().warn(e, this);
      if (!e.isUserFriendlyErrorReported()) {
        getProject().getProjectLoader().getErrorCollector()
            .addUserFriendlyError(
                new UserFriendlyError(e.getMessage(), this.getCompilationUnit(),
                this.getStartLine(), this.getStartColumn()));
      }
    } finally {
      this.setExpressionEnsured(true);
      BinParentFinder.findParentsFor(this);
    }
  }

  protected void callBodyLoader(final BodyContext context)
      throws SourceParsingException {
    getProject().getProjectLoader().getMethodBodyLoader()
        .buildExpressionForField(this, context);
  }

  public final boolean isInvokedVia(BinTypeRef typeRef) {

    // FIXME: use AbstractRefactoring methods instead
    List invocations = Finder.getInvocations(
        getProject(), this, new BinVariableSearchFilter(true, true, true, false, false));
    for (int x = 0; x < invocations.size(); x++) {
      BinFieldInvocationExpression invocation = (BinFieldInvocationExpression)
          ((InvocationData) invocations.get(x)).getInConstruct();
      if (invocation.getInvokedOn().isDerivedFrom(typeRef)) {
        return true;
      }
    }

    return false;
  }

  public final void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public final boolean contains(Scope other) {
    if (other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else {
      return false;
    }
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  public final boolean sameSignature(BinField fieldToCheck) {
    return this.getName().equals(fieldToCheck.getName());
  }

  public final void setParent(BinItemVisitable parent) {
    super.setParent(parent);
  }

  public BinItemFormatter getFormatter() {
    return new BinFieldFormatter(this);
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

  public final void markRebuilding() {
    this.rebuilding = true;
  }

  public final boolean isRebuilding() {
    return this.rebuilding;
  }

  public BinItemReference createReference() {
    return new BinFieldReference(this);
  }

  private boolean rebuilding = false;

  private static final String memberType = "field";

//  private ScopeRules myScopeRules;

  private boolean deprecated;

  public boolean isAnnotationField() {
    return false;
  }
}
