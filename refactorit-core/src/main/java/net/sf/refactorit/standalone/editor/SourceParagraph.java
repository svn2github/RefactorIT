/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;


import net.sf.refactorit.source.format.FormatSettings;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import javax.swing.text.View;

import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;


public class SourceParagraph extends ParagraphView {
  public SourceParagraph(Element elem) {
    super(elem);
  }

  public boolean isVisible() {
    return true;
  }

  public float getMinimumSpan(int axis) {
    return getPreferredSpan(axis);
  }

  public float getPreferredSpan(int axis) {
    if (axis == View.X_AXIS) {
      Document d = getDocument();
      Font f = ((StyledDocument) d).getFont(getAttributes());
      Container c = getContainer();
      FontMetrics fm = c.getFontMetrics(f);

      int start = getStartOffset();
      int end = getEndOffset();
      Segment seg = new Segment();

      try {
        getDocument().getText(start, end - start, seg);
      } catch (BadLocationException ignore) {}

      return Utilities.getTabbedTextWidth(seg, fm, 0, this, start);
    }

    return super.getMaximumSpan(axis);
  }

  public int getResizeWeight(int axis) {
    switch (axis) {
      case View.X_AXIS:
        return 1;
      case View.Y_AXIS:
        return 0;
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  public float getAlignment(int axis) {
    if (axis == View.X_AXIS) {
      return 0;
    }

    return super.getAlignment(axis);
  }

  /* Doesn't work on JDK 1.2.2_001
   protected void layout( int width, int height ) {
    super.layout( Integer.MAX_VALUE - 1, height );
   }
   */

  public float nextTabStop(float x, int tabOffset) {
    Document d = getDocument();
    Font f = ((StyledDocument) d).getFont(getAttributes());
    Container c = getContainer();
    FontMetrics fm = c.getFontMetrics(f);

    int width = FormatSettings.getTabSize() * fm.charWidth('W');
    int tb = (int) getTabBase();
    return ((((int) x - tb) / width + 1) * width + tb);
  }

}
