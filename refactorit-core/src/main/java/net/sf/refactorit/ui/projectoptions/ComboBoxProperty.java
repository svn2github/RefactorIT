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

import javax.swing.JComboBox;
import javax.swing.JComponent;

import java.util.ResourceBundle;


/**
 * Combo box property editor (storage + GUI).
 */
public class ComboBoxProperty extends ProjectProperty {
  static final ResourceBundle resLocalizedStrings = ResourceUtil.getBundle(
      ProjectOptions.class);

  private static final int DEFAULT_CHOICE = 0;

  private String propertyName;
  private ComboBoxPropertyOption[] options;

  private int selectedIndex = DEFAULT_CHOICE;

  private JComboBox editor;

  /**
   * @param propertyName  used for 2 things: getting the title of combo box from resource bundle,
   *                      and as a key when saving/restoring property value.
   * @param	options				the default choice is the first array item (it's used when storage getter gives 'null').
   */
  public ComboBoxProperty(String propertyName, ComboBoxPropertyOption[] options,
      PropertyPersistance persistance) {
    this.propertyName = propertyName;
    this.options = options;
    this.persistance = persistance;
  }

  private int getSelectedIndex() {
    load();
    return this.selectedIndex;
  }

  private void setSelectedIndex(int i) {
    this.selectedIndex = i;
    save();
  }

  public String getSelectedValue() {
    return options[getSelectedIndex()].getValue();
  }

  public void setSelectedValue(String newSelectedValue) {
    for (int i = 0; i < this.options.length; i++) {
      if (this.options[i].getValue().equals(newSelectedValue)) {
        setSelectedIndex(i);
        return;
      }
    }

    throw new IllegalArgumentException("Value \"" + newSelectedValue
        + "\" is illegal for this property (\"" + newSelectedValue + "\")");
  }

  public int getSelectedValueInt() {
    try {
      return Integer.parseInt(getSelectedValue());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Value is not a number: \""
          + getSelectedValue() + "\"");
    }
  }

  public void setSelectedValueInt(int x) {
    setSelectedValue("" + x);
  }

  public boolean getSelectedValueBoolean() {
    return getSelectedValue().equals("true");
  }

  public void setSelectedValueBoolean(boolean x) {
    setSelectedValue(x ? "true" : "false");
  }

  private void load() {
    String stringForm = persistance.get(propertyName);

    if (stringForm == null || "".equals(stringForm)) {
      setSelectedIndex(DEFAULT_CHOICE);
      return;
    }

    setSelectedValue(stringForm);
  }

  private void save() {
    this.persistance.set(this.propertyName,
        "" + this.options[this.selectedIndex].getValue());
  }

  public JComponent getEditor() {
    loadChoiceToEditor();
    return this.editor;
  }

  public String getTitle() {
    return resLocalizedStrings.getString(this.propertyName);
  }

  private void createEditor() {
    this.editor = new JComboBox();

    for (int i = 0; i < this.options.length; i++) {
      this.editor.addItem(resLocalizedStrings.getString(this.options[i].getName())
          + " ");
    }
  }

  public boolean editorModified() {
    return!(""
        + this.options[this.editor.getSelectedIndex()].getValue()).equals(
        this.persistance.get(this.propertyName));
  }

  public void saveChoiceFromEditor() {
    if (this.editor == null) {
      createEditor();
    }

    setSelectedIndex(this.editor.getSelectedIndex());
  }

  public void loadChoiceToEditor() {
    if (this.editor == null) {
      createEditor();
    }

    this.editor.setSelectedIndex(getSelectedIndex());
  }
}
