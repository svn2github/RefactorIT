/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;
import net.sf.refactorit.query.usage.filters.BinInterfaceSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Anton Safonov
 * @author Risto Alas
 */
public final class PackageIndexer extends TargetIndexer {
  private final boolean includeSubpackages;

  public PackageIndexer(final ManagingIndexer supervisor,
      final BinPackage target,
      final boolean includeSubclasses, final boolean includeSuperclasses) {
    this(supervisor, target, includeSubclasses, includeSuperclasses, false);
  }

  public PackageIndexer(final ManagingIndexer supervisor,
      final BinPackage target,
      final boolean includeSubclasses, final boolean includeSuperclasses,
      final boolean includeSubpackages) {
    super(supervisor, target, null, includeSubclasses, includeSuperclasses);

    this.includeSubpackages = includeSubpackages;

    registerMemberDelegates();
  }

  private void registerMemberDelegates() {
    final BinPackage target = (BinPackage) getTarget();
    if (this.includeSubpackages) {
      for (Iterator i = target.getSubPackages().iterator(); i.hasNext(); ) {
        registerTypeIndexers((BinPackage) i.next());
      }
    }

    registerTypeIndexers(target);
  }

  private void registerTypeIndexers(final BinPackage aPackage) {
    for (Iterator i = aPackage.getAllTypes(); i.hasNext(); ) {
      final BinTypeRef entry = (BinTypeRef) i.next();
      registerTypeIndexer(entry);
    }
  }

  private void registerTypeIndexer(final BinTypeRef entry) {
    final SearchFilter filter;
    if (entry.getBinCIType().isClass()
        || entry.getBinCIType().isEnum()
        || entry.getBinCIType().isAnnotation()) {
      filter = new BinClassSearchFilter(
          isIncludeSupertypes(), isIncludeSubtypes());
    } else if (entry.getBinCIType().isInterface()) {
      filter = new BinInterfaceSearchFilter(
          (BinInterface) entry.getBinCIType(),
          isIncludeSupertypes(), isIncludeSubtypes());
    } else {
      throw new IllegalArgumentException("Unsupported entry: " + entry.getBinCIType());
    }

    new TypeIndexer(getSupervisor(), entry.getBinCIType(), filter);
  }

  public final void leave(final CompilationUnit source) {
    removeDuplicatesCoveredByBothPackageAndTypeIndexer(
        getSupervisor().getInvocations());
  }

  private void removeDuplicatesCoveredByBothPackageAndTypeIndexer(final List
      invocations) {
    final List packageInvocations = getInvocationsOfClass(
        BinPackage.class, invocations);
    final List typeInvocations = getInvocationsOfClass(
        BinClass.class, invocations);

    for (Iterator i = packageInvocations.iterator(); i.hasNext(); ) {
      removeDuplicateInvocations(
          (InvocationData) i.next(), typeInvocations, invocations);
    }
  }

  private static void removeDuplicateInvocations(final InvocationData
      packageData, final List typeInvocations, final List invocations) {
    for (Iterator j = typeInvocations.iterator(); j.hasNext(); ) {
      final InvocationData typeData = (InvocationData) j.next();
      if (packageData.getWhereAst().getNextSibling() == typeData.getWhereAst()) {
        invocations.remove(packageData);
      }
    }
  }

  private List getInvocationsOfClass(final Class c, final List invocations) {
    final List result = new ArrayList();

    for (Iterator i = invocations.iterator(); i.hasNext(); ) {
      final InvocationData data = (InvocationData) i.next();
      if (c.isAssignableFrom(data.getWhat().getClass())) {
        result.add(data);
      }
    }

    return result;
  }

  public final void visit(final CompilationUnit source) {
    checkPackageUsageInfos(source, source.getPackageUsageInfos());
  }

  private void checkPackageUsageInfos(final CompilationUnit compilationUnit,
      final List renamePackageData) {
    if (renamePackageData == null) {
      // renamePackageData is null for files in default packages that have no import statements
      return;
    }

    for (Iterator i = renamePackageData.iterator(); i.hasNext(); ) {
      checkPackageUsageInfos(compilationUnit, (PackageUsageInfo) i.next());
    }
  }

  private boolean shouldIndexInvocationOf(final BinPackage aPackage) {
    final BinPackage target = (BinPackage) getTarget();

    if (this.includeSubpackages) {
      return aPackage.isSubPackageOf(target) || aPackage.isIdentical(target);
    } else {
      return aPackage.isIdentical(target);
    }
  }

  private void checkPackageUsageInfos(final CompilationUnit compilationUnit,
      final PackageUsageInfo data) {
    if (shouldIndexInvocationOf(data.getBinPackage())) {
      final InvocationData invocationData = getSupervisor().addInvocation(
          getTarget(),
          compilationUnit,
          data.getNode()
          );
      if (invocationData != null) {
        if (isPackageStatement(data)) {
          invocationData.setPackageStatement(true);
        }
        if (isImportStatement(data)) {
          invocationData.setImportStatement(true);
        }
      }
    }
  }

  private static boolean isPackageStatement(final PackageUsageInfo data) {
    final ASTImpl parent = data.getNode().getParent();
    if (parent != null && parent.getType() == JavaTokenTypes.PACKAGE_DEF) {
      return true;
    }
    return false;
  }

  private static boolean isImportStatement(final PackageUsageInfo data) {
    final ASTImpl parent = data.getNode().getParent();
    if (parent != null && parent.getType() == JavaTokenTypes.IMPORT) {
      return true;
    }

    return false;
  }
}
