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
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.eclipse.dialog.SWTDialogFactory;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.tree.NodeIcons;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import javax.swing.UIManager;

import java.io.File;
import java.net.URL;


/**
 * The main plugin class to be used in the desktop.
 *
 * TODO: saving RefactorIT workspace and project before shutdown
 */
public class RitPlugin extends AbstractUIPlugin {
  private static final Logger log = AppRegistry.getLogger(RitPlugin.class);

  // The shared instance.
	private static RitPlugin plugin;

	public RitPlugin() {
    plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      log.debug(e);
    }

    NodeIcons.setNodeIcons(EclipseIcons.instance);
    RitDialog.setDialogFactory(new SWTDialogFactory());
    HelpViewer.setTopicDisplayer(new EclipseTopicDisplayer());

    URL pluginHomeUrl = Platform.resolve(context.getBundle().getEntry("/"));

    String homePath = getAbsolutePath(pluginHomeUrl);

    System.setProperty("refactorit.modules",
        homePath + File.separator + "modules");

    System.setProperty("refactorit.modules.lib",
        homePath + File.separator + "lib");

    ModuleManager.loadModules();

    new EclipseController();
  }

  private static String getAbsolutePath(URL url) {
    String result = url.getFile();

    if (RuntimePlatform.isWindows()) {
      result = result.substring(1); // remove '/' from beginning
    }

    return result;
  }

	public void stop(BundleContext context) throws Exception {
	  try {
      log.debug("shutdowning plugin");

      IDEController.getInstance().onIdeExit();
    } catch (Exception e) {
      log.error(e);
    } finally {
      super.stop(context);
    }
	}

	/**
	 * Returns the shared instance.
	 */
	public static RitPlugin getDefault() {
		return plugin;
	}

  public static String getId() {
    return plugin.getBundle().getSymbolicName();
  }

  public static Shell getShell() {
    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

    return (w == null) ? null : w.getShell();
  }

  public static void showSwtAwtErrorMessage() {
    MessageBox mb = new MessageBox(getShell(),
        SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);

    mb.setMessage(
        "Failed to initialize SWT_AWT bridge, which prevents" +
        " RefactorIT from functioning properly.\n" +
        "Under Linux, please ensure that Eclipse is running with" +
        " Java version 5.0 or newer.");

    mb.open();
  }
}
