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
import net.sf.refactorit.refactorings.conflicts.resolution.MethodInheritanceResolution;
import net.sf.refactorit.source.format.BinFormatter;


/**
 * @author Tonis Vaga
 */
public class OverriddenMethodConflict extends MethodInheritanceConflict {
  public OverriddenMethodConflict(ConflictResolver resolver, BinMethod method) {
    super(resolver, method);
    setResolution(new MethodInheritanceResolution(method));
  }

  public String getDescription() {
    return "Method " + BinFormatter.format(method) + " is overridden!";
  }

  public ConflictType getType() {
    return ConflictType.OVERRIDEN;
  }

}
