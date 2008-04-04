/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * Superclass for all refactorings.
 *
 * @author Anton Safonov
 */
public abstract class AbstractRefactoring implements Refactoring {
  private final RefactorItContext context;
  private final String name;

  private RefactoringStatus status;
  
  private TransformationManager transformationManager = null;

  public AbstractRefactoring(String name, RefactorItContext context){
    this.name = name;
    this.context = context;

    if (Assert.enabled) {
      Assert.must(name != null, "Name shouldn't be null");
      Assert.must(context != null, "Context shouldn't be null");
    }    
  }

  public String toString() {
    return getName();
  }

  public final RefactorItContext getContext() {
    return this.context;
  }

  public final Project getProject() {
    if (this.context == null) {
      return null;
    }

    return this.context.getProject();
  }

  public String getName() {
    return this.name;
  }

  /**
   * @see Refactoring#checkPreconditions
   */
  public abstract RefactoringStatus checkPreconditions();

  /**
   * @see Refactoring#checkUserInput
   */
  public abstract RefactoringStatus checkUserInput();

  /**
   * @see Refactoring#performChange
   */
  public abstract TransformationList performChange();

  /**
   * @see Refactoring#checkAndExecute
   */
  public final TransformationList checkAndExecute() {
    TransformationList transList = new TransformationList();

    transList.merge(checkPreconditions());
    if (transList.getStatus().isErrorOrFatal()
        || transList.getStatus().isCancel()) {
      return transList;
    }

    transList.merge(checkUserInput());
    if (transList.getStatus().isErrorOrFatal()
        || transList.getStatus().isCancel()) {
      return transList;
    }

    transList.merge(performChange());

    return transList;
  }

  /**
   * @param las list of
   * {@link net.sf.refactorit.classmodel.LocationAware LocationAware}'s
   * to check if possible to edit them
   * @return <code>true</code> when all the places are not guarded
   */
  public static boolean isAllChangeable(final List las) {
    for (int i = 0, max = las.size(); i < max; i++) {
      final LocationAware la = (LocationAware) las.get(i);
      final CompilationUnit source = la.getCompilationUnit();
      if (source.isWithinGuardedBlocks(la.getStartLine(),
          la.getStartColumn())) {
        return false;
      }
    }

    return true;
  }

  /**
   * @param map of source file to list of asts in this source file
   */
  public static boolean isAllChangeable(final MultiValueMap map) {
    Set sources = map.keySet();
    Iterator i = sources.iterator();
    while (i.hasNext()) {
      final CompilationUnit source = (CompilationUnit) i.next();
      final List asts = map.get(source);
      if (!isAllChangeable(source, asts)) {
        return false;
      }
    }

    return true;
  }

  /**
   * @param source source file to check ASTs in
   * @param asts list of
   * {@link net.sf.refactorit.parser.ASTImpl ASTImpl}'s
   * to check if possible to edit them
   */
  public static boolean isAllChangeable(final CompilationUnit source, final List asts) {
    for (int i = 0, max = asts.size(); i < max; i++) {
      final ASTImpl ast = (ASTImpl) asts.get(i);
      if (source.isWithinGuardedBlocks(ast.getStartLine(),
          ast.getStartColumn())) {
        return false;
      }
    }

    return true;
  }

  public final boolean isUsingDefaultChangesPreview() {
    String result = GlobalOptions.getOption("preview." + getKey(), "true");
    return result.equalsIgnoreCase("true");
  }

  public final RefactoringStatus getStatus() {
    if (this.status == null) {
      this.status = new RefactoringStatus();
    }

    return this.status;
  }

  public String getDescription() {
    return getName();
  }

  public RefactoringStatus apply() {
    return getTransformationManager().collectAndPerformTransformations();
  }
  

  public TransformationManager getTransformationManager() {
    if (transformationManager == null){
      transformationManager = new TransformationManager(this);
    }
    this.transformationManager.setProject(context.getProject());
    return this.transformationManager;
  }

  public void setTransformationManager(final TransformationManager transformationManager) {
    this.transformationManager = transformationManager;
    transformationManager.registerTransformationActuator(this);
  }

  public void notifyViewUpdated() {
    
     // FIXME 
    throw new java.lang.UnsupportedOperationException( "method notifyViewUpdated not implemented yet");
  }

  public void notifyConflicts() {
    
     // FIXME 
    throw new java.lang.UnsupportedOperationException( "method notifyConflicts not implemented yet");
  }
}
