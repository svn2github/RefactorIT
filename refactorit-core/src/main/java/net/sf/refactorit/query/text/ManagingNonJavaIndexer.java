/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.query.text;


import net.sf.refactorit.common.util.WildcardPattern;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author  tanel
 */
public class ManagingNonJavaIndexer extends DelegatingNonJavaIndexer {

  List occurrences = new ArrayList(32);

  public ManagingNonJavaIndexer(WildcardPattern[] patterns) {
    super(patterns);
  }

  public void addOccurrence(Occurrence occurrence) {
    occurrences.add(occurrence);
  }

  public List getOccurrences() {
    return occurrences;
  }

}
