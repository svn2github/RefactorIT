/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations.view.triads;

import net.sf.refactorit.transformations.view.Triad;

/**
 *
 * @author  Arseni Grigorjev
 */
public class RemoveTriad extends Triad {
  
  /** Creates a new instance of RemoveTriad */
  public RemoveTriad(Object removedItem) {
    super(removedItem, null);
  }
  
}
