/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.panel;


import net.sf.refactorit.ui.module.IdeWindowContext;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


/**
 * Is responsible for holding/deleteing/returning all the BinPanels
 * that are created. Also is responsible for restructuring the BinPanels.
 *
 * @author  Jaanek Oja
 */
public class BinPanelManager {

  // a Hashtable containing the groupKey mapped against a List of
  // BinPanels that belong to that group.
  private final Hashtable panels = new Hashtable();

  // flag that notifies is the restructuring of BinPanels takes
  // place currently.
  private boolean isRestructuring = false;

  /** Creates a new instance of BinPanelManager */
  public BinPanelManager() {
  }

  /**
   * Collapses panels with the specified key.
   *
   * All BinPanels are identified with the key. And this method
   * collapses all BinPanels with that specified key (a group of
   * BinPanels identified with that key) into one single
   * BinPanel, and that BinPanel is returned to the caller.
   *
   * @param panelKey which identifies a group of BinPanels to be collapsed
   * into one.
   * @return BinPanel, returns the BinPanel into which the other BinPanels
   * were collapsed to.
   */
  public BinPanel collapsePanels(BinPanelKey panelKey) {
    BinPanel targetPanel = getPanel(panelKey);
    try {
      isRestructuring = true;
      List list = getGroupList(panelKey.getGroupKey());
      if (list.size() <= 1) {
        return targetPanel;
      }

      // contains the panels we want to collapse into one panel.
      List collapseablePanels = new ArrayList();
      int thisInd = list.indexOf(targetPanel);
      // Iterate over the panels list and build the list of panels we want
      // to collapse into target panel (the one with the key - panelKey)
      for (int i = 0, max = list.size(); i < max; i++) {
        if (i != thisInd) {
          BinPanel panel = (BinPanel) list.get(i);
          collapseablePanels.add(panel);
        }
      }

      // Now, move all current panes the indovidual panels are
      // holding into target panel. And also remove the source panel.
      for (int i = 0; i < collapseablePanels.size(); i++) {
        BinPanel panel = (BinPanel) collapseablePanels.get(i);
        BinPane currentPane = panel.getCurrentPane();
        panel.movePaneTo(currentPane, targetPanel);
        this.removePanel(panel);
        //System.out.println("Removed panel!");
      }

      // clear the list and add only one into it.
      list.clear();
      list.add(targetPanel);
    } finally {
      isRestructuring = false;
    }

    return targetPanel;
  }

  /**
   * Expands a BinPanel with the specified panelKey into separate BinPanels.
   *
   * It searches for BinPanel with the specified panelKey and expands that
   * BinPanel into separate BinPanels.
   *
   * @param panelKey, which identifies the BinPanel to be expanded.
   * @return BinPanel that was identified with the specified panelKey.
   */
  public BinPanel expandPanes(BinPanelKey panelKey) {
    BinPanel targetPanel = null;

    try {
      isRestructuring = true;
      targetPanel = getPanel(panelKey);

      // contains the panes we want to expand into new panels.
      List expandablePanes = new ArrayList();
      // contains the panes that are inside the target panel.
      Iterator panelPanes = targetPanel.getAllPanes();
      // current pane we do not want to expand. It remains inside the target panel.
      BinPane currentPane = targetPanel.getCurrentPane();

      // Iterate over the target panel panes and get out the panes into expandablePanes.
      // Those are then put into new panels.
      while (panelPanes.hasNext()) {
        BinPane pane = (BinPane) panelPanes.next();
        if (pane != currentPane) {
          // remove pane from targetPanel list
          expandablePanes.add(pane);
        }
      }

      // and then add them one by one into newly created panels.
      for (int i = 0; i < expandablePanes.size(); i++) {
        BinPane paneToMove = (BinPane) expandablePanes.get(i);
        BinPanel panel = new BinPanel(
            targetPanel.getContext(), targetPanel.getName());
        targetPanel.movePaneTo(paneToMove, panel);

        this.addPanel(panel);
      }
    } finally {
      isRestructuring = false;
    }

    return targetPanel;
  }

  /**
   * Returns the panel list for the given groupKey. If the list for that
   * groupKey does not exist, it is created.
   *
   * @param groupKey identifying the group of panels
   * @return List of Binpanels which belong to that group indentified
   * with the groupKey. The List object returned can be empty, but never null
   */
  private List getGroupList(String groupKey) {
    List list = (List)this.panels.get(groupKey);
    if (list == null) {
      list = new ArrayList();
      this.panels.put(groupKey, list);
    }

    return list;
  }

  /**
   * Returns the BinPanel as specified with that "panelKey".
   *
   * @param panelKey, the identifier for BinPanel we want to get.
   * @return BinPanel as identified with the specified "panelKey".
   */
  private BinPanel getPanel(BinPanelKey panelKey) {
    List panels = getGroupList(panelKey.getGroupKey());
    //System.out.println("Got group list for panelKey "+panelKey);
    //System.out.println("Group key: "+ panelKey.getGroupKey());
    //System.out.println("Group list: "+panels);
    // lets iterate through the list.
    for (int i = 0; i < panels.size(); i++) {
      BinPanel panel = (BinPanel) panels.get(i);
      //System.out.println("panel.getKey() "+panel.getKey());
      if (panel.getKey().equals(panelKey)) {
        return panel;
      }
    }

    //System.out.println("out of getPanel()");
    // i.e. returns "null" here.
    return null;
  }

  /**
   * Adds a specified BinPanel into panels list of specified group.
   */
  void addPanel(BinPanel panel) {
    List panels = getGroupList(panel.getKey().getGroupKey());
    panels.add(panel);
    //System.out.println("Added panel into group "+panel.getKey().getGroupKey());

    // and add the panel to IDE context (into Tab usually)
    Object ideComponent = panel.getContext().addTab(panel.getName(), panel);
    panel.setIDEComponent(ideComponent);

    // this is a kinda HACK. Remove it over better alternative.
    // we show the panel after adding it to panels list. Because
    // of Bug 1137 , it didn't showed panel if console wasn't open.
    //panel.getContext().showTab(panel.getIDEComponent());
  }

  /**
   * Looks for each BinPanel in group and returns "true" if the
   * specified BinPane is in group of BinPanels as specified by
   * panelName.
   *
   * @param panelName identifies a group of BinPanels from where
   * this method looks for specified BinPane.
   * @param pane this method looks for.
   * @return boolean, returns true if the specified BinPane is in group of
   * specified BinPanels, or false if not.
   */
  public boolean isInPanelsGroup(
      IdeWindowContext context, String panelName, BinPane pane
  ) {
    String groupKey = BinPanelKey.createGroupKey(context, panelName);
    List list = getGroupList(groupKey);

    for (int i = 0; i < list.size(); i++) {
      BinPanel panel = (BinPanel) list.get(i);
      Iterator panes = panel.getAllPanes();
      while (panes.hasNext()) {
        if (pane == (BinPane) panes.next()) {
          return true;
        }
      }
      //int index = panel.panes.indexOf(pane);
      //if (index != -1) {
      //    return true;
      //}
    }

    return false;
  }

  /**
   * Returns the first BinPanel in the group as specified with the
   * panelName.
   *
   * @param panelName identifying the group into where the returned
   * BinPanel is belonging.
   * @return BinPanel if the list of BinPanels group contains at least
   * one BinPanel, otherwise it returns "null" if group is empty.
   */
  public BinPanel getFirstPanelInGroup(
      IdeWindowContext context, String panelName
  ) {
    final String groupKey = BinPanelKey.createGroupKey(context, panelName);

    final List panels = getGroupList(groupKey);
    if (panels.size() == 0) {
      return null;
    }

    return (BinPanel) panels.get(0);
  }

  /**
   * Returns the first BinPanel in the group as specified with the
   * panelName.
   *
   * @param panelName identifying the group into where the returned
   * BinPanel is belonging.
   * @return BinPanel if the list of BinPanels group contains at least
   * one BinPanel, otherwise it returns "null" if group is empty.
   */
  public BinPanel getLastPanelInGroup(
      IdeWindowContext context, String panelName
  ) {
    final String groupKey = BinPanelKey.createGroupKey(context, panelName);
    final List panels = getGroupList(groupKey);
    if (panels.size() == 0) {
      return null;
    } else {
      return (BinPanel) panels.get(panels.size() - 1);
    }
  }

  /**
   * Removes all BinPanels from the list of BinPanels.
   *
   * And also removes all BinPanels from the IDE.
   */
  public void removeAllPanels() {
    // get the values of Hashtable, i.e. The lists of groups.
    Enumeration enumer = this.panels.elements();
    while (enumer.hasMoreElements()) {
      List group = (List) enumer.nextElement();
      Object[] panels = group.toArray();
      for (int i = 0; i < panels.length; i++) {
        removePanelFromGroup(group, (BinPanel) panels[i]);
      }
      //Iterator panels = group.iterator();
      //while (panels.hasNext()) {
      //    removePanel((BinPanel)panels.next());
      //}
    }

    // clear the whole panels list
    this.panels.clear();
  }

  /**
   * Removes BinPanel from the list.
   */
  public void removePanel(BinPanel removable) {

    // get the group list and remove it from group list.
    List panels = getGroupList(removable.getKey().getGroupKey());
    removePanelFromGroup(panels, removable);
    //System.out.println("Removed panel!");
  }

  /**
   * For internal use to remove panel from group of panels.
   *
   * This function is used by removeAllPanels() and removePanel().
   *
   * @param index, the panel to be removed at that index from the internal
   * list of panels.
   */
  private void removePanelFromGroup(List group, BinPanel removable) {
    if (removable.getContext() != null) {
      // First thing is to remove it from the IDE
      removable.getContext().removeTab(removable.getIDEComponent());
    }

    // then remove it from the list. If it returns -1
    // then the internal panels list doesn't contain the specified
    // panel. It may occur when the remove() is called multiple times
    // on the same panel, like it occurred on Bug 1142 (then if () wasn't here)
    int index = group.indexOf(removable);
    if (index != -1) {
      group.remove(index);
      removable.removeAllPanes();
    }
  }

  public void moveToLast(BinPanel panel) {
    final List panels = getGroupList(panel.getKey().getGroupKey());
    if (panels.size() == 0) {
      return;
    }
    int index = panels.indexOf(panel);
    if (index < 0) {
      return;
    }
    Object tmp = panels.get(panels.size() - 1);
    panels.set(panels.size() - 1, panel);
    panels.set(index, tmp);
  }
}
