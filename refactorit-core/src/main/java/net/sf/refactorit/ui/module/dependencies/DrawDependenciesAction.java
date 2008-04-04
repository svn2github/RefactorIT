/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.dependency.GraphPanel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.FilterDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;

import javax.swing.JCheckBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class DrawDependenciesAction extends DependenciesAction {
  public static final String KEY = "refactorit.action.DrawDependenciesAction";
  public static final String NAME = "Draw Dependencies";

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    return true;
  }

  public boolean isReadonly() {
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
   * @param context context of the refactoring (also provides us current Project)
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    Assert.must(context != null,
        "Attempt to pass NULL context into DrawDependenciesAction.run()");
    Assert.must(object != null,
        "Attempt to pass NULL object into DrawDependenciesAction.run()");

    final Object target = unwrapTarget(object, context.getProject());
    final List listTarget = new ArrayList();
    if (target instanceof Object[]) {
      listTarget.addAll(Arrays.asList((Object[]) target));
    } else {
      listTarget.add(target);
    }

    GraphPanel glPanel = new GraphPanel(context, listTarget);
    ResultArea component = ResultArea.create(glPanel, context, this);
    component.setTargetBinObject(listTarget);
    glPanel.setHolder(component);
    BinPanel panel = BinPanel.getPanel(context, "Draw Dependencies", component);
    //panel.setFilterActionListener(glPanel.getRebuildActionListener());
    panel.setDefaultHelp("refact.draw_dependencies");
    createFilterDialog(context, panel);

    if (listTarget.size() == 1) {
      DialogManager.getInstance().showInformation(
          context, "draw_dependencies.multitarget");
    } else {
      DialogManager.getInstance().showInformation(
          context, "draw_dependencies.node_actions");
    }

    return false;
  }

  protected Object unwrapTarget(final Object object, final Project project) {
    Object target;
    if (object instanceof BinMemberInvocationExpression) {
      target = ((BinMemberInvocationExpression) object).getMember().getOwner().
          getBinCIType();
    } else {
      target = super.unwrapTarget(object, project);
    }

    return target;
  }

  private void createFilterDialog(
      final IdeWindowContext context, final BinPanel panel
  ) {
    final JCheckBox ignoreJdk = new JCheckBox(resLocalizedStrings.getString(
        "ignore-jdk-packages"));
    ignoreJdk.setToolTipText(resLocalizedStrings.getString(
        "ignore-jdk-packages"));
    ignoreJdk.setSelected(GlobalOptions.getOptionAsBoolean("dependencies-ignore-jdk-packages", true));

    final JCheckBox ignoreBinary = new JCheckBox(resLocalizedStrings.getString(
        "ignore-binary-packages"));
    ignoreBinary.setToolTipText(resLocalizedStrings.getString(
        "ignore-binary-packages"));
    ignoreBinary.setSelected(GlobalOptions.getOptionAsBoolean("dependencies-ignore-binary-packages", true));

    panel.setFilterActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!FilterDialog.showDialog(
            context, new JCheckBox[] {ignoreJdk, ignoreBinary},
            resLocalizedStrings.getString("draw_dependencies.filter.title"))) {
          return;
        }

        GlobalOptions.setOption("dependencies-ignore-jdk-packages", "" + ignoreJdk.isSelected());
        GlobalOptions.setOption("dependencies-ignore-binary-packages", "" + ignoreBinary.isSelected());
        GlobalOptions.save();
        panel.invokeReRun();
      }
    });
  }
}
