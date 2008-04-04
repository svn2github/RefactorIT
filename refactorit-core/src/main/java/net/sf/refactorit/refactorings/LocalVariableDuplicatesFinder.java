/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.query.AbstractIndexer;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public final class LocalVariableDuplicatesFinder extends AbstractIndexer {
  private final BinMember target;
  private final String nameToSearchFor;
  private final LocationAware targetLocation;
  private final Scope targetScope;

  private final List duplicates = new ArrayList();

  /**
   * @param target used to skip itself on check, can be null when new one
   * is being created
   * @param nameToSearchFor a new name we are searching duplicates for
   * @param targetScope location of var declaration, identifies scope branch
   * to limit search to
   */
  public LocalVariableDuplicatesFinder(BinMember target,
      String nameToSearchFor,
      LocationAware targetLocation) {
    this.target = target;
    this.nameToSearchFor = nameToSearchFor;
    this.targetLocation = targetLocation;
    this.targetScope = ((BinItem)this.targetLocation).getScope();
  }

  public final void visit(BinCIType x) {
    if (x.isLocal()) {
      // clash with local types with the same name
      check(x, (BinItem) x.getParent());
    }

    super.visit(x);
  }

  public final void visit(BinLocalVariable x) {
    check(x, (BinItem) x.getParent());

    super.visit(x);
  }

  public final void visit(BinTryStatement.CatchClause x) {
    check(x.getParameter(), x);
    super.visit(x);
  }

  public final void visit(BinMethod method) {
    checkParams(method);
    super.visit(method);
  }

  public final void visit(BinConstructor method) {
    checkParams(method);
    super.visit(method);
  }

  private void checkParams(BinMethod method) {
    BinParameter[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      check(params[i], method);
    }
  }

  private void check(BinMember var, BinItem declaredAt) {
    if (this.target != var && this.nameToSearchFor.equals(var.getName())) {
      final LocationAware declaredAtScope = (LocationAware) declaredAt.getScope();

      if (this.targetScope == declaredAtScope) {
        CollectionUtil.addNew(this.duplicates, var);
      } else if (getMemberOrParentMember((BinItem)this.targetLocation)
          == getMemberOrParentMember(declaredAt)) {
        if (((LocationAware)this.targetScope).contains(
            declaredAtScope)
            && ((LocationAware) declaredAt).isAfter(this.targetLocation)) {
          CollectionUtil.addNew(this.duplicates, var);
        } else if (declaredAtScope.contains(this.targetLocation)
            && this.targetLocation.isAfter((LocationAware) declaredAt)) {
          CollectionUtil.addNew(this.duplicates, var);
        }
      }
    }
  }

  private BinMember getMemberOrParentMember(BinItem item) {
    if (item instanceof BinMember) {
      return (BinMember) item;
    }

    return item.getParentMember();
  }

  public final List getDuplicates() {
    return this.duplicates;
  }
}
