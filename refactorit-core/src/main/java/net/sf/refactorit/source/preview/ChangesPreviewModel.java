/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.preview;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.FileEraser;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.source.edit.FilesystemEditor;
import net.sf.refactorit.source.edit.Line;
import net.sf.refactorit.ui.PackageModel.PackageNode;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 *
 * @author Tonis Vaga
 */
public class ChangesPreviewModel extends BinTreeTableModel {
  private Map map;
  private ArrayList filesystemEditors;
  private boolean showAllImports = true;

  /**
   *
   * @param name rootnode name
   * @param map where key is {@link SourceHolder} and value is list of all lines
   */
  public ChangesPreviewModel(String name, Map map, ArrayList filesystemEditors) {
    super(new BinTreeTableNode(name));
    this.map = map;
    this.filesystemEditors = filesystemEditors;
    initChildren(map);
  }

  public int getColumnCount() {
    return 3;
  }

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Location";
      case 1:
        return "Line";
      case 2:
        return "Changed Source";

      default:
        return null;
    }
  }

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column) {
    if (column == 0) {
      return node;
    }

    if (node instanceof SourceLineNode || node instanceof EditorNode) {
      switch (column) {
        case 0:
          return node;
        case 1:
          return ((ParentTreeTableNode) node).getLineNumber() + NUMBER_PADDING;
        case 2:
          return ((ParentTreeTableNode) node).getLineSource();
        default:
          break;
      }
    }

    return null;
  }

  private void initChildren(Map map) {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();

    BinPackage currentPackage = null;
    // FIXME: package name
    BinTreeTableNode packageNode = new BinTreeTableNode(
        RefactorItConstants.debugInfo ? "empty package" : "");

    BinTreeTableNode nonJavaNode = new BinTreeTableNode(
        "Occurrences in non-java files", false);

    // HACK: with non-java file, if package == null so put it in separate branch

    List sortedSources = sortSourcesByPackageName(map);

    for (Iterator iter = sortedSources.iterator(); iter.hasNext(); ) {
      SourceHolder source = (SourceHolder) iter.next();

//      if (source instanceof CompilationUnit
//          && ((CompilationUnit) source).getPackage() != currentPackage) {
      if (source.getPackage() != currentPackage || source.getPackage() == null) {
        currentPackage = source.getPackage();
        if (currentPackage != null) {
          packageNode = new PackageNode(currentPackage);
          packageNode.setShowCheckBox(true);
          // packageNode.setShowCheckBox(true);
          this.addSuitablePackageEditor(source.getSource(), packageNode);
          packageNode.setShowHiddenChildren(true);
          packageNode.reflectLeafNumberToParentName();
          packageNode.setShowHiddenChildren(false);
          root.addChild(packageNode);
        } else {
          // for non-java files, & sources with 'null' in package
          List lines = (List) map.get(source);
          BinTreeTableNode sourceNode = new SourceNode(source);
          sourceNode.setType(UITreeNode.NODE_NON_JAVA);
          sourceNode.setDisplayName(source.getDisplayPath());
          nonJavaNode.addChild(sourceNode);
          for (int index = 0; index < lines.size(); ++index) {
            Line line = (Line) lines.get(index);
            if (line.isChanged()) {
              SourceLineNode sourceLineNode
                  = new SourceLineNode(line, index + 1);
              sourceNode.addChild(sourceLineNode);
            }
          }
        }
      }

      if (source.getPackage() != null) {
        List lines = (List) map.get(source);

        BinTreeTableNode sourceNode = new SourceNode(source);

        addSuitableSourceEditor(source, sourceNode);

        boolean hasAlreadyImport = false;

        for (int index = 0; index < lines.size(); ++index) {
          Line line = (Line) lines.get(index);
          if (line.isChanged()) {
            if (line.isImportStatement()) {
              if (!hasAlreadyImport || showAllImports) {
                SourceLineNode sourceLineNode = new SourceLineNode(line,
                    index + 1);
                sourceLineNode.setDisplayName("Changes in imports:");
                sourceNode.addChild(sourceLineNode);

                hasAlreadyImport = true;
              } else {
                SourceLineNode sourceLineNode = new SourceLineNode(line,
                    index + 1);
                sourceLineNode.setHidden(true);
                //sourceLineNode.setDisplayName("Changes in imports:");
                sourceNode.addChild(sourceLineNode);
              }
            } else {
              SourceLineNode sourceLineNode = new SourceLineNode(line,
                  index + 1);
              sourceNode.addChild(sourceLineNode);
            }
          }
        }

        if (sourceNode.getChildCount() > 0) {
          packageNode.addChild(sourceNode);
        }

        sourceNode.setShowHiddenChildren(true);
        sourceNode.reflectLeafNumberToParentName();
        sourceNode.setShowHiddenChildren(false);
      }
      //removeEmpty(root);
      root.setShowHiddenChildren(true);
      root.reflectLeafNumberToParentName();
      root.setShowHiddenChildren(false);
    }

    removeEmpty(root);

    if (nonJavaNode.getChildCount() > 0) {
      nonJavaNode.setShowHiddenChildren(true);
      nonJavaNode.reflectLeafNumberToParentName();
      nonJavaNode.setShowHiddenChildren(false);
      root.addChild(nonJavaNode);
    }

    root.setShowHiddenChildren(true);
    root.reflectLeafNumberToParentName();
    root.setShowHiddenChildren(false);
  }

  private void removeEmpty(BinTreeTableNode node) {
    // HACK: remove emty package nodes
    if (node instanceof PackageNode) {
      if (node.getChildCount() <= 0) {
        node.getParent().removeChild(node);
      }
    } else {
      Object[] children = node.getAllChildren().toArray();
      for (int i = 0, max = children.length; i < max; i++) {
        removeEmpty((BinTreeTableNode) children[i]);
      }
    }

  }

  private void addSuitableSourceEditor(SourceHolder source,
      BinTreeTableNode node) {
    for (int i = 0; i < filesystemEditors.size(); i++) {
      FilesystemEditor editor = (FilesystemEditor) filesystemEditors.get(i);
      if (editor instanceof FileRenamer) {
        FileRenamer fr;
        fr = (FileRenamer) editor;

        if (source == fr.getTarget()) {
          if (fr.isMover()) {
            node.addChild(new EditorNode(fr, "move to ",
                fr.getDestination(filesystemEditors) + fr.getNewName()));
          } else {
            if (source.getName().equals(fr.getOldName() + ".java")) {
              node.addChild(new EditorNode(fr, "rename to ",
                  fr.getNewName()));
            }
          }
        }
      } else if (editor instanceof FileEraser) {
        FileEraser fEraser = (FileEraser)editor;

        if(source.equals(fEraser.getTarget()) && fEraser.isPreviewEnabled()) {
          EditorNode upperNode = new EditorNode(fEraser, "Remove file ",
              fEraser.getTarget().getName());

          node.addChild(upperNode);
          if(fEraser.getTarget() instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit)fEraser.getTarget();
            List types = cu.getDefinedTypes();
            for(int k=0; k<types.size(); k++) {
              BinTypeRef typeRef = (BinTypeRef)types.get(k);
              EditorNode classNode = new EditorNode(fEraser, "class",
                  typeRef.getQualifiedName());
              classNode.setShowCheckBox(false);
              upperNode.addChild(classNode);
            }
          }
        }
      }
    }
  }

  private void addSuitablePackageEditor(Source source, BinTreeTableNode node) {

    String path = "";
    if (source != null) {
      path = source.getParent().getAbsolutePath();
    }
    for (int i = 0; i < filesystemEditors.size(); i++) {
      FilesystemEditor editor = (FilesystemEditor) filesystemEditors.get(i);

      if (editor instanceof FileEraser) {
        FileEraser fe;
        fe = (FileEraser) editor;

        if (fe.getTarget().getSource().getAbsolutePath().equals(path)) {
          if (!isPresent(node, fe.getName())) {
            node.addChild(new EditorNode(fe, "delete if empty ", fe.getName()));
          }
        }
      }
    }
  }

  private boolean isPresent(BinTreeTableNode node, String string) {
    for (Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
      if (((EditorNode) i.next()).getLineSource().equals(string)) {
        return true;
      }
    }
    return false;
  }

  private List sortSourcesByPackageName(final Map map) {
    List result = new ArrayList(map.keySet());

    Collections.sort(result, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        if (!(obj1 instanceof SourceHolder)
            || !(obj2 instanceof SourceHolder)) {
          return -1; // shouldn't get here, just in case
        }

        SourceHolder sf1 = (SourceHolder) obj1;
        SourceHolder sf2 = (SourceHolder) obj2;

        String pkg1 = sf1.getPackage() != null ? sf1.getPackage().getQualifiedName() : "";
        String pkg2 = sf2.getPackage() != null ? sf2.getPackage().getQualifiedName() : "";

        int result = pkg1.compareTo(pkg2);

        if (result == 0) {
          result = sf1.getName().compareTo(sf2.getName());
        }

        return result;
      }
    });

    return result;
  }

  /**
   *
   * @return true if all nodes are selected in this model.
   */
  public boolean isAllNodesSelected() {
    BinTreeTableNode node = (BinTreeTableNode) getRoot();

    return isAllNodesSelected(node);
  }

  private boolean isAllNodesSelected(BinTreeTableNode node) {
    if (!node.isSelected()) {
      return false;
    }

    for (int i = 0, max = node.getChildCount(); i < max; i++) {
      BinTreeTableNode subNode = (BinTreeTableNode) node.getChildAt(i);

      if (!isAllNodesSelected(subNode)) {
        return false;
      }
    }

    return true;
  }

  public Map getMap() {
    return this.map;
  }

}
