/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import oracle.ide.Ide;
import oracle.ide.addin.Context;
import oracle.ide.addin.View;
import oracle.ide.docking.DockableWindow;
import oracle.ide.model.Document;
import oracle.ide.model.Element;
import oracle.ide.model.Folder;
import oracle.ide.model.Project;
import oracle.ide.model.Workspace;

import javax.swing.JComponent;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.EventObject;


/**
 * This is the JDEV IDE window where Refactorings are shown. It is instantiated
 * from {@link JDevContext} as Tab window. For every refactoring result that
 * must be shown to the user, this RefactorItWindow is created and the result is
 * shown inside this RefactorItWindow.
 *
 * @author  jaanek
 */
public class RefactorItWindow extends DockableWindow {
  /**
   * This is the component that is shown to the user on screen as a
   * result of refactoring.
   */
  private JComponent refactorItResult = null;

  /** This holds the title and tab names of refactorit window */
  private String title = null;

  private Context context;

  /** Creates new RefactorItWindow */
  public RefactorItWindow(final String title, final JComponent component) {
    // call superclass constructor and provide it with parent window and
    // a VIEW_ID (title).
    super((View) AbstractionUtils.getMainWindow(), title);

    this.refactorItResult = component;
    this.title = title;

    this.context = new RefactorItContext(this, Ide.getActiveWorkspace(),
        Ide.getActiveProject(), null, null, null);
  }

  /**
   * This function return the JComponent object that is actually showed
   * on screen window to the user. This JComponent shows the result of
   * refactoring that is needed to be shown to the user.
   * The RefactorIT framework provides this JComponent for JDev extension.
   * This RefactorItWindow is actually as a container that holds this provided
   * JComponent.
   *
   * @return JComponent that is actually shown to the user, and what shows the
   *         result of some refactoring.
   */
  public JComponent getHostedComponent() {
    return this.refactorItResult;
  }

  /**
   * Returns the title of refactoring. For example: "FixMe scan" or "Metrics".
   * It is shown on screen window while it is showed to the user. As a title
   * of window.
   *
   * @return String object containing the title of refactoring.
   */
  public String getTitleName() {
    return this.title;
  }

  /**
   * This is the Tab title that is shown on Tab if this RefactorItWindow is
   * a part of TabbedPane. I.e. this RefactorItWindow can be docked into
   * another window and then it is tabbed as part of it. And the Tab name is
   * then shown on Tab.
   */
  public String getTabName() {
    return this.title;
  }

  /**
   * It returns the Context object of this RefactorItWindow. This context
   * object is used by some object who is interested on context information
   * of this RefactorItWindow.
   *
   * @return a cached context object with most properties set to null
   */
  public Context getContext(EventObject event) {
    // FIXME: something more reasonable should be returned here
    //return RefactorItController.getInstance().getLastEventContext();
    context.setEvent(event);
    return context;
  }

  /**
   */
  public Component getGUI() {
    return this.refactorItResult;
  }

  private class RefactorItContext implements Context {
    private Context delegate = null;

    public RefactorItContext(View view,
        Workspace workspace,
        Project project,
        Element[] selection,
        Document doc,
        Element elem) {
      Class[] paramClasses
          = new Class[] {View.class, Workspace.class,
          Project.class, new Element[0].getClass(),
          Document.class, Element.class};
      Object[] params
          = new Object[] {view, workspace, project, selection, doc, elem};
      try {
        this.delegate = (Context) Class.forName(
            "oracle.ide.addin.DefaultContext")
            .getDeclaredConstructor(paramClasses).newInstance(params);
      } catch (Exception e) {
        // AbstractContext is abstract, so we need to extend it
        this.delegate = new oracle.ide.addin.AbstractContext(
            view, workspace, project, selection, doc, elem) {};
      }
    }

    public View getView() {
      return delegate.getView();
    }

    public Workspace getWorkspace() {
      return delegate.getWorkspace();
    }

    public Project getProject() {
      return delegate.getProject();
    }

    public EventObject getEvent() {
      return delegate.getEvent();
    }

    public void setEvent(EventObject eventobject) {
      delegate.setEvent(eventobject);
    }

    public Element[] getSelection() {
      return delegate.getSelection();
    }

    public Document getDocument() {
      return delegate.getDocument();
    }

    public Element getElement() {
      return delegate.getElement();
    }

    public Object getExtraData() {
      return delegate.getExtraData();
    }

    public void setExtraData(Object obj) {
      delegate.setExtraData(obj);
    }

    public Folder findOwner(Element element) {
      return delegate.findOwner(element);
    }

    /** This is new method in JDev 10g 9.0.5... */
    public Context makeCopy() {
      try {
        final RefactorItContext newContext = (RefactorItContext)this.clone();
        try {
          Method makeCopy = this.delegate.getClass().getMethod(
              "makeCopy", new Class[0]);
          newContext.delegate = (Context) makeCopy.invoke(
              this.delegate, new Object[0]);
        } catch (Exception e) {
          newContext.delegate = this.delegate;
        }
        return newContext;
      } catch (Exception e) {
        return null;
      }
    }

    public void setView(View view) {
    }

    public void setWorkspace(Workspace workspace) {
    }

    public void setProject(Project project) {
    }

    public void setSelection(Element[] elementArray) {
    }

    public void setDocument(Document document) {
    }
  }
}
