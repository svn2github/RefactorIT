/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.jbuilder.node.JavaFileNode;
import com.borland.jbuilder.node.java.JavaStructure;
import com.borland.primetime.node.DuplicateNodeException;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.vfs.Url;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;

import java.lang.reflect.Method;


/**
 * Extends standard java file node to return customized structure view.
 *
 * @author Igor Malinin
 * @author Vlad Vislogubov
 * @author risto
 * @author Anton Safonov
 */
public class JavaFileNodeRe extends JavaFileNode {
  static Icon refactoritIcon = ResourceUtil
      .getIcon(UIResources.class, "RefactorIt.gif");

  private static boolean newStructureViewPopupApi = false;

  // TODO use it for proxying StructureView methods when we override e.g.
  // Productivity! node by registering our node
  private static Class delegateFileNodeClass = null;

  private static boolean productivityCompatibilityMode = GlobalOptions
      .getOptionAsBoolean(GlobalOptions.JB_PRODUCTIVITY_COMP_MODE, false);

  public static boolean isNewStructureViewPopupApi() {
    return newStructureViewPopupApi;
  }

  public JavaFileNodeRe(Project project, Node parent,
      Url url) throws DuplicateNodeException {
    super(project, parent, url);
  }

  public Class getTextStructureClass() {
    if (newStructureViewPopupApi) {
      return super.getTextStructureClass();
    }

    return JavaStructureRe.class;
  }

  /** replaces default JavaFileNode implementation */
  public static void initOpenTool(byte major, byte minor) {
    //System.err.println("JBuilder API version: " + major + ", " + minor);

    delegateFileNodeClass = FileNode.findFileNodeClass("java", true);
    if (delegateFileNodeClass.getName().startsWith("com.borland")) {
      delegateFileNodeClass = null;
    }

    if (major >= 4 && minor >= 5) { // JB 8 and newer
      try {
        Method method = JavaStructure.class.getMethod(
            "registerContextActionProvider",
            new Class[] {
            JavaStructure.ContextActionProvider.class});
        method.invoke(JavaStructure.class, new Object[] {
          new StructureActionProvider()
        });
        newStructureViewPopupApi = true;

//        VFS.addVFSListener(
//            new VFSListener() {
//              public void bufferConflict(Buffer buffer) {
//                System.err.println("bufferConflict: " + buffer);
//              }
//
//              public void fileRenamed(Url url, Url url1) {
//
//              }
//
//              public void fileWillBeRenamed(Url url, Url url1) {
//
//              }
//
//              public void fileDeleted(Url url) {
//
//              }
//
//              public void fileCreated(Url url) {
//
//              }
//
//              public void fileWillBeDeleted(Url url) {
//
//              }
//            }
//        );

      } catch (Exception ex) {
        System.err.println("Exception " + ex);
        if (delegateFileNodeClass != null && !productivityCompatibilityMode) {
          registerOurFileNode();
        }
      }
    } else {
      if (delegateFileNodeClass == null || !productivityCompatibilityMode) {
        registerOurFileNode();
      }
    }
  }

  private static void registerOurFileNode() {
    FileNode.registerFileNodeClass(
        "java", "Java Source File (RefactorIT)", JavaFileNodeRe.class, ICON);
    try {
      com.borland.primetime.util.FileAssociation
          .create("text/x-java", "java", "DocSource");
    } catch (Throwable e) {
      // ignore - probably an old version, this class appeared in JB7
    }
  }

//  private static boolean shownProductivityWarning = false;

  public static void informAboutProductivityModeIfNeeded() {
    if (newStructureViewPopupApi) {
      return;
    }

//    if (productivityCompatibilityMode && delegateFileNodeClass == null
//        && !shownProductivityWarning) {
//      JOptionPane.showMessageDialog(
//          DialogManager.getDialogParent(),
//          "Productivity! tool not detected by RefactorIT, "
//          + "running without compatiblity mode.",
//          "RefactorIT Warning",
//          JOptionPane.WARNING_MESSAGE
//          );
//
//      shownProductivityWarning = true;
//    }

    // nobody has registered before us, so we can safely override
    if (delegateFileNodeClass == null) {
      return;
    }

    // If the user has made already chosen this, never interfere with the message
    if (productivityCompatibilityMode) {
      GlobalOptions.setOption(
          GlobalOptions.JB_PRODUCTIVITY_COMP_MODE_INFO_DISPLAYED,
          "true");
    } else if (!GlobalOptions.getOptionAsBoolean(GlobalOptions.
        JB_PRODUCTIVITY_COMP_MODE_INFO_DISPLAYED, false)) {
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(),
          "<html>RefactorIT has a Productivity! tool compatibility mode " +
          "(to turn it on, <br>go to the RefactorIT tab under the project properties dialog).<br> " +
          "When in that mode, RefactorIT and Productivity! will interfere much less with <br>" +
          "eachother, but the JBuilder's class structure view does not contain RefactorIT<br>" +
          "menus as a consequence.</html>",
          "RefactorIT Info",
          JOptionPane.INFORMATION_MESSAGE);

      GlobalOptions.setOption(GlobalOptions.
          JB_PRODUCTIVITY_COMP_MODE_INFO_DISPLAYED, "true");
    }

    GlobalOptions.save();
  }

  /** Debug method. More detailed than original one. */
  /*  public String toString() {
      String name = this.getClass().getName();
      return name.substring(name.lastIndexOf('.') + 1) + ": " + getUrl();
    }*/

}
