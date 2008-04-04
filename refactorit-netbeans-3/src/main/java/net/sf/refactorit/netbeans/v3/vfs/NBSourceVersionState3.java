/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v3.vfs;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.common.vfs.NBSourceVersionState;
import net.sf.refactorit.vfs.Source;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

/**
 * @author Juri Reinsalu
 */
public class NBSourceVersionState3 implements NBSourceVersionState {

  public String getAbsolutePath(NBSource source) {
    if (source.isRoot()) {
      String systemName;
      try {
        systemName = source.getFileObject().getFileSystem().getSystemName();
      } catch (FileStateInvalidException e) {
        systemName = "Invalid filesystem";
      }
      String folderName = FileObjectUtil.getResourceName(source.getFileObject());
      if(folderName == null) {
        FileObjectUtil.getResourceName(source.getFileObject());
      }
      if (folderName == null || folderName.length() == 0) {
        //return systemName + LINK_SYMBOL;
        return systemName; //remove LINK_SYMBOL,return wrong path with " -> "
      } else {
        //return systemName + LINK_SYMBOL + folderName + LINK_SYMBOL;
        return systemName + NBSource.SEPARATOR_STRING /*source.getSeparatorChar()*/ + folderName;
      }
    }

    String path = source.getParent().getAbsolutePath();
    if (path == null || path.length() == 0) {
      return source.getName();
    } else {
      return path + NBSource.SEPARATOR_STRING /*source.getSeparatorChar()*/ + source.getName();
    }
  }

  public String getRelativePath(NBSource nbSource) {
    if (nbSource.isRoot()) {
      return "";
    }

    String path = nbSource.getParent().getRelativePath();
    if (path.length() == 0) {
      return nbSource.getName();
    }
    return path + Source.RIT_SEPARATOR + nbSource.getName();
  }


  public FileObject renameInNbFilesystems(NBSource source, NBSource destinationDir, String name, String ext) throws IOException {
    FileObject fileObject=source.getFileObject();
    if (!"java".equalsIgnoreCase(ext)) {
      FileLock lock = fileObject.lock();
      try {
        return fileObject.move(lock, destinationDir.getFileObject(), name, ext);
      } finally {
        lock.releaseLock();
      }
    }

    DataObject dataObject = DataObject.find(fileObject);
    DataFolder destination = DataFolder.findFolder(destinationDir.getFileObject());

    // Checking validness here because fileObject.isValid() can cause a checkout
    // sometimes
    if (fileObject.isValid()) {
      source.startEdit();

      byte[] contentBeforeRename = source.getContent();

      // DataObject.rename() (in some DataObject subclass) assumes that ext ==
      // "java";
      // it also assumes that the file name is OK as a class name or something
      // similar.
      // If the name is not acceptable, it will throw an IOException.

      // Must be in this order (rename before move) -- would otherwise crash
      // (that is a speculation which is based on testing these commands in
      // separation under NB 3.5.1 RC1)
      dataObject.rename(name);
      try {
        // FIXME: With NetBeans 4.0, we should use file operation handlers
        // instead; see
        // <http://www.netbeans.org/issues/show_bug.cgi?id=35714>. Also, whole
        // JavaDataObject
        // is deprecated for 4.0, so let's make sure we're not using it anymore.
        Thread.sleep(1000);
      } catch (Exception ignore) {
      }
      dataObject.move(destination);

      try {
        Thread.sleep(1000); // one more hack to be sure :)
      } catch (Exception ignore) {
      }

      // HACK: We undo the NB's automatic constructor and class rename that
      // would otherwise
      // confuse our RenameClass refactoring. We undo this by "wrting" back the
      // old contents of the
      // file... But, we can't change the real file contents, because that could
      // cause the
      // "looping renames" problem in NB. This hack could fail refactorings in
      // some cases, becuase
      // the real file content is different than what RIT will think it is....
      // But, at the moment,
      // that's the only way to get Rename Class working properly.
      FileObject result = dataObject.getPrimaryFile();
      NBSource resultSource = NBSource.getSource(result);
      resultSource.pretendContentIs(contentBeforeRename);

      // done by DataObject.rename(); does _not_ undo the package rename
      // (that has not happened yet, it seems dataObject.move() just
      // schedules the move to some queue).

      // Must save here (at least in NB 3.4.1 RC1 and 4.0 dev-12182002).
      // If the source file has a form
      // file then that form file will be in a "modified" state after rename and
      // NB will
      // not release its locks on the entire DataObject (including java and form
      // files)
      // before we save the form file. (We need NB to release its locks because
      // otherwise
      // RefactorIT can't modify the source file.)
      SwingUtilities.invokeLater(new Runnable() {
        // Needs to be called after the NB "rename" events (and before the next
        // RIT events)
        public void run() {
          IDEController.getInstance().saveAllFiles();
        }
      });
    }
    return dataObject.getPrimaryFile();
  }

  public FileObject getFileObjectForPath(String localPath) {
    return getFileObjectForFile(new File(localPath));
  }

  public FileObject getFileObjectForFile(File localFile) {
    Repository repo = org.openide.filesystems.Repository.getDefault();
    java.util.Enumeration all = repo.getFileSystems();
    FileSystem fsByPath=null;
    outer:while (all.hasMoreElements()) {
      Object o = all.nextElement();
      if (o instanceof org.openide.filesystems.LocalFileSystem) {
        org.openide.filesystems.LocalFileSystem lfs = (org.openide.filesystems.LocalFileSystem) o;
        File temp=localFile.getParentFile();
        while(temp!=null) {
          if (lfs.getRootDirectory().equals(temp)) {
            fsByPath = lfs;
            break outer;
          }
        }
      }
    }

    if(fsByPath==null) {
      return null;
    }

    FileObject fObject = fsByPath.findResource(localFile.getAbsolutePath());
    return fObject;
  }

}
