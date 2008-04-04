/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.projectoptions;


import net.sf.refactorit.ui.projectoptions.PropertyPersistance;
import oracle.ide.Ide;
import oracle.ide.panels.TraversableContext;
import oracle.ide.util.Copyable;
import oracle.jdeveloper.model.JProject;
import oracle.jdeveloper.model.JProjectConfiguration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;



/**
 * RefactorIT project options data object -- holds all project options for a
 * single project config. In JDev there can be many configurations per project,
 * and each of these configurations has its own instance of this object.
 *
 * @author  tanel
 * @author  risto
 */
public class ProjectConfiguration
    implements Serializable, Copyable, PropertyPersistance
{
  public static final String DATA_KEY = "RefactorIT";
  public static final String PROP_CACHEPATH = "RefactorIT.cachepath";
  public static final String PROP_IGNORED_SOURCEPATH = "ignored_sourcepath";
  public static final String PROP_JAVADOCPATH = "javadocpath";
  
  
  private HashMap propertyNamesToValues = new HashMap();

  /**
   * Default constructor
   **/
  public ProjectConfiguration () {
    super();
  }
  
  /**
   * Copy constructor
   **/
  public ProjectConfiguration (ProjectConfiguration target) {
    super();
    target.copyTo(this);
  }
  
  public Object copyTo( Object target ) {
    final ProjectConfiguration copy = (target != null)
      ? (ProjectConfiguration) target : new ProjectConfiguration();

    copyToImpl(copy);

    return copy;
  } 
  
  protected final void copyToImpl(ProjectConfiguration copy) { 
    for (Iterator i = this.propertyNamesToValues.keySet().iterator(); i.hasNext();) {
      String propertyName = (String) i.next();
      copy.set(propertyName, get(propertyName));
    }
  }
  
  public String get(String propertyName) {
    return (String) this.propertyNamesToValues.get(propertyName);
  }
  
  public void set(String propertyName, String value) {
    this.propertyNamesToValues.put(propertyName, value);
  }

  /** For JDeveloper's automatic serialization -- enforces saving of the HashMap */
  public HashMap getInternalHashMap() {
    return this.propertyNamesToValues;
  }
  
  /** For JDeveloper's automatic deserialization -- enforces saving of the HashMap */
  public void setInternalHashMap(HashMap map) {
    this.propertyNamesToValues = map;
  }
  
  public String toString() {
    return "ProjectConfiguration[]";
  }
  
  public String getCachePath() {
    return (String) propertyNamesToValues.get(PROP_CACHEPATH);
  }
  
  public void setCachePath(String cachePath) {
    propertyNamesToValues.put(PROP_CACHEPATH, cachePath);
  }
  
  static ProjectConfiguration getProjectConfiguration(TraversableContext dataContext) {
    JProjectConfiguration jdevConf = (JProjectConfiguration)
        dataContext.find(JProjectConfiguration.DATA_KEY);

    if (jdevConf != null) {
      ProjectConfiguration refactorItConf = (ProjectConfiguration)
          jdevConf.getConfigDataByName(DATA_KEY);

      if (refactorItConf != null) {
        return refactorItConf;
      }
    }

    return null;
  }
  
  public static ProjectConfiguration getActiveInstance() {
    JProject project = (JProject) Ide.getActiveProject();
    //System.out.println(project.getActiveConfigDataByName(DATA_KEY));
    return (ProjectConfiguration) project.getActiveConfigDataByName(DATA_KEY);
  }
}
