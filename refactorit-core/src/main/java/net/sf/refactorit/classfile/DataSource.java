/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classfile;


import net.sf.refactorit.classmodel.Project;

import java.io.IOException;
import java.io.InputStream;


public abstract class DataSource {

  /* The project we currently belong to */
  private Project project = null;

  /* The VFS-qualified name for requested resource */
  private String resource = null;

  public DataSource(Project project, String resource) {
    this.project = project;
    this.resource = resource;
  }

  public abstract long lastModified() throws IOException;

  public abstract long length() throws IOException;

  public abstract boolean exists() throws IOException;

  public abstract InputStream getInputStream() throws IOException;

  public static final byte[] getInputData(DataSource data) throws IOException {
    InputStream input = null;

    try {

      // System.out.println("Starting to load data for: "+(data.getResource()));

      byte[] buffer = new byte[(int) data.length()];

      if (buffer.length == 0) {
        throw new IOException("No bytes available for " + data.getResource());
      }

      // System.out.println("Starting to load "+(buffer.length)+" bytes of data for: "+data.getResource());

      input = data.getInputStream();

      if (input == null) {
        throw new IOException("Can not open input stream - "
            + (data.getResource()));
      }

      // System.out.println("Reading input ...");

      // Load content
      int length = 0;
      int offset = 0;

      // Fill the buffer
      for (int count = 0;
          (length < buffer.length) && (count = input.read(buffer, offset,
          buffer.length - offset)) > -1; length += count, offset += count) {}

      // System.out.println("Successfully received "+(length)+" bytes of data");

      // Check consistency
      if (length != buffer.length) {
        throw new IOException("buffer.length != count");
      }

      // System.out.println("Loaded "+(buffer.length)+" bytes for "+(data.getResource()));

      return buffer;

    } finally {
      if (input != null) {
        input.close();
      }
    }
  }

  public final String getResource() {
    return this.resource;
  }

  public final Project getProject() {
    return this.project;
  }
}
