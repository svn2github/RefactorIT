/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.transformations.TransformationActuator;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

/**
 * Establishes stable and clear connection between algorithm itself and
 * invocation/user interface module.<br>
 * Correct order of calls:
 * <OL>
 * <LI>{@link #checkPreconditions()}</LI>
 * <LI>{@link #checkUserInput()}</LI>
 * <LI>{@link #performChange()}</LI>
 * </OL>
 * All of them should in general return <code>null</code> or
 * <code>isOk() == true</code>.
 *
 * @author Anton Safonov
 */
public interface Refactoring extends TransformationActuator {

  RefactoringStatus apply();
  
  /**
   * Checks the preconditions of the refactoring to determine if the refactoring
   * can be started at all.
   * @return never returns <code>null</code>
   */
  RefactoringStatus checkPreconditions();

  /**
   * Checks user entered data for validity.
   * @return never returns <code>null</code>
   */
  RefactoringStatus checkUserInput();

  /**
   * Performs actual refactoring.
   * @return may return <code>null</code>
   */
  TransformationList performChange();
  
  /**
   * Returns the name of this refactoring.
   *
   * @return the refactoring's name
   */
  String getName();

  /**
   * Returns the Description of this refactoring.
   *
   * @return the refactoring's description
   */
  String getDescription();

  /**
   * Runs sequentally {@link #checkPreconditions}, {@link #checkUserInput} and
   * {@link #performChange}. Stops on first ERROR or FATAL status.
   * @return never returns <code>null</code>
   */
  TransformationList checkAndExecute();

  /**
   * @Returns the status of the refactoring work.
   */
  RefactoringStatus getStatus();

  RefactorItContext getContext();

  boolean isUsingDefaultChangesPreview();

  String getKey();
}
