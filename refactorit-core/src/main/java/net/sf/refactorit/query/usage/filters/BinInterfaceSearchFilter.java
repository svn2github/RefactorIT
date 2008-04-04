/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestSuite;


public final class BinInterfaceSearchFilter extends BinClassSearchFilter {
  private final boolean implementers;
  private final BinItemReference targetInterface;

  /**
   * Use for legacy tests only.
   */
  public BinInterfaceSearchFilter(final BinInterface target,
      final boolean includeSupertypes,
      final boolean includeSubtypes) {
    this(target, true, true, true, true, includeSupertypes, includeSubtypes,
        true, true, false, false, false, false);
  }

  public BinInterfaceSearchFilter(final BinInterface target,
      final SearchFilter filter) {
    this(target, true, true, false, false, filter.isIncludeSupertypes(),
        filter.isIncludeSubtypes(), true, filter.isShowDuplicates(),
        filter.isGoToSingleUsage(), filter.isSearchNonJavaFiles(),
        filter.isRunWithDefaultSettings(), filter.isSkipSelf());
  }

  public BinInterfaceSearchFilter(final BinInterface target,
      final boolean usages,
      final boolean importStatements, final boolean methodUsages,
      final boolean fieldUsages, final boolean includeSupertypes,
      final boolean includeSubtypes, final boolean implementers,
      final boolean showDuplicates, final boolean goToSingleUsage,
      final boolean searchNonJavaFiles, final boolean runWithDefaultSettings,
      boolean skipSelf) {
    super(usages, importStatements, methodUsages, fieldUsages,
        includeSupertypes,
        includeSubtypes, showDuplicates, goToSingleUsage, searchNonJavaFiles,
        runWithDefaultSettings, skipSelf);

    this.targetInterface = target.createReference();
    this.implementers = implementers;
  }

  protected final boolean passesFilter(final InvocationData invocationData,
      final Project project) {
    if (isImplementer(invocationData, project)) {
      return this.implementers;
    } else {
      return super.passesFilter(invocationData, project);
    }
  }

  private boolean isImplementer(final InvocationData invocationData,
      final Project project) {
    if (invocationData.getWhere() instanceof BinTypeRef) {
      BinInterface target = (BinInterface) targetInterface.restore(project);
      return ((BinTypeRef) invocationData.getWhere()).isDerivedFrom(target.getTypeRef());
    } else {
      return false;
    }
  }

  public final boolean isImplementers() {
    return this.implementers;
  }

  public static final class Tests extends SearchFilter.Tests {
    public Tests(final String name) {super(name);
    }

    public static Test suite() {return new TestSuite(Tests.class);
    }

    BinInterface type;

    public final void testUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("interface A{}", "A.java", null),
          new Utils.TempCompilationUnit("class B{ A a; B b = a; }", "B.java", null)
      });
      this.type = (BinInterface) project.getTypeRefForName("A").getBinType();

      assertUsages(true, new BinInterfaceSearchFilter(this.type, true, false, false, false, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinInterfaceSearchFilter(this.type, false, true, true, true, false, false, true, false, false, false, false, false));
    }

    public final void testImportStatements() throws Exception {
      this.project = Utils.createTestRbProjectFromString(new Utils.
          TempCompilationUnit[] {
          new Utils.TempCompilationUnit("package package1; interface A{}", "A.java",
          "package1"),
          new Utils.TempCompilationUnit("import package1.A; class B{}", "B.java", null)
      });
      this.type = (BinInterface) project.getTypeRefForName("package1.A").
          getBinType();

      assertUsages(true, new BinInterfaceSearchFilter(this.type, false, true, false, false, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinInterfaceSearchFilter(this.type, true, false, true, true, false, false, true, false, false, false, false, false));
    }

    public final void testMethodUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          "interface X {" +
          "  public void x();" +
          "}" +
          "class XImpl implements X { public void x() {} }" +
          "class XUser {{" +
          "  X x = new XImpl();" +
          "  x.x();" +
          "}}",
          "X.java",
          null
          );
      this.type = (BinInterface)this.project.getTypeRefForName("X").getBinType();

      assertUsages(true, new BinInterfaceSearchFilter(this.type, false, false, true, false, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinInterfaceSearchFilter(this.type, false, true, false, true, false, false, false, false, false, false, false, false));
    }

    public final void testFieldUsages() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          "interface X {" +
          "  public final int field = 3;" +
          "}" +
          "class XUser {{" +
          "  int i = X.field;" +
          "}}",
          "X.java",
          null
          );
      this.type = (BinInterface)this.project.getTypeRefForName("X").getBinType();

      assertUsages(true, new BinInterfaceSearchFilter(this.type, false, false, false, true, false, false, false, false, false, false, false, false));
      assertUsages(false, new BinInterfaceSearchFilter(this.type, false, true, true, false, false, false, true, false, false, false, false, false));
    }

    public final void testImplementers() throws Exception {
      this.project = Utils.createTestRbProjectFromString(
          "interface X {" +
          "  public final int field = 3;" +
          "}" +
          "class XImpl implements X{}",
          "X.java",
          null
          );
      this.type = (BinInterface)this.project.getTypeRefForName("X").getBinType();

      assertUsages(true, new BinInterfaceSearchFilter(this.type, false, false, false, false, false, false, true, false, false, false, false, false));
      assertUsages(false, new BinInterfaceSearchFilter(this.type, true, true, true, true, false, false, false, false, false, false, false, false));
    }

    final void assertUsages(final boolean b, final SearchFilter filter) {
      this.supervisor = new ManagingIndexer();
      new TypeIndexer(supervisor, this.type, filter);
      assertEquals(b, someResultsPass(filter));
    }
  }
}
