/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.structure;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *  "Structure instanceof search" test class.
 *  Test project location: "test/projects/StructureSearch/Instanceof/in"
 * 
 * @author Oleg Tsernetsov
 * 
 * */

public class StructureSearchInstanceofTest extends AbstractStructureSearchTest{
	public StructureSearchInstanceofTest(String name){
		super(name);
	}
	
	public static Test suite(){
		final TestSuite suite = new TestSuite(StructureSearchInstanceofTest.class);
		suite.setName("Structure Search instanceof Operator");
		return suite;
	}
	
	public String getTemplate() {
	    return "StructureSearch/Instanceof/in";
	}
	  
	public int getSearchtype(){
	    return FindRequest.INSTANCEOFSEARCH;
	}
	
	
	BinTypeRef getFoundTypeRef(BinItem foundItem){
		BinInstanceofExpression foundBinExpr = null;
	    if (foundItem instanceof BinInstanceofExpression){
	        foundBinExpr = (BinInstanceofExpression) foundItem; 
	      } else {
	        fail("found binary item is not a binary expression " + foundItem);
	      }

	      return foundBinExpr.getRightExpression().getReturnType();
	}
	
	public void testA() throws Exception{
		checkMatches("A", false, 4);
	}
	
	public void testASubTypes() throws Exception{
		checkMatches("A", true, 12);
	}
	
	public void testB() throws Exception{
		checkMatches("B", false, 4);
	}
	
	public void testBSubTypes() throws Exception{
		checkMatches("B", true, 4);
	}
	
	public void testC() throws Exception{
		checkMatches("C", false, 4);
	}
	
	public void testCSubTypes() throws Exception{
		checkMatches("C", true, 8);
	}
	
	public void testD() throws Exception{
		checkMatches("D", false, 4);
	}
	
	public void testDSubTypes() throws Exception{
		checkMatches("D", true, 4);
	}
	
	public void testJavaLangObj() throws Exception{
		checkMatches("java.lang.Object", false, 0);
	}
	
	public void testJavaLangObjSubTypes() throws Exception{
		checkMatches("java.lang.Object", true, 16);
	}
}
