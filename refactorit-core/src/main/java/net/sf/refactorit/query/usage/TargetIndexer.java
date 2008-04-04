/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.DelegateVisitor;
import net.sf.refactorit.query.usage.filters.SearchFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TargetIndexer extends DelegateVisitor {
  private final ManagingIndexer supervisor;

  private final BinItem target;
  private final BinCIType type;
  private BinTypeRef typeRef;
  private final boolean includeSubtypes;
  private final boolean includeSupertypes;

  private SearchFilter filter;

  private Set supertypes;
  private HashSet subtypes;

  private boolean searchForNames = false;

  public TargetIndexer(final ManagingIndexer supervisor,
      final BinItem target) {
    this(supervisor, target, null, false, false);
  }

  public TargetIndexer(final ManagingIndexer supervisor,
      final BinItem target,
      final BinCIType type) {
    this(supervisor, target, type, false, false);
  }

  public TargetIndexer(final ManagingIndexer supervisor,
      final BinItem target,
      final BinCIType type,
      final SearchFilter filter) {
    this(supervisor, target, type, filter.isIncludeSubtypes(),
        filter.isIncludeSupertypes());

    this.filter = filter;
  }

  public TargetIndexer(final ManagingIndexer supervisor,
      final BinItem target,
      final BinCIType type,
      final boolean includeSubtypes,
      final boolean includeSupertypes) {
    this.supervisor = supervisor;
    this.supervisor.registerDelegate(this);
    this.target = target;
    this.includeSubtypes = includeSubtypes;
    this.includeSupertypes = includeSupertypes;

    if (this.includeSupertypes
        && (target instanceof BinField || target instanceof BinMethod)) {
      this.type = ((BinMember) target).getOwner().getBinCIType();
    } else {
      this.type = type;
    }
    if (this.type != null) {
      this.typeRef = this.type.getTypeRef();
    }
  }

  final void setSearchForNames(final boolean searchForNames) {
    this.searchForNames = searchForNames;
  }

  final boolean isSearchForNames() {
    return this.searchForNames;
  }

  public final ManagingIndexer getSupervisor() {
    return this.supervisor;
  }

  protected final BinItem getTarget() {
    return this.target;
  }

  final SearchFilter getFilter() {
    return filter;
  }

  final BinCIType getType() {
    return this.type;
  }

  final BinTypeRef getTypeRef() {
    return this.typeRef;
  }

  final boolean isIncludeSubtypes() {
    return this.includeSubtypes;
  }

  final boolean isIncludeSupertypes() {
    return this.includeSupertypes;
  }

  public void visit(final BinCIType type) {
    if (type.isLocal()) {
      // some method local types are created and registered only on method body build
      this.supertypes = null;
      this.subtypes = null;
    }
  }

  final Set getSupertypes() {
    if (this.supertypes == null) {
      this.supertypes = this.typeRef.getAllSupertypes();
//      final List list = this.typeRef.getAllSupertypes();
//      this.supertypes = new HashSet(list.size(), 0.9F);
//      this.supertypes.addAll(list);

      /*      Iterator it = this.supertypes.iterator();
            while (it.hasNext()) {
              System.err.println("Super: " + it.next());
            }*/
    }

    return this.supertypes;
  }

  final Set getSubtypes() {
    if (this.subtypes == null) {
      final List list = this.typeRef.getAllSubclasses();
      this.subtypes = new HashSet(list.size(), 0.9F);
      this.subtypes.addAll(list);

      /*      Iterator it = this.subtypes.iterator();
            while (it.hasNext()) {
              System.err.println("Sub: " + it.next());
            }*/
    }

    return this.subtypes;
  }
}
