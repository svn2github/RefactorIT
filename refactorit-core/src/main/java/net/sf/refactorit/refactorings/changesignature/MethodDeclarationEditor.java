/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;



import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.utils.CommentAllocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Tonis Vaga
 * @author Aleksei sosnovski
 */
public class MethodDeclarationEditor {
  public MethodDeclarationEditor() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private BinMethod method;

  private MethodSignatureChange change;

  private String commentsForRemovedParameters;

  /**
   *
   * @param method
   * @param change
   * precond: method.getCompilationUnit() != null
   */
  public MethodDeclarationEditor(BinMethod method, MethodSignatureChange change) {
    this.method = method;
    this.change = change;
  }

  public void doEdit(final TransformationList transList,
      final ImportManager importManager) {
    if (ChangeMethodSignatureRefactoring.debug) {
      System.out.println("editing method declaration "
          + method.getQualifiedName());
    }
    renameParameterReferencesIfNeeded(transList);

    ASTImpl ast = method.getParametersAst();
    int startLine = ast.getStartLine();
    int startColumn = ast.getStartColumn() - 1;

    SourceCoordinate src = method.getParamsClosingBracket();
    int endLine = src.getLine();
    int endColumn = src.getColumn() - 1;


    // if return type has been changed
    if (!method.getReturnType().equals(change.getReturnType())) {
      List statementList;

      statementList = RefactoringUtil.findReturnStatementsForMethod(method);

      for (int x = 0; x < statementList.size(); x++) {

        BinReturnStatement rst = (BinReturnStatement) statementList.get(x);

        if (change.getReturnType().equals(BinPrimitiveType.VOID_REF)) {

          // comment return expression, when method return type changed to void
          transList.add(new StringInserter(method.getCompilationUnit(),
              rst.getReturnExpression().getStartLine(),
              rst.getReturnExpression().getStartColumn() - 1,
              "/*"));
          transList.add(new StringInserter(method.getCompilationUnit(),
              rst.getReturnExpression().getEndLine(),
              rst.getReturnExpression().getEndColumn() - 1,
              "*/"));
        }

        transList.add(new StringInserter(method.getCompilationUnit(),
            rst.getEndLine(),
            rst.getEndColumn() - 1,
            " // FIXME: return type of method was changed, please review"));

      }
    }



    if (isChangeAccessNeeded()) {

      BinModifierBuffer modBuf = new BinModifierBuffer(method.getModifiers());
      modBuf.setFlag(change.getAccessModifier());

      if(change.isStatic) {
        modBuf.setFlag(BinModifier.STATIC);
      } else {
        modBuf.clearFlags(BinModifier.STATIC);
      }

      if(change.isFinal) {
        modBuf.setFlag(BinModifier.FINAL);
      } else {
        modBuf.clearFlags(BinModifier.FINAL);
      }

      transList.add(new ModifierEditor(method, modBuf.getModifiers()));
      if (ChangeMethodSignatureRefactoring.debug) {
        System.out.println("changing method acces to "
            + new BinModifierFormatter(change.getAccessModifier()).print());
      }
    }
    if (startColumn != endColumn || startLine != endLine) {
      Editor eraseAllParameters = new StringEraser(method.getCompilationUnit(),
          startLine,
          startColumn,
          endLine, endColumn);
      transList.add(eraseAllParameters);
    }
    final List parametersList = change.getParametersList(method);

    BinParameter newPars[] = new BinParameter[parametersList.size()];
    boolean useFqn[] = new boolean[parametersList.size()];

//    boolean isFirst=true;
//    if(!isFirst) {
//  buffer.append(", ");
//} else {
//  isFirst = false;
//}
//
//if (modifiers != BinModifier.NONE) {
//  buffer.append(BinModifier.toString(modifiers) + " ");
//}
//
//
//buffer.append(BinFormatter.formatNotQualified(parType));
//
//buffer.append(" " + parameterName);

    for (int index = 0; index < parametersList.size(); ++index) {
      ParameterInfo par = (ParameterInfo) parametersList.get(index);

      BinTypeRef parType = par.getType();
      String parameterName = par.getName();
      int modifiers = par.getModifiers();

      newPars[index] = new BinParameter(parameterName, parType, modifiers);

      try {
        importManager.addExtraImports(parType, method.getOwner());
        useFqn[index] = false;
      } catch (AmbiguousImportImportException e) {
      	useFqn[index] = true;
      }
    }

    transList.add(new StringInserter(method.getCompilationUnit(),
        startLine,
        startColumn,
        getParametersString(newPars, useFqn, parametersList)));

    if (commentsForRemovedParameters != null) {
      transList.add(new StringInserter(method.getCompilationUnit(),
          method.getStartLine(),
          method.getStartColumn() - 1,
          commentsForRemovedParameters));
    }

//    if (change.isReordered) {
//      transList.add(new StringInserter(method.getCompilationUnit(), startLine,
//          0, "//FIXME: the behaviour of method might have changed," +
//          FormatSettings.LINEBREAK +
//          "//       because of parameter reordering\n" + FormatSettings.LINEBREAK));
//    }

    if (!method.getReturnType().equals(change.getReturnType())) {
    	boolean useFqnForReturnType;
    	try {
    		importManager.addExtraImports(change.getReturnType(), method.getOwner());
    		useFqnForReturnType = false;
    	} catch (AmbiguousImportImportException e) {
    		useFqnForReturnType = true;
    	}

  transList.add(new RenameTransformation(method.getCompilationUnit(),
      new CompoundASTImpl(method.getReturnTypeAST()),
      useFqnForReturnType ? BinFormatter.formatQualified
      (change.getReturnType()) : BinFormatter.formatNotQualified(change.getReturnType())));
    }
  }

  private String getParametersString
      (BinParameter[] newPars, boolean[] useFqn, List parametersList) {

    ASTImpl ast = method.getParametersAst();
    int startLine = ast.getStartLine();

    SourceCoordinate src = method.getParamsClosingBracket();
    int endLine = src.getLine();

    MultiValueMap map = CommentAllocator.allocateComments(method);

    boolean br = false;
    if (startLine != endLine && !map.isEmpty()) {
      br = true;
    }

/*
    if (map.isEmpty()) {
      //that's originall code, that removed comments
      BinMethod newMethod = new BinMethod(method.getName(), newPars,
          change.getReturnType(),
          BinModifier.setFlags(method.getModifiers(),
          change.getAccessModifier()), method.getThrows());
      return BinFormatter.formatMethodParameters(newMethod, useFqn);
    }
*/

    String str = "";

    // get indent
    int ind = method.getIndent() + FormatSettings.getContinuationIndent();
    String indent = FormatSettings.getIndentString(ind);

    if (br) {
      str += FormatSettings.LINEBREAK + indent;
    }

    // adding parameters
    for (int i = 0; i < newPars.length; i++) {
      ParameterInfo info = (ParameterInfo) parametersList.get(i);

      List li = null;
      if (info instanceof ExistingParameterInfo) {
        li = ((ExistingParameterInfo) info).getComments();
      }

      String tmp = getParamString(newPars[i], useFqn[i]);

      if (i != newPars.length - 1) {
        tmp += ", ";
      } else

      if (li != null) {
        tmp += " ";
      }

      if (!br) {
        str += getCommentsString(li, br, ind + tmp.length() - 1);
      }

      str += tmp;

      if (br) {
        str += getCommentsString(li, br, ind + tmp.length() - 1);
      }

      if (br && i != newPars.length - 1) {
        str += FormatSettings.LINEBREAK + indent;
      }
    }

    // adding coments for deleted variables
    List removed = change.getDeletedParameters();
    getCommentsForRemoved(removed, ind);

    if (br) {
      str += FormatSettings.LINEBREAK +
          FormatSettings.getIndentString(method.getIndent());
    }

    return str;
  }

  private String getParamString(BinParameter param, boolean useFqn) {
    BinModifierFormatter modifierFormatter =
        new BinModifierFormatter(param.getModifiers());
    modifierFormatter.needsPostfix(true);

    String tmp = modifierFormatter.print();

    if (useFqn) {
      tmp += BinFormatter.formatQualified(param.getTypeRef());
    } else {
      tmp += BinFormatter.formatNotQualified(param.getTypeRef());
    }

    tmp += " " + param.getName();
    return tmp;
  }

  private void getCommentsForRemoved(List li, int ind) {
    if (li != null) {
      String tmp = "/* FIXME Comments for deleted parameters: */" +
          FormatSettings.LINEBREAK;

      boolean ifFound = false;
      for (Iterator iter = li.iterator(); iter.hasNext(); ) {
        ParameterInfo info = (ParameterInfo) iter.next();

        if (info instanceof ExistingParameterInfo) {
          List comments = ((ExistingParameterInfo) info).getComments();

          if (comments == null) {
            break;
          }
          ifFound = true;

          for (Iterator iter2 = comments.listIterator(); iter2.hasNext(); ) {
            tmp += ((Comment) iter2.next()).getText();

            if (iter2.hasNext()) {
              tmp += FormatSettings.LINEBREAK;
            }
          }

          int i = method.getIndent() + FormatSettings.getContinuationIndent();
          tmp = CommentAllocator.indentifyComment(tmp, i, false);
          tmp += FormatSettings.LINEBREAK +
              FormatSettings.getIndentString(method.getIndent());
        }
      }

      if (ifFound) {
        commentsForRemovedParameters = tmp;
        return;
      }
    }
    commentsForRemovedParameters = null;
  }

  private String getCommentsString (List li, boolean br, int indent) {

    String str = "";

    if (li != null) {

      for (Iterator iter = li.iterator(); iter.hasNext(); ) {
        String comment = ((Comment) iter.next()).getText();
        str += comment + " ";

        if (br && iter.hasNext()) {
          str += FormatSettings.LINEBREAK;
        }

      }

      if (br) {
        str = CommentAllocator.indentifyComment(str, indent, false);
      }

      return str;
    }

    return new String("");
  }

  private void renameParameterReferencesIfNeeded(final TransformationList
      transList) {

    List parameters = change.getParametersList(method);

    for (int index = 0; index < parameters.size(); ++index) {
      if (!(parameters.get(index) instanceof ExistingParameterInfo)) {
        continue;
      }
      ExistingParameterInfo item = (ExistingParameterInfo) parameters.get(index);

      if (item.getName().equals(item.getOriginalParameter().getName())) {
        continue;
      }
      List invocations = change.getInvocations(item.getOriginalParameter());

      List astsToEdit = new ArrayList(invocations.size());

      CompilationUnit compilationUnit = method.getCompilationUnit();

      for (int indx = 0; indx < invocations.size(); ++indx) {
        InvocationData data = (InvocationData) invocations.get(indx);

        if (!skipInvocationRenaming(data)) {
          astsToEdit.add(data.getWhereAst());
          Assert.must(data.getCompilationUnit() == compilationUnit);
        }
      }

      transList.add(new RenameTransformation(compilationUnit, astsToEdit, item.getName()));

//      BinParameter var=item.getOriginalParameter();
//
//      RenameLocal renamer=new RenameLocal(IDEController.getInstance().
//                                          createProjectContext(),
//                                          IDEController.getInstance().
//                                          getIDEMainWindow(),var);
//      renamer.setNewName(item.getName());
//      renamer.setSkipDeclarations(true);
//      renamer.setShowConfirmationDialog(false);
//      renamer.performChange(editor);

    }

  }

  private boolean skipInvocationRenaming(InvocationData data) {

    if (!(data.getInConstruct() instanceof BinVariableUseExpression)) {
      AppRegistry.getLogger(this.getClass()).debug("Unexpected inConstruct " + data.getInConstruct());
      return true;
    }

    BinVariableUseExpression expr = (BinVariableUseExpression) data.
        getInConstruct();

    if (isUsedInHierarchyMethodInvocation(expr)) {
      return true;
    }

    return false;
  }

  private boolean isUsedInHierarchyMethodInvocation(BinVariableUseExpression
      expr) {

    BinItemVisitable parent = expr.getParent();

    while (!(parent instanceof BinMember)) {

      if (parent instanceof MethodOrConstructorInvocationExpression) {

        final BinMethod invokedIn = ((MethodOrConstructorInvocationExpression)
            parent).getMethod();
        if (change.isHierarchyMethod(invokedIn)) {
          return true;
        }
      }
      parent = parent.getParent();
    }
    return false;
  }

  private boolean isChangeAccessNeeded() {
    if ((change.getAccessModifier() == method.getAccessModifier())
        && (change.isStatic == method.isStatic()
        && (change.isFinal == method.isFinal()))
        || change.getAccessModifier() == BinModifier.INVALID) {
      return false;
    }

    if (method == change.getMethod()) {
      return true;
    }

    if (method.getOwner().isDerivedFrom(change.getMethod().getOwner())) {
      // overrides main method

      if (BinModifier.compareAccesses(change.getAccessModifier(),
          method.getAccessModifier()) == +1) {
        return true;
      }

      if (change.isChangingToWeakerAccess()) {
        return true;
      }
    } else if (change.getMethod().getOwner().isDerivedFrom(method.getOwner())) {
      // overriden by main method

      if (BinModifier.compareAccesses(method.getAccessModifier(),
          change.getAccessModifier()) == -1) {
        return true;
      }
    }
    return false;
  }

  private void jbInit() throws Exception {
  }
}
