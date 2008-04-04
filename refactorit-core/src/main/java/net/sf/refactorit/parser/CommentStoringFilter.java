/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.loader.UnmodifiableArrayList;

import rantlr.Token;
import rantlr.TokenStream;
import rantlr.TokenStreamException;

import java.util.ArrayList;
import java.util.List;


public final class CommentStoringFilter implements TokenStream {
  final FastJavaLexer base;
  final ArrayList javadocComments = new ArrayList(130);
  final ArrayList simpleComments = new ArrayList(130);

  public CommentStoringFilter(FastJavaLexer base) {
    this.base = base;
  }

  /**
   * Made this unmodifiable for now to avoid bugs.
   */
  public final List getSimpleComments() {
    simpleComments.trimToSize();
    return new UnmodifiableArrayList(simpleComments);
  }

  /**
   * Made this unmodifiable for now to avoid bugs.
   */
  public final List getJavadocComments() {
    javadocComments.trimToSize();
    return new UnmodifiableArrayList(javadocComments);
  }

  public final String getFilename() {
    return base.getFilename();
  }

  private final void saveCommentToken(Token t) {
    // NOTE: fixed coordinates in grammar and lexer not to include linebreak into single-line comment
    final String body = t.getText();
    if (body.startsWith("/**")) {
      javadocComments.add(
          new JavadocComment(body, t.getLine(), t.getColumn(),
          ((TokenExt) t).getEndLine(), ((TokenExt) t).getEndColumn()));
    } else {
      simpleComments.add(new Comment(body, t.getLine(), t.getColumn(),
          ((TokenExt) t).getEndLine(), ((TokenExt) t).getEndColumn()));
    }

//    String body = t.getText();
//    final int bodyLength = body.length() - 1;
//    int pos = bodyLength;
//    char c = body.charAt(pos);
//    while (c == '\n' || c == '\r') {
//      --pos;
//      c = body.charAt(pos);
//    }
//    if (pos < bodyLength) {
//      body = body.substring(0, pos + 1);
//    }
//
//    int line = t.getLine();
//    int column = t.getColumn();
//
//    Comment comment;
//    if( body.startsWith("/**") ) {
//      comment = new JavadocComment(body, line, column);
//      javadocComments.add( comment );
//    } else {
//      comment = new Comment(body, line, column);
//      simpleComments.add( comment );
//    }
//System.err.println("saving comment: " + t + " -> " + comment);
  }

  public final Token nextToken() throws TokenStreamException {
    Token t = base.nextToken();
    int ttype = t.getType();

    while (ttype == JavaTokenTypes.ML_COMMENT
        || ttype == JavaTokenTypes.SL_COMMENT) {
      saveCommentToken(t);

      t = base.nextToken();
      ttype = t.getType();
    }

    return t;
  }
}
