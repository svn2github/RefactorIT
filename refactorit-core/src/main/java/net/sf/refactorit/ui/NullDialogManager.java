/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.source.preview.ChangesPreviewModel;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.util.List;


/**
 * Empty implementation of DialogManager.
 * This class is for the silent testing.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class NullDialogManager extends DialogManager {
  public int answerToYesNoQuestion = DialogManager.YES_BUTTON;

  public String asked;

  public String customErrorString;
  public String infoString;

  public Throwable throwable;

  /**
   * Constructor for NullDialogManager.
   */
  public NullDialogManager() {
    super();

    clearBuffers();
  }

  public BinTreeTableModel showConfirmations(
      RefactorItContext context, BinTreeTableModel model,
      String helpTopicId
  ) {
    return model;
  }

  public BinTreeTableModel showConfirmations(
      RefactorItContext context, BinTreeTableModel model,
      String description, String helpTopicId
      ) {
    return showConfirmations("", context, model, description, helpTopicId);
  }

  public boolean showConfirmations(String caption,
      String helpMessage, ChangesPreviewModel changesPreviewModel,
      RefactorItContext context, String descriptionStr,
      String helpId) {

    return true;
  }

  public void showInformation(IdeWindowContext context, String key) {
    infoString += key;
  }

  public void showInformation(
      IdeWindowContext context, String key, String message
  ) {
    infoString += key;
  }

  public void showWarning(IdeWindowContext context, String key) {
    customErrorString += key;
  }

  public void showWarning(
      IdeWindowContext context, String key, String message
  ) {
    customErrorString += message;
  }

  public int showYesNoCancelQuestion(IdeWindowContext context, String key) {
    return DialogManager.YES_BUTTON;
  }

  public int showYesNoCancelQuestion(
      IdeWindowContext context, String key,
      String message, int defaultSelectedButton
  ) {
    return defaultSelectedButton;
  }

  /*
   * @see DialogManager#showYesNoQuestion(Component, String)
   */
  public int showYesNoQuestion(IdeWindowContext context, String key) {
    asked = key;
    return answerToYesNoQuestion;
  }

  public int showYesNoQuestion(
      IdeWindowContext context,
      String key, String message,
      int defaultSelectedButton
  ) {
    return defaultSelectedButton;
  }

  public int showCustomYesNoQuestion(
      IdeWindowContext context, String title, String text
  ) {
    return DialogManager.YES_BUTTON;
  }

  public void showError(IdeWindowContext context, String key) {
  }

  public void showCustomError(
      IdeWindowContext context, String title, String text
  ) {
    customErrorString = text;
  }

  public void showError(IdeWindowContext context, String title, Throwable t) {
    this.throwable = t;
  }

  public int showCustomYesNoQuestion(
      IdeWindowContext context,
      String title, String text,
      int defaultSelection
  ) {
    return defaultSelection;
  }

  public void showCriticalError(IdeWindowContext context, Project project) {
  }

  public void showJavaVersionWarning(IdeWindowContext context) {
  }

  public void showNonSourcePathItemInfo(
      IdeWindowContext context, String actionName, Object triedToRunUpon
  ) {
  }

  public void showCustomError(IdeWindowContext context, String errorString) {
    customErrorString += errorString;
  }

  public int getResultFromAuxiliaryDialog(String title, String[] strings) {
    return 0;
  }

  public int getResultFromQuestionDialog(String title, String message) {
    return -1;
  }

  public int showYesNoHelpQuestion(
      IdeWindowContext context, String text, String helpButtonKey
  ) {
    return -1;
  }

  public ConflictResolution getResultFromResolutionDialog(List resolutions) {
    return null;
  }

  public int showYesNoHelpQuestion(
      IdeWindowContext context, String key, String text, String helpButtonKey
  ) {
    return -1;
  }

  public void clearBuffers() {
    asked = "";

    customErrorString = "";
    infoString = "";

    throwable = null;
  }

  public BinTreeTableModel showConfirmations(String title,
      RefactorItContext context, BinTreeTableModel model, String description,
      String helpTopicId) {
    return model;
  }


  public BinTreeTableModel showSettings(String title,
      RefactorItContext context, BinTreeTableModel model, String description,
      String helpTopicId) {
    return model;
  }
}
