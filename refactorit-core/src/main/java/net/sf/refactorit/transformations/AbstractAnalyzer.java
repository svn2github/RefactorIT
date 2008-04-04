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
 *
 * @author  Arseni Grigorjev
 */
public abstract class AbstractAnalyzer implements TransformationActuator {
  
  private TransformationManager transformationManager;

  public AbstractAnalyzer(TransformationManager transformationManager) {
    this.transformationManager = transformationManager;
  }
  
  public TransformationList performChange(){
    throw new UnsupportedOperationException("This method is not implemented.");
  }
  
  public TransformationManager getTransformationManager(){
    return this.transformationManager;
  }
}
