/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;



import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.AboutAction;
import net.sf.refactorit.ui.module.BackAction;
import net.sf.refactorit.ui.module.CleanAction;
import net.sf.refactorit.ui.module.CrossHtmlAction;
import net.sf.refactorit.ui.module.HelpAction;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.module.ProductivityGuideAction;
import net.sf.refactorit.ui.module.ProjectOptionsAction;
import net.sf.refactorit.ui.module.RebuildAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.ui.module.StandaloneBrowserAction;
import net.sf.refactorit.ui.module.UndoModule;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.module.type.TypeAction;
import net.sf.refactorit.ui.module.where.WhereAction;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;

import javax.swing.Icon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public abstract class ActionRepository {
  protected HashMap ritActionsMap = new HashMap(30);
  protected HashMap ideActionsMap = new HashMap();

  public ActionRepository() {
  }

  protected abstract Object createPlatformAction(RefactorItAction action);

  public Collection getRefactorItActions() {
    return ritActionsMap.values();
  }

  public Object[] getGangOfFour() {
    String[] keys = MenuMetadata.GANG_FOUR_KEYS;
    return getActionsForKeys(keys);
  }

  public Object[] getActionsForKeys(String[] keys) {
    Object result[] = new Object[keys.length];

    for (int i = 0; i < keys.length; i++) {
      result[i] = getAction(keys[i]);
    }
    return result;
  }

//
//  public Object [] getCommonActions() {
//    return getActionsForKeys(MenuMetadata.COMMON_IDE_ACTION_KEYS);
//  }

  public Object getAction(String key) {
    Object result = ritActionsMap.get(key);
    if (result == null) {
      result = ideActionsMap.get(key);
    }
    if (result == null) {
      AppRegistry.getLogger(this.getClass()).debug("ActionRepository.getAction() returned null for key="
      + key);
    }
    return result;
  }

  /**
   * NB! every subclass instance should call it once and only
   *   once in constructor!!!
   */
  protected final void init() {
    ritActionsMap.clear();
    ideActionsMap.clear();
    initRefactorItActions();
    initIdeActions();
    initUndoActions();
  }

  private void initUndoActions() {
    UndoModule module = UndoModule.getInstance();
    IdeAction actions[] = module.getAllActions();
    addActions(actions);
  }

  private final void initRefactorItActions() {
    List actions = ModuleManager.getAllActions();
    addActions((RefactorItAction[]) actions.toArray(
        new RefactorItAction[actions.size()]));
  }

  public Collection getShortcutActions() {
    ArrayList list = new ArrayList(getRefactorItActions());

    extractShortcutActions(list, ideActions);
    extractShortcutActions(list, UndoModule.getInstance().getAllActions());

    return list;
  }

  private void extractShortcutActions(final ArrayList list,
      final IdeAction[] actions) {
    for (int i = 0; i < actions.length; i++) {
      if (actions[i] instanceof ShortcutAction) {
        list.add(getAction(actions[i].getKey()));
      }
    }
  }

  private void addActions(final RefactorItAction[] actions) {
    for (int i = 0; i < actions.length; i++) {
      RefactorItAction action = actions[i];
      Object platformAct = createPlatformAction(action);
      Assert.must(platformAct != null);
      ritActionsMap.put(action.getKey(), platformAct);
    }
  }

  private final void initIdeActions() {
    addActions(ideActions);
  }

  private void addActions(final IdeAction[] actions) {

    for (int i = 0; i < actions.length; i++) {
      ideActionsMap.put(actions[i].getKey(), createPlatformIDEAction(actions[i]));
    }

  }

  protected abstract Object createPlatformIDEAction(IdeAction action);

  public static ActionRepository getInstance() {
    return IDEController.getInstance().getActionRepository();
  }

  protected Icon getIcon(String actionKey) {
    String fileName = null;

    if (actionKey.equals(WhereAction.KEY)) {
      fileName = "where_action.gif";
    } else if (actionKey.equals(RenameAction.KEY)) {
      fileName = "rename_action.gif";
    } else if (actionKey.equals(TypeAction.KEY)) {
      fileName = "info_action.gif";
    } else if (actionKey.equals(GotoAction.KEY)) {
      fileName = "goto_action.gif";
    } else if (actionKey.equals(StandaloneBrowserAction.KEY)) {
      fileName = "RefactorIt.gif";
    } else if (actionKey.equals(BackAction.KEY)) {
      fileName = "back_action.gif";
    }

    if (fileName != null) {
      return ResourceUtil.getIcon(UIResources.class, fileName);
    }

    return null;
  }

  public abstract Object getIdeSpecificAction(String key, RunContext rContext);

  private final IdeAction[] ideActions = new IdeAction[] {
      new RebuildAction(),
      new CleanAction(),
      new CrossHtmlAction(),
      new OptionsAction(),
      new HelpAction(),
      new AboutAction(),
      new StandaloneBrowserAction(),
      new BackAction(),
      new ProjectOptionsAction(),
      new ProductivityGuideAction(),
  };

  public boolean isIdeAction(Object action) {
    return ideActionsMap.containsValue(action);
  }

  public String[] getIdeActionKeys() {
    String result[] = new String[ideActions.length];

    for (int i = 0; i < ideActions.length; i++) {
      result[i] = ideActions[i].getKey();
    }
    return result;
  }


  public Object[] getToolbarActions() {
    List result = new ArrayList();
    result.add(getAction(BackAction.KEY));
    result.addAll(Arrays.asList(getGangOfFour()));
    return result.toArray();
  }
}
