/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature.analyzer;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.refactorings.changesignature.ExistingParameterInfo;
import net.sf.refactorit.refactorings.changesignature.MethodSignatureChange;
import net.sf.refactorit.refactorings.changesignature.NewParameterInfo;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Aqris AS</p>
 * @author Kirill Buhhalko
 * @version
 */

public class RecursiveParameterAddingAnalyzer {

  private MethodSignatureChange change;
  private ChangesContainer container = new ChangesContainer();

  private HashMap alreadyAdded = new HashMap();

  public RecursiveParameterAddingAnalyzer(MethodSignatureChange change) {
    this.change = change;
    NewParameterInfo[] newParamInfo = getNewParameterInfos();
    ExistingParameterInfo[] paramsToDelete = getParamsToDelete();
    int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][] a;


    addAlreadyTransformatedMetdhods(newParamInfo);

    HashMap selectedMethodsAddParamMap =
        getSelectedMethodsAddParamMap(newParamInfo);

    HashMap nameMapForEachParamInfo =
        getNameMapForEachParamInfo(newParamInfo, selectedMethodsAddParamMap);

    for (int i = 0; i < newParamInfo.length; i++) {
      newParamInfo[i].setParameterNamesMap((HashMap) nameMapForEachParamInfo.
          get(newParamInfo[i]));
    }

    for (int i = 0; i < newParamInfo.length; i++) {
      BinMethod[] methods = (BinMethod[]) selectedMethodsAddParamMap.get(newParamInfo[i]);

      for (int j = 0; j < methods.length; j++) {
        if (!((HashSet) alreadyAdded.get(newParamInfo[i])).contains(methods[j])) {
          ChangeMethodSignatureRefactoring ref = container.
              getMethSignChangeForCurrentMethod(methods[j]);

          MethodSignatureChange currentChange = ref.getChange();

          int index = 0;
          index = currentChange.getParametersList().size();
          BinTypeRef type = newParamInfo[i].getType();
          String defaultValue = newParamInfo[i].getDefaultValue();
          NewParameterInfo newParameter = new NewParameterInfo(type,
              defaultValue, index);

          HashMap names = (HashMap) nameMapForEachParamInfo.get(newParamInfo[i]);
          newParameter.setParameterNamesMap(names);

          newParameter.setName(names.get(methods[j]).toString());

          currentChange.addParameter(newParameter, index);

          ((HashSet) alreadyAdded.get(newParamInfo[i])).add(methods[j]);
          ((HashSet) alreadyAdded.get(newParamInfo[i])).addAll(methods[j].
              findAllOverridesOverriddenInHierarchy());
        }
      }
    }
    HashSet added = new HashSet();

    for(int i=0; i < paramsToDelete.length; i++) {
      ArrayList[] toDelete;
      RecursiveDeleteParameterModel deleteModel = paramsToDelete[i].getDeleteModel();
      MethodsParDeleteCallsNode methodsCallsNode2 = (MethodsParDeleteCallsNode) deleteModel.getRoot();
      toDelete = extractAllMethodsDelete(methodsCallsNode2);
      added.add(paramsToDelete[i].getOriginalParameter());

      for(int x=0; x < toDelete[0].size(); x++) {
        BinMethod method = (BinMethod) toDelete[0].get(x);
        BinParameter parameter = (BinParameter) toDelete[1].get(x);
        if (!added.contains(parameter)) {
          ChangeMethodSignatureRefactoring ref = container.
              getMethSignChangeForCurrentMethod(method);
         MethodSignatureChange ch = ref.getChange();
         ch.deleteParameter(parameter.getIndex());
         added.add(parameter);
        }
      }
    }

  }

  private HashMap getNameMapForEachParamInfo(NewParameterInfo[] newParamInfo,
      HashMap selectedMethodsMap) {
    HashMap result = new HashMap();

    //add all selected methods
    for (int i = 0; i < newParamInfo.length; i++) {
      BinMethod[] binMethods = (BinMethod[]) selectedMethodsMap.get(
          newParamInfo[i]);

      result.put(newParamInfo[i], findName(newParamInfo[i], binMethods));
    }

    return result;
  }

  private HashMap getSelectedMethodsAddParamMap(NewParameterInfo[] newParamInfo) {
    HashMap methodsMap = new HashMap();
    for (int i = 0; i < newParamInfo.length; i++) {
      BinMethod[] methods = extractAllMethods((MethodsCallsNode) newParamInfo[i].
          getRecursiveAddParameterModel().getRoot());
      methodsMap.put(newParamInfo[i], methods);

    }
    return methodsMap;
  }

  private NewParameterInfo[] getNewParameterInfos() {
    List allParams = change.getParametersList();
    List newParams = new ArrayList(allParams.size());
    for (int i = 0; i < allParams.size(); i++) {
      Object currParam = allParams.get(i);

      if (currParam instanceof NewParameterInfo) {
        if (((NewParameterInfo) currParam).
            getRecursiveAddParameterModel() != null) {
          newParams.add(currParam);
        }
      }
    }

    return (NewParameterInfo[]) newParams.toArray(new NewParameterInfo[
        newParams.size()]);
  }

  private void addAlreadyTransformatedMetdhods(NewParameterInfo[] paramInfo) {
    for (int i = 0; i < paramInfo.length; i++) {
      HashSet alrAdded = new HashSet();
      alrAdded.add(change.getMethod());
      alrAdded.addAll(change.getOverridesOverridenHierarchy());

      alreadyAdded.put(paramInfo[i], alrAdded);
    }
  }

  private HashMap findName(NewParameterInfo paramInfo, BinMethod[] methods) {
    HashMap hashMap = new HashMap();
    String suitableName = paramInfo.getName();

    for (int i = 0; i < methods.length; i++) {
      hashMap.put(methods[i], findNameForMethod(methods[i], suitableName));
    }

    ArrayList list = new ArrayList();
    list.addAll(change.getOverridesOverridenHierarchy());
    list.add(change.getMethod());

    for (int i = 0; i < list.size(); i++) {
      hashMap.put(list.get(i), suitableName);
    }

    return hashMap;
  }

  private String findNameForMethod(BinMethod method, String name) {
    String n = name;
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      if (isSuitableNameForNewLocVar(n, method)) {
        return n;
      } else {
        n = name + i;
      }
    }
    return n;
  }

  private boolean isSuitableNameForNewLocVar(String name, BinMethod method) {

    if (hasLocalVariablesWithSameName(name, method)) {
      return false;
    }

    if (hasFieldsWithSameName(name, method)) {
      return false;
    }

    return true;
  }

  private boolean hasLocalVariablesWithSameName(String name, BinMethod method) {
    LocalVariableDuplicatesFinder finder = new LocalVariableDuplicatesFinder(null,
        name, method);
    if (finder.getDuplicates().size() > 0) {
      return true;
    } else {
      return false;
    }
  }

  private boolean hasFieldsWithSameName(String name, BinMethod method) {
    BinCIType type = method.getOwner().getBinCIType();
    List fields = type.getAccessibleFields(type);

    for (int i = 0; i < fields.size(); i++) {
      if (name.equals(((BinField) fields.get(i)).getName())) {
        return true;
      }
    }
    return false;
  }

  private BinMethod[] extractAllMethods(MethodsCallsNode modelRoot) {
    HashSet set = new HashSet();
    extractModelToMethods(modelRoot, set);
    ArrayList list = new ArrayList(set);
    BinMethod[] methods = (BinMethod[]) list.toArray(new BinMethod[list.size()]);

    return methods;
  }

  private ArrayList[] extractAllMethodsDelete(MethodsParDeleteCallsNode modelRoot) {
    ArrayList[] list = new ArrayList[2];
    list[0] = new ArrayList();
    list[1] = new ArrayList();
    extractModelToMethodsDelete(modelRoot, list);
    return list;
  }

  private void extractModelToMethodsDelete(MethodsParDeleteCallsNode node, ArrayList[] list) {
    if (node.isSelected()) {
      if (node.getBin() instanceof BinMethod) {
        list[0].add(node.getBin());
        list[1].add(node.getBinParameter());
      }

      ArrayList childs = node.getChildren();
      for (int i = 0; i < childs.size(); i++) {
        extractModelToMethodsDelete((MethodsParDeleteCallsNode) childs.get(i), list);
      }
    }
  }

  private void extractModelToMethods(MethodsCallsNode node, HashSet set) {
    if (node.isSelected()) {
      if (node.getBin() instanceof BinMethod) {
        set.add(node.getBin());
      }

      ArrayList childs = node.getChildren();
      for (int i = 0; i < childs.size(); i++) {
        extractModelToMethods((MethodsCallsNode) childs.get(i), set);
      }
    }
  }

  public TransformationList getTransList() {
    TransformationList transList = new TransformationList();
    ChangeMethodSignatureRefactoring[] refs = container.getAllRefactorings();

    for (int i = 0; i < refs.length; i++) {
      transList.merge(refs[i].performChange());
    }

    return transList;
  }


  private ExistingParameterInfo[] getParamsToDelete() {
    ArrayList result = new ArrayList();
    List params = change.getDeletedParameters();

    for(int i=0; i < params.size(); i++) {
      if(((ExistingParameterInfo)params.get(i)).getDeleteModel() != null) {
        result.add(params.get(i));
      }
    }

    return (ExistingParameterInfo[]) result.toArray(new ExistingParameterInfo[
        result.size()]);
  }
}
