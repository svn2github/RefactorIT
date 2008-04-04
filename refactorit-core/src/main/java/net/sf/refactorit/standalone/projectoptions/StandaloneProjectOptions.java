/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.projectoptions;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


public class StandaloneProjectOptions extends ProjectOptions {

  private Properties properties;
  private File propertiesFile;

  /**
   * 
   * @param file or null
   * @throws IOException if file doesn't exist
   */
  public StandaloneProjectOptions(File file) throws IOException {
    this.propertiesFile=file;
    if ( file == null ) {
      this.properties=new Properties();
    } else {
      this.properties=load(file);
      
    }
    
  }
  private static Properties load(File file) throws IOException {
    Properties result=new Properties();
    InputStream input = null;
    
    Assert.must(file.exists());
    
    try {
      input= new FileInputStream(file);
      result=new Properties();
      result.load(input) ;
    } finally {
      
      // Perform CleanUp
      if (input != null) {
        input.close();
      }
      
    } 
    
    return result;
  }


  public void set(String propertyName, String value) {
    properties.put(propertyName, value);
  }

  /** null if not found */
  public String get(String propertyName) {
    return (String) properties.get(
        propertyName);
  }
  
  public void store() throws IOException {
    
    OutputStream output = null;
    
    try {
      output = new FileOutputStream(propertiesFile);
      properties.store(output, null);
    } finally {
      
      // Perform CleanUp
      if (output != null) {
        output.close();
      }
    }
  }


}
