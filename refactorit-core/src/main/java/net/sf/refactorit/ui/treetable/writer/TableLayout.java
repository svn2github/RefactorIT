/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;


import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TableLayout {
  public static List getColumnsForLine(BinTreeTableModel model,
      LineContentsProvider contents) {
    List result = new ArrayList();

    result.add(contents.getTypeColumn());
    result.add(contents.getNameColumn());

    for (int i = 1; i < model.getColumnCount(); i++) {
      if (model.isShowing(i)) {
        result.add(contents.getColumn(i));
      }
    }

    result.add(contents.getPackageColumn());
    result.add(contents.getClassColumn());

    return result;
  }

  private static void formatLines(final TableFormat writer,
      final BinTreeTableModel model, final List nodes,
      final StringBuffer result, final Set processedNodes) {

    addColumnNamesLine(writer, model, result);

    for (int i = 0; i < nodes.size(); i++) {
      collectClipboardText(writer, model, result, processedNodes,
          nodes.get(i));
    }
  }

  public static void addColumnNamesLine(TableFormat writer,
      final BinTreeTableModel model, final StringBuffer result) {
    writer.startNewLine(result);

    List columnNames = createColumnNames(model);
    writer.addColumns(columnNames, result, true);

    writer.endLine(result);
  }

  private static List createColumnNames(final BinTreeTableModel model) {
    return getColumnsForLine(model, new HeadersLine(model));
  }

  public static List createColumnContents(BinTreeTableModel model,
      ParentTreeTableNode node) {
    return getColumnsForLine(model, new ContentsLine(model, node));
  }

  // Util methods

  public static void collectClipboardText(TableFormat writer,
      final BinTreeTableModel model, final StringBuffer result,
      final Set processedNodes, final Object node) {
    //we support only ParentTreeTableNodes
    if (node instanceof ParentTreeTableNode) {
      final StringBuffer tmp = ((ParentTreeTableNode) node)
          .collectClipboardTextRecursively(writer, model, processedNodes);
      if (tmp != null) {
        result.append(tmp.toString());
      }
    }
  }

  public static StringBuffer getClipboardText(TableFormat writer,
      BinTreeTableModel model, String title) {
    return collectClipboardText(writer, model, title,
        Collections.singletonList(model.getRoot()));
  }

  public static StringBuffer collectClipboardText(TableFormat writer,
      BinTreeTableModel model, String title, List nodes) {
    StringBuffer result = new StringBuffer();
    Set processedNodes = new HashSet();

    writer.startPage(title, result);
    writer.startTable(result);

    formatLines(writer, model, nodes, result, processedNodes);

    writer.endTable(result);
    writer.endPage(result);

    return result;
  }
}
