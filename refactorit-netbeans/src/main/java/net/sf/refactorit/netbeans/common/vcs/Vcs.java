/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;


import net.sf.refactorit.common.util.Attempter;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;

import org.apache.log4j.Logger;
import org.netbeans.api.vcs.VcsManager;
import org.netbeans.api.vcs.commands.AddCommand;
import org.netbeans.api.vcs.commands.Command;
import org.netbeans.api.vcs.commands.CommandTask;
import org.openide.filesystems.FileObject;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author risto
 */
public class Vcs {
  private static Logger log = Logger.getLogger(Vcs.class);
  
  private static final long CVS_COMMAND_TIMEOUT = 10000;
  
  private static final Attempter attempter = new Attempter(CVS_COMMAND_TIMEOUT, 20);

  public static boolean update(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      // Automatic update-invocation does not work very well for some reason
      showNonmodalMessageDialog("Run MANUALLY: 1) Update 2) Refresh Recursively");
      return true;
    }
    
    return VcsRunner.execute(fileObject, "UPDATE", false, true, false);
  }

  public static boolean commit(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      // Autmatic commit-invocation does not work for some reason
      showNonmodalMessageDialog("COMMIT Changes MANUALLY");
      return true;
    }
    
    return VcsRunner.execute(fileObject, "COMMIT", false, true, false);
  }
  
  public static boolean add(FileObject fileObject, boolean binary) {
    if( ! performAdd(fileObject, binary)) {
      return false;
    }
    
    if (CvsFileStatus.getInstance().isBinary(
        FileObjectUtil.getFileOrNull(fileObject)) != binary) {
      ErrorDialog.error("Failed to set or remove a CVS 'binary file' flag: " +
          fileObject.getNameExt());      
    }
    return true;
  }
  
  private static boolean performAdd(FileObject fileObject, boolean binary) {
    if(RefactorItActions.isNetBeansFour()) {
      AddCommand cmd = (AddCommand) getNb40Command("ADD", fileObject);
      cmd.setBinary(binary);
      boolean result = execute(cmd);
      
      if( ! result) {
        return false;
      }
      
      final File f = FileObjectUtil.getFileOrNull(fileObject);
      if(f != null) {
        result = attempter.attempt(new Attempter.Task() {
          public boolean attempt() {
            return CvsFileStatus.getInstance().isKnown(f);
          }
        });
        return result;
      }
      
      return true;
    } else {
      NbCommandOptions command = VcsRunner.createNbCommand("ADD", fileObject, false, true, true);
      if(command == null) {
        return false;
      }
      
      BinaryFilesMode setting = new BinaryFilesMode(command.getFileSystem(), command.getCommand());
      setting.setBinaryFilesMode(binary);
      command.addRestorable(setting);
      
      if( ! VcsRunner.execute(command)) {
        return false;
      }
      
      return true;
    }
  }

  public static boolean addDir(FileObject dir) {
    if(RefactorItActions.isNetBeansFour()) {
      boolean result = execute(getNb40Command("ADD_DIR_RECURSIVE", dir));
      if(! result) {
        return false;
      }
      
      final File f = FileObjectUtil.getFileOrNull(dir);
      if(f != null) {
        result = attempter.attempt(new Attempter.Task() {
          public boolean attempt() {
            return CvsFileStatus.getInstance().isKnown(f);
          }
        });
        return result;
      }
      
      return true;
    }
    return VcsRunner.execute(dir, "ADD_DIR_RECURSIVE");
  }
  
  public static boolean edit(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      return execute(getNb40Command("EDIT", fileObject));
    }
    
    return VcsRunner.execute(fileObject, "EDIT", true, false, true);
  }
  
  public static boolean get(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      return execute(getNb40Command("GET", fileObject));
    }
    
    return VcsRunner.execute(fileObject, "GET", true, false, true);
  }
  
  public static boolean getr(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      return execute(getNb40Command("GETR", fileObject));
    }
    
    return VcsRunner.execute(fileObject, "GETR", false, false, true);
  }
  
  public static boolean checkout(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      return execute(getNb40Command("CHECKOUT", fileObject));
    }
    
    return VcsRunner.execute(fileObject, "CHECKOUT", true, true, true);
  }
  
  public static boolean remove(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      final File f = FileObjectUtil.getFileOrNull(fileObject);
      final boolean uncommitedAdd = f != null && CvsFileStatus.getInstance().isUncommentedAdd(f); 
      
      boolean result = execute(getNb40Command("REMOVE", fileObject));
      if( ! result) {
        return false;
      }
      
      if(f != null && (!f.isDirectory())) {
        result = attempter.attempt(new Attempter.Task() {
          public boolean attempt() {
            if(uncommitedAdd) {
              return ! CvsFileStatus.getInstance().isKnown(f);
            } else {
              return CvsFileStatus.getInstance().isRemoved(f);
            }
          }
        });
        return result;
      }
      
      return true;
    }
    
    NbCommandOptions command = VcsRunner.getNbCommand("REMOVE", fileObject);
    if(command == null) {
      return false;
    }
    
    if (FileSystemProperties.isVssFileSystem(command.getFileSystem())) {
      String oldOptions = (String) command.getVars().get("CMD_OPTIONS");
      if (oldOptions == null) {
        oldOptions = new String();
      }
      oldOptions += " -S"; // removes local copy also
      command.getVars().put("CMD_OPTIONS", oldOptions);
    } else {
      // fix wrong default settings for files
      String recursive = (String) command.getVars().get("RECURSIVE");
      if (recursive != null && recursive.indexOf("-R") >= 0) {
        command.getVars().put("RECURSIVE", "");
      }
    }
    
    return VcsRunner.execute(command);
  }
  
  public static boolean create(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      return execute(getNb40Command("CREATE", fileObject));
    }
    
    return VcsRunner.execute(fileObject, "CREATE");
  }

  static boolean vssRename(FileObject fileObject, FileObject destination, String newName) {
    if(RefactorItActions.isNetBeansFour()) {
      throw new UnsupportedOperationException();
    }
    
    return VcsRunner.execute(fileObject, destination, newName, "RENAME");
  }

  static boolean share(FileObject fileObject, FileObject destination, String newName) {
    if(RefactorItActions.isNetBeansFour()) {
      throw new UnsupportedOperationException();
    }
    
    return VcsRunner.execute(fileObject, destination, newName, "SHARE");
  }

  public static FileObject rename(final FileObject fileObject,
      final FileObject destination,
      final String name, String ext) {
    
    return Renamer.rename(fileObject, destination, name, ext);
  }
  
  public static boolean supportsEdit(FileObject fileObject) {
    return VcsRunner.getCommandNames(fileObject).contains("EDIT");
  }

  public static boolean supportsCheckout(FileObject fileObject) {
    return VcsRunner.getCommandNames(fileObject).contains("CHECKOUT");
  }

  public static boolean supportsCreate(FileObject fileObject) {
    return VcsRunner.getCommandNames(fileObject).contains("CREATE");
  }

  public static boolean supportsGet(FileObject fileObject) {
    return VcsRunner.getCommandNames(fileObject).contains("GET");
  }
  
  // NB 4.0-section (maybe move into ide4 later)
  
  private static boolean execute(Command cmd) {
    CommandTask task = cmd.execute();
    task.waitFinished();
    return task.getExitStatus() == 0;
  }

  private static Command getNb40Command(String cmdName, FileObject fileObject) {
    Command cmd = VcsManager.getDefault().createCommand(cmdName, new FileObject[] {fileObject});
    cmd.setGUIMode(false);
    return cmd;
  }
  
  private static void showNonmodalMessageDialog(String string) {
    final JFrame frame = new JFrame();
    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    frame.setContentPane(contentPane);
    
    JLabel label = new JLabel(string);
    label.setFont(label.getFont().deriveFont(20));
    contentPane.add(label, BorderLayout.NORTH);
    
    contentPane.add(new JButton("OK") {{
        addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            frame.dispose();
          }});}}, BorderLayout.SOUTH);
    
    frame.pack();
    SwingUtil.centerWindowOnScreen(frame);
    frame.show();
    
    do {
      try {Thread.sleep(100);}catch(InterruptedException e) {throw new RuntimeException(e);}
    } while(frame.isVisible());
  }
}
