/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TreePathReference {
  private DeepNameMatcher[] nodeReferencingStrategies = new DeepNameMatcher[3];

  public TreePathReference(TreePath treePath) {
//    Object node = treePath.getLastPathComponent();

    // The DeepNameMatcher is the best strategy if the names
    // and locations of nodes don't change (it does allow
    // one extra root node to appear or disappear -- for the GrayCopyOf...).

    this.nodeReferencingStrategies[0]
        = new DeepNameMatcher(treePath, DeepNameMatcher.NORMAL);
    this.nodeReferencingStrategies[1]
        = new DeepNameMatcher(treePath, DeepNameMatcher.ALLOW_EXTRA_ROOTNODE);
    this.nodeReferencingStrategies[2]
        = new DeepNameMatcher(treePath, DeepNameMatcher.FORGET_EXTRA_ROOTNODE);

    // FIXME:
    // These (commented out) are the older strategies, not sure if
    // we need them (they could
    // even cause bugs -- if a node is removed, they could return a
    // reference to some other node that matches their criteria).
    //
    // They do not support multiple nodes for the same bin item
    // (for instance, the Audit module can have multiple nodes for the same item);
    // also, if you have a type with the same name in more than one package then
    // when switching to and from the GrayCopy these strategies
    // will not work properly (they will always return the first node with that name).
    //
    // OTOH it is possible that these strategis work better than the
    // DeepNameMatcher if the name (and/or location) of the node changes,
    // but everything else stays the same -- does that ever happen?

    /*this.nodeReferencingStrategies[3]
        = new BinItemReferenceSaver(treePath, node);
         this.nodeReferencingStrategies[4]
        = new SourceLocationSaver(treePath, node);
         this.nodeReferencingStrategies[5]
        = new NodeNameSaver(treePath, node);*/
  }

  public TreePath getPath(Project project, TreeModel model) {
    for (int i = 0; i < this.nodeReferencingStrategies.length; i++) {
      final DeepNameMatcher strategy = this.nodeReferencingStrategies[i];

      Object found = strategy.getNode(model);
      if (found != null) {
        return getTreePathForNode(found, model);
      }
    }

    return null;
  }

  private TreePath getTreePathForNode(Object node, TreeModel model) {
    List treePath = new ArrayList();

    while (node != null) {
      treePath.add(node);
      node = getParentNode(node, model);
    }

    Collections.reverse(treePath);

    return new TreePath(treePath.toArray());
  }

  private Object getParentNode(Object n, TreeModel model) {
    if (n instanceof ParentTreeTableNode) {
      ParentTreeTableNode node = (ParentTreeTableNode) n;
      return node.getParent();
    } else {
      return getParentRecursively(n, model, model.getRoot());
    }
  }

  /** Slow, but never really executed AFAIK (perhaps in FIXME Scanner?) */
  private Object getParentRecursively(Object node, TreeModel model,
      Object startLocation) {
    for (int i = 0; i < model.getChildCount(startLocation); i++) {
      Object aChild = model.getChild(startLocation, i);
      if (aChild == node) {
        return startLocation;
      }

      Object result = getParentRecursively(node, model, aChild);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  /*private abstract static class NodeReference {
    protected int compilationUnitLine;
    protected int pathDepth;

    protected boolean compilationUnitLineSaveable;

    public NodeReference() {}

    public NodeReference(Object node, int pathDepth) {
      if (node instanceof BinTreeTableNode) {
        BinTreeTableNode n = (BinTreeTableNode) node;

        this.compilationUnitLine = n.queryLineNumber();
        this.pathDepth = pathDepth - temporarlyAddedRootNodesInModel(n);
        this.compilationUnitLineSaveable = true;
      } else {
        this.compilationUnitLineSaveable = false;
      }
    }

    private int temporarlyAddedRootNodesInModel(Object node) {
      if (node instanceof ResultArea.GrayCopyOfNode) {
        return 1;
      } else {
        return 0;
      }
    }

    public Object getNode(Project project, TreeModel model) {
      return getNodeRecursively(project, model.getRoot(), model, 1, true);
    }

    protected Object getNodeRecursively(
        Project project,
        Object node,
        TreeModel model,
        int depth,
        boolean lookForLineNrMatch)
    {
      if (this.compilationUnitLineSaveable) {
        BinTreeTableNode n = (BinTreeTableNode) node;

   if ((depth - temporarlyAddedRootNodesInModel(node)) == this.pathDepth &&
   ((n.queryLineNumber() == this.compilationUnitLine) || (!lookForLineNrMatch)) &&
            isReferencedNode(n, project) )
        {
          return node;
        }
      }
      else {
        if (isReferencedNode(node, project)) {
          return node;
        }
      }

      for (int i = 0; i < model.getChildCount(node); i++) {
        Object recursiveResult = getNodeRecursively(project, model.getChild(node, i), model, depth + 1, lookForLineNrMatch);
        if (recursiveResult != null)
          return recursiveResult;
      }

      return null;
    }

    // NOTE: superclass already looks for TreePath length equality (and also for lineNr equality
    // by default, but that can be turned off through parameters of getNodeRecursively()).

    protected abstract boolean isReferencedNode(Object node, Project project);
     }

     private static class SourceLocationSaver extends NodeReference {
    private static final int EXTRA_ROOT_NODE_COUNT = 1;

    private String compilationUnitName;

    public SourceLocationSaver(TreePath treePath, Object n) {
      super(n, treePath.getPathCount() - EXTRA_ROOT_NODE_COUNT);

      if (this.compilationUnitLineSaveable) {
        BinTreeTableNode node = (BinTreeTableNode) n;

        this.compilationUnitName = node.getCompilationUnit() == null ? null : node.getCompilationUnit().getRelativePath();
      }
    }

    protected boolean isReferencedNode(Object n, Project project) {
      if (this.compilationUnitName == null || (!compilationUnitLineSaveable)) {
        return false;
      } else {
        BinTreeTableNode node = (BinTreeTableNode) n;
        return fileNamesEqual(node.getCompilationUnit(), this.compilationUnitName);
      }
    }

    private boolean fileNamesEqual(CompilationUnit compilationUnit, String name) {
      return fileNamesEqual(compilationUnit == null ? null : compilationUnit.getRelativePath(), name);
    }

    private boolean fileNamesEqual(String a, String b) {
      if (a == null && b == null) {
        return true;
      } else if (a == null || b == null) {
        return false;
      } else {
        return a.equals(b);
      }
    }
     }


     private static class BinItemReferenceSaver extends NodeReference {
    private BinItemReference reference;

    public BinItemReferenceSaver(TreePath treePath, Object n) {
      super(n, treePath.getPathCount());

      if (this.compilationUnitLineSaveable) {
        BinTreeTableNode node = (BinTreeTableNode) n;

        this.reference = BinItemReference.create(node.getBin());
      }
    }

    public Object getNode(Project project, TreeModel model) {
      if (!this.compilationUnitLineSaveable) {
        return null;
      }

      Object resultWithLineNrMatch = getNodeRecursively(project, model.getRoot(), model, 1, true);

      if (resultWithLineNrMatch != null) {
        return resultWithLineNrMatch;
      } else {
        return getNodeRecursively(project, model.getRoot(), model, 1, false);
      }
    }

    protected boolean isReferencedNode(Object n, Project project) {
      if (!this.compilationUnitLineSaveable || this.reference.wasUnknownType()) {
        return false;
      }

      BinTreeTableNode node = (BinTreeTableNode) n;

      return node.getBin() == this.reference.findBinObject(project) &&
          node.getBin() != null;
    }
     }


     private static class NodeNameSaver extends NodeReference {
    private String nodeName;

    public NodeNameSaver(TreePath treePath, Object node) {
      super(node, treePath.getPathCount());

      this.nodeName = StringUtil.removeHtml(getPureNodeName(node));
    }

    protected boolean isReferencedNode(Object node, Project project) {
   return this.nodeName.equals(StringUtil.removeHtml(getPureNodeName(node)));
    }

    private String getPureNodeName(Object n) {
      if (this.compilationUnitLineSaveable) {
        BinTreeTableNode node = (BinTreeTableNode) n;
        return node.getName();
      } else {
        return n.toString();
      }
    }
     }*/

  /**
   * Remembers the entire path (by the names of the path elements).
   * Assumes that all children of _one parent_ have unique names.
   */
  private static class DeepNameMatcher {
    private String[] references;

    // These constants rootnodes are here for the Gray tree support
    public static final int ALLOW_EXTRA_ROOTNODE = 0;
    public static final int NORMAL = 1;
    public static final int FORGET_EXTRA_ROOTNODE = 2;

    private final int compareMode;

    public DeepNameMatcher(TreePath path, int compareMode) {
      super();

      this.compareMode = compareMode;

      Object[] realPath = path.getPath();

      references = new String[realPath.length];
      for (int i = 0; i < references.length; i++) {
        references[i] = nodeToString(realPath[i]);
      }
    }

    public Object getNode(TreeModel model) {
      Object current = model.getRoot();

      // NORMALly skip checking the root node -- it probably does not matter

      for (int i = compareMode; i < references.length; i++) {
        Object child = getChild(current, references[i], model);
        if (child == null) {
          return null;
        }
        current = child;
      }

      return current;
    }

    private Object getChild(Object parent, String childName, TreeModel model) {
      for (int i = 0; i < model.getChildCount(parent); i++) {
        Object child = model.getChild(parent, i);

        if (nodeToString(child).equals(childName)) {
          return child;
        }
      }

      return null;
    }

    private static String nodeToString(Object node) {
      String result;
      if (node instanceof ParentTreeTableNode) {
        result = ((ParentTreeTableNode) node).getIdentifier();
      } else {
        result = StringUtil.removeHtml(node.toString());
      }

      int childCounter = result.lastIndexOf('(');
      if (childCounter >= 0) {
        result = result.substring(0, childCounter);
      }

      return result.trim();
    }
  }
}
