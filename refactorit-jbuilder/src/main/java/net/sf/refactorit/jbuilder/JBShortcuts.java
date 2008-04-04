/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.primetime.editor.EditorManager;

import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.Shortcuts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Defines RefactorIT actions and binds it to a keystroke in
 * Source Editor window.
 *
 * @author Vladislav Vislogubov
 */
public class JBShortcuts extends Shortcuts implements PropertyChangeListener {
  public JBShortcuts() {
    super(".jbuilder");
  }

  /**
   * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent e) {
    String propertyName = e.getPropertyName();

    if (propertyName.equals(EditorManager.keymapAttribute)) {
      registerAll();
      /*
       * FIXME here must be also binding of some static actions like
       * show RefactorIT Browser and e.g.
       */
    }
  }

  public boolean isBusy(KeyStroke stroke) {
    Keymap keymap = EditorManager.getKeymap();
    if (keymap == null) {
      return true;
    }

    return keymap.isLocallyDefined(stroke);
  }

  public void register(
      ShortcutAction action, String actionKey, KeyStroke stroke
      ) {
    if (stroke == null) {
      return;
    }

    Keymap keymap = EditorManager.getKeymap();
    if (keymap == null) {
      return;
    }

    keymap.addActionForKeyStroke(stroke, (JBRefactoritAction)
        ActionRepository.getInstance().getAction(action.getKey())
        /*new RITAction( action, actionKey )*/);
  }

  public void unregister(KeyStroke stroke) {
    if (stroke == null) {
      return;
    }

    Keymap keymap = EditorManager.getKeymap();
    if (keymap == null) {
      return;
    }

    keymap.removeKeyStrokeBinding(stroke);
  }

  //    public class RITAction extends EditorAction {
  //        private RefactorItAction action;
  //        private String key;
  //
  //        public RITAction( RefactorItAction action, String actionKey ) {
  //            super( "someRitAction" );
  //
  //            this.action = action;
  //            this.key = actionKey;
  //            //System.out.println( "Created action: " + action.getName() + ", key="+key );
  //        }
  //
  //        public void actionPerformed(ActionEvent e) {
  //            //System.out.println( "*** "+key+" ***" );
  //            EditorPane target = getEditorTarget(e);
  //            if (target == null) return;
  //
  //            RefactorItTool.startRefactorITModules( Browser.getActiveBrowser(),
  //                action, null, target );
  //        }
  //    }
}
