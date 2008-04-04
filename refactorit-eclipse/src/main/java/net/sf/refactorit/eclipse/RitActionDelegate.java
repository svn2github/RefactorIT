/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.ItemByCoordinateFinder;
import net.sf.refactorit.commonIDE.NotFromSrcOrFromIgnoredException;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.FileUtil;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import javax.swing.JOptionPane;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 *
 * @author Igor Malinin
 */
public class RitActionDelegate
implements
    IObjectActionDelegate,
    IEditorActionDelegate,
    IWorkbenchWindowActionDelegate
{
  private static final Logger log = AppRegistry.getLogger(RitActionDelegate.class);

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    final String id = action.getActionDefinitionId();

    // selection shall be saved in the very beginning!
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    ISelectionService service = window.getSelectionService();
    final ISelection selection = service.getSelection();

    //final Point awtPoint = getCursorLocation();//new java.awt.Point(swtPoint.x, swtPoint.y);


    // start in different thread than eclipse
    // interface thread to avoid deadlocks
    Job job = new Job("RefactorIT / " + action.getText()) {
      /*
       * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
       */
      protected IStatus run(IProgressMonitor monitor) {
        runJob(id, selection);
        return Status.OK_STATUS;
      }
    };

    job.setSystem(true);
    job.schedule();
  }

  void runJob(String id, ISelection selection) {
    if (!RefactorITLock.lock()) {
      return;
    }

    try {
      IDEController controller = IDEController.getInstance();

      Object ritAction = controller.getActionRepository().getAction(id);

      Assert.must(ritAction != null);

      if (ritAction instanceof IdeAction) {
        RefactorItActionUtils.run((IdeAction) ritAction);
      } else {
        if (controller.getActiveProjectFromIDE() == null) {
          log.debug("IDE Project not found");
          RitDialog.showMessageDialog(
              controller.createProjectContext(),
              "Can't detect java project from current selection!",
              "RefactorIT", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        if (!controller.ensureProject()) {
          log.debug("Failed ensure project");
          return;
        }

        Object[] object;

        try {
          object = getBinObject(selection);
        } catch (NotFromSrcOrFromIgnoredException e) {
          log.debug("Selection from ignored source");
          DialogManager.getInstance().showWarning(
              IDEController.getInstance().createProjectContext(),
              "warning.action.not_from_source_or_from_ignored_source_eclipse");
          return;
        }

        if (object == null || object.length == 0) {
          // no target found found
          log.debug("no BinObject found");
          DialogManager.getInstance().showWarning(
              IDEController.getInstance().createProjectContext(),
              "warning.action.unit.error");
          return;
        }

        RefactorItAction ra = (RefactorItAction) ritAction;
        RefactorItContext context = controller.createProjectContext();

        if (!ra.isAvailableForTarget(object)) {
          RitDialog.showMessageDialog(context,
              ra.getName() + " action is not applicable to current selection!",
              "RefactorIT", JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        context.setPoint(RitActionDelegate.getCursorLocation());

        if (RefactorItActionUtils.run(ra, context, object)) {
          ra.updateEnvironment(context);
//          browser.getProjectView().refreshTree();
        } else {
          ra.raiseResultsPane(context);
        }
      }
    } catch (Throwable e) {
      AppRegistry.getExceptionLogger().error(e, this);
      DialogManager.getInstance().showError(
          IDEController.getInstance().createProjectContext(),
          "Error occured", e);
    } finally {
      RefactorITLock.unlock();
    }
  }

  public static Point getCursorLocation() {
    final Point[] point = {null};
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {
        org.eclipse.swt.graphics.Point p = Display.getCurrent().getCursorLocation();
        point[0] = new Point(p.x, p.y);
      }
    });
    return point[0];
  }

  private Object[] getBinObject(final ISelection selection)
      throws NotFromSrcOrFromIgnoredException {
    final Object[][] res = {null};
    final NotFromSrcOrFromIgnoredException[] exceptionCaught =
        new NotFromSrcOrFromIgnoredException[1];

    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          res[0] = getBinObject0(selection);
        } catch (NotFromSrcOrFromIgnoredException e) {
          exceptionCaught[0] = e;
        }
      }
    });

    if (exceptionCaught[0] != null) {
      throw exceptionCaught[0];
    }

    return (Object[]) res[0];
  }

  Object[] getBinObject0(ISelection selection)
      throws NotFromSrcOrFromIgnoredException {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;

      ArrayList result = new ArrayList(3);
      for (Iterator i = ss.iterator(); i.hasNext();) {
        Object binElement = null;

        final Object element = i.next();
        if (element instanceof IResource) {
          IJavaElement je = JavaCore.create((IResource) element);
          if (je != null) {
            binElement = getBinObject(je);
          }
        } else if (element instanceof IJavaElement) {
            binElement = getBinObject((IJavaElement) element);
        } else {
          log.debug("selected element == " + element.getClass());
        }

        if (binElement != null) {
          result.add(binElement);
        }
      }

      return result.toArray();
    }

    if (selection instanceof ITextSelection) {
      Object binObject = getBinObject((ITextSelection) selection);
      if(binObject != null) {
        return new Object[] { binObject };
      }
    }

    return null;
  }

  private Object getBinObject(IJavaElement element)
      throws NotFromSrcOrFromIgnoredException {
    Project project = IDEController.getInstance().getActiveProject();

    try {
      if (element instanceof IJavaProject) {
        return project;
      }

      if (element instanceof IPackageFragmentRoot) {
        return project;
      }

      if (element instanceof IPackageDeclaration) {
        IPackageDeclaration pd = (IPackageDeclaration) element;
        return ItemByNameFinder.findBinPackage(project, pd.getElementName());
      }

      if (element instanceof IPackageFragment) {
        IPackageFragment pf = (IPackageFragment) element;
        return ItemByNameFinder.findBinPackage(project, pf.getElementName());
      }

      if (element instanceof ICompilationUnit) {
        ICompilationUnit cu = (ICompilationUnit) element;
        IType type = cu.findPrimaryType();
        if (type != null) {
          return getBinObject(type);
        }
        return null;
      }

      if (element instanceof IClassFile) {
        return getBinObject(((IClassFile) element).getType());
      }

      if (element instanceof IType) {
         IType type = (IType) element;
        if (type.isClass() || type.isInterface()) {
          String typeName = type.getFullyQualifiedName();

          Object o = ItemByNameFinder.findBinCIType(project, typeName);

          if (o != null) {
            return o;
          } else {
              throw new NotFromSrcOrFromIgnoredException();
          }
        }
        return null; // TODO: enum/annotation
      }

      if (element instanceof IMember) {
        IMember member = (IMember) element;
        IType type = member.getDeclaringType();

        BinCIType btype = ItemByNameFinder
            .findBinCIType(project, type.getFullyQualifiedName());

        if(btype == null) {
          throw new NotFromSrcOrFromIgnoredException();
        }

        if (member instanceof IField) {
          return ItemByNameFinder.findBinField(btype, member.getElementName());
        }

        if (member instanceof IMethod) {
          IMethod method = (IMethod) member;

          String[] signatures = method.getParameterTypes();
          String[] args = new String[signatures.length];
          for (int i = 0; i < args.length; i++) {
            args[i] = Signature.toString(signatures[i]);
          }

          if (method.isConstructor()) {
            return ItemByNameFinder.findBinConstructor((BinClass) btype, args);
          }

          return ItemByNameFinder
              .findBinMethod(btype, member.getElementName(), args);
        }

        if (element instanceof IInitializer) {
          return null; // TODO: initializer
        }
      }
    } catch (JavaModelException e) {
      log.debug("getBinClass()", e);
    }

    return null;
  }

  private Object getBinObject(ITextSelection selection)
      throws NotFromSrcOrFromIgnoredException {
    IFile file = EclipseController.getFileFromActiveEditor();

    if (file != null) {
      return getBinObject(file, selection);
    }

    return null;
  }

  private Object getBinObject(IFile file, ITextSelection selection)
      throws NotFromSrcOrFromIgnoredException {
    String qClassName = null;

    ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
    if (cu != null) {
      IType type = cu.findPrimaryType();
      if (type != null) {
        qClassName = type.getFullyQualifiedName();
      }
    } else {
      if (FileUtil.isJspFile(file.getName())) {
        qClassName = JspUtil.getClassName(file.getName());
      }
    }

    if (qClassName == null) {
      log.debug("Couldn't find bin object, selected IResource: " + selection);
      return null;
    }

    Project project = IDEController.getInstance().getActiveProject();

    BinCIType binType = ItemByNameFinder.findBinCIType(project,qClassName );
    if (binType == null) {
      // something wrong? shouldn't happen normally
      log.warn("finding type failed for "+qClassName);
      throw new NotFromSrcOrFromIgnoredException();
    }

    CompilationUnit source = binType.getCompilationUnit();

    int offset = selection.getOffset();
    int length = selection.getLength();

    if (length <= 0) {
      ItemByCoordinateFinder finder = new ItemByCoordinateFinder(source);
      SourceCoordinate sc = source.getLineIndexer().posToLineCol(offset);
      return finder.findItemAt(sc);
    }

    return new BinSelection(source, null, offset, offset + length);
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
  }

  /*
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,org.eclipse.ui.IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
  }

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
  }
}
