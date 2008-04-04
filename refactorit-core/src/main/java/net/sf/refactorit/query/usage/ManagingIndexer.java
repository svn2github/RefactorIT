/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: ManagingIndexer.java,v 1.69 2006/02/28 13:04:43 jevgeni Exp $ */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.query.ProgressMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Do not visit directly! Use getInvocationsFor() or maybe some callVisit()
 *
 * @author Anton Safonov
 */
public final class ManagingIndexer extends DelegatingVisitor {

  /** List of <@link InvocationData} */
  private final ArrayList invocations = new ArrayList(32);

  /** List of <@link BinMember} */
  private ArrayList affectedMembers;

  public ManagingIndexer() {
    this(ProgressMonitor.Progress.FULL);
  }

  public ManagingIndexer(final boolean skipSynthetic) {
    this(ProgressMonitor.Progress.FULL, skipSynthetic);
  }

  public ManagingIndexer(final ProgressMonitor.Progress progress) {
    this(progress, false);
  }

  public ManagingIndexer(final ProgressMonitor.Progress progress,
      final boolean skipSynthetic) {
    super(skipSynthetic);

    // register a progress monitor
    final ProgressListener listener = (ProgressListener) CFlowContext.get(
        ProgressListener.class.getName());
    if (listener != null && progress != ProgressMonitor.Progress.DONT_SHOW) {
      final ProgressMonitor progressMonitor = new ProgressMonitor(listener,
          progress);
      this.registerDelegate(progressMonitor);
    }
  }

  /**
   * Collects invocations from all over the project.
   *
   * @return list of {@link InvocationData}
   */
  public final List getInvocationsForProject(final Project project) {
    // Scan all available sources
    project.accept(this);

    return this.invocations;
  }

  public final List getInvocationsFor(SourceConstruct construct) {
    invokeAcceptOn(construct);

    return invocations;
  }

  /**
   * Just returns already collected data. Can be used after call to some
   * specific <em>visit</em>.
   *
   * @return list of {@link InvocationData}
   */
  public final List getInvocations() {
    return this.invocations;
  }

  public final void clear() {
    if (this.affectedMembers != null) {
      this.affectedMembers.clear();
      this.affectedMembers = null;
    }

    this.invocations.clear();
  }

  public final InvocationData addInvocation(final BinItem what,
      final Object where, final ASTImpl whereAst,
      final SourceConstruct inConstruct) {
//System.err.println("whereAst2: " + whereAst);
    InvocationData data = null;
    if (whereAst.getLine() > 0 && whereAst.getColumn() > 0) {
      data = new InvocationData(what, where, whereAst, inConstruct);
      this.invocations.add(data);
    }
    return data;
  }

  /**
   * Fully specified invocation, so we know everything: what was called, where
   * invocation occured and the node of invocation.
   *
   * @param what was invoked
   * @param where where invoked
   * @param whereAst the node of invocation
   *
   * @return  a new InvocationData that's already added to an internal invocations list
   */
  public final InvocationData addInvocation(final BinItem what,
      final Object where, final ASTImpl whereAst) {
//System.err.println("whereAst: " + whereAst);
    InvocationData data = null;
    if (whereAst.getLine() > 0 && whereAst.getColumn() > 0) {
      data = new InvocationData(what, where, whereAst);
      this.invocations.add(data);
    }
    return data;
  }

  /**
   * Adds already constructed InvocationData object to the list.
   */
  public final InvocationData addInvocation(final InvocationData data) {
    this.invocations.add(data);
    return data;
  }

  /**
   * @return map: key - {@link CompilationUnit}, value - {@link List}
   * of {@link ASTImpl}
   */
  public final MultiValueMap getInvocationsMap() {
    return getInvocationsMap(this.invocations);
  }

  public static MultiValueMap getInvocationsMap(final List invocations) {
    final MultiValueMap map = new MultiValueMap();

    //System.err.println("Number of invocations: " + this.invocations.size());

    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData data = (InvocationData) invocations.get(i);
      if (!map.putAll(data.getCompilationUnit(), data.getWhereAst())) {
        if (Assert.enabled) {
          System.err.println("Failed to put invocation to map: " + data);
        }
      }
    }

    return map;
  }

  /**
   * @return map: key - {@link CompilationUnit}, value - {@link List}
   * of {@link InvocationData}
   */
  public final MultiValueMap getInvocationDataMap() {
    return getInvocationDataMap(this.invocations);
  }

  public static MultiValueMap getInvocationDataMap(final List invocations) {
    final MultiValueMap map = new MultiValueMap();

    //System.err.println("Number of invocations: " + this.invocations.size());

    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData data = (InvocationData) invocations.get(i);
      final CompilationUnit source = data.getCompilationUnit();
      if (!map.putAll(source, data)) {
        if (Assert.enabled) {
          System.err.println("Failed to put invocation to map: " + data);
        }
      }
    }

    return map;
  }

  final void addAffectedMember(final BinMember member) {
    CollectionUtil.addNew(getAffectedMembers(), member);
  }

  public final void callVisit(final Object bin, boolean searchingSuperTypes) {
    callVisit(bin, false, searchingSuperTypes);
  }

  public final void callVisit(final Object bin, final boolean forceFullScan,
      final boolean searchingSuperTypes) {
    Project project = Project.getProjectFor(bin);
    if(project == null) {
      // hack. if project cannot be determined, 
      // consider that no invocations were found
      return;
    }
    
    if (Assert.enabled && project == null) {
      Assert.must(false, "Can't find project for: " + bin);
    }

    if (forceFullScan) {
      this.visit(project);
      return;
    }

    Object[] bins;
    if (bin instanceof Object[]) {
      bins = (Object[]) bin;
    } else {
      bins = new Object[] {bin};
    }
    HashSet visitableUnits = new HashSet();
    for (int i = 0; i < bins.length; i++) {
      Object units = findShortUnitsSet(project, bins[i], searchingSuperTypes);
      if (units == null) { // one of bins requested full scan
        this.visit(project);
        return;
      } else {
        if (units instanceof Collection) {
          visitableUnits.addAll((Collection) units);
        } else {
          visitableUnits.add(units);
        }
      }
    }

    for (Iterator units = visitableUnits.iterator(); units.hasNext(); ) {
      this.visit((CompilationUnit) units.next());
    }
  }

  private static HashSet getCompilationUnitsHavingIdent(
      final Project project, final String[] idents) {
    HashSet toScan = new HashSet();

    List compilationUnits = project.getCompilationUnits();
    upper: for (int i = 0, iMax = compilationUnits.size(); i < iMax; i++) {
      CompilationUnit compilationUnit = (CompilationUnit) compilationUnits.get(i);
      try {
        HashSet unitIdents = compilationUnit.getSource().getASTTree().getIdents();
        for (int k = 0, kMax = idents.length; k < kMax; k++) {
          if (unitIdents.contains(idents[k])) {
            toScan.add(compilationUnit);
            continue upper;
          }
        }
      } catch (NullPointerException e) {
        toScan.add(compilationUnit);
      }
    }

    return toScan;
  }

  /**
   * @return <code>null</code> when full scan is needed,
   * otherwise {@link CompilationUnit} or a set of units
   */
  private static Object findShortUnitsSet(Project project, final Object bin,
      final boolean searchingSuperTypes) {
    if (bin instanceof BinLocalVariable) {
      return ((BinLocalVariable) bin).getCompilationUnit();
    }

    if (bin instanceof BinMember && ((BinMember) bin).isPrivate()) {
      return ((BinMember) bin).getCompilationUnit();
    }

    if (bin instanceof BinConstructor || bin instanceof BinCIType) {
      BinType baseType;
      if (bin instanceof BinConstructor) {
        baseType = ((BinConstructor) bin).getOwner().getBinType();
      } else {
        baseType = (BinType) bin;
      }
      BinTypeRef typeRef = baseType.getTypeRef();
      Set types = new HashSet();
      types.add(typeRef);
      types.addAll(typeRef.getAllSupertypes());
      types.addAll(typeRef.getAllSubclasses());
      if (!searchingSuperTypes && !typeRef.equals(project.getObjectRef())) {
        // a bit of a hack, too many classes use Object
        types.remove(project.getObjectRef());
      }
      String[] idents = extractTypeBasicNames(types);
      return getCompilationUnitsHavingIdent(project, idents);
    }

    if (bin instanceof BinField || bin instanceof BinMethod) {
      return getCompilationUnitsHavingIdent(project,
          new String[] {((BinMember) bin).getName()});
    }

    return null;
  }

  private static String[] extractTypeBasicNames(Set types) {
    Set names = new HashSet(types.size());

    for (Iterator typesIterator = types.iterator(); typesIterator.hasNext(); ) {
      String name = ((BinTypeRef) typesIterator.next()).getName();
      int pos = name.lastIndexOf('.');
      if (pos < 0) {
        pos = name.lastIndexOf('$');
      }
      if (pos >= 0) {
        name = name.substring(pos + 1, name.length());
      }
      names.add(name);
    }

    return (String[]) names.toArray(new String[names.size()]);
  }

  private List getAffectedMembers() {
    if (this.affectedMembers == null) {
      this.affectedMembers = new ArrayList();
    }

    return this.affectedMembers;
  }

  public final BinTypeRef getCurrentType() {
    return super.getCurrentType();
  }

  public final BinItem getCurrentLocation() {
    return super.getCurrentLocation();
  }

}
