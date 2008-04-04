/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jsp;


import net.sf.refactorit.vfs.Source;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Information about compiled JSP page.
 */
public class JspPageInfo implements java.io.Serializable {

  /** Page this information is about. */
  private final Source page;

  /** All pages included from this page and included pages. */
  private final Set includedPages = new HashSet();

  /**
   * Mapping between source of servlet generated from this page and JSP
   * pages. <code>null</code> if not available.
   */
  private JspServletSourceMap servletSourceMap;

  /** Constructs new <code>JspPageInfo</code>. */
  public JspPageInfo(Source page) {
    this.page = page;
  }

  /**
   * Gets pages this page includes.
   *
   * @return pages ({@link Source}) instances. Never returns <code>null</code>.
   */
  public Set getIncludedPages() {
    return includedPages;
  }
  
  public String getRoot() {
    String absolutePath = this.page.getAbsolutePath().replace('\\', '/');
    String relativePath = this.page.getRelativePath().replace('\\', '/');
    String rootPath = absolutePath.substring(0, absolutePath.indexOf(relativePath));
    return rootPath;
  }

  /**
   * Gets page this information is about.
   *
   * @return page. Never returns <code>null</code>.
   */
  public Source getPage() {
    return page;
  }

  /**
   * Gets mapping between source of servlet generated from this page and
   * JSP pages.
   *
   * @return map or <code>null</code> if map is not available.
   */
  public JspServletSourceMap getServletSourceMap() {
    return servletSourceMap;
  }

  /**
   * Sets mapping between source of servlet generated from this page and
   * JSP pages.
   *
   * @param map or <code>null</code> if map is not available.
   */
  public void setSerlvetSourceMap(JspServletSourceMap servletSourceMap) {
    this.servletSourceMap = servletSourceMap;
  }

  /**
   * Gets string representation of this information.
   *
   * @return string representation.
   */
  public String toString() {
    final StringBuffer result = new StringBuffer();
    result.append("JspPageInfo[");
    result.append("page: ").append(page.getAbsolutePath());
    if (getIncludedPages().size() > 0) {
      result.append(", includes: ");
      boolean needDelimiter = false;
      for (final Iterator i = getIncludedPages().iterator(); i.hasNext(); ) {
        final Source includedPage = (Source) i.next();
        if (needDelimiter) {
          result.append(", ");
        } else {
          needDelimiter = true;
        }
        result.append(includedPage.getAbsolutePath());
      }
    }
    result.append(']');
    return result.toString();
  }
}
