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
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.common.usesupertype.UseSupertypeInputDialog.TypesSelectionPanel;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.util.List;

/**
 * SingleUseSupertypeDialog - for displaying input for changing single usage:
 * variable, method return value etc
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga </a>
 * @version $Revision: 1.5 $ $Date: 2004/12/29 14:55:58 $
 */
public class SingleUseSupertypeDialog extends AbstractUseSupertypeInputDialog {

  private SingleUsageContentPanel contentPanel;
  private String targetDescr;

  public SingleUseSupertypeDialog(RefactorItContext context, String targetDescr,
      BinTypeRef[] possibleSupertypes) {
    super(context);
    this.targetDescr = targetDescr;
    contentPanel = new SingleUsageContentPanel(possibleSupertypes);
  }

  class SingleUsageContentPanel extends JPanel {
    private TypesSelectionPanel typesListPanel;

    public SingleUsageContentPanel(BinTypeRef[] possibleSupertypes) {
      super(new BorderLayout());
      typesListPanel = new TypesSelectionPanel(null,
          null, ListSelectionModel.SINGLE_SELECTION);
      typesListPanel.setTypes(possibleSupertypes);
      this.add(typesListPanel,BorderLayout.CENTER);
    }
  }

  protected JPanel createHelpPanel() {
    JPanel help = DialogManager.getHelpPanel(
        "Select supertype to use for " + targetDescr);
    return help;
  }

  /**
   * @see AbstractUseSupertypeInputDialog#createContentPanel()
   */
  protected JPanel createContentPanel() {
    return contentPanel;
  }

  public BinTypeRef getSelectedSupertype() {
    List refs = contentPanel.typesListPanel.getSelectedBinTypeRefs();
    if ( refs.isEmpty() ) {
      return null;
    }
    return (BinTypeRef) refs.get(0);
  }

}
