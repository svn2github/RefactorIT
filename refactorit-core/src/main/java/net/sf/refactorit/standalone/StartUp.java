/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;

import net.sf.refactorit.cli.Cli;
import net.sf.refactorit.cli.UsageInfo;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.test.commonIDE.MockIDEController;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.ModuleManager;

import javax.swing.UIManager;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;


/**
 * Main
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.7 $ $Date: 2005/12/09 12:03:04 $
 */
public final class StartUp {

  /**
   * Starts Standalone RefactorIT Browser.
   *
   * @param args  command line arguments
   */
  public static void main(String[] args) {
    if (args.length > 1 && args[0].equals("-nogui")) {
      initCliEnvironment();
      new Cli().run(args);
      return;
    } else if (args.length >= 1) {
      new UsageInfo().show();
      return;
    }

    initEnvironment();
    ModuleManager.loadModules();

    String opt;

    // FileChooser last path
    opt = GlobalOptions.getOption("lastpath");
    if (opt == null) {
      opt = System.getProperty("user.home");
      GlobalOptions.setOption("lastpath", opt);
    }

    GlobalOptions.setLastDirectory(new File(opt));

    // Look & Feel
    opt = GlobalOptions.getOption("lnf");
    if (opt == null) {
      opt = UIManager.getSystemLookAndFeelClassName();
    } else if (opt.equals("crossplatform")) {
      opt = UIManager.getCrossPlatformLookAndFeelClassName();
    }

    try {
      UIManager.setLookAndFeel(opt);
    } catch (Exception e) {
      try {
        opt = UIManager.getCrossPlatformLookAndFeelClassName();
        UIManager.setLookAndFeel(opt);
      } catch (Exception x) {
        opt = UIManager.getLookAndFeel().getClass().getName();
      }
    }

    GlobalOptions.setOption("lnf", opt);

    new StandaloneController();
  //    IDEController.createFor(IDEController.STANDALONE);

    final JRefactorItFrame frame = StandaloneController.getMainWindow();

    try {
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosed(WindowEvent e) {
          System.exit(0);
        }
      });

      frame.setVisible(true);

      if (RuntimePlatform.isMacOsX()) {
        RuntimePlatform.MacOsX.addAppleEventHandlers(frame);
      }

      if (net.sf.refactorit.ui.UIResources.startupDialogEnabled) {
        showStartupDialogMethod(frame);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  public static void initCliEnvironment() {
    initEnvironment();
    ModuleManager.loadModules();
    IDEController.setInstance(new MockIDEController());
  }

  private static void initEnvironment() {
    if (RuntimePlatform.isMacOsX()) {
      // MacOSX: move menu bar into main Apple menu bar.
      // Only works with Aqua look & feel (which is the default under MacOSX)
      // > 1.3.1
      System.setProperty("com.apple.macos.useScreenMenuBar", "true");

      // > 1.4.1
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    // FIXME: not tested
    /*final URL systemResource = ClassLoader.getSystemResource(
        net.sf.refactorit.ui.Main.class.getName().replace('.', '/')
        + ClassFilesLoader.CLASS_FILE_EXT);*/
    final String systemResourcePath =
      UIResources.class.getName().replace('.', '/') +
      ClassFilesLoader.CLASS_FILE_EXT;

    final URL systemResource =
      UIResources.class.getClassLoader().getResource(systemResourcePath);

    if (systemResource == null) {
      throw new RuntimeException(
          "Could not find main class location via classloader");
    }

    final String dir = FileCopier.getFileFromJarUrl(systemResource).getParent();

    System.setProperty("refactorit.modules", dir + File.separatorChar + "modules");
    System.setProperty("refactorit.modules.lib", dir);
    System.setProperty("refactorit.platform", "standalone");

    GlobalOptions.loadOptions();
  }

  private static void showStartupDialogMethod(JRefactorItFrame frame) {
    File proj = null;

    JStartupDialog startup = new JStartupDialog(frame, true);

    String opt = GlobalOptions.getOption("project.open_recent");
    if (opt != null && "true".equals(opt)) {
      proj = startup.getRecentOpenedProject();
    }

    if (proj == null) {
      startup.show();
      proj = startup.getProject();
    }

    if (proj == null) {
      System.exit(0);
    }

    // Starts loading
    frame.setProject(proj); // XXX ???
  }
}
