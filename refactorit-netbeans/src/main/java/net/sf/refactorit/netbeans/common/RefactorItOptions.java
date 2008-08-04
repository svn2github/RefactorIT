/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.SerializeUtil;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.options.GlobalOptions;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;
import org.openide.options.SystemOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NOTE: Use NBProjectOptions to get/set the properties (the new way). Some
 * methods are here only for backward compatibility (otherwise this object could
 * not be deserialized from its older versions and then users' project settings
 * would be lost when upgrading to the new version). (BTW most setters don't
 * work anymore to make sure that NB can't update these old settings.)
 * 
 * @author Vladislav Vislogubov
 * @author Risto Alas
 */
public class RefactorItOptions extends SystemOption {
  private static final Logger log = Logger.getLogger(RefactorItOptions.class);
  
  static final long                    serialVersionUID                   = -7254788893371773658L;

  public static final transient String HACK                               = "hack";
  public static final transient String PROP_COMPILE                       = "compileBeforeRefactoring";
  public static final transient String PROP_CLASSPATH                     = "userSpecifiedClassPath";
  public static final transient String PROP_CACHEPATH                     = "refactorItCachePath";
  public static final transient String PROP_SOURCEPATH                    = "userSpecifiedSourcePath";
  public static final transient String PROP_JAVADOCPATH                   = "userSpecifiedJavadocPath";
  public static final transient String PROP_AUTODETECT_PATHS              = "autodetectPaths";
  public static final transient String NEW_PROPERTIES                     = "newProperties";

  public static final transient String PROP_NEW_PROJECT_MESSAGE_DISPLAYED = "newProjectMessageDisplayed"; // invisible
                                                                                                          // to
                                                                                                          // user
  public static final transient String PROP_IS_GLOBAL_HACK                = "global";                    // invisible
                                                                                                         // to
                                                                                                         // user

  public void setHack(String s) {
    putProperty(HACK, s, true);
  }

  public String getHack() {
    return (String) getProperty(HACK);
  }

  public String displayName() {
    return GlobalOptions.REFACTORIT_NAME;
  }

  /** Options *are* project-specific -- i.e, they are *not* global. */
  public boolean isGlobal() {
    return false;
  }

  protected boolean clearSharedData() {
    return true;
  }

  public static RefactorItOptions getDefault() {
    return (RefactorItOptions) findObject(RefactorItOptions.class, true);
  }

  protected void initialize() {
    super.initialize();
  }

  /**
   * Ignored; it's here because isGlobal() needs to be described as a property
   * reader, and the existence of a property reader demands existence of a
   * corresponding property writer as well.
   */
  public void setGlobal(boolean b) {
  }

  public Map getNewProperties() {
    // Conversion
    Map oldMap = (Map) getProperty(NEW_PROPERTIES);
    if ((oldMap != null) && (containsSomething(oldMap))) {
      setNewProperties(oldMap);
      putProperty(NEW_PROPERTIES, new HashMap(), true);

      return oldMap;
    }

    Map result = (Map) getObjectFromHackString();
    if (result == null) {
      result = new HashMap();
      setNewProperties(result);
    }

    return result;
  }

  private boolean containsSomething(Map map) {
    return map.size() > 0;
  }

  public void setNewProperties(Map map) {
    saveObjectInHackString(map);
  }

  private void saveObjectInHackString(Object o) {
    try {
      setHack(SerializeUtil.serializeToString(o));
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
    }
  }

  private Object getObjectFromHackString() {
    if (getHack() == null) {
      return null;
    }
    try {
      return SerializeUtil.deserializeFromString(getHack());
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return null;
    }
  }

  public boolean getCompileBeforeRefactoring() {
    Boolean val = (Boolean) getProperty(PROP_COMPILE);

    if (val == null) {
      return false;
    } else {
      return val.booleanValue();
    }
  }

  public void setCompileBeforeRefactoring(boolean compile) {
    //putProperty( PROP_COMPILE, new Boolean(compile), true );
  }

  public boolean getNewProjectMessageDisplayed() {
    Boolean val = (Boolean) getProperty(PROP_NEW_PROJECT_MESSAGE_DISPLAYED);

    if (val == null) {
      return false;
    } else {
      return val.booleanValue();
    }
  }

  public void setNewProjectMessageDisplayed(boolean displayed) {
    //putProperty( PROP_NEW_PROJECT_MESSAGE_DISPLAYED, new Boolean(displayed),
    // true );
  }

  public boolean getAutodetectPaths() {
    Boolean val = (Boolean) getProperty(PROP_AUTODETECT_PATHS);

    if (val == null) {
      return true;
    } else {
      return val.booleanValue();
    }
  }

  public void setAutodetectPaths(boolean autodetectPaths) {
    //System.out.println( "*** setAutodetectPaths" );
    // putProperty( PROP_AUTODETECT_PATHS, new Boolean( autodetectPaths ), true
    // );
  }

  public FileObjectOrUrl[] getUserSpecifiedJavadocPath() {
    FileObjectOrUrl[] result = ((FileObjectOrUrl[]) getProperty(PROP_JAVADOCPATH));
    return (result != null || isWriteExternal()) ? result
        : new FileObjectOrUrl[0];
  }

  public void setUserSpecifiedJavadocPath(FileObjectOrUrl[] javadocPath) {
    /*
     * FileObjectOrUrl[] old = (FileObjectOrUrl[])
     * getProperty(PROP_JAVADOCPATH); putProperty(PROP_JAVADOCPATH,
     * javadocPath); firePropertyChange( PROP_JAVADOCPATH, old, javadocPath );
     */
  }

  public FileObject[] getUserSpecifiedClassPath() {
    FileObject[] result = convertFromLight((FileObjectReference[]) getProperty(PROP_CLASSPATH));
    return (result != null || isWriteExternal()) ? result : new FileObject[0];
  }

  public void setUserSpecifiedClassPath(FileObject[] classPath) {
    /*
     * FileObjectReference[] old = (FileObjectReference[])
     * getProperty(PROP_CLASSPATH); FileObjectReference[] newClassPath =
     * convertToLight(classPath); putProperty(PROP_CLASSPATH, newClassPath);
     * firePropertyChange( PROP_CLASSPATH, old, newClassPath );
     */
  }

  public FileObject[] getUserSpecifiedSourcePath() {
    FileObject[] result = convertFromLight((FileObjectReference[]) getProperty(PROP_SOURCEPATH));
    return (result != null || isWriteExternal()) ? result : new FileObject[0];
  }

  /** @deprecated */
  public void setRefactorItCachePath(String path) {
    putProperty(PROP_CACHEPATH, path);
  }

  /** @deprecated */
  public String getRefactorItCachePath() {
    return (String) getProperty(PROP_CACHEPATH);
  }

  public void setUserSpecifiedSourcePath(FileObject[] sourcePath) {
    /*
     * FileObjectReference[] old = (FileObjectReference[])
     * getProperty(PROP_SOURCEPATH); FileObjectReference[] newSourcePath =
     * convertToLight(sourcePath); putProperty(PROP_SOURCEPATH, newSourcePath);
     * firePropertyChange( PROP_SOURCEPATH, old, newSourcePath );
     */
  }

  //  private static FileObjectReference [] convertToLight(FileObject[] objects)
  // {
  //    if (objects==null) {
  //      return new FileObjectReference[0];
  //    }
  //
  //    List lightFileObjects = new ArrayList();
  //    for (int i=0; i< objects.length; i++) {
  //      if (objects[i] != null) {
  //        try {
  //          lightFileObjects.add(new FileObjectReference(objects[i]));
  //        } catch (FileStateInvalidException e) {
  //          System.err.println("Couldn't convert filesystem " +
  // objects[i].getPackageNameExt('/', '.') + ": " + e);
  //        }
  //      }
  //    }
  //
  //    return ((FileObjectReference[]) lightFileObjects.toArray(new
  // FileObjectReference[0]));
  //  }

  private static FileObject[] convertFromLight(FileObjectReference[] objects) {
    if (objects == null) {
      return new FileObject[0];
    }
    List result = new ArrayList();
    for (int i = 0; i < objects.length; i++) {
      if ((objects[i] != null) && (objects[i].getFileObject() != null)) {
        result.add(objects[i].getFileObject());
      }
    }
    return (FileObject[]) result.toArray(new FileObject[result.size()]);
  }

  public void putPropertyPublic(String key, Object value, boolean notify) {
    try {
      VersionSpecific.getInstance().setAttr(key, SerializeUtil
          .serializeToString(value));
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }
  }

  public Object getPropertyPublic(String key) {
    try {
      String attr = VersionSpecific.getInstance().getAttr(key);
      if ("".equals(attr) || attr == null) {
        return null;
      }

      return SerializeUtil.deserializeFromString(attr);
    } catch (IOException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    } catch (ClassNotFoundException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }
  }

  /**
   * Example: isInternalPropertyNull( PROP_CLASSPATH );<br>
   * <br>
   * 
   * Regular get-methods will give you a default value instead of null, so this
   * method is they only way to check whether a property is stored as "null".
   */
  public boolean isInternalPropertyNull(String key) {
    return getProperty(key) == null;
  }
}
