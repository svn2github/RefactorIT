/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.panel.ResultsTreeDisplayState;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.JTreeTable;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;



/**
 * @author Vlad Vislogubov
 * @author Anton Safonov
 */
public class JConfirmationDialog {
  private final RitDialog dialog;

  private final JButton buttonExpandAll = new JButton(ResourceUtil.getIcon(
      UIResources.class, "ExpandAll.gif"));
  private final JButton buttonCollapseAll = new JButton(ResourceUtil.getIcon(
      UIResources.class, "CollapseAll.gif"));

  private final JButton buttonSelectAll = new JButton("Select All");
  private final JButton buttonDeselectAll = new JButton("Deselect All");

  private final JButton buttonOk = new JButton("Ok");
  private final JButton buttonCancel = new JButton("Cancel");
  private final JButton helpButton = new JButton("Help");

  private final Box leftButtonPanel = Box.createHorizontalBox();
  private final Box rightButtonPanel = Box.createHorizontalBox();

  private BinTreeTableModel model;
  private BinTreeTable table;

  private boolean isOkPressed;
  private final boolean expandAll;

  protected String help;

  private String overridenDesc;

  private String helpId;

  private final RefactorItContext context;

  private static final int BUTTONS_GAP = 4;
  private static final int GROUPS_GAP = 12;

  public JConfirmationDialog(
      String title, String help,
      BinTreeTableModel model, RefactorItContext context,
      String description, String helpId) {
    this(title, help, model, context, description, helpId, true);
  }

  public JConfirmationDialog(
      String title, String help,
      BinTreeTableModel model, RefactorItContext context,
      String description, String helpId, boolean expandAll) {
    this(title, help, model, context,
        description, helpId, new Dimension(676, 530), expandAll);
    Dimension d = dialog.getMaximumSize();
    dialog.setSize(d.width - 50, d.height - 150);
  }

  public JConfirmationDialog(
      String title, String help,
      BinTreeTableModel model, RefactorItContext context,
      String description, String helpId, Dimension dim, boolean expandAll) {

    setDescription(description);
    this.context = context;
    this.expandAll = expandAll;

    this.help = help;
    this.model = model;
    this.helpId = helpId;

    dialog = RitDialog.create(context);

    table = new BinTreeTable(model, JTreeTable.CHECKBOX_STYLE, context);
    table.setListenForEnterKey(false);

    JPanel contentPane = createMainPanel();

    contentPane.setPreferredSize(dim);

    dialog.setTitle(title);
    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, helpButton, this.helpId);
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel,
        helpButton);
  }

  public JConfirmationDialog(
      String title, String help,
      BinTreeTable table, RefactorItContext context,
      String description, String helpId, Dimension dim, boolean expandAll) {
    setDescription(description);
    this.context = context;
    this.expandAll = expandAll;

    this.help = help;
    this.model = table.getBinTreeTableModel();
    this.helpId = helpId;

    dialog = RitDialog.create(context);

    this.table =table;
    this.table.setListenForEnterKey(false);

    JPanel contentPane = createMainPanel();

    contentPane.setPreferredSize(dim);

    dialog.setTitle(title);
    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, helpButton, this.helpId);
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel,
        helpButton);
  }

  protected JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(help);
  }

  public void show() {
    dialog.show();
  }

  public void setDescription(String newDesc) {
    overridenDesc = newDesc;
  }

  private JPanel createMainPanel() {
    JPanel main = new JPanel(new BorderLayout());

    JPanel center = new JPanel(new GridBagLayout());
    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder()));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridwidth = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(0, 0, 0, 0);
    center.add(createMessagePanel(), constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 5, 5, 5);

    String desc = overridenDesc;

    StringBuffer buf = new StringBuffer();

    if(desc != null) {
      buf.append(desc);
    }

    if(buf.length() > 0) {
      JTextPane discrPane = new JTextPane();
      discrPane.setEditable(false);
      discrPane.setBackground(center.getBackground());
      discrPane.setForeground(Color.black);
      center.add(discrPane, constraints);
      discrPane.setText(new String(buf));
      discrPane.setFocusable(false);
    }

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weighty = 1.0;
    constraints.weightx = 1.0;
    constraints.insets = new Insets(10, 5, 5, 5);
    center.add(getScrollPane(), constraints);

    main.add(center, BorderLayout.CENTER);
    main.add(createButtonPanel(), BorderLayout.SOUTH);

    return main;
  }

  private JScrollPane getScrollPane() {
    JScrollPane jScrollPane = new JScrollPane(table);

    if (model.getColumnCount() == 1) {
      table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    } else {
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//		table.getColumnModel().getColumn(1).setMinWidth(5);
//		table.getColumnModel().getColumn(1).setPreferredWidth(50);
//		table.getColumnModel().getColumn(1).setMaxWidth(100);

      TableColumnModel model = table.getColumnModel();

      model.getColumn(0).setMinWidth(100);
      model.getColumn(0).setPreferredWidth(300);

      if (model.getColumnCount() > 1) {

        model.getColumn(1).setMinWidth(50);
        model.getColumn(1).setPreferredWidth(50);
        model.getColumn(1).setMaxWidth(100);
      }

      // to set needed size
      Dimension d = dialog.getMaximumSize();

      if (model.getColumnCount() > 2) {
        model.getColumn(2).setMinWidth(100);
        model.getColumn(2).setPreferredWidth(d.width-400-50);
      }

      int maxPixel = getMaxPixelCount();

      if (model.getColumnCount() > 2
          && maxPixel > model.getColumn(2).getPreferredWidth()) {
        model.getColumn(2).setPreferredWidth(maxPixel);
      }
    }

    if (expandAll) {
      table.expandAll();
    } else {
      table.smartExpand();
    }

    table.setRowSelectionInterval(0, 0);


    return jScrollPane;
    /*
       DefaultTreeModel model = new DefaultTreeModel( root );
       root.setModel( model );
       JTree tree = new JTree( model );
       tree.setCellEditor( new ConfirmationCellEditor() );
       tree.setCellRenderer( new ConfirmationCellRenderer() );
       tree.setEditable( true );
       tree.setRootVisible( false );
       tree.putClientProperty("JTree.lineStyle", "Angled");
       tree.setShowsRootHandles(true);
       return new JScrollPane( tree );
     */
  }

  private int getMaxPixelCount() {
    String rawLine;
    String maxRawLine = "";
    int maxLength = 0;
    int maxPixel = 0;

    List list = new ArrayList(model.getAllChildrenRecursively());
    for (int i = 0, max = list.size(); i < max; i++) {
      rawLine = ((ParentTreeTableNode) list.get(i)).getLineSource();

      rawLine = StringUtil.removeHtml(StringUtil.replace(StringUtil.replace(
          rawLine, "<br>", "\n"), "<BR>", "\n"));

      if (rawLine != null) {

        int length = 0;
        int start = 0;
        int end = 0;

        if (rawLine.indexOf("\n") == -1) {
          if (maxLength < rawLine.length()) {
            maxLength = rawLine.length();
            maxRawLine = rawLine;
          }

        } else {
          while ((end = rawLine.indexOf("\n", start)) != -1) {
            length = end - start;

            if (maxLength < length) {
              maxLength = length;
              maxRawLine = rawLine.substring(start, end);
            }
            start = end + 1;
            if (start == rawLine.length()) {
              break;
            }
          }
        }

      }
    }

    if (maxRawLine.length() > 0) {
      Font oldF = Font.decode(GlobalOptions.getOption("tree.font"));
// FIXME: the size of text in confirmation window is measured in pixels (10px).
// Don't know how to convert pixels into points
      Font newF = new Font(oldF.getName(), oldF.getStyle(), oldF.getSize() + 2);
      maxPixel = new JPanel().getFontMetrics(newF).stringWidth(maxRawLine);
    }

//		System.out.println("pixels:" + maxPixel);//innnnnnnn
//		System.out.println("font1:" + getFont().toString());//innnnnnnn
//		System.out.println("font2:" + new JScrollPane().getFont().toString());//innnnnnnn

    return maxPixel;
  }

  private JPanel createButtonPanel() {
    buttonOk.setSelected(true);

    buttonExpandAll.setDefaultCapable(false);
    buttonExpandAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        table.expandAll();
      }
    });
    leftButtonPanel.add(buttonExpandAll);
    leftButtonPanel.add(Box.createHorizontalStrut(BUTTONS_GAP));

    buttonCollapseAll.setDefaultCapable(false);
    buttonCollapseAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        table.collapseAll();
      }
    });
    leftButtonPanel.add(buttonCollapseAll);
    leftButtonPanel.add(Box.createHorizontalStrut(GROUPS_GAP));

    buttonSelectAll.setDefaultCapable(false);
    buttonSelectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ResultsTreeDisplayState resultsTreeDisplayState
            = new ResultsTreeDisplayState();
        resultsTreeDisplayState.saveExpansionAndScrollState(table.getTree());

        model.selectAll();

        resultsTreeDisplayState.restoreExpansionAndScrollState(
            table.getTree(), context.getProject());
      }
    });
    leftButtonPanel.add(buttonSelectAll);
    leftButtonPanel.add(Box.createHorizontalStrut(BUTTONS_GAP));

    buttonDeselectAll.setDefaultCapable(false);
    buttonDeselectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ResultsTreeDisplayState resultsTreeDisplayState
            = new ResultsTreeDisplayState();
        resultsTreeDisplayState.saveExpansionAndScrollState(table.getTree());

        model.deselectAll();

        resultsTreeDisplayState.restoreExpansionAndScrollState(
            table.getTree(), context.getProject());
      }
    });
    leftButtonPanel.add(buttonDeselectAll);
    leftButtonPanel.add(Box.createHorizontalStrut(GROUPS_GAP));

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = true;
        dialog.dispose();
      }
    });
    rightButtonPanel.add(buttonOk);
    rightButtonPanel.add(Box.createHorizontalStrut(BUTTONS_GAP));

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    rightButtonPanel.add(buttonCancel);
    rightButtonPanel.add(Box.createHorizontalStrut(GROUPS_GAP));

    rightButtonPanel.add(helpButton);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    // right side
    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(rightButtonPanel, constraints);

    // left side
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 20, 3, 0);
    downPanel.add(leftButtonPanel, constraints);

    return downPanel;
  }

  public void addLeftButton(JButton button) {
    leftButtonPanel.add(button);
    leftButtonPanel.add(Box.createHorizontalStrut(BUTTONS_GAP));
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public BinTreeTableModel getModel() {
    return model;
  }

  public BinTreeTable getTable() {
    return table;
  }

  public IdeWindowContext getContext() {
    return context;
  }
}
