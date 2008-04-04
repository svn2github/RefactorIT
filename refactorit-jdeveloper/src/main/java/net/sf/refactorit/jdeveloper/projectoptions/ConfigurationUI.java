/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.projectoptions;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.options.JavadocPathEditingPanel;
import net.sf.refactorit.ui.options.JIgnoredPathChooser;
import oracle.ide.panels.Traversable;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;
import oracle.jdeveloper.model.JProjectConfiguration;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.util.Map;


	/**
   * Provides UI to edit all RefactorIT project options.
   **/
  public class ConfigurationUI extends JPanel implements Traversable  {
    private JDevProjectOptions options = JDevProjectOptions.getInstance();


    public ConfigurationUI() {
      setLayout(new BorderLayout());
      removeAll();
      JPanel contents = new JPanel();      
      contents.setLayout(new BorderLayout());
      contents.add(options.getCommonPropertiesInOnePanel(),
          BorderLayout.CENTER);
      JIgnoredPathChooser ignoredPathsEditor = options.getIgnoredPathsEditor(); 
      JavadocPathEditingPanel javadocPathEditingPanel = options.getJavadocPathEditingPanel();
      
      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab("Ignored path", ignoredPathsEditor);
      tabbedPane.addTab("Javadoc", javadocPathEditingPanel);
      
      add(tabbedPane);
      add(contents,BorderLayout.SOUTH);
    }
    
    public java.awt.Component getComponent() {
      return this;
    }
    
    public void onEntry(TraversableContext dataContext) {
      // Load current values to UI
      IDEController.getInstance().ensureProjectWithoutParsing();

      final ProjectConfiguration editableConf =
          ProjectConfiguration.getProjectConfiguration(dataContext);

      if (editableConf == null) {
        return;
      }
      
      options.loadChoicesToPropertyEditors(editableConf);
    }

    public void onExit(TraversableContext dataContext) throws TraversalException {
      // Save values from UI
      ProjectConfiguration editableConf = new ProjectConfiguration();
      
      options.saveChoicesFromPropertyEditors(editableConf);
      
      JProjectConfiguration jdevConf = (JProjectConfiguration) 
          dataContext.find(JProjectConfiguration.DATA_KEY);

      Map configMap = jdevConf.getConfigData();
      configMap.put(ProjectConfiguration.DATA_KEY, editableConf);

      jdevConf.setConfigData(configMap);

      dataContext.findAndReplace(JProjectConfiguration.DATA_KEY, jdevConf);
    }    
    
    public Object getExitTransition() {
      return null;
    }
    
   
    public String getHelpID() {
      return "";
    }
  }
