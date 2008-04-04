/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.awt.Color;
import java.awt.Font;


/**
 * Utilities for styling text into HTML.
 *
 * @author Igor Malinin
 * @author Risto Alas
 * @author Anton Safonov
 */
public final class HtmlUtil {
  // A hack for BinTreeTable (fixes RIM-64: large HTML fonts in Java 1.5.0_beta2 under WinXP)
  public static final String FONT_SIZE_MODIFIER_PIXEL =
      isWindowsXp()
      && System.getProperty("java.version").indexOf("1.5") != -1
      && System.getProperty("java.version").indexOf("beta") != -1
      ? "px" : "pt";

  public static final String styleText(String text, Color color) {
    return surroundWithFontStyle(text, null, color);
  }

  /** E.g. &lt;FONT style='font-family: arial; font-size: 5px; font-weight: bold;'&gt; */
  public static final String styleText(String text, Font font) {
    return surroundWithFontStyle(text, font, null);
  }

  public static final String styleText(String text, Font font, Color color) {
    return surroundWithFontStyle(text, font, color);
  }

  private static String surroundWithFontStyle(
      String text, Font font, Color color) {
    if (text == null) {
      return "null";
    }
    String result;
    final StringBuffer buf = new StringBuffer(text.length() + 100);
    synchronized (buf) {
      buf.append("<FONT style='");
      appendStyle(buf, font);
      buf.append(' ');
      appendStyle(buf, color);
      buf.append("'>");
      buf.append(text);
      buf.append("</FONT>");
      result = buf.toString();
    }

    return result;
  }

  /**
   * Style text according with font settings.
   *
   * E.g. &lt;HTML&gt;&lt;BODY style="font-family: arial;
   * font-size: 5px; font-weight: bold; color: #E7E7E7;
   * background-color: #006699;"&gt;text&lt;/BODY&gt;&lt;/HTML&gt;
   *
   * @return Font start tag.
   */
  public static final String styleBody(String text, Font font) {
    StringBuffer buf = new StringBuffer(text.length() + 100);
    buf.append("<HTML><BODY style='white-space: nowrap; ");
    appendStyle(buf, font);
    buf.append("'>").append(text).append("</BODY></HTML>");

    return buf.toString();
  }

  private static void appendStyle(final StringBuffer buf, Font font) {
    if (font == null) {
      return;
    }

    buf.append("font-family: ").append(font.getName())
        .append("; font-size: ").append(font.getSize())
        .append(FONT_SIZE_MODIFIER_PIXEL).append(";");

    int style = font.getStyle();
    if (style != 0) {
      if ((style & Font.BOLD) != 0) {
        buf.append(" font-weight: bold;");
      }

      if ((style & Font.ITALIC) != 0) {
        buf.append(" font-style: italic;");
      }
    }
  }

  /** Example: "color: #FF007F" */
  private static void appendStyle(final StringBuffer buf, Color color) {
    if (color == null) {
      return;
    }

    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();

    buf.append("color: #")
        .append(toHexChar(r >> 4)).append(toHexChar(r))
        .append(toHexChar(g >> 4)).append(toHexChar(g))
        .append(toHexChar(b >> 4)).append(toHexChar(b));
  }

  /** '0' - '9', 'A', 'B', 'C', 'D', 'E', 'F' */
  private static char toHexChar(int digit) {
    digit &= 0x0F; // last four bits
    if (digit < 10) {
      return (char) ('0' + digit);
    }

    return (char) (('A' - 10) + digit);
  }

  public static String stripHtmlBody(String text) {
    if (text == null) {
      return text;
    }
    int start = text.indexOf("<BODY");
    if (start != -1) {
      start = text.indexOf(">", start) + 1;
    }
    int end = text.indexOf("</BODY");
    if (start != -1 && end != -1) {
      return text.substring(start, end);
    }

    return text;
  }

  private static boolean isWindowsXp() {
    String os = System.getProperty("os.name").toLowerCase();
    return os != null && os.indexOf("windows xp") >= 0;
  }
}
