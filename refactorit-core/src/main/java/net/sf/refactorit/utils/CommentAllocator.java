/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 
 * @author Oleg Tsernetsov
 *
 * Finds comments, that most probably correspond to variables in declarations 
 * and parameters in methods. Mostly used when reformatting declarations and 
 * method signatures containing user comments.
 */

public class CommentAllocator {
  
  private static List getMethodParamsComments(BinMethod meth) {
    
    int startLine = meth.getParametersAst().getStartLine();
    int startCol = meth.getParametersAst().getStartColumn();
    SourceCoordinate closingBracket = meth.getParamsClosingBracket(); 
    int endLine = closingBracket.getLine();
    int endCol = closingBracket.getColumn()-1;
    List comments = Comment.getCommentsIn(meth);
    for(Iterator it = comments.iterator(); it.hasNext(); ) {
      Comment comment = (Comment) it.next();
      int sLine = comment.getStartLine();
      int sCol = comment.getStartColumn();
      int eLine = comment.getEndLine();
      int eCol = comment.getEndColumn();
      
      if((eLine < startLine || (eLine == startLine && eCol< startCol))
          || ((sLine > endLine || (sLine == endLine && sCol> endCol)))) {
        it.remove();
      }
    }
    return comments;
  }

  public static MultiValueMap allocateComments(BinMethod meth) {
    MultiValueMap varComments = new MultiValueMap();
    BinVariable vars[] = meth.getParameters();
    CompilationUnit cu = meth.getCompilationUnit();
    
    SourceCoordinate start = new SourceCoordinate(meth.getParametersAst().getLine(), 
        meth.getParametersAst().getColumn());
    List comments = getMethodParamsComments(meth);
    removeExpressionComments(vars, comments);
    if(comments.size() >0 ) {
      allocateRaw(meth.getCompilationUnit(), vars, varComments, comments, start);
    }
    return varComments;
  }
  
  public static MultiValueMap allocateComments(BinVariableDeclaration decl) {
    MultiValueMap varComments = new MultiValueMap();
    BinVariable vars[] = decl.getVariables();
    List comments = Comment.getCommentsInAndAfter(decl);
    removeExpressionComments(vars, comments);
    if(comments.size() > 0) {
      SourceCoordinate start = new SourceCoordinate(decl.getStartLine(),
          decl.getStartColumn());
      allocateRaw(decl.getCompilationUnit(), vars, varComments, comments, start);
    }
    return varComments;
  }
  
  private static void removeExpressionComments(BinVariable vars[], List comments) {
    if(vars == null || comments == null) {
      return;
    }
    for(Iterator it = comments.iterator(); it.hasNext(); ) {
      Comment comment = (Comment) it.next();
      for(int i=0; i<vars.length; i++) {
        if(vars[i].hasExpression() && 
            vars[i].getExpression().contains(comment)) {
          it.remove();
        }
      }
    }
  }
  
  private static void allocateRaw(CompilationUnit cu, BinVariable vars[], 
  MultiValueMap varComments, List comments, SourceCoordinate start) {
    
    if(comments.size() == 0) {
      return;
    }
    int line = start.getLine();
    int col = start.getColumn() - 1;
    int varIdx = 0, commentIdx = 0, maxIdx = comments.size();
    int maxLine = ((Comment) comments.get(maxIdx-1)).getStartLine();
    
    String lineStr = cu.getSource().getContentOfLine(line);
    Comment nextComment = (Comment) comments.get(commentIdx);
    List tempComments = new ArrayList();
    boolean commaPassed = false;
    boolean newlinePassed = false;
    
    while (commentIdx < maxIdx && varIdx < vars.length) {
      // quite hacky code here. However, works.
      while (col >= lineStr.length() || col < 0) {
        col = 0;
        line++;
        lineStr = cu.getSource().getContentOfLine(line);
      }
    
      do {
        if (containsCoordinates(line, col, nextComment)) {
    
          line = nextComment.getEndLine();
          lineStr = cu.getSource().getContentOfLine(line);
          col = nextComment.getEndColumn() - 1;
    
          if (!commaPassed) {
            varComments.put(vars[varIdx], nextComment);
          } else {
            tempComments.add(nextComment);
          }
    
          if (++commentIdx < maxIdx) {
            nextComment = (Comment) comments.get(commentIdx);
          }
        } else if (varIdx < vars.length && isInField(line, col, vars[varIdx])) {
          // just skip
          SourceCoordinate coord = getVarEnd(vars[varIdx]);
          line = coord.getLine();
          lineStr = cu.getSource().getContentOfLine(line);
          col = coord.getColumn() - 1;
          newlinePassed = false;
          commaPassed = false;
        } else if (varIdx + 1 < vars.length
            && isInField(line, col, vars[varIdx + 1])) {
          SourceCoordinate coord = getVarEnd(vars[varIdx + 1]);
          line = coord.getLine();
          lineStr = cu.getSource().getContentOfLine(line);
          col = coord.getColumn() - 1;
    
          if (commaPassed) {
            varIdx++;
            varComments.putAll(vars[varIdx], tempComments);
            tempComments.clear();
            commaPassed = false;
          }
          newlinePassed = false;
        } else {
          if (lineStr.charAt(col) == ',') {
            if (newlinePassed) {
              newlinePassed = false;
              varComments.putAll(vars[varIdx], tempComments);
              tempComments.clear();
              varIdx++;
            } else {
              commaPassed = true;
            }
          }
          col++;
        }
      } while (col >= 0 && col < lineStr.length());
    
      // linebreak
      if (commaPassed) {
        if (tempComments.size() > 0) {
          varComments.putAll(vars[varIdx], tempComments);
          tempComments.clear();
        }
        varIdx++;
        commaPassed = false;
      } else {
        newlinePassed = true;
      }
    }
  }
  
  private static SourceCoordinate getVarEnd(BinVariable var) {
    int line, col;
    if (var.hasExpression()) {
      SourceCoordinate coord = var.getExpressionEnd();
      line = coord.getLine();
      col = coord.getColumn();
    } else {
      line = var.getNameStart().getLine();
      col = var.getNameStart().getColumn() + var.getName().length();
    }
    return new SourceCoordinate(line, col);
  }

  private static boolean isInField(int line, int col, BinVariable var) {
    int startLine = var.getNameStart().getLine();
    int startCol = var.getNameStart().getColumn() - 1;
    int endLine = var.hasExpression() ? var.getExpressionEnd().getLine()
        : startLine;
    int endCol = var.hasExpression() ? var.getExpressionEnd().getColumn() - 1
        : startCol + var.getName().length();

    return line >= startLine && line <= endLine && col >= startCol
        && col < endCol;
  }

  private static boolean containsCoordinates(int line, int col, LocationAware la) {
    int startLine = la.getStartLine();
    int endLine = la.getEndLine();
    
    return ((line > startLine ||
        line == startLine && col >= la.getStartColumn() - 1) &&
        (line < endLine ||
        line == endLine && col < la.getEndColumn() - 1));
  }
  
  public static String indentifyComment(String commentText, int indent,
      boolean indentBegin) {
    String indentStr = FormatSettings.getIndentString(indent);
    // FIXME: this won`t work on MAC
    // String indented = CommentBodyEditor.identLines(commentText, indent);
    String indented = (commentText.indexOf('\n')>=0) ? 
        commentText.replaceAll("\n[\t ]*", "\n" + indentStr + " ") :
          new String(commentText);

    if (indentBegin) {
      indented = indentStr + indented;
    }
    
    return indented;
  }
}
