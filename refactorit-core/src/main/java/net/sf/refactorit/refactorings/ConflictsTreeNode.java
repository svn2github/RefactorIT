/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.Comparator;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class ConflictsTreeNode extends BinTreeTableNode {
  public static final Comparator conflictsComparator
      = new ConflictsComparator();

  public ConflictsTreeNode(Object bin, boolean showSource) {
    super(bin, showSource
        || (bin instanceof RefactoringStatus.Entry
        && ((RefactoringStatus.Entry) bin).getBin() instanceof
        BinMemberInvocationExpression));

    /**    if (Assert.enabled) {
          Assert.must((bin instanceof RefactoringStatus.Entry),
              "bin is not of type RefactoringStatus.Entry:"
              + bin.getClass().getName());
        }*/

    if (bin instanceof RefactoringStatus.Entry) {
      Object statusBin = ((RefactoringStatus.Entry) bin).getBin();
      if (statusBin instanceof LocationAware) {
        this.setSourceHolder(((LocationAware) statusBin).getCompilationUnit());
        this.setLine(((LocationAware) statusBin).getStartLine());
        if (statusBin instanceof BinSourceConstruct) {
          this.addAst(((BinSourceConstruct) statusBin).getRootAst());
        }
      }
    }
  }

  public String getDisplayName() {
    if (getBin() instanceof RefactoringStatus.Entry) {
      if (((RefactoringStatus.Entry) getBin()).getBin()
          instanceof BinMemberInvocationExpression) {
        this.name = getLineSource();
        this.name = getSource().getName() + ", " +
            getLineNumber() + ": " + this.name;
      } else {
        this.name = ((RefactoringStatus.Entry) getBin()).getMessage();
      }
    } else {
      this.name = super.getDisplayName();
    }

    return this.name;
  }

  public void addChildren(List entries) {
    for (int i = 0; i < entries.size(); i++) {
      final RefactoringStatus.Entry entry
          = (RefactoringStatus.Entry) entries.get(i);

      ConflictsTreeNode node = new ConflictsTreeNode(entry, false);
      final ConflictsTreeNode existing
          = (ConflictsTreeNode)this.findChild(node.getDisplayName());
      if (existing == null) {
        this.addChild(node);
      } else {
        node = existing;
      }
      if (entry.getSubEntries().size() > 0) {
        node.addChildren(entry.getSubEntries());
      }
    }
  }

  public static final class ConflictsComparator extends NodeComparator {
    public int compare(Object a, Object b) {
      if (!(a instanceof BinTreeTableNode) || !(b instanceof BinTreeTableNode)) {
        return 0;
      }

      if (!(a instanceof ConflictsTreeNode) && b instanceof ConflictsTreeNode) {
        return +1;
      }

      if (a instanceof ConflictsTreeNode && !(b instanceof ConflictsTreeNode)) {
        return -1;
      }

      final Object aBin = ((BinTreeTableNode) a).getBin();
      final Object bBin = ((BinTreeTableNode) b).getBin();
      if (aBin instanceof RefactoringStatus.Entry
          && bBin instanceof RefactoringStatus.Entry) {
        // reverse order to get the most severe first
        int res = ((RefactoringStatus.Entry) bBin).getSeverity()
            - ((RefactoringStatus.Entry) aBin).getSeverity();
        if (res != 0) {
          return res;
        }
      }

      return super.compare(a, b);
    }
  }
}
