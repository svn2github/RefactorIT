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
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.jdeveloper.projectoptions.JDevProjectOptions;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.ActionProxy;
import oracle.ide.Ide;
import oracle.ide.IdeAction;
import oracle.ide.IdeAdapter;
import oracle.ide.IdeEvent;
import oracle.ide.IdeListener;
import oracle.ide.MainWindow;
import oracle.ide.MenuManager;
import oracle.ide.addin.Addin;
import oracle.ide.controls.ToolButton;
import oracle.ide.controls.Toolbar;
import oracle.ide.exception.SingletonClassException;
import oracle.ide.keyboard.KeyStrokeContextRegistry;
import oracle.ide.keyboard.KeyStrokeMap;
import oracle.ide.keyboard.KeyStrokes;

import java.awt.Component;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author  Tanel
 */
public class RefactorItAddin implements Addin {

  public static final String REFACTORY_JDEV_JAR_NAME = "refactoryJDev.jar";
  public static final String REFACTORIT_CATEGORY = "RefactorIT";

  public static IdeAction aboutAction;

  public static IdeAction whereUsedAction;

  private RefactorItAddin instance;

  RefactorItController controller = RefactorItController.getJDevInstance();

  /**
   * Default constructor.  A singleton instance is created when the addin
   * is loaded at startup.
   */
  public RefactorItAddin() throws SingletonClassException {
    if (instance != null) {
      throw new SingletonClassException(this.getClass().getName());
    }

    instance = this;
  }

  /*-------------------------------------------------------------------------
   * Addin implementation
   *------------------------------------------------------------------------*/

  /**
   * Initializes this addin when the product is launched.
   * This method is invoked by the <tt>AddinManager</tt> after the
   * singleton instance of the <tt>Addin</tt> is created.
   * Registration with various IDE managers should be performed here.
   * Other initialization tasks should be deferred until later, if practical, so
   * that the product's startup is not unnecessarily delayed.
   *
   * @see oracle.ide.AddinManager
   */
  public void initialize() {
    if (!RefactorITLock.lock()) {
      return;
    }

    try {
      initialize0();
    } finally {
      RefactorITLock.unlock();
    }
  }

  private void initialize0() {
    Ide.addIdeListener(new IdeListener() {
      public void addinsLoaded(IdeEvent e) {}

      public void mainWindowClosing(IdeEvent e) {}

      public void mainWindowOpened(IdeEvent e) {
        //FIXME: JDev hanged before I  uncommented this line[tonis]
//        if(GlobalOptions.getOptionAsBoolean(GlobalOptions.REBUILD_AT_STARTUP, false)) {
//          RefactorItController.getInstance().ensureProject(new IdeContext());
//        }
      }
    });

//    OldMenuBuilder.getInstance().init(controller);

    // Get the RefactorIT modules path and set it in System properties because
    // the ModuleManager's loadmodules() function needs to know it.
    String modulesPath = PropertyManager.getRefactorITModulesInstallDirectory();
    System.setProperty("refactory.modules", modulesPath);
    System.setProperty("refactory.modules.lib",
        modulesPath + File.separatorChar + "lib");
    //System.err.println("Using " + modulesPath + " as RefactorIT modules path");
    System.setProperty("refactorit.platform", "jdev");

    // Load the RefactorIT modules. It requires that the modules path is set in
    // System properties
    ModuleManager.loadModules();

    GlobalOptions.loadOptions();

    // Add "Info" to the main window's menu bar.
    addRefactorITMenu();

    addCtxMenuListeners(controller);

    JDevProjectOptions.init();

//    DialogManager.setDialogParent(AbstractionUtils.getMainWindow());

    final KeyStrokeContextRegistry kscr = Ide.getKeyStrokeContextRegistry();
    kscr.addContext(new KbdKeyStrokes());

    Ide.addIdeListener(new IdeAdapter() {
      public void addinsLoaded(IdeEvent e) {
        Ide.getSettings().getKeyStrokeOptions().addChangeListener(new
            ChangeListener() {

          public void stateChanged(ChangeEvent event) {
            assignShortcuts();
          }
        });
      }

      public void mainWindowOpened(IdeEvent e) {
        SwingUtilities.invokeLater(
            new Runnable() {
          public void run() {

            Toolbar bar = Ide.getToolbar();
            bar.addSeparator();

            Object mainGang[] = ActionRepository
                .getInstance().getToolbarActions();

            for (int i = 0; i < mainGang.length; i++) {
              bar.add(new ToolButton((IdeAction) mainGang[i]));
            }

            IdeAction debugShellAction = IdeAction.get(9999, null,
                "DEBUG_SHELL", null, new Integer(0), null, Boolean.FALSE, true);

            AbstractionUtils.addController(debugShellAction, controller);

            debugShellAction.putValue(
                IdeAction.CATEGORY, RefactorItAddin.REFACTORIT_CATEGORY);

            bar.add(new ToolButton(debugShellAction));
//
//    action.setController( this );
//              bar.add( new ToolButton( IdeAction.find(RefactorItController.REFACTORY_BACK_CMD_ID) ) );
//              bar.add( new ToolButton( IdeAction.find(RefactorItController.REFACTORY_WHERE_USED_CMD_ID) ) );
//              bar.add( new ToolButton( IdeAction.find(RefactorItController.REFACTORY_GO_TO_DECLARATION_CMD_ID) ) );
//              bar.add( new ToolButton( IdeAction.find(RefactorItController.REFACTORY_INFO_CMD_ID) ) );
//              bar.add( new ToolButton( IdeAction.find(RefactorItController.REFACTORY_RENAME_CMD_ID) ) );
            bar.revalidate();
            bar.repaint();
          }
        }
        );
      }

      public void mainWindowClosing(IdeEvent e) {
        IDEController.getInstance().onIdeExit();
      }
    });
  }

  void assignShortcuts() {
    KeyStrokeMap map = Ide.getSettings().getKeyStrokeOptions().getGlobalKeyMap();

    JDevActionRepository rep = (JDevActionRepository) JDevActionRepository.
        getInstance();
    Iterator it = rep.getShortcutActions().iterator();

    while (it.hasNext()) {
      IdeAction action = (IdeAction) it.next();
      ActionProxy rAction = (ActionProxy) rep.
          getRITActionFromIdeAction(action);

      if (rAction instanceof ShortcutAction) {
        Integer actionCommandId = new Integer(action.getCommandId());
        KeyStroke actionKeyStroke = ((ShortcutAction) rAction).getKeyStroke();
        if (actionKeyStroke != null
            && map.getKeyStrokesFor(actionCommandId).isEmpty()) {
          map.put(new KeyStrokes(actionKeyStroke), actionCommandId);
        }
      }
    }

    /*
     KeyStrokeMap global = Ide.getSettings().getKeyStrokeOptions().getGlobalKeyMap();
      Set set = global.keySet();
      System.out.println( " cglobal.size = " + global.values().size() );
      if ( set == null ) return;

      Iterator it = set.iterator();
      while( it.hasNext() ) {
        KeyStrokes strokes = (KeyStrokes)it.next();
        System.out.println( " strokes = " + strokes.toString());
      }*/
    /*
       Iterator iter = Ide.getKeyStrokeContextRegistry().getAllContexts();
       while( iter.hasNext() ) {
      KeyStrokeContext context = (KeyStrokeContext)iter.next();
      System.out.println( " context.getName = " + context.getName() );
      KeyStrokeMap map = Ide.getSettings().getKeyStrokeOptions().getLocalKeyMap(context);
      if ( map == null ) continue;
      Set set = map.keySet();
      if ( set == null ) continue;
      System.out.println( " map.size = " + map.values().size() );
      Iterator it = set.iterator();
      while( it.hasNext() ) {
        KeyStrokes strokes = (KeyStrokes)it.next();
        System.out.println( " strokes = " + strokes.toString() );
      }
       }
     */
  }

  /**
   * Decides if shutdown may proceed.
   * This method is invoked by the <code>AddinManager</code>
   * before normal terminations of the IDE.
   * Resources should not be released at this time.
   * @return false if shutdown should be aborted; true otherwise.
   */
  public boolean canShutdown() {
    return true;
  }

  /**
   * Terminates this addin.
   * This method is invoked by the <code>AddinManager</code>
   * during normal terminations of the IDE.
   * Any non-java resources held by this addin, such as file handles and
   * database connections, should be released here.
   */
  public void shutdown() {
  }

  /**
   * Reports this addin's version number.
   *
   * @return this addin's version number.
   */
  public float version() {
    // FIXME: return actual version
    return 2.0f;
  }

  /**
   * Identifies the IDE version for which this addin was implemented.
   *
   * @return the IDE's version number.
   */
  public float ideVersion() {
    return Ide.IDE_VERSION; //(float) 5.00;
  }

  /**
   * Creates context menu listeners to manage this addin's context menu
   * actions.
   *
   * @param controller the context menus' action controller.
   */
  public void addCtxMenuListeners(RefactorItController controller) {

    // Add a listener to the Explorer's context menu.
    // This form will work for any manager or view that defines getContextMenu.
    AbstractionUtils.getEditorContextMenu()
        .addContextMenuListener(new EditorContextMenuListener(controller));

    AbstractionUtils.getExplorerContextMenu()
        .addContextMenuListener(new ExplorerContextMenuListener(controller));

    // Add a listener to the Navigator's context menu.
    AbstractionUtils.getNavigatorContextMenu()
        .addContextMenuListener(new NavigatorContextMenuListener(controller), null);
  }

  /**
   * Adds an 'RefactorIT' menu to the main window's menu bar.
   *
   * All items are visible all of the time, because I have not found an easy
   * way of modifying the menu on the fly (do not know hot to receive change notifications).
   */
  public void addRefactorITMenu() {
    MenuManager menuMgr = Ide.getMenubar();

    MenuBuilder rMenu = MenuBuilder.createEmptyRefactorITMenu('C', false);

    rMenu.buildToplevelMenu();
    JMenu menu = (JMenu) rMenu.getMenu();

    try {
      menuMgr.insert(menu, MainWindow.Tools);
    } catch (Error e) {
      try {

        Method add = menuMgr.getClass().getMethod("add",
            new Class[] {Component.class});
        add.invoke(menuMgr, new Object[] {menu});

      } catch (Exception e1) {
        System.err.println("RefactorIT: can't add RefactorIT topmenu: ");
        e1.printStackTrace();
      }
    }
  }


  /**
   * Creates a menu item from IFE action
   *
   * @param action the IDE action embodied by the menu item.
   * @return a newly-created menu item.
   */
  protected static JMenuItem createMenuItem(IdeAction action) {
    return Ide.getMenubar().createMenuItem(action);
  }

  /**
   * Creates an empty 'RefactorIT' menu.
   *
   * @param topLevel true if the menu is to be inserted in the menu bar, false
   * if it is to be a submenu.
   * @return a newly-created menu.
   */
  protected static JMenu createRefactorITMenu(boolean topLevel) {
    MenuManager menuMgr = Ide.getMenubar();

    JMenu menu = topLevel ?
        menuMgr.createMenu("RefactorIT", new Integer('R')) :
        menuMgr.createSubMenu("RefactorIT", new Integer('R'));

    return menu;
  }
}
