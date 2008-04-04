/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;



import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.eclipse.dialog.SWTContext;
import net.sf.refactorit.ui.help.TopicDisplayer;
import net.sf.refactorit.ui.module.IdeWindowContext;

import org.apache.log4j.Logger;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;


class EclipseTopicDisplayer implements TopicDisplayer {
  static final Logger log = AppRegistry.getLogger(EclipseTopicDisplayer.class);

  public void displayTopic(IdeWindowContext context, String topicId) {
    // Fully Qualified ContextID: "plugin.name." + "local_context_id"
    final String contextId = "com.refactorit." + topicId.replace('.', '_');

    Shell shell = null;
    if (context instanceof SWTContext) {
      shell = ((SWTContext) context).getShell();
    } else {
      shell = RitPlugin.getShell();
    }

    Display display;
    if (shell != null) {
      display = shell.getDisplay();
    } else {
      display = Display.getDefault();
    }

    display.asyncExec(new Runnable() {
      public void run() {
        displayTopic(contextId);
      }
    });
  }

  /*
   * Must be executed in SWT user interface thread
   */
  void displayTopic(String contextId) {
    IContext context = HelpSystem.getContext(contextId);
    if (context == null) {
      log.warn("Unknown help context id: " + contextId);
      return;
    }

    log.debug("Help context id found: " + contextId);

    IHelpResource[] resource = context.getRelatedTopics();
    if (resource.length == 0) {
      log.warn("No resources for help context id: " + contextId);
      return;
    }
    if (resource.length > 1) {
      log.warn("Multiple resources for help context id: " + contextId);
    }

    String href = resource[0].getHref();

    log.warn("Help context resource: " + href);

    WorkbenchHelp.displayHelpResource(href);
  }
}
