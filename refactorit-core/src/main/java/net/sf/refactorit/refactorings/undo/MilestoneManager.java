/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import org.apache.log4j.Logger;

import javax.swing.undo.UndoableEdit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;


/**
 * @author Tonis Vaga
 */
public class MilestoneManager extends javax.swing.undo.UndoManager
    implements IMilestoneManager {
  private Project project;
  private MilestoneTransaction transaction;

  private static MilestoneManager instance = null;

  private static final String MILESTONE_SERIAL_FILE = "mtransaction.bin";
  private final String milestoneDir;
  private static final Logger LOG = AppRegistry.getLogger(MilestoneManager.class);

  /**
   * @param project
   */
  private MilestoneManager(Project project) {
    this.project = project;

    milestoneDir = getMilestoneDir(project);
    Assert.must(milestoneDir != null);

    if (new File(milestoneDir).exists()) {
      transaction = deserializeTransaction();
    }

    if (transaction == null) {
      AppRegistry.getLogger(this.getClass()).debug("No previous checkpoints discovered in " + milestoneDir);
    } else {
      this.addTransaction(transaction);
    }

  }

  public static IMilestoneManager getInstance(Project project) {
    if (project == null) {
      return null;
    }
    if (instance == null || instance.project != project) {
      clear();
      instance = new MilestoneManager(project);
    }
    return instance;
  }

  public void createMilestone() {
    super.discardAllEdits();
    transaction = new MilestoneTransaction(project, milestoneDir);
    this.addTransaction(transaction);
    //this.end();
  }

  public static void clear() {
    if (instance != null) {
      instance.serializeTransaction();

      instance.discardAllEdits();
      instance.project = null;
      instance.transaction = null;
      instance = null;
    }
  }

  public String getMilestoneInfo() {
    if (transaction == null) {
      return "";
    }
    return transaction.getInfo();
  }

  public synchronized void redo() throws javax.swing.undo.CannotRedoException {
    if (transaction.checkAndResolveRedoConflicts().getSeverity()
        != RefactoringStatus.CANCEL) {
      super.redo();
    }

  }

  public synchronized void undo() throws javax.swing.undo.CannotUndoException {
    if (transaction.checkAndResolveUndoConflicts().getSeverity()
        != RefactoringStatus.CANCEL) {
      super.undo();
    }
  }

  static String getMilestoneDir(Project project) {
    ProjectOptions projectSettings = project.getOptions();
    String dir = projectSettings.getMilestoneDir();

    if (dir == null || dir.equals("")) {

      dir = generateNewMilestoneDir();
      projectSettings.setMilestoneDir(dir);
    }

    return dir;
  }

  private static String generateNewMilestoneDir() {
    String pathDir = "Milestone";
    String home = ProjectOptions.getProjectHome();

    String dir;

    Random rand = new Random(System.currentTimeMillis());

    do {
      dir = home + File.separator + pathDir + File.separatorChar +
          Math.abs(rand.nextInt());
    } while (new File(dir).exists());

    return dir;
  }

  private MilestoneTransaction deserializeTransaction() {

    MilestoneTransaction result = null;

    String fileName = getMilestoneFileName();

    File file = new File(fileName);

    if (!file.exists()) {
      LOG.debug("file " + fileName + " does not exist");
      return null;
    }

    LOG.debug("deserializing transaction from " + fileName);
    
    try {
      ObjectInputStream stream = new ObjectInputStream(new FileInputStream(
          file));
      result = (MilestoneTransaction) stream.readObject();
      stream.close();
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,this);
      return null;
    }

    return result;
  }

  private void serializeTransaction() {

    if (transaction == null || !transaction.isSignificant()) {
      AppRegistry.getLogger(this.getClass()).debug("transaction not significant =" + transaction);
      return;
    }

    if (!new File(milestoneDir).exists()) {
      AppRegistry.getLogger(this.getClass()).debug("dir " + milestoneDir + " does not exist!");
      return;
    }

    String fileName = getMilestoneFileName();
    File file = new File(fileName);

    AppRegistry.getLogger(this.getClass()).debug("serializing transaction to " + fileName);

    try {
      ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(
          file));

      stream.writeObject(transaction);
      stream.close();

    } catch (IOException ex) {
      AppRegistry.getExceptionLogger().debug(ex,this.getClass());
    }
  }

  /**
   * @return
   */
  private String getMilestoneFileName() {
    return milestoneDir + File.separatorChar + MILESTONE_SERIAL_FILE;
  }

  private boolean addTransaction(UndoableEdit anEdit) {
    return super.addEdit(anEdit);
  }

  public boolean canRedo() {
    return super.canRedo();
  }

  public boolean canUndo() {
    return super.canUndo();
  }

}
