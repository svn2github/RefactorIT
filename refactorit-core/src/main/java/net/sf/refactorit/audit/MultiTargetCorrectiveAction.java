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
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Igor Malinin
 */
public abstract class MultiTargetCorrectiveAction extends CorrectiveAction {
  public static final Logger log = AppRegistry.getLogger(
      MultiTargetCorrectiveAction.class);
  
  public boolean isMultiTargetsSupported() {
    return true;
  }

  public Set run(TreeRefactorItContext context, List violations) {
    boolean hadErrors = false;
    Set sources = new HashSet(violations.size() * 2);

    TransformationManager manager = new TransformationManager(null);

    for (Iterator i = violations.iterator(); i.hasNext(); ) {
      RuleViolation violation = (RuleViolation) i.next();
      try {
        sources.addAll(process(context, manager, violation));
      } catch (Exception e){
        log.error("An exception was caught during corrective action. ", e);
        JErrorDialog error = new JErrorDialog(context, "Error");
        error.setException(e);
        error.show();
        hadErrors = true;
      }
    }
    
    /*if (hadErrors){
      RitDialog.showMessageDialog(context, "Sorry, but due to technical "
          + "reasons some of corrective actions were not applied to selected"
          + " violations. Please report to (support@refactorit.com).",
          "Errors occured", JOptionPane.ERROR_MESSAGE);
    }*/

    manager.setShowPreview(true);
    final RefactoringStatus status = manager.performTransformations();
    if (status.isCancel()){ // prevent reconciling if no sources were changed
      return Collections.EMPTY_SET;
    }

    return sources;
  }

  protected abstract Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation);
}
