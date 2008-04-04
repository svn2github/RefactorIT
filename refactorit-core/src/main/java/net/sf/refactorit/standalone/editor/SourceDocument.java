/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.source.SourceHolder;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import java.awt.Color;
import java.awt.Font;
import java.util.List;


public class SourceDocument extends DefaultStyledDocument {
  private Style style;

  private Style comment;
  private Style keyword;
  private Style literal;
  private Style number;

  /* The source where the data is originating from */
  private SourceHolder source;

  /* The time in milliseconds when the document was last accessed/loaded */
  private long modified = -1L;

  public SourceDocument() {
    initStyles();
  }

  private void initStyles() {
    style = addStyle("default", null);

    comment = addStyle("comment", style);
    StyleConstants.setForeground(comment, Color.gray);

    keyword = addStyle("keyword", style);
    StyleConstants.setForeground(keyword, Color.blue);

    literal = addStyle("literal", style);
    StyleConstants.setForeground(literal, new Color(0, 180, 0));

    number = addStyle("number", style);
    StyleConstants.setForeground(number, Color.red);
  }

  /* *********************************** */
  /*        START OF MISTIC CODE         */
  /* *********************************** */

  public SourceDocument(CompilationUnit sf) {
    initStyles();
    setSource(sf);
  }

  //
  // Accessor methods
  //

  public SourceHolder getSource() {
    return source;
  }

  public void setSource(final SourceHolder source) {
    if (source == null || source.getSource() == null) {
      this.source = null;
      this.modified = 0;
      updateContent();
      return;
    }

    final long mod = source.getSource().lastModified();

    if (this.source == source && this.modified == mod) {
      return;
    }

    this.source = source;
    this.modified = mod;
    updateContent();
  }

  /**
   * Loads data from associated DataSource.
   */
  private void updateContent() {
    // Drop existing data
    try {
      remove(0, getLength());

      if (source != null) {
        insertString(0, readSource(), null);
        //FIXME:syntax highlight for other sources than java
        if (source.getName().endsWith(".java")) {
          insertSyntaxHighlights();
        }
      }
    } catch (BadLocationException ignore) {}
  }

//  private void insertSyntaxHighlightsTest() throws BadLocationException {
//    BinItemFinder f = new BinItemFinder(compilationUnit);
//    LineIndexer x = compilationUnit.getLineIndexer();
//
//    Class []cls = {
//      BinMethod.class, // hall
//      BinMethodInvocationExpression.class, // hall
//      BinField.class, // sinine
//      BinFieldInvocationExpression.class, // sinine
//      BinParameter.class, // sinine
//      BinLocalVariable.class, // sinine
//      BinType.class, // roheline
//      BinFieldInvocationExpression.class // punane
//    };
//
//    Style []styles = {comment, comment, keyword, keyword,keyword, keyword, literal, number};
//
//    for(int i = 0 ; i < getLength() ; ++i) {
//      SourceCoordinate sc = x.posToLineCol(i);
//      Object o = f.findItemAt( sc );
//      boolean found = false;
//      for(int s = 0 ; s < cls.length ; ++s) {
//        if(cls[s].isInstance(o)) {
//          colorDot(i-1, styles[s]);
//          found = true;
//          break;
//        }
//      }
//      if(!found && o != null  ) System.out.println(o.getClass());
//
//    }
//  }
//
//  private void colorDot( int place, Style style ) {
//    setCharacterAttributes(place, 1, style, true);
//  }

  private void insertSyntaxHighlights() throws BadLocationException {
    final LineIndexer indexer = getSource().getSource().getLineIndexer();

    try {
      final List comments = ((CompilationUnit) getSource()).getSimpleComments();
      for (int i = 0, max = comments.size(); i < max; ++i) {
        simpleApply(indexer, (Comment) comments.get(i), comment);
      }
    } catch (NullPointerException e) {
      // failed to parse
    } catch (ClassCastException e) {
      // not yet parsed source
    }

    try {
      final List comments = ((CompilationUnit) getSource()).getJavadocComments();
      for (int i = 0, max = comments.size(); i < max; ++i) {
        simpleApply(indexer, (Comment) comments.get(i), comment);
      }
    } catch (NullPointerException e) {
      // failed to parse
    } catch (ClassCastException e) {
      // not yet parsed source
    }

    try {
      final ASTTree astTree = source.getSource().getASTTree();
      if (astTree != null) {
        for (int i = 0, max = astTree.getASTCount(); i < max; i++) {
          switch (astTree.getAstType(i)) {
            case JavaTokenTypes.STRING_LITERAL:
            case JavaTokenTypes.CHAR_LITERAL:
              simpleApply(indexer, astTree, i, literal);
              break;
            case JavaTokenTypes.LITERAL_if:
            case JavaTokenTypes.LITERAL_else:
            case JavaTokenTypes.LITERAL_for:
            case JavaTokenTypes.LITERAL_while:
            case JavaTokenTypes.LITERAL_do:
            case JavaTokenTypes.LITERAL_break:
            case JavaTokenTypes.LITERAL_continue:
            case JavaTokenTypes.LITERAL_return:
            case JavaTokenTypes.LITERAL_switch:
            case JavaTokenTypes.LITERAL_throw:
            case JavaTokenTypes.LITERAL_case:
            case JavaTokenTypes.LITERAL_default:
            case JavaTokenTypes.LITERAL_try:
            case JavaTokenTypes.LITERAL_finally:
            case JavaTokenTypes.FINAL:
            case JavaTokenTypes.ABSTRACT:
            case JavaTokenTypes.PACKAGE_DEF:
            case JavaTokenTypes.IMPORT:
            case JavaTokenTypes.LITERAL_void:
            case JavaTokenTypes.LITERAL_boolean:
            case JavaTokenTypes.LITERAL_byte:
            case JavaTokenTypes.LITERAL_char:
            case JavaTokenTypes.LITERAL_short:
            case JavaTokenTypes.LITERAL_int:
            case JavaTokenTypes.LITERAL_float:
            case JavaTokenTypes.LITERAL_long:
            case JavaTokenTypes.LITERAL_double:
            case JavaTokenTypes.LITERAL_private:
            case JavaTokenTypes.LITERAL_public:
            case JavaTokenTypes.LITERAL_protected:
            case JavaTokenTypes.LITERAL_static:
            case JavaTokenTypes.LITERAL_transient:
            case JavaTokenTypes.LITERAL_native:
//            case JavaTokenTypes.LITERAL_threadsafe:
            case JavaTokenTypes.LITERAL_synchronized:
            case JavaTokenTypes.LITERAL_volatile:
            case JavaTokenTypes.STRICTFP:
            case JavaTokenTypes.LITERAL_extends:
            case JavaTokenTypes.LITERAL_class:
            case JavaTokenTypes.LITERAL_interface:
            case JavaTokenTypes.LITERAL_throws:
            case JavaTokenTypes.LITERAL_instanceof:
            case JavaTokenTypes.LITERAL_this:
            case JavaTokenTypes.LITERAL_super:
            case JavaTokenTypes.LITERAL_true:
            case JavaTokenTypes.LITERAL_false:
            case JavaTokenTypes.LITERAL_null:
            case JavaTokenTypes.LITERAL_new:
              simpleApply(indexer, astTree, i, keyword);
              break;
            case JavaTokenTypes.NUM_INT:
            case JavaTokenTypes.NUM_FLOAT:
            case JavaTokenTypes.HEX_DIGIT:
            case JavaTokenTypes.EXPONENT:
            case JavaTokenTypes.FLOAT_SUFFIX:
              simpleApply(indexer, astTree, i, number);
              break;
          }
        }
      }
    } catch (Exception e) {
      // FIXME: does this happen when parsing totally crashed?
      // should smth be done or just ignore it?
    }
  }

  private void simpleApply(LineIndexer indexer, ASTTree tree, int index,
      Style style) {
    try {
      int start = indexer.lineColToPos(
          tree.getAstStartLine(index), tree.getAstStartColumn(index));
      int length = tree.getAstText(index).length();
      setCharacterAttributes(start, length, style, true);
    } catch (IllegalArgumentException e) {
      // skip
    }
  }

  private void simpleApply(LineIndexer indexer, Comment aNode, Style style) {
    try {
      int start = indexer.lineColToPos(aNode.getStartLine(),
          aNode.getStartColumn());
      int length = aNode.getText().length();
      setCharacterAttributes(start, length, style, true);
    } catch (IllegalArgumentException e) {
      // skip
    }
  }

  private String readSource() {
    try {
      return getSource().getSource().getContentString();
    } catch (Exception x) {
    }
    return "";
  }

  /* *********************************** */
  /*         END OF MISTIC CODE          */
  /* *********************************** */

  protected Style getDefaultStyle() {
    return style;
  }

  public void setFont(Font font) {
    StyleConstants.setFontFamily(style, font.getFamily());
    StyleConstants.setFontSize(style, font.getSize());
    setParagraphAttributes(0, getLength(), style, true);
  }
}
