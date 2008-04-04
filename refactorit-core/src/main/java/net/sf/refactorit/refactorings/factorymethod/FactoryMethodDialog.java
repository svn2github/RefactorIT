/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.factorymethod;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.TypeChooser;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;


public class FactoryMethodDialog {
  final RitDialog dialog;
  final RefactorItContext context;

  boolean isOkPressed;

  JTextField methodField = new JTextField();

  JButton buttonOk = new JButton("Ok");
  JButton buttonCancel = new JButton("Cancel");

  private JButton buttonHelp = new JButton("Help");

  private JTextField hostField = new JTextField("<choose>", 30);
  private JButton choose = new JButton("Select class");

  private JCheckBox statement = new JCheckBox("Create import statements", false);

  private JCheckBox optimizeVisibilityCheckBox =
      new JCheckBox("Optimize constructor visibility", true);

  private BinClass hostingClass;
  private List invocations;
  private BinConstructor target;

  public FactoryMethodDialog(
      RefactorItContext context, BinConstructor target, List invocations
  ) {
    this.target = target;
    this.invocations = invocations;
    this.context = context;

    hostingClass = (BinClass) target.getOwner().getBinCIType();
    methodField.setText("create" + target.getOwner().getName());

    if (!hostingClass.isFromCompilationUnit()) {
      hostingClass = null;
    } else {
      hostField.setText(hostingClass.getQualifiedName());
    }

    JPanel contentPane = new JPanel();
    contentPane.setPreferredSize(new Dimension(500, 250));

    contentPane.setLayout(new BorderLayout());
    contentPane.add(createCenterPanel(), BorderLayout.CENTER);
    contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);

    hostField.setEditable(false);

    dialog = RitDialog.create(context);
    dialog.setTitle("Factory");
    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.factory_method");

    methodField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
      }

      public void insertUpdate(DocumentEvent e) {
        if (methodField.getText().length() > 0) {
          buttonOk.setEnabled(true);
        } else {
          buttonOk.setEnabled(false);
        }
      }

      public void removeUpdate(DocumentEvent e) {
        if (methodField.getText().length() > 0) {
          buttonOk.setEnabled(true);
        } else {
          buttonOk.setEnabled(false);
        }
      }
    });

    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doChoose();
      }
    });

    if (methodField.getText().length() == 0 || hostingClass == null) {
      buttonOk.setEnabled(false);
    }
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp);
  }

  public void show() {
    dialog.show();
  }

  void doChoose() {
    BinTypeRef typeRef = null;
    if (hostingClass != null) {
      typeRef = hostingClass.getTypeRef();
    }
    TypeChooser tc = new TypeChooser(context, false, "refact.factory_method",
        typeRef, true);
    tc.show();
    BinTypeRef type = tc.getTypeRef();

    if (type != null && type.isReferenceType()
        && type.getBinCIType().isClass()) {
      String text = validateHostingClass((BinClass) type.getBinType());
      if (text == null) {
        hostingClass = (BinClass) type.getBinType();

        hostField.setText(hostingClass.getName());
        buttonOk.setEnabled(true);
        return;
      } else {
        RitDialog.showMessageDialog(context, text);
      }
    }
    //hostField.setText("<choose>");
    //buttonOk.setEnabled(false);

  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "Select the class and name for the factory method."
        );
  }

  private JComponent createCenterPanel() {
    JPanel center = new JPanel(new GridBagLayout());
    //center.setBorder(BorderFactory.createTitledBorder("Factory Method Entry"));
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    center.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    JPanel typeArea = new JPanel(new BorderLayout());
    typeArea.add(hostField, BorderLayout.CENTER);
    typeArea.add(choose, BorderLayout.EAST);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.gridwidth = 2;
    center.add(createMessagePanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 2;
    //temporarily removed this checkbox
    //center.add( statement, constraints );
    center.add(new JLabel(""), constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    JLabel l1 = new JLabel("Hosting Class: ");
    l1.setForeground(Color.black);
    center.add(l1, constraints);

    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    constraints.insets = new Insets(5, 5, 5, 5);
    center.add(typeArea, constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    JLabel l2 = new JLabel("Method Name: ");
    l2.setForeground(Color.black);
    center.add(l2, constraints);

    constraints.gridx = 2;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 2;
    center.add(methodField, constraints);

    constraints.gridx = 2;
    constraints.gridy = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 2;
    center.add(optimizeVisibilityCheckBox, constraints);

    return center;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = validateMethodName(methodField.getText());
        if (text == null) {
          isOkPressed = true;
          dialog.dispose();
          return;
        }

        RitDialog.showMessageDialog(context, text);
      }
    });
    buttonPanel.add(buttonOk);

    choose.setMnemonic(KeyEvent.VK_S);

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = false;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonCancel);

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
    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);
    return downPanel;
  }

  public String getMethodName() {
    return (isOkPressed) ? methodField.getText() : null;
  }

  public BinClass getHostingClass() {
    return (isOkPressed) ? hostingClass : null;
  }

  /**
   * returns null if no conflicts and conflict String if has conflicts
   */
  String validateMethodName(String testMethodName) {
    if (hostingClass == null) {
      return "No hosting class";
    }
    if (testMethodName == null) {
      return "No method name";
    }
    if (!NameUtil.isValidIdentifier(testMethodName)) {
      return "Method name is not valid identifier";
    }

    if (hostingClass.getDeclaredMethod(testMethodName, target.getParameters()) != null) {
      return "Method already exists";
    }

    return null;
  }

  private String validateHostingClass(BinClass testHostingClass) {
    if (testHostingClass == null) {
      return "No hosting class";
    }
    if (!testHostingClass.isFromCompilationUnit()) {
      return "Hosting class not editable";
    }
    if (!target.isAccessible(target.getOwner().getBinCIType(), testHostingClass)) {
      return "Constructor " + target.getName()
          + " is not accessible from class " + testHostingClass.getName();
    }

    BinMethod.Throws[] exceptions = target.getThrows();
    for (int i = 0; i < exceptions.length; ++i) {
      BinTypeRef exRef = exceptions[i].getException();
      if (!exRef.getBinCIType().isAccessible(testHostingClass)) {
        return "Constuctor throws exception - "
            + exRef.getQualifiedName()
            + " - that is not accessible from "
            + testHostingClass.getName();
      }
    }

    BinParameter[] params = target.getParameters();
    for (int i = 0; i < params.length; ++i) {
      BinTypeRef typeRef = params[i].getTypeRef();
      while (typeRef.isArray()) {
        typeRef = ((BinArrayType) typeRef.getBinType()).getArrayType();
      }
      if (typeRef.isPrimitiveType()) {
        continue;
      }
      if (!typeRef.getBinCIType().isAccessible(
          testHostingClass)) {
        return "Constructor parameter - " +
            typeRef.getQualifiedName() +
            " - is not accessible from "
            + testHostingClass.getName();

      }
    }

    // this one for speed
    if (testHostingClass.isPublic()) {
      return null;
    }
    for (int i = 0; i < invocations.size(); ++i) {
      InvocationData id = (InvocationData) invocations.get(i);
      BinCIType invocationPlace = id.getWhereType().getBinCIType();
      if (!testHostingClass.isAccessible(invocationPlace)) {
        return "Class " + testHostingClass.getQualifiedName()
            + " is not accessible everywhere";
      }
    }

    return null;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public boolean getStatement() {
    return statement.isSelected();
  }

  public boolean getOptimizeVisibility() {
    return optimizeVisibilityCheckBox.isSelected();
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setVisible(true);

    FactoryMethodDialog d = new FactoryMethodDialog(null, null, null);
    d.show();
    System.exit(0);
  }
}
