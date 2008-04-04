/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import bsh.EvalError;
import bsh.Interpreter;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.commonIDE.MenuMetadata;
import net.sf.refactorit.commonIDE.NotFromSrcOrFromIgnoredException;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.BackAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.module.shell.ShellFrame;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.LinePositionUtil;
import net.sf.refactorit.utils.SwingUtil;
import oracle.ide.IdeAction;
import oracle.ide.addin.Context;
import oracle.ide.addin.Controller;
import oracle.ide.cmd.SaveAllCommand;
import oracle.ide.model.Element;
import oracle.ide.model.PackageFolder;
import oracle.ide.navigator.NavigatorWindow;
import oracle.jdeveloper.ceditor.CodeEditor;
import oracle.jdeveloper.model.JProject;
import oracle.jdeveloper.model.JavaSourceNode;
import oracle.jdeveloper.model.JspSourceNode;

import java.awt.Component;
import java.awt.Window;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;


/**
 * Manages the IDE actions defined by this addin. IDE actions are associated
 * with menu items.
 *
 * @see oracle.ide.IdeAction
 *
 * @author  Tanel
 */
public class RefactorItController extends JDevController implements Controller {
//  public final static boolean developingMode=false;

  //public static final String PROP_CACHEPATH = "RefactorIT.cachepath";

  private static RefactorItController instance = null;

  public static final String REFACTORY_ACTION = "REFACTORY_ACTION";
  public static final String CMD_STR = "controller.action.";

  /**
   * Default constructor.  A singleton instance is created when the addin
   * is loaded at startup.
   */
  private CompilationUnit compilationUnit;
  private int line;
  private RefactorItController() {
//        initActions();
//        if( developingMode ) {
//          IDEController.setInstance(this);
//        }
  }

  /**
   * Gets the supervising controller; the controller provides default behavior
   * for this controller.
   *
   * @return the supervising controller.
   */
  public Controller supervisor() {
    // FIXME: always null in 9.0.5.1
    final Controller ideController = AbstractionUtils.getIdeController();
    return ideController;
  }

  /**
   * Handles events pertaining to this addin's menu items. This method should
   * handle actions defined for this addin, and may elect to handle others, as
   * well. Other events are delegated to the supervisor.
   *
   * @param action the IDE action associated with the selected menu item or
   * keyboard shortcut.
   * @param context the context current when the event occured.
   *
   * @return  true if the controller or its supervisor handles the action.
   */
  public boolean handleEvent(IdeAction action, Context context) {
//    System.out.println("handling action: " + action.getCommand() + " id: " + action.getCommandId());
//    if (action.getCommandId() == 9999) {
//      //debug shell
//      startBeanShell(context);
//      return true;
//    }

    if (!RefactorITLock.lock()) {
      return true;
    }

    try {
      try {
        SaveAllCommand.saveAll();
      } catch (Exception e) {
        e.printStackTrace();
      }

      setLastEventContext(context);

      JDevActionRepository actionRep = (JDevActionRepository) ActionRepository.
          getInstance();

      Object targetAction = actionRep.getRITActionFromIdeAction(action);

      if (targetAction != null) {
        if (targetAction instanceof RefactorItAction) {
          RefactorItAction ritAction = (RefactorItAction) targetAction;
          //          int binSelectionNeccessity = ( (Integer) action.getValue(
//              OldMenuBuilder.
//              BINSELECTION_NECCESSITY)).intValue();
          performRefactoryAction(ritAction, context,
              JDevMenuBuilder.BINSELECTION_DONT_CARE /*binSelectionNeccessity*/);
        } else {
          net.sf.refactorit.commonIDE.IdeAction ritIdeAction =
              (net.sf.refactorit.commonIDE.IdeAction) targetAction;

          RefactorItActionUtils.run(ritIdeAction);
        }

        return true;
      } else {
        AppRegistry.getLogger(this.getClass()).debug("targetAction " + action
            + " not found in repository");
        return supervisor().handleEvent(action, context);
      }

      // When action is null, then the action is either the shortcut action or
      // When action is null, then the action is either the shortcut action or
      // common action.
//      RefactorItAction refactoryAction = (RefactorItAction)
//          action.getValue(RefactorItController.REFACTORY_ACTION);
//
//      if (refactoryAction == null) {
//        int cmdId = action.getCommandId();
//        String actionKey = getKey(cmdId);
//        // Check, whether it is shortcut action
//        if (actionKey != null) {
//          handleShortcutAction(actionKey, context);
//          return true;
//        } else {
//          // handle common actions
//          if (handleCommonAction(cmdId, context)) {
//            return true;
//          } else {
//            return supervisor().handleEvent(action, context);
//          }
//        }
//      } else {
//        // perform the action
//
//        int binSelectionNeccessity = ((Integer) action.getValue(OldMenuBuilder.
//            BINSELECTION_NECCESSITY)).intValue();
//        performRefactoryAction(refactoryAction, context, binSelectionNeccessity);
//        return true;
//      }
    } finally {
      RefactorITLock.unlock();
    }
  }

  /**
   * Returns the shortcut action ID, or null if specified action
   * is not shortcut action.
   * @param actionId actionId
   * @return action key
   */
//  private String getKey(int actionId) {
//    if (actionId == RefactorItController.REFACTORY_WHERE_USED_CMD_ID)
//      return net.sf.refactorit.ui.module.where.WhereAction.KEY;
//    if (actionId == RefactorItController.REFACTORY_GO_TO_DECLARATION_CMD_ID)
//      return net.sf.refactorit.ui.module.gotomodule.actions.GotoAction.KEY;
//    if (actionId == RefactorItController.REFACTORY_INFO_CMD_ID)
//      return net.sf.refactorit.ui.module.type.TypeAction.KEY;
//    if (actionId == RefactorItController.REFACTORY_RENAME_CMD_ID)
//      return net.sf.refactorit.ui.module.rename.RenameAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_APIDIFF_CMD_ID)
//        return net.sf.refactorit.ui.module.apidiff.ApiDiffAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_APISNAPSHOT_CMD_ID)
//        return net.sf.refactorit.ui.module.apisnapshot.ApiSnapshotAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_AUDIT_CMD_ID)
//        return net.sf.refactorit.ui.module.audit.AuditAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_CALLTREE_CMD_ID)
//        return net.sf.refactorit.ui.module.calltree.CallTreeAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_CLEANIMPORTS_CMD_ID)
//        return net.sf.refactorit.ui.module.cleanimports.CleanImportsAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_CREATEMISSINGMETHOD_CMD_ID)
//        return net.sf.refactorit.ui.module.createmissingmethod.CreateMissingMethodAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_DEPENDENCIES_CMD_ID)
//        return net.sf.refactorit.ui.module.dependencies.DependenciesAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_ENCAPSULATEFIELD_CMD_ID)
//        return net.sf.refactorit.ui.module.encapsulatefield.EncapsulateFieldAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_EXTRACTMETHOD_CMD_ID)
//        return net.sf.refactorit.ui.module.extractmethod.ExtractMethodAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_CREATECONSTRUCTOR_CMD_ID)
//        return net.sf.refactorit.ui.module.extractmethod.ExtractMethodAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_INTRODUCETEMP_CMD_ID)
//        return net.sf.refactorit.ui.module.introducetemp.IntroduceTempAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_EXTRACTSUPER_CMD_ID)
//        return net.sf.refactorit.ui.module.extractsuper.ExtractSuperAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_FACTORYMETHOD_CMD_ID)
//        return net.sf.refactorit.ui.module.factorymethod.FactoryMethodAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_FIND_CMD_ID)
//        return net.sf.refactorit.ui.module.search.FindAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_FIXME_CMD_ID)
//        return net.sf.refactorit.ui.module.fixmescanner.FixmeScannerAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_METRICS_CMD_ID)
//        return net.sf.refactorit.ui.module.metric.MetricsAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_MINACCESS_CMD_ID)
//    return net.sf.refactorit.ui.module.minaccess.MinimizeAccessAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_MOVEMEMBER_CMD_ID)
//        return net.sf.refactorit.ui.module.movemember.MoveMemberAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_MOVETYPE_CMD_ID)
//        return net.sf.refactorit.ui.module.movetype.MoveTypeAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_NOTUSED_CMD_ID)
//        return net.sf.refactorit.ui.module.notused.NotUsedAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_PULLPUSH_CMD_ID)
//    return net.sf.refactorit.ui.module.pullpush.PullPushAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_SUBTYPES_CMD_ID)
//        return net.sf.refactorit.ui.module.subtypes.SubtypesAction.KEY;
//
//  if (actionId == RefactorItController.REFACTORIT_WHERECAUGHT_CMD_ID)
//        return net.sf.refactorit.ui.module.wherecaught.WhereCaughtAction.KEY;
//
//
//    // return null otherwise.
//    return null;
//  }

  /**
   * Handles the shortcut actions . I.e the actions (rename, goto ..., ...)
   * whose types are in this stage not known.
   *
   * @param actionKey the id of the shortcut, defined in RefactorItAction
   * @param context context of the event that caused the action
   *
   * @see net.sf.refactorit.ui.module.RefactorItAction
   */
//  private void handleShortcutAction(String actionKey, Context context) {
//    Object binObject;
//
//    // HACK: this check is here because "errors" tab looses its focus with prepareProject(),
//    // so if a BinTreeTable is active, we have to call the prepareProject()
//    // _after_ the getBinObject() method. In other cases we use the reverse
//    // order because it might be better for those (not sure, though).
//
//    boolean isJsp=false;
//    // JSP checking
//    Document document=context.getDocument();
//    if( document instanceof JspSourceNode) {
//      isJsp=true;
//    }
//    if(context.getView() instanceof RefactorItWindow) {
//      binObject  = getBinObject(context, getActiveProject());
//
//      if( !ensureProject() ) {
//        return;
//      }
//    }
//    else {
//      if (!ensureProject() ) {
//        return;
//      }
//
//      binObject = getBinObject(context, getActiveProject());
//    }
//
//    if ( binObject == null ) {
//      showNoBinObjectError();
//      return;
//    }
//
//    Class cls[];
//    Object[] bins = null;
//    if ( binObject instanceof Object[] ) {
//        bins = (Object[])binObject;
//        cls = new Class[bins.length];
//        for( int i = 0; i < bins.length; i++ )
//                        cls[i] = bins[i].getClass();
//        binObject = ((Object[])binObject)[0];
//    } else {
//        cls = new Class[] { binObject.getClass() };
//    }
//
//    RefactorItAction action
//        = ModuleManager.getAction( cls, actionKey );
//    if( isJsp && action!=null && !action.isPreprocessedSourcesSupported(cls)) {
//      //System.err.println("@@Action prohibited");
//      return;
//    }
//
//    if ( action == null
//        && actionKey.equals(net.sf.refactorit.ui.module.type.TypeAction.KEY) )
//    {
//        //lets call JavaDoc then
//        action = ModuleManager.getAction( cls,
//            JavadocAction.KEY );
//    }
//
//    if (action != null) {
//      if ( (actionKey.equals(net.sf.refactorit.ui.module.gotomodule.actions.GotoAction.KEY)
//          || actionKey.equals(net.sf.refactorit.ui.module.gotomodule.actions.GotoAction.KEY))
//          && CodeEditor.class.isAssignableFrom(context.getView().getClass()) &&
//        sourceURL != null )
//      {
//        int line = ((CodeEditor)context.getView()).getCaretLine();
//        net.sf.refactorit.jdeveloper.BackAction.addRecord( sourceURL, line );
//      }
//
//      try {
//        JDevContext cont = createRefactoryContext(context);
//        boolean res;
//        if ( bins == null ) {
//                res = RefactorItActionUtils.run(
//              action, cont, AbstractionUtils.getMainWindow(), binObject);
//        } else {
//                if ( bins.length == 1 ) {
//                        res = RefactorItActionUtils.run(
//                action, cont, AbstractionUtils.getMainWindow(), bins[0]);
//                } else {
//                        res = RefactorItActionUtils.run(
//                action, cont, AbstractionUtils.getMainWindow(), bins);
//                }
//        }
//        if (res) {
//          action.updateEnvironment(AbstractionUtils.getMainWindow(), cont);
//          //TODO: refresh navigator view
//        } else {
//          action.raiseResultsPane(AbstractionUtils.getMainWindow(), cont);
//        }
//      } catch ( Exception e ) {
//        JErrorDialog err = new JErrorDialog( AbstractionUtils.getMainWindow(), "Error" );
//        err.setException( e );
//        err.show();
//        return;
//      }
//    } else {
//        showNoBinObjectError();
//        return;
//    }
//  }

  /**
   * Executes the refactory action.
   * @param action action
   * @param context context
   * @param binSelectionNeccessity binSelectionNeccessity
   */
  private void performRefactoryAction(
      RefactorItAction action, Context context, int binSelectionNeccessity
      ) {
    if (!ensureProject()) {
      return;
    }

    Object binObject;

    try {
      binObject = getBinObject(context, getActiveProject());
    } catch (NotFromSrcOrFromIgnoredException e) {
      showFromIgnoredSourceError();
      return;
    }

    if (binObject == null) {
      showNoBinObjectError();
      return;
    }

    if (context != null && context.getDocument() instanceof JspSourceNode) {
      if (!action.isPreprocessedSourcesSupported(binObject.getClass())) {
        return;
      }
    }

    if (binSelectionNeccessity == JDevMenuBuilder.BINSELECTION_NEEDED &&
        binObject.getClass() != BinSelection.class) {
      showSelectionNeededError();
      return;
    } else if (binSelectionNeccessity
        == JDevMenuBuilder.BINSELECTION_PROHIBITED &&
        binObject.getClass() == BinSelection.class) {
      showSelectionNotNeededError();
      return;
    }

//    Window oldParent = DialogManager.getDialogParent();
    try {
//      DialogManager.setDialogParent(AbstractionUtils.getMainWindow());
      JDevContext refactoryCtx = createRefactoryContext(context);

      if (action.getKey() == GotoAction.KEY) {
        BackAction.addRecord(compilationUnit, line);
      }

      if (RefactorItActionUtils.run(action, refactoryCtx, binObject)) {
        action.updateEnvironment(refactoryCtx);
        //TODO: refresh navigator view
      } else {
        action.raiseResultsPane(refactoryCtx);
      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
      JErrorDialog err = new JErrorDialog(createProjectContext(), "Error");
      err.setException(e);
      err.show();
//    } finally {
//      DialogManager.setDialogParent(oldParent);
    }
  }

  /**
   * @param context context
   * @return new JDev context
   */
  private JDevContext createRefactoryContext(final Context context) {
    JDevContext refactoryCtx = (JDevContext)super.createProjectContext();

//    JDevContext refactoryCtx = new JDevContext(refactorItProject);
    if (CodeEditor.class.isAssignableFrom(context.getView().getClass())) {
      CodeEditor editor = ((CodeEditor) context.getView());

      Window window = ((AWTContext) refactoryCtx).getWindow();

      refactoryCtx.setPoint(SwingUtil.positionToClickPoint(
          editor.getFocusedEditorPane(), editor.getCaretPosition(), window));
    }

    return refactoryCtx;
  }

  private Object getBinObject(Context context, Project project)
      throws NotFromSrcOrFromIgnoredException {
    Class activeViewClass = context.getView().getClass();

    if (CodeEditor.class.isAssignableFrom(activeViewClass)) {
      return getBinObjectFromEditor(context, project);
    } else

    if (NavigatorWindow.class.isAssignableFrom(activeViewClass)) {
      return getBinObjectFromNavigator(context, project);
      // FIXME: this is ugly, but due to JDev RC API limitations
    } else

    if (activeViewClass.getName().indexOf("ExplorerWindow") > 0) {
      return getBinObjectFromExplorer(context, project);
    } else

    if (context.getView() instanceof RefactorItWindow) {
      return getBinObjectFromRefactoryWindow((RefactorItWindow) context.getView());
    } else {
      throw new ChainableRuntimeException(
          "Don't know how to find BinObject from " + activeViewClass);
    }
  }

  private Object getBinObjectFromRefactoryWindow(RefactorItWindow w) {
    JComponent c = w.getHostedComponent();
    if (!(c instanceof JTabbedPane)) {
      return null;
    }

    Component binPanel = ((JTabbedPane) c).getSelectedComponent();
    if (!(binPanel instanceof BinPanel)) {
      return null;
    }
    BinPanel p = (BinPanel) binPanel;

    JComponent comp = p.getCurrentPane().getComponent().getUI();
    if (!(comp instanceof BinTreeTable)) {
      return null;
    }

    return actionPerformedFromBinTreeTable((BinTreeTable) comp);
  }

  /**
   * finds bin object from the table
   * @param table table
   * @return bin or array of bins
   */
  private Object actionPerformedFromBinTreeTable(BinTreeTable table) {
    TreePath[] paths = table.getTree().getSelectionPaths();
    if (paths == null || paths.length == 0) {
      return null;
    }

    Object obj = null;
    if (paths.length == 1) {
      TreePath path = paths[0];

      BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();
      obj = node.getBin();
    } else {
      int size = paths.length;
      Object[] bins = new Object[size];
      for (int j = 0; j < size; j++) {
        bins[j] = ((BinTreeTableNode) paths[j].getLastPathComponent()).getBin();
      }
      obj = bins;
    }

    return obj;
  }

  /**
   * Finds the bin object from the code editor window.
   *
   * @param project active project
   * @param context event context
   * @return bin object that is under cursor or null if there is no bin object
   * under cursor
   */
  private Object getBinObjectFromEditor(Context context, Project project)
      throws NotFromSrcOrFromIgnoredException {
    String name = null;
    Element element = context.getElement();
    if (element == null) {
      element = context.getDocument();
    }
    if (element instanceof JavaSourceNode) {
      name = getSelectedClassName((JavaSourceNode) element);
    } else if (element instanceof JspSourceNode) {
      JspSourceNode jspNode = (JspSourceNode) element;
      String fileName = jspNode.getURL().getFile();

      // checkme: should or should'nt we decode this url?
      fileName = URLDecoder.decode(fileName);
      name = JspUtil.getClassName(fileName);
    } else {
      return null;
    }

    if (name == null) {
      return null;
    }

    BinTypeRef typeRef = project.getTypeRefForName(name);
    if (typeRef == null) {
      String packName = name;
      if (name.lastIndexOf(".") > 0) {
        packName = name.substring(0, name.lastIndexOf("."));
      }

      if (BinPackage.isIgnored(packName, project)) {
        throw new NotFromSrcOrFromIgnoredException();
      }

      return null;
    }

    CompilationUnit sf = typeRef.getBinCIType().getCompilationUnit();
    if (sf == null) {
      return null; // FIXME: project still has type, but sourcefile has gone, strange
    }

    CodeEditor editor = (CodeEditor) context.getView();

    String text = editor.getSelectedText();
    if (text == null || text.length() == 0) {
      compilationUnit = sf;

      int caretPosition = editor.getCaretPosition();

      ItemByCoordinateFinder finder = new ItemByCoordinateFinder(sf);

      //int tabSize = EditorProperties.getProperties().getIntegerProperty(EditorProperties.PROPERTY_TAB_SIZE);
      //LinePositionUtil.setTabSize(tabSize);
      // FIXME: actually we sould use the commented block above, but with below line it works
      LinePositionUtil.setTabSize(1);

      SourceCoordinate sc = LinePositionUtil.convert(caretPosition,
          sf.getContent());

      line = sc.getLine();

      BinItem item = finder.findItemAt(sc);
      if (item == null) {
        item = ItemByNameFinder.findBinCIType(project, name);
      }
      return item;
    }

    // some text is selected
    BinSelection bs = new BinSelection(sf, text,
        editor.getSelectionStart(), editor.getSelectionEnd());

    return bs;
  }

  /**
   * Determines the currently selected bin object from in the Explorer pane.
   * This method constructs a new ExplorerWrapper and asks the active bin object
   * from the it.
   *
   * @param context event context
   * @param project project
   * @return selected bin object
   **/
  private Object getBinObjectFromExplorer(Context context, Project project)
      throws NotFromSrcOrFromIgnoredException {
    ExplorerWrapper wrapper = new ExplorerWrapper(context);
    return wrapper.getSelectedBinItem(project);
  }

  /**
   * Determines the currently selected bin object from in the Navigator pane.
   *
   * @param context event context
   * @param project project
   * @return selected bin object
   **/
  private Object getBinObjectFromNavigator(Context context, Project project)
      throws NotFromSrcOrFromIgnoredException {
    Element[] selectedElements = context.getSelection();
    if (selectedElements == null || selectedElements.length == 0) {
      return null;
    }

    int size = selectedElements.length;
    Object[] bins = new Object[size];

    List fromIgnored = new ArrayList();

    for (int i = 0; i < size; i++) {
      if ((JavaSourceNode.class).isAssignableFrom(selectedElements[i].getClass())) {
        String className = getSelectedClassName((JavaSourceNode)
            selectedElements[i]);

        BinCIType type = ItemByNameFinder.findBinCIType(project, className);

        if (type == null) {
          String packName = className;
          if (className.lastIndexOf(".") > 0) {
            packName = className.substring(0, className.lastIndexOf("."));
          }

          if (BinPackage.isIgnored(packName, project)) {
            fromIgnored.add(selectedElements[i]);
          }
        }

        bins[i] = type;
      } else

      if ((JProject.class).isAssignableFrom(selectedElements[i].getClass())) {
        bins[i] = project;
      } else

      if ((PackageFolder.class).isAssignableFrom(selectedElements[i].
          getClass())) {
        String packageName = selectedElements[i].getLongLabel();
        BinPackage pack = ItemByNameFinder.findBinPackage(project, packageName);

        if (!pack.hasTypesWithSources()
            && !pack.hasTypesWithoutSources()
            && BinPackage.isIgnored(pack)) {
          fromIgnored.add(selectedElements[i]);
        }

        bins[i] = pack;

      } else {
        throw new ChainableRuntimeException("No active bin object found");
      }
    }

    if (fromIgnored.size() > 0) {
      StringBuffer buffer = new StringBuffer
          ("The following element(s) are from ignored sources:\n");

      for (int i = 0; i < fromIgnored.size(); i++) {
        buffer.append(fromIgnored.get(i) + "\n");
      }

      if (fromIgnored.size() == bins.length) {
        throw new NotFromSrcOrFromIgnoredException();
      } else {
        RitDialog.showMessageDialog(
            createProjectContext(),
            buffer, "Warning", JOptionPane.ERROR_MESSAGE);

        ArrayList li = new ArrayList();

        for (int i = 0; i < bins.length; i++) {
          if (bins[i] != null) {
            li.add(bins[i]);
          }
        }

        bins = li.toArray();
      }
    }

    if (size == 1) {
      return bins[0];
    }

    return bins;
  }

  /**
   * Gets the full class name corresponding to the JavaSourceNode.
   *
   * @param node node where to extract the class name from
   * @return full class name for the node, e.g. net.sf.refactorit.common.util.SwingUtil
   */
  private String getSelectedClassName(JavaSourceNode node) {
    String classLabel = node.getShortLabel();
    String className = classLabel.substring(0, classLabel.indexOf(".java"));
    String packageName = node.getPackage(this.getJProject());

    if ((packageName != null) && (packageName.trim().length() > 0)) {
      return packageName + '.' + className;
    }

    return className;
  }

  /*
   * @return a reference to active JProject
   **/
  private JProject getJProject() {
    return (JProject)super.getIDEProject();
    //return this.cachedJProject;
  }

  /**
   * Enables or disables selected actions. This method should
   * set the enabled status for actions defined for this addin, and may
   * select to set others, as well.  Other actions are delegated to the
   * supervisor.
   *
   * @param action an action to be enabled or disabled.
   * @param context the current context.
   *
   * @return  true if the controller or its supervisor sets the
   * enabled status for the action.
   */
  public boolean update(IdeAction action, Context context) {
    Controller controller = supervisor();
    if (controller == null) {
      controller = context.getView().getController();
    }
    if (controller == null) {
// FIXME: not sure if it harms anything
//      System.err.println("Can't get controller to update action: " + action.getCommand());
      return true;
    } else if (controller != this) {
      // Delegate other actions to the supervisor.
      return controller.update(action, context);
    } else {
      return true;
    }
  }

  /**
   * Initiates the updating of actions defined for this addin.  The supervisor
   * is requested to do the same for its actions. The updating will be
   * performed by the active view.
   *
   * @param context the current context.
   * @param activeController the controller of the active view,
   * or <tt>null<tt> if none.
   */
  public void checkCommands(Context context, Controller activeController) {
    // Ask the active controller to update this controller's command(s).
    if (context != null) {
      for (int i = 0; i < MenuMetadata.COMMON_IDE_ACTION_KEYS.length; i++) {
        updateActions(context, activeController,
            MenuMetadata.COMMON_IDE_ACTION_KEYS[i]);
      }

      updateActions(context, activeController, MenuMetadata.GANG_FOUR_KEYS);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_ABOUT_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_HELP_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_UPDATE_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_BROWSER_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_CROSS_HTML_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_OPTIONS_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_REBUILD_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_CLEAN_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_WHERE_USED_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_GO_TO_DECLARATION_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_RENAME_CMD_ID), context);
//      activeController.update(IdeAction.find(RefactorItController.REFACTORY_INFO_CMD_ID), context);
    }

    // Ask the supervisor to update its commands.
    try {
      supervisor().checkCommands(context, activeController);
    } catch (Exception e) {
      try {
        activeController.checkCommands(context, activeController);
      } catch (Exception e1) {
      }
//      try {
//        Object updateInfo = Class.forName("oracle.ide.update.UpdateInfo").
//            newInstance();
//        context.getView().updateVisibleActions(updateInfo);
//      } catch (Exception e) {
//      }
    }

  }

  private void updateActions(
      final Context context, final Controller activeController,
      final String[] keys
      ) {
    ActionRepository rep = ActionRepository.getInstance();

    for (int i = 0; i < keys.length; i++) {
      activeController.update((IdeAction) rep.getAction(keys[i]), context);
    }
  }

  /**
   * Starts BeanShell console in a new window
   *
   * @param context current context
   */
  public static void startBeanShell(Context context) {
    final ShellFrame frame = new ShellFrame();
    frame.setTitle("Shell");

    final Interpreter bsh = frame.getInterpreter();

    try {
      bsh.set("ctx", context);
      bsh.println("'ctx' set");
    } catch (EvalError e) {
      throw new ChainableRuntimeException("Failed to create Shell console", e);
    }

    frame.setVisible(true);
  }

  private void showNoBinObjectError() {
    RitDialog.showMessageDialog(
        createProjectContext(),
        "Can not perform refactorings on item you selected.",
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  private void showSelectionNeededError() {
    RitDialog.showMessageDialog(
        createProjectContext(),
        "This action needs a selection in the source code",
        "Selection needed",
        JOptionPane.ERROR_MESSAGE);
  }

  private void showSelectionNotNeededError() {
    RitDialog.showMessageDialog(
        createProjectContext(),
        "This action does not work with selections -- just " +
        "place the cursor on the right item, do not select it",
        "Selection not allowed",
        JOptionPane.ERROR_MESSAGE);
  }

  private void showFromIgnoredSourceError() {
    RitDialog.showMessageDialog(
        createProjectContext(),
        "This item(s) is from ignored sourcepath. " +
        "Please refer to Project Options => RefactorIT => Ignored path",
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  public static RefactorItController getJDevInstance() {
    if (instance == null) {
      instance = new RefactorItController();
    }

    return instance;
  }
}
