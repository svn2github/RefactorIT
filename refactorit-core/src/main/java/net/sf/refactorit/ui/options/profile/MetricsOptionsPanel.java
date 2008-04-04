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

import org.w3c.dom.Element;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;


public class MetricsOptionsPanel extends JPanel implements OptionsPanel {
  private Profile profile = null;
  private String metricKey;
  private HashMap fieldsMap = new HashMap();

  public MetricsOptionsPanel(final String metricKey, final String options[],
      ProfileType config, ResourceBundle resLocalizedStrings) {
    super(new FlowLayout(FlowLayout.LEFT)); // Default was FlowLayout

    this.metricKey = metricKey;

    JPanel panel = new JPanel(new GridBagLayout());
    this.add(panel);

    GridBagConstraints constr = new GridBagConstraints();

    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(4, 4, 4, 4);

    for (int i = 0; i < options.length; i++) { // For every metric
      final String option = options[i];
      constr.gridy = i;

      // Get min and max text from LocalizedStrings
      String optionKey = config.getParametersPrefix() + "optionpanel." + option;
      JLabel label = new JLabel(resLocalizedStrings.getString(optionKey));
      constr.gridx = 0;
      panel.add(label, constr);

      final JTextField textField = new JTextField();
      textField.setHorizontalAlignment(SwingConstants.RIGHT);
      textField.setColumns(4);
      constr.gridx = 1;
      panel.add(textField, constr);
      this.fieldsMap.put(option, textField); // Get profile values

      textField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {};
        public void focusLost(FocusEvent e) { // Update and validate options fields on lost focus
          if (!validateTextfield(textField.getText())) {
            textField.setText("0");
          }

          final Element metricElement = getMetricElement();

          if (metricElement != null) {
            metricElement.setAttribute(option, textField.getText()); // Get user defined value
          }
        }
      });
    }
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

  Element getMetricElement() {
    Element metricItem = profile.getMetricsItem(this.metricKey);
    return metricItem;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;

    Iterator entries = this.fieldsMap.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();

      // Get metric threshold from profile
      String metricThreshold = profile.getAttribute(profile.getMetrics(false),
          metricKey, (String) entry.getKey());
      ((JTextField) entry.getValue()).setText(metricThreshold);
    }
  }

  public void setTreeNode(CheckTreeNode treeNode) {
  }
  
  public CheckTreeNode getTreeNode(){
    return null;
  }
}
