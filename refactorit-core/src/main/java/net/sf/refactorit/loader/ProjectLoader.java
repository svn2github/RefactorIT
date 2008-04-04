/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;



import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MissingBinClass;
import net.sf.refactorit.classmodel.MissingBinInterface;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.parser.OptimizedJavaRecognizer;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.source.BodyContext;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.ParsingMessageDialog;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.CompoundClassPath;
import net.sf.refactorit.vfs.CompoundSourcePath;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class ProjectLoader {
  private final Project project;

  private final ErrorCollector errorCollector = new ErrorCollector(this);

  static final float PASS1_TIME = 80;
  static final float PASS2_TIME = 17;
  static final float PASS3_TIME = 3;

  private RebuildLogic rbLogic = null;
  private final PrototypeManager prototypeManager = new PrototypeManager();

  /* The cache for AST trees between reloads */
  private ASTTreeCache astTreeCache = new ASTTreeCache();

  /* The component responsible for mastering our VFS-sourcepath  */
  private final CompilationUnitsLoader sourceLoader
      = new CompilationUnitsLoader(this);
  private final TypeDefLoader typesLoader = new TypeDefLoader(this);

  private MethodBodyLoader methodBodyLoader = new MethodBodyLoader();

  /* The component rresponsible for mastering our VFS-classpath */
  private ClassFilesLoader classLoader = null;

  private ClassFilesLoaderFactory classFilesLoaderFactory
      = new ClassFilesLoaderFactory();

  boolean parsingCanceledLastTime = false;

  public boolean loadingCompleted = false;

  private boolean forceProjectCleanup = false;
  private boolean forceNonIncremental = false;

  private boolean firstTimeLoad = true;

  private final FastStack activeProfilingTimers = new FastStack();
  private static final boolean profilingOnLoadingEnabled =
      "true".equals(GlobalOptions.getOption("debug.profileOnLoading", "false"));

  private boolean printingToStdOut = false;

  private final String debugFileName = System.getProperty("user.home")
      + File.separator + "refactorit.profile.txt";

  // Contains the ProjectChangedListener objects. These objects are added to
  // this vector with addProjectChangedListener() method.
  private final Vector projectChangedListeners = new Vector(10);

  private static final Logger log = AppRegistry.getLogger(ProjectLoader.class);

  /** List of WeakReferences to all fields and methods ever created to help finding leaks */
  private static final List createdItems = new ArrayList();

  public static final boolean checkIntegrityAfterLoad = false;
  private static boolean lowMemoryMode = false;
//      = "true".equals(GlobalOptions.getOption("debug.checkIntegrityAfterLoad", "false"));

  private PrintWriter profilingOutputFile = null;

  public ProjectLoader(final Project project) {
    this.project = project;
    this.rbLogic = new RebuildLogic(project);

    this.classLoader = classFilesLoaderFactory.createFor(project);
  }

  public final Project getProject() {
    return this.project;
  }

  public final ErrorCollector getErrorCollector() {
    return this.errorCollector;
  }

  /**
   * @return true if parsing was canceled last time
   *
   *   */
  public boolean isParsingCanceledLastTime() {
    return parsingCanceledLastTime;
  }

  public boolean isLoadingCompleted() {
    return loadingCompleted;
  }

  private synchronized void load(final ProgressListener listener)
      throws Exception {

    try {
      ProgressListener listener1 = listener;
      boolean success = false;

      try {
        // set an empty listener
        if (listener1 == null) {
          listener1 = ProgressListener.SILENT_LISTENER;
        }

        // if we come from rebuild, then all clearings have already been made
        // if not, then there is nothing to clear
        int jvmMode = getProject().getOptions().getJvmMode();
        if (jvmMode != FastJavaLexer.getJvmMode()) {
          FastJavaLexer.setJvmMode(jvmMode);
        }

        getProject().getPaths().checkClasspathForChanges();

        // if classpath or sourcepath has been changed, then clean the project.
        // i.e. set project into initial state (release allocated memory, ...)
        if (getProject().getPaths().hasClassPathChanged()
            || getProject().getPaths().hasSourcePathChanged()) {
          projectCleanup();
          getProject().getPaths().setClassPathChanged(false);
          getProject().getPaths().setSourcePathChanged(false);
        }
        // too late, too late
        //this.cancelParsing = false;

        getProject().getProjectLoader().getErrorCollector().forgetAllLoadingErrors();

        startProfilingTimer("Loading project");

        if (Assert.enabled) {
          BodyContext.startCollectingCreatedInstances();
        }

        Finder.clearInvocationMap();
        BinSourceConstruct.clearCompoundsCache();
        // start to load the project sources and classes

        // it will forbid visiting the classmodel in discoverAllUsedTypes()
        // while we are still building it
        getProject().setAllTypesDiscovered(true);

//      long now = 0, last = 0;
//      last = System.currentTimeMillis();
//      log.debug("RefactorIT: Loading type definitions...");

        startProfilingTimer("getting java.lang.Object reference");
        // If you remove this then it gets null pointer exception
        getProject().objectRef = getProject().getTypeRefForName(Project.OBJECT);
        stopProfilingTimer();

        List compilationUnitList;

        final Collection sourcesToRebuild = rbLogic.getSourceListToRebuild();

        // FIXME: hack
        sourceLoader.clearJspCache(sourcesToRebuild);

        if (listener1 instanceof ParsingMessageDialog.ParsingProgressListener) {
          ((ParsingMessageDialog.ParsingProgressListener) listener1).beforeRebuild(getProject());
        }
        compilationUnitList = getTypesLoader()
            .loadTypeDefsProfiling(listener1, sourcesToRebuild);

        rbLogic.fixSubtypes();
        CancelSupport.checkThreadInterrupted();

//      now = System.currentTimeMillis();
//      log.debug(" " + (now - last) + "ms");
//      last = now;
        //if(last != 0) return;
//      log.debug("RefactorIT: Building types...");
        sourceLoader.buildTypes(listener1, compilationUnitList);

        // now we need to set new BinTypeRefs to BinArrayTypes, whose base types
        // was rebuilded
        refreshArrayTypes(rbLogic.getTypesToBeRefreshed());

        //sourceLoader.addJspDependencies(compilationUnitList);

//      now = System.currentTimeMillis();
//      log.debug(" " + (now - last) + "ms");
//      last = now;
//      log.debug("RefactorIT: Building method bodies...");
        CancelSupport.checkThreadInterrupted();

        // N.B! Ensure implied members has to be done before buildFieldsAndMethodBodys
        // [SANDER]

        //if (!isParsingCanceled()) {
        CompilationUnitsLoader.ensureImpliedMembers(compilationUnitList);
        //}

        // N.B: Comment this in if you want to test without lazyloading
        //sourceLoader.buildFieldsAndMethodBodys(listener, compilationUnitList);

        //if ( !isParsingCanceled() ) {
//      now = System.currentTimeMillis();
//      log.debug(" " + (now - last) + "ms");
//      last = now;
        //}
        CancelSupport.checkThreadInterrupted();

        // NOTE: don't cause discovering all because allTypesDiscovered is still true
        getProject().cleanEmptyPackages();

        // if we are here then parsing succeed
        success = true;
      } catch (Exception e) {
        // FIXME: Remove this error catch later
        // (because it's added for debugging purposes only)
  e.printStackTrace();

        // sometimes stackTrace is missing?
        AppRegistry.getExceptionLogger().debug(e, "Project load caught exception",
            Project.class);

        if (e instanceof RuntimeException) {
          log.warn("Project load crashed with:", e);
        }

        getProject().getProjectLoader().getErrorCollector().addUserFriendlyError(new UserFriendlyError(
            "Internal crash: " + e.getMessage(), null));
      } finally {
        forceProjectCleanup = false;
        forceNonIncremental = false;

        prototypeManager.clear();
        doMemoryReleases();

        if (!success) {
          parsingCanceledLastTime = true;
          loadingCompleted = false;
          rbLogic.markUnsuccessfulEndBuild();
          projectCleanup();
        } else {
          parsingCanceledLastTime = false;
          loadingCompleted = true;
          getProject().setAllTypesDiscovered(false);

          success = checkProjectIntegrity();
          if (success) {
            rbLogic.markSuccessfulEndBuild();
          } else {
            rbLogic.markUnsuccessfulEndBuild();
          }
        }
//      System.out.println("tonisdebug: finishing load, parsingCanceledLastTime="+parsingCanceledLastTime);

        // fire rebuild performed event to notify all ProjectChangedListener's about this event.
        fireRebuildPerformedEvent();

        if (parsingCanceledLastTime) {
          log.debug("Parsing canceled");
        }

//      if (criticalErrorsCount > MAX_CRITICAL_ERRORS) {
//        // sometimes RIT has mystical errors and only cleanup helps
//        // lets be userfriendly ;) [tonis]
//        if (RefactorItConstants.debugInfo) {
//          DebugInfo.trace(
//              "Project has too many critical errors");
//        }
//
//      }
        if (isParsingCanceledLastTime()) {
          AppRegistry.getLogger(getClass()).debug(
              "Project parsing was not completed, marking project for rebuild");

          markProjectForRebuild();
        }

        stopProfilingTimer();
        if (!activeProfilingTimers.isEmpty()) {
          startProfilingTimer(
              "********* WARNING: NOT ALL PROFILING TIMERS WERE FINISHED ********* ");
          stopProfilingTimer();
        }
      }

      //println("complete - " + loadedTypes.size() + " loaded classes");
      //System.err.println("complete - " + loadedTypes.size() + " loaded classes");

      if (Assert.enabled) {
        BodyContext.assertAllCreatedInstancesAreEmptied();
      }

//    int[] toks = OptimizedJavaRecognizer.TOKENS;
//    int total = 0;
//    for (int i = 0; i < toks.length; i++) {
//      total += toks[i];
//    }
//    int cur = 0;
//    int last = 0;
//    for (int i = 0; i < toks.length; i++) {
//      if (toks[i] > 0) {
//        System.err.print(i + " - " + toks[i] + ", ");
//      }
//      cur += toks[i];
//      if (i % 500 == 0 && i > 499 && last != cur) {
//        System.err.println("\n" + i + " -- " + ((((double)cur) / ((double)total)) * 100) + "%");
//        last = cur;
//      }
//    }
    } catch (CanceledException ex) {
      //println("");
      //println("RefactorIT: Parsing canceled!");

      if (RefactorItConstants.debugInfo) {
        AppRegistry.getLogger(this.getClass()).debug(
            "CanceledException caught in Project.load[OK]");
      }
    }
  }

  public synchronized void build() throws Exception {
    build(null, true);
  }

  /**
   * This is to be called with forceFullBuild=true from 'Rebuild' menu item at IDE-s
   */
  public synchronized void build(
      final ProgressListener listener,
      final boolean forceFullBuild) throws Exception {
    try {
      getProject().getPaths().getClassPath().release();

//    final long starts = System.currentTimeMillis();

      this.forceNonIncremental = this.forceNonIncremental || forceFullBuild;
      final int jvmMode = getProject().getOptions().getJvmMode();
      if (jvmMode != FastJavaLexer.getJvmMode()) {
        FastJavaLexer.setJvmMode(jvmMode);
        this.forceProjectCleanup = true;
      }

      if (this.forceProjectCleanup) {
        rbLogic.forceFullRebuild();
      } else if (this.forceNonIncremental) {
        rbLogic.forceNonIncremental();
      }

//    print("RefactorIT: calculating for incremental rebuild ");

      if (firstTimeLoad) {
        List linked = project.getAllLinkedProjects(new HashSet(5));
        if (linked.size() > 1
            && !(project.getPaths().getSourcePath() instanceof CompoundSourcePath)) {
          project.getPaths().setSourcePath(
              new CompoundSourcePath(Project.collectSourcePaths(linked)));
          project.getPaths().setClassPath(
              new CompoundClassPath(Project.collectClassPaths(linked)));
        }

        rbLogic.startingInitialLoad();
        rbLogic.calculateSourcepathChanges(useIDEFileEvents());
        rbLogic.analyzeChanges();
        firstTimeLoad = false;
      } else {
        rbLogic.startingRebuild();

        getProject().getPaths().checkClasspathForChanges();

        // if sources nor classes used by project has been changed then just
        // return, i.e. project has not changed.
        boolean sourcesChanged = false;

        sourcesChanged = rbLogic.calculateSourcepathChanges(useIDEFileEvents());

        if (!rbLogic.isForcedFullRebuild()) {
          if (!getProject().getPaths().hasSourcePathChanged()
              && !getProject().getPaths().hasClassPathChanged()
              && !sourcesChanged) {
//          final long now = System.currentTimeMillis();
//          println(" " + (now - starts)+" ms.");
//        DebugInfo.trace(
//            "rebuildProject(): No sourcesChanged discovered, exiting without rebuild");

            return;
          }
        }

        rbLogic.analyzeChanges();

        // fire rebuild started event to notify all ProjectChangedListeners about this event.
        fireRebuildStartedEvent();

        if (getProject().getPaths().hasClassPathChanged() || rbLogic.isForcedFullRebuild()) {
          this.classLoader = classFilesLoaderFactory.createFor(getProject());

          if (!rbLogic.isForcedFullRebuild()) {
            rbLogic.forceNonIncremental();
          }
          getProject().getPaths().setClassPathChanged(false);
        }

        getProject().classLoaderEntries = 0;

        try {
          rbLogic.cleanRebuildableSources();
        } catch (Exception t) {
          AppRegistry.getExceptionLogger().error(t,this);
        }
      }

      load(listener);
    } catch (SourceParsingException ex) {
      AppRegistry.getExceptionLogger().error(ex,this);
    }
  }

  public boolean sourcepathOrClasspathHaveChanges(){
    getProject().getPaths().checkClasspathForChanges();
    boolean sourcesChanged = rbLogic.calculateSourcepathChanges(
        useIDEFileEvents());

    return getProject().getPaths().hasSourcePathChanged()
        || getProject().getPaths().hasClassPathChanged()
        || sourcesChanged;
  }

//  public boolean rebuildStatusIsKnown() {
//    if (useIDEFileEvents() != rbLogic.getUseIDEEvents()) {
//      rbLogic.setUseIDEEvents(useIDEFileEvents());
//      return false;
//    }
//
//    return rbLogic.rebuildStatusIsKnown();
//  }

//  public boolean minimalRebuildPossible(final int changedThreshhold) {
//    return rbLogic.minimalRebuildPossible(changedThreshhold);
//  }

  private boolean useIDEFileEvents() {
    return false;

    // Risto: commented this out because IDE Events did not seem to work under my NB 3.5

    //final boolean useIDEFileEvents = "true".equals(Main.getOption("performance.rebuild.use-ide-events"));
    //return useIDEFileEvents;
  }

  public boolean isIncrementalRebuild() {
    return "true".equals(GlobalOptions.getOption("performance.incremental.compiling", "true"));
  }

  /*
   * @pre cache!=null
   */
  public void setAstTreeCache(final ASTTreeCache cache) {
    this.astTreeCache = cache;
  }

  public void validateAstTreeCache() {
    astTreeCache.removeNonExistingSources(getProject().getPaths().getSourcePath().getAllSources());
  }

  public ASTTreeCache getAstTreeCache() {
    return this.astTreeCache;
  }

  public RebuildLogic getRebuildLogic() {
    return rbLogic;
  }

  public PrototypeManager getPrototypeManager() {
    return prototypeManager;
  }

  public CompilationUnitsLoader getSourceLoader() {
    return this.sourceLoader;
  }

  public TypeDefLoader getTypesLoader() {
    return this.typesLoader;
  }

  public MethodBodyLoader getMethodBodyLoader() {
    return this.methodBodyLoader;
  }

  public ClassFilesLoader getClassLoader() {
    return this.classLoader;
  }

  public void projectCleanup() {
    this.classLoader = classFilesLoaderFactory.createFor(getProject());
  }

  public void setClassFilesLoaderFactory(ClassFilesLoaderFactory f) {
    this.classFilesLoaderFactory = f;
  }

  public void markProjectForCleanup() {
    forceProjectCleanup = true;
//    rbLogic.forceFullRebuild();
  }

  /**
   * marks project as needing rebuild
   */
  public final void markProjectForRebuild() {
    forceNonIncremental = true;
//    rbLogic.forceNonIncremental();
  }

  public final void startProfilingTimer(final String activityName) {
    if (profilingOnLoadingEnabled) {
      if (activeProfilingTimers.size() == 0) {
        try {
          profilingOutputFile = new PrintWriter(new BufferedWriter(new FileWriter(
              debugFileName, true)));
          printingToStdOut = false;
        } catch (IOException e) {
          System.out.println("UNABLE TO CREATE PRINT WRITER TO: "
              + debugFileName);
          System.out.println("PRINTING TO SYSTEM OUT INSTEAD");
          System.out.println();
          profilingOutputFile = new PrintWriter(System.out);
          printingToStdOut = true;
        }

        getProject().getPaths().printSourcePathAndClassPath(profilingOutputFile);
      }

      profilingOutputFile.println(getProfileOutputIdent(activeProfilingTimers.size())
          + activityName + " (");
      activeProfilingTimers.push(new Long(System.currentTimeMillis()));
    }
  }

  public final void stopProfilingTimer() {
    if (profilingOnLoadingEnabled) {
      final long stopTime = System.currentTimeMillis();
      final long duration = stopTime
          - ((Long) activeProfilingTimers.pop()).longValue();
      profilingOutputFile.println(getProfileOutputIdent(activeProfilingTimers.size())
          + ") duration: " + duration + " ms");

      if (activeProfilingTimers.size() == 0) {
        if (!printingToStdOut) {
          profilingOutputFile.close();
        }
      }
    }
  }

  private static String getProfileOutputIdent(final int depth) {
    final StringBuffer result = new StringBuffer();
    for (int i = 0; i < depth; i++) {
      result.append("  ");

    }
    return result.toString();
  }

  /**
   * Sets new BinTypeRef for array (for those arrays, whose base type was
   * removed in order to be rebuild)
   *
   * @param typesToBeRefreshed set of array type-refs that need to be refreshed
   */
  private void refreshArrayTypes(final Set typesToBeRefreshed) {
    for (Iterator refreshTypes = typesToBeRefreshed.iterator();
        refreshTypes.hasNext(); ) {

      BinTypeRef typeRef = (BinTypeRef) refreshTypes.next();

      if (!typeRef.isResolved()) { // nothing to update
        if (Assert.enabled) {
          System.err.println("Project.refreshArrayTypes - type already not resolved: "
              + typeRef.getQualifiedName());
        }
        getProject().loadedTypes.remove(typeRef.getQualifiedName());
        continue;
      }

      if (typeRef.isArray()) {
        BinArrayType arrayType = (BinArrayType) typeRef.getBinType();

        BinTypeRef newRef
            = getProject().getTypeRefForName(arrayType.getArrayType().getQualifiedName());

        if (newRef != null){
          arrayType.setArrayType(newRef);
        } else {
          //System.out.println("Didn`t found new BinTypeRef for BinArrayType type");
          getProject().loadedTypes.remove(typeRef.getQualifiedName());
        }
      } else {
        //System.out.println("Type to be refreshed is not array type");
        getProject().loadedTypes.remove(typeRef.getQualifiedName());
      }
    }
  }

  private boolean checkProjectIntegrity() {
    if (!checkIntegrityAfterLoad) {
      return true;
    }

    System.err.println("Started checking project integrity...");

    final List problems = new ArrayList();

    getProject().discoverAllUsedTypes(); // so that it does find all possible types

    final AbstractIndexer visitor = new AbstractIndexer() {
      public void visit(final CompilationUnit source) {
        source.visit(this); // visits javadocs also
        super.visit(source);
      }

      public void visit(final BinConstructor x) {
        if (problems.size() < 20) {
          checkMember(x);
          super.visit(x);
        }
      }

      public void visit(final BinField x) {
        if (problems.size() < 20) {
          checkMember(x);
          super.visit(x);
        }
      }

      public void visit(final BinMethod x) {
        if (problems.size() < 20) {
          checkMember(x);
          super.visit(x);
        }
      }

      private void checkMember(final BinMember member) {
        if (member.getOwner() == null) {
          try { // let's try to get full name
            CollectionUtil.addNew(problems, "member has no owner: "
                + member.getQualifiedName());
          } catch (NullPointerException e) {
            CollectionUtil.addNew(problems, "member has no owner: "
                + member.getName());
          }
        }
      }
    };
    getProject().accept(visitor);

    if (problems.size() < 20) {
      final Iterator types = getProject().loadedTypes.entrySet().iterator();
      while (types.hasNext()) {
        if (problems.size() >= 20) {
          problems.add("too many errors...");
          break;
        }

        final Map.Entry entry = (Map.Entry) types.next();
        final String name = (String) entry.getKey();
        if (name == null) {
          CollectionUtil.addNew(problems, "name is null");
          continue;
        }
        final BinTypeRef ref = (BinTypeRef) entry.getValue();
        if (ref == null) {
          CollectionUtil.addNew(problems, "no reference for name " + name);
          continue;
        }
        BinType type = null;
        try {
          type = ref.getBinType();
          if (type == null || type instanceof MissingBinClass
              || type instanceof MissingBinInterface) {
            if (!ref.getQualifiedName().startsWith("com.borland.")
                && !ref.getQualifiedName().startsWith("oracle.")) {
              CollectionUtil.addNew(problems, "no type info for name " + name);
            }
            continue;
          }
        } catch (Exception e) {
          e.printStackTrace(System.err);
          CollectionUtil.addNew(problems,
              "failed to get type info for name " + name);
          continue;
        }

        if (ref.isReferenceType() && !type.isArray()) {
          try {
            if (ref.getResolver() == null) {
              CollectionUtil.addNew(problems, "no resolver for name " + name);
            }
          } catch (Exception e) {
            e.printStackTrace(System.err);
            CollectionUtil.addNew(problems,
                "failed to get resolver for name " + name);
            continue;
          }
        }

        if (type instanceof BinCIType) {
          boolean cont = false;

          try {
            final BinField[] fields = ((BinCIType) type).getDeclaredFields();
            if (fields == null) {
              CollectionUtil.addNew(problems, "no fields for type " + name);
              cont = true;
            } else {
              for (int i = 0; i < fields.length; i++) {
                if (fields[i].getOwner() == null) {
                  try { // let's try to get full name
                    CollectionUtil.addNew(problems, "field has no owner: "
                        + fields[i].getQualifiedName());
                  } catch (NullPointerException e) {
                    CollectionUtil.addNew(problems, "field has no owner: "
                        + fields[i].getName());
                  }
                  cont = true;
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace(System.err);
            CollectionUtil.addNew(problems, "failed to get fields for type " + name);
            cont = true;
          }

          try {
            final BinMethod[] methods = ((BinCIType) type).getDeclaredMethods();
            if (methods == null) {
              CollectionUtil.addNew(problems, "no methods for type " + name);
              cont = true;
            } else {
              for (int i = 0; i < methods.length; i++) {
                if (methods[i].getOwner() == null) {
                  try { // let's try to get full name
                    CollectionUtil.addNew(problems, "method has no owner: "
                        + methods[i].getQualifiedName());
                  } catch (NullPointerException e) {
                    CollectionUtil.addNew(problems, "method has no owner: "
                        + methods[i].getName());
                  }
                  cont = true;
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace(System.err);
            CollectionUtil.addNew(problems,
                "failed to get methods for type " + name);
            cont = true;
          }

          if (type instanceof BinClass) {
            try {
              final BinConstructor[] cnstrs = ((BinClass) type).
                  getDeclaredConstructors();
              if (cnstrs == null) {
                CollectionUtil.addNew(problems, "no constructors for type " + name);
                cont = true;
              } else {
                for (int i = 0; i < cnstrs.length; i++) {
                  if (cnstrs[i].getOwner() == null) {
                    try { // let's try to get full name
                      CollectionUtil.addNew(problems, "constructor has no owner: "
                          + cnstrs[i].getQualifiedName());
                    } catch (NullPointerException e) {
                      CollectionUtil.addNew(problems, "constructor has no owner: "
                          + cnstrs[i].getName());
                    }
                    cont = true;
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace(System.err);
              CollectionUtil.addNew(problems,
                  "failed to get constructors for type "
                  + name);
              cont = true;
            }
          }

          if (cont) {
            continue;
          }
        }
      }
    }

    // FIXME: move UI stuff ot from here!!!
    if (problems.size() > 0) {
      DialogManager.getInstance().showCustomError(
          IDEController.getInstance().createProjectContext(),
          "Project integrity broken",
          StringUtil.mergeArrayIntoString(
              problems.toArray(new Object[problems.size()]), "\n"));

      System.err.println("Project integrity check failed with:\n"
          + StringUtil.mergeArrayIntoString(
              problems.toArray(new Object[problems.size()]), "\n"));
    }

    System.err.println("Finished checking integrity!");

    return problems.size() == 0;
  }

//  private void checkForDuplicates() {
//    if (!checkIntegrityAfterLoad) {
//      return;
//    }
//
////    final Thread thread = new Thread(new Runnable() {
////      public void run() {
//        System.err.println("Started checking for duplicates...");
//        final List problems = new ArrayList();
//
//        final Runtime runtime = Runtime.getRuntime();
//
//        int totalMemory = (int) (runtime.totalMemory() / 1024);
//        int usedMemory = totalMemory - (int) (runtime.freeMemory() / 1024);
//        System.err.println("before gc: " + usedMemory + "kB / " + totalMemory + "kB");
//
//        runtime.runFinalization();
//        runtime.gc();
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException ex) {}
//        runtime.gc();
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException ex) {}
//        runtime.gc();
//        try {
//          Thread.sleep(1000);
//        } catch (InterruptedException ex) {}
//
//        totalMemory = (int) (runtime.totalMemory() / 1024);
//        usedMemory = totalMemory - (int) (runtime.freeMemory() / 1024);
//        System.err.println("after gc: " + usedMemory + "kB / " + totalMemory + "kB");
//
//        final Iterator members = createdItems.iterator();
//        final HashSet duplicateKeys = new HashSet();
//        final MultiValueMap groupedMembers = new MultiValueMap();
//        while (members.hasNext()) {
//          final WeakReference ref = (WeakReference) members.next();
//          final Object item = ref.get();
//          if (item == null) {
//            members.remove();
//            continue;
//          }
//          final String key;
//          if (item instanceof BinMethod) {
//            key = ((BinMethod) item).getQualifiedNameWithParamTypes();
//          } else if (item instanceof BinMember) {
//            key = ((BinMember) item).getQualifiedName();
//          } else if (item instanceof BinTypeRef) {
//            key = ((BinTypeRef) item).getQualifiedName();
//          } else if (item instanceof CompilationUnit) {
//            key = ((CompilationUnit) item).getSource().getAbsolutePath();
//          } else {
//            throw new RuntimeException("Unsupported item type: "
//                + item.getClass());
//          }
//          if (groupedMembers.containsKey(key)) {
//            duplicateKeys.add(key);
//          }
//          groupedMembers.putNew(key, item);
//        }
//
//        final Iterator forKeys = duplicateKeys.iterator();
//        while (forKeys.hasNext()) {
//          if (problems.size() >= 20) {
//            problems.add("too many errors...");
//            break;
//          }
//
//          final String name = (String) forKeys.next();
//          final List duplicateMembers = groupedMembers.get(name);
//          for (int i = 0, max = duplicateMembers.size(); i < max; i++) {
//            problems.add("duplicate member: "
//                + duplicateMembers.get(i).toString());
//          }
//        }
//
//        if (problems.size() > 0) {
//          System.err.println("Project duplicates check found:\n"
//              + StringUtil.mergeArrayIntoString(
//              problems.toArray(new Object[problems.size()]), "\n"));
////          DialogManager.getInstance()
////              .showCustomError(DialogManager.getDialogParent(),
////              "Project integrity broken",
////              StringUtil.mergeArrayIntoString(
////              problems.toArray(new Object[problems.size()]), "\n"));
//        }
//
//        System.err.println("Finished checking for duplicates!");
////      }
////    });
////    thread.start();
//  }

  /**
   * Goes throw all ProjectChangedListeners and notifies them that Rebuild
   * has been started.
   *
   * The listeners can register themselves with
   * addProjectChangedListener(...) function to get notified.
   */
  public void fireRebuildStartedEvent() {
    final Vector listeners = getProjectChangedListeners();
    final Enumeration enumer = listeners.elements();
    while (enumer.hasMoreElements()) {
      final ProjectChangedListener listener = (ProjectChangedListener) enumer.
          nextElement();
      listener.rebuildStarted(getProject());
    }
  }

  /**
   * Goes throw all ProjectChangedListeners and notifies them that Rebuild
   * has been performed.
   *
   * The listeners can register themselves with
   * addProjectChangedListener(...) function to get notified.
   */
  private void fireRebuildPerformedEvent() {
    final Vector listeners = getProjectChangedListeners();
    final Enumeration enumer = listeners.elements();
    while (enumer.hasMoreElements()) {
      final ProjectChangedListener listener = (ProjectChangedListener) enumer.
          nextElement();
      listener.rebuildPerformed(getProject());
    }
  }

  /**
   * Provides function for ProjectChangedListeners to register themselves
   * for notifications. For example for rebuild events, close events, ...
   *
   * @param listener to be registered on the list, so it could get notified
   * on project changed events.
   */
  public void addProjectChangedListener(final ProjectChangedListener listener) {
    if (listener != null) {
      final Vector listeners = getProjectChangedListeners();
      if (!listeners.contains(listener)) {
        listeners.add(listener);
      }
    }
  }

  /**
   * Removes the ProjectChangedListener from the list of listeners.
   *
   * After removing the listener from the list, it (listener) doesn't get notified
   * about project changed events.
   *
   * @param listener to be removed from the list.
   */
  public void removeProjectChangedListener(final ProjectChangedListener
      listener) {
    if (listener != null) {
      final Vector listeners = getProjectChangedListeners();
      listeners.remove(listener);
    }
  }

  /**
   * Returns the Vector holding ProjectChangedListener objects.
   *
   * @return Vector, containing ProjectChangedListener objects.
   */
  private Vector getProjectChangedListeners() {
    return this.projectChangedListeners;
  }

  public Vector cloneProjectChangedListeners() {
    return (Vector) projectChangedListeners.clone();
  }

  private static void doMemoryReleases() {
//System.err.println("totalAsts: " + ASTTree.totalAsts);
//System.err.println("excessAsts: " + ASTTree.excessiveAsts
//        + " - " + (((double) ASTTree.excessiveAsts) / ((double) ASTTree.totalAsts)));
//System.err.println("totalBytes: " + ASTTree.totalBytes);
//System.err.println("zeroBytes: " + ASTTree.zeroBytes
//        + " - " + (((double) ASTTree.zeroBytes) / ((double) ASTTree.totalBytes)));

    OptimizedJavaRecognizer.releaseMemory();
    FastJavaLexer.clear();
  }

  public static void registerCreatedItem(final Object item) {
    if (checkIntegrityAfterLoad) {
      createdItems.add(new WeakReference(item));
    }
  }

  public final void forceSourceModified(final Source source) {
    this.rbLogic.forceSourceModified(source);
  }

  public static boolean isLowMemoryMode() {
    return lowMemoryMode;
  }

  public static void setLowMemoryMode(final boolean lowMemoryMode) {
    ProjectLoader.lowMemoryMode = lowMemoryMode;
  }

  public void forgetAllLoadingErrors() {
    getErrorCollector().forgetAllLoadingErrors();
  }
}
