/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.resolutiondialog;

import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 *
 * @author vadim
 */
public class ResolutionNode extends BinTreeTableNode {
  private ConflictResolution resolution;

  public ResolutionNode(ConflictResolution resolution) {
    super(resolution.getDescription(), true);

    this.resolution = resolution;
  }

  public ResolutionNode(Object bin, ConflictResolution resolution) {
    super(bin, true);

    this.resolution = resolution;
  }

  public ConflictResolution getResolution() {
    return resolution;
  }
}
