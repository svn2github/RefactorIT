/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.primetime.editor.EditorActions;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.BrowserAdapter;
import com.borland.primetime.ide.NodeViewer;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.TextFileNode;
import com.borland.primetime.viewer.TextNodeViewer;
import com.borland.primetime.viewer.TextView;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;


//import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class BraceMatcher extends BrowserAdapter implements CaretListener {

  private boolean listening = true;
  private DefaultHighlighter.DefaultHighlightPainter highLightPainter = new
      DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  public BraceMatcher() {
  }

  public void caretUpdate(CaretEvent e) {
    if (!RefactorItPropGroup.CUSTOM_BRACE_MATCHER.getBoolean()) {
      return;
    }

    if (!listening) {
      return;
    }
    try {
      // FIXME: maybe add a timer here so that calculations are only done
      // when the thing has stayed in place for 0.5 sec
      listening = false;
      JTextComponent textComponent = (JTextComponent) e.getSource();
      removeHighlights(textComponent);
      applyHighlights(textComponent);
    } finally {
      listening = true;
    }
  }

  private void removeHighlights(JTextComponent textComponent) {
    Object highlight = textComponent.getClientProperty("RFB_HL_1");
    if (highlight != null) {
      textComponent.getHighlighter().removeHighlight(highlight);
      textComponent.putClientProperty("RFB_HL_1", null);
    }

    highlight = textComponent.getClientProperty("RFB_HL_2");
    if (highlight != null) {
      textComponent.getHighlighter().removeHighlight(highlight);
      textComponent.putClientProperty("RFB_HL_2", null);
    }
  }

  private void applyHighlights(JTextComponent textComponent) {
    try {
      int dot = textComponent.getCaret().getDot();

      if (dot == 0) {
        return;
      }

      char c = textComponent.getText(textComponent.getCaret().getDot() - 1,
          1).charAt(0);

      if (c == '{' || c == '}' || c == '(' || c == ')') {

        Highlighter highlighter = textComponent.getHighlighter();
        Object highlight = highlighter.addHighlight(dot - 1, dot,
            highLightPainter);
        textComponent.putClientProperty("RFB_HL_1", highlight);

        int selectionStart = textComponent.getSelectionStart();
        int selectionEnd = textComponent.getSelectionEnd();
        textComponent.setCaretPosition(dot - 1);

        ActionEvent actionEvent = new ActionEvent(textComponent,
            ActionEvent.ACTION_PERFORMED,
            "<SYNTHETIC EVENT>");

        EditorActions.ACTION_SelectionMatchBrace.actionPerformed(actionEvent);

        int match = textComponent.getCaretPosition();
        if (match > dot) {
          match++;
        } else if (match == (dot - 1)) {
          match = dot;

        }
        highlight = highlighter.addHighlight(match - 1, match, highLightPainter);
        textComponent.putClientProperty("RFB_HL_2", highlight);

        textComponent.setCaretPosition((dot == selectionStart) ?
            selectionEnd :
            selectionStart);
        textComponent.moveCaretPosition(dot);

      }
    } catch (BadLocationException e) {
    }
  }

  public void browserNodeActivated(Browser browser, Node node) {
    updateCaretListeners(browser, node, this);
  }

  public void browserNodeClosed(Browser browser, Node node) {
    updateCaretListeners(browser, node, null);
  }

  private void updateCaretListeners(Browser browser, Node node,
      CaretListener listener) {
    if ((node != null) && (node instanceof TextFileNode)) {
      NodeViewer[] nodeViewers = browser.getViewers(node);
      for (int i = 0; i < nodeViewers.length; ++i) {
        if (nodeViewers[i] instanceof TextNodeViewer) {
          TextNodeViewer textNodeViewer = (TextNodeViewer) nodeViewers[i];
          doUpdate(textNodeViewer.getViewerComponent(), listener);
        }
      }
    }
  }

  private void doUpdate(Container container, CaretListener listener) {
    Component[] children = container.getComponents();
    for (int i = 0, n = children.length; i < n; ++i) {
      if (children[i] instanceof TextView) {
        if (listener == null) {
          ((TextView) children[i]).getEditor().removeCaretListener(this);
        } else {
          ((TextView) children[i]).getEditor().addCaretListener(this);
        }
      } else if (children[i] instanceof Container) {
        doUpdate((Container) children[i], listener);
      }
    }
  }

}
