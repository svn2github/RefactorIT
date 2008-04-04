/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts.resolution;


import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;

import java.util.HashMap;
import java.util.List;


/**
 *
 * @author vadim
 */
public abstract class ConflictResolution {
  private boolean isResolved;
  private HashMap imports = new HashMap();

  public ConflictResolution() {
    this(null);
  }

  public ConflictResolution(HashMap imports) {
    this.imports = imports;
  }

  public abstract String getDescription();

  public abstract Editor[] getEditors(ConflictResolver resolver);

  public abstract void runResolution(ConflictResolver resolver);

  public abstract List getDownMembers();

  public boolean isResolved() {
    return isResolved;
  }

  protected void setIsResolved(boolean value) {
    isResolved = value;
  }

  public HashMap getImports() {
    return imports;
  }

  public String toString() {
    return "ConflictResolution";
  }
}
