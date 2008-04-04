/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Oleg Tsernetsov
 * 
 * Class for multitarget corrective action, that performs any kind of violations'
 * grouping and mostly uses editors to perform corrective action transformations.
 * Implemented for grouping corrective action error handling. 
 */
public abstract class MultiTargetGroupingAction extends CorrectiveAction {
  public static final Logger log = AppRegistry.getLogger(
      MultiTargetGroupingAction.class);
  
  public boolean isMultiTargetsSupported() {
    return true;
  }

  public Set run(TreeRefactorItContext context, List violations) {
    boolean hadErrors = false;
    Set sources = new HashSet(violations.size() * 2);

    TransformationManager manager = new TransformationManager(null);
    try {
      sources.addAll(run(manager, context, violations));
    } catch (Exception e){
      log.error("An exception was caught during corrective action. ", e);
      JErrorDialog error = new JErrorDialog(context, "Error");
      error.setException(e);
      error.show();
      hadErrors = true;
    }
    
    if(manager.isContainsEditors()) {
      manager.setShowPreview(true);
      final RefactoringStatus status = manager.performTransformations();
      if (status.isCancel()){ // prevent reconciling if no sources were changed
        return Collections.EMPTY_SET;
      }
    }
    return sources;
  }

  public abstract String getKey();
  public abstract String getName();
  public abstract Set run(TransformationManager manager, 
      TreeRefactorItContext context, List violations);
}
