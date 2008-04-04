/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.vfs.Source;

import rantlr.BaseAST;
import rantlr.Token;
import rantlr.collections.AST;


/**
 * @author Sander Magi
 * @author Anton Safonov
 */
public class TreeASTImpl extends BaseAST implements TokenExt, ASTImpl {

  public final ASTTree.BackReference backRef;
  private int index;

  private ASTImpl parent;

  /** For synthetic nodes not in the tree */
  protected TreeASTImpl() {
    this.index = -1;
    this.backRef = null;
  }

  public TreeASTImpl(final int index, final ASTTree.BackReference backRef) {
//System.err.println("TreeASTImpl(" + index + "), ind: " + backRef.astIndex
//        + ", hex: " + Integer.toHexString(System.identityHashCode(this)));
    this.index = index;
    this.backRef = backRef;
  }

  public Source getSource() {
    return backRef.ownerTree.getSource(index);
  }

  public void setSource(final Source source) {
    backRef.ownerTree.setSource(index, source);
  }

  public final void setParent(final ASTImpl parent) {
    this.parent = parent;
  }

  public final ASTImpl getParent() {
    return this.parent;
  }

  public final int getIndex() {
    return index;
  }

  /** called during recompression when tree is reorganized */
  public final void setIndex(final int index) {
    this.index = index;
  }

  public int getLine() {
    return backRef.ownerTree.startLines[index];
  }

  public int getStartLine() {
    return backRef.ownerTree.startLines[index];
  }

  public int getColumn() {
    return backRef.ownerTree.startColumns[index];
  }

  public int getStartColumn() {
    return backRef.ownerTree.startColumns[index];
  }

  public int getEndColumn() {
    return backRef.ownerTree.endColumns[index];
  }

  public int getEndLine() {
    return backRef.ownerTree.endLines[index];
  }

  public String getText() {
    return backRef.ownerTree.strings[index];
  }

  /** overrides */
  public final int getTextLength() {
    return getText().length();
  }

  public final String getFilename() {
    return null;
  }

  public final void setFilename(String name) {
  }

  public final byte getTypeAsByte() {
    return backRef.ownerTree.astTypes[index];
  }

  public final AST getFirstChild() {
    return down;
  }

  public final AST getNextSibling() {
    return right;
  }

  /**
   * Called on the building time, does not mark the tree tainted
   */
  public final void setFirstChildInternal(final TreeASTImpl node) {
    this.down = node;
    node.setParent(this);
  }

  /**
   * Called on the building time, does not mark the tree tainted
   */
  public final void setNextSiblingInternal(final TreeASTImpl node) {
    if (Assert.enabled && this == node) {
      new Exception("equal1: " + node).printStackTrace();
    }
    this.right = node;
    node.setParent(this.getParent());
  }

  public final void setFirstChild(final AST node) {
    if (down != node) {
      try {
        backRef.ownerTree.markChanged();
//        if (node == backRef.ownerTree.rootNode) {
//          backRef.ownerTree.rootNode = this;
//        }
      } catch (NullPointerException e) {
        // SimpleASTImpl calls fall here
      }

      try {
        this.down = (TreeASTImpl) node;
        ((TreeASTImpl) node).setParent(this);
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println("given: " + node + " - " + node.getClass());
      }
    }
  }

  public final void setNextSibling(final AST node) {
    if (Assert.enabled && this == node) {
      new Exception("equal2: " + node).printStackTrace();
      ASTImpl temp = (ASTImpl) node;
      int i = 0;
      while (temp != null) {
        System.err.println("node" + i++ + ": " + temp);
        temp = temp.getParent();
      }
    }

    if (right != node) {
      try {
        if ( backRef != null ) {
          backRef.ownerTree.markChanged();
        }

        // FIXME not needed?
//        if (node == backRef.ownerTree.rootNode) {
//          backRef.ownerTree.rootNode = this;
//        }
      } catch (NullPointerException e) {
        // SimpleASTImpl calls fall here

        // shouldn't happen anymore?
        AppRegistry.getExceptionLogger().warn(e,TreeASTImpl.class);
      }

      this.right = (TreeASTImpl) node;
    }
  }

  public final void addChild(final AST node) {
    if (node == null) {
      return;
    }

    try {
      backRef.ownerTree.markChanged();
//      if (node == backRef.ownerTree.rootNode) {
//        backRef.ownerTree.rootNode = this;
//      }
    } catch (NullPointerException e) {
      // SimpleASTImpl calls fall here
    }

    super.addChild(node);
//    ((ASTImpl) node).setParent(this);
  }

  public void setColumn(final int column) {
    backRef.ownerTree.startColumns[index] = (short) column;
  }

  public void setEndColumn(final int column) {
    backRef.ownerTree.endColumns[index] = (short) column;
  }

  public void setEndLine(final int line) {
    backRef.ownerTree.endLines[index] = (short) line;
  }

  public void setLine(final int line) {
    backRef.ownerTree.startLines[index] = (short) line;
  }

  public void setStartColumn(final int column) {
    setColumn(column);
  }

  public void setStartLine(final int line) {
    setLine(line);
  }

  public void setText(final String str) {
//System.err.println("TreeASTImpl.setText: " + str);
    backRef.ownerTree.strings[index] = str;
  }

  public void setType(final int ttype) {
//System.err.println("TreeASTImpl.setType: " + ttype);
    try {
      if (ttype >= 128) {
        this.backRef.ownerTree.astTypes[index] = (byte) (127 - ttype);
      } else {
        this.backRef.ownerTree.astTypes[index] = (byte) ttype;
      }

//      if (ownerTree.rootNode == null
//          && (ttype == JavaTokenTypes.PACKAGE_DEF
//          || ttype == JavaTokenTypes.IMPORT)) {
//        ownerTree.rootNode = this;
//      }
    } catch (NullPointerException e) {
      // synthetic nodes without tree fall here
    }
  }

  public int getType() {
    try {
      final byte ttype = backRef.ownerTree.astTypes[index];
      if (ttype >= 0) {
        return ttype;
      } else {
        return 127 - ttype;
      }
    } catch (NullPointerException e) {
      // synthetic nodes without tree fall here
      return -1;
    }
  }

  /** NOTE: compares positions only!!! */
  public final int compareTo(final Object o) {
    if (!(o instanceof ASTImpl)) {return 0;
    }

    int res = this.getLine() - ((ASTImpl) o).getLine();
    if (res == 0) {
      res = this.getColumn() - ((ASTImpl) o).getColumn();
    }

    return res;
  }

  public final void initialize(final Token tok) {
    setType(tok.getType()); // it converts it into byte in its own way
    setText(tok.getText());
    setStartLine(tok.getLine());
    setStartColumn(tok.getColumn());
    setEndLine(((TokenExt) tok).getEndLine());
    setEndColumn(((TokenExt) tok).getEndColumn());
  }

  public final void initialize(final AST t) {
    try {
      final ASTImpl treeNode = (ASTImpl) t;

      setType(treeNode.getType());
      setText(treeNode.getText());
      setStartLine(treeNode.getLine());
      setStartColumn(treeNode.getColumn());
      setEndLine(treeNode.getEndLine());
      setEndColumn(treeNode.getEndColumn());
    } catch (ClassCastException e) {
      System.err.println("node: " + t + " - " + t.getClass());
    }
  }

  public final void initialize(final int t, final String txt) {
//System.err.println("TreeAstImpl.initialize: " + t + " - " + txt);
    // Assert.must(false, "ASTImpl cannot be initialized with (int t=" + t + ", String txt=" + txt + ")");
    setType(t);
    setText(txt);
  }

  public final void initialize(final int t, final int line, final int column) {
//System.err.println("TreeAstImpl.initialize: " + t + " - " + txt);
    setType(t);
    setStartLine(line);
    setStartColumn(column);
  }

  public final String toString() {
    String name = getText();
    if (name == null) {
      name = "<null>";
    }
    String tokenName = getTokenNames()[getType()];
    if (tokenName.length() > 1 && tokenName.charAt(0) == '\"') {
      tokenName = tokenName.substring(1, tokenName.length() - 1);
    }
    if (!name.equalsIgnoreCase(tokenName)) {
      name += " {" + tokenName + "}";
    }
    name += " ["
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + "], " + this.backRef.ownerTree.metaBytes[this.index];
//    if (getSource() != null) {
//      name += " in " + getSource();
//    }

    return name;
  }

  public final int hashCode() {
    final String text = getText();
    final int len = text.length();
    int x;
    if (len > 0) {
      x = 31 * text.charAt(0) + text.charAt(len - 1);
    } else {
      x = 31;
    }
    return 15 * getType()
        + len * x
        + 63 * getLine()
        + 127 * getColumn();

//    return getText().hashCode() + getType()
//        + getLine() + getColumn() + getEndLine() + getEndColumn();
  }

  public final boolean equals(final Object o) {
    return equals((AST) o);
  }

  public final boolean equals(final AST o) {
    if (o == null) {
      return false;
    }

    final ASTImpl other = (ASTImpl) o;
    boolean result = getType() == other.getType()
        && getLine() == other.getLine()
        && getColumn() == other.getColumn()
        && ((getText() == null && other.getText() == null)
        || (getText() != null && other.getText() != null
        && getText().equals(other.getText())))
        && getEndLine() == other.getEndLine()
        && getEndColumn() == other.getEndColumn();
    if (result && getLine() == 0 && getColumn() == 0) {
      // indistinctable otherwise nodes
      result = System.identityHashCode(this) == System.identityHashCode(other);
    }

    return result;
  }

  public final int getLevel() {
    int type = getType();
    for (int i = 0; i < LEVELS.length; i++) {
      final int[] level = (int[]) LEVELS[i];
      for (int k = 0; k < level.length; k++) {
        if (level[k] == type) {
          return i;
        }
      }
    }

    return 0;
  }
}
