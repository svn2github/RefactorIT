/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 *
 *
 * @author Igor Malinin
 */
public class ExplanationAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.explanationaction";
  private String helpTopicId;

  public ExplanationAction(String helpTopicId) {
    this.helpTopicId = helpTopicId;
  }

  public boolean run(RefactorItContext context, Object object) {
    HelpViewer.displayTopic(context, this.helpTopicId);
    return false;
  }

  public boolean isReadonly() {
    return true;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Explain this audit";
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  /**
   * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
   */
  public boolean isAvailableForType(Class type) {
    throw new UnsupportedOperationException("method not implemented yet");
    //return false;
  }
}
