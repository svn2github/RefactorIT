/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.loader.MethodBodyLoader;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.format.BinFormatter;


/**
 * @author Tanel
 * @author Anton Safonov
 */
public final class MethodNotFoundError extends UserFriendlyError {

  private final BinTypeRef owner;
  private final BinTypeRef returnType;
  private final String methodName;
  private final BinExpressionList arguments;
  private boolean staticMethod;
  private final BinTypeRef invokedIn;

  public MethodNotFoundError(BinTypeRef owner, BinTypeRef returnType,
      String methodName, BinExpressionList arguments,
      BodyContext bodyContext, ASTImpl ast) {
    super("FIXME", bodyContext.getCompilationUnit(), ast);
    this.owner = owner;
    this.returnType = returnType;
    this.methodName = methodName;
    this.arguments = arguments;
    invokedIn = bodyContext.getTypeRef();
    this.staticMethod = checkForStatic(bodyContext);
  }

  public String getDescription() {
    String returnDesc = (returnType == null)
        ? "<unknown>"
        : BinFormatter.formatQualified(returnType);
    String params = "";
    if (arguments == null) {
      params = "?";
    } else if (arguments.getExpressions().length > 0) {
      if (arguments.getExpressions().length < 5) {
        params = MethodBodyLoader.displayableListOfReturnTypes(arguments);
      } else {
        params = "..";
      }
    }
    String context = "";
    if (!owner.getTypeRef().equals(invokedIn)) {
      context = " in context: " + invokedIn.getQualifiedName();
    }
    return "Method not found: " + returnDesc + " " + owner.getQualifiedName()
        + "." + methodName + "(" + params + ")" + context;
  }

  public String toString() {
    return "MethodNotFoundError: " + this.getDescription();
  }

  public BinTypeRef getOwner() {
    return this.owner;
  }

  public BinTypeRef getReturnType() {
    return this.returnType;
  }

  public String getMethodName() {
    return this.methodName;
  }

  public BinExpressionList getArguments() {
    return this.arguments;
  }

  public boolean isStaticMethod() {
    return this.staticMethod;
  }

  public BinTypeRef getInvokedIn() {
    return this.invokedIn;
  }

  private boolean checkForStatic(BodyContext bodyContext) {
    BinMember member = bodyContext.getBlock();
    if (member != null) {
      return member.isStatic();
    }

    return false;
  }
}
