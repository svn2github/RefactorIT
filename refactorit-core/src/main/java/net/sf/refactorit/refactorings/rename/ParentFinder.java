/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;


/** @author  risto */
public class ParentFinder {

  private List sourcesInWrongFolders = new ArrayList();

  public boolean hasPrefix(String prefix, Source source) {
    List packages = CollectionUtil.toList(new StringTokenizer(prefix, "."));
    Collections.reverse(packages);

    for (int i = 0; i < packages.size(); i++) {
      String aPackage = (String) packages.get(i);
      if (!aPackage.equals(source.getName())) {
        return false;
      }
      source = source.getParent();
    }
    return true;
  }

  public Source findParent(String prefix, Source source) {
    while (source != null && !hasPrefix(prefix, source)) {
      source = source.getParent();
    }

    return source;
  }

  public Source findRoot(String prefix, Source source) {
    Source parent = findParent(prefix, source);
    int parents = new StringTokenizer(prefix, ".").countTokens();
    for (int i = 0; i < parents; i++) {
      parent = parent.getParent();
    }

    return parent;
  }

  public Set findFolders(String prefix, List packages) {
    Set result = new HashSet();
    for (int j = 0; j < packages.size(); j++) {
      BinPackage p = (BinPackage) packages.get(j);
      findFolders(prefix, p, result);
    }

    return result;
  }

  private void findFolders(String prefix, BinPackage aPackage, Set result) {
    for (Iterator i = aPackage.getAllTypes(); i.hasNext(); ) {
      BinCIType type = ((BinTypeRef) i.next()).getBinCIType();

      Source folder = findFolder(prefix, type);
      if (folder != null) {
        result.add(folder);
      } else {
        getSourcesInWrongFolders().add(type.getCompilationUnit().getSource());
      }
    }
  }

  private Source findFolder(String prefix, BinCIType type) {
    CompilationUnit compilationUnit = type.getCompilationUnit();
    if (compilationUnit == null) {
      return null;
    }

    return findParent(prefix, compilationUnit.getSource());
  }

  public List getSourcesInWrongFolders() {
    return sourcesInWrongFolders;
  }
}
