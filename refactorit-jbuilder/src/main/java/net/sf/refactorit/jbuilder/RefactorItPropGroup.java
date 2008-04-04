/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import java.io.File;
import java.util.List;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.jbuilder.optionsui.RefactorItPropPage;
import net.sf.refactorit.jbuilder.vfs.JBSourcePath;


import com.borland.jbuilder.node.JBProject;
import com.borland.primetime.PrimeTime;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.Project;
import com.borland.primetime.properties.*;


/**
 * This class contains a factory for the
 * RefactorItPropGroup.
 *
 * Provides the needed OpenTools interface required to register the
 * PropertyGroup which retains settings used by the "RefactorIT".
 * It also contains a separate UI that may be used to edit these settings
 * based on context.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */

public class RefactorItPropGroup implements PropertyGroup {
  public static final Object TOPIC = new Object();
  public static final String PAGE_NAME = "RefactorIT";
  public static final String CATEGORY = "net.sf.refactorit";

  public static final GlobalBooleanProperty CUSTOM_BRACE_MATCHER =
      new GlobalBooleanProperty(CATEGORY, "brace matcher enabled", true);

  public static final GlobalIntegerProperty CUSTOM_BRACE_MATCHER_DELAY =
      new GlobalIntegerProperty(CATEGORY, "brace matcher's activated delay",
      500);

  public static final NodeProperty PROP_CACHEPATH =
      new NodeProperty(CATEGORY, "cache path");

  public static final String SPECIFIED_SOURCEPATH = "specified sourcepath";
  public static final String SPECIFIED_JAVADOC = "specified javadoc";
  public static final String IGNORED_SOURCEPATH = "ignored sourcepath";
  public static final String SPECIFIED_CLASSPATH = "specified classpath";
  public static final String AUTODETECT_PATHS = "autodetect paths";

  private static String cachedIgnoredSourcePathStr;
  private static List cachedIgnoredSourcePaths;
  
  
  public static String getProjectProperty(String propertyName,
      String defaultValue) {
    return getActiveProject().getProperty(CATEGORY, propertyName, defaultValue);
  }

  public static void setProjectProperty(String propertyName, String value) {
    getActiveProject().setProperty(CATEGORY, propertyName, value);
  }

  public static boolean getProjectPropertyBoolean(String propertyName,
      boolean defaultValue) {
    return stringToBoolean(getProjectProperty(propertyName,
        booleanToString(defaultValue)));
  }

  public static void setProjectPropertyBoolean(String propertyName,
      boolean value) {
    setProjectProperty(propertyName, booleanToString(value));
  }

  private static boolean stringToBoolean(String s) {
    return "true".equals(s);
  }

  private static String booleanToString(boolean b) {
    if (b) {
      return "true";
    } else {
      return "false";
    }
  }

  public static JBProject getActiveProject() {
    Project project = Browser.getActiveBrowser().getActiveProject();
    if (project == null) {
      project = Browser.getDefaultProject();
    }

    if (project instanceof JBProject) {
      return (JBProject) project;
    } else {
      return null;
    }
  }

  /**
   * Provides the needed OpenTools interface required to register the
   * PropertyGroup which retains/edits settings used by the wizard.
   *
   * @param majorVersion The major version of the current OpenTools API.
   * @param minorVersion The minor version of the current OpenTools API.
   */
  public static void initOpenTool(byte majorVersion, byte minorVersion) {
    if (majorVersion == PrimeTime.CURRENT_MAJOR_VERSION) {
      PropertyManager.registerPropertyGroup(new RefactorItPropGroup());
    }
  }

  /**
   * Create PropertyPageFactory.
   *
   * Provides a class used as a factory to create properties
   * page whenever the topic matches.
   *
   * @param topic The topic for which property pages are desired.
   * @return Returns null or the factory class.
   */
  public PropertyPageFactory getPageFactory(Object topic) {
    if (topic instanceof Project) {
      return new PropertyPageFactory(PAGE_NAME, "RefactorIT options") {
        public PropertyPage createPropertyPage() {
          net.sf.refactorit.classmodel.Project project = IDEController.getInstance().getActiveProject();
          if(project != null) {
            return new RefactorItPropPage(project.getOptions());  
          } else {
            return null;
          }
        }
      };
    } else {
      return null;
    }
  }

  /**
   * Initializes PropertyGroup.
   *
   * Invoked once for a PropertyGroup to indicate that the values of all
   * global properties have been read from permanent storage and that any
   * settings that depend on these properties can be safely made.
   *
   * Only PropertyGroup instances registered during OpenTool initialization
   * will receive this notification.
   */
  public void initializeProperties() {}
  
  
  /**
   * Returns ignored source paths as a list of strings. Uses caching to speed up the process.
   * @param pathStr
   * @return
   */
  public static List getIgnoredSourcePath() {
  	String ignoredSourcepathStr = RefactorItPropGroup.getProjectProperty(RefactorItPropGroup.IGNORED_SOURCEPATH, "");
  	ignoredSourcepathStr = JBSourcePath.isOldStylePath(ignoredSourcepathStr)
				? ""
				: ignoredSourcepathStr;
    if (!ignoredSourcepathStr.equals(cachedIgnoredSourcePathStr)) {
      cachedIgnoredSourcePathStr = ignoredSourcepathStr;
      cachedIgnoredSourcePaths = JBSourcePath.pathStrAsList(ignoredSourcepathStr);
    }
    return cachedIgnoredSourcePaths;
  }  
  
  public static void setIgnoredSourcePath(List ignoredSourcePath) {
  	RefactorItPropGroup.setProjectProperty(RefactorItPropGroup.IGNORED_SOURCEPATH, 
  			StringUtil.join((String[])ignoredSourcePath.toArray(new String[0]), File.pathSeparator));
  }

}
