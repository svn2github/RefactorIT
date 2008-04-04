/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Igor Malinin
 */
public class SkipUnskipAction extends CorrectiveAction {
  public static final String KEY = "refactorit.audit.action.skip-unskip";

  protected String violation;
  protected BinMember owner;
  
  public SkipUnskipAction(String violation){
    this.violation = violation;
  }
  
  public SkipUnskipAction(String violation, BinMember owner) {
    if (violation == null) {
      throw new NullPointerException("violation");
    }

    if (owner == null) {
      throw new NullPointerException("owner");
    }

    this.violation = violation;
    this.owner = owner;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    String name = BinFormatter.format(owner);

    return isSkippedViolation(violation, owner) ? "Remove '@refactorit.skip " + violation
        + "' from " + name : "Add '@refactorit.skip " + violation + "' to " + name;
  }

  public Set run(TreeRefactorItContext context, List violations) {
    TransformationManager manager = new TransformationManager(null);

    process(manager, violation, owner);
    
    if (manager.isContainsEditors()) {
      if (manager.performTransformations().isOk()) {
        return Collections.singleton(owner.getCompilationUnit());
      }
    }

    return Collections.EMPTY_SET;
  }

  /** For tests */
  public static boolean run(final List violations) {
    TransformationManager manager = new TransformationManager(null);

    for (int i = 0; i < violations.size(); i++) {
      final RuleViolation ruleViolation = (RuleViolation) violations.get(i);
      process(manager, ruleViolation.getTypeShortName(), ruleViolation
          .getOwnerMember());
    }
    
    return manager.performTransformations().isOk();
  }

  private static void process(final TransformationManager manager,
      final String violation, final BinMember owner) {
    if (violation.length() > 0) {
      if (isSkippedViolation(violation, owner)) {
        deleteTag(violation, manager, owner);
      } else {
        insertTag(violation, manager, owner);
      }
    }
  }

  protected static void insertTag(final String violation,
      final TransformationManager manager, 
      final BinMember owner) {
    String indent;

    BinTypeRef ref = owner.getOwner();
    if (ref == null) {
      indent = "";
    } else {
      int mi = new BinTypeFormatter(ref.getBinCIType()).getMemberIndent();
      indent = FormatSettings.getIndentString(mi);
    }

    JavadocComment comment = Comment.findJavadocFor(owner);
    if (comment == null) {
      // add new comment
      manager.add(new StringInserter(owner.getCompilationUnit(), owner
          .getStartLine(), owner.getStartColumn() - 1, "/** "
          + SkipTagHelper.SKIP_TAG + " " + violation + " */"
          + FormatSettings.LINEBREAK + indent));
    } else {
      LineIndexer indexer = owner.getCompilationUnit().getLineIndexer();

      int pos = SkipTagHelper.findStartTagPos(comment.getText(), 3);
      if (pos < 0) {
        // add new tag
        if (comment.getStartLine() == comment.getEndLine()) {
          // split comment to multiline
          manager.add(new StringInserter(owner.getCompilationUnit(), indexer
              .posToLineCol(comment.getEndPosition() - 3),
              FormatSettings.LINEBREAK + indent + " * "
              + SkipTagHelper.SKIP_TAG + " " + violation
              + FormatSettings.LINEBREAK + indent + " "));

          manager.add(new StringInserter(owner.getCompilationUnit(), indexer
              .posToLineCol(comment.getStartPosition() + 2),
              FormatSettings.LINEBREAK + indent + " *"));
        } else {
          // add new line
          String newline = "";
          String content = owner.getCompilationUnit().getSource().getContentOfLine(
              comment.getEndLine());
          if (content.substring(0,
              comment.getEndColumn() - 3).trim().length() > 0) {
            newline = FormatSettings.LINEBREAK + indent + " ";
          }

          manager.add(new StringInserter(owner.getCompilationUnit(), indexer
              .posToLineCol(comment.getEndPosition() - 3), newline + "* "
              + SkipTagHelper.SKIP_TAG + " " + violation
              + FormatSettings.LINEBREAK + indent + " "));
        }
      } else {
        // edit existing tag
        SourceCoordinate sc = indexer.posToLineCol(comment.getStartPosition()
            + pos + SkipTagHelper.SKIP_TAG.length() - 1);

        manager.add(new StringInserter(owner.getCompilationUnit(), sc, " "
            + violation));
      }
    }
  }

  protected static void deleteTag(final String violation, 
      final TransformationManager manager, final BinMember owner) {
    JavadocComment comment = Comment.findJavadocFor(owner);
    if (comment == null) {
      // assert
      return;
    }

    String text = comment.getText();

    int pos = SkipTagHelper.findStartTagPos(text, 3);
    if (pos < 0) {
      // assert
      return;
    }

    int end = SkipTagHelper.findEndTagPos(text, pos + 1);
    int pos1 = pos + SkipTagHelper.SKIP_TAG.length();
    int pos2 = SkipTagHelper.findOptionPos(text, pos1, end);

    for (boolean multi = false; pos2 > 0; multi = true) {
      String option = text.substring(pos1, pos2).replace(',', ' ').replace('*',
          ' ').trim();
      if (!option.equals(violation)) {
        pos1 = pos2;
        pos2 = SkipTagHelper.findOptionPos(text, pos1, end);
        continue;
      }

      if (multi) {
        // before options exists
        int start = comment.getStartPosition();
        manager.add(new StringEraser(owner.getCompilationUnit(), start + pos1,
            start + pos2));
        return;
      }

      int pos3 = SkipTagHelper.findOptionPos(text, pos2, end);
      if (pos3 > 0) {
        // after options exists
        int start = comment.getStartPosition();
        manager.add(new StringEraser(owner.getCompilationUnit(), start + pos1,
            start + pos2));
        return;
      }

      // remove complete comment
      int line = comment.getStartLine();
      if (comment.getStartLine() == comment.getEndLine()) {
        // single option on singleline comment
        manager.add(new StringEraser(owner.getCompilationUnit(), line, 0,
            line + 1, 0));
        return;
      } else {
        // single option on multiline comment
        int start = comment.getStartPosition();

        LineIndexer indexer = owner.getCompilationUnit().getLineIndexer();

        int startColumn = 0;
        int endColumn = 0;
        final int startLine = indexer.posToLineCol(start + pos).getLine();
        final int endLine = indexer.posToLineCol(start + end).getLine();
        if (startLine == endLine) {
          startColumn = indexer.posToLineCol(start + pos).getColumn() - 1;
          endColumn = indexer.posToLineCol(start + end).getColumn() - 1;
        }
        manager.add(new StringEraser(owner.getCompilationUnit(), startLine,
            startColumn, endLine, endColumn));
        return;
      }
    }
  }

  public static boolean isSkippedViolation(final String violation, 
      final BinMember owner) {
    JavadocComment comment = Comment.findJavadocFor(owner);
    if (comment == null) {
      // assert
      return false;
    }

    return SkipTagHelper.isSkipped(comment.getText(), violation);
  }
}
