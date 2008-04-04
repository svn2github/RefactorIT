/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;


import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.source.SourceCoordinate;

import java.util.List;
import java.util.Set;


public abstract class BinItemFormatter {
  private Set fqnTypes;

  
  public abstract String print();
//  /**
//   * @deprecated use print() instead
//   */
//  public String formWhole() {
//    return formHeader() + formBody() + formFooter();
//  }
 
//  /**
//   * @deprecated use print() instead
//   */
//  public abstract String formHeader();

//  /**
//   * @deprecated use print() instead
//   */  
//  public abstract String formBody();

//  /**
//   * @deprecated use print() instead
//   */
//  public abstract String formFooter();

  public int getMemberIndent() {
    throw new UnsupportedOperationException();
  }

  public SourceCoordinate findNewMemberPosition() {
    throw new UnsupportedOperationException();
  }

  public void setFqnTypes(Set fqnTypes) {
    this.fqnTypes = fqnTypes;
  }
  
  public Set getFqnTypes() {
    return this.fqnTypes;
  }

  protected String formatTypeName(BinTypeRef typeRef) {
    if (this.fqnTypes != null && this.fqnTypes.contains(typeRef)){
			//Originally formatQualified and formatNotQualified
      return BinFormatter.formatQualifiedForTypeArgumentsWithAllOwners(typeRef).replace('$', '.');
    } else {
      return BinFormatter.formatNotQualifiedForTypeArgumentsWithAllOwners(typeRef).replace('$', '.');
    }
  }

  // General indent code

  static int getIndent(List locationAwaresOnSameLevel, int parentIndent) {
    int[] indents = countIndents(locationAwaresOnSameLevel);
    int indent = getSmallestAbove(locationAwaresOnSameLevel.size() / 2, indents);
    if (indent == Integer.MAX_VALUE) {
      indent = getSmallestAbove(0.0d, indents);
      if (indent == Integer.MAX_VALUE) {
        indent = parentIndent + FormatSettings.getBlockIndent();
      }
    }

    return indent;
  }

  private static int[] countIndents(List locationAwares) {
    int[] result = new int[256];
    for (int i = 0; i < locationAwares.size(); i++) {
      final LocationAware item = (LocationAware) locationAwares.get(i);

      result[item.getIndent()]++;
    }

    return result;
  }

  private static int getSmallestAbove(double indentsThreshold, int[] indents) {
    int indent = Integer.MAX_VALUE;

    for (int i = indents.length - 1; i >= 0; --i) {
      if (indents[i] > indentsThreshold && i < indent) {
        indent = i;
      }
    }

    return indent;
  }
}
