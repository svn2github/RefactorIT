/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class JRowsHeader extends JComponent {
  private static final int BORDER = 4;

  int useCompilationUnitForLineNum = -1;

  private JTextComponent area;

  /**
   * Constructor for JRowsHeader
   */
  public JRowsHeader(JTextComponent area) {
    this.area = area;

    setBackground(area.getBackground());

    area.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent evt) {
        useCompilationUnitForLineNum = -1;
        update();
      }
    });

    update();
  }

  /**
   * @see JComponent#paintComponent(Graphics)
   */
  protected void paintComponent(Graphics g) {
    Rectangle clip = g.getClipBounds();

    int width = getWidth();
    int c0 = clip.y;
    int c1 = c0 + clip.height;

    final Color color = getBackground();
    g.setColor(color);
    g.fillRect(clip.x, c0, clip.width, clip.height);
    g.setColor(color.darker());
    g.drawLine(width - 1, c0, width - 1, c1);
    g.setColor(getForeground());

    g.setFont(area.getFont());
    final FontMetrics fm = g.getFontMetrics();
    int fh = getFontHeight(g);

    int line1 = c1 / fh;
    for (int i = c0 / fh; i <= line1; i++) {
      String line = Integer.toString(i + 1);
      int w = fm.stringWidth(line);
      g.drawString(line, width - BORDER - w, fh + i * fh);
    }
  }

  /**
   * It suddenly happens that FontMetrics.getHeight() returns wrong metrics,
   * but we are calculating row numbers by dividing area height by font neight,
   * so we can and need to control that row numbers calculated by two different
   * methods are the same.
   * @return font height which represents our row number best.
   */
  private int getFontHeight(Graphics g) {
    int fh = g.getFontMetrics().getHeight();
    if (this.useCompilationUnitForLineNum == -1
        || this.useCompilationUnitForLineNum == 0) {
      try {
        fh = (int) area.getPreferredSize().getHeight() /
            ((JSourceArea) area).getSourceDocument().getSource()
            .getSource().getLineCount();
      } catch (Exception e) {
      }

      if (this.useCompilationUnitForLineNum == -1) {
        this.useCompilationUnitForLineNum = 0;
        if (fh == g.getFontMetrics().getHeight()) {
          this.useCompilationUnitForLineNum = 1;
        }
      }
    }

    return fh;
  }

  protected void update() {
    Graphics g = getGraphics();
    if (g == null) {
      return;
    }

    try {
      g.setFont(area.getFont());

      FontMetrics fm = g.getFontMetrics();
      int fh = getFontHeight(g);

      Dimension dim = area.getSize();

      int chars = Integer.toString(dim.height / fh).length();
      dim.width = 8 + fm.stringWidth("8888888".substring(0, chars));

      setPreferredSize(dim);

      invalidate();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      g.dispose();
    }
  }
}
