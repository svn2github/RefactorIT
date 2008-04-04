/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.commonIDE.ShortcutAction;


/**
 * Refactoring action.
 *
 * @author Igor Malinin
 * @author Vlad Vislogubov
 * @author Anton Safonov
 */
public interface RefactorItAction extends ShortcutAction {
  /**
   * Determines whether this action can operate with
   * multiple selected objects.
   *
   * @return  true if action can operate with multiple targets.
   */
  boolean isMultiTargetsSupported();

  /**
   * Determines whether this action can operate with
   * other sources than .java (jsp for example).
   *
   * @return  true if action can operate with preprocessed sources.
   */
  boolean isPreprocessedSourcesSupported(BinItem item);

  /**
   * Determines wheter action can operate on concrete class constructed from JSP source. *
   */
  boolean isPreprocessedSourcesSupported(Class cl);

  /**
   * Module execution.
   *
   * It executes the RefactorItAction on target BinXXX object.
   * If you want to make a clean, first time execution of this RefactorItAction,
   * then you MUST provide a new Context() object into run(...) method,
   * otherwise, if you just want to ReRun the action on specified target,
   * then you provide the old context object into run(...) method it was
   * executed previously.
   *
   * Some module actions check the context object for old data they have
   * put into it. For example Metrics should check whether it should display
   * a Dialog full of metric selections to the user or not, it depends whether
   * it finds old data from Context or not.
   *
   * @param context context on which refactoring was run,
   *                includes: project, point, state
   * @param object Bin object to operate
   * @return false if nothing changed, true otherwise. Return value should be
   * used to decide if the environment must be updated.
   */

  boolean run(RefactorItContext context, Object object);
/*
  /**
 * Transformation list filling method.
 * It DOES NOT execute the RefactorItAction, byt fills the list, given
 * as a parameter with appropriate refactoring transformations.
 *
 * @param context context context on which refactoring was run,
 *                includes: project, point, state
 * @param object object Bin object to operate
 * @param transList Transformation list, that will be updated
 * 					 with selected refactoring transformations
 * @return false if nothing changed, true otherwise. Return value should be
 * used to decide if the environment must be updated.
 *
  boolean run(RefactorItContext context, Object object, TransformationList transList);
  */

  /**
   * Rebuilds the project, reloads current source file and shows errors
   * arosen during or after refactoring.<br>
   * Usually called after "write" refactorings.
   *
   * @param parent parent to show the dialogs
   * @param context context on which refactoring was executed
   */

  void updateEnvironment(RefactorItContext context);

  /** If the refactoring called was of "search" type, e.g. Where Used,
   * Not Used etc., then we need to get focus on results pane.
   *
   * @param parent parent to show the dialogs
   * @param context context on which refactoring was executed
   */
  void raiseResultsPane(RefactorItContext context);

  boolean isPreprocessedSourcesSupported(Class[] cl);

  /**
   * @return true if refactorings does not perform changes, false otherwise
   */
  boolean isReadonly();

  /**
   *
   * @param type bin class
   * @return true if action is applicable for  type
   */
  public boolean isAvailableForType(Class type);

  /**
   * @param target array of bin objects
   * @return true if action is available for targets
   */
  boolean isAvailableForTarget(Object[] target);
}
