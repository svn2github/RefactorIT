/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


/**
 * Insert the type's description here.
 * Creation date: (5/15/2001 12:51:55 PM)
 * @author Vladislav Vislogubov
 */
public class JStatus extends JPanel {
  private JLabel label;
  /**
   * JStatus constructor comment.
   */
  public JStatus() {
    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createLoweredBevelBorder());

    GridBagConstraints constr = new GridBagConstraints();
    constr.gridx = 0;
    constr.gridy = 0;
    constr.gridwidth = 1;
    constr.gridheight = 1;
    constr.fill = GridBagConstraints.BOTH;
    constr.anchor = GridBagConstraints.CENTER;
    constr.weightx = 1.0;
    constr.weighty = 1.0;
    constr.insets = new Insets(2, 5, 2, 2);

    label = new JLabel("", JLabel.LEFT);
    label.setForeground(Color.black);
    add(label, constr);
  }

  /**
   * Insert the method's description here.
   * Creation date: (5/17/2001 17:14:05 PM)
   * @param text java.lang.String
   */
  public void setStatus(String text) {
    label.setText(text);
  }
}
