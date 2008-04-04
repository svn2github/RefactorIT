/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.utils;


import net.sf.refactorit.common.util.PhraseSplitter;
import net.sf.refactorit.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class StringUtilTest extends TestCase {
  public StringUtilTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(StringUtilTest.class);
  }

  public void testSerializeEmptyList() {
    assertEquals("", StringUtil.serializeStringList(new ArrayList()));
    assertEquals(0, StringUtil.deserializeStringList("").size());
  }

  public void testSerializeOneItem() {
    List list = new ArrayList();
    list.add("abc");

    assertEquals("abc", StringUtil.serializeStringList(list));
    assertEquals(list, StringUtil.deserializeStringList("abc"));
  }

  public void testSerializeTwoItems() {
    List list = new ArrayList();
    list.add("abc");
    list.add("def");

    assertEquals("abc|def", StringUtil.serializeStringList(list));
    assertEquals(list, StringUtil.deserializeStringList("abc|def"));
  }

  public void testSerializeItemsThatContainSeparator() {
    List list = new ArrayList();
    list.add("a|b");

    assertEquals("a||b", StringUtil.serializeStringList(list));
    assertEquals(list, StringUtil.deserializeStringList("a||b"));
  }

  public void testLineHasOnlyWhitespaceAndComments() {
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments(""));
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments(" \t "));
    assertFalse(StringUtil.containsOnlyWhitespaceAndComments("x"));
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments(" //"));
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments("/*"));
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments("/*asd"));
    assertTrue(StringUtil.containsOnlyWhitespaceAndComments("/*asd */"));
    assertFalse(StringUtil.containsOnlyWhitespaceAndComments("/*asd */ x"));
    assertFalse(StringUtil.containsOnlyWhitespaceAndComments("/a"));
    assertFalse(StringUtil.containsOnlyWhitespaceAndComments("a//"));
  }
  
  public void testUppercaseStyleName() {
    uppercaseStyleTest("simple","SIMPLE");
    uppercaseStyleTest("camelNotationVar","CAMEL_NOTATION_VAR");
    uppercaseStyleTest("_var","_VAR");
    uppercaseStyleTest("complex_var","COMPLEX_VAR");
    uppercaseStyleTest("camel_Sophisticated","CAMEL_SOPHISTICATED");
    uppercaseStyleTest("takeIt_nameIt","TAKE_IT_NAME_IT");
    uppercaseStyleTest("some__sophisticatedVar","SOME__SOPHISTICATED_VAR");
    uppercaseStyleTest("thisIS_var","THIS_IS_VAR");
    uppercaseStyleTest("externalVar_ONE","EXTERNAL_VAR_ONE");
    uppercaseStyleTest("externalVAR_two","EXTERNAL_VAR_TWO");
    uppercaseStyleTest("_oneTwoThree","_ONE_TWO_THREE");
    uppercaseStyleTest("_a_b_c_D_E_","_A_B_C_D_E_");
  }
  
  public void uppercaseStyleTest(String source, String result) {
    assertTrue(result.equals(StringUtil.getUpercaseStyleName(source)));
  }
  
  public void testIndexesOfSubPhrase1() {
    indexesOfSubPhraseTest("IAm", "Am");
  }
  
  public void testIndexesOfSubPhrase2() {
    indexesOfSubPhraseTest("IAmAm", "Am");
  }
  
  public void testIndexesOfSubPhrase3() {
    indexesOfSubPhraseTest("IWasAmAmSSS", "AmAm");
  }
  
  public void testIndexesOfSubPhrase4() {
    indexesOfSubPhraseTest("IWasAmAmSSSAmAm", "AmAm");
  }
  
  public void testIndexesOfSubPhrase5() {
    indexesOfSubPhraseTest("I_Was_AmAm_SSS_AmAm", "AmAm");
  }
  
  public void testIndexesOfSubPhrase6() {
    indexesOfSubPhraseTest("_I_Was_AmAm_SSS_AmAm_", "AmAm");
  }
  
  public void testIndexesOfSubPhrase7() {
    indexesOfSubPhraseTest("_I_Was_AmAM_SSS_AMAm_", "AmAm");
  }
  
  public void testIndexesOfSubPhrase8() {
    indexesOfSubPhraseTest("_I_Was_Am__AM_SSS_AM_Am_", "AmAm");
  }
  
  public void testIndexesOfSubPhrase9() {
    indexesOfSubPhraseTest("I_was", "was");
  }
  
  public void testSmartWordReplace1() {
    String[] find = new String[] {"A", "_", "COOL", "_", "ROBOT", "_", "MAN"};
    String[] replace = new String[] {"A", "Machine", "Man"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "A_MACHINE_MAN";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    
    assertEquals(expectedString, buffer.toString());
  }
  
  public void testSmartWordReplace2() {
    String[] find = new String[] {"A", "_", "COOL", "_", "ROBOT", "_", "MAN"};
    String[] replace = new String[] {"A", "Machine", "Like", "Man"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "A_MACHINE_LIKE_MAN";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  }  
  
  public void testSmartWordReplace3() {
    String[] find = new String[] {"Aee", "Bee", "Cee", "Dee", "Fee", "Gee"};
    String[] replace = new String[] {"Aee", "_", "Bee", "_", "Cee", "_", "Zee", "_", "Fee", "_", "Gee"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "AeeBeeCeeZeeFeeGee";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  }  
  
  public void testSmartWordReplace4() {
    String[] find = new String[] {"Aee", "_", "Bee", "_","Cee", "_","Dee", "_","Fee", "_", "Gee"};
    String[] replace = new String[] {"Aee", "_", "Bee", "_", "Cee", "_", "Zee", "_", "Fee", "_", "Gee"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "Aee_Bee_Cee_Zee_Fee_Gee";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  }  
  
  public void testSmartWordReplace5() {
    String[] find = new String[] {"A", "_", "B", "_","C", "_","D", "_","F", "_", "G"};
    String[] replace = new String[] {"A", "_", "B", "_", "X", "_", "Y", "_", "Z", "_", "F", "_", "G"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "A_B_X_Y_Z_F_G";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  } 
  
  public void testSmartWordReplace6() {
    String[] find = new String[] {"Aaa", "_", "Bbb", "_","C", "_","D", "_","Fff", "_", "ggg"};
    String[] replace = new String[] {"Aaa", "_", "Bbb", "_", "Xxx", "_", "Yyy", "_", "Zzz", "_", "Fff", "_", "ggg"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "Aaa_Bbb_Xxx_Yyy_Zzz_Fff_ggg";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  }  
  
  public void testSmartWordReplace7() {
    String[] find = new String[] {"BIG"};
    String[] replace = new String[] {"VERY", "_", "BIG"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "VERY_BIG";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  }  
  
  public void testSmartWordReplace8() {
    String[] find = new String[] {"person"};
    String[] replace = new String[] {"cool", "Human"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "coolHuman";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  } 
  
  public void testSmartWordReplace9() {
    String[] find = new String[] {"easy", "Man"};
    String[] replace = new String[] {"Person"};
    String[] result = StringUtil.smartWordReplace(find, replace);
    
    String expectedString = "person";
    
    StringBuffer buffer = new StringBuffer();
    for(int i = 0; i < result.length; i++) {
      buffer.append(result[i]);
    }
    assertEquals(expectedString, buffer.toString());
  } 
  
  public void testSmartPhraseReplace1() {
    String base = "I_AM_A_COOL_ROBOT_MAN";
    String find = "ACoolRobotMan";
    String replace = "AMachineLikeMan";
    String expectedString = "I_AM_A_MACHINE_LIKE_MAN";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }  
  
  public void testSmartPhraseReplace2() {
    String base = "I_wanna_be_cool";
    String find = "cool";
    String replace = "bad";
    String expectedString = "I_wanna_be_bad";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace3() {
    String base = "Phrase_one_two_three";
    String find = "oneTwo";
    String replace = "twoOne";
    String expectedString = "Phrase_two_one_three";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace4() {
    String base = "COMPILATION_UNIT_CONSTANT";
    String find = "CompilationUnit";
    String replace = "SourceHolder";
    String expectedString = "SOURCE_HOLDER_CONSTANT";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace5() {
    String base = "VeryWeirdString_Hei_HEI";
    String find = "WeirdString";
    String replace = "NICE_CONST";
    String expectedString = "VeryNiceConst_Hei_HEI";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace6() {
    String base = "Very_ABRGVALString_Hei_HEI";
    String find = "abrgval";
    String replace = "QWERTY_FOREVER";
    String expectedString = "Very_QWERTY_FOREVERString_Hei_HEI";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace7() {
    String base = "person";
    String find = "Person";
    String replace = "CoolHuman";
    String expectedString = "coolHuman";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace8() {
    String base = "myEasyManName";
    String find = "EASY_MAN";
    String replace = "Person";
    String expectedString = "myPersonName";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testSmartPhraseReplace9() {
    String base = "EasyMan";
    String find = "EASY_MAN";
    String replace = "Person";
    String expectedString = "Person";
    String resultString = StringUtil.smartPhraseReplace(base, find, replace);
    assertEquals(expectedString, resultString);
  }
  
  public void testReplaceCommentsWithWhitespace0(){
    String source = "class A /** aaa */ extends /** bbb */ JPanel {\n/** */";
    String expect = "class A            extends            JPanel {\n/** */";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.indexOf('{')));
  }
  
  public void testReplaceCommentsWithWhitespace1(){
    String source = "class A /** /**aaa */ extends /** bbb */ JPanel {\n/** */";
    String expect = "class A               extends            JPanel {\n/** */";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.indexOf('{')));
  }
  
  public void testReplaceCommentsWithWhitespace2(){
    String source = "class A /** extends /** bbb */ JPanel /** ";
    String expect = "class A                        JPanel     ";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        6, source.length()-1));
  }
  
  public void testReplaceCommentsWithWhitespace3(){
    String source = "class A extends JPanel \n" +
        "           implements KeyListener // needed to read input\n" +
        "           implements /** zju */ SomeInterface // wtf? {\n";
    String expect = "class A extends JPanel \n" +
        "           implements KeyListener                        \n" +
        "           implements            SomeInterface          \n";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.indexOf('{')));
  }

  public void testReplaceCommentsWithWhitespace4(){
    String source = "// /**      \n"
        + "This Text Is Not Commented\n"
        + "// */  ";
    String expect = "            \n"
        + "This Text Is Not Commented\n"
        + "       ";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.length()-1));
  }
  
  public void testReplaceCommentsWithWhitespace5(){
    String source = "class A extends JPanel \r\n" +
        "           implements KeyListener // needed to read input\r\n" +
        "           implements /** zju */ SomeInterface // wtf? {\r\n";
    String expect = "class A extends JPanel \r\n" +
        "           implements KeyListener                        \r\n" +
        "           implements            SomeInterface          \r\n";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.indexOf('{')));
  }
  
   public void testReplaceCommentsWithWhitespace6(){
    String source = "// /**      \r\n"
        + "This Text Is Not Commented\r\n"
        + "// */  ";
    String expect = "            \r\n"
        + "This Text Is Not Commented\r\n"
        + "       ";
    assertEquals(expect, StringUtil.replaceCommentsWithWhitespaces(source,
        0, source.length()-1));
  }

  /**
   * @param bases
   * @param searches
   */
  private void indexesOfSubPhraseTest(final String bases, final String searches) {
    String[] base = new PhraseSplitter(bases).getAllWords();
    String[] search = new PhraseSplitter(searches).getAllWords();
    int[][] positions = StringUtil.indexesOfSubPhrase(base, search);
    assertTrue(positions.length > 0);
    for (int i = 0; i < positions.length; i++) {
      assertTrue(base[positions[i][0]].equalsIgnoreCase(search[0]));
      assertTrue("end position is bigger or equals to the end position",
          positions[i][1] >= positions[i][0]);
    }
  }
}
