/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ClassUtil;


/**
 *
 * @author Arseni Grigorjev
 */
public class BinTypeRefReference extends BinItemReference {

  private final BinItemReference aTypeReference;

  public BinTypeRefReference(final BinTypeRef typeRef) {
    aTypeReference = typeRef.getBinType().createReference();
  }

  public Object findItem(Project project) {
    final BinType aType = (BinType) aTypeReference.restore(project);
    try {
      return aType.getTypeRef();
    } catch (NullPointerException e) {
      return null;
    }
  }

  public String toString(){
    return ClassUtil.getShortClassName(this) + "{" + aTypeReference + "}";
  }
}
