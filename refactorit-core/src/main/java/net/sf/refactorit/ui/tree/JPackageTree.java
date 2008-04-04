/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public final class JPackageTree extends JPanel {
  static final ImageIcon[] iconsFilter = {
      ResourceUtil.getIcon(TypeNode.class, "ShowAll.gif"), // red
      ResourceUtil.getIcon(TypeNode.class, "HidePrivate.gif"),
      ResourceUtil.getIcon(TypeNode.class, "HidePackage.gif"),
      ResourceUtil.getIcon(TypeNode.class, "ShowPublic.gif"), // green
  };

  JButton filterButton;
  JPopupMenu filterPopup;
  int visibility;

  private static final Insets noMargin = new Insets(0, 0, 0, 0);

  private static final ImageIcon iconFields =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Fields.gif");
  private static final ImageIcon iconMethods =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Methods.gif");
  private static final ImageIcon iconStatic =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Static.gif");

  // member list - toolbar items
  private JToggleButton showFields;
  private JToggleButton showMethods;

  private JToggleButton showStatic;

  private final JLabel label;
  private final JClassTree tree;

  public JPackageTree(RefactorItContext context) {
    setLayout(new BorderLayout());

    JPanel header = new JPanel();
    header.setLayout(new BorderLayout());

    NodeIcons icons = NodeIcons.getNodeIcons();

    label = new JLabel("Packages", icons.getPackageIcon(false), JLabel.LEFT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    header.add(label);

    JToolBar bar = createToolBar();
    header.add(bar, BorderLayout.EAST);

    add(header, BorderLayout.NORTH);

    tree = new JClassTree(PackageTreeModel.EMPTY, context);
    tree.setRootVisible(true);

    filterButton.setNextFocusableComponent(tree);
    /*    filterButton.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            System.out.println("filterButton in focus");
            System.out.println("tree.isRequestFocusEnabled(): "
                + tree.isRequestFocusEnabled());
          }
          public void focusLost(FocusEvent e) {
            System.out.println("filterButton next: "
                + filterButton.getNextFocusableComponent());
          }
        });*/

    add(new JScrollPane(tree), BorderLayout.CENTER);
  }

  private void createFilterButton(final JToolBar toolbar) {
    filterPopup = new JPopupMenu("Access Modifiers");

    JMenuItem item;
    ButtonGroup group = new ButtonGroup();

    item = new JRadioButtonMenuItem("Show All", iconsFilter[0]);
    item.setSelected(true);
    item.addActionListener(new FilterPopupListener(0));
    group.add(item);
    filterPopup.add(item);

    item = new JRadioButtonMenuItem("Hide Private", iconsFilter[1]);
    item.addActionListener(new FilterPopupListener(1));
    group.add(item);
    filterPopup.add(item);

    item = new JRadioButtonMenuItem("Hide Package", iconsFilter[2]);
    item.addActionListener(new FilterPopupListener(2));
    group.add(item);
    filterPopup.add(item);

    item = new JRadioButtonMenuItem("Show Public", iconsFilter[3]);
    item.addActionListener(new FilterPopupListener(3));
    group.add(item);
    filterPopup.add(item);

    filterButton = new JButton(iconsFilter[0]);
    filterButton.setMargin(noMargin);
    filterButton.setToolTipText("Access Modifiers");
    filterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        filterPopup.show(toolbar,
            filterButton.getX() + filterButton.getWidth()
            - filterPopup.getPreferredSize().width,
            filterButton.getY() + filterButton.getHeight());
        filterPopup.requestFocus();
      }
    });

    toolbar.add(filterButton);
  }

  private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    showFields = new JToggleButton(iconFields);
    showFields.setSelected(true);
    showFields.setMargin(noMargin);
    showFields.setToolTipText("Fields");
    showFields.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshTree();
      }
    });

    toolbar.add(showFields);

    showMethods = new JToggleButton(iconMethods);
    showMethods.setSelected(true);
    showMethods.setMargin(noMargin);
    showMethods.setToolTipText("Methods");
    showMethods.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshTree();
      }
    });

    toolbar.add(showMethods);

    toolbar.addSeparator();

    showStatic = new JToggleButton(iconStatic);
    showStatic.setSelected(true);
    showStatic.setMargin(noMargin);
    showStatic.setToolTipText("Static Members");
    showStatic.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshTree();
      }
    });

    toolbar.add(showStatic);

    toolbar.addSeparator();

    createFilterButton(toolbar);

    return toolbar;
  }

  public final void refreshTree() {
    int filter = visibility;

    if (!showFields.isSelected()) {
      filter |= TypeNode.HIDE_FIELDS;
    }

    if (!showMethods.isSelected()) {
      filter |= TypeNode.HIDE_METHODS;
    }

    if (!showStatic.isSelected()) {
      filter |= TypeNode.HIDE_STATIC;
    }

    getPackageTreeModel().filter(filter);
  }

  public final JLabel getHeaderLabel() {
    return label;
  }

  public final JClassTree getClassTree() {
    return tree;
  }

  public final PackageTreeModel getPackageTreeModel() {
    return (PackageTreeModel) tree.getModel();
  }

  public final void setPackageTreeModel(PackageTreeModel model) {
    tree.setModel(model);
  }

  final class FilterPopupListener implements ActionListener {
    private final int level;

    public FilterPopupListener(int level) {
      this.level = level;
    }

    /**
     * @param evt event
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public final void actionPerformed(ActionEvent evt) {
      visibility = level;
      filterButton.setIcon(iconsFilter[level]);
      filterButton.requestFocus();
      refreshTree();
    }
  }
}
