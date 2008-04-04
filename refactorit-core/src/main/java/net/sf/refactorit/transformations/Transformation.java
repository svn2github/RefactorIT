/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.EditorManager;


/**
 * Public interface for Transformations. Transformations work with
 * ClassModel and delegate source editing further, to the editors.
 *
 * @author Jevgeni Holodkov
 * @author Anton Safonov
 */
public interface Transformation {
  public RefactoringStatus apply(EditorManager e);
}
