/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.swingtests;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.RitDialogFactory;
import net.sf.refactorit.ui.dialog.SwingDialog;
import net.sf.refactorit.ui.dialog.SwingDialogFactory;
import net.sf.refactorit.ui.errors.JWarningDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.common.ChangeMethodSignatureDialog;
import net.sf.refactorit.ui.module.common.ChangeMethodSignaturePanel;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class ChangeMethodSignatureDialogTest extends TestCase {
  private static final String BEHAVIOUR_CHANGE_WARNING = 
      "Behaviour of code might change";
  
  private ChangeMethodSignatureRefactoring refactoring;
  private ChangeMethodSignatureDialog dialog;

  private MockDialogFactory mockDialogFactory;
  
  private RitDialogFactory old;
  private Properties oldOptions;
  
  public ChangeMethodSignatureDialogTest(String name) {
    super(name);
  }
  
  public void setUp() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public class X {\n" + 
        "  public X(int a, int b) {\n" +
        "  }\n" +
        "  \n" +
        "  public void user() {\n" +
        "    new X(1, 2);\n" + 
        "  }\n" +
        "  \n" +
        "}", 
        "X.java", null);
    BinClass type = (BinClass) ItemByNameFinder.findBinCIType(p, "X");
    BinConstructor constructor = type.getDeclaredConstructors()[0];
    refactoring = new ChangeMethodSignatureRefactoring(constructor);
    
    old = RitDialog.getDialogFactory();
    mockDialogFactory = new MockDialogFactory();
    RitDialog.setDialogFactory(mockDialogFactory);
    
    dialog = new ChangeMethodSignatureDialog(refactoring);
    
    oldOptions = GlobalOptions.getProperties();
  }
  
  public void tearDown() {
    RitDialog.setDialogFactory(old);
    
    GlobalOptions.setProperties(oldOptions);
    GlobalOptions.save();
  }
  
  public void testOk() {
    //mockDialogFactory.setShowDialogs(true);
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        dialog.clickOk();
      }
    });
    dialog.show();
    
    assertTrue(dialog.isOkPressed());
  }
  
  public void testDontShowWarningsAgainInsideSameDialog() {
    JWarningDialog.saveLastTimeValue(ChangeMethodSignaturePanel.SHOW_WARNING_KEY,
        0, true, JWarningDialog.QUESTION_MESSAGE);
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.setOptionDialogResult(ChangeMethodSignaturePanel.OK);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickUp();
        assertTrue(mockDialogFactory.getOptionDialogLog(),
            mockDialogFactory.getOptionDialogLog().indexOf(BEHAVIOUR_CHANGE_WARNING) >= 0);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickDown();
        assertEquals("", mockDialogFactory.getOptionDialogLog());
      }
    });
    dialog.show();
  }
  
  public void testShowWarningsAgainOnOk() {
    JWarningDialog.saveLastTimeValue(ChangeMethodSignaturePanel.SHOW_WARNING_KEY,
        0, true, JWarningDialog.QUESTION_MESSAGE);
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.setOptionDialogResult(ChangeMethodSignaturePanel.OK);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickUp();
        assertTrue(mockDialogFactory.getOptionDialogLog(),
            mockDialogFactory.getOptionDialogLog().indexOf(BEHAVIOUR_CHANGE_WARNING) >= 0);
      }
    });
    dialog.show();
    
    dialog = new ChangeMethodSignatureDialog(refactoring);
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickDown();
        assertTrue(mockDialogFactory.getOptionDialogLog(),
            mockDialogFactory.getOptionDialogLog().indexOf(BEHAVIOUR_CHANGE_WARNING) >= 0);
      }
    } );
    dialog.show();
  }
  
  public void testDontShowWarningsAgain() {
    //mockDialogFactory.setShowDialogs(true);
    
    JWarningDialog.saveLastTimeValue(ChangeMethodSignaturePanel.SHOW_WARNING_KEY,
        0, true, JWarningDialog.QUESTION_MESSAGE);
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.setOptionDialogResult(ChangeMethodSignaturePanel.DONT_SHOW_AGAIN);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickUp();
        assertTrue(mockDialogFactory.getOptionDialogLog(),
            mockDialogFactory.getOptionDialogLog().indexOf(BEHAVIOUR_CHANGE_WARNING) >= 0);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickDown();
        assertEquals("", mockDialogFactory.getOptionDialogLog());
      }
    });
    dialog.show();
  }
  
  public void testDontShowWarningsAgain_twoRuns() {
    //mockDialogFactory.setShowDialogs(true);
    
    JWarningDialog.saveLastTimeValue(ChangeMethodSignaturePanel.SHOW_WARNING_KEY,
        0, true, JWarningDialog.QUESTION_MESSAGE);
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.setOptionDialogResult(ChangeMethodSignaturePanel.DONT_SHOW_AGAIN);
        
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickUp();
        assertTrue(mockDialogFactory.getOptionDialogLog(),
            mockDialogFactory.getOptionDialogLog().indexOf(BEHAVIOUR_CHANGE_WARNING) >= 0);
      }
    });
    dialog.show();

    dialog = new ChangeMethodSignatureDialog(refactoring);
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      public void run(SwingDialog d) {
        mockDialogFactory.clearOptionDialogLog();
        dialog.clickUp();
        assertEquals("", mockDialogFactory.getOptionDialogLog());
      }
    } );
    dialog.show();
  }
}
