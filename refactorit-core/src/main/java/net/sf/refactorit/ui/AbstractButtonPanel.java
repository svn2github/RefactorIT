/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;


/**
 * AbstractButtonPanel panel with ok, cancel and help buttons
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.2 $ $Date: 2004/12/21 14:37:42 $
 */
public abstract class AbstractButtonPanel extends JPanel {

  public final JButton buttonOk = new JButton("Ok"); //resLocalizedStrings.getString("button.next") );
  public final JButton buttonCancel = new JButton("Cancel"); //resLocalizedStrings.getString("button.cancel") );

  public final JButton helpButton = new JButton("Help");
  protected boolean isOkPressed = false;

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public AbstractButtonPanel() {
    JPanel downPanel=this;
    this.setLayout(new GridBagLayout());


    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonOk.setSelected(true);

    buttonPanel.add(buttonOk);

    buttonCancel.setDefaultCapable(false);
    buttonCancel.setMnemonic(KeyEvent.VK_C);
    buttonPanel.add(buttonCancel);

    helpButton.setMnemonic(KeyEvent.VK_H);
    buttonPanel.add(helpButton);

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
    buttonOk.setMnemonic(KeyEvent.VK_O);
    buttonCancel.setNextFocusableComponent(buttonOk);
  }
}
