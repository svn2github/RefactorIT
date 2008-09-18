/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.eclipse.dialog.SWTContext;
import net.sf.refactorit.eclipse.vfs.EclipseSource;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.dialog.ContextWrapper;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import javax.swing.JComponent;

import java.awt.Point;


public class EclipseContext extends TreeRefactorItContext implements SWTContext {
	private final Project project;

  final IWorkbenchPage page;

  private Object state;
  private Point  point;

	public EclipseContext(Project project) {
    this.project = project;

    // FIXME: get real window somehow depending on action context

    IWorkbench wb = PlatformUI.getWorkbench();
    IWorkbenchWindow w = wb.getActiveWorkbenchWindow();
    if (w == null) {
      w = wb.getWorkbenchWindows()[0];
    }

    IWorkbenchPage p = w.getActivePage();
    if (p == null) {
      p = w.getPages()[0];
    }

    page = p;
	}

	public Project getProject() {
		return project;
	}

	public void open(SourceHolder cu) {
    final EclipseSource src = (EclipseSource) cu.getSource();
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          IDE.openEditor(page, (IFile) src.getResource());
        } catch (PartInitException e) {}
      }
    });
	}

	public void show(SourceHolder cu, final int line, final boolean mark) {
    // TODO: line/mark
    final EclipseSource src = (EclipseSource) cu.getSource();
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          IEditorPart part = IDE.openEditor(page, (IFile) src.getResource());
          if (part instanceof AbstractTextEditor) {
            AbstractTextEditor editor = (AbstractTextEditor) part;
            IDocument document = editor.getDocumentProvider()
              .getDocument(editor.getEditorInput());
            editor.selectAndReveal(document.getLineOffset(line-1), 0);
          }
        } catch (BadLocationException e) {
        } catch (PartInitException e) {
        }
      }
    });
	}

	public void reload() {
		// Should not be needed if all filesystem operations are done
    // through IResource or any other Eclipse interface
	}

  public String getWindowId() {
    return "Eclipse" + System.identityHashCode(page);
  }

	public Object addTab(final String title, final JComponent component) {
    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          RitViewPart view = (RitViewPart) page.showView(
              RitViewPart.ID, title, IWorkbenchPage.VIEW_ACTIVATE);

          view.setContent(component);
        } catch (PartInitException e) {
        }
      }
    });

    return title;
	}

	public void removeTab(Object tab) {
    final String secondary = (String) tab;

    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        IViewReference view = page
            .findViewReference(RitViewPart.ID, secondary);
        if (view != null) {
          page.hideView(view);
        }
      }
    });
	}

	public boolean showTab(Object tab) {
    final String secondary = (String) tab;

    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          page.showView(
              RitViewPart.ID, secondary, IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
        }
      }
    });

    return true; // Can't detect from Eclipse if view already exists
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}

	public void setState(Object state) {
		this.state = state;
	}

	public Object getState() {
		return state;
	}

  public IdeWindowContext copy(RitDialog owner) {
    return new ContextWrapper(this, owner);
  }

  public Shell getShell() {
    return page.getWorkbenchWindow().getShell();
  }
}
