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
public interface TransformationActuator {

  /** Is called by TransformationManager when ProjectView is changed */
  void notifyViewUpdated();

  /** Is called by TransformationManager when new conflicts were found */
  void notifyConflicts();

  /** @return list of final editors, that should be applied to source. */
  TransformationList performChange();

  TransformationManager getTransformationManager();

}
