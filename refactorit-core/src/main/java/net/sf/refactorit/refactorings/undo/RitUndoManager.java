/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ui.RuntimePlatform;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Tonis Vaga
 */
public class RitUndoManager extends javax.swing.undo.UndoManager
    implements IUndoManager {
  private static final Logger log=AppRegistry.getLogger(RitUndoManager.class);

  private Project project;
  private static RitUndoManager instance;
  private UndoableTransaction currentTransaction;

  private ArrayList dirsList;
  private LinkedList usedDirsQueue;

  private Map sourcesActualModifyMap;

  public static final boolean debug = false;

  private RitUndoManager(Project project) {
    this.project = project;
    instance = this;
    initBackupDirs();
    this.setLimit(N_UNDOS);
    sourcesActualModifyMap = new TreeMap();
  }

  private String undoRootDir; //="/tmp/backup";

  private static RitUndoManager createUndoManager(Project project) {
    instance = new RitUndoManager(project);
    return instance;
  }

  public static final void clear() { // avoid memory leaks on project change
    if (instance != null) {
      instance.project = null;
    }
    instance = null;
  }

  Project getProject() {
    return this.project;
  }


  /**
   * for use where context is not available. Can return null.
   * @return instance of undo manager
   */
  public static RitUndoManager getInstance() {
    return instance;
  }

  public static synchronized RitUndoManager getInstance(Project project) {
//    if (instance == null || instance.getProject() != project) {
//      instance = createUndoManager(project);
//    }

    // FIXME: need to refactoring
    if (project != null) {
     RitUndoManager manager = project.getRitUndoManager();

     if(manager == null) {
       manager = createUndoManager(project);
       project.setRitUndoManager(manager);
       instance = manager;
     } else {
       manager = project.getRitUndoManager();
       if (manager.getProject() == null) {
         manager.setProject(project);
       }
       instance = manager;
     }
    } else {
      if (instance == null) {
        instance = createUndoManager(project);
      }
    }

    return instance;
  }

  /**
   * Can use only one transaction at time!! If not commited, info will be lost.
   * @param name name
   * @param details details
   * @return transaction
   */
  public synchronized UndoableTransaction createTransaction(String name,
      String details) {
    String undoDir = null;

    if ( debug ) {
      log.debug("\n\n===========================================");
      log.debug("Transaction " + name + " " + details + " started");
    }
    Assert.must(currentTransaction == null,
        "previous transaction was not finished correctly");
    if (usedDirsQueue.size() == dirsList.size()) {
      undoDir = (String) usedDirsQueue.removeLast();
    }
    {
      Assert.must(usedDirsQueue.size() < dirsList.size());
      for (int i = 0; i < dirsList.size(); i++) {
        String element = (String) dirsList.get(i);
        if (!usedDirsQueue.contains(element)) {
          undoDir = element;
        }
      }
      Assert.must(undoDir != null);
    }
    currentTransaction = new UndoableTransaction(this, name, details,
        new BackupRepository(undoDir));
    return currentTransaction;
  }

  public static synchronized UndoableTransaction getCurrentTransaction() {
    if (instance == null) {
      return null;
    }
    return instance.currentTransaction;
  }

  public synchronized void commitTransaction() {
    Assert.must(currentTransaction != null);
    if (currentTransaction != null /*&& currentTransaction.isSignificant() */) {
      if (!currentTransaction.isSignificant()) {
        if ( debug ) {
          log.debug(" not significant transaction, calling die ");
        }
        currentTransaction.die();
      } else {
        if (Assert.enabled) {
          Assert.must(dirsList.contains(currentTransaction.getBackupDir()) &&
              !usedDirsQueue.contains(currentTransaction.getBackupDir()));
        }
        usedDirsQueue.addFirst(currentTransaction.getBackupDir());

        currentTransaction.end();

        this.addEdit(currentTransaction);
      }

      if ( debug ) {

        log.debug("Transaction " + currentTransaction.getName() + " "
            + currentTransaction.getDetails() + " finished");
        log.debug("===========================================\n\n");
      }
      currentTransaction = null;

    }
  }

  public synchronized void rollbackTransaction() {
    if ( debug ) log.debug("transaction rollback called");

    if (currentTransaction == null) {
      log.warn("undo rollback called when transaction doesn't exists!");
      log.debug(new Exception("wrong rollback call"));
    } else {
      try {
        currentTransaction.end();
        if (currentTransaction.canUndo()) {
          currentTransaction.undo();
        }
        currentTransaction.die();
        if ( debug ) {
          log.debug("Transaction " + currentTransaction.getName() + " " +
              currentTransaction.getDetails() + " finished");
          log.debug("===========================================\n\n");
        }
      } finally {
        currentTransaction = null;
      }

    }
  }

  /**
   * Number of undos available
   */
  public static final int N_UNDOS = 4; //RefactorItConstants.developingMode ? 4 : 1;

  /**
   * There are {@link #N_UNDOS} repositories
   */
  private void initBackupDirs() {

    undoRootDir = RuntimePlatform.getConfigDir() + File.separatorChar + "Undo";
    dirsList = new ArrayList(N_UNDOS);
    usedDirsQueue = new LinkedList();

    for (int i = 0; i < N_UNDOS; i++) {
      String backupPath = undoRootDir + File.separatorChar + i;
      dirsList.add(backupPath);
    }
  }

  public synchronized void redo() throws javax.swing.undo.CannotRedoException {
    super.redo();
  }

  public synchronized void undo() throws javax.swing.undo.CannotUndoException {
    super.undo();
  }

  public synchronized boolean canRedo() {
    // @todo: override this
    return super.canRedo();
  }

  public String getPresentationNameWIthDetails(boolean undo) {
    UndoableTransaction trans = (UndoableTransaction)
        (undo ? super.editToBeUndone() : super.editToBeRedone());

    String result = "";

    if (trans != null) {
      result = trans.getName();
      if (trans.getDetails().length() > 0) {
        result = result + " '" + trans.getDetails() + '\'';
      }
    }

    return result;
  }

  public void addLastModifiedRedirect(String path, long lastModifedAfterUndo,
      Long savedLastModified){

    Map sourceModifyHistory = (Map) sourcesActualModifyMap.get(path);
    if (sourceModifyHistory == null){
      sourceModifyHistory = new TreeMap();
      sourcesActualModifyMap.put(path, sourceModifyHistory);
    }
    sourceModifyHistory.put(new Long(lastModifedAfterUndo), savedLastModified);
  }

  public long getActualLastModified(String path, long physicalyModified){
    Map sourceModifyHistory = (Map) sourcesActualModifyMap.get(path);
    if (sourceModifyHistory == null){
      return physicalyModified;
    }

    Long curTimeModified = null;
    Long newKey = new Long(physicalyModified);
    while(newKey != null && !newKey.equals(curTimeModified)){
      curTimeModified = newKey;
      newKey = (Long) sourceModifyHistory.get(newKey);
  }

    return curTimeModified.longValue();
    }

  public void setProject(final Project project) {
      this.project = project;
  }
  }
