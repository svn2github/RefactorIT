/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author  Arseni Grigorjev
 */
public final class GenericsUtil {
  
  private GenericsUtil(){}
  
  /**
   * @param method some method
   * @param typeParameter some type parameter
   *
   * @return true, if method`s return type uses given type parameter
   */
  public static boolean methodReturnTypeDependsOnTypeParameter(final BinMethod method,
      final BinTypeRef typeParameter){
    if (typeParameter.getBinCIType().isTypeParameter()
        && typeParameter.getBinCIType().getParentMember() instanceof BinMethod){
      return methodReturnTypeDependsOnMethodTypeParameter(method,
          typeParameter);
    }
    return memberReturnTypeDependsOnTypeParameter(method.getReturnType(),
        typeParameter);
  }
  
  /**
   * @param field some field
   * @param typeParameter some type parameter
   *
   * @return true, if field`s type uses given type parameter
   */
  public static boolean fieldTypeDependsOnTypeParameter(final BinField field,
      final BinTypeRef typeParameter){
    return memberReturnTypeDependsOnTypeParameter(field.getTypeRef(),
        typeParameter);
  }
  
  private static boolean methodReturnTypeDependsOnMethodTypeParameter(
      final BinMethod method,
      final BinTypeRef typeParameter){
    final List typesToCheck = new ArrayList(10);
    extractTypeParameterRefsFromType(method.getReturnType(), typesToCheck);
    for (final Iterator it = typesToCheck.iterator(); it.hasNext(); ){
      final BinTypeRef currentType = (BinTypeRef) it.next();
      if (currentType.equals(typeParameter)){
        return true;
      }
    }
    return false;
  }
  
  private static boolean memberReturnTypeDependsOnTypeParameter(
      final BinTypeRef returnType, final BinTypeRef typeParameter){
    final List typesToCheck = new ArrayList(10);
    extractTypeParameterRefsFromType(returnType, typesToCheck);
    for (final Iterator it = typesToCheck.iterator(); it.hasNext(); ){
      final BinTypeRef currentType = (BinTypeRef) it.next();
      if (currentType.getBinCIType().getParentMember() instanceof BinMethod){

      } else {
        final List path = new ArrayList(5);
        findInheritancePath(currentType.getBinCIType().getOwner(),
            typeParameter.getBinCIType().getOwner(), path, new HashSet(5));
        if (path.size() == 0){
          return false;
        } else if (analyzeInheritancePath(1, typeParameter, currentType, path)){
          return true;
        }
      }
    }
    return false;
  }
  
  private static boolean analyzeInheritancePath(final int level,
      final BinTypeRef currentTypeParameter, final BinTypeRef targetTypeParameter,
      final List path){
    if (currentTypeParameter.equals(targetTypeParameter)){
      return true;
    }
    
    if (level >= path.size()){
      return false;
    }

    final BinTypeRef currentInheritanceElement = (BinTypeRef) path.get(level);
    final BinTypeRef[] typeArguments = currentInheritanceElement.getTypeArguments();
    for (int i = 0; typeArguments != null && i < typeArguments.length; i++){
      if (typeArgumentsDependOnTypeParameter(typeArguments[i],
          currentTypeParameter)){
        if (analyzeInheritancePath(level+1, ((BinSpecificTypeRef) typeArguments[i])
            .getCorrespondingTypeParameter(), targetTypeParameter, path)){
          return true;
        }
      } else {
      }
    }

    return false;
  }
  
  /**
   * @param typeArg some type argument
   * @param typeParam some type parameter
   *
   * @return true, if typeArg is typeParam, or if any of typeArg type arguments
   *    is typeParam
   */
  public static boolean typeArgumentsDependOnTypeParameter(final BinTypeRef typeArg,
      final BinTypeRef typeParam){
    if (typeArg.equals(typeParam)){
      return true;
    }
        
    final BinTypeRef[] typeArguments = typeArg.getTypeArguments();
    for (int i = 0; typeArguments != null && i < typeArguments.length; i++){
      if (typeArguments[i].equals(typeParam)
          || typeArgumentsDependOnTypeParameter(typeArguments[i], typeParam)){
        return true;
      }
    }
    return false;
  }
  
  /**
   * @param superClass the class which is higher in hierarchy
   * @param subClass the class which is lower in hierarchy
   * @param result contains inheritance sequence of BinTypeRef`s
   * @param visited a set of visited types, pass it 'new HashSet(5)' or smth.
   */
  public static boolean findInheritancePath(final BinTypeRef superClass,
      final BinTypeRef subClass, final List result, final Set visited){
    result.add(subClass);
    if (subClass == superClass || subClass.equals(superClass)){
      return true;
    }

    if (visited.contains(subClass)){
      return false;
    }
    visited.add(subClass);

    final BinTypeRef[] subClassSupertypes = subClass.getSupertypes();
    for (int i = 0, max_i = subClassSupertypes.length; i < max_i; i++){
      final BinTypeRef currentSuperType = subClassSupertypes[i];
      if (findInheritancePath(superClass, currentSuperType, result, visited)){
        return true;
      }
    }
    result.remove(result.size() - 1);
    return false;
  }
  
  /**
   * Searches the type and all its type arguments recursively for type
   *  parameters and puts results to the results list
   *
   * @param type some return type
   * @param result results container
   */
  private static void extractTypeParameterRefsFromType(final BinTypeRef type,
      final List result){
    if (type.getBinCIType().isTypeParameter()){
      result.add(type);
    } else {
      final BinTypeRef[] typeArguments = type.getTypeArguments();
      for (int i = 0; typeArguments != null && i < typeArguments.length; i++){
        extractTypeParameterRefsFromType(typeArguments[i], result);
      }
    }
  }
}
