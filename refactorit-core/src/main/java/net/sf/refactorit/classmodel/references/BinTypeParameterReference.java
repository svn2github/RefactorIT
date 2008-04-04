/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeParameterManager;
import net.sf.refactorit.classmodel.Project;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinTypeParameterReference extends BinItemReference {
  
  protected final BinItemReference ownerReference;
  protected final String qName;

  public BinTypeParameterReference(final BinType binType) {
    BinMember parentMember = binType.getParentMember();
    if (parentMember == null || parentMember instanceof BinCIType) {
      ownerReference = binType.getOwner().getBinCIType().createReference();
    } else if (parentMember instanceof BinMethod) {
      ownerReference = parentMember.createReference();
    } else {
      ownerReference = null;
      log.warn("Unexpected parent member for type parameter: " + parentMember
          + " (bad BinItemReference will be created)");
    }
    qName = binType.getName();
  }
  
  public Object findItem(Project project){
    BinTypeParameterManager owner = (BinTypeParameterManager) ownerReference
        .restore(project);
    return owner.getTypeParameter(qName).getBinType();
  }
}
