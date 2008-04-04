/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.reports.ReportGeneratorFactory;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.options.profile.ProfileDialog;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class MetricsAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.MetricsAction";
  public static final String NAME = "Metrics";

  /** count of available metric columns */
  public static final int METRICS = 1 + 30;

  static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(MetricsAction.class);

  static ArrayList columnKeys = new ArrayList();
  static ArrayList columnNames = new ArrayList();
  static ArrayList columnTooltips = new ArrayList();

  static {
    for (int i = 0; i < METRICS; i++) {
      String key = GlobalOptions.getOption("metric.column." + i);
      if (key == null) {
        key = getDefaultKey(i);

      }

      String name = resLocalizedStrings.getString(key);
      String tip = resLocalizedStrings.getString(key + ".tooltip");

      columnKeys.add(key);
      columnNames.add(name);
      columnTooltips.add(tip);
    }
  }

  public boolean isAvailableForType(Class type) {
    return Project.class.equals(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type);
  }

  public boolean isReadonly() {
    return true;
  }

  //  private static final ImageIcon showAll
  //      = ResourceUtil.getIcon(MetricsAction.class, "showAll.gif");

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  /**
   * Module execution.
   *
   * @param context
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters

    Assert.must(
        context != null,
        "Attempt to pass NULL context into MetricsAction.run()");

    final Object target = RefactorItActionUtils.unwrapTarget(object);

    if (target instanceof BinMember
        && !((BinMember) target).getTopLevelEnclosingType().isFromCompilationUnit()) {
      DialogManager.getInstance()
          .showNonSourcePathItemInfo(context, getName(), target);
      return false;
    }

    reload(null, context, target);

    return false;
  }

  BinTreeTable createTable(
      MetricsModel model, final RefactorItContext context, final Object target
      ) {
    final BinTreeTable table = new BinTreeTable(model, context) {
      public String getToolTipText(MouseEvent event) {
        int hitColumnIndex = columnAtPoint(event.getPoint());
        return (String) columnTooltips.get(hitColumnIndex);
      }
    };

    table.setDefaultRenderer(Integer.class, new MetricsCellRenderer());
    table.setDefaultRenderer(Double.class, new MetricsCellRenderer());

    final JTableHeader header = new JTableHeader(table.getColumnModel()) {
      public String getToolTipText(MouseEvent event) {
        int hitColumnIndex = columnAtPoint(event.getPoint());
        return (String) columnTooltips.get(hitColumnIndex);
      }
    };

    table.setTableHeader(header);

    final MetricsModel.State curState = model.getState();

    boolean[] showColumns = new boolean[METRICS - 1];
    for (int i = 0; i < METRICS - 1; i++) {
      showColumns[i] = getColumnShowFlag(
          (String) columnKeys.get(i + 1),
          curState);
    }

    for (int i = 1, max = table.getColumnModel().getColumnCount(); i < max; i++) {
      final TableColumn column = table.getColumnModel().getColumn(i);
      if (!showColumns[i - 1]) {
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setWidth(0);
        column.setPreferredWidth(0);
        column.setResizable(false);
      } else {
        //int mid = column.getPreferredWidth();
        column.setMinWidth(5);
        column.setMaxWidth(100);
        column.setPreferredWidth(column.getPreferredWidth());
        column.setResizable(true);
      }
    }

    //    if (target != null) ((BinTreeTable)table).expandAll();

    //    table.getTree().setRootVisible(false);
    //    table.getTree().setShowsRootHandles(true);

    table.setSelectionMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

    table.getColumnModel().getColumn(0).setMinWidth(30);
    table.getColumnModel().getColumn(0).setMaxWidth(2000);
    try {
      int width = new Integer(GlobalOptions.getOption("Metrics.column.target.width", "200"))
          .intValue();
      table.getColumnModel().getColumn(0).setPreferredWidth(width);
      table.getColumnModel().getColumn(0).setWidth(width);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      table.getColumnModel().getColumn(0).setPreferredWidth(200);
    }

    table.getColumnModel().addColumnModelListener(
        new TableColumnModelListener() {
      private boolean adjusting;

      public void columnAdded(TableColumnModelEvent e) {}

      public void columnRemoved(TableColumnModelEvent e) {}

      public void columnSelectionChanged(ListSelectionEvent e) {}

      public void columnMoved(TableColumnModelEvent e) {
        int from = e.getFromIndex();
        int to = e.getToIndex();

        if (from == to) {
          return;
        }

        Object key = columnKeys.remove(from);
        Object name = columnNames.remove(from);
        Object tip = columnTooltips.remove(from);

        columnNames.add(to, name);
        columnTooltips.add(to, tip);
        columnKeys.add(to, key);

        saveColumns();
      }

      /** Tells listeners that a column was moved due to a margin change. */
      public void columnMarginChanged(ChangeEvent e) {
        if (!adjusting) {
          adjusting = true;

          int width = table.getColumnModel().getColumn(0).getWidth();
          if (width < 30) {
            width = 30;
          }

          GlobalOptions.setOption("Metrics.column.target.width", new Integer(width).toString());

          adjusting = false;
        }
      }
    });

    return table;
  }

  private void reload(
      BinPanel panel, final RefactorItContext context, final Object object
  ) {
    final MetricsModel model = new MetricsModel(columnNames, getActionIndexes());

    if (context.getState() == null) {
      MetricsModel.State result = ProfileDialog.showMetrics();
      if (result == null) {
        // Cancel
        return;
      }

      model.setState(result);
      context.setState(result);
    } else {
      // Reloading, so just get ready settings
      model.setState((MetricsModel.State) context.getState());
    }

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model.populate(context.getProject(), object);
        }
      }, true);

      BinTreeTable table = createTable(model, context, object);
      if (panel == null) {
        String moduleName = resLocalizedStrings.getString("module.name");
        ResultArea results = ResultArea.create(table, context,
            MetricsAction.this);
        results.setTargetBinObject(object);
        BinPanel newPanel = BinPanel.getPanel(context, moduleName, results);
        table.smartExpand();

        // Register default help for panel's current toolbar
        newPanel.setDefaultHelp("refact.metrics");
        newPanel.addToolbarButton(ReportGeneratorFactory.getMetricsReportGenerator().getReportButton(
                new BinTreeTableModel[] { model }, context, object));
            //getCopy2HtmlButton(model, context, object));

        // reload the panel because we added a help button, so
        // the help button be come visible on the screen. Otherwise
        // if we do not do reload(), then the help button is not visible
        // in the first time. It comes visible after some other object calls
        // reload on BinPanel. It actually needs refactoring. This call SMELLS!
        // It fixes Bug 1104
        newPanel.reload();

        //  initFilterButtons(newPanel, context, parent, object);
      } else {
        panel.addToolbarButton(ReportGeneratorFactory.getMetricsReportGenerator().getReportButton(
            new BinTreeTableModel[] { model }, context, object));
        panel.reload(table);
        table.smartExpand();
      }
    } catch (SearchingInterruptedException ex) {
    }

  }

//  JButton getCopy2HtmlButton(
//      final MetricsModel model, final IdeWindowContext context, Object target
//  ) {
//    JButton html = new JButton(iconHtml);
//    html.setMargin(new Insets(0, 0, 0, 0));
//    html.setToolTipText(resLocalizedStrings.getString("button.html.tooltip"));
//
//    String name = "Metrics for ";
//    if (target != null) {
//      if (target instanceof BinPackage) {
//        name += ((BinPackage) target).getQualifiedName();
//      } else if (target instanceof BinMember) {
//        name += ((BinMember) target).getQualifiedName();
//      } else if (target instanceof CompilationUnit) {
//        name += ((CompilationUnit) target).getName();
//      }
//    } else {
//      name += "the whole project";
//    }
//
//    final String fName = name;
//
//    html.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        onCopy2HtmlClick(model, context, fName);
//      }
//    });
//
//    return html;
//  }
//
//  void onCopy2HtmlClick(
//      MetricsModel model, IdeWindowContext context, String title
//  ) {
//    JFileChooser chooser = new JFileChooser();
//    chooser.setFileFilter(new FileFilter() {
//      public boolean accept(File f) {
//        if (f.isDirectory()) {
//          return true;
//        }
//
//        String fileExt = FileUtil.getExtension(f);
//        if (fileExt.equals("xml")) {
//          return true;
//        }
//
//        return false;
//      }
//
//      public String getDescription() {
//        return "XML files";
//      }
//    });
//
//    chooser.setFileFilter(new FileFilter() {
//      public boolean accept(File f) {
//        if (f.isDirectory()) {
//          return true;
//        }
//
//        String fileExt = FileUtil.getExtension(f);
//        if (fileExt.equals("htm") ||
//            fileExt.equals("html")) {
//          return true;
//        }
//
//        return false;
//      }
//
//      public String getDescription() {
//        return "HTML files (default)";
//      }
//    });
//
//    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
//    int res = RitDialog.showFileDialog(context, chooser);
//    if (res != JFileChooser.APPROVE_OPTION) {
//      return;
//    }
//
//    File file = chooser.getSelectedFile();
//    String fileExt = FileUtil.getExtension(file);
//    if (!(fileExt.equals("htm") || fileExt.equals("html") || fileExt.equals("xml"))) {
//      file = new File(file.getAbsolutePath() + ".html");
//    }
//
//    PrintWriter pw = null;
//    try {
//      pw = new PrintWriter(new FileOutputStream(file));
//
//      if(fileExt.equals("xml")) {
//        XMLExporter converter = new MetricsXMLExporter(GlobalOptions.getEncoding(), null, true);
//        converter.process(model, pw);
//      } else {
//        pw.print(TableLayout.getClipboardText(new HtmlTableFormat(), model, title));
//      }
//    } catch (Exception ignore) {
//    } finally {
//      if (pw != null) {
//        pw.close();
//      }
//    }
//  }

  boolean getColumnShowFlag(String key, MetricsModel.State curState) {
    if ("table.column.loc".equals(key)) {
      return curState.isLocRun();
    } else if ("table.column.complexity".equals(key)) {
      return curState.isCcRun();
    } else if ("table.column.ncloc".equals(key)) {
      return curState.isNclocRun();
    } else if ("table.column.cloc".equals(key)) {
      return curState.isClocRun();
    } else if ("table.column.dc".equals(key)) {
      return curState.isDcRun();
    } else if ("table.column.executableStatements".equals(key)) {
      return curState.isExecRun();
    } else if ("table.column.numberOfParameters".equals(key)) {
      return curState.isNpRun();
    } else if ("table.column.weightedMethodsPerClass".equals(key)) {
      return curState.isWmcRun();
    } else if ("table.column.rfc".equals(key)) {
      return curState.isRfcRun();
    } else if ("table.column.depthInTree".equals(key)) {
      return curState.isDitRun();
    } else if ("table.column.numberOfChildren".equals(key)) {
      return curState.isNocRun();
    } else if ("table.column.not".equals(key)) {
      return curState.isNotRun();
    } else if ("table.column.nota".equals(key)) {
      return curState.isNotaRun();
    } else if ("table.column.notc".equals(key)) {
      return curState.isNotcRun();
    } else if ("table.column.note".equals(key)) {
      return curState.isNoteRun();
    } else if ("table.column.ca".equals(key)) {
      return curState.isCaRun();
    } else if ("table.column.ce".equals(key)) {
      return curState.isCeRun();
    } else if ("table.column.instability".equals(key)) {
      return curState.isInstabilityRun();
    } else if ("table.column.abstractness".equals(key)) {
      return curState.isAbstractnessRun();
    } else if ("table.column.distance".equals(key)) {
      return curState.isDistanceRun();
    } else if ("table.column.cyc".equals(key)) {
      return curState.isCycRun();
    } else if ("table.column.dcyc".equals(key)) {
      return curState.isDcycRun();
    } else if ("table.column.lsp".equals(key)) {
      return curState.isLspRun();
    } else if ("table.column.dip".equals(key)) {
      return curState.isDipRun();
    } else if ("table.column.ep".equals(key)) {
      return curState.isEpRun();
    } else if ("table.column.mq".equals(key)) {
      return curState.isMqRun();
    } else if ("table.column.nt".equals(key)) {
      return curState.isNtRun();
    } else if ("table.column.lcom".equals(key)) {
      return curState.isLcomRun();
    } else if ("table.column.nof".equals(key)) {
      return curState.isNofRun();
    } else if ("table.column.noa".equals(key)) {
      return curState.isNoaRun();
    } else {
      return false;
    }
  }

  private static int getActionIndex(String key) {
    if ("table.column.target".equals(key)) {
      return 0;
    } else if ("table.column.complexity".equals(key)) {
      return 1;
    } else if ("table.column.loc".equals(key)) {
      return 2;
    } else if ("table.column.ncloc".equals(key)) {
      return 3;
    } else if ("table.column.cloc".equals(key)) {
      return 4;
    } else if ("table.column.dc".equals(key)) {
      return 5;
    } else if ("table.column.numberOfParameters".equals(key)) {
      return 6;
    } else if ("table.column.executableStatements".equals(key)) {
      return 7;
    } else if ("table.column.weightedMethodsPerClass".equals(key)) {
      return 8;
    } else if ("table.column.rfc".equals(key)) {
      return 9;
    } else if ("table.column.depthInTree".equals(key)) {
      return 10;
    } else if ("table.column.numberOfChildren".equals(key)) {
      return 11;
    } else if ("table.column.not".equals(key)) {
      return 12;
    } else if ("table.column.nota".equals(key)) {
      return 13;
    } else if ("table.column.notc".equals(key)) {
      return 14;
    } else if ("table.column.note".equals(key)) {
      return 15;
    } else if ("table.column.ca".equals(key)) {
      return 16;
    } else if ("table.column.ce".equals(key)) {
      return 17;
    } else if ("table.column.instability".equals(key)) {
      return 18;
    } else if ("table.column.abstractness".equals(key)) {
      return 19;
    } else if ("table.column.distance".equals(key)) {
      return 20;
    } else if ("table.column.cyc".equals(key)) {
      return 21;
    } else if ("table.column.dcyc".equals(key)) {
      return 22;
    } else if ("table.column.lsp".equals(key)) {
      return 23;
    } else if ("table.column.dip".equals(key)) {
      return 24;
    } else if ("table.column.ep".equals(key)) {
      return 25;
    } else if ("table.column.mq".equals(key)) {
      return 26;
    } else if ("table.column.nt".equals(key)) {
      return 27;
    } else if ("table.column.lcom".equals(key)) {
      return 28;
    } else if ("table.column.nof".equals(key)) {
      return 29;
    } else if ("table.column.noa".equals(key)) {
      return 30;
    } else {
      return 0;
    }
  }

  public static String getDefaultKey(int index) {
    switch (index) {
      case 0:
        return "table.column.target";
      case 1:
        return "table.column.complexity";
      case 2:
        return "table.column.loc";
      case 3:
        return "table.column.ncloc";
      case 4:
        return "table.column.cloc";
      case 5:
        return "table.column.dc";
      case 6:
        return "table.column.numberOfParameters";
      case 7:
        return "table.column.executableStatements";
      case 8:
        return "table.column.weightedMethodsPerClass";
      case 9:
        return "table.column.rfc";
      case 10:
        return "table.column.depthInTree";
      case 11:
        return "table.column.numberOfChildren";
      case 12:
        return "table.column.ca";
      case 13:
        return "table.column.ce";
      case 14:
        return "table.column.instability";
      case 15:
        return "table.column.abstractness";
      case 16:
        return "table.column.distance";
      case 17:
        return "table.column.not";
      case 18:
        return "table.column.nota";
      case 19:
        return "table.column.notc";
      case 20:
        return "table.column.note";
      case 21:
        return "table.column.cyc";
      case 22:
        return "table.column.dcyc";
      case 23:
        return "table.column.lsp";
      case 24:
        return "table.column.dip";
      case 25:
        return "table.column.ep";
      case 26:
        return "table.column.mq";
      case 27:
        return "table.column.nt";
      case 28:
        return "table.column.lcom";
      case 29:
        return "table.column.nof";
      case 30:
        return "table.column.noa";
    }

    return "table.column.target";
  }

  void saveColumns() {
    for (int i = 0; i < METRICS; i++) {
      GlobalOptions.setOption("metric.column." + i, ((String) columnKeys.get(i)));
    }

    GlobalOptions.save();
  }

  public static ArrayList getColumnNames() {
    return columnNames;
  }

  public static ArrayList getDefaultColumnNames() {
    ArrayList result = new ArrayList(METRICS);
    for (int i = 0; i < METRICS; i++) {
      result.add(resLocalizedStrings.getString(getDefaultKey(i)));
    }

    return result;
  }

  public static int[] getActionIndexes() {
    int[] actionIndexes = new int[METRICS];
    for (int i = 0; i < METRICS; i++) {
      actionIndexes[i] = getActionIndex((String) columnKeys.get(i));
    }
    return actionIndexes;
  }

  public static int[] getDefaultActionIndexes() {
    int[] actionIndexes = new int[METRICS];
    for (int i = 0; i < METRICS; i++) {
      actionIndexes[i] = i;
    }
    return actionIndexes;
  }

  public char getMnemonic() {
    return 'M';
  }
}
