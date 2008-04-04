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
import net.sf.refactorit.ui.options.profile.auditoptions.AuditOptionsPrioritySubPanel;
import net.sf.refactorit.ui.options.profile.auditoptions.DefaultAuditOptionsSubPanel;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

/**
 * 
 * @author Oleg Tsernetsov
 *
 *	Wrapper class for two Options panels
 *
 */

public class AuditOptionsPanel extends JPanel implements OptionsPanel{
	//private Profile profile = null;
	private AuditOptionsPrioritySubPanel priorityPanel = null;
	private AuditOptionsSubPanel optionsPanel = null;
	
  public AuditOptionsPanel(final String key, ProfileType config,
      AuditOptionsSubPanel p) {
    
  }
  
	public AuditOptionsPanel(final String auditKey, ProfileType config,
		      AuditOptionsSubPanel p, ResourceBundle resLocalizedStrings){
		super(new BorderLayout());
		super.setBorder(new EmptyBorder(3,15,3,15));
		addPriorityPanel(new AuditOptionsPrioritySubPanel(auditKey, config, resLocalizedStrings));
		addOptionsPanel(p);
	}
	
	public AuditOptionsPanel(final String auditKey, final String[] options, 
		      ProfileType config, ResourceBundle resLocalizedStrings){
		super(new BorderLayout());
		super.setBorder(new EmptyBorder(3,15,3,15));
		addPriorityPanel(new AuditOptionsPrioritySubPanel(auditKey, config, resLocalizedStrings));
		addOptionsPanel(new DefaultAuditOptionsSubPanel(auditKey, options,  config, resLocalizedStrings));
	}
	
	public void addPriorityPanel(AuditOptionsPrioritySubPanel priorityPanel){
		this.priorityPanel = priorityPanel;
		if(priorityPanel != null){
			add(priorityPanel, BorderLayout.NORTH);
		}
	}
	
	public void addOptionsPanel(AuditOptionsSubPanel optionsPanel){
		this.optionsPanel = optionsPanel;
		if(optionsPanel != null){
			add(optionsPanel, BorderLayout.WEST);
		}
	}
	
	public AuditOptionsPrioritySubPanel getPriorityPanel(){
		return priorityPanel;
	}
	
	public AuditOptionsSubPanel getOptionsPanel(){
		return optionsPanel;
	}
	
	public void setProfile(Profile profile) {
	    if(optionsPanel != null){
	    	optionsPanel.setProfile(profile);
	    }
	    if(priorityPanel != null){
	    	priorityPanel.setProfile(profile);
	    }
	}
  
  public void setTreeNode(CheckTreeNode treeNode) {
    if(optionsPanel != null){
      optionsPanel.setTreeNode(treeNode);
    }
    if(priorityPanel != null){
      priorityPanel.setTreeNode(treeNode);
    }
  }
  
  public CheckTreeNode getTreeNode(){
    if(priorityPanel != null){
      return priorityPanel.getTreeNode();
    }
    return null;
  }
  
}
