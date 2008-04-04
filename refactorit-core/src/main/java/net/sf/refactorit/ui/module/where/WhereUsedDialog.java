/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.query.usage.filters.BinInterfaceSearchFilter;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.query.usage.filters.BinPackageSearchFilter;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.query.usage.filters.SimpleFilter;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


public class WhereUsedDialog {
  final RitDialog dialog;

  boolean okPressed;

  JPanel filter;

  JButton buttonOk = new JButton("Ok");

  private JButton buttonCancel = new JButton("Cancel");
  private JButton buttonHelp = new JButton("Help");

  private Object bin;

  private CommonPanel commonPanel;
  private SearchFilter defaultFilter;
  private boolean wasMemberExpression;

  /**
   * @param defaultFilter null if no default options specified,
   * a proper {@link SearchFilter} instance for setting default choices in the
   * dialog.
   */
  public WhereUsedDialog(IdeWindowContext context, final Object bin,
      final SearchFilter defaultFilter) {
    this.wasMemberExpression = bin instanceof BinMemberInvocationExpression;
    this.bin = RefactorItActionUtils.unwrapTargetIfNotConstructor(bin);
    this.defaultFilter = defaultFilter;

    JPanel contentPanel = new JPanel();

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    dialog = RitDialog.create(context);
    dialog.setTitle(getShortDisplayName(this.bin));
    dialog.setContentPane(contentPanel);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, getHelpTopicId());

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp);
  }

  public void show() {
    this.okPressed = false;
    dialog.show();
  }

  public SearchFilter getFilter() {
    SearchFilter result
        = createSearchFilter(this.getFilterPanel(), this.getCommonPanel());
    GlobalOptions.save();

    return result;
  }

  public boolean isRunWithDefaultSettings() {
    return this.getCommonPanel().isRunWithDefaultSettings();
  }

  private SearchFilter createSearchFilter(final JPanel pane,
      final CommonPanel common) {
    SearchFilter result;
    if (bin instanceof BinVariable) {
      VariablePanel vp = (VariablePanel) pane;
      result = new BinVariableSearchFilter(vp.isRead(), vp.isWrite(),
          common.isShowDuplicates(), common.isGoToSingleUsage(),
          common.isRunWithDefaultSettings());
    } else if (bin instanceof BinConstructor) {
      result = new SimpleFilter(common.isShowDuplicates(),
          common.isGoToSingleUsage(), common.isRunWithDefaultSettings());
    } else if (bin instanceof BinMethod) {
      BinMethod method = (BinMethod) bin;
      if (method.isPrivate()) {
        result = new BinMethodSearchFilter(true, false, false, false,
            common.isShowDuplicates(), common.isGoToSingleUsage(), true,
            common.isRunWithDefaultSettings(), false);
      } else {
        MethodPanel mp = (MethodPanel) pane;
        result = new BinMethodSearchFilter(mp.isUsages(), mp.isOverridden(),
            mp.isSupertypes(), mp.isSubtypes(), common.isShowDuplicates(),
            common.isGoToSingleUsage(), mp.isImplementationSearch(),
            common.isRunWithDefaultSettings(), mp.isSkipSelf());
      }
    } else if (bin instanceof BinPackage) {
      PackagePanel pp = (PackagePanel) pane;
      result = new BinPackageSearchFilter(pp.isUsages(), pp.isImports(),
          pp.isStatements(), pp.isSubpackages(), pp.isSupertypes(),
          pp.isSubtypes(), pp.isNonJavaFiles(),
          common.isShowDuplicates(), common.isGoToSingleUsage(),
          common.isRunWithDefaultSettings());
    } else if (bin instanceof BinClass) {
      ClassPanel cp = (ClassPanel) pane;
      result = new BinClassSearchFilter(cp.isUsages(), cp.isStatements(),
          cp.isMethodUsages(), cp.isFieldUsages(), cp.isSupertypes(),
          cp.isSubtypes(), common.isShowDuplicates(), common.isGoToSingleUsage(),
          cp.isNonJavaFiles(), common.isRunWithDefaultSettings(),
          cp.isSkipSelf());
    } else if (bin instanceof BinInterface) {
      InterfacePanel ip = (InterfacePanel) pane;
      result = new BinInterfaceSearchFilter((BinInterface) bin, ip.isUsages(),
          ip.isStatements(), ip.isMethodUsages(), ip.isFieldUsages(),
          ip.isSupertypes(), ip.isSubtypes(), ip.isImplementors(),
          common.isShowDuplicates(), common.isGoToSingleUsage(),
          ip.isNonJavaFiles(), common.isRunWithDefaultSettings(),
          ip.isSkipSelf());
    } else {
      result = new SimpleFilter(common.isShowDuplicates(),
          common.isGoToSingleUsage(), common.isRunWithDefaultSettings());
    }

    return result;
  }

  private String getHelpTopicId() {
    if (bin instanceof BinVariable) {
      return "refact.whereUsed.filters.variable";
    } else if (bin instanceof BinConstructor) {
      return "refact.whereUsed.filters.simple";
    } else if (bin instanceof BinMethod) {
      BinMethod method = (BinMethod) bin;
      if (method.isPrivate()) {
        return "refact.whereUsed.filters.simple";
      } else {
        return "refact.whereUsed.filters.method";
      }
    } else if (bin instanceof BinPackage) {
      return "refact.whereUsed.filters.package";
    } else if (bin instanceof BinClass) {
      return "refact.whereUsed.filters.class";
    } else if (bin instanceof BinInterface) {
      return "refact.whereUsed.filters.interface";
    } else {
      return "refact.whereUsed.filters";
    }
  }

  private static String getShortDisplayName(final Object bin) {
    if (bin instanceof Object[]) {
      return "Filter Usages for several objects";
    } else if (bin instanceof BinMember) {
      return "Filter Usages for " + ((BinMember) bin).getName();
    } else if (bin instanceof BinPackage) {
      return "Filter Usages for " + ((BinPackage) bin).getQualifiedName();
    } else {
      return bin.toString();
    }
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel("Set filter entries below");
  }

  private JComponent createCenterPanel() {
    JPanel center = new JPanel(new BorderLayout());
    //center.setBorder( BorderFactory.createTitledBorder( "Factory Method Entry") );
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    center.setBorder(BorderFactory.createEtchedBorder());

    center.add(createMessagePanel(), BorderLayout.NORTH);
    final JPanel filterPanel = getFilterPanel();
    if (filterPanel != null) {
      center.add(filterPanel, BorderLayout.CENTER);
    }
    center.add(getCommonPanel(), BorderLayout.SOUTH);

    return center;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = false;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonCancel);
    
    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 60, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonHelp);
    buttonHelp.setNextFocusableComponent(filter);
    return downPanel;
  }

  public boolean isOkPressed() {
    return this.okPressed;
  }

  public void forceOkPressed() {
    this.okPressed = true;
    this.dialog.dispose();
  }

  private CommonPanel getCommonPanel() {
    if (commonPanel == null) {
      commonPanel = new CommonPanel(defaultFilter);
    }

    return commonPanel;
  }

  private JPanel getFilterPanel() {
    if (filter != null) {
      return filter;
    }

    if (bin instanceof BinVariable) {
      filter = new VariablePanel(buttonOk,
          (BinVariableSearchFilter) defaultFilter);
    } else if (bin instanceof BinConstructor) {
      filter = null;
    } else if (bin instanceof BinMethod) {
      BinMethod method = (BinMethod) bin;
      if (method.isPrivate()) {
        filter = null;
      } else {
        filter = new MethodPanel(buttonOk,
            (BinMethodSearchFilter) defaultFilter);
        if (defaultFilter == null) {
          if (this.wasMemberExpression) {
            ((MethodPanel) filter).setImplementationSearch(true);
          }
        }
      }
    } else if (bin instanceof BinPackage) {
      filter = new PackagePanel(buttonOk,
          (BinPackageSearchFilter) defaultFilter);
    } else if (bin instanceof BinClass) {
      filter = new ClassPanel(buttonOk, (BinClassSearchFilter) defaultFilter);
    } else if (bin instanceof BinInterface) {
      filter = new InterfacePanel(buttonOk,
          (BinInterfaceSearchFilter) defaultFilter);
    } else {
      filter = null;
    }

    if (filter != null) {
      filter.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          filter.transferFocus();
        }
      });
    }

    return filter;
  }
}
