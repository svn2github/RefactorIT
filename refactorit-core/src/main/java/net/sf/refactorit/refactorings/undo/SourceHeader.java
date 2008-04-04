/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import net.sf.refactorit.vfs.Source;


public class SourceHeader extends SourceInfo {
  int fileLength;
  long lastModified;
  String fileAbsolutePath;

  public String getAbsolutePath() {
    return fileAbsolutePath; //rootPath+separatorChar+relativePath;
  }

  public SourceHeader(final Source dir, String newName) {
    super(dir, newName);
    fileAbsolutePath = dir.getAbsolutePath();
    lastModified = dir.lastModified();
  }

  public SourceHeader(final Source source) {
    super(source);

    fileAbsolutePath = source.getAbsolutePath();
    fileLength = (int) source.length();
    lastModified = source.lastModified();
  }

  public boolean equals(Object obj) {
    SourceHeader header = (SourceHeader) obj;
    return getAbsolutePath().equals(header.getAbsolutePath())
        && fileLength == header.fileLength;
  }

  public String toString() {
    return getAbsolutePath() + " length: " + fileLength;
  }
}
