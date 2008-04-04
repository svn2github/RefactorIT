/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 *
 * FIXME: class not actually implemented
 */

public class StandaloneActionRepository extends ActionRepository {
  public StandaloneActionRepository() {
    init();
  }

  protected Object createPlatformIDEAction(IdeAction action) {
    return action;
  }

  protected Object createPlatformAction(RefactorItAction action) {
    return action;
  }

  public Object getIdeSpecificAction(String key, RunContext rContext) {

    if (!(rContext instanceof StandaloneRunContext)) {
      Assert.must(false, "wrong RunContext");
      return null;
    }
    final Object action = getAction(key);

    StandaloneRunContext sCtx = (StandaloneRunContext) rContext;

    // we create action with context at runtime
    if (action instanceof RefactorItAction) {
      return AbstractStandaloneAction.create(
          (RefactorItAction) action, sCtx.getTargetItem(), sCtx.getClickPoint());
    }
    // StandaloneIdeAction
    return action;
  }

}
