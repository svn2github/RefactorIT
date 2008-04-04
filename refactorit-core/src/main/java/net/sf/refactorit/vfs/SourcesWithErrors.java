/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ErrorTabNode;
import net.sf.refactorit.ui.treetable.ErrorTreeTableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Aleksei sosnovski
 */
public class SourcesWithErrors {
  private static SourcesWithErrors instance;

  //how many times same errors must occur before file is prompted to be ignored
  private static final int REPEATS = 3;

  private HashMap errors;
  private HashSet constantErrors;

  private SourcesWithErrors() {
    errors = new HashMap();
    constantErrors = new HashSet();
  }

  public static SourcesWithErrors getInstance() {
    if (instance == null) {
      instance = new SourcesWithErrors();
    }

    return instance;
  }

  public void addSources(ArrayList sources) {
    constantErrors.clear();

    Set oldKeys = errors.keySet();
    HashMap newErrors = new HashMap();

    for (int i = 0; i < sources.size(); i++) {
      BinTreeTableNode newNode = (BinTreeTableNode) sources.get(i);

      if (newNode instanceof ErrorTabNode) {
        continue;
      }

      BinTreeTableNode oldNode =
          (BinTreeTableNode) findObjectInSet(oldKeys, newNode);

      if (oldNode == null) {
        newErrors.put(newNode, new Integer(1));
        continue;
      }

      ArrayList oldNodeChildren = oldNode.getAllChildren();
      ArrayList newNodeChildren = newNode.getAllChildren();

      if (areListsEqual(oldNodeChildren, newNodeChildren)) {
        Integer cnt = (Integer) errors.get(oldNode);
        cnt = new Integer(cnt.intValue() + 1);

        newErrors.put(oldNode, cnt);

        if (cnt.intValue() >= REPEATS
            && oldNode.getBin() instanceof CompilationUnit) {
          ErrorTreeTableNode node = new ErrorTreeTableNode(oldNode.getBin());

          for (Iterator iter = newNodeChildren.iterator(); iter.hasNext(); ) {
            //ErrorTabNode errorNode = (ErrorTabNode)iter.next();

            //ErrorTreeTableNode errorDialogNode =
            //    new ErrorTreeTableNode(errorNode.getBin());
            //errorDialogNode.setDisplayName(errorNode.getDisplayName());
            //errorDialogNode.setShowCheckBox(false);
            //node.addChild(errorDialogNode);
            ErrorTabNode errorNode = (ErrorTabNode) iter.next();
            errorNode.setShowCheckBox(false);
            node.addChild(errorNode);
          }

          constantErrors.add(node);
        }
      } else {
        newErrors.put(newNode, new Integer(1));
        continue;
      }
    }

    errors = newErrors;
  }

  public HashSet getConstantErrors() {
    return constantErrors;
  }

  public void clear() {
    this.errors.clear();
  }

  private Object findObjectInSet(Set set, BinTreeTableNode node1) {
    for (Iterator iter = set.iterator(); iter.hasNext(); ) {
      BinTreeTableNode node2 = (BinTreeTableNode) iter.next();
      CompilationUnit CP1 = (CompilationUnit) node1.getBin();
      CompilationUnit CP2 = (CompilationUnit) node2.getBin();

      String name1 = CP1.getSource().getAbsolutePath();
      String name2 = CP2.getSource().getAbsolutePath();

      if (name1.equals(name2)) {
        return node2;
      }
    }
    return null;
  }

  private boolean areListsEqual(List oldList, List newList) {
    HashSet oldErrors = new HashSet();
    HashSet newErrors = new HashSet();

    if (oldList.size() != newList.size()) {
      return false;
    }

    for (int i = 0, max = oldList.size(); i< max; i++ ) {
      String name1 = ((BinTreeTableNode) oldList.get(i)).getDisplayName();
      int cnt = 0;

      for (int j = 0; j< max; j++ ) {
        String name2 = ((BinTreeTableNode) newList.get(i)).getDisplayName();

        if (name1.equals(name2)) {
          cnt++;
        }
      }

      if (cnt != newList.size()) {
        return false;
      }
    }

    /*
    for (int i = 0, max = oldList.size(); i< max; i++ ) {
      oldErrors.add(((ErrorTabNode)oldList.get(i)).getBin());
      newErrors.add(((ErrorTabNode)newList.get(i)).getBin());
    }

    //probably not needed, but just in case...
    if (oldErrors.size() != newErrors.size()) {
      return false;
    }

    for (Iterator iter = newErrors.iterator(); iter.hasNext(); ) {
      int cnt = 0;

      for (Iterator iter2 = oldErrors.iterator(); iter2.hasNext(); ) {
        UserFriendlyError error1 = (UserFriendlyError) iter.next();
        UserFriendlyError error2 = (UserFriendlyError) iter.next();


        BinTreeTableNode node1 = (BinTreeTableNode) iter.next();
        BinTreeTableNode node2 = (BinTreeTableNode) iter.next();

        CompilationUnit CP1 = (CompilationUnit) node1.getBin();
        CompilationUnit CP2 = (CompilationUnit) node2.getBin();

        String name1 = CP1.getSource().getAbsolutePath();
        String name2 = CP2.getSource().getAbsolutePath();


        if (name1.equals(name2)) {
          cnt++;
        }
      }

      if (cnt != newErrors.size()) {
        return false;
      }
    }
    */

    return true;
  }
}
