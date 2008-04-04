/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitableUtil;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.RefactoringStatus;

import java.util.List;


/** @author  RISTO A */
public class DependencyAnalyzer {
  RefactoringStatus status = new RefactoringStatus();

  public void checkUsedItemsAvailableOnClassLevel(final BinLocalVariable var) {
    if (var.getExpression() == null) {
      return;
    }

    var.getExpression().accept(new BinItemVisitor() {
      public void visit(BinVariableUseExpression x) {
        if (BinItemVisitableUtil.contains(var.getOwner().getBinCIType(),
            x.getVariable())) {
          status.addEntry("Variable declaration uses another local variable: " +
              x.getVariable().getName(), RefactoringStatus.ERROR);
        }

        super.visit(x);
      }
    });

    if (usesLocalClasses(var)) {
      status.addEntry("Variable uses a local class", RefactoringStatus.ERROR);
    }
  }

  public RefactoringStatus getStatus() {
    return status;
  }

  public boolean usesLocalClasses(SourceConstruct c) {
    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, (BinItem) c);
    List uses = supervisor.getInvocationsFor(c);

    for (int i = 0; i < uses.size(); i++) {
      BinType type = ((InvocationData) uses.get(i)).getWhatType().getBinType();
      if (type.isLocal() && type.getOwner().equals(c.getOwner())) {
        return true;
      }
    }

    return false;
  }
}
