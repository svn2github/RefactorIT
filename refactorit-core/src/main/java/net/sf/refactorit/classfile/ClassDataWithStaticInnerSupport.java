/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classfile;


import net.sf.refactorit.classfile.ClassData.MyInnerType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * NOTE: need one instance per project (or at least one per outer type);
 * otherwise the contentes of "staticInners" will be wrong.
 */
public final class ClassDataWithStaticInnerSupport {
  private final HashSet staticInners = new HashSet(3);

  public final ClassData get(byte[] data) throws IOException, ClassFormatException {
    ClassData result = new ClassData(data);

    if (staticInners.contains(result.getName())) {
      result.markStatic();
    }

    collectStaticInners(result);
    return result;
  }

  private void collectStaticInners(ClassData c) {
    ArrayList inners = c.getDeclaredTypes();
    if (inners != null) {
      for (int i = 0, max = inners.size(); i < max; i++) {
        MyInnerType inner = (MyInnerType) inners.get(i);
        if (inner.isStatic) {
          staticInners.add(inner.name);
        } else {
          staticInners.remove(inner.name);
        }
      }
    }
  }
}
