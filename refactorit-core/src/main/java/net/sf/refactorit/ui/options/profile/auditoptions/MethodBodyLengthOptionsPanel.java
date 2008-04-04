/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile.auditoptions;


import net.sf.refactorit.ui.options.profile.AuditOptionsSubPanel;
import net.sf.refactorit.ui.options.profile.ProfileType;

import org.w3c.dom.Element;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;

/**
 *
 * @author Arseni Grigorjev
 */
public class MethodBodyLengthOptionsPanel extends AuditOptionsSubPanel{
  
  public MethodBodyLengthOptionsPanel(final String auditKey, 
      ProfileType config, ResourceBundle resLocalizedStrings){
    super(auditKey, config, resLocalizedStrings);
    
    super.setLayout(new BorderLayout());

    JPanel panel = new JPanel(new GridBagLayout());
    this.add(panel);
    panel.setLayout(new GridBagLayout());
    GridBagConstraints constr = new GridBagConstraints();
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(4, 4, 4, 4);
    
    String[] textFieldOptions = {"min_value","max_value"};
    
    // add header text
    constr.gridy = 0;
    constr.gridwidth = 3;
    panel.add(new JLabel(getDisplayText("head_text")),  constr);
    
    for (int i = 0; i < textFieldOptions.length; i++) { // For every metric
      final String option = textFieldOptions[i];
      
      // constraint options for these rows
      constr.gridy = i+1;
      constr.gridwidth = 1;
      
      // add check boxes for these options
      final JCheckBox checkBox 
          = new JCheckBox(getDisplayTextAsHTML(option+"_text"));
      
      constr.gridx = 0;
      panel.add(checkBox, constr);
      putOptionField("allow_"+option, checkBox);
      addCheckBoxListener(checkBox, "allow_"+option);
      
      // ADD TEXT FIELD
      final JTextField textField = new JTextField();
      textField.setHorizontalAlignment(JTextField.RIGHT);
      textField.setColumns(4);
      
      constr.gridx = 1;
      panel.add(textField, constr);
      putOptionField(option, textField);

      textField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {};
        
        // Update and validate options fields on lost focus
        public void focusLost(FocusEvent e) { 
          if (!validateTextfield(textField.getText())) {
            textField.setText("0");
          }

          final Element optionsElement = getOptionsElement();

          if (optionsElement != null) {
            // Get user defined value
            optionsElement.setAttribute(option, textField.getText());
          }
        }
      });
               
      // add text shared by both rows
      JLabel label = new JLabel(getDisplayText("shared_text"));
      constr.gridx = 2;
      panel.add(label, constr);
    }
    
    // add  checkbox for getter/setter methods skiping option
    final JCheckBox checkBoxGetters 
        = new JCheckBox(getDisplayTextAsHTML("skip_getters.name"));
    constr.gridx = 0;
    constr.gridy = 3;
    constr.gridwidth = 3;
    panel.add(checkBoxGetters, constr);
    putOptionField("skip_getters", checkBoxGetters);
    addCheckBoxListener(checkBoxGetters, "skip_getters");
  }
    
  public boolean validateTextfield(String text) {
    int nPoint = 0;
    int length = text.length();

    if (length > 5) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      if ((text.charAt(i) < '0') || (text.charAt(i) > '9')) {
        if ((text.charAt(i) != '.')) {
          return false;
        } else {
          nPoint++; // No more than one point
        }
      }
    }

    if (nPoint > 1) {
      return false;
    }

    return true;
  }
}
