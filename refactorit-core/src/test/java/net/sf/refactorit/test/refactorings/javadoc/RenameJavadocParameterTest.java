/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings.javadoc;



import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameLocal;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameJavadocParameterTest extends RefactoringTestCase {

  private static final Category cat =
      Category.getInstance(RenameJavadocParameterTest.class.getName());

  public RenameJavadocParameterTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameJavadoc/ParameterTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameJavadocParameterTest.class);
    suite.setName("Rename Javadoc Parameter");
    return suite;
  }

  private void renameMustWork(String methodName,
      BinParameter[] params_,
      String[] newNames
      ) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    BinTypeRef aRef = project.findTypeRefForName("p.testWork1");
    BinMethod method = aRef.getBinCIType().getDeclaredMethod(methodName,
        params_);
    BinParameter[] params = method.getParameters();
    BinItemReference[] paramRefs = new BinItemReference[params.length];
    for (int i = 0; i < params.length; i++) {
      paramRefs[i] = params[i].createReference();
    }

    for (int i = 0; i < paramRefs.length; i++) {
      BinParameter param = (BinParameter) paramRefs[i].restore(project);
      final RenameLocal renameLocal
          = new RenameLocal(new NullContext(project), param);
      renameLocal.setRenameInJavadocs(true);
      renameLocal.setNewName(newNames[i]);
      
      RefactoringStatus status = 
        renameLocal.apply();
      
      
      if (!status.isOk()) {
        fail("Renaming " + param.getQualifiedName() + " -> " + newNames[i]
            + " failed."
            + " Message: " + status.getAllMessages());
      } else {
        project.getProjectLoader().build(null, false);
      }
    }

    final Project expected = getExpectedProject();
    RwRefactoringTestUtils.assertSameSources("", expected, project);

    cat.info("SUCCESS");
  }

  public void testWork1() throws Exception {
    renameMustWork("f1", new BinParameter[] {new BinParameter("a1",
        BinPrimitiveType.INT_REF, 0),
        new BinParameter("a2", BinPrimitiveType.CHAR_REF, 0),
        new BinParameter("a3", BinPrimitiveType.DOUBLE_REF, 0)}
        ,
        new String[] {"a11", "a22", "a33"});
  }
}
