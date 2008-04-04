/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.MethodIndexer;

import junit.framework.Test;
import junit.framework.TestSuite;


public final class BinMethodSearchFilter extends SearchFilter {
  private final boolean usages;
  private final boolean overrides;

  private final boolean implementation;

  private final boolean skipSelf;

  /**
   * Use for legacy tests only.
   */
  public BinMethodSearchFilter(final boolean superTypes, final boolean subTypes) {
    this(true, true, superTypes, subTypes, true, false, true, false, false);
  }

  public BinMethodSearchFilter(final SearchFilter filter) {
    this(true, true, filter.isIncludeSupertypes(),
        filter.isIncludeSubtypes(), filter.isShowDuplicates(),
        filter.isGoToSingleUsage(), false, filter.isRunWithDefaultSettings(),
        filter.isSkipSelf());
  }

  public BinMethodSearchFilter(final boolean usages, final boolean overrides,
      final boolean superTypes, final boolean subTypes,
      final boolean showDuplicateLines,
      final boolean goToSingleUsage, final boolean implementation,
      final boolean runWithDefaultSettings, final boolean skipSelf) {
    super(showDuplicateLines, goToSingleUsage, superTypes, subTypes,
        runWithDefaultSettings);

    this.usages = usages;
    this.overrides = overrides;

    this.implementation = implementation;

    this.skipSelf = skipSelf;
  }

  protected final boolean passesFilter(final InvocationData invocationData,
      final Project project) {
    return true;
  }

  public final boolean isUsages() {
    return this.usages;
  }

  public final boolean isOverrides() {
    return this.overrides;
  }

  public final boolean isImplementationSearch() {
    return this.implementation;
  }

  public final boolean isInterfaceSearch() {
    return!this.implementation;
  }

  public final boolean isSkipSelf() {
    return this.skipSelf;
  }

  /////////////////////////////// TESTS ///////////////////////////////

  public static final class Tests extends SearchFilter.Tests {
    public Tests(final String name) {
      super(name);
    }

    public static Test suite() {
      return new TestSuite(Tests.class);
    }

    public final void testOverridesOnly() throws Exception {
      initProject(
          "public class X {\n" +
          "  public void x() {}\n" +
          "}\n\n" +
          "public class XSub extends X {\n" +
          "  public void x() {}\n" +
          "}\n"
          );

      assertUsages(false);
      assertOverrides(true);
    }

    public final void testUsageAndOverride() throws Exception {
      initProject(
          "public class X {\n" +
          "  public void x() { x(); }\n" +
          "}\n\n" +
          "public class XSub extends X {\n" +
          "  public void x() { x(); }\n" +
          "}\n"
          );
      assertUsages(true);
      assertOverrides(true);
    }

    public final void testUsagesOnly() throws Exception {
      initProject(
          "public class X {\n" +
          "  public void x() { x(); }\n" +
          "}\n"
          );
      assertUsages(true);
      assertOverrides(false);
    }

    private void assertUsages(final boolean hasUsages) throws Exception {
      assertUsages(hasUsages, true, false);
    }

    private void assertOverrides(final boolean overrides) throws Exception {
      assertUsages(overrides, false, true);
    }

    private void assertUsages(final boolean hasUsages,
        final boolean includeUsages,
        final boolean includeOverrides) {
      final BinMethodSearchFilter filter
          = new BinMethodSearchFilter(includeUsages, includeOverrides, true,
          true, true, false, true, false, false);
      this.supervisor = new ManagingIndexer();
      new MethodIndexer(supervisor, this.method, filter);
      final boolean pass = someResultsPass(filter);
      assertEquals("Usages: " + filter.filter(
          this.supervisor.getInvocations(), this.project),
          hasUsages, pass);
    }
  }
}
