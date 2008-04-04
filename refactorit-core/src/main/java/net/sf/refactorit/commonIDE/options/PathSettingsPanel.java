/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Container panel for path settings
 */
public class PathSettingsPanel extends JPanel {
  private SourcePathEditingPanel sourcepathPanel;
  private ClassPathEditingPanel classpathPanel;
  private SourcePathEditingPanel ignoredSourcepathPanel;
  private JavadocPathEditingPanel javadocPanel;
  private ProjectDependenciesPanel projectDependenciesPanel;

  private MethodPanel method;

  JRadioButton autodetect = new JRadioButton("Detect classpath and sourcepath");
  JRadioButton manual = new JRadioButton("Specify classpath and sourcepath:");

  boolean canAutoDetect;
  private ProjectOptions options;

  public PathSettingsPanel(ProjectOptions options) {
    super(new BorderLayout());

    this.canAutoDetect = options.canAutoDetect();

    this.sourcepathPanel = new SourcePathEditingPanel(true);
    this.classpathPanel = new ClassPathEditingPanel();
    this.ignoredSourcepathPanel = new SourcePathEditingPanel(false);
    this.javadocPanel = new JavadocPathEditingPanel();
    this.projectDependenciesPanel = new ProjectDependenciesPanel();

    this.method = new MethodPanel();
    JComponent pathsPanel = createTabbedPathsPanel();

    add(method, BorderLayout.NORTH);
    add(pathsPanel);

    addListeners();
    setProjectOptions(options);
    //this.setProjectSettings(projectSettings);
  }

  public void updateSettings() {
   options.setAutoDetect(this.isAutoDetected());
   options.setSourcePath(new Path(sourcepathPanel.getPathItems()));
   options.setClassPath(new Path(classpathPanel.getPathItems()));
   options.setIgnoredSourcePath(new Path(ignoredSourcepathPanel.getPathItems()));
   options.setJavadocPath(new Path(javadocPanel.getPathItems()));
   options.saveChoicesFromPropertyEditors();
   options.setDependencies(new Path(projectDependenciesPanel.getSelectedProjects()));
  }

  private JComponent createTabbedPathsPanel() {
    JTabbedPane result = new JTabbedPane();
    result.addTab("Sourcepath", sourcepathPanel);
    result.addTab("Ignored Sourcepath", ignoredSourcepathPanel);
    result.addTab("Classpath", classpathPanel);
    result.addTab("Javadoc",javadocPanel);
    if(IDEController.runningEclipse()) {
      // eclipse the only one supports project dependencies, right?
      result.addTab("Project Dependencies",projectDependenciesPanel);
    }
    return result;
  }

  void onSpecify() {
    this.manual.setSelected(true);
    sourcepathPanel.setEnabled(true);
    ignoredSourcepathPanel.setEnabled(true);
    classpathPanel.setEnabled(true);
    javadocPanel.setEnabled(true);

    if (!options.isAutoDetect()) {
      sourcepathPanel.setContents(options.getSourcePath().toPathItems());
      classpathPanel.setContents(options.getClassPath().toPathItems());

      ignoredSourcepathPanel.setContents(
          options.getIgnoredSourcePath().toPathItems());
      javadocPanel.setContents(options.getJavadocPath().toPathItems());
    }
  }

  public boolean isAutoDetected() {
    return this.autodetect.isSelected();
  }

  void onAutoDetect() {
    this.autodetect.setSelected(true);
    sourcepathPanel.setEnabled(false);
    ignoredSourcepathPanel.setEnabled(false);
    classpathPanel.setEnabled(false);

    classpathPanel.setAutoDetect();
    sourcepathPanel.setAutoDetect();
    javadocPanel.setAutoDetect();
    javadocPanel.setEnabled(false);

    Project project = IDEController.getInstance().getActiveProject();

    ClassPath classPath = project.getPaths().getClassPath();
    classPath.release();

    // TODO: what does this ClassPath in the Project mean??
    classpathPanel.setContents(
        createPathItemList(classPath.getAutodetectedElements()));

    sourcepathPanel.setContents(
        createPathItemList(project.getPaths().getSourcePath().getAutodetectedElements()));

    javadocPanel.setContents(
        createPathItemList(project.getPaths().getJavadocPath().getElements()));
  }

  private static List createPathItemList(Source[] elements) {
    List result = new ArrayList(elements.length);

    for (int i = 0; i < elements.length; i++) {
      PathItem pathItem;

      pathItem = new PathItem(elements[i]);
      result.add(pathItem);
    }

    return result;
  }
  private static List createPathItemList(String[] elements) {
    List result = new ArrayList(elements.length);

    for (int i = 0; i < elements.length; i++) {
      PathItem pathItem;

      pathItem = new PathItem(elements[i]);
      result.add(pathItem);
    }

    return result;
  }

  private static List createPathItemList(ClassPathElement[] elements) {
    List result = new ArrayList(elements.length);

    for (int i = 0; i < elements.length; i++) {
      PathItem pathItem;

      pathItem = new PathItem(elements[i]);
      result.add(pathItem);
    }

    return result;
  }


  private void addListeners() {
    manual.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onSpecify();
      }
    });

    autodetect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onAutoDetect();
      }
    });
  }

  class MethodPanel extends JPanel {
    public MethodPanel() {
      this.setLayout(new BorderLayout());

      ButtonGroup group = new ButtonGroup();
      JRadioButton[] buttons = null;
      if (canAutoDetect) {
        buttons = new JRadioButton[2];
        buttons[0] = autodetect;
        buttons[1] = manual;
      } else {
        buttons = new JRadioButton[1];
        buttons[0] = manual;
        buttons[0].setVisible(false);
      }
      for (int i = 0; i < buttons.length; i++) {
        group.add(buttons[i]);
      }

//      group.setSelected(buttons[0].getModel(),true);
//      group.add(manual);

      this.add(SwingUtil.combineInNorth(buttons));
//      this.add(SwingUtil.combineInNorth(autodetect, manual));
    }
  }

  public void setProjectOptions(final ProjectOptions options) {
    this.options = options;

    if (this.canAutoDetect && options.isAutoDetect()) {
      onAutoDetect();
    } else {
      onSpecify();
    }
  }
}
