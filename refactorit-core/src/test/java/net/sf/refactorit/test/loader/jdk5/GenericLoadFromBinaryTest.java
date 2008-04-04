/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.test.ProjectMetadata;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * The files, which were compiled into .jar file, which is used for this test
 * can be found in ~/projects/GenericsLoadFromBinary/libsources.tar.gz
 *
 * @author Arseni Grigorjev
 */
public class GenericLoadFromBinaryTest extends TestCase {
  private static final String PROJECT_ID = "generics_binary";

  public GenericLoadFromBinaryTest(String name) {
    super(name);
  }

  int oldJvmMode = FastJavaLexer.JVM_14;
  private Project project;

  public static Test suite() {
    return new TestSuite(GenericLoadFromBinaryTest.class);
  }

  protected void setUp() throws Exception {
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    // INIT PROJECT FROM SOURCE AND PATH
    final ProjectMetadata metadata = Utils.getTestProjects().getProject(
        PROJECT_ID);
    if (metadata == null) {
      fail("Wasn`t able to find project with such id '" + PROJECT_ID + "'");
    }

    project = Utils.createTestRbProject(metadata);
    if (project == null) {
      fail("Failed to create project: " + PROJECT_ID);
    }

    try {
      project.getProjectLoader().build();
    } catch (Exception e) {
      fail("Failed project.load(): " + e);
    }

    assertFalse("Project was not built properly - has errors: "
        + CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()),
        (project.getProjectLoader().getErrorCollector()).hasErrors());
  }

  protected void tearDown() throws Exception {
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }
    
  public void testBugREF1948() throws Exception {
  //  Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    String file = "public class TestClass {\n" +
      "{\n" + 
        "java.util.HashMap<String, Class[]> map = new java.util.HashMap<String, Class[]>();\n" +
        "Class<?>[] validChildren = map.get(null);" +
      "}" +
    "}";
    final Project project = Utils.createTestRbProjectFromString(file);
    project.getProjectLoader().build();

    class TestRefVisitor extends BinTypeRefVisitor {
      public TestRefVisitor() {
        setCheckTypeSelfDeclaration(false);
        setIncludeNewExpressions(true);
      }
    }
    
    SinglePointVisitor visitor = new SinglePointVisitor() {
      private TestRefVisitor typeRefVisitor = new TestRefVisitor();
      
      public void onEnter(Object o) {
        if(o instanceof BinTypeRefManager) {
          ((BinTypeRefManager)o).accept(typeRefVisitor); // should not throw the AIOOBE!
        }
      }
  
      public void onLeave(Object o) {
        // nothing to do
      }
      
    };

    visitor.visit(project); 
  }
  

  public void testOneSimpleParameter() {
    String qName = "generics.HasOneSimpleParameter";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    assertNotNull(type.getTypeParameters());
    assertTrue("Number of type parameters differs for '" + qName + "'",
        type.getTypeParameters().length == 1);

    BinTypeRef parameterRef = type.getTypeParameters()[0];
    assertTrue("Class bound 'java.lang.Object' type expected.", parameterRef
        .getSuperclass().getQualifiedName().equals("java.lang.Object"));
    assertTrue("No interface bounds expected",
        parameterRef.getInterfaces() == null
        || parameterRef.getInterfaces().length == 0);
  }

  public void testTwoParameters() {
    String qName = "generics.HasTwoParameters";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    assertNotNull(type.getTypeParameters());
    assertTrue("Number of type parameters differs for '" + qName + "'",
        type.getTypeParameters().length == 2);

    BinTypeRef parameterRef = type.getTypeParameters()[0];
    assertTrue("T1 class bound 'java.lang.Object' expected", parameterRef
        .getSuperclass().getQualifiedName().equals("java.lang.Object"));
    assertTrue("T1-ref should have no interface bounds",
        parameterRef.getInterfaces() == null
        || parameterRef.getInterfaces().length == 0);

    parameterRef = type.getTypeParameters()[1];
    assertTrue("T2-ref should have no superclass - " +
        "signature defines only interfaces",
        parameterRef.getSuperclass() == null);
    assertTrue("T2-ref should have 2 interfaces",
        parameterRef.getInterfaces() != null
        && parameterRef.getInterfaces().length == 2);

    assertTrue("T2-ref`s 1st interface should be List", parameterRef
        .getInterfaces()[0] == project.getTypeRefForName("java.util.List"));
    assertTrue("T2-ref`s 2nd interface should be List", parameterRef
        .getInterfaces()[1] == project.getTypeRefForName("java.util.Set"));
  }

  public void testTwoParametersRecursion() {
    String qName = "generics.HasTwoParametersRecursion";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    assertNotNull(type.getTypeParameters());
    assertTrue("Number of type parameters differs for '" + qName + "'",
        type.getTypeParameters().length == 2);

    // for test will take first T2 and second T2 arguments and compare them :)
    BinTypeRef firstT2ref = type.getTypeParameters()[0].getSuperclass()
        .getTypeArguments()[1];
    BinTypeRef secondT2ref = type.getTypeParameters()[1].getSuperclass()
        .getTypeArguments()[1];
    assertNotNull(firstT2ref);
    assertNotNull(secondT2ref);
    assertEquals("Argument refs are not equal", firstT2ref, secondT2ref);
  }

  public void testMultilevelArguments() {
    String qName = "generics.HasMultilevelArguments";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);

    BinTypeRef[] typeParameters = ref.getBinCIType().getTypeParameters();
    assertNotNull(typeParameters);
    assertTrue("Number of type parameters differs for '" + qName + "'",
        typeParameters.length == 1);

    // for test will try to get Integer and String refs from Map<..,..>
    // String
    BinTypeRef[] interfaces = typeParameters[0].getInterfaces();
    assertNotNull(interfaces);
    BinTypeRef[] firstLevelArguments = interfaces[0].getTypeArguments();
    assertNotNull(firstLevelArguments);
    BinTypeRef[] secondLevelArguments = firstLevelArguments[0].getTypeArguments();
    assertNotNull(secondLevelArguments);
    assertNotNull(secondLevelArguments[0]);
    assertEquals("First type argument for Map interface should be " +
        "java.lang.String!",
        secondLevelArguments[0], project.getTypeRefForName("java.lang.String"));

    // Integer
    assertNotNull(secondLevelArguments[1]);
    assertEquals("Second type argument for Map interface should be " +
        "java.lang.Integer!",
        secondLevelArguments[1], project.getTypeRefForName("java.lang.Integer"));
  }

  public void testExtendsParametrizedType() {
    String qName = "generics.ExtendsParametrizedType";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    assertNotNull("Superclass reference should not be null",
        ref.getSuperclass());
    assertTrue("Superclass should have non-zero type arguments.",
        ref.getSuperclass().getTypeArguments() != null
        && ref.getSuperclass().getTypeArguments().length == 1);
    assertTrue("Type-argument ref should be java.util.Map ref",
        ref.getSuperclass().getTypeArguments()[0].getQualifiedName()
        .equals("java.util.Map"));

    // check, if Map arguments are references to W1 and W2 parameters
    assertNotNull(ref.getBinCIType().getTypeParameters());
    assertTrue("Type should have 2 type-parameters",
        ref.getBinCIType().getTypeParameters().length == 2);
    BinTypeRef parameter1 = ref.getBinCIType().getTypeParameters()[0];
    BinTypeRef parameter2 = ref.getBinCIType().getTypeParameters()[1];
    assertNotNull(parameter1);
    assertNotNull(parameter2);
    assertNotNull(ref.getSuperclass().getTypeArguments()[0].getTypeArguments());
    assertTrue("java.util.Map should have 2 type-arguments",
        ref.getSuperclass().getTypeArguments()[0].getTypeArguments().length
        == 2);
    assertNotNull(ref.getSuperclass().getTypeArguments()[0].getTypeArguments()[
        0]);
    assertNotNull(ref.getSuperclass().getTypeArguments()[0].getTypeArguments()[
        1]);

    assertEquals("First type-argument for java.util.Map should be W1-ref",
        ref.getSuperclass().getTypeArguments()[0].getTypeArguments()[0],
        parameter1);
    assertEquals("Second type-argument for java.util.Map should be W2-ref",
        ref.getSuperclass().getTypeArguments()[0].getTypeArguments()[1],
        parameter2);
  }

  public void testInnerClassParameters() {
    String qName = "generics.InnerClassParameters";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    assertNotNull(type.getTypeParameters());
    assertTrue("Number of type parameters differs for '" + qName + "'",
        type.getTypeParameters().length == 1);

    assertNotNull(qName + " expected to have inners!", type.getDeclaredTypes());
    assertEquals(qName + " expected to have 3 inner classes",
        type.getDeclaredTypes().length, 3);

    // get InnerClassParameters.InnerClassA, and check his type parameter
    BinTypeRef innerType = type.getDeclaredType("InnerClassA");
    assertNotNull("InnerClassA reference is null", innerType);
    assertEquals("InnerClassA expected to have 1 type parameter",
        innerType.getBinCIType().getTypeParameters().length, 1);
    assertEquals("Wrong upperbound for InnerClassA type parameter.",
        innerType.getBinCIType().getTypeParameters()[0]
        .getSuperclass(), type.getTypeParameters()[0]);

    // get InnerClassParameters.InnerClassB, and check his type parameter
    innerType = type.getDeclaredType("InnerClassB");
    assertNotNull("InnerClassB reference is null", innerType);
    assertEquals("InnerClassB expected to have 1 type parameter",
        innerType.getBinCIType().getTypeParameters().length, 1);
    assertEquals("Wrong upperbound for InnerClassB type parameter.",
        innerType.getBinCIType().getTypeParameters()[0]
        .getInterfaces()[0], project.getTypeRefForName("java.util.Set"));

    // check static inner class - constructor resolving
    innerType = type.getDeclaredType("InnerStaticClass");
    assertNotNull("InnerStaticClass reference is null", innerType);
    BinTypeRef[] params = new BinTypeRef[] {innerType,
        BinPrimitiveType.INT_REF};
    assertNotNull(((BinClass) innerType.getBinCIType()).getConstructor(params));
    params = new BinTypeRef[] {BinPrimitiveType.INT_REF};
    assertNotNull(((BinClass) innerType.getBinCIType()).getConstructor(params));
  }

  public void testParametrizedMembers() {
    String qName = "generics.ParametrizedMembers";

    BinTypeRef ref = project.getTypeRefForName(qName);
    assertNotNull(ref);
    BinCIType type = ref.getBinCIType();

    assertNotNull(type.getTypeParameters());
    assertTrue("Number of type parameters differs for '" + qName + "'",
        type.getTypeParameters().length == 2);
    assertEquals("1st type parameter expected to be 'java.util.List'",
        type.getTypeParameters()[0].getInterfaces()[0],
        project.getTypeRefForName("java.util.List"));
    assertEquals("2nd type parameter expected to be 'java.util.Set'",
        type.getTypeParameters()[1].getInterfaces()[0],
        project.getTypeRefForName("java.util.Set"));

    // check fields
    BinField[] fields = type.getDeclaredFields();
    assertNotNull(fields);
    assertEquals(fields.length, 2);
    assertNotNull(fields[0]);
    assertEquals(fields[0].getTypeRef(), type.getTypeParameters()[0]);
    assertNotNull(fields[1]);
    assertEquals(fields[1].getTypeRef(), type.getTypeParameters()[1]);

    // check methods
    BinMethod[] methods = type.getDeclaredMethods();
    assertEquals(methods[0].getName(), "returnsType1");
    assertEquals(methods[1].getName(), "getsMtype");

    assertEquals(methods[0].getParameters()[0].getTypeRef(), type
        .getTypeParameters()[1]);
    assertEquals(methods[0].getReturnType(), type.getTypeParameters()[0]);

    assertNotNull(methods[1].getTypeParameters());
    assertEquals(methods[1].getTypeParameters().length, 1);
    assertEquals(methods[1].getParameters()[0].getTypeRef(),
        methods[1].getTypeParameters()[0]);
    assertEquals(methods[1].getReturnType(), type.getTypeParameters()[0]);

    // check constructor
    BinConstructor constr = ((BinClass) type).getDeclaredConstructors()[0];
    assertNotNull(constr);
    assertEquals(constr.getParameters()[0].getTypeRef(),
        type.getTypeParameters()[0]);
    assertEquals(constr.getParameters()[1].getTypeRef(),
        type.getTypeParameters()[1]);
  }
}
