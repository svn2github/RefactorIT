/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractsuper;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Category;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Retrieves super name, member list to extract and flag: class/interface.
 *
 * @author Anton Safonov
 * @author Tonis Vaga
 */
public class ExtractSuperDialog {
  final IdeWindowContext context;
  RitDialog dialog;

  boolean isOkPressed;

  ExtractSuper extractor;

  BinTable membersTable;
  JTextField newTypeName;
  JTextField newPackageName;
  JTextField oldPackageName;
  JRadioButton radioNewNameSub;
  JRadioButton radioNewNameSuper;

  JPanel contentPanel = new JPanel();

  JCheckBox changeAccesRightsButton = new JCheckBox("Change access when needed");

  private JRadioButton extractClass;
  private JCheckBox upcastUsages = new JCheckBox("Use supertype where possible");

  private JTextArea previewArea;
  private JButton buttonOk = new JButton("Ok");
  private JButton buttonCancel = new JButton("Cancel");
  private JButton buttonSelectAll = new JButton("Select All");
  private JButton buttonDeselectAll = new JButton("Deselect All");
  private JButton buttonHelp = new JButton("Help");

  private JRadioButton extractAbstract;
  private ButtonGroup nameModeSelection = null;

  public ExtractSuperDialog(IdeWindowContext context, ExtractSuper extractor) {
    this.context = context;
    this.extractor = extractor;

    contentPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    if (((ExtractableMembersModel) membersTable.getModel()).update()) {
      fixColumnSizes();
    }

    updatePreview();
  }

  /**
   * Allowed to be called several times - recreates dialog as needed.
   */
  public void show() {
    isOkPressed = false;

    dialog = RitDialog.create(context);
    dialog.setTitle("Extract Superclass/Interface");
    dialog.setContentPane(contentPanel);

    dialog.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            newTypeName.requestFocus();
            newTypeName.setCaretPosition(newTypeName.getText().length());
          }
        });
      }
    });

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.extract_super");

    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp);

    dialog.show();
  }

  private JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(
        "Specify name of the new class/interface and select members to extract into it.\n"
        + "Selection list tracks inter-member dependencies automatically."
        );
  }

  private JComponent createMainPanel() {
    JPanel center = new JPanel(new BorderLayout(5, 5));
    //center.setBorder( BorderFactory.createTitledBorder( "AAA") );
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    center.setBorder(BorderFactory.createEtchedBorder());
    /*
         center.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5) )
        );
     */

    // create checkboxes early, since center panel will use its values on create

    final JPanel checkboxPanel = createCheckboxPanel();

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(createCenterPanel(), BorderLayout.CENTER);
    p.add(checkboxPanel, BorderLayout.SOUTH);

    center.add(createMessagePanel(), BorderLayout.NORTH);
    center.add(p, BorderLayout.CENTER);

    return center;
  }

  private JPanel createCheckboxPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1, 5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

    JLabel label = new JLabel("Extract");
    extractClass = new JRadioButton("superclass", true);
    extractAbstract = new JRadioButton("abstract class", false);
    JRadioButton extractInterface = new JRadioButton("interface", false);

    // disabled as requested in #2171
    extractAbstract.setVisible(false);

    ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (membersTable != null && membersTable.getModel() != null) {
          if (((ExtractableMembersModel) membersTable.getModel()).update()) {
            fixColumnSizes();
          }
        } else {
          if (membersTable == null) {
            new NullPointerException(
                "PLEASE REPORT to anton@refactorit.com - membersTable is null")
                .printStackTrace(System.err);
          } else {
            new NullPointerException(
                "PLEASE REPORT to anton@refactorit.com - members model is null")
                .printStackTrace(System.err);
          }
        }
        updatePreview();
      }
    };
    extractClass.setMnemonic(KeyEvent.VK_S);
    extractAbstract.setMnemonic(KeyEvent.VK_A);
    extractInterface.setMnemonic(KeyEvent.VK_I);

    ButtonGroup group = new ButtonGroup();
    group.add(extractClass);
    group.add(extractAbstract);
    group.add(extractInterface);

    Box extractChecksBox = Box.createHorizontalBox();
    extractChecksBox.add(label);
    extractChecksBox.add(Box.createHorizontalStrut(7));
    extractChecksBox.add(extractClass);
    extractChecksBox.add(extractAbstract);
    extractChecksBox.add(extractInterface);

    panel.add(extractChecksBox);

    if (extractor.getTypeRef().getBinCIType().isInterface()
        || extractor.getTypeRef().getBinCIType().isEnum()
        || extractor.getTypeRef().getBinCIType().isAnnotation()) {
      extractClass.setSelected(false);
      extractClass.setEnabled(false);
      extractAbstract.setSelected(false);
      extractAbstract.setEnabled(false);
      extractInterface.setSelected(true);
      extractInterface.setEnabled(false);
    }

    if (!isPossibleExtractInterface()) {
      extractInterface.setEnabled(false);
    }

    if (!isPossibleExtractAbstract()) {
      extractAbstract.setEnabled(false);
    }

    upcastUsages.setEnabled(true);
    changeAccesRightsButton.setSelected(true);
    extractor.setConvertPrivate(true);

    changeAccesRightsButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        final boolean selected = changeAccesRightsButton.isSelected();

        if (selected != extractor.isConvertPrivate()) {
          extractor.setConvertPrivate(selected);

          ExtractableMembersModel model = ((ExtractableMembersModel)
              membersTable.getModel());
          model.setSelectionForAll(false);

//          model.update();
//          updatePreview();
        }
      }
    });

    upcastUsages.setMnemonic(KeyEvent.VK_U);
    extractChecksBox.add(Box.createHorizontalGlue());

    extractChecksBox.add(changeAccesRightsButton);
    extractChecksBox.add(upcastUsages);

    extractClass.addChangeListener(changeListener);
    extractAbstract.addChangeListener(changeListener);
    extractInterface.addChangeListener(changeListener);

    return panel;
  }

  public boolean isRefactorToSupertypeEnabled() {
    return upcastUsages.isSelected();
  }

  private boolean isPossibleExtractAbstract() {
    final List members = new ArrayList(50);
    members.addAll(Arrays.asList(
        extractor.getTypeRef().getBinCIType().getDeclaredFields()));
    members.addAll(Arrays.asList(
        extractor.getTypeRef().getBinCIType().getDeclaredMethods()));
    for (int i = 0, max = members.size(); i < max; i++) {
      final BinMember member = (BinMember) members.get(i);
      if (!member.isPrivate()) {
        return true;
      }
    }

    return false;
  }

  private boolean isPossibleExtractInterface() {
    final List members = new ArrayList(50);
    members.addAll(Arrays.asList(
        extractor.getTypeRef().getBinCIType().getDeclaredFields()));
    members.addAll(Arrays.asList(
        extractor.getTypeRef().getBinCIType().getDeclaredMethods()));
    // searching for at least one member satisfying our conditions
    for (int i = 0, max = members.size(); i < max; i++) {
      final BinMember member = (BinMember) members.get(i);
      // members must be public
      if (!member.isPublic()) {
        continue;
      }
      // fields should be public static final
      if (member instanceof BinField) {
        if (!member.isFinal() || !member.isStatic()) {
          continue;
        }
      }
      return true;
    }

    return false;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 4, 0));

    buttonOk.setEnabled(false);
    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        membersTable.stopEditing();
        isOkPressed = true;
        updatePreview(); // update extractor with new values
        dialog.dispose();
      }
    });
    buttonSelectAll.setMnemonic(KeyEvent.VK_S);
    buttonDeselectAll.setMnemonic(KeyEvent.VK_D);

    buttonPanel.add(buttonSelectAll);
    buttonPanel.add(buttonDeselectAll);

    buttonSelectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ((ExtractableMembersModel) membersTable.getModel()).setSelectionForAll(true);
      }
    });

    buttonDeselectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ((ExtractableMembersModel) membersTable.getModel()).setSelectionForAll(false);
      }
    });

    buttonPanel.add(buttonOk);

    buttonCancel.setSelected(true); // ???
    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonCancel);

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

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);

    return downPanel;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public boolean isExtractClass() {
    return this.extractClass.isSelected() || this.extractAbstract.isSelected();
  }

  public boolean isExtractAbstract() {
    return this.extractAbstract.isSelected();
  }

  private JPanel createCenterPanel() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));

    Box namesPanel = Box.createHorizontalBox();
    JPanel namesRealPanel = new JPanel(new GridLayout());
    namesRealPanel.add(namesPanel);
    namesRealPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

    Box radioButtons = Box.createVerticalBox();
    final Box namesInputFields = Box.createVerticalBox();

    namesPanel.add(radioButtons);
    namesPanel.add(Box.createHorizontalStrut(5));
    namesPanel.add(namesInputFields);

    final JPanel subNamePanel = new JPanel(new BorderLayout());
    final JPanel superNamePanel = new JPanel(new BorderLayout());

    final JComponent inputOldName = createOldNameInput();
    final JComponent inputNewName = createNewNameInput();

    subNamePanel.add(inputOldName, BorderLayout.CENTER);
    superNamePanel.add(inputNewName, BorderLayout.CENTER);

    namesInputFields.add(subNamePanel);
    namesInputFields.add(Box.createVerticalStrut(5));
    namesInputFields.add(superNamePanel);

    DocumentListener listener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        contentPanel.revalidate();
        updatePreview();
      }

      public void insertUpdate(DocumentEvent e) {
        contentPanel.revalidate();
        updatePreview();
      }

      public void removeUpdate(DocumentEvent e) {
        contentPanel.revalidate();
        updatePreview();
      }
    };

    newTypeName.getDocument().addDocumentListener(listener);
    newPackageName.getDocument().addDocumentListener(listener);
    oldPackageName.getDocument().addDocumentListener(listener);

    ActionListener nameModeListener = new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("sub")){
          subNamePanel.removeAll();
          subNamePanel.add(inputNewName, BorderLayout.CENTER);

          if (extractor.getOldPackageName().length() > 0) {
            newPackageName.setText(extractor.getOldPackageName() + ".");
          } else {
            newPackageName.setText("");
          }

          newPackageName.setEditable(false);
          oldPackageName.setEditable(true);

          superNamePanel.removeAll();
          superNamePanel.add(inputOldName, BorderLayout.CENTER);
          panel.repaint();
          updatePreview();
        } else { // event "super"
          subNamePanel.removeAll();
          subNamePanel.add(inputOldName, BorderLayout.CENTER);

          if (extractor.getOldPackageName().length() > 0) {
            oldPackageName.setText(extractor.getOldPackageName() + ".");
          } else {
            oldPackageName.setText("");
          }

          newPackageName.setEditable(true);
          oldPackageName.setEditable(false);

          superNamePanel.removeAll();
          superNamePanel.add(inputNewName, BorderLayout.CENTER);
          updatePreview();
          panel.repaint();
        }
      }
    };

    radioNewNameSub = new JRadioButton("Sub" + extractor
        .getTypeRef().getBinCIType().getMemberType() + " name", false);
    radioNewNameSub.setActionCommand("sub");
    radioNewNameSub.addActionListener(nameModeListener);

    radioNewNameSuper = new JRadioButton(
        getLabelForSupertypeName(), true);
    radioNewNameSuper.setActionCommand("super");
    radioNewNameSuper.addActionListener(nameModeListener);

    nameModeSelection = new ButtonGroup();
    nameModeSelection.add(radioNewNameSub);
    nameModeSelection.add(radioNewNameSuper);

    radioButtons.add(radioNewNameSub);
    radioButtons.add(Box.createVerticalStrut(5));
    radioButtons.add(radioNewNameSuper);

    panel.add(namesRealPanel, BorderLayout.NORTH);

    JPanel center = new JPanel(new GridLayout(1, 2, 5, 5));

    JPanel membersPanel = new JPanel(new BorderLayout());
    membersPanel.setBorder(
        BorderFactory.createTitledBorder("Members to extract"));

    final ExtractableMembersModel model = new ExtractableMembersModel(extractor);
    model.setDialog(this);
    membersTable = new BinTable(model);
    membersTable.setDefaultRenderer(
        Boolean.class, new ExtractSTableRenderer());
    membersTable.setDefaultRenderer(
        ExtractableMemberNode.class, new ExtractSTableRenderer());
    membersTable.getTableHeader().setReorderingAllowed(false);
    membersTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    fixColumnSizes();
    final JScrollPane membersScroll = new JScrollPane(membersTable);
    membersScroll.getViewport().setBackground(membersTable.getBackground());
    membersPanel.add(membersScroll, BorderLayout.CENTER);

    JPanel previewPanel = new JPanel(new BorderLayout());
    previewPanel.setBorder(BorderFactory.createTitledBorder(
        "Supertype preview"));
    previewArea = new JTextArea();
    previewArea.setBackground(panel.getBackground());
    previewArea.setEditable(false);
    JScrollPane previewPane = new JScrollPane(previewArea);
    previewPanel.add(previewPane, BorderLayout.CENTER);

    center.add(membersPanel);
    center.add(previewPanel);

    panel.add(center, BorderLayout.CENTER);

    return panel;
  }

  private JComponent createOldNameInput() {
    JTextField oldTypeName = new JTextField(extractor.getTypeRef().getName());
    oldTypeName.setEditable(false);
    oldTypeName.setBackground(contentPanel.getBackground());

    String oldName = extractor.getTypeRef().getPackage()
        .getQualifiedName();
    if (oldName.length() > 0) {
      oldName += '.';
    }

    oldPackageName = new JTextField(oldName) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        final Insets insets = getInsets();
        final Insets margin = getMargin();
        dim.width = getFontMetrics(getFont()).stringWidth(getText())
            + insets.left + insets.right
            + margin.left + margin.right;

        if (getText() == null || getText().length() == 0) {
          dim.width += 7;
        }
        return dim;
      }

      public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };
    oldPackageName.setColumns(oldName.trim().length());
    oldPackageName.setEditable(false);
    oldPackageName.setBackground(contentPanel.getBackground());
    Insets margin = oldPackageName.getMargin();
    margin.right = 1;
    oldPackageName.setMargin(margin);

    JPanel oldTypeBox = new JPanel(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();
    constr.weightx = 0.0;
    constr.gridy = 0;
    constr.gridx = 0;
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    oldTypeBox.add(oldPackageName, constr);

    constr.weightx = 1.0;
    constr.gridx = 1;
    constr.fill = GridBagConstraints.HORIZONTAL;
    oldTypeBox.add(oldTypeName, constr);

    return oldTypeBox;
  }

  private JComponent createNewNameInput() {
    newTypeName = new JTextField();
    String newName = extractor.getTypeRef().getPackage()
        .getQualifiedName();
    if (newName.length() > 0) {
      newName += '.';
    }

    newPackageName = new JTextField(newName) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        final Insets insets = getInsets();
        final Insets margin = getMargin();
        dim.width = getFontMetrics(getFont()).stringWidth(getText())
            + insets.left + insets.right
            + margin.left + margin.right;

        if (getText() == null || getText().length() == 0) {
          dim.width += 7;
        }
        return dim;
      }

      public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };
    newPackageName.setColumns(newName.trim().length());
    Insets margin = newPackageName.getMargin();
    margin.right = 1;
    newPackageName.setMargin(margin);

    JPanel newTypeBox = new JPanel(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();
    constr.weightx = 0.0;
    constr.gridy = 0;
    constr.gridx = 0;
    constr.fill = GridBagConstraints.NONE;
    constr.anchor = GridBagConstraints.WEST;
    newTypeBox.add(newPackageName, constr);

    constr.weightx = 1.0;
    constr.gridx = 1;
    constr.fill = GridBagConstraints.HORIZONTAL;
    newTypeBox.add(newTypeName, constr);

    return newTypeBox;
  }

  public String getLabelForSupertypeName(){
    final boolean isClass = extractor.getTypeRef().getBinCIType().isClass();
    if (isClass) {
      return "Superclass/interface name";
    } else {
      return "Super interface name";
    }
  }

  void fixColumnSizes() {
    int width = 20;
    membersTable.getColumn(membersTable.getColumnName(0)).setMinWidth(2);
    membersTable.getColumn(membersTable.getColumnName(0)).setPreferredWidth(
        width);
    membersTable.getColumn(membersTable.getColumnName(0)).setWidth(width);
    membersTable.getColumn(membersTable.getColumnName(0)).setMaxWidth(width);
    membersTable.getColumn(membersTable.getColumnName(0)).setResizable(false);
    membersTable.getColumn(membersTable.getColumnName(2)).setMinWidth(2);
    membersTable.getColumn(membersTable.getColumnName(2)).setPreferredWidth(70);
    membersTable.getColumn(membersTable.getColumnName(2)).setWidth(70);
  }

  private boolean isExtractWithOldName(){
    return nameModeSelection.getSelection().getActionCommand().equals("sub");
  }

  void updatePreview() {
    if (membersTable == null) {
      return;
    }

    String pName;

    //when extractig with old name, we edit oldPackageName, not newPackageName
    if (radioNewNameSuper.isSelected()) {
      pName = this.newPackageName.getText().trim();
    } else {
      pName = this.oldPackageName.getText().trim();
    }

    if (pName.endsWith(".")) {
      this.extractor.setNewPackageName(pName.substring(0, pName.length() - 1));
    } else {
      extractor.setNewPackageName(pName);
    }

    this.extractor.setNewTypeName(this.newTypeName.getText().trim());
    this.extractor.setExtractClass(isExtractClass());
    this.extractor.setForceExtractMethodsAbstract(isExtractAbstract());
    this.extractor.setExtractWithOldName(isExtractWithOldName());

    ExtractableMembersModel model
        = (ExtractableMembersModel) membersTable.getModel();
    model.fireTableDataChanged();
    List membersToExtract = new ArrayList(model.getAllRowCount());
    Set abstractMethods = new HashSet();
    for (int i = 0, max = model.getAllRowCount(); i < max; i++) {
      final ExtractableMemberNode node = model.getRowFromAll(i);
      if (node.isSelected() && !node.isHidden()) {
        membersToExtract.add(node.getBin());
        if (node.isAbstract()) {
          abstractMethods.add(node.getBin());
        }
      }
    }

    this.extractor.setMembersToExtract(membersToExtract);
    this.extractor.setExplicitlyAbstractMethods(abstractMethods);

    this.previewArea.setText(extractor.getSuperTypePreview());
    this.previewArea.setCaretPosition(0);

    updateOkButton();
  }

  private void updateOkButton() {
    final String newTypeName = extractor.getNewTypeName();
    final List membersToExtract = extractor.getMembersToExtract();

    if (radioNewNameSub.isSelected()) {
      if (newPackageName.getText().trim().length() > 0
          && oldPackageName.getText().trim().length() == 0) {
        buttonOk.setEnabled(false);
        return;
      }
    } else {
      if (oldPackageName.getText().trim().length() > 0
          && newPackageName.getText().trim().length() == 0) {
        buttonOk.setEnabled(false);
        return;
      }
    }

    buttonOk.setEnabled(membersToExtract != null
        && (NameUtil.isValidIdentifier(newTypeName)
        || extractor.getNewPackageName().length() == 0
        && NameUtil.isValidPackageName(extractor.getNewPackageName()))
        /* && membersToExtract.size() > 0 */ );
  }

  /** Test driver for {@link ExtractSuperDialog}. */
  public static class TestDriver extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    /** Test project. */
    private Project project;
    private BinClass testClass;

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("ExtractSuperDialog tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(Utils
          .getTestProjects().getProject("ExtractSuperDialog_DependencyCheck"));
      project.getProjectLoader().build();

      testClass = (BinClass) project.getTypeRefForName("Test").getBinType();
    }

    protected void tearDown() {
      project = null;
      testClass = null;
      DialogManager.setInstance(new NullDialogManager());
    }

    public void testA1() {
      selectMethodTest("public_methodA1",
          new String[] {"public_methodA1", "public_methodA2"}
          , false);
    }

    public void testA2() {
      selectMethodTest("public_methodA2", new String[] {"public_methodA2"}
          , false);
    }

    public void testB1_1() {
      selectMethodTest("public_methodB1",
          new String[] {"public_methodB1", "private_methodB2"}
          , false);
    }

    public void testB1_2() {
      selectMethodTest("public_methodB1",
          new String[] {"public_methodB1", "private_methodB2"}
          , true);
    }

    public void testB2_1() {
      selectMethodTest("private_methodB2",
          new String[] {"public_methodB1", "private_methodB2"}
          , false);
    }

    public void testB2_2() {
      selectMethodTest("private_methodB2",
          new String[] {"private_methodB2"}
          , true);
    }

    public void testC1_1() {
      selectMethodTest("private_methodC1",
          new String[] {"private_methodC1", "public_methodC2"}
          , false);
    }

    public void testC1_2() {
      selectMethodTest("private_methodC1",
          new String[] {"private_methodC1", "public_methodC2"}
          , true);
    }

    public void testC2_1() {
      selectMethodTest("public_methodC2",
          new String[] {"public_methodC2"}
          , false);
    }

    public void testC2_2() {
      selectMethodTest("public_methodC2",
          new String[] {"public_methodC2"}
          , true);
    }

    public void testD1() {
      selectMethodTest("public_methodD1",
          new String[] {"public_methodD1", "public_methodD2"}
          , true);
    }

    public void testD2() {
      selectMethodTest("public_methodD2",
          new String[] {"public_methodD1", "public_methodD2"}
          , true);
    }

    public void testE() {
      selectMethodTest("public_methodE",
          new String[] {"public_methodE", "public_field"}
          , true);
    }

    public void testF() {
      selectMethodTest("private_methodF",
          new String[] {"private_methodF", "public_field"}
          , true);
    }

    public void testG_1() {
      selectMethodTest("public_methodG",
          new String[] {"public_methodG", "private_field", "private_methodH"}
          ,
          false);
    }

    public void testG_2() {
      selectMethodTest("public_methodG",
          new String[] {"public_methodG", "private_field"}
          , true);
    }

    public void testH_1() {
      selectMethodTest("private_methodH",
          new String[] {"private_methodH", "private_field", "public_methodG"}
          ,
          false);
    }

    public void testH_2() {
      selectMethodTest("private_methodH",
          new String[] {"private_methodH", "private_field"}
          , true);
    }

    public void testPublicField() {
      selectFieldTest("public_field",
          new String[] {"public_field"}
          , false);
    }

    public void testPrivateField_1() {
      selectFieldTest("private_field",
          new String[] {"private_field", "public_methodG", "private_methodH"}
          ,
          false);
    }

    public void testPrivateField_2() {
      selectFieldTest("private_field",
          new String[] {"private_field"}
          ,
          true);
    }

    public void testPublicField2() {
      selectFieldTest("public_field2",
          new String[] {"public_field2", "public_methodX"}
          ,
          true);
    }

    public void testPublicField3_1() {
      selectFieldTest("public_field3",
          new String[] {"public_field3", "private_methodY", "private_field3"}
          ,
          false);
    }

    public void testPublicField3_2() {
      selectFieldTest("public_field3",
          new String[] {"public_field3", "private_methodY"}
          ,
          true);
    }

    public void testPrivateField2() {
      selectFieldTest("private_field2",
          new String[] {"private_field2", "public_methodX"}
          ,
          true);
    }

    public void testPrivateField3_1() {
      selectFieldTest("private_field3",
          new String[] {"private_field3", "private_methodY", "public_field3"}
          ,
          false);
    }

    public void testPrivateField3_2() {
      selectFieldTest("private_field3",
          new String[] {"private_field3", "private_methodY"}
          ,
          true);
    }

    public void testUnselectMethodA2() {
      unselectMethodTest("public_methodA1", "public_methodA2",
          new String[0], false);
    }

    public void testUnselectMethodA1() {
      unselectMethodTest("public_methodA1", "public_methodA1",
          new String[] {"public_methodA2"}
          , false);
    }

    public void testUnselectMethodB1_1() {
      unselectMethodTest("public_methodB1", "public_methodB1",
          new String[0], false);
    }

    public void testUnselectMethodB1_2() {
      unselectMethodTest("public_methodB1", "public_methodB1",
          new String[] {"private_methodB2"}
          , true);
    }

    private void selectMethodTest(String methodName, String[] expectedNames,
        final boolean convertPrivate) {

      cat.info("Tests selecting method " + methodName);

      ExtractableMembersModel model = createModel(convertPrivate);

      BinMethod method = testClass.getDeclaredMethod(
          methodName, BinTypeRef.NO_TYPEREFS);
      assertNotNull("Found method", method);

      model.updateSelection(method, true);

      checkNames(model, expectedNames);

      cat.info("SUCCESS");
    }

    private void selectFieldTest(String fieldName, String[] expectedNames,
        final boolean convertPrivate) {

      cat.info("Tests selecting field " + fieldName);

      ExtractableMembersModel model = createModel(convertPrivate);

      BinField field = testClass.getDeclaredField(fieldName);
      assertNotNull("Found field", field);

      model.updateSelection(field, true);

      checkNames(model, expectedNames);

      cat.info("SUCCESS");
    }

    private void unselectMethodTest(String selectMethodName,
        String unselectMethodName,
        String[] expectedNames,
        final boolean convertPrivate) {

      cat.info("Tests unselecting method " + unselectMethodName);

      ExtractableMembersModel model = createModel(convertPrivate);

      BinMethod method = testClass.getDeclaredMethod(
          selectMethodName, BinTypeRef.NO_TYPEREFS);
      assertNotNull("Found method", method);

      model.updateSelection(method, true);

      method = testClass.getDeclaredMethod(
          unselectMethodName, BinTypeRef.NO_TYPEREFS);
      assertNotNull("Found method", method);

      model.updateSelection(method, false);

      checkNames(model, expectedNames);

      cat.info("SUCCESS");
    }

    private ExtractableMembersModel createModel(final boolean convertPrivate) {
      ExtractSuper extractor = new ExtractSuper(
          new NullContext(project), testClass.getTypeRef());
      extractor.setConvertPrivate(convertPrivate);
//      DialogManager.setInstance(new NullDialogManager() {
//        public int showYesNoQuestion(Component parent, String key,
//                                     String message,
//                                     int defaultSelectedButton) {
//          if (convertPrivate) {
//            return DialogManager.YES_BUTTON;
//          } else {
//            return DialogManager.NO_BUTTON;
//          }
//        }
//
//      });

      ExtractableMembersModel model
          = new ExtractableMembersModel(extractor);
      return model;
    }

    private void checkNames(final ExtractableMembersModel model,
        final String[] expectedNames) {

      List selectedNames = new ArrayList();
      for (int i = 0, max = model.getAllRowCount(); i < max; i++) {
        final BinTreeTableNode node = model.getRowFromAll(i);
        if (node.isSelected() && !node.isHidden()) {
          CollectionUtil.addNew(selectedNames, ((BinMember) node.getBin()).getName());
        }
      }

      assertNamesEqual("Selected members", expectedNames, selectedNames);
    }

    /**
     * Asserts that usages found are as expected.
     *
     * @param expectedNames
     * @param actualNames
     */
    private void assertNamesEqual(String message,
        String[] expectedNames,
        List actualNames) {
      final List actual = new ArrayList(expectedNames.length);
      for (int i = 0, max = actualNames.size(); i < max; i++) {
        actual.add(actualNames.get(i));
      }

      final List expected = new ArrayList(expectedNames.length);
      for (int i = 0, len = expectedNames.length; i < len; i++) {
        expected.add(expectedNames[i]);
      }

      Collections.sort(actual);
      Collections.sort(expected);

      assertEquals(message, expected, actual);
    }
  }
}
