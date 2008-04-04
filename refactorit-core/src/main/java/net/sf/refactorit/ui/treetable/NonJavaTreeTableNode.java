/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.ui.tree.UITreeNode;



/**
 * Represents a node that is referring to a line in some non java file
 * (e.g. xml or text file).
 *
 * @author  tanel
 * @author Anton Safonov
 */
public final class NonJavaTreeTableNode extends BinTreeTableNode {
  public NonJavaTreeTableNode(final Occurrence occurrence) {
    super(occurrence);
    setType(UITreeNode.NODE_NON_JAVA);

    setDisplayName(occurrence.getLine().getSource().getDisplayPath());

    setLine(occurrence.getLine().getLineNumber());

    String content = occurrence.getLine().getContent();
    StringBuffer buf = new StringBuffer(80);
    buf.append(StringUtil.tagsIntoHTML(
        content.substring(0, occurrence.getStartPos())));
    buf.append("<FONT color=#C02040>");
    buf.append(StringUtil.tagsIntoHTML(
        content.substring(occurrence.getStartPos(), occurrence.getEndPos())));
    buf.append("</FONT>");
    buf.append(StringUtil.tagsIntoHTML(
        content.substring(occurrence.getEndPos())));
    setLineSource(buf.toString());
  }
}
