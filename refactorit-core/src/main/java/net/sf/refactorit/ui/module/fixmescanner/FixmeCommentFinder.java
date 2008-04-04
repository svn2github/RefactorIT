/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.loader.Comment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FixmeCommentFinder {
  private IsFixmeCommentChecker deicider;
  private boolean uniteMultilineComments = true;

  public FixmeCommentFinder(List fixmeWords) {
    this.deicider = new IsFixmeCommentChecker(fixmeWords);
  }

  public void setUniteMultilineComments(boolean b) {
    this.uniteMultilineComments = b;
  }

  public List getFixmeComments(CompilationUnit compilationUnit,
      boolean scanAllLines, int startLine, int stopLine) {
    if (compilationUnit == null) {
      throw new IllegalArgumentException("compilationUnit == null");
    }

    List result = new ArrayList();

    if (this.uniteMultilineComments) {
      result.addAll(
          selectFixmeComments(
              new MultilineCommentUniter(this.deicider).unite(
                  compilationUnit.getSimpleComments())));
    } else {
      result.addAll(
          selectFixmeComments(compilationUnit.getSimpleComments()));
    }

    result.addAll(
        selectFixmeComments(compilationUnit.getJavadocComments()));

    if (!scanAllLines) {
      result = getCommentsInRange(result, startLine, stopLine);
    }

    return result;
  }

  private List getCommentsInRange(List comments,
      int startLine, int stopLine) {
    ArrayList result = new ArrayList();

    for (Iterator i = comments.iterator(); i.hasNext(); ) {
      Comment c = (Comment) i.next();
      if (c.getStartLine() >= startLine && c.getStartLine() <= stopLine) {
        result.add(c);
      }
    }

    return result;
  }

  private List selectFixmeComments(List comments) {
    ArrayList result = new ArrayList();

    for (Iterator c = comments.iterator(); c.hasNext(); ) {
      Comment comment = (Comment) c.next();
      if (deicider.isFixmeComment(comment.getText())) {
        result.add(comment);
      }
    }

    return result;
  }
}
