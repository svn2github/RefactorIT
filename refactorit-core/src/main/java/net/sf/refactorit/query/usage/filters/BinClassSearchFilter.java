/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestSuite;


public class BinClassSearchFilter extends SearchFilter {
  private final boolean usages;
  private final boolean importStatements;
  private final boolean methodUsages;
  private final boolean fieldUsages;
  private boolean newExpressions/* = true*/;
  private final boolean skipSelf;

  /**
   * Use for legacy tests only.
   */
  public BinClassSearchFilter(final boolean includeSupertypes,
      final boolean includeSubtypes) {
    this(true, true, true, true, includeSupertypes, includeSubtypes, true,
        false, false, false, false);
  }

  public BinClassSearchFilter(final SearchFilter filter) {
    this(true, true, false, false, filter.isIncludeSupertypes(),
        filter.isIncludeSubtypes(), filter.isShowDuplicates(),
        filter.isGoToSingleUsage(), filter.isSearchNonJavaFiles(),
        filter.isRunWithDefaultSettings(), filter.isSkipSelf());
  }

  public BinClassSearchFilter(final boolean usages,
      final boolean importStatements,
      final boolean methodUsages, final boolean fieldUsages,
      final boolean includeSupertypes, final boolean includeSubtypes,
      final boolean showDuplicates, final boolean goToSingleUsage,
      final boolean searchNonJavaFiles, final boolean runWithDefaultSettings,
      final boolean skipSelf) {
    super(showDuplicates, goToSingleUsage, includeSupertypes, includeSubtypes,
        searchNonJavaFiles, runWithDefaultSettings);

    this.usages = usages;
    this.importStatements = importStatements;
    this.methodUsages = methodUsages;
    this.fieldUsages = fieldUsages;
    this.skipSelf = skipSelf;
  }

  protected boolean passesFilter(final InvocationData invocationData,
      final Project project) {
    if (isFqnTypeImport(invocationData)) {
      return this.importStatements;
    } else if (isMethodUsage(invocationData)) {
      return this.methodUsages;
    } else if (isFieldUsage(invocationData)) {
      return this.fieldUsages;
    } else {
      return this.usages;
    }
  }

  public final boolean isIncludeNewExpressions() {
    return this.newExpressions;
  }

  public final void setIncludeNewExpressions(final boolean b) {
    this.newExpressions = b;
  }

  private static boolean isMethodUsage(final InvocationData invocationData) {
    return (invocationData.getWhat() instanceof BinMethod) &&
        (!(invocationData.getWhat() instanceof BinConstructor));
  }

  private static boolean isFieldUsage(final InvocationData invocationData) {
    return invocationData.getWhat() instanceof BinField;
  }

  public final boolean isUsages() {
    return this.usages;
  }

  public final boolean isImportStatements() {
    return this.importStatements;
  }

  public final boolean isMethodUsages() {
    return this.methodUsages;
  }

  public final boolean isFieldUsages() {
    return this.fieldUsages;
  }

  public final boolean isSkipSelf() {
    return skipSelf;
  }

  public static final class Tests extends SearchFilter.Tests {
    public Tests(final String name) {super(name);
    }

    public static Test suite() {return new TestSuite(Tests.class);
    }

    BinClass type;

    public final void testUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          new Utils.TempCompilationUnit[] {
          new Utils.TempCompilationUnit("class A{}", "A.java", null),
          new Utils.TempCompilationUnit("class B{ A a; B b = a; }", "B.java", null)
      });
      this.type = (BinClass) project.getTypeRefForName("A").getBinType();

      assertUsages(true, new BinClassSearchFilter(true, false, false, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinClassSearchFilter(false, true, true, true, false, false, false, false, false, false, false));
    }

    public final void testImportStatements() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1;class A{}", "A.java",
          "package1"),
          new Utils.TempCompilationUnit("import package1.A; class B{}", "B.java", null)
      });
      this.type = (BinClass) project.getTypeRefForName("package1.A").getBinType();

      assertUsages(true, new BinClassSearchFilter(false, true, false, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinClassSearchFilter(true, false, true, true, false, false, false, false, false, false, false));
    }

    public final void testMethodUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          "class X {" +
          "  public void x() {}" +
          "}" +
          "class XUser {{" +
          "  new X().x();" +
          "}}",
          "X.java",
          null
          );
      this.type = (BinClass)this.project.getTypeRefForName("X").getBinType();

      assertUsages(true, new BinClassSearchFilter(false, false, true, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinClassSearchFilter(false, true, false, true, false, false, false, false, false, false, false));
    }

    public final void testFieldUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          "class X {" +
          "  public int field; {}" +
          "}" +
          "class XUser {{" +
          "  new X().field = 1;" +
          "}}",
          "X.java",
          null
          );
      this.type = (BinClass)this.project.getTypeRefForName("X").getBinType();

      assertUsages(true, new BinClassSearchFilter(false, false, false, true, false, false, false, false, false, false, false));
      assertUsages(false, new BinClassSearchFilter(false, true, true, false, false, false, false, false, false, false, false));
    }

    final void assertUsages(final boolean b, final SearchFilter filter) {
      this.supervisor = new ManagingIndexer();
      new TypeIndexer(supervisor, this.type, filter);
      assertEquals(b, someResultsPass(filter));
    }
  }
}
