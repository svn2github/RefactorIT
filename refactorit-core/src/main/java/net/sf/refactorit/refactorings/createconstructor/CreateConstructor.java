/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.createconstructor;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.BinMethod.Throws;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


public class CreateConstructor extends AbstractRefactoring {
  public static String key = "refactoring.createconstructor";

  private BinField[] fields;

  public CreateConstructor(RefactorItContext context, BinSelection selection) {
    super("Create Constructor", context);
    ArrayList selectedFields = new ArrayList();

    BinCIType type = findCITypeForSelection(selection);
    if (type != null) {
      BinField[] fields = type.getDeclaredFields();

      for (int i = 0; i < fields.length; i++) {
        if (isContained(fields[i], selection)) {
          selectedFields.add(fields[i]);
        }
      }
    }

    this.fields = (BinField[]) selectedFields.toArray(
        new BinField[selectedFields.size()]);
  }

  /**
   *
   * @param context
   * @param fields list of  {@link BinField}s
   */
  public CreateConstructor(RefactorItContext context, List fields) {
    super("Create Constructor", context);
    ArrayList selectedFields = new ArrayList();

    for (int i = 0; i < fields.size(); i++) {
      if (fields.get(i) instanceof BinField) {
        selectedFields.add(fields.get(i));
      }
    }

    this.fields = (BinField[]) selectedFields.toArray(
        new BinField[selectedFields.size()]);
  }

  private BinCIType findCITypeForSelection(BinSelection s) {
    CompilationUnit cu = s.getCompilationUnit();

    List list = cu.getDefinedTypes();

    //return first suitable type for this selection
    for (int i = 0; i < list.size(); i++) {
      BinTypeRef ref = (BinTypeRef) list.get(i);

      if (ref.getBinCIType().getStartLine() <= s.getStartLine() &&
          ref.getBinCIType().getEndLine() >= s.getEndLine()) {
        if (ref.getBinCIType().getStartLine() == s.getStartLine()) {
          if (!(ref.getBinCIType().getStartColumn() <= s.getStartColumn())) {
            continue;
          }
        }

        if (ref.getBinCIType().getEndLine() == s.getEndLine()) {
          if (!(ref.getBinCIType().getEndColumn() >= s.getEndColumn())) {
            continue;
          }
        }
        return ref.getBinCIType();
      }

    }

    return null;
  }

  private boolean isContained(BinField field, BinSelection s) {
    ASTImpl nameAst = field.getNameAstOrNull();
    if (nameAst == null) {
      return false;
    }

    int sL = nameAst.getStartLine(),
        eL = nameAst.getEndLine(),
        sC = nameAst.getStartColumn(),
        eC = nameAst.getEndColumn();

    if (sL > s.getStartLine() && sL < s.getEndLine()
        || eL > s.getStartLine() && eL < s.getEndLine()) {
      return true;
    }

    if (sL == s.getStartLine() && sC >= s.getStartColumn()) {
      return true;
    }

    if (eL == s.getEndLine() && eC <= s.getEndColumn()) {
      return true;
    }

    return false;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkPreconditions
   */
  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();

    // check that user selected any fields
    if (fields.length == 0) {
      status.addEntry("No object fields selected!", RefactoringStatus.ERROR);
      return status;
    }

    status.merge(checkThatTheyAreNotAlreadyInitializedFinals());

    BinCIType type = fields[0].getParentType();

    // check if it is interface
    if (type instanceof BinInterface) {
      status.addEntry("Interface can not have constructors",
          RefactoringStatus.ERROR);
      return status;
    }

    // check that there is no such constructor available that we start to create.
    status.merge(checkSuchCunstructorNotPresent());

    return status;
  }

  private RefactoringStatus checkSuchCunstructorNotPresent() {
    RefactoringStatus status = new RefactoringStatus();
    BinClass binClass = (BinClass) fields[0].getParentType();

    BinTypeRef[] newTypes = new BinTypeRef[fields.length];
    for (int i = 0; i < fields.length; i++) {
      newTypes[i] = fields[i].getTypeRef();
    }

    if (binClass.getAccessibleConstructor(binClass, newTypes) != null) {
      status.addEntry(
          "There is already a constructor with the same signature.\n",
          RefactoringStatus.ERROR);
    }
    return status;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#checkUserInput
   */
  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();
    return status;
  }

  /**
   * @see net.sf.refactorit.refactorings.Refactoring#performChange
   */
  public TransformationList performChange() {
    TransformationList transList = new TransformationList();

    SourceCoordinate coordinate = fields[0].getParentType().
        findNewConstructorPosition();

    BinConstructor constructor = generateConstructor();
    BinMethodFormatter formatter = (BinMethodFormatter) constructor.
        getFormatter();

    StringBuffer buffer = new StringBuffer();

    buffer.append(FormatSettings.LINEBREAK);
    buffer.append(generateJavaDoc());
    buffer.append(formatter.formHeader());
    buffer.append(generateConstructorBody());
    buffer.append(formatter.formFooter());
    buffer.append(FormatSettings.LINEBREAK);

    transList.add(new StringInserter(fields[0].getCompilationUnit(), coordinate,
        buffer.toString()));

    return transList;
  }

  private String generateJavaDoc() {
    StringBuffer buf = new StringBuffer();
    String block = FormatSettings.getIndentString(FormatSettings.
        getBlockIndent());

    buf.append(block + "/**" + FormatSettings.LINEBREAK);

    for (int i = 0; i < fields.length; i++) {
      buf.append(block + " * @param " + fields[i].getName()
          + FormatSettings.LINEBREAK);
    }

    buf.append(block + " */" + FormatSettings.LINEBREAK);

    return buf.toString();
  }

  private BinConstructor generateConstructor() {
    BinParameter[] params = new BinParameter[fields.length];
    for (int i = 0; i < fields.length; i++) {
      params[i] = new BinParameter(fields[i].getName(),
          fields[i].getTypeRef(), BinModifier.NONE);
    }
    BinConstructor constructor = new BinConstructor(params, BinModifier.PUBLIC,
        new Throws[0]);
    constructor.setName(fields[0].getParentType().getName());

    return constructor;
  }

// may be little better mechanism to find place for the new constructor
//  private SourceCoordinate findNewConstructorCoord() {
//    SourceCoordinate coordinate = null;
//    BinClass type = (BinClass) selectedFields[0].getParentType();
//    BinConstructor[] consts = type.getDeclaredConstructors();
//
//    if(consts == null || consts.length == 0) {
//      //place after last field
//      BinField[] fields = type.getDeclaredFields();
//      BinField field = fields[fields.length-1];
//
//      coordinate = new SourceCoordinate(field.getEndLine()+1,
//          0);
//
//    } else {
//      BinConstructor c = consts[consts.length-1];
//
//      coordinate = new SourceCoordinate(c.getEndLine()+1,
//          0);
//    }
//
//    return coordinate;
//  }

  private String generateConstructorBody() {
    StringBuffer body = new StringBuffer();
    String ident = FormatSettings.getIndentString(FormatSettings.
        getBlockIndent())
        + FormatSettings.getIndentString(FormatSettings.getTabSize());
    for (int i = 0; i < fields.length; i++) {
      body.append(
          ident +
          "this." + fields[i].getName() + " = "
          + fields[i].getName() + ";" + FormatSettings.LINEBREAK);
    }
    return body.toString();
  }

  private RefactoringStatus checkThatTheyAreNotAlreadyInitializedFinals() {
    RefactoringStatus status = new RefactoringStatus();
    for (int i = 0; i < fields.length; i++) {
      // should not be final, and have initialization in declaration
      if (fields[i].isFinal()
          && fields[i].getExpression() != null) {

        status.addEntry("Field " + fields[i].getName()
            + " is final and have\n"
            + "an initialization expression already,\n"
            + "so it can't be reinitialized in the constructor.",
            RefactoringStatus.ERROR);
      }
    }

    return status;
  }

  public final String getDescription() {
    StringBuffer buf = new StringBuffer();

    buf.append("Create constructor for: ");

    BinCIType type = fields[0].getParentType();
    if (type != null) {
      buf.append(type.getName());

      return buf.toString(); //super.getDescription();
    }
    return "";
  }

  public String getKey() {
    return key;
  }
}
