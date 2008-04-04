/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;

import net.sf.refactorit.common.util.StringUtil;

/**
 * Finds all occurances of member's qualified name. Qualified name must be
 * separated from other text by characters that are not valid java identifiers.
 *
 * @author  tanel
 */
public class QualifiedNameIndexer extends ManagedIndexer {
  public static final int QUALIFIED_ONLY = 0;
  public static final int SLASH_PATH = 1;
  public static final int BACKSLASH_PATH = 2;
  public static final int SLASH_AND_BACKSLASH_PATH = 3;
  
  private String qualifiedName;
  private String slashedName;
  private String backslashedName;
  
  public QualifiedNameIndexer(ManagingNonJavaIndexer supervisor,
      String qualifiedName, int type) {
    super(supervisor);
    this.qualifiedName = qualifiedName;
    if((type & SLASH_PATH) > 0) {
      this.slashedName = StringUtil.getSlashedPath(qualifiedName);
    }
    if((type & BACKSLASH_PATH) > 0) {
      this.backslashedName = StringUtil.getBackslashedPath(qualifiedName);
    }
  }
  
  public QualifiedNameIndexer(ManagingNonJavaIndexer supervisor,
      String qualifiedName) {
    this(supervisor, qualifiedName, QUALIFIED_ONLY);
  }

  public void visit(Line line) {
    bypassLine(line, qualifiedName, QUALIFIED_ONLY);
    if(slashedName != null) {
      bypassLine(line, slashedName, SLASH_PATH);
    }
    if(backslashedName != null) {
      bypassLine(line, backslashedName, BACKSLASH_PATH);
    }
  }
  
  private void bypassLine(Line line, String name, int invType) {
    String content = line.getContent();
    int lineLength = content.length();
    int start = 0;
    while (start < lineLength) {
      int index = content.indexOf(name, start);
      if (index == -1) {
        break;
      } else {
        int end = index + name.length();

        if (((index == 0)
            || (!Character.isJavaIdentifierStart(content.charAt(index - 1))))
            && ((end + 1 > lineLength)
            || (!Character.isJavaIdentifierPart(content.charAt(end))))) {
          if((invType & SLASH_AND_BACKSLASH_PATH) == 0) {
            addOccurrence(line, index, end);
          } else {
            addOccurrence(new PathOccurrence(invType,line, index, end));
          }
        }
        start = end;
      }
    }
  }
  

}
