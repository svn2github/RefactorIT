/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.query.text;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.vfs.Source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author  tanel
 */
public class DelegatingNonJavaIndexer extends NonJavaSourcesIndexer {

  private DelegateNonJavaIndexer[] delegates = new DelegateNonJavaIndexer[0];
  private int delegatesNum = 0;

  public DelegatingNonJavaIndexer(WildcardPattern[] patterns) {
    super(patterns);
  }

  public void registerDelegate(DelegateNonJavaIndexer delegate) {
    List dels = new ArrayList(Arrays.asList(delegates));
    if (!dels.contains(delegate)) {
      dels.add(delegate);
      ++this.delegatesNum;
      delegates = (DelegateNonJavaIndexer[]) dels.toArray(
          new DelegateNonJavaIndexer[dels.size()]);
    }
  }

  // Visitors

  public void visit(final Project x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
  }

  public void visit(final Source x) throws IOException {
    for (int i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
  }

  public void visit(final Line x) {
    for (int i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
  }

}
