/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;



public final class ConstructorIndexer extends TargetIndexer {

  private boolean addDefaultCstor;
  private static final BinExpressionList emptyExpressionList
      = new BinExpressionList(BinParameter.NO_PARAMS);

  public ConstructorIndexer(final ManagingIndexer supervisor,
      final BinConstructor target) {
    super(supervisor, target, target.getOwner().getBinCIType());
  }

  // Constructor-specific actions
  public final void visit(final BinNewExpression expression) {
    final BinExpressionList eList = expression.getExpressionList();
    if (eList != null) {
      BinTypeRef returnType = expression.getReturnType();
      if (matchesTargetConstructor(returnType, eList.getExpressionTypes())) {
        addInvocation(returnType.getNode(), expression);
      }
    } else {
      //net.sf.refactorit.loader.SourceMethodBodyLoader.ASTDebugOn(expression.getRootAst());
      // FIXME: what else?
      // like in new Object[0];
    }
  }

  public final void visit(final BinConstructorInvocationExpression expression) {
    final BinTypeRef invokedByExpression;

    if (expression.isSuper()) {
      final BinTypeRef currentClass = getSupervisor().getCurrentType();

      if (Assert.enabled) {
        Assert.must(currentClass.getSuperclass() != null,
            "Call to super constructor of type which doesn't have superclass: "
            + currentClass.getQualifiedName());
      }

      invokedByExpression = currentClass.getSuperclass();
    } else {
      invokedByExpression = getSupervisor().getCurrentType();
    }
    this.addDefaultCstor = false;

    if (matchesTargetConstructor(invokedByExpression,
        expression.getExpressionList().getExpressionTypes())) {
      addInvocation(expression.getNameAst(), expression);
    }
  }

  private void addInvocation(final ASTImpl ast,
      final SourceConstruct construct) {
    getSupervisor().addInvocation(getTarget(),
        getSupervisor().getCurrentLocation(),
        ast,
        construct);
  }

  public final void visit(final BinConstructor cstor) {
    // FIXME: what if there is local class inside which gets visited and breaks our field?
    // HINT: may be use BinConstructor.hasSuperInvocation??? should be checked for stability...
    this.addDefaultCstor = true;
  }

  public final void leave(final BinConstructor cstor) {
    if (this.addDefaultCstor
        && !((BinMember) getTarget()).getOwner().equals(cstor.getOwner())) {
      BinTypeRef type = cstor.getOwner();
      int superLevel = 0;
      // we want to check owner and its super only!
      while (superLevel < 2 && type != null
          && !type.equals(type.getProject().getObjectRef())) {
        if (matchesTargetConstructor(type, BinTypeRef.NO_TYPEREFS)) {
          // FIXME: hack, created artificially BinConstructorInvocationExpression to get inconstruct[tonis]
          ASTImpl nameNode = cstor.getNameAstOrNull();

          BinConstructorInvocationExpression invocation
              = new BinConstructorInvocationExpression(cstor.getOwner(),
              cstor.getOwner().getSuperclass(),
              emptyExpressionList, true, nameNode);

          invocation.setNameAst(nameNode);
          invocation.setParent(cstor);

          addInvocation(cstor.getNameAstOrNull(), invocation);
          break;
        }
        type = type.getSuperclass();
        superLevel++;
      }
    }
  }

  private boolean matchesTargetConstructor(
      final BinTypeRef type, final BinTypeRef[] signature) {
    if (!isTargetType(type)) { // *Only* for speed
      return false;
    }

    if (Assert.enabled && !(type.getBinType() instanceof BinClass)) {
      Assert.must(false, "type is not BinClass: " + type.getBinType());
    }

    final BinClass cls = (BinClass) type.getBinCIType();
    final BinConstructor invoked = cls.getAccessibleConstructor(cls, signature);

    return ((BinConstructor) getTarget()) == invoked;
  }

  private boolean isTargetType(final BinTypeRef type) {
    if (type.isArray()) {
      return false;
    } else if (!getTypeRef().equals(type)) {
      return false;
    } else {
      return true;
    }
  }
}
