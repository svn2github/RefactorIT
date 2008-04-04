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


public class HTMLLinkEntity extends HTMLEntity {

  /* The anchor's name */
  private String name = null;

  /* The document to link to */
  private String file = null;

  /**
   */
  public HTMLLinkEntity(ASTImpl node, String style, String name, String file) {
    super(node, style);

    // Init field(s)
    setName(name);
    setFile(file);
  }

  public String toString() {
    return "<A href=\"" + getFile() + "#" + getName() + "\"><CODE " + getStyle()
        + ">" + getNode().getText() + "</CODE></A>";
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

  public String getFile() {
    return this.file;
  }

  public void setFile(String file) {
    this.file = file;
  }

}
