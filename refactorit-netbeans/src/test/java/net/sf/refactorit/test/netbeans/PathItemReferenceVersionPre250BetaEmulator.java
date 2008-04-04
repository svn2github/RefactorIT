/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.vfs.NBFileObjectClassPathElement;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.ZipClassPathElement;
import net.sf.refactorit.vfs.local.DirClassPathElement;
import net.sf.refactorit.vfs.local.LocalSource;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.io.Serializable;
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
public class PathItemReferenceVersionPre250BetaEmulator implements Serializable {
  private static final String FILE_OBJECT_URL = "fileObjectUrl";
  private static final String FILE_SYSTEM_NAME_TO_DISPLAY =
      "fileSystemNameToDisplay";
  private static final String FILE_NAME = "fileName";
  private static final String IS_LOCAL_FOLDER = "isLocalFolder";
  private static final String IS_LOCAL_JAR = "isLocalJar";
  private static final String IS_STRING_FORM = "isStringForm";

  private URL fileObjectUrl;
  private String fileSystemNameToDisplay;
  private String fileName;

  private boolean isLocalFolder;
  private boolean isLocalJar;
  private boolean isStringForm;

  /** For options serialization */
  public Map serialize() {
    HashMap result = new HashMap();
    result.put(FILE_OBJECT_URL, fileObjectUrl);
    result.put(FILE_SYSTEM_NAME_TO_DISPLAY, fileSystemNameToDisplay);
    result.put(FILE_NAME, fileName);
    result.put(IS_LOCAL_FOLDER, nbProjectOptionsBooleanToStringEmulator(isLocalFolder));
    result.put(IS_LOCAL_JAR, nbProjectOptionsBooleanToStringEmulator(isLocalJar));
    result.put(IS_STRING_FORM, nbProjectOptionsBooleanToStringEmulator(isStringForm));

    return result;
  }

  /** For options deserialization */
  public PathItemReferenceVersionPre250BetaEmulator(Object serialization) throws BadFieldValues {
    if(serialization instanceof String) {
      set((String)serialization);
      return;
    }
    
    Map map = (Map) serialization;
    
    this.fileObjectUrl = (URL) map.get(FILE_OBJECT_URL);
    this.fileSystemNameToDisplay = (String) map.get(FILE_SYSTEM_NAME_TO_DISPLAY);
    this.fileName = (String) map.get(FILE_NAME);
    this.isLocalFolder = nbProjectOptionsStringToBoolean((String) map.get(
        IS_LOCAL_FOLDER));
    this.isLocalJar = nbProjectOptionsStringToBoolean((String) map.get(
        IS_LOCAL_JAR));
    this.isStringForm = nbProjectOptionsStringToBoolean((String) map.get(
        IS_STRING_FORM));

    // Converter code from old format; works only under NB 3.x. Can't convert under 4.x.
    if(isFileObject()) {
      FileObject fromOldFormat = VersionSpecificGetInstanceFindFileObjectViaOldFormat(map);
      if(fromOldFormat != null) {
        set(fromOldFormat);
        return;
      } else if (fileObjectUrl == null) {
        throw new BadFieldValues("FileObjectUrl is null");
      }
    }
    
    // The field fileSystemNameToDisplay was introduced in version 1.3 (RC), so the
    // first time the user upgrades, the filesystemNameToDisplay field will be null
    // here; we need to give it a value, otherwise a NPE will be thrown later (bug #1804).
    if (this.fileSystemNameToDisplay == null) {
      if (isFileObject()) {
        calculateFileSystemNameToDisplay();
      } else {
        this.fileSystemNameToDisplay = "";
      }
    }
  }

  private void calculateFileSystemNameToDisplay() {
    try {
      calculateFileSystemNameToDisplay(getFileObject());
    } catch (Exception e) {
      // Could happen if the file is not found anymore
      // if (getFileObject() == null or something else (what else?)).
      // In this case the file system name does not matter,
      // because the code will think the file is invalid anyway, right?
      // (Could be that the project options will list this one in a weird format, though)
      this.fileSystemNameToDisplay = "Not found";
    }
  }

  private static boolean showedBadFieldValuesErrorInCurrentSession = false;

  public static PathItemReferenceVersionPre250BetaEmulator[] deserializeMapArray(Object[] serializedReferences) {
    if (serializedReferences == null) {
      return new PathItemReferenceVersionPre250BetaEmulator[0];
    }

    boolean someErrors = false;

    List result = new ArrayList(serializedReferences.length);
    for (int i = 0; i < serializedReferences.length; i++) {
      try {
        result.add(new PathItemReferenceVersionPre250BetaEmulator(serializedReferences[i]));
      } catch (BadFieldValues e) {
        someErrors = true;
      }
    }

    if (someErrors && (!showedBadFieldValuesErrorInCurrentSession)) {
      showedBadFieldValuesErrorInCurrentSession = true;
      DialogManager.getInstance().showError(
          IDEController.getInstance().createProjectContext(),
          "warning.path.items.discarded");
    }

    return (PathItemReferenceVersionPre250BetaEmulator[]) result.toArray(new PathItemReferenceVersionPre250BetaEmulator[0]);
  }

  public static Map[] serializeToMapArray(PathItemReferenceVersionPre250BetaEmulator[] references) {
    Map[] result = new Map[references.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = references[i].serialize();
    }

    return result;
  }

  /**
   * Constructs a reference whose getFileObject() will be enabled, but getFile() will be disabled.
   */
  public PathItemReferenceVersionPre250BetaEmulator(FileObject fileObject) {
    set(fileObject);
  }

  private void set(FileObject fileObject) {
    try {
      this.fileObjectUrl = fileObject.getURL();
    } catch (FileStateInvalidException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    this.fileName = fileObject.getNameExt();
    
    this.isLocalFolder = false;
    this.isLocalJar = false;
    this.isStringForm = false;
    
    calculateFileSystemNameToDisplay(fileObject);
  }

  private void calculateFileSystemNameToDisplay(FileObject fileObject) {
    this.fileSystemNameToDisplay = versionSpecificGetInstanceGetFileSystemNameToDisplay(fileObject);
  }

  /**
   * Creates a reference whose getFileObject() will be disabled, but getFile() will be enabled.
   */
  public PathItemReferenceVersionPre250BetaEmulator(File file) {
    this.fileObjectUrl = null;
    this.fileName = file.getAbsolutePath();
    this.fileSystemNameToDisplay = "";
    this.isLocalFolder = file.isDirectory();
    this.isLocalJar = !this.isLocalFolder;
    this.isStringForm = false;
  }

  public PathItemReferenceVersionPre250BetaEmulator(String stringForm) {
    set(stringForm);
  }

  private void set(String stringForm) {
    this.fileObjectUrl = null;
    this.fileName = stringForm;
    this.fileSystemNameToDisplay = "";
    this.isLocalFolder = false;
    this.isLocalJar = false;
    this.isStringForm = true;
  }

  /** IMPORTANT: caller should ensure that isValid() == true, otherwise a RTE will be thrown. */
  public ClassPathElement getClassPathElement() {
    if (this.isLocalFolder) {
      return new DirClassPathElement(new File(this.fileName));
    }

    if (this.isLocalJar) {
      return new ZipClassPathElement(new File(this.fileName));
    }

    FileObject fileObject = getFileObject();
    if (fileObject == null) {
      throw new RuntimeException("File object lost -- precondition violation");
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

    return new File(this.fileName);
  }

  /**
   * Shows if getFile() method is enabled.
   */
  public boolean isLocalFile() {
    return this.isLocalFolder || this.isLocalJar;
  }

  /**
   * Shows if getFileObject() method is enabled.
   */
  public boolean isFileObject() {
    return !isLocalFile() && !isStringForm();
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

    /*if ((fileSystemName != null) && (fileName != null)) {
      org.openide.filesystems.FileSystem fileSystem =
        Repository.getDefault().findFileSystem(fileSystemName);
      if (fileSystem != null) {
        FileObject fileObject = fileSystem.findResource(fileName);
        return fileObject;
      }
    }*/
    
    FileObject[] fos = URLMapper.findFileObjects(fileObjectUrl);
    FileObject result = fos != null && fos.length > 0 ? fos[0] : null;

    if(result == null) {
      AppRegistry.getLogger(PathItemReferenceVersionPre250BetaEmulator.class).warn(
          "FileObject is null (deserialization failed?): " + fileName);
    }
    
    return result;
  }

  /** IMPORTANT: caller should ensure that isValid() == true, otherwise a RTE will be thrown. */
  public Source getSource() {
    if (isLocalFile()) {
      return LocalSource.getSource(new File(this.fileName));
    } else {
      FileObject fileObject = getFileObject();
      if (fileObject == null) {
        throw new RuntimeException("File object lost -- precondition violation");
      }
      return NBSource.getSource(fileObject);
    }
  }

  public boolean isValid() {
    if (isLocalFile()) {
      return new File(this.fileName).exists();
    } else if (isFileObject()){
      if (getFileObject() == null) {
        return false;
      } else {
        return getFileObject().isValid();
      }
    } else if (isStringForm()) {
      return true;
    } else {
      throw new RuntimeException("Unsupported type");
    }
  }

  /** Example: "c:\some_file.txt (INVALID)"; for displaying to users in lists */
  public String getDisplayNameLong() {
    if(isStringForm()) {
      return getStringForm();
    } else if (isValid()) {
      return getAbsolutePath();
    } else {
      return getAbsolutePath() + " (INVALID)";
    }
  }

  /** Name with filesystem and folders.<br>
   * Example: "c:\some_file.txt" ; used internally, for some IDs and for users
   */   
  public String getAbsolutePath() {
    if (isLocalFile()) {
      return this.fileName;
    } else if (isFileObject()) {
      if(isValid()) {
        return versionSpecificGetInstanceGetLongDisplayName(getFileObject());
      } else {
        return this.fileSystemNameToDisplay;
      }
    } else {
      throw new RuntimeException("Unsupported case for this method");
    }
  }
  
  public String getStringForm() {
    if(!isStringForm()) {
      throw new RuntimeException("This reference is not to a String form");
    }
    
    return fileName;
  }

  /** Example: "some_file.txt"; for displaying to users in trees */
  public String getDisplayNameWithoutFolders() {
    if (isLocalFile()) {
      return getFile().getName();
    } else if (isFileObject()) {
      if(isValid()) {
        FileObject fileObject = getFileObject();
        String nameExt = fileObject.getNameExt();
        
        if (fileObject.isRoot()) {
          return this.fileSystemNameToDisplay;
        } else {
          return nameExt;
        }
      } else {
        return this.fileName;
      }
    } else if(isStringForm()) {
      return getStringForm();
    } else {
      throw new RuntimeException("Unsupported type");
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PathItemReferenceVersionPre250BetaEmulator)) {
      return false;
    } else {
      return equals((PathItemReferenceVersionPre250BetaEmulator) o);
    }
  }

  private boolean equals(PathItemReferenceVersionPre250BetaEmulator other) {
    if (this.isLocalFile() && other.isLocalFile()) {
      return this.getFile().getAbsolutePath().equals(other.getFile().
          getAbsolutePath());
    } else if (this.isFileObject() && other.isFileObject()) {
      return this.getFileObjectOrException().equals(other.getFileObjectOrException());
    } else {
      return false;
    }
  }

  private Object getFileObjectOrException() {
    FileObject result = getFileObject();
    if(result == null) {
      throw new RuntimeException("Could not find a FileObject: " + fileObjectUrl);
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

  public PathItemReferenceVersionPre250BetaEmulator getParent() {
    if(!isValid()) {
      throw new RuntimeException("Should not call getParent() on an invalid reference; please check with isValid() first");
    }
    
    PathItemReferenceVersionPre250BetaEmulator result;
    
    if (isLocalFile() && getFile().getParentFile() != null) {
      result = new PathItemReferenceVersionPre250BetaEmulator(getFile().getParentFile());
    } else if (isFileObject() && getFileObject().getParent() != null) {
      result = new PathItemReferenceVersionPre250BetaEmulator(getFileObject().getParent());
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
  public static PathItemReferenceVersionPre250BetaEmulator createForSource(Source source) {
    PathItemReferenceVersionPre250BetaEmulator result;

    if (source instanceof NBSource) {
      result = new PathItemReferenceVersionPre250BetaEmulator(((NBSource) source).getFileObject());
    } else if (source instanceof LocalSource) {
      result = new PathItemReferenceVersionPre250BetaEmulator(source.getFileOrNull());
    } else {
      return null;
    }

    if (result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  private static List getParents(PathItemReferenceVersionPre250BetaEmulator reference) {
    List result = new ArrayList();

    while (reference != null) {
      result.add(reference);
      reference = reference.getParent();
    }

    return result;
  }

  private static boolean inListOrParentInList(PathItemReferenceVersionPre250BetaEmulator reference,
      PathItemReferenceVersionPre250BetaEmulator[] list) {
    return CollectionUtil.containsSome(Arrays.asList(list), getParents(reference));
  }

  public static boolean inListOrParentInList(FileObject fileObject,
      PathItemReferenceVersionPre250BetaEmulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersionPre250BetaEmulator(fileObject), items);
  }

  public static boolean inListOrParentInList(File f, PathItemReferenceVersionPre250BetaEmulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersionPre250BetaEmulator(f), items);
  }

  public static PathItemReferenceVersionPre250BetaEmulator[] removeInvalid(PathItemReferenceVersionPre250BetaEmulator[] items) {
    List result = new ArrayList(items.length);
    for (int i = 0; i < items.length; i++) {
      if (items[i].isValid()) {
        result.add(items[i]);
      }
    }

    return (PathItemReferenceVersionPre250BetaEmulator[]) result.toArray(new PathItemReferenceVersionPre250BetaEmulator[0]);
  }
  
  public static List toSources(PathItemReferenceVersionPre250BetaEmulator[] items) {
    List result = new ArrayList(items.length);
    
    for (int i = 0; i < items.length; i++) {
      result.add(items[i].getSource());
    }
    
    return result;
  }

  public static class BadFieldValues extends Exception {
    public BadFieldValues(String message) {
      super(message);
    }
  }

  public boolean isStringForm() {
    return isStringForm;
  }
  
  // Methods to emulate methods of other classes
  
  private static Object nbProjectOptionsBooleanToStringEmulator(boolean b) {
    return b ? "true" : "false";
  }
  
  private static boolean nbProjectOptionsStringToBoolean(String s) {
    return "true".equalsIgnoreCase(s);
  }

  private static FileObject VersionSpecificGetInstanceFindFileObjectViaOldFormat(Map map) {
    throw new UnsupportedOperationException();
  }

  public static String versionSpecificGetInstanceGetFileSystemNameToDisplay(FileObject fileObject) {
    return PathItemReference.versionSpecificGetInstanceGetFileSystemNameToDisplayLegacyEmulator(fileObject);
  }

  private static String versionSpecificGetInstanceGetLongDisplayName(FileObject fileObject) {
    if(RefactorItActions.isNetBeansThree()) {
      try {
        return FileObjectUtil.getFileOrNull(fileObject).getAbsolutePath();
      } catch (Exception e) {
        AppRegistry.getExceptionLogger().error(e, PathItemReferenceVersionPre250BetaEmulator.class);
        throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
      }
    }
    
    Class fileUtil;
    try {
      fileUtil = Class.forName("org.openide.filesystems.FileUtil");
    } catch (ClassNotFoundException e) {
      AppRegistry.getExceptionLogger().error(e, PathItemReferenceVersionPre250BetaEmulator.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    
    return (String) ReflectionUtil.invokeMethod(
        fileUtil, "getFileDisplayName", FileObject.class, fileObject);
  }
}
