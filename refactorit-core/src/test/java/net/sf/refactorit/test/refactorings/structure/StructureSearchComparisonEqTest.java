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
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *  "Structure comparison (equal/non-equal) search" test class.
 *  Test project location: "test/projects/StructureSearch/ComparisonEq/in"
 * 
 * @author Oleg Tsernetsov
 * 
 * */

public class StructureSearchComparisonEqTest extends AbstractStructureSearchTest{
	public StructureSearchComparisonEqTest(String name){
		super(name);
	}
	
	public static Test suite(){
		final TestSuite suite = new TestSuite(StructureSearchComparisonEqTest.class);
		suite.setName("Structure Search comparison Operator");
		return suite;
	}
	
	public String getTemplate() {
	    return "StructureSearch/ComparisonEq/in";
	}
	  
	public int getSearchtype(){
	    return FindRequest.COMPARISONSEARCH;

	}
	
	
	BinTypeRef getFoundTypeRef(BinItem foundItem){
		BinExpression foundBinExpr = null;
	    if (foundItem instanceof BinExpression){ 
	        foundBinExpr = (BinExpression) foundItem;
	        if(foundBinExpr.getParent() instanceof BinLogicalExpression){
	 	        BinLogicalExpression foundLogicalExpr =  (BinLogicalExpression)foundBinExpr.getParent();
	        	
				if((foundLogicalExpr.getAssigmentType() != JavaTokenTypes.EQUAL) && 
					   (foundLogicalExpr.getAssigmentType() != JavaTokenTypes.NOT_EQUAL))
					fail("found binary item is not a equal/non-equal comparison expression " + foundItem);
				  
	        }
	        else {
	        	fail("found binary item is not a binary logical expression " + foundItem);
	        }
	    } 
	    else {
	        fail("found binary item is not a binary expression " + foundItem);
	    }

	    return foundBinExpr.getReturnType();
	}
	
	public void testA() throws Exception{
		checkMatches("A", false, 6);
	}
	
	public void testASubtypes() throws Exception{
		checkMatches("A", true, 14);
	}
	
	public void testB() throws Exception{
		checkMatches("B", false, 2);
	}
	
	public void testBSubtypes() throws Exception{
		checkMatches("B", true, 2);
	}
	
	public void testC() throws Exception{
		checkMatches("C", false, 2);
	}
	
	public void testCSubtypes() throws Exception{
		checkMatches("C", true, 8);
	}
	
	public void testD() throws Exception{
		checkMatches("D", false, 6);
	}
	
	public void testDSubtypes() throws Exception{
		checkMatches("D", true, 6);
	}
	
	public void testJavaLangObject() throws Exception{
		checkMatches("java.lang.Object", false, 0);
	}
	
	public void testJavaLangObjectSubtypes() throws Exception{
		checkMatches("java.lang.Object", true, 18);
	}
	
	public void testint() throws Exception{
		checkMatches("int", false, 4);
	}
	
	public void testintSubTypes() throws Exception{
		checkMatches("int", true, 4);
	}
	
	
	public void testString() throws Exception{
		checkMatches("java.lang.String", false, 2);
	}
	
	public void testStringSubTypes() throws Exception{
		checkMatches("java.lang.String", true, 2);
	}
	
	public void testchar() throws Exception{
		checkMatches("int", false, 4);
	}
	
	public void testcharSubTypes() throws Exception{
		checkMatches("int", true, 4);
	}

}
