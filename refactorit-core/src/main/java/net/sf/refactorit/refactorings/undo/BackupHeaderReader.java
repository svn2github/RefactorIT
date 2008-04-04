/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.common.util.AppRegistry;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


class BackupHeaderReader {
  private ObjectInputStream headerStream;
  private int headersCount;
  public BackupHeaderReader(String headerFileName) throws IOException {
    File file = new File(headerFileName);
    if (!file.exists()) {
      throw new RuntimeException("Undo file " + file.getAbsolutePath()
          + " doesn't exist!");
    }
    headerStream = new ObjectInputStream(new
        FileInputStream(file));
    headersCount = headerStream.readInt();
  }

  public SourceHeader nextHeader() throws IOException {
//      if (headerStream.available() <= 0) {
//        return null;
//      }
    try {
      return (SourceHeader) headerStream.readObject();
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(
          "wrong backup file format");

    } catch (EOFException e) {
      return null;

    }

  }

  public void close() {
    try {
      headerStream.close();
    } catch (IOException ex) {
      AppRegistry.getLogger(this.getClass()).debug("HeaderReader close failed");
    }
  }

  public int getHeadersCount() {
    return this.headersCount;
  }
}
