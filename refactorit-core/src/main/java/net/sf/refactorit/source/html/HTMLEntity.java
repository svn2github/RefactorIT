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


public abstract class HTMLEntity implements Comparable {

  /* The target node */
  private ASTImpl node = null;

  /* The style identifier */
  private String style = null;

  public HTMLEntity(ASTImpl node, String style) {
    setNode(node);
    setStyle(style);
  }

  //
  // The java.lang.Comparable interface
  //

  public int compareTo(Object data) {
    ASTImpl std = this.getNode();
    ASTImpl ref = ((HTMLEntity) data).getNode();

    if (std.getLine() == ref.getLine()) {
      return (std.getColumn() - ref.getColumn());
    }

    // Not equal
    return (std.getLine() - ref.getLine());
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof HTMLEntity)) {
      return false;
    }
    ASTImpl thisNode = this.getNode();
    ASTImpl compareNode = ((HTMLEntity) obj).getNode();
    return thisNode.getStartLine() == compareNode.getStartLine() &&
        thisNode.getStartColumn() == compareNode.getStartColumn() &&
//        thisNode.getEndColumn() == compareNode.getEndColumn()
//        &&
        thisNode.getText().equals(compareNode.getText());
  }

  //
  // Accessor methods
  //

  public ASTImpl getNode() {
    return this.node;
  }

  public void setNode(ASTImpl node) {
    this.node = node;
  }

  public String getStyle() {
    return this.style;
  }

  public void setStyle(String style) {
    this.style = (style != null ? "class=\"" + (style) + "\"" : "");
  }
}
