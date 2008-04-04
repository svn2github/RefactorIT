/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.edit.Line;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Kirill Buhhalko
 */
class CompareDialog implements Runnable {
  final RitDialog dialog;

  private JButton buttonOk = new JButton("OK");
  private JTextPane textAreaOld = new JTextPane();
  private JTextPane textAreaNew = new JTextPane();
  private int lineNr = 0;

  JScrollPane scrollPaneOfOld = new JScrollPane(textAreaOld);
  JScrollPane scrollPaneOfNew = new JScrollPane(textAreaNew);

  private Action close = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      dialog.dispose();
    }
  };

  CompareDialog(IdeWindowContext context, List lines, int lineNr,
      SourceHolder source) {
    this.lineNr = lineNr;
    dialog = RitDialog.create(context);
    dialog.setTitle("Source compare - " + source.getName());
    Dimension d = dialog.getMaximumSize();
    dialog.setSize(d.width - 50, d.height - 150);

    StringBuffer oldContentBuffer = new StringBuffer();
    StringBuffer newContentBuffer = new StringBuffer();

    if (lines != null)
      for (int x = 0; x < lines.size(); x++) {
        Line line = (Line) lines.get(x);

        if (line.isChanged()) {
          newContentBuffer.append(
              setBackgroundBetween_BR(
              replaceLineSeparatorTo_BR(line.getMarkedNewContent()),
              "<code style=\"background-color: #fffade\">", "</code>"));

          oldContentBuffer.append(
              setBackgroundBetween_BR(
              replaceLineSeparatorTo_BR(line.getMarkedOldContent()),
              "<code style=\"background-color: #fffade\">", "</code>"));

          if (countLineBreaks(line.getContent())
              > countLineBreaks(line.getOriginalContent())) {

            oldContentBuffer.append(doLineBreaks(countLineBreaks(line.
                getContent())
                - countLineBreaks(line.getOriginalContent())));

          } else if (countLineBreaks(line.getContent())
              < countLineBreaks(line.getOriginalContent())) {

            newContentBuffer.append(doLineBreaks( -(countLineBreaks(line.
                getContent())
                - countLineBreaks(line.getOriginalContent()))));
          }

        } else {
          newContentBuffer.append(setBackgroundBetween_BR(
              replaceLineSeparatorTo_BR(StringUtil.textIntoHTML(line.getContent())),
              "<code>", "</code>"));

          oldContentBuffer.append(setBackgroundBetween_BR(
              replaceLineSeparatorTo_BR(StringUtil.textIntoHTML(line.
              getOriginalContent())),
              "<code>", "</code>"));
        }
      }

    Font font = textAreaOld.getFont();

    setBackgroundColor(Color.WHITE);

    textAreaOld.setEditorKit(new HTMLEditorKit());
    textAreaNew.setEditorKit(new HTMLEditorKit());

    textAreaOld.setText(HtmlUtil.styleBody(
        convert_BRbackToLineSeparators(oldContentBuffer.toString()), font));
    textAreaNew.setText(HtmlUtil.styleBody(
        convert_BRbackToLineSeparators(newContentBuffer.toString()), font));

    JPanel contentPanel = getContentPanel();
    dialog.setContentPane(contentPanel);

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk);

    SwingUtilities.invokeLater(this);
  }

  private void setBackgroundColor(Color color) {
    textAreaOld.setBackground(color);
    textAreaNew.setBackground(color);
  }

  private String setBackgroundBetween_BR(String s, String openTag,
      String closeTag) {
    String temp[];
    StringBuffer buf = new StringBuffer();
    temp = splitBy(s, "<br>");

    for (int i = 0; i < temp.length; i++) {
      buf.append(openTag).append(temp[i]).append(closeTag);
      if (i + 1 < temp.length) {
        buf.append("<br>");
      }

    }
    return buf.toString();
  }

  String[] splitBy(String s, String pattern) {
    ArrayList list = new ArrayList();
    int start = 0, end = 0;
    boolean cont = true;

    while (cont) {
      end = s.indexOf(pattern, start);
      if (end != -1) {
        list.add(s.substring(start, end));
        start = end + pattern.length();
      } else {
        list.add(s.substring(start, s.length()));
        cont = false;
      }
    }

    return (String[]) list.toArray(new String[list.size()]);
  }

  private String convert_BRbackToLineSeparators(String s) {
    String r = StringUtil.replace(s, "&nbsp;", " ");
    r = StringUtil.replace(r, "<br>", "\n");
    return "<pre>" + r + "</pre>";
  }

  private JPanel getContentPanel() {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout());

    contentPanel.add(getTextAreasPanel(), BorderLayout.CENTER);
    contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);

    return contentPanel;
  }

  private JPanel getTextAreasPanel() {
    JPanel textAreaPanel = new JPanel();
    textAreaPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );
    textAreaPanel.setLayout(new GridLayout(1, 2));

    textAreaOld.setEditable(false);
    textAreaNew.setEditable(false);

    textAreaPanel.add(scrollPaneOfOld);
    textAreaPanel.add(scrollPaneOfNew);

    scrollPaneOfOld.getHorizontalScrollBar().setEnabled(true);
    scrollPaneOfNew.getHorizontalScrollBar().setEnabled(true);

    scrollPaneOfNew.getVerticalScrollBar().addAdjustmentListener(new
        AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = CompareDialog.this.scrollPaneOfNew.
            getVerticalScrollBar();
        CompareDialog.this.scrollPaneOfOld.getVerticalScrollBar().setValue(bar.
            getValue());
      }
    });
    scrollPaneOfOld.getVerticalScrollBar().addAdjustmentListener(new
        AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = CompareDialog.this.scrollPaneOfOld.
            getVerticalScrollBar();
        CompareDialog.this.scrollPaneOfNew.getVerticalScrollBar().setValue(bar.
            getValue());
      }
    });
    scrollPaneOfNew.getHorizontalScrollBar().addAdjustmentListener(new
        AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = CompareDialog.this.scrollPaneOfNew.
            getHorizontalScrollBar();
        CompareDialog.this.scrollPaneOfOld.getHorizontalScrollBar().setValue(
            bar.getValue());
      }
    });
    scrollPaneOfOld.getHorizontalScrollBar().addAdjustmentListener(new
        AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = CompareDialog.this.scrollPaneOfOld.
            getHorizontalScrollBar();
        CompareDialog.this.scrollPaneOfNew.getHorizontalScrollBar().setValue(
            bar.getValue());
      }
    });

    // Disable ENTER-handling so that we could use Enter for closing the dialog, for example.
    textAreaOld.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(
        KeyEvent.VK_ENTER, 0), close);
    textAreaNew.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(
        KeyEvent.VK_ENTER, 0), close);

    // add popup menu with 'copy'
    textAreaNew.addMouseListener(new PopupListener(getPopupMenu(textAreaNew)));
    textAreaOld.addMouseListener(new PopupListener(getPopupMenu(textAreaOld)));

    return textAreaPanel;
  }

  private JPopupMenu getPopupMenu(final JTextPane textArea) {
    final JPopupMenu popup = new JPopupMenu();
    JMenuItem menuItem = new JMenuItem("Copy");
    menuItem.addMouseListener(new MouseListener() {

      public void mousePressed(MouseEvent event) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(textArea.
            getSelectedText());
        clipboard.setContents(selection, selection);
      }

      public void mouseExited(MouseEvent event) {
      }

      public void mouseEntered(MouseEvent event) {
      }

      public void mouseReleased(MouseEvent event) {
      }

      public void mouseClicked(MouseEvent event) {
      }
    });
    popup.add(menuItem);
    return popup;
  }

  private JPanel getButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    GridBagConstraints constr = new GridBagConstraints();
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.CENTER;
    constr.gridx = 0;
    constr.gridy = 0;
    buttonOk.setPreferredSize(new Dimension((int) (buttonOk.getPreferredSize().
        getWidth() * 2), (int) buttonOk.getPreferredSize().getHeight()));
    buttonPanel.add(buttonOk, constr);

    buttonOk.addActionListener(close);

    return buttonPanel;
  }

  private String replaceLineSeparatorTo_BR(String str) {
    str = StringUtil.replace(str, "\r\n", "\n");
    str = StringUtil.replace(str, "\r", "\n");
    str = StringUtil.replace(str, "\n", "<br>");

    return str;
  }

  private int countLineBreaks(String str) {
    int n = 0;

    for (int x = 0; x < str.length(); x++) {
      if (str.charAt(x) == '\n') {
        n++;
      }
    }
    return n;
  }

  private String doLineBreaks(int n) {
    StringBuffer buf = new StringBuffer();

    for (int x = 0; x < n; x++) {
      buf.append(FormatSettings.LINEBREAK);
    }

    return replaceLineSeparatorTo_BR(buf.toString());
  }

  public void show() {
    dialog.show();

  }

  public void run() {
    int max = scrollPaneOfNew.getVerticalScrollBar().getMaximum();
    int lines = textAreaNew.getHeight() /
        textAreaNew.getFontMetrics(textAreaNew.getFont()).getHeight();
    scrollPaneOfNew.getVerticalScrollBar()
        .setValue((max / lines) * (lineNr - 1));
  }

  private class PopupListener implements MouseListener {
    JPopupMenu popup;

    private PopupListener(JPopupMenu popup) {
      this.popup = popup;
    }

    public void mouseReleased(MouseEvent event) {
      showPopupMenu(event);
    }

    public void mousePressed(MouseEvent event) {
      showPopupMenu(event);
    }

    private void showPopupMenu(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        popup.show(event.getComponent(),
            event.getX(), event.getY());
      }
    }

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }
  }

}
