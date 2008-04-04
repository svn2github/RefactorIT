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
import com.borland.primetime.ide.BrowserPropertyGroup;
import com.borland.primetime.ide.KeymapManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.ui.module.ActionProxy;


public class RefactorItShortcuts implements PropertyChangeListener {

  public static void initOpenTool(byte major, byte minor) {
    RefactorItTool.ensureControllerInit();
    EditorManager.addPropertyChangeListener(new RefactorItShortcuts());
    updateShortcuts();
  }

  public void propertyChange(PropertyChangeEvent e) {
    String propertyName = e.getPropertyName();

    // We are only interested in keymap changes
    if (propertyName.equals(EditorManager.keymapAttribute)) {
      updateShortcuts();
    }
  }

  private static void updateShortcuts() {
    boolean changed = false;
    Keymap keymap = KeymapManager.getKeymap(EditorManager.getKeymapName()); // We need a current keymap to change
    if (keymap == null) {
      return;
    }
    ActionRepository repository = ActionRepository.getInstance();
    Collection actions = repository.getShortcutActions();

    for (Iterator i = actions.iterator(); i.hasNext(); ) {
      Action item = (Action) i.next();
      KeyStroke[] strokeArr = keymap.getKeyStrokesForAction(item);
      if (strokeArr == null || strokeArr.length < 1) {
        if (item instanceof JBRefactoritAction) {
          ActionProxy rAction = ((JBRefactoritAction) item).getAction();
          if (rAction instanceof ShortcutAction) {
            KeyStroke stroke = ((ShortcutAction) rAction).getKeyStroke();
            if (stroke != null) {
              keymap.addActionForKeyStroke(stroke, item); // Change the keymap..
              changed = true;
            }
          }
        }
      }
    }

    if (changed) {
      try {
        BrowserPropertyGroup.updateKeymap();
      } catch (Exception e) {
        AppRegistry.getLogger(RefactorItShortcuts.class).warn(
            "Can not update Shortcuts KeyMap under "
            + IDEController.getInstance().getIdeName() + " "
            + IDEController.getInstance().getIdeBuild());
      }
    }
  }


}
