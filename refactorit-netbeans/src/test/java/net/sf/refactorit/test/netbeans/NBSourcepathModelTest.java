/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans;

import java.io.File;
import java.util.List;

import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.projectoptions.ui.NBSourcepathModel;
import net.sf.refactorit.netbeans.common.projectoptions.ui.PathItemReferenceWrapper;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class NBSourcepathModelTest extends TestCase {
  public void testRoots() {
    PathItemReference ref = new PathItemReference(
        new File("."));
    NBSourcepathModel m = new NBSourcepathModel(new PathItemReference[] { ref });
    
    List roots = m.getRootDataObjects();
    assertEquals(1, roots.size());
    assertEquals(ref, ((PathItemReferenceWrapper)roots.get(0)).getReference());
  }
  
  public void testInvalidSourcepathItem() {
    PathItemReference invalid = new PathItemReference(
        new File("invalid-file-name (file must not exist)"));
    NBSourcepathModel m = new NBSourcepathModel(new PathItemReference[] { invalid });
    
    assertEquals(0, m.getRootDataObjects().size());
  }
}
