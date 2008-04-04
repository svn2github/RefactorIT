/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.memory;

public class BaseMemoryArea implements MemoryArea {
  public static final String UNIT_KILOBYTES = "k";
  public static final String UNIT_MEGABYTES = "m";
  public static final String UNIT_BYTES = null;

  private final Integer maxMegabytes;

  public BaseMemoryArea(Integer max, String measurementUnit) {
    if (max == null) {
      throw new IllegalArgumentException("max must be not null");
    }

    this.maxMegabytes = new Integer(toMegabytes(max, measurementUnit));
  }

  private int toMegabytes(Integer quantity, String measurementUnit) {

    if (measurementUnit == UNIT_BYTES) {
      return quantity.intValue() / (1024 * 1024);
    } else if (measurementUnit.equalsIgnoreCase(UNIT_KILOBYTES)) {
      return quantity.intValue() / 1024;
    } else if (measurementUnit.equalsIgnoreCase(UNIT_MEGABYTES)) {
      return quantity.intValue();
    } else {
      throw new IllegalArgumentException("Unkown quantifier for -Xmx: "
          + measurementUnit);
    }
  }

  public String maxToString() {
    return maxMegabytes.toString() + UNIT_MEGABYTES;
  }

  public Float getMaxInMBs() {
    return new Float(maxMegabytes.floatValue());
  }
}
