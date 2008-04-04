/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.wherecaught;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.CallTreeIndexer;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * TreeTableModel for JTreeTable component
 *
 * @author  Anton Safonov
 */
public class WhereCaughtModel extends BinTreeTableModel {
  private BinTypeRef targetExc;

  private boolean tooDeep = false;

  private WhereCaughtModel(Object root) {
    super(root);
  }

  public WhereCaughtModel(Project project, BinItem target) {
    this(new BinTreeTableNode(target));

    if (target instanceof BinThrowStatement) {
      this.targetExc = ((BinThrowStatement) target)
          .getExpression().getReturnType();
      if (this.targetExc == null) {
        this.targetExc = project.getTypeRefForName(
            "java.lang.NullPointerException");
      }
    } else if (target instanceof BinMethod.Throws) {
      this.targetExc = ((BinMethod.Throws) target).getException();
    } else {
      if (Assert.enabled) {
        Assert.must(false, "Strange target: " + target);
      }
      return;
    }

    MultiValueMap invocations
        = new WhereCaughtIndexer(target).getInvocationsNet(project);

    populateTree(invocations, (BinTreeTableNode) getRoot(), target);

    ((BinTreeTableNode) getRoot()).sortAllChildren();

    invocations.clear();
    invocations = null;
  }

  private void populateTree(final MultiValueMap invocations,
      BinTreeTableNode root, Object startItem) {
    List items = null;
    if (invocations.containsKey(startItem)) {
      items = invocations.get(startItem);
    }
    if (items == null || items.size() == 0) {
      root.addChild(new WhereCaughtNode("Not used anywhere!"));
      return;
    }
    HashMap workingMap = new HashMap(128);
    workingMap.put(root, items);

    int totalItems = 0;

    HashMap newMap = new HashMap(128);
    while (true) {
      Iterator it = workingMap.entrySet().iterator();
      while (it.hasNext()) {
        final Map.Entry entry = (Map.Entry) it.next();
        final BinTreeTableNode curParent = (BinTreeTableNode) entry.getKey();
        final List curItems = (List) entry.getValue();

        if (isTooDeep()) {
          curParent.addChild(new WhereCaughtNode("Too deep"));
        } else {
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

            final BinTreeTableNode node = new WhereCaughtNode(invocation);
            totalItems++;
            curParent.addChild(node);

            if (curItem instanceof BinTryStatement.TryBlock) {
              final BinTryStatement.CatchClause[] catches
                  = ((BinTryStatement) ((BinTryStatement.TryBlock) curItem)
                  .getParent()).getCatches();
              for (int c = 0, cmax = catches.length; c < cmax; c++) {
                BinCIType catchType = catches[c].getParameter()
                    .getTypeRef().getBinCIType();
                if (this.targetExc.isDerivedFrom(catchType.getTypeRef())) {
                  // caught
                  node.addChild(new WhereCaughtNode(catches[c]));
                  totalItems++;
                  continue nextItem;
                }
              }
            }

            Object inv = getCurrentItemInvocations(invocations, curItem);
            if (inv != null) {

                newMap.put(node, inv);

            } else {
              node.addChild(new WhereCaughtNode("Falls through!"));
              totalItems++;
            }
          }
        }
      }

      if (isTooDeep() || totalItems >= 5000) {
        it = newMap.keySet().iterator();
        while (it.hasNext()) {
          final BinTreeTableNode node = (BinTreeTableNode) it.next();
          node.addChild(new WhereCaughtNode("Too deep"));
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

//  private HashSet s = new HashSet();
  private Object getCurrentItemInvocations(MultiValueMap invoctns, Object curItem) {
    //if curItem is BinInitializer, so lets find all  constructors invocations
    if(curItem instanceof BinInitializer) {
      if(!((BinInitializer) curItem).isStatic()) {
         BinCIType type = ((BinInitializer) curItem).getOwner().getBinCIType();
         if(type instanceof BinClass) {

           BinConstructor[] c = ((BinClass) type).getConstructors();
           ArrayList list = new ArrayList();
           for(int i=0; i < c.length; i++) {
             if(invoctns.containsKey(c[i])) {
               list.addAll(invoctns.get(c[i]));
             }
           }
           return list;

         }
      }
    }


    return invoctns.get(curItem);

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
}
