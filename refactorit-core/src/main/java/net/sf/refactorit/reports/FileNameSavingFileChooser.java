/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import javax.swing.JFileChooser;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * this file chooser saves file name while swithing FileFilters
 */
public class FileNameSavingFileChooser extends JFileChooser {
  
  private String fileName = null;

  public FileNameSavingFileChooser() {
    super();
    initializeFileNameSaving();
  }

  private void initializeFileNameSaving() {
    FileChooserUI chooserUI = this.getUI();
    if(chooserUI instanceof BasicFileChooserUI) {
      final BasicFileChooserUI basicChooserUI = (BasicFileChooserUI) chooserUI;
      
      this.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY,
        new PropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent evt) {
            setFileName(basicChooserUI.getFileName());
          }
        
        });
      
      basicChooserUI.getModel().addListDataListener(new ListDataListener() {
        
        public void contentsChanged(ListDataEvent e) {
          basicChooserUI.setFileName(getFileName());
        }

        public void intervalAdded(ListDataEvent e) {
        }

        public void intervalRemoved(ListDataEvent e) {
        }
     });
    }
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

}
