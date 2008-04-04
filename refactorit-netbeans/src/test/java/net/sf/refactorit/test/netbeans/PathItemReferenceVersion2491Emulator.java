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
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.vfs.NBFileObjectClassPathElement;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Risto
 */
public class PathItemReferenceVersion2491Emulator implements Serializable {
  private static final String FILE_OBJECT_URL = "fileObjectUrl";
  private static final String FILE_SYSTEM_NAME_TO_DISPLAY =
      "fileSystemNameToDisplay";
  private static final String FILE_NAME = "fileName";
  private static final String IS_LOCAL_FOLDER = "isLocalFolder";
  private static final String IS_LOCAL_JAR = "isLocalJar";

  private URL fileObjectUrl;
  private String fileSystemNameToDisplay;
  private String fileName;

  private boolean isLocalFolder;
  private boolean isLocalJar;

  /** For options serialization */
  public Map serialize() {
    HashMap result = new HashMap();
    result.put(FILE_OBJECT_URL, fileObjectUrl);
    result.put(FILE_SYSTEM_NAME_TO_DISPLAY, fileSystemNameToDisplay);
    result.put(FILE_NAME, fileName);
    result.put(IS_LOCAL_FOLDER, nbProjectOptionsBooleanToStringEmulator(isLocalFolder));
    result.put(IS_LOCAL_JAR, nbProjectOptionsBooleanToStringEmulator(isLocalJar));

    return result;
  }

  /** For options deserialization */
  public PathItemReferenceVersion2491Emulator(Map map) throws BadFieldValues {
    this.fileObjectUrl = (URL) map.get(FILE_OBJECT_URL);
    this.fileSystemNameToDisplay = (String) map.get(FILE_SYSTEM_NAME_TO_DISPLAY);
    this.fileName = (String) map.get(FILE_NAME);
    this.isLocalFolder = nbProjectOptionsStringToBooleanEmulator((String) map.get(
        IS_LOCAL_FOLDER));
    this.isLocalJar = nbProjectOptionsStringToBooleanEmulator((String) map.get(
        IS_LOCAL_JAR));

    if(isFileObject()) {
      FileObject fromOldFormat = versionSpecificGetInstanceFindFileObjectViaOldFormatEmulator(map);
      if(fromOldFormat != null) {
        set(fromOldFormat);
        return;
      }
    }
    
    // Some time ago it was possible to create & serialize such bad references;
    // now, to make NPEs away, this code makes sure that the PathItemReference
    // does not get ininted if the map contains such old info
    if (isFileObject() && fileObjectUrl == null) {
      throw new BadFieldValues("FileObjectUrl is null");
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

  static PathItemReferenceVersion2491Emulator[] deserializeMapArray(Map[] serializedReferences) {
    if (serializedReferences == null) {
      return new PathItemReferenceVersion2491Emulator[0];
    }

    boolean someErrors = false;

    List result = new ArrayList(serializedReferences.length);
    for (int i = 0; i < serializedReferences.length; i++) {
      try {
        result.add(new PathItemReferenceVersion2491Emulator(serializedReferences[i]));
      } catch (BadFieldValues e) {
        someErrors = true;
      }
    }

    if (someErrors && (!showedBadFieldValuesErrorInCurrentSession)) {
      showedBadFieldValuesErrorInCurrentSession = true;
      throw new RuntimeException("Something failed");
    }

    return (PathItemReferenceVersion2491Emulator[]) result.toArray(new PathItemReferenceVersion2491Emulator[0]);
  }

  public static Map[] serializeToMapArray(PathItemReferenceVersion2491Emulator[] references) {
    Map[] result = new Map[references.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = references[i].serialize();
    }

    return result;
  }

  /**
   * Constructs a reference whose getFileObject() will be enabled, but getFile() will be disabled.
   */
  public PathItemReferenceVersion2491Emulator(FileObject fileObject) {
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
    
    calculateFileSystemNameToDisplay(fileObject);
  }

  private void calculateFileSystemNameToDisplay(FileObject fileObject) {
    this.fileSystemNameToDisplay = PathItemReference.versionSpecificGetInstanceGetFileSystemNameToDisplayLegacyEmulator(fileObject);
  }

  /**
   * Creates a reference whose getFileObject() will be disabled, but getFile() will be enabled.
   */
  public PathItemReferenceVersion2491Emulator(File file) {
    this.fileObjectUrl = null;
    this.fileName = file.getAbsolutePath();
    this.fileSystemNameToDisplay = "";
    this.isLocalFolder = file.isDirectory();
    this.isLocalJar = !this.isLocalFolder;
  }

  /** This one is for javadocs only. */
  public PathItemReferenceVersion2491Emulator(URL url) {
    this.fileObjectUrl = null;
    this.fileSystemNameToDisplay = "";
    this.fileName = URLDecoder.decode(url.toExternalForm());
    this.isLocalFolder = false;
    this.isLocalJar = false;
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
    return !isLocalFile();
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
      AppRegistry.getLogger(PathItemReferenceVersion2491Emulator.class).warn(
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
    } else {
      if (getFileObject() == null) {
        return false;
      } else {
        return getFileObject().isValid();
      }
    }
  }

  /** Example: "c:\some_file.txt (INVALID)"; for displaying to users in lists */
  public String getDisplayNameLong() {
    if (isValid()) {
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
    } else {
      return versionSpecificGetInstanceGetLongDisplayNameEmulator(getFileObject());      
    }
  }

  /** Example: "some_file.txt"; for displaying to users in trees */
  public String getDisplayNameWithoutFolders() {
    if (isLocalFile()) {
      return getFile().getName();
    } else {
      FileObject fileObject = getFileObject();
      String nameExt = fileObject.getNameExt();
      
      if (fileObject.isRoot()) {
        return this.fileSystemNameToDisplay;
      } else {
        return nameExt;
      }
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PathItemReferenceVersion2491Emulator)) {
      return false;
    } else {
      return equals((PathItemReferenceVersion2491Emulator) o);
    }
  }

  private boolean equals(PathItemReferenceVersion2491Emulator other) {
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

  public PathItemReferenceVersion2491Emulator getParent() {
    PathItemReferenceVersion2491Emulator result;
    
    if (isLocalFile() && getFile().getParentFile() != null) {
      result = new PathItemReferenceVersion2491Emulator(getFile().getParentFile());
    } else if (isFileObject() && getFileObject().getParent() != null) {
      result = new PathItemReferenceVersion2491Emulator(getFileObject().getParent());
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
    return getAbsolutePath();
  }

  /**
   * Returns null if it cannot find a file or FileObject to represent, or if the reference
   * is not valid)
   */
  public static PathItemReferenceVersion2491Emulator createForSource(Source source) {
    PathItemReferenceVersion2491Emulator result;

    if (source instanceof NBSource) {
      result = new PathItemReferenceVersion2491Emulator(((NBSource) source).getFileObject());
    } else if (source instanceof LocalSource) {
      result = new PathItemReferenceVersion2491Emulator(source.getFileOrNull());
    } else {
      return null;
    }

    if (result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  private static List getParents(PathItemReferenceVersion2491Emulator reference) {
    List result = new ArrayList();

    while (reference != null) {
      result.add(reference);
      reference = reference.getParent();
    }

    return result;
  }

  private static boolean inListOrParentInList(PathItemReferenceVersion2491Emulator reference,
      PathItemReferenceVersion2491Emulator[] list) {
    return CollectionUtil.containsSome(Arrays.asList(list), getParents(reference));
  }

  public static boolean inListOrParentInList(FileObject fileObject,
      PathItemReferenceVersion2491Emulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersion2491Emulator(fileObject), items);
  }

  public static boolean inListOrParentInList(File f, PathItemReferenceVersion2491Emulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersion2491Emulator(f), items);
  }

  public static PathItemReferenceVersion2491Emulator[] removeInvalid(PathItemReferenceVersion2491Emulator[] items) {
    List result = new ArrayList(items.length);
    for (int i = 0; i < items.length; i++) {
      if (items[i].isValid()) {
        result.add(items[i]);
      }
    }

    return (PathItemReferenceVersion2491Emulator[]) result.toArray(new PathItemReferenceVersion2491Emulator[0]);
  }
  
  public static List toSources(PathItemReferenceVersion2491Emulator[] items) {
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

  // Methdods to emulate behaviour of other classes at the time of RIT 2.0
  
  private static final String UNSUPPORTED_MESSAGE = 
    "If you need this method, please implement it as it was on 12-Nov-2004 00:00";
    
  private static String nbProjectOptionsBooleanToStringEmulator(boolean b) {
    return b ? "true" : "false";
  }
  
  private boolean nbProjectOptionsStringToBooleanEmulator(String s) {
    return "true".equalsIgnoreCase(s);
  }

  private FileObject versionSpecificGetInstanceFindFileObjectViaOldFormatEmulator(Map map) {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  private String versionSpecificGetInstanceGetLongDisplayNameEmulator(FileObject fileObject) {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }
}
