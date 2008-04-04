/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.swingtests;

import java.awt.HeadlessException;

import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.SwingDialog;
import net.sf.refactorit.ui.dialog.SwingDialogFactory;
import net.sf.refactorit.ui.module.IdeWindowContext;

  
public class MockDialogFactory extends SwingDialogFactory {
  private boolean showDialogs = false;
  private DialogShowListener dialogShowListener = null;
  
  private String optionDialogLog = "";
  private int optionDialogResult = 0;
  
  public RitDialog createDialog(IdeWindowContext context) {
    return new SwingDialog(context) {
      public void show() {
        if(showDialogs) {
          super.show();
        }

        if(dialogShowListener == null) {
          throw new RuntimeException("Please attach a dialog-show listener to your MockDialogFactory instance");
        }
        
        dialogShowListener.run(this);
      }
    };
  }

  public void setShowDialogs(final boolean showDialogs) {
    this.showDialogs = showDialogs;
  }

  public void setDialogShowListener(final DialogShowListener dialogShowListener) {
    this.dialogShowListener = dialogShowListener;
  }
  
  public String getOptionDialogLog() {
    return optionDialogLog;
  }
  
  public void clearOptionDialogLog() {
    this.optionDialogLog = "";
  }
  
  public void setOptionDialogResult(final int optionDialogResult) {
    this.optionDialogResult = optionDialogResult;
  }
  
  public int showOptionDialog(IdeWindowContext context, Object message, String title, 
      int optionType, int messageType, Object[] options, Object initialValue)
      throws HeadlessException {
    
    if(showDialogs) {
      super.showOptionDialog(context, message, title, 
          optionType, messageType, options, initialValue);
    }
    
    optionDialogLog += message;
    return optionDialogResult;
  }
  
  public static class NullDialogShowListener implements DialogShowListener {
    public void run(SwingDialog d) {};
  }
}
