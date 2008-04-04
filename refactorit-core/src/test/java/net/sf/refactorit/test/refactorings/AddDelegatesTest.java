/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.delegate.AddDelegatesModel;
import net.sf.refactorit.refactorings.delegate.AddDelegatesRefactoring;
import net.sf.refactorit.refactorings.delegate.MethodNode;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 * @author Tonis Vaga
 */
public class AddDelegatesTest extends RefactoringTestCase {
  public AddDelegatesTest() {
    super(AddDelegatesTest.class.getName());
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(AddDelegatesTest.class);
    suite.setName("AddDelegatesTest");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(4));
  }

  public void testPrimitive() throws Exception {
    Project pr = getMutableProject();

    BinTypeRef ref = pr.findTypeRefForName("Primitive");
    BinField field = ref.getBinCIType().getDeclaredField("pField");

    Refactoring refactoring = new AddDelegatesRefactoring(
        IDEController.getInstance().createProjectContext(), field);

    assertTrue("checkPreconditions should fail on primitive type!",
        !refactoring.checkPreconditions().isOk());

  }

  public void testFinal() throws Exception {

    Project pr = getMutableProject();

    BinTypeRef ref = pr.findTypeRefForName("FinalTest");
    BinField field = ref.getBinCIType().getDeclaredField("field");

    BinMethod cantDelegateMethod = field.getTypeRef().
        getBinCIType().getDeclaredMethod("f", BinParameter.NO_PARAMS);

    assertTrue(cantDelegateMethod != null);

    List list = AddDelegatesRefactoring.createDelegateMethodsList(field);

    assertTrue("delegate list should not contain " + cantDelegateMethod,
        !list.contains(cantDelegateMethod));

  }

  public void testProtectedAccess() throws Exception {

    protectedAccessTest();

  }

  public void testProtectedAccess2() throws Exception {

    protectedAccessTest();

  }

  public void testProtectedAccess3() throws Exception {

    protectedAccessTest();

  }

  /**
   * @throws Exception
   */
  private void protectedAccessTest() throws Exception {
    Project pr = getMutableProject();

    BinTypeRef targetRef = pr.findTypeRefForName(
        "points2.Point2");

    assertTrue(targetRef != null);

    AddDelegatesRefactoring refactoring = new AddDelegatesRefactoring(
        new NullContext(pr), targetRef.getBinCIType());

    // must be called before getModel
    refactoring.checkPreconditions();

    final AddDelegatesModel model = refactoring.getModel();
    model.selectAll();

    refactoring.setModel(model);

    Project expected = getExpectedProject();

    RwRefactoringTestUtils.assertRefactoring(
        refactoring, expected,
        pr);
  }

  public void testIssue252() throws Exception {
    Project pr = getMutableProject();

    BinTypeRef ref = pr.findTypeRefForName("A");
    BinField field = ref.getBinCIType().getDeclaredField("buf");
    
    assertTrue(ref != null);


    AddDelegatesRefactoring refactoring = new AddDelegatesRefactoring(
    		new NullContext(pr), field);
    
    
    // must be called before getModel
    refactoring.checkPreconditions();

    final AddDelegatesModel model = refactoring.getModel();
    
    List list = model.getAllChildrenRecursively();
    for (int i = 0; i<list.size(); i++) {
    	if (list.get(i) instanceof MethodNode) {
    		MethodNode item = (MethodNode)list.get(i);
    		if ("toString()".equals(item.getNameForTextOutput())) {
    			item.setSelected(true);
    		}
    	}

    }
    //model.setValueAt(
    //model.selectAll();

    refactoring.setModel(model);
    

    Project expected = getExpectedProject();

    RwRefactoringTestUtils.assertRefactoring(
        refactoring, expected,
        pr);
  	
  }
  
  public String getTemplate() {
    return "AddDelegates/<test_name>/<in_out>";
  }
  
}
