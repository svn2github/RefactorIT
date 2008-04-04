/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.audit;

import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AuditRunner;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.reports.Statistics;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.ProfileDialog;

import net.sourceforge.pmd.Rule;



public class AuditAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.AuditAction";
  public static final String NAME = "Audits";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return NAME;
  }

  /*
   * @see net.sf.refactorit.ui.module.RefactorItAction#isMultiTargetsSupported()
   */
  public boolean isMultiTargetsSupported() {
    return true;
  }
  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }

    return false;

  }


  /*
   * @see net.sf.refactorit.ui.module.RefactorItAction#isReadonly()
   */
  public boolean isReadonly() {
    return true;
  }

  public boolean run(final RefactorItContext context, final Object inObject) {
    final Object object = RefactorItActionUtils.unwrapTarget(inObject);

    if (object instanceof BinCIType) {
      if (!((BinCIType) object).isFromCompilationUnit()) {
        DialogManager.getInstance()
            .showNonSourcePathItemInfo(context, getName(), object);
        return false;
      }
    }

    Profile selected = (Profile) context.getState();
    if (selected == null) {
      selected = ProfileDialog.showAudit();

      // User dismissed this dialog
      if (selected == null) {
        return false;
      }

      context.setState(selected);
    }

    
    AuditRule[] rules = Audit.createActiveRulesForStatisticsLogging(selected);
    Rule[] pmdRules = Audit.createActivePMDRulesForStatisticsLogging(selected);
    Statistics s = Statistics.getInstance();
    for(int i=0; i<rules.length; i++)
      s.addUsage(Statistics.CATEGORY_AUDITS, rules[i].getKey(), rules[i].getAuditName(), rules[i].getCategoryName());
    for(int i=0; i<pmdRules.length; i++)
      s.addUsage(Statistics.CATEGORY_AUDITS, 
          (new Audit(pmdRules[i])).getKey(), 
          StringUtil.splitCamelStyleIntoWords(pmdRules[i].getName()), 
          null);

    AuditRunner ar = new AuditRunner((TreeRefactorItContext) context);
    ar.doAudit(this, AuditRunner.getCompilationUnits(object, context), object);

    return true;
  }

  public char getMnemonic() {
    return 'A';
  }

  // FIXME this should be somewhere else, but not here if needed...
//  AuditTreeTable.Model getModel() {
//    return this.model;
//  }
//
//  void setModel(AuditTreeTable.Model model) {
//    this.model = model;
//  }
}
