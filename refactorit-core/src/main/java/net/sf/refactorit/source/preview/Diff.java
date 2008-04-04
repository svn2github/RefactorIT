/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.preview;


import net.sf.refactorit.common.util.StringUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public final class Diff {
  private String line1;
  private String line2;

  private String tagBeforeNew = "<font color=\"green\">";
  private String tagAfterNew = "</font color=\"green\">";
  private String tagBeforeGone = "<font color=\"red\">";
  private String tagAfterGone = "</font color=\"red\">";

  private String[] str = new String[2];

  ArrayList currentPath = new ArrayList();
  ArrayList bestPath = new ArrayList();

  private String[] was;
  private String[] nowIS;

  private String markedWas = "";
  private String markedIsNow = "";

  private boolean[][] matrix;

  long t1;

  /**
   * @param before line before refactoring
   * @param after line after refactoring
   */
  public Diff(final String before, final String after) {
    this.line1 = before;
    this.line2 = after;
  }

  public void runDiff() {
    t1 = System.currentTimeMillis();

    str[0] = line1;
    if (line2.length() > 0) {
      markedWas = line1;
    } else {
      markedWas = tagBeforeGone
          + StringUtil.tagsIntoHTML(StringUtil.replace(line1, " ",
          "&nbsp;")) + tagAfterGone;
      return;
    }

    str[1] = line2;
    if (line1.length() > 0) {
      markedIsNow = line2;
    } else {
      markedIsNow = tagBeforeNew
          + StringUtil.tagsIntoHTML(StringUtil.replace(line2, " ",
          "&nbsp;")) + tagAfterNew;
      return;
    }

    // System.out.println("*" +line1 + "*\n*" + line2 + "*");

    // return 2 lists filled with worlds
    getWorldsList();

    // generate matrix with changes
    matrix = getMatrix();

    // get list with all diagonals
    ArrayList diagonals = retDiags(matrix);

    diagonals = retDiags(matrix);

    if (diagonals.size() > 0) {
      int c = 0, d = 0;
      for (int x = 0; x < diagonals.size(); x++) {
        if (c < ( (Diagonal) diagonals.get(x)).cost()) {
          c = ( (Diagonal) diagonals.get(x)).cost();
          d = x;
        }
      }

      final Diagonal diag = (Diagonal) diagonals.get(d);

      final ArrayList li = new ArrayList();
      li.add(diag);

      for (int x = 0; x < diagonals.size(); x++) {
        if ( ( (Diagonal) diagonals.get(x) != diag)
            && !areOverlapping( (Diagonal) diagonals.get(x), diag)) {
          li.add(diagonals.get(x));
        }
      }
      if (li.size() > 1) {
        diagonals = li;
      }

    }
//    System.out.println("Dif: build matrix: " + (System.currentTimeMillis() - t1));
//    System.out.println("Dif: matrix: " + matrix.length + " : "
//        + matrix[0].length);
    if (diagonals.size() > 0) {
      t1 = System.currentTimeMillis();

      loopBreaker = 0;
      visit(diagonals, 0);

      //      System.out.println("Dif: vidit: " + (System.currentTimeMillis() - t1));

      final boolean[] Y = changedY(bestPath, matrix);
      final boolean[] X = changedX(bestPath, matrix);

      StringBuffer buf = new StringBuffer();

      for (int x = 0; x < X.length; x++) {

        if (X[x] || nowIS[x].trim().length() == 0
            || "\n".equals(nowIS[x]) || "\r\n".equals(nowIS[x])
            || "\r".equals(nowIS[x])) {
          buf.append(StringUtil.tagsIntoHTML(StringUtil.replace(nowIS[x], " ",
              "&nbsp;")));
        }
        else {
          buf.append(tagBeforeNew);
          buf.append(StringUtil.tagsIntoHTML(StringUtil.replace(nowIS[x], " ",
              "&nbsp;")));
          buf.append(tagAfterNew);
        }
      }

      markedIsNow = buf.toString();

      buf = new StringBuffer();

      for (int x = 0; x < Y.length; x++) {
        if (Y[x] || was[x].trim().length() == 0
            || "\n".equals(was[x]) || "\r\n".equals(was[x])
            || "\r".equals(was[x])) {
          buf.append(StringUtil.tagsIntoHTML(StringUtil.replace(was[x], " ",
              "&nbsp;")));
        } else {
          buf.append(tagBeforeGone);
          buf.append(StringUtil.tagsIntoHTML(StringUtil.replace(was[x], " ",
              "&nbsp;")));
          buf.append(tagAfterGone);
        }
      }

      markedWas = buf.toString();

    }
  }

  private boolean[] changedX(final ArrayList diags, final boolean mat[][]) {
    final boolean[] X = new boolean[mat[0].length];

    for (int x = 0; x < diags.size(); x++) {
      final Diagonal d = (Diagonal) diags.get(x);

      for (int x1 = d.x1; x1 <= d.x2; x1++) {
        X[x1] = true;
      }
    }

    return X;
  }

  private boolean[] changedY(final ArrayList diags, final boolean mat[][]) {
    final boolean[] Y = new boolean[mat.length];

    for (int x = 0; x < diags.size(); x++) {
      final Diagonal d = (Diagonal) diags.get(x);

      for (int y1 = d.y1; y1 <= d.y2; y1++) {
        Y[y1] = true;
      }
    }

    return Y;
  }

  private int cost(final ArrayList list) {
//    int costX = 0;
//    int costY = 0;

    if (list == null) {
      return 0;
    }

//    for (int x = 0; x < list.size(); x++) {
//      Diagonal d = (Diagonal) list.get(x);
//
//      costX += d.costX();
//      costY += d.costY();
//    }
//
//    return costX + costY;

    int cost = 0;
    for (int x = 0; x < list.size(); x++) {
      final Diagonal d = (Diagonal) list.get(x);

      cost += d.cost();

    }

    return cost;
  }

  private static int loopBreaker = 0;
  private void visit(final ArrayList list, int x) {
    if (loopBreaker >= 10000) {
      return;
    } // XXX: hangs in this visit in some cases, ask Anton for example
    ++loopBreaker;
    for (; x < list.size(); x++) {
      currentPath.add(list.get(x));
      if (!areOverlapping(currentPath)) {
        if (cost(bestPath) < cost(currentPath)) {
          bestPath = new ArrayList(currentPath);
        }
        visit(list, x + 1);
        //break;
      }
      currentPath.remove(list.get(x));
    }
  }

  private boolean areOverlapping(final ArrayList list) {
    if (list.size() > 1) {
      for (int x = 0; x < list.size(); x++) {
        for (int y = x; y < list.size(); y++) {
          if (list.get(x) != list.get(y)) {
            if (areOverlapping( (Diagonal) list.get(x), (Diagonal) list.get(y))) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private boolean areOverlapping(final Diagonal c1, final Diagonal c2) {
    if ( (c1.y2 < c2.y1 && c1.x2 < c2.x1) || (c2.y2 < c1.y1 && c2.x2 < c1.x1)) {
      return false;
    }

    return true;
  }

  private static ArrayList retDiags(final boolean[][] mat) {
    final ArrayList listDiags = new ArrayList();
    Diagonal ct = null;
    for (int y = 0; y < mat.length; y++) {
      for (int x = 0; x < mat[0].length; x++) {
        if (mat[y][x]) { // proverka, est li zdes true
          if (y > 0 && x > 0) {
            if (!mat[y - 1][x - 1]) { // esli 4e po dioganali do etogo?
              ct = getDiag(mat, y, x);
            }
          }
          else {
            ct = getDiag(mat, y, x);
          }
        }
        if (ct != null) {
          listDiags.add(ct);
          ct = null;
        }
      }
    }

    return listDiags;
  }

  private void getWorldsList() {
    final ArrayList list[] = new ArrayList[2];

    StringBuffer buf;

    for (int x = 0; x < list.length; x++) {
      list[x] = new ArrayList();
      buf = new StringBuffer();

      for (int z = 0; z < str[x].length(); z++) {

        if (str[x].charAt(z)=='\r' || str[x].charAt(z) == '\n' || str[x].charAt(z) == '.' ||
            str[x].charAt(z) == '{' ||
            str[x].charAt(z) == '}' || str[x].charAt(z) == ';'
            || str[x].charAt(z) == '(' || str[x].charAt(z) == ')'
            || str[x].charAt(z) == '[' || str[x].charAt(z) == ']') {

          if (buf.length() > 0) {
            list[x].add(new String(buf));
          }

          list[x].add(str[x].charAt(z) + "");

          buf = new StringBuffer();

        }
        else {
          buf.append(str[x].charAt(z));
        }
      }

      if (buf.length() > 0) {
        list[x].add(new String(buf));
      }
    }

    final ArrayList velList[] = new ArrayList[2];
    for (int q = 0; q < velList.length; q++) {
      velList[q] = new ArrayList();

      for (int x = 0; x < list[q].size(); x++) {
        final String s = (String) list[q].get(x);
        final boolean probely[] = new boolean[s.length()];

        for (int z = 0; z < probely.length; z++) {
          if (s.charAt(z) == ' ') {
            probely[z] = true;
          }
        }

        buf = new StringBuffer();
        for (int z = 0; z < probely.length; z++) {
          if (z + 1 < probely.length) {
            if (probely[z] ^ probely[z + 1]) {
              buf.append(s.charAt(z));
              if (buf.length() > 0) {
                velList[q].add(new String(buf));
              }
              buf = new StringBuffer();
            }
            else {
              buf.append(s.charAt(z));
            }
          }
          else {
            buf.append(s.charAt(z));
          }
        }
        if (buf.length() > 0) {
          velList[q].add(new String(buf));
        }
        buf = new StringBuffer();

      }
    }

    was = (String[]) velList[0].toArray(new String[list[0].size()]);
    nowIS = (String[]) velList[1].toArray(new String[list[1].size()]);
  }

  private static Diagonal getDiag(final boolean[][] mat, int y, int x) {
    boolean end = false;

    final Diagonal ct = new Diagonal();

    do {
      ct.setPoint(y, x);
      x++;
      y++;
      if (y < mat.length && x < mat[0].length && mat[y][x]) {

      }
      else {
        end = true;
      }

    }
    while (!end);

    if (ct.isEmpty()) {
      return null;
    }
    else {
      return ct;
    }

  }

  private boolean[][] getMatrix() {

    //build matrix
    // was,  now is
    final boolean[][] mat = new boolean[was.length][nowIS.length];

    for (int b = 0; b < was.length; b++) {
      for (int s = 0; s < nowIS.length; s++) {
        mat[b][s] = was[b].equals(nowIS[s]);
      }
    }

//    DEBUG!!!
//    it print matrix
//
//    StringBuffer sb = new StringBuffer();
//
//    for (int b = 0; b < was.length; b++) {
//      for (int s = 0; s < nowIS.length; s++) {
//        if (mat[b][s]) {
//          sb.append(" 1");
//        }
//        else {
//          sb.append(" 0");
//        }
//
//      }
//      sb.append('\n');
//
//    }
//
//    System.out.println(new String(sb));
    return mat;
  }

  public String getMarkedWas() {
    return this.markedWas;
  }

  public String getMarkedIsNow() {
    return this.markedIsNow;
  }

  public static void main(final String[] args) {

    final StringBuffer buf = new StringBuffer();
    final StringBuffer buf1 = new StringBuffer();
    try {

      FileReader file_reader = new FileReader(
          "/home/buhich/workspace/testnow/test/Test.java");
      BufferedReader br_reader = new BufferedReader(file_reader);
      String line = "";

      do {
        line = br_reader.readLine();
        buf.append(line);

      }
      while (line != null);

      file_reader.close();

      file_reader = new FileReader(
          "/home/buhich/workspace/testnow/test/src/Test.java");
      br_reader = new BufferedReader(file_reader);
      line = "";

      do {
        line = br_reader.readLine();
        buf1.append(line);

      }
      while (line != null);

      file_reader.close();
    }
    catch (FileNotFoundException e) {}
    catch (IOException e) {}

    final long t1 = System.currentTimeMillis();
    final Diff dif = new Diff(new String(buf), new String(buf1));
    dif.runDiff();
    final long t2 = System.currentTimeMillis();
    System.out.println(dif.getMarkedIsNow());
    System.out.println(t2 - t1);

  }

  public void setTagAfterGone(final String tagAfterGone) {
    this.tagAfterGone = tagAfterGone;
  }

  public void setTagBeforeNew(final String tagBeforeNew) {
    this.tagBeforeNew = tagBeforeNew;
  }

  public void setTagAfterNew(final String tagAfterNew) {
    this.tagAfterNew = tagAfterNew;
  }

  public void setTagBeforeGone(final String tagBeforeGone) {
    this.tagBeforeGone = tagBeforeGone;
  }
}
