/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;


/**
 * This is deprecated, it's only here to preserve backward compatibility with serialized classes
 * (RefactorItOptions needs it to be deserialisable from it's older versions.)
 */
public class FileObjectOrUrl implements Serializable {
  private final boolean hasFileObject;

  private final FileObjectReference fileObject;
  private final URL url;

  public FileObjectOrUrl(FileObject fileObject) throws
      FileStateInvalidException {
    this.hasFileObject = true;
    this.fileObject = new FileObjectReference(fileObject);
    this.url = null;
  }

  public FileObjectOrUrl(URL url) {
    this.hasFileObject = false;
    this.fileObject = null;
    this.url = url;
  }

  public String getName() {
    if (this.hasFileObject) {
      try {
        FileObject fileObject = this.fileObject.getFileObject();
        return
            fileObject.getFileSystem().getDisplayName() +
            File.separator +
            fileObject.getName();
      } catch (FileStateInvalidException e) {
        // Invalid filesystem: perhaps some error message should be displayed here?
        return "";
      }
    } else {
      return URLDecoder.decode(url.toExternalForm());
    }
  }

  public String toString() {
    return getName();
  }
}
