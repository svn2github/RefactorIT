/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common.usesupertype;


import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeUtil;
import net.sf.refactorit.ui.BinCITypeRefWrapper;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.TypeInputPanel;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class UseSupertypeInputDialog extends AbstractUseSupertypeInputDialog {

  private ContentPanel contentPanel;

  public UseSupertypeInputDialog(
      RefactorItContext context, String title,
      BinTypeRef supertype
      ) {
    super(context);
    dialog.setTitle(title);

    contentPanel = new ContentPanel(supertype);
    contentPanel.setPreferredSize(new Dimension(350, 200));
  }

  static class TypesSelectionPanel extends JPanel {
    JList typesList;
    public TypesSelectionPanel(String headerStr, String footerStr) {
      this(headerStr, footerStr, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public TypesSelectionPanel(String headerStr, String footerStr,
        int selectionModel) {
      super(new BorderLayout(5, 5));

//      GridBagConstraints constraints = new GridBagConstraints();
//      constraints.gridx = 1;
//      constraints.gridy = 0;
//      constraints.fill = GridBagConstraints.BOTH;
//      constraints.anchor = GridBagConstraints.NORTH;
//      constraints.weightx = 1.0;
//      constraints.weighty = 0.0;
//      constraints.insets = new Insets(5, 5, 0, 5);

      typesList = new JList();
      typesList.setSelectionMode(selectionModel);

//      typesList.setBorder(BorderFactory.createLineBorder(Color.white, 1));

      if (headerStr != null) {
        add(new JLabel(headerStr), BorderLayout.NORTH);
      }

      add(new JScrollPane(typesList), BorderLayout.CENTER);

      if (footerStr != null) {
        add(new JLabel(footerStr), BorderLayout.SOUTH);
      }
    }

    public List getSelectedBinTypeRefs() {
      List result = new ArrayList();
      Object values[] = typesList.getSelectedValues();

      for (int i = 0; i < values.length; i++) {
        if (values[i] == null) {
          continue;
        }

        BinCITypeRefWrapper wrapper = (BinCITypeRefWrapper) values[i];
        result.add(wrapper.getItem());
      }
      return result;
    }

    public void clearSelection() {
      typesList.getSelectionModel().clearSelection();
    }

    /**
     * @param list
     */
    public void setTypes(List list) {
      BinTypeRef[] types = (BinTypeRef[]) list.toArray(new BinTypeRef[list.size()]);

      setTypes(types);
    }

    public void setTypes(BinTypeRef[] types) {
      ArrayList data = new ArrayList(types.length);
      for (int i = 0; i < types.length; ++i) {
        data.add(new BinCITypeRefWrapper(types[i]));
      }
      typesList.setListData(data.toArray(new BinCITypeRefWrapper[data.size()]));
      if (types.length > 0) {
        typesList.setSelectedIndex(0);
      }
    }

    public void selectAll() {
      typesList.getSelectionModel().addSelectionInterval(0,
          typesList.getModel().getSize() - 1);
    }
  }


  class ContentPanel extends JPanel {

    private TypeInputPanel supertypeInputPanel;

    private TypesSelectionPanel typesListPanel;

    /**
     * @param supertype list of {@link BinTypeRef}s
     */
    ContentPanel(BinTypeRef supertype) {
      super(new BorderLayout(5, 5));

      String headerStr = "to replace usages of selected subtype(s)";
      String footerString = "Note: only subtypes which have usages are listed.";
      typesListPanel = new TypesSelectionPanel(headerStr, footerString);
      setSupertype(supertype);
      JPanel supertypePanel = createSupertypePanel(supertype);

      add(supertypePanel, BorderLayout.NORTH);
      add(typesListPanel, BorderLayout.CENTER);
    }

    private JPanel createSupertypePanel(BinTypeRef supertype) {
      JPanel superTypePanel = new JPanel(new GridBagLayout());

      supertypeInputPanel = new TypeInputPanel(context,
          supertype != null ? supertype.getQualifiedName() : "");

      supertypeInputPanel.addTypeChangedListener(new TypeInputPanel.
          TypeChangedListener() {

        public void onChange(BinTypeRef type) {
          setSupertype(type);
        }
      });

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 1;
      constraints.gridy = 0;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.anchor = GridBagConstraints.NORTH;
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.insets = new Insets(5, 0, 0, 5);

      superTypePanel.add(new JLabel("Use super type"), constraints);

      constraints.gridx = 2;
      constraints.weightx = 1.0;
      constraints.insets = new Insets(5, 0, 0, 0);
      superTypePanel.add(supertypeInputPanel, constraints);

      return superTypePanel;
    }

    private void setSupertype(BinTypeRef ref) {
      typesListPanel.clearSelection();

      final JButton buttonOk = buttonPanel.buttonOk;

      if (ref == null) {
        buttonOk.setEnabled(false);
        typesListPanel.setTypes(CollectionUtil.EMPTY_ARRAY_LIST);
      } else {
        List subtypes = new ArrayList(UseSuperTypeUtil.getAllSubtypes(ref));
        for (Iterator iter = subtypes.iterator(); iter.hasNext();) {
          // don't show anonymous types
          if (((BinTypeRef) iter.next()).getBinCIType().isAnonymous()) {
            iter.remove();
          }
        }
        Collections.sort(subtypes, BinTypeRef.QualifiedNameSorter.getInstance());

        typesListPanel.setTypes(subtypes);

        if (subtypes.size() > 0) {
          typesListPanel.selectAll();
          buttonOk.setEnabled(true);
        } else {
          buttonOk.setEnabled(false);
        }
      }

    }

    public List getSelectedSubtypes() {
      List result = typesListPanel.getSelectedBinTypeRefs();
      return result;
    }
  }

  protected JPanel createHelpPanel() {
    JPanel help = DialogManager.getHelpPanel(
        "Select supertype and then subtypes to replace with this supertype");
    return help;
  }

  protected JPanel createContentPanel() {
    return contentPanel;
  }

  public List getSelectedSubtypes() {
    return contentPanel.getSelectedSubtypes();
  }

  public BinTypeRef getSelectedSupertype() {
    return contentPanel.supertypeInputPanel.getSelectedType();
  }
}
