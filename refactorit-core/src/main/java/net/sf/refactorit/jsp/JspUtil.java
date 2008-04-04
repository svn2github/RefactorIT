/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jsp;


import net.sf.refactorit.classmodel.AbstractLocationAware;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.module.ModuleManager;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;


public class JspUtil {
  private static final String JSP_COMPILER = "rjasper/compiler/Compiler.class";
  private static final String JSP_PACKAGE = "rjasper.jar";
  private static final String SERVLET_PACKAGE = "servlet-api.jar";

//  private static final String JSP_BASE = "rjasper/runtime/HttpJspBase.class";
//  private static final String SERVLET_BASE = "javax/servlet/http/HttpServlet.class";

  public static String getPackageName(JspPageInfo pageInfo) {
    String sourcePath = pageInfo.getPage().getAbsolutePath().replace('\\', '/').replace('/', '.');
    String sourceName = pageInfo.getPage().getName();
    
    if(sourcePath.indexOf('.') == 0) {
      sourcePath = sourcePath.substring(1, sourcePath.length());
    }
    
    int index = sourcePath.indexOf(sourceName);
    if(index <= 0) { // not found 
      return null;
    }
    String packageName = sourcePath.substring(0, index);
    packageName = getClassName(packageName);
    return packageName;
  }
  
  /**
   *
   * @param path
   * @return 2-elements array of string, first rjasper.jar path, second servlet-api.jar path
   * @throws RuntimeException if rjasper.jar not found
   */
  
  public static final String[] getJasperClasspath() {
    String ritPath = RuntimePlatform.getLibPath();
    if (ritPath.endsWith("\\") || ritPath.endsWith("/")) {
      ritPath = ritPath.substring(0, ritPath.length() - 1);
    }

//    ritPath = ritPath + File.separatorChar + "lib" + File.separatorChar;

    ritPath += File.separator;

    String jasperJarPath = ritPath + JSP_PACKAGE;
    String servletPath = ritPath + SERVLET_PACKAGE;

    if (!new File(jasperJarPath).exists()) {
      AppRegistry.getLogger(JspUtil.class).debug(jasperJarPath + " not found, using getClassLoader()");

      String jsp = findJSPCompilerJar();
      if (jsp == null) {
        // fixme: i18n
        String msg = JSP_PACKAGE + " not found";
        throw new SystemException(ErrorCodes.INTERNAL_ERROR,msg);
      }

      jsp = StringUtil.replace(jsp, '/', File.separatorChar);
      if (jsp.startsWith("\\")) {
        jsp = jsp.substring(1);
      }

      String servlet = StringUtil.replace(jsp, JSP_PACKAGE, SERVLET_PACKAGE);

      jasperJarPath = jsp;
      servletPath = servlet;
    }

    String result[] = new String[2];
    result[0] = jasperJarPath;
    result[1] = servletPath;

    return result;
  }

  private static String findJSPCompilerJar() {
    Class cls = ModuleManager.class;
    URL url = cls.getClassLoader().getResource(JSP_COMPILER);

    String urlString = URLDecoder.decode(url.toString());
    if (!urlString.startsWith("jar:file:")) {
      return null;
    }

    int pos = urlString.indexOf("!");
    if (pos == -1) {
      return null;
    }

    return urlString.substring(9, pos);
  }

  public static final void mangleCoordinates(ASTImpl t, JspPageInfo jpi) {
    walkTree(t, jpi);
  }

  private static final void walkTree(ASTImpl node, JspPageInfo jpi) {
    if (node == null) { // no nodes
      return;
    }

    mangleNode(node, jpi);

    ASTImpl child = (ASTImpl) node.getFirstChild();
    walkTree(child, jpi);
    walkTree((ASTImpl) node.getNextSibling(), jpi);
  }

  private static final void mangleNode(ASTImpl node, JspPageInfo jpi) {
    if (node == null) { // no nodes
      return;
    }

    final JspServletSourceMap.JspPageArea area = jpi.getServletSourceMap()
        .mapArea(node.getStartLine() - 1, node.getStartColumn() - 1,
        node.getEndLine() - 1, node.getEndColumn() - 1);

    if (area == null || area.page != jpi.getPage()) {
      node.setStartLine(0);
      node.setStartColumn(0);
      node.setEndLine(0);
      node.setEndColumn(0);

      if (area != null) {
        node.setSource(area.page);
      } else {
        node.setSource(jpi.getPage());
      }
    } else {
//FIXME debug
//System.err.println("Mangling: java " +
//    node.getStartLine() + '.' + node.getStartColumn() + '-' +
//    node.getEndLine() + '.' + node.getEndColumn() + " -> jsp " +
//    (area.startLine+1) + '.' + (area.startColumn+1) + '-' +
//    (area.endLine+1) + '.' + (area.endColumn+1) );
      node.setStartLine(area.startLine + 1);
      node.setStartColumn(area.startColumn + 1);
      node.setEndLine(area.endLine + 1);
      node.setEndColumn(area.endColumn + 1);
      node.setSource(area.page);
    }
  }

  public static final void printTree(ASTImpl node, int index) {
    if (node == null) { // no nodes
      return;
    }

    printNode(node, index);

    ASTImpl child = (ASTImpl) node.getFirstChild();
    printTree(child, index + 1);
    printTree((ASTImpl) node.getNextSibling(), index);
  }

  private static final void printNode(ASTImpl node, int index) {
    if (node == null) { // no nodes
      return;
    }

    for (int i = 0; i < index; i++) {
      System.err.print("  ");
    }

    System.err.println(node.getText() + "-" + node.toString());
  }

  public static final String getClassName(String filename) {
    int iSep = filename.lastIndexOf('/') + 1;
    int iEnd = filename.length();

    StringBuffer modifiedClassName = new StringBuffer(filename.length() - iSep);

    if (!Character.isJavaIdentifierStart(filename.charAt(iSep))
        || filename.charAt(iSep) == '_') {
      // If the first char is not a start of Java identifier or is _
      // prepend a '_'.
      modifiedClassName.append('_');
    }

    for (int i = iSep; i < iEnd; i++) {
      char ch = filename.charAt(i);
      if (Character.isJavaIdentifierPart(ch)) {
        modifiedClassName.append(ch);
      } else if (ch == '.') {
        modifiedClassName.append('_');
      } else {
        modifiedClassName.append(mangleChar(ch));
      }
    }

    return modifiedClassName.toString();
  }

  private static final String mangleChar(char ch) {
    String s = Integer.toHexString(ch);
    int nzeros = 5 - s.length();

    char[] result = new char[6];
    result[0] = '_';

    for (int i = 1; i <= nzeros; i++) {
      result[i] = '0';
    }

    for (int i = nzeros + 1, j = 0; i < 6; i++, j++) {
      result[i] = s.charAt(j);
    }

    return new String(result);
  }

  /**
   * @param bins
   */
  public static boolean containsJSPNodes(final Object[] bins) {
    for (int i = 0; i < bins.length; ++i) {
      if (bins[i] instanceof AbstractLocationAware) {
        if (((AbstractLocationAware) bins[i]).isPreprocessedSource()) {
          return true;
        }
      }
    }

    return false;
  }


}
