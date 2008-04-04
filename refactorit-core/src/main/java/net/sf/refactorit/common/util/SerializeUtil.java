/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;


public final class SerializeUtil {

  public static String serializeToString(Object o) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ObjectOutputStream stream = new ObjectOutputStream(bytes);
    stream.writeObject(o);
    stream.flush();
    return bytesToString(bytes.toByteArray());
  }

  public static Object deserializeFromString(String obj) throws IOException, ClassNotFoundException {
    byte[] b = stringToBytes(obj);
    ByteArrayInputStream bytes = new ByteArrayInputStream(b);
    ObjectInputStream stream = new ObjectInputStream(bytes);
    return stream.readObject();
  }

  private static String bytesToString(byte[] bytes) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      result.append(bytes[i]);
      result.append(" ");
    }
    return result.toString();
  }

  private static byte[] stringToBytes(String s) {
    StringTokenizer tokenizer = new StringTokenizer(s);
    byte[] result = new byte[tokenizer.countTokens()];

    for (int i = 0; i < result.length; i++) {
      result[i] = Byte.parseByte(tokenizer.nextToken());
    }

    return result;
  }
}
