/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts;

import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.edit.Editor;


/**
 *
 * @author vadim
 */
public abstract class Conflict {
  private ConflictResolution resolution;

  public Conflict() {}

  public abstract void resolve();

  public abstract Editor[] getEditors();

  public abstract ConflictType getType();

  public abstract boolean isResolvable();

  public abstract String getDescription();

  public abstract int getSeverity();

  public boolean isResolved() {
    return ((resolution != null) && resolution.isResolved());
  }

  public void setResolution(ConflictResolution resolution) {
    this.resolution = resolution;
  }

  public ConflictResolution getResolution() {
    return resolution;
  }

  public boolean isObsolete() {
    return false;
  }

  public String toString() {
    return getType().toString() + " " + Integer.toHexString(hashCode());
  }

}
