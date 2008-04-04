/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;

import net.sf.refactorit.common.util.CollectionUtil;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

import java.util.Properties;


public class NBProject {
  private static final Logger log = Logger.getLogger(NBProject.class);
  
  private FileObject projectFile;
  private Properties properties;

  public NBProject(FileObject projectFile, Properties properties) {
    this.projectFile = projectFile;
    this.properties = properties;
  }

  public Properties getAllPropertiesCloned() {
    return CollectionUtil.clone(this.properties);
  }
  
  public void setAllPropertiesFrom(Properties newProperties) {
    saveProperties(CollectionUtil.clone(newProperties));
  }
  
  public void setProperty(String name, String value) {
    properties.setProperty(name, value);
    saveProperties(properties);
  }

  public String getProperty(String name) {
    return properties.getProperty(name);
  }

  public void saveProperties(Properties newProperties) {
    this.properties = newProperties;

    try {
      FileLock lock = projectFile.lock();
      try {
        newProperties.store(projectFile.getOutputStream(lock), "");
      } finally {
        lock.releaseLock();
      }
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
  }
}
