/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.io.ByteArrayOutputStream;


/**
 *
 * @author  tanel
 */
public final class HexUtil {

  /** Field HEX_DIGITS */
  private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'A', 'B', 'C', 'D',
      'E', 'F'};

  /**
   *  converts String to Hex String. Example: niko ->6E696B6F
   *
   * @param  ba      Description of Parameter
   * @param  offset  Description of Parameter
   * @param  length  Description of Parameter
   * @return         Description of the Returned Value
   */
  public static String toHexString(byte[] ba, int offset, int length) {

    char[] buf;

    buf = new char[length * 2];

    for (int i = offset, j = 0, k; i < offset + length; ) {
      k = ba[i++];
      buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
      buf[j++] = HEX_DIGITS[k & 0x0F];

    }

    return new String(buf);
  }

  /**
   * Converts readable hex-String to byteArray
   *
   * @param strA
   * @return array of bytes
   */
  public static byte[] hexStringToByteArray(String strA) {
    ByteArrayOutputStream result = new ByteArrayOutputStream();

    byte sum = (byte) 0x00;
    boolean nextCharIsUpper = true;

    for (int i = 0; i < strA.length(); i++) {
      char c = strA.charAt(i);

      switch (Character.toUpperCase(c)) {

        case '0':
          if (nextCharIsUpper) {
            sum = (byte) 0x00;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x00;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '1':
          if (nextCharIsUpper) {
            sum = (byte) 0x10;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x01;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '2':
          if (nextCharIsUpper) {
            sum = (byte) 0x20;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x02;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '3':
          if (nextCharIsUpper) {
            sum = (byte) 0x30;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x03;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '4':
          if (nextCharIsUpper) {
            sum = (byte) 0x40;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x04;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '5':
          if (nextCharIsUpper) {
            sum = (byte) 0x50;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x05;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '6':
          if (nextCharIsUpper) {
            sum = (byte) 0x60;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x06;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '7':
          if (nextCharIsUpper) {
            sum = (byte) 0x70;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x07;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '8':
          if (nextCharIsUpper) {
            sum = (byte) 0x80;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x08;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case '9':
          if (nextCharIsUpper) {
            sum = (byte) 0x90;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x09;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'A':
          if (nextCharIsUpper) {
            sum = (byte) 0xA0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0A;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'B':
          if (nextCharIsUpper) {
            sum = (byte) 0xB0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0B;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'C':
          if (nextCharIsUpper) {
            sum = (byte) 0xC0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0C;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'D':
          if (nextCharIsUpper) {
            sum = (byte) 0xD0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0D;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'E':
          if (nextCharIsUpper) {
            sum = (byte) 0xE0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0E;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;

        case 'F':
          if (nextCharIsUpper) {
            sum = (byte) 0xF0;
            nextCharIsUpper = false;
          } else {
            sum |= (byte) 0x0F;
            result.write(sum);
            nextCharIsUpper = true;
          }
          break;
      }
    }

    if (!nextCharIsUpper) {
      throw new RuntimeException(
          "The String did not contain an equal number of hex digits");
    }

    return result.toByteArray();
  }

}
