/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.JHTMLDialog;
import net.sf.refactorit.ui.ProductivityGuideDialog;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.TypeChooser;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.license.AboutDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.module.ProjectOptionsAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.tree.UITreeNode;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ResourceBundle;


/**
 * RefactorIT Standalone Browser main frame.
 *
 * @author Vladislav Vislogubov
 * @author Igor Malinin
 */
public class JRefactorItFrame extends JFrame {
  static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(UIResources.class);

  private static final int RECENT_COUNT = 10;

  StandaloneController controller;

  // Menu
  private JMenuItem menuFileNew;
  private JMenuItem menuFileOpen;

  private JCheckBoxMenuItem menuViewConsole;

  /** null under Mac */
  private JMenuItem menuViewOptions;

  private JMenu menuProject;

  JMenu menuTools;

  private RefactorItProject project;
  ActionListener rebuildAction;

  public JRefactorItFrame() {
    controller = (StandaloneController) IDEController.getInstance();

    init();

    OptionsAction.addCustomOptionsTab(new StandaloneShortcuts());

//    frameInstance = this;
  }

  private void init() {
//    DialogManager.setDialogParent(this);
//
    setIconImage(ResourceUtil.getImage(UIResources.class, "RefactorIt.gif"));

    setTitle(resLocalizedStrings.getString("frame.title"));

    Dimension dim = getToolkit().getScreenSize();

    String str;

    int w = dim.width - 20;
    str = GlobalOptions.getOption("window.width");
    if (str != null) {
      w = Integer.parseInt(str);
    }

    int h = dim.height - 60;
    str = GlobalOptions.getOption("window.height");
    if (str != null) {
      h = Integer.parseInt(str);
    }

    setSize(w, h);

    int x = (dim.width - w) / 2;
    str = GlobalOptions.getOption("window.x");
    if (str != null) {
      x = Integer.parseInt(str);
    }

    int y = Math.max(0, (dim.height - h) / 2 - 15);
    str = GlobalOptions.getOption("window.y");
    if (str != null) {
      y = Integer.parseInt(str);
    }

    setLocation(x, y);

    rebuildAction = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        IDEController.getInstance().ensureProject(
            new LoadingProperties(false, false));
      }
    };

    setJMenuBar(createMenuBar());

    setContentPane(getBrowser());

    getBrowser().getTree().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        menuTools.removeAll();

        TreePath[] paths = getBrowser().getTree().getSelectionPaths();
        if (paths == null || paths.length == 0) {
          return;
        }

        Point point = SwingUtilities.convertPoint(
            getBrowser().getTree(), 0, 0, JRefactorItFrame.this);

        JPopupMenu menu = null;
        if (paths.length == 1) {
          Object node = paths[0].getLastPathComponent();

          menu = getBrowser().getPopupManager().getPopupMenu(
              ((UITreeNode) node).getBin(), point);
        } else {
          int size = paths.length;
          Object[] bins = new Object[size];
          for (int i = 0; i < size; i++) {
            bins[i] = ((UITreeNode) paths[i].getLastPathComponent()).getBin();
          }
          menu = getBrowser().getPopupManager().getPopupMenu(bins, point);
        }

        // Do not show empty PopupMenu
        if (menu != null) {
          Component[] items = menu.getComponents();
          if (items != null) {
            for (int i = 0; i < items.length; i++) {
              menuTools.add(items[i]);
            }
          }
        }

        menuTools.setEnabled(menuTools.getItemCount() > 0);
      }
    });

    menuTools.setEnabled(menuTools.getItemCount() > 0);

    addWindowListener(new WindowAdapter() {

      private boolean rebuild = false; // avoid multiple rebuild on startup.

      public void windowIconified(WindowEvent e) {
        rebuild = true;
        //System.out.println( "e: " + e );
      }

      public void windowDeiconified(WindowEvent e) {
        if (rebuild && GlobalOptions.getOption("performance.rebuild.activation")
            .equals("true")) {
          rebuildAction.actionPerformed(null);
          rebuild = false;
        }
      }

      public void windowClosing(WindowEvent e) {

        IDEController.getInstance().onIdeExit();
        exitWithSaveConfirm();
      }
    });
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    //redirect all out into the application console
    System.setOut(getBrowser().getConsol().allocateTab(
        JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
        getBrowser().getConsolListener()));

    // System.setErr( stream );
  }

  public void exitWithSaveConfirm() {
    // save project (MUST BE BEFORE SAVE PROPERTIES)
    if (!saveProject(true)) {
      return;
    }

    GlobalOptions.setOption("window.x", String.valueOf(getX()));
    GlobalOptions.setOption("window.y", String.valueOf(getY()));
    GlobalOptions.setOption("window.width", String.valueOf(getWidth()));
    GlobalOptions.setOption("window.height", String.valueOf(getHeight()));

    GlobalOptions.setOption("window.vdivider", String.valueOf(getBrowser().getVDividerLocation()));
    GlobalOptions.setOption("window.hdivider", String.valueOf(getBrowser().getHDividerLocation()));

    GlobalOptions.setOption("window.console", "" + menuViewConsole.isSelected());

    GlobalOptions.save();

    System.exit(0);
  }

  private JMenu createMenu_File() {
    JMenu menuFile = new JMenu(resLocalizedStrings.getString("menu.file"));
    menuFile.setMnemonic(KeyEvent.VK_F);

    menuFileNew = new JMenuItem(resLocalizedStrings.getString("menu.file.new"),
        KeyEvent.VK_N);
    menuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
        ActionEvent.CTRL_MASK));
    menuFileNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (getRefactorItProject() != null &&
            getRefactorItProject().isChanged()) {
          saveProject(true);

        }
        JStartupDialog startup = new JStartupDialog(JRefactorItFrame.this, false);
        startup.show();

        File proj = startup.getProject();
        if (proj != null) {
          setProject(proj);
        }
      }
    });

    menuFile.add(menuFileNew);

    menuFileOpen = new JMenuItem(resLocalizedStrings.getString("menu.file.open"),
        KeyEvent.VK_O);
    menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
        ActionEvent.CTRL_MASK));
    menuFileOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (getRefactorItProject() != null &&
            getRefactorItProject().isChanged()) {
          if (!saveProject(true)) {
            return;
          }
        } else {
          saveRecentProjectList(getRefactorItProject());
        }

        JStartupDialog startup = new JStartupDialog(JRefactorItFrame.this, true);
        startup.show();

        File proj = startup.getProject();
        if (proj != null) {
          setProject(proj);
        }
      }
    });

    menuFile.add(menuFileOpen);

    JMenuItem menuFileSave = new JMenuItem(resLocalizedStrings.getString(
        "menu.file.save"), KeyEvent.VK_S);
    menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
        ActionEvent.CTRL_MASK));
    menuFileSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // save project
        saveProject(false);
      }
    });

    menuFile.add(menuFileSave);

    if (!RuntimePlatform.isMacOsX()) {
      menuFile.add(new JSeparator());

      JMenuItem menuFileExit = new JMenuItem("Exit", KeyEvent.VK_X);
      menuFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
          ActionEvent.CTRL_MASK));
      menuFileExit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          exitWithSaveConfirm();
        }
      });

      menuFile.add(menuFileExit);
    }

    return menuFile;
  }

  public boolean openNewProject(File proj) {
    if (getRefactorItProject() != null &&
        getRefactorItProject().isChanged()) {
      if (!saveProject(true)) {
        return false;
      }
    } else {
      saveRecentProjectList(getRefactorItProject());
    }

    if (proj != null) {
      setProject(proj);
      return true;
    }

    return false;
  }

  private JMenu createMenu_View() {
    JMenu menuView = new JMenu(resLocalizedStrings.getString("menu.view"));
    menuView.setMnemonic(KeyEvent.VK_V);

    menuViewConsole = new JCheckBoxMenuItem(resLocalizedStrings.getString(
        "menu.view.console"));
    menuViewConsole.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
        ActionEvent.SHIFT_MASK));

    getBrowser().setViewConsolMenuItem(menuViewConsole);

    String cons = GlobalOptions.getOption("window.console");
    menuViewConsole.setSelected(cons != null && cons.equals("true"));

    menuView.add(menuViewConsole);

    if (!RuntimePlatform.isMacOsX()) {
      menuView.add(new JSeparator());

      menuViewOptions = createViewOptionsCommand();
      menuView.add(menuViewOptions);
    }

    return menuView;
  }

  private JMenuItem createViewOptionsCommand() {
    menuViewOptions = new JMenuItem(resLocalizedStrings.getString(
        "menu.view.options"));
    menuViewOptions.setMnemonic(KeyEvent.VK_I);
    menuViewOptions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        OptionsAction.runAction(
            IDEController.getInstance().createProjectContext(), getBrowser());
      }
    });

    return menuViewOptions;
  }

  private JMenu createMenu_Project() {
    menuProject = new JMenu(resLocalizedStrings.getString("menu.project"));
    menuProject.setMnemonic(KeyEvent.VK_P);

    // open class menu Item
    JMenuItem menuProjectGotoClass = new JMenuItem(
        resLocalizedStrings.getString("menu.project.open.class"));
    menuProjectGotoClass.setMnemonic(KeyEvent.VK_G);

    ActionListener openClassActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BrowserContext context = new BrowserContext(
            controller.getActiveProject(), getBrowser());

        TypeChooser tc = new TypeChooser(context, "", false, "");
        tc.show();

        BinTypeRef typeRef = tc.getTypeRef();
        if (typeRef == null || typeRef.isPrimitiveType()) {
          AppRegistry.getLogger(this.getClass()).debug("TypeRef==null");
          return;
        }
        BinCIType binCIType = typeRef.getBinCIType();
        RefactorItAction gotoAction = ModuleManager.getAction(binCIType.
            getClass(), GotoAction.KEY);
        Assert.must(gotoAction != null);
        RefactorItActionUtils.run(gotoAction, context, binCIType);
//        gotoAction.run(context,parent,binCIType);
      }
    };

    menuProjectGotoClass.addActionListener(openClassActionListener);

    //menuProjectRebuild.addActionListener( rebuildAction );
    menuProject.add(menuProjectGotoClass);

    // Rebuild
    JMenuItem menuProjectRebuild = new JMenuItem(resLocalizedStrings.getString(
        "menu.project.rebuild"));
    menuProjectRebuild.setMnemonic(KeyEvent.VK_R);

    menuProjectRebuild.addActionListener(rebuildAction);

    menuProject.add(menuProjectRebuild);

    // Clean Rebuild
    JMenuItem menuProjectCleanRebuild = new JMenuItem(resLocalizedStrings.
        getString("menu.project.clean.rebuild"));
    menuProjectCleanRebuild.setMnemonic(KeyEvent.VK_L);

    menuProjectCleanRebuild.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        IDEController instance = IDEController.getInstance();
        Project project = instance.getActiveProject();
        project.getProjectLoader().markProjectForCleanup();
        instance.ensureProject();
      }
    });

    menuProject.add(menuProjectCleanRebuild);

    // Properties
    JMenuItem menuProjectProperties = new JMenuItem(resLocalizedStrings.
        getString("menu.project.properties"));
    menuProjectProperties.setMnemonic(KeyEvent.VK_I);

    menuProjectProperties.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        IdeAction action = (IdeAction) IDEController.getInstance().getActionRepository()
            .getAction(ProjectOptionsAction.KEY);

        if(RefactorItActionUtils.run(action)) {
          setProjectChanged(true); // Trigger "Save-As"
        }
      }
    });

    menuProject.add(menuProjectProperties);

    menuProject.addSeparator();

    menuProject.add(createMenuItemForCrossReferencedHtmlGeneration());

    return menuProject;
  }

  private JMenuItem createMenuItemForCrossReferencedHtmlGeneration() {
    JMenuItem result = new JMenuItem(UIResources.resLocalizedStrings.getString(
        "menu.project.html"));
    result.setMnemonic(KeyEvent.VK_C);

    result.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JHTMLDialog html = new JHTMLDialog(
            controller.createProjectContext(),
            controller.getActiveProject());
        html.display();
      }
    });

    return result;
  }

  private JMenu createMenu_Tools() {
    menuTools = new JMenu(resLocalizedStrings.getString("menu.tools"));
    menuTools.setMnemonic(KeyEvent.VK_T);

    return menuTools;
  }

  private JMenu createMenu_Help() {
    JMenu helpMenu = new JMenu(resLocalizedStrings.getString("menu.help"));
    helpMenu.setMnemonic(KeyEvent.VK_H);

    JMenuItem menuHelpHelp = new JMenuItem(resLocalizedStrings.getString(
        "menu.help.help"));
    menuHelpHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
        ActionEvent.CTRL_MASK));

    menuHelpHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        HelpViewer.displayDefaultTopic();
      }
    });
    helpMenu.add(menuHelpHelp);

    JMenuItem menuHelpProductivityGuide = new JMenuItem(
        resLocalizedStrings.getString("menu.help.productivityguide"));
    menuHelpProductivityGuide.setMnemonic(KeyEvent.VK_G);
    menuHelpProductivityGuide.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        (new ProductivityGuideDialog()).show();
      }
    });

    helpMenu.add(menuHelpProductivityGuide);

    helpMenu.add(new JSeparator());
    if (!RuntimePlatform.isMacOsX()) {
      helpMenu.add(new JSeparator());
      helpMenu.add(getMenuHelpAbout());
    }

    return helpMenu;
  }

  private JMenuItem getMenuHelpAbout() {
    JMenuItem menuHelpAbout = new JMenuItem(resLocalizedStrings.getString(
        "menu.help.about"));
    menuHelpAbout.setMnemonic(KeyEvent.VK_A);

    menuHelpAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        IdeWindowContext context = IDEController
            .getInstance().createProjectContext();
        AboutDialog dialog = new AboutDialog(context);
        dialog.show();
      }
    });

    return menuHelpAbout;
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    menuBar.add(createMenu_File());
    menuBar.add(createMenu_View());
    menuBar.add(createMenu_Project());
    menuBar.add(createMenu_Tools());
    menuBar.add(createMenu_Help());

    return menuBar;
  }

  // Accessor methods
  public RefactorItProject getRefactorItProject() {
    return project;
  }

  public void setRefactorItProject(RefactorItProject project) {
    this.project = project;
    getBrowser().setProject(project != null
        ? controller.getActiveProject() : null);
  }

  /*  private boolean isProjectChanged() {
      RefactorItProject project = getRefactorItProject();

      return (project != null) ? project.isChanged() : false;
    }*/

  void setProjectChanged(boolean changed) {
    RefactorItProject project = getRefactorItProject();

    if (project != null) {
      project.setChanged(changed);
    }
  }

  void saveRecentProjectList(RefactorItProject project) {
    if (project == null) {
      return;
    }

    if (GlobalOptions.getOption("project.recent.0") != null) {
      if (project.getFile().getAbsolutePath().equals(
          GlobalOptions.getOption("project.recent.0"))) {
        //we do not need to save it because it is already first
        return;
      }
    }

    LinkedList list = new LinkedList();
    list.add(project.getFile().getAbsolutePath());

    for (int i = 0; i < RECENT_COUNT; i++) {
      String was = GlobalOptions.getOption("project.recent." + i);
      if (was == null || was.length() == 0) {
        continue;
      }

      if (was.equals(project.getFile().getAbsolutePath())) {
        continue;
      }

      list.addLast(was);
    }

    //put it back into options
    int count = list.size();
    for (int i = 0; i < count; i++) {
      GlobalOptions.setOption("project.recent." + i, ((String) list.get(i)));
    }

    GlobalOptions.save();
  }

  boolean saveProject(boolean showConfirmDialogIfProjectChanged) {
    // Waste a reference
    RefactorItProject project = getRefactorItProject();
    if (project == null || project.getFile() == null) {
      return false;
    }

    saveRecentProjectList(project);

    if (project.isChanged() || !(project.getFile().getAbsolutePath())
        .equals(GlobalOptions.getOption("project.recent.0"))) {
      int r = JOptionPane.YES_OPTION;
      if (showConfirmDialogIfProjectChanged && project.isChanged()) {
        r = RitDialog.showConfirmDialog(
            controller.createProjectContext(),
            resLocalizedStrings.getString("message.saveProjectText"),
            resLocalizedStrings.getString("message.saveProjectTitle"),
            JOptionPane.YES_NO_CANCEL_OPTION);
        if (r == JOptionPane.CANCEL_OPTION) {
          return false;
        }
      }

      if (r == JOptionPane.YES_OPTION) {
        // Check if STORE-action is required
        if (project != null && project.isChanged()) {
          try {
            project.save();
          } catch (IOException e) {
            AppRegistry.getExceptionLogger().error(e, this);
            RitDialog.showMessageDialog(
                controller.createProjectContext(),
                "Saving project failed, see log for details!",
                "Project save error", JOptionPane.OK_OPTION);
          }
        }
      }
    }

    return true;
  }

  public void setProject(File projectFile) {
    // Create new Project
    RefactorItProject rProject;
    try {
      rProject = controller.createIDEProject(projectFile);
    } catch (IOException e) {
        RitDialog.showMessageDialog(
            IDEController.getInstance().createProjectContext(),
            resLocalizedStrings.getString("message.noProject"));
        AppRegistry.getExceptionLogger().error(e, this);

        // It sucks! Bail out.
        return;
    }

    if (RuntimePlatform.isMacOsX()) {
      RuntimePlatform.MacOsX.setRefactorITCreatorCode(projectFile);
    }

    saveRecentProjectList(rProject);
    GlobalOptions.save();

    // So far, So good. Let 'em know we're alive
    setRefactorItProject(rProject);
    setTitle(rProject.getFile().getAbsolutePath());

    getBrowser().getConsol().closeAll();
    getBrowser().getConsol().clear();
    getBrowser().open(null);

    controller.ensureProject();

//    startParsing( false, true );
  }

  public JBrowserPanel getBrowser() {
    return controller.getBrowser();
  }
}
