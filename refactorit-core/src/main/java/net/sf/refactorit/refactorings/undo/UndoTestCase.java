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
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.vfs.Source;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;


public abstract class UndoTestCase extends TestCase {

  net.sf.refactorit.refactorings.undo.SourceHeader[] sourceHeaders;

  /** Autogenerated proxy constructor. */
  public UndoTestCase() {
    super();
  }

  /**
   * @param project
   */
  protected void checkProjectState(final Project project) throws Exception {
    project.getProjectLoader().markProjectForCleanup();
    project.getProjectLoader().build(null, false);
    checkSourcesEquality(project, sourceHeaders);
  }

  /**
   * @param project
   */
  protected void saveProjectState(final Project project) throws Exception {
    project.getProjectLoader().build();
    sourceHeaders =
        extractHeadersFromSources(CompilationUnit.extractSourcesFromCompilationUnits(
        project.getCompilationUnits()));

  }

//  public void testUndoableStatus() throws Exception {
//   Project project = getMutableProject("bingo");
//   project.load();
//   List typesList = project.getDefinedTypes();
//   BinTypeRef types[] = new BinTypeRef[typesList.size()];
//
//   types = (BinTypeRef[]) typesList.toArray(types);
//
//   UndoManager manager = UndoManager.getInstance(project);
//
//   int count = 0;
//   for (int i = 0; i < 1 /*types.length*/; i++) {
//
//     manager.createTransaction("", "");
//
//     BinCIType type = types[i].getBinCIType();
//     if (type == null) {
//       continue;
//     }
//     ++count;
//     String newName = "NewName" + type.getName();
//     RenameType renameType = new RenameType(new NullContext(project), null,
//                                            type);
//     renameType.setNewName(newName);
//     renameType.checkPreconditions();
//     renameType.checkPreconditions();
//     renameType.performChange();
//     manager.commitTransaction();
//
//     UndoableTransaction transaction = manager.getCurrentTransaction();
  //BackupRestorer restorer=new BackupRestorer( transaction.getRepository(),transaction.getSourcePath(),);
//
//
//
  ////checkState(project);
//     assertTrue( "current transaction not null after rollback",manager.getCurrentTransaction()==null);
//   }
//
// }

  /**
   * @param project
   * @param sourceHeaders
   */
  public static void checkSourcesEquality(final Project project,
      final SourceHeader[] sourceHeaders) {
    checkHeadersEquality(sourceHeaders,
        extractHeadersFromSources(CompilationUnit.extractSourcesFromCompilationUnits(
        project.getCompilationUnits())));
  }

  public static net.sf.refactorit.refactorings.undo.SourceHeader[]
      extractHeadersFromSources(List sources) {

    SourceHeader[] result = new SourceHeader[sources.size()];
    for (int i = 0; i < sources.size(); i++) {
      result[i] = new net.sf.refactorit.refactorings.undo.SourceHeader((
          Source) sources.get(i));
    }
    return result;
  }

  public static void checkHeadersEquality(SourceHeader[] param1,
      SourceHeader[] param2) {
    assertTrue("different count of headers:" + param1.length + " vs " +
        param2.length, param1.length == param2.length);

    Comparator comp = new Comparator() {
      public int compare(Object obj1, Object obj2) {
        SourceHeader h1 = (SourceHeader) obj1, h2 = (SourceHeader) obj2;
        return h1.getAbsolutePath().compareTo(h2.getAbsolutePath());
      }
    };

    Arrays.sort(param1, comp);
    Arrays.sort(param2, comp);

    for (int i = 0; i < param1.length; i++) {
      assertTrue("header1=" + param1[i] + "\nheader2=" + param2[i],
          param1[i].equals(param2[i]));
    }

  }

}
