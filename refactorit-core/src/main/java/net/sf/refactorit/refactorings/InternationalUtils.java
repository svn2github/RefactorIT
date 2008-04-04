/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public final class InternationalUtils {
  private InternationalUtils() {
  }

  /**
   * @param results
   * @param rootNode
   */
  public static void duplicateLiteralsReport(final ArrayList results,
      final BinTreeTableNode rootNode) {
    final MultiValueMap map = new MultiValueMap();

    for (int i = 0; i < results.size(); ++i) {
      BinLiteralExpression literal = (BinLiteralExpression) results.get(i);
      map.putAll(literal.getLiteral(), literal);
    }

    List keyList = new ArrayList(map.keySet());
    for (int i = keyList.size() - 1; i >= 0; --i) {
      Object key = keyList.get(i);
      if (map.get(key).size() == 1) {
        map.clearKey(key);
        keyList.remove(i);
      }
    }

    Collections.sort(keyList,
        new Comparator() {
      public int compare(Object o1, Object o2) {
        return new Integer(map.get(o1).size()).compareTo(
            new Integer(map.get(o2).size()));
      }
    }
    );

    for (int i = keyList.size() - 1; i >= 0; --i) {
      String key = (String) keyList.get(i);
      BinTreeTableNode parentNode = new BinTreeTableNode(key);

      List literalList = map.get(key);

      for (int c = 0; c < literalList.size(); ++c) {
        BinLiteralExpression aLiteral = (BinLiteralExpression) literalList.get(
            c);
        BinTreeTableNode childNode = new BinTreeTableNode(aLiteral.
            getCompilationUnit().getName() + " - " + aLiteral.getLiteral());
        CompilationUnit childSource = aLiteral.getCompilationUnit();
        ASTImpl childAst = aLiteral.getRootAst(childSource);
        childNode.setSourceHolder(childSource);
        childNode.addAst(childAst);
        childNode.setLine(childAst.getLine());
        parentNode.addChild(childNode);
      }

      rootNode.addChild(parentNode);
      parentNode.sortAllChildren();
    }

  }
}
