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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Font;
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
public class ParameterOrderOptionsPanel extends AuditOptionsSubPanel{
  private final int MIN = 50;
  private final int MAX = 100;
  private final int DEFAULT = 65;
  
  public ParameterOrderOptionsPanel(final String auditKey, 
      ProfileType config, ResourceBundle resLocalizedStrings){
    super(auditKey, config, resLocalizedStrings);
    
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
    final JPanel panel = new JPanel(new GridBagLayout());
    this.add(panel);
                   
    final JSlider slider = new JSlider(MIN, MAX, DEFAULT);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(5);
    slider.setPaintTicks(true);
    slider.setSnapToTicks(true);
        
    final JTextField curValue = new JTextField(""+DEFAULT, 3);
    curValue.setEnabled(false);
    curValue.setEditable(false);
    curValue.setBackground(getBackground());
    curValue.setDisabledTextColor(curValue.getForeground());
    curValue.setHorizontalAlignment(JTextField.CENTER);
    curValue.transferFocus();
    
    JLabel head = new JLabel(getDisplayTextAsHTML("header.text"));
    JLabel foot = new JLabel(getDisplayTextAsHTML("footer.text"));
    Font font = foot.getFont();
    foot.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()-1));
    
    GridBagConstraints constr = new GridBagConstraints();
    constr.insets = new Insets(4, 4, 4, 4);
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
        
    constr.gridwidth = 2;
    constr.gridx = 0;
    constr.gridy = 0;
    panel.add(head, constr);
    
    constr.gridwidth = 1;
    constr.gridy = 1;
                
    constr.gridx = 0;
    panel.add(slider, constr);
    constr.gridx = 1;
    panel.add(curValue, constr);
    
    constr.anchor = GridBagConstraints.CENTER;
    constr.gridwidth = 2;
    constr.gridy = 2;
    constr.gridx = 0;
    panel.add(foot, constr);
    
    slider.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        int number = source.getValue();
        curValue.setText(""+number);
        panel.repaint();
      }
    });
    
    slider.addFocusListener(new FocusListener(){
      public void focusGained(FocusEvent e) {};
        
      public void focusLost(FocusEvent e) { 
        final Element optionsElement = getOptionsElement();

        if (optionsElement != null) {
          optionsElement.setAttribute("precision", ""+slider.getValue());
        }
      }
    });
    
    putOptionField("precision", slider);
    putOptionField("precision", curValue);
  }
  
}
