/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vfs;

import java.io.IOException;
import java.io.OutputStreamWriter;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.action.RenameAction;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import org.openide.windows.TopComponent;

/**
 *
 * @author risto
 */
public class FileNotFoundReasonInSourceWindowTest extends NbTestCase {
  DialogManager oldDialogManager;
  NullDialogManager ndm;
  
  Source source;
  
  public void mySetUp() throws Exception {
    oldDialogManager = DialogManager.getInstance();
    ndm = new NullDialogManager();
    DialogManager.setInstance(ndm);
    
    source = createSampleSource();
  }
  
  public void myTearDown() {
    DialogManager.setInstance(oldDialogManager);
  }
  
  public void testIgnoredOnSourcepath() throws IOException {
    setIgnoredOnSourcepath((NBSource) source.getParent());

    showInEditor(source, 1, 25);
    new RenameAction().performAction(TopComponent.getRegistry().getActivatedNodes());
    
    assertEquals(FileNotFoundReason.ELEMENT_IGNORED, ndm.customErrorString);
  }
  
  public void testBadCursorLocation() throws IOException {
    showInEditor(source, 1, 1);
    new RenameAction().performAction(TopComponent.getRegistry().getActivatedNodes());
    
    assertEquals(FileNotFoundReason.CURSOR_MISPLACED_IN_SOURCE, ndm.customErrorString);
  }
}
