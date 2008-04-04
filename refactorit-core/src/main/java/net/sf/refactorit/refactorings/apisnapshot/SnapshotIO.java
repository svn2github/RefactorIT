/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/** Loads and saves Snapshots */
public class SnapshotIO {
  public static File browserStartLocation;

  public static Snapshot getSnapshotFromFile(String fileName) throws
      IOException {
    List linesList = getSnapshotLinesFromFile(fileName);
    String[] lines = (String[]) linesList.toArray(new String[linesList.size()]);

    return new Snapshot(lines);
  }

  private static List getSnapshotLinesFromFile(String fileName) throws
      IOException {
    List result = new ArrayList();

    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    String oneLine;

    while ((oneLine = reader.readLine()) != null) {
      result.add(oneLine);
    }

    reader.close();

    return result;
  }

  /** Overwrites the file if it exists */
  public static void writeSnapshotToFile(Snapshot snapshot,
      String fileName) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        fileName, false)));
    String[] lines = snapshot.getSerializedForm();
    for (int i = 0; i < lines.length; i++) {
      out.println(lines[i]);
    }
    out.close();
  }
}
