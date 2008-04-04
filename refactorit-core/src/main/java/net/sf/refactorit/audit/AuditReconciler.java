/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.List;
import java.util.Set;


public interface AuditReconciler {
  boolean reconcile(Set sources, TreeRefactorItContext context);
  
  List revalidateViolations(List violations, RefactoringStatus status);
}
