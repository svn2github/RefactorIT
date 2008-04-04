/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.ui.RuntimePlatform;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;


class BackupFileReader {
  BufferedInputStream buffStream;

  byte buffArray[] = new byte[BackupManagerUtil.INITIAL_BUFF_SIZE];

  BackupFileReader(final String backupFileName) throws IOException {
    buffStream = new BufferedInputStream(new
        FileInputStream(backupFileName),
        BackupManagerUtil.INITIAL_BUFF_SIZE);
  }

  public byte[] getContentsFor(final SourceHeader header) throws IOException {
    if (buffArray.length < header.fileLength) {
      buffArray = new byte[(int) (1.5 * header.fileLength)];
    }
    if (buffStream.read(buffArray, 0, header.fileLength) !=
        header.fileLength) {
      throw new IllegalStateException(
          "RefactorIT encountered error during undo\n " +
          header.getAbsolutePath() +
          " file length not matched, corrupted backup file??"
          );
    }
    return buffArray;
  }

  public void close() {
    try {
      buffStream.close();
    } catch (IOException ex) {
    }
  }

  public void skipHeader(final SourceHeader header) {
    try {
      buffStream.skip(header.fileLength);
    } catch (IOException e) {
      // shouldn't happen!!!
      e.printStackTrace(RuntimePlatform.console);
    }

  }
}
