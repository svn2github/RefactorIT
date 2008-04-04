/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.Project;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinParameterReference extends BinItemReference {
  
  protected BinItemReference methodReference;
  protected int place;
  
  public BinParameterReference(final BinParameter param) {
    BinMethod method = param.getMethod();
    methodReference = method.createReference();
    BinParameter[] params = method.getParameters();
    place = 0;
    for (; place < params.length; ++place) {
      if (param == params[place]) {
        break;
      }
    }
  }
  
  public Object findItem(Project project) {
    BinMethod method = (BinMethod) methodReference.restore(project);
    return method.getParameters()[place];
  }

  public String toString() {
    return super.toString() + ", place=" + place + ", {" + methodReference + "}";
  }
}
