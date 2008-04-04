/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;



import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.refactorings.changesignature.ExistingParameterInfo;
import net.sf.refactorit.refactorings.changesignature.MethodSignatureChange;
import net.sf.refactorit.refactorings.changesignature.NewParameterInfo;
import net.sf.refactorit.refactorings.changesignature.ParameterInfo;
import net.sf.refactorit.refactorings.changesignature.analyzer.MethodsInvocationsMap;
import net.sf.refactorit.refactorings.changesignature.analyzer.RecursiveAddParameterModel;
import net.sf.refactorit.refactorings.changesignature.analyzer.RecursiveDeleteParameterModel;
import net.sf.refactorit.refactorings.common.Permutation;
import net.sf.refactorit.source.LocationlessSourceParsingException;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.TypeChooser;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.JWarningDialog;
import net.sf.refactorit.ui.module.SettingsDialog;
import net.sf.refactorit.ui.table.BinTable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;


/**
 * @author Igor Malinin
 */
public final class ChangeMethodSignaturePanel extends JPanel {
  public static final int OK = 0;
  public static final int DONT_SHOW_AGAIN = 1;

  public static final String SHOW_WARNING_KEY = "change.method.signature.warn.functionality.changes";

  private JButton upButton;
  private JButton downButton;

  private boolean alreadyWarnedInThisDialog = false;

  /**
   * Delegates state to <code>MethodSignatureChange</code>
   */
  final class ModifierButtonModel extends JToggleButton.ToggleButtonModel {
    private final int modifier;

    ModifierButtonModel(int modifier) {
      this.modifier = modifier;
    }

    public void setGroup(ButtonGroup group) {
      super.setGroup(group);

      if (change != null && change.getAccessModifier() == modifier) {
        group.setSelected(this, true);
      }
    }

    public void setSelected(boolean selected) {
      if (selected && change != null) {
        selected = change.setAccessModifier(modifier);
      }

      super.setSelected(selected);

      if (selected) {
        updatePreview();
      }
    }
  }

  /**
   * Delegates state to <code>MethodSignatureChange</code>
   */
  final class ParametersTableModel extends AbstractTableModel {
    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "Type";
        case 1:
          return "Name";
        case 2:
          return "Default";
      }

      throw new IndexOutOfBoundsException("column: " + column);
    }

    public int getRowCount() {
      if (change == null) {
        return 0; // debug mode
      }

      return change.getParametersCount();
    }

    public Object getValueAt(int row, int column) {
      ParameterInfo info = getParameterInfo(row);

      switch (column) {
        case 0:
          return BinFormatter.formatQualified(info.getType());

        case 1:
          return info.getName();

        case 2:
          if (info instanceof NewParameterInfo) {
            return ((NewParameterInfo) info).getDefaultValue();
          }
          return "";
      }

      throw new IndexOutOfBoundsException("column: " + column);
    }

    public void setValueAt(Object value, int row, int column) {
      ParameterInfo info = getParameterInfo(row);

      switch (column) {
        case 0:
          info.setType(getTypeRef((String) value));
          break;

        case 1:
          info.setName((String) value);
          break;

        case 2: {
          if (info instanceof NewParameterInfo) {
            ((NewParameterInfo) info).setDefaultValue((String) value);
          }
          break;
        }
      }

      updatePreview();
    }

    public boolean isCellEditable(int row, int column) {
      ParameterInfo info = getParameterInfo(row);

      switch (column) {
        case 0:
        case 1:
          return true;

        case 2: {
          if (info instanceof NewParameterInfo) {
            return true;
          }
        }
      }

      return false;
    }

    private BinTypeRef getTypeRef(String name) {
      String element = name;

      int dimension = 0;
      while (element.endsWith("[]")) {
        element = element.substring(0, element.length() - 2);
        ++dimension;
      }

      BinMethod method = change.getMethod();
      Project project = method.getProject();

      BinTypeRef type = null;

      try {
        // local resolve
        type = method.getOwner().getResolver().resolve(element);
      } catch (LocationlessSourceParsingException e) {
      } catch (SourceParsingException e) {}

      if (type == null) {
        // global resolve
        type = project.findTypeRefForName(element);
        if (type == null) {
          // fake instance for unknown type
          type = new BinCITypeRef(element, null);
        }
      }

      if (dimension == 0) {
        return type;
      }

      return project.createArrayTypeForType(type, dimension);
    }

    private ParameterInfo getParameterInfo(int row) {
      return (ParameterInfo) change.getParametersList().get(row);
    }
  }


  final ParametersTableModel parametersTableModel = new ParametersTableModel();

  final BinTable parametersTable = new BinTable(parametersTableModel);
  {
    parametersTable.setSelectionMode(
        ListSelectionModel.SINGLE_INTERVAL_SELECTION);
  }


  private final JTextArea editor = new JTextArea();
  {
    editor.setEditable(false);
  }

  private MethodsInvocationsMap methodsInvocationsMap;
  final ChangeMethodSignatureRefactoring refactoring;
  final MethodSignatureChange change;
  JCheckBox isFinalCheckBox;
  JCheckBox isStaticCheckBox;

  ChangeMethodSignaturePanel(final ChangeMethodSignatureRefactoring refactoring) {
    super(new BorderLayout());

    this.refactoring = refactoring;
    this.change = refactoring.createSingatureChange();
    refactoring.setChange(change);

    TableColumn column = parametersTable.getColumn("Type");
    column.setCellRenderer(new TypeCellRenderer());
    column.setCellEditor(new TypeCellEditor(refactoring.getContext()));

    Box box = new Box(BoxLayout.Y_AXIS);

    box.add(createModifierPanel());
    box.add(createReturnTypePanel());
    box.add(createParametersPanel());

    add(box, BorderLayout.NORTH);
    add(createPreviewPanel());
  }

  public void stopEditing() {
    this.parametersTable.stopEditing();
  }

  private JComponent createModifierPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Modifier"));

    int[] possibleModifiers = change.getPossibleModifiers();

    ButtonGroup group = new ButtonGroup();

    JRadioButton button;

    button = new JRadioButton("public");
    button.setModel(new ModifierButtonModel(BinModifier.PUBLIC));
    group.add(button);
    panel.add(button);
    button.setEnabled(isAvailable(possibleModifiers, BinModifier.PUBLIC));

    button = new JRadioButton("protected");
    button.setModel(new ModifierButtonModel(BinModifier.PROTECTED));
    group.add(button);
    panel.add(button);
    button.setEnabled(isAvailable(possibleModifiers, BinModifier.PROTECTED));

    button = new JRadioButton("package private");
    button.setModel(new ModifierButtonModel(BinModifier.PACKAGE_PRIVATE));
    group.add(button);
    panel.add(button);
    button.setEnabled(isAvailable(possibleModifiers,
        BinModifier.PACKAGE_PRIVATE));

    button = new JRadioButton("private");
    button.setModel(new ModifierButtonModel(BinModifier.PRIVATE));
    group.add(button);
    panel.add(button);
    button.setEnabled(isAvailable(possibleModifiers, BinModifier.PRIVATE));

    this.isStaticCheckBox = new JCheckBox("static");
    this.isFinalCheckBox = new JCheckBox("final");

    isStaticCheckBox.setSelected(change.getMethod().isStatic());
    isStaticCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChangeMethodSignaturePanel panel = ChangeMethodSignaturePanel.this;
        String report;

        if (panel.isStaticCheckBox.isSelected()) {
          report = panel.change.canBeStatic();
          if (report != null) {
            RitDialog.showMessageDialog(refactoring.getContext(),
                report, "", JOptionPane.ERROR_MESSAGE);
            panel.isStaticCheckBox.setSelected(false);
          } else {
            panel.change.isStatic = true;
          }
        } else {
          panel.change.isStatic = false;
        }
        panel.updatePreview();
      }
    });

    isFinalCheckBox.setSelected(change.getMethod().isFinal());
    //isFinalCheckBox.setEnabled(change.getMethod().isFinal() ? false : true );
    isFinalCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChangeMethodSignaturePanel panel = ChangeMethodSignaturePanel.this;
        String report;

        if (panel.isFinalCheckBox.isSelected()) {

          report = panel.change.canBeFinal();
          if (report != null) {
            RitDialog.showMessageDialog(refactoring.getContext(),
                report, "", JOptionPane.ERROR_MESSAGE);
            panel.isFinalCheckBox.setSelected(false);
          } else {
            panel.change.isFinal = true;
          }

        } else {
          panel.change.isFinal = false;
        }
        panel.updatePreview();
      }
    });

    panel.add(this.isStaticCheckBox);
    panel.add(this.isFinalCheckBox);

    return panel;
  }

  private boolean isAvailable(int[] modifiers, int toCheck) {
    for (int i = 0; i < modifiers.length; i++) {
      if (modifiers[i] == toCheck) {
        return true;
      }
    }

    return false;
  }

  private JComponent createReturnTypePanel() {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    panel.add(new JLabel("Return Type:"), BorderLayout.WEST);

    final JTextField field = new JTextField();
    field.setEditable(false); // allow edit for simple types

    if (change != null) {
      BinTypeRef ref = change.getMethod().getReturnType();
      field.setText(BinFormatter.formatQualified(ref));
    }

    panel.add(field);

    JButton button = new JButton("...");
    if (change.getMethod() instanceof BinConstructor) {
      button.setEnabled(false);
    }

    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TypeChooser tc = new TypeChooser(refactoring.getContext(), "", false, null);
        tc.setIncludePrimitives(true);
        tc.setIncludeVoid(true);
        tc.show();

        BinTypeRef ref = tc.getTypeRef();
        if (ref != null) {
          field.setText(ref.getQualifiedName());
          change.setReturnType(ref);
        }

        updatePreview();
      }
    });

    panel.add(button, BorderLayout.EAST);

    return panel;
  }

  private JComponent createParametersPanel() {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel.setBorder(BorderFactory.createTitledBorder("Parameters"));

    JScrollPane sp = new JScrollPane(parametersTable);
    sp.setPreferredSize(new Dimension(10, 10));
    panel.add(sp);

    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(6, 1));

    JButton button;

    button = new JButton("Add...");
    button.addActionListener(new AddButtonListener(false));
    buttons.add(button);

    button = new JButton("Add recursively");
    button.addActionListener(new AddButtonListener(true));
    buttons.add(button);

    button = new JButton("Remove");
    button.addActionListener(new RemoveButtonListener(false));
    buttons.add(button);

    button = new JButton("Remove recursively");
    button.addActionListener(new RemoveButtonListener(true));
    buttons.add(button);

    ActionListener checkBehaviorChange = new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        BinMethod binMethod;
        binMethod = ChangeMethodSignaturePanel.this.change.getMethod();
        List invList = Finder.getInvocations(binMethod);
        if (invList.size() > 0 && ( ! alreadyWarnedInThisDialog ) &&
                JWarningDialog.shouldAskAgain(SHOW_WARNING_KEY)) {

          alreadyWarnedInThisDialog = true;

          ChangeMethodSignaturePanel.this.change.isReordered = true;
          Object[] options = {"OK", "Don't show again"};
          int result = RitDialog.showOptionDialog(refactoring.getContext(),
              JWarningDialog.MESSAGES_BUNDLE.getString(SHOW_WARNING_KEY), "Warning",
              JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
              options, options[0]);
          if(result == DONT_SHOW_AGAIN) {
            JWarningDialog.saveLastTimeValue(SHOW_WARNING_KEY, 0, false, JWarningDialog.QUESTION_MESSAGE);
          }
        }
      }
    };

    upButton = new JButton("Up");
    upButton.addActionListener(checkBehaviorChange);
    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ListSelectionModel selection = parametersTable.getSelectionModel();

        int min = selection.getMinSelectionIndex();
        int max = selection.getMaxSelectionIndex();
        int row = min - 1;

        int length = parametersTable.getRowCount();
        if (row < 0 || max >= length) {
          return;
        }

        Permutation permutation = new Permutation(length);
        for (int i = row; i < max; ) {
          permutation.setIndex(i, ++i);
        }
        permutation.setIndex(max, row);

        change.reorderParameters(permutation);

        parametersTableModel.fireTableRowsUpdated(row, max);

        selection.setSelectionInterval(row, max - 1);

        updatePreview();
      }
    });
    buttons.add(upButton);

    downButton = new JButton("Down");
    downButton.addActionListener(checkBehaviorChange);
    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ListSelectionModel selection = parametersTable.getSelectionModel();

        int min = selection.getMinSelectionIndex();
        int max = selection.getMaxSelectionIndex();
        int row = max + 1;

        int length = parametersTable.getRowCount();
        if (min < 0 || row >= length) {
          return;
        }

        Permutation permutation = new Permutation(length);
        for (int i = row; i > min; ) {
          permutation.setIndex(i, --i);
        }
        permutation.setIndex(min, row);

        change.reorderParameters(permutation);

        parametersTableModel.fireTableRowsUpdated(min, row);

        selection.setSelectionInterval(min + 1, row);

        updatePreview();
      }
    });
    buttons.add(downButton);

    panel.add(buttons, BorderLayout.EAST);

    return panel;
  }

  private JComponent createPreviewPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Method signature preview"));

    panel.add(new JScrollPane(editor));

    return panel;
  }

  void updatePreview() {
    if (change == null) {
      return; // debug mode
    }

    StringBuffer buf = new StringBuffer(50);

    buf.append(getAccessModifierPrefix());

    if(change.isStatic) {
      buf.append("static ");
    }
    if(change.isFinal) {
      buf.append("final ");
    }

    BinMethod method = change.getMethod();

    if (!(method instanceof BinConstructor)) {
      buf.append(BinFormatter.formatQualified(change.getReturnType()))
          .append(' ');
    }
    buf.append(method.getName()).append('(');

    Iterator i = change.getParametersList().iterator();
    while (i.hasNext()) {
      ParameterInfo param = (ParameterInfo) i.next();

      buf.append("\n    ")
          .append(BinFormatter.formatQualified(param.getType()))
          .append(' ').append(param.getName());

      if (i.hasNext()) {
        buf.append(',');
      } else {
        buf.append('\n');
      }
    }

    buf.append(')');

    editor.setText(buf.toString());
  }

  private String getAccessModifierPrefix() {
    switch (change.getAccessModifier()) {
      case BinModifier.PUBLIC:
        return "public ";

      case BinModifier.PROTECTED:
        return "protected ";

      case BinModifier.PACKAGE_PRIVATE:
        return "";

      case BinModifier.PRIVATE:
        return "private ";

      default:
        throw new IllegalArgumentException("unknown access modifier");
    }
  }

  public void clickUp() {
    upButton.doClick();
  }

  public void clickDown() {
    downButton.doClick();
  }

  /**
   * DEBUG: design preview.
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle("ChangeMethodSignaturePanel - Test");
    frame.setSize(400, 500);

    ChangeMethodSignatureRefactoring ref
        = new ChangeMethodSignatureRefactoring(
        new BinMethod("aaa", BinParameter.NO_PARAMS, null, 0,
        BinMethod.Throws.NO_THROWS));
    frame.getContentPane().add(new ChangeMethodSignaturePanel(ref));

    frame.show();
  }

  public MethodsInvocationsMap getMethodsInvocationsMap() {
    if (methodsInvocationsMap == null) {
      methodsInvocationsMap = new MethodsInvocationsMap(refactoring.getProject());
    }
    return this.methodsInvocationsMap;
  }

  private class AddButtonListener implements ActionListener {
    private boolean addRecursively = false;

    AddButtonListener(boolean addRecursively_) {
      this.addRecursively = addRecursively_;
    }

    public void actionPerformed(ActionEvent e) {
      ListSelectionModel selection = parametersTable.getSelectionModel();

      TypeChooser ts = new TypeChooser(refactoring.getContext(), "", false, null);
      ts.setIncludePrimitives(true);
      ts.setIncludeVoid(false);
      ts.show();

      BinTypeRef type = ts.getTypeRef();

      int num = 0;
      out:while (true) {
        Iterator i = change.getParametersList().iterator();

        while (i.hasNext()) {
          ParameterInfo info = (ParameterInfo) i.next();
          if (info.getName().equals("arg" + num)) {
            ++num;
            continue out;
          }
        }

        break;
      }

      int index = selection.getAnchorSelectionIndex() + 1;

      int nParameters = change.getParametersCount();

      // TODO: quick fix, returns wrong index, causes NPE [tonis]
      if (index > nParameters) {
        AppRegistry.getLogger(this.getClass()).debug("wrong parameter index:"
            + index);
        index = nParameters;
      }

      if (index == 0) {
        index = nParameters;
      }

      // enter name for parameter
      String parName = null;
      if (type != null) {
        parName = RitDialog.showInputDialog(
            refactoring.getContext(),
            "Name of the new parameter:", "arg" + num);
      }

      if (parName == null) {
        type = null;
        parName = "arg" + num;
      }

      if (type != null) {

        NewParameterInfo info = new NewParameterInfo(type, parName, index);
        change.addParameter(info, index);

        if(addRecursively) {
          info.setRecursiveAddParameterModel(
              showRecursiveAddParameterChooseDialog());
        }

        parametersTableModel.fireTableRowsInserted(index, index);
        selection.setSelectionInterval(index, index);
      }

      updatePreview();
    }

    public RecursiveAddParameterModel showRecursiveAddParameterChooseDialog() {
      RecursiveAddParameterModel model = null;
      SettingsDialog d = new SettingsDialog("Add/remove params recursively",
          "This option allows to add current parameter to all method's calling graph",
          new RecursiveAddParameterModel(refactoring,
          getMethodsInvocationsMap()),
          refactoring.getContext(),
          "Select  all needed callers and called methods," +
          " which want to add this parameter to",
          null);
      d.show();
      model = (RecursiveAddParameterModel) d.getModel();

      return model;
    }
  }


  private class RemoveButtonListener implements ActionListener {
    private boolean removeRecursively = false;

    RemoveButtonListener(boolean removeRecursively) {
      this.removeRecursively = removeRecursively;
    }

    public void actionPerformed(ActionEvent e) {
      ListSelectionModel selection = parametersTable.getSelectionModel();

      int min = selection.getMinSelectionIndex();
      int max = selection.getMaxSelectionIndex();

      int length = parametersTable.getRowCount();
      if (min < 0 || max >= length) {
        return;
      }

      boolean processDeleting = true;
      String usedParNames = "";
      for (int x = max; x >= min; x--) {
        int parIndex = 0;
        if (change.getParameterInfo(x) instanceof ExistingParameterInfo) {
          ExistingParameterInfo existingParameterInfo = (ExistingParameterInfo)
                change.getParameterInfo(x);
          parIndex = existingParameterInfo.getIndex();

          BinParameter par = change.getMethod().getParameters()[parIndex];

          List invocations = change.getInvocations(par);

          if (invocations.size() > 0) {
            if (usedParNames.length() != 0) {
              usedParNames += ", ";
            }
            usedParNames += par.getName();
          }

          if (removeRecursively) {
            existingParameterInfo.setDeleteParameterModel(
                getRecursiveDeleteParameterModel(par));
          }

        } else {
          if (change.getParameterInfo(x) instanceof NewParameterInfo) {
            parIndex = ((NewParameterInfo)
                change.getParameterInfo(x)).getIndex();
          } else {
            Assert.must(false,
                "ChangeMethodSignaturePanel: wrong type in place of parameter");
          }
        }
      }

      if (usedParNames.length() != 0)
        if (RitDialog.showConfirmDialog(refactoring.getContext(),
            "Parameters " + usedParNames + " are used, deleting make" +
            " you code wrong, do you want to continue?"
            , "Warning",
            JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
          processDeleting = false;
        }

      if (processDeleting) {
        change.delChecked = true;
        for (int i = max; i >= min; i--) {
          change.deleteParameter(i);
        }

        parametersTableModel.fireTableRowsDeleted(min, max);

        length = parametersTable.getRowCount();
        if (length == 0) {
          selection.clearSelection();
        } else {
          int row = (min < length) ? min : length - 1;

          selection.setSelectionInterval(row, row);
        }
      }

      updatePreview();
    }

    public RecursiveDeleteParameterModel getRecursiveDeleteParameterModel(
        BinParameter par) {
      RecursiveDeleteParameterModel model = null;

      SettingsDialog d = new SettingsDialog("Add/remove params recursively",
          "This option allows to remove the current parameter from all method's calling graph",
          new RecursiveDeleteParameterModel(refactoring,
          getMethodsInvocationsMap(), par)
          ,
          refactoring.getContext(),
          "Select  all needed callers and called methods," +
          " which want to remove this parameter from",
          null);
      d.show();
      model = (RecursiveDeleteParameterModel) d.getModel();

      return model;
    }
  }
}
