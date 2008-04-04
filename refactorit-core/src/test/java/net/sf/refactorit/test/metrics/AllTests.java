/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.metrics;

import net.sf.refactorit.metrics.AbstractTypeCountMetric;
import net.sf.refactorit.metrics.AbstractnessMetric;
import net.sf.refactorit.metrics.AfferentCouplingMetric;
import net.sf.refactorit.metrics.ClocMetric;
import net.sf.refactorit.metrics.ConcreteTypeCountMetric;
import net.sf.refactorit.metrics.CyclicDependencyMetric;
import net.sf.refactorit.metrics.CyclomaticComplexityMetric;
import net.sf.refactorit.metrics.DcMetric;
import net.sf.refactorit.metrics.DipMetric;
import net.sf.refactorit.metrics.DirectCyclicDependencyMetric;
import net.sf.refactorit.metrics.DistanceMetric;
import net.sf.refactorit.metrics.DitMetric;
import net.sf.refactorit.metrics.EfferentCouplingMetric;
import net.sf.refactorit.metrics.EpMetric;
import net.sf.refactorit.metrics.ExecutableStatementsMetric;
import net.sf.refactorit.metrics.ExportedTypeCountMetric;
import net.sf.refactorit.metrics.InstabilityMetric;
import net.sf.refactorit.metrics.LackOfCohesionMetric;
import net.sf.refactorit.metrics.LocMetric;
import net.sf.refactorit.metrics.LspMetric;
import net.sf.refactorit.metrics.NclocMetric;
import net.sf.refactorit.metrics.NocMetric;
import net.sf.refactorit.metrics.NumberOfAttributesMetric;
import net.sf.refactorit.metrics.NumberOfFieldsMetric;
import net.sf.refactorit.metrics.NumberOfTrampsMetric;
import net.sf.refactorit.metrics.RfcMetric;
import net.sf.refactorit.metrics.TypeCountMetric;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** Hidden constructor. */
  private AllTests() {
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Metric");

    suite.addTest(CyclomaticComplexityMetric.TestDriver.suite());
    suite.addTest(ExecutableStatementsMetric.TestDriver.suite());
    suite.addTest(LocMetric.TestDriver.suite());
    suite.addTest(NclocMetric.TestDriver.suite());
    suite.addTest(ClocMetric.TestDriver.suite());
    suite.addTest(DcMetric.TestDriver.suite());
    suite.addTest(DipMetric.TestDriver.suite());
    suite.addTest(DitMetric.TestDriver.suite());
    suite.addTest(NocMetric.TestDriver.suite());
    suite.addTest(EfferentCouplingMetric.TestDriver.suite());
    suite.addTest(EpMetric.TestDriver.suite());
    suite.addTest(AfferentCouplingMetric.TestDriver.suite());
    suite.addTest(InstabilityMetric.TestDriver.suite());
    suite.addTest(AbstractnessMetric.TestDriver.suite());
    suite.addTest(DistanceMetric.TestDriver.suite());
    suite.addTest(RfcMetric.TestDriver.suite());
    suite.addTest(TypeCountMetric.TestDriver.suite());
    suite.addTest(AbstractTypeCountMetric.TestDriver.suite());
    suite.addTest(ConcreteTypeCountMetric.TestDriver.suite());
    suite.addTest(ExportedTypeCountMetric.TestDriver.suite());
    suite.addTest(CyclicDependencyMetric.TestDriver.suite());
    suite.addTest(DirectCyclicDependencyMetric.TestDriver.suite());
    suite.addTest(LspMetric.TestDriver.suite());
    suite.addTest(NumberOfTrampsMetric.TestDriver.suite());
    suite.addTest(LackOfCohesionMetric.TestDriver.suite());
    suite.addTest(NumberOfFieldsMetric.TestDriver.suite());
    suite.addTest(HtmlOutputTest.suite());
    suite.addTest(DialogTest.suite());
    suite.addTest(NumberOfAttributesMetric.TestDriver.suite());

    return suite;
  }
}
