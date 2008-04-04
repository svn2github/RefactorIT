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
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.1 $ $Date: 2004/10/21 09:33:20 $
 */
public class EclipseActionRepository extends ActionRepository {

  /**
   * 
   */
  public EclipseActionRepository() {
    init();
  }
  /**
   * @see net.sf.refactorit.commonIDE.ActionRepository#createPlatformAction(net.sf.refactorit.ui.module.RefactorItAction)
   */
  protected Object createPlatformAction(RefactorItAction action) {
    return action;
  }

  /**
   * @see net.sf.refactorit.commonIDE.ActionRepository#createPlatformIDEAction(net.sf.refactorit.commonIDE.IdeAction)
   */
  protected Object createPlatformIDEAction(IdeAction action) {
    return action;
  }

  /**
   * @see net.sf.refactorit.commonIDE.ActionRepository#getIdeSpecificAction(java.lang.String, net.sf.refactorit.ui.module.RunContext)
   */
  public Object getIdeSpecificAction(String key, RunContext rContext) {
    return getAction(key);
  }

}
