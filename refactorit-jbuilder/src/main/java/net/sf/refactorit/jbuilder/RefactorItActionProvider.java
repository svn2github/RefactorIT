/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.jbuilder.jot.JotClass;
import com.borland.jbuilder.jot.JotFile;
import com.borland.jbuilder.jot.JotPackages;
import com.borland.jbuilder.jot.JotParseException;
import com.borland.jbuilder.node.JBProject;
import com.borland.jbuilder.node.JavaFileNode;
import com.borland.jbuilder.node.JspFileNode;
import com.borland.jbuilder.node.PackageNode;
import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.editor.EditorAction;
import com.borland.primetime.editor.EditorContextActionProvider;
import com.borland.primetime.editor.EditorPane;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.ContextActionProvider;
import com.borland.primetime.node.Node;

import javax.swing.Action;

import net.sf.refactorit.classmodel.*;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.FastItemFinder;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.NotFromSrcOrFromIgnoredException;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.RunContext;


/**
 * RefactorItActionProvider returns action object to be displayed in the
 * context menu when a user right-clicks on a node or set of nodes
 * in the project view.
 *
 * @author  Vladislav Vislogubov
 */
public class RefactorItActionProvider implements EditorContextActionProvider,
    ContextActionProvider {
  private static boolean getNameFromEditorView;
  private static Node node;

  //  private static ActionGroup REFACTORIT_EDITORGROUP;

  public RefactorItActionProvider() {
  }

//	/**
//	 * @return action group
//	 */
//  private static ActionGroup createEmptyRefactorItGroup() {
//      return new ActionGroup( "RefactorIT", 'a', "RefactorIT", ResourceUtil.getIcon(Main.class, "RefactorIt.gif"), true );
//  }

  public Action getContextAction(Browser browser, Node[] nodes) {
    MenuBuilder builder = MenuBuilder.createEmptyRefactorITMenu('a');

    getNameFromEditorView = false;

    RunContext ctx = getRunContext(nodes);

    builder.buildContextMenu(ctx);

    return (Action) builder.getMenu();
  }

  private RunContext getRunContext(final Node[] nodes) {
    int size = nodes.length;

    Class[] classes = new Class[size];

    int type = RunContext.JAVA_CONTEXT;

    for (int i = 0; i < size; i++) {
      Node node = nodes[0];
      Class cls = null;
      if (node instanceof JavaFileNode) {
        cls = BinClass.class;
      } else if (node instanceof PackageNode) {
        cls = BinPackage.class;
      } else if (node instanceof JspFileNode) {
        type = RunContext.JSP_CONTEXT;
      } else if (node instanceof com.borland.primetime.node.Project) {
        cls = Project.class;
      } else {
        type = RunContext.UNSUPPORTED_CONTEXT;
      }
      classes[i] = cls;
    }

    RunContext runContext = new JBRunContext(type, classes, true, nodes);
    return runContext;

//		JBAction action = new JBStructureViewRefactoryAction("unknown action", null, nodes);
//		JBRunContext jbCtx =
//			new JBRunContext(runContext, action);
//		return jbCtx;
  }

  public Action getContextAction(EditorPane editor) {
    // FIXME: do we need syncronize it?
    if (!RefactorITLock.lock()) {
      // FIXME: remove this
      //System.out.println("tonisdebug: getContextACtion lock not granted");
      return null;
    }

    try {
      // FIXME: remove this
      //System.out.println("tonisdebug: synchronized getContextAction");
      return getContextAction0(editor);
    } finally {
      RefactorITLock.unlock();
    }
  }

  private Action getContextAction0(EditorPane editor) {
    getNameFromEditorView = true;
    node = null;

    MenuBuilder menuBuilder =
        MenuBuilder.createEmptyRefactorITMenu('a', true);

    EditorPane pane = EditorAction.getFocusedEditor();
    String text = pane.getSelectedText();
    if (text != null && text.length() > 0) {
//			if (!StringUtil.isSingleWord(text)) {

      RunContext ctx = new RunContext(
          RunContext.JAVA_CONTEXT, BinSelection.class, false);

//				JBAction action=new JBStructureViewRefactoryAction("unknown action",null,null);
//				JBRunContext jbCtx = new JBRunContext(ctx,action);

      menuBuilder.buildContextMenu(ctx);
      return (Action) menuBuilder.getMenu();
//			}
    }

    Node n = getNode();

    RunContext runCtx = null;
    if (n instanceof JavaFileNode) { // parse only java files
      Class[] binClass = getBinClassUnderCursor(editor);
      //      if (binClass != null) {
      runCtx = new RunContext(RunContext.JAVA_CONTEXT, binClass, false);
      //        ActionGroup refactorItGroup = createEmptyRefactorItGroup();
      //          populateActionGroupForBinClass(refactorItGroup, binClass);
      //        return refactorItGroup;
      //      }
    } else if (n instanceof JspFileNode) {
      runCtx = new RunContext(
          RunContext.JSP_CONTEXT, new Class[] {BinLocalVariable.class}
          , false);
      //      ActionGroup refactorItGroup = createEmptyRefactorItGroup();
      //      populateJspActionGroupForBinClass(refactorItGroup,
      //          new Class[] {BinLocalVariable.class});
      //      return refactorItGroup;
    } else {
      return null;
    }

    menuBuilder.buildContextMenu(runCtx);
    return (ActionGroup) menuBuilder.getMenu();

    //    if (n instanceof JavaFileNode) {
    //      return REFACTORIT_EDITORGROUP;
    //    } else {
    //      return null;
    //    }
  }

  /**
   * Determines the bin class currently under the editor caret. Returns null if
   * current source file has parsing errors or if the bin class cannot be
   * determined or if the item under cursor does not correspond to any bin class.
   *
   * @param editor editor
   * @return bin class or null if it cannot be determined.
   */
  private Class[] getBinClassUnderCursor(final EditorPane editor) {
    try {
      String sourceContent = editor.getText();
      int caretPos = editor.getCaretPosition();
      return FastItemFinder.getCurrentBinClass(
          "",
          sourceContent,
          editor.getLineNumber(caretPos),
          editor.getColumnNumber(caretPos));
    } catch (Exception e) {
      return null;
    }
  }

//	/**
//	 * Populates the given action group with actions specific to the given
//	 * bin class.
//	 * @param group action group
//	 * @param binClass classes array
//	 */
//  private static void populateActionGroupForBinClass(ActionGroup group, Class[] binClass) {
//    Iterator iter = ModuleManager.getActionUnion( binClass ).iterator();
//    while( iter.hasNext() ) {
//      RefactorItAction act = (RefactorItAction)iter.next();
//      group.add( new RefactorItActions.RITAction(
//        act.getName(),
//        act.getKey(),
//        null
//      ) );
//    }
//  }
//
//  private static void populateJspActionGroupForBinClass(ActionGroup group, Class[] binClass) {
//   List list=ModuleManager.getActions( binClass, true);
//   ModuleManager.filterJspSupportedActions(list,binClass);
//   Iterator iter=list.iterator();
//
//   while( iter.hasNext() ) {
//     RefactorItAction act = (RefactorItAction)iter.next();
//     group.add( new RefactorItActions.RITAction(
//       act.getName(),
//       act.getKey(),
//       null
//     ) );
//   }
// }

  public int getPriority() {
    return 1;
  }

  private static Node getNode() {
    if (getNameFromEditorView) {
      return Browser.getActiveBrowser().getActiveNode();
    } else {
      return node;
    }
  }

  public static String getFullClassName() throws
      InterruptRefactorItActionSilently {
    return getFullClassName(getNode());
  }

  // TODO: replace with correct functionality
  // returns name of first class in selected source
  public static String getFullClassName(Node n) throws
      InterruptRefactorItActionSilently {
    if (n instanceof JavaFileNode) {
      JavaFileNode jfn = (JavaFileNode) n;
      JBProject project = (JBProject) n.getProject();
      JotPackages pack = project.getJotPackages();
      JotFile file = pack.getFile(jfn.getUrl());

      JotClass cls[];

      try {
        cls = file.getClasses();
      } catch (JotParseException e) {
        try {
          return project.getJomManager().getJomFileFromSourceInfo(jfn.getSourceInfo()).getPrimaryClass().getName();
        } catch (Exception e2) {
	        DialogManager.getInstance().showCustomError(
	            IDEController.getInstance().createProjectContext(),
	            "Parse error", "Fix this first: " + e.getMessage());

	        throw new InterruptRefactorItActionSilently();
        }
      }

      if (cls != null && cls.length > 0) {
        String name = jfn.getDisplayName();
        JotClass jotCls =
            file.getClass(name.substring(0, name.indexOf(".java")));
        if (jotCls != null) {
          return jotCls.getName();
        }
        return cls[0].getName();
      }
    } else if (n instanceof PackageNode) {
      return ((PackageNode) n).getName();
    } else if (n instanceof JspFileNode) {
      JspFileNode jspNode = (JspFileNode) n;
      String fileName = jspNode.getUrl().getFullName();
      String className = JspUtil.getClassName(fileName);
      //System.err.println("JSP file " + fileName + "\nclass name:" + className);
      return className;
    }

    return null;
  }

  public static Object getBinObjectFromEditor(
      EditorPane pane, Project project
  ) throws InterruptRefactorItActionSilently, NotFromSrcOrFromIgnoredException {
    RefactorItPropGroup.getActiveProject().getJotPackages().releaseAll();

    getNameFromEditorView = true;
    String name = getFullClassName();
    getNameFromEditorView = false;
    if (name == null) {
      return null;
    }

    String packName = name;
    if (name.lastIndexOf(".") > 0) {
      packName = name.substring(0, name.lastIndexOf("."));
    }

    if (BinPackage.isIgnored(packName, project)) {
      throw new NotFromSrcOrFromIgnoredException();
    }

    BinTypeRef typeRef = project.getTypeRefForName(name);
    if (typeRef == null) {
      throw new NotFromSrcOrFromIgnoredException();
    }

    CompilationUnit sf = typeRef.getBinCIType().getCompilationUnit();

    String text = pane.getSelectedText();
    if (text != null && text.length() > 0) {
      JBUsersSelection jbSelection = new JBUsersSelection();
      if (StringUtil.isSingleWord(text)) {
        int pos = pane.getSelectionStart() + 1;
        ItemByCoordinateFinder finder = new ItemByCoordinateFinder(sf);
        SourceCoordinate sc =
            new SourceCoordinate(
            pane.getLineNumber(pos),
            pane.getColumnNumber(pos));
        BinItem item = finder.findItemAt(sc);
        if (item != null) {
          jbSelection.setBinItem(item);
        }
      }

      int start = pane.getSelectionStart();
      int end = pane.getSelectionEnd();

      int startL = pane.getLineNumber(start);
      int startC = pane.getColumnNumber(start);
      int endL = pane.getLineNumber(end);
      int endC = pane.getColumnNumber(end);

      BinSelection selection = new BinSelection(
          text, startL, startC, endL, endC);
      selection.setCompilationUnit(sf);

      jbSelection.setBinSelection(selection);
        return jbSelection;
    }

    int pos = pane.getCaretPosition();
    ItemByCoordinateFinder finder = new ItemByCoordinateFinder(sf);
    SourceCoordinate sc =
        new SourceCoordinate(
        pane.getLineNumber(pos),
        pane.getColumnNumber(pos));
    BinItem item = finder.findItemAt(sc);
    if (item == null) {
      BinCIType type = ItemByNameFinder.findBinCIType(project, name);
      if (type != null) {
        return type;
      }

      DialogManager.getInstance().showWarning(
          IDEController.getInstance().createProjectContext(),
          "warning.action.unit.error");
    }

    return item;
  }

  public static Object getBinObject(Project project) throws
      InterruptRefactorItActionSilently,
      NotFromSrcOrFromIgnoredException {
    // This makes sure that JB reports right packages for files (because when the user or MoveClass changes
    // package declaration without moving the file, JB would not know automatically update it's caches)
    RefactorItPropGroup.getActiveProject().getJotPackages().releaseAll();

    //System.out.println( "RefactorItActionProvider.getBinObject: getNameFromEditorView=" + getNameFromEditorView );
    Browser browser = Browser.getActiveBrowser();
    EditorPane pane = EditorAction.getFocusedEditor();

    if (getNameFromEditorView) {
      return getBinObjectFromEditor(pane, project);
    } else {
      node = browser.getProjectView().getSelectedNode();
      //System.out.println( "RefactorItActionProvider.getBinObject: node = " + node );
      if (node == null) {
        node = browser.getActiveNode();
        //System.out.println( "RefactorItActionProvider.getBinObject: getActiveNode = " + node );
        String name = getFullClassName();

        //System.out.println( "(1)RefactorItActionProvider.getBinObject: name = " + name );
        if (name == null) {
          return null;
        }

        CompilationUnit sf =
            project.getTypeRefForName(name)
            .getBinCIType()
            .getCompilationUnit();

        String text = pane.getSelectedText();
        if (text != null && text.length() > 0) {
          if (StringUtil.isSingleWord(text)) {
            int pos = pane.getSelectionStart() + 1;
            ItemByCoordinateFinder finder = new ItemByCoordinateFinder(sf);
            SourceCoordinate sc =
                new SourceCoordinate(
                pane.getLineNumber(pos),
                pane.getColumnNumber(pos));
            BinItem item = finder.findItemAt(sc);
            if (item != null) {
              return item;
            }
          }

          DialogManager.getInstance().showWarning(
              IDEController.getInstance().createProjectContext(),
              "warning.action.selection.error");
          return null;
        }

        int pos = pane.getCaretPosition();
        //System.out.println( "RefactorItActionProvider.getBinObject: pos = " + pos );
        ItemByCoordinateFinder finder = new ItemByCoordinateFinder(sf);
        SourceCoordinate sc =
            new SourceCoordinate(
            pane.getLineNumber(pos),
            pane.getColumnNumber(pos));
        BinItem item = finder.findItemAt(sc);
        //System.out.println( "RefactorItActionProvider.getBinObject: item = " + item );
        if (item == null) {
          // item = null on first startup of JB
          BinCIType type = ItemByNameFinder.findBinCIType(project, name);
          if (type == null) {
            DialogManager.getInstance().showWarning(
                IDEController.getInstance().createProjectContext(),
                "warning.action.unit.error");
          }
          return type;
        }
        return item;
      }
    }

    return getBinItemFromProjectViewNode(project, browser, node);
  }

  public static Object[] getBinItemFromProjectViewNode(
      Project project,
      Browser browser,
      Node[] nodes) throws InterruptRefactorItActionSilently,
      NotFromSrcOrFromIgnoredException {
    Object[] result = new Object[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      result[i] = getBinItemFromProjectViewNode(project, browser, nodes[i]);
    }

    return result;
  }

  public static Object getBinItemFromProjectViewNode(
      Project project,
      Browser browser,
      Node projectViewNode)
      throws InterruptRefactorItActionSilently,
      NotFromSrcOrFromIgnoredException {
    // FIXME: remove this
    //System.out.println("tonisdebug: projectViewNode");
    if (projectViewNode instanceof com.borland.primetime.node.Project) {
      // We are on the project node now
      return project;
    }

    String name = getFullClassName(projectViewNode);
    //System.out.println( "(2)RefactorItActionProvider.getBinObject: name = " + name );
    if (name == null) {
      DialogManager.getInstance().showWarning(
          IDEController.getInstance().createProjectContext(),
          "warning.action.unit.error");
      return null;
    }

    if (projectViewNode instanceof PackageNode) {
      /* Just to see contain of PackageNode
          PackageNode pn = (PackageNode)node;
          Node[] childs = pn.getChildren();
          System.out.println( "getChildren count: " + childs.length );
          for( int i = 0; i < childs.length; i++ ) {
        System.out.println( "child: " + childs[i] );
          }

          childs = pn.getDisplayChildren();
          System.out.println( "getDisplayChildren count: " + childs.length );
          for( int i = 0; i < childs.length; i++ ) {
        System.out.println( "child: " + childs[i] );
          }
       */

      if (BinPackage.isIgnored(name, project)) {
        throw new NotFromSrcOrFromIgnoredException();
      }

      BinPackage p = ItemByNameFinder.findBinPackage(project, name);
      if (p == null) {
        DialogManager.getInstance().showWarning(
            IDEController.getInstance().createProjectContext(),
            "warning.action.unit.error");
      }
      return p;
    }

    BinCIType type = ItemByNameFinder.findBinCIType(project, name);
    if (type == null) {

      throw new NotFromSrcOrFromIgnoredException();

      //old code
      /*
      DialogManager.getInstance().showWarning(
          IDEController.getInstance().createProjectContext(),
          "warning.action.unit.error");
      */
    }
    return type;
  }

//	private final class JBStructureViewRefactoryAction extends JBRefactoritAction {
//		private final Node[] nodes;
//		private JBStructureViewRefactoryAction(String actionName, Icon icon, Node[] nodes) {
//			super(actionName, icon);
//			this.nodes = nodes;
//		}
//		public void actionPerformed(ActionEvent evt) {
//			DebugInfo.trace("performing StructureView action"+getAction().getName());
//			RefactorItTool.performAction(
//				(RefactorItAction) getAction(),
//				nodes);
//		}
//	}

  public static class InterruptRefactorItActionSilently extends Exception {
  }
}

class JBUsersSelection {
  BinSelection binSelection = null;
  BinItem binItem = null;

  public BinItem getBinItem() {
      return this.binItem;
  }

  public void setBinItem(final BinItem binItem) {
      this.binItem = binItem;
  }

  public BinSelection getBinSelection() {
      return this.binSelection;
  }

  public void setBinSelection(final BinSelection binSelection) {
      this.binSelection = binSelection;
  }
}
