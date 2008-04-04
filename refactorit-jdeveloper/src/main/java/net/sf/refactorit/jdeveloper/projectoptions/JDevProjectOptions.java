/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.projectoptions;


import net.sf.refactorit.commonIDE.options.JavadocPathEditingPanel;
import net.sf.refactorit.ui.options.JIgnoredPathChooser;
import net.sf.refactorit.ui.options.SourceListRenderer;
import net.sf.refactorit.ui.options.TreeChooserSourceRenderer;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;
import net.sf.refactorit.ui.projectoptions.PropertyPersistance;
import oracle.ide.panels.Navigable;
import oracle.jdeveloper.model.JProjectConfiguration;

import javax.swing.tree.TreeModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <code>ProjectOptions</code> child that adds some specific properties
 * (ignored sourcepath) to general properties and delegates <code>set</code>
 * and <code>get</code> calls further to <code>ProjectConfiguration</code>
 * as to PropertyPersistance implementation which is IDE specific.
 * 
 * @author juri
 */
public class JDevProjectOptions extends ProjectOptions {
  private static JDevProjectOptions instance;
  
  /**
   * Manual sourcepath property
   */
  private IgnoredSourcePathProperty ignoredSourcepathProperty;
  
  private JavadocPathProperty javadocPathProperty;
  
  
  /**
   * Used in ProjectConfiguration.ConfigurationUI when sowed to user
   */
  private JIgnoredPathChooser ignoredSourcePathEditor;
  
  private JavadocPathEditingPanel javadocPathEditingPanel;
  
  /**
   * The need for this appeared when JDev-specific properties were
   * needed, but in the UI they needed to exist as a property group. ie
   * not in the default gridbag layouted two column panel as provided by
   * ProjectOptions (JURI R)
   */
  private List jDevSpecificPropertyList=new ArrayList();
  
  /**
   * this method is done to init static fields of this class.
   * inlined static inits do not suit since JDev loading files only on request
   */
  public static void init() {
    // looks like a hack, but it just creates a JDevProjectOptions instance.
    // Probably, it would be wise to separe gui creation and logic of options
    getInstance();
  }
  
  /** 
   * Should be a singleton because it should not register ProjectConfiguration more than once 
   */
  public static JDevProjectOptions getInstance() {
    if(instance == null) {
      instance = new JDevProjectOptions();
    }
    return instance;
  }
  
  private JDevProjectOptions() {
    super();
    JProjectConfiguration.registerConfigData(
        ProjectConfiguration.DATA_KEY, ProjectConfiguration.class);

    JProjectConfiguration.registerConfigUI(
        new Navigable("RefactorIT", ConfigurationUI.class));
    TreeModel treeSourceModel=new JDevProjectSourcepathsTreeModel();
    ignoredSourcePathEditor = new JIgnoredPathChooser(
            treeSourceModel, new TreeChooserSourceRenderer(treeSourceModel));
//    ignoredSourcePathEditor.setBorder(new TitledBorder("ignored sourcepath"));
    ignoredSourcePathEditor.setBorder(null);
    ignoredSourcePathEditor.setCellRenderer(new SourceListRenderer());
        
    ignoredSourcepathProperty=new IgnoredSourcePathProperty(this);
    ignoredSourcepathProperty.setEditor(ignoredSourcePathEditor);
    this.jDevSpecificPropertyList.add(ignoredSourcepathProperty);
    
    javadocPathEditingPanel = new JavadocPathEditingPanel();
    javadocPathEditingPanel.setBorder(null);
    
    javadocPathProperty = new JavadocPathProperty(this);
    javadocPathProperty.setEditor(javadocPathEditingPanel);
    this.jDevSpecificPropertyList.add(javadocPathProperty);
    
  }
  
  /**
   * This is overriding the same method in ProjectOptions so that additional properties from 
   * this <code>jDevSpecificPropertyList</code> would also be notified
   * The functionality of the super method is retained (via a call to it)
   */
  public void loadChoicesToPropertyEditors(PropertyPersistance persistance){
    super.loadChoicesToPropertyEditors(persistance);
    for( Iterator i = this.jDevSpecificPropertyList.iterator(); i.hasNext(); ) {
      ProjectProperty eachProperty = (ProjectProperty)i.next();
      eachProperty.loadChoiceToEditor(persistance); 
    }
  }
  
  /**
   * This is overriding the one in ProjectOptions so that additional properties from 
   * this <code>jDevSpecificPropertyList</code> would also be notified
   * The functionality of the super method is retained (via a call to it)
   */
  public void saveChoicesFromPropertyEditors(PropertyPersistance persistance){
    super.saveChoicesFromPropertyEditors(persistance);
    for( Iterator i = this.jDevSpecificPropertyList.iterator(); i.hasNext(); ) {
      ProjectProperty eachProperty = (ProjectProperty)i.next();
      eachProperty.saveChoiceFromEditor(persistance);
    }
  }
  
  public void set(String propertyName, String value) {
    ProjectConfiguration.getActiveInstance().set( propertyName, value );
  }
  
  public String get(String propertyName) {
    return ProjectConfiguration.getActiveInstance().get( propertyName );
  }  
  
  protected List getPropertyList(){
    return this.propertyList;
  }
  
  public JIgnoredPathChooser getIgnoredPathsEditor(){
    return this.ignoredSourcePathEditor;
  }
  
  public JavadocPathEditingPanel getJavadocPathEditingPanel(){
    return this.javadocPathEditingPanel;
  }
  
}
