/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.EmptyLine;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.MoveEditor;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Anton Safonov
 */
public class ExtractMethod extends AbstractRefactoring {
  public static String key = "refactoring.extractmethod";

  public static boolean generateJavadocTemplate = false;

  private BinSelection selection;

  private ExtractMethodAnalyzer analyzer;

  private String methodName = null;

  private int modifier = BinModifier.PRIVATE;

  private String[] newParamNames;

  private int[] newParamIds;

  private List typesToImport = new ArrayList();

  private static final String SPACE = " ";

  private int indent = 0;

  private boolean needsSemicolon = false;

  private boolean needsLinebreak = false;

  private BinMethodFormatter formatter = null;

  public ExtractMethod(RefactorItContext context, BinSelection selection) {
    super("Extract Method", context);

    this.selection = selection;
    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Selection: " + this.selection.toString());
    }

    analyzer = new ExtractMethodAnalyzer(context, this.selection);

    if (ExtractMethodAnalyzer.showDebugMessages) {
      System.err.println("Top LAs: " + analyzer.getLasToMove());
      System.err.println("Fqns: " + analyzer.getFqnTypes());
    }
  }

  public BinVariable[] getAnalyzedParameters() {
    return analyzer.getParameters();
  }

  public BinTypeRef[] getAnalyzedExceptions() {
    return analyzer.getThrownExceptions();
  }

  public void setNewParameterNames(String[] newParamNames) {
    this.newParamNames = newParamNames;
  }

  public void setNewParameterIds(int[] newParamIds) {
    this.newParamIds = newParamIds;
  }

  /**
   * @return status object with severity and messages to be shown to the user
   */
  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();

    if (analyzer.hasErrors()) {
      status.addEntry(analyzer.getErrorMessage(), RefactoringStatus.ERROR);
      return status;
    }

    if (analyzer.WARNING != null) {
        status.addEntry(analyzer.WARNING, RefactoringStatus.WARNING);
    }

    List las = analyzer.getLasToMove();
    if (las == null || las.size() == 0) {
      status.addEntry("You should select something to extract.",
          RefactoringStatus.ERROR);
    }

    return status;
  }

  /**
   * @return status object with severity and messages to be shown to the user
   */
  public RefactoringStatus checkUserInput() {
    final RefactoringStatus status = new RefactoringStatus();

    if (this.methodName == null || this.methodName.length() == 0) {
      status.addEntry("New method name is missing.", RefactoringStatus.ERROR);
    }

    final BinTypeRef[] paramTypes = new BinTypeRef[analyzer.getParameters().length];
    final BinVariable[] params = analyzer.getParameters();
    for (int i = 0; i < params.length; i++) {
      int index = i;
      if (this.newParamIds != null) {
        index = this.newParamIds[index];
      }
      paramTypes[i] = params[index].getTypeRef();
    }

    final BinCIType type = getType();
    final BinMethod[] methods = type
        .getAccessibleMethods(this.methodName, type);
    for (int i = 0; i < methods.length; i++) {
      if (MethodInvocationRules.isApplicable(methods[i], paramTypes)
          && methods[i].getOwner().getBinCIType() == getType()) {
        status.addEntry("Method with the same signature already exists.",
            RefactoringStatus.ERROR);
        break;
      }
    }

    return status;
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    detectBeginningEndingFeatures();

    writeNewMethod(transList);

    writeMethodCall(transList);

    manageImports(transList);

    return transList;
  }

  private String formatTypeName(BinTypeRef typeRef) {
    if (analyzer.getFqnTypes().contains(typeRef)) {
      return BinFormatter.formatQualifiedForTypeArgumentsWithAllOwners(typeRef)
          .replace('$', '.');
    } else {
      return BinFormatter.formatNotQualifiedForTypeArgumentsWithAllOwners(
          typeRef).replace('$', '.');
    }
  }

  private void writeMethodCall(final TransformationList transList) {
    final String newlineIndentString = FormatSettings
        .getIndentString(getBaseIndent() + FormatSettings.getBlockIndent());
    String methodCall = FormatSettings.getIndentString(this.indent); // initial
    // indent

    BinLocalVariable[] needsDeclaration = analyzer.getNeedsDeclarationBefore();
    for (int i = 0; i < needsDeclaration.length; i++) {
      // FIXME: use BinLocalVariableFormatter
      methodCall += formatTypeName(needsDeclaration[i].getTypeRef()) + SPACE
          + needsDeclaration[i].getName() + ";" + FormatSettings.LINEBREAK
          + newlineIndentString;
    }

    ExtractMethodAnalyzer.ReturnType returnType = analyzer.getReturnType();
    if (returnType.status == ExtractMethodAnalyzer.ReturnType.VARIABLE) {
      // FIXME: use BinLocalVariableFormatter
      BinLocalVariable[] declared = analyzer.getDeclared();
      for (int i = 0; i < declared.length; i++) {
        if (declared[i] == returnType.variable) {
          if (declared[i].isFinal()) {
            methodCall += "final" + SPACE;
          }
          methodCall += formatTypeName(declared[i].getTypeRef()) + SPACE;
          break;
        }
      }

      methodCall += returnType.variable.getName() + SPACE + "=" + SPACE;
    } else if (returnType.status == ExtractMethodAnalyzer.ReturnType.RETURN
        && !returnType.typeRef.equals(BinPrimitiveType.VOID_REF)) {
      methodCall += "return" + SPACE;
    }

    methodCall += getMethodName() + "(";
    BinVariable[] params = analyzer.getParameters();
    for (int i = 0; i < params.length; i++) {
      if (i > 0) {
        methodCall += "," + SPACE;
      }
      int index = i;
      if (this.newParamIds != null) {
        index = this.newParamIds[index];
      }
      methodCall += params[index].getName();
    }

    methodCall += ")";

    if (this.needsSemicolon) {
      methodCall += ";";
    }
    if (this.needsLinebreak) {
      methodCall += FormatSettings.LINEBREAK;
    }

    if (returnType.status == ExtractMethodAnalyzer.ReturnType.RETURN
        && BinPrimitiveType.VOID_REF.equals(returnType.typeRef)) {
      if (!this.needsLinebreak) {
        methodCall += FormatSettings.LINEBREAK;
      }
      methodCall += newlineIndentString + "return;";
      if (this.needsLinebreak) {
        methodCall += FormatSettings.LINEBREAK;
      }
    }

    List las = analyzer.getLasToMove();
    transList.add(new StringInserter(selection.getCompilationUnit(),
        ((LocationAware) las.get(0)).getStartLine(), (this.indent > 0 ? 0
            : ((LocationAware) las.get(0)).getStartColumn() - 1), methodCall));
  }

  private void writeNewMethod(final TransformationList transList) {
    if (Assert.enabled) {
      Assert.must(analyzer.getRangeMember() != null,
          "Range member wasn't found during analysis!");
    }

    final SourceCoordinate targetCoordinate = new SourceCoordinate(
        getTargetLine(), 0);
    final int baseIndent = new BinTypeFormatter(analyzer.getRangeMember()
        .getOwner().getBinCIType()).getMemberIndent();

    transList.add(new StringInserter(selection.getCompilationUnit(),
        targetCoordinate, formMethodHeader(baseIndent)));

    boolean returningExpression = false;
    ExtractMethodAnalyzer.ReturnType returnType = analyzer.getReturnType();
    if (returnType.status == ExtractMethodAnalyzer.ReturnType.EXPRESSION
        && !returnType.typeRef.equals(BinPrimitiveType.VOID_REF)) {
      returningExpression = true;
    }

    renameParameters(transList);

    transList.add(new MoveEditor(analyzer.getLasToMove(), targetCoordinate,
        baseIndent + FormatSettings.getBlockIndent(), !returningExpression));

    transList.add(new StringInserter(selection.getCompilationUnit(),
        targetCoordinate, formMethodFooter(baseIndent)));
  }

  private void manageImports(final TransformationList transList) {
    final CompilationUnit source = selection.getCompilationUnit();
    final StringBuffer imports = new StringBuffer();
    final Set alreadyAdded = new HashSet();

    for (int i = 0, max = this.typesToImport.size(); i < max; i++) {
      BinTypeRef typeRef = (BinTypeRef) this.typesToImport.get(i);
      while (typeRef.getBinType() instanceof BinArrayType) {
        typeRef = ((BinArrayType) typeRef.getBinType()).getArrayType();
      }

      if (alreadyAdded.contains(typeRef) || typeRef.isPrimitiveType()
          || typeRef.getQualifiedName().startsWith("java.lang.")
          || analyzer.getFqnTypes().contains(typeRef)) {
        continue;
      }

      if (ImportUtils.needsTypeImported(source, typeRef.getBinCIType(), typeRef
          .getPackage())) {
        imports.append(ImportUtils.generateImportClause(
            typeRef.getQualifiedName()).toString());
        imports.append(ImportUtils.generateNewlines(1).toString());
      }
      alreadyAdded.add(typeRef);
    }

    if (imports.length() > 0) {
      ImportUtils.ImportPosition importPosition = ImportUtils
          .calculateNewImportPosition(source, false);

      StringBuffer importLine = ImportUtils
          .generateNewlines(importPosition.before);
      importLine.append(imports.toString());
      importLine.append(ImportUtils.generateNewlines(importPosition.after - 1)
          .toString());

      transList.add(new StringInserter(source, importPosition.line,
          importPosition.column, importLine.toString()));
    }
  }

  protected int getTargetLine() {
    return analyzer.getRangeMember().getEndLine() + 1;
  }

  protected int getBaseIndent() {
    return analyzer.getRangeMember().getStartColumn() - 1;
  }

  private void renameParameters(final TransformationList transList) {
    if (this.newParamNames == null || this.newParamIds == null
        || analyzer.getUsageMap() == null) {
      return;
    }

    MultiValueMap usageMap = analyzer.getUsageMap();
    CompilationUnit source = analyzer.getRangeMember().getCompilationUnit();

    BinLocalVariable[] analyzedParams = analyzer.getParameters();
    for (int i = 0; i < this.newParamNames.length; i++) {
      int paramId = this.newParamIds[i];
      if (!this.newParamNames[i].equals(analyzedParams[paramId].getName())) {
        List nodes = usageMap.get(analyzedParams[paramId]);
        transList.add(new RenameTransformation(source, nodes,
            this.newParamNames[i]));
      }
    }
  }

  public boolean shouldBeStatic() {
    if (analyzer.canBeStatic() && analyzer.getRangeMember().isStatic()) {
      return true;
    }
    return false;
  }

  public boolean mustBeStatic() {
    BinItemVisitable visitable = analyzer.selectionAnalyzer.findTopExpression();
    while (visitable instanceof BinExpression
        || visitable instanceof BinExpressionList
        || visitable instanceof BinVariable) {
      if (visitable instanceof BinConstructorInvocationExpression) {
        return true;
      }
      if (visitable instanceof BinVariable
          && ((BinVariable) visitable).isStatic()) {
        return true;
      }
      visitable = visitable.getParent();
    }

    return false;
  }

  private String formMethodHeader(int baseIndent) {
    // FIXME baseIndent should be defined by BinItemFormatter

    // actually now it's done in extractMethodaction.java, but somehow is still needed
    if (shouldBeStatic() || mustBeStatic()) {
      modifier |= BinModifier.STATIC;
    }

    BinVariable[] originalParams = analyzer.getParameters();
    BinParameter[] params = new BinParameter[originalParams.length];
    for (int i = 0; i < originalParams.length; i++) {
      int index = i;
      if (this.newParamIds != null) {
        index = this.newParamIds[index];
      }
      String paramName;
      if (this.newParamNames != null) {
        paramName = this.newParamNames[i];
      } else {
        paramName = originalParams[index].getName();
      }
      params[i] = new BinParameter(paramName, originalParams[index]
          .getTypeRef(), originalParams[index].getModifiers());
      this.typesToImport.add(originalParams[index].getTypeRef());
    }

    BinTypeRef[] thrownExceptions = analyzer.getThrownExceptions();
    BinMethod.Throws[] throwses = new BinMethod.Throws[thrownExceptions.length];
    for (int i = 0; i < thrownExceptions.length; i++) {
      throwses[i] = new BinMethod.Throws(thrownExceptions[i]);
      this.typesToImport.add(thrownExceptions[i]);
    }

    BinMember extracted = null;

    // are we extracting to constructor?
    if (getMethodName().equals(analyzer.getRangeMember().getOwner().getName())) {
      extracted = new BinConstructor(params, modifier, throwses);
      ((BinConstructor) extracted).setBody(null);
    } else {
      extracted = new BinMethod(methodName, params,
          analyzer.getReturnType().typeRef, modifier, throwses, true);
      this.typesToImport.add(analyzer.getReturnType().typeRef);
    }
    extracted.setOwner(getType().getTypeRef());

    String result = FormatSettings.LINEBREAK;

    if (generateJavadocTemplate
        && (!analyzer.getReturnType().typeRef.equals(BinPrimitiveType.VOID_REF)
            || params.length > 0 || throwses.length > 0)) { // why should we
      // have totaly empty
      // javadoc?!
      String indent = FormatSettings.getIndentString(baseIndent);
      result += indent + "/**" + FormatSettings.LINEBREAK;
      for (int i = 0; i < params.length; i++) {
        result += indent + " * @param " + params[i].getName()
            + FormatSettings.LINEBREAK;
      }
      if (!analyzer.getReturnType().typeRef.equals(BinPrimitiveType.VOID_REF)) {
        result += indent + " * @return" + FormatSettings.LINEBREAK;
      }
      for (int i = 0; i < throwses.length; i++) {
        result += indent + " * @throws " + throwses[i].getException().getName()
            + FormatSettings.LINEBREAK;
      }
      result += indent + " */" + FormatSettings.LINEBREAK;
    }

    formatter = (BinMethodFormatter) extracted.getFormatter();
    formatter.setFqnTypes(analyzer.getFqnTypes());
    result += formatter.formHeader().replace('$', '.');

    final BinLocalVariable[] needingDeclaration = analyzer
        .getNeedsDeclarationWithin();
    for (int i = 0; i < needingDeclaration.length; i++) {
      result += FormatSettings.getIndentString(baseIndent
          + FormatSettings.getBlockIndent())
          + formatTypeName(needingDeclaration[i].getTypeRef())
          + SPACE
          + needingDeclaration[i].getName() + ";" + FormatSettings.LINEBREAK;
      this.typesToImport.add(needingDeclaration[i].getTypeRef());
    }

    ExtractMethodAnalyzer.ReturnType returnType = analyzer.getReturnType();
    if (returnType.status == ExtractMethodAnalyzer.ReturnType.EXPRESSION
        && !returnType.typeRef.equals(BinPrimitiveType.VOID_REF)) {
      result += FormatSettings.getIndentString(baseIndent
          + FormatSettings.getBlockIndent())
          + "return" + SPACE;
    }

    return result;
  }

  private String formMethodFooter(int baseIndent) {
    String result = "";

    if (!this.needsSemicolon) {
      result += ";";
    }

    if (!this.needsLinebreak) {
      result += FormatSettings.LINEBREAK;
    }

    ExtractMethodAnalyzer.ReturnType returnType = analyzer.getReturnType();
    if (returnType.status == ExtractMethodAnalyzer.ReturnType.VARIABLE) {
      String returnVariableName = returnType.variable.getName();
      if (this.newParamNames != null && this.newParamIds != null) {
        BinLocalVariable[] analyzedParams = analyzer.getParameters();
        for (int i = 0; i < this.newParamNames.length; i++) {
          if (analyzedParams[this.newParamIds[i]] == returnType.variable) {
            returnVariableName = this.newParamNames[i];
            break;
          }
        }
      }

      result += FormatSettings.LINEBREAK
          + FormatSettings.getIndentString(baseIndent
              + FormatSettings.getBlockIndent()) + "return" + SPACE
          + returnVariableName + ";" + FormatSettings.LINEBREAK;
    }

    result += formatter.formFooter();

    return result;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public BinCIType getType() {
    if (analyzer.getRangeMember() != null) {
      return analyzer.getRangeMember().getOwner().getBinCIType();
    } else {
      return null;
    }
  }

  public BinTypeRef getReturnType() {
    return analyzer.getReturnType().typeRef;
  }

  public void setModifier(int modifier) {
    this.modifier = modifier;
  }

  public int getModifier() {
    return this.modifier;
  }

  public void addModifier(int modifier) {
    this.modifier |= modifier;
  }

  /**
   * Searches for the initial indent in the beginning of the block, colon and
   * linebreak at the end
   */
  private void detectBeginningEndingFeatures() {
    List las = analyzer.getLasToMove();

    LineIndexer indexer = this.selection.getCompilationUnit().getLineIndexer();
    String content = this.selection.getCompilationUnit().getContent();

    LocationAware la = (LocationAware) las.get(0);
    int startPos = indexer.lineColToPos(la.getStartLine(),
    // FIXME test if we need to subtract 1 here
        la.getStartColumn() - 1);

    this.indent = 0;
    while (startPos >= 0 && Character.isWhitespace(content.charAt(startPos))
        && content.charAt(startPos) != '\r' && content.charAt(startPos) != '\n') {

      if (content.charAt(startPos) == '\t') {
        indent += FormatSettings.getTabSize();
      } else {
        this.indent++;
      }
      --startPos;
    }
    if (startPos > 0) {
      if (content.charAt(startPos) != '\r' && content.charAt(startPos) != '\n') {
        this.indent = 0;
      }
    }

    int index = las.size();
    do {
      --index;
      la = (LocationAware) las.get(index);
    } while (la != null && (la instanceof Comment || la instanceof EmptyLine));

    if (la != null) {
      // FIXME is that -1 correct?
      int endPos = indexer.lineColToPos(la.getEndLine(), la.getEndColumn() - 1);

      this.needsSemicolon = content.charAt(endPos) == ';';
      if (!this.needsSemicolon) {
        // FIXME: should we specify exact list of "colonable" statements here?
        if (la instanceof BinStatement) {
          this.needsSemicolon = true; // affects method call forming
        }
      }
    }

    la = (LocationAware) las.get(las.size() - 1);
    // FIXME is that -1 correct?
    int endPos = indexer.lineColToPos(la.getEndLine(), la.getEndColumn() - 1);

    // skip whitespace
    ++endPos;
    while (endPos < content.length()
        && Character.isWhitespace(content.charAt(endPos))
        && content.charAt(endPos) != '\r' && content.charAt(endPos) != '\n') {
      ++endPos;
    }

    if (content.charAt(endPos) == '\r' || content.charAt(endPos) == '\n') {
      this.needsLinebreak = true;
    }
  }

  public String getDescription() {

    return "Extract method " + this.getMethodName() + "(..);";// super.getDescription();
  }

  public String getKey() {
    return key;
  }
}
