/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.common.util.StringUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Oleg Tsernetsov
 */
public final class MultiFieldRenamePanel extends JPanel {

  private boolean isNamesOk = false;
  final MultiFieldRenameTable renameTable;
  private final JTextArea editor = new JTextArea();

  public MultiFieldRenamePanel(List fields) {
    super(new BorderLayout());
    editor.setEditable(false);
    
    renameTable = new MultiFieldRenameTable(fields);
    
    Box box = new Box(BoxLayout.Y_AXIS);

    box.add(createNamesPanel());
    add(box, BorderLayout.NORTH);
    add(createStatusPanel());
    updateStatus();
  }

  private JComponent createNamesPanel() {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel
        .setBorder(BorderFactory
            .createTitledBorder("Parameters (double-click on \"New name\" column to edit)"));

    JScrollPane sp = new JScrollPane(renameTable);
    sp.setPreferredSize(new Dimension(10, 180));
    panel.add(sp);

    JPanel buttons = new JPanel();
    GridLayout buttonLayout = new GridLayout(6, 1);
    buttonLayout.setVgap(3);
    buttonLayout.setHgap(3);
    buttons.setLayout(buttonLayout);

    JButton button;

    button = new JButton("Uppercase");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        renameTable.changeSelectedToDefault();
        updateStatus();
      }
    });
    buttons.add(button);

    button = new JButton("Old name");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        renameTable.changeSelectedToUppercase();
        updateStatus();
      }
    });
    buttons.add(button);
    
    button = new JButton("Invert Selection");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        renameTable.invertSelection();
      }
    });
    buttons.add(button);
    
    panel.add(buttons, BorderLayout.EAST);
    return panel;
  }

  private JComponent createStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Status"));

    panel.add(new JScrollPane(editor));

    return panel;
  }

  public void setStatusText(String text) {
    editor.setText(text);
  }

  public void appendStatusText(String text) {
    editor.append(text);
  }

  public boolean isNewNamesOk() {
    return isNamesOk;
  }
  
  private void updateStatus() {
    isNamesOk = true;
    editor.setText("");
    List errors = renameTable.getNamingErrorList();
    if(!errors.isEmpty()) {
      for(int i = 0, max = errors.size(); i < max; i++) {
        appendStatusText((String)errors.get(i));
      }
      isNamesOk = false;
    }
  }
  
  public Map getFieldNames() {
    return renameTable.getFieldNames();
  }
}

class MultiFieldRenameTable extends JTable {
  
  private static final int QUALIFIED_OWNER = 0;
  private static final int OLD_NAME = 1;
  private static final int NEW_NAME = 2;
  
  private Object[][] fields;
  
  private int orderedBy = -1;
  private boolean asc = false;
  
  private class TableSortListener implements MouseListener {
    public void mousePressed(MouseEvent evt) {}
    public void mouseReleased(MouseEvent evt) {}
    public void mouseExited(MouseEvent evt) {}
    public void mouseEntered(MouseEvent evt) {}
    
    public void mouseClicked(MouseEvent evt) {
      int orderBy = columnAtPoint(evt.getPoint());
      sortByColumn(orderBy);
    }
  }
  
  private final class TableArraySorter implements Comparator {
    private int col;
    public TableArraySorter(int col, boolean asc) {
      this.col = col;
    }
    
    public int compare(Object a, Object b) {
      int result = -1;
      try {
        int idx = col/2;
        Object o1 = ((Object[])a)[idx];
        Object o2 = ((Object[])b)[idx];
        switch(col) {
          case 0: {
            String name1 = ((BinField)o1).getOwner().getQualifiedName(); 
            String name2 = ((BinField)o2).getOwner().getQualifiedName();
            result = name1.compareTo(name2);
          }
          break;
          
          case 1: {
            String name1 = ((BinField)o1).getName(); 
            String name2 = ((BinField)o2).getName();
            result = name1.compareTo(name2);
          }
          break;
            
          case 2: {
            result = o1.toString().compareTo(o2.toString());
          }
          break;
        }
      } catch(Exception e) {}
      
      return (!asc)? result: -result;
    }
  }
  
  private final class NamesTableModel extends AbstractTableModel {
    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0:
        return "Owner";
      case 1:
        return "Old name";
      case 2:
        return "New name";
      }
      throw new IndexOutOfBoundsException("column: " + column);
    }

    public int getRowCount() {
      return fields.length;
    }

    public Object getValueAt(int row, int column) {

      switch (column) {
      case 0:
        return ((BinField) fields[row][0]).getOwner().getQualifiedName();
      case 1:
        return ((BinField) fields[row][0]).getName();
      case 2:
        return (String) fields[row][1];
      }
      throw new IndexOutOfBoundsException("row: " + row + "   column: " + column);
    }

    public void setValueAt(Object value, int row, int column) {
      switch (column) {
      case 0:
        break;

      case 1:
        break;
        
      case 2:
        fields[row][1] = value.toString();
        break;
      }
    }

    public boolean isCellEditable(int row, int column) {
      switch (column) {
      case 2:
        return true;
      }
      return false;
    }
  }
  
  private final class SortHeaderRenderer implements TableCellRenderer {
    TableCellRenderer old;
    public SortHeaderRenderer(TableCellRenderer old) {
      this.old = old;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, 
        int row, int column) {
      Component c = old.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if(c instanceof JLabel) {
        ((JLabel)c).setIcon(new Arrow(column));
      }
      return c;
    }
  }
  
  private final class Arrow implements Icon {
    private int column;
    
    public Arrow(int column) {
      this.column = column;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if(column == orderedBy) {
        g.setColor(Color.GRAY);
        if (!asc) {
          g.drawLine(x, y, x + 3, y + 7);
          g.drawLine(x + 3, y + 7, x + 6, y);
          g.drawLine(x, y, x + 7, y);
        } else {
          g.drawLine(x, y + 7, x + 3, y);
          g.drawLine(x + 3, y, x + 6, y + 7);
          g.drawLine(x, y + 7, x + 6, y + 7);
        }
      }
    }

    public int getIconWidth() {
      return 6;
    }

    public int getIconHeight() {
      return 8;
    }
  }
  
  public MultiFieldRenameTable(final List fields) {
    this.fields = generateFieldArray(fields);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setModel(new NamesTableModel());
    
    
    JTableHeader header = getTableHeader();
    header.setReorderingAllowed(false);
    header.addMouseListener(new TableSortListener());
    header.setDefaultRenderer(new SortHeaderRenderer(header.getDefaultRenderer()));
    sortByColumn(OLD_NAME);
    initColumnSizes();
  }
  
  private void initColumnSizes() {
    for(int i=0; i<getColumnCount(); i++){
      int maxj = getRowCount();
      if(maxj > 0) {
        int maxWidth = Integer.MIN_VALUE;
        for(int j = 0; j < maxj; j++) {
          TableCellRenderer renderer = getCellRenderer(j, i);
          Component c = renderer.getTableCellRendererComponent(
              this, getValueAt(j, i), false, false, j, i);
          maxWidth = Math.max(maxWidth, c.getPreferredSize().width);
        }
        getColumnModel().getColumn(i).setPreferredWidth(maxWidth);
      }
    }
  }
  
  private Object[][] generateFieldArray(List fieldList) {
    int max = fieldList.size();
    Object result[][] = new Object[max][2];
    for(int i = 0; i < max; i++) {
      BinField fld = (BinField) fieldList.get(i);
      result[i][0] = fld;
      result[i][1] = StringUtil.getUpercaseStyleName(fld.getName());
    }
    return result;
  }
  
  private void sortByColumn(int orderBy) {
    if(orderBy == orderedBy) {
      asc = !asc;
    }
    orderedBy = orderBy;
    Arrays.sort(fields, new TableArraySorter(orderedBy, asc));
    ((AbstractTableModel)getModel()).fireTableDataChanged();
    getTableHeader().repaint();
  }
  
  public void changeSelectedToUppercase() {
    ListSelectionModel selection = getSelectionModel();

    int min = selection.getMinSelectionIndex();
    int max = selection.getMaxSelectionIndex();

    int length = getRowCount();
    if (min < 0 || max >= length) {
      return;
    }

    while (min <= max) {
      if(selection.isSelectedIndex(min)) {
        fields[min][1] = ((BinField)fields[min][0]).getName();
      }
      min++;
    }
    ((AbstractTableModel)getModel()).fireTableRowsUpdated(selection
        .getMinSelectionIndex(), max);
  }
  
  public void changeSelectedToDefault() {
    ListSelectionModel selection = getSelectionModel();

    int min = selection.getMinSelectionIndex();
    int max = selection.getMaxSelectionIndex();

    int length = getRowCount();
    if (min < 0 || max >= length) {
      return;
    }

    while (min <= max) {
      if(selection.isSelectedIndex(min)) {
        fields[min][1] = StringUtil
        .getUpercaseStyleName(((BinField)fields[min][0]).getName());
      }
      min++;
    }
    ((AbstractTableModel)getModel()).fireTableRowsUpdated(selection
        .getMinSelectionIndex(), max);
  }
  
  public void invertSelection() {
    ListSelectionModel model = getSelectionModel();
    for(int i=0, max = getRowCount(); i < max; i++) {
      if(model.isSelectedIndex(i)) {
        model.removeSelectionInterval(i,i);
      } else {
        model.addSelectionInterval(i,i);
      }
    }
  }
  
  public List getNamingErrorList() {
    List errors = new ArrayList();
    for (int k = 0; k < fields.length; k++) {
      String kName = (String) fields[k][1];
      for (int j = k + 1; j < fields.length; j++) {
        if (k != j) {
          String jName = (String) fields[j][1];
          BinField kField = (BinField) fields[k][0];
          BinField jField = (BinField) fields[j][0];

          if (jName.equals(kName)
              && kField.getOwner().equals(jField.getOwner())) {
            errors.add("Error: New names for "
                + jField.getNameWithAllOwners() + " and "
                + kField.getNameWithAllOwners() + " are equal\n");
          }
        }
      }
    }
    return errors;
  }
  
  public Map getFieldNames() {
    Map result = new HashMap();
    for(int i = 0; i < fields.length; i++) {
      result.put(fields[i][0], fields[i][1]);
    }
    return result;
  }

}
