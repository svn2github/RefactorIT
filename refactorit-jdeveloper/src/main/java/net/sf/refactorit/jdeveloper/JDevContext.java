/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.jdeveloper.vfs.JDevSource;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.vfs.Source;
import oracle.ide.Ide;
import oracle.ide.model.Node;
import oracle.ide.model.NodeFactory;
import oracle.ide.net.URLFactory;

import java.net.URL;

import java.awt.Point;
import java.awt.Window;
import javax.swing.JComponent;


/**
 *
 * @author  Tanel
 */
public class JDevContext extends TreeRefactorItContext implements AWTContext {
  Window  window;
  Project project;
  Point point;

  private Object state;

  /** Creates new JDevContext */
  public JDevContext(Project project) {
    this.project = project;
    window = AbstractionUtils.getMainWindow();
  }

  public Project getProject() {
    return project;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getWindowId()
   */
  public String getWindowId() {
    return "JDeveloper" + System.identityHashCode(window);
  }

  /**
   */
  public Object addTab(final String title, final JComponent component) {
    TabbedManager.addTab(title, component);

    // Get the titled Panel for previous Dockable
    //TitledPanel titledPanel = DockStation.getInstance().getTitledPanel(dockableWindow);

    // Create the window which holds the refactory result JComponent
    //RefactorItWindow dockableWindow2 = new RefactorItWindow ("another", null);
    //titledPanel.addClient(dockableWindow2, true);

    // return the dockable window.
    return component;
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
   * Just open a file in editor.
   */
  public void open(SourceHolder src) {
    Source source = src.getSource();
    if (source instanceof JDevSource) {
      try {
        Node node = NodeFactory.findOrCreate(URLFactory.newFileURL(source.
            getAbsolutePath()));
        AbstractionUtils.openDefaultEditorInFrame(node);
      } catch (Exception e) {
        // FIXME: report error?
        System.err.println("Cannot open file: " + e);
      }
    }
  }

  /**
   * @see RefactorItContext#showTab(Object)
   */
  public boolean showTab(Object category) {
    return TabbedManager.showTab(category);
  }

  /**
   */
  public void removeTab(Object category) {
    TabbedManager.removeTab(category);
  }

  /**
   * Open a file in editor and show specified line of
   * in file. If mark is true then highlight this
   * line where possible (might be ignored).
   */
  public void show(SourceHolder src, int line, boolean mark) {
    Source source = src.getSource();
    if (source instanceof JDevSource) {
      URL sourceUrl = URLFactory.newFileURL(source.getAbsolutePath());
      // FIXME: use our cached Project and Workspace here
      oracle.jdeveloper.runner.Source.showSourceFile(
          Ide.getActiveWorkspace(),
          Ide.getActiveProject(), sourceUrl, line, mark);
    }

  }

  /**
   * @see RefactorItContext#reload()
   */
  public void reload() {
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

  /*
   * @see net.sf.refactorit.ui.dialog.AWTContext#getWindow()
   */
  public Window getWindow() {
    return window;
  }
}
