/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;

import rantlr.ASTVisitor;

import java.util.ArrayList;
import java.util.List;


public final class ASTUtil implements JavaTokenTypes {
  private static final String[] TOKEN_TEXT = new String[1 << 8];

  private ASTUtil() {
  }

  public static List getForLine(final ASTImpl root, final int line) {
    final List result = new ArrayList(32);

    getForLine(root, line, result);

    return result;
  }

  private static void getForLine(final ASTImpl root, final int line,
      final List buffer) {

    for (ASTImpl current = root, child; current != null;
        current = (ASTImpl) current.getNextSibling()) {
      child = (ASTImpl) current.getFirstChild();

      // Check for children
      if (child != null) {
        getForLine(child, line, buffer);
      }

      // Check the node itself
      if (current.getLine() == line) {
        buffer.add(current);
      }

    }
  }

  /**
   * Note: initial list must be ordered!
   * @param line line number
   * @param initialList list of ASTs to be searched in
   * @return list of ASTs residing on the given line
   */
  public static List getAstsForLine(final int line, final List initialList) {
    final List result = new ArrayList(4);

    if (initialList == null) {
      return result;
    }

    for (int i = 0, max = initialList.size(); i < max; i++) {
      final ASTImpl current = (ASTImpl) initialList.get(i);

      // Check line numbers
      if (current.getLine() == line) {
        result.add(current);
      }

      // hack-quality code for better peformance
      if (current.getLine() > line) {
        break; // Bail out
      }
    }

    return result;
  }

  /**
   * Gets first occurence of the child node with a certain type.
   * @param type
   * @return <code>ASTImpl</code>
   * @see ASTImpl#getType()
   */
  public static ASTImpl getFirstChildOfType(final ASTImpl rootAst,
      final int type) {
    if (rootAst == null) {
      return null;
    }
    ASTImpl result = null;

    ASTImpl sibling = (ASTImpl) rootAst.getFirstChild();
    while (sibling != null) {
      if (sibling.getType() == type) {
        result = sibling;
        break;
      }
      sibling = (ASTImpl) sibling.getNextSibling();
    }

    return result;
  }

  public static ASTImpl getLastChildOfTypeInConsecutiveList(final ASTImpl
      rootAst, final int type) {
    final ASTImpl firstChild = getFirstChildOfType(rootAst, type);

    if (firstChild == null) {
      return null;
    }

    ASTImpl result = firstChild;
    ASTImpl sibling = (ASTImpl) firstChild.getNextSibling();

    while (sibling != null && sibling.getType() == type) {
      result = sibling;
      sibling = (ASTImpl) sibling.getNextSibling();
    }

    return result;
  }

  public static ASTImpl getLastChildOfType(ASTImpl rootAst, int type) {
    final ASTImpl firstChild = getFirstChildOfType(rootAst, type);

    if (firstChild == null) {
      return null;
    }

    ASTImpl result = firstChild;
    ASTImpl sibling = (ASTImpl) firstChild.getNextSibling();

    while (sibling != null) {
      if (sibling.getType() == type) {
        result = sibling;
      }

      sibling = (ASTImpl) sibling.getNextSibling();
    }

    return result;
  }

  /**
   * Returns the first sibling of the node, that is, the first child of the
   * node's parent that is not the given node. Returns null if this node is the
   * only child of it's parent.
   */
  public static ASTImpl getFirstSibling(final ASTImpl node) {
    final ASTImpl parent = node.getParent();
    ASTImpl child = (ASTImpl) parent.getFirstChild();
    if (child == node) {
      child = (ASTImpl) child.getNextSibling();
    }
    return child;
  }

  /**
   * Extracts the first node with available line number.
   *
   * @param node defines top of the tree to search for line number.
   * @return node having line number.
   */
  public static ASTImpl getFirstNodeOnLine(final ASTImpl node) {
    for (ASTImpl current = node, child; current != null;
        current = (ASTImpl) current.getNextSibling()) {

      // Check if this node carries reasonable information
      if (current.getLine() > 0 && current.getColumn() > 0) {
        return current;
      }

      // Descend into children
      if ((child = (ASTImpl) current.getFirstChild()) != null) {
        child = getFirstNodeOnLine(child);

        if (child != null) {
          return child;
        }
      }
    }

    return null;
  }

  public static boolean sameCoordinates(ASTImpl a, ASTImpl b) {
    return
        a.getStartLine() == b.getStartLine() &&
        a.getStartColumn() == b.getStartColumn() &&
        a.getEndLine() == b.getEndLine() &&
        a.getEndColumn() == b.getEndColumn();
  }

  public static boolean contains(final ASTImpl first, final ASTImpl second) {
    if (first == null || second == null) {
      return false;
    }

    if (first == second) {
      return true;
    }

    boolean contains = first.getStartLine() <= second.getStartLine()
        && first.getEndLine() >= second.getEndLine();

    if (contains && first.getStartLine() == second.getStartLine()
        && first.getStartColumn() > second.getStartColumn()) {
      contains = false;
    }

    if (contains && first.getEndLine() == second.getEndLine()
        && first.getEndColumn() < second.getEndColumn()) {
      contains = false;
    }

    return contains;
  }

  // FIXME get rid of by using the right source of info
  public static ASTImpl getAssignedExpression(ASTImpl variableDef) {
    ASTImpl assignment = (ASTImpl) getModifiers(variableDef).
        getNextSibling().getNextSibling().getNextSibling();
    if (assignment == null) {
      return null;
    }

    return (ASTImpl) assignment.getFirstChild();
  }

  // FIXME get rid of by using the right source of info
  private static ASTImpl getModifiers(final ASTImpl variableDef) {
    return (ASTImpl) variableDef.getFirstChild();
  }

  public static final List getChildren(final ASTImpl ast) {
    final List result = new ArrayList();
    ASTImpl child = (ASTImpl) ast.getFirstChild();
    while (child != null) {
      result.add(child);
      child = (ASTImpl) child.getNextSibling();
    }

    return result;
  }

  public static final List getAllChildren(final ASTImpl ast) {
    return getAllChildren(ast, new ArrayList());
  }

  private static final List getAllChildren(final ASTImpl ast, final List result) {
    if (Assert.enabled) {
      Assert.must(ast != null, "Ast shouldn't be null!");
    }
    ASTImpl child = (ASTImpl) ast.getFirstChild();
    while (child != null) {
      result.add(child);
      getAllChildren(child, result);
      child = (ASTImpl) child.getNextSibling();
    }

    return result;
  }

  public static ASTImpl getStatementList(BinMember member) {
    return ASTUtil.getFirstChildOfType(member.getOffsetNode(),
        SLIST);
  }

  public static ASTImpl getCommonTypeNode(ASTImpl variableDef) {
    ASTImpl typeNode = getFirstChildOfType(variableDef, TYPE);
    ASTImpl result = (ASTImpl) typeNode.getFirstChild();

    while (result.getType() == ARRAY_DECLARATOR) {
      result = (ASTImpl) result.getFirstChild();
    }

    return result;
  }

  public static final int indexFor(final ASTImpl node) {
    if (node == null || !(node instanceof TreeASTImpl)) {
      return -1;
    }
    return ((TreeASTImpl) node).getIndex();
  }

  public static boolean isBefore(ASTImpl before, ASTImpl after) {
    return SourceCoordinate.getForEnd(before).
        isBefore(SourceCoordinate.getForStart(after));
  }

  public static final int getModifierForAST(final ASTImpl modifierNode) {
    final int modifierType = modifierNode.getType();
    switch (modifierType) {
      case 0:
// non-existent type, generated by BinMember for package private place
//      case LITERAL_package:
        return BinModifier.PACKAGE_PRIVATE;
      case LITERAL_public:
        return BinModifier.PUBLIC;
      case LITERAL_private:
        return BinModifier.PRIVATE;
      case LITERAL_protected:
        return BinModifier.PROTECTED;
      case ABSTRACT:
        return BinModifier.ABSTRACT;
      case LITERAL_static:
        return BinModifier.STATIC;
      case FINAL:
        return BinModifier.FINAL;
      case LITERAL_native:
        return BinModifier.NATIVE;
      case LITERAL_synchronized:
        return BinModifier.SYNCHRONIZED;
      case STRICTFP:
        return BinModifier.STRICTFP;
      case LITERAL_volatile:
        return BinModifier.VOLATILE;
      case LITERAL_transient:
        return BinModifier.TRANSIENT;
      case LITERAL_interface:
        return BinModifier.INTERFACE;
      case ANNOTATION:
        return BinModifier.ANNOTATION;
      default:
    }
    throw new IllegalArgumentException("Not modifier node:" + modifierType);
  }

  public static ASTImpl[] getWithSubtree(final ASTImpl node) {
    final ArrayList results = new ArrayList();
    if (node != null) {
      results.add(node);

      traverse((ASTImpl) node.getFirstChild(),
          new ASTVisitor() {
        public void visit(final rantlr.collections.AST aNode) {
          results.add(aNode);
        }
      }
      );
    }

    return (ASTImpl[]) results.toArray(new ASTImpl[results.size()]);
  }

  /**
   * Traverses a AST tree and calls visit(AST) method of aVisitor param for every
   * node.<P>
   * N.B! all siblings of the initial argument are also traversed.
   *
   * @param aNode a node to traverse
   * @param aVisitor visitor
   */
  public static void traverse(ASTImpl aNode, final ASTVisitor aVisitor) {
    for (; aNode != null; aNode = (ASTImpl) aNode.getNextSibling()) {
      aVisitor.visit(aNode);
      final ASTImpl child = (ASTImpl) aNode.getFirstChild();
      if (child != null) {
        ASTUtil.traverse(child, aVisitor);
      }
    }
  }

//  public static boolean nodesHaveOverlappingArea( ASTImpl a, ASTImpl b) {
//    return
//      insideAreaOfNode( a.getStartColumn(), a.getStartLine(), b ) ||
//      insideAreaOfNode( a.getEndColumn(), a.getEndLine(), b ) ||
//      insideAreaOfNode( b.getStartColumn(), b.getStartLine(), a ) ||
//      insideAreaOfNode( b.getEndColumn(), b.getEndLine(), a );
//  }
//
//  public static boolean insideAreaOfNode( int column, int line, ASTImpl node ) {
//    return LinePositionUtil.between( column, line,
//      node.getStartColumn(), node.getStartLine(), node.getEndColumn(), node.getEndLine() );
//  }

  public static String intern(final byte type, final String text) {
    if (text == null) {
      return null;
    }

    switch (type) {
      case (127 - NUM_INT):
      case (127 - NUM_LONG):
      case (127 - NUM_DOUBLE):
      case (127 - NUM_FLOAT):
      case (127 - HEX_DIGIT):
      case (127 - CHAR_LITERAL):
      case (127 - STRING_LITERAL):
      case (127 - WS):
        if (text.length() == 1) {
          return FastJavaLexer.CHARS[text.charAt(0)];
        } else {
          return text;
        }

      case (127 - SL_COMMENT):
      case (127 - ML_COMMENT):
        return text;

//      case (127 - STRING_LITERAL):
//        if (text.length() <= 7) {
//          return text.intern();
//        } else {
//          return text;
//        }

      case IDENT:
        if (text.length() == 1) {
          return FastJavaLexer.CHARS[text.charAt(0)];
        } else {
//        if (text.length() <= 7) {
          return text.intern();
//        } else {
//          return text;
//        }
        }

//      case (127 - LITERAL_instanceof):
//      case (127 - LITERAL_true):
//      case (127 - LITERAL_false):
//      case (127 - LITERAL_null):
//      case (127 - LITERAL_new):
//      case (127 - SR_ASSIGN):
//      case (127 - BSR_ASSIGN):
//      case (127 - SL_ASSIGN):
//      case LITERAL_package:
//      case LITERAL_import:
//      case LITERAL_static:
//      case LITERAL_extends:
//      case LITERAL_super:
//      case LITERAL_void:
//      case LITERAL_boolean:
//      case LITERAL_byte:
//      case LITERAL_char:
//      case LITERAL_short:
//      case LITERAL_int:
//      case LITERAL_float:
//      case LITERAL_long:
//      case LITERAL_double:
//      case LITERAL_private:
//      case LITERAL_public:
//      case LITERAL_protected:
//      case LITERAL_transient:
//      case LITERAL_native:
////      case LITERAL_threadsafe:
//      case LITERAL_synchronized:
//      case LITERAL_volatile:
//      case LITERAL_class:
//      case LITERAL_interface:
//      case LITERAL_enum:
//      case LITERAL_default:
//      case LITERAL_implements:
//      case LITERAL_this:
//      case LITERAL_throws:
//      case LITERAL_if:
//      case LITERAL_else:
//      case LITERAL_while:
//      case LITERAL_do:
//      case LITERAL_break:
//      case LITERAL_continue:
//      case LITERAL_return:
//      case LITERAL_switch:
//      case LITERAL_throw:
//      case LITERAL_assert:
//      case LITERAL_for:
//      case LITERAL_case:
//      case LITERAL_try:
//      case LITERAL_finally:
//      case LITERAL_catch:
//      case BSR:
      default:
      {
        final int ind;
        if (type >= 0) {
          ind = type;
        } else {
          ind = 127 - type;
        }
        String name = TOKEN_TEXT[ind];
        if (name == null) {
          name = text.intern();
          TOKEN_TEXT[ind] = name;
        }
//        if (!name.equals(text)) {
//          System.err.println("AAAAA: " + type + " - " + text + " - " + name);
//          name = text;
//        }
        return name;
      }

//      default:
////        String name;
////        if (type >= 0) {
////          name = JavaRecognizer._tokenNames[type];
////        } else {
////          name = JavaRecognizer._tokenNames[127 - type];
////        }
////        if (name.charAt(0) == '\"') { // literal
////          return name.substring(1, name.length() - 1);
////        }
////
//        if (text.length() < 3) { // operators get here
//          return text.intern();
//        } else {
//          return text;
//        }
    }
  }

  /**
   * delete all ANNOTATION nodes from list
   * @param modifierNodes
   */
  public static void setAnnotationsNodesAsCompoundNodes(List modifierNodes) {
    for(int i = 0; i < modifierNodes.size(); i++) {
      ASTImpl node = (ASTImpl)modifierNodes.get(i);
      if(node.getType() == ANNOTATION) {
        modifierNodes.set(i, new CompoundASTImpl(node));
      }
    }
  }
}
