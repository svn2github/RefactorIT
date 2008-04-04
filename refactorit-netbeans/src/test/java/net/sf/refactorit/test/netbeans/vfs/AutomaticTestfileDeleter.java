/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;
import net.sf.refactorit.vfs.AbstractSource;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.RepositoryEvent;
import org.openide.filesystems.RepositoryListener;
import org.openide.filesystems.RepositoryReorderedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AutomaticTestfileDeleter {
  private static class CreatedFilesDeleter implements FileChangeListener, TestfileCreationListener {
    // We need them synchronized because for some reason 
    // the CVS tests cause files to be created and deleted at the same time
    // (but this may be a bug that'll be found and fixed some day).
    private List createdFiles = Collections.synchronizedList(new ArrayList());
    private List listenees = Collections.synchronizedList(new ArrayList());
    
    public void fileFolderCreated(FileEvent e) {
      createdFiles.add(e.getFile());
      startListening(e.getFile());
    }

    public void fileDataCreated(FileEvent e) {
      createdFiles.add(e.getFile());
    }
    
    public void fileChanged(FileEvent e) {}
    public void fileDeleted(FileEvent e) {}
    public void fileRenamed(FileRenameEvent e) {}
    public void fileAttributeChanged(FileAttributeEvent e) {}

    public void startListening() {
      for (Iterator iter = getFilesystemRoots().iterator(); iter.hasNext();) {
        FileObject f = (FileObject) iter.next();
        
        startListening(f);
      }
    }
    
    private void startListening(FileObject f) {
      addFileChangeListener(f);
      
      FileObject[] children = f.getChildren();
      for (int i = 0; i < children.length; i++) {
        startListening(children[i]);
      }
    }

    private void addFileChangeListener(FileObject f) {
      f.addFileChangeListener(this);
      listenees.add(f);
    }

    public void stopListening() {
      for (Iterator iter = listenees.iterator(); iter.hasNext();) {
        ((FileObject) iter.next()).removeFileChangeListener(this);
        iter.remove();
      }
    }
    
    public void deleteCreatedFiles() {
      List snapshotOfCreatedFiles = new ArrayList(createdFiles);
      Collections.reverse(snapshotOfCreatedFiles);
      
      for (Iterator i = snapshotOfCreatedFiles.iterator(); i.hasNext();) {
        FileObject f = (FileObject) i.next();
        createdFiles.remove(f);
        
        try {
          if(localFile(f)) { // If we delete CVS files then we ruin our NB CVS integeration tests
            f.delete();
          }
        } catch (IOException e) {
          AppRegistry.getExceptionLogger().error(e, this);
          throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
        }
      }
    }
    
    private boolean localFile(FileObject f) {
      return ! inVcs(f) && 
          ( ! AbstractSource.inVersionControlDirList(f.getNameExt())) && 
          ( ! AbstractSource.inVersionControlDirList(f.getParent().getNameExt()));
    }

    private boolean inVcs(FileObject f) {
      return CvsFileStatus.getInstance().isKnown(
          FileObjectUtil.getFileOrNull(f));
    }

    // Assumes path autiod
    private List getFilesystemRoots() {
      Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      Object projectKey = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(ideProject);
      Assert.must(NBProjectOptions.getInstance(projectKey).getAutodetectPaths(),
          "This method only supports path autodetection -- " + 
          "otherwise we'd need special support for LocalFileSystem");
      PathItemReference[] roots = PathUtil.getInstance()
          .getAutodetectedSourcepath(ideProject, false);
      
      List result = new ArrayList();
      for (int i = 0; i < roots.length; i++) {
        result.add(roots[i].getFileObject());
      }
      return result;
    }
  }

  private static class MountedFilesystemsUnmounter implements RepositoryListener, TestfileCreationListener {
    private List addedFilesystems = new ArrayList();

    public void fileSystemRemoved(RepositoryEvent event) {
      addedFilesystems.remove(event.getFileSystem());
    }

    public void fileSystemAdded(RepositoryEvent event) {
      addedFilesystems.add(event.getFileSystem());
    }

    public void fileSystemPoolReordered(RepositoryReorderedEvent event) {}
    
    public void deleteCreatedFiles() {
      for (Iterator i = new ArrayList(addedFilesystems).iterator(); i.hasNext(); ) {
        Repository.getDefault().removeFileSystem((FileSystem) i.next());
      }
    }

    public void startListening() {
      Repository.getDefault().addRepositoryListener(this);
    }

    public void stopListening() {
      Repository.getDefault().removeRepositoryListener(this);
    }
  }
  
  public interface TestfileCreationListener {
    void deleteCreatedFiles();

    void startListening();

    void stopListening();
  }

  private List deleters = new ArrayList() {{
    add(new CreatedFilesDeleter());
    
    if(RefactorItActions.isNetBeansThree()) { 
      add(new MountedFilesystemsUnmounter());
    }
  }};
  
  public void startListening() {
    for (Iterator i = deleters.iterator(); i.hasNext();) {
      TestfileCreationListener listener = (TestfileCreationListener) i.next();
      listener.startListening();
    }
  }

  public void stopListening() {
    for (Iterator i = deleters.iterator(); i.hasNext();) {
      TestfileCreationListener listener = (TestfileCreationListener) i.next();
      listener.stopListening();
    }
  }

  public void deleteCreatedFiles() {
    for (Iterator i = deleters.iterator(); i.hasNext();) {
      TestfileCreationListener listener = (TestfileCreationListener) i.next();
      listener.deleteCreatedFiles();
    }
  }
}
