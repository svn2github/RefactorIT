/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import java.io.IOException;

import net.sf.refactorit.ui.RuntimePlatform;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

/**
 *
 * @author risto
 */
public class NBShortcutsInstaller {
  private static final Logger log = Logger.getLogger(NBShortcutsInstaller.class);
  
  private static final String EXT_INSTANCE = "instance";
  private static final String ATTR_INSTANCE_CLASS = "instanceClass";
  
  public static void installShortcuts(){
		deleteAllShortcuts();
	
    
    installShortcut("CA-U", "net.sf.refactorit.netbeans.common.action.UndoNBAction");
    installShortcut("CA-Y", "net.sf.refactorit.netbeans.common.action.RedoNBAction");
    installShortcut("CA-P", "net.sf.refactorit.netbeans.common.action.AuditAction");
    installShortcut("CA-B", "net.sf.refactorit.netbeans.common.action.CreateMissingMethodAction");
    installShortcut("CA-X", "net.sf.refactorit.netbeans.common.action.ExtractMethodAction");
    installShortcut("CA-I", "net.sf.refactorit.netbeans.common.action.IntroduceTempAction");
    installShortcut("CA-V", "net.sf.refactorit.netbeans.common.action.CallTreeAction");
    installShortcut("CA-F", "net.sf.refactorit.netbeans.common.action.DrawDependenciesNBAction");
    installShortcut("CA-O", "net.sf.refactorit.netbeans.common.action.DependenciesAction");
    installShortcut("CA-G", "net.sf.refactorit.netbeans.common.action.GotoAction");
    installShortcut("CA-Q", "net.sf.refactorit.netbeans.common.action.JavadocNBAction");
    installShortcut("CA-T", "net.sf.refactorit.netbeans.common.action.TypeNBAction");
    installShortcut("CA-W", "net.sf.refactorit.netbeans.common.action.WhereCaughtAction");
    installShortcut("CA-E", "net.sf.refactorit.netbeans.common.action.MetricsAction");
    
    if(RuntimePlatform.isMacOsX()) {
      installShortcut("C-I", "net.sf.refactorit.netbeans.common.action.InlineAction");
      installShortcut("C-M", "net.sf.refactorit.netbeans.common.action.MoveAction");
      installShortcut("C-Y", "net.sf.refactorit.netbeans.common.action.RenameAction");
      installShortcut("C-X", "net.sf.refactorit.netbeans.common.action.WhereAction");
    } else {
      installShortcut("A-I", "net.sf.refactorit.netbeans.common.action.InlineAction");
      installShortcut("A-M", "net.sf.refactorit.netbeans.common.action.MoveAction");
      installShortcut("A-Y", "net.sf.refactorit.netbeans.common.action.RenameAction");
      installShortcut("A-X", "net.sf.refactorit.netbeans.common.action.WhereAction");
    }
  }

  public static void installShortcut(final String keycode, final String instanceClass) {
    try {
      if (occupiedByAnotherModule(keycode)) {
        throw new RuntimeException("Shortcut occupied by another module (instanceClass is " + 
            getInstanceClass(getExistingShortcut(keycode)) + ")");
      }
      
      doCreateShortcut(keycode, instanceClass);
      
    } catch(Exception e) {
      log.warn("Failed to assign shortcut " + keycode + " to action " + instanceClass);
    }
  }

  private static void doCreateShortcut(final String keycode, final String instanceClass)
      throws IOException {

    FileObject f = getShortcutsFolder().createData(keycode, EXT_INSTANCE);
    f.setAttribute(ATTR_INSTANCE_CLASS, 
        instanceClass);
  }

  private static boolean occupiedByAnotherModule(final String keycode) {
    FileObject existingShortcut = getExistingShortcut(keycode);
    return existingShortcut != null && ( ! belongsToRefactorit(existingShortcut));
  }

  private static boolean belongsToRefactorit(final FileObject shortcut) {
    return getInstanceClass(shortcut) != null &&
        getInstanceClass(shortcut).startsWith("net.sf.refactorit");
  }

  public static String getInstanceClass(final FileObject shortcut) {
    return (String) shortcut.getAttribute(ATTR_INSTANCE_CLASS);
  }

  public static FileObject getExistingShortcut(final String keycode) {
    return getShortcutsFolder().getFileObject(keycode, EXT_INSTANCE);
  }

  private static FileObject getShortcutsFolder() {
    return Repository.getDefault().getDefaultFileSystem().
        getRoot().getFileObject("Shortcuts");
  }

  public static void deleteAllShortcuts() {
    FileObject[] allShortcuts = getShortcutsFolder().getChildren();
    for(int i = 0; i < allShortcuts.length; i++ ) {
      if(/*belongsToRefactorit*/getInstanceClass(allShortcuts[i]) != null) {
        try {
          allShortcuts[i].delete();
        } catch(IOException e) {
          log.warn("Failed to delete an existing shortcut (" + allShortcuts[i] + ")", e);
        }
      }
    }
  }
}
