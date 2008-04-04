/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.nonjava;



import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.query.text.PackageQualifiedNameIndexer;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class WhereUsedPackageTest extends RefactoringTestCase {
  private static final Category cat =
      Category.getInstance(WhereUsedPackageTest.class.getName());

  public WhereUsedPackageTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "NonJavaFiles/WhereUsedPackage";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(WhereUsedPackageTest.class);
    suite.setName("Find package name occurrences in non-java files");
    return suite;
  }

  public void testSimple() throws Exception {
    cat.info("Testing WhereUsedPackage");
    final Project project = getMutableProject();
    BinPackage pack = project.getPackageForName("com.acme");
    ManagingNonJavaIndexer supervisor =
        new ManagingNonJavaIndexer(new WildcardPattern[] {new WildcardPattern(
        "*.xml")});

    new PackageQualifiedNameIndexer(supervisor, pack);

    supervisor.visit(project);

    Occurrence[] result = (Occurrence[]) supervisor.getOccurrences().toArray(new
        Occurrence[0]);
    assertEquals("Number of occurrences", 3, result.length);
    assertEquals(3, result[0].getLine().getLineNumber());
    assertEquals(40, result[0].getStartPos());
    assertEquals(11, result[1].getLine().getLineNumber());
    assertEquals(16, result[2].getLine().getLineNumber());
    cat.info("SUCCESS");
  }
}
