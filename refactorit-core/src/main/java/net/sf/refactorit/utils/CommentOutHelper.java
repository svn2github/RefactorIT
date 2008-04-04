/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;

import java.util.List;


public class CommentOutHelper {
  private static final String CLOSING_EXPR = "*/";
  private static final String OPEN_EXPR = "/*";
  private static final String SINGLE_LINE = "//";
  
  public static void commentOutMember(TransformationManager manager, BinMember member) {
    CompilationUnit cu = member.getCompilationUnit();
    boolean isField = (member instanceof BinField);
    List comments = Comment.getCommentsIn(member);
    handleComments(comments, cu, manager);
    StringInserter inserter = new StringInserter(cu, member.getStartLine(), 
        (isField ? member.getStartColumn()-1 : 0), "/*" + (isField? "": FormatSettings.LINEBREAK));
    manager.add(inserter);
    inserter = new StringInserter(cu, member.getEndLine(), member.getEndColumn()-1, 
        (isField? "": FormatSettings.LINEBREAK)+ "*/");
    manager.add(inserter);
  }
  
  public static void commentOutBlock(CompilationUnit cu,
      TransformationManager manager,
      int startLine, int startCol, int endLine, int endCol,
      boolean sideLinebr, boolean linebrAfter) {
    List comments = Comment.getCommentsIn(cu,
        startLine, startCol, endLine, endCol);
    handleComments(comments, cu, manager);
    StringInserter inserter = new StringInserter(cu, startLine, startCol, 
        "/*" + ((sideLinebr)?FormatSettings.LINEBREAK:""));
    manager.add(inserter);
    inserter = new StringInserter(cu, endLine, endCol, 
        ((sideLinebr)?FormatSettings.LINEBREAK:"") + 
        "*/" +
        ((linebrAfter)?FormatSettings.LINEBREAK:""));
    manager.add(inserter);
  }
  
  public static void commentOutLa(TransformationManager manager, LocationAware la) {
    commentOutBlock(la.getCompilationUnit(), manager, 
        la.getStartLine(), la.getStartColumn()-1,
        la.getEndLine(), la.getEndColumn()-1, false, false);
  }
  
  private static void handleComments(List comments, CompilationUnit compilationUnit,
      TransformationManager manager) {
    for (int i = 0; i < comments.size(); i++) {
      Comment c = (Comment) comments.get(i);
      String commentText = c.getText();
      if (commentText.indexOf(CLOSING_EXPR) >= 0) {
        StringEraser eraser = new StringEraser(c);
        manager.add(eraser);
        
        String newText= c.getText();
        if(!commentText.trim().startsWith(SINGLE_LINE)) {
          commentText = openCommentTags(commentText);
          newText = " " + SINGLE_LINE + " ";
          boolean newlinePassed = false;
          for(int k = 0; k < commentText.length(); k++) {
            char chr = commentText.charAt(k); 
            if(chr == '\n') {
              newlinePassed = true;
            } else if(newlinePassed && 
                !Character.isWhitespace(chr)) {
              newText = newText + SINGLE_LINE + " ";
              newlinePassed = false;
            }
            newText = newText + chr;
          }
          newText = newText + " ";
        } else {
          newText = commentText.replaceAll("[*]/","*");
        }
        StringInserter inserter = new StringInserter(compilationUnit, 
            c.getStartLine(), c.getStartColumn()-1, newText);
        manager.add(inserter);
      }
    }
  }
  
  public static String openCommentTags(String commentText) {
    return commentText.substring(1).replaceAll("[*]/","*");
  }
}
