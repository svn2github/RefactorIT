/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.InvocationData;


public final class SimpleFilter extends SearchFilter {

  public SimpleFilter(final boolean showDuplicates,
      final boolean goToSingleUsage,
      final boolean runWithDefaultSettings) {
    super(showDuplicates, goToSingleUsage, runWithDefaultSettings);
  }

  protected final boolean passesFilter(final InvocationData invocationData,
      final Project project) {
    return true;
  }
}
