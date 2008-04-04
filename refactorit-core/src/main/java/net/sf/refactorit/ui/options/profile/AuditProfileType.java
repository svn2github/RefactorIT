/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

//import net.sf.refactorit.audit.rules.serialization.SerializableFieldsRule;

import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.pmd.InterfaceToPMD;
import net.sf.refactorit.audit.rules.BooleanLiteralComparisonRule;
import net.sf.refactorit.audit.rules.DangerousIteratorUsageRule;
import net.sf.refactorit.audit.rules.EmptyBlocksAndBodiesRule;
import net.sf.refactorit.audit.rules.EmptyStatementRule;
import net.sf.refactorit.audit.rules.EqualsHashcodeRule;
import net.sf.refactorit.audit.rules.EqualsOnDiffTypesRule;
import net.sf.refactorit.audit.rules.FloatEqualComparisionRule;
import net.sf.refactorit.audit.rules.IntDivFloatContextRule;
import net.sf.refactorit.audit.rules.LoopCondModificationRule;
import net.sf.refactorit.audit.rules.LoopConditionRule;
import net.sf.refactorit.audit.rules.MissingBlockRule;
import net.sf.refactorit.audit.rules.NestedBlockRule;
import net.sf.refactorit.audit.rules.NonStaticReferenceRule;
import net.sf.refactorit.audit.rules.NotUsedRulesAddOn;
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
import net.sf.refactorit.audit.rules.UnusedImportRule;
import net.sf.refactorit.audit.rules.UnusedLocalVariableRule;
import net.sf.refactorit.audit.rules.complexity.LawOfDemeterRule;
import net.sf.refactorit.audit.rules.complexity.MethodBodyLengthRule;
import net.sf.refactorit.audit.rules.complexity.MethodCallsMethodRule;
import net.sf.refactorit.audit.rules.exceptions.AbortedFinallyRule;
import net.sf.refactorit.audit.rules.exceptions.DangerousCatchRule;
import net.sf.refactorit.audit.rules.exceptions.DangerousThrowRule;
import net.sf.refactorit.audit.rules.exceptions.RedundantThrowsRule;
import net.sf.refactorit.audit.rules.inheritance.AbstractOverrideRule;
import net.sf.refactorit.audit.rules.inheritance.AbstractSubclassRule;
import net.sf.refactorit.audit.rules.inheritance.HiddenFieldRule;
import net.sf.refactorit.audit.rules.inheritance.HiddenStaticMethodRule;
import net.sf.refactorit.audit.rules.j2se5.ForinRule;
import net.sf.refactorit.audit.rules.j2se5.GenericsRule;
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
import net.sf.refactorit.audit.rules.modifiers.PseudoAbstractClassRule;
import net.sf.refactorit.audit.rules.modifiers.RedundantModifiersRule;
import net.sf.refactorit.audit.rules.modifiers.SingleAssignmentFinalRule;
import net.sf.refactorit.audit.rules.modifiers.StaticMethodProposalRule;
import net.sf.refactorit.audit.rules.performance.ForLoopConditionOptimizer;
import net.sf.refactorit.audit.rules.serialization.NotSerializableSuperRule;
import net.sf.refactorit.audit.rules.serialization.SerialVersionUIDRule;
import net.sf.refactorit.audit.rules.service.ServiceAnnotationUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceBinItemReferenceRule;
import net.sf.refactorit.audit.rules.service.ServiceEnumUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceForinUsagesRule;
import net.sf.refactorit.audit.rules.service.ServiceGenericsUsagesRule;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.ui.checktree.CheckTreeNode;
import net.sf.refactorit.ui.options.profile.auditoptions.MethodBodyLengthOptionsPanel;
import net.sf.refactorit.ui.options.profile.auditoptions.MethodCallsMethodOptionsPanel;
import net.sf.refactorit.ui.options.profile.auditoptions.NumericLiteralsOptionsPanel;
import net.sf.refactorit.ui.options.profile.auditoptions.ParameterOrderOptionsPanel;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;

import org.w3c.dom.Element;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Iterator;

public class AuditProfileType implements ProfileType {
  public static final boolean SERVICE_AUDITS_ENABLED = false;

  IProfilePanel profileProvider;

  public String getName() {
    return "Audits";
  }

  public String getParametersPrefix() {
    return "audit.";
  }

  public void createNodes(final CheckTreeNode root) {
    Audit.clearCategoryRecords();
    //addBranchNode(null, root);  // performance is too low
    if (SERVICE_AUDITS_ENABLED) { // these audits for internal use only!
      CheckTreeNode service = new CheckTreeNode("Service", true);
      addBranchNode(root, service);
      addAuditNode(service, ServiceAnnotationUsagesRule.class);
      addAuditNode(service, ServiceEnumUsagesRule.class);
      addAuditNode(service, ServiceForinUsagesRule.class);
      addAuditNode(service, ServiceGenericsUsagesRule.class);
      addAuditNode(service, ServiceBinItemReferenceRule.class);
    }

    CheckTreeNode j2se5 = new CheckTreeNode("J2SE 5.0 constructs", true);
    addBranchNode(root, j2se5);
    addAuditNode(j2se5, ForinRule.class);
    addAuditNode(j2se5, GenericsRule.class);
    addAuditNode(j2se5, RedundantBoxingRule.class);
    addAuditNode(j2se5, RedundantUnboxingRule.class);

    CheckTreeNode exceptions = new CheckTreeNode("Exception usage", true);

    addBranchNode(root, exceptions);

    addAuditNode(exceptions, DangerousCatchRule.class);
    addAuditNode(exceptions, DangerousThrowRule.class);
    addAuditNode(exceptions, RedundantThrowsRule.class,
        new String[] {"include_runtime","include_error"});
    addAuditNode(exceptions, AbortedFinallyRule.class);

    CheckTreeNode inheritance = new CheckTreeNode("Inheritance", true);
    addBranchNode(root, inheritance);

    addAuditNode(inheritance, AbstractSubclassRule.class);
    addAuditNode(inheritance, AbstractOverrideRule.class);
    addAuditNode(inheritance, HiddenFieldRule.class);
    addAuditNode(inheritance, HiddenStaticMethodRule.class);

    CheckTreeNode modifiers = new CheckTreeNode("Modifier usage", true);
    addBranchNode(root, modifiers);

    addAuditNode(modifiers, ModifierOrderRule.class);
    addAuditNode(modifiers, PseudoAbstractClassRule.class);
    addAuditNode(modifiers, RedundantModifiersRule.class);
    addAuditNode(modifiers, StaticMethodProposalRule.class);
    addAuditNode(modifiers, FinalMethodProposalRule.class);
    addAuditNode(modifiers, FinalLocalProposalRule.class);
    addAuditNode(modifiers, ConstantFieldProposalRule.class,
        new String[] { "upper_case_names" });
    addAuditNode(modifiers, SingleAssignmentFinalRule.class);

    addAuditNode(modifiers, MinimizeAccessRule.class, new String[] {"constructors"});

    CheckTreeNode complexity = new CheckTreeNode("Program complexity", true);
    addBranchNode(root, complexity);

    addAuditNode(complexity, MethodCallsMethodRule.class,
        MethodCallsMethodOptionsPanel.class);
    addAuditNode(complexity, MethodBodyLengthRule.class,
        MethodBodyLengthOptionsPanel.class);
    addAuditNode(complexity, LawOfDemeterRule.class, new String[]{"skip_system_out" ,
      "skip_hashmap_keyset", "skip_singleton"});

    CheckTreeNode serialization = new CheckTreeNode("Serialization", true);
    addBranchNode(root, serialization);

    addAuditNode(serialization, NotSerializableSuperRule.class,
        new String[] { "noarg_constr" });
    //addAuditNode(serialization, SerializableFieldsRule.class,
    //    new String[] {"no_instance", "only_transient", "alerts_with_array"}
    //);
    addAuditNode(serialization, SerialVersionUIDRule.class);

    CheckTreeNode useless = new CheckTreeNode("Useless code", true);
    addBranchNode(root, useless);

    addAuditNode(useless, UnusedImportRule.class);
    addAuditNode(useless, UnusedLocalVariableRule.class, // assign
        new String[] { "method_parameters", "catch_parameters" });
    addAuditNode(useless, EmptyStatementRule.class);

    CheckTreeNode redundant = new CheckTreeNode("Redundant code", true);
    addBranchNode(root, redundant);

    addAuditNode(redundant, StringToStringRule.class);
    addAuditNode(redundant, BooleanLiteralComparisonRule.class);
    addAuditNode(redundant, RedundantCastRule.class,
        new String[] { "bitwise_primitives" });
    addAuditNode(redundant, RedundantInstanceofRule.class);
    addAuditNode(redundant, NestedBlockRule.class); // block
    addAuditNode(redundant, UnusedAssignmentRule.class); // assign

    CheckTreeNode danger = new CheckTreeNode("Dangerous code", true);
    addBranchNode(root, danger);

    addAuditNode(danger, ShadingRule.class, new String[] { "constructors",
        "setters" });
    addAuditNode(danger, EqualsHashcodeRule.class);
    addAuditNode(danger, EqualsOnDiffTypesRule.class,
        new String[] { "same_branch" });
    addAuditNode(danger, StringEqualComparisionRule.class);
    addAuditNode(danger, SwitchMissingDefaultRule.class);
    addAuditNode(danger, SwitchCaseFallthroughRule.class);
    addAuditNode(danger, ParameterAssignmentRule.class); // assign
    addAuditNode(danger, ParameterOrderRule.class,
        ParameterOrderOptionsPanel.class);
    addAuditNode(danger, FloatEqualComparisionRule.class);
    addAuditNode(danger, IntDivFloatContextRule.class);
    addAuditNode(danger, SelfAssignmentRule.class);
    addAuditNode(danger, MissingBlockRule.class); // block
    addAuditNode(danger, EmptyBlocksAndBodiesRule.class,
        new String[] { "with_comments" });
    addAuditNode(danger, NonStaticReferenceRule.class);
    addAuditNode(danger, LoopConditionRule.class);
    addAuditNode(danger, StringConcatOrderRule.class);
    addAuditNode(danger, PossibleLostOverrideRule.class);
    addAuditNode(danger, PossibleCallNPERule.class);
    addAuditNode(danger, StaticFieldAccessorsRule.class);
    addAuditNode(danger, LoopCondModificationRule.class);
    addAuditNode(danger, StringEqualsOrderRule.class);
    addAuditNode(danger, DangerousIteratorUsageRule.class);
    addAuditNode(danger, NullParametersRule.class);

    CheckTreeNode misc = new CheckTreeNode("Miscellaneous", true);
    addBranchNode(root, misc);

    addAuditNode(misc, DebugCodeRule.class);
    addAuditNode(misc, DuplicateStringsRule.class);
    addAuditNode(misc, EarlyDeclarationRule.class);
    addAuditNode(misc, NumericLiteralsRule.class,
        NumericLiteralsOptionsPanel.class);

    CheckTreeNode perf = new CheckTreeNode("Performance", true);
    addBranchNode(root, perf);

    addAuditNode(perf, ForLoopConditionOptimizer.class);

    CheckTreeNode nu = new CheckTreeNode("Not used", true);
    addBranchNode(root, nu);
    addNotUsedNodes(nu);

    CheckTreeNode PMD = new CheckTreeNode("PMD add-ons", false);
    root.add(PMD);
    addPMDNodes(PMD);

    refreshBranches();
  }

  /**
   * @param pmdRootNode
   */
  private void addPMDNodes(final CheckTreeNode pmdRootNode) {
    for (Iterator pmdRuleSets = InterfaceToPMD.getAvailableRuleSets()
        .iterator(); pmdRuleSets.hasNext();) {

      RuleSet ruleSet = (RuleSet) pmdRuleSets.next();

      CheckTreeNode ruleSubNode = new CheckTreeNode(ruleSet.getName(), false);
      pmdRootNode.add(ruleSubNode);

      // append rules to subNode
      for (Iterator rules = ruleSet.getRules().iterator(); rules.hasNext();) {
        Rule rule = (Rule) rules.next();
        addAuditNode(ruleSubNode, rule);
      }
    }
  }

  private void addNotUsedNodes(final CheckTreeNode nuRootNode) {
    Audit audit = new Audit(new ExcludeFilterRule.EmptyRule());

    ProfilePanel.TreeNode auditNode = new ProfilePanel.TreeNode(audit) {
      protected Element getItem(String key) {
        return profileProvider.getProfile().getAuditItem(key);
      }

      public void setSelected(boolean selected) {
        Enumeration enumer = children();
        while (enumer.hasMoreElements()) {
          CheckTreeNode node = (CheckTreeNode) enumer.nextElement();

          if(!node.equals(this)){
            if(selected) {
              Audit audit = (Audit) node.getUserObject();
              node.setSelected(
                  ExcludeFilterRule.isDefaultSelected(audit.getKey()));
            } else {
              node.setSelected(selected);
            }
          }
        }
        super.setSelected(selected);
      }
    };
    Audit.addCategoryRecord(auditNode.getUserObject().toString(), NotUsedRulesAddOn.class);
    nuRootNode.add(auditNode);
    for(int i = 0; i < ExcludeFilterRule.ALL_RULES.length; i++) {
      addAuditNode(auditNode, ExcludeFilterRule.ALL_RULES[i]);
    }
  }

  private Audit addAuditNode(CheckTreeNode parent, Class rule) {
    return addAuditNode(parent, rule, (String[]) null);
  }

  private Audit addAuditNode(CheckTreeNode parent, Rule PMDRule) {
    Audit audit = new Audit(PMDRule);
    parent.add(new ProfilePanel.TreeNode(audit) {
      protected Element getItem(String key) {
        return profileProvider.getProfile().getAuditItem(key);
      }
    });
    return audit;
  }//

  private void addAuditNode(CheckTreeNode parent, ExcludeFilterRule rule) {
    Audit audit = new Audit(rule);
    ProfilePanel.TreeNode auditNode = new ProfilePanel.TreeNode(audit) {
      protected Element getItem(String key) {
        return profileProvider.getProfile().getAuditItem(key);
      }
    };

    parent.add(auditNode);

    String key = audit.getKey();

    if (rule.getPropertyEditor() != null) {
      rule.setProperties(rule.getPropertyEditor());

      if(!profileProvider.getProfile().isDefault()) {
        rule.setProfile(profileProvider.getProfile());
      }

      OptionsPanel nuPanel = (OptionsPanel)rule.getPropertyEditor();
      if(nuPanel!=null) {
        profileProvider.addOptionsPanel(nuPanel, key);
      }
    }
  }

  public void refreshBranches(){
    Component[] comps = profileProvider.getOptionsPanelComponents();
    for (int i=0; i < comps.length; i++) {
      if (comps[i] instanceof AuditBranchOptionsPanel) {
        ((AuditBranchOptionsPanel) comps[i]).definePriority();
      }
    }
  }

  public void refreshBranch(String key){
    if (key == null ){
      return;
    }
    Component[] comps = profileProvider.getOptionsPanelComponents();
    for (int i=0; i < comps.length; i++) {
      if ((comps[i] instanceof AuditBranchOptionsPanel) &&
          (key.equals(((AuditBranchOptionsPanel) comps[i]).getKey()))) {
        ((AuditBranchOptionsPanel) comps[i]).definePriority();
      }
    }
  }

  public void refreshAudit(String key){
    if (key == null) {
      return;
    }
    Component[] comps = profileProvider.getOptionsPanelComponents();
    for (int i=0; i < comps.length; i++) {
      if (comps[i] instanceof AuditOptionsPanel) {
        CheckTreeNode node = ((AuditOptionsPanel)comps[i]).getTreeNode();
        if (node != null) {
          if (node != null && (node.getParent() instanceof CheckTreeNode) &&
              key.equals(node.getParent().toString())) {
            ((AuditOptionsPanel)comps[i]).getPriorityPanel().setSelection();
          }
        }
      }
    }
  }

  private void addBranchNode(CheckTreeNode parent, CheckTreeNode node) {
    if (parent != null) {
      parent.add(node);
    }
    String key = node.toString();
    AuditBranchOptionsPanel branchOptionsPanel =
      new AuditBranchOptionsPanel(node, this, key);
    branchOptionsPanel.setProfile(profileProvider.getProfile());
    profileProvider.addOptionsPanel(branchOptionsPanel, key);
  }

  private Audit addAuditNode(CheckTreeNode parent, Class rule, String[] options) {

    Audit audit = new Audit(rule);
    Audit.addCategoryRecord(parent.getUserObject().toString(), rule);

    ProfilePanel.TreeNode auditNode = new ProfilePanel.TreeNode(audit) {
      protected Element getItem(String key) {
        return profileProvider.getProfile().getAuditItem(key);
      }};

    parent.add(auditNode);
    String key = audit.getKey();

    AuditOptionsPanel optionsPanel = new AuditOptionsPanel(key, options, this,
        ResourceUtil.getBundle(rule));
    optionsPanel.setProfile(profileProvider.getProfile());
    optionsPanel.setTreeNode(auditNode);
    profileProvider.addOptionsPanel(optionsPanel, key);

    return audit;
  }

  private Audit addAuditNode(CheckTreeNode parent, Class rule, Class options) {
    Audit audit = addAuditNode(parent, rule);
    String key = audit.getKey();

    try {
      AuditOptionsSubPanel optionsSubPanel = (AuditOptionsSubPanel) options
          .getDeclaredConstructors()[0].newInstance(new Object[] { key, this,
          ResourceUtil.getBundle(rule) });
      AuditOptionsPanel optionsPanel = new AuditOptionsPanel(key, this,
          optionsSubPanel, ResourceUtil.getBundle(rule));

      optionsPanel.setProfile(profileProvider.getProfile());
      profileProvider.addOptionsPanel(optionsPanel, key);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return audit;
  }

  public void setProfilePanel(IProfilePanel p) {
    this.profileProvider = p;
  }

  public Profile createDefaultProfile() {
    return Profile.createDefaultAudit();
  }
}
