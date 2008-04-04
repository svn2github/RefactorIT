/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits;



import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.audit.SkipUnskipAction;
import net.sf.refactorit.audit.rules.RedundantCastRule;
import net.sf.refactorit.audit.rules.RemoveRedundantCast;
import net.sf.refactorit.audit.rules.ShadingRule;
import net.sf.refactorit.audit.rules.modifiers.StaticMethodProposalRule;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Vadim Hahhulin
 * @author Anton Safonov
 */
public class AuditTest2 extends RefactoringTestCase {
  /** Logger instance. */
  private static final Category cat
      = Category.getInstance(AuditTest2.class.getName());

  public AuditTest2(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(AuditTest2.class);
    suite.setName("Audit tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public String getTemplate() {
    return "Audit/<stripped_test_name>/<in_out>";
  }

  private RuleViolation[] runAudit(Project project, Class auditClass) {
    Audit audit = new Audit(auditClass);
    AuditRule rule = audit.createAuditingRule();
    DelegatingVisitor supervisor = new DelegatingVisitor(true);
    supervisor.registerDelegate(rule);
    rule.setSupervisor(supervisor);

    List sources = project.getCompilationUnits();
    for (int i = 0, max = sources.size(); i < max; i++) {
      supervisor.visit((CompilationUnit) sources.get(i));
    }

    return (RuleViolation[]) rule.getViolations().toArray(new RuleViolation[0]);
  }

  private RuleViolation[] checkViolations(Class auditClass,
      String[] violationsStrings,
      String[] skippedViolationsStrings) throws Exception {

    cat.info("Testing " + getStrippedTestName());
    Project project = getMutableProject();

    RuleViolation[] violations = runAudit(project, auditClass);
    List ruleViolations = new ArrayList();
    List skippedRuleViolations = new ArrayList();
    for (int i = 0; i < violations.length; i++) {
      if (violations[i].isSkipped()) {
        skippedRuleViolations.add(violations[i]);
      } else {
        ruleViolations.add(violations[i]);
      }
    }

    assertTrue("quantity of result violations is not equal to expected:" +
        "result:" + ruleViolations.size() + ";" +
        "expected:" + violationsStrings.length,
        (violationsStrings.length == ruleViolations.size()));

    assertTrue(
        "quantity of result skipped violations is not equal to expected:" +
        "result:" + skippedRuleViolations.size() + ";" +
        "expected:" + skippedViolationsStrings.length,
        (skippedViolationsStrings.length == skippedRuleViolations.size()));

    for (int i = 0, max = ruleViolations.size(); i < max; i++) {
      RuleViolation resultViolation = (RuleViolation) ruleViolations.get(i);
      assertTrue("result violation:" + resultViolation.getMessage() + "; " +
          "expected:" + violationsStrings[i],
          violationsStrings[i].equals(resultViolation.getMessage()));
    }

    for (int i = 0, max = skippedRuleViolations.size(); i < max; i++) {
      RuleViolation resultViolation = (RuleViolation) skippedRuleViolations.get(
          i);
      assertTrue("result violation:" + resultViolation.getMessage() + "; " +
          "expected:" + skippedViolationsStrings[i],
          skippedViolationsStrings[i].equals(resultViolation.getMessage()));
    }

    return violations;
  }

  private void switchSkip(final RuleViolation[] violations,
      final String members, final boolean skip) throws Exception {

    StringTokenizer mems = new StringTokenizer(members, ",");
    List memberNames = new ArrayList();
    while (mems.hasMoreTokens()) {
      memberNames.add(mems.nextToken());
    }
    List violationsToChange = new ArrayList();
    for (int i = 0; i < violations.length; i++) {
      final boolean skipped = violations[i].isSkipped();
      if (((skip && !skipped) || (!skip && skipped))
          && memberNames.contains(violations[i].getOwnerMember().getName())) {
        violationsToChange.add(violations[i]);
      }
    }

    Project project = fixViolations(violationsToChange, SkipUnskipAction.KEY);

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);
  }

  private Project fixViolations(final List violations, final String actionKey) {
    if (violations.size() == 0) {
      return null;
    }

    final Project project
        = ((RuleViolation) violations.get(0)).getBinTypeRef().getProject();
    if (actionKey == SkipUnskipAction.KEY) {
      SkipUnskipAction.run(violations);
    } else {
      Iterator correctiveActions = ((RuleViolation) violations.get(0)).
          getCorrectiveActions().iterator();
      while (correctiveActions.hasNext()) {
        CorrectiveAction action = (CorrectiveAction) correctiveActions.next();
        if (action.getKey().equals(actionKey)) {
          action.run(new NullContext(project), violations);
          break;
        }
      }
    }

    return project;
  }

  public void testAddSkipTag1() throws Exception {
    String[] violations = new String[] {"Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field"};
    String[] skippedViolations = new String[] {"Variable shades a field: field"};

    RuleViolation[] ruleViolations
        = checkViolations(ShadingRule.class, violations, skippedViolations);
    switchSkip(ruleViolations, "f2,f3,f4,f5,f6", true);
  }

  public void testDeleteSkipTag1() throws Exception {
    String[] violations = new String[] {"Variable shades a field: field"};
    String[] skippedViolations = new String[] {
        "Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field",
        "Variable shades a field: field"};

    RuleViolation[] ruleViolations
        = checkViolations(ShadingRule.class, violations, skippedViolations);
    switchSkip(ruleViolations, "f1,f2,f3,f4,f5,f6", false);
  }

  public void testBug2187() throws Exception {
    final Project mutableProject = getMutableProject();
    RuleViolation[] ruleViolations
        = runAudit(mutableProject, RedundantCastRule.class);
    assertTrue("Project is ok"
        + CollectionUtil.toList((mutableProject.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()),
        !(mutableProject.getProjectLoader().getErrorCollector()).hasErrors());
    assertEquals("Some rule violations", 1, ruleViolations.length);
    Project project = fixViolations(
        Arrays.asList(ruleViolations), RemoveRedundantCast.KEY);
    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);
  }

  public void testBug2188() throws Exception {
    final Project mutableProject = getMutableProject();
    RuleViolation[] ruleViolations
        = runAudit(mutableProject, StaticMethodProposalRule.class);
    assertTrue("Project is ok"
        + CollectionUtil.toList((mutableProject.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()),
        !(mutableProject.getProjectLoader().getErrorCollector()).hasErrors());
    assertEquals("No rule violations", 0, ruleViolations.length);
  }
}
