/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Tonis Vaga
 */
public class ClassSearchPanel extends AbstractClassSearchPanel {
//  private JButton ok = new JButton("Ok");
//  private JButton cancel = new JButton("Cancel");
//  private JButton buttonHelp = new JButton("Help");

  CancelOkListener listener;
  JList matchesList;
  JTextField textField;

  private BinTypeRef selectedNode;
  private JPanel searchingPanel;

  private String previousInput = "";
  private Project project;
  private List matches;
  private boolean includeClassPath;

  /**
   *
   * @pre listener!=null
   * @param initialClassName initial classname used for input
   */
  public ClassSearchPanel(
      final RefactorItContext context,
      String initialClassName, final boolean includeClasspath,
      final CancelOkListener listener
  ) {
    this.listener = listener;
    this.includeClassPath = includeClasspath;
    project = context.getProject();
    //this.helpTopicId = helpTopicId;
    final String className = initialClassName;
    this.textField = new JTextField(className, 20);
    this.textField.setEnabled(true);

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 2, 3),
        BorderFactory.createEtchedBorder()));

//    JPanel helpPanel =
//        DialogManager.getHelpPanel("Select destination class or interface");

    //helpPanel.add(textField);

    matchesList = new JList(); //new JTextArea();

    textField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        listener.doOk();
      }
    });

    //previousInput = textField.getText();

    Runnable target = new Runnable() {
      public void run() {
        try {
          initItems();
        } finally {
          try {
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                process(textField.getText());
                textField.addCaretListener(new MyCaretListener());
              }
            });
          } catch (InterruptedException e) {
            e.printStackTrace(RuntimePlatform.console); //To change body of catch statement use Options | File Templates.
          } catch (InvocationTargetException e) {
            e.printStackTrace(RuntimePlatform.console); //To change body of catch statement use Options | File Templates.
          }
        }
      }
    };

//    target.run();
    new Thread(target).start();

//    matchesList.addListSelectionListener(new ListSelectionListener() {
//      public void valueChanged(ListSelectionEvent e) {
//      	if ( e.getValueIsAdjusting() ) {
//      		return;
//      	}
//        //
//        System.out.println("item selection changed");
//        final BinCITypeRefItem item =
//          (BinCITypeRefItem) matchesList.getSelectedValue();
//        if (item == null) {
//          return;
//        }
//        textField.setText( item.ref.getName());
//      }
//    });

    matchesList.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
          listener.doOk();
        } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
          listener.doCancel();
        }
      }
    });

    matchesList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int cellIndex = matchesList.locationToIndex(e.getPoint());
        if (cellIndex == -1) {
          return;
        }
        if (e.getClickCount() == 2) {
          listener.doOk();
        }
        //matchesList.setSelectedIndex(cellIndex);
        final BinCITypeRefWrapper item =
            (BinCITypeRefWrapper) matchesList.getSelectedValue();
        if (item == null) {
          return;
        }
      }
    });

    searchingPanel = new JPanel(new BorderLayout());
    searchingPanel.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 2, 3),
        BorderFactory.createEtchedBorder()));

    searchingPanel.add(textField, BorderLayout.NORTH);
    searchingPanel.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(AncestorEvent event) {
        textField.requestFocus();
      }

      public void ancestorMoved(AncestorEvent event) {
      }

      public void ancestorRemoved(AncestorEvent event) {
      }
    });

    searchingPanel.add(new JScrollPane(matchesList));

    //searchingPanel.add(left,BorderLayout.SOUTH);

//    center.add(helpPanel, BorderLayout.NORTH);

    //center.add(left, BorderLayout.CENTER);

    center.add(searchingPanel, BorderLayout.CENTER);

//    JPanel contentPane = new JPanel();
    JPanel contentPane = this;

    contentPane.setLayout(new BorderLayout());
    contentPane.add(center, BorderLayout.CENTER);
    //contentPane.add(createButtonsPanel(), BorderLayout.SOUTH);

    //    contentPane.setPreferredSize(new Dimension(595, 380));
    contentPane.setPreferredSize(new Dimension(450, 350));

//    ok.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        doOk();
//      }
//    });
//
//    ok.setEnabled(false);

//    cancel.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        doCancel();
//      }
//    });
  }

  public void onShow() {
    textField.requestFocus();
    textField.selectAll();
//    textField.setCaretPosition(textField.getText().length());
  }

  private void dispose() {
    //table.getTree().removeTreeSelectionListener(listener);
    //this.removeListeners(this.ok, ActionListener.class);
    //this.removeListeners(this.cancel, ActionListener.class);
    //this.left.setComponent(null);
//    dialog.dispose();
  }

  public void doOk() {
    int maxIndex = matchesList.getModel().getSize() - 1;
    BinCITypeRefWrapper item;

    if (maxIndex == 0) {
      matchesList.setSelectedIndex(0);
    }

    if (maxIndex < 0 /*|| matchesList.isSelectionEmpty()*/) {
      selectedNode = null;
      //      System.out.println("not valid selection, index=" + maxIndex);
    } else {
      item = (BinCITypeRefWrapper) matchesList.getSelectedValue();
      if (item != null) {
        selectedNode = item.ref;
      } else {
        //      System.out.println("item == null");
        selectedNode = null;
      }
    }

    dispose();
  }

  public void doCancel() {
    selectedNode = null;
    dispose();
  }

  private void filterMatches(String className) {
    if (className.equals("") || previousInput.equals(className)) {
      return;
    }

    String className1 = className.toLowerCase();
    BinCITypeRefWrapper item;

    Iterator iter = matches.iterator();
    while (iter.hasNext()) {
      item = (BinCITypeRefWrapper) iter.next();

      if (className.indexOf(".") == -1) {
        String name = item.ref.getName().toLowerCase();
        if (!name.startsWith(className1)) {
          iter.remove();
        }
      } else {
        String qName = item.ref.getQualifiedName().toLowerCase();
        if (!qName.startsWith(className1)) {
          iter.remove();
        }
      }
    }
  }

  void initItems() {
    matches = new ArrayList();

    //add primitive types
    BinTypeRef[] primTypes = project.getPrimitiveTypeRefs();
    for(int i=0; i < primTypes.length; i++) {
      matches.add(new BinCITypeRefWrapper(primTypes[i]));
    }

    if (includeClassPath) {
      BinPackage packages[] = project.getAllPackages();
      for (int i = 0; i < packages.length; ++i) {
        for (Iterator it = packages[i].getAllTypes(); it.hasNext(); ) {
          BinTypeRef item = (BinTypeRef) it.next();
          item.getAllSupertypes(); // retrieve all binary supertypes
          matches.add(new BinCITypeRefWrapper(item));
          alreadyAddedTypesByQualifiedName.add(item);
        }
      }
    } else {
      List allTypes = project.getDefinedTypes();

      for (int i = 0; i < allTypes.size(); ++i) {
        BinTypeRef typeRef = (BinTypeRef) allTypes.get(i);
//        if (typeRef.getName().toLowerCase()
//            .startsWith(className.toLowerCase())) {
        BinCITypeRefWrapper item = new BinCITypeRefWrapper(typeRef);
        alreadyAddedTypesByQualifiedName.add(item);
        matches.add(item);
//        }
      }
    }

    Collections.sort(matches, new Comparator() {

      public int compare(Object o1, Object o2) {
        BinCITypeRefWrapper ref1 = (BinCITypeRefWrapper) o1,
            ref2 = (BinCITypeRefWrapper) o2;

        return ref1.ref.getName().compareTo(ref2.ref.getName());
      }

    });
  }

  private Set alreadyAddedTypesByQualifiedName = new HashSet();

  private void tryTofindByQualifiedName(String className) {
    if (includeClassPath && className != null && !"".equals(className)) {
      BinPackage packages[] = project.getAllPackages();
      for (int i = 0; i < packages.length; ++i) {
        addTypeRefWrapperToMatchesIfPossible(
            project.getTypeRefForName(
            packages[i].getQualifiedDisplayName() + "." + className));
      }
      addTypeRefWrapperToMatchesIfPossible(project.getTypeRefForName(className));
    }
  }

  private void addTypeRefWrapperToMatchesIfPossible(BinTypeRef ref) {
    if (ref != null) {
      if (!alreadyAddedTypesByQualifiedName.contains(ref)) {
        matches.add(new BinCITypeRefWrapper(ref));
        alreadyAddedTypesByQualifiedName.add(ref);
      }
    }
  }

  void process(String str) {
    if (!str.equals(previousInput)) {
      tryTofindByQualifiedName(str);
    }
    if (!str.startsWith(previousInput)) {
      initItems();
      filterMatches(str);
    } else {
      filterMatches(str);
    }
    // don't show otherwise, too much elements
    if (!str.equals("")) {
      matchesList.setListData(matches.toArray());
      if (matches.size() > 0) {
        matchesList.setSelectedIndex(0);
        matchesList.ensureIndexIsVisible(0);
      }

    } else {
      matchesList.setListData(new Object[0]);
    }

//    if (matchesList.getMaxSelectionIndex() != -1) {
//
//    } else {
//      System.out.println("max visible == -1");
//    }

    previousInput = str;
  }

  public String getUserInput() {
    return textField.getText();
  }

  private final class MyCaretListener implements CaretListener {
    public void caretUpdate(CaretEvent event) {
      String str = textField.getText();
      process(str);
//System.out.println("caret update");
    }
  }


  public BinTypeRef getTypeRef() {
    return selectedNode;
  }
}
