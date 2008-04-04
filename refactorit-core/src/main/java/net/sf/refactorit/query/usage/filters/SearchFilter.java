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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.test.Utils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Some parameters are used in the filter() method, some others are only kept
 * here for storage (to keep all search parameters in one place).
 * "Include superclasses" & "- subclasses" options are
 * among those that are not used by filter itself.<br>
 * This filter is used both for specifying search options and for post-search
 * data filtering.
 *
 * @author Risto Alas
 * @author Anton Safonov
 */
public abstract class SearchFilter {
  private final boolean showDuplicates;
  private final boolean goToSingleUsage;
  private final boolean runWithDefaultSettings;

  private final boolean includeSupertypes;
  private final boolean includeSubtypes;

  private final boolean searchNonJavaFiles;

  /**
   * Assumes that "includeSubtypes" and "includeSupertypes" are TRUE;
   * used by filters that don't care about and don't support super- and subclass
   * options.
   */
  public SearchFilter(final boolean showDuplicates,
      final boolean goToSingleUsage,
      final boolean runWithDefaultSettings) {
    this(showDuplicates, goToSingleUsage, true, true, runWithDefaultSettings);
  }

  /**
   * Sets <code>searchNonJavaFiles</code> to <code>false</code>  and
   * <code>nonJavaFilePatterns</code> to <code>null</code>.
   */
  public SearchFilter(final boolean showDuplicates,
      final boolean goToSingleUsage,
      final boolean includeSupertypes, final boolean includeSubtypes,
      final boolean runWithDefaultSettings) {
    this(showDuplicates, goToSingleUsage, includeSupertypes, includeSubtypes,
        false, runWithDefaultSettings);
  }

  public SearchFilter(final boolean showDuplicates,
      final boolean goToSingleUsage,
      final boolean includeSupertypes, final boolean includeSubtypes,
      final boolean searchNonJavaFiles, final boolean runWithDefaultSettings) {
    this.showDuplicates = showDuplicates;
    this.goToSingleUsage = goToSingleUsage;
    this.runWithDefaultSettings = runWithDefaultSettings;
    this.includeSupertypes = includeSupertypes;
    this.includeSubtypes = includeSubtypes;
    this.searchNonJavaFiles = searchNonJavaFiles;
  }

  public final List filter(final List invocationsData, final Project project) {
    final List result = new ArrayList(invocationsData.size());

    for (int i = 0, max = invocationsData.size(); i < max; i++) {
      final InvocationData invocationData
          = (InvocationData) invocationsData.get(i);

      if (passesFilter(invocationData, project)) {
        result.add(invocationData);
      }
    }

    return result;
  }

  public final boolean isIncludeSubtypes() {
    return this.includeSubtypes;
  }

  public final boolean isIncludeSupertypes() {
    return this.includeSupertypes;
  }

  public final boolean isShowDuplicates() {
    return this.showDuplicates;
  }

  public final boolean isSearchNonJavaFiles() {
    return this.searchNonJavaFiles;
  }

  public final boolean isGoToSingleUsage() {
    return this.goToSingleUsage;
  }

  public final boolean isRunWithDefaultSettings() {
    return this.runWithDefaultSettings;
  }

  protected abstract boolean passesFilter(InvocationData invocationData,
      Project project);

  static boolean isFqnTypeImport(final InvocationData invocationData) {
    return ImportUtils.isChildOfImportNode(invocationData.getCompilationUnit(),
        invocationData.getWhereAst());
  }

  static boolean isPackageImport(final InvocationData invocationData) {
    return invocationData.isImportStatement();
  }

  public boolean isSkipSelf() {
    return false;
  }

  public abstract static class Tests extends TestCase {

    public Tests(final String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite result = new TestSuite();
      result.addTest(BinVariableSearchFilter.Tests.suite());
      result.addTest(BinMethodSearchFilter.Tests.suite());
      result.addTest(BinPackageSearchFilter.Tests.suite());
      result.addTest(BinClassSearchFilter.Tests.suite());
      result.addTest(BinInterfaceSearchFilter.Tests.suite());
      return result;
    }

    Project project;
    ManagingIndexer supervisor = null;

    BinClass aClass;
    BinMethod method;

    void initProject(final String sourceCode,
        final String packageName) throws Exception {

      this.project = Utils.createTestRbProjectFromString(sourceCode, "X.java",
          packageName);

      this.aClass = (BinClass) project.getTypeRefForName(getPackagePrefix(
          packageName) + "X").getBinType();
      this.method = aClass.getDeclaredMethod("x", BinTypeRef.NO_TYPEREFS);
    }

    private String getPackagePrefix(final String packageName) {
      return (packageName == null) ? "" : packageName + '.';
    }

    final void initProject(final String sourceCode) throws Exception {
      initProject(sourceCode, null);
    }

    final boolean someResultsPass(final SearchFilter filter) {
      if (this.supervisor == null) {
        return false;
      }

      final List results = filter.filter(
          this.supervisor.getInvocationsForProject(this.project), this.project);
      return results.size() > 0;
    }
  }
}
