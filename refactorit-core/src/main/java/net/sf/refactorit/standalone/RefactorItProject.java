/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.standalone.projectoptions.StandaloneProjectOptions;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


/** Only used in standalone */
public class RefactorItProject {

  // Indicates if the project is up-to-date
  private boolean changed = false;

  // Abslute path to the project in filesystem
  private final File projectFile;

  // Main for this project
  final StandaloneProjectOptions options;

  private static final Logger log
      = net.sf.refactorit.common.util.AppRegistry.getLogger(RefactorItProject.class);

  private String projectName;

  /**
   * @throws IOException if couldn't load project
   */
  public RefactorItProject(final File projectFile) throws IOException {
    this.projectFile = projectFile;

    this.options = new StandaloneProjectOptions(projectFile);

    this.projectName = getFile().getName();

    // remove . end
    int pos = projectName.indexOf('.');
    if (pos > 0) {
      projectName = projectName.substring(0, pos);
    }

    log.debug("loaded project from " + projectFile.getAbsolutePath());
  }

  public final File getFile() {
    return this.projectFile;
  }

  public final boolean isChanged() {
    return this.changed;
  }

  public final void setChanged(boolean changed) {
    this.changed = changed;

    // TODO: Some sort of Listener-Functionality?
  }

  /**
   * @throws IOException if saving failes
   */
  public void save() throws IOException {
    options.store();
    changed = false;
  }

  public String getName() {
    return projectName;
  }

  // shall return unique for every project
  public String toString() {
    if (this.cachedUniqueString == null) {
      this.cachedUniqueString
          = getFile().getAbsolutePath() + new Long(dateCreated).toString();
    }
    return this.cachedUniqueString;
  }

  private long dateCreated = Calendar.getInstance().getTimeInMillis();

  private String cachedUniqueString = null;

}
