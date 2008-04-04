/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jsp;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import rjasper.JasperException;
import rjasper.JspCompilationContext;
import rjasper.Options;
import rjasper.compiler.Compiler;
import rjasper.compiler.JspLineMap;
import rjasper.compiler.JspLineMapItem;
import rjasper.compiler.JspReader;
import rjasper.compiler.ParseEventListener;
import rjasper.compiler.ParserController;
import rjasper.compiler.ServletWriter;
import rjasper.compiler.TextArea;
import rjasper.compiler.TldLocationsCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JspCompiler {

  private final SourcePath sourcePath;
  public JspCompiler(SourcePath sourcePath) {
    this.sourcePath = sourcePath;
  }

  public JspPageInfo compile(Source page,
      Writer servletSource) throws JasperException {
    if (page == null) {
      throw new NullPointerException("page == null");
    }

    if (servletSource == null) {
      throw new NullPointerException("servletSource == null");
    }

    final CompilationOptions options = new CompilationOptions();
    final JspPageInfo pageInfo = new JspPageInfo(page);
    pageInfo.setSerlvetSourceMap(new JspServletSourceMap());
    
    final CompilationContext ctx = new CompilationContext(pageInfo, options);
    
    //contextRoot = getContextRoot(page, ctx.getJspFile());
    
    ctx.setServletClassName(JspUtil.getClassName(page.getName()));
    
//  <HACK> to make possible to load multiply projects with 
//  the similar source names (prj1/index.jsp and prj2/index.jsp)
    ctx.setServletPackageName(JspUtil.getPackageName(pageInfo));
//  </HACK>
    
//    ctx.setServletClassName(("JSP" + Integer.toString(page.hashCode(), 16)).replace('-', '_'));

    final ServletWriter servletWriter = new ServletWriter(new PrintWriter(
        servletSource));
    ctx.setWriter(servletWriter);

    final ParserController parserCtl = new ParserController(ctx);
    try {
      parserCtl.parse(ctx.getJspFile());
    } catch (FileNotFoundException e1) {
      AppRegistry.getExceptionLogger().error(e1,this);
      throw new SystemException(ErrorCodes.IO_ERROR,e1);
    }

    final ParseEventListener listener = parserCtl.getParseEventListener();
    listener.beginPageProcessing();
    listener.endPageProcessing();
    try {
      servletWriter.close();
    } catch (IOException e) {
      AppRegistry.getExceptionLogger().error(e,this);
    }
    
    // Populate page information from Jasper's servlet -> JSP map
    final List jasperMap = servletWriter.getMap();
//    final Source contextRoot;
//    {
//      Source tmp = page.getParent();
//      while (tmp.getParent() != null) {
//        tmp = tmp.getParent();
//      }
//      contextRoot = tmp;
//    }
    final Source contextRoot = getContextRoot(pageInfo, ctx.getJspFile());
    
    final JspServletSourceMap map = pageInfo.getServletSourceMap();

    // Add exact mapping
    for (final Iterator i = jasperMap.iterator(); i.hasNext(); ) {
      final Object[] entry = (Object[]) i.next();
      final TextArea javaArea = (TextArea) entry[0];
      final TextArea jspArea = (TextArea) entry[1];

      final JspServletSourceMap.JspPageArea area =
          new JspServletSourceMap.JspPageArea();
      area.startLine = jspArea.startLine;
      area.startColumn = jspArea.startColumn;
      area.endLine = jspArea.endLine;
      area.endColumn = jspArea.endColumn;
      area.page = contextRoot.getChild(resolveRelativePath(jspArea.page));
      if (area.page == null) {
        throw new RuntimeException("Failed to find Source for"
            + " " + jspArea.page + " relative to " + contextRoot);
      }

//      if ( area.page.getIdentifier().equals(page.getIdentifier()) ) {
      //add mappings only to given source file
      map.addExactMapping(javaArea.startLine,
          javaArea.startColumn,
          javaArea.endLine,
          javaArea.endColumn,
          area);
//      }
    }

    // Add rough mapping
    final JspLineMap jasperLineMap = servletWriter.getLineMap();
    for (int i = 0, len = jasperLineMap.size(); i < len; i++) {
      final JspLineMapItem item = jasperLineMap.get(i);
      final JspServletSourceMap.JspPageArea area =
          new JspServletSourceMap.JspPageArea();
      area.startLine = item.getBeginJspLnr();
      area.startColumn = item.getBeginJspColNr();
      area.endLine = item.getEndJspLnr();
      area.endColumn = item.getEndJspLnr();
      final String pagePath = resolveRelativePath(jasperLineMap
          .getFileName(item.getStartJspFileNr()));
      area.page = contextRoot.getChild(pagePath);
      if (area.page == null) {
        throw new RuntimeException("Failed to find Source for"
            + " " + pagePath + " relative to " + contextRoot);
      }
//     if ( area.page.getIdentifier().equals(page.getIdentifier()) ) {
      //add mappings only to given source file
      map.addRoughMapping(item.getBeginServletLnr(),
          0,
          item.getEndServletLnr(),
          0,
          area);
//      }
    }

    //FIXME: for debug
//    map.printExactMap();
    return pageInfo;
  }

  public static Source getContextRoot(JspPageInfo pageInfo, String inFileName) {
    String pageRoot = pageInfo.getRoot();
    if(pageRoot.endsWith("/")) {
      pageRoot = pageRoot.substring(0, pageRoot.length() - 1);
    }
        
    final Source pageParent = pageInfo.getPage().getParent();
    if (pageParent == null) {
      return null;
    }   
       
    // If URI is relative, then it is relative to directory this page is
    // located in. If URI is absolute, it is relative to root file system
    // this page is located under.
    final Source resolveRoot; // directory relative to which URI is resolved
    if (inFileName.startsWith("/")) {
      // absolute URI
      Source tmp = pageParent;
      for(String parentAbsolutePath = tmp.getAbsolutePath().replace('\\', '/');
      		!parentAbsolutePath.equals(pageRoot);
      		parentAbsolutePath = tmp.getAbsolutePath().replace('\\', '/')) 
      {
        tmp = tmp.getParent();
        if(tmp == null) {
          break;
        }
      }
      resolveRoot = tmp;
    } else {
      // relative URI
      resolveRoot = pageParent;
    }
    return resolveRoot;
  }
  
  /**
   * Transform such path 'folder/subfolder/../xxx.jsp' to 'folder/xxx.jsp'
   */
  private static String resolveRelativePath(final String inFileName) {
    inFileName.replace('\\', '/');
    String[] parts = inFileName.split("/");
    
    ArrayList newParts = new ArrayList();
    for(int i = (parts.length - 1); i >= 0; i--) {
      if(parts[i].equals("..")) {
        i--;
        continue;
      }
      newParts.add(parts[i]);
    }
    Collections.reverse(newParts);

    StringBuffer newPathBuffer = new StringBuffer();
    for(Iterator it = newParts.iterator(); it.hasNext(); ) {
      newPathBuffer.append(it.next());
      newPathBuffer.append('/');
    }
    newPathBuffer.setLength(newPathBuffer.length()-1);
    return newPathBuffer.toString();
  }

  public static final void main(String[] params) throws Exception {
    final SourcePath sourcePath =
        new LocalSourcePath("test/projects/jsp/mapping");
    final Source webapp = sourcePath.getRootSources()[0];
    final Source page = webapp.getChild("helloworld.jsp");

    final JspCompiler compiler = new JspCompiler(sourcePath);
    final Writer servletOut = new FileWriter("servlet.java");
    final JspPageInfo pageInfo = compiler.compile(page, servletOut);
    System.out.println("page info = " + pageInfo);
  }

  private class CompilationContext implements JspCompilationContext {

    private final JspPageInfo pageInfo;
    private final Options options;

    private String contentType = "ISO-8859-1";
    private JspReader pageReader;
    private boolean errorPage = false;
    private String servletJavaFileName;
    private String servletClassName;
    private String servletPackageName;
    private ServletWriter writer;

    private CompilationContext(JspPageInfo pageInfo, Options options) {
      this.pageInfo = pageInfo;
      this.options = options;
    }

    /**
     * Create a "Compiler" object based on some init param data. This
     * is not done yet. Right now we're just hardcoding the actual
     * compilers that are created.
     */
    public Compiler createCompiler() throws JasperException {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * What class loader to use for loading classes while compiling
     * this JSP?
     */
    public ClassLoader getClassLoader() {
      return null;
    }

    /**
     * The classpath that is passed off to the Java compiler.
     */
    public String getClassPath() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * The content type of this JSP.
     *
     * Content type includes content type and encoding.
     */
    public String getContentType() {
      return contentType;
    }

    /**
     * The scratch directory to generate code into for javac.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getJavacOutputDir() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Path of the JSP URI. Note that this is not a file name. This is
     * the context rooted URI of the JSP file.
     */
    public String getJspFile() {
      final String uri = pageInfo.getPage().getRelativePath();
      if (!uri.startsWith("/")) {
        return "/" + uri;
      } else {
        return uri;
      }
    }

    /**
     * Get hold of the Options object for this context.
     */
    public Options getOptions() {
      return options;
    }

    /**
     * The scratch directory to generate code into.
     *
     * FIXME: In some places this is called scratchDir and in some
     * other places it is called outputDir.
     */
    public String getOutputDir() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Get the input reader for the JSP text.
     */
    public JspReader getReader() {
      return pageReader;
    }

    /**
     * Gets the actual path of a URI relative to the context of
     * the compilation.
     */
    public String getRealPath(String path) {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    public URL getResource(String res) throws MalformedURLException {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Gets a resource as a stream, relative to the meanings of this
     * context's implementation.
     * @return a null if the resource cannot be found or represented
     *        as an InputStream.
     */
    public InputStream getResourceAsStream(String res) {
      if (res == null) {
        throw new NullPointerException("res == null");
      }

// /*     //System.out.println("CompilationContext.getResourceAsStream(\"" + res + "\")");
//      final Source pageParent = pageInfo.getPage().getParent();
//      if (pageParent == null) {
//        return null;
//      }
//      
////	  final Source resolveRoot; // directory relative to which URI is resolved
////	  final String relativeUri; // URI relative with respect to resolveRoot
//      
//      String absolutePath = pageInfo.getPage().getAbsolutePath(); 
//      absolutePath = absolutePath.replace('\\', '/');
//      String resolvedRoot = absolutePath.substring(0, absolutePath.indexOf(res));
//      
//      if(!res.startsWith("/")) {
//        resolvedRoot = resolvedRoot.substring(0, resolvedRoot.length()-1);
//       // resolvedRoot += "/";
//       // res = res.substring(1);
//      } else {
//        
//      }
//      
//          
//         
//      // If URI is relative, then it is relative to directory this page is
//      // located in. If URI is absolute, it is relative to root file system
//      // this page is located under.
//      final Source resolveRoot; // directory relative to which URI is resolved
//      final String relativeUri; // URI relative with respect to resolveRoot
//      if (res.startsWith("/")) {
//        // absolute URI
//        Source tmp = pageParent;
//        //String parentAbsolutePath = tmp.getAbsolutePath().replace('\\', '/');
//        for(String parentAbsolutePath = tmp.getAbsolutePath().replace('\\', '/');
//        		!parentAbsolutePath.equals(resolvedRoot);
//        		parentAbsolutePath = tmp.getAbsolutePath().replace('\\', '/')) 
//        {
//          tmp = tmp.getParent();
//          if(tmp == null) {
//            break;
//          }
//        }
//        resolveRoot = tmp;
//        relativeUri = res.substring(1);
//      } else {
//        // relative URI
//        resolveRoot = pageParent;
//        relativeUri = res;
//      }
//      */
      res = resolveRelativePath(res);
      
      Source resolveRoot = JspCompiler.getContextRoot(pageInfo, res);
      
      String relativeUri;
      if (res.startsWith("/")) {
        relativeUri = res.substring(1);
      } else {
        // relative URI
        relativeUri = res;
      }
      
      //System.out.println("Resolving \"" + relativeUri + "\" relative to " + pageParent);
      final Source resource = resolveRoot.getChild(relativeUri);
      // System.out.println("resource = " + resource);
      if (resource == null) {
        return null;
      } else {
        // FIXME: should this registration be done before or after obtaining
        //        input stream?
        final Object resourceId = resource.getIdentifier();
        final Object pageId = pageInfo.getPage().getIdentifier();
        if (resourceId == null) {
          throw new NullPointerException("resource.getIdentifier() == null");
        }
        if (pageId == null) {
          throw new NullPointerException(
              "pageInfo.getPage().getIdentifier() == null");
        }
        if (!resourceId.equals(pageId)) {
          pageInfo.getIncludedPages().add(resource);
        }

        try {
          return resource.getInputStream();
        } catch (IOException e) {
          System.err.println("Failed to get JSP resource (\"" + res + "\"):");
          e.printStackTrace(System.err);
          return null;
        }
      }
    }

    /**
     * Just the class name (does not include package name) of the
     * generated class.
     */
    public String getServletClassName() {
      return servletClassName;
    }

    /**
     * Full path name of the Java file into which the servlet is being
     * generated.
     */
    public String getServletJavaFileName() {
      return servletJavaFileName;
    }

    /**
     * The package name into which the servlet class is generated.
     */
    public String getServletPackageName() {
      return servletPackageName;
    }

    /**
     * Get the 'location' of the TLD associated with
     * a given taglib 'uri'.
     *
     * @return An array of two Strings. The first one is
     * real path to the TLD. If the path to the TLD points
     * to a jar file, then the second string is the
     * name of the entry for the TLD in the jar file.
     * Returns null if the uri is not associated to
     * a tag library 'exposed' in the web application.
     * A tag library is 'exposed' either explicitely in
     * web.xml or implicitely via the uri tag in the TLD
     * of a taglib deployed in a jar file (WEB-INF/lib).
     */
    public String[] getTldLocation(String uri) throws JasperException {
      return null;
    }

    /**
     * Where is the servlet being generated?
     */
    public ServletWriter getWriter() {
      return writer;
    }

    /**
     * Are we processing something that has been declared as an
     * errorpage?
     */
    public boolean isErrorPage() {
      return errorPage;
    }

    /**
     * Are we keeping generated code around?
     */
    public boolean keepGenerated() {
      return true;
    }

    /**
     * Get the full value of a URI relative to this compilations context
     */
    public String resolveRelativeUri(String uri) {
      // FIXME: should work
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    public void setErrorPage(boolean isErrPage) {
      this.errorPage = isErrPage;
    }

    public void setReader(JspReader reader) {
      this.pageReader = reader;
    }

    public void setServletClassName(String servletClassName) {
      this.servletClassName = servletClassName;
    }

    public void setServletJavaFileName(String servletJavaFileName) {
      this.servletJavaFileName = servletJavaFileName;
    }

    public void setServletPackageName(String servletPackageName) {
      this.servletPackageName = servletPackageName;
    }

    public void setWriter(ServletWriter writer) {
      this.writer = writer;
    }
  }


  /**
   * Provides necessary options to Jasper engine.
   */
  private static class CompilationOptions implements Options {

    private CompilationOptions() {}

    /**
     * Java platform encoding to generate the JSP
     * page servlet.
     */
    public String getJavaEncoding() {
      return "ISO-8859-1";
    }

    /**
     * Should errors be sent to client or thrown into stderr?
     */
    public boolean getSendErrorToClient() {
      return false;
    }

    /**
     * Should we include debug information in compiled class?
     */
    public boolean getClassDebugInfo() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * What classpath should I use while compiling the servlets
     * generated from JSP files?
     */
    public String getClassPath() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Class ID for use in the plugin tag when the browser is IE.
     */
    public String getIeClassId() {
      return "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
    }

    /**
     * Path of the compiler to use for compiling JSP pages.
     */
    public String getJspCompilerPath() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * What compiler plugin should I use to compile the servlets
     * generated from JSP files?
     */
    public Class getJspCompilerPlugin() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Are we keeping generated code around?
     */
    public boolean getKeepGenerated() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }

    /**
     * Are we supporting large files?
     */
    public boolean getLargeFile() {
      return false;
    }

    /**
     * Are we supporting HTML mapped servlets?
     */
    public boolean getMappedFile() {
      return false;
    }

    /**
     * What is my scratch dir?
     */
    public File getScratchDir() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
      // return new File("jsp_output");
    }

    /**
     * The cache for the location of the TLD's
     * for the various tag libraries 'exposed'
     * by the web application.
     * A tag library is 'exposed' either explicitely in
     * web.xml or implicitely via the uri tag in the TLD
     * of a taglib deployed in a jar file (WEB-INF/lib).
     *
     * @return the instance of the TldLocationsCache
     * for the web-application.
     */
    public TldLocationsCache getTldLocationsCache() {
      throw new UnsupportedOperationException(
          "This method shouldn't be called");
    }
  }
}
