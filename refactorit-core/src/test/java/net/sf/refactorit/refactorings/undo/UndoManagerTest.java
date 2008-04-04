/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.vfs.Source;

import javax.swing.undo.UndoableEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class UndoManagerTest extends UndoTestCase {
  public UndoManagerTest() {
  }

  public static Test suite() {
    return new TestSuite(UndoManagerTest.class);
  }

  public void testCreateNewFile() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();

    saveProjectState(project);


    IUndoManager manager = RitUndoManager.getInstance(project);
    manager.createTransaction("createNewFile test", "");

    Source parent = project.getPaths().getSourcePath().getRootSources()[0];

    String filename = "TempTest.tst";

    //should remove, when be moved on Editors
    IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (trans != null) {
      undo = trans.createCreateFileUndo(new SourceInfo(parent, filename));
    }

    Source source = parent.createNewFile(filename);

    if (trans != null && source !=null) {
      trans.addEdit(undo);
    }

    assertTrue("file creating failed", source != null);

    String filePath = source.getAbsolutePath();

    manager.commitTransaction();
    manager.undo();
    project.getProjectLoader().build();

    assertTrue("undo failed", UndoUtil.findSource(project.getPaths().getSourcePath(),
        filePath) == null);

    manager.redo();

    project.getProjectLoader().build();
    assertTrue("redo failed "+" filepath ="+filePath+", sourcepath="+project.getPaths().getSourcePath().getAllSources(), UndoUtil.findSource(project.getPaths().getSourcePath(),
        filePath) != null);
    manager.undo();

    checkProjectState(project);
  }

  public void testRenameTo() throws Exception {
    final String projectName = "bingo";
    Project project = RwRefactoringTestUtils.getMutableProject(projectName);
    project.getProjectLoader().build();

    saveProjectState(project);

    IUndoManager manager = RitUndoManager.getInstance(project);
    manager
        .createTransaction("createNewFile test", "");

    Source src = ((CompilationUnit) project.getCompilationUnits().get(0)).getSource();

    Source destParent = src.getParent();
    assertTrue(destParent != null);

    String fileName = "TempTest.java";

    // remove it when, undo be moved to Editors
    IUndoableTransaction undoTransaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;


    if (undoTransaction != null) {
      undo = undoTransaction.createRenameFileUndo(src, destParent, fileName);
    }

    Source dest = src.renameTo(destParent, fileName);

    if (undoTransaction != null && dest != null) {
      undoTransaction.addEdit(undo);
    }


    assertTrue("file " + src.getName() + " renaming failed", dest != null);

    SourceInfo destInfo = new SourceInfo(dest);

    manager.commitTransaction();
    manager.undo();
    project.getProjectLoader().build();

    assertTrue("undo failed", UndoUtil.findSource(project.getPaths().getSourcePath(),
        destInfo) == null);

    manager.redo();

    project.getProjectLoader().build();
    assertTrue("redo failed", UndoUtil.findSource(project.getPaths().getSourcePath(),
        destInfo) != null);
    manager.undo();

    checkProjectState(project);
  }

  public void testRenameType() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();
    saveProjectState(project);
    List typesList = project.getDefinedTypes();
    BinTypeRef types[] = new BinTypeRef[typesList.size()];

    types = (BinTypeRef[]) typesList.toArray(types);

    IUndoManager manager = RitUndoManager.getInstance(project);

    int count = 0;
    for (int i = 0; i < 1 /*types.length*/; i++) {
      manager.createTransaction("renameTypeTest", "");

      BinCIType type = types[i].getBinCIType();
      if (type == null) {
        continue;
      }
      ++count;
      String newName = "NewName" + type.getName();
      RenameType renameType = new RenameType(new NullContext(project), type);
      renameType.setNewName(newName);
      renameType.checkPreconditions();
      renameType.checkPreconditions();
      renameType.apply(); //TransformationManager.performTransformationFor(renameType);

      manager.commitTransaction();
      manager.undo();

      manager.redo();
      manager.undo();
      project.getProjectLoader().markProjectForCleanup();

      checkProjectState(project);

    }
  }

  public void testMoveTypeRefactoring() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();

    saveProjectState(project);
    List typesList = project.getDefinedTypes();
    BinTypeRef types[] = new BinTypeRef[typesList.size()];

    types = (BinTypeRef[]) typesList.toArray(types);

    RitUndoManager manager = RitUndoManager.getInstance(project);
    BinPackage[] packages = project.getAllPackages();

    int count = 0;
    for (int i = 0; i < 1 /*types.length*/; i++) {
      manager.createTransaction("moveTypeTest", "");

      BinCIType type = types[i].getBinCIType();
      if (type == null) {
        continue;
      }

      ++count;

      MoveType mover = new MoveType(new NullContext(project), type);
      BinPackage newPackage = null;

      for (int index = 0; index < packages.length; index++) {
        if (packages[index] != type.getPackage()) {
          newPackage = packages[index];
          break;
        }
      }

      assertTrue(newPackage != null);

      mover.setTargetPackage(newPackage);

      mover.apply().isOk();
      /*TransformationManager
          .performTransformationFor(mover).isOk();*/
      if (!RitUndoManager.getCurrentTransaction().isSignificant()) {
        System.out.println("[tonisdebug]: transaction not significant?!!!");
      } else {
        manager.commitTransaction();
        manager.undo();

        manager.redo();
        manager.undo();
        project.getProjectLoader().markProjectForCleanup();
      }

      checkProjectState(project);
    }
  }

  public void testTransactionRepository() throws Exception {
    if (RitUndoManager.N_UNDOS < 2) {
      System.err.println("Too small allowed undo size, cannot test");
      return;
    }

    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();

    final int N = RitUndoManager.N_UNDOS;

    IUndoManager manager = RitUndoManager.getInstance(project);

    //Set repositories=new HashSet(N);
    LinkedList repositories = new LinkedList();

    for (int i = 0; i < N; i++) {
      UndoableTransaction transaction = manager.createTransaction("", "");
      BackupRepository repository = transaction.getRepository();

      IUndoableEdit edit = new SingleUndoableEdit(transaction) {
        public boolean isSignificant() {
          return true;
        }
      };

      transaction.addEdit(edit);
      manager.commitTransaction();

      assertTrue("Error:transaction doesn't have unique repository!!",
          !repositories.contains(repository));
      repositories.addFirst(repository);
    }

    for (int i = 0; i < N; i++) {
      repositories.removeLast(); // remove last and we should have one unique repos more
      UndoableTransaction transaction = manager.createTransaction("", "");
      BackupRepository repository = transaction.getRepository();
      IUndoableEdit edit = new SingleUndoableEdit(transaction) {
        public boolean isSignificant() {
          return true;
        }
      };

      transaction.addEdit(edit);
      manager.commitTransaction();

      assertTrue("Error: transaction doesn't have unique repository!!",
          !repositories.contains(repository));
      repositories.addFirst(repository);
    }

//    Set keys = new HashSet();


//    // test key uniqu
//    for ( int i=0; i < N  ; ++i ) {
//      keys.add(repository.generateNewKey());
//    }
//    assertTrue("repository generated some not unique keys", keys.size()== N );
  }

  public void testRollBack() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();
    saveProjectState(project);
    List typesList = project.getDefinedTypes();
    BinTypeRef types[] = new BinTypeRef[typesList.size()];

    types = (BinTypeRef[]) typesList.toArray(types);

    RitUndoManager manager = RitUndoManager.getInstance(project);

    int count = 0;
    for (int i = 0; i < 1 /*types.length*/; i++) {

      manager.createTransaction("renameTypeTest", "");

      BinCIType type = types[i].getBinCIType();
      if (type == null) {
        continue;
      }
      ++count;
      String newName = "NewName" + type.getName();
      RenameType renameType = new RenameType(new NullContext(project), type);
      renameType.setNewName(newName);
      renameType.checkPreconditions();
      renameType.checkPreconditions();
      renameType.apply();//TransformationManager.performTransformationFor(renameType);
      manager.rollbackTransaction();
      checkProjectState(project);

      assertTrue("current transaction not null after rollback",
          RitUndoManager.getCurrentTransaction() == null);
    }
  }

  public void testCanUndoCanRedo() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();

    IUndoManager manager = RitUndoManager.getInstance(project);

    UndoableTransaction transaction = manager.createTransaction("", "");

    List typesList = project.getDefinedTypes();
    BinTypeRef types[] = new BinTypeRef[typesList.size()];

    types = (BinTypeRef[]) typesList.toArray(types);

    BinCIType type = types[0].getBinCIType();

    Source source = type.getCompilationUnit().getSource();

    SourceContentUndo undo = new SourceContentUndo(
        new ArrayList(Collections.singletonList(source)), transaction);

    transaction.addEdit(undo);

    manager.commitTransaction();

    project.getProjectLoader().build();

    assertTrue(manager.canUndo());
    manager.undo();
    assertTrue(manager.canRedo());
    manager.redo();
    assertTrue(manager.canUndo());

    long currentTime = System.currentTimeMillis();
    //Thread.currentThread().sleep(100);

    source.setLastModified(currentTime + 1000);

    assertTrue(source.lastModified() > currentTime);
    assertTrue(manager.canUndo());

    UndoableStatus status = new BackupRestorer(transaction,
        transaction.getRepository(),
        project.getPaths().getSourcePath(), undo.undoKey, undo.redoKey, currentTime)
        .getUndoableStatus();

    // assert conflicts
    assertTrue(status instanceof ModifiedStatus);

    manager.undo();

    currentTime = System.currentTimeMillis();
    //Thread.currentThread().sleep(100);

    source.setLastModified(currentTime + 1000);

    assertTrue(manager.canRedo());

    status = new BackupRestorer(transaction, transaction.getRepository(),
        project.getPaths().getSourcePath(), undo.redoKey, undo.undoKey, currentTime)
        .getUndoableStatus();

    assertTrue(status instanceof ModifiedStatus);
  }

  public void testDeleteFileUndo() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");
    project.getProjectLoader().build();

//    BackupManagerTest.
//        extractHeadersFromSources(SourceUtil.extractSourcesFromCompilationUnits(
//        project.getCompilationUnitList()));

    Source root = project.getPaths().getSourcePath().getRootSources()[0];

    String filename = "undoTestFile.java";

    //should remove, when be moved on Editors
    IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (trans != null) {
      undo = trans.createCreateFileUndo(new SourceInfo(root, filename));
    }

    Source source = root.createNewFile(filename);

    if (trans != null && source !=null) {
      trans.addEdit(undo);
    }


    java.io.OutputStream output = source.getOutputStream();
    output.write(new String("class ThisClassDoesntExist { }").getBytes());
    output.flush();

    output.close();

    Source emptyDir = root.mkdir("undoTestDir--");

    project.getProjectLoader().build(null, false);

    saveProjectState(project);

    // test file
    deleteFileUndoTest(project, source);

    // test directory
    deleteFileUndoTest(project, emptyDir);

    checkProjectState(project);

    // remove it when, undo be moved to Editors
    IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
    if (transaction != null) {
      IUndoableEdit und = transaction.createDeleteFileUndo(source);
      transaction.addEdit(undo);
    }
    if (transaction != null) {
      IUndoableEdit und = transaction.createDeleteFileUndo(emptyDir);
      transaction.addEdit(undo);
    }

    source.delete();
    emptyDir.delete();

  }

  private void deleteFileUndoTest(final Project project,
      final Source source) throws Exception {
    project.getProjectLoader().build();

    String name = source.getName();

    Source parent = source.getParent();

    IUndoManager manager = RitUndoManager.getInstance(project);
    IUndoableTransaction transaction = manager.createTransaction(
        "deleteFile undo", "");

    transaction.createDeleteFileUndo(source);
    SourceInfo srcInfo = new SourceInfo(source);

    // remove it when, undo be moved to Editors
    IUndoableTransaction trans = RitUndoManager.
        getCurrentTransaction();
    if (transaction != null) {
      IUndoableEdit undo = trans.createDeleteFileUndo(source);
      trans.addEdit(undo);
    }

    boolean result = source.delete();

    assertTrue("Source.delete() failed!", result);

    manager.commitTransaction();

    project.getProjectLoader().build();

    assertTrue(parent.getChild(name) == null);

    manager.undo();
    project.getProjectLoader().build();

    Source src = UndoUtil.findSource(project.getPaths().getSourcePath(), srcInfo);
    assertTrue("undo failed, file not created", src != null);

    manager.redo();

    assertTrue(parent.getChild(name) == null);

    manager.undo();
  }

  public void testNotSignificantCannotUndo() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("bingo");

    IUndoManager manager = RitUndoManager.getInstance(project);
    UndoableTransaction transaction = manager.createTransaction("", "");

    UndoableEdit edit = new SingleUndoableEdit(transaction) {
      public boolean isSignificant() {
        return false;
      }
    };

    transaction.addEdit(edit);
    manager.commitTransaction();

    assertTrue(!manager.canUndo());
    assertTrue(!manager.canRedo());
  }
}
