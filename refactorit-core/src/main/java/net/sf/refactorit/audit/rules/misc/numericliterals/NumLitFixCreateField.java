/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitFixCreateField extends NumLitFix {
  
  private BinField fakeField;

  public NumLitFixCreateField(final BinField fakeField){    
    super(null);
    this.fakeField = fakeField;     
  }

  public static BinField createFakeField(
      final String name, final BinCIType where, final String access, 
      final String literalValue) {
    BinField fakeField;
    int modif = BinModifier.setFlags(0, BinModifier.FINAL | BinModifier.STATIC);
    if (access.equals("public")){
      modif = BinModifier.setFlags(modif, BinModifier.PUBLIC);
    } else if (access.equals("protected")){
      modif = BinModifier.setFlags(modif, BinModifier.PROTECTED);
    } else {
      modif = BinModifier.setFlags(modif, BinModifier.PRIVATE);
    }
    
    fakeField = new BinField(name, 
        where.getProject().getObjectRef(), modif, true){
      public void ensureExpression(){
        return;
      }
      
      public boolean hasExpression(){
        return false;
      }
    };
    
    fakeField.setExpression(new BinExpression(where.getOffsetNode()) {
      public String toString(){
        return literalValue;
      }
      
      public BinTypeRef getReturnType(){
        return null;
      }
    });
    
    fakeField.setOwner(where.getTypeRef());
    
    return fakeField;
  }
  
  public BinField getField() {
    return fakeField;
  }
  
  protected void createPreviewEditors() {
  }
  
  protected void createOtherEditors() {
    // create new field where requested
    List modifierList = BinModifier.splitModifier(fakeField.getModifiers());
    String modifierString = "";
    
    int modifier;
    for (Iterator it = modifierList.iterator(); it.hasNext();){
      modifier = ((Integer) it.next()).intValue();
      modifierString += new BinModifierFormatter(modifier).print() + " ";
    }
    
    String fieldDeclaration = 
        FormatSettings.LINEBREAK 
        + FormatSettings.getIndentString(fakeField.getOwner().getBinCIType()
        .getIndent() + FormatSettings.getBlockIndent()) + modifierString 
        + "int " + fakeField.getName() + " = " + fakeField.getExpression() +";";

    StringInserter newFieldInserter = new StringInserter(
        fakeField.getOwner().getBinCIType().getCompilationUnit(),
        fakeField.getOwner().getBinCIType().getBodyAST().getStartLine(),
        fakeField.getOwner().getBinCIType().getBodyAST().getStartColumn(),
        fieldDeclaration
    );
    editors.add(newFieldInserter);
  }
}
