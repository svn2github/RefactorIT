/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.standalone.editor.JSourceArea;
import net.sf.refactorit.standalone.editor.JSourcePanel;
import net.sf.refactorit.standalone.editor.SourceDocument;
import net.sf.refactorit.ui.MemoryMonitor;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.module.RunContext;
import net.sf.refactorit.ui.tree.JClassTree;
import net.sf.refactorit.ui.tree.JPackageTree;
import net.sf.refactorit.ui.tree.PackageTreeModel;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * RefactorIT Browser panel.
 * Content of {@link JRefactorItFrame} and {@link JRefactorItDialog}.
 *
 * @author Vladislav Vislogubov
 */
public class JBrowserPanel extends JPanel implements OptionsChangeListener {
  public static JBrowserPanel lastInstance;
  {lastInstance = this;}

  JCheckBoxMenuItem viewConsol;
  JConsol consol;
  PopupManager popupManager = new PopupManager();

  JSplitPane split1;
  JSplitPane split2;

  int hdivider = 300;
  private int vdivider = 150;

  private JStatus status;

  //  private JClassTree   tree;
  private JPackageTree packages;
  //  private JSourceArea  text;
  private final JSourcePanel source = new JSourcePanel(this);

  private ActionListener viewConsolListener;
  private MouseListener popupMouseListener;

  private Project project;

  public class PopupManager {
    public JPopupMenu getPopupMenu(final Object[] bins, Point point) {
      if (bins.length == 1) {
        this.object = bins[0];
      } else {
        this.object = bins;
      }

      MenuBuilder builder = IDEController.getInstance()
          .createMenuBuilder("", (char) 0, "", false);

      // FIXME: could be JSP context etc!

      StandaloneRunContext ctx = new StandaloneRunContext(
          RunContext.JAVA_CONTEXT, object, point, true);

      builder.buildContextMenu(ctx);

      menu = (JPopupMenu) builder.getMenu();

//      if (menu == null) {
//        menu = new JPopupMenu();
//      } else {
//        // TODO: cleanup listeners of old items?
//        menu.removeAll();
//      }
//
//      List actions;
//      if (this.object instanceof Object[]) {
//        actions = ModuleManager.getActions((Object[]) this.object);
//      } else {
//        actions = ModuleManager.getActions(this.object);
//      }
//
//      for (int i = 0; i < (actions != null ? actions.size() : 0); i++) {
//        final RefactorItAction act = (RefactorItAction) actions.get(i);
//
//        JMenuItem item = new JMenuItem(act.getName());
//
//        item.setAccelerator( Shortcuts.getKeyStrokeByAction(
//            act.getKey() ) );
//
//        ActionListener curListener
//        = (ActionListener) actionListeners.get(act);
//        if (curListener == null) {
//          curListener = new AbstractStandaloneAction(act,object,point);
//          actionListeners.put(act, curListener);
//        }
//        item.addActionListener(curListener);
//
//
//        menu.add(item);
//      }
//      menu.add( OldUndoManager.getInstance(
//          new BrowserContext(project, JBrowserPanel.this),
//          JBrowserPanel.this).getMenu() );

      // Return NULL if Popup contains no items
      return (menu.getSubElements().length > 0) ? menu : null;
    }

    public JPopupMenu getPopupMenu(Object object, Point point) {
      return getPopupMenu(new Object[] {object}, point);
    }

//    private class PopupActionListener implements ActionListener {
//      public PopupActionListener(RefactorItAction action) {
//        this.action = action;
//      }
//
//      public void actionPerformed(ActionEvent evt) {
//        //System.setOut(getConsol().allocateTab(act.getName(), getConsolListener()));
//
//        if (!viewConsol.isSelected()) {
//          viewConsol.doClick();
//        }
//
//        try {
//          BrowserContext context
//            = new BrowserContext(project, JBrowserPanel.this);
//
//          context.setPoint(point);
//
//          if (project.isParsingCanceledLastTime()) {
//            if (!IDEController.getInstance().ensureProject()) {
//              return;
//            }
//          }
//
//          if (project.hasCriticalUserErrors()) {
//            DialogManager.getInstance().showCriticalError(DialogManager.getDialogParent(), project);
//            if(project.someErrorsCausedByAssertMode()) {
//              DialogManager.getInstance().showAssertModeWarning(DialogManager.getDialogParent());
//            }
//          } else if ( object != null ) {
//            if(project.someErrorsCausedByAssertMode()){
//              DialogManager.getInstance().showAssertModeWarning(DialogManager.getDialogParent());
//            }
//
//            if (object instanceof BinItemReference) {
//              object = ((BinItemReference) object).findBinObject(project);
//              if ( object == null ) {
//                JOptionPane.showMessageDialog(DialogManager.getDialogParent(),
//                "There is no object in BinItemReference repository",
//                "No or Error BinItemReference",
//                JOptionPane.ERROR_MESSAGE);
//                return;
//              }
//            }
//
//            if (RefactorItActionUtils
//                  .run(action, context, JBrowserPanel.this, object)
//            ) {
//              action.updateEnvironment(JBrowserPanel.this, context);
//            } else {
//              action.raiseResultsPane(JBrowserPanel.this, context);
//            }
//          } else {
//            JOptionPane.showMessageDialog(DialogManager.getDialogParent(),
//                "No units were found to perform refactoring operation for",
//                "No or Error selection Unit", JOptionPane.ERROR_MESSAGE);
//            return;
//          }
//
//          object = BinItemReference.create(object);
//        } catch (Exception ex) {
//          JErrorDialog err = new JErrorDialog(window, "Error");
//          err.setException(ex);
//          err.show();
//        }
//
//        System.setOut(getConsol().allocateTab(
//        JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
//        getConsolListener()));
//      }
//
//      private RefactorItAction action; // possible memory leak here?
//    }

    private Object object = null; // an object to show popup for
    private JPopupMenu menu = null; // to reuse
//    private Map actionListeners = new HashMap();
  }


  /**
   * JBrowserPanel constructor comment.
   */
  public JBrowserPanel() {
    super(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();

    final JPanel centerPanel = new JPanel(new GridLayout());
    constr.fill = GridBagConstraints.BOTH;
    constr.gridx = 0;
    constr.gridy = 0;
    constr.gridwidth = 2;
    constr.weightx = 1.0;
    constr.weighty = 1.0;
    add(centerPanel, constr);

    status = createStatusBar();

    constr.gridwidth = 1;
    constr.gridy = 1;
    constr.weightx = 1.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(2, 0, 0, 0);
    add(status, constr);

    int insetRight;
    if(RuntimePlatform.isMacOsX()) {
      // This way the grow box will not be displayed on top of the status bar (RIM-210)
      insetRight = 20;
    } else {
      insetRight = 0;
    }

    constr.gridx = 1;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(2, 0, 0, insetRight);
    add(new MemoryMonitor(), constr);

    //center
    BrowserContext context = new BrowserContext(project, this);
    packages = new JPackageTree(context);
    final JClassTree tree = getTree();
    tree.setPackageTree(packages);
    tree.setRootVisible(true);
    tree.setShowsRootHandles(false);
    tree.addKeyListener(new BrowserTreeKeyListener(tree, this));
    tree.addMouseListener(new BrowserTreeMouseListener(tree, this));

    //source = new JSourcePanel( this );
    JSourceArea text = source.getSourceArea();
    text.optionsChanged();

    split1 = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        packages, source);

    String div;

    div = GlobalOptions.getOption("window.vdivider");
    if (div != null) {
      vdivider = Integer.parseInt(div);
    }

    split1.setDividerLocation(vdivider);

    div = GlobalOptions.getOption("window.hdivider");
    if (div != null) {
      hdivider = Integer.parseInt(div);
    }

    final JPanel treeAndTextPanel = new JPanel(new GridLayout());
    treeAndTextPanel.add(split1);

    final JMenuItem consolPopup1 =
        new JMenuItem(JRefactorItFrame.resLocalizedStrings.getString(
        "popup.clear"));
    final JMenuItem consolPopup2 =
        new JMenuItem(JRefactorItFrame.resLocalizedStrings.getString(
        "popup.close"));
    consolPopup2.setAccelerator(KeyStroke.getKeyStroke("ESCAPE"));

    final JPopupMenu consolPopup = new JPopupMenu();
    popupMouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        handlePopup(e);
      }

      public void mousePressed(MouseEvent e) {
        handlePopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        handlePopup(e);
      }

      public void handlePopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
          consolPopup1.setEnabled(consol.isClearable());
          consolPopup2.setEnabled(consol.isCloseable());
          consolPopup.show(e.getComponent(), e.getX(), e.getY() - 61); //consolPopup.getHeight());
          consolPopup.requestFocus();
        }
      }
    };

    consol = new JConsol(popupMouseListener);

    split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeAndTextPanel, consol);
    split2.setDividerLocation(hdivider);
    split2.setDividerSize(10);
    split2.setOneTouchExpandable(true);

    consolPopup1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        consol.clear();
      }
    });

    consolPopup2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (consol.getTabCount() > 1) {
          consol.close();
        } else {
          hdivider = split2.getDividerLocation();
          GlobalOptions.setOption("window.hdivider", Integer.toString(hdivider));
          centerPanel.removeAll();
          centerPanel.add(treeAndTextPanel);
          centerPanel.revalidate();
          if (viewConsol != null) {
            viewConsol.setSelected(false);
          }
        }
      }
    });
    JMenuItem consolPopup3 =
        new JMenuItem(JRefactorItFrame.resLocalizedStrings.getString(
        "popup.close.all"));
    consolPopup3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
        ActionEvent.SHIFT_MASK));
    consolPopup3.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hdivider = split2.getDividerLocation();
        GlobalOptions.setOption("window.hdivider", Integer.toString(hdivider));
        centerPanel.removeAll();
        centerPanel.add(treeAndTextPanel);
        centerPanel.revalidate();
        consol.closeAll();
        if (viewConsol != null) {
          viewConsol.setSelected(false);
        }
      }
    });
    consolPopup.add(consolPopup1);
    consolPopup.add(consolPopup2);
    consolPopup.add(consolPopup3);

    String cons = GlobalOptions.getOption("window.console");
    if ((cons != null) && (cons.equals("true"))) {
      try {
        Class[] args = {Double.TYPE};
        java.lang.reflect.Method method =
            split2.getClass().getMethod("setResizeWeight", args);
        Object[] params = {new Double(1.0)};
        method.invoke(split2, params);
      } catch (Exception e) {
      }

      centerPanel.add(split2);
    } else {
      centerPanel.add(treeAndTextPanel);
    }

    viewConsolListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        centerPanel.removeAll();
        if (viewConsol.isSelected()) {
          //CLOSE ALL
          consol.closeAll();

          split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeAndTextPanel,
              consol);
          split2.setOneTouchExpandable(true);
          split2.setDividerLocation(hdivider);
          split2.setDividerSize(10);

          try {
            Class[] args = {Double.TYPE};
            java.lang.reflect.Method method =
                split2.getClass().getMethod("setResizeWeight", args);
            Object[] params = {new Double(1.0)};
            method.invoke(split2, params);
          } catch (Exception ignore) {
          }

          centerPanel.add(split2);
        } else {
          hdivider = split2.getDividerLocation();
          GlobalOptions.setOption("window.hdivider", Integer.toString(hdivider));
          centerPanel.add(treeAndTextPanel);
          tree.requestFocus();
        }
        centerPanel.revalidate();
      }
    };

    packages.getHeaderLabel().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() != 2) {
          return;
        }

        if (split1.getDividerLocation() <
            split1.getMaximumDividerLocation()) {
          split1.setDividerLocation(split1.getWidth());
        } else {
          split1.setDividerLocation(split1.getLastDividerLocation());
        }
      }
    });

    source.getHeaderLabel().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() != 2) {
          return;
        }

        if (split1.getDividerLocation() >
            split1.getMinimumDividerLocation()) {
          split1.setDividerLocation(0);
        } else {
          split1.setDividerLocation(split1.getLastDividerLocation());
        }
      }
    });
  }

  private JStatus createStatusBar() {
    JStatus status = new JStatus();
    status.setStatus(JRefactorItFrame.resLocalizedStrings.getString(
        "status.ready"));
    return status;
  }

  /**
   * Shows the console area if it is not visible.
   */
  public void showConsoleArea() {
    if (!viewConsol.isSelected()) {
      viewConsol.doClick();
    }
  }

  public MouseListener getConsolListener() {
    return popupMouseListener;
  }

  public int getHDividerLocation() {
    return viewConsol.isSelected()
        ? split2.getDividerLocation()
        : split2.getLastDividerLocation();
  }

  public int getVDividerLocation() {
    return split1.getDividerLocation();
  }

  public void setViewConsolMenuItem(JCheckBoxMenuItem viewConsol) {
    this.viewConsol = viewConsol;
    this.viewConsol.addActionListener(viewConsolListener);
  }

  public JClassTree getTree() {
    return packages.getClassTree();
  }

  public JSourceArea getSourceArea() {
    return source.getSourceArea();
  }

  public JConsol getConsol() {
    return consol;
  }

  public JStatus getStatus() {
    return status;
  }

  public PopupManager getPopupManager() {
    return this.popupManager;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    if (this.project != project) {
      getTree().getSelectionModel().clearSelection(); // kill memory leak
    }
    this.project = project;
  }

  public void open(SourceHolder sf) {
    source.setSource(sf);
  }

  public void show(SourceHolder sf, int line, boolean mark) {
    open(sf);
    if (sf == null || sf.getSource() == null) {
      return; // nothing to show anymore
    }

    final JSourceArea text = getSourceArea();
    SourceDocument src = text.getSourceDocument();

    if (line != 0) {
      --line;
    }

    Element root = src.getDefaultRootElement();
    if (root != null) {
      int max = root.getElementCount() - 1;

      Element paragraph;
      int l;

      // FIXME: it is still wrong when the visible area is too small
      l = line + 10;
      if (l > max) {
        l = max;

      }
      paragraph = root.getElement(l);
      int end = paragraph.getStartOffset();
      text.select(end, end);

      l = line - 5;
      if (l < 0) {
        l = 0;

      }
      paragraph = root.getElement(l);
      if (paragraph != null) {
        final int pos = paragraph.getStartOffset();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            text.select(pos, pos);
          }
        });
      } else {
        System.err.println("Paragraph is null for line: " + l + " in "
            + sf);
        /*new Exception("Paragraph is null for line: " + l)
            .printStackTrace(System.err);*/
        mark = false;
      }

      paragraph = root.getElement(line);
      int start = 0;
      if (paragraph != null) {
        start = paragraph.getStartOffset();
        end = paragraph.getEndOffset();
      } else {
        System.err.println("Paragraph is null for line: " + line + " in "
            + sf);
        /*        new Exception("Paragraph is null for line: " + line)
                    .printStackTrace(System.err);*/
        mark = false;
      }

      try {
        text.getHighlighter().removeAllHighlights();

        if (mark) {
          Color color = Color.decode(GlobalOptions.getOption("source.highlight.color"));
          text.getHighlighter().addHighlight(start, end,
              new DefaultHighlighter.DefaultHighlightPainter(color));
        }
      } catch (BadLocationException ignore) {}
    }
  }

  public void show(SourceHolder source, int startLine, int startColumn,
      int endLine, int endColumn) {
    open(source);
    if (source == null || source.getSource() == null) {
      return; // nothing to show anymore
    }

    boolean mark = true;
    final JSourceArea text = getSourceArea();
    SourceDocument src = text.getSourceDocument();

    int line = startLine - 1;
    Element root = src.getDefaultRootElement();
    if (root != null) {
      int max = root.getElementCount() - 1;

      Element paragraph;
      int l;

      // FIXME: it is still wrong when the visible area is too small
      l = line + 10;
      if (l > max) {
        l = max;

      }
      paragraph = root.getElement(l);
      int end = paragraph.getStartOffset();
      text.select(end, end);

      l = line - 5;
      if (l < 0) {
        l = 0;

      }
      paragraph = root.getElement(l);
      if (paragraph != null) {
        final int pos = paragraph.getStartOffset();
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            text.select(pos, pos);
          }
        });
      } else {
        /*new Exception("Paragraph is null for line: " + l)
            .printStackTrace(System.err);*/
        mark = false;
      }
      //System.out.println( "line1 = " + line + ", but max = " + max );
      //System.out.println( "line2 = " + (endLine - 1) );
      int line1 = (line < 0) ? 0 : line;
      int line2 = ((endLine - 1) < 0) ? 0 : (endLine - 1);
      Element paragraph1 = root.getElement(line1);
      Element paragraph2 = root.getElement(line2);
      int start = 0;
      if (paragraph1 != null && paragraph2 != null) {
        start = paragraph1.getStartOffset() + startColumn - 1;
        end = paragraph2.getStartOffset() + endColumn - 1;
      } else {
        /*        new Exception("Paragraph is null for line: " + line)
                    .printStackTrace(System.err);*/
        mark = false;
      }

      try {
        text.getHighlighter().removeAllHighlights();

        if (mark) {
          Color color = Color.lightGray;
          text.getHighlighter().addHighlight(start, end,
              new DefaultHighlighter.DefaultHighlightPainter(color));
        }
      } catch (BadLocationException ignore) {}
    }
  }

  public void reload() {
    JSourceArea area = source.getSourceArea();
    SourceDocument src = area.getSourceDocument();

    Element root = src.getDefaultRootElement();
    Highlighter.Highlight[] high = area.getHighlighter().getHighlights();
    int line = 1;
    boolean mark = false;

    if (high.length > 0) {
      mark = true;
      line = root.getElementIndex(high[0].getStartOffset());
    } else {
      line = root.getElementIndex(area.getCaretPosition());
    }

    // FIXME: This block needs refactoring

    SourceHolder srcFile = src.getSource();
    if (srcFile != null && srcFile.getSource() != null) {
      String absolutePath = srcFile.getSource().getRelativePath();
      java.util.List compilationUnitList = project.getCompilationUnits();

      srcFile = null;
      for (int i = 0; i < compilationUnitList.size(); ++i) {
        CompilationUnit test = (CompilationUnit) compilationUnitList.get(i);
        if (absolutePath.equals(test.getSource().getRelativePath())) {
          srcFile = test;
          break;
        }
      }
    }

    show(srcFile, line + 1, mark);
  }

  public void optionChanged(String key, String newValue) {
    if (key.startsWith("tree.")) {
      getTree().optionsChanged();
    } else if (key.startsWith("source.")) {
      getSourceArea().optionsChanged();
    }
  }

  public void optionsChanged() {
    getTree().optionsChanged();
    getSourceArea().optionsChanged();
  }

  public void rebuildTree() {
    getTree().rebuild(new PackageTreeModel(
        project, "Project"));

  }
}
