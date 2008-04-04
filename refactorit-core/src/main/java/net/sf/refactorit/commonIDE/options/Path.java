/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @author Tonis Vaga
 */
public class Path {
  private PathItem[] items;

  // should be not used in path for all file systems
  static final char PATH_SEPARATOR = File.pathSeparatorChar;

  /**
   * Create empty path
   */
  public Path() {
    this.items = new PathItem[0];
  }

  public Path(PathItem[] items) {
    this.items = items;
  }

  public Path(final String path) {
    deserialize(path);
  }

  public void addItem(final PathItem item) {
    PathItem[] newItems = new PathItem[items.length + 1];
    System.arraycopy(items, 0, newItems, 0, items.length);
    newItems[items.length] = item;
    items = newItems;
  }

  /**
   * reads path items from string
   *
   * @param str path in serialized form
   */
  public void deserialize(String str) {
    if (str == null || str.equals("")) {
      this.items = new PathItem[0];
      return;
    }
    List itemsList = new ArrayList();
    StringTokenizer tok = new StringTokenizer(str, "" + PATH_SEPARATOR);
    while (tok.hasMoreTokens()) {
      itemsList.add(tok.nextToken());
    }

    int size = itemsList.size();
    this.items = new PathItem[size];

    for (int index = 0; index < size; ++index) {
      items[index] = new PathItem((String) itemsList.get(index));
    }
  }

  public String serialize() {
    return toString();
  }

  /**
   * @return string in serialized form
   */
  public String toString() {
    StringBuffer result = new StringBuffer(100);

    for (int i = 0; i < getItems().length; i++) {
      result.append(getItems()[i].path + PATH_SEPARATOR);
    }
    return result.toString();
  }

  /**
   * @return list of path items
   */
  public List toPathItems() {
    return Arrays.asList(getItems());
  }

  public PathItem[] getItems() {
    return this.items;
  }


}
