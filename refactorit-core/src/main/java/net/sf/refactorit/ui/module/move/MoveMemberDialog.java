/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.move;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.ui.TypeInputPanel;
import net.sf.refactorit.ui.module.MoveDialog;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;


/**
 * Retrieves target type name and member list to move.
 *
 * @author Anton Safonov, Vadim Hahhulin
 */
public class MoveMemberDialog extends MoveDialog {
  private TypeInputPanel typeInputPanel;

  public MoveMemberDialog(ConflictResolver resolver, RefactorItContext context) {
    super(resolver, context, "Move Method/Field",
        "closeActionOfMoveMethod", "refact.movemember");

    Dimension d = dialog.getMaximumSize();
    dialog.setSize(d.width - 50, d.height - 150);
  }

  protected JComponent createHierarchyPanel() {
    JPanel classHierarchyPanel = new JPanel(new BorderLayout(3, 3));
    classHierarchyPanel.setBorder(
        BorderFactory.createTitledBorder("Select target"));

    List probableTargetClasses = resolver.getProbableTargetClasses();
    BinCIType nativeType = resolver.getNativeType();
    final MoveMemberClassModel classModel =
        new MoveMemberClassModel(context.getProject(),
        nativeType,
        probableTargetClasses);
    final BinTreeTable ttClass = new BinTreeTable(classModel, context);
    ttClass.setTableHeader(null);
    ttClass.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);
    JTree tree = ttClass.getTree();
    tree.setRootVisible(false);

    classModel.expandPath(tree);

    // expand also package where source class is

    // BinTypeRef typeRef = nativeType.getTypeRef();
    BinPackage typePackage = nativeType.getPackage();
    Assert.must(typePackage != null);

    ParentTreeTableNode selectedNode =
        ((BinTreeTableNode) classModel.getRoot()).findParent(typePackage, false);

    if (selectedNode != null) {
      TreePath path = new TreePath(selectedNode.getPath());
      tree.expandPath(path);
      ttClass.selectNode(selectedNode);

    } else {
      AppRegistry.getLogger(this.getClass()).debug("selected node==null");
    }

    addListenerForClassHierarchy(ttClass);

    JPanel qualifiedClassChooser = createTypeChooserPanel(nativeType);

    classHierarchyPanel.add(qualifiedClassChooser, BorderLayout.NORTH);
    classHierarchyPanel.add(new JScrollPane(ttClass), BorderLayout.CENTER);

    return classHierarchyPanel;

//    JPanel classHierarchyPanel = new JPanel(new BorderLayout());
//		classHierarchyPanel.setBorder(
//        BorderFactory.createTitledBorder("Target class"));
//
//
//
//    packageTreePanel=new PackageTreePanel(context,true,false,resolver.getNativeType().getTypeRef());
//    packageTreePanel.onShow();
//    addListenerForClassHierarchy(packageTreePanel.getTable());
//
//    //return packageTreePanel;
//    classHierarchyPanel.add(packageTreePanel);
//    return classHierarchyPanel;
  }

  void setSelectedClassFrom(String qClassName) {
    BinTypeRef typeRef = context.getProject().findTypeRefForName(qClassName);

    if ( typeRef != null && typeRef.getBinCIType() != null ) {
      //net.sf.refactorit.common.util.AppRegistry.getLogger(MoveMemberDialog.class)
      //.debug("updating target class to : "+qClassName);

      setSelectedClass(typeRef.getBinCIType());
    } else {
      buttonOk.setEnabled(false);
    }
  }

  /**
   */
  private JPanel createTypeChooserPanel(BinCIType nativeType) {
    String qualifiedName = nativeType.getPackage().getQualifiedName();

    typeInputPanel = new TypeInputPanel(context,qualifiedName);
    typeInputPanel.addTypeChangedListener(new TypeInputPanel.TypeChangedListener() {

      public void onChange(BinTypeRef type) {

        setSelectedClassFrom(type==null?"":type.getQualifiedName());

      }
    }
    );


    return typeInputPanel;

  }

  private void addListenerForClassHierarchy(BinTreeTable table) {
    table.getTree().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        BinTreeTableNode node = (BinTreeTableNode) e.getPath().
            getLastPathComponent();
        if (!(node.getBin() instanceof BinCIType)) {
          return;
        }

        BinCIType bin = (BinCIType) node.getBin();

//        BinCIType bin=null;
//        BinTypeRef typeRef = packageTreePanel.getTypeRef();
//        if ( typeRef != null ) {
//          bin=typeRef.getBinCIType();
//        }

        if (bin != null) {
          setSelectedClassName(bin);

//          if (bin == resolver.getNativeType()) {
//            buttonOK.setEnabled(false);
//          }
        } else {
          setSelectedClass(null);
          buttonOk.setEnabled(false);
        }
      }

      private void setSelectedClassName(BinCIType bin) {
        typeInputPanel.setSelectedType(bin.getQualifiedName());
      }
    });
  }
}
