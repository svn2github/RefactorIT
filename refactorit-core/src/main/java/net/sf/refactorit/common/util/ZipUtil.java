/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

// java classes
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public final class ZipUtil {
  // this specifies a standard buffer size for reading bytes from
  // zip files.
  private static final int STANDARD_BUFFER_SIZE_IN_BYTES = 8 * 1024;

  /**
   * reads all bytes the zip entry contains into String.
   *
   * @param zip, the zip file that contains that entry. Also we need
   * it to get the inputstream from it, we do not get it from entry.
   * @param entry, the zip entry from where to read the bytes to String.
   * @return String, that contains all bytes from entry.
   */
  public static String readEntryToString(ZipFile zip,
      ZipEntry entry) throws IOException {
    // determine the buffer size for reading from zip entry to
    // String.
    long unCompressedSize = entry.getSize();
    byte[] buffer = null;
    if (unCompressedSize == -1) {
      // the size wasn't specified in zip entry.
      buffer = new byte[STANDARD_BUFFER_SIZE_IN_BYTES];
    } else {
      // the size was specified, so we know to allocate buffer for it.
      // and if there is not so much memory we try to allocate standard
      // size of bytes.

      buffer = new byte[STANDARD_BUFFER_SIZE_IN_BYTES];
      //try {
      //    buffer = new byte[unCompressedSize];
      //} catch (OutOfMemoryError ome) {
      //    buffer = new byte[STANDARD_BUFFER_SIZE_IN_BYTES];
      //}
    }

    // write zip entry into String.
    InputStream in = zip.getInputStream(entry);
    StringWriter out = new StringWriter(STANDARD_BUFFER_SIZE_IN_BYTES);
    int nrOfBytesRead = 0;
    while (true) {
      nrOfBytesRead = in.read(buffer);
      if (nrOfBytesRead < 0) {
        break;
      }
      // did "for" because so it was currently an easy way to correctly
      // write int values into writer. And because ZipFile doesn't
      // return any Reader's, only InputStream's.
      for (int i = 0; i < nrOfBytesRead; i++) {
        out.write(buffer[i]);
      }
    }
    try {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    } catch (IOException ioe) {
      // do not do nothing
    }

    // return String
    return out.toString();
  }
}
