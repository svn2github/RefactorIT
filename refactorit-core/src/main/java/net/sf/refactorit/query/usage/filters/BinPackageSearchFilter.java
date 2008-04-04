/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.PackageIndexer;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestSuite;


public final class BinPackageSearchFilter extends SearchFilter {
  private final boolean usages;
  private final boolean imports;
  private final boolean packageStatements;
  private final boolean subpackages;

  public BinPackageSearchFilter(final boolean subpackages,
      final boolean includeSuperclasses, final boolean includeSubclasses) {
    this(true, true, true, subpackages,
      includeSuperclasses, includeSubclasses, false,
      true, false, false);
  }

  public BinPackageSearchFilter(final boolean usages, final boolean imports,
      final boolean packageStatements, final boolean subpackages,
      final boolean includeSuperclasses, final boolean includeSubclasses,
      final boolean searchNonJavaFiles,
      final boolean showDuplicates, final boolean goToSingleUsage,
      final boolean runWithDefaultSettings) {
    super(showDuplicates, goToSingleUsage, includeSuperclasses,
        includeSubclasses, searchNonJavaFiles, runWithDefaultSettings);

    this.usages = usages;
    this.imports = imports;
    this.packageStatements = packageStatements;
    this.subpackages = subpackages;
  }

  protected final boolean passesFilter(final InvocationData invocationData,
      final Project project) {
    if (isPackageImport(invocationData) || isFqnTypeImport(invocationData)) {
      return this.imports;
    } else if (invocationData.isPackageStatement()) {
      return this.packageStatements;
    } else {
      return this.usages;
    }
  }

  public final boolean isIncludeSubPackages() {
    return this.subpackages;
  }

  public final boolean isUsages() {
    return this.usages;
  }

  public final boolean isImports() {
    return this.imports;
  }

  public final boolean isPackageStatements() {
    return this.packageStatements;
  }

  ////////////////////////////////////// TESTS //////////////////////////////////

  public static final class Tests extends SearchFilter.Tests {
    public Tests(final String name) {super(name);
    }

    public static Test suite() {return new TestSuite(Tests.class);
    }

    BinPackage aPackage;

    public final void testImportStatementsOnly() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1; public class A{}",
          "A.java", "package1"),
          new Utils.TempCompilationUnit(
          "package package2; import package1.*; public class B{}", "B.java",
          "package2"),
      });
      this.aPackage = this.project.getPackageForName("package1");

      assertUsages(false);
      assertImports(true);
      assertPackageStatements(true);
    }

    public final void testFqnImportStatementsOnly() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1; public class A{}",
          "A.java", "package1"),
          new Utils.TempCompilationUnit(
          "package package2; import package1.A; public class B{}", "B.java",
          "package2"),
      });
      this.aPackage = this.project.getPackageForName("package1");

      assertUsages(false);
      assertImports(true);
      assertPackageStatements(true);
    }

    public final void testPrefixedPackageContent() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1; public class A{}",
          "A.java", "package1"),
          new Utils.TempCompilationUnit(
          "package package2; public class B{ package1.A a; }", "B.java",
          "package2"),
      });
      this.aPackage = this.project.getPackageForName("package1");

      assertUsages(true);
      assertImports(false);
      assertPackageStatements(true);
    }

    public final void testUsagesOnly() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1; public class A{}",
          "A.java", "package1"),
          new Utils.TempCompilationUnit(
          "package package2; public class B{ package1.A a; package1.A b = a; }",
          "B.java", "package2"),
      });
      this.aPackage = this.project.getPackageForName("package1");

      assertUsages(true);
      assertImports(false);
      assertPackageStatements(true);
    }

    public final void testPackageStatementsOnly() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit(
          "package package1; import package2.*; public class A{}", "A.java",
          "package1"),
          new Utils.TempCompilationUnit("package package2; public class B{}",
          "B.java", "package2")
      });
      this.aPackage = this.project.getPackageForName("package1");

      assertUsages(false);
      assertImports(false);
      assertPackageStatements(true);
    }

    public final void testNothing() throws Exception {
      initProject("public class X{}", null);

      this.aPackage = this.project.getTypeRefForName("X").getBinType().
          getPackage();

      assertUsages(false);
      assertImports(false);
      assertPackageStatements(false);
    }

    private void assertUsages(final boolean usagesPresent) {
      assertSomeResultsPass(usagesPresent, new BinPackageSearchFilter(true, false, false, false, false, false, false, false, false, false));
    }

    private void assertImports(final boolean importsPresent) {
      assertSomeResultsPass(importsPresent, new BinPackageSearchFilter(false, true, false, false, false, false, false, false, false, false));
    }

    public final void assertPackageStatements(final boolean b) {
      assertSomeResultsPass(b, new BinPackageSearchFilter(false, false, true, false, false, false, false, false, false, false));
    }

    private void assertSomeResultsPass(final boolean b,
        final SearchFilter filter) {
      this.supervisor = new ManagingIndexer();
      new PackageIndexer(this.supervisor, this.aPackage, true, true);
      assertEquals(b, someResultsPass(filter));
    }
  }
}
