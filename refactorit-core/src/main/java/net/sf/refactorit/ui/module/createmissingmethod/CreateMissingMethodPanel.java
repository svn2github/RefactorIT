/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.createmissingmethod;


import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.createmissing.CreateMethodContext;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.ui.TypeChooser;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.table.BinTable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Panel for setting the details of a new method.
 *
 * @author tanel
 */
public class CreateMissingMethodPanel extends JPanel {
  RefactorItContext context;
  CreateMethodContext node;

  JComboBox modifiersBox = new JComboBox();
  JCheckBox staticBox = new JCheckBox();
  JTextField returnTypeField = new JTextField();

  private JTextField baseClassField = new JTextField();
  private JLabel returnTypeLabel = new JLabel();
  private JTextField methodNameField = new JTextField();
  private JTextArea area = new JTextArea();
  private ParametersTableModel tableModel = new ParametersTableModel();
  private BinTable table = new BinTable();

  private class ParametersTableModel extends AbstractTableModel {
    private String[] columns = {"Type", "Name"};

    /**
     * Constructor for ParametersTableModel.
     */
    public ParametersTableModel() {
      super();
    }

    public int getRowCount() {
      return node.argumentTypes.length;
    }

    public int getColumnCount() {
      return 2;
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

    public Object getValueAt(int row, int column) {
      if (column == 0) {
        return BinFormatter.format(node.argumentTypes[row]);
      } else {
        return node.argumentNames[row];
      }
    }

    public void setValueAt(Object value, int row, int column) {
      if (value instanceof String) {
        node.argumentNames[row] = (String) value;
        signatureMustBeChanged();
      }
    }
  }


  public CreateMissingMethodPanel(RefactorItContext context, CreateMethodContext defaultSelectedNode) {
    this.context = context;
    
    onEntry(defaultSelectedNode);
    
    init();
  }

  private void init() {
    //initModifiers();
    table.optionsChanged();
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setModel(tableModel);

    setLayout(new BorderLayout(5, 5));
    setBorder(BorderFactory.createEmptyBorder());

    add(createTopPanel(), BorderLayout.NORTH);
    add(createParametersPanel(), BorderLayout.CENTER);
    add(createSignaturePanel(), BorderLayout.SOUTH);
  }

  private void initModifiers(int[] modifiers, int defaultModifier) {
    int size = modifiers.length;
    String[] items = new String[size];

    modifiersBox.removeAllItems();
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
      modifiersBox.addItem(items[i]);
    }

    modifiersBox.setSelectedIndex(selected);
  }

  /**
   * @return top panel
   */
  private JPanel createTopPanel() {
    JPanel namesPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 10, 5, 0);

    JLabel baseClassLabel = new JLabel("Base class");
    namesPanel.add(baseClassLabel, constraints);

    baseClassField.setEditable(false);
    baseClassField.setBackground(this.getBackground());

    JPanel classNamePanel = new JPanel(new BorderLayout());
    classNamePanel.add(baseClassField, BorderLayout.CENTER);
    JButton baseClassButton = createModifyButton();
    baseClassButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        baseClassButtonActionPerformed(evt);
      }
    });

    classNamePanel.add(baseClassButton, BorderLayout.EAST);

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 3;
    constraints.gridx = 2;
    constraints.gridy = 1;
    namesPanel.add(classNamePanel, constraints);

    final JLabel visiblityLabel = new JLabel("Visibility");
    constraints.fill = GridBagConstraints.NONE;
    constraints.weightx = 1;
    constraints.gridx = 1;
    constraints.gridy = 2;
    namesPanel.add(visiblityLabel, constraints);

    modifiersBox.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        node.setVisibility(
            BinModifier.toNumber((String) modifiersBox.getSelectedItem()));

        signatureMustBeChanged();
      }
    });

    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridx = 2;
    constraints.gridy = 2;
    namesPanel.add(modifiersBox, constraints);

    final JLabel staticLabel = new JLabel("Static");
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1;
    constraints.gridx = 1;
    constraints.gridy = 3;
    namesPanel.add(staticLabel, constraints);

    staticBox.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        node.setStaticMethod(staticBox.isSelected());
        signatureMustBeChanged();
      }
    });
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridx = 2;
    constraints.gridy = 3;
    namesPanel.add(staticBox, constraints);

    final JLabel methodNameLabel = new JLabel("Method name");
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1;
    constraints.gridx = 1;
    constraints.gridy = 4;
    namesPanel.add(methodNameLabel, constraints);

    methodNameField.setEditable(false);
    methodNameField.setBackground(this.getBackground());
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridx = 2;
    constraints.gridy = 4;
    namesPanel.add(methodNameField, constraints);

    returnTypeLabel.setText("Return type");
    constraints.fill = GridBagConstraints.NONE;
    constraints.gridx = 1;
    constraints.gridy = 5;
    namesPanel.add(returnTypeLabel, constraints);

    returnTypeField.setEditable(true);
    //returnTypeName.setBackground(this.getBackground());
    returnTypeField.getDocument().addDocumentListener(new DocumentListener() {

      public void changedUpdate(DocumentEvent e) {
        update();
      }

      public void insertUpdate(DocumentEvent e) {
        update();
      }

      public void removeUpdate(DocumentEvent e) {
        update();
      }

      private void update() {
        node.setReturnType(returnTypeField.getText());
        signatureMustBeChanged();
        updateReturnTypeState();
      }
    });

    JPanel returnTypePanel = new JPanel(new BorderLayout());
    returnTypePanel.add(returnTypeField, BorderLayout.CENTER);
    JButton returnTypeButton = createModifyButton();
    returnTypeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        TypeChooser tc = new TypeChooser(context, "", false, "");
        tc.show();
        BinTypeRef typeRef = tc.getTypeRef();
        if (typeRef != null) {
          returnTypeField.setText(typeRef.getQualifiedName());
        }
      }
    });

    returnTypePanel.add(returnTypeButton, BorderLayout.EAST);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridx = 2;
    constraints.gridy = 5;
    namesPanel.add(returnTypePanel, constraints);

    return namesPanel;
  }

  /**
   * @return a modify button
   */
  private JButton createModifyButton() {
    JButton button = new JButton("..");
    return button;
  }

  private void initParametersList() {
    tableModel.fireTableDataChanged();
    /*
         tableModel.removeAllNodes();
     if (node.argumentTypes == null || node.argumentTypes.length == 0) return;
         int size = node.argumentTypes.length;
         for (int i = 0; i < size; i++) {
      ParametersTableNode paramNode = new ParametersTableNode(node.argumentTypes[i], node.argumentNames[i]);
      tableModel.addNode(paramNode);
         }
     **/
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

    panel.setPreferredSize(new Dimension(480, 150));
    return panel;
  }

  private JPanel createSignaturePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Signature Preview"));
    ((TitledBorder) panel.getBorder()).setTitleColor(Color.black);

    area.setBackground(getBackground());
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
    JScrollPane scrollPane = new JScrollPane(area,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(480, 70));
    panel.add(scrollPane, constraints);

    return panel;
  }

  void baseClassButtonActionPerformed(ActionEvent evt) {
    BaseClassDialog dialog = new BaseClassDialog(context, node.initialBaseClass);
    dialog.show();
    if (dialog.isOkPressed()) {
      BinTypeRef ref = dialog.getSelectedTypeRef();
      if (ref != null) {
        System.out.println("got: " + ref);
        node.setBaseClass(ref);
        baseClassField.setText(ref.getQualifiedName());
      }
    }
  }

  void updateReturnTypeState() {
    setReturnTypeWarning(!node.checkReturnType());
  }

  private void setReturnTypeWarning(boolean warning) {
    if (warning) {
      returnTypeLabel.setForeground(Color.red);
    } else {
      returnTypeLabel.setForeground(returnTypeLabel.getParent().getForeground());
    }
  }

  void signatureMustBeChanged() {
    StringBuffer buf = new StringBuffer();
    buf.append(new BinModifierFormatter(node.getVisibility(), false, false,
        true).print());
    if (node.isStaticMethod()) {
      buf.append("static ");
    }
    buf.append(node.getReturnType());
    buf.append(" " + node.getMethodName() + "(");

    int size = node.argumentTypes.length;

    if (size > 1) {
      buf.append("\n");

    }
    for (int i = 0; i < size; i++) {
      //ParametersTableNode node = tableModel.getNode(i);
      //if (node == null) continue;

      String param
          = BinFormatter.formatNotQualified(node.argumentTypes[i]) + " "
          + node.argumentNames[i];

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

    area.setText(buf.toString());
  }

  public void onEntry(CreateMethodContext node) {
    this.node = node;
    initModifiers(node.allowedVisibilities, node.getVisibility());
    initParametersList();
    baseClassField.setText(node.getBaseClass().getQualifiedName());
    staticBox.setSelected(node.isStaticMethod());
    methodNameField.setText(node.getMethodName());
    if (node.getReturnType() != null) {
      returnTypeField.setText(node.getReturnType());
    }
    signatureMustBeChanged();
  }

  public void onExit(CreateMethodContext node) {
    table.stopEditing();
    /*
         for (int i= 0; i < tableModel.getRowCount(); i++) {
      node.argumentNames[i] = (String)tableModel.getValueAt(i, 1);
         }
     */
    node.setBaseClass(this.node.getBaseClass());
    node.setReturnType(returnTypeField.getText());
  }
  
  public boolean requestFocusForReturnTypeField() {
    return returnTypeField.requestFocusInWindow();
  }

  public static void main(String[] args) {
//    Project project = Utils.createTestRbProject("");
  }
}
