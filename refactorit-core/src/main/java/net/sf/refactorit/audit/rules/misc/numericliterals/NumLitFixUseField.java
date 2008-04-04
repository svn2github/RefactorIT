/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.conflicts.Conflict;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.ui.audit.numericliterals.NumLitDialog;

import java.util.Map;


/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitFixUseField extends NumLitFix {

  private Map usedConstants;
  private BinField field;
  private boolean canUseShortName;
  private boolean allowConstantalize = false;

  public NumLitFixUseField(NumericLiteral violation, BinField field,
      Map usedConstants, boolean canUseShortName, boolean constantalize) {
    super(violation);
    this.field = field;
    this.canUseShortName = canUseShortName;
    this.usedConstants = usedConstants;

    if (violation instanceof NumericLiteralField
        && ((NumericLiteralField) violation).isConstantalizable()
        && constantalize){
      allowConstantalize = true;
    }
  }

  private String getNameToInsert(){
    return (!this.canUseShortName ? field.getOwner().getName() + "." : "")
        + field.getName();
  }

  public BinField getField() {
    return this.field;
  }

  protected void createPreviewEditors() {
    BinLiteralExpression expr = (BinLiteralExpression) violation
        .getSourceConstruct();

    // remove old literal
    StringEraser eraser = new StringEraser(
        expr.getCompilationUnit(),
        expr.getStartLine(),
        expr.getStartColumn()-1,
        expr.getEndLine(),
        expr.getEndColumn()-1
    );
    editors.add(eraser);

    // insert field name
    StringInserter inserter = new StringInserter(
        expr.getCompilationUnit(),
        expr.getStartLine(),
        expr.getStartColumn()-1,
        getNameToInsert()
    );
    editors.add(inserter);

    if (allowConstantalize){
      final BinField member = ((NumericLiteralField) violation).getField();
      int modifiers = member.getModifiers();
      modifiers = BinModifier.setFlags(
          modifiers, BinModifier.FINAL | BinModifier.STATIC);
      editors.add(new ModifierEditor(member, modifiers));
    }
  }

  protected void createOtherEditors() {
    BinLiteralExpression expr = (BinLiteralExpression) violation
        .getSourceConstruct();

    NumLitFix createField
        = (NumLitFix) usedConstants.get(field);
    if (createField != null){
      editors.merge(createField.getTransformationList());
      usedConstants.remove(field);
    }

    // check need of import
    if (!NumLitDialog.importedTypes.contains(field.getOwner())
        && !ImportUtils.hasTypeImported(expr.getCompilationUnit(),
        field.getOwner().getQualifiedName(),
        field.getOwner().getPackage())){

      BinTypeRef typeToImport = field.getOwner();

      // ambiguous import, better use QualifiedName then
      if (ImportUtils.isAmbiguousImport(typeToImport.getBinCIType(),
          expr.getOwner().getBinCIType())){
         // FIXME: what to do in this case?
      } else {

        ImportManager importManager = new ImportManager();
        Conflict conflict = importManager
           .addExtraImport(expr.getOwner().getBinCIType(), typeToImport,
           expr.getOwner());
        if (conflict == null){
          importManager.createEditors(editors);
          NumLitDialog.importedTypes.add(field.getOwner());
        } else {
          // FIXME: what to do in this case?
        }
      }
    }
  }

}
