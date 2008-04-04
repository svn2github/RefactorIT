/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;

import org.netbeans.modules.vcscore.VcsFileSystem;
import org.netbeans.modules.vcscore.commands.VcsCommand;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class NbCommandOptions {
  private final VcsCommand cmd;
  private final FileObject fileObject;
  private final VcsFileSystem fs;
  private final Hashtable vars;
  private final boolean saveBeforeAction;
  
  private final List appliedSettings = new ArrayList();

  NbCommandOptions(
      final VcsCommand cmd,
      final FileObject fileObject,
      final VcsFileSystem fs,
      final Hashtable vars, 
      final boolean saveBeforeAction) {
    
    this.cmd = cmd;
    this.fileObject = fileObject;
    this.fs = fs;
    this.vars = vars;
    this.saveBeforeAction = saveBeforeAction;
  }
  
  Hashtable getVars() {
    return vars;
  }
  
  VcsCommand getCommand() {
    return cmd;
  }
  
  VcsFileSystem getFileSystem() {
    return fs;
  }
  
  void addRestorable(RestorableSetting setting) {
    appliedSettings.add(setting);
  }
  
  void restoreSettings() {
    Collections.reverse(appliedSettings);
    
    for (Iterator iter = appliedSettings.iterator(); iter.hasNext();) {
      ((RestorableSetting) iter.next()).restore();
    }
    
    appliedSettings.clear();
  }

  boolean isSaveBeforeAction() {
    return saveBeforeAction;
  }

  FileObject getFileObject() {
    return fileObject;
  }
}
