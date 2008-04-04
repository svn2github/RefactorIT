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
import net.sf.refactorit.common.util.FileCopier;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class BackupRepository implements Serializable {
  private String backupDir;
  private int usedKeys = 0;

  static final String BACKUP_FILE_PREFIX = "refactorIt.backup";

  /**
   * @param backupDir
   */
  BackupRepository(String backupDir) {
    this.backupDir = backupDir;
  }

  String generateNewKey() {
    return BACKUP_FILE_PREFIX + (usedKeys++);
  }

  public boolean equals(Object rep) {
    return this.getBackupDir().equals(((BackupRepository) rep).getBackupDir());
  }

  void clean() {
    File dir = new File(getBackupDir());
    if (dir.exists()) {
      if (dir.isDirectory()) {
        FileCopier.emptyDirectory(dir);
      } else {
        dir.delete();
      }
    }
  }

  void backupSources(List sources, String key) throws IOException {
    Assert.must(isKeyValid(key), "wrong key");
    BackupManagerUtil.backupSourcesToDir(sources, getBackupDir(), key);

  }

//  void restoreSources(SourcePath sourcePath,String restoreFilePrefix,String redoPrefix, long lastModifiedTime) throws IOException {
//    if ( Assert.enabled ) {
//      Assert.must(isKeyValid(restoreFilePrefix) && isKeyValid(redoPrefix),"wrong key");
//    }
//    BackupRestorer.restoreBackupFromDir( sourcePath,backupDir,
//                                             restoreFilePrefix, redoPrefix, lastModifiedTime);
//
//  }

  String getBackupDir() {
    return this.backupDir;
  }

  public static boolean isKeyValid(String key) {
    return key.equals("") || key.startsWith(BACKUP_FILE_PREFIX);
  }
}
