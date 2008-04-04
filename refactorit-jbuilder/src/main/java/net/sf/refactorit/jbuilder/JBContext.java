/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import java.awt.Component;
import java.awt.event.FocusEvent;

import com.borland.primetime.editor.EditorAction;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.MessageCategory;
import com.borland.primetime.ide.MessageView;
import com.borland.primetime.node.Node;
import com.borland.primetime.util.VetoException;
import com.borland.primetime.vfs.Url;
import java.awt.event.FocusAdapter;

import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.jbuilder.vfs.JBSource;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalSource;


/**
 * Context of module execution for JBuilder.
 * Provides access to project and source editing area.
 *
 * @author  Igor Malinin
 */
public class JBContext extends TreeRefactorItContext implements AWTContext {
  final Browser browser;
  final Project project;

  private Point point;
  private Object state;

  public JBContext(Project project, Browser browser) {
    this.browser = browser;
    this.project = project;
  }

  public Project getProject() {
    return project;
  }

  public void open(SourceHolder src) {
    try {
      Source source = src.getSource();

      Url url;
      if (source instanceof JBSource) {
        url = ((JBSource) source).getUrl();
      } else if (source instanceof LocalSource) {
        url = new Url(source.getFileOrNull());
      } else {
        return;
      }

      final Node filenode = browser.getActiveProject().getNode(url);
      if (filenode != null) {
        browser.setActiveNode(filenode, true);
      }
    } catch (Exception ignore) {
    } finally {
      // just in case we lost the focus, shouldn't harm
      Browser.getActiveBrowser().dispatchEvent(
          new WindowEvent(Browser.getActiveBrowser(),
          WindowEvent.WINDOW_ACTIVATED));
    }
  }

  boolean postponeShow;

  List postponedShows = new ArrayList();

  public void postponeShowUntilNotified() {
    postponeShow = true;
  }

  public void showPostponedShows() {
    // This ctreates a nested invokeLater (because show() also invokes
    // invokeLater()); that may not be the best way to fix this, but it
    // seems to work and this is they only way I know at the moment.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        postponeShow = false;

        for (int i = 0; i < postponedShows.size(); i++) {
          ((Show) postponedShows.get(i)).invokeShowOn(JBContext.this);
        }

        postponedShows.clear();
      }
    });
  }

  static class Show {
    private SourceHolder compilationUnit;
    private int line;
    private boolean mark;

    Show(SourceHolder compilationUnit, int line, boolean mark) {
      this.compilationUnit = compilationUnit;
      this.line = line;
      this.mark = mark;
    }

    void invokeShowOn(IdeWindowContext context) {
      context.show(compilationUnit, line, mark);
    }
  }

  public void show(final SourceHolder src, final int line, final boolean mark) {
    if (postponeShow) {
      postponedShows.add(new Show(src, line, mark));
      return;
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Source source = src.getSource();
        Url url;
        if (source instanceof JBSource) {
          url = ((JBSource) source).getUrl();
        } else if (source instanceof LocalSource) {
          url = new Url(source.getFileOrNull());
        } else {
          open(src);
          EditorAction.getFocusedEditor().gotoLine(line, mark);
          return;
        }

        new PositionMessage(browser.getActiveProject(), url, line, 1, 0, "", "")
            .displayResult(browser, false);
      }
    });
  }

  public void reload() {
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getWindowId()
   */
  public String getWindowId() {
    return "JBuilder" + System.identityHashCode(browser);
  }

  // FIXME: uses an ugly hack through RefactorITMessageCategory to prevent
  // removeNotify() from being called each time tab is deactivated.
  public Object addTab(String title, JComponent component) {
    RefactorItMessageCategory category = new RefactorItMessageCategory(title,
        component);

    MessageView view = browser.getMessageView();

    // HACK: We should have an "icon" parameter instead (which may be null, for example).
    // (If we can not pass an Icon instance to this method then at least let's pass in something like 'int iconType'.)
    if (title.equals("Errors")) {
      category.setIcon(ResourceUtil.getIcon(JBContext.class, "error.gif"));
    }

    view.addCustomTab(category, category.getPanel());
    view.showTab(category);

    return category;
  }

  public boolean showTab(Object category) {
    MessageView view = browser.getMessageView();

    MessageCategory[] cats = view.getTabs();
    for (int i = 0, max = cats.length; i < max; i++) {
      if (cats[i].equals(category)) {
        view.showTab((MessageCategory) category);

        // FIXME: Not sure if this view.requestFocus() call here is neccessary, but it is
        // causing focus problems (read-only source area etc). It's commented out because of that.

        // view.requestFocus();
        return true;
      }
    }

    return false;
  }

  public void removeTab(Object category) {
    MessageView view = browser.getMessageView();
    try {
      view.removeTab((MessageCategory) category, false);
    } catch (VetoException ignore) {
    }
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
   *  MessageCategory wrapper.
   */
  static class RefactorItMessageCategory extends MessageCategory {
    JComponent binPanel;

    /**
     * NOTE: The FakePanel was meant to hide removeNotify() from its children
     * (so that removeNotify() would not get called each time when a tab was deactivated),
     * so it was meant to fix some bug (that we don't remember anymore).
     *
     * But, the very disabling of removeNotify() causes the IllegalStateException when closing JB
     * ("Can't dispose InputContext while it's active", but this happened only if
     * there was a result pane opened). Now I've commented out
     * some code here; if something goes wrong then perhaps the comments should be removed
     * (but then another solution must be found for the JB closing problem).
     */
    class FakePanel extends JPanel {
      public FakePanel() {
        super(new BorderLayout());
      }

      /*public void removeNotify() {
        // do nothing
             }*/

      public void fakeRemoveNotify() {
        /*for (int i = 0; i < this.getComponentCount(); i++) {
          this.getComponent(i).removeNotify();
                 }*/
      }
    }


    FakePanel panel = new FakePanel();

    public RefactorItMessageCategory(String title, JComponent component) {
      super(title);
      panel.add(component);
      binPanel = component;
    }

    public JPanel getPanel() {
      return this.panel;
    }

    public JComponent getComponent() {
      return binPanel;
    }

    public void categoryClosing() {
      // Not needed?
      // panel.fakeRemoveNotify(); // Commented because this does nothing at the moment
    }
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
    return browser;
  }
}
