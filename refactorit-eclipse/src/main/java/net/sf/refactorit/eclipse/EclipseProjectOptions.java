/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/**
 * EclipseProjectOptions
 * 
 * @author Tõnis Vaga
 * @author Jevgeni Holodkov
 */
public class EclipseProjectOptions extends ProjectOptions {
  private static final String RIT_QUALIFIER = "net.sf.refactorit";  

  private Properties properties;
  private File propertiesFile;

  public EclipseProjectOptions(IProject project) {
    Assert.must( project != null && project.exists());
    
    IPath location = project.getLocation();
    if (location == null) {
      // cannot happen with the project resources
      throw new RuntimeException("Project [" + project.getName() + "]" +
      		" location cannot be determined, cannot continue.");
    }

    IPath directoryPath = location.append(".settings");

    IPath filePath = directoryPath.append(RIT_QUALIFIER + ".prefs");
    File file = filePath.toFile();

    try {
      directoryPath.toFile().mkdir();
    } catch (SecurityException e) {
      throw new RuntimeException(
          "Error occured while loading the project settings file: directory '" + 
          directoryPath + "' cannot be created because of: " + e);
    }

    this.propertiesFile = file;

    if (file.exists()) {
        try {
          this.properties = load(file);
        } catch (IOException e) {
          throw new RuntimeException(
              "Error occured while loading the project settings file" +
              " [" + file.toString() + "]: " + e.getMessage());
      }
    } else {
      this.properties = new Properties();
    } 
  }

  private static Properties load(File file) throws IOException {
    Properties result = new Properties();
    InputStream input = null;

    Assert.must(file.exists());

    try {
      input = new FileInputStream(file);

      result = new Properties();
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
    return (String) properties.get(propertyName);
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
  
  public void serialize() {
    super.serialize();

    try {
      store();
    } catch (IOException e) {
        throw new RuntimeException(
            "Error occured while saving the project settings file: " +
            e.getMessage());
    }
  }
}
