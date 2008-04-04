/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.classmodel.*;
import net.sf.refactorit.commonIDE.NotFromSrcOrFromIgnoredException;
import net.sf.refactorit.query.ItemByNameFinder;
import oracle.ide.addin.Context;
import oracle.ide.explorer.ExplorerContextUtil;
import oracle.ide.explorer.TNode;
import oracle.ide.model.Element;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * A class for getting information about the current Structure Explorer state.
 *
 * @author  tanel
 */
public class ExplorerWrapper {
  Context context;

  /** Creates a new instance of ExplorerWrapper */
  public ExplorerWrapper(Context context) {
    this.context = context;
  }

  /**
   * @return currently selected node
   **/
  public TNode[] getCurrentNodes() {
    return ExplorerContextUtil.getAllTNodes(context);
  }

  public static boolean isPackageNode(TNode node) {
    // FIXME: change this when API improves
    if ((((TNode) node.getParent()).isRoot()) &&
        (node.getData().getClass().getName().indexOf("BaseElement") > 0)) {
      return true;
    }

    // this is for JDev 10
    if (node.getData().getClass().getName().indexOf("PackageElement") > 0) {
      return true;
    }

    return false;
  }

  /**
   * Determines whether the given node corresponds to a class or interface.
   *
   * @param node node to be checked
   * @return true if node corresponds to class or interface
   **/
  public static boolean isClassNode(TNode node) {
    //FIXME: uses a bit ugly hack
    return (node.getData().getClass().getName().indexOf("ClassElement") > 0);
  }

  /**
   * Determines whether the current node in the Navigator window corresponds to a field.
   *
   * FIXME: uses a bit ugly hack
   *
   * @param node current node
   * @return true if the node corresponds to a class field
   **/
  public boolean isFieldNode(TNode node) {
    Element element = node.getData();
    if (isMemberNode(node)) {
      if (element.getClass().getName().indexOf("BaseElement") > 0) {
        if (element.getLongLabel().indexOf("(") == -1) {
          return true;
        }
      }
    }

    // this is for newer JDev 10
    if (element.getClass().getName().indexOf("FieldElement") > 0) {
      if (element.getLongLabel().indexOf("(") == -1) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the current node in the Navigator window corresponds to a method.
   *
   * FIXME: uses a bit ugly hack
   *
   * @param node current node
   * @return true if the node corresponds to a class method
   **/
  public boolean isMethodNode(TNode node) {
    Element element = node.getData();
    if (isMemberNode(node)) {
      if (element.getClass().getName().indexOf("BaseElement") > 0) {
        if (element.getLongLabel().indexOf("(") > 0) {
          return true;
        }
      }
    }

    // this is for newer JDev 10
    if (element.getClass().getName().indexOf("MethodElement") > 0) {
      if (element.getLongLabel().indexOf("(") > 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the node corresponds to class member - that is
   * field, method, constructor or an inner class
   *
   * @return true if node corresponds to a class member
   **/
  private static boolean isMemberNode(TNode node) {
    if (isClassNode((TNode) node.getParent())) {
      String label = node.getData().getLongLabel();
      if (!label.startsWith("implements ") && !label.startsWith("extends ")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Finds the selected bin item from the explorer window.
   *
   * @param active RefactorIT project
   * @return selected bin item or null if the current node does not
   *        correspond to any bin item.
   **/
  public Object getSelectedBinItem(Project project)
      throws NotFromSrcOrFromIgnoredException {
    TNode[] nodes = getCurrentNodes();

    if (nodes == null || nodes.length == 0) {
      return null;
    }

    int size = nodes.length;
    Object[] bins = new Object[size];

    int flag = 0;

    for (int i = 0; i < size; i++) {
      if (isClassNode(nodes[i])) {
        bins[i] = getBinCIType(project, nodes[i]);
      } else if (isFieldNode(nodes[i])) {
        bins[i] = getBinField(project, nodes[i]);
      } else if (isMethodNode(nodes[i])) {
        bins[i] = getBinMethod(project, nodes[i]);
      } else if (isPackageNode(nodes[i])) {
        bins[i] = getBinPackage(project, nodes[i]);
      }

      if (bins[i] == null) {
        flag++;
      }
    }

    if (size == 1) {
      return bins[0];
    }

    if (flag > 0) {
      ArrayList li = new ArrayList();

      for (int i = 0; i < bins.length; i++) {
        if (bins[i] != null) {
          li.add(bins[i]);
        }
      }

      bins = li.toArray();
    }

    return bins;
  }

  private BinField getBinField(Project project, TNode node)
      throws NotFromSrcOrFromIgnoredException {
    String fieldName = node.getData().getLongLabel().trim();

    // they have added type at the end in JDev 10
    int pos = fieldName.indexOf(':');
    if (pos > 0) {
      fieldName = fieldName.substring(0, pos).trim();
    }

    BinCIType type = getBinCIType(project, (TNode) node.getParent());
    if (type != null) {
      return ItemByNameFinder.findBinField(type, fieldName);
    }

    return null;
  }

  private BinMethod getBinMethod(Project project, TNode node)
      throws NotFromSrcOrFromIgnoredException {
    String methodDeclaration = node.getData().getLongLabel();
    int pos0 = methodDeclaration.indexOf('(');
    if (pos0 < 0) {
      return null;
    }

    int pos1 = methodDeclaration.lastIndexOf(')');
    if (pos1 < 0) {
      return null;
    }

    String methodName = methodDeclaration.substring(0, pos0++).trim();

    StringTokenizer st = new StringTokenizer(
        methodDeclaration.substring(pos0, pos1), ",");

    String[] args = new String[st.countTokens()];

    for (int i = 0; st.hasMoreTokens(); i++) {
      String argument = st.nextToken().trim();
      int len = argument.length();
      for (int n = 0; n < len; n++) {
        char ch = argument.charAt(n);
        if (Character.isWhitespace(ch)) {
          argument = argument.substring(0, n);
          break;
        }
      }

      args[i] = argument;
    }

    BinCIType type = getBinCIType(project, (TNode) node.getParent());
    if (type != null) {
      if (methodName.equals(type.getName())) {
        //constructor
        return ItemByNameFinder.findBinConstructor((BinClass) type, args);
      } else {
        // method
        return ItemByNameFinder.findBinMethod(type, methodName, args);
      }
    } else {
      return null;
    }
  }

  /**
   * Returns the bin item corresponding to the currently selected node
   *
   * @return class or interface bin type or null if the node does not
   *        correspond to class or interface
   **/
  private BinCIType getBinCIType(Project project, TNode node)
      throws NotFromSrcOrFromIgnoredException {
    BinCIType type =
        ItemByNameFinder.findBinCIType(project, getFullClassName(node));

    if (type != null) {
      return type;
    } else {
      throw new NotFromSrcOrFromIgnoredException();
    }
  }

  private BinPackage getBinPackage(Project project, TNode node)
       throws NotFromSrcOrFromIgnoredException {
    BinPackage pack =
        ItemByNameFinder.findBinPackage(project, node.getData().getLongLabel());

    if (!pack.hasTypesWithSources()
        && !pack.hasTypesWithoutSources()
        && BinPackage.isIgnored(pack)) {
      throw new NotFromSrcOrFromIgnoredException();
    }

    return pack;
  }

  /**
   * Determines the full class name, including package name, corresponding to
   * the given node.
   *
   * @param node a node corresponding to a class or interface
   * @return full class name
   **/
  public static String getFullClassName(TNode node) {
    String className = node.getData().getLongLabel();
    TNode parent = (TNode) node.getParent();

    if (parent.getData().getClass().getName().indexOf("JavaRootElement") > 0
        || parent.getData().getClass().getName().indexOf("JavaRootFolder") > 0) {
      String packageName = getPackageName(node);
      return (packageName == null || packageName.length() == 0) ?
          className : packageName + '.' + className;
    } else {
      return getFullClassName(parent) + '$' + className;
    }
  }

  /**
   * Determines the package name corresponging to the given node.
   *
   * @param node a node in the Structure explorer, can be any node
   * @return package name
   **/
  public static String getPackageName(TNode node) {
    // get the first node
    TNode firstNode = (TNode) getRoot(node).getChildAt(0);
    // now make sure if it is a package node
    if (isPackageNode(firstNode)) {
      return firstNode.getData().getLongLabel();
    }

    return "";
  }

  /**
   * Finds the root of the given node.
   *
   * @param node any node in the structure exlporer window
   * @return root node
   **/
  public static TNode getRoot(TNode node) {
    TNode parent = (TNode) node.getParent();
    if (parent.isRoot()) {
      return parent;
    }

    return getRoot(parent);
  }
}
