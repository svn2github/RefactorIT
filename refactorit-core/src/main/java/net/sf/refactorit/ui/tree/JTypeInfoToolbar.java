/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public final class JTypeInfoToolbar extends JPanel {
  // the containers to hold the references to buttons that were added by users of
  // this instance of JTypeInfoToolbar, using methods addXXXButton(..).
  private final List toolBarAddInButtons = new ArrayList();

  private JToolBar extraBar = null;
  private static final Insets defaultMargin = new Insets(2, 2, 2, 2);

  public JTypeInfoToolbar() {
    super(new BorderLayout(0, 0));

    this.extraBar = new JToolBar(JToolBar.HORIZONTAL);
    this.extraBar.setFloatable(false);
    this.extraBar.setMargin(defaultMargin);
    this.extraBar.setMinimumSize(new Dimension(0, 0));
    this.extraBar.setBorder(BorderFactory.createEmptyBorder());

    this.add(this.extraBar, BorderLayout.CENTER);
  }

  public final void addToolbarButton(AbstractButton button) {
    tuneButtonProperties(button);
    this.extraBar.add(button);
    // add the button into an array, so the users of this class
    // can retrieve the buttons later they have been added into this
    // toolbar by calling getAddInButtons(..) function.
    this.toolBarAddInButtons.add(button);
  }

  /**
   * FIXME: Copied it wholly from BinPanelToolBar#tuneButtonProperties
   */
  private void tuneButtonProperties(final AbstractButton button) {
    button.setMargin(defaultMargin);

    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setAlignmentY(Component.CENTER_ALIGNMENT);

    new MetalLookAndFeel().getDefaults();
    button.setRolloverEnabled(true);

    if (button instanceof JToggleButton) {
      //      button.setBorder(BasicBorders.getToggleButtonBorder());
      button.setBorderPainted(true);
    } else {
      button.setBorder(
          new BorderUIResource.CompoundBorderUIResource(
          new MetalBorders.Flush3DBorder(),
          new BasicBorders.MarginBorder()));
      button.setBorderPainted(false);
    }

    final Color initialColor = UIManager.getColor("Button.background");
    int shift = 30;
    if (initialColor.getRed() - shift < 0) {
      shift = initialColor.getRed();
    }
    if (initialColor.getGreen() - shift < 0) {
      shift = initialColor.getGreen();
    }
    final Color newColor = new Color(
        initialColor.getRed() - shift,
        initialColor.getGreen() - shift,
        initialColor.getBlue());

    button.addMouseListener(new MouseAdapter() {

      public void mouseEntered(MouseEvent mouseEvent) {
        if (button.isEnabled()) {
          if (!(button instanceof JToggleButton)) {
            button.setBorderPainted(true);
          }
          button.setBackground(newColor);
        }
      }

      public void mouseExited(MouseEvent mouseEvent) {
        if (button.isEnabled()) {
          if (!(button instanceof JToggleButton)) {
            button.setBorderPainted(false);
          }
          button.setBackground(initialColor);
        }
      }
    });
  }
}
