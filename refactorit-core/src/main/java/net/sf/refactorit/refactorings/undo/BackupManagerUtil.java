/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * @author Tonis Vaga
 */
public class BackupManagerUtil {

//  private final static String outFilePrefix = BackupRepository.BACKUP_FILE_PREFIX;
  static final int INITIAL_BUFF_SIZE = 128 * 1024;
  private BackupManagerUtil() {
  }

//  static class FileDeleteHeader implements BackupHeader {
//    String absolutePath;
//    /**
//     * @param fileAbsolutePath
//     */
//    public FileDeleteHeader(String fileAbsolutePath) {
//      absolutePath=fileAbsolutePath;
//    }
//    public String getAbsolutePath() {
//      return absolutePath;
//    }
//
//  }
  /**
   *
   * @param sources mutable list of sources, can contain nulls!
   * @param dirName dir name
   * @param filePrefix filePrefix
   * @throws IOException
   */
  static void backupSourcesToDir(List sources, String dirName,
      String filePrefix) throws IOException {

    if (sources.size() < 1) {
      return;
    }

    long startTime = System.currentTimeMillis();

    ensureBackupDir(dirName);

    // remove nulls
    while (sources.remove(null)) {
    }

    Collections.sort(sources, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        return ((Source) obj1).getAbsolutePath().compareTo(
            ((Source) obj2).getAbsolutePath());
      }
    });

    String backupFileName = dirName + File.separatorChar + filePrefix;

    BufferedOutputStream outFileStream = new BufferedOutputStream(new
        FileOutputStream(backupFileName), 1024);

    String headerFileName = extractHeaderFilePath(dirName, filePrefix);

    BackupHeaderWriter headerWriter
        = new BackupHeaderWriter(headerFileName, sources.size());

    for (Iterator i = sources.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();

      if (!item.isFile()) {
        continue;
      }

//      Assert.must( item.isFile(), item.getAbsolutePath()+" is not a file");
      headerWriter.writeHeader(new SourceHeader(item));
      outFileStream.write(item.getContent());

      if (RitUndoManager.debug) {
        System.out.println("Backing up source " +
            item.getAbsolutePath());
      }

    }
    outFileStream.flush();
    outFileStream.close();
    headerWriter.close();
    if (RefactorItConstants.debugInfo) {
      AppRegistry.getLogger(BackupManagerUtil.class).debug("Backing up " + sources.size() +
      " files took " +
      (System.currentTimeMillis() - startTime) + " mms");
    }

  }

  /**
   * @param dirName dir name
   */
  private static void ensureBackupDir(final String dirName) {
    File destDir = new File(dirName);
    if (destDir.exists() && !destDir.isDirectory()) {
      destDir.delete();
    }

    if (!destDir.exists()) {
      destDir.mkdirs();
    }
  }

  /**
   * @param dirName dir name
   * @param filePrefix filePrefix
   * @return header file path
   */
  static String extractHeaderFilePath(final String dirName,
      final String filePrefix) {
    return dirName + File.separatorChar + filePrefix + ".header";
  }

  /**
   * @param compilationUnits source file list
   * @return source list
   */
  static List createSourcesList(final List compilationUnits) {
    List sources = new ArrayList(compilationUnits.size());

    for (int i = 0; i < compilationUnits.size(); i++) {
      sources.add(((CompilationUnit) compilationUnits.get(i)).getSource());
    }

    return sources;
  }



}
