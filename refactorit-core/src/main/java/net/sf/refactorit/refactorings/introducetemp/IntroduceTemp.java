/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.introducetemp;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.PropertyNameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 * @author Kirill Buhhalko
 */
public class IntroduceTemp extends AbstractRefactoring {
  public static String key = "refactoring.introducetemp";

  private BinSelection selection;

  private final IntroduceTempAnalyzer analyzer;

  private String newVarName = null;
  private boolean replaceAll = true;
  private boolean declareFinal = false;
  private boolean declareInForStatement = false;

  public IntroduceTemp(RefactorItContext context, BinSelection selection) {
    super("Introduce Explaining Variable", context);

    this.selection = selection;
    this.analyzer = new IntroduceTempAnalyzer(context, selection);
  }

  public RefactoringStatus checkPreconditions() {
    final RefactoringStatus status = new RefactoringStatus();

    status.merge(this.analyzer.getStatus());

    if (this.declareInForStatement && !analyzer.canBeDeclaredInForStatement()) {
      status.addEntry("Can't be put into for loop initializer",
          RefactoringStatus.ERROR);
    }

    if (!status.isOk()) {
      return status;
    }

// TODO
//    if (getTargetLocation() == null) {
//      status.addEntry("Selection is not contained within a statement",
//          RefactoringStatus.ERROR);
//    }

    return status;
  }

  public RefactoringStatus checkUserInput() {
    final RefactoringStatus status = new RefactoringStatus();

    if (this.newVarName == null || this.newVarName.trim().length() == 0) {
      status.addEntry(
          "New variable should have a name specified",
          RefactoringStatus.ERROR);
      return status;
    }

    if (!NameUtil.isValidIdentifier(this.newVarName)) {
      status.addEntry(
          "Entered new variable name is not a valid Java identifier",
          RefactoringStatus.ERROR);
      return status;
    }

    LocalVariableDuplicatesFinder duplicateFinder
        = new LocalVariableDuplicatesFinder(null, this.newVarName,
        getTargetLocation(this.analyzer.getExtractableExpressions()));
    this.analyzer.getRangeMember().accept(duplicateFinder);

    if (duplicateFinder.getDuplicates().size() > 0) {
      status.addEntry(
          "There are variables with such name within the current scope",
          duplicateFinder.getDuplicates(),
          RefactoringStatus.ERROR);
      return status;
    }

    //<FIX author Aleksei Sosnovski>
/** Original code, seems not to be needed any more
 *
 *  final BinCIType owner = getRangeType();
 *  BinField existingField = owner.getAccessibleField(getNewVarName(), owner);
 *   if (existingField != null) {
 *    status.addEntry(
 *        "There is existing accessible field with such name within the current scope",
 *        CollectionUtil.singletonArrayList(existingField),
 *        RefactoringStatus.WARNING);
 *  }
 */

    List li = getAccessibleLocalVariables();
    if (li != null) {
      status.addEntry(
          "There are local variables with such name within the current scope",
          li, RefactoringStatus.WARNING);
    }

    final BinCIType owner2 = (this.analyzer.getExtractableExpressions())[0].
        getOwner().getBinCIType();
    BinField existingField2 = getAccessibleField(owner2, getNewVarName(), owner2);
    if (existingField2 != null) {
      status.addEntry(
          "There is existing accessible field with such name within the current scope",
          CollectionUtil.singletonArrayList(existingField2),
          RefactoringStatus.WARNING);
    }
    //</FIX>

    if (declareInForStatement && replaceAll) {
      status.addEntry("Can't declare in For initializer and replace " +
          "all usages in same time", RefactoringStatus.ERROR);
    }

    //<FIX REf-1358 author Aleksei Sosnovski>
    if (selection.getText().trim().equals(this.newVarName)) {
      status.addEntry("Cannot assign to variable value of variable with same" +
          "name, please enter another variable name", RefactoringStatus.ERROR);
      return status;
    }
    //</FIX>

    return status;
  }

  private BinCIType getRangeType() {
    return this.analyzer.getRangeMember().getOwner().getBinCIType();
  }

  private List getAccessibleLocalVariables() { //author Aleksei Sosnovski
    DupFinder duplicateFinder
        = new DupFinder(this.newVarName);

    BinCIType type =
        this.analyzer.getExtractableExpressions()[0].getOwner().getBinCIType();
    BinItemVisitable item = type.getParent();

    while (item != null) {

      if (item instanceof BinMethod) {
        ((BinMember) item).accept(duplicateFinder);

        if (duplicateFinder.getVars().size() > 0) {
          return duplicateFinder.getVars();
        }
      }

      item = item.getParent();
    }

    return null;
  }

  private BinField getAccessibleField //author Aleksei Sosnovski
      (BinCIType owner, String name, BinCIType context) {
    BinField field = null;

    do {
      field = owner.getAccessibleField(name, context);

      if (field != null) {
        return field;
      }

      if (owner.getOwner() != null) {
        owner = owner.getOwner().getBinCIType();
      } else
        owner = null;

    } while (owner != null);

    return null;
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    if (this.selection.getText().trim().length() == 0) {
      return transList;
    }

    final BinExpression[] exprs = this.analyzer.getExtractableExpressions();

    insertNewTempDeclaration(transList, exprs);
    editExtractedExpressions(transList, exprs);

    return transList;
  }

  private void editExtractedExpressions(final TransformationList transList,
      final BinExpression[] expressions) {
    BinExpression[] exprs;
    if (isReplaceAll()) {
      exprs = expressions;
    } else {
      exprs = new BinExpression[1];
      exprs[0] = this.analyzer.getSelectedExpression();
    }

    final CompilationUnit source = exprs[0].getCompilationUnit();
    for (int i = 0; i < exprs.length; i++) {
      ASTImpl ast = exprs[i].getRootAst();
      boolean dontAdd = false;
      if (exprs[i].getParent() instanceof BinExpressionStatement) {
        ast = ((BinSourceConstruct) exprs[i].getParent()).getRootAst();
        dontAdd = true;
      } else {
        while (ast.getParent().getType() == JavaTokenTypes.LPAREN
            && ast.getNextSibling() == null
            && ast.getParent().getFirstChild() == ast) { // just surrounded with ()
          ast = ast.getParent();
        }
      }

      ast = new CompoundASTImpl(ast);

      StringEraser eraser = new StringEraser(source, ast, false);
      transList.add(eraser);

      if (!dontAdd) {
        transList.add(new StringInserter(source,
            ast.getEndLine(), ast.getEndColumn() - 1,
            getNewVarName()));
      }
    }
  }

  private void insertNewTempDeclaration(final TransformationList transList,
      final BinExpression[] exprs) {
    boolean useFqn = false;
    ImportManager imports = new ImportManager();
    try {
      imports.addExtraImports(
          getVarType(),
          this.analyzer.getRangeMember().getTopLevelEnclosingType().getTypeRef());
      imports.createEditors(transList);
    } catch (AmbiguousImportImportException e) {
      useFqn = true;
    }

    final LocationAware targetLocation = getTargetLocation(exprs);
    String varDeclaration = "";
    if (this.declareFinal) {
      BinModifierFormatter formatter = new BinModifierFormatter(BinModifier.
          FINAL);
      formatter.needsPostfix(true);
      varDeclaration += formatter.print();
    }

    if (!declareInForStatement
        || (declareInForStatement && analyzer.isEmtyForInitStatement())) {
      varDeclaration += useFqn ? BinFormatter.formatQualified(getVarType())
          : BinFormatter.formatNotQualified(getVarType());
      varDeclaration += " " + getNewVarName() + " = ";
      if (declareInForStatement && analyzer.isEmtyForInitStatement()) {
        transList.add(new StringEraser(
            findForsInitializerStatementPlace(analyzer.getForStatement(
            analyzer.getSelectedExpression())), true));
      }
    } else {
      varDeclaration += ((BinLocalVariableDeclaration) targetLocation).
          getText();
      varDeclaration += ", " + getNewVarName() + " = ";
      transList.add(new StringEraser(targetLocation));
    }
    String selectionText = this.selection.getText().trim();

// TODO: this requires more analysis or would mistake on e.g. "(1+2) + (3+4)"
//    while (selectionText.startsWith("(") && selectionText.endsWith(")")) {
//      selectionText = selectionText.substring(1, selectionText.length() - 1);
//    }

    varDeclaration += selectionText;

    if (!declareInForStatement
        || (declareInForStatement && analyzer.isEmtyForInitStatement())) {

      varDeclaration += ";";

      if (declareInForStatement && analyzer.isEmtyForInitStatement()) {
        varDeclaration += " ";
      }
    }

    if (!this.analyzer.isFirstExpressionBecomesInitializer()
        && !declareInForStatement) {
      // i.e. are we generating new standalone statement?
      int indent = targetLocation.getIndent();
      varDeclaration += FormatSettings.LINEBREAK;
      varDeclaration += FormatSettings.getIndentString(indent);
    }

    transList.add(new StringInserter(exprs[0].getCompilationUnit(),
        targetLocation.getStartLine(), targetLocation.getStartColumn() - 1,
        varDeclaration));

  }

  //use it only when initializer is emty,
  //so BinForStatement.getInitSrcCnstr.() == null
  private BinSelection findForsInitializerStatementPlace(BinForStatement fSt) {
    String text = fSt.getText();
    int start = fSt.getStartPosition();
    int offset = 0;
    int max_offset = fSt.getEndPosition() - start;
    for (; text.charAt(offset) != '(' && offset <= max_offset; offset++);

    if (text.charAt(offset) != '(') {
      return null;
    }

    int startInitPosition = start + offset + 1;
    int endPosition = fSt.getCondition().getStartPosition();

    BinSelection binSel = new BinSelection(fSt.getCompilationUnit(), ";",
        startInitPosition, endPosition);
    return binSel;
  }

  public String getNewVarName() {
    return this.newVarName;
  }

  public void setNewVarName(final String newVarName) {
    this.newVarName = newVarName;
  }

  /** @return we are going to insert right before this location */
  private LocationAware getTargetLocation(final BinExpression[] expressions) {
    BinStatement topScope = null;

    if (declareInForStatement) {
      if ((analyzer.getForStatement(analyzer.getSelectedExpression())).
          getInitSourceConstruct() != null) {
        return (analyzer.getForStatement(analyzer.getSelectedExpression())).
            getInitSourceConstruct();
      } else {
        return analyzer.getForStatement(analyzer.getSelectedExpression()).
            getCondition();
      }

    }

    BinExpression[] exprs;
    if (isReplaceAll()) {
      exprs = expressions;
    } else {
      exprs = new BinExpression[1];
      exprs[0] = this.analyzer.getSelectedExpression();
    }

    for (int i = 0; i < exprs.length; i++) {
      BinStatement scope = exprs[i].getEnclosingStatement();
      if (topScope == null) {
        topScope = scope;
      } else {
        if (scope.contains(topScope)) {
          topScope = scope;
        } else {
          if (topScope.isAfter(scope)) {
            BinStatement tmp = topScope;
            topScope = scope;
            scope = tmp;
          }

          topScope = getMinimalCommonLocation(topScope, scope);
        }
      }

      topScope = getStandaloneStatement(topScope);
    }

    return topScope;
  }

  private BinStatement getStandaloneStatement(final BinStatement statement) {
    BinStatement standalone = statement;
//System.err.println("standalone: " + standalone + ", parent: " + standalone.getParent());
    while (!(standalone.getParent() instanceof BinStatementList)
        || !"{".equals(((BinStatementList) standalone.getParent())
        .getRootAst().getText())) {
      standalone = (BinStatement) standalone.getParent();
//System.err.println("standalone1: " + standalone + ", parent: " + standalone.getParent());
    }

    return standalone;
  }

  private BinStatement getMinimalCommonLocation(final BinStatement firstBranch,
      final BinStatement secondBranch) {
    BinStatement cur = firstBranch;
    BinStatement previous = cur;

    while (!cur.contains(secondBranch)) {
      previous = cur;
      cur = (BinStatement) cur.getParent();
    }

    // this way previous and secondBranch must be siblings of the same parent
    // and we can put our temp just infront of first of them
    return previous;
  }

  public boolean isReplaceAll() {
    return this.replaceAll;
  }

  public void setReplaceAll(final boolean replaceAll) {
    this.replaceAll = replaceAll;
  }

  public boolean isDeclareFinal() {
    return this.declareFinal;
  }

  public void setDeclareFinal(final boolean makeFinal) {
    this.declareFinal = makeFinal;
  }

  public int getOccurencesNumber() {
    return this.analyzer.getExtractableExpressions().length;
  }

  public BinTypeRef getVarType() {
    BinTypeRef varType
        = this.analyzer.getExtractableExpressions()[0].getReturnType();
    if (varType == null) {
      return getProject().objectRef;
    } else {
      BinTypeRef result = varType;
      if (varType.getBinType().isAnonymous()
          || varType.getBinType().isTypeParameter()
          || varType.getBinType().isWildcard()) {
        result = varType.getSupertypes()[0];
        if (result == getProject().objectRef) {
          BinTypeRef interf = getDeepestInterface(varType);
          result = (interf == null) ? result : interf;
        }
      }
      return result;
    }

  }

  /**
   * @param varType
   * @return
   */
  private BinTypeRef getDeepestInterface(BinTypeRef varType) {
    BinTypeRef result = null;
    if (varType.getInterfaces().length > 0) {
      for (int i = 0; i < varType.getInterfaces().length; i++) {
        BinTypeRef interf = varType.getInterfaces()[i];
        if ((result == null)
            || (interf.getTypeRef().getInterfaces().length
            > result.getInterfaces().length)) {
          result = interf.getTypeRef();
        }
      }
    }
    return result;
  }

  // TODO extract into separate class and reuse in InlineMethod
  public String getPossibleName() {
    // TODO add better heuristics - watch IDEA for this!
    String name = null;
    BinExpression expr = this.analyzer.getExtractableExpressions()[0];
    if (expr == null) {
      return "";
    }

    boolean plural = false;

    BinTypeRef type = null;
    if (expr instanceof BinMethodInvocationExpression) {
      BinMethod method = ((BinMethodInvocationExpression) expr).getMethod();
      name = PropertyNameUtil.getFieldName(method.getName());
      if (name != null && name.length() == 0) {
        if (method.getReturnType().isReferenceType()) {
          type = method.getReturnType();
        }
      } else if (name == null && method.getName().length() > 1
          && !method.getOwner().equals(method.getProject().getObjectRef())) { // let it be (c)
        if ("toString".equals(method.getName())) {
          name = "str";
        } else {
          name = method.getName();
        }
      }
    } else if (expr instanceof BinFieldInvocationExpression
        && expr.getReturnType().isReferenceType()) {
      String tName = ((BinFieldInvocationExpression) expr).getField().getName();
      name = tName;
      if (tName.toUpperCase().equals(tName)) {
        tName = tName.toLowerCase();
      }
      if (tName.equals(tName)) {
        name = tName + "_";
      }
    } else if (expr instanceof BinNewExpression) {
      type = expr.getReturnType();
      if (type.getBinType().isAnonymous()) {
        BinTypeRef candidateType = type.getSupertypes()[0];
        if (candidateType == getProject().objectRef) {
          BinTypeRef interf = getDeepestInterface(type);
          type = (interf == null) ? candidateType : interf;
        }
      }
    } else if (expr instanceof BinCastExpression) {
      type = ((BinCastExpression) expr).getReturnType();
    } else if (expr instanceof BinLiteralExpression &&
        expr.getReturnType() != null &&
        expr.getReturnType().isString()) {
      name = "str";
    }

    if (type != null && type.isArray()) {
      type = ((BinArrayType) type.getBinType()).getArrayType();
      plural = true;
    }

    if ((name == null || name.length() == 0)
        && type != null && type.isReferenceType()) {
      final BinTypeRef[] interfaces = type.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        // concrete implementation of generic interface? e.g. List in ArrayList
        if (type.getName().indexOf(interfaces[i].getName()) != -1) {
          type = interfaces[i];
          break;
        }
      }

      name = type.getName();
    }

    // primitives fall here
    if (name == null || name.length() == 0) {
      // FIXME refactor
      type = expr.getReturnType();
      if (type != null) {
        if (type.isArray()) {
          type = ((BinArrayType) type.getBinType()).getArrayType();
          plural = true;
        }
        name = type.getName().substring(0, 1);
      } else {
        name = "temp";
      }
    }

    if (!((name.length() > 1) // hack to avoid decapitalizing such cases: NBAction
        && ("" + name.charAt(1)).toUpperCase().equals("" + name.charAt(1)))) {
      name = StringUtil.decapitalizeFirstLetter(name);
    }

    if (plural) {
      name += "s";
    }

    final String oldName = this.newVarName;
    this.newVarName = name;

    int count = 1;
    do {
      //if (count >= 10) {
      //  break;
      //}

      final RefactoringStatus status = checkUserInput();

      if (!status.isOk()) {
        if (this.newVarName.length() == 1
            && Character.isJavaIdentifierStart(
            (char) (this.newVarName.charAt(0) + 1))) {
          this.newVarName = new Character(
              (char) (this.newVarName.charAt(0) + 1)).toString();
        } else {
          this.newVarName = name + count;
          ++count;
        }
        continue;
      } else
      break;

    } while (true);

    name = this.newVarName;
    this.newVarName = oldName;

    return name;
  }

  public String getDescription() {
    return "Introduce " + newVarName + " = " + this.selection.getText().trim();
  }

  public String getKey() {
    return key;
  }

  public void setDeclareInForStatement(final boolean declareInForStatement) {
    this.declareInForStatement = declareInForStatement;
  }

  public boolean canBeDeclaredInForStatement() {
    return analyzer.canBeDeclaredInForStatement();
  }
}

class DupFinder extends BinItemVisitor {
  ArrayList variables = new ArrayList();
  String str;
  int count = 0;
  public DupFinder (String searchFor) {
    str = searchFor;
  }

  public void visit (BinLocalVariable var) {
    if (count == 0 && var.getName().equals(str)) {
      CollectionUtil.addNew (this.variables, var);
    }
  }

  public void visit (BinCIType type) {
    count ++;
  }

  public void leave (BinCIType type) {
    count --;
  }

  public List getVars() {
    return variables;
  }
}
