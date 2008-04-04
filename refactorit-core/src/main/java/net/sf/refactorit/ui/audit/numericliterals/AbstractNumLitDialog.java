/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.numericliterals;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.treetable.BinTreeTable;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arseni Grigorjev
 */
public abstract class AbstractNumLitDialog {
  private static final String CONSTANTALIZE_OPTION_STRING
      = "make field declarations 'static final' where possible";
  
  protected ActionListener escapeActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      cancelActionPerformed();
    }
  };

  final RitDialog dialog;

  private boolean buttonOKPressed = false;
  private JScrollPane scrollPane;
  private JCheckBox constantalizeBox = null;
  private int scrollPaneXPadding = 0;
  private int scrollPaneYPadding = 0;
  
  private boolean showConstantalizeOption = false;
  
  protected List fixData = new ArrayList();
   
  public AbstractNumLitDialog(IdeWindowContext context, String title, 
      boolean showConstantalizeOption){
    dialog = RitDialog.create(context);
    dialog.setTitle(title);

    this.showConstantalizeOption = showConstantalizeOption;

    createContentPane();
  }
    
  public abstract void okActionPerformed();
  
  public abstract void cancelActionPerformed();
  
  public List getFixData(){
    return fixData;
  }
    
  public final boolean isButtonOKPressed(){
    return this.buttonOKPressed;
  }
  
  protected final void showErrorMessage(String messages, int messageType){
    RitDialog.showMessageDialog(
        IDEController.getInstance().createProjectContext(),
        messages, "Fix literals errors", messageType); 
  }
  
  protected final int showQuestionMessage(String message){
    return RitDialog.showConfirmDialog(
        IDEController.getInstance().createProjectContext(),
        message, "Constants detected", JOptionPane.YES_NO_CANCEL_OPTION);
  }
  
  private final void createContentPane(){
    final JPanel contentPanel = new JPanel() {
      public void paintComponent(Graphics g){
        resizeScrollPane();
        super.paintComponent(g);
      }
    };
    dialog.setContentPane(contentPanel);
  }
  
  protected void resizeScrollPane(){ 
    int xsize = this.scrollPane.getParent().getWidth() - scrollPaneXPadding;
    int ysize = this.scrollPane.getParent().getHeight() - scrollPaneYPadding;
    this.scrollPane.setPreferredSize(new Dimension(xsize, ysize));
  }
  
  protected void addEnterAndEscapeKeysListener(BinTreeTable table){
    table.addKeyListener(new KeyListener(){
      public void keyPressed(KeyEvent e){
        final int kc = e.getKeyCode();
        switch (kc) {

          case KeyEvent.VK_ENTER:
            e.consume();
            okActionPerformed();
            break;
            
          case KeyEvent.VK_ESCAPE:
            e.consume();
            cancelActionPerformed();
            break;
        }
      }
      
      public void keyTyped(KeyEvent e){}
      
      public void keyReleased(KeyEvent e){}
    });
  }
    
  protected final void setScrollPanePadding(final int x, final int y) {
    this.scrollPaneYPadding = y;
    this.scrollPaneXPadding = x;
  }

  protected final void setButtonOKPressed(final boolean buttonOKPressed) {
    this.buttonOKPressed = buttonOKPressed;
  }

  protected final JScrollPane getScrollPane() {
    return this.scrollPane;
  }

  protected final void setScrollPane(final JScrollPane scrollPane) {
    this.scrollPane = scrollPane;
  }
  
  protected JCheckBox createConstantalizeOption(){
    this.constantalizeBox = new JCheckBox(CONSTANTALIZE_OPTION_STRING);
    this.constantalizeBox.setSelected(true);
    return this.constantalizeBox;
  }

  public boolean showConstantalizeOption() {
    return this.showConstantalizeOption;
  }
  
  public boolean definesConstantalizeFix(){
    return (constantalizeBox != null && constantalizeBox.isSelected());
  }
}
