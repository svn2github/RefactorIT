/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;


import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.vfs.NBJavadocPath;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.projectoptions.ProjectProperty;
import net.sf.refactorit.utils.SwingUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


public class SettingsDialog {
  private final RitDialog dialog;

  private FileObjectPathChooser sourcepathChooser;
  private FileObjectPathChooser classpathChooser;
  private FileObjectPathChooser javadocpathChooser;
  private FileObjectPathChooser ignoredSourcesChooser;
  private JRadioButton detect;
  private JRadioButton specify;
  private Settings mySettings;
  private Settings resultSettings = null;

  private JButton buttonHelp = new JButton("Help");
  
  private static SettingsDialog instanceInUse;

  /**Construct the frame*/
  public SettingsDialog(IdeWindowContext context, Settings s) {
    mySettings = s;

    dialog = RitDialog.create(context);
    dialog.setTitle("RefactorIT Project Options");

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(695, 620));

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Main", createMainTab());
    tabs.setMnemonicAt(0, KeyEvent.VK_M);
    tabs.addTab("Ignored On Sourcepath", createIgnoredSourcesTab());
    tabs.setMnemonicAt(1, KeyEvent.VK_I);
    

    contentPane.add(tabs, BorderLayout.CENTER);

    contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp,
        "getStart.netbeans.settings");
  }

  private JPanel createMainTab() {
    JPanel content = new JPanel();
    content.setLayout(new BorderLayout());

    content.add(createRadioPanel(), BorderLayout.NORTH);
    content.add(createPathPanel(), BorderLayout.CENTER);
    content.add(createCommonProjectPropertiesPanel(), BorderLayout.SOUTH);

    return content;
  }

  private JPanel createIgnoredSourcesTab() {
    JPanel content = new JPanel();
    content.setLayout(new BorderLayout());

    content.add(createIgnoredSourcesPanel(), BorderLayout.CENTER);

    return content;
  }

  protected void doMoveDetect() {
    sourcepathChooser.setButtonsEnabled(false);
    classpathChooser.setButtonsEnabled(false);
    javadocpathChooser.setButtonsEnabled(false);
    sourcepathChooser.setPath(mySettings.detectedSourcePath);
    classpathChooser.setPath(mySettings.detectedClassPath);
    javadocpathChooser.setPath(mySettings.detectedJavadocPath);
  }

  protected void doMoveSpecify() {
    sourcepathChooser.setButtonsEnabled(true);
    classpathChooser.setButtonsEnabled(true);
    javadocpathChooser.setButtonsEnabled(true);
  }

  /** null when "Cancel" was pressed */
  public Settings getResultSettings() {
    return resultSettings;
  }

  protected void doOk() {

    resultSettings = new Settings();
    resultSettings.doAutodetect = detect.isSelected();

    if (resultSettings.doAutodetect) {
      resultSettings.detectedClassPath = this.classpathChooser.getPath();
      resultSettings.detectedSourcePath = this.sourcepathChooser.getPath();
      resultSettings.detectedJavadocPath = this.javadocpathChooser.getPath();
    } else {
      resultSettings.specifiedClassPath = this.classpathChooser.getPath();
      resultSettings.specifiedSourcePath = this.sourcepathChooser.getPath();
      resultSettings.specifiedJavadocPath = this.javadocpathChooser.getPath();
    }

    resultSettings.specifiedIgnoredSourcePath = this.ignoredSourcesChooser.getPath();

    dialog.dispose();
  }

  protected void doCancel() {
    dialog.dispose();
  }

  private JPanel createRadioPanel() {
    JPanel radioPanel = new JPanel(new GridLayout(2, 1));

    detect = new JRadioButton("Detect classpath and sourcepath");
    specify = new JRadioButton("Specify classpath and sourcepath");
    
    detect.setMnemonic('d');
    specify.setMnemonic('s');

    detect.setSelected(mySettings.doAutodetect);
    specify.setSelected(!mySettings.doAutodetect);

    ButtonGroup group = new ButtonGroup();
    group.add(detect);
    group.add(specify);

    radioPanel.add(detect);
    radioPanel.add(specify);

    radioPanel.setBorder(new TitledBorder("Choose the method"));

    detect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doMoveDetect();
      }
    });

    specify.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doMoveSpecify();
      }
    });

    return radioPanel;

  }
  
  public static void clickAddIgnoredSource() {
    instanceInUse.ignoredSourcesChooser.clickAddFromFilesystems();
  }

  private JPanel createIgnoredSourcesPanel() {
    JPanel result = new JPanel();
    result.setLayout(new BorderLayout());

    ignoredSourcesChooser = new FileObjectPathChooser(FileObjectPathChooser.
        IGNORED_ON_SOURCEPATH);
    ignoredSourcesChooser.setBorder(new TitledBorder("Ignored On Sourcepath"));
    ignoredSourcesChooser.setPath(mySettings.specifiedIgnoredSourcePath);
    result.add(ignoredSourcesChooser, BorderLayout.NORTH);

    return result;
  }

  private JPanel createPathPanel() {
    JPanel pathPanel = new JPanel();
    pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));

    sourcepathChooser = new FileObjectPathChooser(FileObjectPathChooser.
        SOURCEPATH);
    sourcepathChooser.setBorder(new TitledBorder("sourcepath"));
    sourcepathChooser.setPath(mySettings.doAutodetect ?
        mySettings.detectedSourcePath : mySettings.specifiedSourcePath);
    sourcepathChooser.setButtonsEnabled(!mySettings.doAutodetect);
    pathPanel.add(sourcepathChooser);

    classpathChooser = new FileObjectPathChooser(FileObjectPathChooser.
        CLASSPATH);
    classpathChooser.setBorder(new TitledBorder("classpath"));
    classpathChooser.setPath(mySettings.doAutodetect ?
        mySettings.detectedClassPath : mySettings.specifiedClassPath);
    classpathChooser.setButtonsEnabled(!mySettings.doAutodetect);
    pathPanel.add(classpathChooser);

    javadocpathChooser = new FileObjectPathChooser(FileObjectPathChooser.
        JAVADOCPATH);
    javadocpathChooser.setBorder(new TitledBorder("javadoc path"));

    javadocpathChooser.setPath(mySettings.doAutodetect ?
        mySettings.detectedJavadocPath : mySettings.specifiedJavadocPath);
    javadocpathChooser.setButtonsEnabled(!mySettings.doAutodetect);

    pathPanel.add(javadocpathChooser);

    return pathPanel;
  }

  private JPanel createCommonProjectPropertiesPanel() {
    JPanel result = net.sf.refactorit.utils.SwingUtil.wrapInMinimizer(
        IDEController.getInstance().getActiveProject().getOptions().getCommonPropertiesInOnePanel());

    result.setBorder(new TitledBorder(""));
    return result;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;

    JButton buttonOk = new JButton("OK");
    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doOk();
      }
    });

    constraints.gridx = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(4, 16, 4, 16);
    buttonPanel.add(buttonOk, constraints);

    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(4, 0, 4, 0);

    JButton buttonCancel = new JButton("Cancel");
    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doCancel();
      }
    });

    constraints.gridx = 1;
    buttonPanel.add(buttonCancel, constraints);

    constraints.gridx = 3;
    constraints.insets = new Insets(4, 16, 4, 16);
    buttonPanel.add(buttonHelp, constraints);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk,
        buttonCancel, buttonHelp);

    return buttonPanel;
  }

  private static class Settings {
    public boolean doAutodetect = true;
    public PathItemReference[] detectedSourcePath;
    public PathItemReference[] detectedClassPath;
    public PathItemReference[] detectedJavadocPath;
    public PathItemReference[] specifiedSourcePath;
    public PathItemReference[] specifiedClassPath;
    public PathItemReference[] specifiedJavadocPath;
    public ProjectProperty[] additionalProperties;
    public PathItemReference[] specifiedIgnoredSourcePath;
  }


  public void show() {
    dialog.show();
  }

  /**
   * Show and Edit the Project Options.
   * @param ideProject
   *
   * @param options the options to display and edit.
   * @param ownerFrame the frame to display above
   * @return whether the changes were saved.
   */
  public static boolean showAndEditOptions(
      IdeWindowContext context, Object ideProject, NBProjectOptions options
  ) {
    // Makes sure that there are no such weird dialogs later, when the user
    // chooses "add from filesystems", for example. Because then some path panels
    // could get out of synch.
    PathUtil.getInstance().checkForNewlyMountedFilesystems(ideProject);

    Settings s = new Settings();
    s.detectedSourcePath = PathUtil.getInstance().getAutodetectedSourcepath(ideProject,
        true);
    s.detectedClassPath = PathUtil.getInstance().getAutodetectedClasspath(ideProject);
    s.detectedJavadocPath = NBJavadocPath.getAutodetectedJavadocPath(ideProject);
    s.specifiedSourcePath = options.getUserSpecifiedSourcePath(
        true);
    // s.specifiedSourcePath = options.getUserSpecifiedSourcePath(false);
    s.specifiedClassPath = options.getUserSpecifiedClassPath();
    s.specifiedJavadocPath = options.getUserSpecifiedJavadocPath();
    s.doAutodetect = options.getAutodetectPaths();
    s.additionalProperties = options.getCommonProperties();
    s.specifiedIgnoredSourcePath = options.getUserSpecifiedIgnoredSourcePathDirectories();

    instanceInUse = new SettingsDialog(context, s);
    instanceInUse.show();
    Settings result = instanceInUse.getResultSettings();

    if (result != null) { // If "Ok" pressed...
      if (result.specifiedSourcePath != null) {
        options.setUserSpecifiedSourcePath(result.specifiedSourcePath);
      }
      if (result.specifiedClassPath != null) {
        options.setUserSpecifiedClassPath(result.specifiedClassPath);
      }
      if (result.specifiedJavadocPath != null) {
        options.setUserSpecifiedJavadocPath(result.specifiedJavadocPath);
      }
      if (result.specifiedIgnoredSourcePath != null) {
        options.setUserSpecifiedIgnoredSourcePathDirectories(result.
            specifiedIgnoredSourcePath);
      }

      options.setAutodetectPaths(result.doAutodetect);
      options.saveChoicesFromPropertyEditors();

      // we already visited options explicitly
      options.setNewProjectMessageDisplayed(true);

      return true;
    }

    return false;
  }
}
