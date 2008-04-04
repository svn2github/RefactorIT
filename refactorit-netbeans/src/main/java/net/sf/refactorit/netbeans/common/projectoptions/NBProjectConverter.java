/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;

// refactory classes

import net.sf.refactorit.common.util.SerializeUtil;
import net.sf.refactorit.netbeans.common.RefactorItOptions;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 */
public class NBProjectConverter {
  private static final Logger log = Logger.getLogger(NBProjectConverter.class);
  
  /**
   */
  public static NBProjectConverter getConverter() {
    return new NBProjectConverter();
  }

  /**
   * If refactorit settings file contains the 1.2 version then return false,
   * otherwise return true.
   */
  public boolean isUpdateNeededFor(FileObject projectFile) {
    // System.out.println("isUpdateNeeded for:"+projectFile.getName());
    Properties props = new Properties();
    try {
      props.load(projectFile.getInputStream());
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return false;
    }
    String version = props.getProperty("RefactorIT_Settings_Version");
    if (version == null) {
      return true;
    }
    return false;
  }

  /**
   * Convert the settings from netbeans/forte to refactorit settings file
   * and update the option "RefactorIT_Settings_Version=1.2" ,so that
   * next time the conversion is missed.
   */
  public void updateProjectSettings(FileObject projectFile) {
    //System.out.println("Starting to convert:"+projectFile.getName());
    // get all old version keys.
    RefactorItOptions options = RefactorItOptions.getDefault();
    Map oldProps = options.getNewProperties();
    Iterator oldPropsEnum = oldProps.keySet().iterator();

    // load the exisiting project properties.
    Properties props = new Properties();
    try {
      props.load(projectFile.getInputStream());
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return;
    }
    NBProject project = new NBProject(projectFile, props);

    // update the props from old props.
    while (oldPropsEnum.hasNext()) {
      String propName = (String) oldPropsEnum.next();
      Object propValue = oldProps.get(propName);
      String serializedPropValue = null;
      try {
        serializedPropValue = SerializeUtil.serializeToString(propValue);
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
        continue;
      }
      props.setProperty(propName, serializedPropValue);
    }
    // set the version to 1.2, so that next time the update is not done.
    String serializedVersion = null;
    try {
      serializedVersion = SerializeUtil.serializeToString("1.2");
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return;
    }
    props.setProperty("RefactorIT_Settings_Version", serializedVersion);
    project.saveProperties(props);
  }
}
