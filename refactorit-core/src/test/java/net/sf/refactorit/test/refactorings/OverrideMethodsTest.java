/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.delegate.OverrideMethodsModel;
import net.sf.refactorit.refactorings.delegate.OverrideMethodsRefactoring;
import net.sf.refactorit.refactorings.delegate.TypeRefHierarchyComparator;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class OverrideMethodsTest extends RefactoringTestCase {
  public OverrideMethodsTest() {
    super(OverrideMethodsTest.class.getName());
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(OverrideMethodsTest.class);
    suite.setName("OverrideMethodsTest");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(4));
  }

  public void test1() throws Exception {
    Project pr = getMutableProject();

    BinTypeRef ref = pr.findTypeRefForName("OverridesAbstract");

    OverrideMethodsRefactoring refactoring = new OverrideMethodsRefactoring(
        IDEController.getInstance().createProjectContext(),
        (BinClass) ref.getBinCIType());

    OverrideMethodsModel model = refactoring.getModel();
    model.selectAll();

    RwRefactoringTestUtils.assertRefactoring(
        refactoring, getExpectedProject(), pr);
  }

  public void testImplement() throws Exception {
    runTestOn("ImplementTest");
  }

  private void runTestOn(final String qName) throws Exception {
    Project pr = getMutableProject();

    BinTypeRef ref = pr.findTypeRefForName(qName);

    OverrideMethodsRefactoring refactoring = new OverrideMethodsRefactoring(
        IDEController.getInstance().createProjectContext(),
        (BinClass) ref.getBinCIType());

//      OverrideMethodsModel model = refactoring.getModel();
//      model.deselectAll();
//
//      model.selectMethod("actionPerformed");

    RwRefactoringTestUtils.assertRefactoring(
        refactoring, getExpectedProject(), pr);
  }

  public void testInterfaceInheritance() throws Exception {
    runTestOn("ImplementTest");
  }

  public void testTypeHierachyComparator() throws Exception {
    Project pr = RwRefactoringTestUtils.getMutableProject("bingo");
    pr.getProjectLoader().build();

    BinTypeRef playerRef = pr.getTypeRefForName(
        "bingo.player.Player");
    BinTypeRef objectRef = pr.getObjectRef();

    ArrayList subs = new ArrayList(playerRef.getAllSupertypes());

    subs.add(playerRef);

    Collections.sort(subs, new TypeRefHierarchyComparator());

    assertEquals(subs.get(0), playerRef);
    assertEquals(subs.get(subs.size() - 1), objectRef);

    for (int index = 0; index < subs.size(); ++index) {
      BinTypeRef item = (BinTypeRef) subs.get(index);

      if (index > 0) {
        assertTrue("wrongly sorted type hierachy ",
            !item.getAllSupertypes().contains(subs.get(index - 1)));
      }
    }
  }

  public String getTemplate() {
    return "OverrideMethods/<test_name>/<in_out>";
  }
}
