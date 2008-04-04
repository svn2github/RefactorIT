/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.pmd;

import net.sf.refactorit.common.util.StringUtil;

import net.sourceforge.pmd.CommandLineOptions;
import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.TargetJDK1_5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * @author Kirill Buhhalko
 */
public class InterfaceToPMD {

  // list of violations related to 1 source
  private List results;
    
  private static final HashSet DISABLED_RULES = new HashSet(30);
  private static final HashSet DISABLED_RULESETS = new HashSet(10);
  
  static {
// List of all available rules for PMD v3.1 
// You can disable PMD rules here.
//    
//  "Basic Rules"
//      "ForLoopShouldBeWhileLoop"
//      "UnnecessaryConversionTemporary"
        DISABLED_RULES.add("ReturnFromFinallyBlock");
//      "DoubleCheckedLocking"
//      "CollapsibleIfStatements"
        DISABLED_RULES.add("EmptyTryBlock");
        DISABLED_RULES.add("EmptyStaticInitializer");
//      "UnconditionalIfStatement"
        DISABLED_RULES.add("EmptyStatementNotInLoop");
//      "UnnecessaryReturn"
        DISABLED_RULES.add("EmptyWhileStmt");
        DISABLED_RULES.add("EmptySynchronizedBlock");
        DISABLED_RULES.add("UnnecessaryFinalModifier");
        DISABLED_RULES.add("EmptyIfStmt");
//      "BooleanInstantiation"
        DISABLED_RULES.add("EmptyCatchBlock");
        DISABLED_RULES.add("EmptyFinallyBlock");
        DISABLED_RULES.add("EmptySwitchStatements");
//      "JumbledIncrementer"
        DISABLED_RULES.add("OverrideBothEqualsAndHashcode");
    DISABLED_RULESETS.add("Braces Rules");
//      "ForLoopsMustUseBraces"
//      "IfStmtsMustUseBraces"
//      "IfElseStmtsMustUseBraces"
//      "WhileLoopsMustUseBraces"
//  "Clone Implementation Rules"
//      "CloneThrowsCloneNotSupportedException"
//      "CloneMethodMustImplementCloneable"
//      "ProperCloneImplementation"
//  "Code Size Rules"
//      "TooManyFields"
//      "CyclomaticComplexity"
//      "ExcessiveParameterList"
//      "ExcessiveClassLength"
//      "ExcessivePublicCount"
//      "ExcessiveMethodLength"
//  "Controversial Rules"
//      "UnnecessaryParentheses"
//      "NullAssignment"
//      "AssignmentInOperand"
//      "DontImportSun"
//      "UnnecessaryConstructor"
//      "SingularField"
        DISABLED_RULES.add("CallSuperInConstructor");
//      "SuspiciousOctalEscape"
//      "AtLeastOneConstructor"
//      "OnlyOneReturn"
        DISABLED_RULES.add("UnusedModifier");
//  "Coupling Rules"
//      "LooseCoupling"
//      "ExcessiveImports"
//      "CouplingBetweenObjects"
//  "Design Rules"
//      "NonCaseLabelInSwitchStatement"
//      "UseSingleton"
        DISABLED_RULES.add("MissingBreakInSwitch");
//      "SimpleDateFormatNeedsLocale"
//      "SimplifyBooleanExpressions"
//      "AvoidInstanceofChecksInCatchClause"
//      "SimplifyConditional"
//      "AvoidCallingFinalize"
//      "SimplifyBooleanReturns"
//      "AvoidProtectedFieldInFinalClass"
//      "AssignmentToNonFinalStatic"
//      "ConfusingTernary"
//      "InstantiationToGetClass"
//      "AvoidDeeplyNestedIfStmts"
//      "DefaultLabelNotLastInSwitchStmt"
//      "UseLocaleWithCaseConversions"
//      "AvoidSynchronizedAtMethodLevel"
//      "EqualsNull"
//      "CloseConnection"
        DISABLED_RULES.add("BadComparison");
//      "AccessorClassGeneration"
//      "UseNotifyAllInsteadOfNotify"
        DISABLED_RULES.add("AvoidReassigningParameters");
//      "MissingStaticMethodInNonInstantiatableClass"
//      "FinalFieldCouldBeStatic"
//      "ConstructorCallsOverridableMethod"
        DISABLED_RULES.add("AbstractClassWithoutAbstractMethod");
//      "NonStaticInitializer"
//      "OptimizableToArrayCall"
        DISABLED_RULES.add("IdempotentOperations");
//      "SwitchDensity"
        DISABLED_RULES.add("SwitchStmtsShouldHaveDefault");
//      "ImmutableField"
//  "Finalizer Rules"
//      "ExplicitCallToFinalize"
//      "FinalizeDoesNotCallSuperFinalize"
//      "EmptyFinalizer"
//      "FinalizeOverloaded"
//      "FinalizeShouldBeProtected"
//      "FinalizeOnlyCallsSuperFinalize"
    DISABLED_RULESETS.add("Import Statement Rules");
//      "DontImportJavaLang"
//      "UnusedImports"
//      "ImportFromSamePackage"
//      "DuplicateImports"
//  "JavaBean Rules"
//      "BeanMembersShouldSerialize"
        DISABLED_RULES.add("MissingSerialVersionUID");
//  "JUnit Rules"
//      "UnnecessaryBooleanAssertion"
//      "UseAssertEqualsInsteadOfAssertTrue"
//      "UseAssertSameInsteadOfAssertTrue"
//      "JUnitStaticSuite"
//      "JUnitSpelling"
//      "TestClassWithoutTestCases"
//      "JUnitTestsShouldIncludeAssert"
//      "JUnitAssertionsShouldIncludeMessage"
//  "Java Logging Rules"
//      "SystemPrintln"
//      "MoreThanOneLogger"
//      "LoggerIsNotStaticFinal"
//  "Naming Rules"
//      "SuspiciousEqualsMethodName"
//      "LongVariable"
//      "AvoidNonConstructorMethodsWithClassName"
//      "ClassNamingConventions"
        DISABLED_RULES.add("SuspiciousConstantFieldName");
//      "ShortMethodName"
//      "SuspiciousHashcodeMethodName"
//      "ShortVariable"
//      "AbstractNaming"
//      "AvoidFieldNameMatchingTypeName"
//      "VariableNamingConventions"
//      "AvoidFieldNameMatchingMethodName"
//      "MethodWithSameNameAsEnclosingClass"
//      "MethodNamingConventions"
//      "AvoidDollarSigns"
//  "Optimization Rules"
        DISABLED_RULES.add("MethodArgumentCouldBeFinal");
//      "SimplifyStartsWith"
//      "UseArrayListInsteadOfVector"
//      "AvoidInstantiatingObjectsInLoops"
//      "UseStringBufferForStringAppends"
        DISABLED_RULES.add("LocalVariableCouldBeFinal");
//  "Strict Exception Rules"
        DISABLED_RULES.add("AvoidThrowingCertainExceptionTypes");
//      "AvoidCatchingNPE"
//      "ExceptionTypeChecking"
        DISABLED_RULES.add("AvoidCatchingThrowable");
//      "ExceptionAsFlowControl"
        DISABLED_RULES.add("SignatureDeclareThrowsException");
//  "java.lang.String Rules"
//      "AvoidConcatenatingNonLiteralsInStringBuffer"
//      "AvoidDuplicateLiterals"
        DISABLED_RULES.add("StringToString");
//      "StringInstantiation"
//  "Security Code Guidelines"
//      "MethodReturnsInternalArray"
//      "ArrayIsStoredDirectly"
    DISABLED_RULESETS.add("Unused Code Rules");
//      "UnusedLocalVariable"
//      "UnusedPrivateMethod"
//      "UnusedFormalParameter"
//      "UnusedPrivateField"
  }


  /*
   * Runs PMD on given source file with given rules set save all violations
   * to outReport.
   * @param fileToCheck
   * @param ruleset list of rules to check
   */
  public void processFile(String fileToCheck, RuleSet ruleset) {
    // needed to make Options for PMD, but not used,
    // there(CommandLineOptions) are some default options, which are needed
    // for correct work
    // it is possible to put they directly in needed places in go();
    String[] args = new String[3];
    args[1] = "csv";
    args[0] = fileToCheck;
    args[2] = "empty";
    //----

    CommandLineOptions opts = new CommandLineOptions(args);

    PMD pmd = new PMD(new TargetJDK1_5());

    RuleContext ctx = new RuleContext();
    ctx.setReport(new Report());

    try {

      File file = new File(fileToCheck);

      ctx.setSourceCodeFilename(
          glomName(opts.shortNamesEnabled(), fileToCheck, file));
      try {
        // file chechking....
        pmd.processFile(
            new FileInputStream(file), opts.getEncoding(), ruleset, ctx);
      } catch (PMDException pmde) {
        if (opts.debugEnabled()) {
          pmde.getReason().printStackTrace();
        }
        ctx.getReport().addError(new Report.ProcessingError(pmde.getMessage(),
            glomName(opts.shortNamesEnabled(), fileToCheck, file)));
      }

    } catch (FileNotFoundException fnfe) {
      System.out.println(opts.usage());
      fnfe.printStackTrace();
    }

    // get violations(ctx.getReport()) from PMD, remake it to
    // format ReportContainer (each container has one violation)
    // MyRender().render()  returns list of ReportContainers which
    // is related to checked file
    try {
      MyRender r = new MyRender();
      results = r.render(ctx.getReport());
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(opts.usage());
      if (opts.debugEnabled()) {
        e.printStackTrace();
      }
    }

    return;
  }

  private static String glomName(boolean shortNames, String inputFileName,
      File file) {
    if (shortNames && inputFileName.indexOf(',') == -1) {
      if ((new File(inputFileName)).isDirectory()) {
        return trimAnyPathSep(file.getAbsolutePath().substring(inputFileName.
            length()));
      } else {
        if (inputFileName.indexOf(
            System.getProperty("file.separator").charAt(0)) == -1) {
          return inputFileName;
        }
        return trimAnyPathSep(inputFileName.substring(inputFileName.lastIndexOf(
            System.getProperty("file.separator"))));
      }
    } else {
      return file.getAbsolutePath();
    }
  }

  private static String trimAnyPathSep(String name) {
    if (name.startsWith(System.getProperty("file.separator"))) {
      name = name.substring(1);
    }
    return name;
  }
  
  private static void printPMDHelp(){
    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    try {
      StringBuffer stringNavigation = new StringBuffer();
      StringBuffer stringRules = new StringBuffer();
      Iterator ruleSets = ruleSetFactory.getRegisteredRuleSets();
      stringNavigation.append("<ul>");
      while(ruleSets.hasNext()){
        RuleSet ruleSet = (RuleSet) ruleSets.next();
        if (!isDisabled(ruleSet)){
          
          stringNavigation.append("<li><a href=\"overview.html#");
          stringNavigation.append(ruleSet.getName());
          stringNavigation.append("\">");
          stringNavigation.append(ruleSet.getName());
          stringNavigation.append("</a></li>\n");
          
          stringRules.append("<a name=\"");
          stringRules.append(ruleSet.getName());
          stringRules.append("\"><h2>");
          stringRules.append(ruleSet.getName());
          stringRules.append("</h2></a>\n");
          
          stringNavigation.append("<ul>");
          
          for (Iterator rules = ruleSet.getRules().iterator(); rules.hasNext();){
            Rule rule = (Rule) rules.next();
            if (!isDisabled(rule)){
              final String ruleName = StringUtil.splitCamelStyleIntoWords(
                  rule.getName());
              stringNavigation.append("<li><a href=\"overview.html#");
              stringNavigation.append(ruleName);
              stringNavigation.append("\">");
              stringNavigation.append(ruleName);
              stringNavigation.append("</a></li>\n");
              
              stringRules.append("<a name=\"");
              stringRules.append(ruleName);
              stringRules.append("\"><h3>");
              stringRules.append(ruleName);
              stringRules.append("</h3></a>\n");
              
              stringRules.append(rule.getDescription());
              stringRules.append("<br>");
              stringRules.append("<pre>");
              stringRules.append(rule.getExample());
              stringRules.append("</pre>\n\n");
            }
          }
          
          stringNavigation.append("</ul>");
        }
      }
      stringNavigation.append("</ul>");
      System.out.println(stringNavigation);
      System.out.println(stringRules);
    } catch (RuleSetNotFoundException rsnfe) {
      rsnfe.printStackTrace();
    }
  }

  public static List getAvailableRuleSets(){
    List result = new ArrayList(30);

    //printPMDHelp();
    
    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    // take the list of audits from PMD
    try {
      Iterator ruleSets = ruleSetFactory.getRegisteredRuleSets();
      while(ruleSets.hasNext()){
        RuleSet ruleSet = (RuleSet) ruleSets.next();
//        System.out.println("//  \"" + ruleSet.getName() + "\"");
        if (!isDisabled(ruleSet)){
          for (Iterator rules = ruleSet.getRules().iterator(); rules.hasNext();){
            Rule rule = (Rule) rules.next();
//            System.out.println("//      \"" + rule.getName() + "\"");
            if (isDisabled(rule)){
              rules.remove();
            }
          }
          result.add(ruleSet);
        }
      }
    } catch (RuleSetNotFoundException rsnfe) {
      rsnfe.printStackTrace();
    }
    return result;
  }

  // check if this rule is in String disabledRules, returns true
  public static boolean isDisabled(Rule rule) {
    return DISABLED_RULES.contains(rule.getName());
  }

  // check if this RuleSet in PMD is disabled, returns true
  public static boolean isDisabled(RuleSet ruleset) {
    return DISABLED_RULESETS.contains(ruleset.getName());
  }

  public List getResults(){
    return results;
  }
}
