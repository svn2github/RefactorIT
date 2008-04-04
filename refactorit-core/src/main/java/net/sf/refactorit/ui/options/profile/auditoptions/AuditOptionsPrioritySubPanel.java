/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile.auditoptions;

import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.options.profile.AuditOptionsSubPanel;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.ProfileType;

import org.w3c.dom.Element;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class AuditOptionsPrioritySubPanel extends AuditOptionsSubPanel {
  private Profile profile = null;

  private String auditKey;
  private ProfileType config;
  private final String attribute = Profile.PRIORITY;
  
  private CheckTreeNode treeNode;
  
  private JComboBox priorityCombo;

  public AuditOptionsPrioritySubPanel(final String auditKey,
      final ProfileType config, ResourceBundle resLocalizedStrings) {
    super(auditKey, config, resLocalizedStrings);

    this.auditKey = auditKey;
    this.config = config;
    setMaximumSize(new Dimension(420, 35));
    setLayout(new FlowLayout(FlowLayout.LEFT));

    priorityCombo = new JComboBox(Priority.getPriorityArray());
    priorityCombo.removeItem(Priority.DEFAULT);
    priorityCombo.setMinimumSize(new Dimension(140, 23));  

    putOptionField(attribute, priorityCombo);

    add(new JLabel("Audit priority:  "));
    add(priorityCombo);
  }

  /**
   * Overridden
   */

  public Element getOptionsElement() {
    if (profile == null) {
      return null;
    }

    Element rule = profile.getAuditItem(this.auditKey);
    return rule;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
    setSelection();
  }
  
  public void setSelection() {
    Element rule;
    if(profile == null)
      rule = null;
    else
      rule = profile.getAuditItem(this.auditKey);

    if (rule != null) {
      ActionListener l = null;
      if(priorityCombo.getActionListeners().length > 0) {
        l = priorityCombo.getActionListeners()[0];
        priorityCombo.removeActionListener(l);
      }
      priorityCombo.setSelectedItem(Priority.getPriorityByName(rule
          .getAttribute(attribute)));
      if (l != null) {
        priorityCombo.addActionListener(l);
      }
    }
  }
  
  public void setTreeNode(final CheckTreeNode treeNode){
    this.treeNode = treeNode;
    // ho-ho-ho, here follows horrible trash code...
    priorityCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Element element = getOptionsElement();
        if (element != null) {
          element.setAttribute(attribute, ((Priority) priorityCombo
              .getSelectedItem()).getName());
          DefaultMutableTreeNode branchNode = (DefaultMutableTreeNode)treeNode.getParent();
          if (branchNode instanceof CheckTreeNode) {
            config.refreshBranch(((CheckTreeNode)branchNode).toString());
          }
        }
      }
    });
  }

  public CheckTreeNode getTreeNode(){
    return this.treeNode;
  }
}
