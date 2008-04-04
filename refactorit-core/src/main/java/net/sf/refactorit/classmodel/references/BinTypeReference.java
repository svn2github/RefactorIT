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
 * @author Arseni Grigorjev
 */
public class BinTypeReference extends BinItemReference {

  private final String qName;

  public BinTypeReference(final BinType binType) {
    qName = binType.getQualifiedName();
  }

  public Object findItem(Project project){
    final BinTypeRef ref = project.getTypeRefForName(qName);
    return ref.getBinType();
  }
  
  public String toString(){
    return ClassUtil.getShortClassName(this) + "(" + qName + ")";
  }
}
