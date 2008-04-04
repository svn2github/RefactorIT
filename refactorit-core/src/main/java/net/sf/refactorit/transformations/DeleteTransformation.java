/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.source.edit.StringEraser;


/**
 * @author Jevgeni Holodkov
 */
public final class DeleteTransformation extends AbstractTransformation {
  private static final int DELETE_STRING = 0;
  private static final int DELETE_BIN_VARIABLE_DECLARATION = 1;
  public static final int DELETE_TYPE_NODE = 2;
  public static final int DELETE_ARRAY_DECLARATION_BRACKETS_AFTER_NAME = 3;

  private BinVariable binVariable;
  private BinVariableDeclaration declaration;

  private int transformationType = DELETE_STRING;

  public DeleteTransformation(CompilationUnit compilationUnit) {
    super(compilationUnit);
  }

  /** Do not erase multiple variables at the same time with this */
  public DeleteTransformation(BinVariable var,
      BinVariableDeclaration declaration) {
    this(var, DELETE_BIN_VARIABLE_DECLARATION);
    this.declaration = declaration;

  }

  public DeleteTransformation(BinVariable var, int transformationType) {
    super(var.getCompilationUnit());
    this.binVariable = var;
    this.transformationType = transformationType;
  }

  public RefactoringStatus apply(EditorManager editor) {
    RefactoringStatus status = new RefactoringStatus();
    switch (transformationType) {
      case DELETE_BIN_VARIABLE_DECLARATION:
        deleteBinDeclaration(editor);
        break;

        /** Not for BinParameter instances */
      case DELETE_TYPE_NODE:
        deleteTypeNode(editor);
        break;

      case DELETE_ARRAY_DECLARATION_BRACKETS_AFTER_NAME:
        deleteArrayDeclaratonBracketsAfterName(editor);
        break;

      case DELETE_STRING:
        break;
      default:
        throw new RuntimeException(
            "Unknown transformation type usage in Rename Transformation!");
    }
    return status;
  }

  /**
   * @param SourceEditor editor
   */
  private void deleteBinDeclaration(EditorManager editor) {
    StringEraser eraser;
    if (declaration.getVariables().length > 1) {
      ASTImpl start = binVariable.getNameAstOrNull();

      ASTImpl assignedExpression = ASTUtil.getAssignedExpression(binVariable.
          getOffsetNode());
      ASTImpl end = assignedExpression != null ? new CompoundASTImpl(
          assignedExpression) : start;

      eraser = new StringEraser(getSource(), start.getStartLine(),
          start.getStartColumn() - 1, end.getEndLine(), end.getEndColumn() - 1,
          true, !declaration.isFirst(binVariable));
    } else {
      eraser = new StringEraser(binVariable);
    }
    eraser.setRemoveLinesContainingOnlyComments(true);
    editor.addEditor(eraser);
  }

  private void deleteTypeNode(final EditorManager editor) {
    BinVariableDeclaration declaration = (BinVariableDeclaration) binVariable.
        getWhereDeclared();
    ASTImpl firstNameAst = declaration.getVariables()[0].getNameAstOrNull();

    editor.addEditor(new StringEraser(binVariable.getCompilationUnit(),
        binVariable.getStartLine(), binVariable.getStartColumn() - 1,
        firstNameAst.getStartLine(), firstNameAst.getStartColumn() - 1, true,
        false));

    deleteArrayDeclaratonBracketsAfterName(editor);
  }

  private void deleteArrayDeclaratonBracketsAfterName(EditorManager editor) {
    if (BracketFinder.hasBracketsAfterName(binVariable)) {
      ASTImpl nameAst = binVariable.getNameAstOrNull();
      SourceCoordinate bracketEnd = BracketFinder.findBracketsEndAfterName(
          binVariable);
      editor.addEditor(new StringEraser(binVariable.getCompilationUnit(),
          nameAst.getEndLine(), nameAst.getEndColumn() - 1,
          bracketEnd.getLine(), bracketEnd.getColumn()));
    }
  }
}
