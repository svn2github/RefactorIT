/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.cli.actions;


import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AuditRunner;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.ProfileUtil;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.util.HashSet;


public class AuditModelBuilder implements ModelBuilder {
  public final String profileFilename;

  public AuditModelBuilder() {
    this("");
  }

  public AuditModelBuilder(String profileFilename) {
    this.profileFilename = profileFilename;
  }

  public BinTreeTableModel populateModel(Project p) {
    Profile profile = getProfile(profileFilename);
    AuditTreeTableModel model = new AuditTreeTableModel();
    AuditRule[] rules = Audit.createActiveRulesAndSetToModel(profile, model);
    AuditRunner runner = new AuditRunner(model);
    //AuditTreeTableModel model = runner.getAuditModel();
    BinTreeTableModel result = runner.findViolationsToModel(
        new HashSet(p.getCompilationUnits()),
        rules);
    return result;
  }

  private Profile getProfile(String profileFilename) {
    if (profileFilename.equals("")) {
      return Profile.createDefaultAudit();
    } else {
      return ProfileUtil.createProfile(profileFilename);
    }
  }

  public boolean supportsProfiles() {
    return true;
  }
}
