/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Panel lists all properties of a type (class or
 * interface). It have toolbar buttons which defines
 * filtering of properties shown (show inherited
 * members, hide private members, ... ).
 *
 * @author Igor Malinin
 */
public final class JTypeInfoPanel extends JPanel implements OptionsChangeListener {
  static final ImageIcon[] iconsFilter = {
      ResourceUtil.getIcon(JTypeInfoPanel.class, "ShowAll.gif"), // red
      ResourceUtil.getIcon(JTypeInfoPanel.class, "HidePrivate.gif"),
      ResourceUtil.getIcon(JTypeInfoPanel.class, "HidePackage.gif"),
      ResourceUtil.getIcon(JTypeInfoPanel.class, "ShowPublic.gif"), // green
  };

  private static final Object[] empty = {};
  private static final Insets noMargin = new Insets(0, 0, 0, 0);

  private static final ImageIcon iconInherited =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Inherited.gif");

  private static final ImageIcon iconFields =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Fields.gif");
  private static final ImageIcon iconMethods =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Methods.gif");

  private static final ImageIcon iconStatic =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Static.gif");

  private static final ImageIcon iconSuperTypes =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "SuperTypes.gif");

  private static final ImageIcon iconSubTypes =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "SubTypes.gif");

  private static final ImageIcon iconLock =
      ResourceUtil.getIcon(JTypeInfoPanel.class, "Lock.gif");

  final List lockObservers = new ArrayList();

  // this member displayed in tree
  BinMember member;

  // top level components
  JClassTree superClassesTree;
  JClassTree subClassesTree;

  JList list;
  JLabel hierarhyInfoLabel;

  JButton filterButton;
  JPopupMenu filterPopup;

  final boolean ensureProject;
  boolean isLocked;

  int visibility;

  RefactorItContext context;
  Component owner;

  // member list - toolbar items
  private JToggleButton showInherited;

  private JToggleButton showFields;
  private JToggleButton showMethods;

  private JToggleButton showStatic;

  private JSplitPane verticalSplit;
  private JSplitPane horizontalSplit;

  // determines an active tree, one of tree
  // components below
  private JClassTree activeTree;

  private JPanel treeHierarhyPanel;
  private JPanel javaDocPanel;
  private JLabel head;
  private JTypeInfoJavaDoc javadocRenderer;

  // members of this type are listed
  private BinCIType type;

  private boolean showJavadoc;

  private Object lastSelection;

  /**
   * Use this constructor if you want to set your custom showJavadoc flag
   *
   * @param context context
   * @param owner owner
   * @param showJavadoc showJavadoc
   * @param ensureProject true if SubMenuModel.ensureProject() is required. Default: false.
   */
  public JTypeInfoPanel(
      RefactorItContext context, boolean showJavadoc, boolean ensureProject
  ) {
    this.showJavadoc = showJavadoc;
    this.ensureProject = ensureProject;
    this.context = context;

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder());

    GlobalOptions.registerOptionChangeListener(this);

    init();
  }

  /**
   * Use this constructor if you want to show JavaDoc according with
   * "module.type.javadoc" option value.
   * @param context context
   * @param owner owner
   */
  public JTypeInfoPanel(RefactorItContext context) {
    this(context,
        GlobalOptions.getOption("module.type.javadoc").equalsIgnoreCase("true"), false);
  }

  /**
   * This method also sets "module.type.javadoc" option to 'show' value
   * @param show show
   */
  public final void showJavaDoc(boolean show) {
    GlobalOptions.setOption("module.type.javadoc", "" + show);
    showJavadoc = show;
    removeAll();
    init();

    if (showJavadoc && javadocRenderer != null) {
      javadocRenderer.dockInto(javaDocPanel);
      updateJavaDoc(type);
    }
  }

  private void init() {
    // create vertical split pane
    if (verticalSplit == null) {
      verticalSplit = new JSplitPane(
          JSplitPane.VERTICAL_SPLIT,
          createHierarhyPanel(),
          createListPanel());

      String dividerString = GlobalOptions.getOption("typeinfo.divider");
      int divider;
      try {
        divider = (dividerString == null) ? 150
            : Integer.valueOf(dividerString).intValue();
      } catch (Exception e) {
        divider = 150;
      }
      verticalSplit.setDividerLocation(divider);
      verticalSplit.setDividerSize(2);

      verticalSplit.addPropertyChangeListener("dividerLocation",
          new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          GlobalOptions.setOption("typeinfo.divider", String.valueOf(evt.getNewValue()));
          GlobalOptions.save();
        }
      });
    }

    if ((javaDocPanel == null || horizontalSplit == null) && showJavadoc) {
      javaDocPanel = createJavaDocPanel();
      horizontalSplit = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          verticalSplit,
          javaDocPanel);
      //horizontalSplit.setOneTouchExpandable(true);
      horizontalSplit.setDividerLocation(300);
      horizontalSplit.setDividerSize(2);
    }

    if (showJavadoc) {
      horizontalSplit = new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT,
          verticalSplit,
          javaDocPanel);
      //horizontalSplit.setOneTouchExpandable(true);
      horizontalSplit.setDividerLocation(300);
      add(horizontalSplit);
    } else {
      add(verticalSplit);
    }

    // set active tree
    if (activeTree == null) {
      setActiveTree(superClassesTree);
      hierarhyInfoLabel.setText("Show the Supertype Hierarchy");
    }
  }

  private JPanel createHierarhyPanel() {

    // create a hierarhyPanel
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    createSuperTypesPanel();
    createSubTypesPanel();
    JTypeInfoToolbar toolBar = createTypeHierarhyToolbar();

    // make a toolbar panel where to put toolbar and
    // hierarhy information.
    JPanel toolBarPanel = new JPanel();
    toolBarPanel.setLayout(new BorderLayout());
    hierarhyInfoLabel = new JLabel();

    // add toolbar and hierarhy label to toolbar panel
    toolBarPanel.add(toolBar, BorderLayout.WEST);
    toolBarPanel.add(hierarhyInfoLabel, BorderLayout.CENTER);

    // make a treeHierarhyPanel with 1 column
    // and as many rows as necessary
    treeHierarhyPanel = new JPanel();
    treeHierarhyPanel.setLayout(new GridLayout(0, 1));

    // add component to main panel
    panel.add(toolBarPanel, BorderLayout.NORTH);
    panel.add(treeHierarhyPanel, BorderLayout.CENTER);
    return panel;
  }

  private JTypeInfoToolbar createTypeHierarhyToolbar() {
    // create and initialize toolbar
    JTypeInfoToolbar toolBar = new JTypeInfoToolbar();
    toolBar.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

    final JToggleButton refreshButton = new JToggleButton(UIResources.getRefreshIcon());
    final JToggleButton lockButton = new JToggleButton(iconLock, false);
    final JToggleButton superTypesButton = new JToggleButton(iconSuperTypes, true);
    final JToggleButton subTypesButton = new JToggleButton(iconSubTypes, false);

    refreshButton.setMargin(noMargin);
    lockButton.setMargin(noMargin);
    superTypesButton.setMargin(noMargin);
    subTypesButton.setMargin(noMargin);

    refreshButton.setToolTipText("Refreshes the current view");
    lockButton.setToolTipText("Locks the current view");
    superTypesButton.setToolTipText("Shows the supertype hierarchy");
    subTypesButton.setToolTipText("Shows the subtype hierarchy");

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshAll();
        refreshButton.setSelected(false);
      }
    });

    lockButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isLocked = lockButton.isSelected();
        notifyLockObservers();
      }
    });

    superTypesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (superTypesButton.isSelected()) {
          setActiveTree(superClassesTree);
          hierarhyInfoLabel.setText("Supertype Hierarchy");
          subTypesButton.setSelected(false);
        } else {
          superTypesButton.setSelected(true);
        }
      }
    });

    subTypesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (subTypesButton.isSelected()) {
          setActiveTree(subClassesTree);
          hierarhyInfoLabel.setText("Subtype Hierarchy");
          superTypesButton.setSelected(false);
        } else {
          subTypesButton.setSelected(true);
        }
      }
    });

    if (IDEController.runningNetBeans()) {
      toolBar.addToolbarButton(refreshButton);
    }

    toolBar.addToolbarButton(lockButton);
    toolBar.addToolbarButton(superTypesButton);
    toolBar.addToolbarButton(subTypesButton);

    return toolBar;
  }

  final void setActiveTree(JClassTree tree) {
    treeHierarhyPanel.removeAll();
    activeTree = tree;
    treeHierarhyPanel.add(new JScrollPane(activeTree));
    //expand( superClassesTree.getPathForRow(0), activeTree );
    showSelectedNodeInfo(activeTree);

    // not sure that all of them are needed
    invalidate();
    validate();
    repaint();
  }

  /**
   * Creates a panel containing a tree of subtypes for Bin object.
   * @return panel
   */
  protected final JPanel createSubTypesPanel() {
    // add subclasses part into the panel. i.e. add
    // label/separator for subclasses tree and subclasses
    // tree itself.
    JPanel subclassesPanel = new JPanel();
    subclassesPanel.setLayout(new BorderLayout());
    JLabel subclassesLabel = new JLabel("Subclasses");
    subclassesLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    subClassesTree = new JClassTree(InheritanceTreeModel.empty, context);
    subClassesTree.setRootVisible(true);
    setTreeHierarhyListeners(subClassesTree);
    //panel.add(new JScrollPane(subclassesTree));
    subclassesPanel.add(subclassesLabel, BorderLayout.NORTH);
    subclassesPanel.add(new JScrollPane(subClassesTree), BorderLayout.CENTER);

    // add event listeners for subclassesTree!!!

    return subclassesPanel;
  }

  /**
   * Creates a superclasses hierarchy panel for Bin object.
   * @return panel
   */
  protected final JPanel createSuperTypesPanel() {

    JPanel superclassesPanel = new JPanel();
    superclassesPanel.setLayout(new BorderLayout());
    JLabel superclassesLabel = new JLabel("Superclasses");
    superclassesLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    superClassesTree = new JClassTree(InheritanceTreeModel.empty, context);
    superClassesTree.setRootVisible(true);
    setTreeHierarhyListeners(superClassesTree);
    superclassesPanel.add(superclassesLabel, BorderLayout.NORTH);
    superclassesPanel.add(new JScrollPane(superClassesTree),
        BorderLayout.CENTER);

    return superclassesPanel;
  }

  /**
   * creates a panel where javadocs for classes and methods/fields
   * are shown.
   * @return panel
   */
  protected final JPanel createJavaDocPanel() {
    JPanel javaDocPanel = new JPanel();
    javaDocPanel.setLayout(new BorderLayout());
    javaDocPanel.add(new JLabel("BBBBB"));
    return javaDocPanel;
  }

  /**
   * Sets the renderer of javadoc information for this instance
   * of JTypeInfoPanel.
   *
   * @param javadocRenderer the renderer of javadoc information.
   */
  public final void setJavaDocRenderer(JTypeInfoJavaDoc javadocRenderer) {
    this.javadocRenderer = javadocRenderer;

    if (showJavadoc) {
      javadocRenderer.dockInto(javaDocPanel);
    }
  }

  /**
   * Sets a hierarhy listeners (keyboard, mouse, ...) for a
   * specified tree.
   * @param tree tree
   */
  private void setTreeHierarhyListeners(final JTree tree) {
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent evt) {
        showSelectedNodeInfo(tree);
      }
    });

    KeyStroke ks = KeyStroke.getKeyStroke(
        KeyEvent.VK_M, ActionEvent.CTRL_MASK);

    try {
    tree.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
          return;
        }

        Object node = path.getLastPathComponent();

        JPopupMenu popup = createPopup(((UITreeNode) node).getBin());

        // Do not show empty PopupMenu
        if (popup != null) {
          Rectangle rect = tree.getPathBounds(path);

          popup.show(tree, rect.x + rect.width / 2, rect.y + rect.height / 2);

          SwingUtil.ensureInViewableArea(popup);

          popup.requestFocus();
        }
      }
    }, ks, WHEN_FOCUSED);
    } catch (Exception e) {
      // failed to register, let's live without
    }

    tree.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        int row = tree.getRowForLocation(evt.getX(), evt.getY());
        if (row < 0) {
          return;
        }

        TreePath path = tree.getPathForRow(row);
        tree.setSelectionPath(path);

        if (evt.isPopupTrigger()) {
          popup(evt);
          return;
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          popup(evt);
          return;
        }
      }

      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
          TreePath path = tree.getSelectionPath();
          Object node = path.getLastPathComponent();

          if (node instanceof SourceNode) {
            SourceNode src = (SourceNode) node;

            CompilationUnit file = src.getCompilationUnit();
            if (file == null) {
              return;
            }

            SourceCoordinate start = src.getStart();

            context.show(file, (start != null) ? start.getLine() : 1,
                GlobalOptions.getOption("source.selection.highlight").equals("true"));
          }
        }

        registerKeyboardActions(tree);
      }

      private void popup(MouseEvent evt) {
        TreePath path = tree.getSelectionPath();
        Object node = path.getLastPathComponent();

        JPopupMenu popup = createPopup(((UITreeNode) node).getBin());

        // do not show empty popup
        if (popup != null) {
          popup.show(evt.getComponent(), evt.getX(), evt.getY());

          SwingUtil.ensureInViewableArea(popup);
          popup.requestFocus();
        }
      }
    });
  }

  /**
   * Shows the info (methods, fields, ...) in the list panel
   * for selected node in the tree.
   *
   * @param tree a tree containing the selected node for which
   * the info is needed to show in the list panel. If no node
   * is selected then no info is shown.
   */
  final void showSelectedNodeInfo(final JTree tree) {
    registerKeyboardActions(tree);

    TreePath path = tree.getSelectionPath();
    if (path == null) {
      return;
    }
    Object node = path.getLastPathComponent();
    if (node instanceof InheritanceNode) {
      BinCIType t = ((InheritanceNode) node).getBinCIType();
      if (t == type) {
        return;
      }
      type = t;
      updateJavaDoc(type);
      refreshList();
    }
  }

  private void createFilterButton(final JToolBar toolbar) {
    ButtonGroup group = new ButtonGroup();

    filterPopup = new JPopupMenu("Access Modifiers");

    JMenuItem item;

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
            filterButton.getX() + filterButton.getWidth() -
            filterPopup.getPreferredSize().width,
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

    showInherited = new JToggleButton(iconInherited);
    showInherited.setMargin(noMargin);
    showInherited.setToolTipText("Show All Inherited Members");
    showInherited.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshList();
      }
    });

    toolbar.add(showInherited);

    toolbar.addSeparator();

    showFields = new JToggleButton(iconFields);
    showFields.setSelected(true);
    showFields.setMargin(noMargin);
    showFields.setToolTipText("Fields");
    showFields.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshList();
      }
    });

    toolbar.add(showFields);

    showMethods = new JToggleButton(iconMethods);
    showMethods.setSelected(true);
    showMethods.setMargin(noMargin);
    showMethods.setToolTipText("Methods");
    showMethods.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        refreshList();
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
        refreshList();
      }
    });

    toolbar.add(showStatic);

    toolbar.addSeparator();

    createFilterButton(toolbar);

    return toolbar;
  }

  private JPanel createListPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    JPanel header = new JPanel();
    header.setLayout(new BorderLayout());

    head = new JLabel();
    head.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    header.add(head);

    header.add(createToolBar(), BorderLayout.EAST);

    panel.add(header, BorderLayout.NORTH);

    list = new JList(empty);
    list.setCellRenderer(new CellRenderer());
    list.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
    list.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
    list.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));

    list.setSelectionBackground(
        Color.decode(GlobalOptions.getOption("tree.selection.background")));
    list.setSelectionForeground(
        Color.decode(GlobalOptions.getOption("tree.selection.foreground")));

    list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        resetKeyboardActions();

        if (evt.getValueIsAdjusting()) {
          return;
        }

        BinMember m = (BinMember) list.getSelectedValue();
        if (m == member) {
          return;
        }

        registerKeyboardActions(list);

        member = m;
        updateJavaDoc(member);
        refreshTree();
      }
    });

    KeyStroke ks = KeyStroke.getKeyStroke(
        KeyEvent.VK_M, ActionEvent.CTRL_MASK);

    try {
    list.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Object node = list.getSelectedValue();
        if (node == null) {
          return;
        }

        JPopupMenu popup = createPopup(node);

        // Do not show empty PopupMenu
        if (popup != null) {
          int row = list.getSelectedIndex();
          Rectangle rect = list.getCellBounds(row, row);

          popup.show(list, rect.x + rect.width / 2, rect.y + rect.height / 2);

          SwingUtil.ensureInViewableArea(popup);

          popup.requestFocus();
        }
      }
    }, ks, WHEN_FOCUSED);
    } catch (Exception e) {
      // failed to register, let's live without
    }

    list.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int row = list.locationToIndex(e.getPoint());
        if (row < 0) {
          return;
        }

        list.setSelectedIndex(row);

        if (e.isPopupTrigger()) {
          popup(e);
          return;
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          popup(e);
          return;
        }
      }

      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
          popup(e);
          return;
        }

        if (e.getClickCount() == 2) {
          BinMember m = (BinMember) list.getSelectedValue();
          if (m == null) {
            // Happens, for example, if the list is empty
            // (IOW if the class contains no members).
            return;
          }

          BinCIType t = m.getOwner().getBinCIType();
          CompilationUnit file = t.getCompilationUnit();
          if (file == null) {
            return;
          }

          ASTImpl node = m.getOffsetNode();
          if (node == null) {
            return;
          }

          SourceCoordinate start = SourceCoordinate
              .getForAST(ASTUtil.getFirstNodeOnLine(node));

          context.show(file, (start != null) ? start.getLine() : 1,
              GlobalOptions.getOption("source.selection.highlight").equals("true"));
        }

        registerKeyboardActions(list);
      }

      private void popup(MouseEvent evt) {
        Object bin = list.getSelectedValue();
        if (bin == null) {
          // Happens, for example, if the list is empty
          // (IOW if the class contains no members).
          return;
        }

        JPopupMenu popup = createPopup(bin);

        // Do not show empty PopupMenu
        if (popup != null) {
          popup.show(evt.getComponent(), evt.getX(), evt.getY());

          SwingUtil.ensureInViewableArea(popup);
          popup.requestFocus();
        }
      }
    });

    panel.add(BorderLayout.CENTER, new JScrollPane(list));

    return panel;
  }

  final void registerKeyboardActions(final JTree tree) {
    resetKeyboardActions();
    TreePath path = tree.getSelectionPath();
    if (path == null) {
      return;
    }

    Object node = path.getLastPathComponent();
    if (node instanceof InheritanceNode) {
      BinCIType t = ((InheritanceNode) node).getBinCIType();
      registerKeyboardActions(t);
    }
  }

  final void registerKeyboardActions(JList list) {
    registerKeyboardActions((BinMember) list.getSelectedValue());
  }

  private void registerKeyboardActions(BinMember target) {
    if (target == null) {
      return;
    }

    List actions = ModuleManager.getActions(target);
    if (actions == null || actions.size() == 0) {
      return;
    }

    for (int i = 0; i < actions.size(); i++) {
      final RefactorItAction act = (RefactorItAction) actions.get(i);

      KeyStroke keystroke = act.getKeyStroke();

      try {
        registerKeyboardAction(createListener(act, target),
            keystroke, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      } catch (Exception e) {
        // failed to register, let's live without
      }
    }

    if (ideKeyboardActionsRegister != null) {
      List safeActions = ideKeyboardActionsRegister.getKeyboardActions();
      for (Iterator i = safeActions.iterator(); i.hasNext(); ) {
        KeyboardAction action = (KeyboardAction) i.next();
        try {
          registerKeyboardAction(createListener(action, target),
              action.getKeyStroke(), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        } catch (Exception e) {
          // failed to register, let's live without
        }
      }
    }
  }

  private static IdeKeyboardActionsRegister ideKeyboardActionsRegister;

  public interface IdeKeyboardActionsRegister {
    List getKeyboardActions();
  }


  public static void setIdeShortcutsImporter(IdeKeyboardActionsRegister
      register) {
    ideKeyboardActionsRegister = register;
  }

  public abstract static class KeyboardAction {
    private final KeyStroke keyStroke;

    public KeyboardAction(KeyStroke keyStroke) {
      this.keyStroke = keyStroke;
    }

    public final KeyStroke getKeyStroke() {
      return this.keyStroke;
    }

    public abstract void actionPerformed(Object bin, Component owner);
  }


  public final ActionListener createListener(final RefactorItAction act,
      final Object obj) {
    return new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Object bin = ensureBinItem(obj);
        if (bin == null) {
          return;
        }

        if (RefactorItActionUtils.run(act, context, bin)) {
          act.updateEnvironment(context);
          refreshTree();
          refreshList();
        } else {
          act.raiseResultsPane(context);
        }
      }
    };
  }

  public final ActionListener createListener(final KeyboardAction act,
      final Object obj) {
    return new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        Object bin = ensureBinItem(obj);
        if (bin == null) {
          return;
        }

        act.actionPerformed(bin, owner);

        refreshTree();
        refreshList();
      }
    };
  }

  final void refreshAll() {
    ensureBinItem(new Object()); // "new Object()" is just a random input value, no reason...

    refreshTree();
    refreshList();
  }

  final Object ensureBinItem(Object obj) {
    lastSelection = list.getSelectedValue();

    // FIXME: rebuildProject & reference translation
    Object bin = obj;

    BinItemReference binRef = BinItemReference.create(bin);
    bin = null;

    if (ensureProject && IDEController.runningNetBeans()) {
      if (!IDEController.getInstance().ensureProject()) {
        return null; // Silent ignore
      }

      context = IDEController.getInstance().createProjectContext();
    }

    bin = binRef.restore(context.getProject());

    return bin;
  }

  final JPopupMenu createPopup(final Object obj) {
    JPopupMenu popup = new JPopupMenu();

    List actions = ModuleManager.getActions(obj);
    if (actions == null || actions.size() == 0) {
      return null;
    }

    for (int i = 0; i < actions.size(); i++) {
      final RefactorItAction act = (RefactorItAction) actions.get(i);

      JMenuItem item = new JMenuItem(act.getName());
      //item.setAccelerator(act.getKeyStroke());
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Object bin = obj;

          BinItemReference binRef = BinItemReference.create(bin);
          bin = null;

          // FIXME: works as long as only Netbeans can have modless TypeInfoPanel
          // Really a hack
          boolean ensureFailed = false;
          if (ensureProject && IDEController.runningNetBeans()) {
            if (!IDEController.getInstance().ensureProject()) {
              ensureFailed = true; // Silent ignore
            }

            context = IDEController.getInstance().createProjectContext();
          }

          bin = binRef.restore(context.getProject());
          if (ensureFailed || bin == null) {
            return;
          }

          if (RefactorItActionUtils.run(act, context, bin)) {
            act.updateEnvironment(context);
            refreshTree();
            refreshList();
          } else {
            act.raiseResultsPane(context);
          }
        }
      });

      popup.add(item);
    }

    // Return NULL if popup contains no items
    return (popup.getSubElements().length > 0) ? popup : null;
  }

  public final void setBinCIType(final BinCIType type, final RefactorItContext context) {
    this.context = context;
    setBinCIType(type);

    //minimizeTopComponent();
  }

//  private void minimizeTopComponent() {
//    int max = verticalSplit.getHeight() / 2;
//    int loc = (int)activeTree.getPreferredSize().getHeight() + 35;
//
//    if ( loc < max ) {
//      verticalSplit.setDividerLocation( loc );
//    }
//  }

  public final BinCIType getBinCIType() {
    return this.type;
  }

  public final void setBinCIType(final BinCIType type) {
    // set a tree model for supertypes tree
    if (type == null) {
      superClassesTree.setModel(InheritanceTreeModel.empty);
      subClassesTree.setModel(InheritanceTreeModel.empty);

      this.type = null;
      this.member = null;
      refreshList();

      return;
    }

    TreeModel model = new InheritanceTreeModel(type);
    superClassesTree.setModel(model);
    InheritanceTreeModel.expandTree(superClassesTree.getPathForRow(0),
        superClassesTree);
    superClassesTree.setSelectionRow(0);
    setTreeHierarhyModelListeners(model, superClassesTree);

    // set a tree model for subtypes tree
    TreeModel subClassesModel = new InheritanceTreeModel(new
        InheritanceSubTypeNode(null, type));
    subClassesTree.setModel(subClassesModel);
    InheritanceTreeModel.expandTree(subClassesTree.getPathForRow(0),
        subClassesTree);
    subClassesTree.setSelectionRow(0);
    setTreeHierarhyModelListeners(subClassesModel, subClassesTree);
    updateJavaDoc(type);
  }

  private void setTreeHierarhyModelListeners(final TreeModel model,
      final JTree tree) {
    model.addTreeModelListener(new TreeModelListener() {
      public void treeNodesInserted(TreeModelEvent e) {
        tree.expandPath(e.getTreePath());
      }

      public void treeNodesRemoved(TreeModelEvent e) {}

      public void treeNodesChanged(TreeModelEvent e) {}

      public void treeStructureChanged(TreeModelEvent e) {}
    });
  }

  final void refreshTree() {
    ((InheritanceTreeModel) activeTree.getModel()).setBinMember(member);
  }

  final void refreshList() {
    if (type == null) {
      head.setIcon(null);
      head.setText(null);
      list.setListData(empty);
      return;
    }

    NodeIcons icons = NodeIcons.getNodeIcons();

    int modifiers = NodeIcons.getModifiers(type);
    head.setIcon(type.isClass() || type.isEnum()
        ? icons.getClassIcon(modifiers)
        : icons.getInterfaceIcon(modifiers));

    head.setText(type.getName());

    Set fields = new TreeSet(MemberComparator.FIELD);
    Set methods = new TreeSet(MemberComparator.METHOD);

    if (showFields.isSelected()) {
      addFields(fields, type.getDeclaredFields());
    }

    if (showMethods.isSelected()) {
      if (type instanceof BinClass) {
        methods.addAll(
            Arrays.asList(((BinClass) type).getDeclaredConstructors()));
        addInitializer(methods, ((BinClass) type).getInitializers());
      }
      addMethods(methods, type.getDeclaredMethods());
    }

    if (showInherited.isSelected()) {
      addInherited(fields, methods, type);
    }

    List members = new ArrayList(fields.size() + methods.size());
    members.addAll(fields);
    members.addAll(methods);

    list.setListData(members.toArray());
    list.setSelectedValue(lastSelection, true);
  }

  private void addFields(Set set, BinField[] fields) {
    boolean hideStatic = !showStatic.isSelected();

    int len = fields.length;
    for (int i = 0; i < len; i++) {
      BinField field = fields[i];

      if (hideStatic && field.isStatic()) {
        continue;
      }

      if (TypeNode.filter(field, visibility)
          && !set.contains(field)) {
        set.add(field);
      }
    }
  }

  private void addMethods(Set set, BinMethod[] methods) {
    boolean hideStatic = !showStatic.isSelected();

    int len = methods.length;
    for (int i = 0; i < len; i++) {
      BinMethod method = methods[i];

      if (hideStatic && method.isStatic()) {
        continue;
      }

      if (TypeNode.filter(method, visibility)
          && !set.contains(method)) {
        set.add(method);
      }
    }
  }

  private void addInitializer(Set set, BinInitializer[] initializers) {
    boolean hideStatic = !showStatic.isSelected();

    int len = initializers.length;
    for (int i = 0; i < len; i++) {
      BinInitializer initializer = initializers[i];

      if (hideStatic && initializer.isStatic()) {
        continue;
      }

      if (TypeNode.filter(initializer, visibility)
          && !set.contains(initializer)) {
        set.add(initializer);
      }
    }
  }

  private void addInherited(Set fields, Set methods, BinCIType bin) {
    BinTypeRef ref = bin.getTypeRef().getSuperclass();

    if (ref != null) {
      BinCIType type = ref.getBinCIType();
      if (type != null) {
        if (showFields.isSelected()) {
          addFields(fields, type.getDeclaredFields());
        }

        if (showMethods.isSelected()) {
          // constructors is never inherited!
          methods.addAll(Arrays.asList(((BinClass) type).
              getDeclaredConstructors()));
          addMethods(methods, type.getDeclaredMethods());
        }

        addInherited(fields, methods, type);
      }
    }

    BinTypeRef[] refs = bin.getTypeRef().getInterfaces();
    if (refs != null) {
      // iterate through interfaces
      for (int pos = 0, max = (refs != null ? refs.length : 0); pos < max; pos++) {
        BinCIType type = refs[pos].getBinCIType();

        if (showFields.isSelected()) {
          addFields(fields, type.getDeclaredFields());
        }

        if (showMethods.isSelected()) {
          addMethods(methods, type.getDeclaredMethods());
        }

        addInherited(fields, methods, type);
      }
    }
  }

  public final void addLockListener(LockObserver observer) {
    if (!lockObservers.contains(observer)) {
      lockObservers.add(observer);
    }
  }

  public final boolean isNotLocked() {
    return!isLocked;
  }

  final void notifyLockObservers() {
    for (int i = 0, max = lockObservers.size(); i < max; i++) {
      ((LockObserver) lockObservers.get(i)).update(this);
    }
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
      refreshList();
    }
  }


  static final class CellRenderer extends DefaultListCellRenderer {
    public final Component getListCellRendererComponent(
        JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus
        ) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      BinMember member = (BinMember) value;
      if (value instanceof BinField) {
        setText(BinFormatter.format((BinField) member));
      } else if (value instanceof BinMethod) {
        setText(BinFormatter.format((BinMethod) member));
      } else if (value instanceof BinInitializer) {
        setText(member.getName());
      } else {
        new Exception("Unsupported item: " + value.getClass().getName())
            .printStackTrace(System.err);
      }

      NodeIcons icons = NodeIcons.getNodeIcons();

      if (value instanceof BinField) {
        setIcon(icons.getFieldIcon(NodeIcons.getModifiers(member)));
      } else if (value instanceof BinConstructor
          || value instanceof BinInitializer) {
        setIcon(icons.getConstructorIcon(NodeIcons.getModifiers(member)));
      } else {
        setIcon(icons.getMethodIcon(NodeIcons.getModifiers(member)));
      }

      setEnabled(list.isEnabled());
      setFont(list.getFont());

      return this;
    }
  }


  final void updateJavaDoc(BinMember member) {
    if ((javadocRenderer != null) && (member != null) && showJavadoc) {
      javadocRenderer.updateJavaDoc(member, context.getProject());
    }
  }

  public final void optionChanged(String key, String newValue) {
    if ("tree.font".equals(key)) {
      list.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
      subClassesTree.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
      superClassesTree.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
    } else if ("tree.background".equals(key)) {
      list.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
      subClassesTree.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
      superClassesTree.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
    } else if ("tree.foreground".equals(key)) {
      list.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
      subClassesTree.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
      superClassesTree.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
    } else if ("tree.selection.background".equals(key)) {
      list.setSelectionBackground(Color.decode(GlobalOptions.getOption("tree.selection.background")));
      subClassesTree.optionsChanged();
      superClassesTree.optionsChanged();
    } else if ("tree.selection.foreground".equals(key)) {
      list.setSelectionForeground(Color.decode(GlobalOptions.getOption("tree.selection.foreground")));
      subClassesTree.optionsChanged();
      superClassesTree.optionsChanged();
    }
  }

  public final void optionsChanged() {
    list.setFont(Font.decode(GlobalOptions.getOption("tree.font")));
    list.setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
    list.setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
    list.setSelectionBackground(Color.decode(GlobalOptions.getOption("tree.selection.background")));
    list.setSelectionForeground(Color.decode(GlobalOptions.getOption("tree.selection.foreground")));

    subClassesTree.optionsChanged();
    superClassesTree.optionsChanged();
  }
}
