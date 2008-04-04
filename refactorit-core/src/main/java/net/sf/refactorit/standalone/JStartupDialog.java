/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.commonIDE.options.ProjectOptionsPanel;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.standalone.projectoptions.StandaloneProjectOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.utils.ClasspathUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;


/**
 * Create / Open project dialog.
 *
 * @author Vladislav Vislogubov
 */
public class JStartupDialog {
  private static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(UIResources.class);

  static final FileFilter rbpFilter = new FileExtensionFilter(
      ".rbp", resLocalizedStrings.getString("filter.desc"));

  final RitDialog dialog;

  File project; // first recent project file
  File newProject; // new project file

  JRadioButton radioCreate;
  JRadioButton radioRecent;

  CardLayout mainCardLayout;
  JPanel mainCardPanel;

  CardLayout startCardLayout;
  JPanel startCardPanel;

  CardLayout textCardLayout;
  JPanel textCardPanel;

  JTextField fileField;
  JList recentList;


  private JButton buttonSaveAs;

  JButton buttonBack;
  JButton buttonNext;
  JButton buttonFinish;
  private JButton buttonHelp;

  JFileChooser saveFileChooser;
  JFileChooser openFileChooser;

  ProjectOptionsPanel pathPanel;

  private JRefactorItFrame frame;

  /**
   * Constructor.
   *
   * @param frame  owner frame
   * @param open  if true open existing project;
   *              create new one otherwise
   */
  public JStartupDialog(JRefactorItFrame frame, boolean open) {
    this.frame = frame;

    JPanel contentPane = new JPanel();
    contentPane.setPreferredSize(new Dimension(495, 480));

    contentPane.setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(4, 4, 4, 4);

    constraints.gridy = 0;
    constraints.weighty = 0.0;
    contentPane.add(createTextPanel(), constraints);

    constraints.gridy = 1;
    constraints.weighty = 1.0;
    contentPane.add(createMainPanel(), constraints);

    constraints.gridy = 2;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 4, 4, 4);
    contentPane.add(createButtonPanel(), constraints);

    DefaultListModel model = new DefaultListModel();
    recentList.setModel(model);

    ArrayList projects = getRecentOpenedProjectsNames();

    if (projects.size() == 0) {
      // no projects in recent list
      open = false;
    } else {
      Iterator i = projects.iterator();
      while(i.hasNext()) {
      	model.addElement(i.next());
      }
      recentList.setSelectedIndex(0);
    }

    dialog = RitDialog.create(IDEController.getInstance().createProjectContext());
    dialog.setDisposeOnClose(false);
    dialog.setTitle(resLocalizedStrings.getString("title.startup"));
    dialog.setContentPane(contentPane);


    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "getStart.setUpInStandalone");

    if (open) {
      radioRecent.setSelected(true);
    }

    setupDefaultButton();
    focus();

    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        if(isNeedDialogDispose()) {
          project = null;
          dialog.dispose();
        }
      }
    });

    dialog.getRootPane().addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent evt) {
        keyTyped(evt);
      }

      public void keyReleased(KeyEvent evt) {
        keyTyped(evt);
      }

      public void keyTyped(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_CANCEL ||
            evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
          if(isNeedDialogDispose()) {
            project = null;
            dialog.dispose();
          }
        }
      }
    });
  }

  /**
   * Looks for the projects, what may have been recently opened
   * and put them into a set.
   * @return set of strings (recent project names). Returns an empty array in case
   * nothing was found
   */
  private ArrayList getRecentOpenedProjectsNames() {
    ArrayList recentProjects = new ArrayList();
    for (int i = 0; i < 20; i++) {
      String name = GlobalOptions.getOption("project.recent." + i);
      if (name == null || name.length() <= 0) {
        continue;
      }

      File projectCandidate = new File(name);
      if (projectCandidate.exists() && projectCandidate.isFile()
          && !recentProjects.contains(name)) {
        recentProjects.add(name);
      }

    }
    return recentProjects;
  }

  // bottom buttonbar

  private boolean isNeedDialogDispose() {
    if(IDEController.getInstance().getActiveProject() != null) {
      return true;
    }
    int x = RitDialog.showConfirmDialog(IDEController.getInstance().createProjectContext(),
        "This action will close the application.",
        "Close application?",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if(x == JOptionPane.YES_OPTION) { // clicked yes
      return true;
    } else {
      return false;
    }
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new GridBagLayout());

    JButton buttonCancel = new JButton(resLocalizedStrings.getString(
        "button.cancel"));
    buttonCancel.setMnemonic(KeyEvent.VK_C);

    buttonBack = new JButton(resLocalizedStrings.getString("button.back"));
    buttonBack.setMnemonic(KeyEvent.VK_B);
    buttonBack.setEnabled(false);

    buttonNext = new JButton(resLocalizedStrings.getString("button.next"));
    buttonNext.setMnemonic(KeyEvent.VK_N);
    buttonNext.setEnabled(false);

    buttonFinish = new JButton(resLocalizedStrings.getString("button.finish"));
    buttonFinish.setMnemonic(KeyEvent.VK_F);
    buttonFinish.setEnabled(false);

    buttonHelp = new JButton(" " + resLocalizedStrings.getString("button.help")
        + " ");
    buttonHelp.setMnemonic(KeyEvent.VK_H);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;

    constraints.gridx = 0;
    //constraints.weightx = 1.0;
    //constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 16, 0, 4);
    buttonPanel.add(buttonBack, constraints);

    //constraints.weightx = 0.0;
    //constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 0, 0, 4);
    constraints.gridx = 1;
    buttonPanel.add(buttonNext, constraints);

    //constraints.weightx = 0.0;
    //constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 0, 0, 16);
    constraints.gridx = 2;
    buttonPanel.add(buttonFinish, constraints);

    constraints.gridx = 3;
    constraints.insets = new Insets(0, 16, 0, 4);
    buttonPanel.add(buttonCancel, constraints);

    constraints.gridx = 4;
    constraints.insets = new Insets(0, 0, 0, 0);
    buttonPanel.add(buttonHelp, constraints);

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(isNeedDialogDispose()) {
          project = null;
          dialog.dispose();
        }
      }
    });

    buttonBack.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonBack.setEnabled(false);
        buttonNext.setEnabled(true);
        buttonFinish.setEnabled(radioRecent.isSelected()
            && recentList.getSelectedIndex() >= 0);
        textCardLayout.show(textCardPanel, "new");
        mainCardLayout.show(mainCardPanel, "radio");
      }
    });

    buttonNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        if (radioRecent.isSelected()) {
          project = new File((String) recentList.getSelectedValue());
        } else if (radioCreate.isSelected()) {
          project = newProject;
        } else {
          project = null;
        }
        if (project == null) {
          // no project !? dont move forth
          return;
        }
        buttonBack.setEnabled(true);
        buttonNext.setEnabled(false);
        buttonFinish.setEnabled(true);

       try {
          StandaloneProjectOptions options;

          if (radioRecent.isSelected()) {
            options = new StandaloneProjectOptions(project);

            // FIXME: add support
            //ProjectSettings projectSettings = new ProjectSettings();
            //projectSettings.deserialize(options,false);

          } else {
            // new project creating, adding deafult values into classpath and sourcepath
            project.createNewFile();

            options = new StandaloneProjectOptions(project);



            File projectFolder = project.getParentFile();
            //ProjectSettings projectSettings = options.getProjectSettings();


            if (projectFolder.exists()) {
              options.getSourcePath()
              .addItem(new PathItem(projectFolder));

            }
            String defaultClasspath = ClasspathUtil.getDefaultClasspath();
            options.getClassPath().deserialize(defaultClasspath);


            // save changed settings
            options.serialize();

          }


          addOptionPanel(options);


          textCardLayout.show(textCardPanel, "paths");
          mainCardLayout.show(mainCardPanel, "paths");
          StandaloneController controller = (StandaloneController) IDEController
              .getInstance();
          controller.setIdeProject(project);
        } catch (IOException x) {
          net.sf.refactorit.common.util.AppRegistry.getLogger(JStartupDialog.class)
              .error(x);
        }

      }

      private void addOptionPanel(StandaloneProjectOptions options) {
        // smell: little hacky
        mainCardPanel.remove(pathPanel);

        pathPanel = createProjectOptionsPanel(options);

        mainCardPanel.add(pathPanel,"paths");
      }
    });

    buttonFinish.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (radioRecent.isSelected() && !recentList.isSelectionEmpty()) {
          project = new File((String) recentList.getSelectedValue());
        }

        if (project == null) {
          // don't let finish if project file is missing, should not happend.
          return;
        }
        try {
          if (buttonBack.isEnabled()) { // if backbutton is enabled then we are in path select page.. save.

            // smell: little hacky
            StandaloneProjectOptions projectOptions = new StandaloneProjectOptions(project);
            ProjectOptions options = pathPanel.getProjectOptions();
            pathPanel.updateSettings();
            projectOptions.setJvmMode(options.getJvmMode());
            options.serialize(projectOptions);
            net.sf.refactorit.common.util.AppRegistry.getLogger(JStartupDialog.class)
            	.debug("project options are " + projectOptions);
            //ProjectSettings projectSettings = pathPanel.getProjectSettings();
            //projectSettings.serialize(projectOptions);
            //net.sf.refactorit.common.util.AppRegistry.getLogger(JStartupDialog.class)
            //    .debug("project settings are "+projectSettings);
            projectOptions.store();
          }
          dialog.dispose();
        } catch (IOException x) {
          x.printStackTrace();
        }
      }
    });

    buttonFinish.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonHelp);
    buttonHelp.setNextFocusableComponent(radioCreate);

    JPanel downPanel = new JPanel(new GridBagLayout());
    constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 0, 0, 20);
    downPanel.add(buttonPanel, constraints);

    return downPanel;
  }

  private JPanel createMainPanel() {
    mainCardLayout = new CardLayout();
    mainCardPanel = new JPanel(mainCardLayout);

    mainCardPanel.add(createRadioPanel(), "radio");
    try {
      pathPanel = createProjectOptionsPanel(new StandaloneProjectOptions(null));
    } catch (IOException e) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    mainCardPanel.add(pathPanel, "paths");

    return mainCardPanel;
  }

  private JPanel createNewPanel() {
    JPanel newPanel = new JPanel(new GridBagLayout());

    newPanel.setBorder(new TitledBorder(resLocalizedStrings.getString(
        "label.creatNewProject")));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.insets = new Insets(4, 4, 4, 4);
    constraints.fill = GridBagConstraints.BOTH;

    JLabel label = new JLabel(resLocalizedStrings.getString(
        "label.projectFileName"));
    constraints.gridx = 0;
    constraints.weightx = 0.0;
    newPanel.add(label, constraints);

    fileField = new JTextField(resLocalizedStrings.getString(
        "message.clickSaveAs"));
    fileField.setRequestFocusEnabled(false);
    fileField.setEditable(false);

    constraints.gridx = 1;
    constraints.weightx = 1.0;
    newPanel.add(fileField, constraints);

    buttonSaveAs = new JButton(resLocalizedStrings.getString("button.saveAs"));
    buttonSaveAs.setMnemonic(KeyEvent.VK_S);
    buttonSaveAs.setDefaultCapable(false);

    constraints.gridx = 2;
    constraints.weightx = 0.0;
    newPanel.add(buttonSaveAs, constraints);

    buttonSaveAs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (saveFileChooser == null) {
          saveFileChooser = new JFileChooser();
        }
        saveFileChooser.setFileFilter(rbpFilter);
        saveFileChooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

        saveFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), saveFileChooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        GlobalOptions.setLastDirectory(saveFileChooser.getCurrentDirectory());

        newProject = saveFileChooser.getSelectedFile();
        if (newProject != null) {
          String str = newProject.getName();
          if (str.indexOf('.') < 0) {
            newProject = new File(newProject.getPath() + ".rbp");
          }
          fileField.setText(newProject.getAbsolutePath());
          buttonNext.setEnabled(true);
        }
      }
    });

    return newPanel;
  }

  ProjectOptionsPanel createProjectOptionsPanel(StandaloneProjectOptions options) {
    return new ProjectOptionsPanel(options/*,false*/);
  }

  // handles layout change between new/existing project
  private JPanel createRadioPanel() {
    JPanel radioPanel = new JPanel(new GridBagLayout());

    ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src == radioCreate) {
          if (radioCreate.isSelected()) {
            textCardLayout.show(textCardPanel, "new");
            startCardLayout.show(startCardPanel, "new");
            buttonNext.setEnabled(newProject != null);
            buttonFinish.setEnabled(false);
            setupDefaultButton();
          }
        } else if (src == radioRecent) {
          if (radioRecent.isSelected()) {
            textCardLayout.show(textCardPanel, "open");
            startCardLayout.show(startCardPanel, "open");
            buttonNext.setEnabled(recentList.getSelectedIndex() >= 0);
            buttonFinish.setEnabled(recentList.getSelectedIndex() >= 0);
            setupDefaultButton();
          }
        }
      }
    };

    FocusListener focusListener = new FocusAdapter() {
      public void focusGained(FocusEvent evt) {
        setupDefaultButton();
      }
    };

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.CENTER;

    ButtonGroup group = new ButtonGroup();

    radioCreate = new JRadioButton(resLocalizedStrings.getString(
        "label.creatNewProject"));
    radioCreate.setMnemonic(KeyEvent.VK_N);
    radioCreate.setSelected(true);
    radioCreate.addFocusListener(focusListener);
    radioCreate.addChangeListener(listener);
    group.add(radioCreate);

    constraints.gridx = 0;
    radioPanel.add(radioCreate, constraints);

    radioRecent = new JRadioButton(resLocalizedStrings.getString(
        "label.openExistingProject"));
    radioRecent.setMnemonic(KeyEvent.VK_P);
    radioRecent.addChangeListener(listener);
    radioRecent.addFocusListener(focusListener);
    group.add(radioRecent);

    constraints.gridx = 1;
    radioPanel.add(radioRecent, constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.gridwidth = 2;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    radioPanel.add(createStartPanel(), constraints);

    return radioPanel;
  }

  // Recent files Panel

  private JPanel createRecentPanel() {
    JPanel recentPanel = new JPanel(new GridBagLayout());

    recentPanel.setBorder(new TitledBorder(resLocalizedStrings.getString(
        "label.openExistingProject")));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.insets = new Insets(4, 4, 4, 4);

    recentList = new JList();

    recentList.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        setupDefaultButton();
      }
    });

    constraints.gridx = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    recentPanel.add(new JScrollPane(recentList), constraints);

    JButton openButton = new JButton(resLocalizedStrings.getString(
        "button.other"));
    openButton.setMnemonic(KeyEvent.VK_O);

    constraints.gridx = 1;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    recentPanel.add(openButton, constraints);

    recentList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        buttonFinish.setEnabled(recentList.getSelectedIndex() >= 0);
      }
    });

    openButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (openFileChooser == null) {
          openFileChooser = new JFileChooser();
        }
        openFileChooser.setFileFilter(rbpFilter);
        openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openFileChooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

        openFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), openFileChooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        if (!openFileChooser.getSelectedFile().exists()) {
          RitDialog.showMessageDialog(IDEController.getInstance()
              .createProjectContext(), "Specified file \""
              + openFileChooser.getSelectedFile().getAbsolutePath()
              + "\" not found. \nPlease, check the path.");
          return;
        }

        GlobalOptions.setLastDirectory(openFileChooser.getCurrentDirectory());
        project = openFileChooser.getSelectedFile();

        if (!((DefaultListModel) recentList.getModel()).contains(project.
            getAbsolutePath())) {
          ((DefaultListModel) recentList.getModel()).addElement(project.
              getAbsolutePath());
        }
        recentList.setSelectedValue(project.getAbsolutePath(), true);

        // FIXME: logic is duplicated though whole file
        try {
	        StandaloneProjectOptions options = new StandaloneProjectOptions(project);

	        mainCardPanel.remove(pathPanel);

	        pathPanel = JStartupDialog.this.createProjectOptionsPanel(options);

	        mainCardPanel.add(pathPanel,"paths");

	        textCardLayout.show(textCardPanel, "paths");
	        mainCardLayout.show(mainCardPanel, "paths");

	        buttonBack.setEnabled(true);
	        buttonNext.setEnabled(false);
	        buttonFinish.setEnabled(true);
        } catch (IOException x) {
          net.sf.refactorit.common.util.AppRegistry.getLogger(JStartupDialog.class)
              .error(x);
        }
//        dispose();
      }
    });

    return recentPanel;
  }

  private JPanel createStartPanel() {
    startCardLayout = new CardLayout();
    startCardPanel = new JPanel(startCardLayout);

    startCardPanel.add(createNewPanel(), "new");
    startCardPanel.add(createRecentPanel(), "open");

    return startCardPanel;
  }

  private JPanel createTextPanel() {
    textCardLayout = new CardLayout();
    textCardPanel = new JPanel(textCardLayout);
    textCardPanel.setBackground(DialogManager.DEFAULT_HELP_PANEL_COLOR);

    Dimension size = new Dimension(16, 60);

    textCardPanel.setMinimumSize(size);
    textCardPanel.setPreferredSize(size);

    textCardPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    JTextArea text = new JTextArea(resLocalizedStrings.getString(
        "label.startup.createDesc"));
    text.setRequestFocusEnabled(false);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    //text.setBackground( getBackground() );
    text.setBackground(DialogManager.DEFAULT_HELP_PANEL_COLOR);
    text.setFont(textCardPanel.getFont());

    textCardPanel.add(text, "new");

    text = new JTextArea(resLocalizedStrings.getString("label.startup.openDesc"));
    text.setRequestFocusEnabled(false);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    //text.setBackground( getBackground() );
    text.setBackground(DialogManager.DEFAULT_HELP_PANEL_COLOR);
    text.setFont(textCardPanel.getFont());

    textCardPanel.add(text, "open");

    text = new JTextArea(resLocalizedStrings.getString("label.startup.pathDesc"));
    text.setRequestFocusEnabled(false);
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    //text.setBackground( getBackground() );
    text.setBackground(DialogManager.DEFAULT_HELP_PANEL_COLOR);
    text.setFont(textCardPanel.getFont());

    textCardPanel.add(text, "paths");

    return textCardPanel;
  }

  void setupDefaultButton() {
    if (radioCreate.isSelected()) {
      if (buttonNext.isEnabled()) {
        dialog.getRootPane().setDefaultButton(buttonNext);
      } else {
        dialog.getRootPane().setDefaultButton(buttonSaveAs);
      }
    } else if (radioRecent.isSelected()) {
      dialog.getRootPane().setDefaultButton(buttonFinish);
    }
  }

  private void focus() {
    if (radioCreate.isSelected()) {
      buttonSaveAs.requestFocus();
    }
    if (radioRecent.isSelected()) {
      recentList.requestFocus();
    }
  }

  public void show() {
    dialog.show();
  }

  public File getProject() {
    return project;
  }

  public File getRecentOpenedProject() {
    String project = (String) recentList.getSelectedValue();
    if(project == null) {
      return null;
    }
    return new File(project);
  }

  /**
   * Test this dialog.
   *
   * @param args an array of command-line arguments
   */
  public static void main(String[] args) {
    try {
      JStartupDialog dialog = new JStartupDialog(null, true);
      dialog.dialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
      dialog.show();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }
}
