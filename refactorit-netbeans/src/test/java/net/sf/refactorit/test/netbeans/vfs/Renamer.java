/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vfs;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;


import junit.framework.Assert;


/**
 *
 * @author  RISTO A
 */
public class Renamer {

  public static void rename(final String from, final String to) {
    final DialogManager oldInstance = DialogManager.getInstance();
    DialogManager.setInstance(new NullDialogManager());
    try {
      renameWithCurrentDialogManager(from, to);
    } finally {
      DialogManager.setInstance(oldInstance);
    }
  }

  public static void renameWithCurrentDialogManager(final String from,
      final String to) {
    BinTypeRef fromTypeRef = getTypeRefForName(from);

    RenameType renamer
        = new RenameType(getNbContext(), fromTypeRef.getBinCIType());
    renamer.setNewName(to);

    RefactoringStatus result;
    try {

      result = renamer.apply();

    } finally {
      // removes old source files from sourcepath
      IDEController.getInstance().ensureProject(new LoadingProperties(false));
    }

    Assert.assertTrue(result.getAllMessages(), result.isOk());
  }

  private static BinTypeRef getTypeRefForName(final String fqn) {
    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    Project p = IDEController.getInstance().getActiveProject();
    BinTypeRef fromTypeRef = p.getTypeRefForName(fqn);
    Assert.assertNotNull("Type should exist: \"" + fqn + "\"", fromTypeRef);

    return fromTypeRef;
  }

  private static NBContext getNbContext() {
    return new NBContext(IDEController.getInstance().getActiveProject());
  }
}
