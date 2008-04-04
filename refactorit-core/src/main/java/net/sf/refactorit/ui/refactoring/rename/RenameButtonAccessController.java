/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;


import net.sf.refactorit.refactorings.NameUtil;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


public class RenameButtonAccessController implements DocumentListener {
  private JButton button = null;
  private String initialName = null;

  public RenameButtonAccessController(JButton button) {
    setButton(button);
  }

  public void setInitialName(String initialName) {
    this.initialName = initialName;
  }

  public void changedUpdate(DocumentEvent event) {
    adjustButton(event);
  }

  public void insertUpdate(DocumentEvent event) {
    adjustButton(event);
  }

  public void removeUpdate(DocumentEvent event) {
    adjustButton(event);
  }

  /**
   *
   */
  private void adjustButton(DocumentEvent event) {
    Document document = event.getDocument();

    // Adjust button
    try {
      String newName = document.getText(0, document.getLength());
      getButton().setEnabled(
          !newName.equals(this.initialName)
          && NameUtil.isValidIdentifier(newName));
    } catch (BadLocationException ble) {
    }
  }

  private JButton getButton() {
    return this.button;
  }

  private void setButton(JButton button) {
    this.button = button;
  }

}
