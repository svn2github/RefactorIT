/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;


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
import net.sf.refactorit.audit.rules.PMDrulesAddOn;
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
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.options.profile.Profile;
import net.sf.refactorit.ui.options.profile.UserObject;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents node in an audit configuration dialog.
 * Factory for corresponding AuditRule implementation.
 *
 * @author Igor Malinin
 */
public class Audit implements UserObject {
  private static final Audit[] AUDITS = {
      // service
      new Audit(ServiceGenericsUsagesRule.class),
      new Audit(ServiceEnumUsagesRule.class),
      new Audit(ServiceAnnotationUsagesRule.class),
      new Audit(ServiceForinUsagesRule.class),
      new Audit(ServiceBinItemReferenceRule.class),
      // J2SE5
      new Audit(ForinRule.class),
      new Audit(GenericsRule.class),
      new Audit(RedundantBoxingRule.class),
      new Audit(RedundantUnboxingRule.class),
      // exceptions
      new Audit(DangerousCatchRule.class),
      new Audit(DangerousThrowRule.class),
      new Audit(RedundantThrowsRule.class),
      new Audit(AbortedFinallyRule.class),
      // inheritance
      new Audit(AbstractSubclassRule.class),
      new Audit(AbstractOverrideRule.class),
      new Audit(HiddenFieldRule.class),
      new Audit(HiddenStaticMethodRule.class),
      // serialization
      new Audit(SerialVersionUIDRule.class),
      new Audit(NotSerializableSuperRule.class),
      //new Audit(SerializableFieldsRule.class),
      // modifiers
      new Audit(ModifierOrderRule.class),
      new Audit(PseudoAbstractClassRule.class),
      new Audit(RedundantModifiersRule.class),
      new Audit(StaticMethodProposalRule.class),
      new Audit(ConstantFieldProposalRule.class),
      new Audit(FinalMethodProposalRule.class),
      new Audit(FinalLocalProposalRule.class),
      new Audit(SingleAssignmentFinalRule.class),
      // program complexity
      new Audit(MethodCallsMethodRule.class),
      new Audit(MethodBodyLengthRule.class),
      new Audit(LawOfDemeterRule.class),
      // misc
      new Audit(DebugCodeRule.class),
      new Audit(EarlyDeclarationRule.class),
      new Audit(NumericLiteralsRule.class),
      new Audit(DuplicateStringsRule.class),
      // unsorted
      new Audit(UnusedImportRule.class),
      new Audit(UnusedLocalVariableRule.class),
      new Audit(ShadingRule.class),
      new Audit(StringToStringRule.class),
      new Audit(BooleanLiteralComparisonRule.class),
      new Audit(EqualsHashcodeRule.class),
      new Audit(SwitchMissingDefaultRule.class),
      new Audit(SwitchCaseFallthroughRule.class),
      new Audit(ParameterAssignmentRule.class),
      new Audit(RedundantCastRule.class),
      new Audit(RedundantInstanceofRule.class),
      new Audit(SelfAssignmentRule.class),
      new Audit(MissingBlockRule.class),
      new Audit(NonStaticReferenceRule.class),
      new Audit(EmptyStatementRule.class),
      new Audit(UnusedAssignmentRule.class),
      new Audit(NestedBlockRule.class),
      new Audit(FloatEqualComparisionRule.class),
      new Audit(EmptyBlocksAndBodiesRule.class),
      new Audit(IntDivFloatContextRule.class),
      new Audit(EqualsOnDiffTypesRule.class),
      new Audit(StringEqualComparisionRule.class),
      new Audit(ParameterOrderRule.class),
      new Audit(LoopConditionRule.class),
      new Audit(StringConcatOrderRule.class),
      new Audit(PossibleLostOverrideRule.class),
      new Audit(PossibleCallNPERule.class),
      new Audit(StaticFieldAccessorsRule.class),
      new Audit(LoopCondModificationRule.class),
      new Audit(StringEqualsOrderRule.class),
      new Audit(MinimizeAccessRule.class),
      new Audit(ForLoopConditionOptimizer.class),
      new Audit(DangerousIteratorUsageRule.class),
      new Audit(NullParametersRule.class)
  };

  private final Class rule;
  private final String key;
  private final String name;
  private final String description;

  /**
   * Initializes audit instance with an AuditRule.
   */
  public Audit(final Class rule) {
    Assert.must(AuditRule.class.isAssignableFrom(rule));

    this.rule = rule;

    key = getKey(rule);

    ResourceBundle resLocalizedStrings = ResourceUtil.getBundle(rule);

    name = resLocalizedStrings.getString("audit." + key + ".name");

    description = resLocalizedStrings
        .getString("audit." + key + ".description");
  }

  /**
   *   Initializes audit instance with PMD rules
   */

  public Audit(Rule rulePMD) {
    this.rule = PMDrulesAddOn.class;
    Assert.must(AuditRule.class.isAssignableFrom(rule));

    key = getKey(rule)+rulePMD.getName(); // add PMDrule ident to each Audit rule

    name = StringUtil.splitCamelStyleIntoWords(rulePMD.getName());

    description = rulePMD.getDescription() + "<pre>Example:<CODE>" +
        rulePMD.getExample() + "</CODE></pre>";

//    This code prints a PMD help template to console on Audit action run :)
//
//    System.out.println("<h3>"+name+"</h3>");
//    System.out.println("<p>"+rulePMD.getDescription()+"</p>");
//    System.out.println("<pre>Example:" +
//        rulePMD.getExample() + "</pre>");
  }

  public Audit(ExcludeFilterRule nuRule) {
    this.rule = NotUsedRulesAddOn.class;
    key = nuRule.getKey();
    if(nuRule instanceof ExcludeFilterRule.EmptyRule) {
      name = nuRule.getName();
    } else {
      name = "Exclude " + nuRule.getName().toLowerCase();
    }
    description = nuRule.getDescription();
  }

  public static String getKey(final Class rule) {
    String key = null;
    try {
      Field field = rule.getField("NAME");
      key = (String) field.get(null);
    } catch (Exception e) {
      Assert.must(false);
    }
    return key;
  }

  /**
   * Internal name for an audit.
   */
  public String getKey() {
    return key;
  }

  /**
   * Display name of an audit.
   */
  public String getName() {
    return name;
  }

  /**
   * Contents of this string must be placed inside HTML body.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Creates new instance of an AuditRule registered with this object.
   */
  public AuditRule createAuditingRule() {
    try {
      AuditRule newRule = (AuditRule) rule.newInstance();
      newRule.setName(name);
      newRule.setKey(key);
      newRule.setCategory(getCategoryNameFor(newRule.getClass()));
      return newRule;
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /*
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }

  public static AuditRule[] createActiveRulesForStatisticsLogging(Profile selected){
    List list = new ArrayList();
    
    for(int i=0; i<AUDITS.length; i++)
      if(selected.isActiveItem(selected.getAudit(false), AUDITS[i].getKey()))
        list.add(AUDITS[i].createAuditingRule());
    
    return (AuditRule[]) list.toArray(new AuditRule[]{});
  }
  
  
  public static AuditRule[] createActiveRulesAndSetToModel(Profile profile,
      AuditTreeTableModel model) {
    List list = new ArrayList(AUDITS.length);

    for (int i = 0; i < AUDITS.length; i++) {
      Audit audit = AUDITS[i];
      String key = audit.getKey();
      if (profile.isActiveItem(profile.getAudit(false), key)) {
        AuditRule rule = audit.createAuditingRule();
        rule.setConfiguration(profile.getAuditItem(key));
        list.add(rule);
      }
    }

    createActivePMDRules(profile, list);
    createActiveNotUsedRules(profile,list);

    model.setAuditRules((AuditRule[]) list.toArray(new AuditRule[list.size()]));
    return model.getAuditRules();
  }

  /**
   * Part of PMD integration.<br>
   * <br>
   * Asks all available rules from PMD and if they are enabled in audit profile,
   * adds them to 'TO DO' container of PMDrulesAddOn. PMDrulesAddOn processes
   * source files with contains of this container when visit(CompilationUnit) is
   * called.
   *
   * @param profile audit profile
   * @param list final list of all audit rules
   */
  private static void createActivePMDRules(final Profile profile,
      final List list) {
    PMDrulesAddOn pmdAudit = new PMDrulesAddOn();
    for (Iterator pmdRuleSets = InterfaceToPMD.getAvailableRuleSets().iterator();
        pmdRuleSets.hasNext(); ) {
      RuleSet ruleSet = (RuleSet) pmdRuleSets.next();

      for (Iterator zz = ruleSet.getRules().iterator(); zz.hasNext(); ) {
        Rule pmdRule = (Rule) zz.next();

        Audit audit = new Audit(pmdRule);
        String key = audit.getKey();

        if (profile.isActiveItem(profile.getAudit(false), key)) {
          // adding current rule to PMDaudit rulelist for checking
          // ruleSetToCheck is static, one for all
          pmdAudit.addRuleToCheck(pmdRule);
          //pmdAudit.setKey(key);
          //auditRule.setConfiguration(profile.getAuditItem(key));
        }
      }
    }

    if (pmdAudit.hasRulesToCheck()){
      list.add(pmdAudit); // add Audit to the end of "audits to check" list
    }
  }
  
  public static Rule[] createActivePMDRulesForStatisticsLogging(Profile selected){
    List result = new ArrayList();
    
    for(Iterator a = InterfaceToPMD.getAvailableRuleSets().iterator(); 
        a.hasNext();){
      RuleSet ruleSet = (RuleSet) a.next();
      for(Iterator b = ruleSet.getRules().iterator(); b.hasNext();){
        Rule rule = (Rule) b.next();
        if(selected.isActiveItem(selected.getAudit(false), ((new Audit(rule)).getKey())))
          result.add(rule);
      }
    }
    
    return (Rule[]) result.toArray(new Rule[]{});
  }

  private static void createActiveNotUsedRules(final Profile profile,
      final List list) {
    NotUsedRulesAddOn nuRule = new NotUsedRulesAddOn();
    nuRule.setCategory(getCategoryNameFor(nuRule.getClass()));
    nuRule.setKey(nuRule.NAME);

    List rules = new ArrayList();
    rules.add(new ExcludeFilterRule.EmptyRule());
    rules.addAll(Arrays.asList(ExcludeFilterRule.ALL_RULES));

    for(int i=0; i<rules.size(); i++) {
      ExcludeFilterRule rule = (ExcludeFilterRule) rules.get(i);
      Audit audit = new Audit(rule);
      String key = audit.getKey();
      if(profile.isActiveItem(profile.getAudit(false),key)) {
        rule.setProfile(profile);
        nuRule.addRuleToCheck(rule);
      }
    }

    //nuRule.setKey(NotUsedRulesAddOn.NAME);

    if(nuRule.hasRulesToCheck()) {
      list.add(nuRule);
    }
  }

  private static Map categoryMap = new TreeMap();

  public static void clearCategoryRecords(){
    categoryMap.clear();
  }

  public static void addCategoryRecord(String category, Class rule){
    if (category.equals("Audits")){
      addCategoryRecord("Uncategorized", rule);
      return;
    }
    Set subitems = (Set) categoryMap.get(category);
    if (subitems == null){
      subitems = new HashSet();
      categoryMap.put(category, subitems);
    }
    subitems.add(rule);
  }

  public static String getCategoryNameFor(Class rule){
    for (Iterator i = categoryMap.keySet().iterator(); i.hasNext(); ){
      String category = (String) i.next();
      Set subitems = (Set) categoryMap.get(category);
      if (subitems.contains(rule)){
        return category;
      }
    }
    return "other";
  }
  
  public static Audit[] getAllAudits(){
    return AUDITS;
  }
}
