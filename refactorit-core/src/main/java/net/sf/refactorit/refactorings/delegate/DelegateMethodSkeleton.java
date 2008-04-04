/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;


/**
 *
 *
 * @author Tonis Vaga
 */
public class DelegateMethodSkeleton extends MethodSkeleton {
  public DelegateMethodSkeleton(BinField field, BinCIType owner,
      BinMethod method) {
    super(owner, method, null);

    String returnStr = "";
    if (method.getReturnType().getBinType() != BinPrimitiveType.VOID) {
      returnStr = "return ";
    }

    String startStr = returnStr + field.getName() + "." + method.getName()
        + "(";
    StringBuffer buffer = new StringBuffer(startStr);

    BinParameter[] parameters = super.getMethod().getParameters();

    for (int i = 0; i < parameters.length; i++) {
      buffer.append(parameters[i].getName());
      if ((i + 1) != parameters.length) {
        buffer.append(", ");
      }
    }
    buffer.append(");");

    MethodBodySkeleton bodyContext = new MethodBodySkeleton(buffer.toString());
    setBody(bodyContext);

    validateContext();
  }

  protected void validateContext() {
    BinModifierBuffer buff = new BinModifierBuffer(method.getModifiers());
//    int modifiers=buff.getPrivilegeFlags();

    // clear all other flags
    method.setModifiers(buff.getPrivilegeFlags());

    /**@todo Override this net.sf.refactorit.refactorings.delegate.MethodSkeleton method*/
    super.validateContext();
  }
}
