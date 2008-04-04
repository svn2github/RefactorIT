/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.jdeveloper.projectoptions;


import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sf.refactorit.jdeveloper.vfs.JDevSourceDir;
import net.sf.refactorit.ui.options.JIgnoredPathChooser;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;
import net.sf.refactorit.ui.projectoptions.PropertyPersistance;


/**
 * A ProjectProperty that represents ignored source path. It uses
 * JIgnoredPathChooser as it's editor, and is initialized and configured in
 * @see JDevProjectOptions
 * 
 * @author juri reinsalu
 */
public class IgnoredSourcePathProperty extends ProjectProperty {

  public IgnoredSourcePathProperty(PropertyPersistance persistance) {
    this.persistance = persistance;
  }

  /**
   * Name of this sourcepath property returned by <code>getTitle()</code>.
   * (the value is "RefactorIT.manual_sourcepath")
   *
   * @see #getTitle()
   */
  private static final String title = "RefactorIT.manual_sourcepath";

  /**
   * Semicolon separated paths as one String;
   */
  private String value;

  /**
   * UI editor for this sourcepath
   */
  private JIgnoredPathChooser editor;

  /**
   * Binds an instance of <code>JDevPathsPanel</code> as an ui editor to this
   * path property. That instance is later used by the following methods:
   * <code>loadChoiceToEditor, saveChoiceFromEditor</code>
   *
   */
  public void setEditor(JIgnoredPathChooser pathEditor) {
    this.editor = pathEditor;
  }


  /**
   * Returns the value of this <code>title</code> field which is equals
   * "RefactorIT.manual_sourcepath"
   *
   * @return the name of this implementation of <code>ProjectProperty</code>
   * @see #title
   */
  public String getTitle() {
    return null;
  }

  public javax.swing.JComponent getEditor() {
    return this.editor;
  }

  public void loadChoiceToEditor() {
    String sourcepathStr = this.persistance
        .get(ProjectConfiguration.PROP_IGNORED_SOURCEPATH);
    if(sourcepathStr==null) {
      this.editor.removeAllElements();
      return;
    }
    StringTokenizer st=new StringTokenizer(sourcepathStr,File.pathSeparator);
    ArrayList paths=new ArrayList(st.countTokens());
    while(st.hasMoreElements()) {
      paths.add(JDevSourceDir.getSource(new File(st.nextToken()),""));
    }
    this.editor.setContent(paths);
  }


  public void saveChoiceFromEditor() {
    this.persistance
    	.set(ProjectConfiguration.PROP_IGNORED_SOURCEPATH, editor.getPathString());
  }
  
  
}
