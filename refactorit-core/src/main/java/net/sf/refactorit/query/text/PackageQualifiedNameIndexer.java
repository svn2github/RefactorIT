/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Finds all occurances of package's qualified name. The occurrence is only indexed
 * if the name follows a not a dot and non-java-identifier, or a dot followed
 * by a non-java-identifier-character, or a dot foloowed by a name of one of the
 * package type names.
 * E.g. following occurrences of package "com.acme" are indexed:
 * <ul>
 * <li>"com.acme"</li>
 * <li>"com.acme."</li>
 * <li>"com.acme.*"</li>
 * <li>"com.acme.Test", given that the type <code>com.acme.Test</code> exists.</li>
 * </ul>
 * 
 * SLASH_PATH flag enabled in constructor parameter`s invType enables search of
 * following occurrences:
 * <ul>
 * <li>"com/acme"</li>
 * <li>"com/acme/"</li>
 * </ul> 
 * 
 * BACKSLASH_PATH flag enabled in constructor parameter`s invType enables search 
 * of next occurrences:
 * <ul>
 * <li>"com\acme"</li>
 * <li>"com\acme\"</li>
 * </ul> 
 * 
 * Following occurrences are not indexed:
 * <ul>
 * <li>"com.acme.sub", given that there is no type <code>com.acme.sub</code></li>
 * <li>"com.acme2"</li>
 * </ul>
 *
 * @author  tanel
 */
public class PackageQualifiedNameIndexer extends ManagedIndexer {
  private String qualifiedName;
  private String slashedName;
  private String backslashedName;
  private boolean indexPrefixes;
  
  private List typeNameList = new ArrayList();

  public PackageQualifiedNameIndexer(ManagingNonJavaIndexer manager,
      BinPackage pack, int invType, boolean indexPrefixes) {
    super(manager);
    this.qualifiedName = pack.getQualifiedName();
    this.indexPrefixes = indexPrefixes;
    
    if((invType & QualifiedNameIndexer.SLASH_PATH) > 0) {
      this.slashedName = StringUtil.getSlashedPath(qualifiedName);
    }
    if((invType & QualifiedNameIndexer.BACKSLASH_PATH) > 0) {
      this.backslashedName = StringUtil.getBackslashedPath(qualifiedName);
    }
    for (Iterator i = pack.getAllTypes(); i.hasNext(); ) {
      BinTypeRef type = (BinTypeRef) i.next();
      typeNameList.add(type.getQualifiedName());
    }
  }
  
  public PackageQualifiedNameIndexer(ManagingNonJavaIndexer manager,
      BinPackage pack) {
    this(manager, pack, QualifiedNameIndexer.QUALIFIED_ONLY, false);
  }

  public void visit(Line line) {
    bypassLine(line, qualifiedName, QualifiedNameIndexer.QUALIFIED_ONLY);
    if(slashedName != null) {
      bypassLine(line, slashedName, QualifiedNameIndexer.SLASH_PATH);
    }
    if(backslashedName != null) {
      bypassLine(line, backslashedName, QualifiedNameIndexer.BACKSLASH_PATH);
    }
  }
  
  private void bypassLine(Line line, String name, int invType) {
    String content = line.getContent();
    int lineLength = content.length();
    int start = 0;

    while (start < lineLength) {
      int index = content.indexOf(name, start);

      if (index == -1) {
        return;
      } else {
        int end = index + name.length();
        if ((index == 0)
            || (!Character.isJavaIdentifierStart(content.charAt(index - 1)))) {
          //start is OK
          if ((lineLength == end) // line ends after that
              || ((lineLength == end + 1)
              && !Character.isJavaIdentifierPart(content.charAt(end)))
              || ((lineLength >= end + 1)
              && !Character.isJavaIdentifierPart(content.charAt(end))
              && (indexPrefixes || (content.charAt(end) != '.')))
              || ((lineLength > end + 1)
              && !Character.isJavaIdentifierPart(content.charAt(end))
              && !Character.isJavaIdentifierPart(content.charAt(end + 1)))
              || ((lineLength > end + 1) && isTypeName(content.substring(index)))) {
            if((invType & QualifiedNameIndexer.SLASH_AND_BACKSLASH_PATH) == 0) {
              addOccurrence(line, index, end);
            } else {
              addOccurrence(new PathOccurrence(invType,line, index, end));
            }
          }
        }
        start = end;
      }
    }
  }
                            

  private boolean isTypeName(String content) {
    for (Iterator i = typeNameList.iterator(); i.hasNext(); ) {
      String typeName = (String) i.next();
      if (content.startsWith(typeName)) {
        int end = typeName.length();
        if ((content.length() == end)
            || (!Character.isJavaIdentifierPart(content.charAt(end)))) {
          return true;
        }
      }
    }
    return false;
  }
}
