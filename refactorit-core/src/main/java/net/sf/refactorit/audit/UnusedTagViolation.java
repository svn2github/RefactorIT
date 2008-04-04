/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;

import net.sf.refactorit.audit.rules.UnusedTagsRule;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Oleg Tsernetsov
 */
public class UnusedTagViolation extends SimpleViolation {
  private JavadocComment comment;

  private List keys;

  public UnusedTagViolation(JavadocComment comment, List keys, BinCIType owner) {
    super(owner.getTypeRef(), owner.getNameAstOrNull(), "Unused '"
        + SkipTagHelper.SKIP_TAG + "' tag options " + keys + " for class '"
        + owner.getName() + "'", "refact.audit.unused_tags");
    setAuditRule(UnusedTagsRule.instance);
    this.comment = comment;
    this.keys = keys;
  }

  public UnusedTagViolation(JavadocComment comment, List keys, BinMethod owner) {
    super(owner.getOwner(), owner.getRootAst(), "Unused '" + SkipTagHelper.SKIP_TAG
        + "' tag options " + keys + " for method '" + owner.getName() + "'",
        "refact.audit.unused_tags");
    setAuditRule(UnusedTagsRule.instance);
    this.comment = comment;
    this.keys = keys;
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveUnusedCommentAction.INSTANCE);
  }

  public JavadocComment getTag() {
    return comment;
  }

  public List getKeys() {
    return keys;
  }
}

class RemoveUnusedCommentAction extends MultiTargetCorrectiveAction {
  public static RemoveUnusedCommentAction INSTANCE = new RemoveUnusedCommentAction();

  public String getKey() {
    return "refactorit.audit.action.not_used.remove_tag";
  }

  public String getName() {
    return "Remove unused @refactorit tag";
  }

  public String getMultiTargetName() {
    return "Remove unused @refactorit tag(s)";
  }

  protected Set process(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof UnusedTagViolation)) {
      return Collections.EMPTY_SET;
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();
    JavadocComment comment = ((UnusedTagViolation) violation).getTag();
    List keys = ((UnusedTagViolation) violation).getKeys();

    List editors = findEditors(compilationUnit, comment, keys);
    for (int i = 0; i < editors.size(); i++) {
      manager.add((Editor) editors.get(i));
    }

    return Collections.singleton(compilationUnit);
  }

  private List findEditors(CompilationUnit compilationUnit,
      JavadocComment comment, List keys) {

    List editors = new ArrayList();
    String commentText = comment.getText();
    List allOptions = SkipTagHelper.getSkippedOptions(commentText);
    int tagBegin = commentText.indexOf(SkipTagHelper.SKIP_TAG);
    int tagEnd = commentText.indexOf('\n', tagBegin);
    if (tagEnd < 0) {
      tagEnd = commentText.indexOf("*", tagBegin) - 1;
    }

    if (keys.containsAll(allOptions)) {
      int b = tagBegin;
      int e = tagEnd;
      String s = commentText.substring(0, b) + commentText.substring(e);
      s = s.replaceAll("[*]", "").replaceAll("/", "").replaceAll("\\\\", "");
      if (s.trim().length() == 0) {
        b = 0;
        e = commentText.length();
      }

      SourceCoordinate begin = fingCoordinateFor(comment, b);
      SourceCoordinate end = fingCoordinateFor(comment, e);

      editors.add(new StringEraser(compilationUnit, begin, end));
    } else {
      for (int j = 0; j < keys.size(); j++) {
        String key = (String) keys.get(j);

        int b = commentText.indexOf(key, tagBegin) - 1;
        int e = b + key.length() + 1;
        SourceCoordinate begin = fingCoordinateFor(comment, b);
        SourceCoordinate end = fingCoordinateFor(comment, e);

        editors.add(new StringEraser(compilationUnit, begin, end));
      }
    }
    return editors;
  }

  private SourceCoordinate fingCoordinateFor(JavadocComment comment, int pos) {
    int line = comment.getStartLine();
    int col = comment.getStartColumn();

    String commentText = comment.getText();
    int p = 0;
    while (p < pos) {
      int tmp = commentText.indexOf('\n', p+1);
      if (tmp < 0 || tmp + 1 > pos) {
        break;
      }
      line++;
      p = tmp;
    }
    col = pos - p;
    if (col < 0) {
      return null;
    }
    if(col != 0) {
      col++;
    }
    return new SourceCoordinate(line, col + 1);
  }
}
