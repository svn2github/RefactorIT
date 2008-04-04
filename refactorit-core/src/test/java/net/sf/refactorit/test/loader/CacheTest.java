/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;



import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.loader.ASTTreeCache;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 * @author Tonis Vaga
 */
public class CacheTest extends junit.framework.TestCase {
  private Project project;

  List jspSources = new ArrayList();
  List javaSources = new ArrayList();

  public CacheTest() {
  }

  public void setUp() {
    try {
      project = Utils.createTestRbProject(Utils.getTestProjects().getProject(
          "bookstore2"));
      //project.getClassPath();
      project.getProjectLoader().build();
      List typeList = project.getDefinedTypes();
      Iterator it = typeList.iterator();

      while (it.hasNext()) {
        BinTypeRef ref = (BinTypeRef) it.next();
        CompilationUnit compilationUnit = ref.getBinCIType().getCompilationUnit();
        Source source = compilationUnit.getSource();
        if (FileUtil.isJspFile(source)
            ) {
          jspSources.add(source);
        } else {
          javaSources.add(source);
        }

        //type.getS
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException("bookstore2 project creating failed");
    }
  }

  public static Test suite() {
    TestSuite result = new TestSuite(CacheTest.class);
//    result.addTest(CacheTest.suite());
    return result;
  }

  public void testJSPCacheLoading() throws Exception {
    ASTTreeCache cache = project.getProjectLoader().getAstTreeCache();
    for (Iterator i = jspSources.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();
      assertNotNull(cache.checkJSPCacheFor(item));
    }
  }

  public void testRemoveJSPCache() throws Exception {
    project.getProjectLoader().build();
    List existingSources = javaSources;
    ASTTreeCache cache = project.getProjectLoader().getAstTreeCache();
    cache.removeNonExistingSources(existingSources);

    Iterator it2 = jspSources.iterator();
    while (it2.hasNext()) {
      Source source = (Source) it2.next();
      assertNull(cache.checkJSPCacheFor(source));
      //type.getS
    }

    // check if Java cache is ok
    for (Iterator i = javaSources.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();
      assertNotNull(cache.checkCacheFor(item));
    }
  }

  public void testRemoveJavaCache() throws Exception {
    project.getProjectLoader().build();

    ASTTreeCache cache = project.getProjectLoader().getAstTreeCache();
    cache.removeNonExistingSources(jspSources);

    for (int i = 0; i < javaSources.size(); ++i) {
      assertNull(cache.checkCacheFor((Source) javaSources.get(i)));
    }

    for (int i = 0; i < jspSources.size(); ++i) {
      assertNotNull(cache.checkCacheFor((Source) jspSources.get(i)));
    }
  }
}
