/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

import java.io.Serializable;


/**
 * *DEPRECATED*, only here to preserve backward compatiblity with older serialized classes.
 * (RefactorItOptions needs it to be deserialisable from it's older versions.)
 */
class FileObjectReference implements Serializable {
  private static final Logger log = Logger.getLogger(FileObjectReference.class);
  
  private final String fileSystemName;
  private final String fileName;

  public FileObjectReference(FileObject fileObject) throws
      FileStateInvalidException {
    this.fileSystemName = fileObject.getFileSystem().getSystemName();
    this.fileName = FileObjectUtil.getResourceName(fileObject);
  }

  public FileObject getFileObject() {
    if ((fileSystemName != null) && (fileName != null)) {
      FileSystem fileSystem = Repository.getDefault().
      	findFileSystem(fileSystemName);
      if (fileSystem != null) {
        FileObject fileObject = fileSystem.findResource(fileName);
        return fileObject;
      }
    }
    log.warn("Couldn't get FileObject from: " + fileSystemName + ":"
        + fileName);
    return null;
  }
}
