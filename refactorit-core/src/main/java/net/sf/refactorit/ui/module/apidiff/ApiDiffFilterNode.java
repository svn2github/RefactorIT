/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.module.apidiff;

import net.sf.refactorit.query.usage.filters.ApiDiffFilter;
import net.sf.refactorit.ui.tree.UITreeNode;


public class ApiDiffFilterNode {
  private Object type;

  public ApiDiffFilterNode(int type) {
    this.type = new Integer(type);
  }

  public void setPublic(Boolean value, ApiDiffFilter filter) {
    filter.setAccess("public", value, type);
  }

  public void setProtected(Boolean value, ApiDiffFilter filter) {
    filter.setAccess("protected", value, type);
  }

  public void setPrivate(Boolean value, ApiDiffFilter filter) {
    filter.setAccess("private", value, type);
  }

  public void setPackagePrivate(Boolean value, ApiDiffFilter filter) {
    filter.setAccess("package private", value, type);
  }

  public Object getPublic(ApiDiffFilter filter) {
    return new Boolean(filter.getAccess(type, "public"));
  }

  public Object getProtected(ApiDiffFilter filter) {
    return new Boolean(filter.getAccess(type, "protected"));
  }

  public Object getPrivate(ApiDiffFilter filter) {
    return new Boolean(filter.getAccess(type, "private"));
  }

  public Object getPackagePrivate(ApiDiffFilter filter) {
    return new Boolean(filter.getAccess(type, "package private"));
  }

  public String getName() {
    int typeInt = ((Integer) type).intValue();
    switch (typeInt) {
      case UITreeNode.NODE_TYPE_FIELD:
        return "Field";
      case UITreeNode.NODE_TYPE_METHOD:
        return "Method";
      case UITreeNode.NODE_ENUM:
        return "Enum";
      case UITreeNode.NODE_INTERFACE:
        return "Interface";
      case UITreeNode.NODE_CLASS:
        return "Class";
      default:
        return "";
    }
  }
}
