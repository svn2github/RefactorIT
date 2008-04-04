/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.reports.Statistics;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;


/**
 *
 *
 * @author Igor Malinin
 * @author Risto
 */
public class ProfileDialog {
  final RitDialog dialog;

  boolean isOkPressed;

  private ProfileDialog(IdeWindowContext context, ProfileType c) {
    dialog = RitDialog.create(context);
    dialog.setTitle(c.getName());
  }

  private void init(ProfilePanel panel, String helpKey) {
//    dialog.setSize(750, 550);

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEtchedBorder());
    center.add(createHelpPanel(), BorderLayout.NORTH);
    center.add(panel);
   
    Container content = dialog.getContentPane();
    content.add(center);
    content.add(createButtonsPanel(helpKey), BorderLayout.SOUTH);
  }

  private JComponent createHelpPanel() {
    return DialogManager.getHelpPanel("Press F1 for help");
  }

  public static MetricsModel.State showMetrics() {
    ProfilePanel panel = new ProfilePanel(new MetricsProfileType());

    if (showDialog(panel, "refact.metrics").isOkPressed) {
      MetricsModel.State state = new MetricsModel.State();
      state.setProfile(panel.getProfile());
			logMetrics(state);
			
      return state;
    }

    return null;
  }

  public static Profile showAudit() {
    return showAudit(null);
  }

  public static Profile showAudit(Profile profile) {
    final AuditProfileType profileType = new AuditProfileType();
    ProfilePanel panel = new ProfilePanel(profileType, profile);

    if (showDialog(panel, "refact.audit").isOkPressed) {
      return panel.getProfile();
    }
    
    if (profile == null) {
      profile = profileType.createDefaultProfile();
    }
    return null;
  }

  private static ProfileDialog showDialog(ProfilePanel panel, String helpKey) {
    ProfileDialog dialog = new ProfileDialog(
        IDEController.getInstance().createProjectContext(),
        panel.getProfileType());

    dialog.init(panel, helpKey);
    dialog.show();

    if (dialog.isOkPressed) {
      panel.saveCurrentProfile();
    }

    return dialog;
  }

  private void show() {
    dialog.show();
  }

  private JPanel createButtonsPanel(String helpKey) {
    JPanel result = new JPanel();

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = true;
        dialog.dispose();
      }
    });

    result.add(okButton);
    
    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(cancelActionListener);

    result.add(cancelButton);

    JButton helpButton = new JButton("Help");
    result.add(helpButton);

    HelpViewer.attachHelpToDialog(dialog, helpButton, helpKey);
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        helpButton, cancelActionListener);

    return result;
  }
	
	private static void logMetrics(MetricsModel.State state){
		Statistics s = Statistics.getInstance();
		ResourceBundle resLocalizedStrings = ResourceUtil.getBundle(MetricsAction.class);
		try{
			if(state.isAbstractnessRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(15), resLocalizedStrings.getString(MetricsAction.getDefaultKey(15) + ".tooltip"), "Object oriented");
       if(state.isCaRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(12), resLocalizedStrings.getString(MetricsAction.getDefaultKey(12) + ".tooltip"), "Object oriented");
       if(state.isCcRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(1), resLocalizedStrings.getString(MetricsAction.getDefaultKey(1) + ".tooltip"), "Object oriented");
       if(state.isCeRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(13), resLocalizedStrings.getString(MetricsAction.getDefaultKey(13) + ".tooltip"), "Object oriented");
       if(state.isClocRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(4), resLocalizedStrings.getString(MetricsAction.getDefaultKey(4) + ".tooltip"), "Object oriented");
       if(state.isCycRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(21), resLocalizedStrings.getString(MetricsAction.getDefaultKey(21) + ".tooltip"), "Object oriented");
       if(state.isDcRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(5), resLocalizedStrings.getString(MetricsAction.getDefaultKey(5) + ".tooltip"), "Object oriented");
       if(state.isDcycRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(22), resLocalizedStrings.getString(MetricsAction.getDefaultKey(22) + ".tooltip"), "Object oriented");
       if(state.isDipRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(24), resLocalizedStrings.getString(MetricsAction.getDefaultKey(24) + ".tooltip"), "Object oriented");
       if(state.isDistanceRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(16), resLocalizedStrings.getString(MetricsAction.getDefaultKey(16) + ".tooltip"), "Object oriented");
       if(state.isDitRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(10), resLocalizedStrings.getString(MetricsAction.getDefaultKey(10) + ".tooltip"), "Object oriented");
       if(state.isEpRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(25), resLocalizedStrings.getString(MetricsAction.getDefaultKey(25) + ".tooltip"), "Object oriented");
       if(state.isExecRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(7), resLocalizedStrings.getString(MetricsAction.getDefaultKey(7) + ".tooltip"), "Object oriented");
       if(state.isInstabilityRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(14), resLocalizedStrings.getString(MetricsAction.getDefaultKey(14) + ".tooltip"), "Object oriented");
       if(state.isLcomRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(28), resLocalizedStrings.getString(MetricsAction.getDefaultKey(28) + ".tooltip"), "Object oriented");
       if(state.isLocRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(2), resLocalizedStrings.getString(MetricsAction.getDefaultKey(2) + ".tooltip"), "Object oriented");
       if(state.isLspRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(23), resLocalizedStrings.getString(MetricsAction.getDefaultKey(23) + ".tooltip"), "Object oriented");
      if(state.isMqRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(26), resLocalizedStrings.getString(MetricsAction.getDefaultKey(26) + ".tooltip"), "Object oriented");
      if(state.isNclocRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(3), resLocalizedStrings.getString(MetricsAction.getDefaultKey(3) + ".tooltip"), "Object oriented");
      if(state.isNocRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(11), resLocalizedStrings.getString(MetricsAction.getDefaultKey(11) + ".tooltip"), "Object oriented");
      if(state.isNofRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(29), resLocalizedStrings.getString(MetricsAction.getDefaultKey(29) + ".tooltip"), "Object oriented");
      if(state.isNotaRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(18), resLocalizedStrings.getString(MetricsAction.getDefaultKey(18) + ".tooltip"), "Object oriented");
      if(state.isNotcRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(19), resLocalizedStrings.getString(MetricsAction.getDefaultKey(19) + ".tooltip"), "Object oriented");
      if(state.isNoteRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(20), resLocalizedStrings.getString(MetricsAction.getDefaultKey(20) + ".tooltip"), "Object oriented");
      if(state.isNotRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(17), resLocalizedStrings.getString(MetricsAction.getDefaultKey(17) + ".tooltip"), "Object oriented");
      if(state.isNpRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(6), resLocalizedStrings.getString(MetricsAction.getDefaultKey(6) + ".tooltip"), "Object oriented");
			if(state.isNtRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(27), resLocalizedStrings.getString(MetricsAction.getDefaultKey(27) + ".tooltip"), "Object oriented");
			if(state.isRfcRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(9), resLocalizedStrings.getString(MetricsAction.getDefaultKey(9) + ".tooltip"), "Object oriented");
			if(state.isWmcRun())
				s.addUsage(Statistics.CATEGORY_METRICS, MetricsAction.getDefaultKey(8), resLocalizedStrings.getString(MetricsAction.getDefaultKey(8) + ".tooltip"), "Object oriented");
		}catch(Exception e){}
	}
}
