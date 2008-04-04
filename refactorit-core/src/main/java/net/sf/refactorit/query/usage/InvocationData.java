/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: InvocationData.java,v 1.56 2005/12/09 12:03:02 anton Exp $ */
package net.sf.refactorit.query.usage;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class InvocationData {
  /** can be BinMethod, BinConstructor, Bin(CI)Type, BinTypeRef, CompilationUnit */
  private final Object where;

  /** can be BinMember, BinPackage, etc...  */
  private final BinItem what;

  /** AST of invocation */
  private final ASTImpl whereAst;

  private boolean isPackageStatement = false;
  private boolean isImportStatement = false;

  private SourceConstruct inConstruct = null;

  public InvocationData(final BinItem what, final Object where,
      final ASTImpl whereAst) {
    this(what, where, whereAst, null);
  }

  public InvocationData(final BinItem what, final Object where,
      final ASTImpl whereAst, final SourceConstruct inConstruct) {
    this.what = what;
    this.where = where;
    this.whereAst = whereAst;
    this.inConstruct = inConstruct;

    if (Assert.enabled) {
      Assert.must(this.whereAst != null,
          "Invocation with null AST: " + where + ", what: " + what);
      Assert.must(this.getCompilationUnit() != null,
          "Source file of Invocation data can not be null - or can it? 'Where'="
          + where);
    }
  }

  public final SourceConstruct getInConstruct() {
    return inConstruct;
  }

  public final boolean equals(final Object obj) {
    return this.whereAst == ((InvocationData) obj).getWhereAst()
        && this.where == ((InvocationData) obj).getWhere()
        && this.what == ((InvocationData) obj).getWhat();
  }

  public final int hashCode() {
    // just some fast hashing...
    return this.whereAst.hashCode()
        + 31 * this.where.hashCode();
  }

  /**
   * @return BinMethod, BinConstructor,
   *         BinInitializer, BinCIType,
   *         BinTypeRef (old hack for class definition location)
   */
  public final Object getWhere() {
    return this.where;
  }

  public final ASTImpl getWhereAst() {
    return this.whereAst;
  }

  public final BinItem getWhat() {
    if (Assert.enabled && this.what == null) {
      Assert.must(false, "item called is null: " + this);
    }
    return this.what;
  }

  public final BinTypeRef getWhatType() {
    if (this.what instanceof BinType) {
      return ((BinType) this.what).getTypeRef();
    } else if (this.what instanceof BinTypeRef) {
        return (BinTypeRef) this.what;
    } else if (this.what instanceof BinMember) {
      return ((BinMember) this.what).getOwner();
    } else {
//      AppRegistry.getExceptionLogger().debug(
//          new IllegalStateException("Unknown what: " + this.what), this);
      return null;
    }
  }

  public final CompilationUnit getCompilationUnit() {
    if (this.where instanceof BinTypeRef && !((BinTypeRef) this.where).isPrimitiveType()) {
      return ((BinTypeRef)this.where).getBinCIType().getCompilationUnit();
    } else if (this.where instanceof LocationAware) {
      return ((LocationAware)this.where).getCompilationUnit();
    } else if (this.where instanceof CompilationUnit) {
      return (CompilationUnit)this.where;
    } else if (this.where == null) {
      System.err.println("Location is null");
    } else {
      System.err.println("Unsupported location: " + this.where);
    }

    return null;
  }

  public final int getLineNumber() {
    return whereAst.getLine();
  }

  public final void setPackageStatement(final boolean b) {
    this.isPackageStatement = b;
  }

  public final boolean isPackageStatement() {
    return this.isPackageStatement;
  }

  public final void setImportStatement(final boolean b) {
    this.isImportStatement = b;
  }

  public final boolean isImportStatement() {
    return this.isImportStatement;
  }

  public String toString() {
    return "source: " + getCompilationUnit() + ", where: " + this.where
        + ", whereAst: " + this.whereAst + ", what: " + this.what
        + ", inConstruct: " + inConstruct;
  }

  public final BinMember getWhereMember() {
    final BinMember cur;

    if (this.where instanceof BinTypeRef
        && ((BinTypeRef) this.where).isReferenceType()) {
      if (this.inConstruct != null) {
        cur = this.inConstruct.getParentMember();
      } else {
        cur = ((BinTypeRef)this.where).getBinCIType();
      }
    } else if (this.where instanceof BinMember) {
      cur = (BinMember)this.where;
    } else if (this.where instanceof CompilationUnit) {
      cur = null; // no member
    } else {
      if (Assert.enabled) {
        Assert.must(false, "Strange location: " + this.where);
      }
      cur = null;
    }

    return cur;
  }

  public final BinTypeRef getWhereType() {
    if (this.where instanceof BinTypeRef) {
      return (BinTypeRef)this.where;
    } else if (this.where instanceof BinMember) {
      return ((BinMember)this.where).getOwner();
    } else {
      return null; // CompilationUnit location also falls here
    }
  }

  public static List getAsts(List invocationDatas) {
    List result = new ArrayList(invocationDatas.size());
    for (int i = 0; i < invocationDatas.size(); i++) {
      InvocationData d = (InvocationData) invocationDatas.get(i);
      result.add(d.getWhereAst());
    }
    return result;
  }
  
  public static List getInvocationObjects(List invocationDatas) {
    List result = new ArrayList(invocationDatas.size());
    for (int i = 0; i < invocationDatas.size(); i++) {
      InvocationData d = (InvocationData) invocationDatas.get(i);
      result.add(d.getWhat());
    }
    return result;
  }
}
