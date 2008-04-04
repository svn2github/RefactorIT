/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;
import oracle.ide.IdeAction;
import oracle.ide.addin.Controller;

import javax.swing.KeyStroke;

import java.util.HashMap;


/**
 *
 *
 * @author Tonis Vaga
 */
public class JDevActionRepository extends ActionRepository {
  HashMap jdevToRITActionMap = new HashMap();

  public JDevActionRepository() {
    init();
  }

  static class JDevActionKey {
    private IdeAction action;

    public JDevActionKey(IdeAction action) {
      this.action = action;
    }

    public boolean equals(Object obj) {
      JDevActionKey action2 = (JDevActionKey) obj;

      return this.action.getCommandId() == action2.action.getCommandId();
    }

    public int hashCode() {
      return action.getCommandId();
    }
  };

  public Object getRITActionFromIdeAction(IdeAction action) {
    return jdevToRITActionMap.get(new JDevActionKey(action));
  }

  protected Object createPlatformIDEAction(
      net.sf.refactorit.commonIDE.IdeAction action
      ) {
    int cmdId = AbstractionUtils.getNewCmdId(action.getKey());
    IDEController controller = IDEController.getInstance();

    IdeAction jdevAction = IdeAction.get(cmdId,
        controller.getClass().getName(), // FIXME: looks strange, not sure about this name
        action.getName(), null,
        new Integer(action.getMnemonic()),
        getIcon(action.getKey()), Boolean.FALSE, true);

    AbstractionUtils.addController(jdevAction, (Controller) controller);
    jdevAction.putValue(IdeAction.CATEGORY, RefactorItAddin.REFACTORIT_CATEGORY);
    jdevAction.putValue(JDevMenuBuilder.BINSELECTION_NECCESSITY,
        new Integer(JDevMenuBuilder.BINSELECTION_DONT_CARE));

    jdevToRITActionMap.put(new JDevActionKey(jdevAction), action);

//    int id = Ide.newCmd(action.getKey());
//    IDEController controller = IDEController.getInstance();
//
////    if (! (controller instanceof RefactorItController)) {
////      DebugInfo.trace(
////          "Controller should be instance of JDev RefactorItController!!!");
////    }
//
//    IdeAction jdevAction = IdeAction.get(id,
//        controller.getClass().getName(), action.getName());
//
//    jdevAction.setController( (Controller) controller);
//    jdevAction.putValue(IdeAction.CATEGORY, RefactorItAddin.REFACTORIT_CATEGORY);

    return jdevAction;
  }

  protected Object createPlatformAction(RefactorItAction
      ritAction) {
    int id = AbstractionUtils.getNewCmdId(ritAction.getKey());
    IDEController controller = IDEController.getInstance();

//    if (! (controller instanceof RefactorItController)) {
//      DebugInfo.trace(
//          "Controller should be instance of JDev RefactorItController!!!");
//
//    }
    KeyStroke stroke = ritAction.getKeyStroke();
    IdeAction jdevAction = IdeAction.get(id,
        controller.getClass().getName(), ritAction.getName(), null,
        new Integer((stroke == null ? 0 : stroke.getKeyCode())),
        getIcon(ritAction.getKey()), Boolean.FALSE, true);

    AbstractionUtils.addController(jdevAction, (Controller) controller);
    jdevAction.putValue(IdeAction.CATEGORY, RefactorItAddin.REFACTORIT_CATEGORY);
    jdevAction.putValue(JDevMenuBuilder.BINSELECTION_NECCESSITY,
        new Integer(JDevMenuBuilder.BINSELECTION_DONT_CARE));

//    IdeAction jdevAction = IdeAction.get(id,
//        controller.getClass().getName(), refactoryAction.getName());
//
//    jdevAction.setController( (Controller) controller);
//    jdevAction.putValue(IdeAction.CATEGORY, RefactorItAddin.REFACTORIT_CATEGORY);

    jdevToRITActionMap.put(new JDevActionKey(jdevAction), ritAction);

    return jdevAction;
  }

  public Object getIdeSpecificAction(String key, RunContext rContext) {
    return getAction(key);
  }
}
