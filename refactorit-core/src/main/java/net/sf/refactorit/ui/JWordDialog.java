/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;



import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Shows given words in a list. User can add/remove them.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public class JWordDialog {
  static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JWordDialog.class);

  final RitDialog dialog;

  JTextField word = new JTextField();
  JList list = new JList();

  JButton add = new JButton(resLocalizedStrings.getString("button.add.short"));
  JButton remove = new JButton(resLocalizedStrings.getString("button.remove"));
  JCheckBox regExpCheck = new JCheckBox("Add As Regexp", true);

  boolean okPressed;

  List words;

  private JButton buttonHelp = new JButton("Help");
  private JButton ok = new JButton(resLocalizedStrings.getString("button.ok"));
  private JButton cancel = new JButton(
      resLocalizedStrings.getString("button.cancel"));
  private JButton selectAll = new JButton("Check all");
  private JButton deselectAll = new JButton("Uncheck all");

  private String helpTopic;

  private final ActionListener cancelListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      okPressed = false;
      dialog.dispose();
    }
  };

  public JWordDialog(
      IdeWindowContext context, String title, List words,
      boolean regularExpressionSupport, String helpTopicId
  ) {
    this(context, title, words, regularExpressionSupport, null, helpTopicId);
  }

  public JWordDialog(
      final IdeWindowContext context, String title, List words,
      final boolean regularExpressionSupport,
      List defaultValues, String helpTopicId
  ) {
    this.words = words;
    this.helpTopic = helpTopicId;

    JPanel contentPane =
        createMainPanel(regularExpressionSupport, defaultValues);
    contentPane.setPreferredSize(new Dimension(495, 330));

    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setContentPane(contentPane);
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, this.helpTopic);
    SwingUtil.initCommonDialogKeystrokes(dialog, ok, cancel, buttonHelp,
        cancelListener);

    word.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {}

      public void insertUpdate(DocumentEvent e) {
        if (word.getText().length() > 0) {
          add.setEnabled(true);
        } else {
          add.setEnabled(false);
        }
      }

      public void removeUpdate(DocumentEvent e) {
        if (word.getText().length() > 0) {
          add.setEnabled(true);
        } else {
          add.setEnabled(false);
        }
      }
    });

    add.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Word newWord;
        try {
          newWord = new Word(word.getText(),
              regularExpressionSupport && regExpCheck.isSelected());
        } catch (Word.BadFormatException exxx) {
          RitDialog.showMessageDialog(
              context, resLocalizedStrings.getString("bad.word.format"));
          return;
        }

        ((DefaultListModel) list.getModel()).addElement(newWord);
        word.setText("");
      }
    });

    list.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        remove.setEnabled(!list.isSelectionEmpty());
      }
    });

    remove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (list.isSelectionEmpty()) {
          return;
        }

        int index = list.getSelectedIndex();
        ((DefaultListModel) list.getModel()).remove(index);

        remove.setEnabled(false);
      }
    });

    selectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
          Word aWord = (Word) list.getModel().getElementAt(i);
          aWord.isSelected = true;
        }

        dialog.getRootPane().repaint();
      }
    });

    deselectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < list.getModel().getSize(); i++) {
          Word aWord = (Word) list.getModel().getElementAt(i);
          aWord.isSelected = false;
        }

        dialog.getRootPane().repaint();
      }
    });

    DefaultListModel model = new DefaultListModel();
    copyListIntoModel(words, model);

    list.setModel(model);

    list.getSelectionModel().setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);

    if (regularExpressionSupport) {
      MouseListener mouseListener = new MouseAdapter() {
        private int lastClickIndex = -1;

        private int minimumCheckboxWidth = 0;

        {
          JCheckBox c = new JCheckBox();
          c.setBounds(0, 0, 0, 0);
          c.setBorder(BorderFactory.createEmptyBorder());
          minimumCheckboxWidth = c.getMinimumSize().width;
        }

        public void mouseClicked(MouseEvent e) {
          int index = list.locationToIndex(e.getPoint());

          if (index == lastClickIndex || e.getX() <= minimumCheckboxWidth) {
            Word selectedWord = (Word) list.getModel().getElementAt(index);
            selectedWord.isSelected = !selectedWord.isSelected;
            list.repaint();
          }
          lastClickIndex = index;
        }
      };
      list.addMouseListener(mouseListener);
      list.setCellRenderer(new CheckBoxCellRenderer());
    }
  }

  public boolean okPressed() {
    return okPressed;
  }

  void copyListIntoModel(List list, DefaultListModel model) {
    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      model.addElement(iter.next());
    }
    add.setEnabled(false);
    remove.setEnabled(false);
  }

  private JPanel createMainPanel(boolean regularExpressionSupport,
      final List defaultValues) {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 0, 3),
        BorderFactory.createEtchedBorder())
        );

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridwidth = 2;
    constraints.insets = new Insets(0, 0, 0, 0);
    mainPanel.add(DialogManager.getHelpPanel("Enter word(s) to scan for"),
        constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridwidth = 1;
    constraints.insets = new Insets(5, 5, 0, 5);
    mainPanel.add(word, constraints);

    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 0, 0, 5);
    mainPanel.add(add, constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(5, 5, 5, 5);
    mainPanel.add(new JScrollPane(list), constraints);

    constraints.gridx = 2;
    constraints.gridy = 3;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weighty = 0.0;
    constraints.weightx = 0.0;
    constraints.insets = new Insets(5, 0, 5, 5);
    mainPanel.add(createXPanel(regularExpressionSupport, defaultValues),
        constraints);

    JPanel central = new JPanel(new BorderLayout());
    central.add(mainPanel, BorderLayout.CENTER);
    central.add(createButtonPanel(regularExpressionSupport, defaultValues),
        BorderLayout.SOUTH);

    return central;
  }

  private JPanel createXPanel(final boolean regularExpressionSupport,
      final List defaultValues) {
    List componentList = new ArrayList();

    if (regularExpressionSupport) {
      /*constraints.gridx = 2;
             constraints.gridy = 0;
             constraints.fill = GridBagConstraints.NONE;
             constraints.anchor = GridBagConstraints.WEST;
             constraints.weightx = 1.0;
             constraints.weighty = 0.0;
             constraints.insets = new Insets( 5, 5, 0, 5 );
             mainPanel.add( regExpCheck, constraints );*/
      componentList.add(regExpCheck);
    }

    componentList.add(remove);

    if (regularExpressionSupport) {
      componentList.add(selectAll);
      componentList.add(deselectAll);
    }

    if (defaultValues != null) {
      JButton restoreDefaults = new JButton("Restore defaults");

      restoreDefaults.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          DefaultListModel model = (DefaultListModel) list.getModel();
          model.clear();
          copyListIntoModel(defaultValues, model);
        }
      });

      componentList.add(restoreDefaults);
    }

    JComponent[] components = (JComponent[]) componentList.toArray(new
        JComponent[componentList.size()]);
    return SwingUtil.combineInNorth(components);
  }

  private JPanel createButtonPanel(
      final boolean regularExpressionSupport, final List defaultValues
      ) {
    JPanel buttonPanel = new JPanel();

    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    ok.setSelected(true);

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int size = list.getModel().getSize();
        words = new ArrayList();

        for (int i = 0; i < size; i++) {
          Object resultElement = (regularExpressionSupport)
              ? list.getModel().getElementAt(i)
              : list.getModel().getElementAt(i).toString();

          words.add(resultElement);
        }

        okPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(ok);

    cancel.addActionListener(cancelListener);
    buttonPanel.add(cancel);

    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(5, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);
    ok.setNextFocusableComponent(cancel);
    cancel.setNextFocusableComponent(word);
    return downPanel;
  }

  public void show() {
    dialog.show();
  }

  public List getWords() {
    return words;
  }

  public static class Word {
    private static final String linePrefix = "(line prefix) ";

    public String word;
    public boolean isRegularExpression;
    public boolean isSelected = true;

    public Word(String word, boolean regularExpression)
    throws BadFormatException {
      this.word = word;
      this.isRegularExpression = regularExpression;

      if (this.isRegularExpression) {
        try {
          Pattern.compile(word);
        } catch (PatternSyntaxException e) {
          throw new BadFormatException();
        }
      }
    }

    /** Not a regexp */
    public Word(String word) {
      this.word = word;
      this.isRegularExpression = true;
    }

    public String toString() {
      if (this.isRegularExpression) {
        return this.word;
      } else {
        return linePrefix + this.word;
      }
    }

    public static void convertStringsToWordInstances(List list) {
      for (int i = 0; i < list.size(); i++) {
        list.set(i, convertToWordInstance(list.get(i)));
      }
    }

    private static Word convertToWordInstance(Object o) {
      if (o instanceof String) {
        try {
          return new JWordDialog.Word((String) o, false);
        } catch (JWordDialog.Word.BadFormatException e) {
          throw new RuntimeException("This should never happen");
        }
      } else if (o instanceof Word) {
        return (Word) o;
      } else {
        throw new IllegalArgumentException(
            "Should have been an instance of String or Word");
      }
    }

    public static class BadFormatException extends Exception {}
  }


  private static class CheckBoxCellRenderer implements ListCellRenderer {

    /** Return a component that has been configured to display the specified
     * value. That component's <code>paint</code> method is then called to
     * "render" the cell.  If it is necessary to compute the dimensions
     * of a list because the list cells do not have a fixed size, this method
     * is called to generate a component on which <code>getPreferredSize</code>
     * can be invoked.
     *
     * @param list The JList we're painting.
     * @param value The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     *
     * @see JList
     * @see ListSelectionModel
     * @see ListModel
     *
     */
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      JCheckBox result = new JCheckBox(value.toString());
      result.setSelected(true);

      if (isSelected) {
        result.setBackground(list.getSelectionBackground());
        result.setForeground(list.getSelectionForeground());
      } else {
        result.setBackground(list.getBackground());
        result.setForeground(list.getForeground());
      }
      result.setEnabled(list.isEnabled());
      result.setSelected(((Word) value).isSelected);

      return result;
    }
  }
}
