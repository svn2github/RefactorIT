/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TargetIndexer;
import net.sf.refactorit.query.usage.filters.BinPackageSearchFilter;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author Anton Safonov
 */
public class DependenciesIndexer extends TargetIndexer {

  private final BinItem targetScope;

  private final TypeRefVisitor typeRefVisitor = new TypeRefVisitor();

  public final class TypeRefVisitor extends BinTypeRefVisitor {
    private Object location;
    private BinMember what;

    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(false);
      setIncludeNewExpressions(true);
    }

    public final void init(BinMember what, Object location) {
      this.what = what;
      this.location = location;
    }

    public final void visit(BinTypeRef data) {
      try {
        BinTypeRef type = data.getTypeRef();

        if (type == null) { // strange imports occur sometimes
          return;
        }

        if (type.isPrimitiveType()) {
          return;
        }

        BinMember whatCalled = what;
        if (whatCalled == null) {
          whatCalled = type.getBinType();
        }

        if (whatCalled instanceof BinType
            && ((BinType) whatCalled).isPrimitiveType()) {
          return;
        }

        if (!isItemInsideTarget(whatCalled)) {
          Object where = location;
          if (where == null) {
            where = getSupervisor().getCurrentLocation();
          }

          if(data.getNode() != null) {
            getSupervisor().addInvocation(
              whatCalled, where, data.getNode());
          }
        }
      } finally {
        super.visit(data);
      }
    }
  }


  public DependenciesIndexer(final ManagingIndexer supervisor,
      final BinItem target) {
    super(supervisor, target);

    // TODO: scope filter aplies here by raising of scope from BinCIType
    // or BinMethod upto BinPackage as user selected
    this.targetScope = target;
  }

  public static List getTypesUsedBy(BinSourceConstruct o) {
    ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, o);
    List result = new ArrayList();

    for (Iterator i = supervisor.getInvocationsFor(o).iterator(); i.hasNext(); ) {
      InvocationData data = (InvocationData) i.next();
      result.add(data.getWhat() instanceof BinConstructor
          ? data.getWhat().getParent() : data.getWhat());
    }

    return result;
  }

  public final void visit(final CompilationUnit source) {
    typeRefVisitor.init(null, source);
    source.accept(typeRefVisitor);
  }

  /** this covers extends and implements statements */
  public final void visit(final BinCIType type) {
    super.visit(type);

    typeRefVisitor.init(null, type.getTypeRef());
    type.accept(typeRefVisitor);

    // checking for Object implicit extend
    final BinTypeRef objectRef = type.getProject().getObjectRef();
    if (type.isClass() && type.getTypeRef().getSuperclass().equals(objectRef)) {
      boolean foundExplicitObject = false;
      List typeRefs = type.getSpecificSuperTypeRefs();
      if (typeRefs != null) {
        for (int i = 0, max = typeRefs.size(); i < max; i++) {
          if (((BinTypeRef) typeRefs.get(i)).getTypeRef().equals(objectRef)) {
            foundExplicitObject = true;
            break;
          }
        }
      }

      if (!foundExplicitObject && type.getNameAstOrNull() != null) {
        getSupervisor().addInvocation(objectRef.getBinCIType(),
            type.getTypeRef(), type.getNameAstOrNull());
      }
    }
  }

  /** this covers cast statements */
  public final void visit(final BinCastExpression x) {
    typeRefVisitor.init(null, null);
    x.accept(typeRefVisitor);
  }

  /** this covers new statements of types having no declared constructors */
  public final void visit(final BinNewExpression x) {
    BinConstructor constructor = null;
    if (!x.getTypeRef().isArray()
        && !x.getTypeRef().getBinType().isAnonymous() // FIXME: MoveType expects this, but I don't understand anything anymore [Maddy]
    ) {
      constructor = x.getConstructor();
    }
    typeRefVisitor.init(constructor, getSupervisor().getCurrentLocation());
    x.accept(typeRefVisitor);
  }

  public final void visit(final BinConstructor constructor) {
    typeRefVisitor.init(null, constructor);
    constructor.accept(typeRefVisitor);
  }

  public final void visit(final BinConstructorInvocationExpression x) {
    final BinConstructor constructor = x.getConstructor();

    if (!isItemInsideTarget(constructor)) {
      getSupervisor().addInvocation(constructor,
          getSupervisor().getCurrentLocation(),
          x.getRootAst());
    }
  }

  /** field declarations:
   *  <pre>
   *  A a = null;
   *  B b;
   *  </pre>
   */
  public final void visit(final BinField field) {
    typeRefVisitor.init(null, null);
    field.accept(typeRefVisitor);
  }

  public final void visit(final BinLocalVariable variable) {
    typeRefVisitor.init(null, null);
    variable.accept(typeRefVisitor);
  }

  /**
   * <CLASS>.staticField
   */
  public void visit(final BinFieldInvocationExpression x) {
    if (!isItemInsideTarget(x.getField())) {
      getSupervisor().addInvocation(x.getField(),
          getSupervisor().getCurrentLocation(),
          x.getNameAst(), x);
    }
  }

  /**
   * This is correct one for static fields and methods invocations
   */
  public final void visit(final BinCITypeExpression x) {
    typeRefVisitor.init(null, null);
    x.accept(typeRefVisitor);
  }

  /** type usage in method declaration */
  public final void visit(final BinMethod method) {
    typeRefVisitor.init(null, method);
    method.accept(typeRefVisitor);
  }

  /** type usage in method throws clause */
  public final void visit(final BinMethod.Throws exception) {
    typeRefVisitor.init(exception.getException().getBinCIType(),
        getSupervisor().getCurrentLocation());
    exception.accept(typeRefVisitor);
  }

  /**
   */
  public void visit(final BinMethodInvocationExpression x) {
    if (!isItemInsideTarget(x.getMethod())) {
      getSupervisor().addInvocation(x.getMethod(),
          getSupervisor().getCurrentLocation(),
          x.getNameAst(), x);
    }

    // type usage in reflection
    /*    if ("java.lang.Class".equals(invokedOn.getQualifiedName())) {
     if ("forName".equals(method.getName())) {
     BinExpression[] exprs = x.getExpressionList().getExpressions();
     for (int i = 0; i < exprs.length; i++) {
     System.err.println("forName: " + exprs[i].getReturnType().getQualifiedName());
     }
     }
     }*/
  }

  public final void visit(final BinStringConcatenationExpression expression) {
    final BinExpression leftExpression = expression.getLeftExpression();
    final BinExpression rightExpression = expression.getRightExpression();
    final BinTypeRef leftType = leftExpression.getReturnType();
    final BinTypeRef rightType = rightExpression.getReturnType();

    if (leftType != null && leftType.isReferenceType()) {
      // toString() implicitly invoked on left type
      final BinMethod[] methods = leftType.getBinCIType()
          .getAccessibleMethods("toString",
          getSupervisor().getCurrentType().getBinCIType());
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getParameters().length == 0
            && !isItemInsideTarget(methods[i])) {
          getSupervisor().addInvocation(methods[i],
              getSupervisor().getCurrentLocation(),
              leftExpression.getClickableNode(), null);
          break;
        }
      }
    }

    if (rightType != null && rightType.isReferenceType()) {
      // toString() implicitly invoked on right type
      final BinMethod[] methods = rightType.getBinCIType()
          .getAccessibleMethods("toString",
          getSupervisor().getCurrentType().getBinCIType());
      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getParameters().length == 0
            && !isItemInsideTarget(methods[i])) {
          getSupervisor().addInvocation(methods[i],
              getSupervisor().getCurrentLocation(),
              rightExpression.getClickableNode(), null);
          break;
        }
      }
    }
  }

  protected boolean isItemInsideTarget(final BinItem item) {
//System.err.println("Scope: " + targetScope + ", item: " + item);
    if (item == null) {
      if (Assert.enabled) {
        Assert.must(false, "Item == null");
      }
      return true;
    }

    BinItem curItem;
    if (item instanceof BinType) {
      BinTypeRef typeRef = ((BinType) item).getTypeRef().getNonArrayType();
      if (typeRef.isPrimitiveType()) {
        return true; // small hack - it is really not an outside call
      }
      curItem = typeRef.getBinType();
    } else {
      curItem = item;
    }

    if (targetScope == null) { // Project, special case
      if (curItem instanceof BinMember
          && ((BinMember) curItem).getCompilationUnit() != null) {
        return true;
      } else if (curItem instanceof BinPackage) {
        return ((BinPackage) curItem).hasTypesWithSources();
      }
      return false;
    }

    // not really pretty, but e.g. BinVariable is not a Scope (BTW, is it a bug?)
    if (targetScope instanceof LocationAware
        && curItem instanceof LocationAware) {
      return ((LocationAware) targetScope).contains((LocationAware) curItem);
    }

    return targetScope.getScope().contains(curItem.getScope());
  }

  /**
   * Gets list of all types outside of the package that types of the package
   * depend on.
   * I.e. types the package depends on.
   *
   * @param pkg package.
   *
   * @return types ({@link BinCIType} instances).
   *         Never returns <code>null</code>.
   */
  public static Set getDependencies(BinPackage pkg) {
    // Gather all dependencies of this package
    // Next create a list of types this package depends on excluding:
    //   * own types
    //   * java.*
    //   * javax.*
    final ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, pkg);

    final List sources = pkg.getCompilationUnitList();
    for (int i = 0, max = sources.size(); i < max; i++) {
      final CompilationUnit source = (CompilationUnit) sources.get(i);
      supervisor.visit(source);
    }

    // All dependencies
    final List invocations = supervisor.getInvocations();

    // Types depended upon
    final HashSet dependencies = new HashSet();
    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData invocation = (InvocationData) invocations.get(i);
      BinTypeRef type = invocation.getWhatType();

      if (type == null) {
        continue; // skip unknown
      }

      type = type.getNonArrayType();

      if (type.isPrimitiveType()) {
        continue;
      }

      if (pkg.isIdentical(type.getPackage())) {
        continue; // only external types should be reported
      }

      /*
             // Don't add java.* and javax.*
       final String typePackageName = type.getPackage().getQualifiedName();
             if ((typePackageName.startsWith("java."))
           || (typePackageName.startsWith("javax."))) {
        continue; // Skip this type
             }
       */

      // Add type and its owners if any
      BinTypeRef currentTypeRef = type;
      do {
        final BinType currentType = currentTypeRef.getBinType();
        dependencies.add(currentType);
        currentTypeRef = currentType.getOwner();
      } while (currentTypeRef != null);
    }

    return dependencies;
  }

  /**
   * Gets list of all types inside the package that depend on outside types.
   * I.e. types of the package which depend on something external.
   *
   * @param pkg package.
   *
   * @return types ({@link BinCIType} instances).
   *         Never returns <code>null</code>.
   */
  public static Set getDependants(BinPackage pkg) {
    final ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, pkg);

    final List sources = pkg.getCompilationUnitList();
    for (int i = 0, max = sources.size(); i < max; i++) {
      final CompilationUnit source = (CompilationUnit) sources.get(i);
      supervisor.visit(source);
    }

    // All dependencies
    final List invocations = supervisor.getInvocations();

    // Types depending on something
    final HashSet dependencies = new HashSet();
    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData invocation = (InvocationData) invocations.get(i);
      BinTypeRef type = invocation.getWhatType();

      if (type == null) {
        continue; // skip unknown
      }

      type = type.getNonArrayType();

      if (type.isPrimitiveType()
          || "java.lang.Object".equals(type.getQualifiedName())) {// FIXME: compare refs?
        continue;
      }

      if (pkg.isIdentical(type.getPackage())) {
        continue; // only external types should be reported
      }

      BinTypeRef where = invocation.getWhereType();
//System.err.println("what: " + type + " - where: " + where);
      if (where == null) {
        continue; // skip unknown
      }

      // Add type and its owners if any
      BinTypeRef currentTypeRef = where;
      do {
        final BinType currentType = currentTypeRef.getBinType();
        dependencies.add(currentType);
        currentTypeRef = currentType.getOwner();
      } while (currentTypeRef != null);
    }

//System.err.println("Deps for: " + pkg + " - " + dependencies);
    return dependencies;
  }


  /**
   * The types outside this package that depend upon types within this package.
   * I.e. types which use given package.
   *
   * @param pkg package.
   *
   * @return types ({@link BinCIType} instances). Never returns <code>null</code>.
   */
  public static Set getReferencingTypes(BinPackage pkg) {
    // Gather all dependants of this package
    final List invocations = Finder.getInvocations(pkg,
        new BinPackageSearchFilter(false, false, false));

    // Types depended upon
    final Set dependants = new HashSet();
    for (final Iterator i = invocations.iterator(); i.hasNext(); ) {
      final InvocationData invocation = (InvocationData) i.next();
      BinTypeRef type = invocation.getWhereType();

      if (type == null) {
        continue; // Skip this
      }

      type = type.getNonArrayType();

      if (type.isPrimitiveType()) {
        continue;
      }

      if (pkg.isIdentical(type.getPackage())) {
        // Don't add dependant from this package
        continue; // Skip
      }

      // Add type and its owners if any
      BinTypeRef currentTypeRef = type;
      do {
        final BinType currentType = currentTypeRef.getBinType();
        dependants.add(currentType);
        currentTypeRef = currentType.getOwner();
      } while (currentTypeRef != null);
    }

    return dependants;
  }

  public static Map createTypesDepsMap(Project project) {
    HashMap typesDepsMap = new HashMap();

    List types = project.getDefinedTypes();

    for (int i = 0, max = types.size(); i < max; i++) {
      final BinTypeRef type = (BinTypeRef) types.get(i);

      BinType binType = type.getBinType();
      if (type.isArray() || type.isPrimitiveType()
          || binType.isLocal() || binType.isInnerType()) {
        continue;
      }

      final ManagingIndexer supervisor = new ManagingIndexer();
      new DependenciesIndexer(supervisor, binType);

      binType.accept(supervisor);

      List invocations = supervisor.getInvocations();

      final Set dependants = new HashSet();
      for (int n = 0, maxN = invocations.size(); n < maxN; n++) {
        final InvocationData invocation = (InvocationData) invocations.get(n);
        BinTypeRef what = invocation.getWhatType();

        if (what == null) {
          continue; // Skip this
        }

        what = what.getNonArrayType();

        if (what.isPrimitiveType() || !what.getBinType().isFromCompilationUnit()) {
          continue;
        }

        dependants.add(what);
      }

      typesDepsMap.put(type, dependants);
    }

    return typesDepsMap;
  }

  /**
   * Creates a map of all packages in the current project with their
   * corresponding dependencies.
   *
   * @param project Project.
   *
   * @return map package - packages_it_depends_on
   */
  public static HashMap createPackageDepsMap(Project project) {

    HashSet sourcePackages = new HashSet();
    List sources = project.getCompilationUnits();

    // Create package list
    for (int i = 0, max = sources.size(); i < max; i++) {
      CompilationUnit source = (CompilationUnit) sources.get(i);
      sourcePackages.add(source.getPackage());
    }

    HashMap packageMap = new HashMap();
    Iterator sourcePackagesIterator = sourcePackages.iterator();

    // Build packages' dependencies map
    while (sourcePackagesIterator.hasNext()) {
      BinPackage aPackage = (BinPackage) sourcePackagesIterator.next();
      Set classDeps = getDependencies(aPackage);

      if (classDeps.size() != 0) {
        Iterator classDepsIterator = classDeps.iterator();

        // Determing package dependencies from classes
        HashSet packageDeps = new HashSet(10, 0.75f);
        while (classDepsIterator.hasNext()) {
          BinType classDependency = (BinType) classDepsIterator.next();
          BinPackage pkg = classDependency.getPackage();

          // Only add user's own packages to dependency map
          if (sourcePackages.contains(pkg)) {
            packageDeps.add(pkg);
          }
        }

        // Package's name and its dependencies
        packageMap.put(aPackage, packageDeps);
      }
    }

    return packageMap;
  }


  /** Test driver for {@link DependenciesIndexer}. */
  public static final class TestDriver extends TestCase {

    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    /** Test project. */
    private Project project;

    public TestDriver(final String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("DependenciesIndexer tests");
      return suite;
    }

    protected final void setUp() throws Exception {
      project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("DependenciesIndexer"));
      project.getProjectLoader().build();
    }

    protected final void tearDown() {
      project = null;
    }

    /**
     * Tests invocations in "new" expressions.
     */
    public final void testNew() {
      cat.info("Tests invocations in \"new\" expressions");

      final BinClass test2
          = (BinClass) project.getTypeRefForName("Test2").getBinType();

      final ManagingIndexer supervisor = new ManagingIndexer();
      new DependenciesIndexer(supervisor, test2);
      supervisor.visit(test2);

      final List invocations = supervisor.getInvocations();

      assertEquals("Number of invocations", 3, invocations.size());

      final List usages = new ArrayList(invocations.size());
      for (int i = 0, max = invocations.size(); i < max; i++) {
        usages.add(
            new Integer(((InvocationData) invocations.get(i)).getLineNumber()));
      }

      assertTrue("Dependency of Test2 on Object",
          usages.remove(new Integer(5)));
      assertTrue("Dependency of Test2 on Object()",
          usages.remove(new Integer(5)));
      assertTrue("Dependency of Test2 on Test1",
          usages.remove(new Integer(7)));

      if (usages.size() > 0) {
        String extra = "";
        for (int i = 0, max = usages.size(); i < max; i++) {
          if (i > 0) {extra += ", ";
          }
          extra += usages.get(i);
        }
        assertTrue("Extra dependencies of Test2 found in lines: " + extra,
            false);
      }

//      System.err.println("Test2: " + invocations);

      cat.info("SUCCESS");
    }

    /**
     * Tests invocations of constructors (this and super).
     */
    public final void testConstructorInvocations() {
      cat.info("Testing this/super invocations");

      final BinClass test3
          = (BinClass) project.getTypeRefForName("Test3").getBinType();

      final ManagingIndexer supervisor = new ManagingIndexer();
      new DependenciesIndexer(supervisor, test3);
      supervisor.visit(test3);

      final List invocations = supervisor.getInvocations();

      assertEquals("Number of invocations", 3, invocations.size());

      final List usages = new ArrayList(invocations.size());
      for (int i = 0, max = invocations.size(); i < max; i++) {
        usages.add(
            new Integer(((InvocationData) invocations.get(i)).getLineNumber()));
      }

      assertTrue("Dependency of Test3 on Test1",
          usages.remove(new Integer(11)));
      assertTrue("Dependency of Test3 on Test1()",
          usages.remove(new Integer(14)));
      assertTrue("Dependency of Test3 on Object",
          usages.remove(new Integer(17)));

      if (usages.size() > 0) {
        String extra = "";
        for (int i = 0, max = usages.size(); i < max; i++) {
          if (i > 0) {extra += ", ";
          }
          extra += usages.get(i);
        }
        assertTrue("Extra dependencies of Test3 found in lines: " + extra,
            false);
      }

//      System.err.println("Test3: " + invocations);

      cat.info("SUCCESS");
    }

    /**
     * Tests invocations of toString in String concatenations.
     */
    public final void testStringConcatenation() {
      cat.info("Testing invocations of toString in String concatenations");

      final BinClass test4
          = (BinClass) project.getTypeRefForName("Test4").getBinType();

      final ManagingIndexer supervisor = new ManagingIndexer();
      new DependenciesIndexer(supervisor, test4);
      supervisor.visit(test4);

      final List invocations = supervisor.getInvocations();

      assertEquals("Number of invocations", 7, invocations.size());

      final List usages = new ArrayList(invocations.size());
      for (int i = 0, max = invocations.size(); i < max; i++) {
        usages.add(
            new Integer(((InvocationData) invocations.get(i)).getLineNumber()));
      }

      assertTrue("Dependency of Test4 on Test3",
          usages.remove(new Integer(23)));
      assertTrue("Dependency of Test4 on Test3()",
          usages.remove(new Integer(23)));
      assertTrue("Dependency of Test4 on System",
          usages.remove(new Integer(25)));
      assertTrue("Dependency of Test4 on out",
          usages.remove(new Integer(26)));
      assertTrue("Dependency of Test4 on println",
          usages.remove(new Integer(27)));
      assertTrue("Dependency of Test4 on String",
          usages.remove(new Integer(28)));
      assertTrue("Dependency of Test4 on Object",
          usages.remove(new Integer(29)));

      if (usages.size() > 0) {
        String extra = "";
        for (int i = 0, max = usages.size(); i < max; i++) {
          if (i > 0) {extra += ", ";
          }
          extra += usages.get(i);
        }
        assertTrue("Extra dependencies of Test3 found in lines: " + extra,
            false);
      }

//      System.err.println("Test4: " + invocations);

      cat.info("SUCCESS");
    }

  }
}
