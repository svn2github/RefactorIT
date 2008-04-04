/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.vfs.Source;

import javax.swing.KeyStroke;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ReconcileActionDecorator extends AbstractRefactorItAction {
  private CorrectiveAction action;
  private boolean enabled = true;
  private boolean multitarget;
  private final AuditReconciler reconciler;
  private List violations;

  public ReconcileActionDecorator(
      CorrectiveAction action, List violations, boolean multitarget, 
      AuditReconciler reconciler) {
    this.action = action; 
    this.multitarget = multitarget;
    this.violations = violations;
    this.reconciler = reconciler;
  }

  public ReconcileActionDecorator(
      CorrectiveAction action, List violations, boolean multitarget, 
      AuditReconciler reconciler, boolean enableSingletarget
      ) {
    this(action, violations, multitarget, reconciler);
    this.enabled = multitarget || enableSingletarget;
  }

  public String getKey() {
    return action.getKey();
  }

  public String getName() {
    return multitarget ? action.getMultiTargetName() : action.getName();
  }

  public KeyStroke getKeyStroke() {
    return action.getKeyStroke();
  }

  public boolean isEnabled(){
    return this.enabled;
  }
  
  public boolean isReadonly() {
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return action.isMultiTargetsSupported();
  }

  public boolean run(final RefactorItContext context, Object object) {
    final TreeRefactorItContext treeContext = (TreeRefactorItContext) context;

    boolean updateEnvironment = false;
    
    
    // If user haven`t saved any files before running corrective action,
    // RIT will automatically save all files
    // FIXME: maybe should make it somewhere in RefactoringActionUtils?
    IDEController.getInstance().ensureProjectWithoutParsing();
    
    // If project was changed, but not rebuilded, corrective action can produce
    // a very bad messed up code. Or it can even crash during editors apply
    // phase. That`s why RIT will try to rebuild project, rerun audits, validate
    // target violations and only then run the action.
    boolean dontcare = needsRebuildAndRerunFirst(treeContext);
    if (needsRebuildAndRerunFirst(treeContext)){
      updateEnvironment = true;
      
      HashSet changedCompilationUnits = getChangedSources(treeContext
          .getProject());
      
      reconciler.reconcile(changedCompilationUnits, treeContext);
          
      RefactoringStatus status = new RefactoringStatus();
      violations = reconciler.revalidateViolations(violations, status);
      if (status.hasSomethingToShow()){
          RitDialog.showMessageDialog(context, status.getAllMessages(),
          "RefactorIT notice", status.getJOptionMessageType());
      }
    } 
    
    if (violations != null && violations.size() == 0){
      return updateEnvironment;
    }
    
    Set sources = action.run(treeContext, violations);

    if (sources.remove(null)){ // NPE safety!
      AppRegistry.getLogger(getClass()).warn("Changes sources set for "
          + action.getClass().getName() + " contained null values!",
          new Exception("Here this one was caught"));
    }
    
    return updateEnvironment
        || reconciler.reconcile(sources, treeContext);
  }

  private HashSet getChangedSources(final Project project) {
    HashSet changedSources = new HashSet();
    HashSet changedCompilationUnits = new HashSet(changedSources.size());

    project.getProjectLoader().getRebuildLogic().analyzeChanges();
    changedSources.addAll(project.getProjectLoader().getRebuildLogic()
        .getSourceListToRebuild());

    for (Iterator it = changedSources.iterator(); it.hasNext(); ){
      Source source = (Source) it.next();
      changedCompilationUnits.add(project.getCompilationUnitForName(
          source.getRelativePath()));
    }

    return changedCompilationUnits;
  }
  
  private boolean needsRebuildAndRerunFirst(TreeRefactorItContext treeContext){
    return treeContext.getProject().getProjectLoader()
        .sourcepathOrClasspathHaveChanges();
  }

  /**
   * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
   */
  public boolean isAvailableForType(Class type) {
    // CorrectiveActions do support selections
    if (BinSelection.class.equals(type)){
      return true;
    }
    
    // FIXME
    throw new UnsupportedOperationException("method not implemented yet");
    //return false;
  }
}
