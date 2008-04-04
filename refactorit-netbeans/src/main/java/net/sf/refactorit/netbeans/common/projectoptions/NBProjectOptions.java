/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: NBProjectOptions.java,v 1.19 2005/12/09 12:03:01 anton Exp $ */
package net.sf.refactorit.netbeans.common.projectoptions;


import net.sf.refactorit.common.util.BaseFileTreeTraverseListener;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.FileObjectOrUrl;
import net.sf.refactorit.netbeans.common.RefactorItOptions;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.util.BaseFileObjectTraverseListener;
import net.sf.refactorit.netbeans.common.vfs.NBSourcePathFilter;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.utils.FileUtil;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


/**
 */
public class NBProjectOptions extends ProjectOptions {
  private static final Logger log = Logger.getLogger(NBProjectOptions.class);

  private static final String PROP_CLASSPATH =
      "NEW_FORMAT_userSpecifiedClassPath";
  private static final String PROP_SOURCEPATH =
      "NEW_FORMAT_userSpecifiedSourcePath";
  private static final String PROP_JAVADOCPATH =
      "NEW_FORMAT_userSpecifiedJavadocPath";
  private static final String PROP_AUTODETECT_PATHS =
      "NEW_FORMAT_autodetectPaths";
  private static final String PROP_CONVERT_NEEDED = "convertNeeded";
  private static final String PROP_SOURCEPATH_IGNORE =
      "ignoredDirectoriesOnSourcepath";

  private static final String PROP_NEW_PROJECT_MESSAGE_DISPLAYED =
      "NEW_FORMAT_newProjectMessageDisplayed"; // invisible to user

  private Object[][] defaultValues = {
      new Object[] {PROP_CLASSPATH, new Map[0]},
      new Object[] {PROP_SOURCEPATH, new Map[0]},
      new Object[] {PROP_JAVADOCPATH, new String[0]},
      new Object[] {PROP_AUTODETECT_PATHS, booleanToString(true)},
      new Object[] {PROP_CONVERT_NEEDED, booleanToString(true)},
      new Object[] {PROP_NEW_PROJECT_MESSAGE_DISPLAYED, booleanToString(false)},
      new Object[] {PROP_SOURCEPATH_IGNORE, new Map[0]}
      };

  private static final HashMap instances = new HashMap();
  private static final NBProjectOptions instance = new NBProjectOptions();
  private final NBSourcePathFilter sourcePathFilteringListener = new
      NBSourcePathFilter() {
    // The mapping from "source path name" to boolean (to include or not).
    SourcePathsTable sourcePathItems = new SourcePathsTable();

    /**
     */
    public boolean includePathItem(PathItemReference item) {
      //      System.out.println(" ##Processing Path Item:"+item.getDisplayNameLong());
      Boolean include = sourcePathItems.get(item);
      if (include != null) {
        return include.booleanValue();
      }

      // if include is null then we must analyse the item and ask user to include it or not.
      // analyse the item
      boolean includePathItem = true;
      // it is the border above where the directory seems to be not java files directory.
      double BORDER = 0.2;
      int MAX_FILES_TO_TRAVERSE = 100;
      FileTraverseInfo info = null;
      if (item.isLocalFile()) {
        TraverseFileTree listener = new TraverseFileTree(MAX_FILES_TO_TRAVERSE);
        File f = item.getFile();
        // if f is null then there is no such file path in local filesystem. Include it because we do not know how to handle it.
        if (f == null) {
          // sourcePathItems.put(item, Boolean.TRUE);
          return true;
        }
        FileCopier.traverse(f, listener);
        info = listener;
      } else if (item.isFileObject()) {
        TraverseFileObject listener = new TraverseFileObject(
            MAX_FILES_TO_TRAVERSE);
        FileObject file = item.getFileObject();
        // if file is null then the project's filesystem doesn't contain the file path specified. Include it because we do not know how to handle it.
        if (file == null) {
          // sourcePathItems.put(item, Boolean.TRUE);
          return true;
        }
        PathUtil.getInstance().traverseFileObject(file, listener);
        info = listener;
      } else {
        sourcePathItems.put(item, Boolean.TRUE);
        // System.out.println("[Included] Source path item:"+item.getDisplayName()+". Not an File or FileObject!");
        return true;
      }

      // check whether this path items seems to contain *.java files or not.
      double point = ((double) info.getJavaFilesCount()) / info.getTotalCount();
      // System.out.println("Item:"+item.getDisplayName()+" . Java Files:"+info.getJavaFilesCount()+" . Total:"+info.getTotalCount()+" . Point:"+point);
      if (point < BORDER) {
        includePathItem = false;
      }
      if (includePathItem == true) {
        // System.out.println("[Included] Source path item:"+item.getDisplayName()+". Java:"+info.getJavaFilesCount()+". Total:"+info.getTotalCount());
        sourcePathItems.put(item, Boolean.TRUE);
        return true;
      }

      // FIXME: Move this code out of here: this class should not have to work with UI. We already have places
      // where we do other sanity checks, let's integrate them all into one place and test it thouroughly.

      // ask user confirmation
      int n = DialogManager.getInstance().showYesNoQuestion(
          IDEController.getInstance().createProjectContext(),
          "warning.nb.bad_sourcepath",
          "Include the source path \"" + item.getDisplayNameLong()
          + "\"?\n It seems not to be a root directory for your *.java source files!\n Traversed "
          + info.getTotalCount() + " files and found only "
          + info.getJavaFilesCount() + " valid sources.",
          DialogManager.NO_BUTTON);
      if (n == DialogManager.YES_BUTTON) {
        includePathItem = true;
      } else if (n == DialogManager.NO_BUTTON) {
        includePathItem = false;
      }
      sourcePathItems.put(item, Boolean.valueOf(includePathItem));

      // String YN = includePathItem?"Y":"N";
      // System.out.println("Include Source  Path item:"+item.getDisplayName()+"["+YN+"]");

      return includePathItem;
    }

    class SourcePathsTable {
      Hashtable sourcePathItems = new Hashtable();

      public void put(PathItemReference item, Boolean include) {
        if (include.booleanValue() == true) {
          sourcePathItems.put(item.getAbsolutePath(), include);
        }

        // if include == false then save that item into ignore list.
        if (include.booleanValue() == false) {
          PathItemReference[] ignoredPathItems =
              getUserSpecifiedIgnoredSourcePathDirectories();
          ArrayList list = new ArrayList();
          for (int i = 0; i < ignoredPathItems.length; i++) {
            list.add(ignoredPathItems[i]);
          }
          list.add(item);
          PathItemReference[] newIgnoredItems = (PathItemReference[]) list.
              toArray(new PathItemReference[list.size()]);
          setUserSpecifiedIgnoredSourcePathDirectories(newIgnoredItems);
        }
      }

      public Boolean get(PathItemReference item) {
        // remove all old FALSE references.
        Enumeration sourcePathNames = sourcePathItems.keys();
        while (sourcePathNames.hasMoreElements()) {
          String pathName = (String) sourcePathNames.nextElement();
          Boolean state = (Boolean) sourcePathItems.get(pathName);
          if (state.booleanValue() == false) {
            sourcePathItems.remove(pathName);
          }
        }

        // get the up to date list. We do not know is it changed or not.
        PathItemReference[] ignoredPathItems =
            getUserSpecifiedIgnoredSourcePathDirectories();
        for (int i = 0; i < ignoredPathItems.length; i++) {
          sourcePathItems.put(ignoredPathItems[i].getAbsolutePath(),
              Boolean.FALSE);
        }
        Boolean include = (Boolean) sourcePathItems.get(item.getAbsolutePath());
        return include;
      }
    }
  };

  /** Has to be singleton because ProjectProperty editor instances need to be the same everywhere.
   * Still, allow to create one instance of project options for each ide project
   * @param ideProject*/
  public static NBProjectOptions getInstance(Object projectKey) {
    Object options = instances.get(projectKey);
    if (options == null) {
      options = new NBProjectOptions();
      instances.put(projectKey, options);
    }
    return (NBProjectOptions)options;
  }

  /**
   */
  public NBSourcePathFilter getSourcepathFilter() {
    return sourcePathFilteringListener;
  }

  private NBProjectOptions() {
    reinitializeOptionsAfterProjectChange();
  }

  /** Only called form constructor or when getValueXXX() gets null */
  private void reinitializeOptionsAfterProjectChange() {
    try {
      convertOptionsToNewFormatIfNeeded();
      initializeDefaultValues();
    } catch (Exception e) {
      // Can not use logger here? No logger inited?
      log.debug(
          "*** Happens on Anton machine, project seem to be null ***");
      log.warn(e.getMessage(), e);
    }
  }

  private void initializeDefaultValues() {
    for (int i = 0; i < defaultValues.length; i++) {
      setValueIfNull((String) defaultValues[i][0], defaultValues[i][1]);
    }
  }

  private boolean convertNeeded() {
    // Can't use regular get() here because that could force recursively new conversion in
    // case of "null" return value.

    String convertNeeded = (String) getOptions().getPropertyPublic(
        PROP_CONVERT_NEEDED);

    return convertNeeded == null || stringToBoolean(convertNeeded);
  }

  private void convertOptionsToNewFormatIfNeeded() {
    if (!convertNeeded()) {
      return;
    }

    if (mustConvertProperty(RefactorItOptions.PROP_AUTODETECT_PATHS,
        PROP_AUTODETECT_PATHS)) {
      setAutodetectPaths(getOptions().getAutodetectPaths());
    }

    if (mustConvertProperty(RefactorItOptions.PROP_NEW_PROJECT_MESSAGE_DISPLAYED,
        PROP_NEW_PROJECT_MESSAGE_DISPLAYED)) {
      setNewProjectMessageDisplayed(getOptions().getNewProjectMessageDisplayed());
    }

    if (mustConvertProperty(RefactorItOptions.PROP_JAVADOCPATH, PROP_JAVADOCPATH)) {
      FileObjectOrUrl[] items = getOptions().getUserSpecifiedJavadocPath();
      PathItemReference[] newItems = new PathItemReference[items.length];
      for (int i = 0; i < newItems.length; i++) {
        newItems[i] = new PathItemReference(items[i].getName());
      }
      setUserSpecifiedJavadocPath(newItems);
    }

    if (mustConvertProperty(RefactorItOptions.PROP_SOURCEPATH, PROP_SOURCEPATH)) {
      setUserSpecifiedSourcePath(fileObjectsArrayToPathItemsArray(getOptions().
          getUserSpecifiedSourcePath()));
    }

    if (mustConvertProperty(RefactorItOptions.PROP_CLASSPATH, PROP_CLASSPATH)) {
      setUserSpecifiedClassPath(fileObjectsArrayToPathItemsArray(getOptions().
          getUserSpecifiedClassPath()));
    }

    set(PROP_CONVERT_NEEDED, booleanToString(false));
  }

  private boolean mustConvertProperty(String fromPropertyName,
      String toPropertyName) {
    boolean oldPropertyIsNull = getOptions().isInternalPropertyNull(
        fromPropertyName);
    boolean newPropertyIsNull = getOptions().getPropertyPublic(toPropertyName) == null;

    return (!oldPropertyIsNull) && (newPropertyIsNull);
  }

  private boolean propertyMustHaveDefaultValue(String propertyName) {
    for (int i = 0; i < defaultValues.length; i++) {
      if (propertyName.equals(defaultValues[i][0])) {
        return true;
      }
    }

    return false;
  }

  private boolean propertyShouldNotBeNull(String propertyName) {
    return propertyMustHaveDefaultValue(propertyName);
  }

  public boolean getNewProjectMessageDisplayed() {
    return stringToBoolean(get(PROP_NEW_PROJECT_MESSAGE_DISPLAYED));
  }

  public void setNewProjectMessageDisplayed(boolean displayed) {
    set(PROP_NEW_PROJECT_MESSAGE_DISPLAYED, booleanToString(displayed));
  }

  public boolean getAutodetectPaths() {
    return stringToBoolean(get(PROP_AUTODETECT_PATHS));
  }

  public void setAutodetectPaths(boolean autodetectPaths) {
    set(PROP_AUTODETECT_PATHS, booleanToString(autodetectPaths));
  }

  public PathItemReference[] getUserSpecifiedJavadocPath() {
    return PathItemReference.deserializeMapArray(
        getPropertyObjectArray(PROP_JAVADOCPATH));
  }

  public void setUserSpecifiedJavadocPath(PathItemReference[] javadocPath) {
    setPropertyMapArray(PROP_JAVADOCPATH,
        PathItemReference.serializeToMapArray(javadocPath));
  }

  public PathItemReference[] getUserSpecifiedClassPath() {
    return PathItemReference.deserializeMapArray(
        getPropertyMapArray(PROP_CLASSPATH));
  }

  public void setUserSpecifiedClassPath(PathItemReference[] newClassPath) {
    setPropertyMapArray(PROP_CLASSPATH,
        PathItemReference.serializeToMapArray(newClassPath));
  }

  public PathItemReference[] getUserSpecifiedSourcePath(boolean
      includeIgnoredListItems) {
    PathItemReference[] pathItemsArray = PathItemReference.deserializeMapArray(
        getPropertyMapArray(PROP_SOURCEPATH)
        );
    if ( ! includeIgnoredListItems) {
      // execute the filter
      ArrayList pathItems = new ArrayList(pathItemsArray.length);
      for (int i = 0; i < pathItemsArray.length; i++) {
        if (pathItemsArray[i].isValid()
            && getSourcepathFilter().includePathItem(pathItemsArray[i])) {
          pathItems.add(pathItemsArray[i]);
        }
      }
      pathItemsArray = (PathItemReference[]) pathItems.toArray(
          new PathItemReference[pathItems.size()]);
    }
    return pathItemsArray;
  }

  public void setUserSpecifiedSourcePath(PathItemReference[] newSourcePath) {
    setPropertyMapArray(PROP_SOURCEPATH,
        PathItemReference.serializeToMapArray(newSourcePath));
  }

  public PathItemReference[] getUserSpecifiedIgnoredSourcePathDirectories() {
    return PathItemReference.removeInvalid(PathItemReference.
        deserializeMapArray(
        getPropertyMapArray(PROP_SOURCEPATH_IGNORE)
        ));
  }

  public void setUserSpecifiedIgnoredSourcePathDirectories(PathItemReference[]
      newValue) {
    setPropertyMapArray(PROP_SOURCEPATH_IGNORE,
        PathItemReference.serializeToMapArray(newValue));
  }

  public Object getRestorableDump() {
    return VersionSpecific.getInstance().getAllPropertiesCloned();
  }

  public void setAllFromRestorableDump(Object dump) {
    VersionSpecific.getInstance().setAllPropertiesFrom(dump);
  }

  // Property GET/SET functions -- *all* properties are got/set with one of these.

  public void set(String propertyName, String value) {
    setPropertyObject(propertyName, value);
  }

  public String get(String propertyName) {
    return (String) getPropertyObject(propertyName);
  }

  void setPropertyArray(String propertyName, String[] value) {
    setPropertyObject(propertyName, value);
  }

  String[] getPropertyArray(String propertyName) {
    return (String[]) getPropertyObject(propertyName);
  }

  void setPropertyMapArray(String propertyName, Map[] mapArray) {
    setPropertyObject(propertyName, mapArray);
  }

  Map[] getPropertyMapArray(String propertyName) {
    return (Map[]) getPropertyObject(propertyName);
  }

  Object[] getPropertyObjectArray(String propertyName) {
    return (Object[]) getPropertyObject(propertyName);
  }

  void setValueIfNull(String propertyName, Object defaultValue) {
    if (getOptions().getPropertyPublic(propertyName) == null) {
      setPropertyObject(propertyName, defaultValue);
    }
  }

  //------- Low-level getters/setters -- all above getters/setters use these

  public Object getPropertyObject(final String propertyName) {
    Object result = getOptions().getPropertyPublic(propertyName);

    if (result != null) {
      return result;
    } else if (propertyShouldNotBeNull(propertyName)) {
      reinitializeOptionsAfterProjectChange();

      return getOptions().getPropertyPublic(propertyName);
    } else {
      return null;
    }
  }

  public void setPropertyObject(String propertyName, Object value) {
    getOptions().putPropertyPublic(propertyName, value, true);
  }

  //-------------------- CONVERTERS

  static String booleanToString(boolean b) {
    return b ? "true" : "false";
  }

  static boolean stringToBoolean(String s) {
    return "true".equalsIgnoreCase(s);
  }

  static PathItemReference[] fileObjectsArrayToPathItemsArray(FileObject[]
      fileObjects) {
    PathItemReference[] result = new PathItemReference[fileObjects.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = new PathItemReference(fileObjects[i]);
    }

    return result;
  }

//  public synchronized Object getAllAttributesCloned() {
//    return VersionSpecific.getInstance().getAllPropertiesCloned();
//  }

  public synchronized void setAllPropertiesFrom(Object oldSnapshot) {
    VersionSpecific.getInstance().setAllPropertiesFrom(oldSnapshot);
  }

  private RefactorItOptions getOptions() {
    return RefactorItOptions.getDefault();
  }

  // ---------- File traversing classes -------------------------- //

  private interface FileTraverseInfo {
    int getJavaFilesCount();

    int getTotalCount();
  }

  private final boolean matchesAny(String[] tokens, String input) {
    for (int i = 0; i < tokens.length; i++) {
      if (input.matches(tokens[i])) {
        return true;
      }
    }

    return false;
  }

  private class TraverseFileTree extends BaseFileTreeTraverseListener
  implements FileTraverseInfo {
    private final int maxfilesToTraverse;
    private final FileFilterConfiguration fileFilterConf;
    private int currentCount = 0;
    private int javaFilesCount = 0;

    public TraverseFileTree(int maxFilesToTraverse) {
      this.maxfilesToTraverse = maxFilesToTraverse;

      fileFilterConf = new FileFilterConfiguration(
          NBProjectOptions.class, "FileFilterConfiguration");
    }

    public int foundFile(File file) {
      // filter out the files we do not want to process (*.properties, ...)
      boolean matched = false;
      try {
        matched = matchesAny(
            fileFilterConf.getFileFilterTokens(), file.getName());
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
      }
      if (matched) {
        return CONTINUE_PROCESSING;
      }

      // check whether the file is java file. count them.
      //System.out.println("[Local]Sourcepath file:"+file.getPath());
      if (NBFileUtil.isValidSource(file.getName())) {
        javaFilesCount++;
      }
      currentCount++;
      if (currentCount >= maxfilesToTraverse) {
        return STOP_PROCESSING;
      }
      return CONTINUE_PROCESSING;
    }

    public int enterDirectory(File file) {
      // filter out the dirs we do not want to process (CVS, ...)
      boolean matched = false;
      try {
        matched = matchesAny(
            fileFilterConf.getDirectoryFilterTokens(), file.getName());
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
      }
      if (matched) {
        return STOP_CURRENT_FILE_PROCESSING;
      }
      return CONTINUE_PROCESSING;
    }

    public int getJavaFilesCount() {
      return javaFilesCount;
    }

    public int getTotalCount() {
      return currentCount;
    }
  }


  private class TraverseFileObject extends BaseFileObjectTraverseListener
  implements FileTraverseInfo {
    private final int maxFilesToTraverse;
    private final FileFilterConfiguration fileFilterConf;
    private int currentCount = 0;
    private int javaFilesCount = 0;

    public TraverseFileObject(int maxFilesToTraverse) {
      this.maxFilesToTraverse = maxFilesToTraverse;

      fileFilterConf = new FileFilterConfiguration(
          NBProjectOptions.class, "FileFilterConfiguration");
    }

    public int foundFile(FileObject file) {
      // filter out the files we do not want to process (*.properties, ...)
      boolean isAllowedNonJavaFile = false;
      try {
        isAllowedNonJavaFile = matchesAny(
            fileFilterConf.getFileFilterTokens(), file.getNameExt());
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
      }
      if (isAllowedNonJavaFile) {
        return CONTINUE_PROCESSING;
      }

      if (NBFileUtil.isValidSource(file.getNameExt())) {
        javaFilesCount++;
      }
      currentCount++;

      if (currentCount >= maxFilesToTraverse) {
        return STOP_PROCESSING;
      }
      return CONTINUE_PROCESSING;
    }

    public int enterDirectory(FileObject file) {
      // filter out the dirs we do not want to process (CVS, ...)
      boolean matched = false;
      try {
        matched = matchesAny(
            fileFilterConf.getDirectoryFilterTokens(), file.getName());
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
      }
      if (matched) {
        return STOP_CURRENT_FILE_PROCESSING;
      }
      return CONTINUE_PROCESSING;
    }

    public int getJavaFilesCount() {
      return javaFilesCount;
    }

    public int getTotalCount() {
      return currentCount;
    }
  }

  private class FileFilterConfiguration {
    private final Properties filterTokens;

    private String[] fileTokens;
    private String[] dirTokens;

    public FileFilterConfiguration(Class baseClass, String filterTokensFileName) {
      InputStream in = ResourceUtil.getResourceAsStream(baseClass,
          filterTokensFileName);
      filterTokens = new Properties();
      try {
        filterTokens.load(in);
      } catch (IOException e) {
        log.warn(e.getMessage(), e);
      }
    }

    public String[] getFileFilterTokens() {
      if (fileTokens == null) {
        String fileTokensStr = filterTokens.getProperty("files");
        fileTokens = parseTokenToArray(fileTokensStr);
      }
      return fileTokens;
    }

    public String[] getDirectoryFilterTokens() {
      if (dirTokens == null) {
        String dirTokensStr = filterTokens.getProperty("dirs");
        dirTokens = parseTokenToArray(dirTokensStr);
      }
      return dirTokens;
    }

    private String[] parseTokenToArray(String token) {
      StringTokenizer tokenizer = new StringTokenizer(token, ",");
      ArrayList tokens = new ArrayList(tokenizer.countTokens());
      while (tokenizer.hasMoreTokens()) {
        String tok = tokenizer.nextToken();
        tokens.add(tok.trim());
      }
      return (String[]) tokens.toArray(new String[tokens.size()]);
    }
  }

  // ---------- END: File traversing classes -------------------------- //
}
