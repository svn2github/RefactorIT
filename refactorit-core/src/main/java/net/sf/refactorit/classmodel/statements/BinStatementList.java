/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.statements;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinStatementListFormatter;

import java.util.HashMap;


/**
 * Defines list which contains statements
 */
public final class BinStatementList extends BinStatement implements Scope {
  private BinStatement[] statements;

  public BinStatementList(BinStatement[] statements, ASTImpl node) {
    super(node);

    this.statements = statements;
  }

  public BinStatement[] getStatements() {
    return statements;
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(BinItemVisitor visitor) {
    for (int i = 0; i < statements.length; ++i) {
      statements[i].accept(visitor);
    }
  }

  public void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public boolean contains(Scope other) {
    if (other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else {
      return false;
    }
  }

  public void clean() {
    if (statements != null) {
      for (int i = 0; i < statements.length; i++) {
        statements[i].clean();
      }
    }

    statements = null;
//    myScopeRules = null;

    super.clean();
  }

  public boolean isSame(BinItem other) {
    if (!(other instanceof BinStatementList)) {
      return false;
    }

    final BinStatementList x = (BinStatementList) other;
    if (this.statements.length != x.statements.length) {
      return false;
    }

    for (int i = 0; i < this.statements.length; i++) {
      if (!this.statements[i].isSame(x.statements[i])) {
        return false;
      }
    }

    return true;
  }

  public BinItemFormatter getFormatter() {
    return new BinStatementListFormatter(this);
  }
}
