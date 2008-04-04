/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.hierarchy;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.netbeans.common.NBContext;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;


/**
 * Shows dockable window with hierarchy view
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class HierarchyViewAction extends CallableSystemAction {
  /**
   * @see org.openide.util.actions.CallableSystemAction#performAction()
   */
  public void performAction() {
    HierarchyView view = HierarchyView.create();
    Mode ourMode = WindowManager.getDefault()
        .getCurrentWorkspace().findMode(view);
    if (ourMode == null) {
      // TC not docked into any mode so dock it somewhere
      Mode explorerMode = WindowManager.getDefault()
          .getCurrentWorkspace().findMode("explorer");
      if (explorerMode != null) {
        explorerMode.dockInto(view);
      } else {
        Mode outputMode = WindowManager.getDefault()
            .getCurrentWorkspace().findMode("output");
        if (outputMode != null) {
          outputMode.dockInto(view);
        }
      }
    }
    view.open();
    NBContext.requestActive(view);
    view.requestVisible();
  }

  public boolean asynchronous() {
    return false;
  }

  /**
   * @see org.openide.util.actions.SystemAction#getName()
   */
  public String getName() {
    return "Class Details View";
  }

  /**
   * @see org.openide.util.actions.SystemAction#getHelpCtx()
   */
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  protected String iconResource() {
    return StringUtil.replace(net.sf.refactorit.netbeans.common.NBIcons.class.
        getPackage().getName(), '.', '/')
        + "/images/hierarchy.gif";
  }

}
