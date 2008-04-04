/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;



/**
 * Checks user entered names for validity in Java.
 *
 * @author Villu
 * @author Anton Safonov
 */
public final class NameUtil {

  private NameUtil() {
  }

  /**
   * Checks if a given <code>string</code> is a legal identifier in Java 2.
   *
   * @param string to check for validity.
   * @return <code>true</code> when given <code>string</code> can be an
   * indentifier.
   */
  public static final boolean isValidIdentifier(String string) {
    return isValidName(string) && !isReservedWord(string);
  }

  /**
   * Checks if the passed parameter <code>string</code> is a legal package name.
   *
   * @param string to check for validity.
   * @return <code>true</code> when given <code>string</code> can be a package
   * name.
   */
  public static final boolean isValidPackageName(String string) {
    int count = 0;

    for (int current = 0, next = -1; current != -1;
        current = (next != -1 ? next + 1 : -1)) {

      next = string.indexOf('.', current);

      String token = string.substring(current,
          next != -1 ? next : string.length());

      if (!isValidIdentifier(token)) {
        return false;
      }

      count++;
    }

    return (count > 0);
  }

  private static final boolean isValidName(String string) {
    if (string == null || string.length() == 0) {
      return false;
    }

    if (!Character.isJavaIdentifierStart(string.charAt(0))) {
      return false;
    }

    // Check the rest of the characters making up given name
    for (int i = 0, max = string.length(); i < max; i++) {

      if (!Character.isJavaIdentifierPart(string.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if given <code>string</code> is reserved word in Java 2.<br>
   * Check is case-sensitive.
   *
   * @param string to check
   * @return <code>true</code> when <code>string</code> is a reserved word.
   */
  private static final boolean isReservedWord(String string) {

    for (int i = 0; i < reservedWords.length; i++) {
      if (reservedWords[i].equals(string)) {
        return true;
      }
    }

    return false; // not reserved
  }

  public static String extractConvenientVariableNameForType(BinTypeRef typeRef) {
    String name = typeRef.getName();

    final boolean isArray = typeRef.isArray();

    if (isArray) {
      typeRef = typeRef.getNonArrayType();
      name = typeRef.getName();
    }

    if (typeRef.isPrimitiveType()) {
      name = name.substring(0, 1);
    }

    if (name.equals("String")) {
      name = "str";
    } else if (name.equals("Object")) {
      name = "obj";
    } else if (name.equals("Class")) {
      name = "cls";
    }

    if (name.length() > 6) {
      name = StringUtil.getLastWordFromIdentifier(name);
    }

    StringBuffer buffer = new StringBuffer(name);

    if (isArray) {
      buffer.insert(0, "arr");
    } else {
      buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
    }

    String result = buffer.toString();

    if (NameUtil.isReservedWord(result)) {
      result += "Var";
    }

    if (Assert.enabled) {
      Assert.must(NameUtil.isValidIdentifier(result));
    }

    return result;
  }

  // The list of reserved words in Java 2. Taken from
  // http://java.sun.com/docs/books/tutorial/java/nutsandbolts/_keywords.html
  private static final String[] reservedWords = {
      "assert",
      "abstract", "boolean", "break",
      "byte", "case", "catch",
      "char", "class", "const",
      "continue", "default", "do",
      "double", "else", "enum", "extends",
      "final", "finally", "float",
      "for", "goto", "if",
      "implements", "import", "instanceof",
      "int", "interface", "long",
      "native", "new", "package",
      "private", "protected", "public",
      "return", "short", "static",
      "strictfp", "super", "switch",
      "synchronized", "this", "throw",
      "throws", "transient", "try",
      "void", "volatile", "while",
  };
}
