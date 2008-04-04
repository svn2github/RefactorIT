/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils.cvsutil;


/**
 * @author risto
 */
public class CvsEntriesLine {

  private final boolean isDirectory;
  private final String name;
  private final String version;
  private final String time;
  private final String options;

  public CvsEntriesLine(String stringForm) {
    String[] tokens = stringForm.split("/", -1);
    
    isDirectory = tokens[0].equals("D");
    name = tokens[1];
    version = tokens[2];
    time = tokens[3];
    options = tokens[4];
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getTime() {
    return time;
  }

  public String getOptions() {
    return options;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public boolean isRemoved() {
    return getVersion().startsWith("-");
  }

  public boolean isUncommittedAdd() {
    return getVersion().startsWith("0");
  }

  public boolean isBinary() {
    return getOptions().indexOf("-kb") >= 0;
  }
}
