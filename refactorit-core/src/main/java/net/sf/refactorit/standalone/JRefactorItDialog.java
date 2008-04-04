/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.license.AboutDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.OptionsAction;
import net.sf.refactorit.ui.tree.PackageTreeModel;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;


/**
 * RefactorIT integrated Browser dialog.
 *
 * @author Vladislav Vislogubov
 * @author Igor Malinin
 */
public class JRefactorItDialog {
  private static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(UIResources.class);

  final RitDialog dialog;

  JBrowserPanel browser;

  private JCheckBoxMenuItem menuViewConsole;
  private JMenuItem menuViewOptions;

  /**
   * Constructor.
   */
  public JRefactorItDialog(IdeWindowContext context, Project project) {
    dialog = RitDialog.create(context);
    init(project);
  }

  /**
   */
  private void init(Project project) {
//    DialogManager.setDialogParent(this);

    Object oldIdeProject = null;
    if (IDEController.getInstance() != null) {
      if (IDEController.runningNetBeans()) {
        IDEController.browserUnderNB = true;
      }
      oldIdeProject = IDEController.getInstance().getIDEProject();
    }

    
    // does it work anymore? ;(
    
    // init controller
    IDEController standaloneController = new StandaloneController();

    standaloneController.setActiveProject(project);

    //IDEController.createFor(IDEController.STANDALONE).setActiveProject(project);

    dialog.setTitle(resLocalizedStrings.getString("frame.title"));

    GlobalOptions.loadOptions();

    Dimension dim = dialog.getMaximumSize();

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

    dialog.setSize(w, h);

    // REVISIT: location is ignored currently

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

    browser = ((StandaloneController) IDEController.getInstance()).getBrowser();
    //browser = new JBrowserPanel();
    browser.setProject(project);

    browser.getTree().rebuild(new PackageTreeModel(project, "Project"));

    dialog.getRootPane().setJMenuBar(createMenuBar());
    dialog.setContentPane(browser);
  }

  private JMenu createMenu_File() {
    //file menu
    JMenu menuFile = new JMenu(resLocalizedStrings.getString("menu.file"));
    menuFile.setMnemonic(KeyEvent.VK_F);

    JMenuItem menuFileExit = new JMenuItem("Close", KeyEvent.VK_X);
    menuFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        ActionEvent.ALT_MASK));
    menuFileExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dialog.dispose();
      }
    });

    menuFile.add(menuFileExit);

    return menuFile;
  }

  private JMenu createMenu_View() {
    JMenu menuView = new JMenu(resLocalizedStrings.getString("menu.view"));
    menuView.setMnemonic(KeyEvent.VK_V);

    menuViewConsole = new JCheckBoxMenuItem(resLocalizedStrings.getString(
        "menu.view.console"));
    menuViewConsole.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
        ActionEvent.SHIFT_MASK));

    browser.setViewConsolMenuItem(menuViewConsole);

    String cons = GlobalOptions.getOption("window.console");
    menuViewConsole.setSelected(cons != null && cons.equals("true"));

    menuView.add(menuViewConsole);

    menuView.add(new JSeparator());

    menuViewOptions = new JMenuItem(resLocalizedStrings.getString(
        "menu.view.options"));
    menuViewOptions.setMnemonic(KeyEvent.VK_I);
    menuViewOptions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        OptionsAction.runAction(
            IDEController.getInstance().createProjectContext(), browser);
      }
    });

    menuView.add(menuViewOptions);

    return menuView;
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

    helpMenu.add(new JSeparator());

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

    helpMenu.add(menuHelpAbout);

    return helpMenu;
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    menuBar.add(createMenu_File());
    menuBar.add(createMenu_View());
    //menuBar.add( Box.createHorizontalGlue() );
    menuBar.add(createMenu_Help());

    return menuBar;
  }

  public void show() {
    dialog.show();
  }
}
