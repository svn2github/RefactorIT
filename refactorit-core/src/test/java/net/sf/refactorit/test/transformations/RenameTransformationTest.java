/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.transformations;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationManager;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RenameTransformationTest extends TestCase {
  public RenameTransformationTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(RenameTransformationTest.class);
    suite.setName("Rename Transformation Tests");
    return suite;
  }
  
  public void test_renameBinItemByAST() {
    TransformationManager manager = new TransformationManager(null);

    try {
      String content = "public class myClass {}";
      Project project = Utils.createTestRbProjectFromString(content);
      
      List compilationUnits = project.getCompilationUnits();
      CompilationUnit unit = (CompilationUnit) compilationUnits.get(0);
      BinTypeRef classRef = (BinTypeRef) unit.getDefinedTypes().get(0);
      BinCIType binType = classRef.getBinCIType();

      manager.add(new RenameTransformation(unit, binType.getNameAstOrNull(),
          "fooClass"));
      manager.performTransformations();
      project.getProjectLoader().build(null, false);

      CompilationUnit newUnit = (CompilationUnit) compilationUnits.get(0);
      BinTypeRef newClassRef = (BinTypeRef)newUnit.getDefinedTypes().get(0);
      BinCIType newBinType = newClassRef.getBinCIType();
      
      assertEquals("fooClass", newBinType.getName()); 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }
  
  public void test_renameBinItem() {
    TransformationManager manager = new TransformationManager(null);

    try {
      String content = "public class myClass {}";
      Project project = Utils.createTestRbProjectFromString(content);

      List compilationUnits = project.getCompilationUnits();
      CompilationUnit unit = (CompilationUnit) compilationUnits.get(0);
      BinTypeRef classRef = (BinTypeRef) unit.getDefinedTypes().get(0);
      BinCIType binType = classRef.getBinCIType();

      manager.add(new RenameTransformation(unit, binType,
          "fooClass"));
      manager.performTransformations();
      project.getProjectLoader().build(null, false);

      CompilationUnit newUnit = (CompilationUnit) compilationUnits.get(0);
      BinTypeRef newClassRef = (BinTypeRef) newUnit.getDefinedTypes()
          .get(0);
      BinCIType newBinType = newClassRef.getBinCIType();

      assertEquals("fooClass", newBinType.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }
  
}
