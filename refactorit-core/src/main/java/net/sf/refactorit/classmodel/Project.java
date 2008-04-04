/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classfile.ClassUtil;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.ProjectReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.loader.FastCompilationUnitForNameFinder;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.parser.JavaRecognizer;
import net.sf.refactorit.parser.TreeASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.undo.MilestoneManager;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.ui.projectoptions.DefaultReadOnlyProjectOptions;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.ui.projectoptions.ProjectSettingsListener;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.CompoundClassPath;
import net.sf.refactorit.vfs.CompoundSourcePath;
import net.sf.refactorit.vfs.JavadocPath;
import net.sf.refactorit.vfs.Paths;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;
import net.sf.refactorit.vfs.SourcePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/*
 * This class holds miscellaneous data for given project
 *
 * @author Erik Bartske
 * @author Sander Magi
 * @author Villu Ruusmann
 * @author Anton Safonov
 * @author Jaanek Oja
 * @author Risto Alas
 * @author Tonis Vaga
 */
public final class Project implements BinItemVisitable, Referable {
  private final String name;

  private final Paths paths;

  private Workspace workspace;

  private final ProjectLoader projectLoader = new ProjectLoader(this);

  /** Time of the last rebuild */
  private long lastRebuilded = 0;

  private final List projectSettingsListeners
      = Collections.synchronizedList(new ArrayList());


  private final FastCompilationUnitForNameFinder compilationUnitForNameFinder
      = new FastCompilationUnitForNameFinder(this);

  private final HashMap createdPackages = new HashMap();
  public int classLoaderEntries = 0;
  private final ArrayList compilationUnits = new ArrayList();
  private final Map nonJavaUnits = new HashMap();
  private final HashSet definedTypeNames = new HashSet(1024);
  public final HashMap loadedTypes = new HashMap(3072);

  private BinPackage defaultPackage = null;
  private BinPackage javaLangPackage = null; // default import
  public static final String OBJECT = "java.lang.Object";
  public BinTypeRef objectRef; // it will hold reference for toplevel object
  private BinTypeRef[] arrayInterfaces = null;

  private static final HashMap primitiveTypes = new HashMap(9);

  static {
    primitiveTypes.put(BinPrimitiveType.VOID.getQualifiedName(), BinPrimitiveType.VOID_REF);
    primitiveTypes.put(BinPrimitiveType.BOOLEAN.getQualifiedName(), BinPrimitiveType.BOOLEAN_REF);
    primitiveTypes.put(BinPrimitiveType.BYTE.getQualifiedName(), BinPrimitiveType.BYTE_REF);
    primitiveTypes.put(BinPrimitiveType.CHAR.getQualifiedName(), BinPrimitiveType.CHAR_REF);
    primitiveTypes.put(BinPrimitiveType.SHORT.getQualifiedName(), BinPrimitiveType.SHORT_REF);
    primitiveTypes.put(BinPrimitiveType.INT.getQualifiedName(), BinPrimitiveType.INT_REF);
    primitiveTypes.put(BinPrimitiveType.LONG.getQualifiedName(), BinPrimitiveType.LONG_REF);
    primitiveTypes.put(BinPrimitiveType.FLOAT.getQualifiedName(), BinPrimitiveType.FLOAT_REF);
    primitiveTypes.put(BinPrimitiveType.DOUBLE.getQualifiedName(), BinPrimitiveType.DOUBLE_REF);

    // init token names for AST.toString() to work
    TreeASTImpl.setVerboseStringConversion(false, JavaRecognizer._tokenNames);
  }

  private boolean allTypesDiscovered = false;

  private Object cachePath;

  private ProjectOptions options = DefaultReadOnlyProjectOptions.instance;

  private RitUndoManager ritUndoManager;

  /**
   * classes shoudln't use it directly, only IDEController impl should create project instance
   */
  public Project(final String name,
      final SourcePath sourcePath, final ClassPath classPath,
      final JavadocPath javadocPath) {
    this(name, new Paths(sourcePath, classPath, javadocPath));
  }

  /**
   * classes shoudln't use it directly, only IDEController impl should create project instance
   */
  public Project(final String name, final Paths paths) {
    this.name = name;
    this.paths = paths;
    if (Assert.enabled && this.paths == null) {
      Assert.must(false, "Paths is null");
    }

    this.javaLangPackage = createPackageForName("java.lang");

    this.defaultPackage = createPackageForName("");

    RitUndoManager.clear();
    MilestoneManager.clear();

    getProjectLoader().addProjectChangedListener(new ProjectChangedListener() {
      public void rebuildStarted(final Project p) {
        lastRebuilded = System.currentTimeMillis();
      }

      public void rebuildPerformed(final Project p) {
        lastRebuilded = System.currentTimeMillis();
      }
    });
  }

  /**
   * @return Returns the project name. Should be unique in workspace!!!
   */
  public String getName() {
    return this.name;
  }

  public Paths getPaths() {
    return this.paths;
  }

  public final ProjectLoader getProjectLoader() {
    return this.projectLoader;
  }

  public String toString() {
    final String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": source path \""
        + getPaths().getSourcePath() + "\", classpath: \"" + getPaths().getClassPath() + "\"";
  }

  public long getLastRebuilded() {
    return this.lastRebuilded;
  }

  /**
   * 
   * @param bin
   * @return Project for the specified ObjectBin or null 
   * if project cannot be detected 
   */
  public static Project getProjectFor(final Object bin) {
    Object target = bin;
    while (target instanceof Object[]) {
      if(((Object[])target).length > 0) {
        target = ((Object[]) target)[0];
      } else {
        return null;
      }
    }
    final Project project;
    if (target instanceof LocationAware
        && ((LocationAware) target).getCompilationUnit() != null) {
      project = ((LocationAware) target).getCompilationUnit().getProject();
    } else if (target instanceof BinMember) {
      project = ((BinMember) target).getProject();
    } else if (target instanceof BinSpecificTypeRef
        && ((BinSpecificTypeRef) target).getCompilationUnit() != null) {
      project = ((BinSpecificTypeRef) target).getCompilationUnit().getProject();
    } else if (target instanceof BinTypeRef) {
      project = ((BinTypeRef) target).getProject();
    } else if (target instanceof BinPackage) {
      project = ((BinPackage) target).getProject();
    } else if (target instanceof Project) {
      project = (Project) target;
    } else if (target instanceof CompilationUnit) {
      project = ((CompilationUnit) target).getProject();
    } else if (target instanceof SourceConstruct
        && ((SourceConstruct) target).getOwner() != null) {
      project = ((SourceConstruct) target).getOwner().getProject();
    } else {
      new Exception("Unhandled target: "
          + (target == null ? null : target.getClass()))
          .printStackTrace(System.err);
      project = null;
    }

    if (project == null) {
      AppRegistry.getLogger(Project.class).debug(
          "no project for: " + bin + ", "
          + (bin == null ? null : Integer.toHexString(bin.hashCode())));
    }

    return project;
  }

  /**
   * This is to be called when user selects 'clean' from the menu
   */
  public synchronized void clean() {
    cleanClassmodel();
    getProjectLoader().getRebuildLogic().markUnsuccessfulEndBuild();
    getPaths().setSourcePathChanged(true);
    getPaths().setClassPathChanged(true);
    getProjectLoader().loadingCompleted = false;

    getProjectLoader().getAstTreeCache().cleanAll();

    getProjectLoader().forgetAllLoadingErrors();

    getProjectLoader().fireRebuildStartedEvent();
  }

  public synchronized void cleanClassmodel() {
    // helps to keep alive even if some error appeared elsewhere leads to memory leak!!!
    if (createdPackages != null) {
      final Iterator packages = createdPackages.values().iterator();
      while (packages.hasNext()) {
        final BinPackage pack = (BinPackage) packages.next();
        pack.cleanUpForRebuild();
      }
    }

    createdPackages.clear();

    for (int i = 0, max = compilationUnits.size(); i < max; i++) {
      final CompilationUnit source = (CompilationUnit) compilationUnits.get(i);
      source.getSource().invalidateCaches();
    }
    SourceMap.invalidateSourceCaches();

    compilationUnits.clear();
    nonJavaUnits.clear();
    definedTypeNames.clear();
    loadedTypes.clear();
    javaLangPackage = createPackageForName("java.lang");
    defaultPackage = createPackageForName("");
    objectRef = null;
    arrayInterfaces = null;

    getProjectLoader().projectCleanup();
  }


  public synchronized void discoverAllUsedTypes() {
    discoverAllUsedTypes(null);
  }

  /**
   * Classes from the classpath are lazyloaded most of the time, so if they are
   * used e.g. inside the method body only or as superclass of a binary class
   * used, they will be added to the corresponding package only after full visit
   * and after recursive resolving of all supertypes.<br>
   * @param listener progress listener or null if listener is not used.
   */
  private synchronized void discoverAllUsedTypes(final ProgressListener listener) {
    // FIXME: implement ProgressListener using

    if (!getProjectLoader().loadingCompleted) {
      return;
    }

    if (this.allTypesDiscovered) {
      return;
    }

    //System.err.println("loaded types before: " + loadedTypes.size());

    // visit sources
    accept(new AbstractIndexer());

    // Switched off - doesn't help a lot, but slows down too much
    //discoverAllBinaryClasses(visitor);

    //System.err.println("loaded types after: " + loadedTypes.size());

    this.allTypesDiscovered = true;
    getPaths().getClassPath().release();
  }

  public void discoverAllBinaryClasses() {
    AbstractIndexer visitor = new AbstractIndexer();

    final HashSet visited = new HashSet(4096);
    while (true) {
      final List types = new ArrayList(loadedTypes.values());
      boolean addedMore = false;
      for (int i = 0, max = types.size(); i < max; i++) {
        final BinTypeRef typeRef = (BinTypeRef) types.get(i);
        if (visited.contains(typeRef)) {
          continue;
        }
        if (!typeRef.getBinCIType().isFromCompilationUnit()) {
          typeRef.getBinCIType().accept(visitor);
          visited.add(typeRef);
          addedMore = true;
        }
      }
      if (!addedMore) {
        break;
      }
    }
  }

  public void accept(final BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return list of {@link BinTypeRef types} having sources.
   */
  public List getDefinedTypes() {
    final ArrayList result = new ArrayList(this.definedTypeNames.size());
    for (int i = 0, max = compilationUnits.size(); i < max; ++i) {
      final CompilationUnit aCompilationUnit
          = (CompilationUnit) compilationUnits.get(i);
      final List definedTypes = aCompilationUnit.getDefinedTypes();
      result.addAll(definedTypes);
    }
    return result;
  }

  /**
   * Returns source file list - since it is used so often it does not clone it
   * but returns working copy.
   */
  public ArrayList getCompilationUnits() {
    // NOTE: don't clone, caller uses remove/clear
    return compilationUnits;
  }

  /**
   * @return <code>null</code> if not found
   */
  public CompilationUnit getCompilationUnitForName(final String relativePath) {
    /*List compilationUnits = getCompilationUnitList();
         for( int i = 0; i < compilationUnits.size(); i++ ) {
      final CompilationUnit compilationUnit = ( CompilationUnit ) compilationUnits.get(i);
      if( compilationUnit.getRelativePath().equals( relativePath ) ) {
        return compilationUnit;
      }
         }

         return null;*/

    return compilationUnitForNameFinder.getCompilationUnitForName(relativePath);
  }

  //package methods for utility classes.
  public HashSet getDefinedTypeNames() {
    // NOTE: don't clone, callers perform remove/clear operations
    return definedTypeNames;
  }

  public void addCompilationUnit(final CompilationUnit compilationUnit) {
    compilationUnits.add(compilationUnit);
  }

  public void addLoadedType(final String name, final BinTypeRef typeRef) {
    loadedTypes.put(name, typeRef);
  }

  /**
   * Workaround method for getTypeRefForName 'feature' of requiring '$' as inner
   * type separator
   */
  public BinTypeRef getTypeRefForSourceName(final String qualifiedName) {
    final BinTypeRef retVal = getTypeRefForName(qualifiedName);

    if (retVal != null) {
      return retVal;
    }

    final int pos = qualifiedName.lastIndexOf('.');
    if (pos == -1) {
      return null;
    }

    final String tmpName = qualifiedName.substring(0, pos) + '$'
        + qualifiedName.substring(pos + 1);

    return getTypeRefForSourceName(tmpName);
  }

  /*
   *	Gets reference for current type.
   * FIXME: Quite strange feature - requires inner types to be referenced with $
   * like mypackage.MyClass$MyInner
   */
  public BinTypeRef getTypeRefForName(final String qName) {
    // RFC - types resolving for workspaces:
    // 1. find own defined source type
    // 2. find from dependant projects defined source type
    // 3. find from own already loaded binary types
    // 4. find from dependant projects already loaded binary types
    // 5. try to load binary types from own classpath
    // 6. try to load binary types from dependant projects classpath

    BinTypeRef foundType = findTypeRefForName(qName); // is that type declared in source...

    if (foundType != null) {
      if (classLoaderEntries == 0 && !foundType.isResolved()) {
        classLoaderEntries++;
        try {
          getProjectLoader().getClassLoader().findTypeForQualifiedName(qName);
        } catch (Exception e) {
          //e.printStackTrace(System.err);
        } finally {
          classLoaderEntries--;
        }

        foundType = findTypeRefForName(qName);
      }
    } else {
      if (classLoaderEntries == 0) {
        // we are getting a real classloaded BinTypeRef
        classLoaderEntries++;
        try {
          getProjectLoader().getClassLoader().findTypeForQualifiedName(qName);
        } catch (Exception e) {
          //e.printStackTrace(System.err);
        } finally {
          classLoaderEntries--;
        }
        foundType = findTypeRefForName(qName);
      } else {
        if (!getProjectLoader().getClassLoader().existsTypeForQualifiedName(qName)) {
          foundType = null;
        } else {
          foundType = new BinCITypeRef(qName, getProjectLoader().getClassLoader());
          addLoadedType(qName, foundType);
        }
      }
    }

    return foundType;
  }

//  private BinTypeRef findTypeRefFromDependent(final String qName) {
//    String projectIds[] = getReferencedProjects();
//    BinTypeRef result=null;
//    for (int i = 0; i < projectIds.length; i++) {
//      Project project = getWorkspace().getProject(projectIds[i]);
//      if ( project != null ) {
//        result=project.findTypeRefForName(qName);
//        if (result != null ) {
//          break;
//        }
//      } else {
//        AppRegistry.getLogger(Project.class).warn("project "+projectIds[i]+" not found in workspace");
//
//      }
//    }
//
//    return result;
//  }

  private Project[] getLinkedProjects() {
    ArrayList result = new ArrayList(3);
    Object id = getWorkspace().getWorkspaceManager().getProjectIdentifier(this);
    CollectionUtil.addAllNew(result,
        getWorkspace().getWorkspaceManager().getDependsOnProjects(id));
    CollectionUtil.addAllNew(result,
        getWorkspace().getWorkspaceManager().getReferencedInProjects(id));
    return (Project[]) result.toArray(new Project[result.size()]);
  }

  public List getAllLinkedProjects(HashSet added) {
    ArrayList result = new ArrayList(3);
    if (added.contains(this)) {
      return result;
    }
    added.add(this);
    result.add(this);

    Object id = null;
    try {
      id = getWorkspace().getWorkspaceManager().getProjectIdentifier(this);
    } catch (NullPointerException e) {
      // ignore - tests
    }

    if (id != null) {
      Iterator prs = getWorkspace().getWorkspaceManager()
          .getDependsOnProjects(id).iterator();
      while (prs.hasNext()) {
        CollectionUtil.addAllNew(result,
            ((Project) prs.next()).getAllLinkedProjects(added));
      }

      prs = getWorkspace().getWorkspaceManager()
          .getReferencedInProjects(id).iterator();
      while (prs.hasNext()) {
        CollectionUtil.addAllNew(result,
            ((Project) prs.next()).getAllLinkedProjects(added));
      }
    }

    return result;
  }

  public static SourcePath[] collectSourcePaths(List projects) {
    ArrayList result = new ArrayList(3);
    for (int i = 0, max = projects.size(); i < max; i++) {
      Project project = (Project) projects.get(i);
      SourcePath sp = project.getPaths().getSourcePath();
      if (sp instanceof CompoundSourcePath) {
        result.add(((CompoundSourcePath) sp).getPaths()[0]);
      } else {
        result.add(sp);
      }
    }

    return (SourcePath[]) result.toArray(new SourcePath[result.size()]);
  }

  public static ClassPath[] collectClassPaths(List projects) {
    ArrayList result = new ArrayList(3);
    for (int i = 0, max = projects.size(); i < max; i++) {
      Project project = (Project) projects.get(i);
      ClassPath cp = project.getPaths().getClassPath();
      if (cp instanceof CompoundClassPath) {
        result.add(((CompoundClassPath) cp).getPaths()[0]);
      } else {
        result.add(cp);
      }
    }

    return (ClassPath[]) result.toArray(new ClassPath[result.size()]);
  }

  public static BinTypeRef findPrimitiveTypeForName(final String qName) {
    return (BinTypeRef) primitiveTypes.get(qName);
  }

  /**
   *	Searches a typedef from already loaded typedefs
   */
  public BinTypeRef findTypeRefForName(final String qName) {
    BinTypeRef foundType = (BinTypeRef) loadedTypes.get(qName);

    if (foundType == null) {
      foundType = findPrimitiveTypeForName(qName);
    }

    return foundType;
  }

  /**
   * Gets all loaded types.
   *
   * @return types ({@link BinTypeRef} instances).
   *         Never returns <code>null</code>.
   */
  public Iterator getLoadedTypes() {
    final List result = new ArrayList(loadedTypes.values());
    return result.iterator();
  }

  /**
   * This is for these types that are defined within method
   */
  public BinTypeRef createLocalTypeRefForType(final BinCIType type) {
    //System.err.println("Creating Local typeref:" + type);
    final BinTypeRef newTypeRef = new BinCITypeRef(type);
    type.setProject(this);

    return newTypeRef;
  }

  public BinTypeRef createCITypeRefForName(final String FQN,
      final ClassFilesLoader loader) {
    BinTypeRef newTypeRef =  findTypeRefForName(FQN);

    if (newTypeRef == null) {
      newTypeRef = new BinCITypeRef(FQN, loader);
      loadedTypes.put(FQN, newTypeRef);
    }

    return newTypeRef;
  }

  public BinTypeRef createCITypeRefForType(final BinCIType type) {
    BinTypeRef newTypeRef = findTypeRefForName(type.getQualifiedName());

    if (newTypeRef == null) {
      newTypeRef = new BinCITypeRef(type);
      loadedTypes.put(type.getQualifiedName(), newTypeRef);
      type.setProject(this);
    } else {
      newTypeRef.setBinType(type);
      type.setTypeRef(newTypeRef);
    }
    return newTypeRef;
  }

  public BinPackage createPackageForName(final String name) {
    return createPackageForName(name, false);
  }

  /**
   * Searches the list of already created packages and returns matching.

   * Creates new one when no such package was found.

   */

  public BinPackage createPackageForName(final String name,
      final boolean fromSource) {
    BinPackage retVal = (BinPackage) createdPackages.get(name);
    if (retVal == null) {
      retVal = new BinPackage(name, this, fromSource);
      createdPackages.put(name, retVal);
    }
    return retVal;
  }

  public boolean hasNonEmptyPackage(final String name) {
    BinPackage p = getPackageForName(name);
    return p != null && p.getCompilationUnitList().size() > 0;
  }

  /**
   * Searches the list of already created packages and returns matching.
   */
  public BinPackage getPackageForName(final String name) {
    return (BinPackage) createdPackages.get(name);
  }

  public BinPackage[] getAllPackages() {
    return (BinPackage[]) createdPackages.values().toArray(new BinPackage[0]);
  }

  public void cleanEmptyPackages() {
    ArrayList removables = new ArrayList();

    ArrayList packs = new ArrayList(createdPackages.values());
    for (int i = 0, max = packs.size(); i < max; i++) {
      final BinPackage aPackage = (BinPackage) packs.get(i);
      String qualifiedName = aPackage.getQualifiedName();
      // NOTE: getTypesNumber() calls discover which may find missing class and create a new package; also binaries
      if (aPackage.getTypesNumber() == 0 && qualifiedName.length() > 0) {
        //aPackage.cleanUpForRebuild(); somehow causes NPE on second MoveType
        removables.add(qualifiedName);
      }
    }

    for (Iterator i = removables.iterator(); i.hasNext(); ) {
      createdPackages.remove(i.next());
    }
  }

  public BinTypeRef createArrayTypeForType(final BinTypeRef type,
      final int dimensions) {
    final String typeFQN = type.getQualifiedName();
    final char arrayTypeChar
        = BinArrayType.getPrimitiveArrayCharForName(typeFQN);

    final String arrayTypeNameS;
    final StringBuffer arrayTypeName = new StringBuffer();
    synchronized (arrayTypeName) {
      for (int q = 0; q < dimensions; q++) {
        arrayTypeName.append('[');
      }

      arrayTypeName.append(arrayTypeChar);

      if (arrayTypeChar == ClassUtil.OBJECT_IDENT) {
        arrayTypeName.append(typeFQN);
        arrayTypeName.append(';');
      }

      arrayTypeNameS = arrayTypeName.toString();
    }

    BinTypeRef retVal = findTypeRefForName(arrayTypeNameS);

    if (retVal == null) {
      final BinArrayType arrayType
          = new BinArrayType(arrayTypeNameS, type, dimensions, this);

      retVal = createCITypeRefForType(arrayType);

      arrayType.setOwners(retVal);

      retVal.setSuperclass(getObjectRef());
      retVal.setInterfaces(getArrayInterfaces());
    }

    return retVal;
  }

  public BinTypeRef getObjectRef() {
    return objectRef;
  }

  public BinPackage getDefaultPackage() {
    return defaultPackage;
  }

  public BinPackage getJavaLangPackage() {
    return javaLangPackage;
  }

  public BinTypeRef[] getArrayInterfaces() {
    if (arrayInterfaces == null) {
      List result = new ArrayList();
      result.add(getTypeRefForName("java.io.Serializable"));
      result.add(getTypeRefForName("java.lang.Cloneable"));

      CollectionUtil.removeNulls(result);

      arrayInterfaces = (BinTypeRef[]) result.toArray(
          new BinTypeRef[result.size()]);
    }

    return arrayInterfaces;
  }

  /**
   * Must work on all IDEs/plaforms, even when running automated test suites.
   * Default read-only options, used when setOptions wasn't called
   */
  public static ProjectOptions getDefaultOptions() {
    return DefaultReadOnlyProjectOptions.instance;
  }

  public Object getCachePath() {
    return cachePath;
  }

  public void setCachePath(final Object cachePath) {
    this.cachePath = cachePath;
  }

  public void defaultTraverse(final BinItemVisitor visitor) {
    ProgressListener listener = (ProgressListener)
        CFlowContext.get(ProgressListener.class.getName());

    List loadedSources = getCompilationUnits();

    for (int i = 0, max = loadedSources.size(); i < max; i++) {
      visitor.visit((CompilationUnit) loadedSources.get(i));

      if (listener != null) {
        ProgressMonitor.Progress progress = AbstractIndexer.getProgress();
        if (progress != null) {
          listener.progressHappened(progress.getPercentage(i,
              loadedSources.size()));
        }
      }
    }
  }

  /** @return null */
  public BinItemVisitable getParent() {
    return null;
  }

  public ProjectOptions getOptions() {
      return this.options;
  }

  public void setOptions(final ProjectOptions projectOptions) {
      this.options = projectOptions;
      FastJavaLexer.setJvmMode(this.options.getJvmMode());
  }

  public void addProjectSettingsListener(final ProjectSettingsListener listener) {
    projectSettingsListeners.add(listener);
  }

  public void fireProjectSettingsChangedEvent() {
    getProjectLoader().markProjectForRebuild();

    for (int i = 0; i < projectSettingsListeners.size(); ++i) {
      ProjectSettingsListener element
          = (ProjectSettingsListener) projectSettingsListeners.get(i);
      element.settingsChanged(getOptions());
    }
    getOptions().serialize();
  }

  public BinItemReference createReference(){
    return ProjectReference.createAdvancedReference(this);
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  public void setWorkspace(final Workspace workspace) {
    this.workspace = workspace;
  }

  public void setAllTypesDiscovered(boolean allTypesDiscovered) {
    this.allTypesDiscovered = allTypesDiscovered;
  }


  public RitUndoManager getRitUndoManager() {
      return this.ritUndoManager;
  }

  public void setRitUndoManager(final RitUndoManager ritUndoManager) {
      this.ritUndoManager = ritUndoManager;
  }

  public BinTypeRef[] getPrimitiveTypeRefs() {
    HashSet list = new HashSet(11);
    list.add(BinPrimitiveType.BOOLEAN_REF);
    list.add(BinPrimitiveType.BYTE_REF);
    list.add(BinPrimitiveType.CHAR_REF);
    list.add(BinPrimitiveType.DOUBLE_REF);
     list.add(BinPrimitiveType.FLOAT_REF);
    list.add(BinPrimitiveType.INT_REF);
    list.add(BinPrimitiveType.LONG_REF);
    list.add(BinPrimitiveType.SHORT_REF);
    list.add(BinPrimitiveType.VOID_REF);
    return (BinTypeRef[]) list.toArray(new BinTypeRef[list.size()]);
  }

  public CompilationUnit getNonJavaUnit(Source s) {
    if(s == null) {
      return null;
    }
    CompilationUnit cu = (CompilationUnit)nonJavaUnits.get(s);
    if(cu == null) {
      cu = new CompilationUnit(s, this);
      nonJavaUnits.put(s,cu);
    }
    return cu;
  }
}
