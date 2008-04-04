/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.Shortcuts;
import net.sf.refactorit.ui.module.RefactorItAction;

import javax.swing.KeyStroke;

import java.util.LinkedList;


/**
 * Defines RefactorIT standalone shortcuts.
 *
 * @author Vladislav Vislogubov
 */
public class StandaloneShortcuts extends Shortcuts {
  private LinkedList strokes = new LinkedList();

  public StandaloneShortcuts() {
    super(".standalone");

    registerAll();
  }

  /**
   * @see Shortcuts#isBusy(KeyStroke)
   */
  public boolean isBusy(KeyStroke stroke) {
    return strokes.contains(stroke);
  }

  /**
   * @see Shortcuts#register(RefactorItAction, String, KeyStroke)
   */
  public void register(
      ShortcutAction action, String actionKey, KeyStroke stroke) {
    if (!strokes.contains(stroke)) {
      strokes.add(stroke);
    }
  }

  /**
   * @see Shortcuts#unregister(KeyStroke)
   */
  public void unregister(KeyStroke stroke) {
    strokes.remove(stroke);
  }
}
