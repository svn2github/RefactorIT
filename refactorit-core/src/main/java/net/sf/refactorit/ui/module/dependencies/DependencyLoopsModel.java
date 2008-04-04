/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.dependency.GLGraphTraverser;
import net.sf.refactorit.ui.graph.BinNode;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




/**

 */
public class DependencyLoopsModel extends BinTreeTableModel {

  private HashMap itemNodes = new HashMap();
  private int loopCount = 0;

  /**
   *
   * @param project
   * @param loops - WARNING! Must provide a copy of original loop array,
   * due to it inner modifications!
   * @param target
   */
  public DependencyLoopsModel(Project project, List loops, BinNode target) {
    super(new DependencyLoopsNode("Dependency Cycles"));
    List mapLoops = prepareMapLoops(loops);
    //GLGraphTraverser.printLoops(loops);

    populateTree((BinTreeTableNode) getRoot(),
        (target != null)?target.getBin():null, mapLoops);
    ((BinTreeTableNode) getRoot()).sortAllChildren();
  }

  private List prepareMapLoops(List loops) {
    List mapLoops = new ArrayList();
    for(int i=0; i<loops.size(); i++) {
      List loop = (List)loops.get(i);
      // unnecessary check, loop must contain >1 elements
      if(loop.size()>1) {
        HashMap mapLoop = new HashMap();
        BinNode previous = (BinNode)loop.get(loop.size()-1);
        for(int k=0; k<loop.size(); k++) {
          BinNode next = (BinNode)loop.get(k);
          mapLoop.put(previous.getBin(), next.getBin());
          previous = next;
        }
        mapLoops.add(mapLoop);
      }
    }

    return mapLoops;
  }


  private void populateTree(BinTreeTableNode parent, BinItem target,
      List allLoops) {

    while (!allLoops.isEmpty()) {
      //System.err.println(allLoops.size());
      if(target == null) {
        target = getMostFrequentItem(allLoops);
      }

      DependencyLoopsNode loopRoot = new DependencyLoopsNode(target);
      itemNodes.put(target,loopRoot);
      loopRoot.setSecondaryText("Loop start");
      parent.addChild(loopRoot);

      List loopsContain = new ArrayList();
      for(int i=0; i<allLoops.size(); i++){
        Map map = (Map)allLoops.get(i);
        if(map.containsKey(target)) {
          loopsContain.add(map);
        }
      }
      List remove = new ArrayList(loopsContain);

      MultiValueMap currentChildren = new MultiValueMap();
      currentChildren.putAll(loopRoot, loopsContain);

      while(!currentChildren.isEmpty()){
        MultiValueMap nextLevelNodes = new MultiValueMap();

        for(Iterator it=currentChildren.keySet().iterator(); it.hasNext();) {
          DependencyLoopsNode currRoot = (DependencyLoopsNode)it.next();
          List loops = currentChildren.get(currRoot);

          Object bin = currRoot.getBin();
          itemNodes.clear();
          for(int j=0; j<loops.size(); j++){
            Map loopMap = (Map)loops.get(j);
            Object binTo = loopMap.get(bin);
            if(binTo != null){
              if(binTo.equals(target)) {
                DependencyLoopsNode loopEnd = new DependencyLoopsNode(target);
                loopEnd.setSecondaryText("Loop end");
                loopCount++;
                currRoot.addChild(loopEnd);
              } else {
                DependencyLoopsNode dlNode = getBinItemNode(binTo);
                if(!dlNode.getChildren().contains(currRoot)){
                  currRoot.addChild(dlNode);
                  nextLevelNodes.put(dlNode, loopMap);
                }
              }
            }
          }
        }

        currentChildren.clear();
        currentChildren = nextLevelNodes;
        itemNodes.clear();
      }

      allLoops.removeAll(remove);
      target = null;
    }

    String secondary = "("+loopCount+")";
    if(loopCount>=GLGraphTraverser.MAX_LOOPS) {
      secondary = secondary + " too many cycles, interrupted...";
    }
    ((BinTreeTableNode)getRoot()).setSecondaryText(secondary);
  }

  private DependencyLoopsNode getBinItemNode(Object item) {
    DependencyLoopsNode node = (DependencyLoopsNode)itemNodes.get(item);
    if(node == null) {
      node = new DependencyLoopsNode(item);
      itemNodes.put(item, node);
    }
    return node;
  }

  private BinItem getMostFrequentItem(List allLoops){
    BinItem mostFrequent = null;
    HashMap frequencies = new HashMap();
    int maxf = 0;

    for(int i=0; i<allLoops.size(); i++) {
      Map loop = (Map)allLoops.get(i);
      for(Iterator it = loop.keySet().iterator(); it.hasNext(); ){
        BinItem item = (BinItem) it.next();
        Integer f = (Integer) frequencies.get(item);
        if(f == null) {
          f = new Integer(0);
        }
        f = new Integer(f.intValue()+1);
        if(f.intValue()>maxf){
          mostFrequent = item;
          maxf = f.intValue();
        }
        frequencies.put(item,f); // need this?
      }
    }
    return mostFrequent;
  }
}
