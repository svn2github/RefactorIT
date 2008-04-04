/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.loader;


import net.sf.refactorit.loader.Comment;

import org.apache.log4j.Category;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link Comment}.
 */
public class CommentTest extends TestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(CommentTest.class.getName());

  /** Creates new CommentTest */
  public CommentTest(String name) {
    super(name);
  }

  public static TestSuite suite() {
    final TestSuite suite = new TestSuite(CommentTest.class);
    suite.setName("Comment tests");
    return suite;
  }

  /**
   * Tests null comment.
   */
  public void testNull() {
    cat.info("Testing null comment");

    final Comment comment = new Comment(null, 11, 25);
    assertNull("Text", comment.getText());
    assertEquals("Start line", 11, comment.getStartLine());
    assertEquals("Start column", 25, comment.getStartColumn());
    assertEquals("End line", 11, comment.getEndLine());
    assertEquals("End column", 25, comment.getEndColumn());

    cat.info("SUCCESS");
  }

  /**
   * Tests empty comment.
   */
  public void testEmpty() {
    cat.info("Testing empty comment");

    final Comment comment = new Comment("", 15, 13);
    assertEquals("Text", "", comment.getText());
    assertEquals("Start line", 15, comment.getStartLine());
    assertEquals("Start column", 13, comment.getStartColumn());
    assertEquals("End line", 15, comment.getEndLine());
    assertEquals("End column", 13, comment.getEndColumn());

    cat.info("SUCCESS");
  }

  /**
   * Tests single-line comment.
   */
  public void testSingleLine() {
    cat.info("Testing single-line comment");

    final Comment comment = new Comment("// Hello World!", 3, 2);
    assertEquals("Text", "// Hello World!", comment.getText());
    assertEquals("Start line", 3, comment.getStartLine());
    assertEquals("Start column", 2, comment.getStartColumn());
    assertEquals("End line", 3, comment.getEndLine());
    assertEquals("End column", 17, comment.getEndColumn());

    cat.info("SUCCESS");
  }

  /**
   * Tests multi-line comment with Windows linebreaks.
   */
  public void testMultiLineWindows() {
    cat.info("Testing multi-line comment with Windows linebreaks");

    final Comment comment = new Comment("/* Hello World!\r\n  */", 1001, 3);
    assertEquals("Text", "/* Hello World!\r\n  */", comment.getText());
    assertEquals("Start line", 1001, comment.getStartLine());
    assertEquals("Start column", 3, comment.getStartColumn());
    assertEquals("End line", 1002, comment.getEndLine());
    assertEquals("End column", 5, comment.getEndColumn());

    cat.info("SUCCESS");
  }

  /**
   * Tests multi-line comment with Unix linebreaks.
   */
  public void testMultiLineUnix() {
    cat.info("Testing multi-line comment with Unix linebreaks");

    final Comment comment = new Comment("/* Hello World!\n  */", 1001, 3);
    assertEquals("Text", "/* Hello World!\n  */", comment.getText());
    assertEquals("Start line", 1001, comment.getStartLine());
    assertEquals("Start column", 3, comment.getStartColumn());
    assertEquals("End line", 1002, comment.getEndLine());
    assertEquals("End column", 5, comment.getEndColumn());

    cat.info("SUCCESS");
  }

  /**
   * Tests multi-line comment with Mac linebreaks.
   */
  public void testMultiLineMac() {
    cat.info("Testing multi-line comment with Mac linebreaks");

    final Comment comment = new Comment("/* Hello World!\r  */", 1001, 3);
    assertEquals("Text", "/* Hello World!\r  */", comment.getText());
    assertEquals("Start line", 1001, comment.getStartLine());
    assertEquals("Start column", 3, comment.getStartColumn());
    assertEquals("End line", 1002, comment.getEndLine());
    assertEquals("End column", 5, comment.getEndColumn());

    cat.info("SUCCESS");
  }
}
