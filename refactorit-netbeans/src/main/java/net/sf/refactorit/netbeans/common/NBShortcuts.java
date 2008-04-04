/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.Shortcuts;
import net.sf.refactorit.ui.module.RefactorItAction;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import java.awt.event.ActionEvent;


/**
 * Defines RefactorIT actions and binds it to a keystroke in
 * Source Editor window.
 *
 * @author Vladislav Vislogubov
 */
public class NBShortcuts extends Shortcuts {
  private static final Logger log = Logger.getLogger(NBShortcuts.class);
  
  private Keymap keymap =  (Keymap)Lookup.getDefault().lookup(Keymap.class);

  public NBShortcuts() {
    super(".netbeans");

    registerAll();
  }

  /**
   * @see Shortcuts#isBusy(KeyStroke)
   */
  public boolean isBusy(KeyStroke stroke) {
    //Keymap keymap = TopManager.getDefault().getGlobalKeymap();
    if (keymap == null) {
      return true;
    }

    return keymap.isLocallyDefined(stroke);
  }

  /**
   * @see Shortcuts#register(RefactorItAction, String, KeyStroke)
   */
  public void register(
      ShortcutAction action,
      String actionKey,
      KeyStroke stroke) {
    //Keymap keymap = TopManager.getDefault().getGlobalKeymap();
//        if ( keymap != null ) {
//            //System.out.println( "registering stroke: " + stroke );
//
//            keymap.addActionForKeyStroke( stroke,
//                new RITAction( action, actionKey) );
//        }
  }

  /**
   * @see Shortcuts#unregister(KeyStroke)
   */
  public void unregister(KeyStroke stroke) {
    //Keymap keymap = TopManager.getDefault().getGlobalKeymap();
    if (keymap != null) {
      keymap.removeKeyStrokeBinding(stroke);
    }
  }

  public class RITAction extends AbstractAction {
    private RefactorItAction action;
    private String key;

    public RITAction(RefactorItAction action, String actionKey) {
      super();

      this.action = action;
      this.key = actionKey;
      log.debug("Created action: " + action.getName() + ", key=" + key);
    }

    public void actionPerformed(ActionEvent e) {
      log.warn("actionPerformed() not overriden:");
      log.warn("*** " + key + " ***");
      log.warn("--- " + action.getName() + " ---");
      log.warn("--- " + e + " ---");
    }
  }


  public interface ActionKeyProvider {
    String getActionKey();
  }
}
