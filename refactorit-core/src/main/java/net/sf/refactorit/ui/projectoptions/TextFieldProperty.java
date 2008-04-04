/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.projectoptions;


import net.sf.refactorit.common.util.ResourceUtil;

import javax.swing.JComponent;
import javax.swing.JTextField;

import java.util.ResourceBundle;


/**
 * Simple text property with a JTextField editor.
 *
 * @author  tanel
 */
public class TextFieldProperty extends ProjectProperty {
  static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(ProjectOptions.class);

  private String propertyName;

  private JTextField textField;

  public TextFieldProperty(String propertyName, PropertyPersistance persistance) {
    this.propertyName = propertyName;
    this.persistance = persistance;
  }

  public void setText(String text) {
    this.persistance.set(this.propertyName, text);
  }

  public String getText() {
    return persistance.get(propertyName);
  }

  public JComponent getEditor() {
    loadChoiceToEditor();
    return this.textField;
  }

  public String getTitle() {
    return resLocalizedStrings.getString(this.propertyName);
  }

  protected void createEditor() {
    this.textField = new JTextField();
    textField.setColumns(20);
  }

  public boolean editorModified() {
    return!(this.textField.getText().equals(getText()));
  }

  public void saveChoiceFromEditor() {
    if (this.textField == null) {
      createEditor();
    }
    setText(textField.getText());
  }

  public void loadChoiceToEditor() {
    if (this.textField == null) {
      createEditor();
    }
    textField.setText(getText());
  }

  protected JTextField getTextField() {
    return this.textField;
  }
}
