/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.source.format.BinFormatter;


public class BinCITypeRefWrapper implements Comparable {
  BinTypeRef ref;
  public BinCITypeRefWrapper(BinTypeRef ref) {
    this.ref = ref;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof BinCITypeRefWrapper)) {
      //DebugInfo.trace("equals called with not BinCI");
      return -1;
    }

    int result = BinFormatter.formatQualified(ref).compareTo(
        BinFormatter.formatQualified(((BinCITypeRefWrapper) obj).ref));
//      if ( result==0 ) {
//        result= this.toString().compareTo(obj.toString());
//      }
    return result;
  }

  public String toString() {
    return BinFormatter.formatQualified(ref);
  }

  public BinTypeRef getItem() {
    return ref;
  }
}
