/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


/**
 * Incapsulates the data, what is shared among editors
 */
public final class TransformationObject {
  private Object object;
  
  public TransformationObject() {
  }
  
  public Object get() {
    return object;
  }
  
  public void set(Object object) {
    this.object = object;
  }
}
