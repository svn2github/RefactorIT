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
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.vfs.NBFileObjectClassPathElement;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.ZipClassPathElement;
import net.sf.refactorit.vfs.local.DirClassPathElement;
import net.sf.refactorit.vfs.local.LocalSource;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Risto
 */
public class PathItemReferenceVersion20Emulator implements Serializable {
  private static final String FILE_SYSTEM_NAME = "fileSystemName";
  private static final String FILE_SYSTEM_NAME_TO_DISPLAY =
      "fileSystemNameToDisplay";
  private static final String FILE_NAME = "fileName";
  private static final String IS_LOCAL_FOLDER = "isLocalFolder";
  private static final String IS_LOCAL_JAR = "isLocalJar";

  private String fileSystemName;
  private String fileSystemNameToDisplay;
  private String fileName;

  private boolean isLocalFolder;
  private boolean isLocalJar;

  /** For options serialization */
  Map serialize() {
    HashMap result = new HashMap();
    result.put(FILE_SYSTEM_NAME, fileSystemName);
    result.put(FILE_SYSTEM_NAME_TO_DISPLAY, fileSystemNameToDisplay);
    result.put(FILE_NAME, fileName);
    result.put(IS_LOCAL_FOLDER, nbProjectOptionsBooleanToStringEmulator(isLocalFolder));
    result.put(IS_LOCAL_JAR, nbProjectOptionsBooleanToStringEmulator(isLocalJar));

    return result;
  }

  /** For options deserialization */
  PathItemReferenceVersion20Emulator(Map map) throws BadFieldValues {
    this.fileSystemName = (String) map.get(FILE_SYSTEM_NAME);
    this.fileSystemNameToDisplay = (String) map.get(FILE_SYSTEM_NAME_TO_DISPLAY);
    this.fileName = (String) map.get(FILE_NAME);
    this.isLocalFolder = nbProjectOptionsStringToBooleanEmulator((String) map.get(
        IS_LOCAL_FOLDER));
    this.isLocalJar = nbProjectOptionsStringToBooleanEmulator((String) map.get(
        IS_LOCAL_JAR));

    // Some time ago it was possible to create & serialize such bad references;
    // now, to make NPEs away, this code makes sure that the PathItemReference
    // does not get ininted if the map contains such old info
    if (isFileObject() && (fileSystemName == null || fileName == null)) {
      throw new BadFieldValues(
          "filesystem and file names are serialized as nulls, discard this fileobject");
    }

    // The field fileSystemNameToDisplay was introduced in version 1.3 (RC), so the
    // first time the user upgrades, the filesystemNameToDisplay field will be null
    // here; we need to give it a value, otherwise a NPE will be thrown later (bug #1804).
    if (this.fileSystemNameToDisplay == null) {
      if (isFileObject()) {
        try {
          calculateFileSystemNameToDisplay(getFileObject());
        } catch (Exception e) {
          // Could happen if the file is not found anymore
          // if (getFileObject() == null or something else (what else?)).
          // In this case the file system name does not matter,
          // because the code will think the file is invalid anyway, right?
          // (Could be that the project options will list this one in a weird format, though)
          this.fileSystemNameToDisplay = fileSystemName;
        }
      } else {
        this.fileSystemNameToDisplay = "";
      }
    }
  }

  private static boolean showedBadFieldValuesErrorInCurrentSession = false;

  static PathItemReferenceVersion20Emulator[] deserializeMapArray(Map[] serializedReferences) {
    if (serializedReferences == null) {
      return new PathItemReferenceVersion20Emulator[0];
    }

    boolean someErrors = false;

    List result = new ArrayList(serializedReferences.length);
    for (int i = 0; i < serializedReferences.length; i++) {
      try {
        result.add(new PathItemReferenceVersion20Emulator(serializedReferences[i]));
      } catch (BadFieldValues e) {
        someErrors = true;
      }
    }

    if (someErrors && (!showedBadFieldValuesErrorInCurrentSession)) {
      showedBadFieldValuesErrorInCurrentSession = true;
      throw new RuntimeException("Something failed");
    }

    return (PathItemReferenceVersion20Emulator[]) result.toArray(new PathItemReferenceVersion20Emulator[0]);
  }

  static Map[] serializeToMapArray(PathItemReferenceVersion20Emulator[] references) {
    Map[] result = new Map[references.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = references[i].serialize();
    }

    return result;
  }

  /**
   * Constructs a reference whose getFileObject() will be enabled, but getFile() will be disabled.
   */
  PathItemReferenceVersion20Emulator(FileObject fileObject) {
    calculateFileSystemNameToDisplay(fileObject);

    try {
      this.fileSystemName = fileObject.getFileSystem().getSystemName();
      this.fileName = fileObject.getPackageNameExt('/', '.');
    } catch (FileStateInvalidException e) {
      // Will this ever happen? Deleting/unmounting the filesystem does _not_ seem to
      // cause this, for example.

      System.err.println(
          "REFACTORIT EXCEPTION (invalid NB filesystem) -- PLEASE REPORT");
      e.printStackTrace();

      throw new ChainableRuntimeException(e);
    }

    this.isLocalFolder = false;
    this.isLocalJar = false;
  }

  private void calculateFileSystemNameToDisplay(FileObject fileObject) {
    Class aClass;
    try {
      aClass = Class.forName("org.openide.execution.NbClassPath");
    } catch (ClassNotFoundException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    
    File rootFile = (File) ReflectionUtil.invokeMethod(aClass, "toFile", FileObject.class, fileObject);
    File[] roots = new File[] {rootFile};
    Object nbClassPathInstance = ReflectionUtil.newInstance("org.openide.execution.NbClassPath", 
        File[].class, roots); 
    this.fileSystemNameToDisplay = (String) ReflectionUtil.invokeMethod(nbClassPathInstance, "getClassPath");

    if ("".equals(this.fileSystemNameToDisplay)) {
      // this happens for mounted jar files for example
      // (tested on NB 3.4.1 beta and 20021206... trunk)
      try {
        this.fileSystemNameToDisplay = fileObject.getFileSystem().
            getDisplayName();
      } catch (FileStateInvalidException ignore) {
        // Seems like nothing to do in this case
      }
    }
  }

  /**
   * Creates a reference whose getFileObject() will be disabled, but getFile() will be enabled.
   */
  public PathItemReferenceVersion20Emulator(File file) {
    this.fileSystemName = "";
    this.fileSystemNameToDisplay = "";
    this.fileName = file.getAbsolutePath();
    this.isLocalFolder = file.isDirectory();
    this.isLocalJar = !this.isLocalFolder;
  }

  /** This one is for javadocs only. */
  public PathItemReferenceVersion20Emulator(URL url) {
    this.fileSystemName = "";
    this.fileSystemNameToDisplay = "";
    this.fileName = URLDecoder.decode(url.toExternalForm());
    this.isLocalFolder = false;
    this.isLocalJar = false;
  }

  /** This one is for javadocs only. */
  public PathItemReferenceVersion20Emulator(final String fileName) {
    this.fileSystemName = "";
    this.fileSystemNameToDisplay = "";
    this.fileName = fileName;
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
    if (!isFile()) {
      throw new RuntimeException("This reference does not reference a File");
    }

    return new File(this.fileName);
  }

  /**
   * Shows if getFile() method is enabled.
   */
  public boolean isFile() {
    return this.isLocalFolder || this.isLocalJar;
  }

  /**
   * Shows if getFileObject() method is enabled.
   */
  public boolean isFileObject() {
    return !isFile();
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

    if ((fileSystemName != null) && (fileName != null)) {
      org.openide.filesystems.FileSystem fileSystem =
          topManagerGetDefaultGetRepositoryFindFileSystem(fileSystemName);
      if (fileSystem != null) {
        FileObject fileObject = fileSystem.findResource(fileName);
        return fileObject;
      }
    }

    System.err.println("Couldn't get FileObject from: " + fileSystemName + ":"
        + fileName);
    return null;
  }

  /** IMPORTANT: caller should ensure that isValid() == true, otherwise a RTE will be thrown. */
  public Source getSource() {
    if (isFile()) {
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
    if (isFile()) {
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
   * Example: "c:\some_file.txt" ; used internally and for some IDs
   */
  public String getAbsolutePath() {
    if (isFile()) {
      return this.fileName;
    } else {
      if (this.fileName != null && this.fileName.length() > 0) {
        if (this.fileSystemNameToDisplay != null
            && this.fileSystemNameToDisplay.length() > 0) {
          return this.fileSystemNameToDisplay + " -> " + this.fileName;
        } else {
          return this.fileName;
        }
      } else {
        return this.fileSystemNameToDisplay;
      }
    }
  }

  /** Example: "some_file.txt"; for displaying to users in trees */
  public String getDisplayNameWithoutFolders() {
    if (isFile()) {
      return getFile().getName();
    } else {
      FileObject fileObject = getFileObject();

      if (fileObject.isRoot()) {
        return this.fileSystemNameToDisplay;
      } else {
        return fileObject.getNameExt();
      }
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PathItemReferenceVersion20Emulator)) {
      return false;
    } else {
      return equals((PathItemReferenceVersion20Emulator) o);
    }
  }

  private boolean equals(PathItemReferenceVersion20Emulator other) {
    if (this.isFile() && other.isFile()) {
      return this.getFile().getAbsolutePath().equals(other.getFile().
          getAbsolutePath());
    } else if (this.isFileObject() && other.isFileObject()) {
      return this.getFileObject().equals(other.getFileObject());
    } else {
      return false;
    }
  }

  public boolean isFolder() {
    if (isFile()) {
      return getFile().isDirectory();
    } else {
      return getFileObject().isFolder();
    }
  }

  public PathItemReferenceVersion20Emulator getParent() {
    if (isFile() && getFile().getParentFile() != null) {
      return new PathItemReferenceVersion20Emulator(getFile().getParentFile());
    } else if (isFileObject() && getFileObject().getParent() != null) {
      return new PathItemReferenceVersion20Emulator(getFileObject().getParent());
    } else {
      return null;
    }
  }

  /** Don't change this method!!! Used to display in JLists */
  public String toString() {
    return getAbsolutePath(); //getDisplayNameLong(); // don't change
  }

  /**
   * Returns null if it cannot find a file or FileObject to represent, or if the reference
   * is not valid)
   */
  public static PathItemReferenceVersion20Emulator createForSource(Source source) {
    PathItemReferenceVersion20Emulator result;

    if (source instanceof NBSource) {
      result = new PathItemReferenceVersion20Emulator(((NBSource) source).getFileObject());
    } else if (source instanceof LocalSource) {
      result = new PathItemReferenceVersion20Emulator(source.getFileOrNull());
    } else {
      return null;
    }

    if (result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  public static class DisplayNameComparator implements Comparator {
    public int compare(Object a, Object b) {
      return ((PathItemReferenceVersion20Emulator) a).getDisplayNameLong().compareTo(
          ((PathItemReferenceVersion20Emulator) b).getDisplayNameLong());
    }
  }


  private static List getParents(PathItemReferenceVersion20Emulator reference) {
    List result = new ArrayList();

    while (reference != null) {
      result.add(reference);
      reference = reference.getParent();
    }

    return result;
  }

  private static boolean inListOrParentInList(PathItemReferenceVersion20Emulator reference,
      PathItemReferenceVersion20Emulator[] list) {
    return CollectionUtil.containsSome(Arrays.asList(list), getParents(reference));
  }

  public static boolean inListOrParentInList(FileObject fileObject,
      PathItemReferenceVersion20Emulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersion20Emulator(fileObject), items);
  }

  public static boolean inListOrParentInList(File f, PathItemReferenceVersion20Emulator[] items) {
    return inListOrParentInList(new PathItemReferenceVersion20Emulator(f), items);
  }

  public static PathItemReferenceVersion20Emulator[] removeInvalid(PathItemReferenceVersion20Emulator[] items) {
    List result = new ArrayList(items.length);
    for (int i = 0; i < items.length; i++) {
      if (items[i].isValid()) {
        result.add(items[i]);
      }
    }

    return (PathItemReferenceVersion20Emulator[]) result.toArray(new PathItemReferenceVersion20Emulator[0]);
  }

  private static class BadFieldValues extends Exception {
    public BadFieldValues(String message) {
      super(message);
    }
  }

  // Methdods to emulate behaviour of other classes at the time of RIT 2.0
  
  private static String nbProjectOptionsBooleanToStringEmulator(boolean b) {
    return b ? "true" : "false";
  }
  
  private boolean nbProjectOptionsStringToBooleanEmulator(String s) {
    return "true".equalsIgnoreCase(s);
  }
  
  private static FileSystem topManagerGetDefaultGetRepositoryFindFileSystem(String fileSystemName) {
    Class topManagerClass;
    try {
      topManagerClass = Class.forName("org.openide.TopManager");
    } catch (ClassNotFoundException e) {
      AppRegistry.getExceptionLogger().error(e, PathItemReferenceVersion20Emulator.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    Object topManager = ReflectionUtil.invokeMethod(topManagerClass, "getDefault");
    Object repository = ReflectionUtil.invokeMethod(topManager, "getRepository");
    return (FileSystem) ReflectionUtil.invokeMethod(repository, "findFileSystem", String.class,
        fileSystemName);
  }
}
