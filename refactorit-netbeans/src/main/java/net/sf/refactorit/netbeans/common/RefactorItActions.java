/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;




import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.FastItemFinder;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.netbeans.common.action.RITAction;
import net.sf.refactorit.netbeans.common.projectoptions.ProjectsManager;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.netbeans.common.testmodule.NBTempFileCreator;
import net.sf.refactorit.netbeans.common.testmodule.NBTestRunnerModule;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.help.TopicDisplayer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.ui.tree.JTypeInfoPanel;
import net.sf.refactorit.ui.tree.NodeIcons;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.text.IndentEngine;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.Keymap;
import javax.swing.text.StyledDocument;


/**
 * Context actions for Class/Interface/field/method.
 *
 * @author Igor Malinin, Risto
 */
public class RefactorItActions extends SystemAction
  implements Presenter.Menu, Presenter.Popup
{
  private static final Logger log = Logger.getLogger(RefactorItActions.class);

  static RefactorItActionsVersionState versionState;

  static {
    NBController.ensureControllerInit();
  }

  public static void setVersionState(RefactorItActionsVersionState newVersionState) {
    versionState = newVersionState;
  }

  /** PUBLIC: used via reflection (look for strings containing its name) */
  public static boolean staticInitDone = false;

  //  private static final int[] NB_3_3      = new int[] {1, 43, 1};
  //  private static final int[] NB_3_4      = new int[] {2, 16, 0};
  //  private static final int[] S1S_5       = new int[] {3, 42, 1};
  //  public static final  int[] NB_3_5      = new int[] {3, 42, 1};
  //  private static final int[] NB_3_6      = new int[] {4, 26, 0};
  //  private static final int[] NB_4_beta_2 = new int[] {4, 43, 0};

  private static final int[] NB_4_0          = new int[] {4, 40, 0};
  private static final int[] NB_5_0          = new int[] {6, 0, 0};
  private static final int[] S1S_5_pre       = new int[] {3, 42, 0};


  protected void initialize() {
    super.initialize();

    staticInit();
  }

  public static synchronized void staticInit() {
    if (staticInitDone) {
      return;
    }

    // Adapt NetBeans API for dialog factoring
    RitDialog.setDialogFactory(new NBDialogFactory());

    if (RefactorItConstants.developingMode) {
      ModuleManager.registerModule(new NBTestRunnerModule());
      TempFileCreator.setInstance(new NBTempFileCreator());
    }

    try {
      NBShortcutsInstaller.installShortcuts();
      installJspPopupMenu();

      // Fixes 1783; needs to be called before a JavaHelpTopicDisplay is
      // constructed
      HelpViewer.setTopicDisplayer(new TopicDisplayer() {
        public void displayTopic(IdeWindowContext context, String topicId) {
          versionState.displayHelpTopic(topicId);
        }
      });

      SwingUtil.invokeLater(new Runnable() {
        public void run() {
//          DialogManager.setDialogParent(WindowManager.getDefault()
//                  .getMainWindow());
        }
      });

      NodeIcons.setNodeIcons(NBIcons.instance);

      installSystemProperties();

      // let's better init it on the first creating of a menu
      //importFormatterEngineDefaults(false);

      // This needs testing, etc under NB (seems to work well on JB, though)
      //MoveType.setFilesToMoveWithJavaCompilationUnits(MoveType.BACKUPS_ENDING_WITH_TILDE);

      ModuleManager.loadModules();
      staticInitDone = true;


      // add listener to system/Projects folder. So that the refactory project
      // file is created
      // under newly created project folder (e.g. system/Projects/test) just in
      // that moment when the user creates it.
      ProjectsManager.addListenerToNBProjectsFolder();

      // Without this, the "class details view" would not have NB shortcuts
      // imported.
      JTypeInfoPanel.setIdeShortcutsImporter(new JTypeInfoPanel.IdeKeyboardActionsRegister() {
          public List getKeyboardActions() {
            List result = new ArrayList();

            Keymap map = (Keymap) Lookup.getDefault()
                    .lookup(Keymap.class);
            KeyStroke[] keystrokes = map.getBoundKeyStrokes();

            for (int j = 0; j < keystrokes.length; j++) {
              Action action = map.getAction(keystrokes[j]);
              if (action instanceof NBShortcuts.ActionKeyProvider) {
                final String actionKey = ((NBShortcuts.ActionKeyProvider) action)
                        .getActionKey();

                result.add(new JTypeInfoPanel.KeyboardAction(
                        keystrokes[j]) {
                  public void actionPerformed(Object bin, Component owner) {
                    RITAction.run(bin, actionKey, owner);
                  }
                });
              }
            }

            return result;
          } });

      // fixes bug 1891
      GlobalOptions.REBUILD_AT_STARTUP_OPT.setVisible(false);

      // fixes bug 1864
      TopComponent.getRegistry().addPropertyChangeListener(
              new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                  if (e.getPropertyName().equals(
                          TopComponent.Registry.PROP_ACTIVATED)) {
                    UIResources.fireHidePopups();
                  }
                }
              });

      Assert.must(IDEController.runningNetBeans(),
              "Running under NetBeans not detected");

      // Makes it easier for me to run tests under NB -- starts tests
      // automatically after NB start.
      // Enable by putting a ".run-tests" file to the NB's "bin" folder.
      if (RefactorItConstants.developingMode && new File(".run-tests").exists()) {
        final Timer timer = new Timer(Integer.MAX_VALUE, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.out.println(
                "Starting CVS test runner (to see how to disable, grep code/ for this message)");
            IDEController.getInstance().ensureProject();
            RefactorItAction action = new NBTestRunnerModule().getActions()[0];
            action.run(
                new NBContext(IDEController.getInstance().getActiveProject()),
                IDEController.getInstance().getActiveProject());
          }
        });
        timer.setInitialDelay(10 * 1000);
        timer.start();
      }
    } catch (Exception ex) {
      // don't let exception out
      log.warn(ex.getMessage(), ex);
    }
  }

  /**
   * set directory where to find refactory modules to
   * {netbeans.home}/modules/refactory OR {netbeans.user.home}/modules/refactory
   */
  private static void installSystemProperties() {
    final File nbModulesPath = discoverNbModulesPath();
    String modulesPath = nbModulesPath.getAbsolutePath() + File.separator
            + "refactory";
    System.setProperty("refactory.modules", modulesPath);
    System.setProperty("refactory.modules.lib", modulesPath
            + File.separatorChar + "lib");

    GlobalOptions.loadOptions();
  }

  private static void installJspPopupMenu() {
    if (openIdeSpecificationAtLeast(S1S_5_pre)) {
      boolean result = false;
      try {
        result = addActionsToJspPopupMenu();
      } catch (IOException e) {
        if (RefactorItConstants.debugInfo) {
          AppRegistry.getExceptionLogger().debug(e,
                  "Exception from Netbeans: installJspMenu",
                  RefactorItActions.class);
        }
      } finally {
        if (!result) {
          // fixme: remove getBundle calll
          //          ResourceBundle bundle =
          // ResourceUtil.getBundle(JWarningDialog.class,
          //              "Warnings");
          if (RefactorItConstants.debugInfo) {
            String msg = "JSP menu not created. JSP disabled?";
            AppRegistry.getLogger(RefactorItActions.class).debug(msg);
          }
        }
      }
    }
  }

  /**
   * Inserts this (permamently) into the filesystem:
   *
   * <pre>
   *
   *
   *    &lt;folder name=&quot;Editors&quot;&gt;
   *    &lt;folder name=&quot;text&quot;&gt;
   *    &lt;folder name=&quot;x-jsp&quot;&gt;
   *    &lt;folder name=&quot;Popup&quot;&gt;
   *    &lt;file name=&quot;SeparatorRIT_1.instance&quot;&gt;
   *    &lt;attr name=&quot;instanceClass&quot; stringvalue=&quot;javax.swing.JSeparator&quot;/&gt;
   *    &lt;/file&gt;
   *
   *    &lt;file name=&quot;net-sf-refactorit-netbeans-action-GotoAction.instance&quot;/&gt;
   *    &lt;file name=&quot;net-sf-refactorit-netbeans-action-InfoAction.instance&quot;/&gt;
   *    &lt;file name=&quot;net-sf-refactorit-netbeans-action-WhereAction.instance&quot;/&gt;
   *
   *    &lt;attr boolvalue=&quot;true&quot; name=&quot;SeparatorRIT_1.instance/net-sf-refactorit-netbeans-action-GotoAction.instance&quot;/&gt;
   *    &lt;attr boolvalue=&quot;true&quot; name=&quot;net-sf-refactorit-netbeans-action-GotoAction.instance/net-sf-refactorit-netbeans-action-InfoAction.instance&quot;/&gt;
   *    &lt;attr boolvalue=&quot;true&quot; name=&quot;net-sf-refactorit-netbeans-action-InfoAction.instance/net-sf-refactorit-netbeans-action-WhereAction.instance&quot;/&gt;
   *    &lt;attr boolvalue=&quot;true&quot; name=&quot;net-sf-refactorit-netbeans-action-WhereAction.instance/SeparatorJSP.instance&quot;/&gt;
   *    &lt;/folder&gt;
   *    &lt;/folder&gt;
   *    &lt;/folder&gt;
   *    &lt;/folder&gt;
   * </pre>
   */
  private static boolean addActionsToJspPopupMenu() throws IOException {
    FileObject systemFileSystem = Repository.getDefault()
            .getDefaultFileSystem().getRoot();

    FileObject fileObj = systemFileSystem.getFileObject("Editors")
            .getFileObject("text").getFileObject("x-jsp");
    if (fileObj == null) {
      return false;
    }
    FileObject folder = fileObj.getFileObject("Popup");
    if (folder == null) {
      return false;
    }

    if (folder.getFileObject("SeparatorRIT_1", "instance") != null) {
      return true;
    }

    folder.createData("SeparatorRIT_1", "instance").setAttribute(
        "instanceClass", "javax.swing.JSeparator");

    folder.createData("net-sf-refactorit-netbeans-action-GotoAction",
        "instance");
    folder.createData("net-sf-refactorit-netbeans-action-InfoAction",
        "instance");
    folder.createData("net-sf-refactorit-netbeans-action-WhereAction",
        "instance");

    folder.setAttribute(
        "SeparatorRIT_1.instance/net-sf-refactorit-netbeans-action-GotoAction.instance",
        "true");
    folder.setAttribute(
        "net-sf-refactorit-netbeans-action-GotoAction.instance/net-sf-refactorit-netbeans-action-InfoAction.instance",
        "true");
    folder.setAttribute(
        "net-sf-refactorit-netbeans-action-InfoAction.instance/net-sf-refactorit-netbeans-action-WhereAction.instance",
        "true");
    folder.setAttribute(
        "net-sf-refactorit-netbeans-action-WhereAction.instance/SeparatorJSP.instance",
        "true");

    return true;
  }

  public static void importFormatterEngineDefaults() {
    String checked = GlobalOptions.getOption("source.format.checked_netbeans_settings", "false");
    if (checked != null && "true".equals(checked)) {
      return; // imported already
    }

    IndentEngine engine = null;

    try {
      Enumeration engines = IndentEngine.indentEngines();
      while (engines.hasMoreElements()) {
        Object someEngine = engines.nextElement();
        if (someEngine.getClass().getName().indexOf("JavaIndent") != -1) {
          engine = (IndentEngine) someEngine;
          break;
        }
      }
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
    }

    //System.err.println("IndentEngine: " + engine);

    //    try {
    //      java.lang.reflect.Method[] methods = engine.getClass().getMethods();
    //      for (int i = 0; i < methods.length; i++) {
    //        System.err.println("Method: " + methods[i]);
    //        System.out.println("Method: " + methods[i]);
    //      }
    //    } catch (Exception e) {
    //      log.warn(e.getMessage(), e);
    //    }

    if (engine == null) {
      return;
    }

    GlobalOptions.setOption("source.format.checked_netbeans_settings", "true");
    GlobalOptions.save();

    int res = DialogManager.getInstance().showCustomYesNoQuestion(
            IDEController.getInstance().createProjectContext(),
            GlobalOptions.REFACTORIT_NAME,
            "Would you like to import formatting settings from\n"
                    + "the Java Indentation Engine of your IDE?",
            DialogManager.YES_BUTTON);
    if (res != DialogManager.YES_BUTTON) {
      return;
    }

    try {
      Object newLine = invokeMethod(engine,
              "getJavaFormatNewlineBeforeBrace");
      if (newLine != null) {
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_NEWLINE_BEFORE_BRACE, ((Boolean) newLine).booleanValue() ? "true" : "false");
      }
      //System.err.println("newLine: " + newLine);

      Object spaceBefore = invokeMethod(engine,
              "getJavaFormatSpaceBeforeParenthesis");
      if (spaceBefore != null) {
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_SPACE_BEFORE_PARENTHESIS, ((Boolean) spaceBefore).booleanValue() ? "true" : "false");
      }
      //System.err.println("spaceBefore: " + spaceBefore);

      Object spacesPerTab = invokeMethod(engine, "getSpacesPerTab");
      if (spacesPerTab != null) {
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, spacesPerTab
        .toString());
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, spacesPerTab
        .toString());
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_CONTINUATION_INDENT, "4");
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BRACE_INDENT, "0");
      }
      //System.err.println("spacesPerTab: " + spacesPerTab);

      Object expandTabs = invokeMethod(engine, "isExpandTabs");
      if (expandTabs != null) {
        GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, ((Boolean) expandTabs).booleanValue() ? "true" : "false");
      }
      // System.err.println("expandTabs: " + expandTabs);

    } catch (Exception e) {
      log.warn(e.toString(), e);
    } finally {
      try {
        GlobalOptions.save();
      } catch (RuntimeException ex) {
      }
    }
  }

  private static Object invokeMethod(Object o, String methodName) {
    try {
      return ReflectionUtil.invokeMethod(o, methodName);
    } catch (Exception e) {
      log.warn(e.toString(), e);
      return null;
    }
  }

  private static boolean alreadyShownOpenIdeSpecificationError = false;

  private static boolean openIdeSpecificationAtLeast(int[] requiredVersion) {
    String propertyName = "org.openide.specification.version";
    try {
      return StringUtil.systemPropertyVersionAtLeast(requiredVersion,
              propertyName);
    } catch (Exception e) {
      if( ! alreadyShownOpenIdeSpecificationError ) {
        alreadyShownOpenIdeSpecificationError = true;

        ErrorManager.showAndLogInternalError(e);
      }

      return false;
    }
  }

  public static boolean isNetBeansFive() {
    return RefactorItActions.openIdeSpecificationAtLeast(RefactorItActions.NB_5_0);
  }

  public static boolean isNetBeansFour() {
    return RefactorItActions.openIdeSpecificationAtLeast(
        RefactorItActions.NB_4_0);
  }

  public static boolean isNetBeansThree() {
    return ! isNetBeansFour();
  }

  private static File discoverNbModulesPath() {
    final URL moduleUrl = RefactorItActions.class.getClassLoader().getResource(
            RefactorItActions.class.getName().replace('.', '/')
                    + ClassFilesLoader.CLASS_FILE_EXT);
    final File module = FileCopier.getFileFromJarUrl(moduleUrl);
    if(isNetBeansFive()) {
      if (!module.getName().equals("refactoryNB5.jar")) {
        throw new RuntimeException("Cannot locate refactoryNB5.jar!");
      }
    } else if (isNetBeansFour()) {
      if (!module.getName().equals("refactoryNB4.jar")) {
        throw new RuntimeException("Cannot locate refactoryNB4.jar!");
      }
    } else {
      if (!module.getName().equals("refactoryNB.jar")) {
        throw new RuntimeException("Cannot locate refactoryNB.jar!");
      }
    }
    return module.getParentFile();
  }

  public void actionPerformed(ActionEvent ev) {
  }

  public String getName() {
    return GlobalOptions.REFACTORIT_NAME;
  }

  protected String iconResource() {
    String path = net.sf.refactorit.ui.UIResources.class.getName();

    // there is rumours that class.getPackage() sometimes doesn't work, so..
    path = path.substring(0, path.lastIndexOf('.')).replace('.', '/');
    path += "/images/RefactorIt.gif";

    return path;
  }

  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  public JMenuItem getMenuPresenter() {
    return createPresenterNoExceptions();
  }

  public JMenuItem getPopupPresenter() {
    return createPresenterNoExceptions();
  }

  /** Must *not* throw exceptions */
  public static JMenu createPresenterNoExceptions() {
    try {
      return createPresenter();
    } catch (Exception e) {
      ErrorManager.showAndLogInternalError(e);

      JMenu result = new JMenu(GlobalOptions.REFACTORIT_NAME);
      result.setFont(result.getFont().deriveFont(Font.BOLD));
      result.setEnabled(false);
      return result;
    }
  }

  static ClickInfo clickInfo;

  private static JMenu createPresenter() {
    clickInfo = new ClickInfo();

    JMenu menu = null;

    MenuBuilder builder = MenuBuilder.createEmptyRefactorITMenu('c');

    if (clickInfo.elements.length == 0) {
      builder.buildContextMenu(new NBRunContext(RunContext.UNSUPPORTED_CONTEXT,
              (Class[]) null, false));
      return (JMenu) builder.getMenu();
    }

    NBRunContext context = null;

    if (clickInfo.selection == null && !isSourceWindow(clickInfo.topComp)) {
      //      SubMenuModel model = new SubMenuModel( clickInfo.elements,
      //        isSourceWindow( clickInfo.topComp ), clickInfo.topComp,
      // clickInfo.caret, clickInfo.line, clickInfo.column );
      context = getExplorerMenuContext(clickInfo.caret, clickInfo.elements,
              clickInfo.topComp);
      builder.buildContextMenu(context);
      return (JMenu) builder.getMenu();
    } else if (clickInfo.selection != null) {
      //context=(NBRunContext)createRunContextForSelection(getNameStatic(),
      // clickInfo.elements, clickInfo.selection );
      context = getRunContextForBinSelection(GlobalOptions.REFACTORIT_NAME,
              clickInfo.elements, clickInfo.selection);
      builder.buildContextMenu(context);
      return (JMenu) builder.getMenu();

      //      menu = getMenuForBinSelection( getNameStatic(), clickInfo.elements,
      // clickInfo.selection );
    }
    //menu.setFont( menu.getFont().deriveFont(Font.BOLD) );

    if (isSourceWindow(clickInfo.topComp)) {
      if (clickInfo.selection == null) {
          //        context=(NBRunContext)getRunContextForSpecificBinItem(clickInfo.elements[0],
          // clickInfo.nodes[0], clickInfo.line, clickInfo.column);

           context = getRunContextForBinItem(clickInfo.elements[0],
               clickInfo.nodes[0],
               clickInfo.line, clickInfo.column);

          //createMenusForSpecificBinItem( clickInfo.elements[0],
          // clickInfo.nodes[0], clickInfo.line, clickInfo.column, menu );
          builder.buildContextMenu(context);

           return (JMenu) builder.getMenu();
      }
    }

    return menu;

  }

  protected static BinClass getBinClass(ElementInfo element) {
    BinCIType type = element.getBinCIType();

    if (type != null && type instanceof BinClass) {
      return (BinClass) type;
    } else {
      return null;
    }
  }

  public static BinMethod getBinMethodOrConstructor(ElementInfo element,
          int line) {
    BinClass binClass = getBinClass(element);
    if (binClass == null) {
      return null;
    }

    for (int i = 0; i < binClass.getDeclaredMethods().length; i++) {
      if (methodOccupiesLine(binClass.getDeclaredMethods()[i], line)) {
        return binClass.getDeclaredMethods()[i];
      }
    }

    for (int i = 0; i < binClass.getDeclaredConstructors().length; i++) {
      if (methodOccupiesLine(binClass.getDeclaredConstructors()[i], line)) {
        return binClass.getDeclaredConstructors()[i];
      }
    }

    return null;
  }

  public static boolean methodOccupiesLine(BinMethod method, int line) {
    return method.getStartLine() <= line && method.getEndLine() >= line;
  }

  static boolean isSourceWindow(Component active) {
    // HACK: Sometimes, when you close a window and then go to Tools menu
    //       NPE is thrown below. This one prevents it.
    if (active == null) {
      return false;
    }
//System.err.println("active: " + active + " - " + active.getClass());
    return (active.getClass().getName().indexOf("JavaEditor") >= 0)
            || (active.getClass().getName().indexOf("JspEditor") >= 0)
            || (active.getClass().getName().indexOf("MultiView") >= 0);
  }

  private static NBRunContext getExplorerMenuContext(final int caretPosition,
          final ElementInfo[] elements, final Container invokedIn) {
    int code = ElementInfo.isJsp(elements)
            ? RunContext.JSP_CONTEXT
            : RunContext.JAVA_CONTEXT;

    NBRunContext result = null;

    Class[] classes = null;
    try {
      classes = ElementInfo.getBinItemClasses(elements);
    } catch (BinItemNotFoundException ex) {
    }

    result = new NBRunContext(code, classes, true);

    result.setAction(new NBRefactorItAction() {
      public void actionPerformed(ActionEvent ex) {
        onExplorerMenuClick(caretPosition, elements, invokedIn,
            (RefactorItAction) super.getAction());
      }
    });

    return result;
  }


  public static void onExplorerMenuClick(final int caretPosition,
          final ElementInfo[] elements, final Container invokedIn,
          final RefactorItAction action) {
    if (!IDEController.getInstance().ensureProject()) {
      return;
    }

    final NBContext context
        = new NBContext(IDEController.getInstance().getActiveProject());

    try {
      final Object[] binItemToOperate = ElementInfo.getBinItems(elements);

      if (binItemToOperate == null || binItemToOperate.length == 0
              || binItemToOperate[0] == null) {
        FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(context,
                elements, false);
        return;
      }

      if (action != null) {
        Window window = ((AWTContext) context).getWindow();

        context.setPoint(SwingUtil.positionToClickPoint(
            invokedIn, caretPosition, window));
//System.err.println("binItemToOperate: " + Arrays.asList(binItemToOperate));

        boolean res;
        if (invokedIn instanceof net.sf.refactorit.netbeans.common.NBContext.TabComponent) {
          RITAction.actionPerformedFromResultPanel((Component) invokedIn, action.getKey());
          res = false;
        } else if (binItemToOperate.length == 1) {
          res = RefactorItActionUtils.run(action, context,
                  binItemToOperate[0]);
        } else {
          res = RefactorItActionUtils.run(action, context, binItemToOperate);
        }

        if (res) {
          action.updateEnvironment(context);
        } else {
          action.raiseResultsPane(context);
        }
      }
    } catch (final BinItemNotFoundException e) {
      SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
        public void run() {
          FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(context,
                  elements, false);
        }
      });
    } catch (final Exception e) {
      SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
        public void run() {
          ErrorManager.showAndLogInternalError(e);
        }
      });
    }
  }

  //  private static class WhereCaughtActionListener implements ActionListener {
  //    private int line;
  //    private ElementInfo element;
  //
  //    public WhereCaughtActionListener( ElementInfo element, int line ) {
  //      this.element = element;
  //      this.line = line;
  //    }
  //
  //    public void actionPerformed( java.awt.event.ActionEvent e ) {
  //      if( !IDEController.getInstance().ensureProject() ) {
  //        return; // Silent ignore
  //      }
  //
  //      if( getThrowStatement() == null ) {
  //        java.awt.Toolkit.getDefaultToolkit().beep();
  //        return; // Ignore
  //      }
  //
  //      RefactorItAction action = getWhereCaughtAction();
  //      if(action == null) {
  //        throw new RuntimeException("Where caught action is not installed");
  //      }
  //      NBContext context = new
  // NBContext(IDEController.getInstance().getActiveProject());
  //      if (RefactorItActionUtils.run(
  //      action, context, IDEController.getInstance().getIDEMainWindow(),
  // getThrowStatement())) {
  //        action.updateEnvironment(IDEController.getInstance().getIDEMainWindow(),
  // context);
  //      } else {
  //        action.raiseResultsPane(IDEController.getInstance().getIDEMainWindow(),
  // context);
  //      }
  //    }
  //
  //    private RefactorItAction getWhereCaughtAction() {
  //      Iterator iter = ModuleManager.getModules().iterator();
  //      while ( iter.hasNext() ) {
  //        RefactorItModule module = (RefactorItModule) iter.next();
  //
  //        RefactorItAction[] ma = module.getActions( getThrowStatement().getClass() );
  //        if ( ma == null || ma.length == 0 ) continue;
  //
  //        // A hack to deny access to the "Shell" module that also responds to
  // ThrowStatement.
  //        if( "Shell".equals( ma[ 0 ].getName() ) )
  //          continue;
  //
  //        return ma[ 0 ];
  //      }
  //
  //      return null;
  //    }
  //
  //    private BinThrowStatement getThrowStatement() {
  //      BinMethod method = getBinMethodOrConstructor( element, line );
  //      if( method == null )
  //        return null;
  //
  //      return new BinThrowStatementFinder().find( method, line );
  //    }
  //  }

  //  private static class BinThrowStatementFinder extends
  // net.sf.refactorit.query.BinItemVisitor {
  //    private int line;
  //
  //    private BinThrowStatement result;
  //
  //    public BinThrowStatement find( BinMethod method, int line ) {
  //      this.line = line;
  //
  //      visit( method );
  //
  //      return result;
  //    }
  //
  //    public void visit( BinThrowStatement throwStatement ) {
  //      if( throwStatement.getRootAst().getLine() == this.line )
  //        result = throwStatement;
  //
  //      super.visit( throwStatement );
  //    }
  //  }

  /**
   * This method is used to check if the the "Where Caught" menu item should be
   * visible
   */
  //  private static boolean sourceLineContainsThrowClause( int caretPosition ) {
  //    if( caretPosition < 0 ) // We're out of source window
  //      return false;
  //
  //    // The content variable above contains content of the *saved* file,
  // which means
  //    // that if the file has not been saved then the real contents of the file
  // in NB Java editor
  //    // and in the variable are different. This should be fixed somehow, but for
  // now this catch
  //    // statement is inserted below just to make sure that the code at least
  // fails silently.
  //    // ...
  //    // (Silent failing is better here because the user might not care if this
  // thing is
  //    // working at 100%, but the users probably hate the error dialogs and
  // disabling of the
  //    // rest of the menu that comes along with *not* catching this exception.)
  //    SourceCoordinate caret;
  //    try {
  //      caret = LinePositionUtil.convert( caretPosition, clickInfo.entireText );
  //    }
  //    catch( IllegalArgumentException e ) {
  //      return true;
  //    }
  //
  //    return LinePositionUtil.extractLine( caret.getLine(),
  // clickInfo.entireText).indexOf( "throw" ) >= 0;
  //  }
  private static NBRunContext getRunContextForBinSelection(String title,
          final ElementInfo[] element, final BinSelection bin) {
    int code = element[0].isJsp()
            ? RunContext.JSP_CONTEXT
            : RunContext.JAVA_CONTEXT;

    NBRunContext newResult = new NBRunContext(code, bin, element, false);

    NBAction newAction = new NBRefactorItAction() {
      public void actionPerformed(ActionEvent event) {
        if (!RefactorITLock.lock()) {
          return;
        }

        try {
          if (!IDEController.getInstance().ensureProject()) {
            return; // Silent ignore
          }

          if (bin != null) {
            bin.setCompilationUnit(element[0].getCompilationUnit());

            NBContext context = new NBContext(IDEController.getInstance()
                    .getActiveProject());

            if (bin.getCompilationUnit() == null) {
              FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(context,
                      element, true);
            } else {
              RefactorItAction action = (RefactorItAction) super.getAction();
              if (RefactorItActionUtils.run(action, context, bin)) {
                action.updateEnvironment(context);
              } else {
                action.raiseResultsPane(context);
              }
            }
          }
        } catch (Exception e) {
          // don't let exceptions fall out of RIT
          log.warn(e.getMessage(), e);
        } finally {
          RefactorITLock.unlock();
        }
      }
    };

    newResult.setAction(newAction);

    return newResult;
  }

  public static class ClickInfo {
    public Node[] nodes;
    public ElementInfo[] elements;

    public String entireText = null;
    public Point point = null;

    public Container topComp;
    public int caret = -1;
    public int line = -1;
    public int column = -1;

    public BinSelection selection = null;

    public ClickInfo() {
      topComp = TopComponent.getRegistry().getActivated();
      nodes = TopComponent.getRegistry().getActivatedNodes();

      if (nodes.length == 0) {
				elements = new ElementInfo[]{};
        //String msg = "TopComponent.getRegistry().getActivatedNodes() returns " +
            //"array with zero length, no nodes are loaded?";
        //throw new ZeroLengthArrayException(msg);
      } else {
        elements = ElementInfo.getElementsFromNodes(nodes);
      }

      if (topComp == null) {
        topComp = WindowManager.getDefault().getMainWindow();
      }

      if (isSourceWindow(topComp)) {
        EditorCookie cookie = null;

        // sometimes nodes is an array of size 0 [RIM-884]
        if (nodes != null && nodes.length > 0) {
          cookie = (EditorCookie) nodes[0]
              .getCookie(EditorCookie.class);
        }

        JEditorPane[] op = null;
        // sometimes cookie is null[bug 1958]
        if (cookie != null) {
          op = cookie.getOpenedPanes();
        }
        if ((op == null) || (op.length < 1)) {
          /*
           *TODO do some actions
           */
        } else {
          entireText = op[0].getText();
          String text = op[0].getSelectedText();
          if (text != null && text.length() > 0) {
            int start = op[0].getSelectionStart();
            int end = op[0].getSelectionEnd();

            StyledDocument doc = cookie.getDocument();

            int startL = NbDocument.findLineNumber(doc, start) + 1;
            int startC = NbDocument.findLineColumn(doc, start) + 1;
            int endL = NbDocument.findLineNumber(doc, end) + 1;
            int endC = NbDocument.findLineColumn(doc, end) + 1;

            selection = new BinSelection(text, startL, startC, endL, endC);
          } else {
            caret = op[0].getCaretPosition();
            StyledDocument doc = cookie.getDocument();

            line = NbDocument.findLineNumber(doc, caret) + 1;
            column = NbDocument.findLineColumn(doc, caret) + 1;

            point = SwingUtil.positionToClickPoint(
                op[0], caret, WindowManager.getDefault().getMainWindow());
          }
        }
      }
    }
  }

  public static NBRunContext getRunContextForBinItem(ElementInfo info,
          Node node, int line, int column) {
    Class[] binClasses = null;

    int code = RunContext.JAVA_CONTEXT;
    try {
      binClasses = FastItemFinder
          .getCurrentBinClass("", clickInfo.entireText, line, column);
    } catch (Exception e) {
      // silent
    }
    if (binClasses == null) {
      AppRegistry.getLogger(RefactorItActions.class)
              .debug("binClasses == null ");
      binClasses = new Class[0];
    }
    if (info.isJsp()) {
      code = RunContext.JSP_CONTEXT;
      binClasses = new Class[]{BinLocalVariable.class};
    }

    NBRunContext context = new NBRunContext(code, binClasses, false);
    context.setAction(new NBRefactorItAction(info, line, column));

    return context;
  }
}
