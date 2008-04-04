/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

// taken from forum.java.sun.com. Adapted for our needs.
public class Patch {

  private byte[] sought;

  private byte[] replacement;

  private String fileName;

  private File file;

  private boolean matches(MappedByteBuffer bb, int pos) {
    for (int j = 0; j < sought.length; ++j) {
      if (sought[j] != bb.get(pos + j)) {
        return false;
      }
    }
    return true;
  }

  private void replace(MappedByteBuffer bb, int pos) {
    for (int j = 0; j < sought.length; ++j) {
      byte b = (j < replacement.length) ? replacement[j] : (byte) ' ';
      bb.put(pos + j, b);
    }
  }

  // returns number of replacements
  private int searchAndReplace(MappedByteBuffer bb, int sz) {
    int replacementsCount = 0;
    for (int pos = 0; pos <= sz - sought.length; ++pos) {
      if (matches(bb, pos)) {
        replace(bb, pos);
        pos += sought.length - 1;
        ++replacementsCount;
      }
    }
    return replacementsCount;
  }

  // Search for occurrences of the input pattern in the given file
  private void run(File f) throws IOException {

    // Open the file and then get a channel from the stream
    RandomAccessFile raf = new RandomAccessFile(f, "rw"); // "rws", "rwd"
    FileChannel fc = raf.getChannel();

    // Get the file's size and then map it into memory
    int sz = (int) fc.size();
    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, sz);

    searchAndReplace(bb, sz);

    bb.force(); // Write back to file, like "flush()"

    // Close the channel and the stream
    raf.close();
  }

  public void replace(String sought, String replacement) throws IOException {
    this.sought = sought.getBytes();
    this.replacement = replacement.getBytes();

    if (this.sought.length != this.replacement.length) {
      throw new IllegalArgumentException(
          "Sought string size shall be equals to replacement string size!");
    }

    run(getFile());
  }

  public Patch(String fileName) {
    this.fileName = fileName;
  }

  public Patch(File file) {
    this.file = file;
  }

  private File getFile() {
    if (file == null) {
      file = new File(fileName);
    }
    return file;
  }
}

