/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.javadoc;


import net.sf.refactorit.refactorings.javadoc.Javadoc;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  risto */
public class JavadocTest extends TestCase {
  private Javadoc.Tag tag;

  public JavadocTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(JavadocTest.class);
  }

  public void testSimpleTags() {
    tag = Javadoc.createTag(null, "@see java.util.String",
        Integer.MAX_VALUE, 1, 1, Collections.EMPTY_LIST, true);
    assertEquals("see", tag.getName());
    assertEquals(
        "<A HREF=\"java.util.String\"><CODE>java.util.String</CODE></A>",
        tag.getHTMLRepresentation());
    assertEquals(0, tag.getDescriptionList().size());

    tag = Javadoc.createTag(null, "@see",
        Integer.MAX_VALUE, 1, 1, Collections.EMPTY_LIST, false);
    assertNull(tag);
  }

  public void testTagsWithEmptyNamesDoNotCrash() {
    tag = Javadoc.createTag(null, "@",
        Integer.MAX_VALUE, 1, 1, Collections.EMPTY_LIST, false);
    assertNull(tag);

    tag = Javadoc.createTag(null, "@ ",
        Integer.MAX_VALUE, 1, 1, Collections.EMPTY_LIST, false);
    assertNull(tag);

    tag = Javadoc.createTag(null, "@a",
        Integer.MAX_VALUE, 1, 1, Collections.EMPTY_LIST, false);
    assertNull(tag);
  }
}
