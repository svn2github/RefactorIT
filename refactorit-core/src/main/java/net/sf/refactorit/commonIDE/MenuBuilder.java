/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.ProjectOptionsAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.ui.module.UndoModule;
import net.sf.refactorit.ui.module.dependencies.DependencyLoopsAction;
import net.sf.refactorit.ui.module.wherecaught.WhereCaughtAction;

import javax.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * @author Tonis Vaga
 */
public abstract class MenuBuilder {
  public static final boolean SHOW_DISABLED_RACTIONS
      = GlobalOptions.getOption("show.disabled.actions", "true").equals("true");

  public static MenuBuilder createEmptyRefactorITMenu(char mnemonic) {
    return createEmptyRefactorITMenu(mnemonic, true);
  }

  /**
   * @param subMenu for JDev menu creating
   */
  public static MenuBuilder createEmptyRefactorITMenu(
      char mnemonic, boolean subMenu
      ) {
    return create("RefactorIT", mnemonic, "RefactorIt.gif", subMenu);
  }

  public static MenuBuilder create(String name, char mnemonic, String icon) {
    return create(name, mnemonic, icon, true);
  }

  /**
   * @param name
   * @param mnemonic null if no mnemonic
   * @param icon null if no icon
   * @param submenu
   */
  public static MenuBuilder create(
      String name, char mnemonic, String icon, boolean submenu
      ) {
    return IDEController.getInstance()
        .createMenuBuilder(name, mnemonic, icon, submenu);
  }

  public void addGangOfFour() {
    String[] keys = MenuMetadata.GANG_FOUR_KEYS;
    addKeys(keys);
  }

  public void addCommonIdeActions() {
    String groupedKeys[][] = MenuMetadata.COMMON_IDE_ACTION_KEYS;
    for (int i = 0; i < groupedKeys.length; i++) {
      this.addKeys(groupedKeys[i]);
      if (i < (groupedKeys.length - 1)) {
        addSeparator();
      }
    }
    //addKeys(MenuMetadata.COMMON_IDE_ACTION_KEYS);
  }

  public void addUndoActions() {
    IdeAction[] actions = UndoModule.getInstance().getAllActions();
    for (int i = 0; i < actions.length; i++) {
      addIdeAction(actions[i]);
    }

//    String keys[] = new String[actions.length];
//    for (int i = 0; i < keys.length; i++) {
//      keys[i] = actions[i].getKey();
//    }
//    addKeys(keys);
  }

  private void addIdeAction(IdeAction action) {
    addAction(ActionRepository.getInstance()
        .getAction(action.getKey()), action.isAvailable());
  }

  private void addKeys(final String[] keys) {
    addKeys(keys, null);
  }

  /**
   * @return true if added at least something
   */
  private boolean addKeys(final String[] keys, RunContext context) {
    boolean anything = false;
    for (int i = 0; i < keys.length; ++i) {
      // show project options only under Netbeans
      if (!keys[i].equals(ProjectOptionsAction.KEY)
          || IDEController.runningNetBeans()) {
        Object action;

        // FIXME something is really wrong here - depending on the availability
        // of context, it returns actions of totally different branches: ide specific or RIT own ones
        if (context == null) {
          action = ActionRepository.getInstance().getAction(keys[i]);
        } else {
          action = ActionRepository.getInstance()
              .getIdeSpecificAction(keys[i], context);
        }

        boolean enabled = true;
        // TODO: many actions doesn't inherit IdeAction
        if (action instanceof IdeAction) {
          enabled = ((IdeAction) action).isAvailable();
        } else {
//AppRegistry.getLogger(this.getClass()).error("Action: " + action + " - " + action.getClass());
        }

        if (action instanceof RefactorItAction
            && ((RefactorItAction) action).getKey() == DependencyLoopsAction.KEY) {
          continue;
        }

        if (action != null) {
          addAction(action, enabled);
          anything = true;
        }
      }
    }

    return anything;
  }

  /**
   * @param runContext
   * @return true if at least one action was added into menu
   */
  public boolean addActionsForContext(RunContext runContext) {
    List enabledActions = ModuleManager.getActions(runContext);

    MenuBuilder subMenuBuilder = null;

    List allActions = new ArrayList(ModuleManager.getAllActions());

    if (Assert.enabled) {
      final boolean containsAll = allActions.containsAll(enabledActions);
      if (!containsAll) {
        enabledActions.removeAll(allActions);
        Assert.must(false,
            "AllActions doesn't contain following actions: " + enabledActions);
      }
    }

    boolean result = false;

    ActionRepository rep = ActionRepository.getInstance();

    List mainMenuKeys = Arrays.asList(MenuMetadata.MOST_USED_ACTIONS);
    List goToKeys = Arrays.asList(MenuMetadata.GO_TO_ACTIONS);

    List mainMenuActions = new ArrayList(mainMenuKeys.size());

    // first process main menu actions
    for (Iterator it = allActions.iterator(); it.hasNext(); ) {
      RefactorItAction rAction = (RefactorItAction) it.next();

      if (!mainMenuKeys.contains(rAction.getKey())) {
        continue;
      }

      mainMenuActions.add(rAction);
      it.remove();
    }

    Object moreSubMenu = null;

    for (int index = 0; index < allActions.size(); index++) {
      RefactorItAction rAction = (RefactorItAction) allActions.get(index);
      Object action = rep.getIdeSpecificAction(rAction.getKey(), runContext);
      if (action != null) {
        boolean enabled = enabledActions.contains(rAction);
        if ((enabled || SHOW_DISABLED_RACTIONS)
            && !mainMenuKeys.contains(rAction.getKey())
            && rAction.isReadonly()) {
          subMenuBuilder = create("More...", 'S', null, true);
          moreSubMenu = subMenuBuilder.getMenu();
//          addSubMenu(moreSubMenu);
//          addSeparator();
          break;
        }
      }
    }

    for (int index = 0; index < allActions.size(); index++) {
      RefactorItAction rAction = (RefactorItAction) allActions.get(index);
      Object action = rep.getIdeSpecificAction(rAction.getKey(), runContext);

      if (rAction.getKey() == DependencyLoopsAction.KEY) {
        continue;
      }

      if (action != null) {
        boolean enabled = enabledActions.contains(rAction);

        if (enabled || SHOW_DISABLED_RACTIONS) {
          if (subMenuBuilder != null && rAction.isReadonly()) {
            subMenuBuilder.addAction(action, enabled);
          } else {
            addAction(action, enabled);
          }
        }

        result = true;
      } else {
        AppRegistry.getLogger(this.getClass()).debug("action" + rAction.getName()
        + " not found in repository!");
      }
    }

    if (result) {
      addSeparator();
    }

    MenuBuilder goToMenuBuilder = null;
    for (Iterator it = mainMenuActions.iterator(); it.hasNext(); ) {
      RefactorItAction rAction = (RefactorItAction) it.next();

      if (rAction.getKey() == DependencyLoopsAction.KEY) {
        continue;
      }

      Object action = rep.getIdeSpecificAction(rAction.getKey(), runContext);
      if (action != null) {
        boolean enabled = enabledActions.contains(rAction);
        if (enabled || SHOW_DISABLED_RACTIONS) {
//          if (goToKeys.contains(rAction.getKey())) {
//            if (goToMenuBuilder == null) {
//              goToMenuBuilder = create("Go To", 'G', null, true);
//              addSubMenu(goToMenuBuilder.getMenu());
//            }
//            goToMenuBuilder.addAction(action, enabled);
//          } else {
            addAction(action, enabled);
//          }
        }

        result = true;
      } else {
        AppRegistry.getLogger(this.getClass()).debug("action" + rAction.getName()
        + " not found in repository!");
      }
    }

    if (moreSubMenu != null) {
      addSubMenu(moreSubMenu);
    }

//    if (subMenuBuilder != null) {
//      addSubMenu(subMenuBuilder.getMenu());
//    }

    return result;
  }

  public void buildContextMenu(RunContext runContext) {
    addUndoActions();
    addSeparator();

    if (runContext.getItemClasses() != null
        && runContext.getItemClasses().length != 0) {
      addActionsForContext(runContext);
    } else {
      addMoreMenu(runContext);
    }
  }

  public void buildToplevelMenu() {
//    addGangOfFour();
//    addSeparator();
    addCommonIdeActions();
//    addSeparator();
//    addSeparator();
//    addUndoActions();
//    addSeparator();

  }

  public abstract void addSubMenu(Object subMenu);

  public abstract void addAction(Object action, boolean isEnabled);

  public abstract void addSeparator();

  public abstract Object getMenu();

  /**
   * @param rootContext -- context which will be cloned for individual
   *    items, should not be null.
   */
  public void addMoreMenu(RunContext rootContext) {
    // Create the submenus
    MenuBuilder packageMenu = create("Package", 'P', null, true);
    MenuBuilder classMenu = create("Class", 'C', null, true);
    MenuBuilder methodMenu = create("Method", 'M', null, true);
    MenuBuilder fieldMenu = create("Field", 'F', null, true);
    MenuBuilder othersMenu = create("Other", 'O', null, true);

    if (packageMenu.addActionsForContext(cloneContext(rootContext, BinPackage.class))) {
      this.addSubMenu(packageMenu.getMenu());
    }

    if (classMenu.addActionsForContext(cloneContext(rootContext,
        new Class[] {BinClass.class, BinInterface.class}))) {
      addSubMenu(classMenu.getMenu());
    }

    if (methodMenu.addActionsForContext(cloneContext(rootContext, BinMethod.class))) {
      addSubMenu(methodMenu.getMenu());
    }

    if (fieldMenu.addActionsForContext(cloneContext(rootContext, BinField.class))) {
      addSubMenu(fieldMenu.getMenu());
    }

    if (othersMenu.addKeys(new String[] {WhereCaughtAction.KEY}
        , rootContext)) {
      addSubMenu(othersMenu.getMenu());
    }

//    // create the root (More) menu and add the submenus into it
//    MenuBuilder moreMenu = create("More", 'M',null);
//    moreMenu.addSubMenu(packageMenu.getMenu());
//    moreMenu.addSubMenu(classMenu.getMenu());
//    moreMenu.addSubMenu(methodMenu.getMenu());
//    moreMenu.addSubMenu(fieldMenu.getMenu());
//    this.addSubMenu(moreMenu);
  }

  private static RunContext cloneContext(
      final RunContext rootContext, final Class binClass) {
    return cloneContext(rootContext, new Class[] {binClass});
  }

  private static RunContext cloneContext(
      RunContext rootContext, Class[] classes) {
    RunContext ctx = (RunContext) rootContext.clone();
    ctx.setItemClasses(classes);
    return ctx;
  }

  protected ImageIcon getIcon(final String icon) {
    return ResourceUtil.getIcon(UIResources.class, icon);
  }
}
