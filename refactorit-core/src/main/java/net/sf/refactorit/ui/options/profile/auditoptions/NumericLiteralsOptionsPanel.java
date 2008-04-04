/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.options.profile.auditoptions;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.options.profile.AuditOptionsSubPanel;
import net.sf.refactorit.ui.options.profile.ProfileType;
import net.sf.refactorit.utils.NumericLiteralsUtils;

import org.w3c.dom.Element;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteralsOptionsPanel extends AuditOptionsSubPanel{
  
  public NumericLiteralsOptionsPanel(final String auditKey, 
      ProfileType config, ResourceBundle resLocalizedStrings){
    super(auditKey, config, resLocalizedStrings);
    
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    final JPanel panel = new JPanel(new GridBagLayout());

    this.add(panel);
    
    // checkbox to skip collections
    final JCheckBox skipCollections = new JCheckBox(getDisplayTextAsHTML(
        "skip_collections.text"));
    
    // the descriptive header text
    final JLabel header = new JLabel(getDisplayTextAsHTML("header.text"));
    Vector vect = new Vector();
    
    // the lisst with all skiped literals
    final JList uilist = new JList(vect);
    uilist.setVisibleRowCount(-1);
    uilist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
    final JScrollPane scroller = new JScrollPane(uilist);
    scroller.setPreferredSize(new Dimension(150, 65));
    
    // in this text field user inputs a new literal that he wants to add
    // from the beginning the text is a hint, saying what is this field for.
    // the text dissapears when the field becomes focus
    final JTextField input = new JTextField(getDisplayText("input_temp.text"));
    input.setForeground(Color.GRAY);
    input.setPreferredSize(new Dimension(258, (int) input.getPreferredSize().getHeight()));
    
    final JButton buttonAdd = new JButton("Add");
    buttonAdd.setPreferredSize(new Dimension(100, (int) buttonAdd.getPreferredSize().getHeight()));
    final JButton buttonRem = new JButton("Remove");
    buttonRem.setPreferredSize(new Dimension(100, (int) buttonRem.getPreferredSize().getHeight()));
    
    GridBagConstraints constr = new GridBagConstraints();
    constr.insets = new Insets(4, 4, 4, 4);
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    
    constr.gridwidth = 2;
    constr.gridx = 0;
    constr.gridy = 0;
    constr.insets.top = 2;
    panel.add(skipCollections, constr);
    
    constr.gridy++;
    panel.add(header, constr);
    
    constr.gridy++;
    constr.gridwidth = 1;
    constr.gridheight = 2;
    panel.add(scroller, constr);
    
    constr.gridx++;
    constr.gridheight = 1;
    constr.insets.top = 4;
    panel.add(buttonAdd, constr);
    
    constr.gridy++;
    panel.add(buttonRem, constr);
    
    constr.gridy++;
    constr.gridx--;
    constr.gridwidth = 2;
    constr.insets.top = 0;
    panel.add(input, constr);
    
    putOptionField("skip_collections", skipCollections);    
    putOptionField("accepted", uilist);
    
    addCheckBoxListener(skipCollections, "skip_collections");
    
    // focus listener added to input text-field to remove hint text
    input.addFocusListener(new FocusListener(){
      public void focusGained(FocusEvent e) {
        if (input.getForeground() == Color.GRAY){
          input.setText("");
          input.setForeground(uilist.getForeground());
        }
      }
        
      public void focusLost(FocusEvent e) {}
          
    });
    
    // key listener is added to list to implement DEL-key support for deleting
    // literals
    uilist.addKeyListener(new KeyListener(){
      public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_DELETE){
          processRemoveLiteralRequest(uilist, panel);
        }
      }
      
      public void keyTyped(KeyEvent e){}
      public void keyReleased(KeyEvent e){}
    });
    
    // key listener is added to input to make ENTER add the literal to the list
    input.addKeyListener(new KeyListener(){
      public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
          processAddLiteralRequest(input, uilist, panel);
          e.consume();
        }
      }
      
      public void keyTyped(KeyEvent e){}
      public void keyReleased(KeyEvent e){}
    });
    
    // add action listeners for buttons
    buttonAdd.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        processAddLiteralRequest(input, uilist, panel);
      }
    });
    
    buttonRem.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        processRemoveLiteralRequest(uilist, panel);
      }
    });
  }
  
  void processAddLiteralRequest(JTextField input, JList uilist, 
      JPanel panel){
    String newLiteral = input.getText();
    if (newLiteral.trim().length() == 0) {
      // don`t bother user with messages, just do nothing
    } else if (!NumericLiteralsUtils.isValidNumLiteral(newLiteral)){
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(),
          "Sorry, '" + newLiteral + "' is not a valid numeric literal.",
          "Literal not added", JOptionPane.ERROR_MESSAGE);
    } else if (alreadyInList(newLiteral, 
        (DefaultListModel) uilist.getModel())){
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(), 
          "The literal '" + newLiteral + "' is already on the list.", 
          "Literal not added", JOptionPane.ERROR_MESSAGE);
    } else {
      ((DefaultListModel) uilist.getModel()).addElement(newLiteral);
      input.setText("");
      panel.repaint();

      final Element optionsElement = getOptionsElement();

      if (optionsElement != null) {
        final String newAttr = optionsElement.getAttribute("accepted") 
            + newLiteral + ";";
        optionsElement.setAttribute("accepted", newAttr);
      }
    }
  }
  
  void processRemoveLiteralRequest(JList uilist, JPanel panel){
    ListSelectionModel selectModel = uilist.getSelectionModel();
        
    DefaultListModel listModel = (DefaultListModel) uilist.getModel();
    int selected = -1;
    while ((selected = selectModel.getMaxSelectionIndex()) != -1){
      listModel.remove(selected);
    }

    StringBuffer prop = new StringBuffer("");
    for (int i = 0; i < listModel.getSize(); i++){
      prop.append((String) listModel.getElementAt(i)).append(";");
    }

    final Element optionsElement = getOptionsElement();

    if (optionsElement != null) {
      optionsElement.setAttribute("accepted", new String(prop));
    }

    panel.repaint();
  }
  
  private static boolean alreadyInList(String literal, 
      DefaultListModel listModel){
    for (int i = 0; i < listModel.size(); i++){
      if (literal.equals(listModel.get(i))){
        return true;
      }
    }
    return false;
  }
}
