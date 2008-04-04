/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils.cvsutil;


import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileCopier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author  risto
 */
public class CvsFileStatus {
  private static CvsFileStatus instance = new CvsFileStatus();

  public static CvsFileStatus getInstance() {
    return instance;
  }

  public static void setInstance(CvsFileStatus newInstance) {
    instance = newInstance;
  }

  public boolean isBinary(File f) {
    final CvsEntriesLine cvsStatusLine = getCvsEntriesLine(f);
    return cvsStatusLine != null && cvsStatusLine.isBinary();
  }
  
  public boolean isRemoved(File f) {
    CvsEntriesLine cvsStatusLine = getCvsEntriesLine(f);
    return cvsStatusLine != null && cvsStatusLine.isRemoved();
  }

  public boolean isUncommentedAdd(File f) {
    CvsEntriesLine cvsStatusLine = getCvsEntriesLine(f);
    return cvsStatusLine != null && cvsStatusLine.isUncommittedAdd();
  }

  public boolean isKnown(File f) {
    if (f.isDirectory() && ( ! new File(f, "CVS").exists())) {
      return false;
    } else if ((!f.isDirectory()) && getCvsEntriesLine(f) == null) {
      return false;
    }
    
    return true;
    
    // FIXME: commented out; there has to be a better way: check for CVS/Repository or something?
    /*if(RefactorItActions.isNetBeansFour()) {
      return true;
    } else {
      return true;
      
      
      // Rarely gives "false" (only if for example a user
      // has created a "CVS" folder manually and has not
      // created such folders for parent directories).
      return allParentsKnown(source);
    }*/
  }

  public CvsEntriesLine getCvsEntriesLine(File file) {
    BufferedReader reader = null;
    try {
      File folder = file.getParentFile();
      File entriesFile = FileCopier
          .getChild(FileCopier.getChild(folder, "CVS"), "Entries");

      reader = new BufferedReader(new FileReader(entriesFile));

      String line;
      while ((line = reader.readLine()) != null) {
        if(isEntriesLine(line)) {
          CvsEntriesLine parsedLine = new CvsEntriesLine(line);
          if(parsedLine.getName().equals(file.getName())) {
            return parsedLine;
          }
        }
      }
    } catch (IOException ignore) {
    } catch (NullPointerException ignore2) {
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        throw new ChainableRuntimeException(e);
      }
    }

    return null;
  }

  private boolean isEntriesLine(String line) {
    return line.indexOf("/") >= 0;
  }
}
