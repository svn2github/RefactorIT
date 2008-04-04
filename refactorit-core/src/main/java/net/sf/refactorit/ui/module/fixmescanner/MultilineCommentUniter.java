/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;

import java.util.ArrayList;
import java.util.List;


/**
 * Example:
 *
 * // SOME_MARKER_WORD: this is the first line of
 * // the multiline comment, and this one is the last.
 *
 * The above comment is united into one big comment in this method, because the
 * comments are on alongside lines and the first one is a FIXME comment. <br><br>
 */
class MultilineCommentUniter {
  private IsFixmeCommentChecker checker;

  public MultilineCommentUniter(IsFixmeCommentChecker checker) {
    this.checker = checker;
  }

  public List unite(List comments) {
    List result = new ArrayList(comments);

    for (int i = 0; i < result.size(); i++) {
      Comment currentComment = (Comment) result.get(i);
      result.set(i, uniteWithFollowingComments(currentComment, result, i));
    }

    return result;
  }

  private Comment uniteWithFollowingComments(Comment currentComment,
      final List result, final int i) {
    while (shouldUnite(currentComment, nextComment(result, i))) {
      Comment nextComment = nextComment(result, i);

      currentComment = CommentBodyEditor.combine(currentComment, nextComment);
      result.remove(nextComment);
    }

    return currentComment;
  }

  private Comment nextComment(final List result, final int i) {
    if (i + 1 < result.size()) {
      return (Comment) result.get(i + 1);
    } else {
      return null;
    }
  }

  private boolean shouldUnite(Comment comment, Comment nextComment) {
    if (nextComment != null) {
      return
          oneLineComment(nextComment)
          && onAlongsideLines(comment, nextComment)
          && checker.isFixmeComment(comment.getText())
          && (!checker.isFixmeComment(nextComment.getText()));
    } else {
      return false;
    }
  }

  private boolean onAlongsideLines(Comment comment, Comment nextComment) {
    return comment.getStartLine() +
        (StringUtil.lineBreakCount(comment.getText()) + 1)
        == nextComment.getStartLine();
  }

  private boolean oneLineComment(Comment comment) {
    return comment.getStartLine() == comment.getEndLine();
  }
}
