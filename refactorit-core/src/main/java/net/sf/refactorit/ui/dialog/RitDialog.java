/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.dialog;


import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowListener;


/**
 * Base class for RefactorIt dialogs. Wraps JDialog and/or
 * anything needed to bridge Swing with other windowing systems.
 * <br>
 * NB! The dialog is supposed to be shown only once.
 *
 * @author Igor Malinin
 */
public abstract class RitDialog implements RootPaneContainer {
  /** Context of this dialog owner */
  protected final IdeWindowContext owner;

  /** Context of this dialog */
  protected final IdeWindowContext context;

  /** Instance of Swing dialog to delegate */
  protected final JDialog dialog;

  protected RitDialog(IdeWindowContext owner) {
    this.owner = owner;

    context = owner.copy(this);
    dialog = createDialogInstance();
    setDisposeOnClose(true);
  }

  protected RitDialog(IdeWindowContext owner, JOptionPane pane) {
    this.owner = owner;

    context = owner.copy(this);
    this.dialog = createDialogInstance(pane);
    setDisposeOnClose(true);
  }

  protected abstract JDialog createDialogInstance();
  protected abstract JDialog createDialogInstance(JOptionPane pane);

  protected JDialog createDialogInstance(Window owner) {
    if (owner instanceof Dialog) {
      return new JDialog((Dialog) owner, true);
    }

    return new JDialog((Frame) owner, true);
  }

  protected JDialog createDialogInstance(Window owner, JOptionPane pane) {
    return pane.createDialog(owner, null);
  }

  public IdeWindowContext getOwner() {
    return owner;
  }

  public IdeWindowContext getContext() {
    return context;
  }

  public void show() {
    if (dialog.getWidth() == 0 || dialog.getHeight() == 0) {
      dialog.pack();
    }

    GraphicsConfiguration gconf = dialog.getGraphicsConfiguration();
    Rectangle screen = gconf.getBounds();
    Insets insets = dialog.getToolkit().getScreenInsets(gconf);
    Dimension size = dialog.getSize();

    int x = (screen.width - size.width - insets.left - insets.right) / 2;
    int y = (screen.height - size.height - insets.top - insets.bottom) / 2;

    dialog.setLocation(screen.x + x, screen.y + y);

    dialog.show();
  }

  public void dispose() {
    dialog.dispose();
  }

  public String getTitle() {
    return dialog.getTitle();
  }

  public void setTitle(String title) {
    dialog.setTitle(title);
  }

  public void setSize(int width, int height) {
    dialog.setSize(width, height);
  }

  public int getWidth() {
    return dialog.getWidth();
  }

  public int getHeight() {
    return dialog.getHeight();
  }

  public Dimension getMaximumSize() {
    GraphicsConfiguration gconf = dialog.getGraphicsConfiguration();
    Rectangle screen = gconf.getBounds();
    Insets insets = dialog.getToolkit().getScreenInsets(gconf);

    int width  = screen.width - insets.left - insets.right;
    int height = screen.height - insets.top - insets.bottom;

    return new Dimension(width, height);
  }

  public void setDisposeOnClose(boolean dispose) {
    dialog.setDefaultCloseOperation(
        dispose ? JDialog.DISPOSE_ON_CLOSE : JDialog.DO_NOTHING_ON_CLOSE);
  }

  public void addWindowListener(WindowListener listener) {
    dialog.addWindowListener(listener);
  }

  public void removeWindowListener(WindowListener listener) {
    dialog.removeWindowListener(listener);
  }

  /*
   * @see javax.swing.RootPaneContainer#getContentPane()
   */
  public Container getContentPane() {
    return dialog.getContentPane();
  }

  /*
   * @see javax.swing.RootPaneContainer#setContentPane(java.awt.Container)
   */
  public void setContentPane(Container contentPane) {
    dialog.setContentPane(contentPane);
  }

  /*
   * @see javax.swing.RootPaneContainer#getGlassPane()
   */
  public Component getGlassPane() {
    return dialog.getGlassPane();
  }

  /*
   * @see javax.swing.RootPaneContainer#setGlassPane(java.awt.Component)
   */
  public void setGlassPane(Component glassPane) {
    // TODO Auto-generated method stub
    dialog.setGlassPane(glassPane);
  }

  /*
   * @see javax.swing.RootPaneContainer#getLayeredPane()
   */
  public JLayeredPane getLayeredPane() {
    return dialog.getLayeredPane();
  }

  /*
   * @see javax.swing.RootPaneContainer#setLayeredPane(javax.swing.JLayeredPane)
   */
  public void setLayeredPane(JLayeredPane layeredPane) {
    dialog.setLayeredPane(layeredPane);
  }

  /*
   * @see javax.swing.RootPaneContainer#getRootPane()
   */
  public JRootPane getRootPane() {
    return dialog.getRootPane();
  }

  // ---------------------- FACTORY ----------------------

  // TODO
  private static RitDialogFactory factory = new SwingDialogFactory();

  public static void setDialogFactory(RitDialogFactory factory) {
    RitDialog.factory = factory;
  }

  public static RitDialogFactory getDialogFactory() {
    return RitDialog.factory;
  }

  public static RitDialog create(IdeWindowContext context) {
    return factory.createDialog(context);
  }

  public static RitDialog create(IdeWindowContext context, JOptionPane pane) {
    return factory.createDialog(context, pane);
  }


  // -------------- JFileChooser operations --------------

  public static int showFileDialog(
      IdeWindowContext context, JFileChooser chooser
  ) {
    return factory.showFileDialog(context, chooser);
  }


  // --------------- JOptionPane operations ---------------


  // Message Dialogs

  public static void showMessageDialog(
      IdeWindowContext context, Object message
  ) {
    showMessageDialog(context, message,
        UIManager.getString("OptionPane.messageDialogTitle"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   *
   * @param context
   * @param message
   * @param title
   * @param messageType JOptionPane constant
   */
  public static void showMessageDialog(
      IdeWindowContext context,
      Object message, String title,
      int messageType
  ) {
    showOptionDialog(context, message, title,
        JOptionPane.DEFAULT_OPTION, messageType, null, null);
  }


  // Confirm Dialogs

  public static int showConfirmDialog(
      IdeWindowContext context, Object message
  ) {
    return showConfirmDialog(context, message,
        UIManager.getString("OptionPane.titleText"),
        JOptionPane.YES_NO_CANCEL_OPTION);
  }

  public static int showConfirmDialog(
      IdeWindowContext context,
      Object message, String title,
      int optionType
  ) {
    return showConfirmDialog(context, message, title,
        optionType, JOptionPane.QUESTION_MESSAGE);
  }

  public static int showConfirmDialog(
      IdeWindowContext context,
      Object message, String title,
      int optionType, int messageType
  ) {
    return showOptionDialog(context, message, title,
        optionType, messageType, null, null);
  }


  // Input Dialogs

  public static String showInputDialog(
      IdeWindowContext context, Object message
  ) {
    return showInputDialog(context, message,
        UIManager.getString("OptionPane.inputDialogTitle"),
        JOptionPane.QUESTION_MESSAGE);
  }

  public static String showInputDialog(
      IdeWindowContext context,
      Object message, Object initialSelectionValue
  ) {
    return (String) showInputDialog(context, message,
        UIManager.getString("OptionPane.inputDialogTitle"),
        JOptionPane.QUESTION_MESSAGE, null, initialSelectionValue);
  }

  public static String showInputDialog(
      IdeWindowContext context,
      Object message, String title,
      int messageType
  ) {
    return (String) showInputDialog(context, message, title,
        messageType, null, null);
  }

  public static Object showInputDialog(
      IdeWindowContext context,
      Object message, String title, int messageType,
      Object[] selectionValues, Object initialSelectionValue
  ) {
    return factory.showInputDialog(context, message, title,
        messageType, selectionValues, initialSelectionValue);
  }


  // Generic Option Dialog

  public static int showOptionDialog(
      IdeWindowContext context, Object message, String title,
      int optionType, int messageType,
      Object[] options, Object initialValue
  ) {
    return factory.showOptionDialog(context, message, title,
        optionType, messageType, options, initialValue);
  }
}
