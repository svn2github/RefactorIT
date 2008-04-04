/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.rename.ParentFinder;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;
import net.sf.refactorit.vfs.local.LocalSource;

import java.util.Collections;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  risto */
public class ParentFinderTest extends TestCase {
  private ParentFinder f;

  public ParentFinderTest(String name) {super(name);
  }

  public static Test suite() {
    return new TestSuite(ParentFinderTest.class);
  }

  public void setUp() {
    f = new ParentFinder();
  }

  public void testFindParent() throws Exception {
    assertEquals(MockSource.getSource("/a"), f.findParent("a",
        MockSource.getSource("/a/X")));
    assertEquals(MockSource.getSource("/a"), f.findParent("a",
        MockSource.getSource("/a/b/X")));
    assertEquals(MockSource.getSource("/a/b"), f.findParent("a.b",
        MockSource.getSource("/a/b/X")));
    assertEquals(MockSource.getSource("/a/b"), f.findParent("a.b",
        MockSource.getSource("/a/b/c/d/X")));
    assertEquals(MockSource.getSource("/x/y/z"), f.findParent("y.z",
        MockSource.getSource("/x/y/z/X")));
    assertNull(f.findParent("a", MockSource.getSource("a.X")));
  }

  public void testFindRoot() throws Exception {
    assertEquals(MockSource.getSource(""), f.findRoot("a",
        MockSource.getSource("/a/X")));
    assertEquals(MockSource.getSource(""), f.findRoot("a.b",
        MockSource.getSource("/a/b/X")));
    assertEquals(MockSource.getSource("/x"), f.findRoot("y.z",
        MockSource.getSource("/x/y/z/X")));
  }

  public void testFindParents() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "package a; public class X {}", "X.java", "a");

    Set result = f.findFolders("a",
        Collections.singletonList(p.getPackageForName("a")));

    assertEquals(1, result.size());
    assertEquals("a", ((Source) result.iterator().next()).getName());

    assertEquals(0, f.getSourcesInWrongFolders().size());
  }

  public void testFindParents_badFolderName() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "package a; public class X {}", "X.java", "bad-folder-name");

    Set result = f.findFolders("a",
        Collections.singletonList(p.getPackageForName("a")));

    assertEquals(0, result.size());

    assertEquals(1, f.getSourcesInWrongFolders().size());
    assertEquals(p.getCompilationUnitForName("bad-folder-name/X.java").getSource(),
        f.getSourcesInWrongFolders().get(0));
  }

  // utilities

  private static class MockSource extends LocalSource {
    private final String path;

    private MockSource(String path) {
      super(null);
      this.path = path;
    }

    public static final MockSource getSource(String path) {
      Source result = SourceMap.getSource(path);
      if (result == null || !(result instanceof MockSource)) {
        result = new MockSource(path);
        SourceMap.addSource(result);
      }
      return (MockSource) result;
    }

    public Object getIdentifier() {
      return this.path;
    }

    public String getAbsolutePath() {
      return this.path;
    }

    public Source getParent() {
      if (getAbsolutePath().indexOf("/") < 0) {
        return null;
      } else {
        return MockSource.getSource(getAbsolutePath().substring(0,
            getAbsolutePath().lastIndexOf("/")));
      }
    }

    public boolean equals(Object o) {
      if (o == null) {
        return false;
      } else {
        return ((MockSource) o).getAbsolutePath().equals(getAbsolutePath());
      }
    }

    public String getName() {
      if (getAbsolutePath().indexOf("/") < 0) {
        return getAbsolutePath();
      } else {
        return getAbsolutePath().substring(
            getAbsolutePath().lastIndexOf("/") + 1);
      }
    }

    public String toString() {
      return getAbsolutePath();
    }
  }
}
