/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.html;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class CrossReferencedHTMLTest extends RefactoringTestCase {
  public CrossReferencedHTMLTest() {
    super("");
  }

  public static Test suite() {
    return new TestSuite(CrossReferencedHTMLTest.class);
  }

  public void testcrossReferencedHTML1() throws Exception {
    CompilationUnit compilationUnit = null;
    Project project = getMutableProject();
    compilationUnit = (CompilationUnit) project.getCompilationUnits().get(0);
    final String outDirName
        = project.getPaths().getSourcePath().getRootSources()[0].getAbsolutePath();
    File expectedFile = new File(
        resolveTestName(Utils.getTestProjectsDirectory().getAbsolutePath()
        + '/', false) + "/MyClass1.html");
    String outFilePath
        = HTMLSourceEditor.doEditing(outDirName, compilationUnit, false, "\n");
    File outFile = new File(outFilePath);

    RwRefactoringTestUtils.compareWithDiff("", expectedFile, outFile);
  }

  public String getTemplate() {
    return "<extra_name>crossReferencedHTML/<stripped_test_name>/<in_out>/";
  }
}
