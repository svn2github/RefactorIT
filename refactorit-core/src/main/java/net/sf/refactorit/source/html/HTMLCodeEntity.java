/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.html;

import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;


public class HTMLCodeEntity extends HTMLEntity implements JavaTokenTypes {

  /**
   */
  public HTMLCodeEntity(ASTImpl node, String style) {
    super(node, style);

  }

  public String toString() {
    ASTImpl node = getNode();

    // The text to display
    String text = node.getText();

    // Take special care with string and character literals, because thay may contains reserved symbols
    if (node.getType() == CHAR_LITERAL || node.getType() == STRING_LITERAL) {
      text = HTMLSourceEditor.encodeCharacters(new StringBuffer(text), 0,
          text.length()).toString();
    }

    // Return result
    return "<CODE " + getStyle() + ">" + text + "</CODE>";
  }
}
