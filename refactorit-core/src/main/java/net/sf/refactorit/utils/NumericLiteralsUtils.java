/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.parser.JavaTokenTypes;

/**
 *
 * @author Arseni Grigorjev
 */
public final class NumericLiteralsUtils {
  
  private static final char[] DEC_DIGITS = {'0','1','2','3','4','5','6','7','8',
      '9'};
  private static final char[] OCT_DIGITS = {'0','1','2','3','4','5','6','7'};
  private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8',
      '9','a','b','c','d','e','f'};
  
  private NumericLiteralsUtils() {
  }
  
  /**
   * Determins if the literal of given BinLiteralExpression is a 
   * numeric-type literal
   */
  public static boolean isNumericLiteral(BinLiteralExpression literalExpr){
    int literalType = literalExpr.getNameAst().getType();
    return isNumericLiteral(literalType);
  }
  
  /**
   * Determins if the given literal type is a numeric-type
   */
  public static boolean isNumericLiteral(int literalType){
    return literalType == JavaTokenTypes.NUM_INT
        || literalType == JavaTokenTypes.NUM_LONG
        || literalType == JavaTokenTypes.NUM_FLOAT
        || literalType == JavaTokenTypes.NUM_DOUBLE;
  }
     
 /**
  * Determins if the given string is a valid numeric literal
  */
  public static boolean isValidNumLiteral(String literal){
    if (literal == null || literal.length() == 0){
      return false;
    }
    
    StringBuffer buffer = new StringBuffer();
    buffer.append(literal);
    
    char c = buffer.charAt(0);
    if (c == '-'){
      buffer.deleteCharAt(0);
    } 
        
    int comma = buffer.indexOf(".");
    if (comma == -1){ // it is an integer
      return checkIntegerLiteral(buffer);
    } else {
      return checkFloatLiteral(buffer, comma);
    }
  }
  
  private static boolean checkIntegerLiteral(StringBuffer buffer){
    char c = buffer.charAt(buffer.length()-1);
    if (c == 'L' || c == 'l'){
      buffer.deleteCharAt(buffer.length()-1);
      if (buffer.length() == 0){
        return false;
      }
    }

    c = buffer.charAt(0);
    if (c == '0'){
      if (buffer.length() == 1){ // if it is a zero literal: 0 or OL
        return true;
      }

      c = buffer.charAt(1);
      if (c == 'x' || c == 'X'){ // it is a hex integer
        buffer.deleteCharAt(0); 
        buffer.deleteCharAt(0);
        return checkDigits(buffer, HEX_DIGITS);
      } else { // it is an octal integer
        buffer.deleteCharAt(0);
        return checkDigits(buffer, OCT_DIGITS);
      }
    }
    return checkDigits(buffer, DEC_DIGITS);
  }
  
  private static boolean checkFloatLiteral(StringBuffer buffer, int commaIndex){
    char c = buffer.charAt(buffer.length()-1);
    if (c == 'D' || c == 'd' || c == 'F' || c == 'f'){
      buffer.deleteCharAt(buffer.length()-1);
      if (buffer.length() == 0){
        return false;
      }
    }
    
    boolean result = true;
    
    // search for exponent part
    int exp_index = buffer.indexOf("e");
    if (exp_index == -1){
      exp_index = buffer.indexOf("E");
    }
    
    if (exp_index >= 0){ // exponent part present
      result &= checkExponentPart(
          new StringBuffer(buffer.substring(exp_index+1)));
      buffer.delete(exp_index, buffer.length());
    }
    
    // check part before comma
    if (result && commaIndex != 0){ // if comma is not first char, like: .0566
      result &= checkDigits(new StringBuffer(buffer.substring(0, commaIndex-1)), 
          DEC_DIGITS);
    }
    
    // check part after comma
    if (result){
      result &= checkDigits(new StringBuffer(buffer.substring(commaIndex+1)), 
          DEC_DIGITS);
    }
    
    return result;
  }
  
  private static boolean checkExponentPart(StringBuffer buffer){
    char c = buffer.charAt(0);
    if (c == '+' || c == '-'){
      buffer.deleteCharAt(0);
      if (buffer.length() == 0){
        return false;
      }
    }
    
    return checkDigits(buffer, DEC_DIGITS);
  }
    
  private static boolean checkDigits(StringBuffer buffer, 
      char[] allowedChars){
    for (int i = 0; i < buffer.length(); i++){
      char c = buffer.charAt(i);
      char low = Character.toLowerCase(c);
      char upp = Character.toUpperCase(c);
      boolean found = false;
      for (int k = 0; k < allowedChars.length; k++){
        if (allowedChars[k] == low || allowedChars[k] == upp){
          found = true;
          break;
        }
      }
      if (!found){
        return false;
      }
    }
    return true;
  }
}
