/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.test.commonIDE.NullController;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.vfs.local.LocalSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;



/**
 * Runs various benchmarks on RefactorIT project.
 */
public class BenchmarkRunner {

  public static class Results {
    /** Time to load project using <code>load()</code>. */
    public final Series loadProject = new Series("Load project", 1, "ms");

    /** Amount of memory used by loading project */
    public final Series loadProjectMemory =
        new Series("Load project (memory)", 1024, "kB");

    /** Time to index project with {@link AbstractIndexer}. */
    public final Series indexProject = new Series("Index project", 1, "ms");

    /** Amount of memory used by indexing project */
    public final Series indexProjectMemory =
        new Series("Index project (memory)", 1024, "kB");

    /** Time to find usages of {@link BinCIType} with default settings. */
    public final Series findBinCITypeDefault =
        new Series("Usages of BinCIType", 1, "ms");

    /**
     * Amount of memory used by finding usages of {@link BinCIType} with
     * default settings.
     */
    public final Series findBinCITypeDefaultMemory =
        new Series("Usages of BinCIType (memory)", 1024, "kB");

    /** Time to find usages of {@link BinCIType} (sub &#43; super). */
    public final Series findBinCITypeSubSuper =
        new Series("Usages of BinCIType (full)", 1, "ms");

    /**
     * Amount of memory used by finding usages of {@link BinCIType}
     * (sub &#43; super).
     */
    public final Series findBinCITypeSubSuperMemory =
        new Series("Usages of BinCIType (full) (memory)", 1024, "kB");

    /** Time to calculate all metrics for whole project. */
    public final Series allMetrics = new Series("All metrics", 1, "ms");

    /** Amount of memory used to to calculate all metrics for whole project. */
    public final Series allMetricsMemory =
        new Series("All metrics (memory)", 1024, "kB");

    public final Series cleanRebuild = new Series("Clean rebuild", 1, "ms");
    public final Series cleanRebuildMemory =
        new Series("Clean rebuild (memory)", 1024, "kB");

    public final Series fullRebuild = new Series("Full rebuild", 1, "ms");
    public final Series fullRebuildMemory =
        new Series("Full rebuild (memory)", 1024, "kB");

    public final Series incrementalRebuild = new Series("Incremental rebuild",
        1, "ms");
    public final Series incrementalRebuildMemory =
        new Series("Incremental rebuild (memory)", 1024, "kB");

    public List projectErrors = null;

    public List allSeries = new ArrayList();

    public Results() {
      allSeries.add(loadProject);
      allSeries.add(loadProjectMemory);
      allSeries.add(indexProject);
      allSeries.add(indexProjectMemory);
      allSeries.add(findBinCITypeDefault);
      allSeries.add(findBinCITypeDefaultMemory);
      allSeries.add(findBinCITypeSubSuper);
      allSeries.add(findBinCITypeSubSuperMemory);
      allSeries.add(allMetrics);
      allSeries.add(allMetricsMemory);
      allSeries.add(cleanRebuild);
      allSeries.add(cleanRebuildMemory);
      allSeries.add(fullRebuild);
      allSeries.add(fullRebuildMemory);
      allSeries.add(incrementalRebuild);
      allSeries.add(incrementalRebuildMemory);
    }
  }


  /** Data series. */
  public static class Series {
    /** Series data. */
    private final List data = new ArrayList();

    /** Description. */
    private final String description;

    /** Scale. */
    private final double scale;

    /** Measurement unit. */
    private final String unit;

    /**
     * Creates new series.
     *
     * @param description description.
     */
    public Series(String description, double scale, String unit) {
      this.description = description;
      this.scale = scale;
      this.unit = unit;
    }

    /**
     * Gets description.
     *
     * @return description.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Gets scale.
     *
     * @return scale.
     */
    public double getScale() {
      return scale;
    }

    /**
     * Gets measurement unit.
     *
     * @return unit.
     */
    public String getUnit() {
      return unit;
    }

    /**
     * Adds value to this series.
     *
     * @param value value.
     */
    public void addValue(long value) {
      data.add(new Long(value));
    }

    /**
     * Adds Value Not Available to this series.
     */
    public void addNa() {
      data.add(null);
    }

    /**
     * Gets last value.
     *
     * @return last value or <code>null</code> if last value not available.
     */
    public Long getLastValue() {
      if (data.size() < 1) {
        return null;
      }
      return (Long) data.get(data.size() - 1);
    }

    /**
     * Calculates minimum for the series.
     *
     * @return minimum or <code>null</code> if minimum not available.
     */
    public Long getMinimum() {
      int valueCount = 0;
      long minimum = Long.MAX_VALUE;
      for (final Iterator i = data.iterator(); i.hasNext(); ) {
        final Long valueContainer = (Long) i.next();
        if (valueContainer == null) {
          continue;
        }
        valueCount++;
        if (valueContainer.longValue() < minimum) {
          minimum = valueContainer.longValue();
        }
      }

      if (valueCount == 0) {
        return null;
      } else {
        return new Long(minimum);
      }
    }

    /**
     * Calculates maximum for the series.
     *
     * @return maximum or <code>null</code> if maximum not available.
     */
    public Long getMaximum() {
      int valueCount = 0;
      long maximum = Long.MIN_VALUE;
      for (final Iterator i = data.iterator(); i.hasNext(); ) {
        final Long valueContainer = (Long) i.next();
        if (valueContainer == null) {
          continue;
        }
        valueCount++;
        if (valueContainer.longValue() > maximum) {
          maximum = valueContainer.longValue();
        }
      }

      if (valueCount == 0) {
        return null;
      } else {
        return new Long(maximum);
      }
    }

    /**
     * Calculates average for the series.
     *
     * @return average or <code>null</code> if average not available.
     */
    public Double getAverage() {
      int valueCount = 0;
      double sum = 0;
      for (final Iterator i = data.iterator(); i.hasNext(); ) {
        final Long valueContainer = (Long) i.next();
        if (valueContainer == null) {
          continue;
        }
        sum += valueContainer.longValue();
        valueCount++;
      }

      if (valueCount == 0) {
        return null;
      } else {
        return new Double(sum / valueCount);
      }
    }

    /**
     * Calculates standard deviation for the series.
     *
     * @return standard deviation or <code>null</code> if standard deviation
     *         not available.
     */
    public Double getStandardDeviation() {
      final Double averageContainer = getAverage();
      if (averageContainer == null) {
        return null;
      }
      final double average = averageContainer.doubleValue();

      int valueCount = 0;
      double sum = 0;
      for (final Iterator i = data.iterator(); i.hasNext(); ) {
        final Long valueContainer = (Long) i.next();
        if (valueContainer == null) {
          continue;
        }
        final long value = valueContainer.longValue();
        sum += (value - average) * (value - average);
        valueCount++;
      }

      if (valueCount < 2) {
        return null;
      } else {
        return new Double(
            Math.sqrt(sum / (valueCount - 1)));
      }
    }

    /**
     * Gets string represenation of this series.
     */
    public String toString() {
      return getDescription() + ": " + data;
    }

    /**
     * Formats value with proper measurement unit and scale.
     *
     * @return formatted value.
     */
    public String formatValue(Double value) {
      if (value == null) {
        return "N/A";
      } else {
        return ((long) (value.doubleValue() / getScale())) + " " + getUnit();
      }
    }

    /**
     * Formats value with proper measurement unit and scale.
     *
     * @return formatted value.
     */
    public String formatValue(Long value) {
      if (value == null) {
        return "N/A";
      } else {
        return ((long) (value.longValue() / getScale())) + " " + getUnit();
      }
    }
  }


  /** RefactorIT project.*/
  private final Project project;

  /** Results. */
  private final Results results;

  /**
   * Runs benchmarks specified number of times and prints results to standard
   * out.
   *
   * @param params command line parameters.
   */
  public static final void main(String[] params) throws Exception {
    int runCount = 3;
    if (params.length > 0) {
      runCount = Integer.parseInt(params[0]);
    }
    if (runCount < 1) {
      System.err.println("Number of runs must be 1 or greater");
      System.exit(1);
    }

    final PrintStream summaryOut;
    if (params.length > 1) {
      final File summaryFile = new File(params[1]);
      System.out.println("Writing final results to " + summaryFile);
      summaryOut = new PrintStream(new FileOutputStream(summaryFile));
    } else {
      System.out.println("Writing final results stdout");
      summaryOut = System.out;
    }

    runOnce(new Results()); // first run always shows bad results for some reason
    runGc();
    runGc();

    final Results results = new Results();

    for (int i = 0; i < runCount; i++) {
      System.out.println("Starting run " + (i + 1) + " out of " + runCount);
      runOnce(results);

      showIntermediateResults(results);

      runGc();
      runGc();
    }

    System.out.println(); // System.out is correct here
    System.out.println("FINAL RESULTS"); // System.out is correct here
    summaryOut.println(runCount + " run" + ((runCount != 1) ? "s" : "") + ".");
    summaryOut.println();
    showFinalResults(results, summaryOut);
    summaryOut.close();
  }

  /**
   * One run of benchmarks.
   *
   * @param results results.
   */
  private static void runOnce(Results results) throws Exception {
    final BenchmarkRunner runner = new BenchmarkRunner(results);
    runner.runAll();
  }

  /**
   * Shows intermediate results with description, last value and average
   * included for each series.
   */
  private static void showIntermediateResults(Results results) {
    if (results.projectErrors != null) {
      for (int i = 0, max = results.projectErrors.size(); i < max; i++) {
        System.out.println(results.projectErrors.get(i));
      }
    }

    for (final Iterator i = results.allSeries.iterator(); i.hasNext(); ) {
      final Series series = (Series) i.next();
      System.out.println(series.getDescription() + ":"
          + " " + series.formatValue(series.getLastValue())
          + " (avg: " + series.formatValue(series.getAverage())
          + ", s: " + series.formatValue(series.getStandardDeviation())
          + ", min: " + series.formatValue(series.getMinimum())
          + ", max: " + series.formatValue(series.getMaximum()) + ")");
    }
  }

  /**
   * Shows final results with description and average included for each
   * series.
   *
   * @param results results.
   * @param out stream to write results to.
   */
  private static void showFinalResults(Results results, PrintStream out) {
    if (results.projectErrors != null) {
      for (int i = 0, max = results.projectErrors.size(); i < max; i++) {
        out.println(results.projectErrors.get(i));
      }
    }

    String oneLine = "";
    for (final Iterator i = results.allSeries.iterator(); i.hasNext(); ) {
      final Series series = (Series) i.next();
      out.println(series.getDescription() + ":"
          + " avg: " + series.formatValue(series.getAverage())
          + ", s: " + series.formatValue(series.getStandardDeviation())
          + ", min: " + series.formatValue(series.getMinimum())
          + ", max: " + series.formatValue(series.getMaximum()));
      out.flush();
      if (oneLine.length() > 0) {
        oneLine += ", ";
      }
      oneLine += ((long) (series.getAverage().doubleValue() / series.getScale()));
    }
    out.println("Summary: " + oneLine);
    out.flush();
  }

  /**
   * Constructs new benchmark runner that will output results to the specified
   * container.
   *
   * @param results results.
   */
  public BenchmarkRunner(Results results) throws Exception {
    this.results = results;

    GlobalOptions.setProperties(new Properties());
    DialogManager.setInstance(new NullDialogManager());

    GlobalOptions.setOption("debug.checkIntegrityAfterLoad", "false");
    new NullController();

    project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("RefactorIT"));
  }

  /**
   * Runs all benchmarks
   */
  public void runAll() throws Exception {
    loadProject();
    indexProject();

    findBinCITypeDefault();
    findBinCITypeSubSuper();

    calculateAllMetrics();

    calculateCleanRebuild();
    calculateTotalRebuild();
    calculateIncrementalRebuild();

    IDEController.getInstance().getWorkspace().closeProject(project);
  }

  /**
   * Gets amount of Java memory used at the moment.
   *
   * @return amount.
   */
  private static long getUsedMemory() {
    return
        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  /**
   * Runs garbage collector.
   */
  private static void runGc() {
    System.runFinalization();
    System.gc();

    System.runFinalization();
    System.gc();

    System.runFinalization();
    System.gc();

    try {
      Thread.sleep(7000);
    } catch (InterruptedException e) {
    }
  }

  /**
   * Loads project.
   */
  public void loadProject() throws Exception {
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();
    project.getProjectLoader().build();
    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();
    results.loadProject.addValue(timeAfter - timeBefore);
    results.loadProjectMemory.addValue(memoryUsedAfter - memoryUsedBefore);
  }

  /**
   * Indexes project using {@link AbstractIndexer} to load bodies of all
   * methods. Assumes the project is already loaded.
   */
  public void indexProject() throws Exception {
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();
    project.accept(new AbstractIndexer() {});
    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();
    results.indexProject.addValue(timeAfter - timeBefore);
    results.indexProjectMemory.addValue(memoryUsedAfter - memoryUsedBefore);

    if ((project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()) {
      results.projectErrors = CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors());
    }
  }

  /**
   * Finds usages of {@link BinCIType} with default settings.
   * Assumes the project is already loaded.
   */
  public void findBinCITypeDefault() throws Exception {
    Finder.clearInvocationMap();
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();
    final BinCIType type =
        project.getTypeRefForName("net.sf.refactorit.classmodel.BinCIType")
        .getBinCIType();

    Finder.getInvocations(type, new BinClassSearchFilter(true, true, false,
        false, false, false, false, false, false, false, true));

    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();
    results.findBinCITypeDefaultMemory.addValue(
        memoryUsedAfter - memoryUsedBefore);
    results.findBinCITypeDefault.addValue(timeAfter - timeBefore);
  }

  /**
   * Finds usages of {@link BinCIType} (sub &#43; super).
   * Assumes the project is already loaded.
   */
  public void findBinCITypeSubSuper() throws Exception {
    Finder.clearInvocationMap();
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();
    final BinCIType type =
        project.getTypeRefForName("net.sf.refactorit.classmodel.BinCIType")
        .getBinCIType();

    Finder.getInvocations(type, new BinClassSearchFilter(true, true, true,
        true, true, true, false, false, true, false, true));

    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();
    results.findBinCITypeSubSuperMemory.addValue(
        memoryUsedAfter - memoryUsedBefore);
    results.findBinCITypeSubSuper.addValue(timeAfter - timeBefore);
  }

  /**
   * Calculates all metrics for whole project.
   * Assumes the project is already loaded.
   */
  public void calculateAllMetrics() throws Exception {
    Finder.clearInvocationMap();
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();
//    Main.setOption("Metrics.xxx", "on");
    final MetricsModel model = new MetricsModel(
        MetricsAction.getDefaultColumnNames(),
        MetricsAction.getDefaultActionIndexes()
        );
    model.getState().setProfile(Profile.createDefaultMetrics());

    model.populate(project, project);
    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();
    results.allMetricsMemory.addValue(
        memoryUsedAfter - memoryUsedBefore);
    results.allMetrics.addValue(timeAfter - timeBefore);
  }

  private void calculateCleanRebuild() {
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();

    project.clean();

    try {
      project.getProjectLoader().build(null, false);
    } catch (Exception e) {
      e.printStackTrace();
    }

    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();

    results.cleanRebuild.addValue(timeAfter - timeBefore);
    results.cleanRebuildMemory.addValue(memoryUsedAfter - memoryUsedBefore);
  }

  private void calculateTotalRebuild() {
    runGc();

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();

    try {
      project.getProjectLoader().build(null, true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();

    results.fullRebuild.addValue(timeAfter - timeBefore);
    results.fullRebuildMemory.addValue(memoryUsedAfter - memoryUsedBefore);
  }

  private void calculateIncrementalRebuild() {
    runGc();

    List allSources = project.getPaths().getSourcePath().getAllSources();
    for (int i = 0, max = allSources.size(); i < max; i++) {
      LocalSource ls = (LocalSource) allSources.get(i);
      if ("BinCIType.java".equals(ls.getName())) {
        ls.setLastModified(System.currentTimeMillis());
      }
    }

    final long memoryUsedBefore = getUsedMemory();
    final long timeBefore = System.currentTimeMillis();

    try {
      project.getProjectLoader().build(null, false);
    } catch (Exception e) {
      e.printStackTrace();
    }

    final long timeAfter = System.currentTimeMillis();
    final long memoryUsedAfter = getUsedMemory();

    results.incrementalRebuild.addValue(timeAfter - timeBefore);
    results.incrementalRebuildMemory.addValue(memoryUsedAfter
        - memoryUsedBefore);
  }
}
