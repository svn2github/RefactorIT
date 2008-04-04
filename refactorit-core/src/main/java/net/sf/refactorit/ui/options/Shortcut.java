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

import javax.swing.KeyStroke;

import java.awt.event.KeyEvent;


/**
 * Serves as Shortcut.class for the recognizing of class type in Options model.
 *
 * @author Vladislav Vislogubov
 */
public class Shortcut {
  private String strokeText;
  private String name;
  private KeyStroke stroke;

  public Shortcut(KeyStroke stroke) {
    this.stroke = stroke;
  }

  public Shortcut(KeyStroke stroke, String strokeText) {
    this.stroke = stroke;
    this.strokeText = strokeText;
  }

  public Shortcut(KeyStroke stroke, String strokeText, String name) {
    this.stroke = stroke;
    this.strokeText = strokeText;
    this.name = name;
  }

  public KeyStroke getStroke() {
    return stroke;
  }

  public String getDispayableName() {
    if (stroke == null) {
      name = "";
    } else if (name == null) {
      name = KeyEvent.getKeyModifiersText(stroke.getModifiers());
      if (name.length() == 0) {
        name = KeyEvent.getKeyText(stroke.getKeyCode());
      } else {
        name += "+" + KeyEvent.getKeyText(stroke.getKeyCode());
      }
    }

    return name;
  }

  public String getStrokeText() {
    if (strokeText == null) {
      strokeText = Shortcuts.getTextFromKeyStroke(stroke.getKeyCode(),
          stroke.getModifiers());
    }

    return strokeText;
  }

  public String toString() {
    return getDispayableName();
  }
}
