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
import net.sf.refactorit.audit.rules.BooleanLiteralComparisonRule;
import net.sf.refactorit.audit.rules.DangerousIteratorUsageRule;
import net.sf.refactorit.audit.rules.EmptyBlocksAndBodiesRule;
import net.sf.refactorit.audit.rules.EmptyStatementRule;
import net.sf.refactorit.audit.rules.EqualsHashcodeRule;
import net.sf.refactorit.audit.rules.EqualsOnDiffTypesRule;
import net.sf.refactorit.audit.rules.FloatEqualComparisionRule;
import net.sf.refactorit.audit.rules.IntDivFloatContextRule;
import net.sf.refactorit.audit.rules.LoopConditionRule;
import net.sf.refactorit.audit.rules.MissingBlockRule;
import net.sf.refactorit.audit.rules.NestedBlockRule;
import net.sf.refactorit.audit.rules.NonStaticReferenceRule;
import net.sf.refactorit.audit.rules.NullParametersRule;
import net.sf.refactorit.audit.rules.ParameterAssignmentRule;
import net.sf.refactorit.audit.rules.ParameterOrderRule;
import net.sf.refactorit.audit.rules.PossibleCallNPERule;
import net.sf.refactorit.audit.rules.PossibleLostOverrideRule;
import net.sf.refactorit.audit.rules.RedundantCastRule;
import net.sf.refactorit.audit.rules.RedundantInstanceofRule;
import net.sf.refactorit.audit.rules.SelfAssignmentRule;
import net.sf.refactorit.audit.rules.ShadingRule;
import net.sf.refactorit.audit.rules.StaticFieldAccessorsRule;
import net.sf.refactorit.audit.rules.StringConcatOrderRule;
import net.sf.refactorit.audit.rules.StringEqualComparisionRule;
import net.sf.refactorit.audit.rules.StringEqualsOrderRule;
import net.sf.refactorit.audit.rules.StringToStringRule;
import net.sf.refactorit.audit.rules.SwitchCaseFallthroughRule;
import net.sf.refactorit.audit.rules.SwitchMissingDefaultRule;
import net.sf.refactorit.audit.rules.UnusedAssignmentRule;
import net.sf.refactorit.audit.rules.UnusedLocalVariableRule;
import net.sf.refactorit.audit.rules.complexity.LawOfDemeterRule;
import net.sf.refactorit.audit.rules.complexity.MethodBodyLengthRule;
import net.sf.refactorit.audit.rules.complexity.MethodCallsMethodRule;
import net.sf.refactorit.audit.rules.exceptions.AbortedFinallyRule;
import net.sf.refactorit.audit.rules.exceptions.DangerousCatchRule;
import net.sf.refactorit.audit.rules.exceptions.RedundantThrowsRule;
import net.sf.refactorit.audit.rules.j2se5.ForinRule;
import net.sf.refactorit.audit.rules.j2se5.RedundantBoxingRule;
import net.sf.refactorit.audit.rules.j2se5.RedundantUnboxingRule;
import net.sf.refactorit.audit.rules.misc.DebugCodeRule;
import net.sf.refactorit.audit.rules.misc.DuplicateStringsRule;
import net.sf.refactorit.audit.rules.misc.EarlyDeclarationRule;
import net.sf.refactorit.audit.rules.misc.numericliterals.NumericLiteralsRule;
import net.sf.refactorit.audit.rules.modifiers.ConstantFieldProposalRule;
import net.sf.refactorit.audit.rules.modifiers.FinalLocalProposalRule;
import net.sf.refactorit.audit.rules.modifiers.FinalMethodProposalRule;
import net.sf.refactorit.audit.rules.modifiers.MinimizeAccessRule;
import net.sf.refactorit.audit.rules.modifiers.ModifierOrderRule;
import net.sf.refactorit.audit.rules.modifiers.SingleAssignmentFinalRule;
import net.sf.refactorit.audit.rules.modifiers.StaticMethodProposalRule;
import net.sf.refactorit.audit.rules.performance.ForLoopConditionOptimizer;
import net.sf.refactorit.audit.rules.serialization.NotSerializableSuperRule;
import net.sf.refactorit.audit.rules.serialization.SerialVersionUIDRule;
import net.sf.refactorit.ui.options.profile.Profile;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 *
 *
 * @author Villu Ruusmann
 */
public class AllTests {
  private AllTests() {}

  public static Test suite() {
    TestSuite result = new TestSuite("audit tests");

    Audit[] audits = {
        // TODO port old audits
//      new Audit("", UnusedImportRule.class),
//      new Audit("", UnusedLocalVariableRule.class),
        new Audit(MinimizeAccessRule.class),
        new Audit(ForLoopConditionOptimizer.class),
        new Audit(ShadingRule.class),
        new Audit(DangerousCatchRule.class),
        new Audit(StringToStringRule.class),
        new Audit(BooleanLiteralComparisonRule.class),
        new Audit(EqualsHashcodeRule.class),
        new Audit(SwitchMissingDefaultRule.class),
        new Audit(SwitchCaseFallthroughRule.class),
        new Audit(ParameterAssignmentRule.class),
        new Audit(RedundantCastRule.class),
        new Audit(RedundantInstanceofRule.class),
        new Audit(AbortedFinallyRule.class),
        new Audit(SelfAssignmentRule.class),
        new Audit(MissingBlockRule.class),
        new Audit(NonStaticReferenceRule.class),
        new Audit(EmptyStatementRule.class),
        new Audit(DebugCodeRule.class),
        new Audit(UnusedAssignmentRule.class),
        new Audit(StaticMethodProposalRule.class),
        new Audit(RedundantThrowsRule.class),
        new Audit(NestedBlockRule.class),
        new Audit(FloatEqualComparisionRule.class),
        new Audit(EmptyBlocksAndBodiesRule.class),
        new Audit(IntDivFloatContextRule.class),
        new Audit(SerialVersionUIDRule.class),
        new Audit(NotSerializableSuperRule.class),
        new Audit(EqualsOnDiffTypesRule.class),
        new Audit(MethodBodyLengthRule.class),
        new Audit(MethodCallsMethodRule.class),
        new Audit(StringEqualComparisionRule.class),
        new Audit(ParameterOrderRule.class),
        new Audit(ModifierOrderRule.class),
        new Audit(ConstantFieldProposalRule.class),
        new Audit(NumericLiteralsRule.class),
        new Audit(FinalMethodProposalRule.class),
        new Audit(ForinRule.class),
        new Audit(FinalLocalProposalRule.class),
        new Audit(RedundantBoxingRule.class),
        new Audit(RedundantUnboxingRule.class),
        new Audit(LoopConditionRule.class),
        new Audit(StringConcatOrderRule.class),
        new Audit(PossibleLostOverrideRule.class),
        new Audit(UnusedLocalVariableRule.class),
        new Audit(PossibleCallNPERule.class),
        new Audit(StaticFieldAccessorsRule.class),
        new Audit(LoopConditionRule.class),
        new Audit(StringEqualsOrderRule.class),
        
        new Audit(DangerousIteratorUsageRule.class),
        new Audit(SingleAssignmentFinalRule.class),
        new Audit(EarlyDeclarationRule.class),
        new Audit(NullParametersRule.class),
        new Audit(DuplicateStringsRule.class),
        new Audit(DuplicateStringsRule.class),
        new Audit(LawOfDemeterRule.class)
    };

    // default profile
    Profile profile = Profile.createDefaultAudit();

    for (int i = 0; i < audits.length; i++) {
      Audit audit = audits[i];
      AuditRule rule = audit.createAuditingRule();

      rule.setConfiguration(profile.getAuditItem(audit.getKey()));
      // testRun is set to false at the end of AuditTest.testAudit()
      rule.setTestRun(true);
      result.addTest(new AuditTest(rule));
    }

    result.addTest(AuditTest2.suite());

    // corrective actions
    result.addTest(CorrectiveActionTest.suite());

    return result;
  }
}
