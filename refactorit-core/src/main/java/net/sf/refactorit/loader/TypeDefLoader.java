/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.JspCompilationUnit;
import net.sf.refactorit.classmodel.PackageUsageInfo;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.jsp.JspCompiler;
import net.sf.refactorit.jsp.JspPageInfo;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTImplFactory;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.parser.CommentStoringFilter;
import net.sf.refactorit.parser.ErrorListener;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.parser.JavaRecognizer;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.OptimizedJavaRecognizer;
import net.sf.refactorit.parser.TreeASTImpl;
import net.sf.refactorit.source.ImportResolver;
import net.sf.refactorit.source.Resolver;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import rantlr.ANTLRException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * Purpose: Defines class for source code loading
 */
public final class TypeDefLoader implements JavaTokenTypes {
  private Collection sourcesToBeBuilt;

  private final ProjectLoader projectLoader;
  private final ParsingDataBuilder parsingDataBuilder;

  TypeDefLoader(final ProjectLoader projectLoader) {
    this.projectLoader = projectLoader;
    this.parsingDataBuilder = new ParsingDataBuilder(projectLoader);
  }

  public final List loadTypeDefsProfiling(final ProgressListener listener,
      final Collection cachedSourcesToBeBuilt) throws rantlr.ANTLRException,
      IOException, SourceParsingException {
    this.projectLoader.startProfilingTimer("loading all typedefs");
    final List result = loadTypeDefs(listener, cachedSourcesToBeBuilt);
    this.projectLoader.stopProfilingTimer();

    return result;
  }

  /**
   *
   * sourcepath may contain paths or file names separated by current system path separator
   *
   */
  public final List loadTypeDefs(final ProgressListener listener,
      final Collection cachedSourcesToBeBuilt) throws
      /*rantlr.ANTLRException, IOException, */SourceParsingException {

    sourcesToBeBuilt = cachedSourcesToBeBuilt;

    final int sourceCount = sourcesToBeBuilt.size();
    final ArrayList newCompilationUnits = new ArrayList(sourceCount);

    int cur = 0;
    // FIXME: isParsingCanceled && thread.isInterrupted() -- use both or ...?
    CancelSupport.checkThreadInterrupted();

    for (final Iterator i = sourcesToBeBuilt.iterator(); i.hasNext(); ++cur) {
      CancelSupport.checkThreadInterrupted();

      final Source aSource = (Source) i.next();
      try {
        this.projectLoader.startProfilingTimer("building source file for source");
        final CompilationUnit newCompilationUnit =
            buildCompilationUnitForSource(aSource);
        newCompilationUnits.add(newCompilationUnit);
        this.projectLoader.stopProfilingTimer();
        CancelSupport.checkThreadInterrupted();

        if (!LoadingASTUtil.optimized) {
          listener.showMessage(aSource.getAbsolutePath());
        }
      } catch (FileNotFoundException fe) {
        final UserFriendlyError friendlyError = new UserFriendlyError(
            fe.getMessage(),
            null
            );
        (this.projectLoader.getErrorCollector()).addNonCriticalUserFriendlyError(friendlyError);
      } catch (Exception e) {
        final UserFriendlyError friendlyError = new UserFriendlyError(
            e.getMessage(),
            new CompilationUnit(aSource, this.projectLoader.getProject()) // HACK: fake CompilationUnit instance (a real one might not exist yet)
            );

        (this.projectLoader.getErrorCollector()).addUserFriendlyError(friendlyError);
      }
      CancelSupport.checkThreadInterrupted();

      listener.progressHappened(
          ProjectLoader.PASS1_TIME * (cur + 1) / sourceCount);
    }

    // Create resolvers for source files
    CancelSupport.checkThreadInterrupted();

    try {
      this.projectLoader.startProfilingTimer("resolvers for source files");
      resolversForCompilationUnits(newCompilationUnits);
      this.projectLoader.stopProfilingTimer();
    } catch (SourceParsingException e) {
      AppRegistry.getExceptionLogger().error(e, "Don't know how to handle", this);
      if (!e.isUserFriendlyErrorReported()) {
        throw e;
      }
    }

    CancelSupport.checkThreadInterrupted();

    fixImports(newCompilationUnits);

    CancelSupport.checkThreadInterrupted();

    // Resolve superclasses & subclasses
    try {
      //System.out.println("Resolving superclasses & interfaces.");
      resolveSuperclassesAndInterfaces(listener, newCompilationUnits);
    } catch (SourceParsingException e) {
      AppRegistry.getExceptionLogger().error(e, "Don't know how to handle", this);
      if (!e.isUserFriendlyErrorReported()) {
        throw e;
      }
    } finally {
      if (!LoadingASTUtil.optimized) {
        listener.showMessage("");
      }
      sourcesToBeBuilt = null;
    }

    return newCompilationUnits;
  }

  private final void fixImports(final List compilationUnits) {
    Assert.must(this.projectLoader.getProject() != null);
    for (int i = 0; i < compilationUnits.size(); ++i) {
      final CompilationUnit aCompilationUnit = (CompilationUnit)
          compilationUnits.get(i);
      // sometimes JRE crashes below fixImports when ParsingDialog is closed
      CancelSupport.checkThreadInterrupted();

      aCompilationUnit.fixImports();
    }
  }

  private final void resolveSuperclassesAndInterfaces(
      final ProgressListener listener,
      final List compilationUnits) throws SourceParsingException {
    for (int i = 0; i < compilationUnits.size(); i++) {
//      try {
      final CompilationUnit aCompilationUnit
          = (CompilationUnit) compilationUnits.get(i);
      //listener.showMessage(aCompilationUnit.getRelativePath());
      resolveSuperclassesAndInterfaces(aCompilationUnit);

//      }
//      catch( SourceParsingException e ) {
//        if( ! e.isUserFriendlyErrorReported() )
//          throw e;
//        else
//          ; // Silent ignore
//      }
    }
  }

  private final void resolveSuperclassesAndInterfaces(
      final CompilationUnit compilationUnit) /*throws SourceParsingException*/
  {
    if (Settings.debugLevel > 50) {
      System.out.println("Resolving file: " + compilationUnit.getDisplayPath());
    }
    final List lsTypes = compilationUnit.getDefinedTypes();
    for (int i = 0, max = lsTypes.size(); i < max; i++) {
      CancelSupport.checkThreadInterrupted();

      try {
        (this.projectLoader.getErrorCollector()).startRecoverableUserErrorSection();
        final LoadingSourceBinCIType curSourceType
            = (LoadingSourceBinCIType) ((BinTypeRef) lsTypes.get(i)).getBinType();
        curSourceType.getTypeRef().getResolver().resolveSuperTypes();
      } catch (SourceParsingException spe) {
        final UserFriendlyError ufe = spe.getUserFriendlyError();
        if (ufe != null) {
          (this.projectLoader.getErrorCollector()).addNonCriticalUserFriendlyError(ufe);
        } else {
          (this.projectLoader.getErrorCollector()).addNonCriticalUserFriendlyError(new UserFriendlyError("Error while resolving supertypes",
                    compilationUnit));
        }
      } finally {
        (this.projectLoader.getErrorCollector()).endRecoverableUserErrorSection();
      }
    }
  }

  private final void resolversForCompilationUnits(final List compilationUnits) throws
      SourceParsingException {

    for (int i = 0; i < compilationUnits.size(); i++) {
      CancelSupport.checkThreadInterrupted();
      this.projectLoader.startProfilingTimer("resolvers for a source file");
      resolversForCompilationUnit((CompilationUnit) compilationUnits.get(i));
      this.projectLoader.stopProfilingTimer();
    }
  }

  private final void resolversForCompilationUnit(final CompilationUnit
      compilationUnit) throws
      SourceParsingException {
    //System.out.println("Building resolver for file: " + compilationUnit.getFile().getName());
    if (compilationUnit.getImportResolver() == null) {

      compilationUnit.setImportResolver(
          new ImportResolver(compilationUnit,
          compilationUnit.getImportedTypeNames(),
          compilationUnit.getImportedPackages(), compilationUnit.getPackage(),
          this.projectLoader.getProject())
          );

      // adding resolvers for every LoadingSourceBinCIType

      final List lsTypes = compilationUnit.getDefinedTypes();
      for (int i = 0, max = lsTypes.size(); i < max; i++) {
        final LoadingSourceBinCIType curSourceType
            = (LoadingSourceBinCIType) ((BinTypeRef) lsTypes.get(i)).getBinType();
        final BinTypeRef curSourceTypeRef
            = this.projectLoader.getProject().getTypeRefForName(
            curSourceType.getQualifiedName());
        curSourceTypeRef.setResolver(
            Resolver.getForSourceType(curSourceTypeRef, compilationUnit));
      }
    }
  }

  /**
   * Parses the source file and returns the CompilationUnit object containing the
   * AST tree for that source file.
   *
   * @param aSource vfs source file to be parsed
   * @return CompilationUnit, memory representation for source file. i.e. parsed
   * file containing AST tree (generated by parser) for that source.
   */
  private final CompilationUnit buildCompilationUnitForSource(final Source aSource)
      throws rantlr.ANTLRException, IOException, SourceParsingException {
    final CompilationUnit aCompilationUnit;
    if (FileUtil.isJspFile(aSource)) {
      aCompilationUnit = new JspCompilationUnit(aSource, this.projectLoader.getProject());
    } else {
      aCompilationUnit = new CompilationUnit(aSource, this.projectLoader.getProject());
    }

    aCompilationUnit.setPackage(this.projectLoader.getProject().getDefaultPackage());
    aCompilationUnit.addImportedPackage(this.projectLoader.getProject().getJavaLangPackage(), null);

    this.projectLoader.getProject().addCompilationUnit(aCompilationUnit);

    final ErrorListener errorListener
        = new UserFriendlyErrorReporter(this.projectLoader.getProject(), aCompilationUnit);
//System.err.println("getting: " + aSource);
    final FileParsingData data
        = parsingDataBuilder.getParsingData(aSource, errorListener);
//System.err.println("data: " + data);

    if (Assert.enabled &&
        aCompilationUnit.getSource().getFirstNode() != null
        && aCompilationUnit.getSource()
        != aCompilationUnit.getSource().getFirstNode().getSource()) {
      Assert.must(false, "tree for wrong source: "
          + aCompilationUnit.getSource() + " != "
          + aCompilationUnit.getSource().getFirstNode().getSource());
    }

    aCompilationUnit.setSimpleComments(data.simpleComments);
    aCompilationUnit.setJavadocComments(data.javadocComments);

    this.projectLoader.startProfilingTimer("invoking source file build");
    buildCompilationUnit(aCompilationUnit);
    this.projectLoader.stopProfilingTimer();

    final List definedTypes = aCompilationUnit.getDefinedTypes();
////System.err.println("defined types: " + definedTypes);
//
//    //FIXME: nothing is ever being done with this - why?
    final List duplicateTypeDefinitions = new ArrayList();

    final HashSet definedTypeNames = this.projectLoader.getProject().getDefinedTypeNames();

    this.projectLoader.startProfilingTimer(
        "looking for duplicate type definitions");
    for (final Iterator i = definedTypes.iterator(); i.hasNext(); ) {
      final String curName
          = ((BinTypeRef) i.next()).getBinType().getQualifiedName();
      if (definedTypeNames.contains(curName)) {
        duplicateTypeDefinitions.add(curName);
      } else {
        definedTypeNames.add(curName);
      }
    }
    this.projectLoader.stopProfilingTimer();

    return aCompilationUnit;
  }

  private final void buildCompilationUnit(final CompilationUnit compilationUnit) {
//    new rantlr.debug.misc.ASTFrame("root of " + compilationUnit, compilationUnit.getSource().getFirstNode()).setVisible(true);
    buildCompilationUnitForAST(
        compilationUnit.getSource().getFirstNode(), compilationUnit, new FastStack());
  }

  private final void buildCompilationUnitForAST(final ASTImpl topNode,
      final CompilationUnit compilationUnit,
      final FastStack classStack) {

    for (ASTImpl aNode = topNode; aNode != null;
        aNode = (ASTImpl) aNode.getNextSibling()) {

      final int type = aNode.getType();
//System.err.println("type: " + type);
      boolean typeFound = false;
      switch (type) {
        case IMPORT:
          final String importName = LoadingASTUtil.extractPackageStringFromExpression(
              aNode);

          // FIXME: here should be some import validating routines!
          if (importName.endsWith(".*") || importName.equals("*")) {
            final String packageName
                = LoadingASTUtil.extractUntilLastDot(importName);
            final BinPackage aPackage = this.projectLoader.getProject().createPackageForName(
                packageName);

            compilationUnit.addImportedPackage(aPackage,
                (ASTImpl) aNode.getFirstChild());
          } else {
            compilationUnit.addImportedTypeName(importName,
                (ASTImpl) aNode.getFirstChild());
          }
          break;

        case STATIC_IMPORT:
        	final String staticImportName = LoadingASTUtil.extractPackageStringFromExpression(aNode);
        	if (staticImportName.endsWith(".*")) {
        		final String typeName = LoadingASTUtil.extractUntilLastDot(staticImportName);
        		compilationUnit.addOnDemandStaticImport(typeName, (ASTImpl) aNode.getFirstChild());
        	} else {
        		compilationUnit.addSingleStaticImport(staticImportName, aNode);
        	}
        	break;


        case PACKAGE_DEF:
          // JAVA5: handle annotations!
          final String packageName
              = LoadingASTUtil.extractPackageStringFromExpression(aNode);
          final BinPackage _package = this.projectLoader.getProject().createPackageForName(packageName, true);
          _package.setFromSource(true);
          compilationUnit.setPackage(_package);

//MethodBodyLoader.ASTDebugOn(aNode);
          final ASTImpl clearPackageNode = LoadingASTUtil.getFirstChildOfDot(aNode);

//MethodBodyLoader.ASTDebugOn(clearPackageNode);
          if (clearPackageNode != null) {
            compilationUnit.addPackageUsageInfo(
                new PackageUsageInfo(clearPackageNode, _package, false,
                compilationUnit));
          }

          break;

        case CLASS_DEF: // intentional fallthrough
        case INTERFACE_DEF: // intentional fallthrough
        case ENUM_DEF: // intentional fallthrough
        case ANNOTATION_DEF:
          LoadingSourceBinCIType owner = null;
          if (classStack.size() > 0) {
            owner = (LoadingSourceBinCIType) classStack.peek();
          }
          final LoadingSourceBinCIType aType
              = LoadingSourceBinCIType.build(aNode, owner, compilationUnit);

          BinTypeRef typeRef = this.projectLoader.getProject()
              .findTypeRefForName(aType.getQualifiedName());
          if (typeRef != null && compilationUnit != typeRef.getCompilationUnit()
              && typeRef.getCompilationUnit() != null
              && compilationUnit != null) {
            this.projectLoader.getErrorCollector().
                addNonCriticalUserFriendlyError(
                new UserFriendlyError(
                "Duplicate types in: " + compilationUnit.getDisplayPath()
                + " and " + typeRef.getCompilationUnit().getDisplayPath(),
                compilationUnit, aNode));
            return;
          }

          // FIXME: a bit of overhead here searching for typeRef 2 times
          //System.err.println("xxdd : " + aType.getQualifiedName());
          typeRef = this.projectLoader.getProject().createCITypeRefForType(aType);

//Assert.must(LoadingASTUtil.getTypeNodeFromDef(aNode) != null, "Can't find type node");
//          typeRef = BinSpecificTypeRef.create(compilationUnit,
//              LoadingASTUtil.getTypeNodeFromDef(aNode), typeRef, false);
//          aType.setTypeRef(typeRef);

          compilationUnit.addDefinedType(typeRef);

          if (owner != null) {
            owner.addDeclaredType(typeRef);
          }

          compilationUnit.getPackage().addType(typeRef);

          classStack.push(aType);
          typeFound = true;
          break;

        default:

//          DebugInfo.trace("Unknown AST in the top of file: " + type);
//          new rantlr.debug.misc.ASTFrame("strange node", aNode).setVisible(true);
          break;
      } // end switch

      if (type == CLASS_DEF || type == INTERFACE_DEF || type == ENUM_DEF ||
          type == ANNOTATION_DEF || type == OBJBLOCK) {
        final ASTImpl child = (ASTImpl) aNode.getFirstChild();
        if (child != null) {
          buildCompilationUnitForAST(child, compilationUnit, classStack); // inners?
        }
      }
      if (typeFound) {
        classStack.pop();
      }
    }
  }

  private static final class ParsingDataBuilder {
    private ByteArrayOutputStream byteOutstream;

    private boolean mangle;
    private String sourceContents;

    private final ProjectLoader projectLoader;
    private JspCompiler jspc;

    public ParsingDataBuilder(final ProjectLoader projectLoader) {
      this.projectLoader = projectLoader;
    }

    private final void initJspc() {
      this.jspc = new JspCompiler(
          this.projectLoader.getProject().getPaths().getSourcePath());
    }

    public final FileParsingData getParsingData(final Source source,
        final net.sf.refactorit.parser.ErrorListener errorListener) throws
        rantlr.ANTLRException, IOException {

      FileParsingData data = projectLoader.getAstTreeCache().checkCacheFor(source);
      if (data != null) {
        data.astTree.setSource(source);
        source.setASTTree(data.astTree);
        return data;
      }

      data = new FileParsingData();

      // Adds error handling to parser
      OptimizedJavaRecognizer.setErrorListener(errorListener);

      projectLoader.startProfilingTimer("getting content for source");
      sourceContents = source.getContentString();
      // System.err.println("got new content for: " + source + " = \"" + new String(sourceContent) + "\"");
      projectLoader.stopProfilingTimer();

      preprocessJsp(source, data);

      final ASTTree tree = new ASTTree(source.length());
      ASTImplFactory.getInstance().setTree(tree);

      projectLoader.startProfilingTimer("constructing lexer for source content");
      final FastJavaLexer lexer = new FastJavaLexer(sourceContents);
      lexer.setFilename(source.getAbsolutePath());
      final CommentStoringFilter filter = new CommentStoringFilter(lexer);
      projectLoader.stopProfilingTimer();

      projectLoader.startProfilingTimer("constructing javarecognizer");
      final JavaRecognizer parser = new JavaRecognizer(filter);
      parser.setFilename(source.getAbsolutePath());
      parser.setASTFactory(ASTImplFactory.getInstance());
      projectLoader.stopProfilingTimer();

      projectLoader.startProfilingTimer("invocing parser's compilation unit");
      projectLoader.getErrorCollector().startRecoverableUserErrorSection();
      parser.compilationUnit();
      projectLoader.getErrorCollector().endRecoverableUserErrorSection();
      projectLoader.stopProfilingTimer();
//if (parser.getAST() != tree.rootNode) {
//  System.err.println("diff root nodes: " + parser.getAST() + " - " + tree.rootNode + " - " + source);
//}

      projectLoader.startProfilingTimer("getting AST from parser");
      ASTImplFactory.getInstance().setTree(null);
      tree.recompress((TreeASTImpl) parser.getAST());
      tree.setSource(source);
      projectLoader.stopProfilingTimer();

      postProcessJsp(data, tree);

      data.astTree = tree;
      data.simpleComments = filter.getSimpleComments();
      data.javadocComments = filter.getJavadocComments();

      if (!errorListener.hadErrors()) {
        if ( FileUtil.isJavaFile(source.getName())) {
          projectLoader.getAstTreeCache().putToCache(source, data);
        } else if ( FileUtil.isJspFile(source.getName())) {
          projectLoader.getAstTreeCache().putToJspCache(source, data);
        }
      }
      OptimizedJavaRecognizer.setErrorListener(null);

      source.setASTTree(data.astTree);

      return data;
    }

    private final void postProcessJsp(final FileParsingData data, final ASTTree tree) {
      if (mangle) {
        JspUtil.mangleCoordinates(tree.getAstAt(0), data.jpi);
      }
    }

    /**
     * @param source
     * @param data
     * @throws ANTLRException if jsp parsing error occurs, hack
     * FIXME: replace with JasperException
     */
    private final void preprocessJsp(final Source source, final FileParsingData data) throws ANTLRException {
      mangle = false;

      if ( FileUtil.isJspFile(source.getName())) {
        mangle = true;
        if (jspc == null) {
          initJspc();
        }
        //JspUtil.addJasperToHiddenClasspath(project); // moved to AbstractClassPath constructor
        projectLoader.startProfilingTimer("preprocess content ");
        if (byteOutstream == null) {
          byteOutstream = new ByteArrayOutputStream(50 * 1024);
        } else {
          // clear stream
          byteOutstream.reset();
        }
        try {
          final JspPageInfo jpi = jspc.compile(
              source, new OutputStreamWriter(byteOutstream));
          data.jpi = jpi;
        } catch ( rjasper.JasperException e) {
          throw new ANTLRException(e.getMessage());
        }
        projectLoader.stopProfilingTimer();

        sourceContents = byteOutstream.toString();
      }
    }
  }

//  private ASTImpl testCompression(final Source source, final ASTImpl t) {
//    ASTTree cmp = new ASTTree(t);
//    int size = cmp.getMemorySize();
//    memorySize+=size;
//    System.err.println( source.getName() + " added " + size + " bytes to toal of " + memorySize);
//    return cmp.getAstAt(0);
//  }


  public final FileParsingData getParsingData(final Source source, final ErrorListener errorListener) throws ANTLRException, IOException {
    return parsingDataBuilder.getParsingData(source,errorListener);
  }
}
