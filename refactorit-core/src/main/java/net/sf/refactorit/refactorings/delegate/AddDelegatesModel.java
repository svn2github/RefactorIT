/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Tonis Vaga
 */
public class AddDelegatesModel extends BinTreeTableModel {
  private List selectedFields;

  public AddDelegatesModel(BinClass cls, List selectedFields) {
    super(new BinTreeTableNode(cls, false));

    this.selectedFields = selectedFields;

    initChildren();
  }

  private void initChildren() {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();

    BinClass clazz = (BinClass) root.getBin();

    BinField allFields[] = AddDelegatesRefactoring.getDelegateFields(clazz);

    for (int i = 0; i < allFields.length; i++) {
      if (selectedFields.contains(allFields[i]) || selectedFields.isEmpty()) {
        BinTreeTableNode node = createFieldNodes(allFields[i]);
        if (node != null) {
          root.addChild(node);
        }
      }
    }

    root.setSelected(false);
  }

  private BinTreeTableNode createFieldNodes(BinField binField) {
    BinTreeTableNode mainNode = new FieldNode(binField);

    List delegatesList = AddDelegatesRefactoring
        .createDelegateMethodsList(binField);

    Collections.sort(delegatesList, new Comparator() {
      public int compare(Object obj1, Object obj2) {
        return ((BinMethod) obj1).getName()
            .compareTo(((BinMethod) obj2).getName());
      }
    });

    if (delegatesList.size() == 0) {
      return null;
    }

    Iterator iter = delegatesList.iterator();

    while (iter.hasNext()) {
      BinMethod item = (BinMethod) iter.next();
      MethodNode childNode = new MethodNode(item);
      mainNode.addChild(childNode);
    }

    return mainNode;
  }

  public int getColumnCount() {
    return 1;
  }

  public String getColumnName(int column) {
    return "Select Methods";
  }

  public Map getSelectedMap() {
    Map result = new HashMap();

    BinTreeTableNode rootNode = (BinTreeTableNode) getRoot();

    for (int i = 0, max = rootNode.getChildCount(); i < max; i++) {
      FieldNode fieldNode = (FieldNode) rootNode.getChildAt(i);

      //FIXME: because tree doesn't select paren't when child is selected
      //disabled this

//      if (!fieldNode.isSelected()) {
//        continue;
//      }

      int maxJ = fieldNode.getChildCount();
      ArrayList methods = new ArrayList(maxJ);

      for (int j = 0; j < maxJ; ++j) {
        MethodNode methodNode = (MethodNode) fieldNode.getChildAt(j);

        if (methodNode.isSelected()) {
          methods.add(methodNode.getBin());
        }
      }

      if (methods.size() > 0) {
        result.put(fieldNode.getBin(), methods);
      }
    }

    return result;
  }

  public boolean isSomethingSelected() {
    return selectedFields.size() > 0;
  }
}
