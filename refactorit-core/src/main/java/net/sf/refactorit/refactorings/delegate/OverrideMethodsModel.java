/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public class OverrideMethodsModel extends BinTreeTableModel {
  private BinClass targetClass;
  private boolean isEmpty;

  public OverrideMethodsModel(BinClass cls) {
    super(new BinTreeTableNode(cls, false));
    this.targetClass = cls;

    initChildren();
  }

  private void initChildren() {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();

    BinClass clazz = (BinClass) root.getBin();

    BinMethod[] overriddables =
        OverrideMethodsRefactoring.getOverridableMethods(clazz);

    // group by subclass
    MultiValueMap map = new MultiValueMap();

    for (int i = 0; i < overriddables.length; i++) {
      map.putAll(overriddables[i].getOwner(), overriddables[i]);
    }

    isEmpty = (overriddables.length == 0);

    List keys = new ArrayList(map.keySet());

    Collections.sort(keys, new TypeRefHierarchyComparator());

    for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
      BinTypeRef item = (BinTypeRef) iter.next();

//      if (Assert.enabled) {
//        Assert.must(!item.equals(targetClass.getOwner()),
//            "overridable should be only superclass methods!");
//      }
      BinTreeTableNode node = new BinTreeTableNode(item);

      root.addChild(node);

      addMethodNodes(node, map.get(item));
    }
  }

  public int getColumnCount() {
    return 1;
  }

  public String getColumnName(int column) {
    return "Select Methods";
  }

  public List getSelectedList() {
    List result = new ArrayList();

    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();

    for (int i = 0, max = rootNode.getChildCount(); i < max; i++) {
      BinTreeTableNode subclassNode = (BinTreeTableNode) rootNode.getChildAt(i);
//      if (!subclassNode.isSelected()) {
//        continue;
//      }

      for (int j = 0, maxJ = subclassNode.getChildCount(); j < maxJ; ++j) {
        BinTreeTableNode methodNode = (BinTreeTableNode) subclassNode.
            getChildAt(j);

        if (methodNode.isSelected()) {
          result.add(methodNode.getBin());
        }
      }
    }

    // FIXES tests ;)
    Collections.sort(result, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        return ((BinMethod) obj1).getName()
            .compareTo(((BinMethod) obj2).getName());
      }
    });

    return result;
  }

  private void addMethodNodes(BinTreeTableNode parentNode, List collection) {
    Collections.sort(collection, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        return ((BinMethod) obj1).getName()
            .compareTo(((BinMethod) obj2).getName());
      }
    });

    for (Iterator iter = collection.iterator(); iter.hasNext(); ) {
      BinMethod item = (BinMethod) iter.next();

      BinTreeTableNode child = new BinTreeTableNode(item);

      parentNode.addChild(child);

      child.setSelected(item.isAbstract());
    }
  }

//  private BinTreeTableNode createSubTypeNodes(BinTypeRef item) {
//    BinCIType type = item.getBinCIType();
//
//    BinTreeTableNode result = new BinTreeTableNode(type);
//
//    BinMethod methods[] = OverrideMethodsRefactoring
//        .getOverridableMethods(targetClass);
//
//    Arrays.sort(methods, new Comparator() {
//      public int compare(Object obj1, Object obj2) {
//        return ((BinMethod) obj1).getName()
//            .compareTo(((BinMethod) obj2).getName());
//      }
//    });
//
//    for (int i = 0; i < methods.length; i++) {
//      BinTreeTableNode node = new BinTreeTableNode(methods[i]);
//      result.addChild(node);
//
//      node.setSelected(methods[i].isAbstract()); // select all abstract
//    }
//
//    return result;
//  }
//
//  public void selectMethod(String methodName) {
//    BinTreeTableNode root=(BinTreeTableNode) getRoot();
//    for (int i = 0; i < root.getChildCount(); i++) {
//      selectMethod( (BinTreeTableNode) root.getChildAt(i),methodName);
//    }
//  }
//
//  private void selectMethod(BinTreeTableNode parent, String methodName) {
//    for (int i = 0; i < parent.getChildCount(); i++) {
//      BinTreeTableNode node=(BinTreeTableNode) parent.getChildAt(i);
//      if (  ( (BinMethod)node.getBin()).getName().equals(methodName) ) {
//        node.setSelected(true);
//      }
//    }
//  }

  /**
   *
   * @return true if no methods to override
   */
  public boolean isEmpty() {
    return isEmpty;
  }
}
