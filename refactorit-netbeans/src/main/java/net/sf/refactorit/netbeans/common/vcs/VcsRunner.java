/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.test.commonIDE.NullController;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;

import org.apache.log4j.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.vcs.VcsManager;
import org.netbeans.api.vcs.commands.Command;
import org.netbeans.modules.vcscore.VcsAction;
import org.netbeans.modules.vcscore.VcsFileSystem;
import org.netbeans.modules.vcscore.cmdline.UserCommand;
import org.netbeans.modules.vcscore.commands.CommandDataOutputListener;
import org.netbeans.modules.vcscore.commands.CommandOutputListener;
import org.netbeans.modules.vcscore.commands.CommandProcessListener;
import org.netbeans.modules.vcscore.commands.CommandProcessor;
import org.netbeans.modules.vcscore.commands.CommandTaskInfo;
import org.netbeans.modules.vcscore.commands.VcsCommand;
import org.netbeans.modules.vcscore.commands.VcsCommandExecutor;
import org.netbeans.modules.vcscore.commands.VcsDescribedTask;
import org.netbeans.modules.vcscore.util.Table;
import org.netbeans.spi.vcs.VcsCommandsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;


import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class VcsRunner {
  private static Logger log = Logger.getLogger(VcsRunner.class); 

  public static final FileSystem getFileSystem(final FileObject fileObject) {
    FileSystem fileSystem = null;
    try {
      fileSystem = fileObject.getFileSystem();
    } catch (FileStateInvalidException e) {
      fileObject.refresh();
      fileSystem = getFileSystem(fileObject);
    }

    return fileSystem;
  }

  /**
   * @param fileObject
   * @return never returns null
   */
  static final List getCommandNames(final FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      try {
        return Arrays.asList(VcsManager.getDefault().findCommands(new FileObject[] {fileObject}));
      } catch(IllegalArgumentException e) {
        // In this case our file is not in VCS.
        return Collections.EMPTY_LIST;
      }
    }
    
    final FileSystem fileSystem = getFileSystem(fileObject);
    return FileSystemProperties.getCommandNames(fileSystem);
  }
  
  static boolean execute(final NbCommandOptions command) {
    final boolean success[] = new boolean[] {false};
    
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        final RitDialog dialog = createProgressDialog(command.getCommand().getName(), 
            command.getFileObject().getNameExt());
        
        VcsCommandExecutor[] execs = startCommand(command, new Runnable() {
          public void run() {
            SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
              public void run() {
                dialog.dispose();
              }
            } );
          }
        } );
        
        try {
          dialog.show();
        } finally {
          // Here we are sure that the command has been already completed.
          
          command.getFileObject().refresh();
          command.restoreSettings();
          
          if(execs == null) {
            success[0] = false;
          } else {
            success[0] = ! someFailed(execs);
          }
        }
      }
    } );
    
    if( ! success[0]) {
      showErrorDialog(command.getFileObject(), command.getCommand().getName());
    }
    
    return success[0];
  }
  
  static NbCommandOptions getNbCommand(String commandName, FileObject fileObject) {
    return createNbCommand(commandName, fileObject, false, true, true, 
        null, null);
  }

  static NbCommandOptions createNbCommand(String commandName, FileObject fileObject, 
      boolean shouldRunEvenWhenVcsIsDisabled, boolean saveBeforeAction, boolean allowQuietExecution) {
    return createNbCommand(commandName, fileObject, shouldRunEvenWhenVcsIsDisabled, 
        saveBeforeAction, allowQuietExecution, null, null);
  }
  
  private static NbCommandOptions createNbCommand(String commandName, FileObject fileObject, 
      FileObject destination, String newName) {
    return createNbCommand(commandName, fileObject, false, true, true, 
        destination, newName);
  }
  
  private static NbCommandOptions createNbCommand(String commandName, FileObject fileObject, boolean shouldRunEvenWhenVcsIsDisabled, boolean saveBeforeAction, boolean allowQuietExecution, FileObject destination, String newName) {
    if(!FileSystemProperties.inVcs(fileObject, shouldRunEvenWhenVcsIsDisabled)) {
      return null;
    }

    final VcsFileSystem fileSystem = (VcsFileSystem) getFileSystem(fileObject);      
    
    final VcsCommand cmd = getCommand(fileSystem, commandName, destination, newName);
    if (cmd == null) {
      ErrorDialog.error(GlobalOptions.REFACTORIT_NAME + 
          ": unknown VCS command: " + commandName + " (for "
          + fileObject + ")");
      return null;
    }
    
    NbCommandOptions result = new NbCommandOptions(cmd, fileObject, fileSystem, new Hashtable(), saveBeforeAction);
    
    if (Options.versionControlSetToQuietMode() && allowQuietExecution) { 
      NonIteractiveExecution setting = new NonIteractiveExecution(result.getVars(), result.getFileSystem(), result.getCommand());
      setting.apply();
      result.addRestorable(setting);
    }
   
    return result;
  }

  static RitDialog createProgressDialog(final String commandName, String fileName) {
    final RitDialog dialog = RitDialog.create(IDEController.getInstance().createProjectContext());
    dialog.setTitle(GlobalOptions.REFACTORIT_NAME);
    
    if(fileName.equals("")) {
      fileName = "(filesystem root)";
    }
    
    JPanel contentPane = new JPanel();
    GridLayout layout = new GridLayout(2, 1);
    contentPane.setLayout(layout);
    
    JLabel command = new JLabel(commandName.toUpperCase());
    command.setHorizontalAlignment(SwingConstants.CENTER);
    command.setVerticalAlignment(SwingConstants.BOTTOM);
    
    JLabel file = new JLabel(fileName);
    file.setHorizontalAlignment(SwingConstants.CENTER);
    file.setVerticalAlignment(SwingConstants.CENTER);
    
    contentPane.add(command);
    contentPane.add(file);
    
    dialog.setContentPane(contentPane);
    
    // This way if the user closes the dialog, RefactorIT will continue,
    // thinking that the command was successfully finished. (This is at least
    // better than being stuck in a forever-loop.)
    dialog.setDisposeOnClose(true);
    
    dialog.setSize(300, 90);
    
    return dialog;
  }
  
  static VcsCommandExecutor[] startCommand(
      final NbCommandOptions command,
      final Runnable completionListener) {
  
    log.info("=======================================================");
    log.info("REFACTORIT: executing: " + command.getCommand().getName() + " on "
        + command.getFileObject() + " " + Integer.toHexString(command.getFileObject().hashCode()));
    
    try {
      CommandProcessor.getInstance().addCommandProcessListener(new CommandProcessListener() {
        public VcsCommandsProvider getProvider() {
          return null;
        }

        public void commandPreprocessing(Command command) {}

        public void commandStarting(CommandTaskInfo info) {}

        public void commandPreprocessed(Command command1, boolean b2) {}

        public void commandDone(CommandTaskInfo info) {
          if(info.getTask() instanceof VcsDescribedTask) {
            VcsCommand cmd = ((VcsDescribedTask)info.getTask()).getVcsCommand();
            if(cmd.equals(command.getCommand())) {
              CommandProcessor.getInstance().removeCommandProcessListener(this);
              completionListener.run();
            }
          }
        }
      } );
      
      Table files = new Table();
      final String fileName = ClassPath.getClassPath(command.getFileObject(),
          ClassPath.COMPILE).getResourceName(command.getFileObject(), File.separatorChar,
          true);
      files.put(fileName, command.getFileObject());

      return VcsAction.doCommand(
          files, 
          command.getCommand(), 
          command.getVars(), 
          command.getFileSystem(),
          new CommandOutputListener() {
            public void outputLine(String line) {
              printOutput(line);
            }
          }, new CommandOutputListener() {
            public void outputLine(String line) {
              printOutput(line);
            }
          }, new CommandDataOutputListener() {
            public void outputData(String[] data) {
              printOutput(data);
            }
          }, new CommandDataOutputListener() {
            public void outputData(String[] data) {
              printOutput(data);
            }
          }, 
          command.isSaveBeforeAction());
    
    } catch (NoSuchMethodError e) {
      ErrorManager.showAndLogInternalError(e);
      completionListener.run();
      return null;
    } catch(Exception e) {
      ErrorManager.showAndLogInternalError(e);
      completionListener.run();
      return null;
    }
  }

  private static VcsCommand getCommand(VcsFileSystem fileSystem, final String commandName, final FileObject destination, final String newName) {
    VcsCommand command = fileSystem.getCommand(commandName);

    if (command == null && 
        FileSystemProperties.isVssFileSystem(fileSystem)) {
      
      if ("RENAME".equals(commandName)) {
        command = new VssRenameCommand(destination, newName);
      } else if ("SHARE".equals(commandName)) {
        command = new VssShareCommand(destination);
      }
    }
    
    return command;
  }

  private static void showErrorDialog(final FileObject fileObject, final String commandName) {
    ErrorDialog.error(
        "CVS command FAILED, please send this message to support@RefactorIT.com.\n\n" +
        "Command " + commandName + ", file " +
        getFullPathForUser(fileObject) + " " +
        "(CVS status: " +
        "'" +
        CvsFileStatus.getInstance().getCvsEntriesLine(FileObjectUtil.getFileOrNull(
        fileObject)) + "'). ");
  }
  
  static boolean someFailed(final VcsCommandExecutor[] execs) {
    for (int i = 0; i < execs.length; i++) {
      if (execs[i].getExitStatus() != VcsCommandExecutor.SUCCEEDED) {
        return true;
      }
    }
    
    return false;
  }

  private static String getFullPathForUser(FileObject fileObject) {
    String relativePath = ClassPath.getClassPath(fileObject, ClassPath.COMPILE)
        .getResourceName(fileObject, File.separatorChar, true);
    String filesystemName = getFileSystem(fileObject).getSystemName();

    return "'" + relativePath + "' in filesystem '" + filesystemName + "'";
  }

  private static void printOutput(Object obj) {
    if (obj instanceof Object[]) {
      System.err.println(Arrays.asList((Object[]) obj));
    } else {
      System.err.println(obj);
    }
  }

  private static class VssRenameCommand extends UserCommand {
    public VssRenameCommand(FileObject destination, String newName) {
      setName("RENAME");
      setDisplayName("Rename");

//      setProperty(VcsCommand.PROPERTY_ADVANCED_NAME, advancedName);
//-C${QUOTE}Renamed by RefactorIT${QUOTE}
//          + "${PROJECT}${PS}" + destination.getPackageName(File.separatorChar) + "${PS}"
//          "${RUNCDM} ${QUOTE}${VSSCMD}${QUOTE} rename "
//      +"${RENAME_OPTIONS} ${OPTIONS} ${QUOTE}${PROJECT}${PS}${PATH}${QUOTE} "
//          + "${QUOTE}"
//          + newName + "${QUOTE} ${NUR}");
      setProperty(VcsCommand.PROPERTY_EXEC,
          "${RUN} ${QUOTE}${VSSCMD}${QUOTE} rename "
          + "${QUOTE}${FILE}${QUOTE} ${QUOTE}"
          + newName + "${QUOTE} ${RENAME_OPTIONS} ${OPTIONS} ${NUR}");
      //setProperty(UserCommand.PROPERTY_INPUT, input);
      //setProperty("timeout", new Long(getTimeout()));
      setProperty(UserCommand.PROPERTY_DATA_REGEX, "(.*$)");
      setProperty(UserCommand.PROPERTY_ERROR_REGEX, "(.*$)");
      //setProperty(VcsCommand.PROPERTY_DISPLAY_PLAIN_OUTPUT, new Boolean(displayOutput));
      //setProperty("doRefresh", new Boolean(isDoRefresh())); <- not needed any more
      setProperty(VcsCommand.PROPERTY_REFRESH_CURRENT_FOLDER, Boolean.TRUE);
      //setProperty(VcsCommand.PROPERTY_REFRESH_RECURSIVELY_PATTERN_MATCHED, refreshRecursivelyPattern);//getRefreshRecursivelyPattern());
      //setProperty(VcsCommand.PROPERTY_REFRESH_PARENT_FOLDER, new Boolean(doRefresh/*isDoRefresh()*/ && refreshParent));//isRefreshParent()));
      //setProperty(VcsCommand.PROPERTY_REFRESH_CURRENT_FOLDER, new Boolean(doRefresh && !refreshParent));
//      setProperty(VcsCommand.PROPERTY_CHECK_FOR_MODIFICATIONS, new Boolean(checkForModifications));
      setProperty(VcsCommand.PROPERTY_ON_FILE, Boolean.TRUE);
      setProperty(VcsCommand.PROPERTY_ON_DIR, Boolean.FALSE);
      setProperty(VcsCommand.PROPERTY_ON_ROOT, Boolean.FALSE);
      //setProperty(VcsCommand.PROPERTY_CONFIRMATION_MSG, confirmationMsg);
      //setProperty(VcsCommand.PROPERTY_PROCESS_ALL_FILES, new Boolean(processAllFiles));
//      setProperty(VcsCommand.PROPERTY_NUM_REVISIONS, new Integer(numRevisions));
//      setProperty(VcsCommand.PROPERTY_CHANGING_NUM_REVISIONS, new Boolean(changingNumRevisions));
//      setProperty(VcsCommand.PROPERTY_CHANGING_REVISION, new Boolean(changingRevision));
//      setProperty(VcsCommand.PROPERTY_CHANGED_REVISION_VAR_NAME, changedRevisionVariableName);
      //setProperty(UserCommand.PROPERTY_PRECOMMANDS, getPreCommandsStr());
      //setProperty(UserCommand.PROPERTY_PRECOMMANDS_EXECUTE, new Boolean(executePreCommands));

      setProperty(VcsCommand.PROPERTY_CONCURRENT_EXECUTION,
          new Integer(VcsCommand.EXEC_SERIAL_ALL));
      setProperty(VcsCommand.PROPERTY_RUN_ON_MULTIPLE_FILES, Boolean.FALSE);
//      setProperty(VcsCommand.PROPERTY_INPUT_DESCRIPTOR,
//          "LABEL(New Name) PROMPT_FOR(CONVERSION_VAR_0, \"Directory\", \"\") PROMPT_FOR(CONVERSION_VAR_1, \"File\", \"\") ");

      setProperty(VcsCommand.PROPERTY_GENERAL_COMMAND_ACTION_CLASS_NAME,
          "org.netbeans.modules.vcscore.actions.GeneralCommandAction");
      setProperty(VcsCommand.PROPERTY_GENERAL_COMMAND_ACTION_DISPLAY_NAME,
          "Rename Action");

      setProperty(VcsCommand.PROPERTY_NOTIFICATION_SUCCESS_MSG,
          "Rename succeeded!");
      setProperty(VcsCommand.PROPERTY_NOTIFICATION_FAIL_MSG, "Rename failed!");
    }
  }


  private static class VssShareCommand extends UserCommand {
    public VssShareCommand(FileObject destination) {
      setName("SHARE");
      setDisplayName("Share");

//      setProperty(VcsCommand.PROPERTY_ADVANCED_NAME, advancedName);
//-C${QUOTE}Renamed by RefactorIT${QUOTE}
      setProperty(VcsCommand.PROPERTY_EXEC,
          "${RUNCDM} ${QUOTE}${VSSCMD}${QUOTE} cp ${OPTIONS} ${QUOTE}${PROJECT}${PS}"
          + FileObjectUtil.getResourceName(destination) // Here used to be "destination.getPackageName(File.separatorChar)". Is this one equal to that?  
          + "${QUOTE} && ${QUOTE}${VSSCMD}${QUOTE} share "
          + "${QUOTE}${PROJECT}${PS}${PATH}${QUOTE} ${OPTIONS} ${NUR}");
      //setProperty(UserCommand.PROPERTY_INPUT, input);
      //setProperty("timeout", new Long(getTimeout()));
      setProperty(UserCommand.PROPERTY_DATA_REGEX, "(.*$)");
      setProperty(UserCommand.PROPERTY_ERROR_REGEX, "(.*$)");
      //setProperty(VcsCommand.PROPERTY_DISPLAY_PLAIN_OUTPUT, new Boolean(displayOutput));
      //setProperty("doRefresh", new Boolean(isDoRefresh())); <- not needed any more
      setProperty(VcsCommand.PROPERTY_REFRESH_CURRENT_FOLDER, Boolean.TRUE);
      //setProperty(VcsCommand.PROPERTY_REFRESH_RECURSIVELY_PATTERN_MATCHED, refreshRecursivelyPattern);//getRefreshRecursivelyPattern());
      //setProperty(VcsCommand.PROPERTY_REFRESH_PARENT_FOLDER, new Boolean(doRefresh/*isDoRefresh()*/ && refreshParent));//isRefreshParent()));
      //setProperty(VcsCommand.PROPERTY_REFRESH_CURRENT_FOLDER, new Boolean(doRefresh && !refreshParent));
//      setProperty(VcsCommand.PROPERTY_CHECK_FOR_MODIFICATIONS, new Boolean(checkForModifications));
      setProperty(VcsCommand.PROPERTY_ON_FILE, Boolean.TRUE);
      setProperty(VcsCommand.PROPERTY_ON_DIR, Boolean.FALSE);
      setProperty(VcsCommand.PROPERTY_ON_ROOT, Boolean.FALSE);
      //setProperty(VcsCommand.PROPERTY_CONFIRMATION_MSG, confirmationMsg);
      //setProperty(VcsCommand.PROPERTY_PROCESS_ALL_FILES, new Boolean(processAllFiles));
//      setProperty(VcsCommand.PROPERTY_NUM_REVISIONS, new Integer(numRevisions));
//      setProperty(VcsCommand.PROPERTY_CHANGING_NUM_REVISIONS, new Boolean(changingNumRevisions));
//      setProperty(VcsCommand.PROPERTY_CHANGING_REVISION, new Boolean(changingRevision));
//      setProperty(VcsCommand.PROPERTY_CHANGED_REVISION_VAR_NAME, changedRevisionVariableName);
      //setProperty(UserCommand.PROPERTY_PRECOMMANDS, getPreCommandsStr());
      //setProperty(UserCommand.PROPERTY_PRECOMMANDS_EXECUTE, new Boolean(executePreCommands));

      setProperty(VcsCommand.PROPERTY_CONCURRENT_EXECUTION,
          new Integer(VcsCommand.EXEC_SERIAL_ALL));
      setProperty(VcsCommand.PROPERTY_RUN_ON_MULTIPLE_FILES, Boolean.FALSE);
//      setProperty(VcsCommand.PROPERTY_INPUT_DESCRIPTOR,
//          "LABEL(New Name) PROMPT_FOR(CONVERSION_VAR_0, \"Directory\", \"\") PROMPT_FOR(CONVERSION_VAR_1, \"File\", \"\") ");

      setProperty(VcsCommand.PROPERTY_GENERAL_COMMAND_ACTION_CLASS_NAME,
          "org.netbeans.modules.vcscore.actions.GeneralCommandAction");
      setProperty(VcsCommand.PROPERTY_GENERAL_COMMAND_ACTION_DISPLAY_NAME,
          "Share Action");

      setProperty(VcsCommand.PROPERTY_NOTIFICATION_SUCCESS_MSG,
          "Share succeeded!");
      setProperty(VcsCommand.PROPERTY_NOTIFICATION_FAIL_MSG, "Share failed!");
    }
  }
  
  public static boolean execute(FileObject fileObject, String commandName) {
    return execute(fileObject, commandName, false, true, true);
  }
  
  static boolean execute(FileObject fileObject, String commandName, 
      boolean shouldRunEvenWhenVcsIsDisabled, boolean saveBeforeAction, boolean allowQuietExecution) {
    
    NbCommandOptions command = VcsRunner.createNbCommand(commandName, fileObject, 
        shouldRunEvenWhenVcsIsDisabled, saveBeforeAction, allowQuietExecution);
    if(command == null) {
      return false;
    }
    
    return VcsRunner.execute(command);
  }
  
  static boolean execute(FileObject fileObject, FileObject destination, String newName, String commandName) {
    NbCommandOptions command = VcsRunner.createNbCommand(commandName, fileObject, 
        destination, newName);
    if(command == null) {
      return false;
    }
    
    return VcsRunner.execute(command);
  }
  
  static class FileAlreadyExistsException extends IOException {}
  
  /** For testing */
  public static void main(String[] args) {
    IDEController.setInstance(new NullController());
    createProgressDialog("COMMIT", "FastCppLexer.java").show();
  }
  
  public static boolean isVcsFileSystem(FileObject fileObject) {
    if(RefactorItActions.isNetBeansFour()) {
      do {
        if(! VcsRunner.getCommandNames(fileObject).isEmpty()) {
          return true;
        }
        
        fileObject = fileObject.getParent();
      } while(fileObject != null);
      
      return false;
    } else {
      return FileSystemProperties.isVcsFileSystem(VcsRunner.getFileSystem(fileObject));
    }
  }
}
