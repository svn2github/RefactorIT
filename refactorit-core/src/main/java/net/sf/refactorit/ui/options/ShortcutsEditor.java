/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.ui.Shortcuts;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 *
 * @author Vladislav Vislogubov
 */
public class ShortcutsEditor extends DefaultCellEditor {
  private static final JTextField dummyTextField = new JTextField();

  //private String stroke;

  private Shortcut shortcut;

  private int currentKeyCode;
  private int currentModifiers;
  private boolean changed = false;

  private Shortcuts tab;

  public ShortcutsEditor(OptionsTab tab) {
    // we have no condtructor for JButton
    super(dummyTextField);

    this.tab = (Shortcuts) tab;

    setClickCountToStart(2);

    dummyTextField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        shortcutFieldKeyPressed(evt);
      }

      public void keyReleased(KeyEvent evt) {
        shortcutFieldKeyReleased(evt);
      }

      public void keyTyped(KeyEvent evt) {
        shortcutFieldKeyTyped(evt);
      }
    });
  }

  public Object getCellEditorValue() {
    if (changed) {
      changed = false;

      if (dummyTextField.getText().length() == 0) {
        return new Shortcut(null, "", "");
      } else {
        return new Shortcut(
            KeyStroke.getKeyStroke(currentKeyCode, currentModifiers));
      }
    }

    return shortcut;
  }

  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column
      ) {
    shortcut = (Shortcut) value;
    dummyTextField.setText(shortcut.getDispayableName());
    changed = false;

    return editorComponent;
  }

  void shortcutFieldKeyReleased(KeyEvent evt) {
    evt.consume();

    if (currentKeyCode == KeyEvent.VK_BACK_SPACE && currentModifiers == 0) {
      changed = true;
      dummyTextField.setText("");
    } else if (specialShorcutKeyPressed()) {
      // Not finished entering key
      changed = false;
      String name = null;
      if (shortcut != null) {
        name = shortcut.getDispayableName();
      }
      dummyTextField.setText((name == null) ? "" : name);
    } else {
      if (tab.isBusy(KeyStroke.getKeyStroke(currentKeyCode, currentModifiers))) {
        /*
                 JOptionPane.showMessageDialog( dummyTextField,
            "This shortcut is already in use by other action",
            "Shortcut Warning",
            JOptionPane.WARNING_MESSAGE );
         */

        currentKeyCode = KeyEvent.VK_ALT;
        changed = false;
        String text = "";
        if (shortcut != null) {
          text = shortcut.getDispayableName();
        }
        dummyTextField.setText(text);
      } else {
        changed = true;
      }
    }
  }

  private boolean specialShorcutKeyPressed() {
    return (currentKeyCode == KeyEvent.VK_ALT)
        || (currentKeyCode == KeyEvent.VK_ALT_GRAPH)
        || (currentKeyCode == KeyEvent.VK_CONTROL)
        || (currentKeyCode == KeyEvent.VK_SHIFT)
        || (currentKeyCode == ActionEvent.CTRL_MASK);
  }

  void shortcutFieldKeyTyped(KeyEvent evt) {
    evt.consume();
  }

  void shortcutFieldKeyPressed(KeyEvent evt) {
    evt.consume();

    currentKeyCode = evt.getKeyCode();
    currentModifiers = evt.getModifiers();

    String text2 = KeyEvent.getKeyModifiersText(currentModifiers);

    if (text2.length() == 0) {
      if (currentKeyCode == KeyEvent.VK_BACK_SPACE) {
        text2 = "";
      } else {
        text2 = KeyEvent.getKeyText(currentKeyCode);
      }
    } else {
      String keyText = KeyEvent.getKeyText(currentKeyCode);
      if (text2.indexOf(keyText) < 0) {
        text2 += "+" + keyText;
      } else {
        text2 += "+";
      }
    }

    dummyTextField.setText(text2);
  }
}
