/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.CompilationUnitReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.loader.JavadocComment;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.loader.RebuildLogic;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.source.ImportResolver;
import net.sf.refactorit.source.LocationlessSourceParsingException;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.StaticImports;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


// FIXME:
//public class Test1 {
//	class Test1 {
//	}
//}
//
//javac Test1.java
//Test1.java:2: Test1 is already defined in empty package
//        class Test1 {
//        ^

/**
 * Source file parameters.
 */
public class CompilationUnit implements BinTypeRefManager, PackageUsageManager,
    BinItemVisitable, SourceHolder, Referable {

  private Source source;

  private List typeUsageInfos = null;

  private List packageUsageInfos = null;

  private Project project;

//  private BinItemVisitable parent;
  // ------------------------------------------------------------------

  /**
   * formStarts and formEnds items are sorted from smaller to larger
   */
  private SourceCoordinate[] formStarts;
  private SourceCoordinate[] formEnds;

  private ImportResolver importResolver;

  private BinPackage _package;
  private final ArrayList definedTypes = new ArrayList(3);

  private List importedTypeNames = null;
  private List importedTypeNameNodes = null;

  private List importedPackages = null;
  private List importedPackageNodes = null;

  private List importedInners = null;

  private StaticImports staticImports;

  private List simpleComments = null;
  private List javadocComments = null;

  public CompilationUnit(final Source aSource, final Project project) {
    this.source = aSource;
    this.project = project;

    //if (Assert.enabled) {
    //  if (aSource == null) {
    //    Assert.must(false, "CompilationUnit constructed with null source");
    //  }
//      if (project == null) {
//        Assert.must(false, "CompilationUnit constructed with null project: " + aSource.getName());
//      }
    //  }

    // java.lang.* is imported by default
    // addImportedPackage(BinPackage.LANG_PACKAGE);

    if (ProjectLoader.checkIntegrityAfterLoad) {
      ProjectLoader.registerCreatedItem(this);
    }

    if (RebuildLogic.debug) {
      System.err.println("new CompilationUnit: " + aSource.getName());
    }
  }

  public final void setPackage(final BinPackage aPackage) {
    this._package = aPackage;
  }

  public final BinPackage getPackage() {
    return _package;
  }

  public final boolean isWithinGuardedBlocks(final int line, final int column) {
    boolean result = false;
    final SourceCoordinate searchable = new SourceCoordinate(line, column);

    for (int i = 0; i < formStarts.length; ++i) {

      if (formStarts[i].compareTo(searchable) <= 0) {
        if (formEnds[i].compareTo(searchable) >= 0) {
          result = true;
          break;
        }
      } else {
        // if the startPosition of guardedBlock in question was already past
        // the query block, then it can't be within any more as formStarts and
        // formEnds are sorted
        break;
      }

    }

    return result;
  }

  private void calculateGuardedBlocks() {
    final ArrayList starts = new ArrayList();
    final ArrayList ends = new ArrayList();

    final FastStack startPositions = new FastStack();
    boolean hadError = false;

    for (int i = 0; i < simpleComments.size(); ++i) {
      final Comment c = (Comment) simpleComments.get(i);
      final String text = c.getText();
      if (text.startsWith("//GEN-FIRST") || text.startsWith("//GEN-LAST")) {
        final SourceCoordinate start = new SourceCoordinate(c.getStartLine(), 0);
        final SourceCoordinate end = new SourceCoordinate(c.getStartLine(),
            c.getStartColumn() + text.length());
        starts.add(start);
        ends.add(end);
      } else if (text.indexOf("//GEN-BEGIN") >= 0) {
        final SourceCoordinate start = new SourceCoordinate(c.getStartLine(),
            c.getStartColumn());
        starts.add(start);
        ends.add(new Object());
        startPositions.push(new Integer(ends.size() - 1));
      } else if (text.indexOf("//GEN-END") >= 0) {
        final SourceCoordinate end = new SourceCoordinate(c.getStartLine(),
            c.getStartColumn() + text.length());
        if (!startPositions.isEmpty()) {
          final Integer pos = (Integer) startPositions.pop();
          ends.set(pos.intValue(), end);
        } else {
          hadError = true;
          break;
        }
      }
    }

    // stack must be empty
    hadError |= (!startPositions.isEmpty());

    if (hadError) {
      System.err.println("Source file " + getDisplayPath()
          + " had mismatching form comments. Not treating it as form.");
      return;
    }

    formStarts = (SourceCoordinate[]) starts.toArray(new SourceCoordinate[
        starts.size()]);
    formEnds = (SourceCoordinate[]) ends.toArray(new SourceCoordinate[ends.size()]);
  }

  /**
   * @param simpleComments list of {@link Comment}
   */
  public final void setSimpleComments(final List simpleComments) {
    this.simpleComments = simpleComments;
    if (this.simpleComments != null) {
      for (int i = 0, max = this.simpleComments.size(); i < max; i++) {
        ((Comment)this.simpleComments.get(i)).setCompilationUnit(this);
      }
    }
    calculateGuardedBlocks();
  }

  /**
   * @param javadocComments list of {@link JavadocComment}
   */
  public final void setJavadocComments(final List javadocComments) {
    this.javadocComments = javadocComments;
    if (this.javadocComments != null) {
      for (int i = 0, max = this.javadocComments.size(); i < max; i++) {
        ((Comment)this.javadocComments.get(i)).setCompilationUnit(this);
      }
    }
  }

  public final void cleanUp() {
    if (RebuildLogic.debug) {
      System.err.println("cleanUp: " + this);
      // just wanna be sure there are no crosslinks to avoid heavy leaks
    }
    try {
      this.definedTypes.clear();
      this.project = null;
      this._package = null;
      if (this.typeUsageInfos != null) {
        this.typeUsageInfos.clear();
      }
      if (this.packageUsageInfos != null) {
        this.packageUsageInfos.clear();
      }
      this.importResolver = null;
      if (this.importedPackages != null) {
        this.importedPackages.clear();
      }
      if (this.importedPackageNodes != null) {
        this.importedPackageNodes.clear();
      }
      if (this.importedTypeNames != null) {
        this.importedTypeNames.clear();
      }
      if (this.importedTypeNameNodes != null) {
        this.importedTypeNameNodes.clear();
      }
      if (this.importedInners != null) {
        this.importedInners.clear();
      }
      if (this.staticImports != null) {
      	this.staticImports.cleanUp();
      }
      if (this.simpleComments != null) {
        this.simpleComments = null; // don't clear this list!!!
      }
      if (this.javadocComments != null) {
        for (int i = 0, max = this.javadocComments.size(); i < max; i++) {
          ((JavadocComment)this.javadocComments.get(i)).invalidateCache();
        }
        this.javadocComments = null; // don't clear this list!!!
      }
      if (this.source != null) {
        this.source.invalidateCaches();
        // don't set source null, or it starts crashing in standalone
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
  }

  /**
   * @return list of simple {@link Comment}s
   */
  public final List getSimpleComments() {
    if (this.simpleComments == null) {
      this.simpleComments = new ArrayList();
    }
    return this.simpleComments;
  }

  /**
   * @return list of {@link JavadocComment}
   */
  public final List getJavadocComments() {
    if (this.javadocComments == null) {
      this.javadocComments = new ArrayList();
    }
    return javadocComments;
  }

  /**
   * @return list of {@link BinTypeRef}
   */
  public final List getDefinedTypes() {
    return definedTypes;
  }

  /**
   * Searches for a type, which maximally fits to the definition of "main type".
   * @return null only if the file has no types at all
   */
  public final BinTypeRef getMainType() {
    final List independentTypes = getIndependentDefinedTypes();
    String searchName = getName();
    final int dot = searchName.lastIndexOf('.');
    if (dot > 0) {
      searchName = searchName.substring(0, dot);
    }
    for (int i = 0; i < independentTypes.size(); i++) {
      final BinTypeRef typeRef = (BinTypeRef) independentTypes.get(i);
      if (searchName.equals(typeRef.getName())) {
        return typeRef;
      }
    }

    for (int i = 0; i < independentTypes.size(); i++) {
      final BinTypeRef typeRef = (BinTypeRef) independentTypes.get(i);
      if (typeRef.getBinCIType().isPublic()) {
        return typeRef;
      }
    }

    if (independentTypes.size() > 0) {
      return (BinTypeRef) independentTypes.get(0);
    }

    return null;
  }

  /**
   * @return list of {@link BinTypeRef} of non-inner types defined in this
   * source
   */
  public final List getIndependentDefinedTypes() {
    final ArrayList list = new ArrayList(definedTypes.size());

    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      final BinTypeRef typeRef = ((BinTypeRef) definedTypes.get(i));
      if (!typeRef.getBinCIType().isInnerType()) {
        list.add(typeRef);
      }
    }

    list.trimToSize();
    return list;
  }

  public final void addDefinedType(final BinTypeRef typeRef) {
    definedTypes.add(typeRef);
  }

  /**
   * Note - right now all imports ending with .* are incorrectly considered as packages
   * there is also a case 'import otherpackage.TypeWithInners.*'
   * these will be changed in next pass where call to compilationUnit.fixImports is made.
   *
   * Note 2: duplicate entries are accepted now, they will all be included in the list.
   */
  public final void addImportedPackage(final BinPackage aPackage,
      final ASTImpl aNode) {
    if (importedPackages == null) {
      importedPackages = new ArrayList(5);
    }
    importedPackages.add(aPackage);

    if (importedPackageNodes == null) {
      importedPackageNodes = new ArrayList(5);
    }
    importedPackageNodes.add(
        new Integer(ASTUtil.indexFor(aNode)));
  }

  public final void addImportedTypeName(final String qualifiedName,
      final ASTImpl aNode) {
    if (importedTypeNames == null) {
      importedTypeNames = new ArrayList(5);
    }
    importedTypeNames.add(qualifiedName);
    if (importedTypeNameNodes == null) {
      importedTypeNameNodes = new ArrayList(5);
    }
    importedTypeNameNodes.add(
        new Integer(ASTUtil.indexFor(aNode)));
  }

  public final boolean importsTypeDirectly(final String fqn) {
    final String anotherFqn = fqn.replace('$', '.'); // both with and without $ are allowed

    final List names = getImportedTypeNames();

    if (names != null) {
      for (int i = 0, max = names.size(); i < max; i++) {
        final String importedName = (String) names.get(i);
        if (fqn.equals(importedName) || anotherFqn.equals(importedName)) {
          return true;
        }
      }
    }

    return false;
  }

  public final boolean importsPackage(final BinPackage aPackage) {
    final List packages = getImportedPackages();
    if (packages != null) {
      for (int i = 0, max = packages.size(); i < max; i++) {
        if (aPackage.isIdentical((BinPackage) packages.get(i))) {
          return true;
        }
      }
    }

    return false;
  }

  public final boolean importsOnlyOnDemand(final BinCIType type) {
    return
        !importsTypeDirectly(type.getQualifiedName())
        && importsPackage(type.getPackage())
        && type.isPublic(); // FIXME why only public??? possible bug!!!!!
  }

  public final void addSingleStaticImport(String qualifiedName, final ASTImpl node) {
  	if (staticImports == null) {
  		staticImports = new StaticImports(this);
  	}
  	staticImports.addSingleStaticImport(ASTUtil.indexFor(node), qualifiedName);
  }

  public final void addOnDemandStaticImport(String typeName, final ASTImpl node) {
  	if (staticImports == null) {
  		staticImports = new StaticImports(this);
  	}
  	staticImports.addOnDemandStaticImport(ASTUtil.indexFor(node), typeName);
  }

  /**
   *
   * @param name
   * @return a statically imported field (either single or on-demand import),
   * <code>null</code> if no such static  import exists
   */
  public final BinField getStaticImportField(String name, BinCIType context) {
  	if (staticImports != null) {
  		return staticImports.getField(name, context);
  	}
  	return null;
  }

  public final BinField getSingleStaticImportField(String name, BinCIType context) {
  	if (staticImports != null) {
  		return staticImports.getSingleStaticImportField(name, context);
  	}
  	return null;
  }

  public final StaticImports getStaticImports() {
    if (staticImports == null) {
      staticImports = new StaticImports(this);
    }
  	return staticImports;
  }

  public final List getStaticImportMethods(BinCIType context) {
  	if (staticImports != null) {
  		return staticImports.getMethods(context);
  	}
  	return null;
  }



  public final boolean definesType(final BinCIType type) {
    final List types = getDefinedTypes();
    BinTypeRef typeRef = type.getTypeRef();
    for (int i = 0, max = types.size(); i < max; i++) {
      if (typeRef != null && typeRef.equals(types.get(i))) {
        return true;
      }
    }

    return false;
  }

  public final void visit(final BinItemVisitor visitor) {
    final List comments = getJavadocComments();
    if (comments != null) {
      for (int i = 0, max = comments.size(); i < max; i++) {
        final JavadocComment comment = (JavadocComment) comments.get(i);
        comment.visit(visitor);
      }
    }
  }

  public final BinTypeRef resolve(final String name, ASTImpl node) {
    BinTypeRef result = project.getTypeRefForSourceName(name);
    if (result == null) {
      try {
        result = importResolver.resolve(name);
      } catch (LocationlessSourceParsingException x) {
//System.err.println("eeeeeeeeeeeeeeeeeeeeee1");
//x.printStackTrace();
        (project.getProjectLoader().getErrorCollector()).addNonCriticalUserFriendlyError(new UserFriendlyError(x.getMessage(), x.getCompilationUnit(), node));
      } catch (SourceParsingException x) {
        (project.getProjectLoader().getErrorCollector()).addNonCriticalUserFriendlyError(x.getUserFriendlyError());
      }
    }

    return result;
  }

  /**
   * Will go through the list of imported packages and determines, if any of these
   * is actually imported inner types.<br>
   *
   * Then fixes them.<br>
   *
   * Also adds renameTypeData.<br>
   *
   * It was necessary because it can be determined only in pass 2.
   */
  public final void fixImports() {

    if (importedTypeNames != null && importedTypeNameNodes != null) {
      for (int i = 0; i < importedTypeNames.size(); ++i) {
        final String typeName = (String) importedTypeNames.get(i);
        final int nodeNumber
            = ((Integer) importedTypeNameNodes.get(i)).intValue();
        final ASTImpl node = getSource().getASTByIndex(nodeNumber);

        final BinTypeRef ref = resolve(typeName, node);
        if (ref != null) {
          addTypeUsageInfo(
              BinSpecificTypeRef.create(this, node, ref, true));
        }
      }
    }

    if (importedPackages != null && importedPackageNodes != null) {
      final List invalidImportNumbers = new ArrayList();

      for (int i = 0; i < importedPackages.size(); ++i) {
        final BinPackage aPackage = (BinPackage) importedPackages.get(i);
        final int nodeNumber = ((Integer) importedPackageNodes.get(i)).intValue();
        final ASTImpl node = getSource().getASTByIndex(nodeNumber);

        // node is null for implied imports like "java.lang.*"
        if (node == null) {
          if (Assert.enabled) {
            Assert.must(aPackage.getQualifiedName().equals("java.lang"),
                "implied package ?  " + aPackage.getQualifiedName());
          }
          continue;
        }

        final String name = aPackage.getQualifiedName();
        BinTypeRef testRef = null;

        // When we think it is a package, but there is nothing in this package...
        if (aPackage.getTypesNumber() == 0) {
          // XXX actually, not all binary classes were discovered at the
          // moment, so result of getTypesNumber is not fully determined!!!
          testRef = resolve(name, node);

          if (testRef == null) {
            // check for 'import OtherClassInSamePackage.*'
            final String newName = _package.getQualifiedForShortname(name);
            testRef = resolve(newName, node);
          }
        }

        if (testRef == null) {
          // it was a regular package
          addPackageUsageInfo(new PackageUsageInfo(node, aPackage, false, this));
          continue;
        }

        // now import statement was something like 'import OtherClass.*'
        // then testRef is ref to OtherClass

        invalidImportNumbers.add(new Integer(i));
        if (importedInners == null) {
          importedInners = new ArrayList(1);
        }
        importedInners.add(testRef);

        addTypeUsageInfo(
            BinSpecificTypeRef.create(this, node, testRef, true));
      }

      for (int i = invalidImportNumbers.size() - 1; i >= 0; --i) {
        final int index = ((Integer) invalidImportNumbers.get(i)).intValue();
        importedPackages.remove(index);
        importedPackageNodes.remove(index);
      }
    }
    if (staticImports != null) {
    	addTypeUsageInfos(staticImports.getTypeUsageInfos());
    }

    importResolver.setImportedInners(importedInners);
  }

  /**
   * @return list of FQN of types imported
   */
  public final List getImportedTypeNames() {
    return importedTypeNames;
  }

  public final List getImportedTypeNameNodes() {
    if (importedTypeNameNodes != null) {
      final List nodes = new ArrayList(importedTypeNameNodes.size());
      for (int i = 0; i < importedTypeNameNodes.size(); i++) {
        final int nodeNumber = ((Integer) importedTypeNameNodes.get(i)).
            intValue();
        final ASTImpl node = getSource().getASTByIndex(nodeNumber);
        nodes.add(node);
      }
      return nodes;
    }

    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  /** Will contain duplicates if there are duplicates in the source file. */
  public final List getImportedPackages() {
    return importedPackages;
  }

  public final List getImportedPackageNodes() {
    if (importedPackageNodes != null) {
      final List nodes = new ArrayList(importedPackageNodes.size());
      for (int i = 0; i < importedPackageNodes.size(); i++) {
        final int nodeNumber = ((Integer) importedPackageNodes.get(i)).intValue();
        final ASTImpl node = getSource().getASTByIndex(nodeNumber);
        nodes.add(node);
      }
      return nodes;
    }

    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public final void setImportResolver(final ImportResolver importResolver) {
    this.importResolver = importResolver;
  }

  public final ImportResolver getImportResolver() {
    return importResolver;
  }

  //
  // Accessor methods
  //
  public final Project getProject() {
    return project;
  }

  // FIXME: remove delegation?
  // this has to be invalidated after the source has changed
  public final LineIndexer getLineIndexer() {
    return this.source.getLineIndexer();
  }

  // FIXME: remove delegation?
  public final String getContent() {
    return this.source.getContentString();
  }

  public final String getName() {
    return source.getName();
  }

  /**
   * @return path valid mostly for messages and showing to user
   */
  public final String getDisplayPath() {
    if (this.source == null) {
      return "<unknown path>";
    } else {
      // FIXME some tests expect actually relative type instead of displayable absolute path
      return this.source.getRelativePath();
//      return this.source.getDisplayPath();
    }
  }

  public final Source getSource() {
    return source;
  }

  public final void setSource(final Source source) {
    if (Assert.enabled) {
      Assert.must(source != null, "Setting null source to: " + this);
    }
    this.source = source;
  }

  public final void accept(BinTypeRefVisitor visitor) {
    if (this.typeUsageInfos != null) {
      for (int i = 0, max = this.typeUsageInfos.size(); i < max; i++) {
        ((BinTypeRef) this.typeUsageInfos.get(i)).accept(visitor);
      }
    }
  }

  private final void addTypeUsageInfo(final BinTypeRef typeUsageInfo) {
    if (this.typeUsageInfos == null) {
      this.typeUsageInfos = new ArrayList(3);
    }
    this.typeUsageInfos.add(typeUsageInfo);
  }

  private final void addTypeUsageInfos(final List typeUsageInfos) {
    if (this.typeUsageInfos == null) {
      this.typeUsageInfos = new ArrayList(3);
    }
    this.typeUsageInfos.addAll(typeUsageInfos);
  }


  public final List getPackageUsageInfos() {
    return this.packageUsageInfos;
  }

  public final void addPackageUsageInfo(final PackageUsageInfo data) {
    // Init repository lazily
    if (this.packageUsageInfos == null) {
      this.packageUsageInfos = new ArrayList(5);
    }

    this.packageUsageInfos.add(data);
  }

  /**
   * Gets string representation of this source file.
   *
   * @return string representation.
   */
  public final String toString() {
    // NOTE: must not contain hashCode() (otherwise NotUsedTests
    // may be reported as failing differently each time for no good reason).

    if (source != null) {
      return getDisplayPath() /*+ ' ' + Integer.toHexString(hashCode())*/;
    } else {
      return "<unknown source>" /*+ ' ' + Integer.toHexString(hashCode())*/;
    }
  }

  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(BinItemVisitor visitor) {
    List types = getDefinedTypes();
    for (int i = 0, max = types.size(); i < max; i++) {
      BinTypeRef nextType = (BinTypeRef) types.get(i);

//      try {
        if (nextType != null) {
          BinCIType type = nextType.getBinCIType();
          if (type != null && !type.isInnerType()) {
            type.accept(visitor);
          }
        }
//      } catch (NullPointerException e) {
//        // Probably this is the one in RIM-219
//        project.addNonCriticalUserFriendlyError(
//            new UserFriendlyError("Failed to traverse type: " + nextType,
//            this, null));
//      }
    }
  }

  public final BinItemVisitable getParent() {
    return project;
  }

  // FIXME similar exists in Project, merge?
  public static CompilationUnit getCompilationUnit(Source source, Project p) {
    List compilationUnits = p.getCompilationUnits();
    for (int i = 0; i < compilationUnits.size(); i++) {
      CompilationUnit compilationUnit = (CompilationUnit) compilationUnits.get(i);
      if (compilationUnit.getSource().equals(source)) {
        return compilationUnit;
      }
    }

    return null;
  }

//  private static boolean under(Source child, Source parent) {
//    while(child != null) {
//      if(child.equals(parent)) {
//        return true;
//      }
//
//      child = child.getParent();
//    }
//
//    return false;
//  }

  public static List extractSourcesFromCompilationUnits(final Collection set) {
    List result = new ArrayList(set.size());
    for (Iterator i = set.iterator(); i.hasNext(); ) {
      result.add(((CompilationUnit) i.next()).getSource());
    }

    return result;
  }

  public BinItemReference createReference(){
    return new CompilationUnitReference(this);
  }
}
