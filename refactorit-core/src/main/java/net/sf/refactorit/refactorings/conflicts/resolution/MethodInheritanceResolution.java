/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts.resolution;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;



/**
 *
 *
 * @author Tonis Vaga
 */
public class MethodInheritanceResolution extends MoveMemberResolution {
  protected BinMethod method;

  public MethodInheritanceResolution(final BinMethod method) {
    super(method, CollectionUtil.singletonArrayList(method));

    this.method = method;
  }

  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
  }

  public boolean canResolve(ConflictResolver resolver) {
    return true;
  }
}
