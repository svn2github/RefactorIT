/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;

import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.source.format.BinModifierFormatter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class BinModifierTest extends TestCase {
  public BinModifierTest() {
  }

  public static Test suite() {
    return new TestSuite(BinModifierTest.class);
  }

  public void testMask() {
    for (int i = 0; i < BinModifier.modifiers.length; i++) {
      switch (BinModifier.modifiers[i]) {
        case BinModifier.STATIC:
        case BinModifier.FINAL:
        case BinModifier.NATIVE:
        case BinModifier.SYNCHRONIZED:
        case BinModifier.VOLATILE:
        case BinModifier.TRANSIENT:
        case BinModifier.INTERFACE:
          assertTrue("Modifier  "
              + new BinModifierFormatter(BinModifier.modifiers[i]).print()
              + " not masked correctly",
              (BinModifier.modifiers[i]
              & BinModifier.STATIC_TO_INTERFACE_MASK) != 0);
          break;
        default:
          assertTrue((BinModifier.modifiers[i]
              & BinModifier.STATIC_TO_INTERFACE_MASK) == 0);
      }
    }

  }

}
