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
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.ProfileType;

import org.w3c.dom.Element;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 *
 * @author Arseni Grigorjev
 */
public class MethodCallsMethodOptionsPanel extends AuditOptionsSubPanel{
  //simpleAuditOptionsPanel skipPanel;
	DefaultAuditOptionsSubPanel skipPanel;
	
  public MethodCallsMethodOptionsPanel(final String auditKey, 
      ProfileType config, ResourceBundle resLocalizedStrings){
    super(auditKey, config, resLocalizedStrings);
    
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    // define radio buttons
    JRadioButton radioAll 
        = new JRadioButton(getDisplayTextAsHTML("find_all.name"));
    radioAll.setActionCommand("0");
    radioAll.setSelected(true);
    
    JRadioButton radioSup 
        = new JRadioButton(getDisplayTextAsHTML("find_sup.name"));
    radioSup.setActionCommand("1");
    
    // define group for radio button, add group to option fields hashmap
    ButtonGroup group = new ButtonGroup();
    group.add(radioAll);
    group.add(radioSup);
    putOptionField("find", group);
    
    // add listener for radio buttons
    RadioListener listener = new RadioListener();
    radioAll.addActionListener(listener);
    radioSup.addActionListener(listener);
    
    // define sub-panel (default panel with checkboxes)
    String[] options = {"delegation", "proxy", "super"};
    //simpleAuditOptionsPanel skipPanel = new simpleAuditOptionsPanel(
    DefaultAuditOptionsSubPanel skipPanel = new DefaultAuditOptionsSubPanel(
      auditKey, options, config, resLocalizedStrings);
    this.skipPanel = skipPanel;
    
    // add components on main panel using GridBagLayout
    JPanel panel = new JPanel(new GridBagLayout());
    this.add(panel);
    
    panel.setLayout(new GridBagLayout());
    GridBagConstraints constr = new GridBagConstraints();
    
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(2, 4, 2, 4);
    
    constr.gridy = 0;
    panel.add(radioAll, constr);
    constr.gridy = 1;
    constr.insets.left += 20;
    panel.add(skipPanel, constr);
    constr.gridy = 2;
    constr.insets.left -= 20;
    panel.add(radioSup, constr);
  }
  
  class RadioListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      final Element skipElement = getOptionsElement();
      if (skipElement != null) {
        skipElement.setAttribute(
            "find", e.getActionCommand());
      }
      if (e.getActionCommand().equals("0")){
        skipPanel.setCheckBoxesEnabled(true);
      } else {
        skipPanel.setCheckBoxesEnabled(false);
      }
    }
  }
  
  public void setProfile(Profile profile) {
    super.setProfile(profile);
    this.skipPanel.setProfile(profile);
    
    Element skipElement = getOptionsElement();
    if (skipElement != null) {
      if (skipElement.getAttribute("find").equals("0")){
        this.skipPanel.setCheckBoxesEnabled(true);
      } else {
        this.skipPanel.setCheckBoxesEnabled(false);
      }
    }
  }
}
