/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;



import com.borland.jbuilder.node.java.JavaStructure;
import com.borland.jbuilder.node.java.JavaStructureElement;
import com.borland.jbuilder.node.java.JavaStructureNode;
import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.actions.ActionMenu;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.util.VetoException;

import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RunContext;


/**
 * Extends standard java structure view customized for displaying context menus.
 *
 * @author Igor Malinin
 */
public class JavaStructureRe extends JavaStructure {
  public JavaStructureRe() {
    super();
  }

  // save mouse event as we don't get it when build customized menus
//  private MouseEvent me;
//
//  public void mouseReleased( MouseEvent evt ) {
//    super.mouseReleased( me = evt );
//  }
//
//  public void mousePressed( MouseEvent evt ) {
//    super.mousePressed( me = evt );
//  }

  public static JavaStructureNode[] getJavaStructureNodes(TreePath[] paths) {
    if (paths == null) {
      return null;
    }

    int size = paths.length;
    JavaStructureNode[] nodes = new JavaStructureNode[paths.length];

    for (int i = 0; i < size; i++) {
      Object obj = paths[i].getLastPathComponent();
      if (!(obj instanceof JavaStructureNode)) {
        return null;
      }

      nodes[i] = (JavaStructureNode) obj;
    }

    return nodes;
  }

  /**
   * Extended to return customized menu.
   */
  public JPopupMenu getPopup() {
    JPopupMenu popup = super.getPopup();

    MenuBuilder builder = MenuBuilder.createEmptyRefactorITMenu('R');
    RunContext ctx = extractRunContext();
    builder.buildContextMenu(ctx);

    // wrapping to JMenu
    ActionGroup actionGroup = (ActionGroup) builder.getMenu();
    ActionMenu menu = new ActionMenu(new Object(), actionGroup);
    popup.add(menu);

    return popup;
  }

  private RunContext extractRunContext() {
    RunContext unsupported = new RunContext(RunContext.UNSUPPORTED_CONTEXT,
        (Class[])null, true);

    TreePath[] paths = tree.getSelectionPaths();
    JavaStructureNode[] nodes = getJavaStructureNodes(paths);
    if (nodes == null) {
      return unsupported;
    }

    int size = nodes.length;
    Class[] clss = new Class[size];

    int code = RunContext.JAVA_CONTEXT;

    for (int i = 0; i < size; i++) {
      Object obj = nodes[i].getUserObject();
      if (!(obj instanceof JavaStructureElement)) {
        return unsupported;
      }
      JavaStructureElement element = (JavaStructureElement) obj;
      Class cls = getBinClass(element);
      if (cls == null) {
        return unsupported;
      }
      clss[i] = cls;
      //System.out.println( "Class["+i+"] = " + clss[i] );
      //System.out.println( "Node["+i+"] = " + nodes[i] );
    }

    return new RunContext(code, clss, true);
  }

  protected void performAction(
      RefactorItAction action, JavaStructureNode[] nodes
  ) {
    Browser browser = Browser.getActiveBrowser();
    /*
         if ( browser == null ||
         action  == null ||
         nodes    == null )
      return;
         this.browser = browser;
         this.action = action;
         this.javaNodes = nodes;
     */
    try {
      browser.doSaveAll(false);
    } catch (VetoException ignore) {}

    performModuleAction(browser, action, nodes,
        (JavaStructureNode) treeModel.getRoot());
  }

  public static boolean isFromProjectSource (JavaStructureNode root) {
    JavaStructureNode[] nodes = root.getChildNodes();

    for (int i = 0; i < nodes.length; i++) {

      if (!(nodes[i].getUserObject() instanceof JavaStructureElement)) {
        continue;
      }

      JavaStructureElement element = (JavaStructureElement) nodes[i].getUserObject();

      if (isClassType(element) || isIntefaceType(element)) {

        if (findBinCIType(nodes[i], root) != null) {
          return true;
        } else {
          return false;
        }
      }
    }

    return true;
  }

  public static Object getBinObject(
      JavaStructureNode node, JavaStructureNode root) {
    if (!(node.getUserObject() instanceof JavaStructureElement)) {
      return null;
    }

    JavaStructureElement element = (JavaStructureElement) node.getUserObject();

    try {
      if (isClassType(element) || isIntefaceType(element)) {
        return findBinCIType(node, root);
      }

      String elementName = getElementName(element);
      if (elementName == null) {
        return null;
      }

      if (isTypeImportType(element)) {
        BinTypeRef typeRef = IDEController.getInstance().getActiveProject()
            .getTypeRefForName(elementName);
        return typeRef == null ? null : typeRef.getBinType();
      }

      if (isPackageType(element)) {
        if (elementName.endsWith(".*")) {
          elementName = elementName.substring(0, elementName.length() - 2);
        }
        return IDEController.getInstance().getActiveProject()
            .createPackageForName(elementName);
      }

      switch (element.type) {
        case TYPE_FIELD: {
          String name = elementName;

          if (name.lastIndexOf(":") != -1) {
            name = name.substring(0, name.lastIndexOf(":")).trim();
          }

// NOTE: FieldInfo can be used to get field name, at least works in JBX
//          JavaStructureElement.FieldInfo info
//              = (JavaStructureElement.FieldInfo) element.getItemInfo();
//System.err.println("fieldName: " + info.getFieldName());

          BinCIType type = findBinCIType(getParentTypeNode(node), root);
          if (type == null) {
            return null;
          }

          BinField field = ItemByNameFinder.findBinField(type, name);

          if (field == null) {
            // in case of "flat inners" mode
            BinTypeRef[] inners = type.getDeclaredTypes();
            for (int i = 0; i < inners.length; i++) {
              field = ItemByNameFinder.findBinField(inners[i].getBinCIType(), name);
              if (field != null) {
                break;
              }
            }
          }

          return field;
        }

        case TYPE_METHOD:
        case TYPE_CONSTRUCTOR: {
          String str = elementName;

          int pos0 = str.indexOf('(');
          if (pos0 < 0) {
            return null;
          }

          int pos1 = str.lastIndexOf(')');
          if (pos1 < 0) {
            return null;
          }

          String name = str.substring(0, pos0++).trim();

          StringTokenizer st
              = new StringTokenizer(str.substring(pos0, pos1), ",");
          String[] args = new String[st.countTokens()];
          for (int i = 0; st.hasMoreTokens(); i++) {
            str = st.nextToken().trim();
            int len = str.length();
            for (int n = 0; n < len; n++) {
              char ch = str.charAt(n);
              if (Character.isWhitespace(ch)) {
                str = str.substring(0, n);
                break;
              }
            }

            args[i] = str;
          }

          //System.out.println("args: " + Arrays.asList(args));
          BinCIType type = findBinCIType(getParentTypeNode(node), root);

          if (type != null) {
            BinMethod method;
            if (element.type == TYPE_CONSTRUCTOR) {
              method = ItemByNameFinder.findBinConstructor((BinClass) type, args);
            } else {
              method = ItemByNameFinder.findBinMethod(type, name, args);
            }

            if (method == null) {
              // in case of "flat inners" mode
              BinTypeRef[] inners = type.getDeclaredTypes();
              for (int i = 0; i < inners.length; i++) {
                if (element.type == TYPE_CONSTRUCTOR) {
                  method = ItemByNameFinder.findBinConstructor(
                      (BinClass) inners[i].getBinCIType(), args);
                } else {
                  method = ItemByNameFinder.findBinMethod(
                      inners[i].getBinCIType(), name, args);
                }

                if (method != null) {
                  break;
                }
              }
            }

            return method;
          }
        }
      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, JavaStructureRe.class);
      JErrorDialog err = new JErrorDialog(
          IDEController.getInstance().createProjectContext(), "Error");
      err.setException(e);
      err.show();
    }
System.err.println("unknown element: " + element + " - " + element.type);

    return null;
  }

  private static JavaStructureNode getParentTypeNode(final JavaStructureNode
      child) {
    JavaStructureNode parent = (JavaStructureNode) child.getParent();
    while (parent != null
        && parent.getUserObject() instanceof JavaStructureElement) {
      if ((!isClassType((JavaStructureElement) parent.getUserObject())
          && !isIntefaceType((JavaStructureElement) parent.getUserObject()))
          || ((JavaStructureElement) parent.getUserObject()).type
          == TYPE_INHERIT_CLASS) {
        parent = (JavaStructureNode) parent.getParent();
      } else {
        break;
      }
    }
    return parent;
  }

  public boolean performModuleAction(Browser browser, RefactorItAction action,
      JavaStructureNode[] nodes, JavaStructureNode root) {

    if (!IDEController.getInstance().ensureProject(new LoadingProperties(true))) {
      return false;
    }
    Project project = IDEController.getInstance().getActiveProject();

    int size = nodes.length;
    Object[] bins = new Object[size];
    for (int index = 0; index < size; index++) {
      bins[index] = getBinObject(nodes[index], root);
      if (bins[index] == null) {
        RitDialog.showMessageDialog(
            IDEController.getInstance().createProjectContext(),
            "Can not perform refactorings on item you selected\n",
            "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    boolean res = false;
    JBContext context = new JBContext(project, browser);
    try {
      RefactorItActions.setupPointForTree(browser, this.getTree(), context);
    } catch (Exception e) {
      if (Assert.enabled) {
        e.printStackTrace();
      }
    }

    if (size == 1) {
      if (bins[0] != null) {
        res = RefactorItActionUtils.run(action, context, bins[0]);
      }
    } else {
      if (bins != null) {
        res = RefactorItActionUtils.run(action, context, bins);
      }
    }

    if (res) {
      action.updateEnvironment(context);
      browser.getProjectView().refreshTree();
    } else {
      action.raiseResultsPane(context);
    }

    Browser.getActiveBrowser().dispatchEvent(
        new WindowEvent(Browser.getActiveBrowser(),
        WindowEvent.WINDOW_ACTIVATED));

    return res;
  }

  private static boolean isClassType(JavaStructureElement element) {
    return element.type == TYPE_CLASS || element.type == TYPE_INHERIT_CLASS;
  }

  private static boolean isIntefaceType(JavaStructureElement element) {
    return element.type == TYPE_INTERFACE ||
        element.type == TYPE_IMPLEMENT_INTERFACE;
  }

  private static boolean isPackageType(JavaStructureElement element) {
    if (element.type == TYPE_IMPORT) {
      String elementName = getElementName(element);
      return elementName != null && elementName.endsWith(".*");
    }

    return element.type == TYPE_PACKAGE;
  }

  private static boolean isTypeImportType(JavaStructureElement element) {
    if (element.type == TYPE_IMPORT) {
      String elementName = getElementName(element);
      return elementName != null && !elementName.endsWith(".*");
    }

    return false;
  }

  public static Class getBinClass(JavaStructureElement element) {
    if (isClassType(element) || isTypeImportType(element)) {
      return BinClass.class;
    }

    if (isIntefaceType(element)) {
      return BinInterface.class;
    }

    if (isPackageType(element)) {
      return BinPackage.class;
    }

    switch (element.type) {
      case TYPE_FIELD:
        return BinField.class;

      case TYPE_METHOD:
        return BinMethod.class;

      case TYPE_CONSTRUCTOR:
        return BinConstructor.class;
    }
    return null;
  }

  private static BinCIType findBinCIType(JavaStructureNode node,
      JavaStructureNode root) {
    BinCIType type = null;

    TreeNode parent = node.getParent();
    if (parent instanceof JavaStructureNode) {
      Object obj = ((JavaStructureNode) parent).getUserObject();
      if (obj instanceof JavaStructureElement) {
        JavaStructureElement element = (JavaStructureElement) obj;
        if (isClassType(element) || isIntefaceType(element)) {
          type = findBinCIType((JavaStructureNode) parent, root);
        }
      }
    }

    JavaStructureElement element = (JavaStructureElement)
        node.getUserObject();

    Project project = IDEController.getInstance().getActiveProject();

    BinTypeRef declaredType = null;

    if (type != null) {
      declaredType = type.getDeclaredType(getElementName(element));
    }

    if (declaredType == null) {
      return ItemByNameFinder.findBinCIType(project,
          getClassName(getElementName(element), root));
    }
//    System.out.println("name is "+element.name);

    return declaredType.getBinCIType();
  }

  private static final String getElementName(JavaStructureElement element) {
    String name = null;
    try {
      name = (String)element.getClass().getField("name").get(element);
    } catch (Error e) {
    } catch (Exception e) {
    }

    if (name == null) {
      try { // JB2005
        Method method
            = element.getClass().getMethod("getName", new Class[0]);
        name = (String) method.invoke(element, new Object[0]);
      } catch (Error e) {
      } catch (Exception e) {
      }
    }

    if (name == null) {
      try { // JB2005 field
        Method method
            = element.getClass().getMethod("getFieldName", new Class[0]);
        name = (String) method.invoke(element, new Object[0]);
      } catch (Error e) {
      } catch (Exception e) {
      }
    }

    if (name == null) {
      try { // JB2005 method
        Method method
            = element.getClass().getMethod("getSignature", new Class[0]);
        name = (String) method.invoke(element, new Object[0]);
      } catch (Error e) {
      } catch (Exception e) {
      }
    }

    return name;
  }

  // Seems that things work without it

//  public static class JBRunContext extends RunContext {
//    private JBAction action;
//
//    public JBRunContext(RunContext context, JBAction action) {
//      super(context.getContextType(), context.getItems());
//      this.action = action;
//
//    }
//
//    JBAction getAction() {
//      return action;
//    }
//
//  }

  public static String getClassName(String name, JavaStructureNode root) {
    if (name == null) {
      return null;
    }
    String pkg = getPackageName(root);
    return (pkg == null || pkg.length() == 0) ? name : pkg + '.' + name;
  }

  public static String getPackageName(JavaStructureNode root) {
    //JavaStructureNode root = (JavaStructureNode) treeModel.getRoot();
    int len = root.getChildCount();
    for (int i = 0; i < len; i++) {
      JavaStructureNode inode = (JavaStructureNode) root.getChildAt(i);
      JavaStructureElement e = (JavaStructureElement) inode.getUserObject();
      if (e.type == TYPE_IMPORTS) {
        int elementsLength = inode.getChildCount();
        for (int p = 0; p < elementsLength; p++) {
          JavaStructureNode pnode = (JavaStructureNode) inode.getChildAt(p);
          e = (JavaStructureElement) pnode.getUserObject();
          if (e.type == TYPE_PACKAGE) {
            return getElementName(e);
          }
        }

        break;
      }
    }

    return "";
  }
}
