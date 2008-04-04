/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone.editor;


import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.standalone.JBrowserPanel;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.tree.NodeIcons;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;


public class JSourcePanel extends JPanel {
  private JLabel label;
  private JSourceArea text;

  public JSourcePanel(JBrowserPanel browser) {
    setLayout(new BorderLayout());

    NodeIcons icons = NodeIcons.getNodeIcons();

    label = new JLabel("Source", icons.getSourceIcon(), JLabel.LEFT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    Dimension dim = new Dimension(200, 23);
    label.setMinimumSize(dim);
    label.setPreferredSize(dim);

    add(label, BorderLayout.NORTH);

    text = new JSourceArea(browser);

    JScrollPane scroll = RuntimePlatform.createDefaultScrollPane(text);
    scroll.setRowHeaderView(new JRowsHeader(text));

    add(scroll, BorderLayout.CENTER);
  }

  public JLabel getHeaderLabel() {
    return label;
  }

  public JSourceArea getSourceArea() {
    return text;
  }

  public void setSource(SourceHolder source) {
    if (source != null && source.getSource() != null) {
      label.setText("Source - /" + source.getSource().getRelativePath());
    } else {
      label.setText("");
    }
    text.getSourceDocument().setSource(source);
    text.select(0, 0);
  }
}
