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
import net.sf.refactorit.ui.treetable.PositionableTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class TimestampOrderedViewBuilder implements ViewBuilder {
  private DefaultTreeModel model;
  private List comments = new ArrayList();

  public TimestampOrderedViewBuilder(DefaultTreeModel model) {
    this.model = model;
  }

  public void startNewFile(CompilationUnit file) {}

  public void addComment(Comment c) {
    this.comments.add(c);
  }

  public void finish() {
    Collections.sort(comments,
        new TimestampBasedCommentComparator(TimestampFormat.load().
        getDateFormat()));

    for (Iterator i = comments.iterator(); i.hasNext(); ) {
      Comment comment = (Comment) i.next();

      String bodyWithFileName = addFileName(
          CommentBodyEditor.trimAllLines(comment.getText()),
          comment.getCompilationUnit(), comment.getStartLine());
      PositionableTreeNode newNode = new PositionableTreeNode(
          bodyWithFileName, comment.getCompilationUnit(), comment.getStartLine());
      ((DefaultMutableTreeNode)this.model.getRoot()).add(newNode);
    }
  }

  private String addFileName(String lines, CompilationUnit compilationUnit, int line) {
    return removeExtension(compilationUnit.getDisplayPath()) + ":" + line + ":\r\n"
        + CommentBodyEditor.identLines(lines, 8);
  }

  private String removeExtension(String fileName) {
    if (fileName.indexOf(".java") < 0) {
      return fileName;
    } else {
      return fileName.substring(0, fileName.lastIndexOf(".java"));
    }
  }
}
