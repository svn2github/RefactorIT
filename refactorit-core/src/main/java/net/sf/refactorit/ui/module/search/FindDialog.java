/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.search;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.query.structure.FindRequest;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.TypeChooser;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EventListener;


public class FindDialog {
  private static final Logger log = AppRegistry.getLogger(FindDialog.class);
  
  final RitDialog dialog;

  private String[] searchables = {
    "Fields", "Parameters", "Return types", "Type Cast", "Instanceof type", "Comparison parameter type"
  };

  private JComboBox target = new JComboBox(searchables);
  private JTextField text = new JTextField("java.lang.Object", 30);
  private JButton choose = new JButton("Select class");
  private JButton ok = new JButton("Ok");
  private JButton cancel = new JButton("Cancel");
  private JCheckBox subtypes = new JCheckBox("Include subtypes");
  private String whereSearchingMsg;

  private BinTypeRef searchableType;

  private FindRequest request;
  private RefactorItContext context;

  public FindDialog(RefactorItContext context, Object searchIn) {
    this.context = context;

    dialog = RitDialog.create(context);
    dialog.setTitle("Structure Search");

    if (searchIn instanceof BinPackage) {
      whereSearchingMsg = "package " + ((BinPackage) searchIn).getQualifiedName();
    } else if (searchIn instanceof BinMember) {
      CompilationUnit compilationUnit = ((BinMember) searchIn).getCompilationUnit();
      if (compilationUnit != null) {
        whereSearchingMsg = compilationUnit.getName();
      } else {
        // FIXME: element in classpath! Should not get here!!
        whereSearchingMsg = ((BinMember) searchIn).getName();
      }
    } else {
      whereSearchingMsg = "project";
    }

//    ok.setEnabled(false);
    text.setEnabled(true);
    JPanel cp = new JPanel(new BorderLayout());
    dialog.setContentPane(cp);

    JPanel center = new JPanel(new GridLayout(3, 1));
    center.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel north = new JPanel(new BorderLayout());
    north.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    north.add(DialogManager.getHelpPanel(
        "Enter type and context to search in " + whereSearchingMsg),
        BorderLayout.NORTH);

    JPanel search = new JPanel();
    search.setLayout(new FlowLayout(FlowLayout.LEFT));

    search.add(new JLabel("Search for "));
    search.add(target);
    //search.add(new JLabel(whereSearchingMsg));
    search.add(new JLabel(" where class is (fully qualified name)"));

    JPanel typeArea = new JPanel();
    typeArea.add(text);
    text.selectAll();

    choose.setMnemonic(KeyEvent.VK_S);
    typeArea.add(choose);

    center.add(search);
    center.add(typeArea);
    center.add(subtypes);

    north.add(center, BorderLayout.CENTER);
    cp.add(north, BorderLayout.CENTER);

    JButton buttonHelp = new JButton("Help");
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.search");

    // butttons
    JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
    buttons.add(ok);
    buttons.add(cancel);
    buttons.add(buttonHelp);

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    p.add(buttons, constraints);
    cp.add(p, BorderLayout.SOUTH);

    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doChoose();
      }
    });

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOk();
      }
    });

    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    };
    cancel.addActionListener(cancelActionListener);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, ok, cancel, buttonHelp,
        cancelActionListener);
  }

  void doChoose() {
    TypeChooser tc = new TypeChooser(context, "", false, "refact.search");
    tc.show();

    searchableType = tc.getTypeRef();
    if (searchableType == null) {
//      text.setText("<choose>");
//      ok.setEnabled(false);
    } else {
      text.setText(searchableType.getQualifiedName());
      ok.setEnabled(true);
    }
  }

  void onCancel() {
    dispose();
  }

  void onOk() {
    FindRequest req = new FindRequest();
    request = null;
    String userInput = text.getText().trim();
    searchableType = findSearchableType(userInput);
    
    if (searchableType == null) {
      return;
    }
    
    req.searchableType = searchableType;
    req.searchType = target.getSelectedIndex() + 1;
    req.includeSubtypes = subtypes.isSelected();

    request = req;
    dispose();
  }

  public FindRequest getRequest() {
    return request;
  }

  public void show() {
    dialog.show();
  }

  public void dispose() {
    this.removeListeners(this.choose, ActionListener.class);
    this.removeListeners(this.ok, ActionListener.class);
    this.removeListeners(this.cancel, ActionListener.class);

    dialog.dispose();
  }

  /**
   * Removes all listeners of the listenerType that has been added to the specified button.
   *
   * @param button as a target from where to remove all listeners of type listenerType
   * @param listenerType the type of Listener. (ActionListener, ...)
   */
  private void removeListeners(AbstractButton button, Class listenerType) {
    EventListener[] listeners = button.getListeners(listenerType);
    for (int i = 0; i < listeners.length; i++) {
      button.removeActionListener((ActionListener) listeners[i]);
    }
    //System.out.println(listenerType.getName()+" removed: "+listeners.length);
  }
  
  
  /**
   * Trying to parse and find Searchable type from string (class name) user 
   * entered. Returns null, if type has not been found.
   * 
   * @param userInput User's input (name of class)
   * @return object of class BinTypeRef, or null, if type has not been found.
   * @throws IllegalArgumentException if array brackets mismatch
   */
  private BinTypeRef findSearchableType(String typeName) throws IllegalArgumentException {
    if (typeName.length() == 0) {
      return null;
    }
    
    BinArrayType.ArrayType arrayType = null;
    try{
      arrayType = BinArrayType.extractArrayTypeFromString(typeName);
	  } catch(IllegalArgumentException problem){
	    showBracketsMismatchDialog(typeName);
	    return null;
	  }
    
    BinTypeRef typeRef = context.getProject().findTypeRefForName(arrayType.type);
      
    if (typeRef == null){
      showNotFoundTypeDialog(arrayType.type);
      return null;
    }
    
    if (arrayType.dimensions > 0){
      typeRef = context.getProject().createArrayTypeForType(typeRef, arrayType.dimensions);
    }
    
    return typeRef;
  }
  
  
  /**
   * Shows brackets mismatch error dialog
   * @param typeName name of incorrectly entered type
   */
  private void showBracketsMismatchDialog(String typeName){
    String title = "Array brackets mismatch";
    String message = "Array brackets in the type name \"" + typeName + "\" mismatch";
    RitDialog.showMessageDialog(context, message, title, JOptionPane.ERROR_MESSAGE);
  }
  
  
  /**
   * Shows not found type error dialog
   * @param typeName name of incorrectly entered type
   */
  private void showNotFoundTypeDialog(String typeName){
    String title = "Type not found";
    String message = "Type with name \"" + typeName + "\" has not been found";
    RitDialog.showMessageDialog(context, message, title, JOptionPane.ERROR_MESSAGE);
  }
}
