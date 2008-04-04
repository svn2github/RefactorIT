/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.source.html.HTMLSourceEditor;
import net.sf.refactorit.standalone.JStatus;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;


public class JHTMLDialog {
  final RitDialog dialog;

  /* The file-chooser for selecting the output path */
  JFileChooser chooser = new JFileChooser();

  /* The base directory for outputting files */
  JTextField output = new JTextField(24);

  /* The project to convert to HTML */
  private Project project;

  /* The status bar for showing the progress */
  private JStatus status = new JStatus();

  /* GUI Widgets for capturing user-initiated events*/
  private JButton cancel = new JButton("Cancel");
  JButton create = new JButton("Create");
  private JButton select = new JButton("Select");
  private JButton help = new JButton("Help");

  /* Just Another GUI Widget (JAGW) */
  private JCheckBox number = new JCheckBox("Prepend line numbers", false);

  /**
   */
  public JHTMLDialog(IdeWindowContext context, Project project) {
    // Init field(s)
    setProject(project);

    JPanel contentPane = new JPanel();

    // Init UI Components
    {
      contentPane.setLayout(new GridLayout(1, 1));
      contentPane.add(createGUI());
    }

    setDefaultStatusMessage();

    dialog = RitDialog.create(context);
    dialog.setTitle("Generate Cross-referenced HTML");
    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, help, "refact.crossref.html");

    // Listen for Window-events
    dialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent event) {
        dialog.dispose();
      }
    });

    // Update JFileChooser
    {
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
  }

  private JComponent createGUI() {
    JPanel container = new JPanel(new BorderLayout(2, 2));

    // Create "North" panel
    {
      JPanel north = new JPanel(new GridBagLayout()) {
        public Insets getInsets() {
          return new Insets(4, 4, 4, 4);
        }
      };

      // Init constraints
      GridBagConstraints gbc = new GridBagConstraints();

      // Set defaults
      gbc.insets = new Insets(2, 2, 2, 2);

      // Create the first column
      {
        gbc.gridx = 0;
        gbc.gridy = 0;

        addComponent(north, gbc, new JLabel("Output path:", JLabel.RIGHT));
      }

      // Create the second column
      {
        gbc.gridx = 1;
        gbc.gridy = 0;
        addComponent(north, gbc, output);

        gbc.gridx = 1;
        gbc.gridy = 1;
        addComponent(north, gbc, number);
      }

      // Create the third column
      {
        gbc.gridx = 2;
        gbc.gridy = 0;

        addComponent(north, gbc, select);

        // Listen for path-choosing events
        select.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int res = RitDialog.showFileDialog(
                IDEController.getInstance().createProjectContext(), chooser);
            if (res != JFileChooser.APPROVE_OPTION) {
              return; // Bail out in case of UnInteresting events
            }

            // Get the selection
            File file = chooser.getSelectedFile();

            // Update text in JTextField
            output.setText(
                (file != null && file.isDirectory())
                    ? file.getAbsolutePath() : "");
          }
        });
      }

      // Add to container
      container.add(BorderLayout.NORTH, north);
    }

    // Create "Center" panel
    {
      JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)) {
        public Insets getInsets() {
          return new Insets(4, 4, 4, 4);
        }
      };

      // Add buttons
      center.add(create);
      center.add(cancel);
      center.add(help);

      // Attach action-listeners
      cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          dialog.dispose();
        }
      });

      create.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          create.setEnabled(false);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              doConversion();
            }
          });
          create.setEnabled(true);
        }
      });

      // Add to container
      container.add(BorderLayout.CENTER, center);
    }

    // Create "South" panel
    {
      container.add(BorderLayout.SOUTH, getStatus());
    }

    // Return result
    return container;
  }

  private void addComponent(JPanel container, GridBagConstraints gbc,
      JComponent component) {
    ((GridBagLayout) container.getLayout()).setConstraints(component, gbc);

    // Add to container
    container.add(component);
  }

  public void display() {
    setDefaultStatusMessage();

    dialog.show();
  }

  private void setDefaultStatusMessage() {
    getStatus().setStatus("RefactorIT");
  }

  void doConversion() {
    Project project = getProject();

    // Do the conversion-stuff
    try {
      project.getProjectLoader().build(null, false);

      // The path to output directory
      String destination = output.getText().trim();

      // Make sure the output directory is valid
      {
        // Detect illegal user input
        if (destination.length() == 0) {
          RitDialog.showMessageDialog(dialog.getContext(),
              "Output path is missing", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // Ensure directory's existence
        File dir = new File(destination);

        if (dir.exists() == false) {
          // Ask user's confirmation
          int res = RitDialog.showConfirmDialog(dialog.getContext(),
              "Would you like to create the missing directory " +
              destination + "?", "Question", JOptionPane.YES_NO_OPTION);
          switch (res) {
            case JOptionPane.YES_OPTION:
              if (dir.mkdirs() == false) {
                RitDialog.showMessageDialog(dialog.getContext(),
                    "Failed to create directory", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
              }
              break;

            default:
              // Complain and bail out
              RitDialog.showMessageDialog(dialog.getContext(),
                  "Operation cancelled at user request", "Error",
                  JOptionPane.ERROR_MESSAGE);
              return;
          }
        }

        if (dir == null || dir.exists() == false || dir.isDirectory() == false) {
          RitDialog.showMessageDialog(dialog.getContext(),
              "Output path does not denote a directory", "Error",
              JOptionPane.ERROR_MESSAGE);
          return;
        }
      }

      // The list of source files to edit
      List sources = project.getCompilationUnits();

      // Iterate through all sources available
      for (int pos = 0, max = (sources != null ? sources.size() : 0); pos < max;
          pos++) {
        CompilationUnit current = (CompilationUnit) sources.get(pos);

        // Update status bar
        getStatus().setStatus("Converting.. (" + (pos) + "/" + (max) + ")");
        getStatus().paint(getStatus().getGraphics()); // Force the change to take place

        if (current.getSource() != null) {
          HTMLSourceEditor.doEditing(destination, current, number.isSelected());
        }
      }

      // Print stylesheet
      createStylesheet(destination);
    } catch (Exception exception) {
      System.err.println("RefactorIT EXCEPTION -- PLEASE REPORT");
      exception.printStackTrace();

      String message = exception.getMessage();
      if ("".equals(message) || message == null) {
        message = exception.getClass().getName();
      }

      RitDialog.showMessageDialog(dialog.getContext(),
          message, "Error", JOptionPane.ERROR_MESSAGE);

      return;
    } finally {
      setDefaultStatusMessage();
    }

    dialog.dispose();
  }

  /**
   */
  private void createStylesheet(String directory) throws IOException {
    Writer writer = null;

    try {
      // Init writer
      writer = new FileWriter(new File(directory, "source.css"));

      // Style for keywords
      writer.write("CODE.kw {	color: rgb(0, 0, 255) }\n");
      writer.write("CODE.kw { font-weight: 550 }\n");

      // Style for literal text
      writer.write("CODE.lt { color: green }\n");

      // Style for numeric tokens
      writer.write("CODE.nm { color: rgb(192, 0, 0) }\n");

      // Style for BinItem types
      writer.write("CODE.def { font-weight: bold }\n");
      writer.write("CODE.def { color: rgb(192, 0, 192) }\n");

      // Style for BinItem references
      writer.write("CODE.url { color: rgb(0, 128, 128) }\n");

      // Style for line numbers
      writer.write("TD.line { color: rgb(192, 0, 0); padding-right: 5px; text-align: right }\n");

      writer.write("TD.src { white-space: pre; font-family: monospace }\n");
    } finally {
      // Close writer
      if (writer != null) {
        writer.flush();
        writer.close();
      }
    }
  }

  //
  // Accessor methods
  //

  private JStatus getStatus() {
    return this.status;
  }

  private Project getProject() {
    return this.project;
  }

  public void setProject(Project project) {
    this.project = project;
  }
}
