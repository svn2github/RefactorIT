/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import java.util.Arrays;


public class RunContext implements RunContextConstants, Cloneable {
  private Class[] itemClasses;
  /** {@link RunContextConstants} XXX_CONTEXT */
  private int contextType;
  private boolean checkMultiTarget = false;

  public RunContext(int type, Class targetClass, boolean checkMultiTarget) {
    this(type,
        targetClass != null ? new Class[] {targetClass}
        : (Class[])null,
        checkMultiTarget);
  }

  public RunContext(int contextType, Class[] itemClasses,
      boolean checkMultiTarget) {
    this.itemClasses = itemClasses;
    this.contextType = contextType;
    this.checkMultiTarget = checkMultiTarget;
  }

  public String toString() {
    String clsStr = "null";

    if (itemClasses != null) {
      clsStr = Arrays.asList(itemClasses).toString();
    }

    return getClass().getName() + " binClasses:" + clsStr
        + ", type: " + contextType;
  }

  public Object clone() {
    try {
      final RunContext newContext = (RunContext)super.clone();
      newContext.itemClasses = this.itemClasses;
      return newContext;
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  public Class[] getItemClasses() {
    return this.itemClasses;
  }

  public void setItemClasses(final Class[] classes) {
    this.itemClasses = classes;
  }

  public int getContextType() {
    return this.contextType;
  }

  public boolean isCheckMultiTarget() {
    return this.checkMultiTarget;
  }
}
