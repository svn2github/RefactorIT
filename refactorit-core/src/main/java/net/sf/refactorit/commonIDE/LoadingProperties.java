/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;



public class LoadingProperties {
  public boolean showDialogsIfNeeded = true;
  public boolean clean = false;
  public boolean forceFullBuild = false;

  public LoadingProperties() {
  }

  public LoadingProperties(boolean showDialogsIfNeeded) {
    this.showDialogsIfNeeded = showDialogsIfNeeded;
  }

  public LoadingProperties(boolean clean, boolean forceFullBuild) {
    this.clean = clean;
    this.forceFullBuild = forceFullBuild;
  }

  public LoadingProperties(boolean clean, boolean forceFullBuild,
      boolean showDialogsIfNeeded) {
    this.clean = clean;
    this.forceFullBuild = forceFullBuild;
    this.showDialogsIfNeeded = showDialogsIfNeeded;
  }

}
