/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.ui.module.RunContext;

import com.borland.jbuilder.node.java.JavaStructure;
import com.borland.jbuilder.node.java.JavaStructureElement;
import com.borland.jbuilder.node.java.JavaStructureNode;
import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.ide.Browser;


/**
 * @author Anton Safonov
 */
public class StructureActionProvider implements JavaStructure.
    ContextActionProvider {

  public Action getContextAction(
      final Browser browser, final JavaStructureElement aelement[]) {
    MenuBuilder builder = MenuBuilder.createEmptyRefactorITMenu('R');

    RunContext context = getStructureRunContext(browser, aelement);
    builder.buildContextMenu(context);

    return (ActionGroup) builder.getMenu();
  }

  private RunContext getStructureRunContext(
      Browser browser, JavaStructureElement[] aelement) {
    final JTree tree = RefactorItActions
        .getStructureTree(browser.getStructureView());

// TODO aelement can be used also!!! Much easier!
//System.err.println("elems: " + Arrays.asList(aelement));

    int typeCode = RunContext.JAVA_CONTEXT;

    Class[] clss = null;
    if (tree != null) {
      TreePath[] paths = tree.getSelectionPaths();
      final JavaStructureNode[] nodes
          = JavaStructureRe.getJavaStructureNodes(paths);
      if (nodes == null) {
        typeCode = RunContext.UNSUPPORTED_CONTEXT;
        return new RunContext(typeCode, (Class[])null, true);
      }

      int size = nodes.length;
      clss = new Class[size];

      for (int i = 0; i < size; i++) {
        Object obj = nodes[i].getUserObject();
        if (!(obj instanceof JavaStructureElement)) {
          typeCode = RunContext.UNSUPPORTED_CONTEXT;
          return new RunContext(typeCode, (Class[])null, true);
        }
        JavaStructureElement element = (JavaStructureElement) obj;
        Class cls = JavaStructureRe.getBinClass(element);
        if (cls == null) {
          return new RunContext(RunContext.UNSUPPORTED_CONTEXT, (Class[])null, true);
        }
        clss[i] = cls;
        //System.out.println( "Class["+i+"] = " + clss[i] );
        //System.out.println( "Node["+i+"] = " + nodes[i] );
      }

      RunContext context = new RunContext(typeCode, clss, true);

      return context;
    } else {
      System.err.println("StructureView: " + browser.getStructureView());
      System.err.println("StructureView.Component: "
          + browser.getStructureView().getStructureComponent());
//        System.err.println("structure is null, methods: "
//            + StringUtil.mergeArrayIntoString(browser.getStructureView().
//            getStructureComponent()
//            .getClass().getMethods(), "\n"));
      return null;
    }
  }

//    private static class JBStructureRefactoryAction extends JBRefactoritAction {
//      private final Browser browser;
//      private final JavaStructureNode[] nodes;
//      private final JavaStructureNode root;
//      private final JTree tree;
//
//      public JBStructureRefactoryAction(Browser browser, JavaStructureNode[] nodes, JTree tree) {
//        super("", null);
//        this.browser = browser;
//        this.nodes = nodes;
//        this.root = (JavaStructureNode) tree.getModel().
//              getRoot();
//        this.tree = tree;
//      }
//
//      public void actionPerformed(ActionEvent e) {
//        try {
//          browser.doSaveAll(false);
//        } catch (VetoException ignore) {
//        }
//
//        if(!RefactorITLock.lock() ) {
//          return;
//        }
//        //System.out.println("tonisdebug: performing synchronized action");
//        try {
//          performModuleAction();
//        } finally {
//          RefactorITLock.unlock();
//        }
//      }
//
//      // FIXME copy-pasted from JavaStructureRe - needs refactoring!!!
//      public boolean performModuleAction() {
//        RefactorItAction act=(RefactorItAction) getAction();
//        if ( ! IDEController.getInstance().ensureProject() ) {
//          return false;
//        }
//        Project project=IDEController.getInstance().getActiveProject();
//
//        int nodesSize = nodes.length;
//        Object[] bins = new Object[nodesSize];
//        for (int index = 0; index < nodesSize; index++) {
//          bins[index] = JavaStructureRe.getBinObject(nodes[index], root);
//          if (bins[index] == null) {
//            JOptionPane.showMessageDialog(browser,
//                "Can not perform refactorings on item you selected\n",
//                "Error",
//                JOptionPane.ERROR_MESSAGE);
//            return false;
//          }
//        }
//
//        boolean res = false;
//        JBContext context = new JBContext(project, browser);
//
//        try {
//          RefactorItActions.setupPointForTree(browser, tree, context);
//        } catch (Exception ex) {
//          if (Assert.enabled) {
//            ex.printStackTrace();
//          }
//        }
//
//        if (nodesSize == 1) {
//          if (bins[0] != null) {
//            res = RefactorItActionUtils.run(
//                act, context, browser, bins[0]);
//          }
//        } else {
//          if (bins != null) {
//            res = RefactorItActionUtils.run(
//                act, context, browser, bins);
//          }
//        }
//
//        if (res) {
//          act.updateEnvironment(browser, context);
//          browser.getProjectView().refreshTree();
//        } else {
//          act.raiseResultsPane(browser, context);
//        }
//
//        Browser.getActiveBrowser().dispatchEvent(
//            new WindowEvent(Browser.getActiveBrowser(),
//            WindowEvent.WINDOW_ACTIVATED));
//
//        return res;
//      }
//    }
}
