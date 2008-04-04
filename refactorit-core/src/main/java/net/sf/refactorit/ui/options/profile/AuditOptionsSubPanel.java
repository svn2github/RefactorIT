/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import net.sf.refactorit.ui.checktree.CheckTreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class for creating custom audit options panels more easyly.<br>
 * Examples of panels made on base of this class:
 * <ul><li>DefaultAuditOptionsPanel (with checkboxes)</li>
 *     <li>MethodBodyLengthOptionsPanel (with text input fields)</li>
 *     <li>MethodCallsMethodOptionsPanel (with radio buttons)</li>
 * </ul>
 *
 * To "teach" <code>setValue()</code> to treat other JComponents you need to 
 * create a new setValue() method.<br>
 *
 * @author Arseni Grigorjev
 */
public abstract class AuditOptionsSubPanel extends JPanel implements OptionsPanel{
  private String optionsElemName = "options";
  private Profile profile = null;
  private HashMap optionFields = new HashMap();
  private String auditKey;
  private ProfileType config;
  private ResourceBundle resLocalizedStrings;
  private CheckTreeNode treeNode;
  
  public AuditOptionsSubPanel(final String auditKey, ProfileType config,
      ResourceBundle resLocalizedStrings) {
    
    super(null); // default was FlowLayout
    this.auditKey = auditKey;
    this.config = config;
    this.resLocalizedStrings = resLocalizedStrings;

  }
  
  public Element getOptionsElement() {
    if (profile == null) {
      return null;
    }

    Element rule = profile.getAuditItem(this.auditKey);
    Element optionsElement 
        = (Element) rule.getElementsByTagName(optionsElemName).item(0);
    if (optionsElement == null) {
      Document document = rule.getOwnerDocument();
      optionsElement = document.createElement(optionsElemName);
      rule.appendChild(optionsElement);
    }

    return optionsElement;
  }
  
  /**
   * Sets values from <code>profile</code> to option fields taken from 
   * <code>optionFields</code> hashmap.
   */
  public void setProfile(Profile profile) {
    this.profile = profile;

    Iterator entries = this.optionFields.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      final Element optionsElement = getOptionsElement();
      if (optionsElement != null) {
        String value = optionsElement.getAttribute((String) entry.getValue());
        setValue(entry.getKey(), value);
      }
    }
  }
  
  /**
   * Provides backward compatibility with audit tree
   *
   */
  
  public void setTreeNode(CheckTreeNode treeNode){
    this.treeNode = treeNode;
  }
  
  public CheckTreeNode getTreeNode(){
    return this.treeNode;
  }
  
  /**
   * Checks what type of JComponent is actually the entry, gives it to
   * overloaded method that knows how to set valuet to specified
   */
  protected void setValue(Object component, String value){
    if (component instanceof JCheckBox){
      setValue((JCheckBox) component, value);
    } else if (component instanceof JTextField) {
      setValue((JTextField) component, value);
    } else if (component instanceof ButtonGroup){
      setValue((ButtonGroup) component, value);
    } else if (component instanceof JSlider) {
      setValue((JSlider) component, value);
    } else if (component instanceof JList) {
      setValue((JList) component, value);
    } else if (component instanceof JComboBox){
      setValue((JComboBox)component, value);
    }
    else {
      throw new RuntimeException("AuditOptionsPanel.setValue() doesn`t know " +
          "how to handle " + component.getClass());
    }
  }
  
  private void setValue(JCheckBox checkbox, String value){
    checkbox.setSelected(Boolean.valueOf(value).booleanValue());
  }
  
  private void setValue(JTextField textfield, String value){
    textfield.setText(value);
  }
  
  private void setValue(ButtonGroup group, String value){
    Enumeration buttons = group.getElements();
    while (buttons.hasMoreElements()){
      JRadioButton button = (JRadioButton) buttons.nextElement();
      if (button.getActionCommand().equals(value)){
        button.setSelected(true);
        break;
      }
    }
  }
  
  private void setValue(JSlider slider, String value){
    try{
      slider.setValue(Integer.parseInt(value));
    } catch (NumberFormatException e){
      // ??? what should we do?
    }
  }
  
  private void setValue(JList list, String value){
    // treat ';' as separator sign
    String[] elements = value.split(";");
    DefaultListModel listModel = new DefaultListModel();
    for (int i = 0; i < elements.length; i++){
      listModel.addElement(elements[i]);
    }
    list.setModel(listModel);
  }
  
  private void setValue(JComboBox combo, String value){
    String[] elements = value.split(";");
    for(int i=0; i<elements.length; i++){
    	combo.addItem(elements[i]);
    }
  }

  /**
   * Put jcomponent into hashmap. The fields, that are put into hashmap
   * will be automatically set with values on setProfile()-method call. 
   *
   * @param option option key for this component in profile
   * @param component component to put into hashmap
   */
  public void putOptionField(String option, Object component){
    optionFields.put(component, option);
  }
  
  /**
   * Adds standard listener to check box, that changes profile on every
   * state change.
   *
   * @param checkBox checkbox to add listener to
   * @param option option key to add entry to profile
   */
  protected void addCheckBoxListener(final JCheckBox checkBox, 
      final String option) {
    checkBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Element skipElement = getOptionsElement();
        if (skipElement != null) {
          skipElement.setAttribute(
              option, checkBox.isSelected() ? "true" : "false");
        }
      }
    });
  }
  

  public void setOptionsElemName(final String optionsElemName) {
    this.optionsElemName = optionsElemName;
  }
  
  public String getDisplayText(String option){
    return resLocalizedStrings.getString(
        config.getParametersPrefix() + auditKey + "." + optionsElemName 
        + "." + option
   );
  }
    
  public String getDisplayTextAsHTML(String option){
    return "<html>" + getDisplayText(option) + "</html>";
  }

  public HashMap getOptionFields() {
    return this.optionFields;
  }
}
