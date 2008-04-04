/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.type;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.tree.JTypeInfoPanel;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * Shows TypeInfoPanel.
 *
 * @author Vladislav Vislogubov
 */
public class TypeInfoDialog implements ProjectChangedListener {
  final RitDialog dialog;

  final JPanel emptyPanel = new JPanel();

  JButton buttonClose = new JButton("Close");
  JToggleButton buttonDetails = new JToggleButton("Details");

  boolean empty;

  JPanel typePanelHolder;
  JTypeInfoPanel typePanel;

  BinItemReference binRef;
  RefactorItContext context;

  public TypeInfoDialog(RefactorItContext context, JTypeInfoPanel typePanel) {
    this.typePanel = typePanel;
    this.context = context;

    dialog = RitDialog.create(context);
    dialog.setTitle("Hierarchy for " +
        typePanel.getBinCIType().getQualifiedName());

    context.getProject().getProjectLoader().addProjectChangedListener(this);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    boolean show = GlobalOptions.getOption("module.type.javadoc").
        equalsIgnoreCase("true");

    if (show) {
      dialog.setSize(650, 500);
    } else {
      dialog.setSize(400, 500);
    }

    buttonDetails.setSelected(show);

    dialog.getRootPane().setDefaultButton(buttonClose);
    SwingUtil.addEscapeListener(dialog);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            buttonClose.requestFocus();
          }
        });
      }
    });
  }

  public void dispose() {
    context.getProject().getProjectLoader().removeProjectChangedListener(this);
    dialog.dispose();
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "The Hierarchy view offers two different ways to look at a type hierarchy (use the toolbar buttons to alternate between them)"
        //+"Show the Supertype Hierarchy displays the supertype hierarchy of a type. This view shows all supertypes and the hierarchy of all implemented interfaces. The selected type is always shown in the top-left corner.\n"+
        //+"Show the Subtype Hierarchy displays the subtype hierarchy of a type. This view shows all subtypes of the selected type and all implementers of the selected interface (if the view is opened on an interface)."
        );
  }

  private JComponent createCenterPanel() {
    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 1, 3),
        BorderFactory.createEtchedBorder()
        ));

    center.add(createMessagePanel(), BorderLayout.NORTH);
    typePanelHolder = new JPanel(new BorderLayout());
    typePanelHolder.add(typePanel, BorderLayout.CENTER);
    center.add(typePanelHolder, BorderLayout.CENTER);

    return center;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));

    buttonClose.setMnemonic(KeyEvent.VK_C);
    buttonClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPanel.add(buttonClose);

    buttonDetails.setMnemonic(KeyEvent.VK_D);
    buttonDetails.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean show = buttonDetails.isSelected();
        if (show) {
          dialog.setSize(dialog.getWidth() + 250, dialog.getHeight());
        } else {
          dialog.setSize(dialog.getWidth() - 250, dialog.getHeight());
        }

        typePanel.showJavaDoc(show);
        dialog.getRootPane().validate();
      }
    });
    buttonPanel.add(buttonDetails);

    JButton buttonHelp = new JButton("Help");
    buttonHelp.setMnemonic(KeyEvent.VK_H);
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.typeInfo");
    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 60, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonClose.setNextFocusableComponent(buttonDetails);
    buttonDetails.setNextFocusableComponent(buttonHelp);
    buttonHelp.setNextFocusableComponent(typePanel);

    return downPanel;
  }

  /**
   * @param project the old project generation that is going to be replaced by a new one by
   * this function caller.
   */
  public void rebuildStarted(Project project) {
    if (binRef != null) {
      return;
    }

    binRef = typePanel.getBinCIType().createReference();

    try {
      SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
        public void run() {
          if (typePanel != null) {
            typePanel.setBinCIType(null, null);
          }

          typePanelHolder.removeAll();
          typePanelHolder.add(emptyPanel);

          typePanelHolder.validate();
          typePanelHolder.repaint();

          if (empty) {
            binRef = null;
          } else {
            empty = true;
          }
        }
      });
    } catch (Exception ignore) {
      ignore.printStackTrace(System.err);
    }
  }

  /**
   * @param project the project on what the rebuild was performed.
   */
  public void rebuildPerformed(final Project project) {
    if (binRef == null) {
      return;
    }

    try {
      SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
        public void run() {
          Object bin = binRef.restore(project);
          if (bin == null || !(bin instanceof BinCIType)) {
            return;
          }

          if (empty) {
            typePanel.setBinCIType((BinCIType) bin, context);
            typePanelHolder.removeAll();
            typePanelHolder.add(typePanel, BorderLayout.CENTER);
            typePanelHolder.validate();
            typePanelHolder.repaint();
            empty = false;
          }
          binRef = null;
        }
      });
    } catch (Exception ignore) {
      ignore.printStackTrace(System.err);
    }
  }

  public void show() {
    dialog.show();
  }
}
