/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.errors;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.JConfirmationDialog;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ErrorTreeTable;
import net.sf.refactorit.ui.treetable.ErrorTreeTableNode;
import net.sf.refactorit.vfs.Source;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Aleksei sosnovski
 */
public class AddToIgnoreDialog {
  static RefactorItContext context;

  List errorNodes;

  BinTreeTableModel model;

  JConfirmationDialog dialog;

  ArrayList sourcesToIgnore;
  ArrayList filesToIgnore;

  boolean isOkPressed;


  public AddToIgnoreDialog(RefactorItContext context, List errorNodes) {
    this.context = context;
    this.errorNodes = errorNodes;

    model = new ErrorModel(errorNodes, context);

    String title = "Add to ignored source";
    String help = "Select sources you want to make ignored and press OK";
    String helpID = "make_ignored";
    String description = "Make sources with errors ignored";

    ErrorTreeTable table = new ErrorTreeTable(model, context);
    dialog = new JConfirmationDialog(title, help, table, context, description,
        helpID, new Dimension(640, 480), false);

    sourcesToIgnore = new ArrayList();
    filesToIgnore = new ArrayList();
  }

  public void show() {
    PromptToIgnoreDialog dlg =
        new PromptToIgnoreDialog(context, errorNodes.size());

    dlg.setDefaultButton(false);
    if (dlg.display() == DialogManager.YES_BUTTON) {

      dialog.show();

      if (dialog.isOkPressed()) {
        isOkPressed = true;

        ArrayList nodes = ((BinTreeTableNode) model.getRoot()).getAllChildren();

        for (int i = 0, max = nodes.size(); i < max; i++) {
          addToIgnore((BinTreeTableNode) nodes.get(i));
        }
      }
    } else {
      isOkPressed = false;
    }
  }

  private void addToIgnore(BinTreeTableNode node) {
    if (!node.isSelected()) {
      for (Iterator iter = node.getAllChildren().iterator(); iter.hasNext(); ) {
        addToIgnore((BinTreeTableNode) iter.next());
      }
      return;
    } else {
      if (node.getBin() instanceof CompilationUnit) {
        filesToIgnore.add(node.getBin());
      } else

      if (node.getBin() instanceof Source) {
        sourcesToIgnore.add(node.getBin());
      }
    }
  }

  public ArrayList getSourcesToIgnore() {
    return this.sourcesToIgnore;
  }

  public ArrayList getFilesToIgnore() {
    return this.filesToIgnore;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }


  private static class ErrorModel extends BinTreeTableModel {
    public ErrorModel(List errorNodes, RefactorItContext context) {
      super(new BinTreeTableNode("Errors"));
      populate(errorNodes, context);
    }

    private void populate(List errorNodes, RefactorItContext context) {
      BinTreeTableNode root = (BinTreeTableNode) getRoot();

      TreeBuilder treeBuilder = new TreeBuilder(context.getProject());

      ArrayList rootChildren =
          treeBuilder.getRootChildren(errorNodes);

      for (int i = 0; i < rootChildren.size(); i++) {
        root.addChild(rootChildren.get(i));
      }

      root.setSelected(false);
    }
  }
}

/**
 *
 * @author Aleksei sosnovski
 */
class TreeBuilder {
  Project project;

  ArrayList rootChildren;

  HashMap allNodes;

  TreeBuilder(Project project) {
    this.project = project;
    rootChildren = new ArrayList();

    Source[] rootSources = project.getPaths().getSourcePath().getRootSources();

    allNodes = new HashMap();

    for (int i = 0; i < rootSources.length; i++) {
      ErrorTreeTableNode node = new ErrorTreeTableNode(rootSources[i]);
      rootChildren.add(node);
      allNodes.put(node.getBin(), node);
    }
  }

  ArrayList getRootChildren(List nodes) {
    createBranches(nodes);
    cleanRootChildren();

    return rootChildren;
  }

  private void createBranches(List nodes) {
    for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
      ErrorTreeTableNode node = (ErrorTreeTableNode) iter.next();
      Source src = ((CompilationUnit)node.getBin()).getSource();
      addBranch(src, node);
    }
  }

  private ErrorTreeTableNode addBranch
      (Source src, ErrorTreeTableNode node) {
    if (node == null) {
      node = new ErrorTreeTableNode(src);
      node.setDisplayName(src.getName());
    }
    if (allNodes.containsKey(src)) {
      return (ErrorTreeTableNode) allNodes.get(src);
    } else {
      //we don't need files there, as file nodes will never have children
      if (src.isDirectory()) {
        allNodes.put(src, node);
      }
      if (src.getParent() == null) {
    	rootChildren.add(node);
      } else
      if (!allNodes.containsKey(src.getParent())) {
        addBranch(src.getParent(), null).addChild(node);
      } else {
        ((ErrorTreeTableNode) allNodes.get(src.getParent())).addChild(node);
      }
    }
    return node;
  }

  private void cleanRootChildren() {
    for (int i = 0; i < rootChildren.size(); i++) {
      BinTreeTableNode node = (BinTreeTableNode) rootChildren.get(i);
      Object o = node.getBin();

      if (o instanceof Source) {
        Source src = (Source) o;

        if (src.isDirectory() && node.getAllChildren().size() < 2) {
          rootChildren.remove(node);
          rootChildren.addAll(node.getAllChildren());
          i = -1;
        } else {
          node.setDisplayName(src.getAbsolutePath());
        }
      }
    }
  }
}
