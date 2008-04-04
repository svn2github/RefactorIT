/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.netbeans.common.standalone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.refactorit.utils.FileUtil;

/**
 *
 * @author risto
 */
public class FileBasedOptions {
  private static final String OPTIONS_DIR_NAME = ".refactorit";
  private static final String OPTIONS_FILE_NAME = "project.properties";
  
  private final File optionsFile;
  
  private Properties properties = new Properties();
  
  public FileBasedOptions(File parentFolder) throws IOException {
    File optionsDir = new File(parentFolder, OPTIONS_DIR_NAME);
    optionsFile = new File(optionsDir, OPTIONS_FILE_NAME);
  
    if( optionsExist()) {
      load();
    }
  }
  
  public boolean optionsExist() {
    return optionsFile.exists();
  }
  
  public void createOptions() throws IOException {
    if ( ! optionsFile.getParentFile().exists() ) {
      if( ! optionsFile.getParentFile().mkdirs() ) {
        throw new IOException("Failed to create folder for options: " + optionsFile.getParentFile());
      }
    }
    
    optionsFile.createNewFile();
  }

  public void setProperty(String name, String value) throws IOException {
    properties.setProperty(name, value);
    
    save();
  }

  public String getProperty(String name) {
    return properties.getProperty(name);
  }
  
  private void save() throws IOException {
    if( ! optionsFile.exists() ) {
      createOptions();
    }
    
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(optionsFile));
    try {
      properties.store(out, null);
    } finally {
      out.close();
    }
  }
  
  private void load() throws IOException {
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(optionsFile));
    try {
      properties.load(in);
    } finally {
      in.close();
    }
  }

  public Object getSnapshot() {
    return properties.clone();
  }

  public void restoreFromSnapshot(Object snapshot) throws IOException {
    properties = new Properties((Properties)snapshot);
    save();
  }

  public File getOptionsFile() {
    return optionsFile;
  }
}
