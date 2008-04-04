/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.common.util.Assert;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


class BackupHeaderWriter {
  int headersWrote;
  int headersCount;
  ObjectOutputStream headerStream;
  /**
   * if file exists appends, creates new otherwise
   * @param headerFileName absolute path to file where to write
   * @param headersCount headersCount
   * @throws IOException
   */
  public BackupHeaderWriter(String headerFileName,
      int headersCount) throws IOException {

//      List existingHeaders = null;
//      File file = new File(headerFileName);
//      if (file.exists()) {
//        existingHeaders = readAllHeadersFromFile(headerFileName);
//        file.delete();
//      }

    BufferedOutputStream headerStreamBuf = new BufferedOutputStream(new
        FileOutputStream(headerFileName), 1024);
    headerStream = new ObjectOutputStream(headerStreamBuf);

    // first write headers count
    headerStream.writeInt(headersCount);

    this.headersCount = headersCount;

    headersWrote = 0;

  }

  public void writeHeader(SourceHeader item) throws IOException {
    headerStream.writeObject(item);
    ++headersWrote;
  }

  public void close() throws IOException {
    if (Assert.enabled) {
      Assert.must(headersWrote == headersCount);
    }
    headerStream.flush();
    headerStream.close();
  }
}
