/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.OptionsAction;

import org.apache.log4j.Logger;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class NbOptionsAction extends OptionsAction {
  private static final Logger log = Logger.getLogger(NbOptionsAction.class);
  
  public NbOptionsAction() {
  }

  public boolean run(IdeWindowContext context) {
    try {
      RefactorItActions.importFormatterEngineDefaults();
    } catch (RuntimeException e) {
      log.warn(e.getMessage(), e);
    }

    return super.run(context);
  }

}
