/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.notused;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.checktree.JCheckTree;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 *
 * @author Tanel Alumae
 * @author Igor Malinin
 */
public class FilterPanel extends JPanel {
  static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(NotUsedModule.class);

  private static final String FILTER_OPTION_PREFIX = "exclude.filter.";

  JEditorPane descriptionBox;

  CardLayout optionsLayout;
  JPanel optionsPanel;
  JCheckTree tree;

  private Map propertyEditors = new HashMap();

  public FilterPanel() {
    super(new BorderLayout());

    JPanel right = new JPanel(new GridLayout(2, 1));
    right.add(createDescriptionPanel());
    right.add(createOptionsPanel());

    JPanel center = new JPanel(new GridLayout(1, 2));
    center.add(createRulesPanel());
    center.add(right);

    add(center);
  }

  public ExcludeFilterRule[] getSelectedRules() {
    List list = new ArrayList();
    addSelectedRules((CheckTreeNode) tree.getModel().getRoot(), list);
    return (ExcludeFilterRule[]) list.toArray(new ExcludeFilterRule[list.size()]);
  }

  private void addSelectedRules(CheckTreeNode node, List list) {
    if (node.isLeaf()) {
      ExcludeFilterRule rule = (ExcludeFilterRule) node.getUserObject();
      if (node.isSelected()) {
        list.add(rule);
      }
      JComponent propertyEditor = (JComponent) propertyEditors.get(rule.getKey());
      if (propertyEditor != null) {
        rule.setProperties(propertyEditor);
      }
      GlobalOptions.setOption(FILTER_OPTION_PREFIX + rule.getKey(), "" + node.isSelected());

      return;
    }

    int length = node.getChildCount();
    for (int i = 0; i < length; i++) {
      addSelectedRules((CheckTreeNode) node.getChildAt(i), list);
    }
  }

  private JComponent createRulesPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Exclude Rules"));

    tree = new JCheckTree(createTreeNodes());
    tree.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
//    tree.setRootVisible(false);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        // reset to empty
        descriptionBox.setText("");
        optionsLayout.show(optionsPanel, "");

        TreePath path = tree.getSelectionPath();
        if (path != null) {
          Object obj = path.getLastPathComponent();
          if (obj instanceof CheckTreeNode) {
            CheckTreeNode node = (CheckTreeNode) obj;
            if (node.isLeaf()) {
              ExcludeFilterRule rule = (ExcludeFilterRule) node.getUserObject();

              // set specific values if available
              descriptionBox.setText(rule.getDescription());
              optionsLayout.show(optionsPanel, rule.getKey());
            }
          }
        }
      }
    });

    panel.add(new JScrollPane(tree));

    return panel;
  }

  private CheckTreeNode createTreeNodes() {
    CheckTreeNode root = new CheckTreeNode("Exclude Rules", true);
    for (int i = 0; i < ExcludeFilterRule.ALL_RULES.length; i++) {
      addRuleNode(root, ExcludeFilterRule.ALL_RULES[i]);

    }

    return root;
  }

  private void addRuleNode(CheckTreeNode parent, ExcludeFilterRule rule) {
    CheckTreeNode node = new CheckTreeNode(rule);
    final String option = GlobalOptions.getOption(FILTER_OPTION_PREFIX + rule.getKey(), "" + rule.isDefaultChecked());
    boolean defaultValue = Boolean.valueOf(option).booleanValue();
    node.setSelected(defaultValue);
    parent.add(node);
    JComponent editor = rule.getPropertyEditor();
    if (editor != null) {
      optionsPanel.add(editor, rule.getKey());
      propertyEditors.put(rule.getKey(), editor);
    }
  }

  private JComponent createDescriptionPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Description"));

    descriptionBox = new JEditorPane();
    descriptionBox.setEditorKitForContentType("text/html", new HTMLEditorKit());
    descriptionBox.setContentType("text/html");
    descriptionBox.setEditable(false);

    HTMLDocument document = (HTMLDocument) descriptionBox.getDocument();
    document.setPreservesUnknownTags(false);

    panel.add(new JScrollPane(descriptionBox));

    return panel;
  }

  private JComponent createOptionsPanel() {
    optionsLayout = new CardLayout();

    optionsPanel = new JPanel(optionsLayout);
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

    // default card when nothing selected in the tree
    optionsPanel.add(new JPanel(), "");

    return optionsPanel;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle("FilterPanel - Test");
    frame.setSize(600, 400);

    frame.getContentPane().add(new FilterPanel());
    frame.show();
  }
}
