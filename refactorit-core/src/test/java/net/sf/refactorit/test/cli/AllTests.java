/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.test.cli.actions.RunnerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author RISTO A
 */
public class AllTests extends TestCase {
  public AllTests(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite("CLI tests");

    result.addTestSuite(NotUsedSupportTest.class);
    result.addTestSuite(MetricsSupportTest.class);
    result.addTestSuite(AuditModelBuilderTest.class);
    result.addTestSuite(MetricsModelBuilderTest.class);
    result.addTestSuite(RunnerTest.class);
    result.addTestSuite(ArgumentsParserTest.class);
    result.addTestSuite(ArgumentsTest.class);
    result.addTestSuite(ProjectBuilderTest.class);
    result.addTestSuite(MainTest.class);
    result.addTestSuite(CliTest.class);
    result.addTestSuite(ArgumentsValidatorTest.class);

    result.addTestSuite(CommaSeparatedTableFormatTest.class);
    //result.addTestSuite(AuditSupportTest.class);
    //result.addTestSuite(PrintOutputTest.class);
    
    return result;
  }

  public void testSomeOther() {
    fail("");
  }
}
