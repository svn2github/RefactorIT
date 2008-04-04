/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.Project;

/**
 *
 * @author Arseni Grigorjev
 */
public class ArrayReference extends BinItemReference {
  
  private final BinItemReference[] references;
  
  public ArrayReference(final Object[] array) {
    references = new BinItemReference[array.length];
    for (int i = 0; i < array.length; i++){
      references[i] = BinItemReference.create(array[i]);
    }
  }
  
  public Object findItem(Project project) {
    Object[] array = new Object[references.length];
    for (int i = 0; i < references.length; i++){
      array[i] = references[i].restore(project);
      if (array[i] == null){
        log.debug("" + this + " was not restored properly. Item " + i
            + " is null after restore().");
      }
    }
    return array;
  }
  
}
