/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.SourcesModificationOperation;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.loader.RebuildLogic;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.PathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.SourceUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Tonis Vaga
 */
public class BackupRestorer {
  BackupRepository rep;
  String undoKey;
  String redoKey;
  SourcePath sourcePath;
  RepositoryTransaction transaction;

  Source[] sources;

  long backupTime;

  public BackupRestorer(RepositoryTransaction transaction, String undoKey,
      String redoKey){
    this(transaction, transaction.getRepository(), transaction.getSourcePath(),
        undoKey, redoKey, transaction.getFinishTime());
  }

  /**
   *
   * @param rep rep
   * @param sourcepath sourcepath
   * @param undoKey undoKey
   * @param redoKey redo info file name, null if no redo info need to be generated
   * @param lastModificationTime lastModificationTime
   */
  public BackupRestorer(RepositoryTransaction transaction,
      BackupRepository rep, SourcePath sourcepath, String undoKey,
      String redoKey, long lastModificationTime) {
    this.transaction = transaction;
    this.rep = rep;
    this.undoKey = undoKey;
    this.redoKey = redoKey;
    this.backupTime = lastModificationTime;
    sourcePath = sourcepath;
  }

  public void restore() throws IOException {
    if (Assert.enabled) {
      Assert.must(BackupRepository.isKeyValid(undoKey)
          && BackupRepository.isKeyValid(redoKey),
          "wrong key");
    }

    SourcesModificationOperation op = new SourcesModificationOperation() {
      protected void runImpl() throws IOException {
        restoreBackupFromDir();
      }
    };

    IDEController.getInstance().run(op);

    if (op.getException() instanceof IOException) {
      throw (IOException)op.getException();
    }

    if (op.getException() != null) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,op.getException());
    }
  }

  void restoreBackupFromDir() throws IOException {
    Assert.must(!undoKey.equals(redoKey),
        "undo and redo keys are same=" + undoKey);

    if (sourcePath.getRootSources().length == 0) {
      throw new IllegalStateException(
          "Can not restore files: sourcepath root does not exist");
    }

    long startTime = System.currentTimeMillis();
    int headersCount = 0;

    String backupDir = rep.getBackupDir();

    //BackupHeaderReader headerReader = null;
    BackupFileReader fileReader = null;

    try {
      String backupFileName = backupDir + File.separatorChar + undoKey;
      //headerReader = new BackupHeaderReader(headerFileName);
      fileReader = new net.sf.refactorit.refactorings.undo.BackupFileReader(backupFileName);
      SourceHeader[] headers = getHeaders();

      processHeadersAndSources(headers, true);

//      Assert.must( sourcesList.size() == headers.length);
      Assert.must(sources.length > 0 && sources.length == headers.length);

      if (redoKey.length() > 0) {
        // create redo inf
//        List redoList = new ArrayList(sourcesList);
        List redoList = new ArrayList(Arrays.asList(sources));

        BackupManagerUtil.backupSourcesToDir(redoList, backupDir, redoKey);
      }

      for (int i = 0; i < headers.length; i++) {
//        destSource = (Source) sourcesList.get(i);
        Source destSource = sources[i];

        SourceHeader header = headers[i];
        if (destSource == null) {
          RuntimePlatform.console.println("RefactorIT could not create file "
              + headers[i].fileAbsolutePath);
          continue;
        }

        long lastModifiedBeforeWrite = destSource.lastModified();
        if (lastModifiedBeforeWrite == header.lastModified &&
            destSource.length() == header.fileLength) {
//         DebugInfo.trace("file "+destSource.getName()+" not modified, will not overwrite");
          // file same, do not restore
          fileReader.skipHeader(header);
          continue;
        }

        RebuildLogic rebuildLogic = IDEController.getInstance()
            .getActiveProject().getProjectLoader().getRebuildLogic();

        byte contents[] = fileReader.getContentsFor(header);

        OutputStream output = null;
        try {
          output = destSource.getOutputStream();

          output.write(contents, 0, header.fileLength);

          if (RitUndoManager.debug) {
            //FIXME: remove this
            System.out.println("[tonisdebug]:restored file " +
                destSource.getAbsolutePath());
          }
        } finally {
          if (output != null) {
            output.flush();
            output.close();

            rebuildLogic.forceSourceModified(destSource);

            if (transaction instanceof UndoableTransaction){
              ((UndoableTransaction) transaction).createLastModifiedRedirect(
                  destSource);
            }

            headersCount++;
          } else {
            RuntimePlatform.console.println(
                "cannot restore, output stream == null for "
                + destSource.getAbsolutePath());
          }
        }
      }
    } finally {
      //      if ( headerReader!=null ) {
      //        headerReader.close();
      //      }
      if (fileReader != null) {
        fileReader.close();
      }
    }

    AppRegistry.getLogger(this.getClass()).debug(
        "Restoring " + headersCount + " files took " +
        (System.currentTimeMillis() - startTime) + "mms");
  }

  private String getHeaderPath() {
    String headerFileName = BackupManagerUtil.extractHeaderFilePath(rep.
        getBackupDir(), undoKey);

    return headerFileName;
  }

  /**
   * @param headers source headers
   * @param createNewFiles createNewFiles
   * SIDE effects: creates new source if parent exist but destination source doesn't
   */
  private void processHeadersAndSources(SourceHeader[] headers,
      boolean createNewFiles) {

    Source rootSource;

//      List sourcesList=new ArrayList();
    sources = new Source[headers.length];
    Source destSource;

    Source[] rootSources = sourcePath.getRootSources();

    for (int headerIndex = 0; headerIndex < headers.length; ++headerIndex) {
      SourceHeader item = headers[headerIndex];

      if (item == null) {
        System.err.println("Undo: one of the headers is null");
        continue;
      }
      rootSource = SourceUtil.findRootSource(rootSources, item.getRootPath());

      if (rootSource == null) {
        AppRegistry.getLogger(this.getClass()).debug("Finding rootsource failed for!!" + item.getRootPath());
        destSource = null;
      } else {
        destSource = rootSource.getChild(item.getRelativePath());

        if (destSource == null) {
          if (createNewFiles) {
            PathElement element = FileUtil.extractPathElement(item.getRelativePath(),
                item.getSeparatorChar());
            Source parent = rootSource.getChild(element.dir);
            if (parent != null && !parent.isDirectory()) {
              AppRegistry.getLogger(this.getClass()).debug(
                  "BackupRestorer: should be directory, deleting "
                  + parent.getName());

              // remove it when, undo be moved to Editors
              IUndoableTransaction trans = RitUndoManager.
                  getCurrentTransaction();
              IUndoableEdit undo = null;
              if (trans != null) {
                undo = trans.createDeleteFileUndo(parent);
                trans.addEdit(undo);
              }

              boolean result = parent.delete();

              if (trans != null && result ) {

              }


              parent = null;
            }
            if (parent == null && createNewFiles) {
              parent = rootSource.mkdirs(element.dir);

            }
            if (parent == null) {
              AppRegistry.getLogger(this.getClass()).debug("Error: parent for " + element.dir
              + " == null!!!");
            } else {
              // should be meanless but yo never know how  NB works

              destSource = parent.getChild(element.file);
              if (destSource == null) {

                try {

                  //should remove, when be moved on Editors
                  IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
                  IUndoableEdit undo = null;

                  if (trans != null) {
                    undo = trans.createCreateFileUndo(
                        new SourceInfo(parent, element.file));
                  }

                  destSource = parent.createNewFile(element.file);

                  if (trans != null && destSource != null) {
                    trans.addEdit(undo);
                  }


                } catch (IOException ex) {
                  ex.printStackTrace(RuntimePlatform.console);
                }
                if (destSource == null) {
                  AppRegistry.getLogger(this.getClass()).debug("createNewFile failed for " + element.file);
                }
              }
            }
          }
        }
//        Assert.must(destSource==null || destSource.isFile());
      }
      sources[headerIndex] = destSource;
      //        sourcesList.add(destSource);
    }
  }

  public UndoableStatus getUndoableStatus() {
    UndoableStatus result;
    List cantCreateList = new LinkedList();
    List modifiedList = new LinkedList();
    SourceHeader[] headers = getHeaders();

    processHeadersAndSources(headers, false);

    for (int i = 0; i < headers.length; i++) {
      if (sources[i] == null) {
        //cantCreateList.add(headers[i]);
        continue;
      }
      if (Assert.enabled) {
        Assert.must(headers[i].fileAbsolutePath.equals(sources[i].
            getAbsolutePath()),
            "paths should be equal " + headers[i].fileAbsolutePath +
            " vs " + sources[i].getAbsolutePath());
      }

      if (wasExternallyModified(sources[i], headers[i])) {
        modifiedList.add(sources[i]);
      }
    }
    if (cantCreateList.size() > 0) {
      result = new CannotCreateStatus(cantCreateList);
    } else if (modifiedList.size() > 0) {
      result = new ModifiedStatus(modifiedList);
    } else {
      result = new UndoableStatus(UndoableStatus.OK);
    }
    return result;
  }

  private boolean wasExternallyModified(final Source source,
      final SourceHeader header) {
    boolean result;
    RitUndoManager manager = RitUndoManager.getInstance();

    if (manager == null){
      result = source.lastModified() > backupTime;
    } else {
      result = manager.getActualLastModified(header.getAbsolutePath(),
          source.lastModified()) > backupTime;
    }
    return result && source.length() != 0;
  }

  public SourceHeader[] getHeaders() {
    try {
      BackupHeaderReader headerReader = new BackupHeaderReader(getHeaderPath());

      SourceHeader[] result = new SourceHeader[headerReader.getHeadersCount()];

      SourceHeader header = null;
      try {
        for (int i = 0; i < result.length; i++) {
          header = headerReader.nextHeader();
          result[i] = header;
        }
      } finally {
        if (headerReader != null) {
          headerReader.close();
        }
      }
      return result;
    } catch (IOException ex) {
      ex.printStackTrace(RuntimePlatform.console);
      return null;
    }
  }
}
