/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 *
 * @author tonis
 */
public class TypeChooser {
  final RitDialog dialog;

  /**
   * initial class name input for dialog
   */
  private String initalClassName = "";

  AbstractClassSearchPanel tabs[] = new AbstractClassSearchPanel[2];

  boolean includePrimitives;
  boolean includeVoid;
  boolean includeClasspath;

  JTabbedPane tabbedPane;
  Object nodeToOpen;

  RefactorItContext context;

  ChangeListener tabbListener;

  private JButton buttonOk = new JButton("Ok");
  private JButton buttonCancel = new JButton("Cancel");
  private JButton buttonHelp = new JButton("Help");

  private String helpTopicId;

  private JPanel searchTabbedPane, packageTabbedPane;

  private Project project;
  private CancelOkListener listener;

  private int startPanelIndex;
  private int currentPanelIndex;

  public TypeChooser(RefactorItContext context, String initialClassName,
      boolean includeClasspath, String helpTopicId) {
    this(context, includeClasspath, helpTopicId, null, false);
    this.initalClassName = initialClassName;
  }

  /**
   *
   * @param context
   * @param includeClasspath
   * @param helpTopicId
   * @param nodeToOpenParam
   * @param openBrowseTab true if Browse tree initially
   */
  public TypeChooser(
      final RefactorItContext context,
      final boolean includeClasspath, String helpTopicId,
      Object nodeToOpenParam, boolean openBrowseTab
      ) {
    this.context = context;
    this.includeClasspath = includeClasspath;

    startPanelIndex = openBrowseTab ? 1 : 0;

    project = context.getProject();
    if (nodeToOpenParam == null) {
      nodeToOpenParam = project.getPackageForName("java");
    }
    nodeToOpen = nodeToOpenParam;

    this.helpTopicId = helpTopicId;

    listener = new CancelOkListener() {
      public void doOk() {
        TypeChooser.this.doOk();
      }

      public void doCancel() {
        TypeChooser.this.doCancel();
      }
    };

    tabbedPane = new JTabbedPane();

    tabbedPane.addTab("Search Class", searchTabbedPane);
    tabbedPane.addTab("Browse Class", packageTabbedPane);

    tabbListener = new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source == tabbedPane) {
          int index = tabbedPane.getSelectedIndex();
          showPanel(index);
        }
      }
    };

    tabbedPane.addChangeListener(tabbListener);

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    //contentPane.add(center, BorderLayout.CENTER);
    contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);
    contentPane.add(tabbedPane);

//    contentPane.setPreferredSize(new Dimension(595, 380));
    contentPane.setPreferredSize(new Dimension(450, 350));

    dialog = RitDialog.create(context);
    dialog.setTitle("Choose a class or interface");
    dialog.setContentPane(contentPane);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, this.helpTopicId);
    SwingUtil.initCommonDialogKeystrokes(dialog,
        buttonOk, buttonCancel, buttonHelp);
  }

  public void show() {
    int index = startPanelIndex; //tabbedPane.getSelectedIndex();
    showPanel(index);

    dialog.show();
    tabbedPane.setSelectedIndex(index);

//    if ( index != -1 ) {
//      tabs[index].onShow();
//    }
  }

  public BinTypeRef getTypeRef() {
    int index = currentPanelIndex;

    if (index != -1) {
      return tabs[index].getTypeRef();
    }

    return null;
  }

  private void dispose() {
    tabbedPane.removeChangeListener(tabbListener);
    dialog.dispose();
  }

  void doCancel() {
    int index = tabbedPane.getSelectedIndex();
    if (index != -1 && tabs[index] != null) {
      tabs[index].doCancel();
    }
    dispose();
  }

  void doOk() {
    int index = tabbedPane.getSelectedIndex();
    if (index != -1 && tabs[index] != null) {
      tabs[index].doOk();
    }
    dispose();
  }

  private JComponent createButtonsPanel() {
    JPanel contentPanel = new JPanel(new BorderLayout());

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doOk();
      }
    });

    JCheckBox checkBox = new JCheckBox("Include classpath", includeClasspath);
//    checkBox.addChangeListener( new ChangeListener() {
//      public void stateChanged(ChangeEvent event) {
//        includeClasspath=!includeClasspath;
//        int index=tabbedPane.getSelectedIndex();
//        if ( index!=-1 ) {
//          if ( tabs[index] == null ) {
//            return;
//          }
//          BinTypeRef typeRef = tabs[index].getTypeRef();
//          if ( typeRef != null ) {
//            nodeToOpen = typeRef;
//          }
//          tabs[0]= null;
//          tabs[1]=null;
//          showPanel(index);
//        }
//      }
//
//    } );

    checkBox.addItemListener(new ItemListener() {

      public void itemStateChanged(ItemEvent event) {
        includeClasspath = !includeClasspath;
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
          if (tabs[index] == null) {
            return;
          }

          BinTypeRef typeRef = tabs[index].getTypeRef();
          if (typeRef != null) {
            nodeToOpen = typeRef;
          }

          if (index == 0) {
            initalClassName = ((ClassSearchPanel) tabs[0]).getUserInput();
          }

          tabs[0] = null;
          tabs[1] = null;

          showPanel(index);
        }
      }
    });

    contentPanel.add(checkBox, BorderLayout.NORTH);

    //buttonPanel.add(checkBox);

    contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(buttonOk);

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doCancel();
      }
    });
    buttonPanel.add(buttonCancel);

    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    //constraints.anchor = GridBagConstraints.EAST;
    constraints.anchor = GridBagConstraints.CENTER;

    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);

//    downPanel.add(buttonPanel, constraints);
    downPanel.add(contentPanel, constraints);

    return downPanel;
  }

  void showPanel(final int index) {
    if (index != -1) {
      if (tabs[index] == null) {
        if (index == 0) {
          createClassSearchPanel(initalClassName);
        } else {
          try {
            JProgressDialog.run(context, new Runnable() {
              public void run() {
                tabs[index] = new PackageTreePanel(context,
                    true, includeClasspath, includePrimitives,
                    includeVoid, nodeToOpen);
              }
            }


            , true);
          } catch (SearchingInterruptedException ex) {
            return;
          }
        }
        //JPanel panel=(JPanel) tabbedPane.getComponentAt(index);
        //panel.add(tabs[index]);
        tabbedPane.setComponentAt(index, tabs[index]);
      }

      tabbedPane.setSelectedIndex(index);

      currentPanelIndex = index;

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          tabs[index].onShow();
        }
      });

      // FIXME: remove this
      //System.err.println("[tonisdebug]:currentPanelIndex="+currentPanelIndex);
    }
  }

  private void createClassSearchPanel(String initialClassName) {
    tabs[0] = new ClassSearchPanel(
        context, initialClassName, includeClasspath, listener);
  }

  public void setIncludePrimitives(final boolean includePrimitives) {
    this.includePrimitives = includePrimitives;
  }

  public void setIncludeVoid(final boolean includeVoid) {
    this.includeVoid = includeVoid;
  }
}
