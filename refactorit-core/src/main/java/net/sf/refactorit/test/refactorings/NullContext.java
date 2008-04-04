/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.JComponent;

import java.awt.Point;
import java.awt.Window;


/**
 * Context of module execution for tests. Provides access to project.
 *
 * @author Anton Safonov
 */
public class NullContext extends TreeRefactorItContext implements AWTContext {
  private Project project;

  public NullContext(Project project) {
    this.project = project;
  }

  /**
   * @see RefactorItContext#getProject()
   */
  public Project getProject() {
    return project;
  }

  /**
   * @see RefactorItContext#open(SourceHolder)
   */
  public void open(SourceHolder src) {
  }

  /**
   * @see RefactorItContext#show(SourceHolder, int, boolean)
   */
  public void show(SourceHolder src, int line, boolean mark) {
  }

  /**
   * @see RefactorItContext#reload()
   */
  public void reload() {
  }

  /**
   * @see RefactorItContext#addTab(String, JComponent)
   */
  public Object addTab(String title, JComponent component) {
    return null;
  }

  /**
   * @see RefactorItContext#removeTab(Object)
   */
  public void removeTab(Object category) {
  }

  /**
   * @param tab which tab to show
   * @return false if tab is gone and it's not possible to show
   * @see RefactorItContext#showTab(Object)
   */
  public boolean showTab(Object tab) {
    return false;
  }

  /**
   * @param point of module execution; needed to show popups in correct place.
   */
  public void setPoint(Point point) {
  }

  /**
   * @return point of module execution to show popups correctly.
   */
  public Point getPoint() {
    return null;
  }

  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @param state a data holder needed to recreate module execution state
   * on e.g. reload
   */
  public void setState(Object state) {
  }

  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @return a data holder needed to recreate module execution state
   * on e.g. reload
   */
  public Object getState() {
    return null;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy()
   */
  public RefactorItContext copy() {
    return new NullContext(getProject());
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy(net.sf.refactorit.ui.dialog.RitDialog)
   */
  public IdeWindowContext copy(RitDialog owner) {
    return copy();
  }

  public void rebuildAndUpdateEnvironment() {
  }

  /*
   * @see net.sf.refactorit.ui.dialog.AWTContext#getWindow()
   */
  public Window getWindow() {
    return null;
  }
}
