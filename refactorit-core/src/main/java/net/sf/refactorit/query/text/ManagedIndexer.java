/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;

public class ManagedIndexer extends DelegateNonJavaIndexer {

  private ManagingNonJavaIndexer supervisor;

  public ManagedIndexer(ManagingNonJavaIndexer supervisor) {
    this.supervisor = supervisor;
    supervisor.registerDelegate(this);
  }

  protected void addOccurrence(Line line, int startPos, int endPos) {
    addOccurrence(new Occurrence(line, startPos, endPos));
  }

  protected void addOccurrence(Occurrence occurrence) {
    supervisor.addOccurrence(occurrence);
  }
}
