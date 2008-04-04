/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.calltree;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.CallTreeIndexer;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class CallTreeModel extends BinTreeTableModel {

  private boolean tooDeep = false;

  public CallTreeModel(Project project, BinItem target) {
    super(new CallTreeNode(target));

    CallTreeIndexer cti = new CallTreeIndexer();
    MultiValueMap invocations = cti.getInvocationsNet(project);

//    showResult(invocations);

    populateTree((BinTreeTableNode) getRoot(), target, invocations);

//    ((BinTreeTableNode)getRoot()).reflectLeafNumberToParentName();
    ((BinTreeTableNode) getRoot()).sortAllChildren();

    invocations.clear();
  }

  private void populateTree(BinTreeTableNode parent, Object item,
      MultiValueMap invocations) {
    List items = null;
    if (invocations.containsKey(item)) {
      items = invocations.get(item);
    }
    if (items == null || items.size() == 0) {
      return; // not used anywhere
    }
    HashMap workingMap = new HashMap(128);
    workingMap.put(parent, items);

    int totalItems = 0;

    HashMap newMap = new HashMap(128);
    while (true) {
      Iterator it = workingMap.entrySet().iterator();
      while (it.hasNext()) {
        final Map.Entry entry = (Map.Entry) it.next();
        final BinTreeTableNode curParent = (BinTreeTableNode) entry.getKey();

        if (isTooDeep()) {
          curParent.addChild(new CallTreeNode("Too deep"));
        } else {
          final List curItems = (List) entry.getValue();
          nextItem:for (int i = 0, max = curItems.size(); i < max; i++) {
            final CallTreeIndexer.Invocation invocation
                = (CallTreeIndexer.Invocation) curItems.get(i);
            final Object curItem = invocation.getWhere();

            // let's check for recursive loops
            BinTreeTableNode searchNode = curParent;
            while (searchNode != null) {
              if (searchNode.getBin() == curItem) {
                BinTreeTableNode recur = new BinTreeTableNode(curItem);
                totalItems++;
                recur.setDisplayName("recursive call of "
                    + recur.getDisplayName());
                curParent.addChild(recur);
                continue nextItem; // already used in this branch
              }
              searchNode = (BinTreeTableNode) searchNode.getParent();
            }

            final BinTreeTableNode node = new CallTreeNode(invocation);
            totalItems++;
            curParent.addChild(node);

            if (invocations.containsKey(curItem)) {
              newMap.put(node, invocations.get(curItem));
            }
          }

        }
      }

      if (isTooDeep() || totalItems >= 5000) {
        it = newMap.keySet().iterator();
        while (it.hasNext()) {
          final BinTreeTableNode node = (BinTreeTableNode) it.next();
          node.addChild(new CallTreeNode("Too deep"));
        }
        break;
      }

      if (newMap.isEmpty()) {
        break;
      }
      workingMap = (HashMap) newMap.clone();
      newMap.clear();
    }

  }

  private boolean isTooDeep() {
    if (!this.tooDeep) {
      this.tooDeep = Runtime.getRuntime().freeMemory() < ((long) 1 << 10); // 1MB
      if (this.tooDeep) {
        System.runFinalization();
        System.gc();
        this.tooDeep = Runtime.getRuntime().freeMemory() < ((long) 1 << 10); // 1MB
      }
    }

    return this.tooDeep;
  }

  /*  private void showResult(MultiValueMap invocations) {
//    net.sf.refactorit.ui.module.classmodelvisitor.ClassmodelDump cd
//      = new net.sf.refactorit.ui.module.classmodelvisitor.ClassmodelDump();
//    if (this.cti.getThrowMember() instanceof BinMethod) {
//      cd.visit((BinMethod)this.cti.getThrowMember());
//    } else {
//      cd.visit((BinTryStatement)this.cti.getThrowMember());
//    }
//
//    Object obj = cti.getThrowMember();
//    if (obj instanceof BinMember) {
//      System.err.println("Throw in: " + ((BinMember)obj).getQualifiedName());
//    } else {
//      System.err.println("Throw in try");
//    }

      Iterator it = invocations.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry entry = (Map.Entry)it.next();
        String key;
//System.err.println("Key: "+entry.getKey().getClass().getName());
        if (entry.getKey() instanceof BinMethod) {
          key = ((BinMember)entry.getKey()).getQualifiedName();
          BinMethod keyMethod = (BinMethod)entry.getKey();
          List list = keyMethod.getTopMethods();
          for (int i = 0, max = list.size(); i < max; i++) {
            System.err.println("\t"+keyMethod.getQualifiedName()+" top: "+((BinMethod)list.get(i)).getQualifiedName());
          }
        } else if (entry.getKey() instanceof String) {
          key = (String)entry.getKey();
        } else {
          key = ((BinTryStatement)entry.getKey()).toString();
        }
        List list = (List)entry.getValue();
        for (int i = 0, max = list.size(); i < max; i++) {
//      System.err.println("Owner: "+list.get(i).getClass().getName());
          if (list.get(i) instanceof BinMember) {
   System.err.println("Entry: " + ((BinMember)list.get(i)).getQualifiedName()
              + " - "
              + key
              );
//        } else if (list.get(i) instanceof CallTreeIndexer.TryClause) {
//          System.err.println("Entry: " + ((CallTreeIndexer.TryClause)list.get(i)).toString()
//            + " - "
//            + key
//          );
          } else {
   System.err.println("Entry: " + ((BinTryStatement)list.get(i)).toString()
              + " - "
              + key
            );
          }
        }
      }
    }*/
}
