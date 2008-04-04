/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class DependenciesModel extends BinTreeTableModel {
//  private static ResourceBundle resLocalizedStrings
//      = ResourceUtil.getBundle(DependenciesAction.class);

  public DependenciesModel(final Project project, final Object target,
      final ResultFilter resultFilter) {
    super(new BinTreeTableNode(target, false));

    final BinTreeTableNode root = (BinTreeTableNode) getRoot();
    root.setDisplayName(root.getDisplayName()
        + " depends on:"
//        + " " + resLocalizedStrings.getString("tree.root.name")
    );

    List invocations = collectDependencies(target, project);

    if (invocations.size() > 7000) {
      int res = DialogManager.getInstance().showCustomYesNoQuestion(
          IDEController.getInstance().createProjectContext(),
          "Too many dependencies",
          "Too many dependencies found.\n"
          + "Showing them may take too much time and memory resources.\n"
          + "Do you want to continue?",
          DialogManager.NO_BUTTON);
      if (res == DialogManager.NO_BUTTON) {
        root.setHidden(true);
        return;
      }
    }

    Set filter = new HashSet();
    if (resultFilter != null) {
      if (resultFilter.getTarget() instanceof Object[]) {
        filter.addAll(Arrays.asList((Object[]) resultFilter.getTarget()));
      } else {
        filter.add(resultFilter.getTarget());
      }
    }

    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData data = (InvocationData) invocations.get(i);

      BinTreeTableNode node = null;
      Object item = data.getWhat();
      if (item instanceof BinMember) {
        Object realType = item;
        while ((realType = ((BinMember) realType).getOwner()) != null) {
          realType = ((BinTypeRef) realType).getBinCIType();
          item = realType;
        }
      }

      if (item instanceof BinArrayType) {
        BinTypeRef typeRef = ((BinArrayType) item).getArrayType();
        if (typeRef.isPrimitiveType()) {
          if (Assert.enabled) {
            System.err.println("Trying to add primitive type to the tree: "
                + typeRef);
          }
          continue;
        }
        node = (BinTreeTableNode) root.findParent(
            typeRef.getPackage(), true);
        item = typeRef.getBinType();
      } else if (item instanceof BinCIType) {
        node = (BinTreeTableNode) root.findParent(((BinCIType) item).getPackage(), true);
      } else if (item instanceof BinTypeRef) {
        node = (BinTreeTableNode) root.findParent((BinTypeRef) item, true);
        item = ((BinTypeRef) item).getBinCIType();
      } else if (item instanceof CompilationUnit) {
        final BinPackage pack = ((CompilationUnit) item).getPackage();
        final List independentTypes
            = ((CompilationUnit) item).getIndependentDefinedTypes();
        if (independentTypes.size() == 1) {
          node = (BinTreeTableNode) root.findParent(
              (BinTypeRef) independentTypes.get(0), true);
        } else if (pack != null) {
          node = (BinTreeTableNode) root.findParent(pack, true);
        } else {
          node = root;
        }
      } else {
        Assert.must(false, "Unsupported member in DependenciesModel: "
            + item.getClass().getName());
      }

      if (!isPassesResultFilter(filter, data)) {
        continue;
      }

      if (item instanceof CompilationUnit) {
        if (!resultFilterAccepts((CompilationUnit) item)) {
          continue;
        }
      } else if (item instanceof BinCIType) {
        if (!resultFilterAccepts((BinCIType) item)) {
          continue;
        }
      } else {
        Assert.must(false, "Unsupported item in DependenciesModel: "
            + item.getClass().getName());
      }

      Object location = data.getWhere();

      CompilationUnit source = null;

      // This makes the node have the correct icon -- BinTreeTable does not show an
      // icon for BinTypeRefs.
      if (location instanceof BinTypeRef) {
        location = ((BinTypeRef) location).getBinCIType();
      }

      if (location instanceof BinCIType) {
        source = ((BinCIType) location).getCompilationUnit();
      } else if (location instanceof BinMember) {
        source = ((BinMember) location).getCompilationUnit();
      } else if (location instanceof CompilationUnit) {
        source = (CompilationUnit) location;
      }

      if (source == null) {
        Assert.must(false, "Unsupported item in DependenciesModel: "
            + item.getClass().getName());
      }

      BinTreeTableNode bn = node.findChildByType(item);

      if (bn == null) {
        bn = new BinTreeTableNode(item, false);
        bn.addAst(data.getWhereAst());
        bn.setSourceHolder(source);
        node.addChild(bn);
      }

      BinTreeTableNode locationNode
          = bn.findChildByType(location, data.getLineNumber());
      if (locationNode == null) {
        locationNode = new DependenciesNode(location, true);
        locationNode.setSourceHolder(source);
        bn.addChild(locationNode);
      }

      locationNode.addAst(data.getWhereAst());
    }

    Iterator topNodes = ((BinTreeTableNode) getRoot()).getChildren().iterator();
    while (topNodes.hasNext()) {
      BinTreeTableNode node = (BinTreeTableNode) topNodes.next();
      if (node.getChildCount() == 0) {
        ((BinTreeTableNode) getRoot()).removeChild(node);
      }
    }

    ((BinTreeTableNode) getRoot()).sortAllChildren();
    ((BinTreeTableNode) getRoot()).reflectLeafNumberToParentName();
  }

  private boolean isPassesResultFilter(final Set filter,
      final InvocationData data) {
    if (filter.size() == 0) {
      return true;
    }

    // this is for Draw Dependencies
    Iterator it = filter.iterator();
//    boolean ok = false;
    while (it.hasNext()) {
      Object filterResultObject = it.next();
      if (filterResultObject instanceof Scope
          && data.getWhat() instanceof Scope) {
        if (((Scope) filterResultObject).contains((Scope) data.getWhat())) {
          return true;
        }
      }
//      String filterResultObjectName = BinFormatter.formatQualified(filterResultObject);
//      String itemName = BinFormatter.formatQualified(data.getWhat());
//      if (itemName.length() == 0) {
//        // special case - default package
//        if (filterResultObject instanceof BinPackage
//            && ((BinPackage) filterResultObject).isDefaultPackage()) {
//          ok = true;
//          break;
//        } else if (filterResultObject instanceof BinMember
//            && ((BinMember) filterResultObject).getPackage()
//            .isDefaultPackage()) {
//          ok = true;
//          break;
//        }
//      } else if (filterResultObjectName.length() == 0) {
//        // special case - default package
//        final BinItem what = data.getWhat();
//        if (what instanceof BinPackage
//            && ((BinPackage) what).isDefaultPackage()) {
//          ok = true;
//          break;
//        } else if (what instanceof BinMember
//            && ((BinMember) what).getPackage()
//            .isDefaultPackage()) {
//          ok = true;
//          break;
//        }
//      } else if (itemName.startsWith(filterResultObjectName)) {
//        ok = true;
//        break;
//      }
    }

//    return ok;
    return false;
  }

  public static List collectDependencies(final Object target,
      final Project project) {
    List result = new ArrayList();

    if (target instanceof Object[]) {
      for (int i = 0, max = ((Object[]) target).length; i < max; i++) {
        result.addAll(acceptIndexer(((Object[]) target)[i]));
      }
    } else {
      result.addAll(acceptIndexer(target));
    }

    return result;
  }

  private static List acceptIndexer(final Object target) {
    final ManagingIndexer supervisor = new ManagingIndexer();

    if (target instanceof Project) {
      new DependenciesIndexer(supervisor, null);
    } else if (target instanceof BinItem) {
      new DependenciesIndexer(supervisor, (BinItem) target);
    } else if (Assert.enabled) {
      System.err.println("unhandled target: " + target + " - "
          + target.getClass());
    }

    ((BinItemVisitable) target).accept(supervisor);

    return supervisor.getInvocations();
  }

  private boolean resultFilterAccepts(CompilationUnit compilationUnit) {
    return resultFilterAcceptsName(compilationUnit.getSource().getRelativePath());
  }

  private boolean resultFilterAccepts(BinCIType binType) {
    return resultFilterAcceptsName(binType.getQualifiedName());
  }

  private boolean resultFilterAcceptsName(String qualifiedName) {
    if (GlobalOptions.getOptionAsBoolean("dependencies-ignore-jdk-packages", true)) {
      return
          (!qualifiedName.startsWith("java.")) &&
          (!qualifiedName.startsWith("javax."));
    } else {
      return true;
    }
  }

  /* @used */
  private DependenciesModel(Object root) {
    super(root);
  }

  public int getColumnCount() {
    return 3;
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    // FIXME: i18n
    switch (column) {
      case 0:
        return "Location";
      case 1:
        return "Line";
      case 2:
        return "Source";
      default:
    }
    return null;
  }

  public Class getColumnClass(int col) {
    switch (col) {
      case 1:
        return Integer.class;
      case 2:
        return String.class;
      default:
        return super.getColumnClass(col);
    }
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {
    if (node instanceof BinTreeTableNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          return ((BinTreeTableNode) node).getLineNumber() + NUMBER_PADDING;
        case 2:
          return ((BinTreeTableNode) node).getLineSource();
      }
    }

    return null;
  }

}
