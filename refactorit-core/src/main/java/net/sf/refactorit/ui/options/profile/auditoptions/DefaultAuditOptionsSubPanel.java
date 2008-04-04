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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import java.util.Iterator;
import java.util.ResourceBundle;


/**
 * @author unknown
 * @author Arseni Grigorjev
 */
public class DefaultAuditOptionsSubPanel extends AuditOptionsSubPanel {
    
  public DefaultAuditOptionsSubPanel(final String auditKey, final String[] options, 
      ProfileType config, ResourceBundle resLocalizedStrings) {
    super(auditKey, config, resLocalizedStrings);
    setOptionsElemName("skip");
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    if(options != null)
    {
	    for (int i = 0; i < options.length; i++) {
	      final String option = options[i];
	      
	      // get string from locolized string to be shown to the user
	      final JCheckBox checkBox 
	          = new JCheckBox(getDisplayTextAsHTML(option+".name"));
	      // add this checkbox to list of option fields
	      putOptionField(option, checkBox);
	      addCheckBoxListener(checkBox, option);
	      add(checkBox);
	    }
    }

  }
  
  public void setCheckBoxesEnabled(boolean b){
    Iterator boxes = super.getOptionFields().keySet().iterator();
    while (boxes.hasNext()){
      ((JCheckBox) boxes.next()).setEnabled(b);
    }
  }
  

}
