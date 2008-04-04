/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.dialog;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JComponent;

import java.awt.Point;
import java.awt.Window;


/**
 *
 *
 * @author Igor Malinin
 */
public class ContextWrapper implements RefactorItContext, AWTContext, Cloneable {
  private RefactorItContext context;
  private RitDialog owner;

  private Object state;
  private Point point;

  public ContextWrapper(RefactorItContext context, RitDialog owner) {
    this.context = context;
    this.owner = owner;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getProject()
   */
  public Project getProject() {
    return context.getProject();
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#open(net.sf.refactorit.classmodel.CompilationUnit)
   */
  public void open(SourceHolder src) {
    context.open(src);
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#show(net.sf.refactorit.classmodel.CompilationUnit, int, boolean)
   */
  public void show(SourceHolder src, int line, boolean mark) {
    context.show(src, line, mark);
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#reload()
   */
  public void reload() {
    context.reload();
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getWindowId()
   */
  public String getWindowId() {
    return context.getWindowId();
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#addTab(java.lang.String, javax.swing.JComponent)
   */
  public Object addTab(String title, JComponent component) {
    return context.addTab(title, component);
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#removeTab(java.lang.Object)
   */
  public void removeTab(Object category) {
    context.removeTab(category);
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#showTab(java.lang.Object)
   */
  public boolean showTab(Object tab) {
    return context.showTab(tab);
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#setPoint(java.awt.Point)
   */
  public void setPoint(Point point) {
    this.point = point;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getPoint()
   */
  public Point getPoint() {
    return point;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#setState(java.lang.Object)
   */
  public void setState(Object state) {
    this.state = state;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getState()
   */
  public Object getState() {
    return state;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy()
   */
  public RefactorItContext copy() {
    try {
      return (RefactorItContext) clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#copy(net.sf.refactorit.ui.dialog.RitDialog)
   */
  public IdeWindowContext copy(RitDialog owner) {
    ContextWrapper copy = (ContextWrapper) copy();
    copy.owner = owner;
    return copy;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#rebuildAndUpdateEnvironment()
   */
  public void rebuildAndUpdateEnvironment() {
    context.rebuildAndUpdateEnvironment();
  }

  // XXX: refactor and remove

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#postponeShowUntilNotified()
   */
  public void postponeShowUntilNotified() {
    context.postponeShowUntilNotified();
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#showPostponedShows()
   */
  public void showPostponedShows() {
    context.showPostponedShows();
  }

  /*
   * @see net.sf.refactorit.ui.dialog.SwingContext#getWindow()
   */
  public Window getWindow() {
    return owner.dialog;
  }
}
