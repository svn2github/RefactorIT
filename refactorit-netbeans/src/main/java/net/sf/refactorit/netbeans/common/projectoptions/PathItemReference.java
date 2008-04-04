/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.vfs.NBFileObjectClassPathElement;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.ZipClassPathElement;
import net.sf.refactorit.vfs.local.DirClassPathElement;
import net.sf.refactorit.vfs.local.LocalSource;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * References either a FileObject or a local File
 * (for classpath or sourcepath).
 * <br><br>
 * This class was created for two reasons: because FileObjects cannot
 * be serialized directly, and because some items on RefactorIT paths
 * do not (and should not) have their own FileObjects at all in
 * NetBeans filesystems list.
 *
 * @author Risto
 */
public class PathItemReference implements Serializable {
  public static final String INVALID_ITEM_SUFFIX = " (INVALID)";

  private static final String KEY_FORMAT_VERSION = "format-version";
  private static final String KEY_PATH_TYPE = "path-type";
  private static final String KEY_PATH = "path";
  private static final String KEY_ABSOLUTE_PATH_ON_DISK = "absolute-path-on-disk";

  private static final String TYPE_FILE_OBJECT = "netbeans-file-object";
  private static final String TYPE_LOCAL_FILE = "local-file";
  private static final String TYPE_FREEFORM = "freeform";

  private static final String VERSION_LEGACY = "some-legacy-version";
  private static final String VERSION_250_BETA = "2.5.0 beta";
  /** Only updated when format changes */
  private static final String VERSION_LATEST = VERSION_250_BETA;

  private String formatVersion;
  private String pathType;
  private String path;
  private String absolutePathOnDisk;

  /** For options serialization */
  public Map serialize() {
    HashMap result = new HashMap();

    result.put(KEY_FORMAT_VERSION, formatVersion);
    result.put(KEY_PATH_TYPE, pathType);
    result.put(KEY_PATH, path);
    result.put(KEY_ABSOLUTE_PATH_ON_DISK, absolutePathOnDisk);

    return result;
  }

  /** For options deserialization */
  public PathItemReference(Object serialization) {
    if(serialization instanceof String) {
      set((String)serialization);
      return;
    }

    Map map = (Map) serialization;

    this.formatVersion = (String) map.get(KEY_FORMAT_VERSION);
    if(formatVersion == null) {
      formatVersion = VERSION_LEGACY;
    }
    this.pathType = (String) map.get(KEY_PATH_TYPE);
    this.path = (String) map.get(KEY_PATH);
    this.absolutePathOnDisk = (String) map.get(KEY_ABSOLUTE_PATH_ON_DISK);


    if( ! formatVersion.equals(VERSION_LATEST)) {
      importFromOlderFormat(map);
    }
  }

  public static PathItemReference[] deserializeMapArray(Object[] serializedReferences) {
    if (serializedReferences == null) {
      return new PathItemReference[0];
    }

    List result = new ArrayList(serializedReferences.length);
    for (int i = 0; i < serializedReferences.length; i++) {
      result.add(new PathItemReference(serializedReferences[i]));
    }

    return (PathItemReference[]) result.toArray(new PathItemReference[0]);
  }

  public static Map[] serializeToMapArray(PathItemReference[] references) {
    Map[] result = new Map[references.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = references[i].serialize();
    }

    return result;
  }

  /**
   * Constructs a reference whose getFileObject() will be enabled, but getFile() will be disabled.
   */
  public PathItemReference(FileObject fileObject) {
    set(fileObject);
  }

  private void set(FileObject fileObject) {
    try {
      path = fileObject.getURL().toExternalForm();
    } catch (FileStateInvalidException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }

    absolutePathOnDisk = VersionSpecific.getInstance().getLongDisplayName(fileObject);
    pathType = TYPE_FILE_OBJECT;
    formatVersion = VERSION_LATEST;
  }

  /** Creates a reference whose getFileObject() will be disabled, but getFile() will be enabled. */
  public PathItemReference(File file) {
    set(file);
  }

  private void set(File file) {
    this.absolutePathOnDisk = file.getAbsolutePath();
    this.path = absolutePathOnDisk;
    this.pathType = TYPE_LOCAL_FILE;
    this.formatVersion = VERSION_LATEST;
  }

  public PathItemReference(String freeForm) {
    set(freeForm);
  }

  private void set(String freeForm) {
    this.path = freeForm;
    this.absolutePathOnDisk = "";
    this.pathType = TYPE_FREEFORM;
    this.formatVersion = VERSION_LATEST;
  }

  /** IMPORTANT: caller should ensure that isValid() == true, otherwise a RTE will be thrown. */
  public ClassPathElement getClassPathElement() {
    if(isLocalFile()) {
      if (isFolder()) {
        return new DirClassPathElement(getFile());
      } else {
        return new ZipClassPathElement(getFile());
      }
    }

    FileObject fileObject = getFileObject();
    if (fileObject == null) {
      throw new RuntimeException("File object lost -- precondition violation: "
          + getAbsolutePath());
    }

    return new NBFileObjectClassPathElement(fileObject);
  }

  /**
   * Only works if the reference was constructed using a File; otherwise
   * throws a RuntimeException.
   */
  public File getFile() {
    if (!isLocalFile()) {
      throw new RuntimeException("This reference does not reference a File");
    }

    return new File(this.path);
  }

  /** Shows if getFile() method is enabled. */
  public boolean isLocalFile() {
    return pathType.equals(TYPE_LOCAL_FILE);
  }

  /** Shows if getFileObject() method is enabled. */
  public boolean isFileObject() {
    return pathType.equals(TYPE_FILE_OBJECT);
  }

  /**
   * Only works if the reference was constructed using a FileObject; otherwise
   * throws a RuntimeException.
   */
  public FileObject getFileObject() {
    if (!isFileObject()) {
      throw new RuntimeException(
          "This reference does not reference a FileObject");
    }

    URL url;
    try {
      url = new URL(path);
    } catch (MalformedURLException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }

    FileObject[] fos = URLMapper.findFileObjects(url);
    FileObject result = fos != null && fos.length > 0 ? fos[0] : null;

    // will convert jars wrongly handled as simple files
    result = VersionSpecific.getInstance().getArchiveRoot(result);

    return result;
  }

  /** IMPORTANT: caller should ensure that isValid() == true, otherwise a RTE will be thrown. */
  public Source getSource() {
    if( ! isValid()) {
      throw new RuntimeException("Referenced object lost -- precondition violation: " +
          getAbsolutePath());
    }

    if (isLocalFile()) {
      return LocalSource.getSource(getFile());
    } else if (isFileObject()) {
      return NBSource.getSource(getFileObject());
    } else {
      throw new RuntimeException("Invalid path type");
    }
  }

  public boolean isValid() {
    if (isLocalFile()) {
      return getFile().exists();
    } else if (isFileObject()){
      FileObject fileObject = getFileObject();
      return fileObject != null && fileObject.isValid();
    } else if (isFreeform()) {
      return true;
    } else {
      throw new RuntimeException("Unsupported type");
    }
  }

  /** Example: "c:\some_file.txt (INVALID)"; for displaying to users in lists */
  public String getDisplayNameLong() {
    if(isFreeform()) {
      return getFreeform();
    } else if (isValid()) {
      return getAbsolutePath();
    } else {
      return getAbsolutePath() + INVALID_ITEM_SUFFIX;
    }
  }

  /** Example: "some_file.txt (INVALID)"; for displaying to users in trees */
  public String getDisplayNameWithoutFolders() {
    if(isFreeform()) {
      return getFreeform();
    } else if(isValid()) {
      return getNameWithoutFolders();
    } else {
      return getNameWithoutFolders() + INVALID_ITEM_SUFFIX;
    }
  }

  /** Name with filesystem and folders.<br>
   * Example: "c:\some_file.txt" ; used internally, for some IDs and for users
   */
  public String getAbsolutePath() {
    if (isLocalFile() || isFileObject()) {
      return absolutePathOnDisk;
    } else {
      throw new RuntimeException("Unsupported case for this method");
    }
  }

  public String getFreeform() {
    if(!isFreeform()) {
      throw new RuntimeException("This reference is not to a String form");
    }

    return path;
  }

  public boolean isFreeform() {
    return pathType.equals(TYPE_FREEFORM);
  }

  private String getNameWithoutFolders() {
    if (isFileObject() || isLocalFile()) {
      if(hasEmptyName()) {
        return absolutePathOnDisk;
      } else {
        return FileUtil.extractFileNameFromPath(absolutePathOnDisk, File.separatorChar);
      }
    } else if(isFreeform()) {
      return getFreeform();
    } else {
      throw new RuntimeException("Unsupported type");
    }
  }

  private boolean hasEmptyName() {
    return absolutePathOnDisk.endsWith(File.separator);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PathItemReference)) {
      return false;
    } else {
      return equals((PathItemReference) o);
    }
  }

  private boolean equals(PathItemReference other) {
    return this.pathType.equals(other.pathType) &&
        this.path.equals(other.path);
  }

  private Object getFileObjectOrException() {
    FileObject result = getFileObject();
    if(result == null) {
      throw new RuntimeException("Could not find a FileObject: " + path);
    }
    return result;
  }

  public boolean isFolder() {
    if (isLocalFile()) {
      return getFile().isDirectory();
    } else {
      return getFileObject().isFolder();
    }
  }

  public PathItemReference getParent() {
    if(!isValid()) {
      throw new RuntimeException("Should not call getParent() on an invalid reference; please check with isValid() first; called on: " + getAbsolutePath());
    }

    PathItemReference result;

    if (isLocalFile() && getFile().getParentFile() != null) {
      result = new PathItemReference(getFile().getParentFile());
    } else if (isFileObject() && getFileObject().getParent() != null) {
      result = new PathItemReference(getFileObject().getParent());
    } else {
      return null;
    }

    if(result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  public String toString() {
    return getDisplayNameLong();
  }

  /**
   * Returns null if it cannot find a file or FileObject to represent, or if the reference
   * is not valid)
   */
  public static PathItemReference createForSource(Source source) {
    PathItemReference result;

    if (source instanceof NBSource) {
      result = new PathItemReference(((NBSource) source).getFileObject());
    } else if (source instanceof LocalSource) {
      result = new PathItemReference(source.getFileOrNull());
    } else {
      return null;
    }

    if (result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  private static List getParents(PathItemReference reference) {
    List result = new ArrayList();

    while (reference != null) {
      result.add(reference);
      reference = reference.getParent();
    }

    return result;
  }

  private static boolean inListOrParentInList(PathItemReference reference,
      PathItemReference[] list) {
    return CollectionUtil.containsSome(Arrays.asList(list), getParents(reference));
  }

  public static boolean inListOrParentInList(FileObject fileObject,
      PathItemReference[] items) {
    return inListOrParentInList(new PathItemReference(fileObject), items);
  }

  public static boolean inListOrParentInList(File f, PathItemReference[] items) {
    return inListOrParentInList(new PathItemReference(f), items);
  }

  public static PathItemReference[] removeInvalid(PathItemReference[] items) {
    List result = new ArrayList(items.length);
    for (int i = 0; i < items.length; i++) {
      if (items[i].isValid()) {
        result.add(items[i]);
      }
    }

    return (PathItemReference[]) result.toArray(new PathItemReference[0]);
  }

  public static List toSources(PathItemReference[] items) {
    List result = new ArrayList(items.length);

    for (int i = 0; i < items.length; i++) {
      result.add(items[i].getSource());
    }

    return result;
  }

  // Code to import from older versions

  private void importFromOlderFormat(Map map) {
    if(formatVersion.equals(VERSION_LEGACY)) {
      final String FILE_OBJECT_URL = "fileObjectUrl";
      final String FILE_SYSTEM_NAME_TO_DISPLAY =
          "fileSystemNameToDisplay";
      final String FILE_SYSTEM_NAME = "fileSystemName";
      final String FILE_NAME = "fileName";
      final String IS_LOCAL_FOLDER = "isLocalFolder";
      final String IS_LOCAL_JAR = "isLocalJar";
      final String IS_STRING_FORM = "isStringForm";

      URL fileObjectUrl = (URL) map.get(FILE_OBJECT_URL); // for version 2491
      String fileSystemName = (String)map.get(FILE_SYSTEM_NAME); // for version 2.0.7

      String fileSystemNameToDisplay = (String) map.get(FILE_SYSTEM_NAME_TO_DISPLAY);
      String fileName = (String) map.get(FILE_NAME);
      boolean isLocalFolder = NBProjectOptions.stringToBoolean((String) map.get(
          IS_LOCAL_FOLDER));
      boolean isLocalJar = NBProjectOptions.stringToBoolean((String) map.get(
          IS_LOCAL_JAR));
      boolean isStringForm = NBProjectOptions.stringToBoolean((String) map.get(
          IS_STRING_FORM));

      boolean isLocalFile = isLocalFolder || isLocalJar;
      boolean isFileObject = !isLocalFile && !isStringForm;

      // Converter code from an even older format; works only under NB 3.x. Can't convert under 4.x.
      if(isFileObject) {
        FileObject fromOldFormat = versionSpecificGetInstanceFindFileObjectViaOldFormatLegacyEmulator(map);
        if(fromOldFormat != null) {
          set(fromOldFormat);
          return;
        } else if (fileObjectUrl == null && fileSystemName == null) {
          throw new RuntimeException("Bad field value: FileObjectUrl is null");
        }
      }

      // The field fileSystemNameToDisplay was introduced in version 1.3 (RC), so the
      // first time the user upgrades, the filesystemNameToDisplay field will be null
      // here; we need to give it a value, otherwise a NPE will be thrown later (bug #1804).
      if (fileSystemNameToDisplay == null) {
        if (isFileObject) {
          fileSystemNameToDisplay = calculateFileSystemNameToDisplay();
        } else {
          fileSystemNameToDisplay = "";
        }
      }

      if(isFileObject) {
        if(fileObjectUrl != null) {
          set(importGetFileObject(fileObjectUrl));
        } else {
          set(fileSystemName);
        }
      } else if (isLocalFile) {
        set(importGetFile(fileName));
      } else if (isStringForm) {
        set(importGetString(fileName));
      } else {
        throw new RuntimeException("An unknown type -- coud not import an old path item setting");
      }
    } else {
      throw new RuntimeException("Unsupported import version");
    }
  }

  private static FileObject versionSpecificGetInstanceFindFileObjectViaOldFormatLegacyEmulator(Map map) {
    if(RefactorItActions.isNetBeansFour()) {
      return null;
    } else {
      String fileSystemName = (String) map.get("fileSystemName");
      String fileName = (String) map.get("fileName");

      if (fileSystemName != null && fileName != null) {
        FileSystem fileSystem = Repository.getDefault().findFileSystem(fileSystemName);
        if (fileSystem != null) {
          FileObject fileObject = fileSystem.findResource(fileName);
          return fileObject;
        }
      }

      return null;
    }
  }

  private static File importGetFile(String fileName) {
    return new File(fileName);
  }

  private static String importGetString(String fileName) {
    return fileName;
  }

  private static FileObject importGetFileObject(URL fileObjectUrl) {
    FileObject[] fos = URLMapper.findFileObjects(fileObjectUrl);
    FileObject result = fos != null && fos.length > 0 ? fos[0] : null;

    if(result == null) {
      AppRegistry.getLogger(PathItemReference.class).warn(
          "FileObject is null (deserialization failed?): " + fileObjectUrl);
    }

    return result;
  }

  private String calculateFileSystemNameToDisplay() {
    try {
      return calculateFileSystemNameToDisplay(getFileObject());
    } catch (Exception e) {
      // Could happen if the file is not found anymore
      // if (getFileObject() == null or something else (what else?)).
      // In this case the file system name does not matter,
      // because the code will think the file is invalid anyway, right?
      // (Could be that the project options will list this one in a weird format, though)
      return "Not found";
    }
  }

  private String calculateFileSystemNameToDisplay(FileObject fileObject) {
    return versionSpecificGetInstanceGetFileSystemNameToDisplayLegacyEmulator(fileObject);
  }

  public static String versionSpecificGetInstanceGetFileSystemNameToDisplayLegacyEmulator(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      try {
        return fileObject.getFileSystem().getDisplayName();
      } catch (FileStateInvalidException e) {
        AppRegistry.getExceptionLogger().error(e, PathItemReference.class);
        throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
      }
    }

    File rootFile = FileObjectUtil.getFileOrNull(fileObject);
    File[] roots = new File[] {rootFile};
    Object nbClassPathInstance = ReflectionUtil.newInstance("org.openide.execution.NbClassPath",
        File[].class, roots);
    String result = (String) ReflectionUtil.invokeMethod(nbClassPathInstance, "getClassPath");

    if ("".equals(result) || result == null) {
      // this happens for mounted jar files for example
      // (tested on NB 3.4.1 beta and 20021206... trunk)
      try {
        return fileObject.getFileSystem().getDisplayName();
      } catch (FileStateInvalidException ignore) {
        return "INVALID";
      }
    } else {
      return result;
    }
  }
}
