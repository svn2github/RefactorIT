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
import net.sf.refactorit.source.edit.ModifierEditor;


/**
 *
 * @author Arseni Grigorjev
 */
public class NumLitFixConstantalize extends NumLitFix{
  private BinField field;
  
  public NumLitFixConstantalize(NumericLiteral violation, BinField field) {
    super(violation);
    this.field = field;
  }
    
  public BinField getField() {
    return field;
  }
  
  protected void createPreviewEditors() {
    int modifiers = field.getModifiers();
    modifiers = BinModifier.setFlags(modifiers, 
        BinModifier.FINAL | BinModifier.STATIC);
    editors.add(new ModifierEditor(field, modifiers));
  }
  
  protected void createOtherEditors() {
  }
  
}
