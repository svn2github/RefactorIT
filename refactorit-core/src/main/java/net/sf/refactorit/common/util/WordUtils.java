/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

public class WordUtils {
  public static final int STARTS_WITH_CAPITAL_LETTER = 0; // DO NOT CHANGE!
  public static final int STARTS_WITH_LOWERCASE_LETTER = 1; // DO NOT CHANGE!
  public static final int ALL_LETTERS_ARE_CAPITAL = 2; // DO NOT CHANGE!
  public static final int MEANINGLESS_STRING = 3; // DO NOT CHANGE!
  public static final int NUMERIC_STRING = 4; // DO NOT CHANGE!

  public static int getStyle(char[] str) {
    int type = ALL_LETTERS_ARE_CAPITAL;
    
    for (int i = 0; i < str.length; i++) {
      if (Character.isLetter(str[i])) {
        if (Character.isUpperCase(str[i])) {
          continue;
        }

        if (i == 0) {
          type = STARTS_WITH_LOWERCASE_LETTER;
        } else {
          type = STARTS_WITH_CAPITAL_LETTER;
        }
        break;
      } else if(Character.isDigit(str[i])){
        type = NUMERIC_STRING;
        break;
      } else {
        type = MEANINGLESS_STRING;
        break;
      }
    }
    
    return type; 
  }
  
  public static boolean isAllCapitalLetters(char[] str) {
    return getStyle(str) == ALL_LETTERS_ARE_CAPITAL;
  }
  
  public static boolean isCapitalLetterFirst(char[] str) {
    return getStyle(str) == STARTS_WITH_CAPITAL_LETTER;
  }
  
  public static boolean isLowercaseLetterFirst(char[] str) {
    return getStyle(str) == STARTS_WITH_LOWERCASE_LETTER;
  }

  /**
   * Test if the string is starting with not letter or digit
   */
  public static boolean isMeaningless(char[] str) {
    return getStyle(str) == MEANINGLESS_STRING;
  }

  public static boolean isNumeric(char[] str) {
    return getStyle(str) == NUMERIC_STRING;
  }  
  
  /**
   * formats specified char array to the new format
   * @return
   */
  public static char[] format(char[] str, int format) {
    char[] newStr = new char[str.length];
    switch (format) {
    case STARTS_WITH_CAPITAL_LETTER:
      for(int i = 0; i < str.length; i++) {
        if(i == 0) {
          newStr[i] = Character.toUpperCase(str[i]);
        } else {
          newStr[i] = Character.toLowerCase(str[i]);
        }
      }
      return newStr;
    case STARTS_WITH_LOWERCASE_LETTER:
      for(int i = 0; i < str.length; i++) {
         newStr[i] = Character.toLowerCase(str[i]);
      }
      return newStr;
    case ALL_LETTERS_ARE_CAPITAL:
      for(int i = 0; i < str.length; i++) {
        newStr[i] = Character.toUpperCase(str[i]);
      }
      return newStr;
    case MEANINGLESS_STRING:
    case NUMERIC_STRING:    
      return str;
    default:
      throw new IllegalArgumentException("Unsupported word format!");
    }
  }

  /**
   * Looks for the next meaningful word in the sequence.
   * @return position of the next meaningful word in the phrase or -1 in no word found
   */
  public static int getNextWordPosition(String[] phrase, int offset) {
    int pos = -1;
    
    if(offset < 0) {
      return pos;
    }
    
    while(offset < phrase.length) {
      if(WordUtils.isMeaningless(phrase[offset].toCharArray())) {
        offset++;
        continue;
      } else {
        pos = offset;
        break;
      }
    }
    
    return pos;
  }
  
  /**
   * Looks for the previous meaningful word in the sequence.
   * @return position of the previous meaningful word in the phrase or -1 in no word found
   */
  public static int getPreviousWordPosition(String[] phrase, int offset) {
    int pos = -1;
    
    if(offset >= phrase.length) {
      return pos;
    }
    
    while(offset >= 0) {
      if(WordUtils.isMeaningless(phrase[offset].toCharArray())) {
        offset--;
        continue;
      } else {
        pos = offset;
        break;
      }
    }
    
    return pos;
  }
  
}
