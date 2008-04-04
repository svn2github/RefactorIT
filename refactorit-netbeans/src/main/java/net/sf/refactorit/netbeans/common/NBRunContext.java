/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.ui.module.RunContext;


/**
 * @author Tonis Vaga
 */
public class NBRunContext extends RunContext {

  private NBAction action;

  public NBRunContext(int code, BinItem item, ElementInfo[] info,
      boolean checkMultiTarget) {
    super(isJsp(info) ? RunContext.JSP_CONTEXT : RunContext.JAVA_CONTEXT,
        item.getClass(), checkMultiTarget);
  }

  public NBRunContext(int code, Class[] cls, boolean checkMultiTarget) {
    super(code, cls, checkMultiTarget);
  }

  public void setAction(NBAction action) {
    this.action = action;
  }

  public static boolean isJsp(ElementInfo info[]) {

    for (int i = 0; i < info.length; i++) {
      if (info[i].isJsp()) {
        return true;
      }
    }
    return false;
  }

  public NBAction getAction() {
    return this.action;
  }
}
