/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movetype;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.TypeNameIndexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class TypeDependencies {
  public MultiValueMap used = null;
  public List usedList = null;
  public MultiValueMap uses = null;
  public List usesList = null;

  private final Map typeUsedCache = new HashMap();

  public TypeDependencies() {
  }

  public final void findNameUsages(BinCIType type,
      ProgressMonitor.Progress progress,
      boolean findUsesAlso) {
    clear();

    ManagingIndexer supervisor;

    if (findUsesAlso && type.isFromCompilationUnit()) {
      supervisor = new ManagingIndexer(progress.subdivision(0, 2), true);
      new ImportNeedingDependenciesIndexer(supervisor, type);
      supervisor.visit(type);
      this.usesList = supervisor.getInvocations();
      this.uses = ManagingIndexer.getInvocationDataMap(this.usesList);
    }

    supervisor = new ManagingIndexer(progress.subdivision(1, 2), true);
    // NOTE this is not a bug, exactly NameIndexer should be used here!
    new TypeNameIndexer(supervisor, type);
    supervisor.visit(type.getProject());
    this.usedList = supervisor.getInvocations();
    this.used = ManagingIndexer.getInvocationDataMap(this.usedList);
  }

  public final void findUsages(List types, ProgressMonitor.Progress progress) {
    clear();

    ManagingIndexer supervisor;

    this.usesList = new ArrayList();

    for (int i = 0; i < types.size(); i++) {
      BinCIType type = (BinCIType) types.get(i);
      if (type.isFromCompilationUnit()) {
        supervisor = new ManagingIndexer(ProgressMonitor.Progress.DONT_SHOW, true);
        new DependenciesIndexer(supervisor, type);
        supervisor.visit(type);
        CollectionUtil.addAllNew(this.usesList, supervisor.getInvocations());
      }
    }
    this.uses = ManagingIndexer.getInvocationDataMap(this.usesList);

    this.usedList = new ArrayList(128);
    for (int i = 0; i < types.size(); i++) {
      BinCIType type = (BinCIType) types.get(i);
      List invocations = (List) typeUsedCache.get(type);
      if (invocations == null) {
        supervisor = new ManagingIndexer(progress.subdivision(i, types.size()), true);
        new TypeIndexerSkippingMethods(supervisor, type);
        supervisor.visit(type.getProject());
        invocations = supervisor.getInvocations();
        typeUsedCache.put(type, invocations);
      }
      this.usedList.addAll(invocations);
    }
    this.used = ManagingIndexer.getInvocationDataMap(this.usedList);
  }

  private static class TypeIndexerSkippingMethods extends TypeIndexer {
    TypeIndexerSkippingMethods(ManagingIndexer supervisor,
        BinCIType type) {
      super(supervisor, type, false, false);
    }

    public void visit(BinMethodInvocationExpression x) {
    }
  }


  void clear() {
    if (used != null) {
      used.clear();
    }
    if (usedList != null) {
      usedList.clear();
    }
    if (uses != null) {
      uses.clear();
    }
    if (usesList != null) {
      usesList.clear();
    }
  }

  public static List getTypesUsedByName(List userTypes,
      ProgressMonitor.Progress progress) {
    TypeDependencies d = new TypeDependencies();

    List result = new ArrayList();
    for (int i = 0; i < userTypes.size(); i++) {
      BinCIType type = (BinCIType) userTypes.get(i);

      d.findNameUsages(type, progress.subdivision(i, userTypes.size()), false);
      CollectionUtil.addAllNew(result, d.usedList);
    }

    d.clear();

    return result;
  }

  private static class ImportNeedingDependenciesIndexer extends
      DependenciesIndexer {
    ImportNeedingDependenciesIndexer(ManagingIndexer supervisor, BinItem target) {
      super(supervisor, target);
    }

    public void visit(BinMethodInvocationExpression x) {}

    public void visit(BinFieldInvocationExpression x) {}
  }
}
