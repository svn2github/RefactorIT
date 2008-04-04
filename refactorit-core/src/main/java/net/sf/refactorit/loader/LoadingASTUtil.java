/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;

import rantlr.collections.AST;

import java.util.ArrayList;



/**
 * Utility AST functions for source loading.
 */
public final class LoadingASTUtil implements JavaTokenTypes {

  private static final StringBuffer BUFFER = new StringBuffer(256);

  public static final boolean optimized = true;

  private LoadingASTUtil() {}


  /**
   * Extracts type's name from TYPEDEF AST node
   * @param classNode classNode
   * @return type name
   */
  public static String getTypeNameFromDef(final ASTImpl classNode) {
    final ASTImpl typeNode = getTypeNodeFromDef(classNode);

    return (typeNode != null ? typeNode.getText() : null);
  }

  public static String combineIdentsAndDots(final ASTImpl dotNode,
      final ASTImpl lastIn) {
    BUFFER.setLength(0);
  //    final StringBuffer retVal = new StringBuffer(50);
    combineIdentsAndDots(dotNode, lastIn, BUFFER);
    return BUFFER.substring(0);
  }

  public static String combineIdentsAndDots(final ASTImpl dotNode) {
    BUFFER.setLength(0);
  //    final StringBuffer retVal = new StringBuffer(50);
    combineIdentsAndDots(dotNode, BUFFER);
    return BUFFER.substring(0);
  }

  /**
   * FIXME: needs testing
   *	Combines an AST dotNode, containing DOTs and IDENTs, into a dot separated string
   *
   * @param dotNode dotNode
   * @param lastIn lastIn
   * @param retVal retVal
   * @return true on success
   */
  public static boolean combineIdentsAndDots(
      final ASTImpl dotNode, final ASTImpl lastIn, final StringBuffer retVal) {
    if (dotNode == lastIn) {
      retVal.append(dotNode.getText());
      return true;
    }

    ASTImpl child = (ASTImpl) dotNode.getFirstChild();

    if (child == null) {
      if (Assert.enabled) {
        assertDotChildType(dotNode);
      }
      retVal.append(dotNode.getText());
    } else {
      boolean isFirst = true;
      for (; child != null; child = (ASTImpl) child.getNextSibling()) {
        if (!isFirst) {
          retVal.append('.');
        }
        isFirst = false;

        if (child == lastIn) {
          retVal.append(child.getText());
          return true;
        }

        if (Assert.enabled) {
          assertDotChildType(child);
        }
        final boolean isFinish = LoadingASTUtil.combineIdentsAndDots(child, lastIn,
            retVal);
        if (isFinish) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Combines an AST dotNode, containing DOTs and IDENTs,
   * into a dot separated string.
   */
  public static void combineIdentsAndDots(
      final ASTImpl node, final StringBuffer retVal) {
    ASTImpl child = (ASTImpl) node.getFirstChild();

    if (child == null || child.getType() == TYPE_ARGUMENTS) {
      // ignore Type_Arguments - will handle it later on the upper level

      if (Assert.enabled) {
        assertDotChildType(node);
      }
      retVal.append(node.getText());
    } else {
      boolean isFirst = true;
      for (; child != null; child = (ASTImpl) child.getNextSibling()) {
        if (child.getType() == TYPE_ARGUMENTS) {
//new rantlr.debug.misc.ASTFrame("args2", node).setVisible(true);
          continue;
        }

        if (!isFirst) {
          retVal.append('.');
        }
        isFirst = false;
        if (Assert.enabled) {
          assertDotChildType(child);
        }
        combineIdentsAndDots(child, retVal);
      }
    }
  }

  public static String combineIdentsDotsAndStars(final ASTImpl dotNode) {
    BUFFER.setLength(0);
  //    final StringBuffer buffer = new StringBuffer(50);
    combineIdentsDotsAndStars(dotNode, BUFFER);
    return BUFFER.substring(0);
  }

  /**
   * Combines an AST dotNode, containing DOTs, IDENTs and STARs, into a dot
   * separated string
   * @param dotNode dotNode
   * @param buffer buffer
   */
  public static void combineIdentsDotsAndStars(
      final ASTImpl dotNode, final StringBuffer buffer) {
    ASTImpl child = (ASTImpl) dotNode.getFirstChild();
    if (child == null) {
      if (Assert.enabled) {
        assertDotChildType(dotNode);
      }

      buffer.append(dotNode.getText());
    } else {
      boolean isFirst = true;
      for (; child != null; child = (ASTImpl) child.getNextSibling()) {
        if (!isFirst) {
          buffer.append('.');
        }
        isFirst = false;
        if (Assert.enabled) {
          assertDotChildType(child);
        }
        combineIdentsDotsAndStars(child, buffer);
      }
    }

  }

  private static void assertDotChildType(final ASTImpl child) {
  // JAVA5: check the check
  //    final int childType = child.getType();
  //    if (!isPartOfIdent(childType)) {
  //      Assert.must(false,
  //          "DOT nodes must contain only DOT, IDENT, STAR, WILDCARD_TYPE nodes: "
  //          + childType + ", \'" + child.getText() + "\'");
  //    }
  }

  public static int extractTypeModifiers(final ASTImpl typeNode) {
    int modifiers = 0;
    ASTImpl modifierNode = (ASTImpl) typeNode.getFirstChild();
    while (modifierNode != null && modifierNode.getType() != MODIFIERS) {
      modifierNode = (ASTImpl) modifierNode.getNextSibling();
    }
    modifiers |= LoadingASTUtil.getModifiersForAST(modifierNode);
    return modifiers;
  }

  public static int getModifiersForAST(final ASTImpl modifiersNode) {
    if (Assert.enabled) {
      final int modifiersNodeType = modifiersNode.getType();
      if (modifiersNodeType != MODIFIERS) {
        Assert.must(false,
            "not MODIFIERS node: " + modifiersNodeType);
      }
    }
    int retVal = 0;
    ASTImpl modifierNode = (ASTImpl) modifiersNode.getFirstChild();

    // JAVA5: this node should be handled separately at the upper level
//    while(modifierNode != null && modifierNode.getType() == ANNOTATION) {
//      modifierNode = (ASTImpl) modifierNode.getNextSibling();
//    }

    while (modifierNode != null) {
      if(modifierNode.getType() != ANNOTATION) {
        retVal |= ASTUtil.getModifierForAST(modifierNode);
      }
      modifierNode = (ASTImpl) modifierNode.getNextSibling();
    }
    return retVal;
  }

  /**
   * Extracts a type-defining AST node from TYPEDEF AST node.
   * @param classNode classNode
   * @return type node
   */
  public static ASTImpl getTypeNodeFromDef(final ASTImpl classNode) {
    for (ASTImpl child = (ASTImpl) classNode.getFirstChild(); child != null;
        child = (ASTImpl) child.getNextSibling()) {

      if (child.getType() == IDENT) {
        return child;
      }
    }

    return null;
  }

  public static int getArrayDimensions(ASTImpl node) {
    int count = 0;

    while (node != null && node.getType() == ARRAY_DECLARATOR) {
      count++;

      // Descend
      node = (ASTImpl) node.getFirstChild();
    }

    // Return result
    return count;
  }

  /**
   * Extracts package subtree from PACKAGEDEF or IMPORT
   * @param dotNode dotNode
   * @return first child of given dot node
   */
  public static ASTImpl getFirstChildOfDot(final ASTImpl dotNode) {
    ASTImpl packageNode = (ASTImpl) dotNode.getFirstChild();
    while (packageNode != null && packageNode.getType() == ANNOTATIONS) {
      packageNode = (ASTImpl) packageNode.getNextSibling();
    }
    if (packageNode != null
        && (packageNode.getType() == DOT || packageNode.getType() == IDENT)) {
      return packageNode;
    } else {
      return null;
    }
  }

  /**
   * Adds all ident nodes in dot into an array. Does not fail when it contains STAR nodes
   * just does not add them.
   *
   * Also important, that it adds them in correct order
   *
   * @param dotNode dotNode
   * @return array of asts
   */
  public static ASTImpl[] extractIdentNodesFromDot(final ASTImpl dotNode) {
    ASTImpl node = dotNode;

    final ArrayList nodes = new ArrayList(3);

    while (node.getType() == DOT) {
      AST firstChild = node.getFirstChild();
      final ASTImpl nodeToAdd = (ASTImpl) firstChild.getNextSibling();
      if (!"*".equals(nodeToAdd.getText())) {
        nodes.add(nodeToAdd);
      }

      node = (ASTImpl) firstChild;
    }

    if (node.getType() == IDENT && (!"*".equals(node.getText()))) {
      nodes.add(node);
    }

    int size = nodes.size();
    final ASTImpl[] result = new ASTImpl[size];
    int m = 0;
    int n = size - 1;
    while (n >= 0) {
      result[m++] = (ASTImpl) nodes.get(n--);
    }

    return result;
  }

  /**
   * Returns a string of package name for PACKAGEDEF or IMPORT node
   * @param expressionNode expressionNode
   * @return package string
   */
  public static String extractPackageStringFromExpression(
      final ASTImpl expressionNode) {

    ASTImpl identNode = (ASTImpl) expressionNode.getFirstChild();

    // JAVA5: there are also ANNOTATIONS node in the new Java
    while (identNode != null && !LoadingASTUtil.isPartOfIdent(identNode.getType())) {
      identNode = (ASTImpl) identNode.getNextSibling();
    }

    return LoadingASTUtil.combineIdentsDotsAndStars(identNode);
  }

  /**
   * Returns a packagename from import statement or empty string if import
   * statement was invalid
   *
   * @param importName importName
   * @return package name
   */
  public static String extractUntilLastDot(final String importName) {
    final int lastDotI = importName.lastIndexOf('.');
    if (lastDotI != -1) {
      return importName.substring(0, lastDotI);
    } else {
      return "";
    }
  }

  public static String[] extractExtendsOrImplements(final ASTImpl node) {
    if (node != null) {
      final ArrayList _extends = new ArrayList(3);

      for (ASTImpl nameNode = (ASTImpl) node.getFirstChild();
          nameNode != null; nameNode = (ASTImpl) nameNode.getNextSibling()) {
        final String name = LoadingASTUtil.combineIdentsAndDots(nameNode);
        _extends.add(name);
      }

      return (String[]) _extends.toArray(new String[_extends.size()]);
    } else {
      return StringUtil.NO_STRINGS;
    }
  }

  public static ASTImpl findExtendsNode(ASTImpl typeNode) {
    ASTImpl extendsNode = (ASTImpl) typeNode.getFirstChild();
    while (extendsNode != null && extendsNode.getType() != EXTENDS_CLAUSE) {
      extendsNode = (ASTImpl) extendsNode.getNextSibling();
    }

    return extendsNode;
  }

  public static ASTImpl findImplementsNode(ASTImpl typeNode) {
    ASTImpl implementsNode = (ASTImpl) typeNode.getFirstChild();
    while (implementsNode != null && implementsNode.getType() != IMPLEMENTS_CLAUSE) {
      implementsNode = (ASTImpl) implementsNode.getNextSibling();
    }

    return implementsNode;
  }

  public static ASTImpl findTypeArgumentsNode(final ASTImpl typeNode) {
    ASTImpl deepNode = typeNode;

    while (deepNode != null) {
      ASTImpl breadthNode = deepNode;

      while (breadthNode != null
          && breadthNode.getType() != TYPE_ARGUMENTS) {
        breadthNode = (ASTImpl) breadthNode.getNextSibling();
      }
      if (breadthNode != null && breadthNode.getType() == TYPE_ARGUMENTS) {
        return breadthNode;
      }

      deepNode = (ASTImpl) deepNode.getFirstChild();
    }

    return null;
  }

  public static ASTImpl getStatementNode(ASTImpl node) {
    while (node != null && node.getType() != SLIST) {
      node = (ASTImpl) node.getNextSibling();
    }
    return node;
  }

  public static boolean isPartOfIdent(final int nodeType) {
    return nodeType == DOT
        || nodeType == IDENT
        || nodeType == STAR
        || nodeType == WILDCARD_TYPE;
  }

  public static void ASTDebugOn(final ASTImpl node) {
    final rantlr.debug.misc.ASTFrame f
        = new rantlr.debug.misc.ASTFrame("ASTDebugOn", node);
    f.show();
    System.err.println(node.toString());
  }

  public static ArrayList findDefNodesOfOneDeclaration(ASTImpl
      firstVariableDefInDeclaration) {
    ASTImpl variableDef = firstVariableDefInDeclaration;

    ArrayList result = new ArrayList(1);
    result.add(variableDef);
    while (nextSiblingInSameDeclaration(variableDef)) {
      variableDef = (ASTImpl) variableDef.getNextSibling();
      result.add(variableDef);
    }

    return result;
  }

  static boolean nextSiblingInSameDeclaration(final ASTImpl variableDef) {
    return variableDef.getNextSibling() != null
        && variableDef.getNextSibling().getType() == JavaTokenTypes.VARIABLE_DEF
        && ASTUtil.sameCoordinates(ASTUtil.getCommonTypeNode(
        (ASTImpl) variableDef.getNextSibling()),
        ASTUtil.getCommonTypeNode(variableDef));
  }

  public static ASTImpl findLastDefNodeOfDeclaration(ASTImpl variableDef) {
    while (nextSiblingInSameDeclaration(variableDef)) {
      variableDef = (ASTImpl) variableDef.getNextSibling();
    }

    return variableDef;
  }

  public static boolean isMathematicalOperationNode(final ASTImpl node) {
    switch (node.getType()) {
      case SL:
      case SR:
      case BSR:
      case PLUS:
      case MINUS:
      case DIV:
      case MOD:
      case INC:
      case DEC:
      case BNOT:
      case LNOT:
      case STAR:
        return true;
      default:
        return false;
    }
  }

  public static boolean isExplicitValue(final ASTImpl node) {
    switch (node.getType()) {
      case LITERAL_true:
      case LITERAL_false:
      case NUM_INT:
      case CHAR_LITERAL:
      case STRING_LITERAL:
      case NUM_FLOAT:
      case NUM_LONG:
      case NUM_DOUBLE:
        return true;
      default:
        return false;
    }
  }

  public static boolean isAssignmentNode(final ASTImpl node) {
    switch (node.getType()) {
      case ASSIGN:
      case PLUS_ASSIGN:
      case MINUS_ASSIGN:
      case STAR_ASSIGN:
      case DIV_ASSIGN:
      case MOD_ASSIGN:
      case SR_ASSIGN:
      case BSR_ASSIGN:
      case SL_ASSIGN:
      case BAND_ASSIGN:
      case BXOR_ASSIGN:
      case BOR_ASSIGN:
        return true;
      default:
        return false;
    }
  }

  public static boolean isComparisonNode(final ASTImpl node) {
    switch (node.getType()) {
      case NOT_EQUAL:
      case EQUAL:
      case LT:
      case GT:
      case LE:
      case GE:
        return true;
      default:
        return false;
    }
  }

  public static String getIdent(final ASTImpl identNode) {
    if (Assert.enabled) {
      Assert.must(identNode.getType() == IDENT,
          "Identifier expected !");
    }
    return identNode.getText();
  }
}
