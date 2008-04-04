/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4.vfs;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.common.vfs.NBSourceVersionState;
import net.sf.refactorit.vfs.AbstractSource;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Juri Reinsalu
 */
public class NBSourceVersionState4 implements NBSourceVersionState {

  private static final String NETBEANS_REFACTORING_LISTENER
      = "org.netbeans.modules.refactoring.ui.RefactoringOperationListener";
  private static final String NETBEANS_REFACTORING_MODULE
      = "org.netbeans.modules.refactoring";

  public String getAbsolutePath(NBSource source) {
    File file = FileObjectUtil.getFileOrNull(source.getFileObject());
    if(file == null) {
      return "";
    }

    return file.getAbsolutePath();
  }

  public String getRelativePath(NBSource nbSource) {
    final FileObject fileObject = nbSource.getFileObject();
    ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
    String path = cp.getResourceName(fileObject);
    if(path == null) {

      // if NBSource is JSP source
      WebModule module = WebModule.getWebModule(fileObject);
      if(module != null) {
        FileObject documentBaseObject = module.getDocumentBase();
        String documentRoot = FileUtil.toFile(documentBaseObject).getAbsolutePath();
        String jspFile = FileUtil.toFile(fileObject).getAbsolutePath();
        path = jspFile.substring(jspFile.indexOf(documentRoot) + documentRoot.length());
        path = AbstractSource.normalize(path); //path.replace('\\', '/');
      }
    }
    return path;
  }

  public FileObject renameInNbFilesystems(final NBSource source, final NBSource destinationDir,
      final String name, final String ext) throws IOException {

    disableNbAutorefactoring();

    try {
      FileObject fileObject=source.getFileObject();
      if (!"java".equalsIgnoreCase(ext)) {
        FileLock lock = fileObject.lock();
        try {
          return fileObject.move(lock, destinationDir.getFileObject(), name, ext);
        } finally {
          lock.releaseLock();
        }
      }

      final DataObject dataObject = DataObject.find(fileObject);
      final DataFolder destination = DataFolder.findFolder(destinationDir.getFileObject());

      // Checking validness here since fileObject.isValid() may cause a checkout
      if (fileObject.isValid()) {
        source.startEdit();

        // JavaDataObject.rename() assumes ext.equals("java"). Also,
        // it throws an IOException if the name is not a legal Java class name.
        attemptSeveralTimes(new RunnableAttempt() {
          private Exception exception = null;

          public boolean run() throws IOException {
            try {
              dataObject.rename(name);
              return true;
            } catch(Exception e) {
              exception = e;
              return false;
            }
          }

          public void notifyNoMoreAttempts() {
            throw new RuntimeException(exception);
          }
        } );

        final FileObject toMove = dataObject.getPrimaryFile();
        toMove.refresh();

        // Not sure if this helps any; needs lots of testing
        try{Thread.sleep(1000);}catch(InterruptedException e) {throw new RuntimeException(e);}


        // Workaround for a NB 4.0 beta 2 NPE that I had
        attemptSeveralTimes(new RunnableAttempt() {
          private Exception exception = null;

          public boolean run() throws IOException {
            try {
              dataObject.move(destination);
              return true;
            } catch(Exception e) {
              exception = e;

              FileObject newChild = destination.getPrimaryFile().getFileObject(toMove.getNameExt());
              if( FileObjectUtil.exists(toMove) && newChild != null) {
                // Failed move operation sometimes just copies the file but does not delete it from the old place
                newChild.delete();
              }

              return false;
            }
          }

          public void notifyNoMoreAttempts() {
            throw new RuntimeException(exception);
          }
        });


        IDEController.getInstance().saveAllFiles();
      }
      return dataObject.getPrimaryFile();
    } finally {
      enableNbAutorefactoring();
    }
  }

  private void attemptSeveralTimes(RunnableAttempt runnable) throws IOException {
    final int TIMEOUT = 10000;

    long startTime = System.currentTimeMillis();

    if(runnable.run()) {
      return;
    }

    while(System.currentTimeMillis() - startTime < TIMEOUT) {
      try {Thread.sleep(100);} catch(InterruptedException ex) {throw new RuntimeException(ex);}

      if(runnable.run()) {
        return;
      }
    }

    runnable.notifyNoMoreAttempts();
  }

  private void enableNbAutorefactoring() {
    final Class refactoringOperationListener = loadOtherModulesHiddenClass(
        NETBEANS_REFACTORING_MODULE, NETBEANS_REFACTORING_LISTENER);
    ReflectionUtil.invokeMethod(refactoringOperationListener,
        "addOperationalListener");
  }

   /** A hack to make sure NB won't show its own refactoring dialogs on our move operations */
  private void disableNbAutorefactoring() {
    final Class refactoringOperationListener = loadOtherModulesHiddenClass(
        NETBEANS_REFACTORING_MODULE, NETBEANS_REFACTORING_LISTENER);
    ReflectionUtil.invokeMethod(refactoringOperationListener,
        "removeOperationalListener");
  }

  private Class loadOtherModulesHiddenClass(String moduleCodeBaseName, String className) {
    try {
      return getModuleInfo(moduleCodeBaseName).getClassLoader().loadClass(className);
    } catch(ClassNotFoundException e) {
      AppRegistry.getLogger(NBSourceVersionState4.class).error("", e);
      throw new SystemException("", e);
    }
  }

  private ModuleInfo getModuleInfo(final String codeNameBase) {
    ModuleInfo result = null;

    Collection nbModules = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allItems();
    for (Iterator i = nbModules.iterator(); i.hasNext();) {
      Lookup.Item item = (Lookup.Item) i.next();
      ModuleInfo info = (ModuleInfo) item.getInstance();
      if(info.getCodeNameBase().equals(codeNameBase)) {
        result = info;
      }
    }
    return result;
  }

  public FileObject getFileObjectForPath(String localPath) {
    return getFileObjectForFile(new File(localPath));
  }

  public FileObject getFileObjectForFile(File localFile) {
    return org.openide.filesystems.FileUtil.toFileObject(localFile);
  }

  public static interface RunnableAttempt {

    /**
     * @return true on success
     * @throw java.io.IOException on a failure that should not be re-attempted
     */
    boolean run() throws IOException;

    void notifyNoMoreAttempts();
  }
}
