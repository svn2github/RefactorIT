/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;

import java.io.OutputStreamWriter;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vcs.Vcs;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileLock;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author  risto
 */
public class FileObjectUtil {
  private static final Logger log = Logger.getLogger(FileObjectUtil.class);

  /** 
   * @return null if unable to locate the file 
   *              (or if it does not exist, but I'm not sure this is always guaranteed, 
   *              NetBeans's API is not clear about that). 
   */
  public static File getFileOrNull(FileObject fileObject) {
    File result = org.openide.filesystems.FileUtil.toFile(fileObject);
    if (result != null) {
      return result;
    }

    try {
      if (fileObject.getFileSystem() instanceof LocalFileSystem) {
        result = ((LocalFileSystem) fileObject.getFileSystem()).getRootDirectory();
      } else if (fileObject.getFileSystem() instanceof JarFileSystem) {
        result = ((JarFileSystem) fileObject.getFileSystem()).getJarFile();
      } else {
        return null;
      }
    } catch (FileStateInvalidException e) {
      return null;
    }

    for (Iterator i = getPathNames(fileObject).iterator(); i.hasNext(); ) {
      String dirName = (String) i.next();
      result = FileCopier.getChild(result, dirName);
      if (result == null) {
        return null;
      }
    }

    return result;
  }

  private static List getPathNames(FileObject o) {
    List result = new ArrayList();

    while (!o.isRoot()) {
      result.add(o.getNameExt());
      o = o.getParent();
    }

    Collections.reverse(result);
    return result;
  }

  public static boolean samePhysicalFile(FileObject first, FileObject second) {
    try {
      if (first.getFileSystem().equals(second.getFileSystem())) {
        return first.equals(second);
      }
    } catch (FileStateInvalidException failed) {
      return false;
    }

    File f1 = FileObjectUtil.getFileOrNull(first);
    File f2 = FileObjectUtil.getFileOrNull(second);
    
    if(f1 == null || f2 == null) {
      return false;
    } else {
      return f1.equals(f2);
    }
  }

  /** @return null if not found (not on sourcepath, for example) */
  public static CompilationUnit getCompilationUnit(FileObject file) {
    if (IDEController.getInstance().getActiveProject() == null) {
      AppRegistry.getLogger(FileObjectUtil.class).debug("getNBProject==null, calling ensureProject()!!");
      IDEController.getInstance().ensureProject();
    }

    List compilationUnits = IDEController.getInstance().getActiveProject().
        getCompilationUnits();

    for (Iterator i = compilationUnits.iterator(); i.hasNext(); ) {
      final CompilationUnit compilationUnit = (CompilationUnit) i.next();
      final Source source = compilationUnit.getSource();

      if (source instanceof NBSource &&
          samePhysicalFile(file, ((NBSource) source).getFileObject())) {

        return compilationUnit;
      } else if (source.getFileOrNull() != null && match(source.getFileOrNull(), file)) {
        
        return compilationUnit;
      }
    }

    return null;
  }

  /**
   * Determines whether the file corresponds to a Netbeans file object.
   */
  public static boolean match(File file, FileObject fileObject) {
    return file.equals(getFileOrNull(fileObject));
  }

  public static String getResourceName(FileObject fileObject) {
    ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
    return classPath.getResourceName(fileObject, File.separatorChar, true);
  }

  public static String getContents(FileObject fileObject) {
    try {
      return FileCopier.readReaderToString(new InputStreamReader(fileObject.getInputStream()));
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, FileNotFoundReason.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
  }

  public static InputStream getInputStream(FileObject fileObject) throws FileNotFoundException {
    try {
      return fileObject.getInputStream();
    } catch (Exception e) {
      if (Vcs.supportsGet(fileObject)) {
        if (Vcs.get(fileObject)) {
          try {
            // HACK
            {
              NBSource source = NBSource.getSource(fileObject);
              if(source != null) {
                source.invalidateCaches();
              }
            }
            
            return fileObject.getInputStream();
          } catch (FileNotFoundException e1) {
            throw new FileNotFoundException("Check out the file: "
                + fileObject + " or run commit");
          }
        } else {
          log.warn("RefactorIT - VCS.GET failed for: " + fileObject);
        }
      }
      throw new FileNotFoundException("Check out the file: "
          + fileObject + " or run commit");
    }
  }

  public static DataObject getDataObjectOrNull(FileObject fileObject) {
    try {
      return DataObject.find(fileObject);
    } catch (DataObjectNotFoundException ignore) {
      return null;
    }
  }

  public static boolean exists(FileObject fo) {
    if(FileObjectUtil.getFileOrNull(fo) != null) {
      return FileObjectUtil.getFileOrNull(fo).exists();
    } else if (fo.isData()) {
      try {
        // We could use FileObject.canRead() instead, but that would just return a hardcoded "true".
        getInputStream(fo).close();
        return true;
      } catch (FileNotFoundException e) {
        return false;
      } catch (IOException e) {
        return true;
      }
    } else {
      return fo.isValid() && getDataObjectOrNull(fo) != null
          && getDataObjectOrNull(fo).isValid(); // not sure
    }
  }

  public static FileObject setContents(final FileObject fileObject, final String contents) throws IOException {
    FileLock l = fileObject.lock();
    try {
      FileCopier.writeStringToWriter(contents, new OutputStreamWriter(fileObject.getOutputStream(l)));
      return fileObject;
    } finally {
      l.releaseLock();
    }
  }

  /** 
   * safeGetFileObject() is needed for CVS support under NB 4.0 RC 2;
   * without this, moving of some files would not work because 
   * NB would report that a file with the same name already exists in the
   * destination folder. This method can be probably removed later.
   */
  public static FileObject safeGetFileObject(FileObject fo, String name) {
    // The hack is only needed for NB 4.0 (at least RC2, more specifically, for our CVS code (or only our CVS-tests?)).
    if(RefactorItActions.isNetBeansThree()) {
      return fo.getFileObject(name);
    }
    
    FileObject result = fo.getFileObject(name);
    if(result == null) {
      return null;
    }
    
    File file = getFileOrNull(result);
    if(file == null || (!file.exists())) {
      fo.refresh();
      // Retry
      result = fo.getFileObject(name);
      org.apache.log4j.Logger.getLogger(FileObjectUtil.class).debug("Using safeFileObject paid off");
      
      if(result != null) {
        org.apache.log4j.Logger.getLogger(FileObjectUtil.class).debug("Returning hardcoded null -- let's hope this works");
        return null;
      }
    }
    
    return result;
  }

  /** @see #safeGetFileObject(FileObject, String) */
  public static FileObject safeGetFileObject(FileObject parent, String name, String ext) {
    // The hack is only needed for NB 4.0 (at least RC2, more specifically, for our CVS code (or only our CVS-tests?)).
    if(RefactorItActions.isNetBeansThree()) {
      return parent.getFileObject(name, ext);
    }
    
    FileObject result = parent.getFileObject(name, ext);
    if(result == null) {
      return null;
    }
    
    File file = getFileOrNull(result);
    if(file == null || (!file.exists())) {
      parent.refresh();
      // Retry
      result = parent.getFileObject(name, ext);
      org.apache.log4j.Logger.getLogger(FileObjectUtil.class).debug("Using safeGetFileObject paid off");
      
      if(result != null) {
        org.apache.log4j.Logger.getLogger(FileObjectUtil.class).debug("Returning hardcoded null -- let's hope this works");
        return null;
      }
    }
    
    return result;
  }

  public static String getFileName(String name, String ext) {
    if(ext == null || "".equals(ext)) {
      return name;
    } else {
      return name + "." + ext;
    }
  }
}
