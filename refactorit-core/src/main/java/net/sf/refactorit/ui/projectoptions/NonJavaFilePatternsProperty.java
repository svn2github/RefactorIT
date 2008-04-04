/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.checktree.JCheckTree;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.options.Options;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;


/**
 * @author Tanel Alumae
 */
public class NonJavaFilePatternsProperty extends CustomizableTextFieldProperty {
  // FIXME: resource from another package...
  static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(Options.class);

  static Map presetMap = new HashMap();

  static {
    presetMap.put("All xml files", new String[] {"*.xml"});
    presetMap.put("Web application", new String[] {"web.xml"});
    presetMap.put("Struts application",
        new String[] {"struts-config.xml", "validation.xml"});
    presetMap.put("JBoss application", new String[] {"jboss.xml"});
    presetMap.put("Hibernate", new String[] {"hibernate.cfg.xml", "*.hbm.xml"});
    presetMap.put("Apache Axis", new String[] {"*.wsdd"});
    presetMap.put("NetBeans forms", new String[] {"*.form"});
    presetMap.put("Jar manifest", new String[] {"MANIFEST.MF"});
  }

  public NonJavaFilePatternsProperty(String propertyName,
      PropertyPersistance persistance) {
    super(propertyName, persistance);
  }

  /**
   * Shows pattern editor dialog.
   */
  protected void buttonClicked() {
    PatternDialog dialog = new PatternDialog(
        IDEController.getInstance().createProjectContext(),
        "Select patterns", getTextField().getText());
    dialog.show();
    if (dialog.isOkPressed()) {
      getTextField().setText(dialog.getSelectedNodes());
    }
  }

  public String getText() {
    if (super.getText() == null) {
      super.setText("MANIFEST.MF, *.xml, *.form, *.wsdd");
    }
    return super.getText();
  }

  static class PatternDialog {
    final RitDialog dialog;

    boolean okPressed;

    final JCheckTree tree;

    private final CheckTreeNode root;
    private CheckTreeNode customNode;

    public PatternDialog(
        IdeWindowContext context, String title, String patterns
    ) {
      dialog = RitDialog.create(context);
      dialog.setTitle(title);

      //dialog.setSize(400, 400);

      ActionListener closeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };

      JButton okButton = new JButton(resLocalizedStrings.getString("button.ok"));
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          okPressed = true;
        }
      });
      okButton.addActionListener(closeListener);

      final JButton cancelButton = new JButton(resLocalizedStrings.getString(
          "button.cancel"));
      //cancelButton.addActionListener(cancelListener);
      cancelButton.addActionListener(closeListener);

      JButton helpButton = new JButton("Help");
      HelpViewer.attachHelpToDialog(dialog, helpButton,
          "getStart.refactoritOptions");

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      buttonPanel.add(helpButton);

      JPanel south = new JPanel();
      south.setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.weightx = 1.0;
      constraints.insets = new Insets(4, 10, 4, 10);
      south.add(buttonPanel, constraints);

      dialog.getContentPane().setLayout(new BorderLayout());

      JPanel center = new JPanel(new BorderLayout());
      center.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 3, 3, 3),
          BorderFactory.createEtchedBorder()));

      center.add(DialogManager.getHelpPanel(
          "Select non-java file patterns that contain metadata about the application"
          ), BorderLayout.NORTH);
      //JPanel chooserPane = new JPanel(new BorderLayout());
      //chooserPane.add(new JTreeTable(new NonJavaPatternsModel(),
      //    JTreeTable.CHECKBOX_STYLE), BorderLayout.CENTER);

      root = new CheckTreeNode("root");
      for (Iterator i = new TreeMap(presetMap).keySet().iterator(); i.hasNext(); ) {
        String key = (String) i.next();
        CheckTreeNode cat = new CheckTreeNode(key);
        cat.setShowCheckBox(false);
        String[] nodes = (String[]) presetMap.get(key);
        for (int i2 = 0; i2 < nodes.length; i2++) {
          cat.add(new CheckTreeNode(nodes[i2]));
        }
        root.add(cat);
      }

      final DefaultTreeModel treeModel = new DefaultTreeModel(root);

      tree = new JCheckTree(treeModel);
      tree.setEditable(true);
      tree.getSelectionModel().setSelectionMode
          (TreeSelectionModel.SINGLE_TREE_SELECTION);

      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      
      tree.setBackground(BinTree.getBackgroundProperty());
      tree.setForeground(BinTree.getForegroundProperty());
      
      JScrollPane scrollPane = new JScrollPane(tree);
      
      center.add(scrollPane, BorderLayout.CENTER);
      selectNodes(patterns);

      JPanel customizePanel = new JPanel();
      customizePanel.setLayout(new BorderLayout());
      JButton addButton = new JButton("Add..");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String pattern = getPattern();
          if ((pattern != null) && (pattern.trim().length() > 0)) {
            CheckTreeNode childNode = addCustomPattern(pattern);
            //Make sure the user can see the lovely new node.
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
          }
        }
      });

      customizePanel.add(addButton, BorderLayout.NORTH);
      center.add(customizePanel, BorderLayout.EAST);

      dialog.getContentPane().add(center, BorderLayout.CENTER);
      dialog.getContentPane().add(south, BorderLayout.SOUTH);
      
      SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton,
          helpButton, closeListener);
    }

    public void selectNodes(String str) {
      WildcardPattern[] patterns = WildcardPattern.stringToArray(str);
      for (int i = 0; i < patterns.length; i++) {
        String pattern = patterns[i].getPattern();
        CheckTreeNode node = findPatternNode(pattern);
        if (node == null) {
          node = addCustomPattern(pattern);
        }
        node.setSelected(true);
        tree.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }

    private CheckTreeNode findPatternNode(String pattern) {
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      CheckTreeNode root = (CheckTreeNode) model.getRoot();
      for (int i = 0; i < root.getChildCount(); i++) {
        CheckTreeNode cat = (CheckTreeNode) root.getChildAt(i);
        for (int j = 0; j < cat.getChildCount(); j++) {
          CheckTreeNode patternNode = (CheckTreeNode) cat.getChildAt(j);
          if (patternNode.getUserObject().equals(pattern)) {
            return patternNode;
          }
        }
      }
      return null;
    }

    CheckTreeNode addCustomPattern(final String pattern) {
      CheckTreeNode childNode =
          new CheckTreeNode(pattern.trim());
      childNode.setShowCheckBox(true);
      childNode.setSelected(true);
      CheckTreeNode customNode = getCustomParentNode();
      ((DefaultTreeModel) tree.getModel()).insertNodeInto(childNode, customNode,
          customNode.getChildCount());

      return childNode;
    }

    private CheckTreeNode getCustomParentNode() {
      if (customNode == null) {
        customNode = new CheckTreeNode("Custom patterns");
        customNode.setShowCheckBox(false);
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(customNode, root,
            root.getChildCount());
      }
      return customNode;
    }

    /**
     * Asks user for a new custom pattern.
     *
     * @return pattern, or null, if dialog was cancelled
     */
    String getPattern() {
      NewPatternDialog pd = new NewPatternDialog(dialog.getContext());
      pd.show();
      return pd.getPattern();
    }

    public boolean isOkPressed() {
      return this.okPressed;
    }

    public String getSelectedNodes() {
      ArrayList result = new ArrayList(5);
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      CheckTreeNode root = (CheckTreeNode) model.getRoot();
      for (int i = 0; i < root.getChildCount(); i++) {
        CheckTreeNode cat = (CheckTreeNode) root.getChildAt(i);
        for (int j = 0; j < cat.getChildCount(); j++) {
          CheckTreeNode patternNode = (CheckTreeNode) cat.getChildAt(j);
          if (patternNode.isSelected()) {
            result.add(patternNode.getUserObject().toString());
          }
        }
      }
      return StringUtil.join(result, ", ");
    }

    public void show() {
      dialog.show();
    }
  }


  static class NewPatternDialog {
    final RitDialog dialog;

    String pattern;

    public NewPatternDialog(IdeWindowContext context) {
      dialog = RitDialog.create(context);
      dialog.setTitle("New pattern");

//      dialog.setSize(300, 150);

      ActionListener closeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };

      final JButton okButton = new JButton(resLocalizedStrings.getString(
          "button.ok"));
      okButton.setEnabled(false);
      okButton.addActionListener(closeListener);

      final JButton cancelButton = new JButton(resLocalizedStrings.getString(
          "button.cancel"));
      //cancelButton.addActionListener(cancelListener);
      cancelButton.addActionListener(closeListener);
      
      SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
          closeListener);

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new GridLayout(1, 3, 10, 0));
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);

      JPanel south = new JPanel();
      south.setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.weightx = 1.0;
      constraints.insets = new Insets(4, 10, 4, 10);
      south.add(buttonPanel, constraints);

      dialog.getContentPane().setLayout(new BorderLayout());

      JPanel center = new JPanel(new GridBagLayout());
      center.setBorder(
          BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 3, 3, 3),
          BorderFactory.createEtchedBorder())
          );

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);

      constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.anchor = GridBagConstraints.NORTH;
      constraints.insets = new Insets(0, 0, 0, 0);
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.gridwidth = 2;
      center.add(DialogManager.getHelpPanel(
          "Specify new non-java file pattern (e.g *.ext)"
          ), constraints);

      // Attach label(s)
      {
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.ipadx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        gbc.gridx = 0;
        gbc.gridy = 1;
        center.add(new JLabel("Enter pattern:", JLabel.RIGHT), gbc);
      }

      // Attach textfield(s)
      final JTextField patternField = new JTextField();
      patternField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          updateOkButton(e);
        }

        public void removeUpdate(DocumentEvent e) {
          updateOkButton(e);
        }

        public void changedUpdate(DocumentEvent e) {
          updateOkButton(e);
        }

        private void updateOkButton(DocumentEvent e) {
          Document document = e.getDocument();
          // Adjust button
          try {
            String newName = document.getText(0, document.getLength());
            okButton.setEnabled(
                (newName.trim().length() > 0)
                && (newName.indexOf(' ') == -1) && (newName.indexOf(',') == -1));
          } catch (BadLocationException ble) {
          }

        }
      });
      {
        gbc.weightx = 1;
        gbc.ipadx = 4;
        //gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 1;
        gbc.gridy = 1;
        center.add(patternField, gbc);
      }
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pattern = patternField.getText();
        }
      });

      dialog.getContentPane().add(center, BorderLayout.CENTER);
      dialog.getContentPane().add(south, BorderLayout.SOUTH);
    }

    public void show() {
      dialog.show();
    }

    public String getPattern() {
      return this.pattern;
    }
  }
}
