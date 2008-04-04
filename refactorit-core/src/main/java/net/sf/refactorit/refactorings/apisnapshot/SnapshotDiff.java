/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;


import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.usage.filters.ApiDiffFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** Diff is generated in a time-consuming constructor. */
public class SnapshotDiff {
  private final ArrayList added = new ArrayList();
  private final ArrayList removed = new ArrayList();
  private ApiDiffFilter filter;

  public SnapshotDiff(Snapshot snapshot1,
      Snapshot snapshot2) {
    this(snapshot1, snapshot2, null);
  }

  /**
   * Constructor might take long time to run (it also shows a progress bar if available in
   * CFlowContext). In the best case (if snapshots are equal) it runs in O(n) time,
   * in the worst case (if snapshots are *very* different) runs in O(n*n) time. But because
   * snapshots are very likely to be similar, this should not usually be a problem. <br><br>
   *
   * The O(n*n) case could be sped up by only comparing items from the same package, class, etc.
   */
  public SnapshotDiff(Snapshot snapshot1, Snapshot snapshot2,
      ApiDiffFilter filter) {
    ProgressListener listener = (ProgressListener) CFlowContext.get(
        ProgressListener.class.getName());

    SnapshotItem[] firstSnapshot = snapshot1.getItems();
    SnapshotItem[] secondSnapshot = snapshot2.getItems();

    this.filter = filter;

    for (int i = 0; i < firstSnapshot.length; i++) {
      if (listener != null) {
        listener.progressHappened(((float) i) / firstSnapshot.length * 50.0f);
      }

      if (filter != null) {
        if (skip(firstSnapshot[i])) {
          continue;
        }
      }

      if (!firstSnapshot[i].containedIn(secondSnapshot, i)) {
        removed.add(firstSnapshot[i]);
      }
    }

    for (int i = 0; i < secondSnapshot.length; i++) {
      if (listener != null) {
        listener.progressHappened(((float) i) / secondSnapshot.length * 50.0f
            + 50.0f);
      }

      if (filter != null) {
        if (skip(secondSnapshot[i])) {
          continue;
        }
      }

      if (!secondSnapshot[i].containedIn(firstSnapshot, i)) {
        added.add(secondSnapshot[i]);
      }
    }
  }

  public List getAdded() {
    return this.added;
  }

  public List getRemoved() {
    return this.removed;
  }

  private boolean skip(SnapshotItem item) {
    return!filter.getAccess(item.getType(), item.getAccess());
  }

  ///////////////////////////////////////////// TEST CODE /////////////////////////////
  public static class SelfTest extends TestCase {
    public SelfTest(String name) {
      super(name);
    }

    public static Test suite() {
      return new TestSuite(SelfTest.class);
    }

    private Snapshot emptySnapshot = new Snapshot("", Calendar.getInstance(),
        new HashSet());
    private Snapshot snapshotWithItems;

    SnapshotItem a = new SnapshotItem(SnapshotItem.CLASS, "A", "public A", "",
        "", "", "", "public");
    SnapshotItem b = new SnapshotItem(SnapshotItem.CLASS, "B", "public B", "",
        "", "", "", "public");
    SnapshotItem c = new SnapshotItem(SnapshotItem.PACKAGE, "", "", "", "", "",
        "", "");

    public void setUp() throws IOException {
      Set items = new HashSet();

      items.add(a);
      items.add(b);
      items.add(c);

      snapshotWithItems = new Snapshot("", Calendar.getInstance(), items);
    }

    public void testEmpty() {
      SnapshotDiff diff = new SnapshotDiff(emptySnapshot, emptySnapshot);
      assertEquals(0, diff.getRemoved().size());
      assertEquals(0, diff.getAdded().size());
    }

    public void testAddingAll() {
      SnapshotDiff diff = new SnapshotDiff(emptySnapshot, snapshotWithItems);
      assertEquals(0, diff.getRemoved().size());
      assertEquals(snapshotWithItems.getItems().length, diff.getAdded().size());
    }

    public void testRemovingAll() {
      SnapshotDiff diff = new SnapshotDiff(snapshotWithItems, emptySnapshot);
      assertEquals(snapshotWithItems.getItems().length, diff.getRemoved().size());
      assertEquals(0, diff.getAdded().size());
    }

    public void testAddAndRemoveTogether() {
      HashSet ab = new HashSet();
      ab.add(this.a);
      ab.add(this.b);

      HashSet ac = new HashSet();
      ac.add(this.a);
      ac.add(this.c);

      Snapshot abSnapshot = new Snapshot("", Calendar.getInstance(), ab);
      Snapshot acSnapshot = new Snapshot("", Calendar.getInstance(), ac);

      SnapshotDiff diff = new SnapshotDiff(abSnapshot, acSnapshot);

      assertEquals(1, diff.getAdded().size());
      assertEquals(this.c, diff.getAdded().get(0));

      assertEquals(1, diff.getRemoved().size());
      assertEquals(this.b, diff.getRemoved().get(0));
    }
  }
}
