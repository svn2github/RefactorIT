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
import net.sf.refactorit.common.util.CollectionUtil;

import java.util.Collection;

/**
 *
 * @author Arseni Grigorjev
 */
public class CollectionReference extends BinItemReference {
  
  private final BinItemReference arrayReference;
  
  public CollectionReference(final Collection collection) {
    arrayReference = BinItemReference.create(collection.toArray());
  }
  
  public Object findItem(Project project) {
    final Object[] o = (Object[]) arrayReference.restore(project);
    return CollectionUtil.toMutableList(o);
  }
  
}
