/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit;


import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.AuditRule.Priority;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.ui.panel.ResultsTreeDisplayState;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.text.ParseException;
import java.util.Comparator;


/**
 *
 *
 * @author Risto Alas
 * @author Igor Malinin
 */
public abstract class AuditTreeTableColumn {
  public int getMinWidth() {
    return 15;
  }

  public int getPreferredWidth() {
    return 75;
  }

  public int getMaxWidth() {
    return Integer.MAX_VALUE;
  }

  public boolean isPackageSort() {
    return true;
  }

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return null;
  }

  public Object getSortObjectOrNull(RuleViolation violation) {
    return null;
  }

  public Comparator getNodeComparatorOrNull(AuditTreeTableModel model) {
    return null;
  }

  public abstract Object getValue(AuditTreeTableModel model, Object node);
}


class LocationColumn extends AuditTreeTableColumn {
  private ResultsTreeDisplayState state = new ResultsTreeDisplayState();

  public Object getValue(AuditTreeTableModel model, Object node) {
    return node;
  }

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return state;
  }
}


class LineColumn extends AuditTreeTableColumn {
  public int getMinWidth() {
    return 20;
  }

  public int getPreferredWidth() {
    return 50;
  }

  public int getMaxWidth() {
    return 100;
  }

  public Object getValue(AuditTreeTableModel model, Object node) {
    return ((ParentTreeTableNode) node).getLineNumber() + "  ";
  }
}


class SourceColumn extends AuditTreeTableColumn {
  public Object getValue(AuditTreeTableModel model, Object node) {
    return ((ParentTreeTableNode) node).getLineSource();
  }
}


class PriorityColumn extends AuditTreeTableColumn {
  private ResultsTreeDisplayState state = new ResultsTreeDisplayState();

  public int getMinWidth() {
    return 5;
  }

  public int getPreferredWidth() {
    return 80;
  }

  public int getMaxWidth() {
    return 80;
  }

  public Object getValue(AuditTreeTableModel model, Object node) {
    return model.getMaxPriorityOfChildren(node).getDescription();
  }

  public Object getSortObjectOrNull(RuleViolation violation) {
    return violation.getPriority();
  }

  public Comparator getNodeComparatorOrNull(AuditTreeTableModel model) {
    return new PriorityComparator(model);
  }

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return state;
  }
}


class TypeColumn extends AuditTreeTableColumn {
  private ResultsTreeDisplayState state = new ResultsTreeDisplayState();

  public int getMinWidth() {
    return 5;
  }

  public int getPreferredWidth() {
    return 100;
  }

  public int getMaxWidth() {
    return 145;
  }

  public Object getValue(AuditTreeTableModel model, Object node) {
    if (node instanceof AuditTreeTableNode) {
      return ((AuditTreeTableNode) node).getRuleViolation().getTypeShortName();
    }

    return "";
  }

  public Object getSortObjectOrNull(RuleViolation violation) {
    return violation.getAuditName();
  }

  public Comparator getNodeComparatorOrNull(AuditTreeTableModel model) {
    return new PriorityComparator(model);
  }

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return state;
  }
}


class DensityColumn extends AuditTreeTableColumn {
  private ResultsTreeDisplayState state = new ResultsTreeDisplayState();

  public int getMinWidth() {
    return 5;
  }

  public int getPreferredWidth() {
    return 60;
  }

  public int getMaxWidth() {
    return 60;
  }

  public Object getValue(AuditTreeTableModel model, Object node) {
    return model.getDisplayableDensity(node);
  }

  public boolean isPackageSort() {
    return false;
  }

  public Comparator getNodeComparatorOrNull(final AuditTreeTableModel model) {
    return new Comparator() {
      public int compare(Object a, Object b) {
        float f1, f2;

        try {
          f1 = AuditTreeTableModel.densityParser
              .parse(model.getDisplayableDensity(a)).floatValue();
          f2 = AuditTreeTableModel.densityParser
              .parse(model.getDisplayableDensity(b)).floatValue();
        } catch (ParseException e) {
          e.printStackTrace();
          throw new ChainableRuntimeException(e);
        }

        if (f1 > f2) {
          return -1;
        }

        if (f2 > f1) {
          return 1;
        }

        return 0;
      }
    };
  }

  public ResultsTreeDisplayState getResultsTreeDisplayState() {
    return state;
  }
}


class PriorityComparator implements Comparator {
  private AuditTreeTableModel model;

  public PriorityComparator(AuditTreeTableModel model) {
    this.model = model;
  }

  public int compare(Object a, Object b) {
    Priority p1 = model.getMaxPriorityOfChildren(a);
    Priority p2 = model.getMaxPriorityOfChildren(b);

    if (p1 == p2) {
      return 0;
    }

    return (Priority.highest(p1, p2) == p1) ? -1 : 1;
  }
}
