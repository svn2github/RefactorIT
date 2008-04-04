/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;


/**
 * Context of module execution for RefactorIT Browser.
 * Provides access to project and source editing area.
 *
 * @author  Igor Malinin
 */
public class BrowserContext extends TreeRefactorItContext implements AWTContext {
  private Project project;
  private JBrowserPanel browser;
  private Point point;
  private Object state;

  public BrowserContext(Project project, JBrowserPanel browser) {
    this.project = project;
    this.browser = browser;
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
    browser.open(src);
  }

  /**
   * @see RefactorItContext#show(SourceHolder, int, boolean)
   */
  public void show(SourceHolder src, int line, boolean mark) {
    browser.show(src, line, mark);
  }

  public void show(LocationAware la) {
    browser.show(la.getCompilationUnit(), la.getStartLine(), la.getStartColumn(),
        la.getEndLine(), la.getEndColumn());
  }

  public void show(SourceHolder source, ASTImpl ast) {
    browser.show(source, ast.getStartLine(), ast.getStartColumn(),
        ast.getEndLine(), ast.getEndColumn());
  }

  /**
   * @see RefactorItContext#reload()
   */
  public void reload() {
    browser.reload(); // reload source file

    // And rebuild the tree, since members could change
    browser.getTree().rebuild();
  }

  /**
   * @see RefactorItContext#addTab(String, JComponent)
   */
  public Object addTab(String title, JComponent component) {
    browser.showConsoleArea();

    return browser.getConsol().addTab(title, component);
  }

  /**
   * @see RefactorItContext#removeTab(Object)
   */
  public void removeTab(Object category) {
    browser.getConsol().removeTab((JComponent) category);
  }

  /**
   * @param tab which tab to show
   * @return false if tab is gone and it's not possible to show
   * @see RefactorItContext#showTab(Object)
   */
  public boolean showTab(Object tab) {
    Component[] comps = browser.getConsol().getTabs();
    for (int i = 0, max = comps.length; i < max; i++) {
      if (comps[i].equals(tab)) {

        // FIXME: the statement identified as 13. (showTab(..)) is not enough to make the tab
        // visible if console is not open (i.e. visible). To make the console
        // visible, we make a HACK.

        // removed it because it is not needed. createPopup() in JBrowserPanel
        // is doing it.

        //browser.showConsoleArea();

        browser.getConsol().showTab((JComponent) tab); // 13.
        browser.getConsol().requestFocus();
        return true;
      }
    }

    return false;
  }

  /**
   * @param point of module execution; needed to show popups in correct place.
   */
  public void setPoint(Point point) {
    this.point = point;
  }

  /**
   * @return point of module execution to show popups correctly.
   */
  public Point getPoint() {
    return this.point;
  }

  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @param state a data holder needed to recreate module execution state
   * on e.g. reload
   */
  public void setState(Object state) {
    this.state = state;
  }

  /**
   * Note: Every module should manage it's <i>state</i> itself.
   *
   * @return a data holder needed to recreate module execution state
   * on e.g. reload
   */
  public Object getState() {
    return this.state;
  }

  public JBrowserPanel getBrowser() {
    return browser;
  }

  /*
   * @see net.sf.refactorit.ui.dialog.AWTContext#getWindow()
   */
  public Window getWindow() {
    return SwingUtilities.windowForComponent(browser);
  }
}
