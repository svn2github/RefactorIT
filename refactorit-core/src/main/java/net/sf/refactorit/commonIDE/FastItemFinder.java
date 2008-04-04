/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.classmodel.BinAnnotation;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinEnum;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTImplFactory;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.CommentStoringFilter;
import net.sf.refactorit.parser.ErrorListener;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.parser.JavaRecognizer;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.OptimizedJavaRecognizer;
import net.sf.refactorit.parser.TreeASTImpl;

import rantlr.RecognitionException;
import rantlr.TokenStreamException;

import java.util.ArrayList;
import java.util.List;


/** Finds BinItem by analyzing AST tree. */
public final class FastItemFinder implements JavaTokenTypes {
  private FastItemFinder() {}

  /**
   * Determines the BinClass that corresponds to the symbol at location <code>row</code>
   * : <code>col</code>.  Returns <code>null</null> if the BinClass cannot be determined or
   * if there is no symbol at the given location. Returns multiple classes if the
   * class under cursor cannot be determined with enough certainty (e.g. interface
   * and class).
   *
   * @param fileName source file name
   * @param sourceContent source file content
   * @param row current row
   * @param col current column
   * @throws Exception
   * @return array of classes
   */
  public static Class[] getCurrentBinClass(final String fileName,
      final String sourceContent, final int row,
      final int col) throws Exception {
    final ASTImpl ast = getCurrentAst(fileName, sourceContent, row, col);
    return getBinClassesForAst(ast);
  }

  /**
   * Finds a BinClass that corresponds to the given AST.
   * Returns <code>null</null> if the BinClass cannot be determined or
   * if the AST does not correspond to any BinClass.
   *
   * @param ast node
   * @return BinClass that corresponds to the given AST
   */
  public static Class[] getBinClassesForAst(final ASTImpl ast) {
    if (ast == null) {
      return null;
    }
  //System.err.println("ast: " + ast + ", parent: " + ast.getParent());
    if (ast.getType() == IDENT) {
      final ASTImpl parent = ast.getParent();
      switch (parent.getType()) {
        case PACKAGE_DEF:
          return new Class[] {BinPackage.class};

        case INTERFACE_DEF:
        case IMPLEMENTS_CLAUSE:
          return new Class[] {BinInterface.class};

        case EXTENDS_CLAUSE:
        case CLASS_DEF:
        case LITERAL_new:
          return new Class[] {BinClass.class};

        case IMPORT:
        case TYPE:
        case ARRAY_DECLARATOR:
          return new Class[] {BinClass.class, BinInterface.class};

        case METHOD_DEF:
        case METHOD_CALL:
          return new Class[] {BinMethod.class};

        case CTOR_DEF:
          return new Class[] {BinConstructor.class};

        case PARAMETER_DEF:
          return new Class[] {BinParameter.class};

        case DOT:
          return getClassesForDotChildAst(ast);

        case LITERAL_throws:
          return new Class[] {BinMethod.Throws.class};

        case ENUM_DEF:
          return new Class[] {BinEnum.class};

        case ENUM_CONSTANT_DEF:
          return new Class[] {BinEnumConstant.class};

        case ANNOTATION_DEF:
          return new Class[] {BinAnnotation.class};

        default:
          return new Class[] {getBinFieldOrVariable(ast)};
      }
    } else if (ast.getType() == LITERAL_throw) {
      return new Class[] {BinThrowStatement.class};
    } else {
      return null;
    }
  }

  /**
   * Tries to determine whether the ast corresponds to a field or variable
   * or parameter.
   * @param ast node
   * @return bin class
   */
  private static Class getBinFieldOrVariable(final ASTImpl ast) {
    final String name = ast.getText();
    final List parameters = getParameterNames(ast);
    final List variables = getVariableNames(ast);
    if ((variables != null) && getVariableNames(ast).contains(name)) {
      return BinLocalVariable.class;
    } else if ((parameters != null) && getParameterNames(ast).contains(name)) {
      return BinParameter.class;
    } else {
      return BinField.class;
    }
  }

  private static List getVariableNames(final ASTImpl ast) {
    final List list = new ArrayList();
    compileVariableNames(ast, list);
    return list;
  }

  private static void compileVariableNames(final ASTImpl ast,
      final List nameList) {
    if (ast.getType() == CLASS_DEF || ast.getType() == ENUM_DEF
        || ast.getType() == ANNOTATION_DEF
        || ast.getType() == INTERFACE_DEF) {
      // we were not inside a method
      //nameList = null; // not used further
      return;
    }
    if ((ast == null) || (ast.getType() == METHOD_DEF)) {
      return;
    }
    //System.out.println("compileVariableNames: " + ast);
    final ASTImpl parent = ast.getParent();
    if ((parent.getType() == SLIST) || (parent.getType() == FOR_INIT)) {
      addVariableNames(parent, ast, nameList);
    } else if (parent.getType() == LITERAL_for) {
      final ASTImpl forInitAst = ASTUtil.getFirstChildOfType(parent, FOR_INIT);
      addVariableNames(forInitAst, null, nameList);
    }
    compileVariableNames(parent, nameList);
  }

  private static void addVariableNames(final ASTImpl parent,
      final ASTImpl until, final List names) {
    ASTImpl sibling = (ASTImpl) parent.getFirstChild();
    while (sibling != null) {
      if (sibling.getType() == VARIABLE_DEF) {
        final ASTImpl identAst = ASTUtil.getFirstChildOfType(sibling, IDENT);
        names.add(identAst.getText());
      }
      if (sibling.equals(until)) {
        break;
      }
      sibling = (ASTImpl) sibling.getNextSibling();
    }
  }

  private static List getParameterNames(final ASTImpl ast) {
    if (ast == null || ast.getType() == CLASS_DEF
        || ast.getType() == INTERFACE_DEF
        || ast.getType() == ENUM_DEF
        || ast.getType() == ANNOTATION_DEF) {
      return null;
    }

    if (ast.getType() == METHOD_DEF) {
      final ASTImpl parametersAst = ASTUtil.getFirstChildOfType(ast, PARAMETERS);
      if (parametersAst != null) {
        final List names = new ArrayList();
        ASTImpl parameterDefAst = (ASTImpl) parametersAst.getFirstChild();
        while (parameterDefAst != null) {
          final ASTImpl identAst = ASTUtil.getFirstChildOfType(parameterDefAst,
              IDENT);
          names.add(identAst.getText());
          parameterDefAst = (ASTImpl) parameterDefAst.getNextSibling();
        }
        return names;
      }

      return null;
    }

    return getParameterNames(ast.getParent());
  }

  private static Class[] getClassesForDotChildAst(final ASTImpl ast) {
//    final ASTImpl parent = ast.getParent();
    ASTImpl topDot = ast;
    do {
      topDot = topDot.getParent();
    } while (topDot.getParent().getType() == DOT);

    final ASTImpl top = topDot.getParent();
    final ASTImpl lastChild = (ASTImpl) topDot.getFirstChild().getNextSibling();

    if (top.getType() == PACKAGE_DEF) {
      return new Class[] {BinPackage.class};
    }

    if ((top.getType() == IMPLEMENTS_CLAUSE)
        || (top.getType() == EXTENDS_CLAUSE)
        || (top.getType() == IMPORT)
        || (top.getType() == TYPE)
        || (top.getType() == LITERAL_new)) {
      // current ast is class if it's the last child of the top dot ast
      if (lastChild.equals(ast)) {
        switch (top.getType()) {
          case EXTENDS_CLAUSE:
          case LITERAL_new:
            return new Class[] {BinClass.class};
          case IMPLEMENTS_CLAUSE:
            return new Class[] {BinInterface.class};
          default:
            return new Class[] {BinClass.class, BinInterface.class};
        }
      }

      return new Class[] {BinPackage.class};
    }

    if (top.getType() == METHOD_CALL) {
      // current ast is method if it's the last child of the top dot ast
      if (lastChild.equals(ast)) {
        return new Class[] {BinMethod.class};
      }

      // FIXME
      // here we dont't know:
      // it could be field: field.getName()
      // or type: SomeClass.staticMethod()
      // or even package: com.acme.SomeClass.staticMethod()
      //
      // Seems to be not fixable with our current ASTs, only classmodel knows
      // exactly what is here
      return null;
    }

    if ((top.getType() == ASSIGN) || (top.getType() == EXPR)) {
      if (lastChild.equals(ast)) {
        return new Class[] {BinField.class};
      }

      // FIXME
      // here we dont't know:
      // it could be field: member.field
      // or type: SomeClass.field
      // or even package: com.acme.SomeClass.field
      //
      // Seems to be not fixable with our current ASTs, only classmodel knows
      // exactly what is here
      return null;
    }

    if (top.getType() == LITERAL_throws) {
      return new Class[] {BinMethod.Throws.class};
    }

    return null;
  }

  public static ASTImpl getCurrentAst(final String fileName,
      final String sourceContent, final int row,
      final int col) throws RecognitionException, TokenStreamException {
    final ASTTree tree = sourceToAstTree(fileName, sourceContent);
    return getCurrentAstFromTree(tree, row, col);
  }

  public static ASTImpl getCurrentAstFromTree(final ASTTree tree, final int row,
      final int col) {
    ASTImpl currentAst
        = FastItemFinder.getIdentAstForLocation(tree.getAstAt(0), row, col);
    if (currentAst == null) {
      currentAst = FastItemFinder.getForLocation(tree.getAstAt(0), row, col);
      if (currentAst != null
          && currentAst.getType() != LITERAL_throw) {
        currentAst = null;
      }
    }
    return currentAst;
  }

  /**
   * @param fileName file name
   * @param sourceContent sourceContent
   * @return ast tree
   * @throws RecognitionException
   * @throws TokenStreamException
   */
  private static ASTTree sourceToAstTree(
      final String fileName,
      final String sourceContent) throws RecognitionException,
      TokenStreamException {

    final ErrorListener errorListener = new ErrorListener() {
      boolean hadError = false;
      public void onError(final String message, final String fileName,
          final int line, final int column) {
        hadError = true;
      }

      public boolean hadErrors() {
        return hadError;
      }
    };

    // FIXME check project from IDEController and use it if available

    OptimizedJavaRecognizer.setErrorListener(errorListener);
    final ASTTree tree = new ASTTree(sourceContent.length());
    net.sf.refactorit.parser.ASTImplFactory.getInstance().setTree(tree);
    final FastJavaLexer lexer = new FastJavaLexer(
        /*new ByteArrayInputStream(*/sourceContent/*)*/);
    lexer.setFilename(fileName);
    final CommentStoringFilter filter = new CommentStoringFilter(lexer);
    final JavaRecognizer parser = new JavaRecognizer(filter);
    parser.setFilename(fileName);
    parser.setASTFactory(net.sf.refactorit.parser.ASTImplFactory.getInstance());
    parser.compilationUnit();

    ASTImplFactory.getInstance().setTree(null);
    tree.recompress((TreeASTImpl) parser.getAST());

    return tree;
  }

  public static ASTImpl getIdentAstForLocation(final ASTImpl root,
      final int line, final int column) {

    for (ASTImpl current = root, child; current != null;
        current = (ASTImpl) current.getNextSibling()) {
      if (current.getLine() > line) {
        return null;
      }
      child = (ASTImpl) current.getFirstChild();

      // Check for children
      if (child != null) {
        child = getIdentAstForLocation(child, line, column);

        // Check the outcome
        if (child != null) {
          return child;
        }
      }
      if (current.getType() == JavaTokenTypes.IDENT) {
        // Check the node itself
        if (current.getLine() == line) {
          // Compare columns
          if (current.getColumn() <= column && current.getEndColumn() > column) {
            return current;
          }
        }
      }
    }

    // Not found
    return null;
  }

  public static ASTImpl getForLocation(final ASTImpl root, final int line,
      final int column) {

    for (ASTImpl current = root, child; current != null;
        current = (ASTImpl) current.getNextSibling()) {
      child = (ASTImpl) current.getFirstChild();

      // Check for children
      if (child != null) {
        child = getForLocation(child, line, column);

        // Check the outcome
        if (child != null) {
          return child;
        }
      }

      //System.err.print(" " + current.getText()+"("+current.getLine()+","+current.getColumn()+")");
      // Check the node itself
      if (current.getLine() == line) {

        // Compare columns
        if (current.getColumn() <= column && current.getEndColumn() > column) {

          //System.err.println("FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
          // Return success
          return current;
        }
      }
    }

    // Not found
    return null;
  }
}
