/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractmethod;


import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;


/**
 * Extract Module main dialog.
 * Obtain new method name, modifier and parameters order.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class JExtractDialog {
  final RitDialog dialog;

  boolean isOkPressed;

  JTextField methodName = new JTextField();

  BinTable table = new BinTable();
  ParametersTableModel tableModel = new ParametersTableModel();

  final JButton buttonUp = new JButton("Up");
  final JButton buttonDown = new JButton("Down");
  final JButton buttonOk = new JButton("Ok"); //resLocalizedStrings.getString("button.next") );

  private final JButton buttonCancel = new JButton("Cancel"); //resLocalizedStrings.getString("button.cancel") );

  private JTextArea area = new JTextArea();
  private JComboBox modifiersBox;
  private String returnStr;
  private BinVariable[] parameters;
  private BinTypeRef[] exceptions;
  private int[] modifiers;
  
  private ActionListener cancelListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      isOkPressed = false;
      dialog.dispose();
    }
  };

  private class ParametersTableModel extends AbstractTableModel {
    private java.util.List nodes = new ArrayList();
    private String[] columns = {"Type", "Name", "Init Value"};

    /**
     * Constructor for ParametersTableModel.
     */
    public ParametersTableModel() {
      super();
    }

    public int getRowCount() {
      return nodes.size();
    }

    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      return columns[column];
    }

    public Class getColumnClass(int columnIndex) {
      return String.class;
    }

    public boolean isCellEditable(int rowIndex,
        int columnIndex) {
      return (columnIndex == 1);
    }

    public ParametersTableNode getNode(int row) {
      return (ParametersTableNode) nodes.get(row);
    }

    public Object getValueAt(int row, int column) {
      ParametersTableNode n = (ParametersTableNode) nodes.get(row);
      if (column == 0) {
        return n.getParamTypeName();
      }
      if (column == 1) {
        return n.getParamName();
      }
      return n.getOriginalParamName();
    }

    public void setValueAt(Object value, int row, int column) {
      if (value instanceof String) {
        ParametersTableNode node = getNode(row);
        node.setParamName((String) value);

        signatureMustBeChanged();
      }

      fireTableCellUpdated(row, column);
    }

    public void addNode(ParametersTableNode node) {
      nodes.add(node);
    }

    public void moveUp(int index) {
      Object node = nodes.remove(index);
      nodes.add(index - 1, node);
    }

    public void moveDown(int index) {
      Object node = nodes.remove(index);
      nodes.add(index + 1, node);
    }
  }


  public JExtractDialog(
      IdeWindowContext context, int[] modifiers, int defaultModifier,
      BinVariable[] parameters, BinTypeRef returnType,
      BinTypeRef[] exceptions
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Extract Method Refactoring");

    this.returnStr = (returnType == null) ? ""
        : BinFormatter.formatNotQualified(returnType);
    this.parameters = parameters;
    this.modifiers = modifiers;
    this.exceptions = exceptions;

    init(defaultModifier);
  }

  private void initModifiers(int defaultModifier) {
    int size = modifiers.length;
    String[] items = new String[size];

    int selected = 0;
    for (int i = 0; i < size; i++) {
      if (modifiers[i] == BinModifier.PACKAGE_PRIVATE) {
        items[i] = "package private";
      } else {
        items[i] = new BinModifierFormatter(modifiers[i]).print();
      }
      if (modifiers[i] == defaultModifier) {
        selected = i;
      }
    }

    modifiersBox = new JComboBox(items);
    modifiersBox.setSelectedIndex(selected);
  }

  public void show() {
    this.isOkPressed = false;
    dialog.show();
  }

  private void init(int defaultModifier) {
    dialog.getRootPane().setDefaultButton(buttonOk);

    buttonOk.setEnabled(false);
    buttonUp.setEnabled(false);
    buttonDown.setEnabled(false);

    initModifiers(defaultModifier);
    initParametersList();
    dialog.setContentPane(createMainPanel());

    modifiersBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        signatureMustBeChanged();
      }
    });

    methodName.getDocument().addDocumentListener(new DocumentListener() {

      public void changedUpdate(DocumentEvent e) {
        updateButtonState(e);
      }

      public void insertUpdate(DocumentEvent e) {
        updateButtonState(e);
        signatureMustBeChanged();
      }

      public void removeUpdate(DocumentEvent e) {
        updateButtonState(e);
        signatureMustBeChanged();
      }

      private void updateButtonState(DocumentEvent event) {
        Document document = event.getDocument();

        // Adjust button
        try {
          String newName = document.getText(0, document.getLength());
          buttonOk.setEnabled(newName.length() > 0
              && NameUtil.isValidIdentifier(newName));
        } catch (BadLocationException ble) {
        }
      }
    });

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setModel(tableModel);
//table.setDefaultEditor( ParametersTableNode.class, new ParametersCellEditor() );
//table.setRowHeight( table.getRowHeight() + 2 );
    Dimension maxSize = table.getTableHeader().getMaximumSize();
    maxSize.setSize(maxSize.getWidth(), maxSize.getHeight() - 5);
    table.getTableHeader().setMaximumSize(maxSize);

    table.getSelectionModel().addListSelectionListener(new
        ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        buttonUp.setEnabled(false);
        buttonDown.setEnabled(false);

        int index = table.getSelectedRow();
        int size = table.getRowCount();

        if (index < 0 || index >= size) {
          return;
        }

        if (index > 0) {
          buttonUp.setEnabled(true);
        }

        if (index < size - 1) {
          buttonDown.setEnabled(true);
        }

        table.getSelectionModel().setSelectionInterval(index, index);
      }
    });

    signatureMustBeChanged();

    if (!IDEController.runningNetBeans()) {
      dialog.setSize(640, 480);
    }

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            methodName.requestFocus();
            methodName.setCaretPosition(methodName.getText().length());
          }
        });
      }

      public void windowClosing(WindowEvent windowEvent) {
        dialog.removeWindowListener(this);
        dialog.dispose();
      }
    });

  }

  private void initParametersList() {
    if (parameters == null || parameters.length == 0) {
      return;
    }

    int size = parameters.length;
    for (int i = 0; i < size; i++) {
      ParametersTableNode node = new ParametersTableNode(parameters[i]);
      tableModel.addNode(node);
    }
  }

  void signatureMustBeChanged() {
    StringBuffer buf = new StringBuffer();
    BinModifierFormatter formatter = new BinModifierFormatter(
        modifiers[modifiersBox.getSelectedIndex()]);
    formatter.needsPostfix(true);
    buf.append(formatter.print());
    
    buf.append(returnStr);
    buf.append(" " + getMethodName() + "(");

    int size = tableModel.getRowCount();

    if (size > 1) {
      buf.append("\n");

    }
    for (int i = 0; i < size; i++) {
      ParametersTableNode node = tableModel.getNode(i);
      if (node == null) {
        continue;
      }

      String param = "" + node.getParamTypeName() +
          " " + node.getParamName();

      if (size > 1) {
        buf.append("     ");
      }
      buf.append(param);
      if (i != (size - 1)) {
        buf.append(",\n");
      }
    }

    if (size > 1) {
      buf.append("\n");
    }
    buf.append(")");

    if (exceptions != null && exceptions.length > 0) {
      size = exceptions.length;
      buf.append(" throws ");
      for (int i = 0; i < size; i++) {
        buf.append(exceptions[i].getName());
        if (i != (size - 1)) {
          buf.append(", ");
        }
      }
    }

    buf.append("\n");

    area.setText(buf.toString());
  }

  private JPanel createMainPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout());

    JPanel help = DialogManager.getHelpPanel(
        "Enter new method name and specify the method's visibility"
        );
    help.setBorder(BorderFactory.createEtchedBorder());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 5, 0, 5);
    mainPanel.add(help, constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 5, 0, 5);
    mainPanel.add(createMethodPanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(3, 5, 0, 5);
    mainPanel.add(createParametersPanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weighty = 0.0;
    constraints.weightx = 1.0;
    constraints.insets = new Insets(3, 5, 0, 5);
    mainPanel.add(createSignaturePanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.insets = new Insets(3, 5, 1, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    mainPanel.add(createButtonPanel(), constraints);

    return mainPanel;
  }

  private JPanel createMethodPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Method"));
    ((TitledBorder) panel.getBorder()).setTitleColor(Color.black);

    GridBagConstraints constraints = new GridBagConstraints();
    /*
         constraints.insets = new Insets( 5, 5, 5, 5 );
       constraints.gridx = 2;
       constraints.gridy = 1;
         constraints.fill = GridBagConstraints.BOTH;
         constraints.anchor = GridBagConstraints.SOUTH;
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         constraints.gridwidth = 2;
         JLabel l = new JLabel( "Name:" );
         l.setForeground( Color.black );
         panel.add( l, constraints );
     */
    constraints.insets = new Insets(0, 5, 5, 0);
    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.gridwidth = 1;
    panel.add(modifiersBox, constraints);

    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    panel.add(methodName, constraints);

    return panel;
  }

  private JPanel createParametersPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Parameters"));
    ((TitledBorder) panel.getBorder()).setTitleColor(Color.black);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 2;
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(table.getBackground());
    panel.add(scroll, constraints);

    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    panel.add(buttonUp, constraints);

    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    panel.add(buttonDown, constraints);

    buttonUp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = table.getSelectedRow();
        if (index == 0) {
          return;
        }

        tableModel.moveUp(index);
        table.getSelectionModel().setSelectionInterval(index - 1, index - 1);

        signatureMustBeChanged();
      }
    });

    buttonDown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = table.getSelectedRow();
        int size = table.getRowCount();
        if (index == size - 1) {
          return;
        }

        tableModel.moveDown(index);
        table.getSelectionModel().setSelectionInterval(index + 1, index + 1);

        signatureMustBeChanged();
      }
    });

    panel.setPreferredSize(new Dimension(480, 150));
    //panel.setMinimumSize( new Dimension(400, 120) );
    //panel.setMaximumSize( new Dimension(300, 200) );
    return panel;
  }

  private JPanel createSignaturePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Signature Preview"));
    ((TitledBorder) panel.getBorder()).setTitleColor(Color.black);

    area.setBackground(dialog.getRootPane().getBackground());
    //area.setWrapStyleWord( true );
    //area.setLineWrap( true );
    area.setEditable(false);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    panel.add(area, constraints);

    return panel;
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        table.stopEditing();
        isOkPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel.addActionListener(cancelListener);
    buttonPanel.add(buttonCancel);

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.extract_method");
    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

//    buttonOk.setNextFocusableComponent(buttonCancel);
//    buttonCancel.setNextFocusableComponent(buttonOk);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp, cancelListener);
    
    return downPanel;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public String[] getParamNames() {
    int size = tableModel.getRowCount();
    String[] names = new String[size];

    for (int i = 0; i < size; i++) {
      names[i] = tableModel.getNode(i).getParamName();
    }

    return names;
  }

  public int[] getParamIds() {
    int size = tableModel.getRowCount();
    int[] mapping = new int[size];

    for (int i = 0; i < size; i++) {
      BinVariable var = tableModel.getNode(i).getOriginalParam();
      for (int k = 0; k < this.parameters.length; k++) {
        if (this.parameters[k] == var) {
          mapping[i] = k;
          break;
        }
      }
    }

    return mapping;
  }

  public BinLocalVariable[] getParams() {
    int size = tableModel.getRowCount();
    BinLocalVariable[] params = new BinLocalVariable[size];

    for (int i = 0; i < size; i++) {
      params[i] = (BinLocalVariable) tableModel.getNode(i).getOriginalParam();
    }
    return params;
  }

  public int getModifier() {
    return modifiers[modifiersBox.getSelectedIndex()];
  }

  public String getMethodName() {
    return methodName.getText();
  }
}
