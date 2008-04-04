/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractsuper;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.ui.tree.NodeIcons;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Color;
import java.awt.Component;


/**
 * Renderers cells in extract super table.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class ExtractSTableRenderer extends DefaultTableCellRenderer {
  private static final JCheckBox checkBox = new JCheckBox();

  public Component getTableCellRendererComponent(JTable table,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column) {
    Component component;

    if (value instanceof ExtractableMemberNode) {
      final ExtractableMemberNode node = (ExtractableMemberNode) value;
      setIcon(NodeIcons.getBinIcon(node.getType(), node.getBin(), true));
      setText(node.getDisplayName());

      component = this;
    } else {
      component = checkBox;
      checkBox.setSelected(((Boolean) value).booleanValue());
      checkBox.setHorizontalAlignment(JLabel.CENTER);
    }

    component.setEnabled(true);
    component.setBackground(table.getBackground());

    if (column != 0
        && column != 1 // this one for usability
        ) {
      final ExtractableMemberNode node
          = ((ExtractableMemberNode) table.getModel().getValueAt(row, 1));
      if ( /*!node.isSelected()
                  || */(column == 2 && (node.getBin() instanceof BinField
          || ((BinMember) node.getBin()).isStatic()))
          || (column == 2 && !node.isConvertPrivates()
          && ((BinMember) node.getBin()).isPrivate())
          || (column == 2 && node.isForcedAbstract())) {
        component.setEnabled(false);
        component.setBackground(new Color(240, 240, 240));
      }
    }

    return component;
  }
}
