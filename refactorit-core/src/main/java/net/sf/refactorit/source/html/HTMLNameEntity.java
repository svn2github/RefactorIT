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


public class HTMLNameEntity extends HTMLEntity {

  /* Tha name of the target */
  private String name = null;

  /**
   */
  public HTMLNameEntity(ASTImpl node, String style, String name) {
    super(node, style);

    // Init field(s)
    setName(name);
  }

  public String toString() {
    return "<A name=\"" + getName() + "\"><CODE " + getStyle() + ">"
        + getNode().getText() + "</CODE></A>";
  }

  //
  // Accessor methods
  //

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
