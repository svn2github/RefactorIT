/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.reports.Statistics;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;


public class ProductivityGuideDialog {
  private class CloseButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent evt) {
      dialog.dispose();
    }
  }
  
  private class EscapeKeyListener implements KeyListener{
    public void keyPressed(KeyEvent e){
      if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
        dialog.dispose();
    }
    
    public void keyTyped(KeyEvent e){
      keyPressed(e);
    }
    
    public void keyReleased(KeyEvent e){
      keyPressed(e);
    }
  }
  private RitDialog dialog;

  private HashMap data;

  private JButton btnOk = new JButton("Ok");

  private JEditorPane editorPane;

  // private String[] columnNames;

  public ProductivityGuideDialog(IdeWindowContext context){
    // columnNames = new String[]{"Feature", "Used", "First use", "Last use",
    // "Frequency of use"};
    dialog = RitDialog.create(context);
    dialog.setTitle("Productivity guide");
    dialog.setSize(700, 550);
    init();
  }
  
  public ProductivityGuideDialog(){
    // columnNames = new String[]{"Feature", "Used", "First use", "Last use",
    // "Frequency of use"};
    dialog = RitDialog.create(IDEController.getInstance()
        .createProjectContext());
    dialog.setTitle("Productivity guide");
    dialog.setSize(700, 550);
    init();
  }

  public void show() {
    dialog.show();
    // dialog.setVisible(true);
  }

  private void init() {
    SwingUtil.addEscapeListener(dialog);
    Container container = dialog.getContentPane();
    //JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3), 
        BorderFactory.createEtchedBorder()));
    
    mainPanel.add(DialogManager.getHelpPanel("Productivity guide informs you of actions you have used"), BorderLayout.NORTH);
    
    JTable tableCommon = new StatisticsTable(Statistics.CATEGORY_COMMON);
    JScrollPane scrollPaneCommon = new JScrollPane(tableCommon);
    JTable tableAudits = new StatisticsTable(Statistics.CATEGORY_AUDITS);
    JScrollPane scrollPaneAudits = new JScrollPane(tableAudits);
    JTable tableMetrics = new StatisticsTable(Statistics.CATEGORY_METRICS);
    JScrollPane scrollPaneMetrics = new JScrollPane(tableMetrics);
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Common actions", scrollPaneCommon);
    tabbedPane.add("Audits", scrollPaneAudits);
    tabbedPane.add("Metrics", scrollPaneMetrics);
    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    
    container.add(mainPanel, BorderLayout.CENTER);
    
    JPanel buttonsPanel = new JPanel();
    btnOk.addActionListener(new CloseButtonListener());
    SwingUtil.initCommonDialogKeystrokes(dialog, btnOk);
    buttonsPanel.add(btnOk);
    container.add(buttonsPanel, BorderLayout.SOUTH);
    dialog.setContentPane(container);
  }
}

class StatisticsTable extends JTable {
  private class CommonTableModel extends AbstractTableModel {
    public int getRowCount() {
      return rowData.length;
    }

    public Object getValueAt(int row, int column) {
      return rowData[row][column];
    }

    public int getColumnCount() {
      return colNames.length;
    }

    public String getColumnName(int column) {
      return colNames[column];
    }

    public CommonTableModel(Statistics.StatisticsData[] data) {
      colNames = new String[] { "Feature", "Used", "First use", "Last use",
          "Frequency of use" };
      rowData = new Object[data.length][6];

      for (int i = 0; i < data.length; i++){
        Statistics.StatisticsData d = data[i];
        String name = d.getName();
        UsageDate firstUse = new UsageDate(d.getFirstUse());
        UsageDate lastUse = new UsageDate(d.getLastUse());
        // String lastUse = (d.getLastUse() == null ? "never" :
        // d.getLastUse().toString());
        UsageTimes total = new UsageTimes(d.getTotal());
        UsageFrequency fr = new UsageFrequency(d.getFirstUse(), d.getLastUse(), d.getTotal());
        rowData[i] = new Object[] { name, total, firstUse, lastUse, fr };
      }
      Arrays.sort(rowData, new TableComparator());
      sortedBy = 0;
    }
  }

  private class TableComparator implements Comparator {
    private int column;

    private boolean ascending;

    public int compare(Object o1, Object o2) {
      int result = 0;
      
      Object object1 = ((Object[]) o1)[column];
      Object object2 = ((Object[]) o2)[column];
      
      try{
        if((object1 instanceof String) && (object2 instanceof String))
          result = ((String) object1).compareToIgnoreCase((String) object2);
        else
          result = ((Comparable) object1).compareTo((Comparable) object2);
      }catch(ClassCastException e){
      }

//      if (object1 instanceof Date)
//        if (object2 instanceof String)
//          result = 1;
//        else
//          result = ((Date) object1).compareTo((Date) object2);
//      else if (object1 instanceof UsageTimes)
//        result = ((UsageTimes) object1).compareTo((UsageTimes) object2);
//      else if (object1 instanceof String)
//        if (object2 instanceof Date)
//          result = -1;
//        else
//          result = ((String) object1).compareTo(object2);
//      else if(object1 instanceof UsageFrequency)
//        result = ((UsageFrequency) object1).compareTo((UsageFrequency) object2);
      
      return (ascending ? result : -result);
    }

    public boolean equals(Object obj) {
      return false;
    }

    public TableComparator() {
      this(0, true);
    }

    public TableComparator(int column, boolean ascending) {
      this.column = column;
      this.ascending = ascending;
    }
  }
  
  private class HeaderRenderer implements TableCellRenderer{
    public TableCellRenderer oldRenderer;
    
    public HeaderRenderer(TableCellRenderer oldRenderer){
      this.oldRenderer = oldRenderer;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
        boolean hasFocus, int row, int column){
      Component component = oldRenderer.getTableCellRendererComponent(table, value, isSelected, 
          hasFocus, row, column);
      if(component instanceof JLabel){
      JLabel label = (JLabel) component;
      label.setHorizontalTextPosition(JLabel.LEFT);
      label.setIcon(getIcon(column, sortedAscending));
      }
      return component;
    }
  }
  
  private class Arrow implements Icon{
    private boolean ascending;
    
    public Arrow(boolean ascending){
      this.ascending = ascending;
    }
    
    public void paintIcon(Component component, Graphics g, int x, int y){
      int[] xp, yp;
      
        if(ascending){
          g.drawLine(x, y, x + 3, y + 3);
          g.drawLine(x + 6, y, x + 3, y + 3);
//          xp = new int[]{x, x + 6, x + 3};
//          yp = new int[]{y + 1, y + 1, y + 5};
        }else{
          g.drawLine(x + 3, y, x, y + 3);
          g.drawLine(x + 3, y, x + 6, y + 3);
//          xp = new int[]{x + 3, x + 6, x};
//          yp = new int[]{y, y + 5, y + 5};
        }
//          g.fillPolygon(xp, yp, 3);
    }
    
    public int getIconWidth(){
      return 8;
    }
    
    public int getIconHeight(){
      return 5;
    }
  }

  private class SorterListener implements MouseListener {
    public void mouseExited(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
      int column = columnAtPoint(evt.getPoint());
      if (sortedBy == column && sortedAscending) {
        Arrays.sort(rowData, new TableComparator(column, false));
        sortedAscending = false;
      } else {
        Arrays.sort(rowData, new TableComparator(column, true));
        //component.getGraphics().drawLine(0, 10, 10, 0);
        sortedBy = column;
        sortedAscending = true;
      }
      ((AbstractTableModel) getModel()).fireTableDataChanged();
      header.repaint();
    }
  }

  private Object[][] rowData = { {} };

  private String[] colNames = {};

  private int sortedBy = -1;
  private boolean sortedAscending = true;
  
  private JTableHeader header;
  
  public StatisticsTable(String category) {
    super();    

    Statistics.StatisticsData[] data = Statistics.getInstance()
        .getStatisticsData(category);

    setModel(new CommonTableModel(data));
    header = getTableHeader();

    header.addMouseListener(new SorterListener());
    header.setDefaultRenderer(new HeaderRenderer(header.getDefaultRenderer()));
    header.setReorderingAllowed(false);
    
//    setDefaultRenderer(Object.class, new TableCellRenderer(){
//      public Component getTableCellRendererComponent(
//          JTable table, Object value, boolean isSelected, 
//          boolean hasFocus, int row, int column){
//        JLabel component = new JLabel();
//        component.setText(value.toString());
//        getColumnModel().getColumn(column).setPreferredWidth(
//            Math.max(component.getPreferredSize().width, 
//                getColumnModel().getColumn(column).getPreferredWidth()));
//        return component;
//      }
//    });
    
    initColumnSizes();
  }
  
  private void initColumnSizes(){
    for(int i=0; i<getColumnCount(); i++){
      TableColumn column = getColumnModel().getColumn(i);
      TableCellRenderer headerRenderer = column.getHeaderRenderer();
      
      if(headerRenderer == null)
        headerRenderer = getTableHeader().getDefaultRenderer();

      int width = headerRenderer.getTableCellRendererComponent(this, column.getHeaderValue(), 
          false, false, 0, i).getPreferredSize().width;
      for(int j=0; j<getRowCount(); j++){
        int cellWidth = getCellRenderer(j, i).getTableCellRendererComponent(this, getValueAt(j, i), 
            false, false, j, i).getPreferredSize().width;
        width = Math.max(width, cellWidth);
      }
      
      column.setPreferredWidth(width);
    }
//      TableColumn column = getColumnModel().getColumn(0);
//      for(int j=0; j<rowData.length; j++){
//        Component rowCell = getDefaultRenderer(getModel().getColumnClass(0)).
//            getTableCellRendererComponent(this, rowData[j][0], false, false, j, 0);
//        column.setPreferredWidth(Math.max(column.getPreferredWidth(), 
//            rowCell.getPreferredSize().width));
//    }
  }
  
  private Icon getIcon(int column, boolean ascending){
    if(sortedBy != column)
      return null;
    
    return new Arrow(ascending);
  }
}

class UsageDate implements Comparable{
  private Date date;
  
  public UsageDate(Date date){
    this.date = date;
  }
  
  public String toString(){
    if(date == null)
      return "Never";
    else{
      if(date.getDay() == Calendar.getInstance().getTime().getDay())
        return "Today";
      else if(date.getDay() == Calendar.getInstance().getTime().getDay() - 1)
        return "Yesterday";
      else
        return (new SimpleDateFormat("yyyy.MM.dd")).format(date);//date.toString();
    }
  }
  
  public int compareTo(Object object){
    if(object instanceof UsageDate){
      Date date = ((UsageDate) object).date;
      if(this.date == null){
        if(date == null){
          return 0;
        }else
          return -1;
      }else
        if(date == null)
          return 1;
        else
        return -this.date.compareTo(((UsageDate) object).date);
    }else
      return 0;
  }
}

class UsageFrequency implements Comparable{
  String frequency;
  float interval;
  
  public UsageFrequency(Date d1, Date d2, int times){
    if(d1 == null || d2 == null){
      frequency = "N/A";
      interval = Float.MAX_VALUE;
    }else{
      interval = (d2.getTime() - d1.getTime()) / (1000 * times);
      if(interval != 0){
        if(interval < 60)
          frequency = "Once a minute";
        else if(interval < 3600)
          frequency = "Few times an hour";
        else if(interval < 86400)
          frequency = "Few times a day";
        else if(interval < 604800)
          frequency = "Few times a week";
        else if(interval < 18144000)
          frequency = "Few times a month";
        else
          frequency = "Rare";
      }else{
        frequency = "Rare";
        //HACK:
        //interval = Float.MAX_VALUE;
      }
    }
  }
  
  public int compareTo(Object object){
    if(object instanceof UsageFrequency){
      float interval = ((UsageFrequency) object).interval;
      if(this.interval == interval)
        return 0;
      else if(this.interval < interval)
        return -1;
      else
        return 1;
    }else
      return 0;
      
  }
  
  public String toString(){
    return frequency;
  }
}

class UsageTimes implements Comparable{
  int times;
  
  public UsageTimes(int times){
    this.times = times;
  }
  
  public String toString(){
    if(times == 0)
      return "Never";
    else if(times == 1)
      return "Once";
    else if(times == 2)
      return "Twice";
    else
      return times + " times";
  }
  
  public int compareTo(Object object){
    if(object instanceof UsageTimes){
      int times = ((UsageTimes) object).times;
      if(this.times < times)
        return -1;
      else if(this.times == times)
        return 0;
      else
        return 1;
    }else
      return 0;
      
  }
}
//
// class CommonTableModel extends AbstractTableModel{
// protected Object[][] rowData = new String[][]{};
// protected String[] colNames = new String[]{};
//  
// public void sort(int column){
// Arrays.sort(rowData, new TableComparator(column));
// }
//  
// public int getRowCount(){
// return rowData.length;
// }
//  
// public Object getValueAt(int row, int column){
// return rowData[row][column];
// }
//  
// public int getColumnCount(){
// return colNames.length;
// }
//  
// public String getColumnName(int column){
// return colNames[column];
// }
//  
// public String getKeyAt(int i){
// return (String) rowData[i][rowData[i].length - 1];
// }
//  
// public CommonTableModel(Statistics.StatisticsData[] data){
// colNames = new String[]{"Feature", "Used", "First use", "Last use",
// "Frequency of use"};
// this.rowData = new Object[data.length][6];
//    
// for(int i=0; i<data.length; i++){
// Statistics.StatisticsData d = data[i];
// String name = d.getName();
// Object firstUse = (d.getFirstUse() == null ? (Object) "never" :
// d.getFirstUse());
// Object lastUse = (d.getLastUse() == null ? (Object) "never" :
// d.getLastUse());
// //String lastUse = (d.getLastUse() == null ? "never" :
// d.getLastUse().toString());
// Integer total = new Integer(d.getTotal());
// String frequency = "N/A";
// if(d.getFirstUse() != null && d.getLastUse() != null){
// long interval = d.getLastUse().getTime() - d.getFirstUse().getTime();
// }
// rowData[i] = new Object[]{name, total, firstUse, lastUse, frequency};
// }
// Arrays.sort(rowData, new TableComparator());
// }
// }
// private class TableMouseListener implements MouseListener{
// private JTable table;
//  
// public TableMouseListener(JTable table){
// this.table = table;
// }
//  
// public void mouseExited(MouseEvent evt){
// }
//
// public void mouseEntered(MouseEvent evt){
// }
//
// public void mousePressed(MouseEvent evt){
// }
//
// public void mouseReleased(MouseEvent evt){
// }
//
// public void mouseClicked(MouseEvent evt){
// int n = table.columnAtPoint(evt.getPoint());
// ((CommonTableModel) table.getModel()).sort(n);
// }
// }

//
//
// private JTable createStatisticsTable(String category){
// JTable tableCommon = new JTable(new
// CommonTableModel(Statistics.getInstance().getStatisticsData(category)));
// tableCommon.getTableHeader().addMouseListener(new
// TableMouseListener(tableCommon));
// tableCommon.setPreferredScrollableViewportSize(new Dimension(600, 200));
// tableCommon.getColumnModel().getColumn(0).setPreferredWidth(100);
// tableCommon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// //tableCommon.getSelectionModel().addListSelectionListener(new
// SelectionListener(tableCommon.getModel()));
// return tableCommon;
// }

/*
 * private class AuditsAndMetricsGuideTableModel extends CommonTableModel{
 * public AuditsAndMetricsGuideTableModel(Statistics.StatisticsData[] data){
 * colNames = new String[]{"Feature", "Used", "First use", "Last use",
 * "Frequency of use"}; this.rowData = new Object[data.length][7]; this.keys =
 * new String[data.length]; for(int i=0; i<data.length; i++){
 * Statistics.StatisticsData d = data[i]; String name = d.getName(); Object
 * firstUse = (d.getFirstUse() == null ? (Object) "never" : d.getFirstUse());
 * Object lastUse = (d.getLastUse() == null ? (Object) "never" :
 * d.getLastUse()); //String lastUse = (d.getLastUse() == null ? "never" :
 * d.getLastUse().toString()); Integer total = new Integer(d.getTotal()); String
 * frequency = ""; rowData[i] = new Object[]{name, total, firstUse, lastUse,
 * frequency, d.getKey()}; keys[i] = d.getKey(); } Arrays.sort(rowData, new
 * TableComparator()); } } private class CommonActionsGuideTableModel extends
 * CommonTableModel{ public
 * CommonActionsGuideTableModel(Statistics.StatisticsData[] data){ colNames =
 * new String[]{"Feature", "Used", "First use", "Last use", "Frequency of
 * use"};; this.rowData = new Object[data.length][6]; this.keys = new
 * String[data.length]; for(int i=0; i<data.length; i++){
 * Statistics.StatisticsData d = data[i]; String name = d.getName(); Object
 * firstUse = (d.getFirstUse() == null ? (Object) "never" : d.getFirstUse());
 * Object lastUse = (d.getLastUse() == null ? (Object) "never" :
 * d.getLastUse()); //String lastUse = (d.getLastUse() == null ? "never" :
 * d.getLastUse().toString()); Integer total = new Integer(d.getTotal()); String
 * frequency = ""; rowData[i] = new Object[]{name, total, firstUse, lastUse,
 * frequency, d.getKey()}; //keys[i] = d.getKey(); } Arrays.sort(rowData, new
 * TableComparator()); }}
 */

// class StatisticsTable extends JTable{
// private Object[][] rowData = new String[][]{};
// private String[] colNames = new String[]{};
// CommonTableModel model;
// JTable table = this;
// private int sortedBy = -1;
//  
// private class TableComparator implements Comparator{
// private int column;
// private boolean ascending;
//    
// public int compare(Object o1, Object o2){
// int result = 0;
//      
// if(((Object[]) o1)[column] instanceof Date)
// if(((Object[]) o2)[column] instanceof String)
// result = 1;
// else
// result = ((Date) ((Object[]) o1)[column]).compareTo((Date) ((Object[])
// o2)[column]);
// else if(((Object[]) o1)[column] instanceof Integer)
// result = ((Integer) ((Object[]) o1)[column]).compareTo((Integer) ((Object[])
// o2)[column]);
// else if(((Object[]) o1)[column] instanceof String)
// if(((Object[]) o2)[column] instanceof Date)
// result = -1;
// else
// result = ((String) ((Object[]) o1)[column]).compareTo((String) ((Object[])
// o2)[column]);
//        
// //result = 0;
//      
// return (ascending ? result : -result);
// }
//    
// public boolean equals(Object obj){
// return false;
// }
//    
// public TableComparator(){
// this(0, true);
// }
//    
// public TableComparator(int column, boolean ascending){
// this.column = column;
// this.ascending = ascending;
// }
// }
//
// private class CommonTableModel extends AbstractTableModel{
// public void sort(int column){
// if(sortedBy == column){
// Arrays.sort(rowData, new TableComparator(column, false));
// sortedBy = -1;
// //table.getTableHeader();
// }else{
// Arrays.sort(rowData, new TableComparator(column, true));
// sortedBy = column;
// }
// }
//    
// public int getRowCount(){
// return rowData.length;
// }
//    
// public Object getValueAt(int row, int column){
// return rowData[row][column];
// }
//    
// public int getColumnCount(){
// return colNames.length;
// }
//    
// public String getColumnName(int column){
// return colNames[column];
// }
//    
// public String getKeyAt(int i){
// return (String) rowData[i][rowData[i].length - 1];
// }
//    
// public CommonTableModel(Statistics.StatisticsData[] data){
// colNames = new String[]{"Feature", "Used", "First use", "Last use",
// "Frequency of use"};
// rowData = new Object[data.length][6];
//      
// for(int i=0; i<data.length; i++){
// Statistics.StatisticsData d = data[i];
// String name = d.getName();
// Object firstUse = (d.getFirstUse() == null ? (Object) "never" :
// d.getFirstUse());
// Object lastUse = (d.getLastUse() == null ? (Object) "never" :
// d.getLastUse());
// //String lastUse = (d.getLastUse() == null ? "never" :
// d.getLastUse().toString());
// Integer total = new Integer(d.getTotal());
// String frequency = "";
// rowData[i] = new Object[]{name, total, firstUse, lastUse, frequency};
// }
// Arrays.sort(rowData, new TableComparator());
// }
// }
//  
// private class TableMouseListener implements MouseListener{
// public TableMouseListener(){
// }
//    
// public void mouseExited(MouseEvent evt){
// }
//
// public void mouseEntered(MouseEvent evt){
// }
//
// public void mousePressed(MouseEvent evt){
// }
//
// public void mouseReleased(MouseEvent evt){
// }
//
// public void mouseClicked(MouseEvent evt){
// int n = table.columnAtPoint(evt.getPoint());
// ((CommonTableModel) table.getModel()).sort(n);
// }
// }
//  
// public StatisticsTable(String category){
// Statistics.StatisticsData[] data =
// Statistics.getInstance().getStatisticsData(category);
// model = new CommonTableModel(data);
// setModel(model);
// getTableHeader().addMouseListener(new TableMouseListener());
// setPreferredScrollableViewportSize(new Dimension(600, 200));
// getColumnModel().getColumn(0).setPreferredWidth(100);
// setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
// //getTableHeader().setReorderingAllowed(false);
// //super(model);
// }
// }
