/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPaneToolBar;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Input panel where you can specify type using fQName or choose from type chooser
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.7 $ $Date: 2004/12/29 14:55:58 $
 */
public class TypeInputPanel extends JPanel {

  public static interface TypeChangedListener {

    /**
     * @param type current type qualified name
     */
    public void onChange(BinTypeRef type);
  }


  private JTextField targetClassQName;
  private RefactorItContext context;
  private JButton chooseButton;

  public TypeInputPanel(final RefactorItContext context, String qualifiedName) {
    this.context = context;
    this.setLayout(new GridBagLayout());
    targetClassQName = new JTextField(20);
    targetClassQName.setText(qualifiedName);

    GridBagConstraints cnstr = new GridBagConstraints(
        0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0);
    this.add(targetClassQName, cnstr);
    chooseButton = new JButton("...");
//    chooseButton.setMaximumSize(targetClassQName.getPreferredSize());
    BinPaneToolBar.tuneSmallButtonBorder(chooseButton);
    chooseButton.setMargin(new Insets(0, 5, 0, 5));
    chooseButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        doChoose();
      }

      private void doChoose() {
        String initialClassName = targetClassQName.getText();
        int lastDotIndex = initialClassName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex + 1 < initialClassName.length()) {
          initialClassName = initialClassName.substring(lastDotIndex + 1);
        }
        TypeChooser tc
            = new TypeChooser(context, initialClassName, true, "refact.search");
        tc.show();

        BinTypeRef searchableType = tc.getTypeRef();
        if (searchableType == null) {
//          text.setText("<choose>");
//          ok.setEnabled(false);
        } else {
          targetClassQName.setText(searchableType.getQualifiedName());
//          ok.setEnabled(true);
        }
      }

    });

//    choose.setMnemonic(KeyEvent.VK_S);

    cnstr.insets = new Insets(0, 0, 0, 0);
    cnstr.fill = GridBagConstraints.NONE;
    cnstr.gridx = 1;
    cnstr.weightx = 0;
    this.add(chooseButton, cnstr);
  }

  public void addTypeChangedListener(final TypeChangedListener listener) {
    targetClassQName.addCaretListener(new CaretListener() {
      private BinTypeRef lastSelectedType;

      public void caretUpdate(CaretEvent e) {
        BinTypeRef selectedType = getSelectedType();

        if (selectedType != lastSelectedType) {
          listener.onChange(selectedType);
          lastSelectedType = selectedType;
        }

      }
    });
  }

  /**
   * @param qName
   */
  public void setSelectedType(String qName) {
    // text listener calls update
    targetClassQName.setText(qName);
  }

  /**
   *
   * @return selected type of null if doesn't resolve to type
   */
  public BinTypeRef getSelectedType() {
    String text = targetClassQName.getText();
    text = text.trim();

    return context.getProject().getTypeRefForName(text);
  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    targetClassQName.setEnabled(enabled);
    chooseButton.setEnabled(enabled);
  }
}
