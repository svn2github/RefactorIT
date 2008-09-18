/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;


/**
 * EclipseActionRepository
 * 
 * @author Tõnis Vaga
 */
public class EclipseActionRepository extends ActionRepository {
  public EclipseActionRepository() {
    init();
  }

  protected Object createPlatformAction(RefactorItAction action) {
    return action;
  }

  protected Object createPlatformIDEAction(IdeAction action) {
    return action;
  }

  public Object getIdeSpecificAction(String key, RunContext rContext) {
    return getAction(key);
  }
}
