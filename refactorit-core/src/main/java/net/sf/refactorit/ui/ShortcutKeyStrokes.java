/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.ui.module.RedoAction;
import net.sf.refactorit.ui.module.UndoAction;
import net.sf.refactorit.ui.module.audit.AuditAction;
import net.sf.refactorit.ui.module.calltree.CallTreeAction;
import net.sf.refactorit.ui.module.createmissingmethod.CreateMissingMethodAction;
import net.sf.refactorit.ui.module.dependencies.DependenciesAction;
import net.sf.refactorit.ui.module.dependencies.DrawDependenciesAction;
import net.sf.refactorit.ui.module.extractmethod.ExtractMethodAction;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.module.inline.InlineAction;
import net.sf.refactorit.ui.module.introducetemp.IntroduceTempAction;
import net.sf.refactorit.ui.module.move.MoveAction;
import net.sf.refactorit.ui.module.type.JavadocAction;
import net.sf.refactorit.ui.module.type.TypeAction;
import net.sf.refactorit.ui.module.where.WhereAction;
import net.sf.refactorit.ui.module.wherecaught.WhereCaughtAction;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;

import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;


/**
 * <p>Description: Constructs a map, what has a keystroke assosiated
 * with each action, what does have a shortcut.
 * Add here:
 *
 *     ActionToKeyStroke.put(KEY, KEYSTROKE)
 *
 * </p>
 * <p>Company: Aqris Software</p>
 * @author Jevgeni Holodkov
 * @version 1.0
 */

public class ShortcutKeyStrokes {
  private static HashMap actionToKeyStroke;
  static {
    // filling actionToKeyStroke Map. Skipping with null keystrokes.
    actionToKeyStroke = new HashMap();
    //actionToKeyStroke.put(AboutAction.KEY, null); // not shortCutAction
    //actionToKeyStroke.put(CleanAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(CrossHtmlAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(HelpAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(OptionsAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(ProjectOptionsAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(RebuildAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(RedoMilestoneAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(StandaloneBrowserAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(UndoMilestoneAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(UpdaterAction.KEY, null); // not ShortCutAction
    //actionToKeyStroke.put(GoToModuleAction.KEY, null); // does not implement getKeyStroke
    /*actionToKeyStroke.put(BackAction.KEY, null);
         actionToKeyStroke.put(CreateMilestoneAction.KEY, null);
         actionToKeyStroke.put(AddDelegatesAction.KEY, null);
         actionToKeyStroke.put(ApiDiffAction.KEY, null);
         actionToKeyStroke.put(ApiSnapshotAction.KEY, null);
         actionToKeyStroke.put(AstVisitorAction.KEY, null);
         actionToKeyStroke.put(ChangeMethodSignatureAction.KEY, null);
         actionToKeyStroke.put(ClassmodelVisitorAction.KEY, null);
         actionToKeyStroke.put(CleanImportsAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        Main.getShortcutKeyMask() | ActionEvent.SHIFT_MASK));
         actionToKeyStroke.put(ConvertTempToFieldAction.KEY, null);
         actionToKeyStroke.put(CreateConstructorAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_C,
        Main.getShortcutKeyMask() | ActionEvent.ALT_MASK));
         actionToKeyStroke.put(DuplicateLiteralsAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        Main.getShortcutKeyMask() | ActionEvent.SHIFT_MASK));
         actionToKeyStroke.put(EncapsulateFieldAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_N, Main.getShortcutKeyMask()));
         actionToKeyStroke.put(ExtractSuperAction.KEY,
     null);//return KeyStroke.getKeyStroke(KeyEvent.VK_XXX, Main.shortcutKeyMask)
         actionToKeyStroke.put(FactoryMethodAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_O, Main.getShortcutKeyMask()));
         actionToKeyStroke.put(ExplanationAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
        ;//KeyStroke.getKeyStroke(KeyEvent.VK_B, Main.getShortcutKeyMask())
         actionToKeyStroke.put(GoToTypeAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_B,
        Main.getShortcutKeyMask() | ActionEvent.SHIFT_MASK));
     actionToKeyStroke.put(InterAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
        Main.getShortcutKeyMask() | ActionEvent.SHIFT_MASK));
         actionToKeyStroke.put(MinimizeAccessAction.KEY, null);
         actionToKeyStroke.put(NotUsedAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_U, Main.getShortcutKeyMask()));
         actionToKeyStroke.put(OverrideMethodsAction.KEY, null);
         actionToKeyStroke.put(PullPushAction.KEY, null);
         actionToKeyStroke.put(ShellAction.KEY, null);
         actionToKeyStroke.put(SubtypesAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_A, Main.getShortcutKeyMask()));
         actionToKeyStroke.put(UseSuperTypeAction.KEY, null);
         actionToKeyStroke.put(FixmeScannerAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_F, Main.getShortcutKeyMask()));
         actionToKeyStroke.put(FindAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_F,
        Main.getShortcutKeyMask() | ActionEvent.SHIFT_MASK));*/
    actionToKeyStroke.put(UndoAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(RedoAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(AuditAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(CreateMissingMethodAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_B,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(ExtractMethodAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_X,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(InlineAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
    actionToKeyStroke.put(IntroduceTempAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_I,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(MoveAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M,
        ActionEvent.ALT_MASK));
    actionToKeyStroke.put(RenameAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        ActionEvent.ALT_MASK));
    actionToKeyStroke.put(CallTreeAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_V,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(DrawDependenciesAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_F,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(DependenciesAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_O,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(GotoAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(JavadocAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(TypeAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(WhereAction.KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
        ActionEvent.ALT_MASK));
    actionToKeyStroke.put(WhereCaughtAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_W,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
    actionToKeyStroke.put(MetricsAction.KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_E,
        ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
  }

  public static KeyStroke getByKey(Object o) {
    return (KeyStroke) actionToKeyStroke.get(o);
  }

}
