/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.ResourceUtil;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;


/**
 * Insert the type's description here.
 *
 * @author Vladislav Vislogubov
 */
public class JFontChooser extends JComponent {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JFontChooser.class);

  private static final Font DEFAULT_FONT
      = new Font("SansSerif", Font.PLAIN, 12);

  private static final Object[] SIZES = {
      "5", "6", "7", "8", "9", "10",
      "11", "12", "14", "16", "18",
      "20", "22", "24", "28", "32"
  };

  private JList list;
  private JCheckBox bold;
  private JCheckBox italic;
  private JComboBox size;

  private JTextField preview;

  private Font font;
  /**
   * JFontChooser constructor comment.
   */
  public JFontChooser() {
    setBorder(BorderFactory.createEtchedBorder());

    setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();

    // font names
    list = new JList(GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames());

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.gridheight = 3;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(4, 4, 4, 4);
    add(new JScrollPane(list), constraints);

    // Font size
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints pconstr = new GridBagConstraints();

    JLabel label = new JLabel(resLocalizedStrings.getString("fontchooser.size"));
    pconstr.gridx = 0;
    pconstr.insets = new Insets(4, 4, 4, 4);
    panel.add(label, pconstr);

    size = new JComboBox(SIZES);
    label = (JLabel) size.getRenderer();
    label.setHorizontalAlignment(JLabel.RIGHT);
    pconstr.gridx = 1;
    pconstr.insets = new Insets(4, 4, 4, 4);
    panel.add(size, pconstr);

    constraints.gridx = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    add(panel, constraints);

    // Bold/Italc
    panel = new JPanel(new GridBagLayout());
    pconstr = new GridBagConstraints();
    pconstr.anchor = GridBagConstraints.WEST;
    pconstr.insets = new Insets(4, 4, 4, 4);

    bold = new JCheckBox(resLocalizedStrings.getString("fontchooser.bold"));
    bold.setFont(new Font("Dialog", Font.BOLD, 12));
    pconstr.gridy = 0;
    panel.add(bold, pconstr);

    italic = new JCheckBox(resLocalizedStrings.getString("fontchooser.italic"));
    italic.setFont(new Font("Dialog", Font.ITALIC, 12));
    pconstr.gridy = 1;
    panel.add(italic, pconstr);

    constraints.gridy = 1;
    add(panel, constraints);

    // Preview
    preview = new JTextField("AaBbCc");
    preview.setEditable(false);
    preview.setBorder(null);

    JPanel pre = new JPanel(new BorderLayout());
    pre.add(preview, BorderLayout.CENTER);

    pre.setBorder(BorderFactory.createTitledBorder(
        resLocalizedStrings.getString("fontchooser.preview")));
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 0.5;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(4, 4, 4, 4);
    add(pre, constraints);

    // initialize selection
    setFont(DEFAULT_FONT);

    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        preview();
      }
    });

    ActionListener l = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        preview();
      }
    };

    size.addActionListener(l);
    bold.addActionListener(l);
    italic.addActionListener(l);
  }

  /**
   * Insert the method's description here.
   *
   */
  public Font getFont() {
    return font;
  }

  /**
   * Insert the method's description here.
   *
   */
  void preview() {
    int style = bold.isSelected() ? Font.BOLD : Font.PLAIN;
    if (italic.isSelected()) {
      style |= Font.ITALIC;

    }
    int s = Integer.parseInt((String) size.getSelectedItem());
    font = new Font((String) list.getSelectedValue(), style, s);
    preview.setFont(font);
  }

  /**
   * Insert the method's description here.
   *
   */
  public void setFont(Font font) {
    this.font = font;

    String name = font.getFontName();
    int s = list.getModel().getSize();
    for (int i = 0; i < s; i++) {
      if (((String) list.getModel().getElementAt(i)).equalsIgnoreCase(name)) {
        list.setSelectedIndex(i);
        break;
      }
    }

    bold.setSelected(font.isBold());
    italic.setSelected(font.isItalic());

    size.setSelectedItem(Integer.toString(font.getSize()));

    preview.setFont(font);
  }
}
