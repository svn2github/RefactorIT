/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.encapsulatefield;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.utils.EncapsulateUtils;

import java.util.List;



/**
 * @author tanel
 * @author Anton Safonov
 */
public class EncapsulateEditor {

  private static final String prefix = "(";
  private static final String suffix = ")";

  private String getterName, setterName;
  private BinField field;

  private List usages;

  public EncapsulateEditor(BinField field, String getterName, String setterName,
      List usages) {
    this.field = field;
    this.getterName = getterName + prefix + suffix;
    this.setterName = setterName;

    this.usages = usages;
  }

  public void generateEditors(final TransformationList transList) {
    for (int i = 0, max = this.usages.size(); i < max; i++) {
      InvocationData id = (InvocationData)this.usages.get(i);

      if (Assert.enabled) {
        Assert.must(id.getInConstruct()
            instanceof BinFieldInvocationExpression,
            "BinFieldInvocationExpression expected");
      }
      BinFieldInvocationExpression fieldInvocation
          = (BinFieldInvocationExpression) id.getInConstruct();

      // if variable is read
      if (BinVariableSearchFilter.isReadAccess(fieldInvocation) &&
          !BinVariableSearchFilter.isWriteAccess(fieldInvocation)) {
        transList.add(new RenameTransformation(
            fieldInvocation.getCompilationUnit(),
            CollectionUtil.singletonArrayList(fieldInvocation.getNameAst()),
            getterName, false));
        continue;
      }

      if (EncapsulateUtils.isUsedInNotReadIncDec(fieldInvocation)) {
        BinIncDecExpression incDecExpression
            = (BinIncDecExpression) fieldInvocation.getParent();
        int type = incDecExpression.getType();

        String operator = "???";
        switch (type) {
          case JavaTokenTypes.INC:
          case JavaTokenTypes.POST_INC:
            operator = "+";
            break;
          case JavaTokenTypes.DEC:
          case JavaTokenTypes.POST_DEC:
            operator = "-";
            break;
          default:
            Assert.must(false, "Unknown inc/dec epxpression, type = " + type);
        }

        transList.add(new RenameTransformation(
            fieldInvocation.getCompilationUnit(),
            CollectionUtil.singletonArrayList(fieldInvocation.getNameAst()),
            setterName + prefix + getObjectPrefix(fieldInvocation)
            + getterName + ' ' + operator + " 1" + suffix, true));

        // Renamer is better than StringEraser here, since it makes validity
        // checks also
        transList.add(new RenameTransformation(
            fieldInvocation.getCompilationUnit(),
            CollectionUtil.singletonArrayList(incDecExpression.getRootAst()),
            "", true));

        continue;
      }

      // if variable is written
      if (fieldInvocation.getParent() instanceof BinAssignmentExpression) {

        BinAssignmentExpression assignment
            = (BinAssignmentExpression) fieldInvocation.getParent();

        // <FIX> author Aleksei Sosnovski
        BinItemVisitable parent = assignment.getParent();

        if (assignment.getLeftExpression() == fieldInvocation
            && (parent instanceof BinAssignmentExpression
            || parent instanceof BinLogicalExpression
            || parent instanceof BinVariable)) {
          // add getter
          transList.add(new RenameTransformation(
              fieldInvocation.getCompilationUnit(),
              CollectionUtil.singletonArrayList(assignment.getCompoundAst()),
              getterName, true));

          // add  setter
          while (!(parent.getParent() instanceof BinStatementList)
              && !(parent.getParent() instanceof BinCIType)) {
            parent = parent.getParent();
          }

          SourceCoordinate src = new SourceCoordinate
              (((LocationAware) parent).getStartLine(),
              ((LocationAware) parent).getStartColumn() - 1);

          int indent = 0;
          if (parent.getParent() instanceof BinStatementList) {
            indent = ((BinStatementList) parent.getParent()).getIndent()
                + FormatSettings.getBlockIndent();
          } else

          if (parent.getParent() instanceof BinCIType) {
            indent = ((BinCIType) parent.getParent()).getIndent()
                + FormatSettings.getBlockIndent();
          }

          String ind = FormatSettings.getIndentString(indent);

          String setter = setterName + prefix
              + assignment.getRightExpression().getText()
              + suffix + ";" + FormatSettings.LINEBREAK + ind;

          transList.add(new StringInserter(
              assignment.getCompilationUnit(), src, setter));

        } else {
        // </FIX>

          transList.add(new RenameTransformation(
              fieldInvocation.getCompilationUnit(),
              CollectionUtil.singletonArrayList(fieldInvocation.getNameAst()),
              setterName, true));

          int assigmentType = assignment.getAssignmentType();
          if (assigmentType == JavaTokenTypes.ASSIGN) {
            transList.add(new RenameTransformation(
                assignment.getCompilationUnit(),
                CollectionUtil.singletonArrayList(assignment.getRootAst()),
                prefix, true));
          } else {
            String operator = "???";
            switch (assigmentType) {
              case JavaTokenTypes.PLUS_ASSIGN:
                operator = "+";
                break;
              case JavaTokenTypes.MINUS_ASSIGN:
                operator = "-";
                break;
              case JavaTokenTypes.STAR_ASSIGN:
                operator = "*";
                break;
              case JavaTokenTypes.DIV_ASSIGN:
                operator = "/";
                break;
              case JavaTokenTypes.MOD_ASSIGN:
                operator = "%";
                break;
              case JavaTokenTypes.SR_ASSIGN:
                operator = ">>";
                break;
              case JavaTokenTypes.BSR_ASSIGN:
                operator = ">>>";
                break;
              case JavaTokenTypes.SL_ASSIGN:
                operator = "<<";
                break;
              case JavaTokenTypes.BAND_ASSIGN:
                operator = "&";
                break;
              case JavaTokenTypes.BXOR_ASSIGN:
                operator = "^";
                break;
              case JavaTokenTypes.BOR_ASSIGN:
                operator = "|";
                break;

              default:
                Assert.must(false, "Unknown compound assignment encountered");
            }

            transList.add(new RenameTransformation(
                assignment.getCompilationUnit(),
                CollectionUtil.singletonArrayList(assignment.getRootAst()),
                prefix + getObjectPrefix(fieldInvocation)
                + getterName + ' ' + operator + ' ', true));
          }

          // find right expression bound
          CompoundASTImpl compound = new CompoundASTImpl(assignment.getRootAst());
          String content = assignment.getCompilationUnit().getContent();
          LineIndexer indexer = assignment.getCompilationUnit().getLineIndexer();
          int endColumn = indexer.posToLineCol(
              findRightBracketPlace(
              content, indexer.lineColToPos(
              compound.getEndLine(), compound.getEndColumn())))
              .getColumn() - 1;

          transList.add(new StringInserter(
              assignment.getCompilationUnit(),
              compound.getEndLine(), endColumn, suffix));
        }
      }
    }
  }

  private String getObjectPrefix(
      BinFieldInvocationExpression invocationExpression) {

    String objectPrefix = "";
    BinExpression expression = invocationExpression.getExpression();
    if (expression != null) {
      ASTImpl ast = expression.getRootAst();
      if (ast.getFirstChild() == null) {
        objectPrefix = ast.getText() + ".";
      } else {
        Assert.must(false, "Cannot encapsulate, too complex usage: "
            + invocationExpression.getExpression());
      }
    }
    return objectPrefix;
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": " + field;
  }

  // FIXME here seems to be some bugs, check for several complex expressions
  // within assignment
  // FIXME moreover we have most brackets in the AST tree now,
  // so might be made much more simple!
  private int findRightBracketPlace(String line, int index) {

    int i = index;
    for (i = index; i < line.length(); i++) {
      final char c = line.charAt(i);
      if (c == ' ' || c == '\t') {
        continue;
      }

      if ((c == ')') || (c == '}') || (c == ']')) {
        continue;
      }

      break;
    }

    return i;
  }

}
