/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.options.profile.auditoptions.AuditOptionsPrioritySubPanel;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 
 * @author Oleg Tsernetsov
 *
 *  Wrapper class for two Options panels
 *
 */

public class AuditBranchOptionsPanel extends JPanel implements OptionsPanel{
  //private Profile profile = null;
  private AuditOptionsPrioritySubPanel priorityPanel = null;
  
  private String key;
  private Profile profile;
  private JComboBox priorityCombo;
  private CheckTreeNode treeNode;
  private ProfileType config;
  
  public AuditBranchOptionsPanel(CheckTreeNode treeNode, ProfileType config, final String key){
    this(key);
    this.config = config;
    setTreeNode(treeNode);
  }
  
  public AuditBranchOptionsPanel(final String key){
    super();
    super.setBorder(new EmptyBorder(3,15,3,15));

    setLayout(new FlowLayout(FlowLayout.LEFT));
       
    this.key = key;
    
    priorityCombo = new JComboBox(Priority.getPriorityArray());
    priorityCombo.setMinimumSize(new Dimension(140, 23));  

    priorityCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setBranchPriority(treeNode, (Priority) priorityCombo
              .getSelectedItem());
      }
    });

    add(new JLabel("Audit priority:  "));
    add(priorityCombo);
  }
  
  public void addPriorityPanel(AuditOptionsPrioritySubPanel priorityPanel){
    this.priorityPanel = priorityPanel;
    if(priorityPanel != null){
      add(priorityPanel, BorderLayout.NORTH);
    }
  }
  
  public AuditOptionsPrioritySubPanel getPriorityPanel(){
    return priorityPanel;
  }
  
  public String getKey(){
    return this.key;
  }
  
  private Priority getBranchPriority(CheckTreeNode root) {
    
    Priority resultPriority = Priority.DEFAULT;
    if (root == null) {
      return resultPriority;
    }
    Enumeration en = root.preorderEnumeration();
    Object obj = null;
    
    while (en.hasMoreElements()) {
      obj = en.nextElement();
      if (obj instanceof ProfilePanel.TreeNode) {
        
        String auditKey = ((ProfilePanel.TreeNode)obj).getKey();
        Element el = profile.getAuditItem(auditKey);
        Priority nodePriority = Priority.getPriorityByName(el.getAttribute(Profile.PRIORITY));
        if (resultPriority != nodePriority){
          if (resultPriority == Priority.DEFAULT){
            resultPriority = nodePriority;
          } else {
            resultPriority = Priority.DEFAULT;
            break;
          }
        }
      }
    }
    return resultPriority;
  }

  private void setBranchPriority(CheckTreeNode root, Priority priority) {
    if (root == null) {
      return;
    }
    Enumeration en = root.depthFirstEnumeration();
    Object obj = null;
    Profile oldProfile = null;
    
    try {
      if(priority == Priority.DEFAULT) {
        String str = profile.getFileName();
        if (str.indexOf('.') < 0) {
          str = str + ProfilePanel.PROFILE_EXTENSION;
        }
          File file = new File(str);
          oldProfile = new Profile(file);
      }
    } 
    catch (SAXException e) {
      System.err.println(e);
    }
    catch (IOException e) {
      System.err.println(e);
    }
    
    
    while (en.hasMoreElements()) {
      obj = en.nextElement();
      if (obj instanceof ProfilePanel.TreeNode) {
        String auditKey = ((ProfilePanel.TreeNode)obj).getKey();
        Element el = profile.getAuditItem(auditKey);
        if (priority != Priority.DEFAULT) {
          el.setAttribute(Profile.PRIORITY, priority.getName()); 
        } else if (oldProfile != null) {
          el.setAttribute(Profile.PRIORITY, 
              oldProfile.getAuditItem(auditKey).getAttribute(Profile.PRIORITY));
        }
      }
    }
    config.refreshAudit(key);
    if(priority == Priority.DEFAULT) {
      definePriority();
    }
  }
  
  
  public void definePriority(){
    Priority branchPriority = getBranchPriority(treeNode);
    ActionListener l = null;
    if(priorityCombo.getActionListeners().length > 0) {
      l = priorityCombo.getActionListeners()[0];
      priorityCombo.removeActionListener(l);
    }
    priorityCombo.setSelectedItem(branchPriority);
    
    if (l != null) {
      priorityCombo.addActionListener(l);
    }
  }
  
  public void setProfile(Profile profile) {
    this.profile = profile;
  }
  
  public void setTreeNode(CheckTreeNode treeNode){
    this.treeNode = treeNode;
  }
  
  public CheckTreeNode getTreeNode(){
    return this.treeNode;
  }
  
}
