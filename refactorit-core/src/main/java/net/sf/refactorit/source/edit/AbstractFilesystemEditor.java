/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;


public abstract class AbstractFilesystemEditor extends DefaultEditor
    implements FilesystemEditor {

  private boolean enabled = true;

  public AbstractFilesystemEditor(final SourceHolder target) {
    super(target);
  }

  public RefactoringStatus apply(LineManager manager) {
    manager.addFilesystemEditor(this);
    return null;
  }

  public final void disable() {
    enabled = false;
  }

  public final boolean isEnabled() {
    return enabled;
  }
}
