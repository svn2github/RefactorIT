/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import com.borland.primetime.editor.EditorManager;
import com.borland.primetime.help.HelpTopic;
import com.borland.primetime.properties.PropertyPage;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.options.JavadocPathEditingPanel;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.jbuilder.JavaFileNodeRe;
import net.sf.refactorit.jbuilder.RefactorItPropGroup;
import net.sf.refactorit.jbuilder.vfs.JBClassPath;
import net.sf.refactorit.jbuilder.vfs.JBSource;
import net.sf.refactorit.jbuilder.vfs.JBSourcePath;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.options.*;
import net.sf.refactorit.ui.projectoptions.CommonOptionsPanel;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Create RefactorIT property page tab.
 *
 * @author Vladislav Vislogubov
 * @author Juri Reinsalu (updates and comments)
 */
public class RefactorItPropPage extends PropertyPage {
  private JComboBox braceBox = new JComboBox(new Object[] {"Yes", "No"});
  private JComboBox productivityCompMode = new JComboBox(new Object[] {"Yes",
      "No"});

  /**
   * as the items put into JPathChooser in the <code>JDevSourcepathChooser</code>
   * implementation are of type Source, then the propper renderer is provided.
   */
  private SourceListRenderer sourceListRenderer = new SourceListRenderer();
  private JSourcepathChooser sourcepathChooser = new JBSourcepathChooser();
  private JavadocPathEditingPanel javadocPathEditingPanel = new JavadocPathEditingPanel();

  private IgnorablePathsTreeModel ignoredSourcepathChooserModel
      = new IgnorablePathsTreeModel(sourcepathChooser);
  private JIgnoredPathChooser ignoredSourcepathChooser = new
      JIgnoredPathChooser(
      ignoredSourcepathChooserModel, new SourceTreeCellRenderer(
      ignoredSourcepathChooserModel));

  private JClasspathChooser classpathChooser = new JBClasspathChooser();

  {
    sourcepathChooser.setBorder(null);
    classpathChooser.setBorder(null);
    javadocPathEditingPanel.setBorder(null);
    ignoredSourcepathChooser.setBorder(null);

    sourcepathChooser.setCellRenderer(sourceListRenderer);
    ignoredSourcepathChooser.setCellRenderer(sourceListRenderer);
  }

  private PathEditPanel pathsPanel = new PathEditPanel(this, sourcepathChooser,
      ignoredSourcepathChooser, classpathChooser, javadocPathEditingPanel);
  private ProjectOptions options;

  public RefactorItPropPage(ProjectOptions options) {
    this.options=options;
    init();
  }

  private void init() {
    setLayout(new BorderLayout());
    CommonOptionsPanel propertiesPane = options
        .getCommonPropertiesInOnePanel();

    if (!JavaFileNodeRe.isNewStructureViewPopupApi()) {
      propertiesPane.addProperty(
          "Productivity! compatibility mode", productivityCompMode);
    }

    propertiesPane
        .setBorder(BorderFactory.createTitledBorder("Global Options"));

    add(pathsPanel);
    add(propertiesPane, BorderLayout.SOUTH);

    try {
      /*
       * Due to the fact that there is no way to get JB version programatically
       * we can do it via reflections. Code below checks whether we are under 5
       * or 6 version of JBuilder because only in this versions there is no this
       * field.
       */
      EditorManager.class.getField("braceMatchingEnabledAttribute");
    } catch (NoSuchFieldException e) {
      try {
        // this one is found in JB X
        EditorManager.class.getField("highlightOnlyOpposingMatchBraceAttribute");
      } catch (NoSuchFieldException ex) {
        // yes, we are under 5 or 6 version of JBuilder
        propertiesPane.addProperty("Enable brace match highlighting", braceBox);
        //contentPane.addProperty("Brace match highlighting after (in
        // milliseconds)", delayField );
      } catch (Exception ignore) {
      }
    } catch (Exception ignore) {
    }

    //this.add(contentPane, BorderLayout.NORTH);
  }

  public void writeProperties() {
    // Needs to be called here before we set new values to properties in the
    // persistant storage.
    boolean compileOptionsChanged = projectOptionsChanged();

    RefactorItPropGroup.CUSTOM_BRACE_MATCHER.setBoolean((braceBox
        .getSelectedIndex() == 0) ? true : false);

    RefactorItPropGroup.setProjectProperty(
        RefactorItPropGroup.SPECIFIED_SOURCEPATH, sourcepathChooser.getPath()
        .toString());

    RefactorItPropGroup.setProjectProperty(RefactorItPropGroup.SPECIFIED_JAVADOC,
        new Path(javadocPathEditingPanel.getPathItems()).toString());

    RefactorItPropGroup.setProjectProperty(
        RefactorItPropGroup.IGNORED_SOURCEPATH, ignoredSourcepathChooser
        .getPath().toString());
    RefactorItPropGroup.setProjectProperty(
        RefactorItPropGroup.SPECIFIED_CLASSPATH, classpathChooser.getPath()
        .toString());
    RefactorItPropGroup
        .setProjectPropertyBoolean(RefactorItPropGroup.AUTODETECT_PATHS,
        pathsPanel.autodetect.isSelected());

    options.saveChoicesFromPropertyEditors();

    // Save compatibility mode setting globally
    {
      boolean oldValue = GlobalOptions.getOptionAsBoolean(
          GlobalOptions.JB_PRODUCTIVITY_COMP_MODE, false);
      boolean newValue = productivityCompMode.getSelectedIndex() == 0;

      GlobalOptions.setOption(GlobalOptions.JB_PRODUCTIVITY_COMP_MODE, ""
          + newValue);

      GlobalOptions.save();

      if (newValue != oldValue) {
        DialogManager.getInstance().showWarning(
            IDEController.getInstance().createProjectContext(),
            "warning.restart.jbuilder.mode");
      }
    }

    IDEController instance = IDEController.getInstance();
    Project project = instance.getActiveProject();

    if (project != null && compileOptionsChanged) {
      project.getPaths().getClassPath().release();

      project.getProjectLoader().markProjectForCleanup();
      //      instance.ensureProject();
      //      RefactorItTool.ensureRefactoritProject( Browser.getActiveBrowser(),
      // true);
    }
  }

  /**
   * Loads the current property values into the user interface in preparation
   * for displaying the page to the user (copied from <code>PropertyPage</code>).
   * This routine will also be called by the property dialog when the reset
   * button is used, so remove any prior state that your PropertyPage controls
   * may be showing (e.g. always clear models rather than just appending).
   */
  public void readProperties() {
    // Read compatibility mode setting
    {
      JavaFileNodeRe.informAboutProductivityModeIfNeeded();
      productivityCompMode.setSelectedIndex(GlobalOptions.getOptionAsBoolean(
          GlobalOptions.JB_PRODUCTIVITY_COMP_MODE, false) ? 0 : 1);
    }

    if (RefactorItPropGroup.CUSTOM_BRACE_MATCHER.getBoolean()) {
      braceBox.setSelectedIndex(0);
    } else {
      braceBox.setSelectedIndex(1);
    }
    boolean autodetectPaths = RefactorItPropGroup.getProjectPropertyBoolean(
        RefactorItPropGroup.AUTODETECT_PATHS, true);

    if (!autodetectPaths && isLegacyPaths()) {
      RefactorItPropGroup
          .setProjectPropertyBoolean(RefactorItPropGroup.AUTODETECT_PATHS, true);
      autodetectPaths = true;
    }

    if (autodetectPaths) {
      pathsPanel.autodetect.setSelected(true);
      readAutodetectedSourcepath(sourcepathChooser);
      sourcepathChooser.setEnabled(false);
      javadocPathEditingPanel.setEnabled(false);
      classpathChooser.setContent(getAutoDetectedClasspathAsStringList());
      classpathChooser.setEnabled(false);
//      ignoredSourcepathChooser.setContent(Collections.EMPTY_LIST);
//      ignoredSourcepathChooser.setEnabled(false);
      readIgnoredSourcepathContents(ignoredSourcepathChooser);
      ignoredSourcepathChooser.setEnabled(true);
    } else {
      pathsPanel.manual.setSelected(true);
      String pathStr = RefactorItPropGroup.getProjectProperty(
          RefactorItPropGroup.SPECIFIED_CLASSPATH, "");
      classpathChooser.setPath(new ClassPath(pathStr));
      classpathChooser.setEnabled(true);

      pathStr = RefactorItPropGroup.getProjectProperty(
          RefactorItPropGroup.SPECIFIED_JAVADOC, "");
      javadocPathEditingPanel.setContents(new Path(pathStr).toPathItems());
      javadocPathEditingPanel.setEnabled(true);

      pathStr = RefactorItPropGroup.getProjectProperty(
          RefactorItPropGroup.SPECIFIED_SOURCEPATH, "");
      sourcepathChooser.setPath(new SourcePath(pathStr));
      sourcepathChooser.setEnabled(true);

      readIgnoredSourcepathContents(ignoredSourcepathChooser);
      ignoredSourcepathChooser.setEnabled(true);
    }


    options.loadChoicesToPropertyEditors();
  }

  private boolean isLegacyPaths() {
    String pathStr = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_SOURCEPATH, "");
    if (JBSourcePath.isOldStylePath(pathStr)) {
      return true;
    }
    pathStr = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_CLASSPATH, "");
    if (JBSourcePath.isOldStylePath(pathStr)) {
      return true;
    }

    return false;
  }

  private boolean projectOptionsChanged() {
    boolean sourcepathChanged = !sourcepathChooser.getPathString().equals(
        RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_SOURCEPATH, ""));
    boolean ignoredSourcepathChanged = !ignoredSourcepathChooser
        .getPathString().equals(
        RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.IGNORED_SOURCEPATH, ""));
    boolean classpathChanged = !classpathChooser.getPathString().equals(
        RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.SPECIFIED_CLASSPATH, ""));

    Path javadocPath = new Path(javadocPathEditingPanel.getPathItems());
    boolean javadocpathChanged = !javadocPath.equals(
        RefactorItPropGroup.getProjectProperty(
            RefactorItPropGroup.SPECIFIED_JAVADOC, ""));


    boolean autodetectionChanged = pathsPanel.autodetect.isSelected()
        != RefactorItPropGroup
        .getProjectPropertyBoolean(RefactorItPropGroup.AUTODETECT_PATHS, true);

    return sourcepathChanged || ignoredSourcepathChanged || classpathChanged
        || autodetectionChanged || javadocpathChanged
        || options.propertyEditorsModified();
  }

  static List getSourcepathAsStringList() {
    Source[] rootSourcepath = new JBSourcePath(RefactorItPropGroup
        .getActiveProject()).getAutodetectedRootSources();
    return sourcesToStrings(rootSourcepath);
  }

  /**
   * @param sources
   * @return
   */
  private static List sourcesToStrings(Source[] sources) {
    List result = new ArrayList(sources.length);

    for (int i = 0; i < sources.length; i++) {
      result.add(sources[i].getAbsolutePath());
    }

    return result;
  }

  private static List classpathElementsToStrings(
      ClassPathElement[] classpathElements) {
    List result = new ArrayList(classpathElements.length);

    for (int i = 0; i < classpathElements.length; i++) {
      result.add(classpathElements[i].getAbsolutePath());
    }
    return result;
  }

  static List getAutoDetectedClasspathAsStringList() {
    List classpathRoots = new JBClassPath(RefactorItPropGroup.getActiveProject())
        .getAutodetectedClassPathElements();
    return classpathElementsToStrings((ClassPathElement[]) classpathRoots
        .toArray(new ClassPathElement[classpathRoots.size()]));
  }

  /**
   * is used not only automaticaly when ide shows the according PropertyPage,
   * but also when autodetect paths option is set to true by user in ui dialog.
   *
   * @param sourcepathChooser
   */
  protected void readAutodetectedSourcepath(JPathChooser sourcepathChooser) {
    sourcepathChooser.setContent(Arrays.asList(new JBSourcePath(
        RefactorItPropGroup.getActiveProject()).getAutodetectedRootSources()));
  }

  private void readIgnoredSourcepathContents(
      JIgnoredPathChooser ignoredSourcepath) {
    String sourcePathStr = RefactorItPropGroup.getProjectProperty(
        RefactorItPropGroup.IGNORED_SOURCEPATH, "");
    sourcePathStr = JBSourcePath.isOldStylePath(sourcePathStr)
        ? ""
        : sourcePathStr;
    StringTokenizer st=new StringTokenizer(sourcePathStr,File.pathSeparator);
    ArrayList paths=new ArrayList();
    while(st.hasMoreElements()) {
      paths.add(JBSource.getSource(new File(st.nextToken())));
    }
    ignoredSourcepath.setContent(paths);

  }

  public HelpTopic getHelpTopic() {
    return null;
  }
}
