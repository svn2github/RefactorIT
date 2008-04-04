/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.wherecaught;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.query.CallTreeIndexer;


/**
 * @author Anton Safonov
 */
public class WhereCaughtIndexer extends CallTreeIndexer {

  private BinItem target;

  public WhereCaughtIndexer(final BinItem target) {
    super();
    this.target = target;
  }

  public void visit(BinTryStatement.TryBlock x) {
    final BinItem location = getCurrentLocation();
    addSingleInvocation(location, x, x.getRootAst());
    setCurrentLocation(x);

    super.visit(x);

    setCurrentLocation(location);
  }

  public void visit(BinThrowStatement x) {
    if (this.target == x) {
      addSingleInvocation(getCurrentLocation(), x, x.getRootAst());
    }

    super.visit(x);
  }

  public void visit(BinMethod.Throws x) {
    if (this.target == x) {
      addSingleInvocation(getCurrentLocation(), x, x.getRootAst());
    }

    super.visit(x);
  }
}
