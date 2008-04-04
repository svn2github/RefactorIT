/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import java.awt.Image;
import java.awt.Component;
import java.util.List;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalSource;

import org.apache.log4j.Logger;

import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;


/**
 * Context of module execution for NetBeans.
 * Provides access to project and source editing area.
 *
 * @author  Igor Malinin
 */
public class NBContext extends TreeRefactorItContext implements AWTContext {
  private static final Logger log = Logger.getLogger(NBContext.class);
  
  static Line active;

  private Window  window;
  private Project project;

  private Point point;
  private Object state;

  private static boolean wasAbleToShow;

  /**
   * Constructor.
   */
  public NBContext(Project project) {
    this.project = project;

    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        window = WindowManager.getDefault().getMainWindow();
      }
    } );
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
    if (active != null) {
      active.unmarkCurrentLine();
      active = null;
    }

    DataObject data = findDataObject(src.getSource());
    if (data != null) {
      EditorCookie cookie = (EditorCookie) data.getCookie(EditorCookie.class);
      if (cookie == null) {
        return;
      }

      cookie.open();
    }
  }

  public static int close(DataObject data) {
    int result = openedLine(data);
    if (result == 0) {
      return result;
    }

    getEditorCookie(data).close();

    return result;
  }

  public static EditorCookie getEditorCookie(DataObject data) {
    return (EditorCookie) data.getCookie(EditorCookie.class);
  }

  public static int openedLine(DataObject data) {
    if (data == null) {
      return 0;
    }

    EditorCookie cookie = getEditorCookie(data);
    if (cookie == null) {
      return 0;
    }

    JEditorPane[] panes = cookie.getOpenedPanes();
    if (panes == null || panes.length == 0) {
      return 0;
    }

    StyledDocument doc = cookie.getDocument();
    int line = NbDocument.findLineNumber(doc, panes[0].getCaretPosition()) + 1;

    return line;
  }

  /**
   * @see RefactorItContext#show(SourceHolder, int, boolean)
   */
  public void show(SourceHolder src, int line, boolean mark) {
    show(src, line, 1, mark);
  }
  
  public void show(SourceHolder src, int line, int column, boolean mark) {
    show(findDataObject(src.getSource()), line, column, mark);
    if (!wasAbleToShow) {
      log.warn(
          "RefactorIT Error 09375036 -- PLEASE REPORT to support@refactorit.com");
    }
  }
  
  public static void show(DataObject data, int line, boolean mark) {
    show(data, line, 1, mark);
  }

  public static void show(final DataObject data, final int line, final int column, final boolean mark) {
    // We had exceptions in NB log telling us that we should should invoke this from AWT thread.
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        showInCurrentThread(data, line, column, mark);
      }
    });
  }

  private static void showInCurrentThread(final DataObject data, final int line, final int column, final boolean mark) {
    if (line == 0) {
      return;
    }

    if (active != null) {
      active.unmarkCurrentLine();
      active = null;
    }

    if (data != null) {
      LineCookie cookie = (LineCookie) data.getCookie(LineCookie.class);
      if (cookie == null) {
        return;
      }

      final Line highlight = cookie.getLineSet().getOriginal(line - 1);
      highlight.show(Line.SHOW_GOTO, column);

      if (mark) {
        boolean highlightRow = false;
        EditorCookie ecookie = (EditorCookie) data.getCookie(EditorCookie.class);
        if (ecookie != null) {
          JEditorPane openPanes[] = ecookie.getOpenedPanes();
          if (openPanes != null && openPanes.length > 0) {
            Class kitClass = Utilities.getKitClass(openPanes[0]);
            highlightRow = SettingsUtil.getBoolean(kitClass,
                ExtSettingsNames.HIGHLIGHT_CARET_ROW,
                ExtSettingsDefaults.defaultHighlightCaretRow);
          }
        }

        if (!highlightRow) {
          highlight.markCurrentLine();
          active = highlight;

          Thread t = new Thread() {
            public void run() {
              try {
                Thread.sleep(5000);
                highlight.unmarkCurrentLine();
              } catch (Exception e) {}
            }
          };

          t.start();
        }
      }

      wasAbleToShow = true;
    } else {
      wasAbleToShow = false;
    }
    return;
  }

  /**
   * @see RefactorItContext#reload()
   */
  public void reload() {
  }

  private DataObject findDataObject(Source src) {
    try {
      FileObject file = null;
      if (src instanceof NBSource) {
        file = ((NBSource) src).getFileObject();
      } else if (src instanceof LocalSource) {
        file = findFileObjectForFile(src.getFileOrNull());
      }

      if (file == null) {
        return null;
      }

      return DataObject.find(file);
    } catch (DataObjectNotFoundException e) {
      return null;
    } catch (Exception ignore) {
      log.warn(ignore.getMessage(), ignore);
      return null;
    }
  }

  public static FileObject findFileObjectForFile(File file) {
    final Enumeration filesystems = Repository.getDefault().getFileSystems();

    FileObject fileObject = null;

    while (filesystems.hasMoreElements() && fileObject == null) {
      final FileSystem fileSystem = (FileSystem) filesystems.nextElement();
      if (!(fileSystem instanceof LocalFileSystem)) {
        continue;
      }
      File root = ((LocalFileSystem) fileSystem).getRootDirectory();

      String searchName = null;

      try {
        String rootPath = root.getCanonicalPath();

        searchName = file.getCanonicalPath();
        if (searchName.startsWith(rootPath)) {
          searchName = searchName.substring(rootPath.length());
          if (searchName.startsWith("/") || searchName.startsWith("\\")) {
            searchName = searchName.substring(1);
          }
        }
        searchName = searchName.replace('\\', '/');
      } catch (IOException e) {
        searchName = null;
      }

      if (searchName == null) {
        continue;
      }

      fileObject = fileSystem.findResource(searchName);
    }

    return fileObject;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItContext#getWindowId()
   */
  public String getWindowId() {
    return "NetBeans" + System.identityHashCode(window);
  }

  /**
   * @see RefactorItContext#addTab(String, JComponent)
   */
  public Object addTab(final String title, final JComponent component) {
    final Object result[] = new Object[] {null};

    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        Workspace workspace =  WindowManager.getDefault().getCurrentWorkspace();

        TabComponent tab = new TabComponent();
        tab.setName(title);
        tab.setContext(NBContext.this);

        if (title.equals("Errors")) {
          tab.setIcon(ResourceUtil.getIcon(NBContext.class, "error.gif").getImage());
        }

        Mode mode = workspace.findMode("output");
        try {
          mode.dockInto(tab);
        } catch (Exception e) {
          // ignore
        } catch (Error e) {
          // ignore
        }

        tab.setLayout(new BorderLayout());
        tab.add(component);
        tab.open(workspace);
        WindowManager.getDefault().getMainWindow().requestFocus();
        tab.requestFocus();
        component.requestFocus();
        requestActive(tab);
        tab.requestVisible();

        result[0] = tab;
      }
    } );

    return result[0];
  }

  public static final void requestActive(final TopComponent tc) {
    try { // requestActive appeared in NB 3.6
      Method requestActive
          = tc.getClass().getMethod("requestActive", new Class[0]);
      requestActive.invoke(tc, new Object[0]);
    } catch (Exception e) {
      // ignore
    } catch (Error e) {
      // ignore
    }
  }

  /**
   * @see RefactorItContext#removeTab(Object)
   */
  public void removeTab(final Object category) {
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        ((TopComponent) category).close();
      }
    } );
  }

  /**
   * @see RefactorItContext#showTab(Object)
   */
  public boolean showTab(final Object tabToShow) {
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        TopComponent tab = (TopComponent) tabToShow;
        Workspace workspace =  WindowManager.getDefault().getCurrentWorkspace();
        tab.open(workspace);
        tab.requestFocus(); // deprecated, does nothing in new NB 3.6
        NBContext.requestActive(tab);
        tab.requestVisible();
      }
    } );

    return true;

// Very strange legacy code
//    Mode mode = workspace.findMode(tab);
//
//    if (mode != null) {
//      TopComponent[] comps = mode.getTopComponents();
//      for (int i = 0, max = comps.length; i < max; i++) {
//        if (comps[i].equals(tab)) {
//          //if (comps[i] == tab) {
//          boolean result;
//          if (tab.isOpened(workspace)) {
//            tab.open(workspace);
//            tab.requestFocus();
//            //return true;
//            result = true;
//          } else {
//            tab.open(workspace);
//            tab.requestFocus();
//            // Here it causes memory leakage, since that Mode doesn't
//            // clear the table of closed tabs
//            //return false;
//            result = false;
//          }
//          //tab.open(workspace);
//          tab.setVisible(true);
//          //tab.requestVisible();
//          //tab.requestFocus();
//          //System.out.println("In showTab(): "+result);
//          return result;
//        }
//      }
//    }
  }

  // Top Component with cleanup
  public static class TabComponent extends TopComponent {

    // counts the number of TabComponents in the IDE
    private static int count = 0;
    private IdeWindowContext context;
    private static final Image IMAGE
        = DialogManager.getRefactoritIconSmall().getImage();

    void setContext(IdeWindowContext context) {
      this.context = context;
    }

    public int getPersistenceType() {
      return 2; // TopComponent.PERSISTANCE_NEVER;
    }

    protected void openNotify() {
      count++;
      if (context == null) {
        super.close();
      }
    }

    protected void closeNotify() {
      count--;
      if (context == null) {
        return;
      }

      Line line = NBContext.active;
      if (line != null) {
        line.unmarkCurrentLine();
        NBContext.active = null;
      }

      context = null;
      removeAll();

      // remove all BinPanels if the all TabComponents are
      // closed. This kind of detection is used to catch an event
      // when user explicitly presses close button on Mode window.
      if (count == 0) {
        // remove all BinPanels from Output window, because the
        // container of those components are closed (*)
        BinPanel.removeAllPanels();
      }
    }

    /** NB 4.0 calls it and warns us in its log file if this is missing */
    protected String preferredID() {
      return "RefactorItTab";
    }

    protected void componentActivated() {
      // HACK: This was part of fix for RIM-369, more specifically, this makes
      // it possible to use Ctrl-Tab in NB 4.0 to get focus back to WhereUsed window.
      // There should be a cleaner solution (like making toolbar buttons not focusable, etc),
      // but I had no time to implement that properly for everything (and I'm not sure
      // it would have worked everywhere either).

      List children = SwingUtil.getChildJComponents(this);
      if(children.size() > 0) {
        Component toActivate = findBestChildToActivate(children);
        toActivate.requestFocusInWindow();
      }
    }

    /** HACK */
    private Component findBestChildToActivate(List children) {
      for(int i = 0; i < children.size(); i++) {
        Component c = (Component) children.get(i);
        if(c instanceof BinTreeTable) {
          return c;
        }
      }

      return (Component) children.get(0);
    }

    public Image getIcon() {
      return IMAGE;
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
