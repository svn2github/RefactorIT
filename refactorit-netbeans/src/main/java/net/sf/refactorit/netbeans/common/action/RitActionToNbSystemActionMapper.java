/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.netbeans.common.action;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.ActionProxy;

import org.openide.util.actions.SystemAction;


/**
 *
 * @author  RISTO A
 */
public class RitActionToNbSystemActionMapper {
  private static final String ACTIONS_PACKAGE =
      AboutNBAction.class.getPackage().getName();
  
  public static SystemAction getSystemAction(ActionProxy ritAction) {
    SystemAction result = SystemAction.get(getSystemActionClass(ritAction));
    if (result == null) {
      showInternalError("Could not locate a SystemAction for: "
          + getSystemActionClass(ritAction));
    }
    return result;
  }

  private static Class getSystemActionClass(ActionProxy ritAction) {
    return findSystemActionClass(ClassUtil.getShortClassName(ritAction));
  }

  private static Class findSystemActionClass(final String ritActionClassName) {
    Class result = getSystemActionClassFor(ritActionClassName);
    if(result == null) {
      String possibleNames = StringUtil.mergeArrayIntoString(
          getPossibleSystemActionNamesFor(ritActionClassName).toArray(),
          ", ", " or ");
      
      /*
       * This error usually happens due to a programmer error --
       * when a new action has been added to RefactorIT, 
       * but a corresponding action (in the NB "actions"
       * package) has NOT been added.   
       */
      showInternalError("RefactorIT compile or packaging error:\r\n\r\n" + 
          possibleNames + " is missing from " + ACTIONS_PACKAGE);
      
      return null;
    }
    
    return result;
  }

  private static Class getSystemActionClassFor(final String ritActionClassName) {
    List possibleNames = getPossibleSystemActionNamesFor(ritActionClassName);
    
    for (Iterator i = possibleNames.iterator(); i.hasNext();) {
      String possibleName = (String) i.next();
      if (systemActionsPackageContains(possibleName)) {
        return classFromSystemActionsPackage(possibleName);
      }
    }
    
    return null;
  }

  private static List getPossibleSystemActionNamesFor(final String ritActionName) {
    return Arrays.asList(new String[] {
        ritActionName, 
        StringUtil.replace(ritActionName, "Action", "NBAction") } );
  }

  private static void showInternalError(String message) {
    message += "\r\n\r\nMenu disabled.";

    DialogManager.getInstance().showCustomError(
        IDEController.getInstance().createProjectContext(), message);
    throw new RuntimeException(message);
  }

  private static Class classForName(String name) {
    try {
      return Class.forName(name);
    } catch (Exception e) {
      return null;
    }
  }

  private static Class classFromSystemActionsPackage(String className) {
    return classForName(ACTIONS_PACKAGE + "." + className);
  }

  private static boolean systemActionsPackageContains(String className) {
    return classFromSystemActionsPackage(className) != null;
  }
}
