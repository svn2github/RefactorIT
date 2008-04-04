/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import junit.framework.TestCase;

public class WordUtilsTest extends TestCase {

  public void testWordStartsWithCapitalLetter1() {
    char[] str = "Tallinn".toCharArray();
    
    assertTrue(new String(str) + " starts with capital letter", WordUtils
        .isCapitalLetterFirst(str));
  }
  
  public void testWordStartsWithCapitalLetter2() {
    char[] str = "TalliNN".toCharArray();
    
    assertTrue(new String(str) + " starts with capital letter", WordUtils
        .isCapitalLetterFirst(str));
  }
  
  public void testAllLettersAreCapital1() {
    char[] str = "TALLINN".toCharArray();
    
    assertTrue(new String(str) + " - all letters are capital", WordUtils
        .isAllCapitalLetters(str));
  }
  
  public void testAllLettersAreCapital2() {
    char[] str = "BABY".toCharArray();
    
    assertTrue(new String(str) + " - all letters are capital", WordUtils
        .isAllCapitalLetters(str));
  }
  
  public void testStartsWithLowecaseLetter1() {
    char[] str = "xxx".toCharArray();
    
    assertTrue(new String(str) + " - starts with lowercase letter", WordUtils
        .isLowercaseLetterFirst(str));
  }
  
  public void testStartsWithLowecaseLetter2() {
    char[] str = "xXXX".toCharArray();
    
    assertTrue(new String(str) + " - starts with lowercase letter", WordUtils
        .isLowercaseLetterFirst(str));
  }
  
  public void testMeaninglessString() {
    char[] str = "_".toCharArray();
    
    assertTrue(new String(str) + " - is meaningless string", WordUtils
        .isMeaningless(str));
  }
  
  public void testNumericString() {
    char[] str = "123".toCharArray();
    
    assertTrue(new String(str) + " - is numeric string", WordUtils
        .isNumeric(str));
  }
  
  public void testNextWordPosition() {
    String[] words = {"I", "_", "_", "T", "_"};
    assertEquals(-1, WordUtils.getNextWordPosition(words, -1));
    assertEquals(0, WordUtils.getNextWordPosition(words, 0));
    assertEquals(3, WordUtils.getNextWordPosition(words, 0+1));
    assertEquals(-1, WordUtils.getNextWordPosition(words, 3+1));
  }

  public void testPreviousWordPosition() {
    String[] words = {"_", "A", "I", "_", "_", "T", "_"};
    assertEquals(-1, WordUtils.getPreviousWordPosition(words, words.length));
    assertEquals(5, WordUtils.getPreviousWordPosition(words, words.length - 1));
    assertEquals(2, WordUtils.getPreviousWordPosition(words, 5 - 1));
    assertEquals(1, WordUtils.getPreviousWordPosition(words, 2 - 1));
    assertEquals(-1, WordUtils.getPreviousWordPosition(words, 1 - 1));
  }

  
}
