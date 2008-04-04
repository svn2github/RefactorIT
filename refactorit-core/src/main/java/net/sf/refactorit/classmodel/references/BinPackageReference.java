/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinPackageReference extends BinItemReference {
  
  protected final String qName;
  
  public BinPackageReference(final BinPackage pack) {
    qName = pack.getQualifiedName();
  }
  
  public Object findItem(Project project) {
    return project.createPackageForName(qName);
  }
}
