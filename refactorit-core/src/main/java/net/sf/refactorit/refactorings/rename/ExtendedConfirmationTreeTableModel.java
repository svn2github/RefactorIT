/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.NonJavaTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Confirmation tree that can include non-java occurrences.
 *
 * @author  tanel
 */
public class ExtendedConfirmationTreeTableModel extends
    ConfirmationTreeTableModel {
  List nonJavaNodes = new ArrayList();

  public ExtendedConfirmationTreeTableModel(
      Object member, List usages, List mandatoryUsages, List nonJavaOcurrences
      ) {
    super(member, usages, mandatoryUsages);
    initNonJavaNodes(nonJavaOcurrences);
  }

  protected void initNonJavaNodes(List nonJavaOcurrences) {
    if (nonJavaOcurrences.size() > 0) {
      BinTreeTableNode parent
          = new BinTreeTableNode("Occurrences in non-java files", false);
      ((ParentTreeTableNode) getRoot()).addChild(parent);

      for (Iterator i = nonJavaOcurrences.iterator(); i.hasNext(); ) {
        Occurrence o = (Occurrence) i.next();
        NonJavaTreeTableNode node = new NonJavaTreeTableNode(o);
        parent.addChild(node);
        nonJavaNodes.add(node);
      }

      parent.reflectLeafNumberToParentName();
    }
  }

  /**
   *  Finds all non-java occurrences that are checked in the tree.
   */
  public List getCheckedNonJavaOccurrences() {
    List occurrences = new ArrayList();

    for (Iterator iter = nonJavaNodes.iterator(); iter.hasNext(); ) {
      NonJavaTreeTableNode n = (NonJavaTreeTableNode) iter.next();
      if (n.isCheckBoxNeeded() && n.isSelected()) {
        occurrences.add(n.getBin());
      }
    }

    return occurrences;
  }
}
