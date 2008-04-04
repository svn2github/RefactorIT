/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;



/**
 * @author Tonis Vaga
 */
public class NBActionRepository extends ActionRepository {
  public NBActionRepository() {
    init();
  }

  protected Object createPlatformIDEAction(final IdeAction action) {
    IdeAction ideAction = action;

    if (action.getKey().equals(OptionsAction.KEY)) {
      ideAction = new NbOptionsAction();
    } else if (action.getKey().equals(NbProjectOptionsAction.KEY)) {
      ideAction = new NbProjectOptionsAction();
    }
    return new NBAction(ideAction);
    //
//   return action;
  }

  protected Object createPlatformAction(RefactorItAction action) {
    return action;
  }

  public Object getIdeSpecificAction(String key, RunContext rContext) {
    Object result = super.getAction(key);
    if (!(rContext instanceof NBRunContext)) {
      return null;
    }

    if (result instanceof RefactorItAction) {
      RefactorItAction action = (RefactorItAction) result;

      Assert.must(key.equals(action.getKey()),
          "Returned action key not same :" + key + " vs " + action.getKey());

      NBRunContext context = (NBRunContext) rContext;

      NBRefactorItAction nbAction = (NBRefactorItAction) context.getAction();
      NBRefactorItAction newAction;
      if (nbAction != null) {
        newAction = (NBRefactorItAction) nbAction.clone();
      } else {
        // hack, doesn't work?
        //newAction=new NBRefactorItAction();
        return null;
      }
      newAction.setAction(action);
      return newAction;
    }

    return result;
  }
}
