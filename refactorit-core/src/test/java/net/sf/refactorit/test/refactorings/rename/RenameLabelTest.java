/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameLabel;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RenameLabelTest extends RefactoringTestCase {

  private static final String[][] names =
  {{"staticLoop","loop1","localLoop","innerLoop","anonymousLoop"},
    {"staticNew","loop1New","localNew","innerNew","anonymousNew"}};

  public RenameLabelTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameLabel/<in_out>";
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(RenameLabelTest.class, "Rename label test");
    return suite;
  }

  public void testLabelRename() throws Exception {
    final Project project =
      RwRefactoringTestUtils.createMutableProject(getInitialProject());
  project.getProjectLoader().build();

  BinTypeRef aRef = project.findTypeRefForName("RenameLabel.A");

  LabelNameIndexer visitor = new LabelNameIndexer(names[0]);
  aRef.getBinCIType().accept(visitor);
  Map statements = visitor.getResults();

  for(int i = 0; i < names[0].length; i++) {
    BinLabeledStatement statement = (BinLabeledStatement) statements.get(names[0][i]);
    assertTrue("Label " + names[0][i] + " not found in " + aRef, statement != null);
    RenameLabel renamer = new RenameLabel(new NullContext(project), statement);
    renamer.setNewName(names[1][i]);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());
    status.merge(renamer.apply());
    assertTrue("Renaming " + names[0][i] + " -> " + names[1][i]
        + " succeeded: " + status.getAllMessages(),
        status.isOk());
  }

  RwRefactoringTestUtils.assertSameSources("Rename labels",
      getExpectedProject(), project);
  }

  private class LabelNameIndexer extends BinItemVisitor {
    private Map found = new HashMap();
    public LabelNameIndexer(String[] names) {
      for(int i = 0; i < names.length; i++) {
        found.put(names[i], null);
      }
    }

    public void visit(BinLabeledStatement statement) {
      String name = statement.getLabelIdentifierName();
      if(name != null && found.keySet().contains(name)) {
        found.put(name, statement);
      }
      super.visit(statement);
    }

    public Map getResults() {
      return found;
    }
  }
}
