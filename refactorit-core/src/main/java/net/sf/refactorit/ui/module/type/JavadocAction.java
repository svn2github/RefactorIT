/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.type;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.ui.javadoc.TypeInfoJavadoc;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


public class JavadocAction extends AbstractRefactorItAction {
  public static final String NAME = "Quick JavaDoc";
  public static final String KEY = "refactorit.action.JavadocAction";

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  /**
   * Module execution.
   *
   * @param context of refactoring, has Project inside
   * @param owner  any visible component on the screen
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(RefactorItContext context, Object object) {
//    context.getProject().checkForDuplicates();

    BinMember target = null;

    if (object instanceof BinMemberInvocationExpression) {
      target = ((BinMemberInvocationExpression) object).getMember();
    } else if (object instanceof BinMethod.Throws) {
      target = ((BinMethod.Throws) object).getException().getBinCIType();
    } else if (object instanceof BinThrowStatement) {
      target = ((BinThrowStatement) object).getExpression().getReturnType().
          getBinType();
    } else if (object instanceof BinParameter
        && !(object instanceof BinCatchParameter)) {
      target = ((BinParameter) object).getParentMember();
    } else if (object instanceof BinLocalVariable) {
      // show types of locals, since they couldn't have JavaDocs
      target = ((BinLocalVariable) object).getTypeRef().getBinType();
    } else if (object instanceof BinMember) {
      target = (BinMember) object;
    }

    if (target == null || target instanceof BinPrimitiveType) {
      return false;
    }

    TypeInfoJavadoc infoJavaDoc = new TypeInfoJavadoc(context, target);
    infoJavaDoc.showJavadocAction();

    // we never change anything
    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
//    if (BinCIType.class.isAssignableFrom(cl)) {
//      // sometimes shows JSP implementation classes but still allow
//      return true; //false;
//    } else
    if (BinItem.class.isAssignableFrom(cl)) {
      return true;
    }
    return false;
  }
  public boolean isAvailableForType(Class type) {
    return BinCIType.class.isAssignableFrom(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        || BinVariable.class.isAssignableFrom(type)
        || BinMemberInvocationExpression.class.isAssignableFrom(type);
  }
}
