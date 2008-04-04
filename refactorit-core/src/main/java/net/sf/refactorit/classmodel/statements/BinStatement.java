/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.statements;


import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.refactorings.LocationAwareImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Defines statement base class.
 */
public abstract class BinStatement extends BinSourceConstruct {

  public static final BinStatement[] NO_STATEMENTS = new BinStatement[0];

  public BinStatement() {
    super(null);
  }

  public BinStatement(ASTImpl rootAst) {
    super(rootAst);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return all statements on the same level and going after given statement
   */
  public final List getSiblings() {
    final List result = new ArrayList();
    if (getParent() instanceof BinStatementList) {
      final BinStatement[] stats
          = ((BinStatementList) getParent()).getStatements();
      boolean add = false;
      for (int i = 0; i < stats.length; i++) {
        if (add) {
          result.add(stats[i]);
        } else if (stats[i] == this) {
          add = true;
        }
      }
    }

    return result;
  }

  public static final boolean isAllSameLevel(final List statements) {
    if (statements == null || statements.size() == 0) {
      return true;
    }

    List siblings = null;
    final Iterator stats = statements.iterator();
    while (stats.hasNext()) {
      final Object stat = stats.next();
      if (stat instanceof Comment || stat instanceof LocationAwareImpl) {
        continue;
      }

      if (!(stat instanceof BinStatement)) {
        return false;
      }

      if (siblings == null) {
        siblings = ((BinStatement) stat).getSiblings();
      } else {
        if (!siblings.contains(stat)) { // not the same level
          return false;
        }
      }
    }

    return true;
  }
}
