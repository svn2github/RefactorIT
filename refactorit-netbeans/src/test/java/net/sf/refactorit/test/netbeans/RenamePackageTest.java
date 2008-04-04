/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.refactorings.undo.IUndoManager;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.vfs.Source;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class RenamePackageTest extends NbTestCase {

  public void testBugRim464() throws Exception {
    Source source = createNewFile(
        "X.java", 
        "package com.pac; public class X {}", 
        "com/pac", 
        getRoot());
    
    
    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    Project project = IDEController.getInstance().getActiveProject();
    
    IUndoManager undoManager = RitUndoManager.getInstance(project);
    IUndoableTransaction transaction = undoManager.createTransaction("Blah", "blah");
    
    RenamePackage r = new RenamePackage(IDEController.getInstance().createProjectContext(),
        project.getPackageForName("com.pac"));
    r.setRenamePrefix(true);
    r.setPrefix("com");
    r.setNewName("org");
    RefactoringStatus status = r.apply();
    undoManager.commitTransaction();
    
    assertTrue(status.getAllMessages(), status.isOk());
  }
}
