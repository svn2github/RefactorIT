/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common.usesupertype;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.AbstractButtonPanel;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public abstract class AbstractUseSupertypeInputDialog {

  public AbstractUseSupertypeInputDialog(RefactorItContext context) {
    this.context = context;
    dialog = RitDialog.create(context);
  }

  protected abstract JPanel createHelpPanel();

  protected abstract JPanel createContentPanel();

  private void init() {
    initialized = true;

    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    mainPanel.add(createHelpPanel(), constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(5, 5, 5, 5);
    mainPanel.add(createContentPanel(), constraints);

    mainPanel.setPreferredSize(new Dimension(515, 350));

    JPanel container = new JPanel(new BorderLayout());
    container.add(mainPanel, BorderLayout.CENTER);
    container.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setContentPane(container);

    HelpViewer.attachHelpToDialog(dialog, buttonPanel.helpButton,
        "refact.use_supertype");

    dialog.getRootPane().setDefaultButton(buttonPanel.buttonOk);
  }

  protected final RitDialog dialog;

  class MyButtonPanel extends AbstractButtonPanel {
    MyButtonPanel() {
      super();
      buttonOk.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          isOkPressed = true;
          dialog.dispose();
        }
      });

      buttonCancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          isOkPressed = false;
          dialog.dispose();
        }
      });

    }
  }


//  private static final Logger log = AppRegistry.getLogger(UseSupertypeInputDialog.class);


  protected RefactorItContext context;

  private boolean initialized;

  protected final AbstractButtonPanel buttonPanel = new MyButtonPanel();

  public final void show() {
    if (!initialized) {
      init();
    }

    dialog.show();
  }

  /**
   * @return true if ok button was pressed
   */
  public boolean isOkPressed() {
    return buttonPanel.isOkPressed();
  }

  public abstract BinTypeRef getSelectedSupertype();

}
