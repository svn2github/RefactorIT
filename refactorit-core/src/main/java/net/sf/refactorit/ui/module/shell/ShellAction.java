/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.shell;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;

import bsh.EvalError;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;



public class ShellAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.ShellAction";
  public static final String NAME = "Shell";

  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(ShellAction.class);

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    return true;
  }

  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  /**
   * Module execution.
   *
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    Assert.must(context != null,
        "Attempt to pass NULL parent into ShellAction.run()");

    Object target = object;

    Map vars = new LinkedHashMap();
    vars.put("ctx", context);
    vars.put("project", context.getProject());
    vars.put("target", target);
    vars.put("object", object);

    showShell(vars);

    return false;
  }

  private static void showShell(Map variables) {
    final ShellFrame frame = new ShellFrame();
    frame.setTitle("Shell");
    try {
      for(Iterator i = variables.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry e = (Map.Entry) i.next();
        frame.getInterpreter().set((String)e.getKey(), e.getValue());
      }

      frame.getInterpreter().println(
          StringUtil.mergeArrayIntoString(
              variables.keySet().toArray(), ", ", " and ") +
          " set");
      frame.getInterpreter().println("target: " + variables.get("target"));
    } catch (EvalError e) {
      throw new ChainableRuntimeException("Failed to create Shell console",
          e);
    }

    frame.show();
  }

  public boolean isAvailableForType(Class type) {
    return true;
  }
}
