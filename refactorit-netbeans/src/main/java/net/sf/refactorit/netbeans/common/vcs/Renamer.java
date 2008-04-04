/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.Attempter;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;


/**
 * @author risto
 */
public class Renamer {
  private static final Logger log = Logger.getLogger(Renamer.class);
  
  static FileObject rename(final FileObject fileObject, final FileObject destination, final String name, String ext) {
    log.debug("$$$ Rename starts");
    if (FileSystemProperties.isVssFileSystem(fileObject)) {
      return vssRename(fileObject, destination, name, ext);
    } else {
      return cvsRename(fileObject, destination, name, ext);
    }
  }

  private static FileObject cvsRename(final FileObject fileObject, 
      final FileObject destination, final String name, final String ext) {
    
    final FileObject result[] = new FileObject[] {null};
    
    try {
      log.debug("$$$ starting cvs rename");
      if(uncommitedCvsRemove(destination, name, ext)) {
        log.debug("Uncommited CVS remove detected");
        boolean binary = CvsFileStatus.getInstance().isBinary(
            FileObjectUtil.getFileOrNull(fileObject));
        
        // Here safeGetFileObject would give null -- the file does not exist, afterall -- but we need the FO in here
        FileObject existing = destination.getFileObject(name, ext);
        
        log.debug("$$$ If resurrection fails in here, maybe we should *create* that file");
        if (!resurrectFromUncommitedCvsRemove(existing, binary)) {
          log.debug("$$$ Indeed, resurrection failed");
          throw new VcsRunner.FileAlreadyExistsException();
        }

        existing.delete();
        result[0] = fileObject.copy(destination, name, ext);
        log.debug("$$$ result: " + result[0]);
      } else if (FileObjectUtil.safeGetFileObject(destination, name, ext) != null) {
        log.debug("$$$ Destination exists, cannot rename");
        throw new VcsRunner.FileAlreadyExistsException();
      } else {
        log.debug("$$$ Destination does not exist");
        
        FileObject existingFake = destination.getFileObject(name, ext);
        if(existingFake != null) {
          log.debug("$$$ Existing fake exists");
          existingFake.delete();
        }

        try {
          result[0] = fileObject.copy(destination, name, ext);
        } catch(NullPointerException e) {
          
          log.debug("$$$ First copy had a NPE, will attempt again", e);
          boolean success = new Attempter(5000, 100).attempt(new Attempter.Task() {
            public boolean attempt() {
              destination.refresh();
              log.debug("$$$ Destination is: " + destination + ", instanceof: " + destination.getClass());
              
              // Using java.util.File here, FileObject's detection did not work there
              File folder = FileObjectUtil.getFileOrNull(destination);
              File existing = new File(folder, FileObjectUtil.getFileName(name, ext));
              if(existing.exists()) {
                if( ! existing.delete() ) {
                  log.debug("$$$ Failed to delete an existing file");
                } else {
                  destination.refresh();
                  log.debug("$$$ Deleted existing file");
                }
              } else {
                log.debug("$$$ Could not detect an existing file");
              }
              
              try {
                result[0] = fileObject.copy(destination, name, ext);
                log.debug("$$$ Another attempt: success");
                return true;
              } catch(NullPointerException e) {
                return false;
              } catch (IOException e) {
                log.debug("$$$ Another attempt brought a new exception");
                AppRegistry.getExceptionLogger().error(e, this);
                throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
              }
            }
          });
          
          if( ! success ) {
            log.warn("Kept getting NullPointerExceptions on several attempts to rename " +
                fileObject, e);
            return null;
          }
        }
        
        if (CvsFileStatus.getInstance().isKnown(FileObjectUtil.getFileOrNull(
            destination))) {
          boolean binary = CvsFileStatus.getInstance().isBinary(
              FileObjectUtil.getFileOrNull(fileObject));

          Vcs.add(result[0], binary);
        }
      }

      if ( ! fileObject.canWrite()) {
        Vcs.edit(fileObject);
      }
      Vcs.remove(fileObject);
      
      return result[0];
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
      return null;
    }
  }

  private static FileObject vssRename(final FileObject fileObject, final FileObject destination, final String name, String ext) {
    FileObject result = null;
    
    final boolean isRename = FileObjectUtil.getResourceName(fileObject.getParent())
      .equals(FileObjectUtil.getResourceName(destination));
    if (isRename) {
      if ( ! fileObject.canWrite()) {
        Vcs.checkout(fileObject);
      }
      boolean success = Vcs.vssRename(fileObject, destination, name + '.' + ext);
      if (success) {
        destination.refresh();
        Vcs.getr(destination);
 //          if (Assert.enabled) {
        log.debug("REFACTORIT: Children: "
            + Arrays.asList(destination.getChildren()));
 //          }
        result = FileObjectUtil.safeGetFileObject(
            destination, name, ext);
      }
      
 //        if (Assert.enabled) {
      Assert.must(result != null, "New FO is null after "
          + (isRename ? "RENAME" : "SHARE") + " of: " + fileObject);
 //        }
      log.debug("REFACTORIT: New FO: " + result);
      if (result != null) {
        Vcs.get(result);
      }
    } else {
      if ( ! fileObject.canWrite()) {
        Vcs.checkout(fileObject);
      }
      boolean success = Vcs.share(fileObject, destination, name + '.' + ext);
      if (! success) {
        destination.refresh();
        Vcs.getr(destination);
 //            if (Assert.enabled) {
        log.debug("REFACTORIT: Children: "
            + Arrays.asList(destination.getChildren()));
 //            }
        result = FileObjectUtil.safeGetFileObject(
            destination, name, ext);
        
 //          if (Assert.enabled) {
        Assert.must(result != null, "New FO is null after "
            + (isRename ? "RENAME" : "SHARE") + " of: " + fileObject);
 //          }
        log.debug("REFACTORIT: New FO: " + result);
        if (result != null) {
          Vcs.checkout(result);
        }
        if ( ! fileObject.canWrite()) {
          Vcs.checkout(fileObject);
        }
        Vcs.remove(fileObject);
      }
    }
    return result;
  }

  private static boolean uncommitedCvsRemove(FileObject folder, String name, String ext) {
    File parent = FileObjectUtil.getFileOrNull(folder);
    if(parent == null || (!parent.exists())) {
      log.debug("$$$ parent does not exist");
      return false;
    }

    File f = new File(parent, FileObjectUtil.getFileName(name, ext));
    return CvsFileStatus.getInstance().isRemoved(f);
  }

  private static boolean resurrectFromUncommitedCvsRemove(FileObject fo, boolean binary) {
    // HACK: we should try to do without this Thread.sleep()
    // Perhaps it would have been more appropriate to append this sleep()
    // statement to the delete() command, not here, as I beleive this
    // depends on how recently the file-to-be-resurrected was removed,
    // but this way the users don't have to suffer throigh the extra 1,5 seconds
    // sleep on every delete() operation, and I do not know of any other
    // cases where this should matter (although perhaps there are other cases like this).
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      throw new ChainableRuntimeException(e);
    }

    return Vcs.add(fo, binary);
  }
}
