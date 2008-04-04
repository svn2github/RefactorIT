/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;


import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.common.util.AdaptiveMultiValueMap;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.parser.ASTImpl;

import java.util.Iterator;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class CallTreeIndexer extends AbstractIndexer {
  private MultiValueMap invocationsNet = new MultiValueMap();
  private AdaptiveMultiValueMap dependentMethods = new AdaptiveMultiValueMap();

  public class Invocation {
    public Invocation(final Object where, final ASTImpl firstOccurence) {
      this.where = where;
      this.firstOccurence = firstOccurence;
    }

    public final void increaseUsage() {
      ++this.usages;
    }

    public Object getWhere() {
      return this.where;
    }

    public ASTImpl getFirstOccurence() {
      return this.firstOccurence;
    }

    public int getUsages() {
      return this.usages;
    }

    /** Thus Invocation pretends to be a simple location when searched through
     * the map or list */
    public int hashCode() {
      return this.where.hashCode();
    }

    public boolean equals(final Object other) {
      if (other instanceof Invocation) {
        return this.where.equals(((Invocation) other).where);
      } else {
        return this.where.equals(other);
      }
    }

    private final Object where;
    private final ASTImpl firstOccurence;
    private int usages = 1; // at least one usage was when creating this instance
  }


  public CallTreeIndexer() {
  }

  public MultiValueMap getInvocationsNet(Project project) {
//    long start = System.currentTimeMillis();

    fillUpDependencyMap(project);

    this.visit(project);

    this.dependentMethods.clear();

//System.err.println("CallTree took: " + (System.currentTimeMillis() - start) + "ms");

    return this.invocationsNet;
  }
  
  public MultiValueMap getInvocationsNet() {
      return this.invocationsNet;
  }

  private void fillUpDependencyMap(Project project) {
    project.discoverAllUsedTypes(); // we are especially interested in anonymous here

    final List allTypes = project.getDefinedTypes();
    for (int iType = 0; iType < allTypes.size(); iType++) {
      final BinTypeRef type = (BinTypeRef) allTypes.get(iType);

      final BinMethod[] aMethods = type.getBinCIType().getDeclaredMethods();
      for (int iMethod = 0; iMethod < aMethods.length; iMethod++) {
        final BinMethod method = aMethods[iMethod];
//System.err.println("Methods: "+method.getQualifiedName());
        final List subs = method.getOwner().getBinCIType().getSubMethods(method);

        for (int i = 0, max = subs.size(); i < max; i++) {
          this.dependentMethods.put(method, subs.get(i));
        }

        addAllOverridenFromClasspath(method, method);

        addAllOverridenInterfaces(method, method);
      }
    }
  }

  /** Source path deps will handle themselves, since we are filling up both ends.
   * But classpath methods we don't fill up, so we need to add super methods
   * explicitly.
   */
  private void addAllOverridenFromClasspath(BinMethod curMethod,
      BinMethod forMethod) {
    if (forMethod != curMethod
        && !curMethod.getOwner().getBinCIType().isFromCompilationUnit()) {
      this.dependentMethods.put(curMethod, forMethod);
    }
    List overriden = curMethod.findOverrides();
    if (overriden != null) {
      for (int i = 0, max = overriden.size(); i < max; i++) {
        addAllOverridenFromClasspath((BinMethod) overriden.get(i), forMethod);
      }
    }
  }

  private void addAllOverridenInterfaces(BinMethod curMethod,
      BinMethod forMethod) {
    if (forMethod != curMethod
        && ( /*curMethod.isAbstract()
                 ||*/curMethod.getOwner().getBinCIType().isInterface())) {
      this.dependentMethods.put(forMethod, curMethod);
    }
    List overriden = curMethod.findOverrides();
    if (overriden != null) {
      for (int i = 0, max = overriden.size(); i < max; i++) {
        addAllOverridenInterfaces((BinMethod) overriden.get(i), forMethod);
      }
    }
  }

  protected void addSingleInvocation(final Object where, final Object what,
      final ASTImpl exactPlace) {
    final List existingPlaces = this.invocationsNet.get(what);
    Invocation existing = null;
    if (existingPlaces != null) {
      for (int i = 0; i < existingPlaces.size(); i++) {
        final Invocation item = (Invocation) existingPlaces.get(i);
        if (item.equals(where)) {
          existing = item;
          break;
        }
      }
    }
//System.err.println("what: " + what + ", where: " + where + " - exists: "
//    + existing + ", places: " + existingPlaces);
    if (existing == null) {
      this.invocationsNet.putAll(what, new Invocation(where, exactPlace));
    } else {
//System.err.println("what: " + what + ", where: " + where + " - " + (existing.getUsages() + 1));
      existing.increaseUsage();
    }
  }

  private void addInvocationWithDeps(BinItem where, BinMethod method,
      ASTImpl exactPlace) {
    addSingleInvocation(where, method, exactPlace);

    Iterator deps = this.dependentMethods.findIteratorFor(method);
    if (deps != null) {
      while (deps.hasNext()) {
        addSingleInvocation(where, deps.next(), exactPlace);
      }
    }
  }

  // Visitors

  public void visit(BinConstructor x) {
    // FIXME should it be visited also? Don't we miss something here?
    if (!x.isSynthetic()) {
      super.visit(x);
    }
  }

  public void visit(BinConstructorInvocationExpression x) {
    BinConstructor cnstr = x.getConstructor();
    if (cnstr != null) {
      addSingleInvocation(getCurrentLocation(), cnstr, x.getRootAst());
    }
    super.visit(x);
  }

  public void visit(BinNewExpression x) {
//    try {
//      System.err.println("-->BinNewExpression: " + x.getConstructor());
//    } catch (Exception e) {
//    }
    BinConstructor cnstr = x.getConstructor();
    if (cnstr != null) {
      addSingleInvocation(getCurrentLocation(), cnstr, x.getRootAst());
    }
    super.visit(x);
//    try {
//      System.err.println("-->BinNewExpression: " + x.getConstructor());
//    } catch (SourceParsingException ex) {
//    }
  }

// NOTE this doesn't give much, but appears to be quite disturbing
//  public void visit(BinField x) {
//    addSingleInvocation(getCurrentLocation(), x);
//    super.visit(x);
//  }

  public void visit(BinFieldInvocationExpression x) {
    addSingleInvocation(getCurrentLocation(), x.getField(), x.getNameAst());
    super.visit(x);
  }

  public void visit(BinMethodInvocationExpression x) {
//    System.err.println("-->BinMethodInvocation: " + x.getMethod().getQualifiedName());

    BinItem where = getCurrentLocation();
    BinMethod method = x.getMethod();

    if (x.isOutsideMemberInvocation()) {
//      System.err.println("adding outside call");
      addInvocationWithDeps(where, method, x.getNameAst());
    } else {
      if (x.invokedViaSuperReference()) {
//        System.err.println("adding \"super\"");
        addSingleInvocation(where, method, x.getNameAst());
      } else {
//        System.err.println("adding \"this\"");
        //      if (method != getCurrentLocation() ) { // avoid recursion // recursion is avoided in Model.
        addInvocationWithDeps(where, method, x.getNameAst());
        //       }
      }
    }

    super.visit(x);
//    System.err.println("<--BinMethodInvocation: "
//        + x.getMethod().getQualifiedName());
  }
}
