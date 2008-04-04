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
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;


/**
 *
 *
 * @author Tonis Vaga
 */
public class OverridesAbstractMethodResolution extends
    OverridesMethodResolution {
  public OverridesAbstractMethodResolution(final BinMethod method) {
    super(method);
  }

  public void runResolution(ConflictResolver resolver) {
    if (resolver.isTargetSuperclass(method)) {
      setIsResolved(true);
//      String msg = "Do you really want to move overridden method " +
//          BinFormatter.format(method) + "?\n" +
//          "It can change the functionality of your program!\n";
//
//      if (askIfAcceptChangedFunctionality(msg)) {
//        setIsResolved(true);
//      }
    } else {
//      String msg = "Method " + BinFormatter.format(method) +
//          " overrides abstract method\n and can only be moved to super class";
//      DialogManager.getInstance().showCustomError(
//          DialogManager.getDialogParent(),
//          msg);
      setIsResolved(false);
    }
  }

  public boolean canResolve(ConflictResolver resolver) {
    return resolver.isTargetSuperclass(method);
  }
}
