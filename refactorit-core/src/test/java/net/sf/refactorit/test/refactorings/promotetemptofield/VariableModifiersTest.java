/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author  RISTO A
 */
public class VariableModifiersTest extends TestCase {
  public VariableModifiersTest(String name) {super(name);
  }

  public static Test suite() {
    return new TestSuite(VariableModifiersTest.class);
  }

  public void testVarTypeNodeEnd() {
    String declaration = "final int j, i;";
    BinLocalVariable var
        = Utils.createLocalVariableDeclarationFromString(declaration);

    assertEquals(1, var.getStartColumn());
    assertEquals(11, var.getTypeNodeEndColumn());
    assertEquals(2, var.getTypeNodeEndLine());

    assertEquals("final int ", declaration.substring(
        var.getStartColumn() - 1, var.getTypeNodeEndColumn() - 1));
    assertEquals("final int", var.getTypeAndModifiersNodeText());
  }
}
