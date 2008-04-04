/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;

/**
 * @author Anton Safonov
 */
public class ResultFilter {
  private Object target;

  public ResultFilter(Object target) {
    this.target = target;
  }

  public Object getTarget() {
    return this.target;
  }
}
