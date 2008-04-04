/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.commonIDE;


import net.sf.refactorit.RitTestCase;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;


public class ItemByCoordinateFinderTest extends RitTestCase {
  private static final Category cat =
      Category.getInstance(ItemByCoordinateFinderTest.class.getName());

  public ItemByCoordinateFinderTest() {
  }

  public void setUp() throws Exception {
  }

  public void tearDown() {
  }

  public void testAnonymousClass() {
    cat.info("Testing clicking on anonymous class");

    Project project = Utils.createTestRbProjectFromString(
        "public class Test {\n" +
        "  {new Test() {};}\n" +
        "}"
    );
    assertFalse(
//        CollectionUtil.toList(project.getProjectLoader().getErrorCollector().getUserFriendlyErrors()).toString(),
        project.getProjectLoader().getErrorCollector().hasErrors());
    ItemByCoordinateFinder ibcf = new ItemByCoordinateFinder(
        (CompilationUnit) project.getCompilationUnits().get(0));
    BinItem item = ibcf.findItemAt(new SourceCoordinate(2, 8));
    assertTrue("item is class: " + item.getClass(), item instanceof BinClass);
    assertTrue("item is anonymous", ((BinClass) item).isAnonymous());
    assertEquals("name is 1", "1", ((BinClass) item).getName());

    cat.info("SUCCESS");
  }
}
