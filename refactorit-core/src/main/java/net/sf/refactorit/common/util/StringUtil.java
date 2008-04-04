/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Set of methods for string operations
 *
 * @author Siim Kaalep
 * @author Anton Safonov
 * @author Vladislav Vislogubov
 * @author Tanel Alum?e
 * @author Arseni Grigorjev
 */
public final class StringUtil {

  public static final String EMPTY_STRING = "";

  public static final String[] NO_STRINGS = new String[0];

  private static final char SEPARATOR = '|';

  public static final String NEWLINE = System.getProperty("line.separator");

  public static final String PATH_SEPARATOR
      = System.getProperty("path.separator");

  public static final class ToStringComparator implements Comparator {
    public final int compare(Object a, Object b) {
      return a.toString().compareTo(b.toString());
    }

    public final boolean equals(Object o) {
      return o == this;
    }
  }

  /**
   * Gets the stack trace of exception as string
   */
  public static String getStackTrace(final Throwable e) {
    final StringWriter s = new StringWriter();
    e.printStackTrace(new PrintWriter(s));
    return s.toString();
  }

  /**
   * Splits string into strings array separating it with comma
   * @param toSplit string to split
   * @return String array
   */
  public static String[] split(final String toSplit) {
    return split(toSplit, ",");
  }

  /**
   * Splits strings into strings array separating it with given splitter
   * @param toSplit string to split
   * @param splitter separator in string
   * @return String array
   */
  public static String[] split(final String toSplit, final String splitter) {
    if (toSplit == null) {
      return NO_STRINGS;
    }
    final ArrayList retVal = new ArrayList();
    int splitPos = toSplit.indexOf(splitter);
    int lastPos = 0;
    while (splitPos > -1) {
      retVal.add(toSplit.substring(lastPos, splitPos));
      lastPos = splitPos + splitter.length();
      if (lastPos >= toSplit.length()) {
        break;
      }
      splitPos = toSplit.indexOf(splitter, lastPos);
    }
    if (lastPos < toSplit.length()) {
      retVal.add(toSplit.substring(lastPos));
    } else if (lastPos == toSplit.length()) {
      retVal.add("");
    }
    return (String[]) retVal.toArray(new String[retVal.size()]);
  }

  /**
   * Joins strings from list of strings, using comma as separator to one string.
   * @param toJoin list of strings to join
   */
  public static String join(final List toJoin) {
    return join(toJoin, ",");
  }

  /**
   * Joins strings from list of strings, using custom separator.
   * @param toJoin list of strings to join
   * @param joiner string to use as separator
   */
  public static String join(final List toJoin, final String joiner) {
    final String[] tmp = (String[]) toJoin.toArray(new String[toJoin.size()]);
    return join(tmp, joiner);
  }

  /**
   * Joins strings from array of strings, using comma as separator to one string.
   * @param toJoin array of strings to join
   */
  public static String join(final String[] toJoin) {
    return join(toJoin, ",");
  }

  /**
   * Joins strings from array of strings, using custom separator.
   * @param toJoin array of strings to join
   * @param joiner string to use as separator
   */
  public static String join(final String[] toJoin, final String joiner) {
    if (toJoin.length == 0) {
      return "";
    }
    String retVal = toJoin[0];
    for (int q = 1; q < toJoin.length; q++) {
      retVal += joiner + toJoin[q];
    }
    return retVal;
  }

  public static String replace(final String string, final char oldChar,
      final char newChar) {
    if (string == null) {return null;
    }
    if (oldChar == newChar) {return string;
    }

    final char[] sc = string.toCharArray();
    for (int i = 0; i < sc.length; ++i) {
      if (sc[i] == oldChar) {
        sc[i] = newChar;
      }
    }
    return new String(sc);

  }

  public static String replace(final String string, final String newValue,
      final int startColumn, final int endColumn) {
    if ((string == null) || (newValue == null) ||
        (startColumn < 0) || (endColumn < 0) || (startColumn > endColumn)) {
      return string;
    }

    final StringBuffer result = new StringBuffer();

    result.append(string.substring(0, startColumn));
    result.append(newValue);
    result.append(string.substring(endColumn + 1, string.length()));

    return result.toString();
  }

  /**
   * Replaces one pattern to anouther pattern in given string.
   * @param string string to find from
   * @param oldValue pattern to seek
   * @param newValue pattern to replace with
   * @return modified string
   */
  public static String replace(final String string, final String oldValue,
      final String newValue) {
    if (string == null) {
      return null;
    }
    int oldPos = string.indexOf(oldValue);
    if (oldPos < 0) {
      return string;
    }
    final StringBuffer retVal = new StringBuffer(
        string.length() + Math.max((newValue.length() - oldValue.length()) * 5,
        0));
    int lastPos = 0;
    while (oldPos > -1) {
      retVal.append(string.substring(lastPos, oldPos));
      retVal.append(newValue);
      lastPos = oldPos + oldValue.length();
      oldPos = string.indexOf(oldValue, lastPos);
    }
    if (lastPos < string.length()) {
      retVal.append(string.substring(lastPos));
    }
    return retVal.toString();
  }

  /**
   * Counts occurences of pattern in string.
   * @param string string to seek from
   * @param toCount pattern to count
   * @return number of occurences
   */
  public static int count(final String string, final String toCount) {
    if (string == null) {
      return 0;
    }

    int retVal = 0;
    int oldPos = string.indexOf(toCount);
    while (oldPos > -1) {
      retVal++;
      oldPos = string.indexOf(toCount, oldPos + toCount.length());
    }
    return retVal;
  }

  /**
   * Adds zero to beginning of number if it is <10
   * @param number number to format
   * @return formatted string
   */
  public static String addZero(final int number) {
    String retVal = number + "";
    if (number < 10) {
      retVal = "0" + retVal;
    }
    return retVal;
  }

  /**
   * Checks whether string can be taken as number.
   * @param string string to test
   * @return true if string can be converted to number otherwise false
   */
  public static boolean isNumeric(final String string) {
    boolean retVal = true;
    try {
      Float.parseFloat(string);
    } catch (NumberFormatException e) {
      retVal = false;
    }
    return retVal;
  }

  /**
   * Removes sequences of space characters and replaces them with one space char.
   * @param string string to modify
   * @return modified string
   */
  public static String removeExtraSpaces(String string) {
    boolean changed = true;
    while (changed) {
      changed = false;
      final String newString = StringUtil.replace(string, "  ", " ");
      if (!newString.equals(string)) {
        changed = true;
        string = newString;
      }
    }
    return string;
  }

  /**
   * Detects UTF-8 string
   * For algorithm, see http://mail.nl.linux.org/linux-utf8/1999-09/msg00110.html
   * @param str source string with unknown encoding
   * @return true if String was UTF8 encoded, false if wasnt UTF8
   */
  public static boolean isUTF8(final String str) {
    if (str.length() == 0) {
      return true;
    }
    byte[] byteArray;
    try {
      // get wanted bytes
      byteArray = str.getBytes("ISO-8859-1");
    } catch (Exception e) {
      byteArray = str.getBytes();
    }

    byte current_byte,
        previous_byte = 0;
    int count_good_utf = 0,
        count_bad_utf = 0,
        index;

    // parse each byte
    for (index = 0; index < byteArray.length; index++) {
      current_byte = byteArray[index];

      // maintain counters
      if ((current_byte & 0xC0) == 0x80) {
        if ((previous_byte & 0xC0) == 0xC0) {
          count_good_utf++;
        } else if ((previous_byte & 0x80) == 0x00) {
          count_bad_utf++;
        }
      } else if ((previous_byte & 0xC0) == 0xC0) {
        count_bad_utf++;
      }

      // store current byte in previous byte
      previous_byte = current_byte;
    }

    if (count_good_utf > count_bad_utf) {
      return true;
    }
    if (count_good_utf == 0 && count_bad_utf == 0) {
      return true;
    }
    return false;
  }

  public static String generateSpaces(final int amount) {
    if (amount <= 0) {return "";
    }

    final StringBuffer sb = new StringBuffer(amount);
    for (int i = 0; i < amount; i++) {
      sb.append(' ');
    }

    return sb.toString();
  }

  /*
   * Checks whether text is one single word
   * @author Vladislav Vislogubov
   */
  public static boolean isSingleWord(final String text) {
    if (text == null || text.length() == 0) {
      return false;
    }

    final int size = text.length();
    for (int i = 0; i < size; i++) {
      if (!Character.isLetterOrDigit(text.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /** Just removes HTML tags, no conversion of the internal text happens. */
  public static String removeHtmlTags(final String html) {
    final StringBuffer result = new StringBuffer();

    boolean tagOpen = false;

    for (int i = 0; i < html.length(); i++) {
      if (html.charAt(i) == '<') {
        tagOpen = true;
      } else if (html.charAt(i) == '>') {
        tagOpen = false;
      } else if (!tagOpen) {
        result.append(html.charAt(i));
      }
    }

    return result.toString();
  }

  /**
   * 1. remove pure HTML tags <br>
   * 2. when convert rest of the text to the normal form,
   * see {@link #entitiesIntoText(String text)}
   */
  public static String removeHtml(final String text) {
    return entitiesIntoText(removeHtmlTags(text));
  }

  /** <UL>
   * <LI>"&amp;nbsp;" -&gt; " "</LI>
   * <LI>"&amp;lt;" -&gt; "&lt;"</LI>
   * <LI>"&amp;gt;" -&gt; "&gt;"</LI>
   * </UL> */
  public static String entitiesIntoText(final String html) {
    String result = StringUtil.replace(html, "&nbsp;", " ");
    result = StringUtil.replace(result, "&lt;", "<");
    result = StringUtil.replace(result, "&gt;", ">");

    return result;
  }

  /** <UL>
   * <LI>"&lt;" -&gt; "&amp;lt;"</LI>
   * <LI>"&gt;" -&gt; "&amp;gt;"</LI>
   * </UL>
   */
  public static String tagsIntoHTML(final String text) {
    String result = StringUtil.replace(text, "<", "&lt;");
    result = StringUtil.replace(result, ">", "&gt;");
    return result;
  }

  /** <UL>
   * <LI>" " -&gt; "&amp;nbsp;"</LI>
   * <LI>"&lt;" -&gt; "&amp;lt;"</LI>
   * <LI>"&gt;" -&gt; "&amp;gt;"</LI>
   * </UL> */
  public static String textIntoHTML(final String text) {
    if (text.length() == 0) {
      return "&nbsp;";
    }

    String result = StringUtil.replace(text, "<", "&lt;");
    result = StringUtil.replace(result, ">", "&gt;");
    result = StringUtil.replace(result, " ", "&nbsp;");

    return result;
  }

  /** Utility method capitalizes the first letter of string, used to
   * generate method names for patterns
   * @param str The string for capitalization.
   * @return String with the first letter capitalized.
   */
  public static String capitalizeFirstLetter(final String str) {
    if (str == null || str.length() <= 0) {
      return str;
    }

    final char[] chars = str.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    return new String(chars);
  }

  /** Utility method decapitalizes the first letter of string, used to
   * generate variable names from type name
   * @param str string for decapitalization.
   * @return string with the first letter decapitalized.
   */
  public static String decapitalizeFirstLetter(final String str) {
    if (str == null || str.length() <= 0) {
      return str;
    }

    final char[] chars = str.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }

  /**
   * Detects:<ul>
   * <li>/r/n - Windows</li>
   * <li>/n - Unix</li>
   * <li>/r - Mac</li>
   * </ul>
   *
   * @param text searches for a first occurence of linebreak in the given text
   * @return linebreak string
   */
  public static String findEndOfLine(final String text) {
    final StringBuffer eol = new StringBuffer(2);

    for (int i = 0, max = text.length(); i < max; i++) {
      final char c = text.charAt(i);

      if (c == '\r') {
        if (eol.length() == 0) {
          eol.append(c);
          continue;
        }
      }

      if (c == '\n') {
        eol.append(c);
        break;
      }

      if (eol.length() > 0) {
        break;
      }
    }

    return eol.toString();
  }

  public static final String printableLinebreaks(final String text) {
    String result = text;
    result = StringUtil.replace(result, "\n", "\\n");
    result = StringUtil.replace(result, "\r", "\\r");
    result = StringUtil.replace(result, "\t", "\\t");

    return result;
  }

  public static final String getCommonPart(final String str1, final String str2) {
    int min = Math.min(str1.length(), str2.length());
    for (int i = 0; i < min; i++) {
      if (str1.charAt(i) != str2.charAt(i)) {
        return str1.substring(0, i);
      }
    }

    return (str1.length() < str2.length()) ? str1 : str2;
  }

  private static void appendStringEscapingSeparator(final StringBuffer result,
      final String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == SEPARATOR) {
        result.append(SEPARATOR);
        result.append(SEPARATOR);
      } else {
        result.append(s.charAt(i));
      }
    }
  }

  /** Skips empty and null strings */
  public static String serializeStringList(final List strings) {
    if (strings == null) {
      return null;
    }

    final StringBuffer result = new StringBuffer();

    final Iterator i = strings.iterator();
    if (i.hasNext()) {
      appendStringEscapingSeparator(result, i.next().toString());
    }

    while (i.hasNext()) {
      final String s = i.next().toString();

      if (!"".equals(s) && s != null) {
        result.append(SEPARATOR);
        appendStringEscapingSeparator(result, s);
      }
    }

    return result.toString();
  }

  public static List deserializeStringList(final String strings) {
    if (strings == null) {
      return null;
    }

    final List result = new ArrayList();
    StringBuffer word = new StringBuffer();

    int i = 0;
    while (i < strings.length()) {
      if (strings.charAt(i) == SEPARATOR) {
        if (i < strings.length() - 1 && strings.charAt(i + 1) == SEPARATOR) {
          word.append(SEPARATOR);
          i++;
        } else {
          result.add(word.toString());
          word = new StringBuffer();
        }
      } else {
        word.append(strings.charAt(i));
      }

      i++;
    }

    if (!"".equals(word.toString())) {
      result.add(word.toString());
    }

    return result;
  }

  public static int lineBreakCount(final String s) {
    return Math.max(count(s, "\n"), count(s, "\r"));
  }

  public static String getLinebreak(final String str) {
    if (str == null || str.length() == 0) {
      return null;
    }

    int i = str.length() - 1;
    while (i >= 0) {
      final char c = str.charAt(i);
      if (c != '\r' && c != '\n') {
        i++;
        break;
      } else if (i == 0) {
        break;
      }
      --i;
    }

    if (i >= 0 && i < str.length()) {
      return str.substring(i);
    }

    return null;
  }

  public static String mergeArrayIntoString(final Object[] array,
      String middleDelimiter, String lastDelimiter) {

    if (array == null) {
      return null;
    }
    if (middleDelimiter == null) {
      middleDelimiter = "";
    }
    if(lastDelimiter == null) {
      lastDelimiter = middleDelimiter;
    }

    final StringBuffer str = new StringBuffer();
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        if(i == array.length - 1) {
          str.append(lastDelimiter);
        } else {
          str.append(middleDelimiter);
        }
      }
      str.append(array[i].toString());
    }

    return str.toString();
  }

  public static String mergeArrayIntoString(Object[] array, String delimiter) {
    return mergeArrayIntoString(array, delimiter, delimiter);
  }

  public static final int findPrefixWhitespace(final String content,
      int fromIndex) {
    while (fromIndex - 1 >= 0
        && Character.isWhitespace(content.charAt(fromIndex - 1))) {
      --fromIndex;
    }
    return fromIndex;
  }

  public static final int findPostfixWhitespace(final String content,
      int fromIndex) {
    while (fromIndex < content.length()
        && Character.isWhitespace(content.charAt(fromIndex))) {
      ++fromIndex;
    }
    return fromIndex;
  }
  
  /**
   * Replaces all comments with whitespaces in given range.
   *
   *  @param str some string to replace in
   *  @param beginIndex start from
   *  @param endColumn stop at
   *
   *  @return new string with changes
   */
  public static String replaceCommentsWithWhitespaces(String str,
      int beginIndex, int endIndex) {
    StringBuffer buf = new StringBuffer(str);
    int pos, end;

    // Replaces // style comments
    pos = beginIndex;
    end = beginIndex;
    do {
      pos = buf.indexOf("//", pos);
      if (pos == -1){
        break;
      } else if (pos > endIndex){
        break;
      } else {
        end = buf.indexOf("\n", pos);
        if (end == -1){
          end = buf.length() - 1;
        }
        while (pos <= end-1 && pos < buf.length()){
          if (buf.charAt(pos) != '\r'){ // windows linebreaks support
            buf.setCharAt(pos, ' ');
          }
          ++pos;
        }
      }
    } while (true);

    // Replaces /* */ and /** */ style comments
    pos = beginIndex;
    end = beginIndex;
    do {
      pos = buf.indexOf("/*", pos);
      if (pos == -1){
        break;
      } else if (pos > endIndex){
        break;
      } else {
        end = buf.indexOf("*/", pos);
        if (end == -1){
          end = buf.length() - 2;
        }
        while (pos <= end + 1 && pos < buf.length()){
          buf.setCharAt(pos++, ' ');
        }
      }
    } while (true);

    return new String(buf);
  }

  public static final int moveOneLinebreakBack(final String content,
      final int fromIndex) {
    boolean eolStarted = false;
    int index = fromIndex;

    for (; index > 0; index--) {
      if (content.charAt(index - 1) != '\n'
          && content.charAt(index - 1) != '\r') {
        break;
      }

      if (content.charAt(index - 1) == '\n') {
        if (!eolStarted) {
          eolStarted = true;
          continue;
        }
      }

      if (content.charAt(index - 1) == '\r') {
        --index;
        break;
      }

      if (eolStarted) {
        break;
      }
    }

    return index;
  }

  public static final int moveOneLinebreakForth(final String content,
      final int fromIndex) {
    boolean eolStarted = false;
    int index = fromIndex;

    for (; index < content.length(); index++) {
      if (content.charAt(index) != '\n'
          && content.charAt(index) != '\r') {
        break;
      }

      if (content.charAt(index) == '\r') {
        if (!eolStarted) {
          eolStarted = true;
          continue;
        }
      }

      if (content.charAt(index) == '\n') {
        ++index;
        break;
      }

      if (eolStarted) {
        break;
      }
    }

    return index;
  }

  public static String toString(final boolean[] array) {
    String result = "[";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        result += ", ";
      }
      result += array[i];
    }

    return result + "]";
  }

  public static String toString(final int[] array) {
    String result = "[";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        result += ", ";
      }
      result += array[i];
    }

    return result + "]";
  }

  public static boolean containsOnlyWhitespaceAndComments(String s) {
    boolean inComment = false;

    for (int i = 0; i < s.length(); i++) {
      char current = s.charAt(i);

      if (i + 1 < s.length()) {
        char next = s.charAt(i + 1);

        if (current == '/' && next == '/') {
          return true;
        } else if (current == '/' && next == '*') {
          inComment = true;
          i++;
          continue;
        } else if (inComment && current == '*' && next == '/') {
          inComment = false;
          i++;
          continue;
        }
      }

      if (!Character.isWhitespace(current) && !inComment) {
        return false;
      }
    }
    return true;
  }

  public static String getIndent(String s) {
    StringBuffer result = new StringBuffer();

    for (int i = 0; i < s.length() && Character.isWhitespace(s.charAt(i)); i++) {
      result.append(s.charAt(i));
    }

    return result.toString();
  }

  public static boolean startsWith(StringBuffer line, String string) {
    if (line.length() < string.length()) {
      return false;
    }
    for (int i = 0; i < string.length(); i++) {
      if (line.charAt(i) != string.charAt(i)) {
        return false;
      }

    }
    return true;
  }

  public static String countToString(int count, String noun) {
    String result = count + " " + noun;
    if (count != 1) {
      result += "s";
    }
    return result;
  }

  /**
   * Returns last upper case word
   * @param name
   * postcond: if name.length() > 2 then result length >= 2
   */
  public static String getLastWordFromIdentifier(String name) {
    int startIndex = 0;
    for (int i = name.length() - 2; i >= 0; --i) {
      if (Character.isUpperCase(name.charAt(i))) {
        startIndex = i;
        break;
      }
//      else {
//        if ( startIndex != 0 ) {
//          break;
//        }
//      }
    }
    return name.substring(startIndex);
  }

  public static int[] getSystemPropertyAsVersionNumber(String propertyName,
      int minimumLength) throws CheckedNumberFormatException {
    String specVersion = System.getProperty(propertyName);
    return parseVersionNumberParts(specVersion, minimumLength);
  }

  public static boolean versionAtLeast(final int[] requiredVersion,
      final int[] realVersion) {
    for (int i = 0; i < requiredVersion.length; i++) {
      if (realVersion[i] < requiredVersion[i]) {
        return false;
      } else if (realVersion[i] > requiredVersion[i]) {
        return true;
      }
    }

    return true;
  }

  /**
   * Examples:
   *    "2.1.6", 3 => new int[] { 2, 1, 6 }
   *    "3_2"  , 3 => new int[] { 3, 2, 0 }
   *    "3"    , 3 => new int[] { 3, 0, 0 }
   */
  public static int[] parseVersionNumberParts(String s,
      int minimumLength) throws CheckedNumberFormatException {
    s = replace(s, "_", ".");
    s = replace(s, "-", ".");
    int lettersStart = 0;
    for (; lettersStart < s.length(); lettersStart++) {
      if (Character.isLetter(s.charAt(lettersStart))) {
        if (lettersStart > 0 && s.charAt(lettersStart - 1) == '.') {
          --lettersStart;
        }
        break;
      }
    }
    s = s.substring(0, lettersStart);
    List numbers = extractStringsAroundDots(s);

    while (numbers.size() < minimumLength) {
      numbers.add("0");
    }

    int[] result = new int[numbers.size()];
    for (int i = 0; i < numbers.size(); i++) {
      try {
        result[i] = Integer.parseInt(numbers.get(i).toString());
      } catch (NumberFormatException e) {
        throw new CheckedNumberFormatException(e);
      }
    }

    return result;
  }

  /** Example: "1.3" => a list that contains "1" and "3" */
  private static List extractStringsAroundDots(String s) {
    List result = new ArrayList();
    int pos = 0;

    while (s.indexOf('.', pos) >= 0) {
      int nextPos = s.indexOf('.', pos) + 1;
      result.add(s.substring(pos, nextPos - 1));
      pos = nextPos;
    }

    result.add(s.substring(pos));

    return result;
  }

  public static boolean systemPropertyVersionAtLeast(final int[]
      requiredVersion,
      final String propertyName) throws CheckedNumberFormatException {
    return StringUtil.versionAtLeast(requiredVersion,
        StringUtil.getSystemPropertyAsVersionNumber(propertyName,
        requiredVersion.length));
  }

  /**
   * A function that converts 'myABCVariable'-style name to a
   * 'MY_ABC_VARIABLE'=style.
   *
   * @param old the old name in so called 'camel notation' style
   *
   * @return new name in uppercase style with '_' characters between words
   */
  public static final String getUpercaseStyleName(String old){
    return insertDelimitersIntoCamelStyleNotation(old, '_').toUpperCase();
  }

  /**
   * A function that splits 'myABCVariable'-style name into single words like
   * 'my ABC Variable'.
   *
   * @param str  the name in so called 'camel notation' style
   *
   * @return the string with spaces between words
   */
  public static final String splitCamelStyleIntoWords(String str){
    return insertDelimitersIntoCamelStyleNotation(str, ' ');
  }

  /**
   * A function that will insert Your custom dilimiter between words, that are
   * written in so called 'camel notation'.<br><br>
   * For example: 'someVariableABCName' -> 'some[?]Variable[?]ABC[?]Name', where
   * '[?]' -- is Your custom dilimiter.<br><br>
   * See also splitCamelStyleIntoWords(String) (inserts spaces) and
   * getUpercaseStyleName(String) (inserts underscores).
   */
  public static final String insertDelimitersIntoCamelStyleNotation(
      String str, char dilimiter){
    StringBuffer temp = new StringBuffer(str);
    boolean prevUpper = false;
    boolean curUpper = false;
    boolean nextUpper = false;
    int ofst = 0;
    for (int i = 0; i < str.length(); i++){
      if (i == 0 && (Character.isUpperCase(str.charAt(i)))){
        curUpper = true;
      } else {
        prevUpper = curUpper;
        curUpper = nextUpper;
      }

      nextUpper = i+1 < str.length() && Character.isUpperCase(str.charAt(i+1));

      if (!curUpper && str.charAt(i) != dilimiter && nextUpper && i+1 < str.length()){
        temp.insert(i + ofst + 1, dilimiter);
        ofst++;
      }
    }

    return new String(temp);
  }

  /**
   * Counts similarity rate for two strings (0..100, 100 - absolutely equal,
   * 0 - not cimilar at all).
   *
   * @param str1 one string
   * @param str2 another string
   *
   * @return percentage of similarity
   */
  public static final float similarity(String str1, String str2) {
    if (str1.equalsIgnoreCase(str2)){
      return 100;
    }

    int pLen = str1.length();
    int aLen = str2.length();
    float avgLen = (float) (pLen + aLen) / 2;

    return 100 * getLongestIdenticalPartSize(str2, str1) / avgLen;
  }

  /**
   * Gets size of the longest identical subsequence of two strings. For example,
   * the longest identical subsequence of strings <i>"myBr<b>own</b>Fox"</i> and
   * <i>"my<b>Own</b>Wolf"</i> would be <b>"own"</b>, so the returned size is 3
   * (this function ignores case!).
   *
   * @param str1 first string
   * @param str2 second string
   *
   * @return size of longest identical subsequence of two strings
   */
  public static final int getLongestIdenticalPartSize(String str1, String str2){

    str1 = str1.toLowerCase();
    str2 = str2.toLowerCase();

    final int s1_len = str1.length();
    final int s2_len = str2.length();

    int[] rowState = new int[s2_len];

    for (int i = 0; i < rowState.length; i++){
      rowState[i] = 0;
    }

    int longestSeq = 0; // maximum size found so far
    boolean addNewSeq = false;

    for (int s1 = 0; s1 < s1_len; s1++){
      char c1 = str1.charAt(s1);
      for (int s2 = 0; s2 < s2_len; s2++){
        final boolean match = (c1 == str2.charAt(s2));
        if (s1 == 0 && match){
          rowState[s2] = 1;
        } else if (match){
          if (s2 == 0){
            addNewSeq = true;
          } else {
            rowState[s2-1]++;
          }
        } else if (!match && s2 != 0){
          if (rowState[s2-1] > longestSeq){
            longestSeq = rowState[s2-1];
            rowState[s2-1] = 0;
          }
        }
      }

      int goneSequence = shiftRightState(rowState);
      if (goneSequence > longestSeq){
        longestSeq = goneSequence;
      }
      if (addNewSeq){
        rowState[0] = 1;
        addNewSeq = false;
      }
    }

    for (int i = 0; i < rowState.length; i++){
      if (rowState[i] > longestSeq){
        longestSeq = rowState[i];
      }
    }

    // return the longest result
    return longestSeq;
  }

  public static final int shiftRightState(int[] state){
    int returnValue = state[state.length-1];
    for (int i = state.length-1; i > 0; i--){
      state[i] = state[i-1];
    }
    state[0] = 0;
    return returnValue;
  }
  
  public static String getIndent(final int depth, final int singleIndentSize){
    final StringBuffer result = new StringBuffer();
    final StringBuffer singleIndent = new StringBuffer();
    int i;

    for(i = 0; i < singleIndentSize; i++){
      singleIndent.append(' ');
    }

    for (i = 0; i < depth; i++){
      result.append(singleIndent);
    }

    return result.toString();
  }

  public static final class CheckedNumberFormatException extends RuntimeException {
    public CheckedNumberFormatException(NumberFormatException e) {
      super(e);
    }
  }

  /**
   * Splits indentifier into words
   * @refactoring maybe we shall refactor insertDelimitersIntoCamelStyleNotation 
   *  to return array of words already. 
   */
  public static String[] splitIdentifierToWords(String str) {
    str = insertDelimitersIntoCamelStyleNotation(str, '_');
    return str.split("_");
  }

  /**
   * replaces words in a phrase while preserving style
   * @return
   */
  public static String smartPhraseReplace(String base, String find, String replace) {
    PhraseSplitter baseSplitter = new PhraseSplitter(base);
    PhraseSplitter findSplitter = new PhraseSplitter(find);
    PhraseSplitter replaceSplitter = new PhraseSplitter(replace);
    
    String[] baseWords = baseSplitter.getAllWords();
    String[] findWords = findSplitter.getAllWords();
    String[] replaceWords = replaceSplitter.getAllWords();

    int pos[][] = StringUtil.indexesOfSubPhrase(baseWords, findWords);
    
    List result = new ArrayList();
    
    int end = 0;
    for(int i = 0; i < pos.length; i++) {
      for(int j = end; j < pos[i][0]; j++) {
        result.add(baseWords[j]);
      }
      String[] phrase = new String[pos[i][1] - pos[i][0] + 1];
      System.arraycopy(baseWords, pos[i][0], phrase, 0, pos[i][1] - pos[i][0] + 1);
      String[] replacedPhrase = StringUtil.smartWordReplace(phrase, replaceWords);
      
      CollectionUtil.addAll(result, replacedPhrase);
      
      end = pos[i][1] + 1;
    }
    
    for(int j = end; j < baseWords.length; j++) {
      result.add(baseWords[j]);
    }

    StringBuffer buffer = new StringBuffer();
    for(Iterator it = result.iterator(); it.hasNext(); ) {
      buffer.append(it.next());
    }
    
    return buffer.toString();
  }
  
  /**
   * applies new words and preserves the format
   * example: I_AM_A_ROBOT is replaced by IAmAMachine, return value is I_AM_A_MACHINE
   */
  public static String[] smartWordReplace(String[] base, String[] replace) {
    int baseIndex = -1;
    int replaceIndex = -1;
    
    int baseStartPosition = -1;
    int baseEndPosition = -1;
    
    int replaceStartPosition = -1;
    int replaceEndPosition = -1;
    
    // -----------------
    // detect, where the replacement will be applied
    baseIndex = WordUtils.getNextWordPosition(base, 0);
    replaceIndex = WordUtils.getNextWordPosition(replace, 0);
    
    while(baseIndex != -1 && replaceIndex != -1) {
      if(!base[baseIndex].equalsIgnoreCase(replace[replaceIndex])) {
        baseStartPosition = baseIndex;
        replaceStartPosition = replaceIndex;
        break;
      } else {
        baseIndex = WordUtils.getNextWordPosition(base, baseIndex + 1);
        replaceIndex = WordUtils.getNextWordPosition(replace, replaceIndex + 1);
      }
    }
    
    // -----------------
    // detect, where the replacement will end
    baseIndex = WordUtils.getNextWordPosition(base, base.length - 1);
    replaceIndex = WordUtils.getNextWordPosition(replace, replace.length - 1);
    
    while(baseIndex != -1 && replaceIndex != -1) {
      if(!base[baseIndex].equalsIgnoreCase(replace[replaceIndex])) {
        baseEndPosition = baseIndex;
        replaceEndPosition = replaceIndex;
        break;
      } else {
        baseIndex = WordUtils.getPreviousWordPosition(base, baseIndex - 1);
        replaceIndex = WordUtils.getPreviousWordPosition(replace, replaceIndex - 1);
      }
    }
    

    // if unable to replace we suggest that whole string is replacing
    if(baseStartPosition == -1 
        || baseEndPosition == -1
        || replaceStartPosition == -1
        || replaceEndPosition == -1) {
        return replace;
    }
    
    //  +1 is because indexes are inclusive
    ArrayList list = new ArrayList(base.length);
    for(int i = 0; i < baseStartPosition; i++) {
      list.add(base[i]);
    }
    
    // format detecting
    int style = -1;
    for(int i = baseStartPosition; i <= baseEndPosition; i++) {
      if(base[i].length() == 1) {
        continue;
      }
      style = WordUtils.getStyle(base[i].toCharArray());
      break;
    }
    
    if(style == -1) {
      HashMap map = new HashMap();
      for(int i = 0; i < base.length; i++) {
        int s = WordUtils.getStyle(base[i].toCharArray());
        Object o = map.get("" + s);
        if(o == null) {
          o = new int[]{0};
          map.put("" + s, o);
        }
        ((int[])o)[0]++;
      }
      Set set = map.entrySet();
      int styleNumberOccured = 0;
      for(Iterator it = set.iterator(); it.hasNext(); ) {
        Map.Entry entry = (Map.Entry)it.next();
        int value = ((int[])entry.getValue())[0];
        String key = (String)entry.getKey();
        if(value > styleNumberOccured) {
          try {
          styleNumberOccured = value;
          style = Integer.valueOf(key).intValue();
          } catch(NumberFormatException e) {
            style = WordUtils.STARTS_WITH_CAPITAL_LETTER;
          }
        }
      }
    }
    
    if(style == -1) {
      style = WordUtils.STARTS_WITH_CAPITAL_LETTER;
    }
    
    boolean hasDelimiters = false;
    if (style == WordUtils.ALL_LETTERS_ARE_CAPITAL) {
      hasDelimiters = true;
    } else {
      for(int i = baseStartPosition; i <= baseEndPosition; i++) {
        if(WordUtils.isMeaningless(base[i].toCharArray())) {
          hasDelimiters = true;
          break;
        }
      }
    }
     
    // replacing
    for(int i = replaceStartPosition; i <= replaceEndPosition; i++) {
      if(WordUtils.isMeaningless(replace[i].toCharArray())) {
        continue;
      }
      
      char[] ch = WordUtils.format(replace[i].toCharArray(), style);
      String word = new String(ch); 
      list.add(word);
      if(hasDelimiters && i != replaceEndPosition) {
        list.add("_");
      }
      
      // switching to camelStyle if no delimiter specified
      if(style == WordUtils.STARTS_WITH_LOWERCASE_LETTER && !hasDelimiters) {
        style = WordUtils.STARTS_WITH_CAPITAL_LETTER;
      }
    }
    // end of replacing

    //+1 is because indexes are inclusive (pointing to the last word that is needed to be replaced)
    for(int i = baseEndPosition + 1; i < base.length; i++) {
      list.add(base[i]);
    }    
    return (String[])list.toArray(new String[list.size()]);
  }
  
  /**
   * 
   * @param phrase1 
   * @param phrase2
   * @return double array of ints. [] - position. [][0] - startposition, [][1] - end position
   */
  public static int[][] indexesOfSubPhrase(String[] phrase1, String[] phrase2) {
    int[][] positions = new int[(int) Math.ceil((double)phrase1.length / (double)phrase2.length)][];
    int count = 0;
    
    int p1IndexStart = WordUtils.getNextWordPosition(phrase1, 0);
    int p1IndexEnd = 0;
    int p2Index = WordUtils.getNextWordPosition(phrase2, 0);
    
    while(p1IndexStart != -1 && p2Index != -1) {
      
      if(phrase1[p1IndexStart].equalsIgnoreCase(phrase2[p2Index])) {
        int p1Offset = p1IndexStart;
        int p2Offset = p2Index;
        boolean bingo = true;
        
        while(p2Offset != -1) {
          if(!phrase1[p1Offset].equalsIgnoreCase(phrase2[p2Offset])) {
            bingo = false;
            break;
          }

          p1IndexEnd = p1Offset;
          
          p1Offset = WordUtils.getNextWordPosition(phrase1, p1Offset + 1);
          p2Offset = WordUtils.getNextWordPosition(phrase2, p2Offset + 1);
          if(p1Offset == -1 && p2Offset != -1) {
            bingo = false;
            break;
          } 

        }
        
        if (bingo) {
          positions[count++] = new int[] { p1IndexStart, p1IndexEnd };
        }
      }
      
      p1IndexStart = WordUtils.getNextWordPosition(phrase1, p1IndexStart + 1);
      p1IndexEnd = p1IndexStart;
    }
   
    // trimming empty elements
    int[][] returnPositions = new int[count][2];
    System.arraycopy(positions, 0, returnPositions, 0, count);
    
    return returnPositions;
  }
  
  public static String getSlashedPath(String name) {
    return getPath(name, '/');
  }
  
  public static String getBackslashedPath(String name) {
    return getPath(name, '\\');
  }
  
  private static String getPath(String name, char delimiter) {
    String s = "";
    for(int i=0; i<name.length(); i++) {
      if(name.charAt(i) == '.') {
        s = s+delimiter;
      } else {
        s = s + name.charAt(i);
      }
    }
    return (s.equals(name) || s.trim().length() == 0)?null:s;
  }
}
