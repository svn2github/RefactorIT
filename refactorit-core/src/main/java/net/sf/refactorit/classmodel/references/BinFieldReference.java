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
import net.sf.refactorit.classmodel.Project;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinFieldReference extends BinItemReference {

  private BinItemReference owner;
  private String name;

  public BinFieldReference(final BinMember field) {
    name = field.getName();
    owner = field.getOwner().getBinCIType().createReference();
  }

  public Object findItem(Project project) {
    final BinCIType ownerType = (BinCIType) owner.restore(project);
    return ownerType.getDeclaredField(name);
  }
}
