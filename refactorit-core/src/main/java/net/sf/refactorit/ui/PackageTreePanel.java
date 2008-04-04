/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;



import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPane;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.Dimension;


/**
 *
 * <p>Title: </p>
 * <p>Description: Displays package tree where user can select class</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Anton
 * @author Risto
 * @author Tonis
 * @version 1.0
 */
public class PackageTreePanel extends AbstractClassSearchPanel {
  private BinTreeTable table;
  private PackageModel model;
  TreeSelectionListener listener;

  private BinPane left;

  protected BinTreeTableNode selectedNode;

  public BinTreeTable getTable() {
    return table;
  }

  public PackageTreePanel(final RefactorItContext context,
      final boolean includeTypes,
      final boolean includeClasspath,
      final Object openItem) {
    this(context, includeTypes, includeClasspath, false, false, openItem);
  }

  public PackageTreePanel(final RefactorItContext context,
      final boolean includeTypes,
      final boolean includeClasspath,
      final boolean includePrimitives,
      final boolean includeVoid,
      final Object openItem) {
    final Project project = context.getProject();

    model = new PackageModel(project, openItem, null,
        new PackageModel.ModelOptions(includeTypes, includeClasspath,
        includePrimitives, includeVoid));

    //    BinTreeTableNode node = model.getOpenNode();
    //node.findChildByType(className);
    //table.selectNode(node);

    table = new BinTreeTable(this.model, context);
    table.setTableHeader(null);

    listener = new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        BinTreeTableNode node = null;

        if (path != null) {
          node = (BinTreeTableNode) path.getLastPathComponent();
        }

        selectedNode = node;
      }
    };

    table.getTree().addTreeSelectionListener(listener);

    left = new BinPane();

    left.setComponent(ResultArea.create(table, context, null));
    left.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createEtchedBorder())
        );

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 2, 3),
        BorderFactory.createEtchedBorder())
        );

    center.add(left, BorderLayout.CENTER);

    JPanel contentPane = this;

    contentPane.setLayout(new BorderLayout());
    contentPane.add(center, BorderLayout.CENTER);
    //contentPane.add( createButtonsPanel(), BorderLayout.SOUTH );

    contentPane.setPreferredSize(new Dimension(595, 380));
  }

  public void onShow() {
    BinTreeTableNode node = model.getOpenNode();
    table.selectNode(node);
  }

  public BinTypeRef getTypeRef() {
    if (selectedNode instanceof PackageModel.TypeRefNode) {
      return ((PackageModel.TypeRefNode) selectedNode).getTypeRef();
    }

    if (selectedNode != null) {
      Object bin = selectedNode.getBin();
      if (bin instanceof BinType) {
        return ((BinType) bin).getTypeRef();
      }

      AppRegistry.getLogger(this.getClass()).debug(
          "unknown element type, bin: " + bin.getClass().getName());
    }

    return null;
  }

  public void dispose() {
    table.getTree().removeTreeSelectionListener(listener);
//    this.removeListeners(this.ok, ActionListener.class);
//    this.removeListeners(this.cancel, ActionListener.class);
    this.left.setComponent(null);
    //dialog.dispose();
  }

  public void doOk() {
    dispose();
  }

  public void doCancel() {
    selectedNode = null;
    dispose();
  }
}
