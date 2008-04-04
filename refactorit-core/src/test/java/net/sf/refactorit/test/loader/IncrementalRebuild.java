/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.vfs.Source;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests how loading projects works.
 */
public class IncrementalRebuild extends TestCase {
//  /** Logger instance. */
//  private static final Category cat =
//    Category.getInstance(IncrementalRebuild.class.getName());

  /** Project under test. */
  private Project project;
  private BinMethod[] methods;
  private BinConstructor[] constructors;
  private BinField[] fields;

  public IncrementalRebuild(String name) throws Exception {
    super(name);
    initProject();
  }

  public static Test suite() {
    return new TestSuite(IncrementalRebuild.class);
  }

  public void initProject() throws Exception {
    project = LocalTypeTests.createAndLoadProject("IncrementalRebuild/project1");

    BinTypeRef aRef = project.getTypeRefForName("A");
    Source source = aRef.getBinCIType().getCompilationUnit().getSource();
    source.setLastModified(source.lastModified() + 1);

    BinClass aType = (BinClass) aRef.getBinCIType();

    methods = aType.getDeclaredMethods();
    constructors = aType.getDeclaredConstructors();
    fields = aType.getDeclaredFields();

    System.err.println("Project doInc:" + project.getProjectLoader().isIncrementalRebuild());
    project.getProjectLoader().build(null, false);
  }

  // FIXME: what about default constructors, that are created somehow differently?
  public void test1() throws Exception {
    BinCIType aType = project.getTypeRefForName("A").getBinCIType();
    BinMethod[] c_methods = aType.getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {
      assertTrue("methods not equal between rebuilds",
          methods[i] == c_methods[i]);
    }
  }

  public void test2() throws Exception {
    BinCIType aType = project.getTypeRefForName("A").getBinCIType();
    BinField[] c_fields = aType.getDeclaredFields();
    for (int i = 0; i < fields.length; ++i) {
      assertTrue("field not equal between rebuilds", fields[i] == c_fields[i]);
    }
  }

  public void test3() throws Exception {
    BinClass aType = (BinClass) project.getTypeRefForName("A").getBinType();
    BinConstructor[] c_constructors = aType.getDeclaredConstructors();
    for (int i = 0; i < constructors.length; ++i) {
      assertTrue("constructors not equal between rebuilds",
          constructors[i] == c_constructors[i]);
    }
  }
}
