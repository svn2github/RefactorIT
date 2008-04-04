/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.parser.TreeASTImpl;

import java.util.List;


public final class PackageNameIndexer extends TargetIndexer {

  public PackageNameIndexer(final ManagingIndexer supervisor,
      final BinPackage target) {
    super(supervisor, target);
    setSearchForNames(true);
  }

  public final void visit(final CompilationUnit source) {
    source.visit(getSupervisor());
  }

  public final void leave(final CompilationUnit compilationUnit) {
//    System.err.println("leaving CompilationUnit: " + compilationUnit);
    final List list = compilationUnit.getPackageUsageInfos();

    // it is null when there is no imports
    if (list != null) {
      for (int i = 0, max = list.size(); i < max; i++) {
        final PackageUsageInfo data = (PackageUsageInfo) list.get(i);
        if (getTarget() == data.getBinPackage()) {
          getSupervisor().addInvocation(
              getTarget(), // we don't need to know WHAT was called ???
              compilationUnit,
              data.getNode());
        }
//      System.err.println("PackageUsageInfo: " + list.get(i));
      }
    }

    //<FIX> Aleksei Sosnovski 08.2005
    if (getTarget() instanceof BinPackage) {
      BinPackage pack = (BinPackage) getTarget();

      if (pack.getQualifiedName() != null
          && pack.getQualifiedName().length() == 0) {

        if (compilationUnit.getPackage().getQualifiedName().length() == 0) {
          TreeASTImpl ast = new SimpleASTImpl();
          ast.setStartLine(1);
          ast.setStartColumn(0);
          ast.setEndLine(1);
          ast.setEndColumn(0);
          ast.setText("");

          getSupervisor().addInvocation( new InvocationData(
              getTarget(), // we don't need to know WHAT was called ???
              compilationUnit,
              ast));//hack
        }
      }
    }
    //</FIX>
  }
}
