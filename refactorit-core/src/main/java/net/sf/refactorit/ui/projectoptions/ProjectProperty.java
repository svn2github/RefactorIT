/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;

import javax.swing.JComponent;


/**
 * Properties should save and load themselves through a PropertyPersistance instance
 * (a protected field of this object). <br><br>
 *
 * Properties *must* automatically save their values whenever they change
 * and automatically load their values whenever they are needed (IOW -- they must *not* remember
 * or cache their own values, they *must* keep them in PropertyPersistance).
 * This is neccessary because PropertyPersistance instances might change often. <br><br>
 *
 * Values of properties can be changed through their editors (that they provide)
 * and through their custom setXXX methods (that interface is not part of this abstract class).
 */
public abstract class ProjectProperty {
  protected PropertyPersistance persistance;

  /** Title for users (i18n'ed) */
  public abstract String getTitle();

  /**
   * The value chosen in the editor is *not* automatically visible (and *not* saved) in the property.
   * To copy chosen values from editors to properties, call ProjectOptions.saveChoicesFromPropertyEditors().
   * This extra step is needed to allow canceling of edits. <br><br>
   *
   * The editor does *not* contain a label, it's a simple checkbox/combobox/etc.
   *
   * @return  the same property editor instance every time.
   */
  public abstract JComponent getEditor();

  /** Updates the property from its editor */
  public abstract void saveChoiceFromEditor();

  /** Updates the editor from its property */
  public abstract void loadChoiceToEditor();

  public void saveChoiceFromEditor(PropertyPersistance customPersistance) {
    PropertyPersistance oldPersistance = this.persistance;
    this.persistance = customPersistance;
    saveChoiceFromEditor();
    this.persistance = oldPersistance;
  }

  public void loadChoiceToEditor(PropertyPersistance customPersistance) {
    PropertyPersistance oldPersistance = this.persistance;
    this.persistance = customPersistance;
    loadChoiceToEditor();
    this.persistance = oldPersistance;
  }

  /**
   * @return false if no changes have been made; otherwise _might_ return true,
   * but does not need to. This is used to optimize code -- we might not need
   * to reload project, for example, if no properties have been changed, etc.
   */
  boolean editorModified() {
    return true;
  }
}
