/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.netbeans.common.projectoptions.NBFileUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.vfs.Source;


public class PathItemReferenceWrapper {
  private final PathItemReference reference;
  
  private final String cachedName;
  private final boolean cachedIsFolder;
  
  private List children = null;

  public PathItemReferenceWrapper(PathItemReference reference) {
    this.reference = reference;
    this.cachedName = reference.getDisplayNameWithoutFolders();
    this.cachedIsFolder = reference.getSource().isDirectory();
  }
  
  public String getDisplayName() {
    return cachedName;
  }
  
  public String toString() {
    return getDisplayName();
  }
  
  public PathItemReference getReference() {
    return reference;
  }
  
  public List getChildren() {
    if(children == null) {
      children = new ArrayList();
      
      Source[] childSources = reference.getSource().getChildren();
      for (int i = 0; i < childSources.length; i++) {
        if (NBFileUtil.sourceAcceptedIfNotIgnored(childSources[i])) {
          PathItemReference childReference = PathItemReference.createForSource(
              childSources[i]);
          if (childReference != null && childReference.isFolder()) {
            children.add(new PathItemReferenceWrapper(childReference));
          }
        }
      }
    
      Collections.sort(children, new StringUtil.ToStringComparator());
    }
  
    return children;
  }

  public boolean isFolder() {
    return cachedIsFolder;
  }
}
