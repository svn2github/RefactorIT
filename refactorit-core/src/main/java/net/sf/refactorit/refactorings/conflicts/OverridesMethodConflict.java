/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.conflicts.resolution.MethodInheritanceResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.OverridesAbstractMethodResolution;
import net.sf.refactorit.refactorings.conflicts.resolution.OverridesMethodResolution;
import net.sf.refactorit.source.format.BinFormatter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class OverridesMethodConflict extends MethodInheritanceConflict {
  boolean implementsAbstract;
  protected ConflictResolver resolver;

//  public OverridesMethodConflict(ConflictResolver resolver, BinMethod method) {
//    this(resolver,method,false);
//  }

  public OverridesMethodConflict(ConflictResolver resolver, BinMethod method,
      boolean implementsAbstract) {
    super(resolver, method);
    this.resolver = resolver;
    if (implementsAbstract) {
      this.setResolution(new OverridesAbstractMethodResolution(method));
    } else {
      setResolution(new OverridesMethodResolution(method));
    }

    this.implementsAbstract = implementsAbstract;
  }

  public boolean isResolvable() {
    Assert.must(getResolution() instanceof MethodInheritanceResolution);
    MethodInheritanceResolution resolution = (MethodInheritanceResolution)
        getResolution();
    return resolution.canResolve(resolver);
  }

  public String getDescription() {
    final String msg;
    if (isResolvable()) {
      msg = "Method " + BinFormatter.format(method)
          + " overrides one or more methods";
    } else {
      msg = "Cannot move! Method " + BinFormatter.format(method) +
          " implements abstract method and can be moved only to superclass";
    }
    return msg;
  }

  public ConflictType getType() {
    return ConflictType.OVERRIDES;
  }

}
