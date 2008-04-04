/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Tonis Vaga
 */
public final class MapPair implements java.util.Map.Entry {
  final Object key;
  Object value;

  public MapPair(Object key, Object value) {
    this.key = key;
    this.value = value;
  }

  public final Object getKey() {
    return key;
  }

  public final Object getValue() {
    return value;
  }

  public final Object setValue(Object parm1) {
    Object temp = value;
    this.value = parm1;

    return temp;
  }

  public final int hashCode() {
    return key.hashCode();
  }

  public final boolean equals(Object o) {
    // copied from JDK javadoc
    Map.Entry e1 = this, e2;
    if (!(o instanceof Map.Entry)) {
      return false;
    }
    e2 = (Entry) o;

    return (e1.getKey() == null ?
        e2.getKey() == null : e1.getKey().equals(e2.getKey())) &&
        (e1.getValue() == null ?
        e2.getValue() == null : e1.getValue().equals(e2.getValue()));

  }

}
