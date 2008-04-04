/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.vfs.Source;



/**
 * Incapsulates the data, what is shared among editors.
 * @author Jevgeni Holodkov
 * @author Anton Safonov
 */
public final class SimpleSourceHolder implements SourceHolder {
  private Source source;
  private final Project project;
  private BinPackage _package;
  private String name = "<unknown>";
  private String displayPath;

  public SimpleSourceHolder(final Project project) {
    this.project = project;
    if (Assert.enabled) {
      Assert.must(this.project != null, "Project is null");
    }
  }

  public SimpleSourceHolder(final Source source, final Project project) {
    this(project);
    this.source = source;
    if (Assert.enabled) {
      Assert.must(this.source != null, "Source is null");
    }
    setDisplayPath(this.source.getDisplayPath());
  }

  public String getName() {
    if (this.source == null) {
      return this.name;
    }
    return this.source.getName();
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setDisplayPath(final String displayPath) {
    if (this.displayPath == null) {
      this.displayPath = displayPath;
    }
  }

  public String getDisplayPath() {
    if (Assert.enabled && this.displayPath == null) {
      Assert.must(false, "NO DISPLAY PATH: " + this);
    }
    return this.displayPath;
  }

  public Source getSource() {
    return this.source;
  }

  public void setSource(Source source) {
    if (Assert.enabled) {
      Assert.must(source != null, "Setting null source");
    }
    this.source = source;
    if (this.source != null) {
      setDisplayPath(this.source.getDisplayPath());
    }
  }

  public Project getProject() {
    return this.project;
  }

  public BinPackage getPackage() {
    return this._package;
  }

  public void setPackage(final BinPackage _package) {
    this._package = _package;
  }

  public String toString() {
    return ClassUtil.getShortClassName(this) + ": " + this.source;
  }

  public int hashCode() {
    return getDisplayPath().hashCode();
  }

  public boolean equals(Object other) {
    if (other == null || !(other instanceof SourceHolder)) {
      return false;
    }

    return getDisplayPath().equals(((SourceHolder) other).getDisplayPath());
  }
}
