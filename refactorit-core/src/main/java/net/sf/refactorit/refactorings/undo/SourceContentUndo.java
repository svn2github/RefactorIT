/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import net.sf.refactorit.common.util.AppRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: Undo for sources modifying. Will backup sources and restore them.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class SourceContentUndo extends SingleUndoableEdit {
  boolean significant;
  String undoKey;
  String redoKey;

  SourceContentUndo(net.sf.refactorit.vfs.Source source,
      RepositoryTransaction transaction) {
    this(new ArrayList(Collections.singletonList(source)), transaction);
  }

  /**
   *
   * @param sources list to backup. NB! must be mutable so we can sort
   * @param transaction
   */

  SourceContentUndo(List sources, RepositoryTransaction transaction) {
    this(sources, transaction, true);
  }

  SourceContentUndo(List sources, RepositoryTransaction transaction,
      boolean createRedo) {
    super(transaction);

    significant = sources.size() != 0;
    if (significant) {
      try {
        BackupRepository repository = transaction.getRepository();
        undoKey = repository.generateNewKey();
        if (createRedo) {
          redoKey = transaction.getRepository().generateNewKey();
        }

        // Remember lastModified() property for files, before they are changed
        if (transaction instanceof UndoableTransaction){
          ((UndoableTransaction) transaction).rememberLastModifiedFor(sources);
        }

        repository.backupSources(sources, undoKey);
      } catch (IOException ex) {
        AppRegistry.getExceptionLogger().error(ex, this);
      }
    }
  }

  public SourceContentUndo() {
    super(null);

    significant = false;
  }

  public void deserializeFromRepository(RepositoryTransaction transaction) {
    // hack!
    setTransaction(transaction);

    if (getUndoHeaderContent() != null) {
      significant = true;
    } else {
      significant = false;
    }

  }

  public UndoableStatus getRedoStatus() {
    BackupRestorer restorer = new BackupRestorer(getTransaction(), redoKey,
        undoKey);

    return restorer.getUndoableStatus();
  }

  public UndoableStatus getUndoStatus() {
    BackupRestorer restorer = new BackupRestorer(getTransaction(), undoKey,
        redoKey);

    return restorer.getUndoableStatus();
  }

//  public boolean canUndo() {
//    return checkCanRestoreResolveConflicts(undoKey,redoKey);
//  }
//  public boolean canRedo() {
//    return checkCanRestoreResolveConflicts(redoKey,undoKey);
//  }

  public void redo() throws javax.swing.undo.CannotRedoException {
    super.redo();

    try {
      BackupRepository repository = getTransaction().getRepository();

      BackupRestorer restorer = new BackupRestorer(getTransaction(),
          redoKey, undoKey);

      restorer.restore();

      //repository.restoreSources(transaction.getSourcePath(),redoKey,undoKey, transaction.getFinishTime());
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  /**
   * NB! doesn't check conflicts, just restores files
   * @throws javax.swing.undo.CannotUndoException
   */

  public void undo() throws javax.swing.undo.CannotUndoException {
    super.undo();

    try {
      BackupRepository repository = getTransaction().getRepository();
      if (redoKey == null) {
        redoKey = "";
      }
      BackupRestorer restorer = new BackupRestorer(getTransaction(), undoKey,
          redoKey);
      restorer.restore();


    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

  public boolean isSignificant() {
    return significant;
  }

  public boolean checkCanRestoreResolveConflicts(String undoKey, String redoKey) {

    BackupRestorer restorer = new BackupRestorer(getTransaction(), undoKey,
        redoKey);

    UndoableStatus status = restorer.getUndoableStatus();
    if (!status.isOk()) {
      status.resolve();
      return status.isOk();
    }
    return true;
  }

  public SourceHeader[] getUndoHeaderContent() {
    BackupRestorer restorer = new BackupRestorer(getTransaction(), undoKey,
        redoKey);

    return restorer.getHeaders();
  }

  public SourceHeader[] getRedoHeaders() {
    BackupRestorer restorer = new BackupRestorer(getTransaction(), redoKey,
        undoKey);

    return restorer.getHeaders();
  }

}
