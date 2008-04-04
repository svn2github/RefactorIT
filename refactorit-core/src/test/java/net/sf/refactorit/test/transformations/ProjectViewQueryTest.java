/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.transformations;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.transformations.view.ProjectViewQuery;
import net.sf.refactorit.transformations.view.triads.NameTriad;
import net.sf.refactorit.transformations.view.triads.OwnerTriad;
import net.sf.refactorit.transformations.view.triads.PackageTriad;
import net.sf.refactorit.transformations.view.triads.SourceHolderTriad;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class ProjectViewQueryTest extends TestCase {

  public ProjectViewQueryTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(ProjectViewQueryTest.class);
    suite.setName("ProjectViewQuery Tests");
    return suite;
  }

  public void test00(){
    Project bingoProject = loadProject("bingo");
    assertNotNull("Could not load bingo project.", bingoProject);

    TransformationManager manager = new TransformationManager(null);
    manager.beginViewUpdateTransaction(null);

    BinTypeRef typeCard = bingoProject.getTypeRefForName("bingo.shared.Card");
    BinPackage packGame = bingoProject.getPackageForName("bingo.game");

    assertNotNull(typeCard);
    assertNotNull(packGame);

    manager.updateView(null, new NameTriad(typeCard.getBinCIType(), "Karte"));
    manager.updateView(null, new NameTriad(packGame, "bingo.spiel"));
    manager.updateView(null, new PackageTriad(typeCard.getCompilationUnit(),
        packGame));

    ProjectViewQuery query = new ProjectViewQuery(manager.getProjectView());
    assertEquals("bingo.spiel.Karte", query.getQualifiedName(typeCard));
  }

  public void test01(){
    Project project = loadProject("fruits");
    assertNotNull("Could not load project.", project);

    BinTypeRef typeFruitShop = project.getTypeRefForName("ee.fruitstore.handle.FruitShop");
    BinTypeRef typePaperBag = project.getTypeRefForName("ee.fruitstore.handle.PaperBag");

    assertNotNull(typeFruitShop);
    assertNotNull(typePaperBag);

    BinPackage packNew = new BinPackage("ee.fruitstore.new", project, false);
    SimpleSourceHolder sourceNew = new SimpleSourceHolder(project);
    sourceNew.setDisplayPath("somenewsource"); // make it mappable
    sourceNew.setPackage(packNew);

    TransformationManager manager = new TransformationManager(null);
    int transactionId = manager.beginViewUpdateTransaction(null);

    manager.updateView(null, new SourceHolderTriad(typeFruitShop.getBinCIType(),
        sourceNew));
    manager.updateView(null, new NameTriad(typeFruitShop.getBinCIType(),
        "FruitHandle"));
    manager.updateView(null, new NameTriad(packNew, "ee.fruitstore.handle2"));

    ProjectViewQuery query = new ProjectViewQuery(manager.getProjectView());
    assertEquals("ee.fruitstore.handle2.FruitHandle", query.getQualifiedName(typeFruitShop));
    assertEquals("ee.fruitstore.handle.PaperBag", query.getQualifiedName(typePaperBag));

    // now kill transaction and see, that everything is like it was before
    manager.killViewUpdateTransaction(transactionId);
    assertEquals("ee.fruitstore.handle.FruitShop", query.getQualifiedName(typeFruitShop));
  }

  public void test02(){
    Project project = loadProject("fruits");
    assertNotNull("Could not load project.", project);

    BinTypeRef typeFruitShelf = project.getTypeRefForName(
        "ee.fruitstore.handle.FruitShop$FruitShelf");
    assertNotNull(typeFruitShelf);

    TransformationManager manager = new TransformationManager(null);
    ProjectViewQuery query = new ProjectViewQuery(manager.getProjectView());

    assertEquals("ee.fruitstore.handle.FruitShop$FruitShelf", query.getQualifiedName(typeFruitShelf));

    SimpleSourceHolder sourceNew = new SimpleSourceHolder(project);
    sourceNew.setDisplayPath("somenewsource"); // make it mappable
    sourceNew.setPackage(typeFruitShelf.getPackage());

    manager.beginViewUpdateTransaction(null);

    manager.updateView(null, new SourceHolderTriad(typeFruitShelf.getBinCIType(),
        sourceNew));
    manager.updateView(null, new OwnerTriad(typeFruitShelf.getBinCIType(), null));

    assertEquals("ee.fruitstore.handle.FruitShelf", query.getQualifiedName(typeFruitShelf));
  }

  public void test03(){
    Project project = loadProject("fruits");
    assertNotNull("Could not load project.", project);

    BinTypeRef typePaperBag = project.getTypeRefForName("ee.fruitstore.handle.PaperBag");
    BinTypeRef typeFruitShop = project.getTypeRefForName("ee.fruitstore.handle.FruitShop");
    assertNotNull(typePaperBag);

    TransformationManager manager = new TransformationManager(null);
    manager.beginViewUpdateTransaction(null);
    manager.updateView(null, new OwnerTriad(typePaperBag.getBinCIType(),
        typeFruitShop));

    ProjectViewQuery query = new ProjectViewQuery(manager.getProjectView());
    assertEquals("ee.fruitstore.handle.FruitShop$PaperBag", query.getQualifiedName(typePaperBag));
  }

  public static Project loadProject(String projectId){
    Project result = null;
    try {
      result = Utils.createTestRbProjectFromXml(projectId);
      result.getProjectLoader().build();
      return result;
    } catch (Exception e){
      return null;
    }
  }
}
