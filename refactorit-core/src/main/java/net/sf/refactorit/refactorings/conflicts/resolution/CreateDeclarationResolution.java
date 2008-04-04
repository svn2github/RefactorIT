/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.conflicts.resolution;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.InitializerEditor;
import net.sf.refactorit.source.edit.ModifierEditor;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.source.format.BinTypeFormatter;
import net.sf.refactorit.source.format.FormatSettings;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author vadim
 */
public class CreateDeclarationResolution extends ConflictResolution {
  private BinMember upMember;
  private int declarationAccess;

  public CreateDeclarationResolution(BinMember upMember) {
    this(upMember, BinModifier.PUBLIC);
  }

  public CreateDeclarationResolution(BinMember upMember, int declarationAccess) {
    this.upMember = upMember;
    this.declarationAccess = declarationAccess;
  }

  public Editor[] getEditors(ConflictResolver resolver) {
    if (!isResolved()) {
      return new Editor[0];
    }

    Editor[] editor;

    BinCIType targetType = resolver.getTargetType();
    BinCIType nativeType = upMember.getOwner().getBinCIType();

    if (upMember instanceof BinMethod) {

      String indent =
          FormatSettings.getIndentString(new BinTypeFormatter(targetType).
          getMemberIndent());
      boolean isIntoAbstractClass = ((targetType instanceof BinClass) &&
          targetType.isAbstract());
      editor = new Editor[1];
      editor[0] = new StringInserter(targetType.getCompilationUnit(),
          targetType.findNewMethodPosition(),
          (composeMethodSignature((BinMethod) upMember,
          indent,
          isIntoAbstractClass) + ";" +
          FormatSettings.LINEBREAK));
    } else if (upMember instanceof BinField) {
//      int newModifier = BinModifier.clearFlags(upMember.getModifiers(),
//                                               BinModifier.PRIVILEGE_MASK |
//                                               BinModifier.
//                                               STATIC_TO_NATIVE_MASK/*1023*/);

      BinModifierBuffer modifier = new BinModifierBuffer(upMember.getModifiers());

      modifier.clearFlags(BinModifier.PRIVILEGE_MASK |
          BinModifier.
          STATIC_TO_INTERFACE_MASK);

      if (nativeType.isInterface()) {
        modifier.setFlag(BinModifier.STATIC | BinModifier.FINAL);
        if (!targetType.isInterface()) {
          modifier.setFlag(BinModifier.PUBLIC);
        }
      }

      int newModifier = modifier.getModifiers();

      if (((BinField) upMember).hasExpression()) {
        editor = new Editor[1];
        editor[0] = new ModifierEditor(upMember,
            newModifier);
      } else {
        editor = new Editor[2];
        editor[0] = new InitializerEditor((BinField) upMember);
        editor[1] = new ModifierEditor(upMember,
            newModifier);
      }
    } else {
      throw new RuntimeException("upMember is neither BinMember nor BinField");
    }

    return editor;
  }

  // FIXME duplicates BinMethodFormatter.formHeader() !!!
  protected String composeMethodSignature(BinMethod method, String indent,
      boolean isIntoAbstractClass) {
    StringBuffer result = new StringBuffer(indent);

    if (isIntoAbstractClass) {
      String accessString = new BinModifierFormatter(declarationAccess).print();
      if (accessString.length() > 0) {
        result.append(accessString + " abstract ");
      } else {
        result.append("abstract ");
      }
    }

    result.append(method.getReturnType().getName() + " ");
    result.append(method.getName() + "(");

    BinParameter[] params = method.getParameters();
    for (int i = 0; i < params.length; i++) {
      result.append(params[i].getTypeRef().getName() + " ");
      result.append(params[i].getName());

      if ((params.length - i) > 1) {
        result.append(", ");
      }
    }

    result.append(")");

    BinMethod.Throws[] allThrows = method.getThrows();
    for (int i = 0; i < allThrows.length; i++) {
      if (i == 0) {
        result.append(" throws ");
      } else {
        result.append(", ");
      }
      result.append(allThrows[i].getException().getName());
    }

    return result.toString();
  }

  public int getDeclarationAccess() {
    return declarationAccess;
  }

  public String getDescription() {
    return "Create declaration of " + BinFormatter.format(upMember) +
        " in target type";
  }

  public void runResolution(ConflictResolver resolver) {
    setIsResolved(true);
  }

  public String toString() {
    return "CreateDeclarationResolution";
  }

  public List getDownMembers() {
    return new ArrayList();
  }

}
