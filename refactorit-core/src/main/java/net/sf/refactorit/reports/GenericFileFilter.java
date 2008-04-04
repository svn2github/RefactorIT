/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.reports;

import javax.swing.filechooser.FileFilter;

import java.io.File;

public class GenericFileFilter extends FileFilter {

  private String description;
  private String[] acceptedExtensions;
  
  private int index;
  
  public GenericFileFilter(String description, String[] acceptedExtensions) {
    if(acceptedExtensions.length == 0) {
      throw new IllegalArgumentException("One or more accepted extensions shall be specified");
    }
    
    if(description.trim().length() == 0) {
      throw new IllegalArgumentException("description shall be not empty");
    }
    
    this.description = description;
    this.acceptedExtensions = acceptedExtensions;
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    
    return isExtensionAccepted(f);
  }

  public boolean isExtensionAccepted(final File f) {
    String fileName = f.getName();
    for(index = 0; index < acceptedExtensions.length; index++) {
      if(fileName.endsWith(acceptedExtensions[index])) {
        return true;
      }
    }
    return false;
  }
  
  public String getFirstAvailableExtension() {
    return acceptedExtensions[0];
  }

  public String getDescription() {
    return this.description;
  }

}
