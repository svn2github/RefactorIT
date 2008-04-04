/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;


import rantlr.ASTFactory;
import rantlr.Token;
import rantlr.collections.AST;


/**
 * Factory that creates proper ASTImpl node instances based on different
 * information provided by the parser.
 */
public final class ASTImplFactory extends ASTFactory {
  /** The one and only instance of this class. */
  private static final ASTImplFactory INSTANCE = new ASTImplFactory();

  private ASTTree tree = null;

  /**
   * Gets instance of this factory.
   *
   * @return instance. Never returns <code>null</code>.
   */
  public static ASTImplFactory getInstance() {
    return INSTANCE;
  }

  public void setTree(final ASTTree tree) {
    this.tree = tree;
  }

  /** Hidden constructor. */
  private ASTImplFactory() {}

  /** Overrides */
  public final AST create() {
    final TreeASTImpl result = createASTImpl();
//System.err.println("ast simple");
    return result;
  }

  /** Overrides */
  public final AST create(final AST tr) {
    if (tr == null) {
      return null;
    }

//System.err.println("ast from: " + tr);
    final TreeASTImpl result = createASTImpl();
    result.initialize(tr);
    return result;
  }

  /** Overrides */
  public final AST create(final int type) {
    final TreeASTImpl result = createASTImpl();
    result.initialize(type, "");
//System.err.println("ast: " + type);
    return result;
  }

  /** Overrides */
  public final AST create(final int type, final String txt) {
    final TreeASTImpl result = createASTImpl();
    result.initialize(type, txt);
//System.err.println("ast: " + type + " - " + txt);
    // this works when there is no imports and package is default
//    if (result.backRef.ownerTree.rootNode == null) {
//      result.backRef.ownerTree.rootNode = result;
//    }

    return result;
  }

  /** Overrides */
  public final AST create(final Token tok) {
    final TreeASTImpl result = createASTImpl();
    result.initialize(tok);
//System.err.println("ast from: " + tok);
    return result;
  }

  /**
   * Creates ASTImpl.
   *
   * @return ASTImpl instance. Never returns <code>null</code>.
   */
  public final TreeASTImpl createASTImpl() {
    return tree.createNewAst();
  }

  /** Overrides */
  public AST create(String className) {
    return create();
  }

  /** Overrides */
  public AST create(Token tok, String className) {
    return create(tok);
  }

  /** Overrides */
  public AST create(int type, String txt, String className) {
    return create(type, txt);
  }

  /** Overrides */
  protected AST createUsingCtor(Token token, String className) {
    return create(token);
  }

  /** Overrides */
  protected AST create(Class c) {
    return create();
  }

}
