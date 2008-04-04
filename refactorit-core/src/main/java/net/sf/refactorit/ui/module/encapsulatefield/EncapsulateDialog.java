/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.encapsulatefield;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateField;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateFields;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;



/**
 * Retrieves getter and setter name and accessor.
 *
 * @author Vladislav Vislogubov
 * @author Kirill Buhhalko
 */
public class EncapsulateDialog {
  public static final int FIELD_PRIVATE = 0;
  public static final int FIELD_PACKAGE = 1;
  public static final int FIELD_PUBLIC = 2;
  public static final int FIELD_PROTECTED = 3;
  public static final int FIELD_AS_IS = 4;

  final RitDialog dialog;

  boolean isOkPressed;

  JCheckBox getterCheckBox;
  JCheckBox setterCheckBox;

  private JRadioButton pubRadio = new JRadioButton("public");
  private JRadioButton protRadio = new JRadioButton("protected");
  private JRadioButton packRadio = new JRadioButton("package private");
  private JRadioButton privRadio = new JRadioButton("private", true);
  private JRadioButton asisRadio = new JRadioButton("leave as is");
  private JButton buttonOk = new JButton("Ok");

  EncapsulateField[] encapsulatorA;

  EncapsulateTableModel model;
  private BinTable table;
  
  private ActionListener cancelActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      isOkPressed = false;
      dialog.dispose();
    }
  };

  public EncapsulateDialog(
      IdeWindowContext context, EncapsulateFields encf
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Self Encapsulate of ");
    dialog.setSize(600,440);

    this.encapsulatorA = encf.getEncapsulateFields();
    model = new EncapsulateTableModel(encapsulatorA);
    table = new BinTable(model);
    TableColumnModel tcm = table.getColumnModel();
    tcm.getColumn(0).setMaxWidth(20);
    tcm.getColumn(0).setResizable(false);

    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

      Color brown = new Color(120,88,9);//(139, 105, 20);
      Color gray = new Color(229, 229, 229);
      EncapsulateTableModel model;

      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
          ) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
            row, column);


        model =(EncapsulateTableModel) table.getModel();
        this.setBackground(Color.white);
        this.setForeground(Color.black);

        if (value instanceof String) {

          if (model.getIsFinal(row) && column == 2 ) {
          this.setBackground(gray);
          //this.setForeground(Color.gray);
          }

          if (!model.getIsEnabled(row) && (column > 0) ) {
            this.setBackground(gray);
          }
          if (column==2 && !model.setterEnabled ) {
            this.setBackground(gray);
          }

          if (column == 3 && !model.getterEnabled) {
            this.setBackground(gray);
          }

          if (column == 2
              && EncapsulateDialog.this.
              isSuchMethod(model.getSetterName(row))) {
            this.setForeground(Color.gray);
          }
          if (column == 3
              && EncapsulateDialog.this.
              isSuchMethod(model.getGetterName(row))) {
            this.setForeground(Color.gray);
          }
        }

        return this;
      }
    });



    table.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        dialog.getRootPane().repaint();
      }

      public void mousePressed(MouseEvent e){};
      public void mouseReleased(MouseEvent e){};
      public void mouseEntered(MouseEvent e){};
      public void mouseExited(MouseEvent e){};

    });

    getterCheckBox = new JCheckBox("Getter");
    getterCheckBox.setSelected(model.getterEnabled);

    setterCheckBox = new JCheckBox("Setter");
    setterCheckBox.setSelected(model.setterEnabled);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    getterCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.getterEnabled =
            EncapsulateDialog.this.getterCheckBox.isSelected();
        dialog.getRootPane().repaint();
      }
    });

    setterCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.setterEnabled =
            EncapsulateDialog.this.setterCheckBox.isSelected();
        dialog.getRootPane().repaint();
      }
    });
    
    SwingUtil.invokeLater(new Runnable() {
      public void run() {
        buttonOk.requestFocus();
      }
    } );
  }

  public void show() {
    this.isOkPressed = false;
    dialog.show();
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "Create getting and setting methods for the " +
        "fields and use only those to access the fields");
  }

  private JComponent createMainPanel() {
    JPanel center = new JPanel(new BorderLayout());

    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder()));

    center.add(createMessagePanel(), BorderLayout.NORTH);
    center.add(new JScrollPane(table), BorderLayout.CENTER);
    center.add(getCenterPanel(), BorderLayout.SOUTH);

    return center;
  }

  private JComponent createButtonsPanel() {
    JButton buttonCancel = new JButton("Cancel");
    JButton buttonHelp = new JButton("Help");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {

      public EncapsulateTableModel model() {
        return EncapsulateDialog.this.model;
      }

      public String fieldType(int row) {
        return EncapsulateDialog.this.encapsulatorA[row].
            getField().getTypeRef().getQualifiedName();
      }

      public boolean isEqualSetterNames() {
        for (int x = 0, size = model().getRowCount(); x < size; x++) {
          String prName = model().getSetterName(x);
          if (prName.length() > 0) {
	          for (int y = x + 1; y < size; y++) {
	            if (prName.equals(model().getSetterName(y))) {
	              if (fieldType(x).equals(fieldType(y))) {
	                if (model().getterEnabled && model().getIsEnabled(x)
	                    && model().getIsEnabled(y))
	                  return true;
	              }
	            }
	          }
          }
        }

        return false;
      }

      public boolean isEqualGetterNames() {
        for (int x = 0, size = model().getRowCount(); x < size; x++) {
          String prName= model().getGetterName(x);
          if (prName.length() > 0) {
	          for (int y = x + 1; y < size; y++) {
	            if (prName.equals(model().getGetterName(y))) {
	              if (model().getterEnabled && model().getIsEnabled(x)
	                  && model().getIsEnabled(y))
	                return true;
	            }
	          }
          }
        }

        return false;
      }

      public void actionPerformed(ActionEvent e) {
        EncapsulateDialog.this.stopEditing();

        if (EncapsulateDialog.this.getterCheckBox.isEnabled()
           && isEqualSetterNames()) {

          RitDialog.showMessageDialog(dialog.getContext(),
              "There are fields with the same type, which " +
              "have the same Setter names", "alert",
              JOptionPane.ERROR_MESSAGE);
        } else {
          if (EncapsulateDialog.this.setterCheckBox.isEnabled()
              && isEqualGetterNames()) {

            RitDialog.showMessageDialog(dialog.getContext(),
                "There are fields, which have the same Getter names", "alert",
                JOptionPane.ERROR_MESSAGE);
          } else {
            isOkPressed = true;
            dialog.dispose();
          }
        }
      }
    });

    buttonPanel.add(buttonOk);
    buttonCancel.addActionListener(cancelActionListener);
    buttonPanel.add(buttonCancel);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.encapsulate_field");
    buttonPanel.add(buttonHelp);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp, 
        cancelActionListener);

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
    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);
    return downPanel;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  private JPanel getCenterPanel() {
    JPanel center = new JPanel(new BorderLayout());

    JPanel checkbox = new JPanel(new GridLayout(4, 1, 0, 0));
    checkbox.setBorder(BorderFactory.createTitledBorder("Add"));

    // setter
    checkbox.add(setterCheckBox);
    //getter
    checkbox.add(getterCheckBox);

    center.add(checkbox,BorderLayout.WEST );

    //RadioButtons
    ButtonGroup group = new ButtonGroup();
    group.add(pubRadio);
    group.add(protRadio);
    group.add(packRadio);
    group.add(privRadio);
    group.add(asisRadio);

    asisRadio.setSelected(true);

    JPanel radio = new JPanel(new GridLayout(4, 2, 0, 0));
    radio.setBorder(BorderFactory.createTitledBorder("Field Modifier"));
    JRadioButton hd = new JRadioButton("");
    JRadioButton hd2 = new JRadioButton("");
    hd.setVisible(false);
    hd2.setVisible(false);
    radio.add(privRadio);
    radio.add(asisRadio);
    radio.add(pubRadio);
    radio.add(hd);
    radio.add(packRadio);
    radio.add(hd2);
    radio.add(protRadio);

    center.add(radio,BorderLayout.CENTER );

    return center;
  }

  public String getGetterName(int row) {
    return model.getGetterName(row);
  }

  public String getSetterName(int row) {
    return model.getSetterName(row);
  }

  /*
   * @return default modifier
   */
  public int getGetterAccessor() {
    return BinModifier.PUBLIC;
  }

  /*
   * @return default modifier
   */
  public int getSetterAccessor() {
    return BinModifier.PUBLIC;
  }

  public int getFieldAccessor() {
    if (privRadio.isSelected()) {
      return FIELD_PRIVATE;
    }

    if (packRadio.isSelected()) {
      return FIELD_PACKAGE;
    }

    if (pubRadio.isSelected()) {
      return FIELD_PUBLIC;
    }

    if (protRadio.isSelected()) {
      return FIELD_PROTECTED;
    }

    return FIELD_AS_IS;
  }

  public boolean isGetterEnabled(int row) {
    if (getterCheckBox.isSelected()) {
      return model.getIsEnabled(row);
    }

    return false;
  }

  public boolean isSetterEnabled(int row) {
    if (setterCheckBox.isSelected()) {
      return model.getIsEnabled(row) && !model.getIsFinal(row);
    }

    return false;
  }

  public void stopEditing() {
    this.table.stopEditing();
  }

  public boolean isSuchMethod(String s) {
    EncapsulateField[] field = this.encapsulatorA;
    BinMethod[] methods = field[0].getField().getOwner().getBinCIType().getDeclaredMethods();
    int size = methods.length;

    String[] methodNames = new String[size];

    for (int x=0; x < size; x++) {
      methodNames[x] = methods[x].getName();
    }

    for (int x=0; x < size; x++) {
      if (methodNames[x].equals(s)) {
        return true;
      }
    }

    return false;
  }

  public void dispose() {
    dialog.dispose();
  }
}
