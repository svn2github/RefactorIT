/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Tanel Alumae
 */

public abstract class CustomizableTextFieldProperty extends TextFieldProperty {
  private JPanel panel;

  public CustomizableTextFieldProperty(
      String propertyName, PropertyPersistance persistance
  ) {
    super(propertyName, persistance);
  }

  protected void createEditor() {
    super.createEditor();

    panel = new JPanel(new BorderLayout());

    JTextField textField = getTextField();
    panel.add(textField, BorderLayout.CENTER);

    JButton button = new JButton("..");
    button.setPreferredSize(new Dimension(20, 20));

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonClicked();
      }
    });

    panel.add(button, BorderLayout.EAST);
  }

  public JComponent getEditor() {
    loadChoiceToEditor();
    return this.panel;
  }

  protected abstract void buttonClicked();
}
