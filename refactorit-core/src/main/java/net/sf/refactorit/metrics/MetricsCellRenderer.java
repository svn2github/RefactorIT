/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;


/**
 * @author Anton Safonov
 */
public final class MetricsCellRenderer extends DefaultTableCellRenderer {
  private NumberFormat formatter = UIResources.createDecimalFormat();

  public MetricsCellRenderer() {
    super();
    setHorizontalAlignment(JLabel.RIGHT);
  }

  public void setValue(Object value) {
    if (value instanceof Double) {
      setText((value == null) ? "" : formatter.format(value));
    } else {
      super.setValue(value);
    }
  }

  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean selected,
      boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, selected, hasFocus, row,
        column);

//    System.err.println("Value: " + value + " - " + (value == null ? "null"
//        : value.getClass().toString()));

    MetricsModel model
        = (MetricsModel) ((BinTreeTable) table).getBinTreeTableModel();

    if (value instanceof Number && isAppliable(table, row, column, model)) {
      double min = model.getState().getMin(model.getKey(column));
      double max = model.getState().getMax(model.getKey(column));

      if (Double.isNaN(min) || Double.isNaN(max)) {
        setBackground(table.getBackground());
      } else {

        // TODO check if int's are converted well to double

        double val = ((Number) value).doubleValue();
        double threshold = max - min;

        if ((min <= val && val <= max) || (threshold <= 0)) {
          setBackground(Color.white);
        } else if ((val < min) && (threshold > 0)) {
          double deviation = ((min - val) * 100) / (threshold);
          setBackground(getViolationColor(deviation));
        } else if ((val > max) && (threshold > 0)) {
          double deviation = ((val - max) * 100) / (threshold);
          setBackground(getViolationColor(deviation));
        }
      }
    } else {
      setBackground(table.getBackground());
    }

    return this;
  }

  private boolean isAppliable(final JTable table,
      final int row, final int column,
      final MetricsModel model) {

    BinTreeTable btt = ((BinTreeTable) table);

    for (int x = 0; x < model.getColumnCount(); x++) {
      if (btt.getValueAt(row, x) instanceof BinTreeTableNode) {

        BinTreeTableNode bttn = (BinTreeTableNode) btt.getValueAt(row, x);
        Object bin = bttn.getBin();

        int type = model.getApplicability(column);
        switch (type) {
          case MetricsModel.NONE:
            return false;

          case MetricsModel.METHOD:
            if (!(bin instanceof BinMethod)) {
              return false;
            }
            break;

          case MetricsModel.CLASS:
            if (!(bin instanceof BinCIType)) {
              return false;
            }
            break;

          case MetricsModel.PACKAGE:
            if (!(bin instanceof BinPackage)) {
              return false;
            }
            break;

        }
        break;
      }
    }
    return true;
  }

  public Color getViolationColor(double deviation) {
    Color color;

    if (deviation <= 25) {
      int gb = 250; //Green and Blue
      gb = gb - (((int) (deviation / 2.5)) * 25);
      color = new Color(255, gb, gb);
    } else {
      color = Color.red;
    }

    return color;
  }
}
