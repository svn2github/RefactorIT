/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.errors;


import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ErrorTabNode;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcesWithErrors;

import javax.swing.JTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ErrorsTab {
  static BinPanel panel;
  static BinTreeTableModel model;

  public static void remove() {
    if (panel != null) {
      SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
        public void run() {
          ErrorsTab.panel.close();
          ErrorsTab.panel = null;
        }
      });
    }
  }

  public static void addNew(RefactorItContext context) {
    try {
      if (!context.getProject().getProjectLoader().getErrorCollector().
          hasUserFriendlyErrors()
          && !context.getProject().getProjectLoader().getErrorCollector().
          hasUserFriendlyInfos()) {
        return;
      }

      model = new ErrorModel(
          context.getProject().getProjectLoader().getErrorCollector().
          getUserFriendlyErrors(),
          context.getProject().getProjectLoader().getErrorCollector().
          getUserFriendlyInfos());
      ((BinTreeTableNode) model.getRoot()).reflectLeafNumberToParentName();
      ((BinTreeTableNode) model.getRoot()).sortAllChildren();





      //showAddToIgnoreDialog(context, model);





      final BinTreeTable table = new BinTreeTable(model, context) {
        public List getApplicableActions
            (final UITreeNode[] nodes, final IdeWindowContext ctx) {

          for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getType() != UITreeNode.NODE_SOURCE) {
              return Collections.EMPTY_LIST;
            }
          }
          List result = new ArrayList();

          result.add(new IgnoreDirAction());
          result.add(new IgnoreFileAction());
          return result;
        }
      };
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      ErrorsTab.remove();
      panel = BinPanel.getPanel(context, "Errors", table);
      table.getColumnModel().getColumn(0).setPreferredWidth(1024);
      table.smartExpand();
    } catch (Exception e) {
      System.err.println("ErrorsTab#addNew - context: " + context);
      e.printStackTrace(System.err);
    }
  }

  public static void showAddToIgnoreDialog
      (final RefactorItContext context, final BinTreeTableModel model) {
    //System.out.println("Trying to show dialog");

    if (model == null) {
      return;
    }

    SourcesWithErrors badSources = SourcesWithErrors.getInstance();

    badSources.addSources
        (((BinTreeTableNode) model.getRoot()).getAllChildren());

    HashSet promptToIgnore = badSources.getConstantErrors();

    if (promptToIgnore.size() > 0) {
      badSources.clear();
      ArrayList li = new ArrayList();
      li.addAll(promptToIgnore);

      AddToIgnoreDialog dialog = new AddToIgnoreDialog(context, li);
      dialog.show();

      if (dialog.isOkPressed()) {
        ArrayList foldersToIgnore = dialog.getSourcesToIgnore();
        ArrayList filesToIgnore = dialog.getFilesToIgnore();

        boolean wasActionRun = false;

        if (foldersToIgnore.size() > 0) {
          RefactorItActionUtils.run(new IgnoreSourceAction(), context,
              foldersToIgnore.toArray());

          wasActionRun = true;
        }

        if (filesToIgnore.size() > 0) {
          RefactorItActionUtils.run(new IgnoreFileAction(), context,
              filesToIgnore.toArray());

          wasActionRun = true;
        }

        if (wasActionRun) {
          context.rebuildAndUpdateEnvironment();

          //otherwise errors tab will be built with old, redundant model
          //return;
        }
      }
    }
  }

  public static BinTreeTableModel getModel() {
    return model;
  }

  private abstract static class IgnoreAction extends AbstractRefactorItAction {
    public boolean isMultiTargetsSupported() {
      return true;
    }

    public boolean isReadonly() {
      return false;
    }

    public boolean run(RefactorItContext context, Object object) {
      Set ignorePaths = new HashSet();
      if (object instanceof Object[]) {
        Object[] nodes = (Object[]) object;
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] instanceof CompilationUnit) {
            ignorePaths.add(getIgnoredSource((CompilationUnit) nodes[i]));
          }
        }
      } else {
        if (object instanceof CompilationUnit) {
          ignorePaths.add(getIgnoredSource((CompilationUnit) object));
        }
      }
      IDEController.getInstance().addIgnoredSources(context.getProject(),
          (Source[]) ignorePaths.toArray(Source.NO_SOURCES));
      return true;
    }

    protected abstract Source getIgnoredSource(CompilationUnit u);
  }


  static final class IgnoreDirAction extends IgnoreAction {
    public String getKey() {
      return "refactorit.errors.action.ignore_package";
    }

    public String getName() {
      return "Ignore files in this folder";
    }

    public boolean run(RefactorItContext context, Object object) {
      Set ignorePaths = new HashSet();
      if (object instanceof Object[]) {
        Object[] nodes = (Object[]) object;
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] instanceof CompilationUnit) {
            ignorePaths.add(((CompilationUnit) nodes[i]).getSource().getParent());
          }
        }
      } else {
        if (object instanceof CompilationUnit) {
          ignorePaths.add(((CompilationUnit) object).getSource().getParent());
        }
      }
      IDEController.getInstance().addIgnoredSources(context.getProject(),
          (Source[]) ignorePaths.toArray(Source.NO_SOURCES));
      return true;
    }

    protected Source getIgnoredSource(CompilationUnit u) {
      return u.getSource().getParent();
    }

    /**
     * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
     */
    public boolean isAvailableForType(Class type) {
      if (BinSelection.class.equals(type)) {
        return true;
      } else {
        throw new UnsupportedOperationException("method not implemented yet");
      }
    }
  }


  static final class IgnoreFileAction extends IgnoreAction {
    public String getKey() {
      return "refactorit.errors.action.ignore_source";
    }

    public String getName() {
      return "Ignore this source file";
    }

    protected Source getIgnoredSource(CompilationUnit u) {
      return u.getSource();
    }

    /**
     * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
     */
    public boolean isAvailableForType(Class type) {
      if (BinSelection.class.equals(type)) {
        return true;
      } else {
        throw new UnsupportedOperationException("method not implemented yet");
      }
    }
  }


  static final class IgnoreSourceAction extends IgnoreAction {
    public String getKey() {
      return "refactorit.errors.action.ignore_source";
    }

    public String getName() {
      return "Ignore this folder";
    }

    public boolean run(RefactorItContext context, Object object) {
      Set ignorePaths = new HashSet();
      if (object instanceof Object[]) {
        Object[] nodes = (Object[]) object;
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] instanceof Source) {
            ignorePaths.add((Source) nodes[i]);
          }
        }
      } else {
        if (object instanceof Source) {
          ignorePaths.add((Source) object);
        }
      }
      IDEController.getInstance().addIgnoredSources(context.getProject(),
          (Source[]) ignorePaths.toArray(Source.NO_SOURCES));
      return true;
    }

    //never used
    protected Source getIgnoredSource(CompilationUnit u) {
      return null;
    }

    /**
     * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
     */
    public boolean isAvailableForType(Class type) {
      if (BinSelection.class.equals(type)) {
        return true;
      } else {
        throw new UnsupportedOperationException("method not implemented yet");
      }
    }
  }


  private static class ErrorModel extends BinTreeTableModel {
    public ErrorModel(Iterator errors, Iterator infos) {
      super(new BinTreeTableNode("Errors"));
      populate(errors);
      populate(infos);
    }

    public void populate(Iterator errors) {
      while (errors.hasNext()) {
        UserFriendlyError o = (UserFriendlyError) errors.next();

        BinTreeTableNode parent = null;
        CompilationUnit compUnit = o.getCompilationUnit();

        if (compUnit == null) {
          parent = (BinTreeTableNode) getRoot();
        } else {
          parent = ((BinTreeTableNode) getRoot()).findChildByType(compUnit);
          if (parent == null) {
            parent = new BinTreeTableNode(compUnit, false);
            if (compUnit.getSource().getASTTree() == null
                || compUnit.getSource().getASTTree().getASTCount() == 0) {
              parent.setLine(0); // to escape erroneous "smart" autodetection
            }
            ((BinTreeTableNode) getRoot()).addChild(parent);
          }
        }

        BinTreeTableNode child = new ErrorTabNode(o);
        parent.addChild(child);
      }
    }
  }
}
