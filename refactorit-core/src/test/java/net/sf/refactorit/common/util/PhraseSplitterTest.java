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

public class PhraseSplitterTest extends TestCase {
  
  public void testWordDetection1() {
    String str = "IAmARobot";
    String[] expectedWords = { "I", "Am", "A", "Robot"};
    
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection2() {
    String str = "I_Am_A_Robot";
    String[] expectedWords = { "I", "Am", "A", "Robot"};
    
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection3() {
    String str = "getHTMLFormat";
    String[] expectedWords = { "get", "HTML",
        "Format"};
    
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection4() {
    String str = "run100Times";
    String[] expectedWords = { "run", "100", "Times"};
    
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection5() {
    String str = "A1GoThereNOW";
    String[] expectedWords = { "A", "1", "Go", "There", "NOW"};
    
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection6() {
    String str = "Here_";
    String[] expectedWords = { "Here"};
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection7() {
    String str = "Here_11";
    String[] expectedWords = { "Here", "11"};
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection8() {
    String str = "Here____";
    String[] expectedWords = { "Here"};
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection9() {
    String str = "___IAmHere_T0000__";
    String[] expectedWords = {"I", "Am", "Here", "T", "0000"};
    wordDetection(str, expectedWords);
  }

  public void testWordDetection10() {
    String str = "";
    String[] expectedWords = new String[]{};
    wordDetection(str, expectedWords);
  }
  
  public void testWordDetection11() {
    String str = "_____";
    String[] expectedWords = new String[] {};
    wordDetection(str, expectedWords);
  }
  
  private void wordDetection(String string, String[] expectedWords) {
    PhraseSplitter convention = new PhraseSplitter(string);
    String[] detectedWords = convention.getSignificantWords();
    assertTrue("Expected word number is " + expectedWords.length
        + ", detected word number is " + detectedWords.length,
        expectedWords.length == detectedWords.length);
    for(int i = 0; i < expectedWords.length; i++) {
      assertTrue("Expected word is " + expectedWords[i] + ", detected word is " + detectedWords[i],
          expectedWords[i].equals(detectedWords[i]));
    }
  }
}
