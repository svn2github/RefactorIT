/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.FilterDialog;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.JWordDialog;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.tree.MultilineRowTree;
import net.sf.refactorit.ui.treetable.PositionableTreeNode;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


public class FixmeScannerAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.FixmeScannerAction";
  public static final String NAME = "FIXME Scan";

  static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(FixmeScannerAction.class);

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.equals(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isPreprocessedSourcesSupported() {
    return true;
  }

  public String getKey() {
    return KEY;
  }

  public char getMnemonic() {
    return 'F';
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean run(final RefactorItContext context, final Object binObjectToOperate) {
    if (context.getState() == null || !(context.getState() instanceof FixmeState)) {
      FixmeState state = new FixmeState();
      if (!state.loadWords()) {
        state.restoreDefaultWords();
      }
      context.setState(state);

      if ("true".equals(GlobalOptions.getOption("misc.show.fixme.dialog"))) {
        if (showWordsDialog(context) == null) {
          return false;
        }
      }
    }

    try {
      new ActionInstance().run(
          context, RefactorItActionUtils.unwrapTarget(binObjectToOperate));
    } catch (SourceNotAvailableForItemException e) {
      DialogManager.getInstance().showNonSourcePathItemInfo(
          context, getName(),
          RefactorItActionUtils.unwrapTarget(binObjectToOperate));
    } catch (SearchingInterruptedException ex) {
    }

    // we don't change anything, aren't we?
    // I guess timestamp update will manage project reload itself, right?
    return false;
  }

  public String getName() {
    return NAME;
  }

  FixmeState showWordsDialog(IdeWindowContext context) {
    FixmeState state = (FixmeState) context.getState();

    JWordDialog dialog = new JWordDialog(
        context, resLocalizedStrings.getString("words.dialog.title"),
        state.words, true, FixmeState.DEFAULT_WORDS, "refact.fixmeScan.words");

    dialog.show();

    if (dialog.okPressed()) {
      state.words = dialog.getWords();
      state.saveWords();
      context.setState(state);

      return state;
    }

    return null;
  }

  class ActionInstance implements OptionsChangeListener {
    List sourcesToScan;

    boolean scanAllLines;

    int startLine;
    int stopLine;

    private MultilineRowTree tree;
    private Object binObjectToOperate;

    public void optionChanged(String key, String newValue) {
      checkColorAndFont();
    }

    public void optionsChanged() {
      checkColorAndFont();
    }

    private void checkColorAndFont() {
      if (tree != null) {
        tree.optionsChanged();
      }
    }

    private void findWhatToScan(Project project) throws
        SourceNotAvailableForItemException {
      if (this.binObjectToOperate instanceof BinPackage) {
        this.sourcesToScan = getSourcesToScanFrom((BinPackage)this.
            binObjectToOperate);
        this.scanAllLines = true;
      } else if (this.binObjectToOperate instanceof BinCIType) {
        this.sourcesToScan = getSourcesToScanFrom(
            (BinCIType) this.binObjectToOperate, project);
        this.scanAllLines = true;
      } else if (this.binObjectToOperate instanceof BinMethod) {
        this.sourcesToScan = getSourcesToScanFrom((BinMethod)this.
            binObjectToOperate);
        this.scanAllLines = false;
        this.startLine = ((BinMethod)this.binObjectToOperate).getStartLine();
        this.stopLine = ((BinMethod)this.binObjectToOperate).getEndLine();
      } else {
        this.sourcesToScan = project.getCompilationUnits();
        this.scanAllLines = true;
      }
    }

    public void run(
        final RefactorItContext context, final Object binObjectToOperate
    ) throws
        SourceNotAvailableForItemException,
        SearchingInterruptedException
    {
      final FixmeState state = (FixmeState) context.getState();

      this.binObjectToOperate = binObjectToOperate;

      findWhatToScan(context.getProject());

      tree = new MultilineRowTree(context);

      addSourceDisplayFeature(this.tree, context);

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          updateTreeContents(state);
        }
      }, true);

      ResultArea results = ResultArea.create(this.tree, context,
          FixmeScannerAction.this);
      results.setTargetBinObject(binObjectToOperate);
      BinPanel panel = BinPanel.getPanel(
          context, resLocalizedStrings.getString("tab.title"), results);

      ActionListener filterDialog = addFilterDialog(context, state, panel);

      panel.addToolbarButton(getWordsButton(context));

      panel.addToolbarButton(
          getTimestampButton((FixmeScannerTreeModel)this.tree.
          getOriginalDelegateModel(),
          state, filterDialog
          )
          );

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.fixmeScan");
    }

    private ActionListener addFilterDialog(
        final IdeWindowContext context, final FixmeState state, final BinPanel panel
    ) {
      final JCheckBox timestampSortedToggleButton = new JCheckBox(
          resLocalizedStrings.getString("sort.by.timestamp"),
          state.sortedByTimestamp);

      ActionListener result = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JComboBox comboBox = getTimestampFormatComboBox();

          if (!FilterDialog.showDialog(
              context, new JCheckBox[] {timestampSortedToggleButton},
              resLocalizedStrings.getString("filter.dialog.title"),
              "Timestamp format: ", comboBox)) {
            return;
          }

          ((TimestampFormat) comboBox.getSelectedItem()).save();

          state.sortedByTimestamp = timestampSortedToggleButton.isSelected();
          context.setState(state);
          updateTreeContents(state);
        }
      };

      panel.setFilterActionListener(result);

      return result;
    }

    JComboBox getTimestampFormatComboBox() {
      TimestampFormat[] timestampFormats = TimestampFormat.
          getAvailableFormatsSortedAlphabetically();
      JComboBox comboBox = new JComboBox(timestampFormats);
      TimestampFormat currentFormat = TimestampFormat.load();
      for (int i = 0; i < timestampFormats.length; i++) {
        if (currentFormat.equals(timestampFormats[i])) {
          comboBox.setSelectedIndex(i);
        }
      }

      return comboBox;
    }

    void updateTreeContents(FixmeState state) {
      this.tree.rememberExpansions();
      this.tree.rememberSelection();

      TreeModel treeModel = new FixmeScannerTreeModel(this.sourcesToScan,
          state.words, state.sortedByTimestamp, this.scanAllLines,
          this.startLine, this.stopLine);
      this.tree.setModel(treeModel);

      this.tree.restoreExpansions();
      this.tree.restoreSelection();
      tree.expandRow(0);
    }

    private AbstractButton getWordsButton(final IdeWindowContext context) {
      final ImageIcon iconWords = ResourceUtil
          .getIcon(FixmeScannerAction.class, "words.gif");

      JButton wordsButton = new JButton(iconWords);
      wordsButton.setMargin(new Insets(0, 0, 0, 0));

      wordsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          FixmeState state = showWordsDialog(context);
          if (state != null) {
            updateTreeContents(state);
          }
        }
      });

      wordsButton.setToolTipText(resLocalizedStrings.getString("words.tooltip"));

      return wordsButton;
    }

    private AbstractButton getTimestampButton(
        final FixmeScannerTreeModel model,
        final FixmeState state,
        final ActionListener filterDialogInvoker) {
      final ImageIcon iconTime = ResourceUtil
          .getIcon(FixmeScannerAction.class, "time.gif");

      JButton result = new JButton(iconTime);
      result.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Object[] options = {
              resLocalizedStrings.getString("ok"),
              resLocalizedStrings.getString("cancel"),
              "Timestamp Format..."
          };

          final int dialogResult = RitDialog.showOptionDialog(
              IDEController.getInstance().createProjectContext(),
              resLocalizedStrings.getString("timestamp.confirm"),
              resLocalizedStrings.getString("timestamp.confirm.title"),
              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
              options, options[0]);

          if (dialogResult == 2) {
            filterDialogInvoker.actionPerformed(e);
            return;
          }

          if (dialogResult != 0) {
            return;
          }

          try {
            Timestamper timestamper = new Timestamper(
                sourcesToScan, state.words, scanAllLines, startLine, stopLine);

            timestamper.applyTimestamp(
                Calendar.getInstance(), TimestampFormat.load().getDateFormat());
          } catch (IOException exeption) {
            DialogManager.getInstance().showCustomError(
                IDEController.getInstance().createProjectContext(),
                "Error editing file: " + exeption);
          }

          updateTreeContents(state);
        }
      });

      result.setToolTipText(resLocalizedStrings.getString("timestamp.tooltip"));
      result.setMargin(new Insets(0, 0, 0, 0));

      return result;
    }

    private void addSourceDisplayFeature(final JTree tree,
        final IdeWindowContext context) {
      tree.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          handleDoubleclick(e);
        }

        public void mousePressed(MouseEvent e) {
          //handleDoubleclick(e);
        }

        public void mouseReleased(MouseEvent e) {
          //handleDoubleclick(e);
        }

        public void handleDoubleclick(MouseEvent e) {
          if (e.getClickCount() >= 2
              && e.getModifiers() == MouseEvent.BUTTON1_MASK) {
            TreePath path = tree.getSelectionPath();
            //TreePath path = tree.getPathForLocation( e.getX(), e.getY() );

            if (path != null
                && path.getLastPathComponent() instanceof PositionableTreeNode) {
              PositionableTreeNode node = (PositionableTreeNode) path.
                  getLastPathComponent();
              context.show(node.getCompilationUnit(),
                  node.getLine(),
                  "true".equals(GlobalOptions.getOption("source.selection.highlight"))
                  );
            }
          }
        }
      });
    }

    private java.util.List getSourcesToScanFrom(BinPackage binPackage) throws
        SourceNotAvailableForItemException {
      ArrayList result = new ArrayList();

      Iterator allSources = binPackage.getProject().getCompilationUnits().iterator();

      if (!allSources.hasNext()) {
        throw new SourceNotAvailableForItemException();
      }

      while (allSources.hasNext()) {
        CompilationUnit source = (CompilationUnit) allSources.next();

        if (source.getPackage().isIdentical(binPackage)
            || isParentPackage(source.getPackage(), binPackage)) {
          result.add(source);
        }
      }

      return result;
    }

    private boolean isParentPackage(BinPackage childPackage,
        BinPackage parentPackage) {
      return childPackage.getQualifiedName().indexOf(parentPackage.
          getQualifiedName()) == 0;
    }

    private java.util.List getSourcesToScanFrom(BinCIType binCIType,
        Project project) throws SourceNotAvailableForItemException {
      ArrayList result = new ArrayList(1);

      if (binCIType.getCompilationUnit() == null) { // If array type
        String name = binCIType.getName();
        name = name.substring(2, name.length() - 1);
        BinTypeRef typeRef = project.getTypeRefForName(name);

        if (typeRef == null) {
          throw new SourceNotAvailableForItemException();
        }

        if (typeRef.getBinType() instanceof BinCIType) {
          result.add(typeRef.getBinType().getCompilationUnit());
        } else {
          throw new SourceNotAvailableForItemException();
        }
      } else {
        result.add(binCIType.getCompilationUnit());
      }

      return result;
    }

    private java.util.List getSourcesToScanFrom(BinMethod binMethod) throws
        SourceNotAvailableForItemException {
      ArrayList result = new ArrayList(1);
      CompilationUnit source = getCompilationUnit(binMethod);
      if (source == null) {
        throw new SourceNotAvailableForItemException();
      }
      result.add(source);
      return result;
    }

    private CompilationUnit getCompilationUnit(BinMethod method) {
      return method.getTopLevelEnclosingType().getCompilationUnit();
    }
  }


  private class SourceNotAvailableForItemException extends Exception {
    public SourceNotAvailableForItemException() {
      super();
    }
  }
}
