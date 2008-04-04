/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.html;

import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;


public class HTMLASTIndexer implements JavaTokenTypes {

  /* The parent indexer */
  private HTMLLinkIndexer indexer = null;

  public HTMLASTIndexer(HTMLLinkIndexer indexer) {
    setIndexer(indexer);
  }

  public void visitAST(ASTImpl node) {

    for (ASTImpl current = node; current != null;
        current = (ASTImpl) current.getNextSibling()) {

      // Descend as needed
      if (current.getFirstChild() != null) {
        visitAST((ASTImpl) current.getFirstChild());
      }

      HTMLEntity entity = getEntityForAST(current);

      // Attach to list if needed
      if (entity != null) {
        getIndexer().addEntity(entity);
      }
    }
  }

  private HTMLEntity getEntityForAST(ASTImpl node) {
    int type = node.getType();
    int data = -1;

    // if(node.getType() == EXTENDS_CLAUSE){
    //	System.out.println("\'"+node.getText()+"\' "+(node.getLine())+"/"+(node.getColumn()));
    // }


    // Detect keyword
    keyword: {
      int[] keywords = HTMLASTIndexer.keywords;

      for (int pos = 0, max = keywords.length; pos < max; pos++) {
        data = (type - keywords[pos]);

        // Detect match
        if (data == 0) {
          return createKeywordEntity(node);
        }

        // Break the loop
        if (data <= 0) {
          break keyword;
        }
      }
    }

    // Detect literal:
    literal: {
      int[] literals = HTMLASTIndexer.literals;

      for (int pos = 0, max = literals.length; pos < max; pos++) {
        data = (type - literals[pos]);

        // Detect match
        if (data == 0) {
          return createLiteralEntity(node);
        }
      }
    }

    // Detect numeric
    numeric: {
      int[] numbers = HTMLASTIndexer.numbers;

      for (int pos = 0, max = numbers.length; pos < max; pos++) {
        data = (type - numbers[pos]);

        // Detect match
        if (data == 0) {
          return createNumericEntity(node);
        }
      }
    }

    // Not found
    return null;
  }

  private HTMLEntity createKeywordEntity(ASTImpl node) {
    return createStyledEntity(node, STYLE_KEYWORD);
  }

  private HTMLEntity createNumericEntity(ASTImpl node) {
    return createStyledEntity(node, STYLE_NUMERIC);
  }

  private HTMLEntity createLiteralEntity(ASTImpl node) {
    return createStyledEntity(node, STYLE_LITERAL);
  }

  private HTMLEntity createStyledEntity(ASTImpl node, String style) {
    return (new HTMLCodeEntity(node, style));
  }

  //
  // Accesso methods
  //

  public HTMLLinkIndexer getIndexer() {
    return this.indexer;
  }

  public void setIndexer(HTMLLinkIndexer indexer) {
    this.indexer = indexer;
  }

  //
  // Class variables
  //

  /* Style constants */
  private static final String STYLE_KEYWORD = "kw";
  private static final String STYLE_NUMERIC = "nm";
  private static final String STYLE_LITERAL = "lt";

  // The list of 'interesting' token types
  private static final int[] keywords = {
      LITERAL_import,
      LITERAL_package,
      LITERAL_void,
      LITERAL_boolean,
      LITERAL_byte,
      LITERAL_char,
      LITERAL_short,
      LITERAL_int,
      LITERAL_float,
      LITERAL_long,
      LITERAL_double,
      LITERAL_private,
      LITERAL_public,
      LITERAL_protected,
      LITERAL_static,
      LITERAL_transient,
      LITERAL_native,
//      LITERAL_threadsafe,
      LITERAL_synchronized,
      LITERAL_volatile,
      STRICTFP,
      LITERAL_class,
      LITERAL_extends,
      LITERAL_interface,
      LITERAL_implements,
      LITERAL_throws,
      LITERAL_if,
      LITERAL_else,
      LITERAL_for,
      LITERAL_while,
      LITERAL_do,
      LITERAL_break,
      LITERAL_continue,
      LITERAL_return,
      LITERAL_switch,
      LITERAL_throw,
      LITERAL_case,
      LITERAL_try,
      LITERAL_finally,
      LITERAL_catch,
      LITERAL_instanceof,
      LITERAL_this,
      LITERAL_super,
      LITERAL_true,
      LITERAL_false,
      LITERAL_null,
      LITERAL_new};

  private static final int[] numbers = {
      NUM_INT,
      NUM_FLOAT,
      HEX_DIGIT,
      EXPONENT,
      FLOAT_SUFFIX};

  private static final int[] literals = {
      CHAR_LITERAL,
      STRING_LITERAL};

}
