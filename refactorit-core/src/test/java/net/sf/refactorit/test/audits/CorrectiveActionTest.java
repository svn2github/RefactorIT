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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.refactorings.javadoc.Javadoc;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.audits.corrective.AddCastToFloatActionTest;
import net.sf.refactorit.test.audits.corrective.AddDefaultConstructorTest;
import net.sf.refactorit.test.audits.corrective.AddFinalModifierTest;
import net.sf.refactorit.test.audits.corrective.AddSerializableToSuperclassTest;
import net.sf.refactorit.test.audits.corrective.AddVersionUIDActionTest;
import net.sf.refactorit.test.audits.corrective.ApplyMinimalAccessActionTest;
import net.sf.refactorit.test.audits.corrective.ChangeToEqualsActionTest;
import net.sf.refactorit.test.audits.corrective.CommentNotUsedMemberActionTest;
import net.sf.refactorit.test.audits.corrective.CommentNotUsedTypeActionTest;
import net.sf.refactorit.test.audits.corrective.ConcatEmptyStringExpressionTest;
import net.sf.refactorit.test.audits.corrective.CorrectFloatEqualComparisionTest;
import net.sf.refactorit.test.audits.corrective.CorrectModifiersOrderTest;
import net.sf.refactorit.test.audits.corrective.CorrectVersionUIDActionTest;
import net.sf.refactorit.test.audits.corrective.DeleteFromSignatureActionTest;
import net.sf.refactorit.test.audits.corrective.DuplicateStringsTest;
import net.sf.refactorit.test.audits.corrective.EarlyDeclarationTest;
import net.sf.refactorit.test.audits.corrective.EmbraceArithmExpressionActionTest;
import net.sf.refactorit.test.audits.corrective.FinalizeLocalsActionTest;
import net.sf.refactorit.test.audits.corrective.FinalizeParamsActionTest;
import net.sf.refactorit.test.audits.corrective.ForLoopConditionOptimizeTest;
import net.sf.refactorit.test.audits.corrective.ForinTest;
import net.sf.refactorit.test.audits.corrective.GenericsArgumentsAnalyzerTest;
import net.sf.refactorit.test.audits.corrective.InsertEmptyBlockCommentActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveBracketsActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveNotUsedMemberActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveNotUsedTypeActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveRedundantBoxingActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveRedundantThrowsTest;
import net.sf.refactorit.test.audits.corrective.RemoveRedundantUnboxingActionTest;
import net.sf.refactorit.test.audits.corrective.RemoveUnusedLocalVariableTest;
import net.sf.refactorit.test.audits.corrective.ReplaceBoolEquationAssignmentTest;
import net.sf.refactorit.test.audits.corrective.SingleAssignmentPrivateFieldTest;
import net.sf.refactorit.test.loader.ProjectLoadTest;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.ui.options.profile.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 *
 * @author Arseni Grigorjev
 */
public abstract class CorrectiveActionTest extends RefactoringTestCase{

  protected Profile profile;

  public static Test suite() {
    TestSuite result = new TestSuite("Corrective actions tests");

    // corrective actions tests
    result.addTest(AddCastToFloatActionTest.suite());
    result.addTest(AddVersionUIDActionTest.suite());
    result.addTest(CorrectVersionUIDActionTest.suite());
    result.addTest(CorrectFloatEqualComparisionTest.suite());
    result.addTest(AddDefaultConstructorTest.suite());
    result.addTest(AddSerializableToSuperclassTest.suite());
    result.addTest(InsertEmptyBlockCommentActionTest.suite());
    result.addTest(ChangeToEqualsActionTest.suite());
    result.addTest(CorrectModifiersOrderTest.suite());
    result.addTest(AddFinalModifierTest.suite());
    result.addTest(RemoveBracketsActionTest.suite());
    result.addTest(FinalizeParamsActionTest.suite());
    result.addTest(FinalizeLocalsActionTest.suite());
    result.addTest(RemoveRedundantBoxingActionTest.suite());
    result.addTest(RemoveRedundantUnboxingActionTest.suite());
    result.addTest(ConcatEmptyStringExpressionTest.suite());
    result.addTest(EmbraceArithmExpressionActionTest.suite());
    result.addTest(ReplaceBoolEquationAssignmentTest.suite());
    result.addTest(RemoveUnusedLocalVariableTest.suite());
    result.addTest(DeleteFromSignatureActionTest.suite());
    result.addTest(RemoveNotUsedMemberActionTest.suite());
    result.addTest(RemoveNotUsedTypeActionTest.suite());
    result.addTest(CommentNotUsedMemberActionTest.suite());
    result.addTest(CommentNotUsedTypeActionTest.suite());
    result.addTest(ForinTest.suite());
    result.addTest(ApplyMinimalAccessActionTest.suite());
    result.addTest(ForLoopConditionOptimizeTest.suite());
    result.addTest(SingleAssignmentPrivateFieldTest.suite());
    result.addTest(RemoveRedundantThrowsTest.suite());
    // generics analyzer test
    result.addTest(GenericsArgumentsAnalyzerTest.suite());
    result.addTest(EarlyDeclarationTest.suite());
    result.addTest(DuplicateStringsTest.suite());

    return result;
  }

  protected void setUp() throws Exception {
    this.profile = Profile.createDefaultAudit();
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(2));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(2));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, "true");
  }

  public CorrectiveActionTest(final String name) {
    super(name);
  }

  public String getName() {
    return StringUtil.capitalizeFirstLetter(super.getName());
  }
  /**
   * This method builds a mutable project, runs specified audit on it,
   * and tryes to resolve violations with specified corrective action.
   * Then it compares modified input project with expected output proj.
   * It also compares the expected violation count number (wich he takes
   * from the javadoc in the source file) with gotten during runtime
   * violations count.
   *
   * @param rule audit rule to run on the project
   * @param actionKey key of the action
   *
   * @throws Exception
   */
  protected void performTest(AuditRule rule, String actionKey)
      throws Exception {

    Project project = getInitialProject();
    
    performProjectLoadTest(project);
    
    project = getMutableProject(project);
    renameToOut(project);

    assertTrue("has sources", project.getCompilationUnits().size() > 0);

    if ((project.getProjectLoader().getErrorCollector()).hasErrors()) {
      fail("shouldn't have errors: "
          + CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()));
    }

    List sources = project.getCompilationUnits();
    
    for(int k = 0; k < sources.size(); k++) {
      CompilationUnit testSource = (CompilationUnit) sources.get(k);
  
      Iterator javadocTags = findTypeTags(testSource.getMainType()).iterator();
  
      Javadoc.Tag violationsTag = null;
      while (javadocTags.hasNext()) {
        Javadoc.Tag tag = (Javadoc.Tag) javadocTags.next();
  
        if (tag.getName().equals("violations")){
          violationsTag = tag;
          break;
        }
      }
  
      if (violationsTag == null){
        throw new RuntimeException("Can`t find @violations javadoc-tag for "
            + testSource.getMainType().getName());
      }
  
      int expectedViolationCount = -1;
      try{
        expectedViolationCount = Integer.parseInt(violationsTag.getLinkName());
      } catch (NumberFormatException e){
        throw new RuntimeException("@violations javadoc-tag in "
            + testSource.getMainType().getName()
            + " has an illegal arg (must be integer)");
      }
  
      rule.clearViolations();
      DelegatingVisitor supervisor = new DelegatingVisitor(true);
      supervisor.registerDelegate(rule);
      rule.setSupervisor(supervisor);
      supervisor.visit(testSource);
      rule.postProcess();
  
      List violations = rule.getViolations();
      
      assertEquals("number of violations in '" +testSource.getDisplayPath()+ 
          "' doesn`t match: ", expectedViolationCount, violations.size());
  
      if(violations.size() == 0) {
        continue;
      }
      
      CorrectiveAction actionInstance = null;
      for (int v = 0; v < violations.size(); v++){
        Iterator actions =
            ((RuleViolation) violations.get(v)).getCorrectiveActions().iterator();
        while (actions.hasNext()){
          CorrectiveAction action = (CorrectiveAction) actions.next();
  
          if (action.getKey().equals(actionKey)) {
            actionInstance = action;
            	break;
          }
        }
        if (actionInstance != null){
          break;
        }
      }

      assertNotNull("Rule violations created by given audit rule can not be " +
          "repaired by given corrective action.", actionInstance);

      actionInstance.setTestRun(true);
  
      Set changedSources;
      if(actionInstance.isMultiTargetsSupported()){
      	changedSources = actionInstance.run(new NullContext(project),
            violations);
        assertValidChangedSources(changedSources);
      } else {
        /*
         *  FIXME: can cause infinite loop in case corrective action doesn`t
         *  make violations dissapear! (for example, some kind of
         *  "mark with comment"-style action).
         */
      	while(violations.size() > 0){
  	    	changedSources = actionInstance.run(new NullContext(project), violations);
          assertValidChangedSources(changedSources);
          project.getProjectLoader().build();
  	      testSource = (CompilationUnit) project.getCompilationUnits().get(0);
  
  	      rule.clearViolations();
  
  	      /*supervisor = new DelegatingVisitor(true);
  	      supervisor.registerDelegate(rule);
  	      rule.setSupervisor(supervisor);*/
  	      supervisor.visit(testSource);
  	      rule.postProcess();
  	      //rule.sortViolations(); // FIXME: this can cause sensible decrease in performance
  	      violations = rule.getViolations();
      	}
      }
      actionInstance.setTestRun(false);
    }
    rule.finishedRun();
    RwRefactoringTestUtils.assertSameSources("same sources",
        getExpectedProject(), project);
  }
  
  private void performProjectLoadTest(Project project) {
    try {
      ProjectLoadTest.processProject(project);
    } catch (Exception e) {
      assertTrue("Project \"" + project.getName() + 
          "\" build failed:" + e.getMessage(), false);
    }
  }

  /**
   * @param changedSources
   */
  private void assertValidChangedSources(final Set changedSources) {
    assertNotNull("Corrective action should never return NULL. "
        + "It should return Collections.EMPTY_SET, if no cahnges were made.",
        changedSources);
    assertFalse("Corrective action should return at least one compilation "
        + "unit, that was changed after it.",
        changedSources.size() == 0);
  }

  /**
   * Builds AuditRule, loads default audit profile and runs test
   *
   * @param auditClass YourAuditRule.class
   * @param actionKey the action, that will be applied to violations
   */
  protected void performSimpleTest(Class auditClass, String actionKey)
      throws Exception {
    Audit audit = new Audit(auditClass);
    AuditRule rule = audit.createAuditingRule();
    rule.setConfiguration(profile.getAuditItem(audit.getKey()));
    this.performTest(rule, actionKey);
  }

  /**
   * Finds all javadoc tags for specified typeRef (not only "wanted" tags)
   *
   * @param typeRef typeRef for that we are searching javadoc tags
   * @return list of javadoc tags
   */
  private List findTypeTags(BinTypeRef typeRef) {
    // Singleton javadoc
    JavadocComment comment = Comment.findJavadocFor(typeRef.getBinCIType());
    if (comment == null){
      throw new RuntimeException("Did not find javadoc comment for: "
          + typeRef.getQualifiedName());
    }

    String commentText = comment.getText();
    final BufferedReader text
        = new BufferedReader(new StringReader(commentText));
    Javadoc javadoc = null;
    try {
      javadoc = Javadoc.parse(text, 0, 0);
    } catch (IOException e) {
      return null;
    }

    return javadoc.getStandaloneTags();
  }
}
