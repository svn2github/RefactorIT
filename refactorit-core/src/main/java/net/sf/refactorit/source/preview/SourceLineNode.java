/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.preview;

import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.Line;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.awt.Font;


/**
 * @author Tonis Vaga
 * @author Kirill
 */
public class SourceLineNode extends BinTreeTableNode {
  private Line line;
  private int lineNumber;
  private String lineSource = null;
  private String fontProp;

  public SourceLineNode(Line line, int lineNumber) {
    super("", true);
    this.line = line;
    this.lineNumber = lineNumber;
  }

  public String getLineSource() {
    if (lineSource != null) {
      return lineSource;
    }

    Line line = this.getLine();

    String str = line.getMarkedNewContent();
    if (isEmptyLine(str) || isNotMarked(str)) {
      str = line.getMarkedOldContent();
    }

    str = StringUtil.replace(str, "\r\n", "\n");
    str = StringUtil.replace(str, "\r", "\n");
    str = StringUtil.replace(str, "\n", "<br>");
    str = str.trim();

    while (true) {
      boolean changed = false;
      while (str.endsWith("\n")) {
        str = str.substring(0, str.length() - "\n".length()).trim();
        changed = true;
      } while (str.endsWith("<br>")) {
        str = str.substring(0, str.length() - "<br>".length()).trim();
        changed = true;
      }
      if (!changed) {
        break;
      }
    } while (true) {
      // FIXME: there is <html> in the beginning
      boolean changed = false;
      while (str.startsWith("\n")) {
        str = str.substring("\n".length()).trim();
        changed = true;
      } while (str.startsWith("<br>")) {
        str = str.substring("<br>".length()).trim();
        changed = true;
      }
      if (!changed) {
        break;
      }
    }

    if (str.endsWith("<br></font color=\"red\">")
        || str.endsWith("<BR></font color=\"red\">")) {
      str = str.substring(0,
          str.length() - "<br></font color=\"red\">".length()).trim();
      str += "</font color=\"red\">";
    }
    if (str.endsWith("<br></font color=\"green\">")
        || str.endsWith("<BR></font color=\"green\">")) {
      str = str.substring(0,
          str.length() - "<br></font color=\"green\">".length()).trim();
      str += "</font color=\"green\">";
    }

    if (fontProp == null) {
      fontProp = "font-family: monospace";
      Font font = BinTree.getFontProperty();

      if (font != null) {
        fontProp += ", " + font.getName() + "; font-size: " + font.getSize() +
            HtmlUtil.FONT_SIZE_MODIFIER_PIXEL + ";";

      } else {
        fontProp += ";";
      }
    }

    lineSource = "<html><body style=\"white-space: nowrap; " + fontProp + "\">" + str + "</body></html>";
    return lineSource;
  }

  private boolean isEmptyLine(String line) {
    if (line == null) {
      return true;
    }
    for (int x = 0; x < line.length(); x++) {
      char c = line.charAt(x);
      if (c == ' ' || c == '\n' || c == '\r' || c == '<') {
        if (c == '<') {
          for (; line.charAt(x) != '>' && line.length() > x; x++) {

          }
        }
        continue;
      }
      return false;
    }

    return true;
  }

  private boolean isNotMarked(String line) {
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == '<' || line.charAt(i) == '>') {
        return false;
      }
    }

    return true;
  }

  public int queryLineNumber() {
    return lineNumber;
  }

  public String getLineNumber() {
    return new Integer(lineNumber).toString();
  }

  public SourceHolder getSource() {
    return ((SourceNode)this.getParent()).getSource();
  }

  public Line getLine() {
    return line;
  }
}
