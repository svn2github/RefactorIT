/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.ide.Browser;

import javax.swing.Icon;

import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.ui.module.BackAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.ui.module.StandaloneBrowserAction;


/**
 *
 *
 * @author Tonis Vaga
 */
public class JBActionRepository extends ActionRepository {
  public JBActionRepository() {
    init();
  }

  public Object createPlatformAction(RefactorItAction action) {
    return new JBRefactoritAction(action, getIcon(action.getKey()));
  }

  public Object createPlatformIDEAction(final IdeAction action) {
    Object result = null;

    Icon icon = getIcon(action.getKey());

    if (action.getKey().equals(StandaloneBrowserAction.KEY)) {
      result = new JBIdeAction(action, icon) {
        public void actionPerformed(Browser browser) {
          RefactorItTool.startRefactorITBrowser(browser);
        }
      };
    } else if (action.getKey().equals(BackAction.KEY)) {
      result = new net.sf.refactorit.jbuilder.BackAction(action);
    } else {
//      if (false && action instanceof ShortcutAction) {
//        result = new JBShortcutAction( (ShortcutAction) action, icon) {
//          public void actionPerformed(ActionEvent e) {
//            action.run(IDEController.getInstance().getIDEMainWindow());
//          }
//
//        };
//      } else
      result = new JBIdeAction(action, icon);
    }

    return result;
  }

  public Object getIdeSpecificAction(String key, RunContext rContext) {
    Object action = getAction(key);
//    if ( rContext instanceof JBRunContext ) {
//    	JBRunContext jbCtx=(JBRunContext) rContext;
//      JBAction jbAction = jbCtx.getAction();
//      jbAction.setAction(ModuleManager.getAction(rContext.getItems()[0],key));
//    }
    return action;
  }
}
