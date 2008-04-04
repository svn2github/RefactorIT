/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.transformations.view;

/**
 *  A basic unit of modification for {@link ProjectView}.
 *
 * @author  Arseni Grigorjev
 */
public class Triad {
  private Object object;
  private Object subject;

  /**
   * Describes relation of subject -> object.
   *
   * @param subject 
   * @param object
   */
  public Triad(Object subject, Object object) {
    this.subject = subject;
    this.object = object;
  }

  public Object getObject() {
    return this.object;
  }

  public Object getSubject() {
    return this.subject;
  }
}
