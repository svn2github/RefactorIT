/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.transformations.view.ProjectView;
import net.sf.refactorit.transformations.view.ProjectViewImpl;
import net.sf.refactorit.transformations.view.Triad;
import net.sf.refactorit.ui.DialogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Wrapper for editors. editors deal only with Sources, while Transformations
 * work with CompilationUnits. This Manager helps to connect them and edit the
 * source using both of them.
 *
 * @author Jevgeni Holodkov
 * @author Anton Safonov
 */
public final class TransformationManager {

  /** List of refactorings and analyzers communicating on this manager */
  private final List actuators = new ArrayList();
  
  /** Virtual project state */
  private final ProjectViewImpl projectView;

  private final RefactoringStatus status;
  private final EditorManager editor;
  
  private List projects = null;

  public static final Object NULL_LOCATION = new Object();

  public TransformationManager(Refactoring refactoring) {
    boolean showPreview = false;
    String name = null;
    String description = null;

    if (refactoring != null) {
      registerTransformationActuator(refactoring);
      //registerTransformationActuators(createStandartActuatorsSet());
      showPreview = refactoring.isUsingDefaultChangesPreview()
          && IDEController.getInstance().getPlatform() != IDEController.TEST;
      name = refactoring.getName();
      description = refactoring.getDescription();
    }

    projectView = new ProjectViewImpl();
    editor = new EditorManager(showPreview, name, description);
    status = new RefactoringStatus();
  }

  private List createStandartActuatorsSet() {
    return Collections.singletonList(new NewImportManager(this));
  }

  public void setShowPreview(final boolean showPreview){
    editor.setShowPreview(showPreview && IDEController.getInstance()
        .getPlatform() != IDEController.TEST);
  }

  public RefactoringStatus performTransformations() {
    return status.merge(editor.performEdit());
  }
  
  public RefactoringStatus collectAndPerformTransformations(){
    final TransformationList generalTranList = new TransformationList();

    TransformationList changes;
    for (Iterator it = actuators.iterator(); it.hasNext(); ){
      final Object next = it.next();
      changes = ((TransformationActuator) next).performChange();
      if (changes != null){
        add(changes);
      }
    }

    this.add(generalTranList);

    if (!this.status.isErrorOrFatal() && !this.status.isCancel()) {
      //manager.setProgressArea(EDITORS_EDIT);
      performTransformations();
    }

    // FIXME: this is only for Inline Method! or not?
    if (this.status.isErrorOrFatal() && this.status.hasSomethingToShow()) {
      DialogManager.getInstance().showCustomError(
          IDEController.getInstance().createProjectContext(),
          this.status.getAllMessages());
    }

    return this.status;
  }

  public void registerTransformationActuator(TransformationActuator actuator) {
    actuators.add(actuator);
  }
  
  public void registerTransformationActuators(List actuators) {
    this.actuators.addAll(actuators);
  }
  
  // FIXME: fix RenamePackageTransformation(move status returning somewhere else)
  public RefactoringStatus add(Transformation transformation) {
    if (Assert.enabled) {
      Assert.must(transformation != null, "Was given null editor");
    }

//    transformation.apply(editor);
//    return this;
    return transformation.apply(editor);
  }

  public TransformationManager add(TransformationList list) {
    for(Iterator it = list.iterator(); it.hasNext(); ) {
      Object obj = it.next();

      if(obj instanceof Transformation) {
        status.merge( this.add((Transformation)obj));
      } else if(obj instanceof Editor) {
        this.add((Editor)obj);
      }
    }
    status.merge(list.getStatus());
    return this;
  }

  public TransformationManager add(Editor ed) {
    editor.addEditor(ed);
    return this;
  }

  public int getEditorsCount() {
    return editor.getEditorsCount();
  }

  public boolean isContainsEditors() {
    return getEditorsCount() > 0;
  }

  public RefactoringStatus getStatus() {
    return status;
  }

  public ProjectView getProjectView(){
    return projectView;
  }

  public int beginViewUpdateTransaction(TransformationActuator caller){
    return projectView.beginTransaction(caller);
  }
  
  public void endViewUpdateTransaction(TransformationActuator caller){
    projectView.endTransaction();
    notifyActuatorsViewUpdated(caller);
  }
  
  public void updateView(TransformationActuator caller, List triads){
    projectView.add(triads);
  }
  
  public void updateView(TransformationActuator caller, Triad triad){
    projectView.add(triad);
  }
  
  public void notifyActuatorsViewUpdated(TransformationActuator caller){
    for (Iterator it = actuators.iterator(); it.hasNext(); ){
      TransformationActuator actuator = (TransformationActuator) it.next();
      if (caller != actuator){ // do not notify the one who added triads
        actuator.notifyViewUpdated();
      }
    }
  }

  public static BinTypeRef getNullOwner() {
    return null;
  }

  public int getCurrentTransactionId() {
    return projectView.getCurrentTransactionId();
  }

  public void killViewUpdateTransaction(int transactionId) {
    projectView.killTransaction(transactionId);
  }

  public boolean isKilledViewUpdateTransaction(int transactionId) {
    return projectView.isKilledTransaction(transactionId);
  }

  public void disableViewUpdateTransaction(int transactionId) {
    projectView.disableTransaction(transactionId);
  }
  
  public void enableViewUpdateTransaction(int transactionId) {
    projectView.enableTransaction(transactionId);
  }

  public boolean isActiveViewUpdateTransaction(int transactionId) {
    return projectView.isActiveTransaction(transactionId);
  }

  public List getProjects() {
    return projects;
  }

  public void setProject(final Project project) {
    this.projects = Collections.singletonList(project);
  }
  
  public EditorManager getEditorManager(){
    return editor;
  }
}
