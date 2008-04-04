/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature.analyzer;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.CallTreeIndexer;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;

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

public class MethodsInvocationsMap {

  // Map (method_, list_) - 'list_'  of methods which call 'method_'
  // NB! 'list_' contains CallTreeIndexer.Invocation
  private MultiValueMap methodInvocationsMap;

  // Map (method_, list_) - 'list_'  of methods are calles by 'method_'
  private MultiValueMap methodsCalledByThisMap = new MultiValueMap();

  private MultiValueMap cache = new MultiValueMap();

  public MethodsInvocationsMap(Project project) {
    CallTreeIndexer callTreeIndexer = new CallTreeIndexer();

    methodInvocationsMap = callTreeIndexer.getInvocationsNet(project);
//    methodsCalledByThisMap = reverseAtoB(methodInvocationsMap);
  }

  /*
   * @param method - method which is called by others Methods
   * @return List - list of BinMethod
   */
  public List findAllMethodsWhichCallThis(BinMethod method) {
    // findMethodsCalledByThis contains list  of invocations of this method
    // it is needed to unbox methods from invocations

    Object o = cache.get(method);
    if (o == null) {
      List invocations = methodInvocationsMap.get(method);

      for (int i = 0; invocations != null && i < invocations.size(); i++) {
        cache.putNew(method,
            ((CallTreeIndexer.Invocation) invocations.get(i)).getWhere());
      }
      o = cache.get(method);
    }

    return (List) o;
  }

  public List findAllMethodsAreCalledByThis(BinMethod method) {

    if (methodsCalledByThisMap.get(method) == null) {
      return findMethodsAreCalledByThis(method);
    }
    return methodsCalledByThisMap.get(method);
  }

//  private MultiValueMap reverseAtoB(MultiValueMap methodsInvMap) {
//    MultiValueMap map = new MultiValueMap();
//
//    for (Iterator i = methodsInvMap.keySet().iterator(); i.hasNext(); ) {
//      Object o = i.next();
//
//      if (o instanceof BinMethod && ((BinMethod) o).getCompilationUnit() != null) {
//        List methods = methodsInvMap.get(o);
//
//        for (int j = 0; j < methods.size(); j++) {
//          CallTreeIndexer.Invocation inv = (CallTreeIndexer.Invocation) methods.
//              get(j);
//
//          map.put(inv.getWhere(), o);
//        }
//      }
//    }
//    return map;
//  }

  private List findMethodsAreCalledByThis(BinMethod method) {
    ManagingIndexer supervisor = new ManagingIndexer(true);
    new DependenciesIndexer(supervisor,
        method);
//    dependencyIndexer.leave(method);
    method.accept(supervisor);
//    supervisor.visit(method);
    List invoc = supervisor.getInvocations();

    for (int i = 0; i < invoc.size(); i++) {
      BinItem item = ((InvocationData) invoc.get(i)).getWhat();
      if (item instanceof BinMethod) {
        if (((BinMethod) item).getOwner().getCompilationUnit() != null) {
          methodsCalledByThisMap.put(method, item);
        }
      }
    }

    return methodsCalledByThisMap.get(method);
  }
}
