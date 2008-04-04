/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.SourcesModificationOperation;
import net.sf.refactorit.netbeans.common.action.RenameAction;
import net.sf.refactorit.netbeans.common.action.UndoNBAction;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.SwingUtil;

import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class NBControllerTest extends NbTestCase {
  private boolean ran;
  
  /*private DialogManager oldDialogManager;
  private NullDialogManager ndm;*/

  public void mySetUp() {
    /*oldDialogManager = DialogManager.getInstance();
    ndm = new NullDialogManager();
    DialogManager.setInstance(ndm);*/
    
    ran = false;
  }
  
  public void testBugRim503EndToEnd() {
    /* // Not fully implemented yet; maybe it should be finished some day
    showInEditor(source, 1, 25);
    setRenameParams(source.getName(), "NewName.java");
    new RenameAction().performAction(TopComponent.getRegistry().getActivatedNodes());
    
    source = findSource("NewName.java");
    showInEditor(source, 1, 25);
    new UndoNBAction().performAction(TopComponent.getRegistry().getActivatedNodes());
    */
  }
  
  private synchronized void ran() {
    ran = true;
  }
  
  private synchronized boolean hasRan() {
    return ran;
  }
  
  public void testBugRim503_swingThread() {
    SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
      public void run() {
        IDEController.getInstance().run(new SourcesModificationOperation() {
          public void runImpl() throws Exception {
            SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
              public void run() {
                ran();
              }      
            } );
          }
        });
      }
    } );
    
    assertTrue(hasRan());
  }
  
  public void testBugRim503_anotherThread() {
    // Tests are not ran in the Swing thread, so we do not need to launch an extrac Thread here
    
    IDEController.getInstance().run(new SourcesModificationOperation() {
      public void runImpl() throws Exception {
        SwingUtil.invokeAndWaitFromAnyThread_noCheckedExceptions(new Runnable() {
          public void run() {
            ran();
          }      
        } );
      }
    });
    
    assertTrue(hasRan());
  }
  
}
