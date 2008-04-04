/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.utils.SwingUtil;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Juri Reinsalu
 * @author Risto
 */
public class NB40ProjectChangeTracker /*implements LookupListener*/ {
  private static Lookup lookup;
  
  private Project lastResult = null;
  
  private static NB40ProjectChangeTracker instance = null;

  public static NB40ProjectChangeTracker getInstance() {
    if (instance == null)
      instance = new NB40ProjectChangeTracker();
    return instance;
  }

  /** singleton, use getInstance() */
  private NB40ProjectChangeTracker() {
    lookup = Utilities.actionsGlobalContext();
    
    /*setUpListeners();
    checkProjectChange();*/
  }

  private boolean contains(Project[] openProjects, Project activeProject2) {
    for (int i = 0; i < openProjects.length; i++) {
      if (activeProject2.equals(openProjects[i]))
        return true;
    }
    return false;
  }
  
  Project getActiveProject() {
    Project result = getActiveProjectFromIDE();
    if(result == null && lastResult != null) {
      Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
      if (contains(openProjects, lastResult)) {
        return lastResult;
      }
    }
    
    lastResult = result;
    return result;
  }
  
  private Project getActiveProjectFromIDE() {
    Project[] projects = getProjectsFromLookup();
    if (projects.length == 1) {
      return projects[0];
    } else {
      return null;
    }
  }
  
  private Project[] getProjectsFromLookup() {
    final Project[][] result = new Project[][] {null};
    
    // A stack trace in NB log told us once that we needed this to be ran in the Swing thread
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions( new Runnable() {
      public void run() {
        result[0] = getProjectsFromLookupInCurrentThread();
      }
    } );
    
    return result[0];
  }

  private Project[] getProjectsFromLookupInCurrentThread() {
    Set result = new HashSet();

    //  First find out whether there is a project directly in the Lookup
    Collection projects = lookup.lookup(new Lookup.Template(Project.class))
            .allInstances();
    for (Iterator it = projects.iterator(); it.hasNext();) {
      Project p = (Project) it.next();
      result.add(p);
    }

    //  Now try to guess the project from dataobjects
    Collection dataObjects = lookup.lookup(
            new Lookup.Template(DataObject.class)).allInstances();
    for (Iterator it = dataObjects.iterator(); it.hasNext();) {
      DataObject dObj = (DataObject) it.next();
      FileObject fObj = dObj.getPrimaryFile();
      Project p = FileOwnerQuery.getOwner(fObj);
      if (p != null) {
        result.add(p);
      }
    }
    Project[] projectsArray = new Project[result.size()];
    result.toArray(projectsArray);

    return projectsArray;
  }
  
  
    /* COMMENTED OUT: does not work sometimes.
   * The listeners aren't called properly sometimes: under my Linux instrallation (FC1, NB 4.0)
   * when I change active editor windows so that a file from another project becomes open,
   * WITHOUT moving the cursor inside that other file, RefactorIT will not be notified of change.
   * Also, even when I do move the cursor, RefactorIT is notified a bit too late sometimes.
   */  
  /*private Project activeProject = null;
  private boolean projectOptionsInitialized = false;

  private Set listeners = null;
   
  
  //The strong references Lookup.Result's (according to Jaroslav Tulach from
  //Netbeans staff lack of these could be the cause of listener drops)
  private Lookup.Result lookupResult[];

  public Project addProjectChangeListener(ProjectChangeListener l) {
    if (listeners == null) {
      listeners = new HashSet();
    }
    listeners.add(l);
    return activeProject;
  }

  public void removeProjectChangeListener(ProjectChangeListener l) {
    if (listeners != null)
      listeners.remove(l);
  }

  private void notifyListeners(Project oldProject, Project newProject) {
    if (listeners == null)
      return;
    Iterator i = listeners.iterator();
    while (i.hasNext()) {
      ProjectChangeListener listener = (ProjectChangeListener) i.next();
      listener.ideProjectChanged(oldProject, newProject);
    }
    AppRegistry.getLogger(this.getClass()).debug("NEW PROJECT:" + newProject);
  }

  public void resultChanged(LookupEvent ev) {
    checkProjectChange();
  }

  private void checkProjectChange() {
    // knowledge gained from ProjectAction.java, LookupAction.java
    // in nb sources (use fs find)
    Project newActiveProject = getActiveProject();
    
    if (activeProject == newActiveProject) {
      return;
    }
    if (newActiveProject == null && activeProject != null) {
      Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
      if (contains(openProjects, activeProject)) {
        return;
      }
    }
    notifyListeners(activeProject, newActiveProject);
    activeProject = newActiveProject;
    if (activeProject != null && !projectOptionsInitialized) {
      projectOptionsInitialized = true;
    }
  }
   
  public void setUpListeners() {
    lookup = Utilities.actionsGlobalContext();
    Class[] watch = {Project.class, DataObject.class};
    LookupListener[]resultListeners = new LookupListener[watch.length];
    lookupResult = new Result[watch.length];
    // Needs to listen on changes in results
    for (int i = 0; i < watch.length; i++) {
      Class watchClass = watch[i];
      lookupResult[i] = lookup.lookup(new Template(watchClass));
      lookupResult[i].allItems();
      if (resultListeners[i] == null)
        resultListeners[i] = (LookupListener) WeakListeners.create(
                LookupListener.class, this, lookupResult);
      lookupResult[i].removeLookupListener(resultListeners[i]);
      lookupResult[i].addLookupListener(resultListeners[i]);
    }
  }
  
  */
}
