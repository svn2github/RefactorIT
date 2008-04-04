/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.FindRerunInfoReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.query.structure.FindRequest;

public class FindRerunInfo implements Referable {
  public Object object;
  public FindRequest request;

  public FindRerunInfo(Object object, FindRequest request) {
    this.object = object;
    this.request = request;
  }

  public BinItemReference createReference() {
    return new FindRerunInfoReference(this);
  }
  
}
