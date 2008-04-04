/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations.view.triads;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.view.Triad;

/**
 *
 * @author  Arseni Grigorjev
 */
public class PackageTriad extends Triad {
  
  public PackageTriad(SourceHolder source, BinPackage newPackage) {
    super(source, newPackage);
  }
  
}
