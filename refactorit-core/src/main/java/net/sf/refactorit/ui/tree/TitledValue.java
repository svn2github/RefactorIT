/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

public final class TitledValue {
  private static final Object NO_REFERENCED_OBJECT = new Object();

  private final String title;
  private final Object referencedObject;

  public TitledValue(String title, Object referencedObject) {
    this.title = title;
    this.referencedObject = referencedObject;
  }

  public TitledValue(String title) {
    this(title, NO_REFERENCED_OBJECT);
  }

  public final String toString() {
    return this.title;
  }

  public final Object getReferencedObject() {
    return this.referencedObject;
  }

  public final boolean hasReferencedObject() {
    return this.referencedObject != NO_REFERENCED_OBJECT;
  }
}
