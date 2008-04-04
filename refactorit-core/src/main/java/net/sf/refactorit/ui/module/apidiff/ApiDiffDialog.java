/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.apisnapshot.Snapshot;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotIO;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;


public class ApiDiffDialog {
  static final ResourceBundle bundle =
      ResourceUtil.getBundle(ApiDiffDialog.class);

  final RitDialog dialog;

  boolean okPressed;

  private FileChoosePanel fileChoosePanel1;
  FileChoosePanel fileChoosePanel2;

  private JRadioButton currentSnapshot;
  JRadioButton anotherSnapshot;

  public ApiDiffDialog(IdeWindowContext context) {
    dialog = RitDialog.create(context);
    dialog.setTitle(bundle.getString("dialog.title"));

    JPanel contentPane = new JPanel(new GridBagLayout());
    dialog.setContentPane(contentPane);

    fileChoosePanel1 = new FileChoosePanel();
    fileChoosePanel1.setBorder(BorderFactory.createTitledBorder(bundle.
        getString("filechoosepanel1.name")));
    ((TitledBorder) fileChoosePanel1.getBorder()).setTitleColor(Color.black);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 5, 0, 5);
    contentPane.add(DialogManager.getHelpPanel(
        "Choose desired snapshots to see differences"), constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(5, 5, 5, 5);
    contentPane.add(fileChoosePanel1, constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(0, 5, 0, 5);
    contentPane.add(createCurrentSnapshotOrFileChoosePanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.insets = new Insets(3, 5, 1, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    contentPane.add(createButtonsPanel(), constraints);

    //contentPane.add( fileChoosePanel1, BorderLayout.NORTH );
    //contentPane.add( createCurrentSnapshotOrFileChoosePanel(), BorderLayout.CENTER );
    //contentPane.add( createButtonsPanel(), BorderLayout.SOUTH );

    dialog.setSize(400, 450);
  }

  private JComponent createCurrentSnapshotOrFileChoosePanel() {
    currentSnapshot = new JRadioButton(bundle.getString("current.snapshot"));
    anotherSnapshot = new JRadioButton(bundle.getString("another.snapshot"));

    ButtonGroup group = new ButtonGroup();
    group.add(currentSnapshot);
    group.add(anotherSnapshot);

    fileChoosePanel2 = new FileChoosePanel();
    //fileChoosePanel2.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );

    JPanel result = new JPanel(new GridBagLayout());
    result.setBorder(BorderFactory.createTitledBorder(bundle.getString(
        "filechoosepanel2.name")));
    ((TitledBorder) result.getBorder()).setTitleColor(Color.black);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 5, 0, 5);
    result.add(currentSnapshot, constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 5, 0, 5);
    result.add(anotherSnapshot, constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(0, 0, 0, 0);
    result.add(fileChoosePanel2, constraints);

    //addTightlyToTheTop( result, currentSnapshot, anotherSnapshot, fileChoosePanel2 );

    currentSnapshot.setSelected(true);
    fileChoosePanel2.setEnabled(anotherSnapshot.isSelected());

    ActionListener fileChooserEnabler = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileChoosePanel2.setEnabled(anotherSnapshot.isSelected());
      }
    };

    currentSnapshot.addActionListener(fileChooserEnabler);
    anotherSnapshot.addActionListener(fileChooserEnabler);

    return result;
  }

  public boolean getOkWasPressed() {
    return this.okPressed;
  }

  public String getFirstSnapshotFileName() {
    return fileChoosePanel1.getFileName().trim();
  }

  public boolean getShouldCompareAgainstCurrentCode() {
    return this.currentSnapshot.isSelected();
  }

  public String getAnotherSnapshotFileName() {
    return this.fileChoosePanel2.getFileName().trim();
  }

  boolean requiredFieldsFilled() {
    if ("".equals(getFirstSnapshotFileName())) {
      return false;
    }

    if (!getShouldCompareAgainstCurrentCode() &&
        "".equals(getAnotherSnapshotFileName())) {
      return false;
    }

    return true;
  }

  private JComponent createButtonsPanel() {
    JButton buttonOk = new JButton(bundle.getString("button.ok"));
    JButton buttonCancel = new JButton(bundle.getString("button.cancel"));
    JButton buttonHelp = new JButton("Help");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!requiredFieldsFilled()) {
          RitDialog.showMessageDialog(
              dialog.getContext(), bundle.getString("fields.not.filled"));

          return;
        }

        ApiDiffDialog.this.okPressed = true;

        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    final ActionListener cancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };
    buttonCancel.addActionListener(cancelListener);
    buttonPanel.add(buttonCancel);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.apids");
    buttonPanel.add(buttonHelp);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, 
        buttonHelp, cancelListener);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);
    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);
    return downPanel;

  }

  private class FileChoosePanel extends JPanel {
    JTextField fileName;
    private JButton fileChooseButton;
    private JTextArea infoPanelTextArea;

    public FileChoosePanel() {
      super(new GridBagLayout());
      //FileChoosePanel.this.setLayout( new BorderLayout() );

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 1;
      constraints.gridy = 1;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.weightx = 1.0;
      constraints.weighty = 0.0;
      constraints.insets = new Insets(5, 5, 0, 5);
      this.add(createFileNamePanel(), constraints);

      constraints.gridx = 1;
      constraints.gridy = 2;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.anchor = GridBagConstraints.CENTER;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.insets = new Insets(0, 5, 5, 5);
      this.add(createFileInfoPanel(), constraints);
    }

    private JPanel createFileNamePanel() {
      JPanel result = new JPanel();
      result.setLayout(new BorderLayout());

      fileName = new JTextField("");

      fileChooseButton = new JButton("...");
      fileChooseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          File startDir = SnapshotIO.browserStartLocation;

          JFileChooser fileChooser = (startDir == null)
              ? new JFileChooser() : new JFileChooser(startDir);

          fileChooser.addChoosableFileFilter(new FileExtensionFilter(".snp",
              "RefactorIT Snapshot Files (*.snp)"));
          fileChooser.setMultiSelectionEnabled(false);

          fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
          int res = RitDialog.showFileDialog(
              IDEController.getInstance().createProjectContext(), fileChooser);
          if (res == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
              String fileNameStr = fileChooser
                .getSelectedFile().getAbsolutePath();
              fileName.setText(fileNameStr);
              SnapshotIO.browserStartLocation = fileChooser
                .getSelectedFile().getParentFile();
            }
          }
        }
      });

      result.add(fileName, BorderLayout.CENTER);
      result.add(fileChooseButton, BorderLayout.EAST);

      return result;
    }

    private JPanel createFileInfoPanel() {
      JPanel result = new JPanel(new BorderLayout());

      infoPanelTextArea = new JTextArea();
      infoPanelTextArea.setEditable(false);
      infoPanelTextArea.setBackground(FileChoosePanel.this.getBackground());

      final int requiredHeight = 70;

      JScrollPane scrollPane = new JScrollPane(infoPanelTextArea) {
        public Dimension getMaximumSize() {
          return new Dimension(
              super.getMaximumSize().width,
              requiredHeight
              );
        }

        public Dimension getMinimumSize() {
          return new Dimension(
              super.getMinimumSize().width,
              requiredHeight
              );
        }

        public Dimension getPreferredSize() {
          return new Dimension(
              super.getPreferredSize().width,
              requiredHeight
              );
        }
      };

      scrollPane.setBorder(BorderFactory.createTitledBorder(bundle.getString(
          "info.about.selected.file")));
      ((TitledBorder) scrollPane.getBorder()).setTitleColor(Color.black);

      infoPanelTextArea.setText("");

      fileName.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
          updateInfoPanelContents();
        }

        public void insertUpdate(DocumentEvent e) {
          updateInfoPanelContents();
        }

        public void removeUpdate(DocumentEvent e) {
          updateInfoPanelContents();
        }
      });

      result.add(scrollPane, "Center");

      return result;
    }

    void updateInfoPanelContents() {
      try {
        Snapshot snapshot = SnapshotIO.getSnapshotFromFile(getFileName());
        infoPanelTextArea.setText(snapshot.getDescription() +
            System.getProperty("line.separator") + snapshot.getDateAsString());
      } catch (IOException fileDoesNotExist) {
        infoPanelTextArea.setText("");
      }
    }

    public String getFileName() {
      return fileName.getText();
    }

    public void setEnabled(boolean b) {
      fileName.setEnabled(b);
      fileChooseButton.setEnabled(b);

      super.setEnabled(b);
    }
  }

  public void show() {
    dialog.show();
  }
}
