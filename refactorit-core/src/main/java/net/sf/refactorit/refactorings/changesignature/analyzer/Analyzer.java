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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.refactorings.changesignature.MethodSignatureChange;
import net.sf.refactorit.refactorings.changesignature.NewParameterInfo;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Analyzer {
  BinMethod method2;
  private TransformationList transList;
  private String[] suitableNames;
  private NewParameterInfo[] paramInfo;
  private HashMap mapOfNames;

  public Analyzer(ChangeMethodSignatureRefactoring ref) {

    MethodSignatureChange change = ref.getChange();
    this.method2 = change.getMethod();

    alreadyAdded.add(method2);
    alreadyAdded.addAll(change.getOverridesOverridenHierarchy());

    List allParams = change.getParametersList();
    List newParams = new ArrayList(allParams.size());
    for (int i = 0; i < allParams.size(); i++) {
      Object currParam = allParams.get(i);

      if (currParam instanceof NewParameterInfo) {
        newParams.add(currParam);
      }
    }

    paramInfo = (NewParameterInfo[]) newParams.toArray(
        new NewParameterInfo[newParams.size()]);

    suitableNames = new String[paramInfo.length];

    for (int i = 0; i < paramInfo.length; i++) {
      suitableNames[i] = paramInfo[i].getName();
    }

    BinTreeTableModel model = getModel(ref);
    model = DialogManager
        .getInstance().showConfirmations("Clean Imports", ref.getContext(),
        model,
        "Select places to remove imports", "refact.clean_imports");

    if (model != null) {
      BinMethod[] methods = extractAllMethods((MethodsCallsNode) model.getRoot());
      mapOfNames = checkForSuitableNames(suitableNames, methods);

      for (int i = 0; i < paramInfo.length; i++) {

        HashMap map_ = new HashMap();
        map_.put(method2, suitableNames[i]);
        for (int z = 0; z < change.getOverridesOverridenHierarchy().size(); z++) {
          map_.put(change.getOverridesOverridenHierarchy().get(z),
              suitableNames[i]);
        }

        ((HashMap) mapOfNames.get(suitableNames[i])).putAll(map_);
        paramInfo[i].setParameterNamesMap((HashMap) mapOfNames.get(
            suitableNames[i]));
      }
    }
    transList = new TransformationList();

    tryToProcessMethod_down(method2, model);

  }

  private void tryToProcessMethod_down(BinMethod method0,
      BinTreeTableModel model) {
    List childrens = ((ParentTreeTableNode) model.getRoot()).getAllChildren();
    MethodsCallsNode startNode = null;
    MethodsCallsNode childNode = null;
    for (int i = 0; i < childrens.size(); i++) {
      if (((ParentTreeTableNode) childrens.get(i)).getBin().equals(method0)) {
        startNode = (MethodsCallsNode) childrens.get(i);
      }
    }

    childNode = (MethodsCallsNode) startNode.getChildAt(1); // get the first node, with methods which was called.

    doit(childNode);

    childNode = (MethodsCallsNode) startNode.getChildAt(0);

    doit(childNode);

    return;
  }

  HashSet alreadyAdded = new HashSet();

  private void doit(MethodsCallsNode childNode) {
    if (childNode.isSelected()) {
      List allChildren = childNode.getAllChildren();

      for (int i = 0; i < allChildren.size(); i++) {
        doit((MethodsCallsNode) allChildren.get(i));
      }

      Object oMethod = ((MethodsCallsNode) childNode).getBin();

      if (!alreadyAdded.contains(oMethod) && oMethod instanceof BinMethod) {
        BinMethod binMethod = (BinMethod) oMethod;

        ChangeMethodSignatureRefactoring ref = new
            ChangeMethodSignatureRefactoring(binMethod);
        MethodSignatureChange change = ref.createSingatureChange();

        int index = 0;
        for (int j = 0; j < paramInfo.length; j++) {
          index = change.getParametersList().size();
          BinTypeRef type = paramInfo[j].getType();
          String defaultValue = paramInfo[j].getDefaultValue();
          NewParameterInfo param = new NewParameterInfo(type,
              defaultValue, index);
          HashMap thisParamNameMapForAllMethods = (HashMap) mapOfNames.get(
              suitableNames[j]);
          param.setParameterNamesMap(thisParamNameMapForAllMethods);
          param.setName(thisParamNameMapForAllMethods.get(binMethod).toString());

          change.addParameter(param, index);
        }

        ref.setChange(change);

        transList.merge(ref.performChange());
        alreadyAdded.add(binMethod);
        alreadyAdded.addAll(change.getOverridesOverridenHierarchy());

      }
    }
  }

  private HashMap checkForSuitableNames(String[] names, BinMethod[] methods) {
    HashMap result = new HashMap();
    for (int i = 0; i < names.length; i++) {
      HashMap curMethodNames = findName(methods, names[i]);
      result.put(names[i], curMethodNames);
    }
    return result;
  }

  private HashMap findName(BinMethod[] methods, String suitableName) {
    HashMap hashMap = new HashMap();

    for (int i = 0; i < methods.length; i++) {
      hashMap.put(methods[i], findNameForMethod(methods[i], suitableName));
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

  private BinMethod[] extractAllMethods(MethodsCallsNode node) {
    HashSet set = new HashSet();
    extractModelToMethods(node, set);
    ArrayList list = new ArrayList(set);
    BinMethod[] methods = (BinMethod[]) list.toArray(new BinMethod[list.size()]);

    return methods;
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

  private BinTreeTableModel getModel(ChangeMethodSignatureRefactoring ref) {
    return new RecursiveAddParameterModel(ref,
        new MethodsInvocationsMap(ref.getProject()));
  }

  public TransformationList getTransList() {
    return this.transList;
  }

  public HashSet getAlradyAdded() {
    return this.alreadyAdded;
  }
}
