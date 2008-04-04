/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.options.TreeChooser;
import net.sf.refactorit.vfs.Source;

import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SourcePathEditingPanel extends AbstractPathEditingPanel {
  
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    addFromProjectFS.setEnabled(b);
  }
  private JButton addFromProjectFS = new JButton("Add From Project Paths");
  private final JComponent[] buttons;

  public SourcePathEditingPanel(boolean canAddAnyFile
  ) {
    
    initButtons();
    if ( canAddAnyFile ) {
      buttons = new JComponent[] {
          addButton, addFromProjectFS,
          removeButton, upButton, downButton
      };
          
    } else {
      buttons = new JComponent[] {
          addFromProjectFS,
          removeButton, upButton, downButton
      };
    }

  }
  
  private void initButtons() {

    addButton.addActionListener(new AddDirAction());
    
   
    final ProjectSourcePathModel srcpathModel=new ProjectSourcePathModel();
    srcpathModel.addExcludeItems(getPathItems());


    addFromProjectFS.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] urls = TreeChooser.getNewDataObjectReferences(
            IDEController.getInstance().createProjectContext(),
            "Add from project paths", srcpathModel, new SourceRenderer());

        if (urls != null) {
          for (int i = 0; i < urls.length; i++) {
            PathItem pathItem = new PathItem((Source)urls[i]);
            
            addPathItem(pathItem);
            srcpathModel.addExcludeItem(pathItem);
          }
        }
      }
    });

  }





  protected JComponent[] getButtons() {
    return buttons;
  }
}
