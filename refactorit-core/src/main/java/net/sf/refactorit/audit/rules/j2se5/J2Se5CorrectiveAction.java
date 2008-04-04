/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.JOptionPane;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author  Arseni Grigorjev
 */
public abstract class J2Se5CorrectiveAction extends MultiTargetCorrectiveAction {
  
  public Set run(TreeRefactorItContext context, List violations) {
    int mode = FastJavaLexer.getActualJvmMode();
    if (!isTestRun() && mode != FastJavaLexer.JVM_50) {
      int userSelection = RitDialog.showConfirmDialog(context,
          "You are about to apply corrective action, which will add J2SE5 "
          + "constructs into your code.\n\n Would you like RefactorIT switch" +
          "into J2SE5 support mode?", "RefactorIT: switch to J2SE5 mode?",
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
      switch (userSelection){
        case JOptionPane.CANCEL_OPTION:
          // abort action
          return Collections.EMPTY_SET;
        case JOptionPane.YES_OPTION:
          context.getProject().getOptions().setJvmMode(FastJavaLexer.JVM_50);
          break;
        case JOptionPane.NO_OPTION:
          // change nothing, proceed with action
          break;
        default:
          // hmm? abort action
          return Collections.EMPTY_SET;
      }
    }
    return super.run(context, violations);
  }

  protected abstract Set process(TreeRefactorItContext context,
      TransformationManager manager, RuleViolation violation);
  
  public abstract boolean isMultiTargetsSupported();
    
  public abstract String getName();

  public abstract String getKey();
}
