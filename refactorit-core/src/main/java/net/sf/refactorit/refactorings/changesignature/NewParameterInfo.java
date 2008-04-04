/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.changesignature.analyzer.RecursiveAddParameterModel;

import java.util.HashMap;


/**
 *
 *
 * @author Tonis Vaga
 */
public class NewParameterInfo extends ParameterInfo {
  private String defaultValue;
  private HashMap parameterNamesMap = null;

  private RecursiveAddParameterModel recursiveAddParameterModel = null;

  public NewParameterInfo(BinTypeRef type, String name, int index) {
    super(type, name, index);

    defaultValue = type.getBinType().getDefaultValue();
  }

  public NewParameterInfo(
      BinTypeRef type, String name, String defaultValue, int index
      ) {
    super(type, name, index);

    this.defaultValue = defaultValue;
  }

  public String getDefaultValue(BinMember invokedIn ) {
    if(parameterNamesMap != null) {
      Object suitableDefaultValue = parameterNamesMap.get(invokedIn);
      if(suitableDefaultValue != null) {
        return (String) suitableDefaultValue;
      }
    }

    return defaultValue;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public boolean checkDefaultValue() {
    if (defaultValue == null) {
      return false;
    }

    BinTypeRef ref = getType();
    if (ref.isPrimitiveType()) {
      BinPrimitiveType type = (BinPrimitiveType) ref.getBinType();
      if (!type.canConvertFromString(defaultValue)) {
        return false;
      }
    }

    return true;
  }

  public void setParameterNamesMap(final HashMap parameterNamesMap) {
    this.parameterNamesMap = parameterNamesMap;
  }

  public RecursiveAddParameterModel getRecursiveAddParameterModel() {
    return this.recursiveAddParameterModel;
  }

  public void setRecursiveAddParameterModel(
      final RecursiveAddParameterModel recursiveAddParameterModel) {
    this.recursiveAddParameterModel = recursiveAddParameterModel;
  }
}
