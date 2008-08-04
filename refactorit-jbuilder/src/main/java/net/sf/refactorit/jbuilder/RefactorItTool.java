/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.standalone.JRefactorItDialog;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.BackAction;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.tree.NodeIcons;

import com.borland.primetime.PrimeTime;
import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.editor.EditorManager;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.BrowserListener;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.ide.ProjectView;
import com.borland.primetime.node.Node;
import com.borland.primetime.properties.PropertyManager;
import com.borland.primetime.util.VetoException;

import java.io.File;
import java.net.URL;


/**
 * Main Tool class.
 *
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 *
 */
public class RefactorItTool extends JBController {

  static {
    ensureControllerInit();
  }

//  public static void performAction( RefactorItAction action, Node[] nodes ) {
//    Browser browser = Browser.getActiveBrowser();
//    try {
//      browser.doSaveAll( false );
//    } catch( VetoException ignore ) {}
//
//    startRefactorITModules( browser, action, nodes );
//  }

  public static void initOpenTool(byte major, byte minor) {
    RefactorItTool.ensureControllerInit();

    boolean bValue = RefactorITLock.lock();

    // must be true or something definitely wron
    Assert.must(bValue);
    try {
      initOpenTool0(major, minor);
    } finally {
      if (bValue) {
        RefactorITLock.unlock();
      }
    }
  }

  /**
    JB5 - 4:2
    JB6 - 4:3
    JB7 - 4:4
    JB8 - 4:5
    JB9 - 4:6
    JBX - 4:7
    JB2005 - 4:8?
   */
  private static void initOpenTool0(byte major, byte minor) {

    ((JBController)IDEController.getInstance()).setIdeVersion(major, minor);
    //System.out.println("VERSION:"+IDEController.getInstance().getIdeVersion());

    //System.err.println("OTA major: " + major + ", minor: " + minor);
    MoveType.setFilesToMoveWithJavaCompilationUnits(MoveType.
        BACKUPS_ENDING_WITH_TILDE);

    // Check OpenTools version number
    if (major != PrimeTime.CURRENT_MAJOR_VERSION) {
      return;
    }

    try {
      /*
       * Code below checks whether we are under 5 or 6 version of JBuilder
       * because only in this versions there is no this field.
       */
      EditorManager.class.getField("braceMatchingEnabledAttribute");
    } catch (NoSuchFieldException e) {
      // yes, we are under 5 or 6 version of JBuilder
      Browser.addStaticBrowserListener(new BraceMatcher());
    } catch (Exception ignore) {
    }

    NodeIcons.setNodeIcons(JBIcons.instance);

    if (Browser.getActiveBrowser() == null) {
      Browser.addStaticBrowserListener(new BrowserListener() {
        public void browserOpened(Browser browser) {
//          DialogManager.setDialogParent(browser);
          Browser.removeStaticBrowserListener(this);

          if (GlobalOptions.getOptionAsBoolean(GlobalOptions.REBUILD_AT_STARTUP, false)) {
            IDEController.getInstance().ensureProject();
          }
        }

        public void browserActivated(Browser browser) {}

        public void browserClosed(Browser browser) {}

        public void browserClosing(Browser browser) {}

        public void browserDeactivated(Browser browser) {}

        public void browserNodeActivated(Browser browser, Node node) {}

        public void browserNodeClosed(Browser browser, Node node) {}

        public void browserProjectActivated(Browser browser,
            com.borland.primetime.node.Project project) {}

        public void browserProjectClosed(Browser browser,
            com.borland.primetime.node.Project project) {}

        public void browserViewerActivated(Browser browser, Node node,
            NodeViewer viewer) {}

        public void browserViewerDeactivating(Browser browser, Node node,
            NodeViewer viewer) {}
      });
//    } else {
//      DialogManager.setDialogParent(Browser.getActiveBrowser());
    }

//    initProperties();

    Browser.addStaticBrowserListener(new BrowserListener() {
      public void browserOpened(Browser browser) {}

      public void browserActivated(Browser browser) {}

      public void browserDeactivated(Browser browser) {}

      public void browserClosing(Browser browser) throws VetoException {
        IDEController.getInstance().onIdeExit();
//        if ( serializeOldProject(browser) ) return;
//
//        if ( riProject != null ) {
//          RefactorItCache.writeCache( riProject.getAstTreeCache(),
//          RefactorItPropGroup.PROP_CACHEPATH.getValue( JBController.jbProject ) );
//        }

        return;
      }

      public void browserClosed(Browser browser) {}

      public void browserNodeActivated(Browser browser, Node node) {}

      public void browserProjectActivated(Browser browser,
          com.borland.primetime.node.Project project) {}

      public void browserProjectClosed(Browser browser,
          com.borland.primetime.node.Project project) {}

      public void browserNodeClosed(Browser browser, Node node) {}

      public void browserViewerActivated(Browser browser,
          Node node,
          NodeViewer viewer) {}

      public void browserViewerDeactivating(Browser browser,
          Node node,
          NodeViewer viewer) throws VetoException {}
    });

    MenuBuilder builder = MenuBuilder.createEmptyRefactorITMenu('o', false);
    builder.buildToplevelMenu();

//    builder.addGangOfFour();
//    builder.addSeparator();
//    builder.addCommonIdeActions();
    Browser.addMenuGroup((ActionGroup) builder.getMenu());

//    Browser.addMenuGroup((ActionGroup)MenuFactory.create().createIdeActionsMenu());

    RefactorItActionProvider provider = new RefactorItActionProvider();
    EditorManager.registerContextActionProvider(provider);
    ProjectView.registerContextActionProvider(provider);

    //toolbar

//    final ActionGroup TOOLBAR_GROUP = new ActionGroup( "RefactorIT" );
//    MenuBuilder builder2=new JBMenuBuilder(TOOLBAR_GROUP);

    MenuBuilder builder2 = MenuBuilder.create("RefactorIt", (char) 0, null, true);

    ActionRepository rep = ActionRepository.getInstance();

    builder2.addAction(rep.getAction(BackAction.KEY), true);
    builder2.addGangOfFour();

    Browser.addToolBarGroup((ActionGroup) builder2.getMenu());

    // Shortcuts
    //JBShortcuts shortcuts = new JBShortcuts();
    //EditorManager.addPropertyChangeListener( shortcuts );

    GlobalOptions.loadOptions();

    // let's register our node here - this way (as opposite to doing it
    // through Manifest) it can choose itself what it actually does
    JavaFileNodeRe.initOpenTool(major, minor);
  }

  private static void initProperties() {
    // set directory where to find refactorit modules to
    // {InstallRootDir}/lib/ext/refactorit/modules
    final File jbroot = PropertyManager.getInstallRootUrl().getFileObject();
    final String jbhome = jbroot.getAbsolutePath();

    System.setProperty("jbuilder.home", jbhome);

    final File modulesDir = discoverModulesDir();

    final String libPath = modulesDir.getAbsolutePath() +
        File.separator + "refactorit";

    System.setProperty("refactorit.modules", libPath +
        File.separatorChar + "modules");

    System.setProperty("refactorit.modules.lib", libPath);
  }

  /**
   * Discovers path to directory where RefactorIT modules are located.
   *
   * @return modules directory. Never returns <code>null</code>.
   */
  private static File discoverModulesDir() {
    final URL moduleUrl =
        RefactorItTool.class.getClassLoader().getResource(
        RefactorItTool.class.getName().replace('.', '/')
        + ClassFilesLoader.CLASS_FILE_EXT);
    final File module = FileCopier.getFileFromJarUrl(moduleUrl);

    if (!module.getName().equals("refactorit-jbuilder.jar")) {
      throw new RuntimeException(
          "Cannot locate refactorit-jbuilder.jar!");
    }

    return module.getParentFile();
  }

  public static void startRefactorITBrowser(Browser browser) {
    try {
      browser.doSaveAll(false);
    } catch (VetoException ignore) {}

    IDEController controller = IDEController.getInstance();
    if (!controller.ensureProject()) {
      return;
    }

    try {
//      Window oldParent = DialogManager.getDialogParent();

      JRefactorItDialog refactorit = new JRefactorItDialog(
          controller.createProjectContext(), controller.getActiveProject());

      // FIXME: it is not perfect
//      DialogManager.setDialogParent(refactorit);

      refactorit.show();

//      if (oldParent != null) {
//        DialogManager.setDialogParent(oldParent);
//      }

      browser.getProjectView().refreshTree();
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, RefactorItTool.class);
      JErrorDialog err = new JErrorDialog(
          controller.createProjectContext(), "Error");
      err.setException(e);
      err.show();
    } finally {
      // HACK to revert back right IDEController
      IDEController.setInstance(controller);
    }
  }

//  public static Project getProjectOld() {
//    return IDEController.getInstance().getActiveProject();
////    return riProject;
//  }

  /**
   * Ensures that IDEController initialization
   */

  public static void ensureControllerInit() {
    //
    IDEController instance = IDEController.getInstance();
    if (instance == null) {
      new net.sf.refactorit.jbuilder.JBController();
//      IDEController.createFor(IDEController.JBUILDER);
      initProperties();
    }
    ModuleManager.loadModules();
  }
}
