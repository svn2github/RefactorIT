/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


public class SourceEditorKit extends StyledEditorKit implements ViewFactory {
  /**
   * @see StyledEditorKit#createDefaultDocument()
   */
  public Document createDefaultDocument() {
    return new SourceDocument();
  }

  /**
   * @see DefaultEditorKit#getViewFactory()
   */
  public ViewFactory getViewFactory() {
    return this;
  }

  /**
   * @see ViewFactory#create(Element)
   */
  public View create(Element elem) {
    String kind = elem.getName();

    if (kind != null) {
      if (kind.equals(AbstractDocument.ContentElementName)) {
        return new LabelView(elem);
      }

      if (kind.equals(AbstractDocument.ParagraphElementName)) {
        return new SourceParagraph(elem);
      }

      if (kind.equals(AbstractDocument.SectionElementName)) {
        return new BoxView(elem, View.Y_AXIS);
      }

      if (kind.equals(StyleConstants.ComponentElementName)) {
        return new ComponentView(elem);
      }

      if (kind.equals(StyleConstants.IconElementName)) {
        return new IconView(elem);
      }
    }

    return new LabelView(elem);
  }
}
