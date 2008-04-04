/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.vfs.Source;

import java.util.List;


/**
 * @author Anton Safonov
 */
public class BinStatementListFormatter extends BinItemFormatter {
  private final BinStatementList stmt;

  public BinStatementListFormatter(final BinStatementList statementList) {
    this.stmt = statementList;
  }
  
  public String print() {
    BinStatement[] statements = stmt.getStatements();
    String text = new String();
    for(int i = 0; i < statements.length; i++) {
      System.out.println("Statement["+i+"] check. Size: ");
      text += statements[i].getFormatter().print();
    }
    return text;
  }

  public ASTImpl getOpeningBrace() {
    final Source source = stmt.getCompilationUnit().getSource();
    final LineIndexer indexer = source.getLineIndexer();
    final int indent = stmt.getIndent();

    SimpleASTImpl result = new SimpleASTImpl();

    String str = source.getText(stmt.getStartLine(), 0,
        stmt.getStartLine(), stmt.getStartColumn());

    if (!StringUtil.containsOnlyWhitespaceAndComments(str)) {
      result.setStartLine(stmt.getStartLine());
      result.setStartColumn(stmt.getStartColumn());
      String txt = new String();
      if (FormatSettings.isNewlineBeforeBrace()) {
        txt += FormatSettings.LINEBREAK;
        txt += FormatSettings.getIndentString(
            indent + FormatSettings.getBlockIndent()
            + FormatSettings.getBraceIndent());
      } else {
        txt += " ";
      }
      txt += "{";
      txt += FormatSettings.LINEBREAK
          + FormatSettings.getIndentString(indent
          + FormatSettings.getBlockIndent());
      result.setText(txt);
    } else {
      int pos = indexer.lineColToPos(stmt.getStartLine(), 1);
      pos = StringUtil.moveOneLinebreakBack(source.getContentString(), pos);
      
      int ln = indexer.posToLineCol(pos).getLine();
      int col = indexer.posToLineCol(pos).getColumn();
      
      /* 
       * Check if brace will be placed inside a '// comment'-style comment.
       * If true, then will place brace before comment.
       */
      List comments = stmt.getCompilationUnit().getSimpleComments();
      for (int c = 0; c < comments.size(); c++){
        Comment comment = (Comment) comments.get(c);
        if (comment.getStartLine() == ln
            && comment.getText().charAt(1) != '*'){
          col = comment.getStartColumn();
          break;
        }
      }
      
      String txt = new String();
      if (!Character.isWhitespace(source.getContentString().charAt(pos - 1))) {
        txt += " ";
      }
      result.setStartLine(ln);
      result.setStartColumn(col);
      txt += "{";
      result.setText(txt);
    }

    return result;
  }

  public ASTImpl getClosingBrace() {
    final Source source = stmt.getCompilationUnit().getSource();
//    final LineIndexer indexer = source.getLineIndexer();
    final int indent = ((LocationAware) stmt.getParent()).getIndent();

    SimpleASTImpl result = new SimpleASTImpl();
    result.setStartLine(stmt.getEndLine());
    result.setStartColumn(stmt.getEndColumn());

//    String str = source.getText(
//        indexer.lineColToPos(stmt.getEndLine(), stmt.getEndColumn()),
//        indexer.lineColToPos(stmt.getEndLine() + 1, 1));
//    System.err.println("str: \"" + StringUtil.printableLinebreaks(str) + "\"");
//    if (StringUtil.containsOnlyWhitespaceAndComments(str)) {
    String is = FormatSettings.getIndentString(indent);
    result.setText(FormatSettings.LINEBREAK + is + "}"
        + FormatSettings.LINEBREAK + is);
//    } else {
//      result.setText("}");
//    }

    return result;
  }
}
