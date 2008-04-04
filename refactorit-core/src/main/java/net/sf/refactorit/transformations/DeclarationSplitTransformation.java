/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.utils.CommentAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Oleg Tsernetsov
 */
public final class DeclarationSplitTransformation extends
    AbstractTransformation {
  private BinVariableDeclaration decl;

  /**
   * key - BinVariable, value - Integer (new modifier)
   */
  private HashMap modifiers = new HashMap();

  private List excluded = new ArrayList();

  private MultiValueMap varComments;

  public DeclarationSplitTransformation(SourceHolder input,
      BinVariableDeclaration expr, List excludes) {
    this(input, expr);
    this.excluded.addAll(excludes);
  }
  
  public DeclarationSplitTransformation(SourceHolder input,
      BinVariableDeclaration expr) {
    super(input);
    this.decl = expr;
    varComments = CommentAllocator.allocateComments(decl);
  }

  public DeclarationSplitTransformation(SourceHolder input,
      BinVariableDeclaration expr, Map modifiableAccesses) {
    this(input, expr);
    modifiers.putAll(modifiableAccesses);
  }

  public DeclarationSplitTransformation(SourceHolder input,
      BinVariableDeclaration expr, Map modifiableAccesses, List excludes) {
    this(input, expr, modifiableAccesses);
    this.excluded.addAll(excludes);
  }

  public RefactoringStatus apply(EditorManager editor) {
    RefactoringStatus status = new RefactoringStatus();
    prepareAccesses(decl, modifiers);
    removeOld(decl, editor);
    createDeclarations(decl, varComments, editor);
    return status;
  }

  private void prepareAccesses(BinVariableDeclaration decl, Map modifiers) {
    BinVariable vars[] = decl.getVariables();
    for (int i = 0; i < vars.length; i++) {
      if (!modifiers.containsKey(vars[i])) {
        modifiers.put(vars[i], new Integer(vars[i].getAccessModifier()));
      }
    }
  }

  private void removeOld(BinVariableDeclaration decl, EditorManager editor) {
    StringEraser eraser = new StringEraser(decl);
    editor.addEditor(eraser);

    // erase affected comments after the declaration
    List comments = Comment.getCommentsInAndAfter(decl);
    for (int i = comments.size() - 1; i >= 0; i--) {
      Comment comment = (Comment) comments.get(i);
      if (decl.contains(comment)) {
        break;
      }
      eraser = new StringEraser(comment);
      editor.addEditor(eraser);
    }
  }

  private void createDeclarations(BinVariableDeclaration decl,
      MultiValueMap varComments, EditorManager editor) {
    BinVariable vars[] = decl.getVariables();

    CompilationUnit cu = decl.getCompilationUnit();

    int insertionLine = decl.getStartLine();
    int insertionColumn = decl.getStartColumn() - 1;

    int oldModifier = vars[0].getModifiers();

    for (int i = 0; i < vars.length; i++) {
      if (excluded.contains(vars[i])) {
        continue;
      }
      Integer modifier = (Integer) modifiers.get(vars[i]);
      int newModifier = BinModifier.setFlags(oldModifier, modifier.intValue());

      String declExpr = ((i == 0) ? "" : FormatSettings.getIndentString(decl
          .getIndent()))
          + new BinModifierFormatter(newModifier).print()
          + " "
          + BinFormatter.formatWithType(vars[i])
          + ((vars[i].hasExpression()) ? " = "
              + vars[i].getExpression().getText() : "") + ";";

      List l = varComments.get(vars[i]);
      int commentIndent = getLineEndIndent(declExpr) + 1
          + ((i == 0) ? insertionColumn + 1: 0);
      if (l != null && l.size() > 0) {
        declExpr = declExpr
            + " " + CommentAllocator.indentifyComment(
                ((Comment) l.get(0)).getText(), commentIndent, false);
      }
      if(i != vars.length - 1 || (l!=null && l.size()> 1)) {
        declExpr = declExpr + FormatSettings.LINEBREAK;
      }
      editor.addEditor(new StringInserter(cu, insertionLine, insertionColumn,
          declExpr));

      if (l != null && l.size() > 0) {
        for (int k = 1; k < l.size(); k++) {
          Comment c = (Comment) l.get(k);
          String commentStr = CommentAllocator.indentifyComment(
              c.getText(), commentIndent, true);
          if(i != vars.length - 1 || k!=l.size()-1) {
            commentStr += FormatSettings.LINEBREAK;
          }
          editor.addEditor(new StringInserter(cu, insertionLine,
              insertionColumn, commentStr));
        }
      }
    }
  }

  public static int getLineEndIndent(String line) {
    int result = line.length();
    int pos = line.lastIndexOf('\n');
    if(pos>=0) {
      result -= pos+1;
    }
    return result;
  }

  public MultiValueMap getVarComments() {
    return varComments;
  }
}
