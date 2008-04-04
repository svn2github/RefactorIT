/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;


public class PathOccurrence extends Occurrence {
  int pathType;
  public PathOccurrence(int pathType, Line line, int startPos, int endPos) {
    super(line, startPos, endPos);
    this.pathType = pathType;
  }
  
  public boolean isSlashedPath() {
    return (QualifiedNameIndexer.SLASH_PATH & pathType) > 0;
  }
  
  public boolean isBackslashedPath() {
    return (QualifiedNameIndexer.BACKSLASH_PATH & pathType) > 0;
  }
  
  public String getText() {
    String name = super.getText();
    if(isSlashedPath()) {
      return name.replaceAll("/",".");
    } else if(isBackslashedPath()) {
      return name.replaceAll("\\\\",".");
    }
    return null;
  }
  
  public String getOriginalPath() {
    return super.getText();
  }
}
