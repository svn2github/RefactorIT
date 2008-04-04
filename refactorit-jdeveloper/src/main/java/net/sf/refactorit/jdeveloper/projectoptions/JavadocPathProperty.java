/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.projectoptions;

import java.util.List;

import javax.swing.JComponent;

import net.sf.refactorit.commonIDE.options.JavadocPathEditingPanel;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;
import net.sf.refactorit.ui.projectoptions.PropertyPersistance;


public class JavadocPathProperty extends ProjectProperty {

  public JavadocPathProperty(PropertyPersistance persistance) {
    this.persistance = persistance;
  }

  private static final String title = "RefactorIT.manual_javadoc";
  
  /**
   * UI editor for javadoc path
   */
  private JavadocPathEditingPanel editor;
  
  public String getTitle() {
    return title;
  }


  public JComponent getEditor() {
    return this.editor;
  }

  /**
   * Binds an instance of <code>JDevPathsPanel</code> as an ui editor to this
   * path property. That instance is later used by the following methods:
   * <code>loadChoiceToEditor, saveChoiceFromEditor</code>
   *
   */
  public void setEditor(JavadocPathEditingPanel pathEditor) {
    this.editor = pathEditor;
  }

  public void saveChoiceFromEditor() {
    this.persistance.set(ProjectConfiguration.PROP_JAVADOCPATH,
        new Path(editor.getPathItems()).toString());
  }

  public void loadChoiceToEditor() {
    String javadocPath = this.persistance
    .get(ProjectConfiguration.PROP_JAVADOCPATH);
	if (javadocPath == null) {
      this.editor.removeAllElement();
      return;
    }
	
	List items = new Path(javadocPath).toPathItems();
	this.editor.setContents(items);
  }

}
