/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.netbeans.common.action.RitActionToNbSystemActionMapper;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RefactorItActionUtils;

import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Tonis Vaga
 */
public class NBAction implements ActionListener {
  private ActionProxy action;

  public NBAction(ActionProxy action) {
    setAction(action);
  }

  public String getName() {
    return action.getName();
  }

  public void actionPerformed(ActionEvent e) {
    try {
	    if (action instanceof IdeAction) {
	        RefactorItActionUtils.run((IdeAction) action);
	    }
    } catch (ProjectNotFoundException ex) {
      JOptionPane.showMessageDialog(
          WindowManager.getDefault().getMainWindow(), 
          "No open projects.\n" +
          "(Select one in the \"Projects\" window and try again.)",
          GlobalOptions.REFACTORIT_NAME,
          JOptionPane.INFORMATION_MESSAGE);
    } catch(Exception exx) {
      ErrorManager.showAndLogInternalError(exx);
    }
  }

  public ActionProxy getAction() {
    return action;
  }

  public void setAction(final ActionProxy action) {
    this.action = action;
  }

  public JMenuItem createMenuItem(boolean isEnabled) {
    JMenuItem item = new JMenuItem(getName());
    item.addActionListener(this);

    item.setAccelerator((KeyStroke) getNbSystemAction().getValue(Action.
        ACCELERATOR_KEY));
    item.setIcon((Icon) getNbSystemAction().getValue(Action.SMALL_ICON));

    if (getMnemonic() != '\0') {
      item.setMnemonic(getMnemonic());
    }

    item.setEnabled(isEnabled);

    return item;
  }

  public SystemAction getNbSystemAction() {
    return RitActionToNbSystemActionMapper.getSystemAction(getAction());
  }

  public char getMnemonic() {
    return getAction().getMnemonic();
  }
}
