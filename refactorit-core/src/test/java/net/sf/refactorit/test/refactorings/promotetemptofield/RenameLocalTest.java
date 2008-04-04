/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.LocalVariableNameIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.refactorings.rename.RenameLocal;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class RenameLocalTest extends TestCase {
  public static Test suite() {
    return new TestSuite(RenameLocalTest.class);
  }

  public RenameLocalTest(String name) {super(name);
  }

  private Project p;
  private BinLocalVariable var;
  ManagingIndexer supervisor;
  private DialogManager oldDialogManager;

  public void setUp() throws Exception {
    p = Utils.createTestRbProjectFromArray(new String[] {
        "class X{",
        "  void m(){",
        "    int i = 0;",
        "    int usage = i;",
        "  }",
        "}"
    });

    var = (BinLocalVariable) ItemByNameFinder.findVariable(p, "i");
    supervisor = new ManagingIndexer();

    oldDialogManager = DialogManager.getInstance();
  }

  public void tearDown() {
    DialogManager.setInstance(oldDialogManager);
  }

  public void testNormalParameterIndexerOperation() throws Exception {
    new LocalVariableNameIndexer(supervisor, var);
    supervisor.callVisit(p, false);

    assertEquals(2, supervisor.getInvocations().size());
    assertEquals(BinLocalVariableDeclaration.class,
        ((InvocationData) supervisor.getInvocations().get(0)).getInConstruct().
        getClass());
    assertEquals(BinVariableUseExpression.class,
        ((InvocationData) supervisor.getInvocations().get(1)).getInConstruct().
        getClass());
  }

  public void testSkippingDeclarations() throws Exception {
    LocalVariableNameIndexer indexer
        = new LocalVariableNameIndexer(supervisor, var);
    indexer.setSkipDeclarations(true);
    supervisor.callVisit(p, false);

    assertEquals(1, supervisor.getInvocations().size());
    assertEquals(BinVariableUseExpression.class,
        ((InvocationData) supervisor.getInvocations().get(0)).getInConstruct().
        getClass());
  }

  public void testRenameLocalWithoutRenamingDeclaration() throws Exception {
    Project after = Utils.createTestRbProjectFromArray(new String[] {
        "class X{",
        "  void m(){",
        "    int i = 0;",
        "    int usage = newName;",
        "  }",
        "}"
    });

    RenameLocal renamer = new RenameLocal(new NullContext(p), var);
    renamer.setNewName("newName");
    renamer.setSkipDeclarations(true);

    RwRefactoringTestUtils.assertRefactoring(renamer, p, after);
  }

  public void testNoConfirmationWindowPopsUp() throws Exception {
    final boolean poppedUp[] = new boolean[] {false};

    DialogManager.setInstance(new NullDialogManager() {
      public BinTreeTableModel showConfirmations(
          RefactorItContext context, BinTreeTableModel model, String helpTopicId
      ) {
        poppedUp[0] = true;

        return model;
      }

      public BinTreeTableModel showConfirmations(
          RefactorItContext context, BinTreeTableModel model,
          String description, String helpTopicId
      ) {
        poppedUp[0] = true;

        return model;
      }
    });

    PromoteTempToField promoter =
        new PromoteTempToField(new NullContext(p), var, "newName", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
        promoter.apply();

    assertFalse(poppedUp[0]);
  }
}
