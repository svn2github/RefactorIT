/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.refactorings.createmissing;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.source.LocationlessSourceParsingException;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author  tanel
 */
public class CreateMethodContext {

  public static interface ChangeListener {
    void contextChanged();
  }


  private String methodName;
  public BinTypeRef initialBaseClass;
  private BinTypeRef baseClass;
  private String returnType;
  private int visibility;
  public int[] allowedVisibilities;
  public BinTypeRef[] argumentTypes;
  public String[] argumentNames;
  private boolean staticMethod;
  private BinTypeRef invokedIn;
  boolean selected = true;

  private List listeners = new LinkedList();

  public CreateMethodContext(final MethodNotFoundError error) {
    this.setMethodName(error.getMethodName());
    this.initialBaseClass = error.getOwner();
    BinTypeRef returnTypeRef = error.getReturnType();
    this.returnType = (returnTypeRef == null) ? ""
        : BinFormatter.formatNotQualified(returnTypeRef);
    this.staticMethod = error.isStaticMethod();
    this.argumentTypes = getArgumentTypes(error, 
        error.getInvokedIn().getProject().getObjectRef());
    this.invokedIn = error.getInvokedIn();
    this.baseClass = initialBaseClass;

    // TODO: set them correctly
    this.allowedVisibilities = new int[] {BinModifier.PRIVATE,
        BinModifier.PACKAGE_PRIVATE, BinModifier.PROTECTED, BinModifier.PUBLIC};
    this.visibility = BinModifier.PUBLIC;

    BinExpression[] params = error.getArguments().getExpressions();

    this.argumentNames = new String[argumentTypes.length];
    // FIXME must be refactored, probably into NameUtil? check also other places, IntroduceTemp...
    for (int i = 0; i < argumentTypes.length; i++) {
      if (params[i] instanceof BinFieldInvocationExpression) {
        this.argumentNames[i]
            = ((BinFieldInvocationExpression) params[i]).getField().getName();
      } else if (params[i] instanceof BinVariableUseExpression) {
        this.argumentNames[i]
            = ((BinVariableUseExpression) params[i]).getVariable().getName();
      } else {
        this.argumentNames[i] = "param" + i;
      }
    }
  }

  private BinTypeRef[] getArgumentTypes(final MethodNotFoundError error, final BinTypeRef objectRef) {
    BinExpression[] params = error.getArguments().getExpressions();
    
    BinTypeRef[] result = error.getArguments().getExpressionTypes();
    for(int i = 0; i < params.length; i++) {
      if((params[i] instanceof BinLiteralExpression) &&
          ((BinLiteralExpression)params[i]).isNull()) {
        result[i] = objectRef;
      } else if(result[i].getBinType().isAnonymous()) {
        result[i] = getSupertypeOfAnonymousClass(result[i], objectRef);
      }
    }

    return result;
  }

  private BinTypeRef getSupertypeOfAnonymousClass(final BinTypeRef anonymousClass, final BinTypeRef objectRef) {
    BinTypeRef superclass = anonymousClass.getSuperclass();
    BinTypeRef[] interfaces = anonymousClass.getInterfaces();
    boolean merelyImplementsAnInterface = interfaces.length == 1 && 
        superclass.equals(objectRef);
    
    if(merelyImplementsAnInterface) {
      return interfaces[0];
    } else {
      return superclass;
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  private void notifyListeners() {
    for (Iterator i = listeners.iterator(); i.hasNext(); ) {
      ChangeListener listener = (ChangeListener) i.next();
      listener.contextChanged();
    }
  }

  public boolean checkReturnType() {
    if (returnType.length() != 0) {
      try {
        String arraylessReturnType
            = BinArrayType.extractArrayTypeFromString(returnType).type;
        BinTypeRef ref = resolve(arraylessReturnType);
        if (ref != null) {
          return true;
        } else {
          return false;
        }
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * @param arraylessReturnType type name
   * @return type reference
   * @throws LocationlessSourceParsingException
   * @throws SourceParsingException
   */
  public BinTypeRef resolve(final String arraylessReturnType) throws
      LocationlessSourceParsingException, SourceParsingException {

    BinTypeRef ref = invokedIn.getProject().findPrimitiveTypeForName(
        arraylessReturnType);
    if (ref == null) {
      ref = invokedIn.getResolver().resolve(arraylessReturnType);
    }

    return ref;
  }

  public boolean isSelected() {
    return this.selected;
  }

  public void setSelected(final boolean selected) {
    this.selected = selected;
    notifyListeners();
  }

  public String getMethodName() {
    return this.methodName;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
    notifyListeners();
  }

  public BinTypeRef getBaseClass() {
    return this.baseClass;
  }

  public void setBaseClass(final BinTypeRef baseClass) {
    this.baseClass = baseClass;
    notifyListeners();
  }

  public String getReturnType() {
    return this.returnType;
  }

  public void setReturnType(final String returnType) {
    this.returnType = returnType;
    notifyListeners();
  }

  public int getVisibility() {
    return this.visibility;
  }

  public void setVisibility(final int visibility) {
    this.visibility = visibility;
    notifyListeners();
  }

  public BinTypeRef getInvokedIn() {
    return this.invokedIn;
  }

  public boolean isStaticMethod() {
    return this.staticMethod;
  }

  public void setStaticMethod(final boolean staticMethod) {
    this.staticMethod = staticMethod;
  }
}
