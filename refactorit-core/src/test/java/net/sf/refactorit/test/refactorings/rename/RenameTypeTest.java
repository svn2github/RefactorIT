/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RenameTypeTest extends TestCase {
  public RenameTypeTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(RenameTypeTest.class);
  }

  private Project loadMutableProject(String name) throws Exception {
    return loadMutableProject(getProject(name));
  }

  private Project loadMutableProject(Project p) throws Exception {
    final Project project =
        RwRefactoringTestUtils.createMutableProject(p);
    project.getProjectLoader().build();
    return project;
  }

  private Project getProject(String name) throws Exception {
    return Utils.createTestRbProject("RenameType/" + name);
  }
  
 
  public void testRenameType(String projectName, String type, String newName, 
      boolean renameNonJava) throws Exception {
    Project before = loadMutableProject(projectName + "/in");
    
    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName(type).getBinCIType());
    renameType.setNewName(newName);
    renameType.setRenameInNonJavaFiles(renameNonJava);
    Project after = getProject(projectName + "/out");
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testPathsInNonJava() throws Exception {
    testRenameType("paths_in_non_java","com.test.main.Test","Test1", true);
  }
  
  public void testBug1636() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  public String getName() {\n" +
        "    return b.X.NAME;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;\n" +
        "\n" +
        "public class X {\n" +
        "  public static final String NAME = \"\";\n" +
        "}",
        "X.java", "b"
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  public String getName() {\n" +
        "    return b.X2.NAME;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;\n" +
        "\n" +
        "public class X2 {\n" +
        "  public static final String NAME = \"\";\n" +
        "}",
        "X2.java", "b"
        )
    });

    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName("b.X").getBinCIType());
    renameType.setNewName("X2");

    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }


  
  public void testMultiVarDeclaration() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class A {\n" +
        "  A a, b;\n" +
        "}",
        "A.java", ""
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class B {\n" +
        "  B a, b;\n" +
        "}",
        "B.java", ""
        )
    });

    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName("A").getBinCIType());
    renameType.setNewName("B");

    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }

  public void testBug1980() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "import b.*;\n" +
        "import c.*;\n" +
        "public class X {\n" +
        "}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;\n" +
        "\n" +
        "public class Test {\n" +
        "}",
        "Test.java", "b"
        ),
        new Utils.TempCompilationUnit(
        "package c;\n" +
        "\n" +
        "public class Y {\n" +
        "}",
        "Y.java", "c"
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "import b.*;\n" +
        "import c.*;\n" +
        "public class X {\n" +
        "}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;\n" +
        "\n" +
        "public class Test {\n" +
        "}",
        "Test.java", "b"
        ),
        new Utils.TempCompilationUnit(
        "package c;\n" +
        "\n" +
        "public class Test {\n" +
        "}",
        "Test.java", "c"
        )
    });

    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName("c.Y").getBinCIType());
    renameType.setNewName("Test");

    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }

  /** Tests empty javadoc tag name */
  public void testBug2035() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        "/**\n" +
        " * @\n" +
        " */\n" +
        "public class X {}", "X.java", null);

    Project after = Utils.createTestRbProjectFromString(
        "/**\n" +
        " * @\n" +
        " */\n" +
        "public class X2 {}", "X2.java", null);

    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName("X").getBinCIType());
    renameType.setNewName("X2");

    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }

  /** Important for NB CVS support -- to make sure that the files don't disappear from the repository */
  public void testTargetSourceExists() throws Exception {
    Project p = Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a; public class X {}", "X.java", "a"),
        new Utils.TempCompilationUnit("some-random-file", "X2.java", "a")});
    
    RenameType renameType = new RenameType(new NullContext(p),
        p.getTypeRefForName("a.X").getBinCIType());
    renameType.setNewName("X2");

    RefactoringStatus status =
      renameType.apply();

    assertTrue(status.isInfoOrWarning());
    //FIXME: add to RenameType checking of  .perfomTransformation() status.
    //Project after = RwRefactoringTestUtils.createMutableProject(p, true);
    //RwRefactoringTestUtils.assertSameSources("", after, p);
  }
  
  public void testRenameTypeToLong() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
            "public class MyClass {}", 
            "MyClass.java", null);

    Project after = Utils.createTestRbProjectFromString(
            "public class Long {}", 
            "Long.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
            .getTypeRefForName("MyClass").getBinCIType());
    renameType.setNewName("Long");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename1() throws Exception {
      Project before = Utils.createTestRbProjectFromString(
        "public class Person {String personName;}", "Person.java", null);

    Project after = Utils.createTestRbProjectFromString(
        "public class Human {String humanName;}", "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before),
        before.getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename2() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} class XXX { private Person person = new Person(); }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} class XXX { private Human human = new Human(); }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename3() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} class XXX { private Person person = new Person();" +
            "String personName = person.name; " +
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} class XXX { private Human human = new Human();" +
            "String humanName = human.name; " +
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename4() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} class XXX { private Person person = new Person();" +
            "String personName; { personName = person.name; }" +
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} class XXX { private Human human = new Human();" +
            "String humanName; { humanName = human.name; }" +
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename5() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX { String personName = getPersonName(); " +
            "public String getPersonName() { return new Person().name; } }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX { String humanName = getHumanName(); " +
            "public String getHumanName() { return new Human().name; } }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename6() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX { Person person = getPerson(); " +
            "public Person getPerson() { return new Person(); } }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX { Human human = getHuman(); " +
            "public Human getHuman() { return new Human(); } }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename7() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX { String personName = getPersonName(); " +
            "public String getPersonName() { return getPerson().name; }" + 
            "public Person getPerson() { return new Person(); }" +
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX { String humanName = getHumanName(); " +
            "public String getHumanName() { return getHuman().name; }" + 
            "public Human getHuman() { return new Human(); }" +
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename8() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX {" +
            "public void personPersonCoolPersonPERSON() { Person ps; }" + 
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX {" +
            "public void humanHumanCoolHumanHUMAN() { Human ps; }" + 
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename9() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX {" +
            "public void getPersonName(Person person) {}" + 
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX {" +
            "public void getHumanName(Human human) {}" + 
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename10() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class XXX {" +
            "public Person getPerson() {return null;}" + 
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class XXX {" +
            "public Human getHuman() {return null;}" + 
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename11() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;} " +
            "class PersonTest {" +
            "public Person getPerson(return null;) {}" + 
            " }",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;} " +
            "class HumanTest {" +
            "public Human getHuman(return null;) {}" + 
            " }",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename12() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(
            "public class Person {String name;}" +
            "class XXX {Person ps = new Person();{ps.name = Constant.personName;}}" +
            "class Constant {static String personName = \"XXX\";}",
            "Person.java", null);

    Project after = Utils
        .createTestRbProjectFromString(
            "public class Human {String name;}" +
            "class XXX {Human ps = new Human();{ps.name = Constant.humanName;}}" +
            "class Constant {static String humanName = \"XXX\";}",
            "Human.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
        .getTypeRefForName("Person").getBinCIType());
    renameType.setNewName("Human");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
  public void testSemanticRename13() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
            "public class Long {" + 
            "public java.lang.Long var;" + 
            "}", 
            "Long.java", null);

    Project after = Utils.createTestRbProjectFromString(
            "public class MyClass {" + 
            "public java.lang.Long var;" + 
            "}", 
            "MyClass.java", null);

    RenameType renameType = new RenameType(new NullContext(before), before
            .getTypeRefForName("Long").getBinCIType());
    renameType.setNewName("MyClass");
    renameType.setSemanticRename(true);
    RwRefactoringTestUtils.assertRefactoring(renameType, before, after);
  }
  
}
